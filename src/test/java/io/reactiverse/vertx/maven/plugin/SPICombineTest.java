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

import io.reactiverse.vertx.maven.plugin.components.ServiceFileCombinationConfig;
import io.reactiverse.vertx.maven.plugin.components.ServiceUtils;
import io.reactiverse.vertx.maven.plugin.components.impl.ServiceFileCombinationImpl;
import io.reactiverse.vertx.maven.plugin.mojos.AbstractVertxMojo;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author kameshs
 */
public class SPICombineTest {

    private ServiceFileCombinationImpl combiner = new ServiceFileCombinationImpl();

    @Test
    public void testCombine() throws Exception {
        File jar1 = new File("target/testCombine1.jar");
        File jar2 = new File("target/testCombine2.jar");
        File jar3 = new File("target/testCombine3.jar");

        JavaArchive jarArchive1 = ShrinkWrap.create(JavaArchive.class);
        jarArchive1.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl");

        jarArchive1.as(ZipExporter.class).exportTo(jar1, true);


        JavaArchive jarArchive2 = ShrinkWrap.create(JavaArchive.class);
        jarArchive2.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl2");
        jarArchive2.addAsServiceProvider("com.test.demo.DemoSP2",
            "com.test.demo.DemoSPI2.impl.DemoSPI2Impl2");
        jarArchive2.as(ZipExporter.class).exportTo(jar2, true);

        JavaArchive jarArchive3 = ShrinkWrap.create(JavaArchive.class);
        jarArchive3.addClass(SPICombineTest.class);
        jarArchive3.as(ZipExporter.class).exportTo(jar3, true);

        Set<Artifact> artifacts = new LinkedHashSet<>();
        Artifact a1 = new DefaultArtifact("org.acme", "a1", "1.0",
            "compile", "jar", "", null);
        a1.setFile(jar1);
        Artifact a2 = new DefaultArtifact("org.acme", "a2", "1.0",
            "compile", "jar", "", null);
        a2.setFile(jar2);
        Artifact a3 = new DefaultArtifact("org.acme", "a3", "1.0",
            "compile", "jar", "", null);
        a3.setFile(jar3);

        artifacts.add(a1);
        artifacts.add(a2);
        artifacts.add(a3);

        MavenProject project = new MavenProject();
        project.setVersion("1.0");
        project.setArtifactId("foo");

        AbstractVertxMojo mojo = new AbstractVertxMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {

            }
        };

        mojo.setLog(new SystemStreamLog());
        Build build = new Build();
        build.setOutputDirectory("target/junk");
        project.setBuild(build);

        ServiceFileCombinationConfig config = new ServiceFileCombinationConfig()
            .setProject(project)
            .setArtifacts(artifacts)
            .setArchive(ServiceUtils.getDefaultFatJar())
            .setMojo(mojo);

        combiner.doCombine(config);

        File merged = new File("target/junk/META-INF/services/com.test.demo.DemoSPI");
        assertThat(merged).isFile();

        List<String> lines = FileUtils.readLines(merged, "UTF-8");
        assertThat(lines).containsExactly("com.test.demo.DemoSPI.impl.DemoSPIImpl",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl2");
        Stream.of(jar1, jar2, jar3, new File("target/junk")).forEach(FileUtils::deleteQuietly);
    }

    @Test
    public void testCombineDiffSPI() throws Exception {

        File jar1 = new File("target/testCombineDiffSPI.jar");
        File jar2 = new File("target/testCombineDiffSPI2.jar");
        File jar3 = new File("target/testCombineDiffSPI3.jar");
        File jar4 = new File("target/testCombineDiffSPI4.jar");

        JavaArchive jarArchive1 = ShrinkWrap.create(JavaArchive.class);
        jarArchive1.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl");
        jarArchive1.as(ZipExporter.class).exportTo(jar1, true);


        JavaArchive jarArchive2 = ShrinkWrap.create(JavaArchive.class);
        jarArchive2.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl2");
        jarArchive2.as(ZipExporter.class).exportTo(jar2, true);

        JavaArchive jarArchive3 = ShrinkWrap.create(JavaArchive.class);
        jarArchive3.addClass(SPICombineTest.class);
        jarArchive3.as(ZipExporter.class).exportTo(jar3, true);

        JavaArchive jarArchive4 = ShrinkWrap.create(JavaArchive.class);
        jarArchive4.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl4");
        jarArchive4.as(ZipExporter.class).exportTo(jar4, true);

        Set<Artifact> artifacts = new LinkedHashSet<>();
        Artifact a1 = new DefaultArtifact("org.acme", "a1", "1.0",
            "compile", "jar", "", null);
        a1.setFile(jar1);
        Artifact a2 = new DefaultArtifact("org.acme", "a2", "1.0",
            "compile", "jar", "", null);
        a2.setFile(jar2);
        Artifact a3 = new DefaultArtifact("org.acme", "a3", "1.0",
            "compile", "jar", "", null);
        a3.setFile(jar3);
        Artifact a4 = new DefaultArtifact("org.acme", "a4", "1.0",
            "compile", "jar", "", null);
        a4.setFile(jar4);

        artifacts.add(a1);
        artifacts.add(a2);
        artifacts.add(a3);
        artifacts.add(a4);

        MavenProject project = new MavenProject();
        project.setVersion("1.0");
        project.setArtifactId("foo");

        AbstractVertxMojo mojo = new AbstractVertxMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {

            }
        };

