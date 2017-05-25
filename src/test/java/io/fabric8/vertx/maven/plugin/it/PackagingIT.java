/*
 *    Copyright (c) 2016 Red Hat, Inc.
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

package io.fabric8.vertx.maven.plugin.it;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kameshs
 */
public class PackagingIT extends VertxMojoTestBase {

    String PACKAGING_META_INF = "projects/packaging-meta-inf-it";

    private Verifier verifier;


    public void initVerifier(File root) throws VerificationException {
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
        // TODO Fix this one
        entry = jar.getJarEntry("org/apache/commons/lang3/RandomUtils.class");
        assertThat(entry).isNull();

    }




}
