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

package io.fabric8.vertx.maven.plugin;

import io.fabric8.vertx.maven.plugin.utils.MojoUtils;
import io.fabric8.vertx.maven.plugin.utils.SetupTemplateUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

public class SetupMojoTest {


    @Before
    public void setUp() {
        File junk = new File("target/junk");
        FileUtils.deleteQuietly(junk);
    }

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

        vertxMavenPlugin.setConfiguration(configuration(element("redeploy", "true")));

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
        Plugin vmp = project.getPlugin("io.fabric8:vertx-maven-plugin");
        assertNotNull(vmp);
        Xpp3Dom pluginConfig = (Xpp3Dom) vmp.getConfiguration();
        assertNotNull(pluginConfig);
        String redeploy = pluginConfig.getChild("redeploy").getValue();
        assertNotNull(redeploy);
        assertTrue(Boolean.valueOf(redeploy));
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

        vertxMavenPlugin.setConfiguration(configuration(element("redeploy", "true")));

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
        Plugin vmp = project.getPlugin("io.fabric8:vertx-maven-plugin");
        assertNotNull(vmp);
        Xpp3Dom pluginConfig = (Xpp3Dom) vmp.getConfiguration();
        assertNotNull(pluginConfig);
        String redeploy = pluginConfig.getChild("redeploy").getValue();
        assertNotNull(redeploy);
        assertTrue(Boolean.valueOf(redeploy));
    }

    @Test
    public void testAddVertxMavenPluginWithVertxVersion() throws Exception {

        System.setProperty("vertxVersion", "3.4.0");

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

        vertxMavenPlugin.setConfiguration(configuration(element("redeploy", "true")));

        model.getBuild().getPlugins().add(vertxMavenPlugin);

        String vertxVersion = System.getProperty("vertxVersion") == null ? MojoUtils.getVersion("vertx-core-version") :
            System.getProperty("vertxVersion");

        model.getProperties().putIfAbsent("vertx.version", vertxVersion);

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
        Plugin vmp = project.getPlugin("io.fabric8:vertx-maven-plugin");
        assertNotNull(vmp);
        Xpp3Dom pluginConfig = (Xpp3Dom) vmp.getConfiguration();
        assertNotNull(pluginConfig);
        String redeploy = pluginConfig.getChild("redeploy").getValue();
        assertNotNull(redeploy);
        assertTrue(Boolean.valueOf(redeploy));
        Properties projectProps = project.getProperties();
        assertNotNull(projectProps);
        assertFalse(projectProps.isEmpty());
        assertEquals(projectProps.getProperty("vertx.version"), vertxVersion);
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

    @Test
    public void testVerticleCreation() throws MojoExecutionException {
        MavenProject mock = mock(MavenProject.class);
        Log log = mock(Log.class);
        when(mock.getBasedir()).thenReturn(new File("target/junk"));

        SetupTemplateUtils.createVerticle(mock, "me.demo.Foo.java", log);
        SetupTemplateUtils.createVerticle(mock, "me.demo.Bar", log);
        SetupTemplateUtils.createVerticle(mock, "Baz.java", log);
        SetupTemplateUtils.createVerticle(mock, "Bob", log);

        assertThat(new File("target/junk/src/main/java/me/demo/Foo.java")).isFile();
        assertThat(new File("target/junk/src/main/java/me/demo/Bar.java")).isFile();
        assertThat(new File("target/junk/src/main/java/Baz.java")).isFile();
        assertThat(new File("target/junk/src/main/java/Bob.java")).isFile();
    }

    @Test
    public void testVerticleCreatPom() throws Exception {

        new File("target/unit/nopom").mkdirs();

        File pomFile = new File("target/unit/nopom/pom.xml");

        String vertxVersion = MojoUtils.getVersion("vertx-core-version");

        Map<String, String> tplContext = new HashMap<>();
        tplContext.put("mProjectGroupId", "com.example.vertx");
        tplContext.put("mProjectArtifactId", "vertx-example");
        tplContext.put("mProjectVersion", "1.0-SNAPSHOT");
        tplContext.put("vertxVersion", vertxVersion);
        tplContext.put("vertxVerticle", "com.example.vertx.MainVerticle");
        tplContext.put("fabric8VMPVersion", MojoUtils.getVersion("vertx-maven-plugin-version"));
        SetupTemplateUtils.createPom(tplContext, pomFile);

        assertThat(pomFile).isFile();

        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
        Model model = xpp3Reader.read(new FileInputStream(pomFile));
        MavenProject project = new MavenProject(model);
        Optional<Plugin> vmPlugin = MojoUtils.hasPlugin(project, "io.fabric8:vertx-maven-plugin");
        assertTrue(vmPlugin.isPresent());
        Plugin vmp = project.getPlugin("io.fabric8:vertx-maven-plugin");
        assertNotNull(vmp);
        Xpp3Dom pluginConfig = (Xpp3Dom) vmp.getConfiguration();
        assertNotNull(pluginConfig);
        String redeploy = pluginConfig.getChild("redeploy").getValue();
        assertNotNull(redeploy);
        assertTrue(Boolean.valueOf(redeploy));
        Properties projectProps = project.getProperties();
        assertNotNull(projectProps);
        assertFalse(projectProps.isEmpty());
        assertEquals(project.getGroupId(), "com.example.vertx");
        assertEquals(project.getArtifactId(), "vertx-example");
        assertEquals(project.getVersion(), "1.0-SNAPSHOT");
        assertEquals(projectProps.getProperty("vertx.version"), vertxVersion);

        assertThat(projectProps.getProperty("vertx-maven-plugin.version"))
            .contains(MojoUtils.getVersion("vertx-maven-plugin-version"));
        assertEquals(projectProps.getProperty("vertx.verticle"), "com.example.vertx.MainVerticle");
        DependencyManagement dependencyManagement = project.getDependencyManagement();
        assertThat(dependencyManagement).isNotNull();
        assertThat(dependencyManagement.getDependencies()).isNotEmpty();
        Dependency vertxBom = project.getDependencyManagement().getDependencies().get(0);
        assertThat(vertxBom.getGroupId()).isEqualTo("io.vertx");
        assertThat(vertxBom.getArtifactId()).isEqualTo("vertx-dependencies");
        assertThat(vertxBom.getVersion()).isEqualTo("${vertx.version}");
        assertThat(vertxBom.getType()).isEqualTo("pom");
        assertThat(vertxBom.getScope()).isEqualTo("import");

        assertThat(project.getDependencies()).isNotEmpty();
        Dependency vertxCore =project.getDependencies().get(0);
        assertThat(vertxCore).isNotNull();
        assertThat(vertxCore.getGroupId()).isEqualTo("io.vertx");
        assertThat(vertxCore.getArtifactId()).isEqualTo("vertx-core");
    }
}
