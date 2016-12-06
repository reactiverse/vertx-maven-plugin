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

import io.fabric8.vertx.maven.plugin.utils.MojoUtils;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.twdata.maven.mojoexecutor.MojoExecutor.dependency;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

/**
 * This Goal helps in setting up Vert.x maven project with vertx-maven-plugin, with sane defaults
 */
@Mojo(name = "setup")
public class SetupMojo extends AbstractMojo {

    final String PLUGIN_GROUPID = "io.fabric8";
    final String PLUGIN_ARTIFACTID = "vertx-maven-plugin";
    final String VERTX_MAVEN_PLUGIN_VERSION_PROPERTY = "vertx-maven-plugin-version";
    final MojoUtils mojoUtils = new MojoUtils();

    /**
     * The Maven project which will define and confiure the vertx-maven-plugin
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        //We should get cloned of the OriginalModel, as project.getModel will return effective model
        Model model = project.getOriginalModel().clone();
        File pomFile = project.getFile();

        Optional<Plugin> vmPlugin = mojoUtils.hasPlugin(project, "io.fabric8:vertx-maven-plugin");

        if (!vmPlugin.isPresent()) {
            try {

                //Set  a property at maven project level for vert.x  and vert.x maven plugin versions
                model.getProperties().putIfAbsent("fabric8.vertx.plugin.version",
                    mojoUtils.getVersion(VERTX_MAVEN_PLUGIN_VERSION_PROPERTY));
                model.getProperties().putIfAbsent("vertx.version", mojoUtils.getVersion("vertx-core-version"));

                //Add Vert.x BOM
                addVertxBom(model);

                //Add Vert.x Core Dependency
                addVertxDependencies(model);

                Plugin vertxMavenPlugin = plugin(PLUGIN_GROUPID, PLUGIN_ARTIFACTID, "${fabric8.vertx.plugin.version}");

                if (isParentPom(model)) {
                    if (model.getBuild().getPluginManagement() != null) {
                        if (model.getBuild().getPluginManagement().getPlugins() == null) {
                            model.getBuild().getPluginManagement().setPlugins(new ArrayList<>());
                        }
                        model.getBuild().getPluginManagement().getPlugins().add(vertxMavenPlugin);
                    }
                    //strip the version off
                    vertxMavenPlugin = plugin(PLUGIN_GROUPID, PLUGIN_ARTIFACTID);
                } else {
                    vertxMavenPlugin = plugin(PLUGIN_GROUPID, PLUGIN_ARTIFACTID, "${fabric8.vertx.plugin.version}");
                }

                PluginExecution pluginExec = new PluginExecution();
                pluginExec.addGoal("initialize");
                pluginExec.addGoal("package");
                pluginExec.setId("vmp-init-package");
                vertxMavenPlugin.addExecution(pluginExec);

                if (model.getBuild().getPlugins() == null) {
                    model.getBuild().setPlugins(new ArrayList<>());
                }

                model.getBuild().getPlugins().add(vertxMavenPlugin);

                MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
                final FileWriter pomFileWriter = new FileWriter(pomFile);
                xpp3Writer.write(pomFileWriter, model);

                pomFileWriter.flush();
                pomFileWriter.close();
            } catch (IOException e) {
                throw new MojoExecutionException("Error while setup of vertx-maven-plugin", e);
            }
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

            if (!mojoUtils.hasDependency(project, "io.vertx", "vertx-core")) {
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
        Dependency vertxBom = dependency("io.vertx", "vertx-dependencies", "${vertx.version}");
        vertxBom.setType("pom");
        vertxBom.setScope("import");

        if (model.getDependencyManagement() != null) {
            if (!mojoUtils.hasDependency(project, "io.vertx", "vertx-dependencies")) {
                model.getDependencyManagement().addDependency(vertxBom);
            }
        } else {
            DependencyManagement depsMgmt = new DependencyManagement();
            depsMgmt.addDependency(vertxBom);
            model.setDependencyManagement(depsMgmt);
        }
    }

}
