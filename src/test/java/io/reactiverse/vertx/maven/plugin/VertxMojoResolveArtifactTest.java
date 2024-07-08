package io.reactiverse.vertx.maven.plugin;

import io.reactiverse.vertx.maven.plugin.utils.MavenUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class VertxMojoResolveArtifactTest {

    @Test
    public void resolveCoordinates() {
        checkArtifact(mavenArtifact());
        checkArtifact(mavenArtifact("war", null));
        checkArtifact(mavenArtifact("jar", "sources"));
    }


    private void checkArtifact(Artifact artifact) {
        checkCoordsAgainstSourceArtifact(artifact, MavenUtils.asMavenCoordinates(artifact));
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

    private static Artifact mavenArtifact() {
        return mavenArtifact("jar", null);
    }

    private static DefaultArtifact mavenArtifact(String type, String classifier) {
        return new DefaultArtifact("com.company", "some-artifact", "0.4.1-SNAPSHOT", "compile", type, classifier, new DefaultArtifactHandler());
    }
}
