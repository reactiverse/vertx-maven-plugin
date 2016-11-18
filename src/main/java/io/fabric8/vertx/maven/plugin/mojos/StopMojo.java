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

package io.fabric8.vertx.maven.plugin.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This goal is used to stop the vertx application in background mode identified by vertx process id stored
 * in the project workingDirectory with name vertx-start-process.id
 *
 * @author kameshs
 */
@Mojo(name = "stop",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class StopMojo extends AbstractRunMojo {

    /**
     * this control how long the process should to start, if the process does not stop within the time, its deemed as
     * failed, the default value is 10 seconds
     */
    @Parameter(alias = "timeout", property = "vertx.stop.timeout", defaultValue = "10")
    protected int timeout;

    /**
     * the vertx application id that will be used to stop the process, if left blank this value will be intialized
     * form the ${project.basedir}/{@link Constants#VERTX_PID_FILE}
     */
    @Parameter(alias = "appIds")
    protected Set<String> appIds;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        vertxCommand = Constants.VERTX_COMMAND_STOP;

        getAppId();

        for (String vertxProcId : appIds) {
            try {

                addClasspath(argsList);

                if (isVertxLauncher(launcher)) {
                    addVertxArgs(argsList);
                } else {
                    argsList.add(launcher);
                }

                argsList.add(vertxProcId);

                run(argsList);

                Files.delete(Paths.get(workDirectory.toString(), Constants.VERTX_PID_FILE));

            } catch (IOException e) {
                throw new MojoExecutionException("Unable to read process file from directory :" + workDirectory.toString());
            }
        }
    }

    /**
     * This will compute the vertx application id(s) that will be passed to the vertx applicaiton with &quot;-id&quot;
     * option, if the appId is not found in the configuration an new {@link UUID}  will be generated and assigned
     */
    private void getAppId() throws MojoExecutionException {

        if (appIds == null) {
            appIds = new HashSet<>();
        }

        Path vertxPidFile = Paths.get(workDirectory.toString(), Constants.VERTX_PID_FILE);

        if (Files.exists(vertxPidFile)) {
            try {
                byte[] bytes = Files.readAllBytes(vertxPidFile);
                appIds.add(new String(bytes));
            } catch (IOException e) {
                throw new MojoExecutionException("Error reading " + Constants.VERTX_PID_FILE, e);
            }
        }
    }
}
