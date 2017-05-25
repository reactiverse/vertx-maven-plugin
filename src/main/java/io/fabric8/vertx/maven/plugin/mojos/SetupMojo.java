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

import com.google.common.base.Strings;
import io.fabric8.vertx.maven.plugin.dependencies.VertxDependencies;
import io.fabric8.vertx.maven.plugin.dependencies.VertxDependency;
import io.fabric8.vertx.maven.plugin.utils.MojoUtils;
import io.fabric8.vertx.maven.plugin.utils.Prompter;
import io.fabric8.vertx.maven.plugin.utils.SetupTemplateUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * This Goal helps in setting up Vert.x maven project with vertx-maven-plugin, with sane defaults
 */
@Mojo(name = "setup", requiresProject = false)
public class SetupMojo extends AbstractMojo {

    final String PLUGIN_GROUPID = "io.fabric8";
    final String PLUGIN_ARTIFACTID = "vertx-maven-plugin";
    final String VERTX_MAVEN_PLUGIN_VERSION_PROPERTY = "vertx-maven-plugin-version";

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

    @Parameter(property = "vertxVersion")
    protected String vertxVersion;

    @Parameter(property = "verticle")
    protected String verticle;

    @Parameter(property = "dependencies")
    protected List<String> dependencies;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        File pomFile = project.getFile();

        Model model;

        //Create pom.xml if not
        if (pomFile == null || !pomFile.isFile()) {
            String workingdDir = System.getProperty("user.dir");
            getLog().info("No pom.xml found, creating it in " + workingdDir);
            pomFile = new File(workingdDir, "pom.xml");
            Prompter prompter = new Prompter();
            prompter.startInteraction();
            try {

                if (projectGroupId == null) {
                    projectGroupId = prompter.prompt("Define value for property 'projectGroupId'",
                        "com.example.vertx");
                }

                // If the user does not specify the artifactId, we switch to the interactive mode.
                if (projectArtifactId == null) {
                    projectArtifactId = prompter.prompt("Define value for property 'projectArtifactId'",
                        "vertx-example");

                    // Ask for version only if we asked for the artifactId
                    projectVersion = prompter.prompt("Define value for property 'projectVersion'", projectVersion);

                    // Ask for maven version if not set
                    if (vertxVersion == null) {
                        vertxVersion = prompter.prompt("Define value for property 'vertx.projectVersion'",
                            MojoUtils.getVersion("vertx-core-version"));
                    }

                    if (verticle == null) {
                        verticle = prompter.prompt("Define value for property 'vertx.verticle'",
                            projectGroupId + ".MainVerticle");
                    }
                }

                if (verticle != null && verticle.endsWith(".java")) {
                    verticle = verticle.substring(0, verticle.length() - ".java".length());
                }

                Map<String, String> context = new HashMap<>();
                context.put("mProjectGroupId", projectGroupId);
                context.put("mProjectArtifactId", projectArtifactId);
                context.put("mProjectVersion", projectVersion);
                context.put("vertxVersion", vertxVersion != null ? vertxVersion : MojoUtils.getVersion("vertx-core-version"));

                context.put("vertxVerticle", verticle);
                context.put("fabric8VMPVersion", MojoUtils.getVersion(VERTX_MAVEN_PLUGIN_VERSION_PROPERTY));

                SetupTemplateUtils.createPom(context, pomFile);

                //The project should be recreated and set with right model
                MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();

                model = xpp3Reader.read(new FileInputStream(pomFile));
            } catch (Exception e) {
                throw new MojoExecutionException("Error while setup of vertx-maven-plugin", e);
            } finally {
                prompter.endInteraction();
            }

            project = new MavenProject(model);
            project.setPomFile(pomFile);
            project.setOriginalModel(model); // the current model is the original model as well

            // Add dependencies
            if (addDependencies(model)) {
                save(pomFile, model);
            }
        }

        //We should get cloned of the OriginalModel, as project.getModel will return effective model
        model = project.getOriginalModel().clone();

        createDirectories();
        SetupTemplateUtils.createVerticle(project, verticle, getLog());


        Optional<Plugin> vmPlugin = MojoUtils.hasPlugin(project, "io.fabric8:vertx-maven-plugin");

