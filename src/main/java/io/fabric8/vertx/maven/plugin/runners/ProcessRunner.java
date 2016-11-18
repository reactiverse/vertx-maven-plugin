/*
 *   Copyright 2016 Kamesh Sampath
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.fabric8.vertx.maven.plugin.runners;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import io.fabric8.vertx.maven.plugin.utils.SignalListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class is used to build and run a forked process for the Vert.X application
 *
 * @author kameshs
 */
public class ProcessRunner {

    public static final int PROCESS_STOP_GRACE_TIMEOUT = 10;

    private static final Method INHERIT_IO_METHOD = MethodUtils.getAccessibleMethod(ProcessBuilder.class, "inheritIO");

    private final Path javaPath;
    private final File workDirectory;

    private List<String> argsList;

    private Log logger;

    private CountDownLatch latch;

    private boolean waitFor;

    private Process process;

    public ProcessRunner(List<String> argsList, File workDirectory, Log logger, boolean waitFor) {
        this.argsList = argsList;
        this.workDirectory = workDirectory;
        this.logger = logger;
        this.waitFor = waitFor;
        this.latch = new CountDownLatch(1);
        this.javaPath = findJava();
    }

    public Process getProcess() {
        return process;
    }

    public void run() throws MojoExecutionException {

        try {

            argsList.add(0, this.javaPath.toString());

            if (logger.isDebugEnabled()) {
                String cliArgs = argsList.stream().collect(Collectors.joining(" ")).toString();
                logger.debug("Process Run Command : " + cliArgs);
            }

            ProcessBuilder vertxRunProcBuilder = new ProcessBuilder(this.javaPath.toString())
                    .directory(this.workDirectory)
                    .command(argsList);

            boolean inheritedIO = inheritIO(vertxRunProcBuilder);

            this.process = vertxRunProcBuilder.start();
            //Attach Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Thread.sleep(100L);
                    stopGracefully(PROCESS_STOP_GRACE_TIMEOUT, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    //nothing to do
                }
            }));

            if (!inheritedIO) {
                redirectOutput();
            }

            SignalListener.handle(() -> handleSigInt());

            if (waitFor) {
                this.process.waitFor();
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Error starting process", e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Error starting process", e);
        }

    }

    /**
     * An utility method to check of the process has started within given timeout
     *
     * @param timeout  - the timeout within which the process is expected to be started
     * @param timeUnit - the {@link TimeUnit} for the timoeut
     * @throws InterruptedException
     */
    public void awaitReadiness(long timeout, TimeUnit timeUnit) throws InterruptedException {
        this.latch.await(timeout, timeUnit);
    }

    /**
     * An utility to find the Java Executable from the host
     *
     * @return - the {@link Path} representing the Java executable path
     */
    protected Path findJava() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new RuntimeException("unable to locate java binary");
        }

        Path binDir = FileSystems.getDefault().getPath(javaHome, "bin");

        Path java = binDir.resolve("java.exe");
        if (java.toFile().exists()) {
            return java;
        }

        java = binDir.resolve("java");
        if (java.toFile().exists()) {
            return java;
        }

        throw new RuntimeException("unable to locate java binary");
    }

    /**
     * method to handle the SIGINT signals from the process, once received the process will be destroyed gracefully
     * by calling stopGracefully method with PROCESS_STOP_GRACE_TIMEOUT timeout in seconds
     */
    protected void handleSigInt() {
        try {
            stopGracefully(PROCESS_STOP_GRACE_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //cant do anything here
        }
    }

    /**
     * There's a bug in the Windows VM (https://bugs.openjdk.java.net/browse/JDK-8023130)
     * that means we need to avoid inheritIO
     * Thanks to SpringBoot Maven Plugin(https://github.com/spring-projects/spring-boot/blob/master/spring-boot-tools/spring-boot-maven-plugin)
     * for showing way to handle this
     */
    protected boolean isInheritIOBroken() {
        if (!System.getProperty("os.name", "none").toLowerCase().contains("windows")) {
            return false;
        }
        String runtime = System.getProperty("java.runtime.version");
        if (!runtime.startsWith("1.7")) {
            return false;
        }
        String[] tokens = runtime.split("_");
        if (tokens.length < 2) {
            return true;
        }
        try {
            Integer build = Integer.valueOf(tokens[1].split("[^0-9]")[0]);
            if (build < 60) {
                return true;
            }
        } catch (Exception ex) {
            return true;
        }
        return false;
    }

    /**
     * this method will check to see if the process is inheting the IO from the host
     *
     * @param processBuilder - the {@link ProcessBuilder} which will be streaming its error/output
     * @return true if its inheriting the host IO
     */
    protected boolean inheritIO(ProcessBuilder processBuilder) {

        if (isInheritIOBroken()) {
            return false;
        }

        try {
            INHERIT_IO_METHOD.invoke(processBuilder);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * if the process is not inheriting the IO {@link ProcessRunner#inheritIO(ProcessBuilder)} then we need to redirect
     * the output to System.out
     */
    protected void redirectOutput() {

        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(this.process.getInputStream()));
        new Thread() {
            @Override
            public void run() {
                try {
                    String line = reader.readLine();
                    while (line != null) {
                        System.out.println(line);
                        line = reader.readLine();
                        System.out.flush();
                    }
                    reader.close();
                } catch (IOException e) {
                    //Ignore
                }
            }
        }.start();

    }

    /**
     * This method will try to destroy the process gracefully within the timeout, if the process is not killed within
     * the timeout this will kill the process forcibly
     *
     * @param timeout  - the timout
     * @param timeunit - the timeout units as {@link TimeUnit}
     * @throws InterruptedException - will be thrown when the typical interruptions that might occur during exit
     */
    protected void stopGracefully(int timeout, TimeUnit timeunit) throws InterruptedException {
        this.process.destroy();
        if (!this.process.waitFor(timeout, timeunit)) {
            this.process.destroyForcibly();
        }
    }

}
