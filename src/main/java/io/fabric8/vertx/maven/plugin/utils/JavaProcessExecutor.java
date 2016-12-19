/*
 *
 *   Copyright (c) 2016 Red Hat, Inc.
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

package io.fabric8.vertx.maven.plugin.utils;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @author kameshs
 */
public class JavaProcessExecutor extends JavaExecutor {

    protected List<String> argsList = new ArrayList<>();

    protected Log logger;

    protected boolean waitFor = true;

    protected File workingDirectory;

    protected Thread watchdog;

    protected List<String> jvmArgs;

    @Override
    public Optional<Process> execute() throws Exception {

        Commandline commandLine = buildCommandLine();

        Process process = null;

        try {

            logger.debug("Executing command :" + commandLine);

            process = commandLine.execute();

            Process reference = process;
            watchdog = new Thread(() -> {
               if (reference != null  && reference.isAlive()) {
                   reference.destroy();
               }
            });

            Runtime.getRuntime().addShutdownHook(watchdog);

            if (waitFor) {
                redirectOutput(process, logger);
                process.waitFor();
                if (! process.isAlive()) {
                    Runtime.getRuntime().removeShutdownHook(watchdog);
                }
            }

            return Optional.of(process);

        } catch (InterruptedException e) {
            if (process.isAlive()) {
                process.destroy();
            }
            return Optional.empty();
        } catch (Exception e) {
            if (process != null  && process.isAlive()) {
                process.destroy();
            }
            throw new Exception("Error running java command : " + e.getMessage(), e);
        }

    }

    @Override
    public Commandline buildCommandLine() throws Exception {
        Commandline cli = new Commandline();

        //Disable explicit quoting of arguments
        cli.getShell().setQuotedArgumentsEnabled(false);

        cli.setExecutable(javaPath.toString());

        cli.setWorkingDirectory(workingDirectory);

        addClasspath(this.argsList);

        argsLine(cli);

        return cli;
    }

    @Override
    public void argsLine(Commandline commandline) {
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
}
