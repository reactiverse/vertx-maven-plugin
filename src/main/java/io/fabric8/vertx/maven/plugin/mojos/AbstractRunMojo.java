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

import io.fabric8.vertx.maven.plugin.utils.*;
import org.apache.commons.lang3.ClassUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class AbstractRunMojo extends AbstractVertxMojo {


    /* ==== Maven related ==== */

    /**
     * The maven project classes directory, defaults to target/classes
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    protected File classesDirectory;

    /**
     * This property is used to enable vertx to do redeployment of the verticles in case of modifications
     * to the sources.
     * The redeployPattern defines the source directories that will be watched for changes to trigger redeployment
     */
    @Parameter(name = "redeploy")
    protected boolean redeploy;

    /**
     * The default command to use when calling io.vertx.core.Launcher.
     * possible commands are,
     * <ul>
     * <li>bare</li>
     * <li>list</li>
     * <li>run</li>
     * <li>start</li>
     * <li>stop</li>
     * <li>run</li>
     * </ul>
     */
    protected String vertxCommand = "run";

    /**
     * This property will be passed as the -config option to vertx run. It defaults to file
     * "src/main/config/application.json", if it exists it will passed to the vertx run
     */
    @Parameter(alias = "config", property = "vertx.config", defaultValue = "src/main/conf/application.json")
    File config;

    /**
     * This property will be used as the working directory for the process when running in forked mode.
     * This defaults to ${project.basedir}
     */
    @Parameter(alias = "workDirectory", property = "vertx.directory", defaultValue = "${project.basedir}")
    File workDirectory;

    /**
     * JVM arguments that should be associated with the forked process used to run the
     * application. On command line, make sure to wrap multiple values between quotes.
     * <p>
     * The additional arguments that will be passed as program arguments to the JVM, all standard vertx arguments are
     * automatically applied.
     * <p>
     * @since 1.0.2
     */
    @Parameter(alias = "jvmArgs", property = "vertx.jvmArguments")
    protected List<String> jvmArgs;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("vertx:run skipped by configuration");
            return;
        }

        List<String> argsList = new ArrayList<>();

        scanAndLoadConfigs();

        boolean isVertxLauncher = isVertxLauncher(launcher);

        getLog().info("Launching Vert.x Application");

        if (isVertxLauncher) {
            addVertxArgs(argsList);
        } else if (redeploy) {
            getLog().info("Vert.x application redeploy enabled");
            argsList.add(IO_VERTX_CORE_LAUNCHER);
            argsList.add("run");
            StringBuilder redeployArg = new StringBuilder();
            redeployArg.append(VERTX_ARG_REDEPLOY); //fix for redeploy to work
            computeOutputDirsWildcard(redeployArg);
            argsList.add(redeployArg.toString());
            argsList.add(VERTX_ARG_LAUNCHER_CLASS);
            argsList.add(launcher);

            if (jvmArgs != null && !jvmArgs.isEmpty()) {
                String javaOpts = jvmArgs.stream().collect(Collectors.joining(" "));
                String argJavaOpts = VERTX_ARG_JAVA_OPT +
                    "=" + javaOpts;
                argsList.add(argJavaOpts);
            }
        } else {
            argsList.add(launcher);
        }

        run(argsList);
    }

    /**
     * This add or build the classpath that will be passed to the forked process JVM i.e &quot;-cp&quot;
     *
     * @param args - the forked process argument list to which the classpath will be appended
     * @throws MojoExecutionException - any error that might occur while building or adding classpath
     */
    protected void addClasspath(List<String> args) throws MojoExecutionException {
        try {
            StringBuilder classpath = new StringBuilder();
            for (URL ele : getClassPathUrls()) {
                classpath = classpath
                    .append(classpath.length() > 0 ? File.pathSeparator : "")
                    .append(new File(ele.toURI()));
            }
            getLog().debug("Classpath for forked process: " + classpath);
            args.add("-cp");
            args.add(classpath.toString());
        } catch (Exception ex) {
            throw new MojoExecutionException("Could not build classpath", ex);
        }
    }

    /**
     * This will add the ${project.build.outputDirectory} to the  classpath url collection
     *
     * @param classpathUrls - the existing classpath url collection to which the ${project.build.outputDirectory} be added
     * @throws IOException - any exception that might occur while get the classes directory as URL
     */
    protected void addClassesDirectory(List<URL> classpathUrls) throws IOException {

        classpathUrls.add(this.classesDirectory.toURI().toURL());
    }

    /**
     * This will add the project resources typically ${basedir}/main/resources to the classpath url collection
     *
     * @param classpathUrls - the existing classpath url collection to which the ${project.build.outputDirectory} be added
     * @throws IOException - any exception that might occur while get the classes directory as URL
     */
    protected void addProjectResources(List<URL> classpathUrls) throws IOException {

        for (Resource resource : this.project.getResources()) {
            File f = new File(resource.getDirectory());
            classpathUrls.add(f.toURI().toURL());
        }
    }

    /**
     * This will build the Vertx specific arguments that needs to be passed to the runnable process
     *
     * @param argsList - the existing collection of arguments to which the vertx arguments will be added
     */
    protected void addVertxArgs(List<String> argsList) {

        Objects.requireNonNull(launcher);

        if (IO_VERTX_CORE_LAUNCHER.equals(launcher)) {
            argsList.add(IO_VERTX_CORE_LAUNCHER);
        } else {
            argsList.add(launcher);
        }

        argsList.add(vertxCommand);

        //Since Verticles will be deployed from custom launchers we dont pass this as argument
        if (verticle != null && !VERTX_COMMAND_STOP.equals(vertxCommand)) {
            argsList.add(verticle);
        }

        if (redeploy && !(VERTX_COMMAND_START.equals(vertxCommand)
            || VERTX_COMMAND_STOP.equals(vertxCommand))) {
            getLog().info("Vert.x application redeploy enabled");

            StringBuilder redeployArg = new StringBuilder();
            redeployArg.append(VERTX_ARG_REDEPLOY); //fix for redeploy to work

            computeOutputDirsWildcard(redeployArg);

            argsList.add(redeployArg.toString());

            if (jvmArgs != null && !jvmArgs.isEmpty()) {
                String javaOpts = jvmArgs.stream().collect(Collectors.joining(" "));
                argsList.add(VERTX_ARG_JAVA_OPT + "=" + javaOpts);
            }
        }

        if (!VERTX_COMMAND_STOP.equals(vertxCommand)) {
            String argLauncherClass = VERTX_ARG_LAUNCHER_CLASS +
                "=\"" +
                launcher +
                "\"";
            argsList.add(argLauncherClass);

            if (config != null && config.exists() && config.isFile()) {
                getLog().info("Using configuration from file: " + config.toString());
                argsList.add(VERTX_ARG_CONF);
                argsList.add(config.toString());
            }
        }
    }

    private void computeOutputDirsWildcard(StringBuilder redeployArg) {
        final String wildcardClassesDir = this.classesDirectory.toString() + "/**/*";
        redeployArg.append(wildcardClassesDir);
    }

    /**
     * Method to check if the Launcher is {@link AbstractRunMojo#IO_VERTX_CORE_LAUNCHER} or instance of
     * {@code io.vertx.core.Launcher}
     *
     * @param launcher - the launcher class as string that needs to be checked
     * @return true if its {@link AbstractRunMojo#IO_VERTX_CORE_LAUNCHER} or instance of
     * {@code io.vertx.core.Launcher}
     * @throws MojoExecutionException - any error that might occur while checking
     */
    protected boolean isVertxLauncher(String launcher) throws MojoExecutionException {
        if (launcher != null) {
            if (IO_VERTX_CORE_LAUNCHER.equals(launcher)) {
                return true;
            } else {
                try {
                    Class customLauncher = buildClassLoader(getClassPathUrls()).loadClass(launcher);
                    List<Class<?>> superClasses = ClassUtils.getAllSuperclasses(customLauncher);
                    boolean isAssignable = false;
                    if (superClasses != null) {
                        for (Class<?> superClass : superClasses) {
                            if (IO_VERTX_CORE_LAUNCHER.equals(superClass.getName())) {
                                isAssignable = true;
                                break;
                            }
                        }
                    }
                    return isAssignable;
                } catch (ClassNotFoundException e) {
                    throw new MojoExecutionException("Class \"" + launcher + "\" not found");
                }
            }
        } else {
            return false;
        }
    }

    /**
     * This method will trigger the lauch of the applicaiton as non-forked, running in same JVM as maven.
     *
     * @param argsList - the arguments to be passed to the vertx launcher
     * @throws MojoExecutionException - any error that might occur while starting the process
     */

    protected void run(List<String> argsList) throws MojoExecutionException {
        JavaProcessExecutor vertxExecutor = new JavaProcessExecutor()
            .withJvmOpts(redeploy ? Collections.emptyList() : jvmArgs)
            .withArgs(argsList)
            .withClassPath(getClassPathUrls())
            .withLogger(getLog())
            .withWaitFor(true);
        try {


            //When redeploy is enabled spin up the Incremental builder in background
            if (redeploy && !(VERTX_COMMAND_START.equals(vertxCommand)
                || VERTX_COMMAND_STOP.equals(vertxCommand))) {
                getLog().debug("Collected mojos: " + MojoSpy.MOJOS);

                Set<Path> inclDirs = Collections
                    .singleton(new File(project.getBasedir(), "src/main").toPath());

                //TODO - handle exceptions effectively
                // TODO - Not sure about the runAsync here, it uses the default fork join pool
                CompletableFuture.runAsync(() -> {
                    List<Callable<Void>> chain = computeExecutionChain();
                    IncrementalBuilder incrementalBuilder = new IncrementalBuilder(inclDirs,
                        chain, getLog(), 1000L);
                    incrementalBuilder.run();
                });

            }

            vertxExecutor.execute();

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to launch incremental builder", e);
        }
    }

    private List<Callable<Void>> computeExecutionChain() {
        List<Callable<Void>> list = new ArrayList<>();
        if (MojoSpy.MOJOS.isEmpty()) {
            getLog().info("No plugin execution collected. The vertx:initialize goal has not " +
                "been run beforehand. Only handling resources and java compilation");
            list.add(new JavaBuildCallback());
            list.add(new ResourceBuildCallback());
        } else {
            list = MojoSpy.MOJOS.stream()
                // Include only mojo in [generate-source, process-classes]
                .filter(exec -> MojoSpy.PHASES.contains(exec.getLifecyclePhase()))
                .map(this::toTask)
                .collect(Collectors.toList());

        }
        return list;
    }

    private Callable<Void> toTask(MojoExecution execution) {
        MojoExecutor executor = new MojoExecutor(execution, project, mavenSession, buildPluginManager);

        return () -> {
            try {
                executor.execute();
            } catch (Exception e) {
                getLog().error("Error while doing incremental build", e);
            }
            return null;
        };
    }

    /**
     * This method to load Vert.X application configurations.
     * This will use the pattern ${basedir}/src/main/conf/application.[json/yaml/yml]
     */
    protected void scanAndLoadConfigs() throws MojoExecutionException {

        Path confBaseDir = Paths.get(this.project.getBasedir().toString(), "src", "main", "conf");

        if (Files.exists(confBaseDir) && Files.isDirectory(confBaseDir)) {

            DirectoryScanner directoryScanner = new DirectoryScanner();
            directoryScanner.setBasedir(this.project.getBasedir() + DEFAULT_CONF_DIR);
            directoryScanner.setIncludes(WILDCARD_CONFIG_FILES);
            directoryScanner.scan();

            String[] configFiles = directoryScanner.getIncludedFiles();

            if (configFiles != null && configFiles.length != 0) {
                String configFile = configFiles[0];
                Path confPath = Paths.get(confBaseDir.toFile().toString(), configFile);
                //Check if its JSON
                if (isJson(configFile)) {
                    config = confPath.toFile();
                } else if (isYaml(configFile)) {
                    //Check if its YAML or YML
                    Path jsonConfDir = Paths.get(this.projectBuildDir, "conf");
                    jsonConfDir.toFile().mkdirs();
                    Path jsonConfPath = Paths.get(jsonConfDir.toString(), VERTX_CONFIG_FILE_JSON);
                    try {
                        if (Files.createFile(jsonConfPath).toFile().exists()) {
                            ConfigConverterUtil.convertYamlToJson(confPath, jsonConfPath);
                            config = jsonConfPath.toFile();
                        }
                    } catch (IOException e) {
                        throw new MojoExecutionException("Error loading configuration file:" + confPath.toString(), e);
                    } catch (Exception e) {
                        throw new MojoExecutionException("Error loading and converting configuration file:"
                            + confPath.toString(), e);
                    }
                }
            }
        }
    }

    /**
     * This will build the {@link URLClassLoader} object from the collection of classpath URLS
     *
     * @param classPathUrls - the classpath urls which will be used to build the {@link URLClassLoader}
     * @return an instance of {@link URLClassLoader}
     * @throws MojoExecutionException - any error that might occur while building the {@link URLClassLoader}
     */
    protected ClassLoader buildClassLoader(Collection<URL> classPathUrls) throws MojoExecutionException {
        return new URLClassLoader(classPathUrls.toArray(new URL[classPathUrls.size()]));
    }

    /**
     * This will resolve the project's test and runtime dependencies along with classes directory, resources directory
     * to the collection of classpath urls
     *
     * @return @{link {@link List<URL>}} which will have all the dependencies, classes directory, resources directory etc.,
     * @throws MojoExecutionException any error that might occur while building collection like resolution errors
     */
    protected List<URL> getClassPathUrls() throws MojoExecutionException {
        List<URL> classPathUrls = new ArrayList<>();

        try {
            addProjectResources(classPathUrls);
            addClassesDirectory(classPathUrls);

            Set<Optional<File>> compileAndRuntimeDeps = extractArtifactPaths(this.project.getDependencyArtifacts());

            Set<Optional<File>> transitiveDeps = extractArtifactPaths(this.project.getArtifacts());

            classPathUrls.addAll(Stream.concat(compileAndRuntimeDeps.stream(), transitiveDeps.stream())
                .filter(Optional::isPresent)
                .map(file -> {
                    try {
                        return file.get().toURI().toURL();
                    } catch (Exception e) {
                        getLog().error("Error building classpath", e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        } catch (IOException e) {
            throw new MojoExecutionException("Unable to run:", e);
        }
        return classPathUrls;
    }


    /**
     * Method to check if the file is JSON file
     *
     * @param configFile - the config file to be checked
     * @return if its json file e.g. application.json
     */
    private boolean isJson(String configFile) {
        return configFile != null && configFile.endsWith(".json");
    }

    /**
     * Method to check if the file is YAML file
     *
     * @param configFile - the config file to be checked
     * @return if its YAML file e.g. application.yml or application.yml
     */
    private boolean isYaml(String configFile) {
        return configFile != null && (configFile.endsWith(".yaml") || configFile.endsWith(".yml"));
    }

    /**
     *
     */
    public final class JavaBuildCallback implements Callable<Void> {

        @Override
        public Void call() {
            try {
                MojoUtils.compile(project, mavenSession, buildPluginManager);
            } catch (Exception e) {
                getLog().error("Error while doing incremental Java build: " + e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     *
     */
    public final class ResourceBuildCallback implements Callable<Void> {

        @Override
        public Void call() {
            try {
                MojoUtils.copyResources(project, mavenSession, buildPluginManager);
            } catch (Exception e) {
                getLog().error("Error while doing incremental resource processing: " + e.getMessage(), e);
            }

            return null;
        }
    }

}
