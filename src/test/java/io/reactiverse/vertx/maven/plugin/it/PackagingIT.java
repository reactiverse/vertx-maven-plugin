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

package io.reactiverse.vertx.maven.plugin.it;

import org.apache.commons.io.IOUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * @author kameshs
 */
public class PackagingIT extends VertxMojoTestBase {

    private String PACKAGING_META_INF = "projects/packaging-meta-inf-it";
    private String PACKAGING_DUPLICATE = "projects/packaging-duplicate-it";

    private Verifier verifier;


    private void initVerifier(File root) throws VerificationException {
        verifier = new Verifier(root.getAbsolutePath());
        verifier.setAutoclean(false);
        installPluginToLocalRepository(verifier.getLocalRepository());
    }

    @Test
    public void testContentInTheJar() throws IOException, VerificationException {
        File testDir = initProject(PACKAGING_META_INF);
        assertThat(testDir).isDirectory();
        initVerifier(testDir);
        prepareProject(testDir, verifier);
        runPackage(verifier);

        assertThat(testDir).isNotNull();

        File out = new File(testDir, "target/vertx-demo-start-0.0.1.BUILD-SNAPSHOT.jar");
        assertThat(out).isFile();
        JarFile jar = new JarFile(out);

        // Commons utils
        // org.apache.commons.io.CopyUtils
        JarEntry entry = jar.getJarEntry("org/apache/commons/io/CopyUtils.class");
        assertThat(entry).isNotNull();
        assertThat(entry.isDirectory()).isFalse();

        // SLF4J-API
        // /org/slf4j/MDC.class
        entry = jar.getJarEntry("org/slf4j/MDC.class");
        assertThat(entry).isNotNull();
        assertThat(entry.isDirectory()).isFalse();

        // tc native
        // /org/apache/tomcat/Apr.class
        entry = jar.getJarEntry("org/apache/tomcat/Apr.class");
        assertThat(entry).isNotNull();
        assertThat(entry.isDirectory()).isFalse();
        // /org/apache/tomcat/apr.properties
        entry = jar.getJarEntry("org/apache/tomcat/apr.properties");
        assertThat(entry).isNotNull();
        assertThat(entry.isDirectory()).isFalse();
        // /META-INF/native/libnetty-tcnative-linux-x86_64.so
        entry = jar.getJarEntry("META-INF/native/libnetty-tcnative-linux-x86_64.so");
        assertThat(entry).isNotNull();
        assertThat(entry.isDirectory()).isFalse();

        // Jackson (transitive of vert.x core)
        // /com/fasterxml/jackson/annotation/JacksonAnnotation.class
        entry = jar.getJarEntry("com/fasterxml/jackson/annotation/JacksonAnnotation.class");
        assertThat(entry).isNotNull();
        assertThat(entry.isDirectory()).isFalse();

        // Not included
        // codegen - transitive optional
        entry = jar.getJarEntry("io/vertx/codegen/Case.class");
        assertThat(entry).isNull();
        // log4j - transitive provided
        entry = jar.getJarEntry("org/apache/log4j/MDC.class");
        assertThat(entry).isNull();
        // junit - test
        entry = jar.getJarEntry("junit/runner/Version.class");
        assertThat(entry).isNull();
        // commons-lang3 - provided
        entry = jar.getJarEntry("org/apache/commons/lang3/RandomUtils.class");
        assertThat(entry).isNull();

    }

    @Test
    public void testDuplicateManagement() throws VerificationException, IOException {
        File testDir = initProject(PACKAGING_DUPLICATE);
        assertThat(testDir).isDirectory();
        initVerifier(testDir);
        prepareProject(testDir, verifier);

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

        installJarToLocalRepository(verifier.getLocalRepository(), "A", A);
        installJarToLocalRepository(verifier.getLocalRepository(), "B", B);

        runPackage(verifier);

        File out = new File(testDir, "target/vertx-demo-start-0.0.1.BUILD-SNAPSHOT.jar");
        assertThat(out).isFile();
        JavaArchive archive = ShrinkWrap.createFromZipFile(JavaArchive.class, out);
        assertNotNull(archive);

        Asset a = archive.get( "/res/A").getAsset();
        Asset b = archive.get( "/res/B").getAsset();
        Asset c = archive.get( "/res/C").getAsset();

        String content_a = IOUtils.toString(a.openStream(), "UTF-8");
        String content_b = IOUtils.toString(b.openStream(), "UTF-8");
        String content_c = IOUtils.toString(c.openStream(), "UTF-8");

        assertThat(content_a).isEqualToIgnoringCase("A3\n");
        assertThat(content_b).isEqualToIgnoringCase("B1");
        assertThat(content_c).isEqualToIgnoringCase("C2");
    }




}
