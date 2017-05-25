/*
 *
 *   Copyright (c) 2016-2017 Red Hat, Inc.
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

package io.fabric8.vertx.maven.plugin.mojos;

import io.fabric8.vertx.maven.plugin.utils.WebJars;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@Mojo(name = "initialize",
    defaultPhase = LifecyclePhase.INITIALIZE,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class InitializeMojo extends AbstractVertxMojo {

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter
    private File webRoot;

    @Parameter(defaultValue = "true")
    private boolean stripWebJarVersion;

    @Parameter(defaultValue = "true")
    private boolean stripJavaScriptDependencyVersion;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("vertx:initialize skipped by configuration");
            return;
        }

        // Initialize the web root directory with Vert.x web default.
        // The directory is only created on demand.
        if (webRoot == null) {
            webRoot = new File(project.getBuild().getOutputDirectory(), "webroot");
        }

        Set<Artifact> dependencies = new LinkedHashSet<>();
        dependencies.addAll(this.project.getDependencyArtifacts());
        dependencies.addAll(this.project.getArtifacts());

        copyJSDependencies(dependencies);
        unpackWebjars(dependencies);

        // Start the spy
        MavenExecutionRequest request = session.getRequest();
        MojoSpy.init(request);
    }

    private void unpackWebjars(Set<Artifact> dependencies) throws MojoExecutionException {
        for (Artifact artifact : dependencies) {
            Optional<File> maybeFile = getArtifactFile(artifact);
            if (artifact.getType().equalsIgnoreCase("jar")  && maybeFile.isPresent()) {
                if (WebJars.isWebJar(getLog(), maybeFile.get())) {
                    try {
                        WebJars.extract(this, maybeFile.get(),
                            createWebRootDirIfNeeded(), stripWebJarVersion);
                    } catch (IOException e) {
                        throw new MojoExecutionException("Unable to unpack '"
                            + artifact.toString() + "'", e);
                    }
                }
            }
        }
    }

    private File createWebRootDirIfNeeded() {
        if (! webRoot.isDirectory()) {
            boolean created = webRoot.mkdirs();
            if (created) {
                getLog().debug("Webroot directory created: " + webRoot.getAbsolutePath());
            } else {
                getLog().error("Unable to create directory: " + webRoot.getAbsolutePath());
            }
        }

        return webRoot;
    }

    private void copyJSDependencies(Set<Artifact> dependencies) throws MojoExecutionException {
        for (Artifact artifact : dependencies) {
            if (artifact.getType().equalsIgnoreCase("js")) {
                Optional<File> file = getArtifactFile(artifact);
                if (file.isPresent()) {
                    try {
                        if (stripJavaScriptDependencyVersion) {
                            String name = artifact.getArtifactId();
                            if (artifact.getClassifier() != null) {
                                name += "-" + artifact.getClassifier();
                            }
                            name += ".js";
                            File output = new File(createWebRootDirIfNeeded(), name);
                            FileUtils.copyFile(file.get(), output);
                        } else {
                            FileUtils.copyFileToDirectory(file.get(), createWebRootDirIfNeeded());
                        }
                    } catch (IOException e) {
                        throw new MojoExecutionException("Unable to copy '"
                            + artifact.toString() + "'", e);
                    }
                } else {
                    getLog().warn("Skipped the copy of '"
                        + artifact.toString() + "' - The artifact file does not exist");
                }
            }
        }
    }
}