        mojo.setLog(new SystemStreamLog());
        Build build = new Build();
        build.setOutputDirectory("target/junk");
        project.setBuild(build);

        ServiceFileCombinationConfig config = new ServiceFileCombinationConfig()
            .setProject(project)
            .setArtifacts(artifacts)
            .setArchive(ServiceUtils.getDefaultFatJar())
            .setMojo(mojo);

        combiner.doCombine(config);
        File merged = new File("target/junk/META-INF/services/com.test.demo.DemoSPI");
        assertThat(merged).isFile();

        List<String> lines = FileUtils.readLines(merged, "UTF-8");
        assertThat(lines).hasSize(3).containsExactly(
            "com.test.demo.DemoSPI.impl.DemoSPIImpl",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl2",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl4");
        Stream.of(jar1, jar2, jar3, jar4, new File("target/junk")).forEach(FileUtils::deleteQuietly);

    }

    @Test
    public void testCombineWithSpringDescriptors() throws Exception {
        File jar1 = new File("target/testCombine1Spring.jar");
        File jar2 = new File("target/testCombine2Spring.jar");
        File jar3 = new File("target/testCombine3Spring.jar");

        JavaArchive jarArchive1 = ShrinkWrap.create(JavaArchive.class);
        jarArchive1.add(new StringAsset("com.test.demo.DemoSPI.impl.DemoSPIImpl"),
            "/META-INF/spring.foo");

        jarArchive1.as(ZipExporter.class).exportTo(jar1, true);


        JavaArchive jarArchive2 = ShrinkWrap.create(JavaArchive.class);
        jarArchive2.add(new StringAsset("com.test.demo.DemoSPI.impl.DemoSPIImpl2"),
            "/META-INF/spring.foo");
        jarArchive2.add(new StringAsset("com.test.demo.DemoSPI2.impl.DemoSPI2Impl2"),
            "/META-INF/spring.bar");
        jarArchive2.as(ZipExporter.class).exportTo(jar2, true);

        JavaArchive jarArchive3 = ShrinkWrap.create(JavaArchive.class);
        jarArchive3.addClass(SPICombineTest.class);
        jarArchive3.as(ZipExporter.class).exportTo(jar3, true);

        Set<Artifact> artifacts = new LinkedHashSet<>();
        Artifact a1 = new DefaultArtifact("org.acme", "a1", "1.0",
            "compile", "jar", "", null);
        a1.setFile(jar1);
        Artifact a2 = new DefaultArtifact("org.acme", "a2", "1.0",
            "compile", "jar", "", null);
        a2.setFile(jar2);
        Artifact a3 = new DefaultArtifact("org.acme", "a3", "1.0",
            "compile", "jar", "", null);
        a3.setFile(jar3);

        artifacts.add(a1);
        artifacts.add(a2);
        artifacts.add(a3);

        MavenProject project = new MavenProject();
        project.setVersion("1.0");
        project.setArtifactId("foo");

        AbstractVertxMojo mojo = new AbstractVertxMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {

            }
        };

        mojo.setLog(new SystemStreamLog());
        Build build = new Build();
        build.setOutputDirectory("target/junk");
        project.setBuild(build);

        ServiceFileCombinationConfig config = new ServiceFileCombinationConfig()
            .setProject(project)
            .setArtifacts(artifacts)
            .setArchive(ServiceUtils.getDefaultFatJar())
            .setMojo(mojo);

        combiner.doCombine(config);

        File merged = new File("target/junk/META-INF/spring.foo");
        assertThat(merged).isFile();

        List<String> lines = FileUtils.readLines(merged, "UTF-8");
        assertThat(lines).containsExactly("com.test.demo.DemoSPI.impl.DemoSPIImpl",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl2");
        Stream.of(jar1, jar2, jar3, new File("target/junk")).forEach(FileUtils::deleteQuietly);
    }
}
