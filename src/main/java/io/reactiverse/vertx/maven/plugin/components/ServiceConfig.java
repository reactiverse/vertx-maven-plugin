package io.reactiverse.vertx.maven.plugin.components;

import io.reactiverse.vertx.maven.plugin.mojos.AbstractVertxMojo;
import io.reactiverse.vertx.maven.plugin.mojos.Archive;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Set;

/**
 * Common configuration.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ServiceConfig {


    private AbstractVertxMojo mojo;
    private MavenProject project;
    private Set<Artifact> artifacts;
    private File output;
    private Archive archive;

    public AbstractVertxMojo getMojo() {
        return mojo;
    }

    public ServiceConfig setMojo(AbstractVertxMojo mojo) {
        this.mojo = mojo;
        return this;
    }

    public MavenProject getProject() {
        return project;
    }

    public ServiceConfig setProject(MavenProject project) {
        this.project = project;
        return this;
    }

    public Set<Artifact> getArtifacts() {
        return artifacts;
    }

    public ServiceConfig setArtifacts(Set<Artifact> artifacts) {
        this.artifacts = artifacts;
        return this;
    }

    public File getOutput() {
        return output;
    }

    public ServiceConfig setOutput(File output) {
        this.output = output;
        return this;
    }

    public Archive getArchive() {
        return archive;
    }

    public ServiceConfig setArchive(Archive archive) {
        this.archive = archive;
        return this;
    }
}
