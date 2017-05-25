/*
 *    Copyright (c) 2016-2017 Red Hat, Inc.
 *
 *    Red Hat licenses this file to you under the Apache License, version
 *    2.0 (the "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *    implied.  See the License for the specific language governing
 *    permissions and limitations under the License.
 */

package io.fabric8.vertx.maven.plugin;

import io.fabric8.vertx.maven.plugin.components.impl.ProjectManifestCustomizer;
import io.fabric8.vertx.maven.plugin.components.impl.SCMManifestCustomizer;
import io.fabric8.vertx.maven.plugin.mojos.AbstractVertxMojo;
import io.fabric8.vertx.maven.plugin.mojos.PackageMojo;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.manager.ScmManager;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kameshs
 */
public class ExtraManifestInfoTest extends PlexusTestCase {

    ScmManager scmManager;

    protected Model buildModel(File pomFile) {
        try {
            return new MavenXpp3Reader().read(ReaderFactory.newXmlReader(pomFile));
        } catch (IOException var3) {
            throw new RuntimeException("Failed to read POM file: " + pomFile, var3);
        } catch (XmlPullParserException var4) {
            throw new RuntimeException("Failed to parse POM file: " + pomFile, var4);
        }
    }

    @Before
    public void setup() throws Exception {
        super.setUp();
        scmManager = (ScmManager) lookup(ScmManager.ROLE);
        assertThat(scmManager);
    }

    public void testExtraManifestsNoClassifier() throws Exception {
        File testJarPom = Paths.get("src/test/resources/unit/jar-packaging/pom-extramf-jar.xml").toFile();
        assertNotNull(testJarPom);
        assertTrue(testJarPom.exists());
        assertTrue(testJarPom.isFile());
        MavenProject mavenProject = new MavenProject(buildModel(testJarPom));
        assertNotNull(mavenProject);

        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

        ProjectManifestCustomizer customizer = new ProjectManifestCustomizer();
        Map<String, String> atts = customizer.getEntries(new PackageMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {

            }

            @Override
            public ScmManager getScmManager() {
                return scmManager;
            }
        }, mavenProject);
        atts.entrySet().forEach(entry -> attributes.putValue(entry.getKey(), entry.getValue()));

        assertThat(attributes.isEmpty()).isFalse();

        assertThat(attributes.getValue("Manifest-Version")).isEqualTo("1.0");
        assertThat(attributes.getValue("Project-Name")).isEqualTo("vertx-demo");
        assertThat(attributes.getValue("Build-Timestamp")).isNotNull().isNotEmpty();
        assertThat(attributes.getValue("Project-Dependencies")).isEqualTo("io.vertx:vertx-core:3.4.1");
        assertThat(attributes.getValue("Project-Group")).isEqualTo("org.vertx.demo");
        assertThat(attributes.getValue("Project-Version")).isEqualTo("1.0.0-SNAPSHOT");

    }

    public void testExtraManifestsWithClassifier() throws Exception {
        File testJarPom = Paths.get("src/test/resources/unit/jar-packaging/pom-extramf-classifier-jar.xml").toFile();
        assertNotNull(testJarPom);
        assertTrue(testJarPom.exists());
        assertTrue(testJarPom.isFile());
        MavenProject mavenProject = new MavenProject(buildModel(testJarPom));
        assertNotNull(mavenProject);

        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

        ProjectManifestCustomizer customizer = new ProjectManifestCustomizer();
        Map<String, String> atts = customizer.getEntries(new PackageMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {

            }

            @Override
            public ScmManager getScmManager() {
                return scmManager;
            }
        }, mavenProject);
        atts.entrySet().forEach(entry -> attributes.putValue(entry.getKey(), entry.getValue()));

        assertThat(attributes.isEmpty()).isFalse();

        assertThat(attributes.getValue("Manifest-Version")).isEqualTo("1.0");
        assertThat(attributes.getValue("Project-Name")).isEqualTo("vertx-demo");
        assertThat(attributes.getValue("Build-Timestamp")).isNotNull().isNotEmpty();
        assertThat(attributes.getValue("Project-Dependencies")).isEqualTo("com.example:example:3.4.1:vertx");
        assertThat(attributes.getValue("Project-Group")).isEqualTo("org.vertx.demo");
        assertThat(attributes.getValue("Project-Version")).isEqualTo("1.0.0-SNAPSHOT");

    }

    public void testExtraManifestsWithSCMUrlAndTag() throws Exception {
        File testJarPom = Paths.get("src/test/resources/unit/jar-packaging/pom-extramf-scm-jar.xml").toFile();
        assertNotNull(testJarPom);
        assertTrue(testJarPom.exists());
        assertTrue(testJarPom.isFile());
        MavenProject mavenProject = new MavenProject(buildModel(testJarPom));
        assertNotNull(mavenProject);

        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

        ProjectManifestCustomizer customizer = new ProjectManifestCustomizer();
        Map<String, String> atts = customizer.getEntries(new PackageMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {

            }

            @Override
            public ScmManager getScmManager() {
                return scmManager;
            }
        }, mavenProject);

        atts.entrySet().forEach(entry -> attributes.putValue(entry.getKey(), entry.getValue()));

        atts = new SCMManifestCustomizer().getEntries(new PackageMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {

            }

            @Override
            public ScmManager getScmManager() {
                return scmManager;
            }
        }, mavenProject);
        atts.entrySet().forEach(entry -> attributes.putValue(entry.getKey(), entry.getValue()));

        assertThat(attributes.isEmpty()).isFalse();

        assertThat(attributes.getValue("Manifest-Version")).isEqualTo("1.0");
        assertThat(attributes.getValue("Project-Name")).isEqualTo("vertx-demo");
        assertThat(attributes.getValue("Build-Timestamp")).isNotNull().isNotEmpty();
        assertThat(attributes.getValue("Project-Dependencies")).isEqualTo("com.example:example:3.3.3:vertx");
        assertThat(attributes.getValue("Project-Group")).isEqualTo("org.vertx.demo");
        assertThat(attributes.getValue("Project-Version")).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(attributes.getValue("Scm-Url")).isEqualTo("https://github.com/fabric8io/vertx-maven-plugin");
        assertThat(attributes.getValue("Scm-Tag")).isEqualTo("HEAD");

    }
}
