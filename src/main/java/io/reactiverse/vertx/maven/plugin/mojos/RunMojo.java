/*
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

package io.reactiverse.vertx.maven.plugin.mojos;

import io.reactiverse.vertx.maven.plugin.utils.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This goal helps in running Vert.x applications as part of maven build.
 * Pressing <code>Ctrl+C</code> will then terminate the application.
 */
@Mojo(name = "run", threadSafe = true,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RunMojo extends AbstractVertxMojo {

    private static final String WEB_ENVIRONMENT_VARIABLE_NAME = "VERTXWEB_ENVIRONMENT";

    /**
     * Whether redeployment is enabled.
     */
    @Parameter(property = "vertx.redeploy.enabled", defaultValue = "true")
    boolean redeploy;

    /**
     * The root directory to scan for changes.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main")
    File redeployRootDirectory;

    /**
     * A list of <a href="https://ant.apache.org/manual/dirtasks.html#patterns">Ant-like</a> patterns of files/directories to include in change monitoring.
     * <p>
     * The patterns must be expressed relatively to the {@link #redeployRootDirectory}.
     */
    @Parameter
    List<String> redeployIncludes;

    /**
     * A list of <a href="https://ant.apache.org/manual/dirtasks.html#patterns">Ant-like</a> patterns of files/directories to exclude from change monitoring.
     * <p>
     * The patterns must be expressed relatively to the {@link #redeployRootDirectory}.
     */
    @Parameter
    List<String> redeployExcludes;

    /**
     * How often, in milliseconds, should the source files be scanned for file changes.
     */
    @Parameter(property = "vertx.redeploy.scan.period", defaultValue = "1000")
    long redeployScanPeriod;

    /**
     * How long, in milliseconds, the plugin should wait between two redeployments.
     */
    @Parameter(property = "vertx.redeploy.grace.period", defaultValue = "1000")
    long redeployGracePeriod;

    /**
     * Sets the environment the Vert.x Web app is running in.
     * <p>
     * If not set and the {@code VERTXWEB_ENVIRONMENT} environment variable is absent, defaults to {@code dev}.
     */
    @Parameter(property = "vertxweb.environment")
    String vertxWebEnvironment;

    /**
     * The path to the file that contains Vert.x options.
     */
    @Parameter(alias = "options", property = "vertx.options")
    File options;

    /**
     * The path to the file that contains the main verticle config.
     */
    @Parameter(alias = "config", property = "vertx.config")
    File config;

    /**
     * The working directory for the Vert.x application.
     */
    @Parameter(alias = "workDirectory", property = "vertx.directory", defaultValue = "${project.basedir}")
    File workDirectory;

    /**
     * JVM arguments that should be associated with the forked process used to run the application.
     * On command line, make sure to wrap multiple values between quotes.
     */
    @Parameter(alias = "jvmArgs", property = "vertx.jvmArguments")
    List<String> jvmArgs;

    /**
     * The custom or additional run arguments that can be passed to the Launcher.
     */
    @Parameter(name = "runArgs", property = "vertx.runArgs")
    List<String> runArgs;

    /**
     * Whether the Vert.x application should be started with blocked thread checker disabled.
     */
    @Parameter(property = "vertx.disable.blocked.thread.checker", defaultValue = "true")
    boolean disableBlockedThreadChecker;

    /**
     * Whether the JVM running the Vert.x application should start a remote debug server.
     */
    @Parameter(property = "vertx.debug", defaultValue = "true")
    boolean debug;

    /**
     * Whether the Vert.x application should wait for a remote debugger to attach before starting.
     */
    @Parameter(property = "vertx.debug.suspend", defaultValue = "false")
    boolean debugSuspend;

    /**
     * The remote debugging port used by the Vert.x application.
     */
    @Parameter(property = "vertx.debug.port", defaultValue = "5005")
    String debugPort;

    private Properties systemEnvVars;
    private File java;

    private final CountDownLatch stopLatch = new CountDownLatch(1);
    private Process vertxApp;
    private volatile boolean stop;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("vertx:run skipped by configuration");
            return;
        }

        compileIfNeeded();

        systemEnvVars = CommandLineUtils.getSystemEnvVars();

        java = findJava();
        getLog().info("Found java executable: " + java);

        if (StringUtils.isBlank(vertxWebEnvironment)) {
            vertxWebEnvironment = systemEnvVars.getProperty(WEB_ENVIRONMENT_VARIABLE_NAME, "dev");
        } else {
            vertxWebEnvironment = vertxWebEnvironment.trim();
        }
        getLog().info("Using web environment: " + vertxWebEnvironment);

        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookTask()));

        try (FileChangesHelper fileChangesHelper = new FileChangesHelper(getLog(), redeployRootDirectory, redeployIncludes, redeployExcludes)) {
            buildLoop(fileChangesHelper);
        } catch (Exception e) {
            throw new MojoExecutionException("Failure while running Vert.x application", e);
        } finally {
            destroyApp();
            stopLatch.countDown();
        }
    }

    private void compileIfNeeded() {
        File classes = new File(project.getBuild().getOutputDirectory());
        if (!classes.isDirectory()) {
            MavenExecutionUtils.execute("compile", project, mavenSession, lifecycleExecutor, container);
        }
    }

    private File findJava() throws MojoExecutionException {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            Properties envVars = systemEnvVars;
            javaHome = envVars.getProperty("JAVA_HOME");
        }
        if (javaHome != null) {
            File binDir = new File(javaHome, "bin");
            if (binDir.exists() && binDir.isDirectory()) {
                File java = new File(binDir, SystemUtils.IS_OS_WINDOWS ? "java.exe" : "java");
                if (java.isFile() && java.canExecute()) {
                    return java;
                }
            }
        }
        throw new MojoExecutionException("Unable to find the Java executable.");
    }

    private void buildLoop(FileChangesHelper fileChangesHelper) throws MojoExecutionException {
        while (!stop) {

            VertxAppBuilder appBuilder = new VertxAppBuilder(java, getVertxApplicationInfo().mainClass())
                .env(WEB_ENVIRONMENT_VARIABLE_NAME, vertxWebEnvironment)
                .workDir(workDirectory);

            for (File classPathElement : getClassPathElements()) {
                appBuilder.addClasspathElement(classPathElement);
            }

            if (debug || disableBlockedThreadChecker) {
                appBuilder
                    .addJvmArg(String.format("-Dvertx.options.maxEventLoopExecuteTime=%d", Long.MAX_VALUE))
                    .addJvmArg(String.format("-Dvertx.options.maxWorkerExecuteTime=%d", Long.MAX_VALUE));
            }

            if (debug) {
                appBuilder
                    .addJvmArg(String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=%s,address=%s", debugSuspend ? "y" : "n", debugPort));
            }

            if (jvmArgs != null) {
                for (String jvmArg : jvmArgs) {
                    if (StringUtils.isNotBlank(jvmArg)) {
                        appBuilder.addJvmArg(jvmArg.trim());
                    }
                }
            }

            if (getVertxApplicationInfo().isVertxLauncher()) {
                if (getVertxApplicationInfo().isLegacyVertxLauncher()) {
                    appBuilder.addAppArg("run");
                }
                appBuilder.addAppArg(getVertxApplicationInfo().mainVerticle());
                File optionsFile = scanAndLoad("options", options);
                if (optionsFile != null) {
                    appBuilder
                        .addAppArg("-options")
                        .addAppArg(StringUtils.quoteAndEscape(optionsFile.getAbsolutePath(), '"'));
                }
                File configFile = scanAndLoad("application", config);
                if (configFile != null) {
                    appBuilder
                        .addAppArg("-conf")
                        .addAppArg(StringUtils.quoteAndEscape(configFile.getAbsolutePath(), '"'));
                }
            }

            if (runArgs != null) {
                for (String runArg : runArgs) {
                    if (StringUtils.isNotBlank(runArg)) {
                        try {
                            for (String arg : CommandLineUtils.translateCommandline(runArg.trim())) {
                                appBuilder.addAppArg(arg);
                            }
                        } catch (Exception e) {
                            throw new MojoExecutionException("Failed to parse Vert.x run argument:" + runArg, e);
                        }
                    }
                }
            }

            getLog().info("Launching Vert.x Application");
            ProcessBuilder processBuilder = appBuilder.processBuilder();

            try {
                vertxApp = processBuilder.start();
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to start Vert.x Application", e);
            }

            if (redeploy) {
                try {
                    Thread.sleep(redeployGracePeriod);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new MojoExecutionException("Interrupted while sleeping for grace period", e);
                }
            }

            while (true) {
                if (!vertxApp.isAlive()) {
                    getLog().info("Vert.x Application has stopped");
                    return;
                }
                if (stop) {
                    return;
                }
                if (redeploy) {
                    if (fileChangesHelper.foundChanges()) {
                        break;
                    }
                    try {
                        Thread.sleep(redeployScanPeriod);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new MojoExecutionException("Interrupted while sleeping for scan period", e);
                    }
                }
            }

            if (!destroyApp()) {
                throw new MojoExecutionException("Failed to destroy Vert.x Application gracefully");
            }

            getLog().info("Redeploying Vert.x Application");

            Set<Artifact> artifacts = project.getArtifacts();
            for (Callable<Void> buildTask : computeExecutionChain(artifacts)) {
                try {
                    buildTask.call();
                } catch (Exception e) {
                    throw new MojoExecutionException("Failed to build Vert.x Application", e);
                }
            }
        }
    }

    private List<Callable<Void>> computeExecutionChain(Set<Artifact> artifacts) {
        List<Callable<Void>> list = new ArrayList<>();
        ExecutionListener executionListener = mavenSession.getRequest().getExecutionListener();
        if (executionListener instanceof MojoSpy) {
            MojoSpy spy = (MojoSpy) executionListener;
            for (MojoExecution execution : spy.getMojos()) {
                Callable<Void> task = toTask(execution, artifacts);
                list.add(task);
            }
        }
        if (list.isEmpty()) {
            getLog().info("No plugin execution collected. The vertx:initialize goal has not been run beforehand. Only handling resources and java compilation");
            list.add(new JavaBuildCallback());
            list.add(new ResourceBuildCallback());
        }
        return list;
    }

    private Callable<Void> toTask(MojoExecution execution, Set<Artifact> artifacts) {
        MojoExecutor executor = new MojoExecutor(execution, project, mavenSession, buildPluginManager);
        return () -> {
            project.setArtifacts(artifacts);
            try {
                //--- vertx-maven-plugin:1.0-SNAPSHOT:run (default-cli) @ vertx-demo
                getLog().info(String.format(">>> %s:%s:%s (%s) @%s", execution.getArtifactId(), execution.getVersion(), execution.getGoal(), execution.getExecutionId(), project.getArtifactId()));
                executor.execute();
            } catch (Exception e) {
                getLog().error("Error while doing incremental build", e);
            }
            return null;
        };
    }

    private File lookForConfiguration(String filename) {
        File confBaseDir = new File(project.getBasedir(), "src/main/conf");
        if (!confBaseDir.isDirectory()) {
            return null;
        }
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir(confBaseDir);
        directoryScanner.setIncludes(new String[]{filename + ".json", filename + ".yml", filename + ".yaml"});
        directoryScanner.scan();
        return Arrays.stream(directoryScanner.getIncludedFiles())
            .map(found -> new File(confBaseDir, found))
            .findFirst()
            .orElse(null);
    }

    /**
     * This method loads configuration files from `src/main/conf`.
     * It uses the pattern {@code ${basedir}/src/main/conf/{options/application}.{json/yaml/yml}}. In case of YAML, the
     * configuration is converted to JSON.
     * <p>
     * Visible for testing
     */
    File scanAndLoad(String configName, File userProvided) throws MojoExecutionException {
        File file;
        if (userProvided != null) {
            if (!userProvided.isFile()) {
                getLog().error("Cannot load the configuration - file " + userProvided.getAbsolutePath() + " does not exist");
                return userProvided;
            }
            file = userProvided;
        } else {
            file = lookForConfiguration(configName);
            if (file == null) {
                getLog().debug("No configuration found");
                return null;
            }
        }
        return isYaml(file) ? convertYamlToJson(file) : file;
    }

    private File convertYamlToJson(File yamlFile) throws MojoExecutionException {
        File jsonConfDir = new File(projectBuildDir, "conf");
        jsonConfDir.mkdirs();
        String output = FilenameUtils.removeExtension(yamlFile.getName()) + ".json";
        File convertedJsonFile = new File(jsonConfDir, output);
        try {
            ConfigConverterUtil.convertYamlToJson(yamlFile, convertedJsonFile);
            getLog().debug(yamlFile.getAbsolutePath() + " converted to " + convertedJsonFile.getAbsolutePath());
            return convertedJsonFile;
        } catch (Exception e) {
            throw new MojoExecutionException("Error loading or converting the configuration file:" + yamlFile, e);
        }
    }

    private boolean isYaml(File file) {
        if (file != null) {
            String fileName = file.getName();
            return fileName.endsWith(".yaml") || fileName.endsWith(".yml");
        }
        return false;
    }

    /**
     * @return true if process stopped gracefully, false otherwise
     */
    private boolean destroyApp() {
        if (vertxApp != null) {
            vertxApp.destroy();
            try {
                if (!vertxApp.waitFor(30, TimeUnit.SECONDS)) {
                    vertxApp.destroyForcibly();
                    return false;
                }
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                vertxApp.destroyForcibly();
                return false;
            }
        }
        return false;
    }

    public final class JavaBuildCallback implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            MojoUtils.compile(project, mavenSession, buildPluginManager);
            return null;
        }
    }

    public final class ResourceBuildCallback implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            MojoUtils.copyResources(project, mavenSession, buildPluginManager);
            return null;
        }
    }

    private class ShutdownHookTask implements Runnable {
        @Override
        public void run() {
            getLog().info("Shutting down...");
            stop = true;
            try {
                stopLatch.await();
            } catch (InterruptedException e) {
                getLog().warn("Interrupted while waiting for shutdown", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
