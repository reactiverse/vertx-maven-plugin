/*
 *
 *   Copyright (c) 2016-2018 Red Hat, Inc.
 *
 *   Red Hat licenses this file to you under the Apache License, version
 *   2.0 (the "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *   implied.  See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package io.reactiverse.vertx.maven.plugin.utils;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamPumper;
import org.jvnet.winp.WinProcess;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author kameshs
 */
public class JavaProcessExecutor {

    private List<String> argsList = new ArrayList<>();

    private Log logger;

    private boolean waitFor = true;

    private File workingDirectory;

    private Map<String, String> environment = new HashMap<>();

    protected List<String> jvmArgs;

    private final File java = findJava();
    private Collection<URL> classPathUrls;

    public Process execute() throws Exception {
        Commandline commandLine = buildCommandLine();

        Process process = null;
        try {
            logger.debug("Executing command :" + commandLine);
            process = commandLine.execute();
            Process reference = process;
            Thread watchdog = new Thread(() -> destroy(reference));

            Runtime.getRuntime().addShutdownHook(watchdog);

            if (waitFor) {
                redirect(process.getInputStream(), System.out);
                redirect(process.getErrorStream(), System.err);
                process.waitFor();
                if (!process.isAlive()) {
                    Runtime.getRuntime().removeShutdownHook(watchdog);
                }
            }

            return process;

        } catch (InterruptedException e) {
            destroy(process);
            // We sure the interrupt flag is restored.
            Thread.currentThread().interrupt();
            return process;
        } catch (Exception e) {
            destroy(process);
            throw new Exception("Error running java command : " + e.getMessage(), e);
        }

    }

    private void destroy(Process process) {
        if (process == null || !process.isAlive()) {
            return;
        }
        if (!SystemUtils.IS_OS_WINDOWS) {
            process.destroy();
        } else {
            // On Windows, destroying a cmd process does not destroy the children
            // So we need to send a Ctrl-C signal
            WinProcess winProcess = getWinProcess(process);
            if (!winProcess.sendCtrlC()) {
                logger.warn("Failed to send Ctrl-C signal");
            }
        }
    }

    private WinProcess getWinProcess(Process process) {
        Method pidMethod = findPidMethod();
        WinProcess winProcess;
        if (pidMethod != null) {
            // Java 9+
            // To avoid reflection access warnings we get the pid with builtin method
            int pid;
            try {
                pid = Math.toIntExact((long) pidMethod.invoke(process, new Object[]{}));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            winProcess = new WinProcess(pid);
        } else {
            winProcess = new WinProcess(process);
        }
        return winProcess;
    }

    private Method findPidMethod() {
        for (Method method : Process.class.getMethods()) {
            if ("pid".equals(method.getName()) && (method.getParameterCount() == 0)) {
                return method;
            }
        }
        return null;
    }

    private Commandline buildCommandLine() throws Exception {
        Commandline cli = new Commandline();

        //Disable explicit quoting of arguments
        cli.getShell().setQuotedArgumentsEnabled(false);

        cli.setExecutable(java.getAbsolutePath());

        environment.forEach(cli::addEnvironment);

        cli.setWorkingDirectory(workingDirectory);

        addClasspath(this.argsList);

        argsLine(cli);

        return cli;
    }

    private void argsLine(Commandline commandline) {
        if (jvmArgs == null) {
            return;
        }
        List<String> full = new ArrayList<>(jvmArgs);
        full.addAll(argsList);

        full.forEach(arg -> {
            Arg cliArg = commandline.createArg();
            cliArg.setValue(arg);
        });
    }

    public JavaProcessExecutor withArgs(List<String> argsList) {
        this.argsList = argsList;
        return this;
    }

    public JavaProcessExecutor withLogger(Log logger) {
        this.logger = logger;
        return this;
    }

    public JavaProcessExecutor withClassPath(Collection<URL> classPathUrls) {
        this.classPathUrls = classPathUrls;
        return this;
    }

    public JavaProcessExecutor withWaitFor(boolean waitFor) {
        this.waitFor = waitFor;
        return this;
    }

    public JavaProcessExecutor withEnvVar(String name, String value) {
        environment.put(name, value);
        return this;
    }

    public JavaProcessExecutor withJvmOpts(List<String> jvmArgs) {
        if (jvmArgs == null) {
            this.jvmArgs = Collections.emptyList();
        } else {
            this.jvmArgs = jvmArgs;
        }
        return this;
    }

    public JavaProcessExecutor withWorkDirectory(File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The working directory "
                + directory.getAbsolutePath() + " is not a directory");
        }
        this.workingDirectory = directory;
        return this;
    }

    private void redirect(InputStream in, PrintStream ps) {
        StreamPumper pumper = new StreamPumper(in, new PrintWriter(new BufferedOutputStream(ps)));
        pumper.setPriority(Thread.MIN_PRIORITY + 1);
        pumper.start();
    }


    /**
     * This add or build the classpath that will be passed to the forked process JVM i.e &quot;-cp&quot;
     *
     * @param argsList - the forked process argument list to which the classpath will be appended
     * @throws MojoExecutionException - any error that might occur while building or adding classpath
     */
    private void addClasspath(List<String> argsList) throws MojoExecutionException {
        try {

            StringBuilder classpath = new StringBuilder();

            for (URL ele : this.classPathUrls) {
                classpath.append(classpath.length() > 0 ? File.pathSeparator:"");
                classpath.append(new File(ele.toURI()));
            }

            String oldClasspath = System.getProperty("java.class.path");

            if (oldClasspath != null) {
                classpath.append(File.pathSeparator);
                classpath.append(oldClasspath);
            }

            if (SystemUtils.IS_OS_WINDOWS) {
                classpath.insert(0, '"').append('"');
            }

            argsList.add(0, "-cp");
            argsList.add(1, classpath.toString());

        } catch (Exception ex) {
            throw new MojoExecutionException("Could not build classpath", ex);
        }
    }

    /**
     * An utility to find the Java Executable from the host
     *
     * @return - the {@link File} representing the Java executable path
     */
    private File findJava() {
        String javaHome = System.getProperty("java.home");
        File found;
        if (javaHome == null) {
            found = ExecUtils.findExecutableInSystemPath("java");
        } else {
            File bin = new File(javaHome, "bin");
            found = ExecUtils.find("java", bin);
        }

        if (found == null || !found.isFile()) {
            throw new IllegalStateException("Unable to find the java executable in JAVA_HOME or in the system path");
        }
        return found;
    }
}
