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
package io.reactiverse.vertx.maven.plugin.components;

import io.reactiverse.vertx.maven.plugin.mojos.AbstractVertxMojo;
import io.reactiverse.vertx.maven.plugin.mojos.Archive;
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

    @Override
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
