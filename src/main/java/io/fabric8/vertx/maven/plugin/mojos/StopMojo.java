/*
 *
 *   Copyright (c) 2016-2017 Red Hat, Inc.
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

package io.fabric8.vertx.maven.plugin.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This goal is used to stop the vertx application in background mode identified by vertx process id stored
 * in the project workingDirectory with name vertx-start-process.id
 *
 * @author kameshs
 */
@Mojo(name = "stop",
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class StopMojo extends AbstractRunMojo {

    /**
     * the vertx application id that will be used to stop the process, if left blank this value will be intialized
     * form the ${project.basedir}/{@link AbstractVertxMojo#VERTX_PID_FILE}
     */
    @Parameter(alias = "appIds")
    protected Set<String> appIds;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("vertx:stop skipped by configuration");
            return;
        }

        vertxCommand = VERTX_COMMAND_STOP;

        getAppId();

        List<String> argsList = new ArrayList<>();

        for (String vertxProcId : appIds) {
            try {

                addClasspath(argsList);

                argsList.add(IO_VERTX_CORE_LAUNCHER);

                argsList.add(vertxCommand);

                argsList.add(vertxProcId);

                run(argsList);

                Files.delete(Paths.get(workDirectory.toString(), VERTX_PID_FILE));

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

        Path vertxPidFile = Paths.get(workDirectory.toString(), VERTX_PID_FILE);

        if (Files.exists(vertxPidFile)) {
            try {
                byte[] bytes = Files.readAllBytes(vertxPidFile);
                appIds.add(new String(bytes));
            } catch (IOException e) {
                throw new MojoExecutionException("Error reading " + VERTX_PID_FILE, e);
            }
        }
    }
}
