package io.fabric8.vertx.maven.plugin;

import io.fabric8.vertx.maven.plugin.mojos.PackageMojo;
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

        // finale name set
        String fn = PackageMojo.computeOutputName(project, null);
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT-GA.jar");

        // final name set with .jar
        build.setFinalName("some-artifact-id-1.0-SNAPSHOT-GA2.jar");
        fn = PackageMojo.computeOutputName(project, null);
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT-GA2.jar");

        // same as 1 with classifier
        build.setFinalName("some-artifact-id-1.0-SNAPSHOT-GA");
        fn = PackageMojo.computeOutputName(project, "fat");
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT-GA-fat.jar");

        // same as 2 with classifier
        build.setFinalName("some-artifact-id-1.0-SNAPSHOT-GA2.jar");
        fn = PackageMojo.computeOutputName(project, "fat");
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT-GA2-fat.jar");

        // no final name
        build.setFinalName(null);
        fn = PackageMojo.computeOutputName(project, "");
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT.jar");

        // no final name with classifier
        build.setFinalName(null);
        fn = PackageMojo.computeOutputName(project, "fat");
        assertThat(fn).isEqualTo("some-artifact-id-1.0-SNAPSHOT-fat.jar");
    }
}
