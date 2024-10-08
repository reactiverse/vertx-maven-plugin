/*
 *
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

import io.reactiverse.vertx.maven.plugin.components.Prompter;
import io.reactiverse.vertx.maven.plugin.utils.MojoUtils;
import io.reactiverse.vertx.maven.plugin.utils.SetupTemplateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static org.twdata.maven.mojoexecutor.MojoExecutor.dependency;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

/**
 * This Goal helps in setting up Vert.x maven project with vertx-maven-plugin, with sane defaults
 */
@Mojo(name = "setup", requiresProject = false)
public class SetupMojo extends AbstractMojo {

    public static final String JAVA_EXTENSION = ".java";
    public static final String VERTX_CORE_VERSION = "vertx-core-version";
    public static final String VERTX_GROUP_ID = "io.vertx";
    public static final String VERTX_BOM_ARTIFACT_ID = "vertx-stack-depchain";
    public static final String VERTX_CORE_ARTIFACT_ID = "vertx-core";
    public static final String VERTX_LAUNCHER_APPLICATION_ARTIFACT_ID = "vertx-launcher-application";

    private static final String PLUGIN_GROUP_ID = "io.reactiverse";
    private static final String PLUGIN_ARTIFACT_ID = "vertx-maven-plugin";
    private static final String VERTX_MAVEN_PLUGIN_VERSION_PROPERTY = "vertx-maven-plugin-version";

    /**
     * The Maven Session.
     */
    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    /**
     * The Maven project which will define and configure the vertx-maven-plugin
     */
    @Parameter(defaultValue = "${project}")
    protected MavenProject project;

    @Parameter(property = "projectGroupId")
    protected String projectGroupId;

    @Parameter(property = "projectArtifactId")
    protected String projectArtifactId;

    @Parameter(property = "projectVersion", defaultValue = "1.0-SNAPSHOT")
    protected String projectVersion;

    @Parameter(property = "vertxBom", defaultValue = VERTX_BOM_ARTIFACT_ID)
    protected String vertxBom;

    @Parameter(property = "vertxVersion")
    protected String vertxVersion;

    @Parameter(property = "verticle")
    protected String verticle;

    @Parameter(property = "javaVersion")
    protected String javaVersion;

    @Component
    protected Prompter prompter;

    @Override
    public void execute() throws MojoExecutionException {
        File pomFile = project.getFile();

        Model model;

        //Create pom.xml if not
        if (pomFile == null || !pomFile.isFile()) {
            pomFile = createPomFileFromUserInputs();
        }

        //We should get cloned of the OriginalModel, as project.getModel will return effective model
        model = project.getOriginalModel().clone();

        vertxVersion = vertxVersion == null ? MojoUtils.getVersion(VERTX_CORE_VERSION) : vertxVersion;

        createDirectories();
        SetupTemplateUtils.createVerticle(project, vertxVersion, verticle, getLog());

        Optional<Plugin> vmPlugin = MojoUtils.hasPlugin(project, PLUGIN_GROUP_ID + ":" + PLUGIN_ARTIFACT_ID);
        if (vmPlugin.isPresent()) {
            return;
        }

        //Set  a property at maven project level for vert.x  and vert.x maven plugin versions
        model.getProperties().putIfAbsent("vertx-maven-plugin.version", MojoUtils.getVersion(VERTX_MAVEN_PLUGIN_VERSION_PROPERTY));

        model.getProperties().putIfAbsent("vertx.version", vertxVersion);
        if (!StringUtils.isBlank(verticle)) {
            if (verticle.endsWith(JAVA_EXTENSION)) {
                verticle = verticle.substring(0, verticle.length() - JAVA_EXTENSION.length());
            }
            model.getProperties().putIfAbsent("vertx.verticle", verticle);
        }

        //Add Vert.x BOM
        addVertxBom(model);

        //Add Vert.x Dependencies
        Stream.Builder<String> deps = Stream.<String>builder().add(VERTX_CORE_ARTIFACT_ID);
        if (vertxVersion.startsWith("5.")) {
            deps.add(VERTX_LAUNCHER_APPLICATION_ARTIFACT_ID);
        }
        addVertxDependencies(model, deps.build().collect(toCollection(LinkedHashSet::new)));

        Plugin vertxMavenPlugin = plugin(PLUGIN_GROUP_ID, PLUGIN_ARTIFACT_ID, "${vertx-maven-plugin.version}");

        if (isParentPom(model)) {
            addPluginManagementSection(model, vertxMavenPlugin);
            //strip the vertxVersion off
            vertxMavenPlugin = plugin(PLUGIN_GROUP_ID, PLUGIN_ARTIFACT_ID);
        } else {
            vertxMavenPlugin = plugin(PLUGIN_GROUP_ID, PLUGIN_ARTIFACT_ID, "${vertx-maven-plugin.version}");
        }

        PluginExecution pluginExec = new PluginExecution();
        pluginExec.addGoal("initialize");
        pluginExec.addGoal("package");
        pluginExec.setId("vmp");
        vertxMavenPlugin.addExecution(pluginExec);

        Build build = createBuildSectionIfRequired(model);
        build.getPlugins().add(vertxMavenPlugin);

        save(pomFile, model);
    }

    private Build createBuildSectionIfRequired(Model model) {
        Build build = model.getBuild();
        if (build == null) {
            build = new Build();
            model.setBuild(build);
        }
        if (build.getPlugins() == null) {
            build.setPlugins(new ArrayList<>());
        }
        return build;
    }

