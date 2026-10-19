/*
 *    Copyright (c) 2016-2026 Red Hat, Inc.
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

package io.reactiverse.vertx.maven.plugin.it;

import org.apache.commons.io.IOUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * @author kameshs
 */
public class PackagingIT extends VertxMojoTestBase {

    @Test
    public void testDuplicateManagement() throws Exception {
        File testDir = initProject("projects/packaging-duplicate-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        File A = new File("target/A-1.0.jar");
        File B = new File("target/B-1.0.jar");
        JavaArchive jarArchive1 = ShrinkWrap.create(JavaArchive.class);
        jarArchive1.add(new StringAsset("A1"), "/res/A");
        jarArchive1.add(new StringAsset("B1"), "/res/B");
        jarArchive1.as(ZipExporter.class).exportTo(A, true);

        JavaArchive jarArchive2 = ShrinkWrap.create(JavaArchive.class);
        jarArchive2.add(new StringAsset("A2"), "/res/A");
        jarArchive2.add(new StringAsset("B2"), "/res/B");
        jarArchive2.add(new StringAsset("C2"), "/res/C");
        jarArchive2.as(ZipExporter.class).exportTo(B, true);

        installJarToLocalRepository(getLocalRepository(), "A", A);
        installJarToLocalRepository(getLocalRepository(), "B", B);

        runPackage(testDir);

        File out = new File(testDir, "target/vertx-demo-start-0.0.1.BUILD-SNAPSHOT.jar");
        assertThat(out).isFile();
        JavaArchive archive = ShrinkWrap.createFromZipFile(JavaArchive.class, out);
        assertNotNull(archive);

        Asset a = archive.get("/res/A").getAsset();
        Asset b = archive.get("/res/B").getAsset();
        Asset c = archive.get("/res/C").getAsset();

        String content_a = IOUtils.toString(a.openStream(), StandardCharsets.UTF_8);
        String content_b = IOUtils.toString(b.openStream(), StandardCharsets.UTF_8);
        String content_c = IOUtils.toString(c.openStream(), StandardCharsets.UTF_8);

        assertThat(content_a).isEqualToIgnoringCase("A3" + System.lineSeparator());
        assertThat(content_b).isEqualToIgnoringCase("B1");
        assertThat(content_c).isEqualToIgnoringCase("C2");
    }
}
