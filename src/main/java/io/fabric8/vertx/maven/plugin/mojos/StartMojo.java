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

import io.fabric8.vertx.maven.plugin.utils.MojoUtils;
import org.apache.maven.artifact.Artifact;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This goal is used to run the vertx application in background mode, the application id will be persisted in the
 * working directory of the project with a file name vertx-start-process.id
 *
 * @author kameshs
 */
@Mojo(name = "start", threadSafe = true,
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class StartMojo extends AbstractRunMojo {

    /**
     * this control how long the process should to start, if the process does not start within the time, its deemed as
     * failed, the default value is 10 seconds
     */
    @Parameter(alias = "timeout", property = "vertx.start.timeout", defaultValue = "10")
    protected int timeout;

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

    private MojoUtils mojoUtils = new MojoUtils();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        vertxCommand = Constants.VERTX_COMMAND_START;

        String vertxProcId = getAppId();

        try {

            Path pidFilePath = Paths.get(workDirectory.toString(), Constants.VERTX_PID_FILE);

            if (pidFilePath.toFile().exists() && pidFilePath.toFile().isFile()) {
                Files.delete(pidFilePath);
            }

            Files.write(Paths.get(workDirectory.toString(), Constants.VERTX_PID_FILE), vertxProcId.getBytes());

        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write process file to directory :" + workDirectory.toString());
        }

        boolean jarMode = Constants.VERTX_RUN_MODE_JAR.equals(runMode);

        if (jarMode) {

            Optional<Artifact> vertxJar = getVertxArtifact();

            if (!vertxJar.isPresent()) {
                getLog().info("Vertx application jar not found, building ...");
                mojoUtils.withLog(getLog()).buildVertxArtifact(this.project, this.mavenSession
                        , this.buildPluginManager);
            }

            //Double check it
            vertxJar = getVertxArtifact();

            if (vertxJar.isPresent()) {
                argsList.add("-jar");
                argsList.add(vertxJar.get().getFile().toString());
            } else {
                throw new MojoFailureException("Unable to find vertx application jar --> "
                        + this.project.getArtifactId() + "-fat.jar");
            }
        } else {
            addClasspath(argsList);
        }

        if (isVertxLauncher(launcher)) {
            addVertxArgs(argsList);
        } else {
            argsList.add(launcher);
        }

        ArrayList<String> removebaleArgs = new ArrayList<>();
        removebaleArgs.add(verticle);
        removebaleArgs.add(launcher);
        removebaleArgs.add(launcher);
        removebaleArgs.add(Constants.VERTX_ARG_LAUNCHER_CLASS);

        if (jarMode) {
            argsList.removeAll(removebaleArgs);
        }
        argsList.add("-id");
        argsList.add(vertxProcId);

        if (jvmArgs != null && !jvmArgs.isEmpty()) {
            String javaOpts = jvmArgs.stream().collect(Collectors.joining(" "));
            StringBuilder argJavaOpts = new StringBuilder();
            argJavaOpts.append(Constants.VERTX_ARG_JAVA_OPT);
            argJavaOpts.append("=\"");
            argJavaOpts.append(javaOpts);
            argJavaOpts.append("\"");
            argsList.add(argJavaOpts.toString());
        }

        run(argsList);

    }

    /**
     * This will retrieve the attached artifact with classifier &quot;vertx&quot;
     *
     * @return - the {@link Artifact} which attached to the project with classifier &quot;vertx&quot;
     */
    private Optional<Artifact> getVertxArtifact() {
        return this.project.getAttachedArtifacts().stream()
                .filter(artifact -> "vertx".equals(artifact.getClassifier()))
                .findFirst();
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
