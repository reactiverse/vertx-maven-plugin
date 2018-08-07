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

package io.reactiverse.vertx.maven.plugin.mojos;

import io.reactiverse.vertx.maven.plugin.utils.MavenExecutionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This goal is used to run the vertx application in background mode, the application id will be persisted in the
 * working directory of the project with a file name vertx-start-process.id
 *
 * @author kameshs
 */
@Mojo(name = "start", threadSafe = true,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class StartMojo extends AbstractRunMojo {

    /**
     * this parameter is used to decide which mode the application should be started, it can have two values
     * &quot;jar&quot; or &quot;exploded&quot; default mode is &quot;jar&quot;
     */
    @Parameter(alias = "mode", property = "vertx.start.mode", defaultValue = "jar")
    protected String runMode;

    /**
     * the custom id that will be assigned when running vertx application, this should be unique. If left blank
     * the {@link UUID} will be used to generate the id.  The same id will be used to terminate the application
     * via the &quot;vertx:stop&quot; goal
     */
    @Parameter(alias = "appId", property = "vertx.app.id")
    protected String appId;

    /**
     * The artifact classifier. If not set, the plugin uses the default final name.
     */
    @Parameter(name = "classifier")
    protected String classifier;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("vertx:start skipped by configuration");
            return;
        }

        vertxCommand = VERTX_COMMAND_START;

        String applicationId = getAppId();

        scanAndLoadConfigs();

        List<String> argsList = new ArrayList<>();

        writePidFile(applicationId);

        boolean jarMode = VERTX_RUN_MODE_JAR.equals(runMode);

        if (jarMode) {
            String name = PackageMojo.computeOutputName(computeArchive(), project, classifier);
            File fatjar = new File(project.getBuild().getDirectory(), name);

            buildJarIfRequired(fatjar);

            if (fatjar.isFile()  &&  isVertxLauncher(launcher)) {
                argsList.add("-jar");
                argsList.add(fatjar.getAbsolutePath());
            } else if (fatjar.isFile()  && ! isVertxLauncher(launcher)) {
                argsList.add("-cp");
                argsList.add(fatjar.getAbsolutePath());
                argsList.add(IO_VERTX_CORE_LAUNCHER);
            } else {
                throw new MojoFailureException("Unable to find vertx application jar --> "
                    + fatjar.getAbsolutePath());
            }
        } else {
            addClasspath(argsList);
            if (isVertxLauncher(launcher)) {
                argsList.add(launcher);
            } else {
                argsList.add(IO_VERTX_CORE_LAUNCHER);
            }
        }

        argsList.add(vertxCommand);


        if (! jarMode) {
            argsList.add(verticle);
        }

        appendConfigIfRequired(argsList);
        appendLauncherIfRequired(argsList);

        argsList.add("-id");
        argsList.add(applicationId);

        appendSysProperties(argsList);

        run(argsList);

    }

    private void writePidFile(String vertxProcId) throws MojoExecutionException {
        try {
            File pid = new File(workDirectory, VERTX_PID_FILE);
            FileUtils.write(pid, vertxProcId, "UTF-8");
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write process file to directory :"
                + workDirectory.toString(), e);
        }
    }

    private void buildJarIfRequired(File fatjar) {
        if (! fatjar.isFile()) {
            getLog().warn("Unable to find the Vert.x application jar, triggering the build");
            MavenExecutionUtils.execute("package", project, mavenSession, lifecycleExecutor, container);
        }
    }

    private void appendSysProperties(List<String> argsList) {
        if (jvmArgs != null && !jvmArgs.isEmpty()) {
            String javaOpts = jvmArgs.stream().collect(Collectors.joining(" "));
            String argJavaOpts = VERTX_ARG_JAVA_OPT +
                "=" + javaOpts;
            argsList.add(argJavaOpts);
        }
    }

    private void appendLauncherIfRequired(List<String> argsList) throws MojoExecutionException {
        if (launcher != null  && ! isVertxLauncher(launcher)) {
            argsList.add(AbstractRunMojo.VERTX_ARG_LAUNCHER_CLASS);
            argsList.add(launcher);
        }
    }

    private void appendConfigIfRequired(List<String> argsList) {
        if (config != null && config.exists() && config.isFile()) {
            getLog().info("Using configuration from file: " + config.toString());
            argsList.add(VERTX_ARG_CONF);
            argsList.add(config.toString());
        }
    }

    /**
     * This will compute the vertx application id that will be passed to the vertx applicaiton with &quot;-id&quot;
     * option, if the appId is not found in the configuration an new {@link UUID}  will be generated and assigned
     *
     * @return - the application id
     */
    private String getAppId() {
        if (appId == null) {
            UUID uuid = UUID.randomUUID();
            appId = uuid.toString();
        }
        return appId;
    }
}
