/*
 *    Copyright (c) 2016-2018 Red Hat, Inc.
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

package io.reactiverse.vertx.maven.plugin;

import io.reactiverse.vertx.maven.plugin.components.impl.ProjectManifestCustomizer;
import io.reactiverse.vertx.maven.plugin.mojos.PackageMojo;
import io.reactiverse.vertx.maven.plugin.utils.VertxCoreVersion;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static io.reactiverse.vertx.maven.plugin.model.ExtraManifestKeys.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kameshs
 */
public class ExtraManifestInfoTest extends PlexusTestCase {

    private Model buildModel(File pomFile) {
        try {
            return new MavenXpp3Reader().read(ReaderFactory.newXmlReader(pomFile));
        } catch (IOException var3) {
            throw new RuntimeException("Failed to read POM file: " + pomFile, var3);
        } catch (XmlPullParserException var4) {
            throw new RuntimeException("Failed to parse POM file: " + pomFile, var4);
        }
    }

    public void testExtraManifestsNoClassifier() {
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
            public void execute() {

            }
        }, mavenProject);
        atts.forEach(attributes::putValue);

        assertThat(attributes.isEmpty()).isFalse();

        assertThat(attributes.getValue("Manifest-Version")).isEqualTo("1.0");
        assertThat(attributes.getValue(PROJECT_NAME.header())).isEqualTo("vertx-demo");
        assertThat(attributes.getValue(BUILD_TIMESTAMP.header())).isNotNull().isNotEmpty();
        assertThat(attributes.getValue(PROJECT_DEPS.header())).isEqualTo("io.vertx:vertx-core:" + VertxCoreVersion.VALUE);
        assertThat(attributes.getValue(PROJECT_GROUP_ID.header())).isEqualTo("org.vertx.demo");
        assertThat(attributes.getValue(PROJECT_VERSION.header())).isEqualTo("1.0.0-SNAPSHOT");

    }

    public void testExtraManifestsWithClassifier() {
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
            public void execute() {

            }
        }, mavenProject);
        atts.forEach(attributes::putValue);

        assertThat(attributes.isEmpty()).isFalse();

        assertThat(attributes.getValue("Manifest-Version")).isEqualTo("1.0");
        assertThat(attributes.getValue(PROJECT_NAME.header())).isEqualTo("vertx-demo");
        assertThat(attributes.getValue(BUILD_TIMESTAMP.header())).isNotNull().isNotEmpty();
        assertThat(attributes.getValue(PROJECT_DEPS.header())).isEqualTo("com.example:example:" + VertxCoreVersion.VALUE + ":vertx");
        assertThat(attributes.getValue(PROJECT_GROUP_ID.header())).isEqualTo("org.vertx.demo");
        assertThat(attributes.getValue(PROJECT_VERSION.header())).isEqualTo("1.0.0-SNAPSHOT");
    }

}