    private void addPluginManagementSection(Model model, Plugin vertxMavenPlugin) {
        if (model.getBuild().getPluginManagement() != null) {
            if (model.getBuild().getPluginManagement().getPlugins() == null) {
                model.getBuild().getPluginManagement().setPlugins(new ArrayList<>());
            }
            model.getBuild().getPluginManagement().getPlugins().add(vertxMavenPlugin);
        }
    }

    private File createPomFileFromUserInputs() throws MojoExecutionException {
        File pomFile;
        Model model;
        String currentDirectory = System.getProperty("user.dir");
        getLog().info("Creating a new pom.xml file in: " + currentDirectory);
        pomFile = new File(currentDirectory, "pom.xml");

        try {
            projectGroupId = promptOrUseDefault(projectGroupId, "Set the project groupId", "io.vertx.example");
            projectArtifactId = promptOrUseDefault(projectArtifactId, "Set the project artifactId", "my-vertx-project");
            projectVersion = promptOrUseDefault(projectVersion, "Set the project version", "1.0-SNAPSHOT");
            vertxVersion = promptOrUseDefault(vertxVersion, "Set the Vert.x version", MojoUtils.getVersion(VERTX_CORE_VERSION));
            verticle = promptOrUseDefault(verticle, "Set the verticle class name", projectGroupId.replace("-", ".").replace("_", ".") + ".MainVerticle");
        } catch (IOException e) {
            throw new MojoExecutionException("Error while prompting setup config", e);
        }

        if (verticle != null && verticle.endsWith(JAVA_EXTENSION)) {
            verticle = verticle.substring(0, verticle.length() - JAVA_EXTENSION.length());
        }

        Map<String, String> context = new HashMap<>();
        context.put("mProjectGroupId", projectGroupId);
        context.put("mProjectArtifactId", projectArtifactId);
        context.put("mProjectVersion", projectVersion);
        context.put("vertxBom", vertxBom != null ? vertxBom : VERTX_BOM_ARTIFACT_ID);
        context.put("vertxVersion", vertxVersion != null ? vertxVersion : MojoUtils.getVersion(VERTX_CORE_VERSION));

        context.put("vertxVerticle", verticle);
        context.put("vmpVersion", MojoUtils.getVersion(VERTX_MAVEN_PLUGIN_VERSION_PROPERTY));

        context.put("javaVersion", javaVersion != null ? javaVersion : SystemUtils.JAVA_SPECIFICATION_VERSION);

        SetupTemplateUtils.createPom(context, pomFile);

        //The project should be recreated and set with right model
        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
        try (InputStream is = Files.newInputStream(pomFile.toPath())) {
            model = xpp3Reader.read(is);
        } catch (IOException | XmlPullParserException e) {
            throw new MojoExecutionException("Error while reading POM file model", e);
        }

        project = new MavenProject(model);
        project.setPomFile(pomFile);
        project.setOriginalModel(model); // the current model is the original model as well

        return pomFile;
    }

    private String promptOrUseDefault(String value, String msg, String defaultValue) throws IOException {
        if (value != null) {
            return value;
        }
        if (mavenSession.getRequest().isInteractiveMode()) {
            return prompter.promptWithDefaultValue(msg, defaultValue);
        }
        return defaultValue;
    }

    private void save(File pomFile, Model model) throws MojoExecutionException {
        MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
        try (FileWriter pomFileWriter = new FileWriter(pomFile)) {
            xpp3Writer.write(pomFileWriter, model);
            pomFileWriter.flush();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write the pom.xml file", e);
        }
    }

    private void createDirectories() {
        File root = project.getBasedir();
        File source = new File(root, "src/main/java");
        File resources = new File(root, "src/main/resources");
        File test = new File(root, "src/test/java");

        String prefix = "Creation of ";
        if (!source.isDirectory()) {
            boolean res = source.mkdirs();
            getLog().debug(prefix + source.getAbsolutePath() + " : " + res);
        }
        if (!resources.isDirectory()) {
            boolean res = resources.mkdirs();
            getLog().debug(prefix + resources.getAbsolutePath() + " : " + res);
        }
        if (!test.isDirectory()) {
            boolean res = test.mkdirs();
            getLog().debug(prefix + test.getAbsolutePath() + " : " + res);
        }
    }


    private boolean isParentPom(Model model) {
        return "pom".equals(model.getPackaging());
    }

    /**
     * Method used to add the vert.x dependencies typically the vert.x core
     *
     * @param model - the {@code {@link Model}}
     */
    private void addVertxDependencies(Model model, LinkedHashSet<String> toAdd) {
        for (Dependency dependency : model.getDependencies()) {
            if (VERTX_GROUP_ID.equals(dependency.getGroupId())) {
                toAdd.remove(dependency.getArtifactId());
            }
        }
        for (String artifactId : toAdd) {
            model.getDependencies().add(dependency(VERTX_GROUP_ID, artifactId, null));
        }
    }


    /**
     * Method used to add the vert.x BOM
     *
     * @param model - the {@code {@link Model}}
     */
    private void addVertxBom(Model model) {
        DependencyManagement dependencyManagement = model.getDependencyManagement();
        if (dependencyManagement != null) {
            for (Dependency dependency : dependencyManagement.getDependencies()) {
                if (VERTX_GROUP_ID.equals(dependency.getGroupId()) && vertxBom.equals(dependency.getArtifactId())) {
                    return;
                }
            }
        } else {
            dependencyManagement = new DependencyManagement();
            model.setDependencyManagement(dependencyManagement);
        }
        Dependency dependency = dependency(VERTX_GROUP_ID, vertxBom, "${vertx.version}");
        dependency.setType("pom");
        dependency.setScope("import");
        dependencyManagement.addDependency(dependency);
    }

}
