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

import io.reactiverse.vertx.maven.plugin.mojos.Archive;
import io.reactiverse.vertx.maven.plugin.mojos.PackageMojo;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PackageMojoTest {


    @Test
    public void testOutputFileNameComputation() {
        MavenProject project =  new MavenProject();
        Build build = new Build();
        project.setBuild(build);
        project.setArtifactId("some-artifact-id");
        project.setVersion("1.0-SNAPSHOT");
        build.setFinalName("some-artifact-id-1.0-SNAPSHOT-GA");

        // output name set
        String fn = PackageMojo.computeOutputName(new Archive().setOutputFileName("hello"), project, null);
        assertThat(fn).isEqualTo("hello.jar");

        // output name set with extension
        fn = PackageMojo.computeOutputName(new Archive().setOutputFileName("hello.jar"), project, null);
        assertThat(fn).isEqualTo("hello.jar");

        // final name set
        fn = PackageMojo.computeOutputName(new Archive(), project, null);
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT-GA.jar");


        // final name set with .jar
        build.setFinalName("some-artifact-id-1.0-SNAPSHOT-GA2.jar");
        fn = PackageMojo.computeOutputName(new Archive(), project, null);
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT-GA2.jar");

        // same as 1 with classifier
        build.setFinalName("some-artifact-id-1.0-SNAPSHOT-GA");
        fn = PackageMojo.computeOutputName(new Archive(), project, "fat");
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT-GA-fat.jar");

        // same as 2 with classifier
        build.setFinalName("some-artifact-id-1.0-SNAPSHOT-GA2.jar");
        fn = PackageMojo.computeOutputName(new Archive(), project, "fat");
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT-GA2-fat.jar");

        // no final name
        build.setFinalName(null);
        fn = PackageMojo.computeOutputName(new Archive(), project, "");
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT.jar");

        // no final name with classifier
        build.setFinalName(null);
        fn = PackageMojo.computeOutputName(new Archive(), project, "fat");
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT-fat.jar");
    }
}
