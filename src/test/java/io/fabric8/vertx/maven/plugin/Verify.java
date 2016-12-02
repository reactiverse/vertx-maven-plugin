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

//import io.restassured.RestAssured;

import java.io.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kameshs
 */
public class Verify {

    public static void verifyVertxJar(File jarFile) throws Exception {
        VertxJarVerifier vertxJarVerifier = new VertxJarVerifier(jarFile);
        vertxJarVerifier.verifyJarCreated();
        vertxJarVerifier.verifyManifest();
    }

    public static void verifyServiceRelocation(File jarFile) throws Exception {
        VertxJarServicesVerifier vertxJarVerifier = new VertxJarServicesVerifier(jarFile);
        vertxJarVerifier.verifyJarCreated();
        vertxJarVerifier.verifyServicesContent();
    }


    public static String read(InputStream input) throws IOException {
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
        return Stream.of(args).collect(Collectors.joining(" ")).toString();
    }

    public static class VertxJarVerifier {

        File jarFile;

        public VertxJarVerifier(File jarFile) {
            this.jarFile = jarFile;
        }

        protected void verifyJarCreated() throws Exception {
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
            try {
                this.jarFile = new JarFile(jarFile);
            } catch (IOException e) {
                throw e;
            }
        }

        protected void verifyJarCreated() throws Exception {
            assertThat(jarFile).isNotNull();
        }

        protected void verifyServicesContent() throws Exception {

            String expected = "foo.bar.baz.MyImpl\ncom.fasterxml.jackson.core.JsonFactory";

            ZipEntry spiEntry1 = jarFile.getEntry("META-INF/services/com.fasterxml.jackson.core.JsonFactory");
            ZipEntry spiEntry2 = jarFile.getEntry("META-INF/services/io.vertx.core.spi.FutureFactory");

            assertThat(spiEntry1).isNotNull();
            assertThat(spiEntry2).isNotNull();

            InputStream in = jarFile.getInputStream(spiEntry1);
            String actual = read(in);
            assertThat(actual).isEqualTo(expected);

            in = jarFile.getInputStream(spiEntry2);
            actual = read(in);
            assertThat(actual).isEqualTo("io.vertx.core.impl.FutureFactoryImpl");

            // This part is going to be used once Vert.x 3.4.0 is released //
            // TODO Uncomment me once Vert.x 3.4.0 is released

//            ZipEntry spiEntry3 = jarFile.getEntry("META-INF/services/org.codehaus.groovy.runtime.ExtensionModule");
//            assertThat(spiEntry3).isNotNull();
//            in = jarFile.getInputStream(spiEntry3);
//            actual = read(in);
//            assertThat(actual).contains("moduleName=vertx-demo-pkg")
//                .contains("moduleVersion=0.0.1")
//                .contains("io.vertx.groovy.ext.jdbc.GroovyStaticExtension")
//                .contains("io.vertx.groovy.ext.jdbc.GroovyExtension");

        }
    }

}