        if (!vmPlugin.isPresent()) {

            //Set  a property at maven project level for vert.x  and vert.x maven plugin versions
            model.getProperties().putIfAbsent("fabric8-vertx-maven-plugin.projectVersion",
                MojoUtils.getVersion(VERTX_MAVEN_PLUGIN_VERSION_PROPERTY));

            vertxVersion = vertxVersion == null ? MojoUtils.getVersion("vertx-core-version") : vertxVersion;

            model.getProperties().putIfAbsent("vertx.projectVersion", vertxVersion);
            if (!Strings.isNullOrEmpty(verticle)) {
                if (verticle.endsWith(".java")) {
                    verticle = verticle.substring(0, verticle.length() - ".java".length());
                }
                model.getProperties().putIfAbsent("vertx.verticle", verticle);
            }

            //Add Vert.x BOM
            addVertxBom(model);

            //Add Vert.x Core Dependency
            addVertxDependencies(model);
            // Add other dependencies
            addDependencies(model);

            Plugin vertxMavenPlugin = plugin(PLUGIN_GROUPID, PLUGIN_ARTIFACTID, "${fabric8-vertx-maven-plugin.projectVersion}");

            if (isParentPom(model)) {
                if (model.getBuild().getPluginManagement() != null) {
                    if (model.getBuild().getPluginManagement().getPlugins() == null) {
                        model.getBuild().getPluginManagement().setPlugins(new ArrayList<>());
                    }
                    model.getBuild().getPluginManagement().getPlugins().add(vertxMavenPlugin);
                }
                //strip the vertxVersion off
                vertxMavenPlugin = plugin(PLUGIN_GROUPID, PLUGIN_ARTIFACTID);
            } else {
                vertxMavenPlugin = plugin(PLUGIN_GROUPID, PLUGIN_ARTIFACTID, "${fabric8-vertx-maven-plugin.projectVersion}");
            }

            PluginExecution pluginExec = new PluginExecution();
            pluginExec.addGoal("initialize");
            pluginExec.addGoal("package");
            pluginExec.setId("vmp-init-package");
            vertxMavenPlugin.addExecution(pluginExec);

            //Plugin Configuration
            vertxMavenPlugin.setConfiguration(configuration(element("redeploy", "true")));

            Build build = model.getBuild();

            if (build == null) {
                build = new Build();
                model.setBuild(build);
            }

            if (build.getPlugins() == null) {
                build.setPlugins(new ArrayList<>());
            }

            build.getPlugins().add(vertxMavenPlugin);

            save(pomFile, model);
        }
    }

    private void save(File pomFile, Model model) throws MojoExecutionException {
        MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
        FileWriter pomFileWriter = null;
        try {
            pomFileWriter = new FileWriter(pomFile);
            xpp3Writer.write(pomFileWriter, model);

            pomFileWriter.flush();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write the pom.xml file", e);
        } finally {
            IOUtils.closeQuietly(pomFileWriter);
        }
    }

    private boolean addDependencies(Model model) {
        if (dependencies == null || dependencies.isEmpty()) {
            return false;
        }

        boolean updated = false;
        List<VertxDependency> deps = VertxDependencies.get();
        for (String dependency : this.dependencies) {
            Optional<VertxDependency> optional = deps.stream()
                .filter(d -> d.labels().contains(dependency.toLowerCase()))
                .findAny();

            if (optional.isPresent()) {
                getLog().info("Adding dependency " + optional.get().toCoordinates());
                model.addDependency(optional.get().toDependency());
                updated = true;
            } else if (dependency.contains(":")) {
                // Add it as a dependency
                // groupId:artifactId:version:classifier
                String[] segments = dependency.split(":");
                if (segments.length >= 2) {
                    Dependency d = new Dependency();
                    d.setGroupId(segments[0]);
                    d.setArtifactId(segments[1]);
                    if (segments.length >= 3  && ! segments[2].isEmpty()) {
                        d.setVersion(segments[2]);
                    }
                    if (segments.length >= 4) {
                        d.setClassifier(segments[3]);
                    }
                    getLog().info("Adding dependency " + d.getManagementKey());
                    model.addDependency(d);
                    updated = true;
                } else {
                    getLog().warn("Invalid dependency description '" + dependency + "'");
                }
            } else {
                getLog().warn("Cannot find a dependency matching '" + dependency + "'");
            }
        }

        return updated;
    }


    private void createDirectories() {
        File root = project.getBasedir();
        File source = new File(root, "src/main/java");
        File resources = new File(root, "src/main/resources");
        File test = new File(root, "src/test/java");

        if (!source.isDirectory()) {
            boolean res = source.mkdirs();
            getLog().debug("Creation of " + source.getAbsolutePath() + " : " + res);
        }
        if (!resources.isDirectory()) {
            boolean res = resources.mkdirs();
            getLog().debug("Creation of " + resources.getAbsolutePath() + " : " + res);
        }
        if (!test.isDirectory()) {
            boolean res = test.mkdirs();
            getLog().debug("Creation of " + test.getAbsolutePath() + " : " + res);
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
    private void addVertxDependencies(Model model) {
        if (model.getDependencies() != null) {

            if (!MojoUtils.hasDependency(project, "io.vertx", "vertx-core")) {
                model.getDependencies().add(
                    dependency("io.vertx", "vertx-core", null));
            }
        } else {
            model.setDependencies(new ArrayList<>());
            model.getDependencies().add(dependency("io.vertx", "vertx-core", null));
        }
    }


    /**
     * Method used to add the vert.x dependencies BOM
     *
     * @param model - the {@code {@link Model}}
     */
    private void addVertxBom(Model model) {
        Dependency vertxBom = dependency("io.vertx", "vertx-dependencies", "${vertx.projectVersion}");
        vertxBom.setType("pom");
        vertxBom.setScope("import");

        if (model.getDependencyManagement() != null) {
            if (!MojoUtils.hasDependency(project, "io.vertx", "vertx-dependencies")) {
                model.getDependencyManagement().addDependency(vertxBom);
            }
        } else {
            DependencyManagement depsMgmt = new DependencyManagement();
            depsMgmt.addDependency(vertxBom);
            model.setDependencyManagement(depsMgmt);
        }
    }

}
