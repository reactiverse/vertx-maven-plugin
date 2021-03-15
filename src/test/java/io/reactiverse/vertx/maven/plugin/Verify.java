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
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.*;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author kameshs
 */
@SuppressWarnings("unused")
public class Verify {

    public static void verifyVertxJar(File jarFile) throws Exception {
        VertxJarVerifier vertxJarVerifier = new VertxJarVerifier(jarFile);
        vertxJarVerifier.verifyJarCreated();
        vertxJarVerifier.verifyManifest();
    }

    public static void verifyContains(File archive, String file) throws IOException {
        JarFile jar = new JarFile(archive);
        assertThat(jar.getJarEntry(file)).isNotNull();
    }

    public static void verifyNotContain(File archive, String file) throws IOException {
        JarFile jar = new JarFile(archive);
        assertThat(jar.getJarEntry(file)).isNull();
    }

    public static void verifyContainsInManifest(File archive, String key, String value) throws Exception {
        Manifest manifest = new JarFile(archive).getManifest();
        assertThat(manifest).isNotNull();
        String v = manifest.getMainAttributes().getValue(key);
        assertThat(v).isNotNull().isEqualTo(value);
    }

    public static void verifyServiceRelocation(File jarFile) throws Exception {
        VertxJarServicesVerifier vertxJarVerifier = new VertxJarServicesVerifier(jarFile);
        vertxJarVerifier.verifyJarCreated();
        vertxJarVerifier.verifyServicesContent();
    }

    public static void verifyOrderServiceContent(File jarFile, String content) throws Exception {
        VertxJarServicesVerifier vertxJarVerifier = new VertxJarServicesVerifier(jarFile);
        vertxJarVerifier.verifyJarCreated();
        vertxJarVerifier.verifyOrderedServicesContent(content);
    }

    public static void verifyContainWithContent(File jarFile, String path, String... lines) throws IOException {
        JarFile jar = new JarFile(jarFile);
        ZipEntry entry = jar.getEntry(path);
        assertThat(entry).isNotNull();
        String content = read(jar.getInputStream(entry));
        assertThat(content).containsSubsequence(lines);
    }

    public static void verifySetup(File pomFile) throws Exception {
        assertNotNull("Unable to find pom.xml", pomFile);
        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
        Model model = xpp3Reader.read(new FileInputStream(pomFile));

        MavenProject project = new MavenProject(model);

        Optional<Plugin> vmPlugin = MojoUtils.hasPlugin(project, "io.reactiverse:vertx-maven-plugin");
        assertTrue(vmPlugin.isPresent());

        //Check if the properties have been set correctly
        Properties properties = model.getProperties();
        assertThat(properties.containsKey("vertx.version")).isTrue();
        assertThat(properties.getProperty("vertx.version"))
            .isEqualTo(MojoUtils.getVersion("vertx-core-version"));


        assertThat(properties.containsKey("vertx-maven-plugin.version")).isTrue();
        assertThat(properties.getProperty("vertx-maven-plugin.version"))
            .isEqualTo(MojoUtils.getVersion("vertx-maven-plugin-version"));

        //Check if the dependencies has been set correctly
        DependencyManagement dependencyManagement = model.getDependencyManagement();
        assertThat(dependencyManagement).isNotNull();
        assertThat(dependencyManagement.getDependencies().isEmpty()).isFalse();

        //Check Vert.x dependencies BOM
        Optional<Dependency> vertxDeps = dependencyManagement.getDependencies().stream()
            .filter(d -> d.getArtifactId().equals("vertx-stack-depchain")
                && d.getGroupId().equals("io.vertx"))
            .findFirst();

        assertThat(vertxDeps.isPresent()).isTrue();
        assertThat(vertxDeps.get().getVersion()).isEqualTo("${vertx.version}");

        //Check Vert.x core dependency
        Optional<Dependency> vertxCoreDep = model.getDependencies().stream()
            .filter(d -> d.getArtifactId().equals("vertx-core") && d.getGroupId().equals("io.vertx"))
            .findFirst();
        assertThat(vertxCoreDep.isPresent()).isTrue();
        assertThat(vertxCoreDep.get().getVersion()).isNull();

        //Check Redeploy Configuration
        Plugin vmp = project.getPlugin("io.reactiverse:vertx-maven-plugin");
        assertNotNull(vmp);
        Xpp3Dom pluginConfig = (Xpp3Dom) vmp.getConfiguration();
        assertNotNull(pluginConfig);
        String redeploy = pluginConfig.getChild("redeploy").getValue();
        assertNotNull(redeploy);
        assertTrue(Boolean.valueOf(redeploy));
    }

