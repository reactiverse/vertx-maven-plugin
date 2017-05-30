package io.fabric8.vertx.maven.plugin.components;

import io.fabric8.vertx.maven.plugin.mojos.AbstractVertxMojo;
import io.fabric8.vertx.maven.plugin.mojos.Archive;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Set;

/**
 * Configuration of the {@link PackageService}
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PackageConfig extends ServiceConfig {

    private String classifier;

    public PackageConfig setArchive(Archive archive) {
        super.setArchive(archive);
        return this;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }


    @Override
    public PackageConfig setMojo(AbstractVertxMojo mojo) {
        super.setMojo(mojo);
        return this;
    }

    @Override
    public PackageConfig setProject(MavenProject project) {
        super.setProject(project);
        return this;

    }

    @Override
    public PackageConfig setArtifacts(Set<Artifact> artifacts) {
        super.setArtifacts(artifacts);
        return this;
    }

    @Override
    public PackageConfig setOutput(File output) {
        super.setOutput(output);
        return this;
    }
}
