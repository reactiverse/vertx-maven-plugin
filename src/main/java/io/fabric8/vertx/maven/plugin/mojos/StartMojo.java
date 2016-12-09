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

package io.fabric8.vertx.maven.plugin.mojos;

import io.fabric8.vertx.maven.plugin.utils.MavenExecutionUtils;
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
     * The additional arguments that will be passed as program arguments to the JVM, all standard vertx arguments are
     * automatically applied
     */
    @Parameter(alias = "jvmArgs", property = "vertx.jvmArguments")
    protected List<String> jvmArgs;

    /**
     * The artifact classifier. If not set, the plugin uses the default final name.
     */
    @Parameter(name = "classifier")
    protected String classifier;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        vertxCommand = VERTX_COMMAND_START;

        String vertxProcId = getAppId();
        scanAndLoadConfigs();

        List<String> argsList = new ArrayList<>();

        try {

            Path pidFilePath = Paths.get(workDirectory.toString(), VERTX_PID_FILE);

            if (pidFilePath.toFile().exists() && pidFilePath.toFile().isFile()) {
                Files.delete(pidFilePath);
            }

            Files.write(Paths.get(workDirectory.toString(), VERTX_PID_FILE), vertxProcId.getBytes());

        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write process file to directory :"
                + workDirectory.toString());
        }

        boolean jarMode = VERTX_RUN_MODE_JAR.equals(runMode);

        if (jarMode) {
            String name = PackageMojo.computeOutputName(project, classifier);
            File fatjar = new File(project.getBuild().getDirectory(), name);

            if (! fatjar.isFile()) {
                getLog().warn("Unable to find the Vert.x application jar, triggering the build");
                MavenExecutionUtils.execute("package", project, mavenSession, lifecycleExecutor, container);
            }

            if (fatjar.isFile()  && launcher.equals(IO_VERTX_CORE_LAUNCHER)) {
                argsList.add("-jar");
                argsList.add(fatjar.getAbsolutePath());
            } else if (fatjar.isFile()  && ! launcher.equals(IO_VERTX_CORE_LAUNCHER)) {
                argsList.add("-cp");
                argsList.add(fatjar.getAbsolutePath());
                argsList.add(IO_VERTX_CORE_LAUNCHER);
            } else {
                throw new MojoFailureException("Unable to find vertx application jar --> "
                    + fatjar.getAbsolutePath());
            }
        } else {
            addClasspath(argsList);
            argsList.add(IO_VERTX_CORE_LAUNCHER);
        }

        argsList.add(vertxCommand);


        if (! jarMode) {
            // The run command should not be required when using Vert.x 3.4.x+
            argsList.add("run");
            argsList.add(verticle);
        }

        if (config != null && config.exists() && config.isFile()) {
            getLog().info("Using configuration from file: " + config.toString());
            argsList.add(VERTX_ARG_CONF);
            argsList.add(config.toString());
        }

        argsList.add(AbstractRunMojo.VERTX_ARG_LAUNCHER_CLASS);
        if (launcher != null) {
            argsList.add(launcher);
        } else {
            argsList.add(IO_VERTX_CORE_LAUNCHER);
        }

        argsList.add("-id");
        argsList.add(vertxProcId);

        if (jvmArgs != null && !jvmArgs.isEmpty()) {
            String javaOpts = jvmArgs.stream().collect(Collectors.joining(" "));
            String argJavaOpts = VERTX_ARG_JAVA_OPT +
                "=" + javaOpts;
            argsList.add(argJavaOpts);
        }

        run(argsList);

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