    public static void verifySetupWithVersion(File pomFile) throws Exception {
        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
        Model model = xpp3Reader.read(new FileInputStream(pomFile));

        MavenProject project = new MavenProject(model);
        Properties projectProps = project.getProperties();
        assertNotNull(projectProps);
        assertFalse(projectProps.isEmpty());
        assertEquals(projectProps.getProperty("vertx.version"), "4.0.3");
    }

    public static void verifySetupWithBom(File pomFile) throws Exception {
        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
        Model model = xpp3Reader.read(new FileInputStream(pomFile));

        MavenProject project = new MavenProject(model);

        //Check if the dependencies has been set correctly
        DependencyManagement dependencyManagement = model.getDependencyManagement();
        assertThat(dependencyManagement).isNotNull();
        assertThat(dependencyManagement.getDependencies().isEmpty()).isFalse();

        //Check Vert.x dependencies BOM
        Optional<Dependency> vertxDeps = dependencyManagement.getDependencies().stream()
            .filter(d -> d.getArtifactId().equals("vertx-dependencies")
                && d.getGroupId().equals("io.vertx"))
            .findFirst();

        assertThat(vertxDeps.isPresent()).isTrue();
        assertThat(vertxDeps.get().getVersion()).isEqualTo("${vertx.version}");
    }

    private static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

    public static Stream<String> readAsStream(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.toList()).stream();
        }
    }

    public static String argsToString(String[] args) {
        return Stream.of(args).collect(Collectors.joining(" "));
    }

    public static class VertxJarVerifier {

        File jarFile;

        public VertxJarVerifier(File jarFile) {
            this.jarFile = jarFile;
        }

        protected void verifyJarCreated() {
            assertThat(jarFile).isNotNull();
            assertThat(jarFile).isFile();
        }

        protected void verifyManifest() throws Exception {

            Manifest manifest = new JarFile(jarFile).getManifest();

            assertThat(manifest).isNotNull();
            String mainClass = manifest.getMainAttributes().getValue("Main-Class");
            String mainVerticle = manifest.getMainAttributes().getValue("Main-Verticle");
            assertThat(mainClass).isNotNull().isEqualTo("io.vertx.core.Launcher");
            assertThat(mainVerticle).isNotNull().isEqualTo("org.vertx.demo.MainVerticle");

        }
    }

    public static class VertxJarServicesVerifier {

        JarFile jarFile;

        public VertxJarServicesVerifier(File jarFile) throws IOException {
            this.jarFile = new JarFile(jarFile);
        }

        protected void verifyJarCreated() {
            assertThat(jarFile).isNotNull();
        }

        protected void verifyServicesContent() throws Exception {

            String expected = "foo.bar.baz.MyImpl\ncom.fasterxml.jackson.core.JsonFactory";

            ZipEntry spiEntry1 = jarFile.getEntry("META-INF/services/com.fasterxml.jackson.core.JsonFactory");

            assertThat(spiEntry1).isNotNull();

            InputStream in = jarFile.getInputStream(spiEntry1);
            String actual = read(in);
            assertThat(actual).isEqualTo(expected);
        }

        protected void verifyOrderedServicesContent(String orderedContent) throws Exception {

            ZipEntry spiEntry = jarFile.getEntry("META-INF/services/io.reactiverse.vmp.foo");

            assertThat(spiEntry).isNotNull();

            InputStream in = jarFile.getInputStream(spiEntry);
            String actual = read(in);
            assertThat(actual).isEqualTo(orderedContent);
        }
    }

}
