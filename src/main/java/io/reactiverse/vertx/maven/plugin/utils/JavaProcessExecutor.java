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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.toolchain.Toolchain;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamPumper;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @author kameshs
 */
public class JavaProcessExecutor {

    private final Toolchain toolchain;
    private List<String> argsList = new ArrayList<>();

    private Log logger;

    private boolean waitFor = true;

    private File workingDirectory;

    protected List<String> jvmArgs;

    private final File java;
    private Collection<URL> classPathUrls;

    public JavaProcessExecutor(Toolchain toolchain) {
        this.toolchain = toolchain;
        this.java = findJava();
    }

    public Process execute() throws Exception {
        Commandline commandLine = buildCommandLine();

        Process process = null;
        try {
            logger.debug("Executing command :" + commandLine);
            process = commandLine.execute();
            Process reference = process;
            Thread watchdog = new Thread(() -> {
                if (reference != null && reference.isAlive()) {
                    reference.destroy();
                }
            });

            Runtime.getRuntime().addShutdownHook(watchdog);

            if (waitFor) {
                redirectOutput(process, logger);
                process.waitFor();
                if (!process.isAlive()) {
                    Runtime.getRuntime().removeShutdownHook(watchdog);
                }
            }

            return process;

        } catch (InterruptedException e) {
            if (process.isAlive()) {
                process.destroy();
            }
            // We sure the interrupt flag is restored.
            Thread.currentThread().interrupt();
            return process;
        } catch (Exception e) {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
            throw new Exception("Error running java command : " + e.getMessage(), e);
        }

    }

    private Commandline buildCommandLine() throws Exception {
        Commandline cli = new Commandline();

        //Disable explicit quoting of arguments
        cli.getShell().setQuotedArgumentsEnabled(false);

        cli.setExecutable(java.getAbsolutePath());

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

    public JavaProcessExecutor withJvmOpts(List<String> jvmArgs) {
        if (jvmArgs == null) {
            this.jvmArgs = Collections.emptyList();
        } else {
            this.jvmArgs = jvmArgs;
        }
        return this;
    }

    public JavaProcessExecutor withWorkDirectory(File directory) {
        if (! directory.isDirectory()) {
            throw new IllegalArgumentException("The working directory "
                + directory.getAbsolutePath() + " is not a directory");
        }
        this.workingDirectory = directory;
        return this;
    }

    private void redirectOutput(Process process, Log logger) {
        StreamToLogConsumer logConsumer = logger::info;

        StreamPumper outPumper = new StreamPumper(process.getInputStream(), logConsumer);
        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), logConsumer);

        outPumper.setPriority(Thread.MIN_PRIORITY + 1);
        errPumper.setPriority(Thread.MIN_PRIORITY + 1);

        outPumper.start();
        errPumper.start();
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
                classpath = classpath
                    .append(classpath.length() > 0 ? File.pathSeparator : "")
                    .append(new File(ele.toURI()));
            }

            String oldClasspath = System.getProperty("java.class.path");

            if (oldClasspath != null) {
                classpath.append(File.pathSeparator);
                classpath.append(oldClasspath);
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
        if (toolchain != null) {
            String javaHome = toolchain.findTool("java");
            return new File(javaHome);
        }
        String javaHome = System.getProperty("java.home");
        File found;
        if (javaHome == null) {
            found = ExecUtils.findExecutableInSystemPath("java");
        } else {
            File bin = new File(javaHome, "bin");
            found = ExecUtils.find("java", bin);
        }

        if (found == null || ! found.isFile()) {
            throw new IllegalStateException("Unable to find the java executable in JAVA_HOME or in the system path");
        }
        return found;
    }
}
