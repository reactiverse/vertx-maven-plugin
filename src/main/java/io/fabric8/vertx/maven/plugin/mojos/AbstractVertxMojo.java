/*
 *   Copyright 2016 Kamesh Sampath
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.fabric8.vertx.maven.plugin.mojos;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The base Mojo class that will be extended by the other plugin goals
 */
public abstract class AbstractVertxMojo extends AbstractMojo {

    /* ==== Maven deps ==== */
    /**
     * The Maven project which will define and confiure the vertx-maven-plugin
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * The project build output directory, defaults to ${project.build.directory} which will be target directory
     * of the project
     */
    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    /**
     * The maven artifact resolution session, which will be used to resolve Maven artifacts
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected DefaultRepositorySystemSession repositorySystemSession;

    /**
     * The list of remote repositories that will be used to resolve artifacts
     */
    @Parameter(alias = "remoteRepositories", defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    protected List<ArtifactRepository> remoteRepositories;

    /* ==== Maven Components ==== */
    @Component
    protected MavenProjectHelper mavenProjectHelper;

    /**
     * this will get the current maven session
     */
    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    @Component
    protected BuildPluginManager buildPluginManager;

    @Component
    protected RepositorySystem repositorySystem;

    /* ==== Config ====  */
    // TODO-ROL: It would be awesome if this would not be required but, if not given,
    // the plugin tries to detect a single verticle. Maybe even decorated with a specific annotation ?
    // (like @MainVerticle ?). Only if no such verticle can be uniquely identified, then throw an exception.
    /**
     * The verticle that will be the main entry point on to the vertx application, the same property will be used
     * as &quot;Main-Verticle;&quot; attribute value on the MANIFEST.MF
     */
    @Parameter(alias = "verticle", property = "vertx.verticle")
    protected String verticle;

    /**
     * The main launcher class that will be used when launching the Vert.X applications.
     * It defaults to {@link io.vertx.core.Launcher}
     */
    @Parameter(defaultValue = "io.vertx.core.Launcher", property = "vertx.launcher")
    protected String launcher;

    public MavenProject getProject() {
        return project;
    }

    /**
     * this method resolves maven artifact from all configured repositories using the maven coordinates
     *
     * @param artifact - the maven coordinates of the artifact
     * @return {@link Optional} {@link File} pointing to the resolved artifact in local repository
     */
    protected Optional<File> resolveArtifact(String artifact) {
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(new org.eclipse.aether.artifact.DefaultArtifact(artifact));
        try {
            ArtifactResult artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest);
            if (artifactResult.isResolved()) {
                getLog().info("Resolved :" + artifactResult.getArtifact().getArtifactId());
                return Optional.of(artifactResult.getArtifact().getFile());
            } else {
                getLog().error("Unable to resolve:" + artifact);
            }
        } catch (ArtifactResolutionException e) {
            getLog().error("Unable to resolve:" + artifact);
        }

        return Optional.empty();
    }

    /**
     * this method helps in extracting the Artifact paths from the Maven local repository
     *
     * @param artifacts - the collection of artifacts which needs to be resolved to local {@link File}
     * @return A {@link Set} of {@link Optional} file paths
     */
    protected Set<Optional<File>> extractArtifactPaths(Set<Artifact> artifacts) {
        return artifacts
                .stream()
                .filter(e -> e.getScope().equals("compile") || e.getScope().equals("runtime"))
                .map(artifact -> asMavenCoordinates(artifact))
                .map(s -> resolveArtifact(s))
                .collect(Collectors.toSet());
    }

    /**
     * this method helps in resolving the {@link Artifact} as maven coordinates
     * coordinates ::= group:artifact:version:[packaging]:[classifier]
     * @param artifact - the artifact which need to be represented as maven coordinate
     * @return string representing the maven coordinate
     */
    protected String asMavenCoordinates(Artifact artifact) {
        // TODO-ROL: Shouldn't there be also the classified included after the groupId (if given ?)
        // Maybe we we should simply reuse DefaultArtifact.toString() (but could be too fragile as it might change
        // although I don't think it will change any time soon since probably many people already
        // rely on it)
        StringBuilder artifactCords = new StringBuilder().
                append(artifact.getGroupId())
                .append(":")
                .append(artifact.getArtifactId())
                .append(":")
                .append(artifact.getVersion());
        if (!"jar".equals(artifact.getType())) {
            artifactCords.append(":").append(artifact.getType());
        }
        if (artifact.hasClassifier()) {
            artifactCords.append(":").append(artifact.getClassifier());
        }
        return artifactCords.toString();
    }

    /**
     * This method returns the project's primary artifact file, this method tries to compute the artifact file name
     *  based on project finalName is configured or not
     * @param artifact - the project artifact for which the target file will be needed
     * @return {@link Optional<File>} representing the optional project primary artifact file
     */
    protected Optional<File> getArtifactFile(Artifact artifact) {
        final String finalName = this.project.getName();
        if (artifact == null) {
            Path finalNameArtifact = Paths.get(this.projectBuildDir, finalName + "." + this.project.getPackaging());
            if (Files.exists(finalNameArtifact)) {
                return Optional.of(finalNameArtifact.toFile());
            }
        } else {
            return Optional.of(artifact.getFile());
        }
        return Optional.empty();
    }

}
