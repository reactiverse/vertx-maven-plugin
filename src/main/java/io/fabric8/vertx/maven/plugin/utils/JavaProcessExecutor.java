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

package io.fabric8.vertx.maven.plugin.utils;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author kameshs
 */
public class JavaProcessExecutor extends JavaExecutor {

    protected List<String> argsList = new ArrayList<>();

    protected Log logger;

    protected boolean waitFor = true;

    protected File workingDirectory;

    @Override
    public Optional<Process> execute() throws Exception {

        Commandline commandLine = buildCommandLine();

        Process process;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Executing command :" + commandLine);
            }

            process = commandLine.execute();

            if (waitFor) {
                redirectOutput(process, logger);
                process.waitFor();
            }

            return Optional.of(process);

        } catch (Exception e) {
            throw new Exception("Error running command :" + commandLine, e);
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

        final String[] args = new String[argsList.size()];
        argsList.toArray(args);

        argsList.forEach(arg -> {
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

    public JavaProcessExecutor withWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public JavaProcessExecutor withWaitFor(boolean waitFor) {
        this.waitFor = waitFor;
        return this;
    }

}
