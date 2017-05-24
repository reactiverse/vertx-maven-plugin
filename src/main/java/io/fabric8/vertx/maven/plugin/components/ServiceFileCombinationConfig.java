package io.fabric8.vertx.maven.plugin.components;

import io.fabric8.vertx.maven.plugin.model.CombinationStrategy;
import io.fabric8.vertx.maven.plugin.mojos.AbstractVertxMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Set;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ServiceFileCombinationConfig extends ServiceConfig {


    private CombinationStrategy strategy;

    @Override
    public ServiceFileCombinationConfig setMojo(AbstractVertxMojo mojo) {
        super.setMojo(mojo);
        return this;
    }

    @Override
    public ServiceFileCombinationConfig setProject(MavenProject project) {
        super.setProject(project);
        return this;
    }

    @Override
    public ServiceFileCombinationConfig setOutput(File output) {
        super.setOutput(output);
        return this;
    }

    @Override
    public ServiceFileCombinationConfig setArtifacts(Set<Artifact> artifacts) {
        super.setArtifacts(artifacts);
        return this;
    }

    public ServiceFileCombinationConfig setStrategy(CombinationStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public CombinationStrategy getStrategy() {
        return strategy;
    }
}
