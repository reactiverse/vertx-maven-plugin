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

package io.fabric8.vertx.maven.plugin;

import io.fabric8.vertx.maven.plugin.utils.MojoUtils;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.dependency;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

public class SetupMojoTest {


    @Test
    public void testAddVertxMavenPlugin() throws Exception {
        InputStream pomFile = getClass().getResourceAsStream("/unit/setup/vmp-setup-pom.xml");
        assertNotNull(pomFile);
        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
        Model model = xpp3Reader.read(pomFile);

        MavenProject project = new MavenProject(model);

        Optional<Plugin> vmPlugin = MojoUtils.hasPlugin(project, "io.fabric8:vertx-maven-plugin");
        assertFalse(vmPlugin.isPresent());

        Plugin vertxMavenPlugin = plugin("io.fabric8", "vertx-maven-plugin",
            MojoUtils.getVersion("vertx-maven-plugin-version"));

        PluginExecution pluginExec = new PluginExecution();
        pluginExec.addGoal("initialize");
        pluginExec.addGoal("package");
        pluginExec.setId("vmp-init-package");
        vertxMavenPlugin.addExecution(pluginExec);

        model.getBuild().getPlugins().add(vertxMavenPlugin);

        model.getProperties().putIfAbsent("vertx.version", MojoUtils.getVersion("vertx-core-version"));

        Dependency vertxBom = dependency("io.vertx", "vertx-dependencies", "${vertx.version}");
        vertxBom.setType("pom");
        vertxBom.setScope("import");

        addDependencies(model, vertxBom);

        MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
        StringWriter updatedPom = new StringWriter();

        xpp3Writer.write(updatedPom, model);
        updatedPom.flush();
        updatedPom.close();

        //System.out.println(updatedPom);

        //Check if it has been added

        model = xpp3Reader.read(new StringReader(updatedPom.toString()));
        project = new MavenProject(model);
        vmPlugin = MojoUtils.hasPlugin(project, "io.fabric8:vertx-maven-plugin");
        assertTrue(vmPlugin.isPresent());
    }

    @Test
    public void testAddVertxMavenPluginWithNoBuildPom() throws Exception {
        InputStream pomFile = getClass().getResourceAsStream("/unit/setup/vmp-setup-nobuild-pom.xml");
        assertNotNull(pomFile);
        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
        Model model = xpp3Reader.read(pomFile);

        MavenProject project = new MavenProject(model);

        Optional<Plugin> vmPlugin = MojoUtils.hasPlugin(project, "io.fabric8:vertx-maven-plugin");
        assertFalse(vmPlugin.isPresent());

        Plugin vertxMavenPlugin = plugin("io.fabric8", "vertx-maven-plugin",
            MojoUtils.getVersion("vertx-maven-plugin-version"));

        PluginExecution pluginExec = new PluginExecution();
        pluginExec.addGoal("initialize");
        pluginExec.addGoal("package");
        pluginExec.setId("vmp-init-package");
        vertxMavenPlugin.addExecution(pluginExec);

        Build build = new Build();
        model.setBuild(build);
        build.getPlugins().add(vertxMavenPlugin);

        model.getProperties().putIfAbsent("vertx.version", MojoUtils.getVersion("vertx-core-version"));

        Dependency vertxBom = dependency("io.vertx", "vertx-dependencies", "${vertx.version}");
        vertxBom.setType("pom");
        vertxBom.setScope("import");

        addDependencies(model, vertxBom);

        MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
        StringWriter updatedPom = new StringWriter();

        xpp3Writer.write(updatedPom, model);
        updatedPom.flush();
        updatedPom.close();

        //System.out.println(updatedPom);

        //Check if it has been added

        model = xpp3Reader.read(new StringReader(updatedPom.toString()));
        project = new MavenProject(model);
        vmPlugin = MojoUtils.hasPlugin(project, "io.fabric8:vertx-maven-plugin");
        assertTrue(vmPlugin.isPresent());
    }

    private void addDependencies(Model model, Dependency vertxBom) {
        if (model.getDependencyManagement() != null) {
            Optional<Dependency> vertxCore =
                model.getDependencyManagement().getDependencies().stream()
                    .filter(dependency -> dependency.getArtifactId().equals("vertx-dependencies")).findFirst();
            model.getDependencyManagement().addDependency(vertxBom);
        } else {
            DependencyManagement depsMgmt = new DependencyManagement();
            depsMgmt.addDependency(vertxBom);
            model.setDependencyManagement(depsMgmt);
        }

        if (model.getDependencies() != null) {
            Optional<Dependency> vertxCore =
                model.getDependencies().stream()
                    .filter(dependency -> dependency.getArtifactId().equals("vertx-core")).findFirst();
            if (!vertxCore.isPresent()) {
                model.getDependencies().add(dependency("io.vertx", "vertx-core", null));
            }
        } else {
            model.setDependencies(new ArrayList<>());
            model.getDependencies().add(dependency("io.vertx", "vertx-core", null));
        }
    }
}
