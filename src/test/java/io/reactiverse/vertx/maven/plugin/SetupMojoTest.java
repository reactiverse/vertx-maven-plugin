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

package io.reactiverse.vertx.maven.plugin;

import io.reactiverse.vertx.maven.plugin.utils.MojoUtils;
import io.reactiverse.vertx.maven.plugin.utils.SetupTemplateUtils;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.twdata.maven.mojoexecutor.MojoExecutor.dependency;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

public class SetupMojoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File baseDir;

    @Before
    public void setUp() throws Exception {
        baseDir = temporaryFolder.newFolder();
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty("vertxVersion");
    }

    @Test
    public void testAddVertxMavenPlugin() throws Exception {
        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();

        Model model;
        try (InputStream pomFile = getClass().getResourceAsStream("/unit/setup/vmp-setup-pom.xml")) {
            assertNotNull(pomFile);
            model = xpp3Reader.read(pomFile);
        }

        MavenProject project = new MavenProject(model);

        Optional<Plugin> vmPlugin = MojoUtils.hasPlugin(project, "io.reactiverse:vertx-maven-plugin");
        assertFalse(vmPlugin.isPresent());

        Plugin vertxMavenPlugin = plugin("io.reactiverse", "vertx-maven-plugin",
            MojoUtils.getVersion("vertx-maven-plugin-version"));

        PluginExecution pluginExec = new PluginExecution();
        pluginExec.addGoal("initialize");
        pluginExec.addGoal("package");
        pluginExec.setId("vmp");
        vertxMavenPlugin.addExecution(pluginExec);

        model.getBuild().getPlugins().add(vertxMavenPlugin);

        model.getProperties().putIfAbsent("vertx.version", MojoUtils.getVersion("vertx-core-version"));

        Dependency vertxBom = dependency("io.vertx", "vertx-stack-depchain", "${vertx.version}");
        vertxBom.setType("pom");
        vertxBom.setScope("import");

        addDependencies(model, vertxBom);

        MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
        StringWriter updatedPom = new StringWriter();

        xpp3Writer.write(updatedPom, model);
        updatedPom.flush();
        updatedPom.close();

        //Check if it has been added

        model = xpp3Reader.read(new StringReader(updatedPom.toString()));
        project = new MavenProject(model);
        vmPlugin = MojoUtils.hasPlugin(project, "io.reactiverse:vertx-maven-plugin");
        assertTrue(vmPlugin.isPresent());
        Plugin vmp = project.getPlugin("io.reactiverse:vertx-maven-plugin");
        assertNotNull(vmp);
        Xpp3Dom pluginConfig = (Xpp3Dom) vmp.getConfiguration();
        assertNull(pluginConfig);
    }

    @Test
    public void testAddVertxMavenPluginWithNoBuildPom() throws Exception {
        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();

        Model model;
        try (InputStream pomFile = getClass().getResourceAsStream("/unit/setup/vmp-setup-nobuild-pom.xml")) {
            assertNotNull(pomFile);
            model = xpp3Reader.read(pomFile);
        }

        MavenProject project = new MavenProject(model);

        Optional<Plugin> vmPlugin = MojoUtils.hasPlugin(project, "io.reactiverse:vertx-maven-plugin");
        assertFalse(vmPlugin.isPresent());

        Plugin vertxMavenPlugin = plugin("io.reactiverse", "vertx-maven-plugin", MojoUtils.getVersion("vertx-maven-plugin-version"));

        PluginExecution pluginExec = new PluginExecution();
        pluginExec.addGoal("initialize");
        pluginExec.addGoal("package");
        pluginExec.setId("vmp");
        vertxMavenPlugin.addExecution(pluginExec);

        Build build = new Build();
        model.setBuild(build);
        build.getPlugins().add(vertxMavenPlugin);

        model.getProperties().putIfAbsent("vertx.version", MojoUtils.getVersion("vertx-core-version"));

        Dependency vertxBom = dependency("io.vertx", "vertx-stack-depchain", "${vertx.version}");
        vertxBom.setType("pom");
        vertxBom.setScope("import");

        addDependencies(model, vertxBom);

        MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
        StringWriter updatedPom = new StringWriter();

        xpp3Writer.write(updatedPom, model);
        updatedPom.flush();
        updatedPom.close();

        //Check if it has been added

        model = xpp3Reader.read(new StringReader(updatedPom.toString()));
        project = new MavenProject(model);
        vmPlugin = MojoUtils.hasPlugin(project, "io.reactiverse:vertx-maven-plugin");
        assertTrue(vmPlugin.isPresent());
        Plugin vmp = project.getPlugin("io.reactiverse:vertx-maven-plugin");
        assertNotNull(vmp);
        Xpp3Dom pluginConfig = (Xpp3Dom) vmp.getConfiguration();
        assertNull(pluginConfig);
    }

    @Test
    public void testAddVertxMavenPluginWithVertxVersion() throws Exception {
        System.setProperty("vertxVersion", "4.3.1");
        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();

        Model model;
        try (InputStream pomFile = getClass().getResourceAsStream("/unit/setup/vmp-setup-pom.xml")) {
            assertNotNull(pomFile);
            model = xpp3Reader.read(pomFile);
        }

        MavenProject project = new MavenProject(model);

        Optional<Plugin> vmPlugin = MojoUtils.hasPlugin(project, "io.reactiverse:vertx-maven-plugin");
        assertFalse(vmPlugin.isPresent());

        Plugin vertxMavenPlugin = plugin("io.reactiverse", "vertx-maven-plugin", MojoUtils.getVersion("vertx-maven-plugin-version"));

        PluginExecution pluginExec = new PluginExecution();
        pluginExec.addGoal("initialize");
        pluginExec.addGoal("package");
        pluginExec.setId("vmp");
        vertxMavenPlugin.addExecution(pluginExec);

        model.getBuild().getPlugins().add(vertxMavenPlugin);

        String vertxVersion = System.getProperty("vertxVersion") == null ? MojoUtils.getVersion("vertx-core-version") : System.getProperty("vertxVersion");

        model.getProperties().putIfAbsent("vertx.version", vertxVersion);

        Dependency vertxBom = dependency("io.vertx", "vertx-stack-depchain", "${vertx.version}");
        vertxBom.setType("pom");
        vertxBom.setScope("import");

        addDependencies(model, vertxBom);

        MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
        StringWriter updatedPom = new StringWriter();

        xpp3Writer.write(updatedPom, model);
        updatedPom.flush();
        updatedPom.close();

        //Check if it has been added

        model = xpp3Reader.read(new StringReader(updatedPom.toString()));
        project = new MavenProject(model);
        vmPlugin = MojoUtils.hasPlugin(project, "io.reactiverse:vertx-maven-plugin");
        assertTrue(vmPlugin.isPresent());
        Plugin vmp = project.getPlugin("io.reactiverse:vertx-maven-plugin");
        assertNotNull(vmp);
        Xpp3Dom pluginConfig = (Xpp3Dom) vmp.getConfiguration();
        assertNull(pluginConfig);
        Properties projectProps = project.getProperties();
        assertNotNull(projectProps);
        assertFalse(projectProps.isEmpty());
        assertEquals(projectProps.getProperty("vertx.version"), vertxVersion);
    }


    private void addDependencies(Model model, Dependency vertxBom) {
        if (model.getDependencyManagement() != null) {
            Optional<Dependency> vertxCore =
                model.getDependencyManagement().getDependencies().stream()
                    .filter(dependency -> dependency.getArtifactId().equals("vertx-stack-depchain")).findFirst();
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
        when(mock.getBasedir()).thenReturn(baseDir);

        SetupTemplateUtils.createVerticle(mock, "me.demo.Foo.java", log);
        SetupTemplateUtils.createVerticle(mock, "me.demo.Bar", log);
        SetupTemplateUtils.createVerticle(mock, "Baz.java", log);
        SetupTemplateUtils.createVerticle(mock, "Bob", log);

        assertThat(new File(baseDir, "src/main/java/me/demo/Foo.java")).isFile();
        assertThat(new File(baseDir, "src/main/java/me/demo/Bar.java")).isFile();
        assertThat(new File(baseDir, "src/main/java/Baz.java")).isFile();
        assertThat(new File(baseDir, "src/main/java/Bob.java")).isFile();
    }

    @Test
    public void testVerticleCreatePom() throws Exception {
        File pomFile = temporaryFolder.newFile("pom.xml");

        String vertxVersion = MojoUtils.getVersion("vertx-core-version");

        Map<String, String> tplContext = new HashMap<>();
        tplContext.put("mProjectGroupId", "com.example.vertx");
        tplContext.put("mProjectArtifactId", "vertx-example");
        tplContext.put("mProjectVersion", "1.0-SNAPSHOT");
        tplContext.put("vertxBom", "vertx-stack-depchain");
        tplContext.put("vertxVersion", vertxVersion);
        tplContext.put("javaVersion", "1.8");
        tplContext.put("vertxVerticle", "com.example.vertx.MainVerticle");
        tplContext.put("vmpVersion", MojoUtils.getVersion("vertx-maven-plugin-version"));
        SetupTemplateUtils.createPom(tplContext, pomFile);

        assertThat(pomFile).isFile();

        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
        Model model;
        try (InputStream is = Files.newInputStream(pomFile.toPath())) {
            model = xpp3Reader.read(is);
        }
        MavenProject project = new MavenProject(model);

        Optional<Plugin> vmPlugin = MojoUtils.hasPlugin(project, "io.reactiverse:vertx-maven-plugin");
        assertTrue(vmPlugin.isPresent());
        Plugin vmp = project.getPlugin("io.reactiverse:vertx-maven-plugin");
        assertNotNull(vmp);
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
        assertThat(vertxBom.getArtifactId()).isEqualTo("vertx-stack-depchain");
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
