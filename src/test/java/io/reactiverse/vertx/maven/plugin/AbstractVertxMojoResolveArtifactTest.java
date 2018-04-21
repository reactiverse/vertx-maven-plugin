package io.reactiverse.vertx.maven.plugin;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import io.reactiverse.vertx.maven.plugin.mojos.AbstractVertxMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractVertxMojoResolveArtifactTest {


    private TestMojo mojo;
    private MavenProject project;

    @Before
    public void setup() throws Exception {
        mojo = new TestMojo();
    }

    @Test
    public void resolveCoordinates() {
        checkArtifact(mavenArtifact("com.company", "some-artifact", "0.4.1-SNAPSHOT"));
        checkArtifact(mavenArtifact("com.company", "some-artifact", "0.4.1-SNAPSHOT", "war", null));
        checkArtifact(mavenArtifact("com.company", "some-artifact", "0.4.1-SNAPSHOT", "jar", "sources"));
    }


    private void checkArtifact(Artifact artifact) {
        checkCoordsAgainstSourceArtifact(artifact, mojo.asMavenCoordinates(artifact));
    }

    private void checkCoordsAgainstSourceArtifact(Artifact mavenArtifact, String coords) {
        org.eclipse.aether.artifact.DefaultArtifact artifact = new org.eclipse.aether.artifact.DefaultArtifact(coords);
        assertThat(artifact).extracting(
            org.eclipse.aether.artifact.DefaultArtifact::getGroupId,
            org.eclipse.aether.artifact.DefaultArtifact::getArtifactId,
            org.eclipse.aether.artifact.DefaultArtifact::getVersion,
            org.eclipse.aether.artifact.DefaultArtifact::getExtension,
            org.eclipse.aether.artifact.DefaultArtifact::getClassifier
        ).containsExactly(
            mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), mavenArtifact.getVersion(),
            mavenArtifact.getType(), Objects.toString(mavenArtifact.getClassifier(), "")
        );
    }

    private static Artifact mavenArtifact(
        String groupId, String artifactId, String version) {
        return mavenArtifact(groupId, artifactId, version, "jar", null);
    }
    private static DefaultArtifact mavenArtifact(
        String groupId, String artifactId, String version, String type, String classifier) {
        return new DefaultArtifact(
            groupId, artifactId, version, "compile", type, classifier, new DefaultArtifactHandler()
        );
    }

    static class TestMojo extends AbstractVertxMojo {

        @Override
        public void execute() throws MojoExecutionException, MojoFailureException {

        }

        @Override
        protected String asMavenCoordinates(Artifact artifact) {
            return super.asMavenCoordinates(artifact);
        }
    }
}
