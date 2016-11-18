/*
 *   Copyright 2016 Kamesh Sampath
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.fabric8.vertx.maven.plugin;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author kameshs
 */
public class MojoUtilsTest extends AbstractMojoTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testWhenJarPluginIsAbsent() throws Exception {
        File testJarPom = Paths.get("src/test/resources/unit/jar-packaging/pom-jar.xml").toFile();
        assertNotNull(testJarPom);
        assertTrue(testJarPom.exists());
        assertTrue(testJarPom.isFile());
        MavenProject mavenProject = new MavenProject(buildModel(testJarPom));
        assertNotNull(mavenProject);
        String jarPluginKey = "org.apache.maven.plugins:maven-jar-plugin";
        Optional<Plugin> jarPlugin = mavenProject.getBuildPlugins().stream()
                .filter(plugin -> jarPluginKey.equals(plugin.getKey()))
                .findFirst();
        assertFalse(jarPlugin.isPresent());
    }

    protected Model buildModel(File pomFile) throws IOException {
        try {
            return new MavenXpp3Reader().read(ReaderFactory.newXmlReader(pomFile));
        } catch (IOException var3) {
            throw new RuntimeException("Failed to read POM file: " + pomFile, var3);
        } catch (XmlPullParserException var4) {
            throw new RuntimeException("Failed to parse POM file: " + pomFile, var4);
        }
    }
}
