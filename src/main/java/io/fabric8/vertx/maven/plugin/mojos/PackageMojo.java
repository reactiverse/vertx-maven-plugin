/*
 *
 *   Copyright (c) 2016 Red Hat, Inc.
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

import io.fabric8.vertx.maven.plugin.model.CombinationStrategy;
import io.fabric8.vertx.maven.plugin.utils.PackageHelper;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;


/**
 * This goal helps in packaging VertX application as uber jar with bundled dependencies
 *
 * @since 1.0.0
 */
@Mojo(name = "package",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class PackageMojo extends AbstractVertxMojo {


    /**
     * The service provider combination strategy.
     */
    @Parameter(name = "serviceProviderCombination", defaultValue = "combine")
    protected CombinationStrategy serviceProviderCombination;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be attached with that classifier and
     * the main artifact will be deployed as the main artifact. If this is not given (default), it will replace the
     * main artifact and only the Vert.x (fat) jar artifact will be deployed (in the Maven sense). Attaching the
     * artifact allows to deploy it alongside to the original one. Attachment is controlled using the `attach`
     * parameter.
     */
    @Parameter(name = "classifier")
    protected String classifier;

    /**
     * Whether or not the created archive needs to be attached to the project. If attached, the fat jar is
     * installed and deployed alongside the main artifact. Notice that you can't disable attachment if the classifier
     * is not set (it would mean detaching the main artifact).
     */
    @Parameter(name = "attach", defaultValue = "true")
    protected boolean attach;

    public static String computeOutputName(MavenProject project, String classifier) {
        String finalName = project.getBuild().getFinalName();
        if (finalName != null) {
            if (finalName.endsWith(".jar")) {
                finalName = finalName.substring(0, finalName.length() - 4);
            }
            if (classifier != null && !classifier.isEmpty()) {
                finalName += "-" + classifier;
            }
            finalName += ".jar";
            return finalName;
        } else {
            finalName = project.getArtifactId() + "-" + project.getVersion();
            if (classifier != null && !classifier.isEmpty()) {
                finalName += "-" + classifier;
            }
            finalName += ".jar";
            return finalName;
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("vertx:package skipped by configuration");
            return;
        }

        // Fix empty classifier.
        if (classifier != null  && classifier.trim().isEmpty()) {
            getLog().debug("The classifier is empty, it won't be used");
            classifier = null;
        }

        if (classifier == null && !attach) {
            throw new MojoExecutionException("Cannot disable attachment of the created archive when it's the main " +
                "artifact");
        }

        final Artifact artifact = this.project.getArtifact();

        Optional<File> primaryArtifactFile = getArtifactFile(artifact);

        //Step 0: Resolve and Collect Dependencies as g:a:v:t:c coordinates

        Set<Optional<File>> compileAndRuntimeDeps = extractArtifactPaths(this.project.getDependencyArtifacts());
        Set<Optional<File>> transitiveDeps = extractArtifactPaths(this.project.getArtifacts());

        PackageHelper packageHelper = new PackageHelper(this.launcher, this.verticle)
            .withOutputName(computeOutputName(project, classifier))
            .compileAndRuntimeDeps(compileAndRuntimeDeps)
            .transitiveDeps(transitiveDeps);

        //Step 1: build the jar add classifier and add it to project

        try {

            Path pathProjectBuildDir = Paths.get(this.projectBuildDir);

            File primaryFile = null;
            if (primaryArtifactFile.isPresent()) {
                primaryFile = primaryArtifactFile.get();
            }

            File fatJarFile = packageHelper
                .log(getLog())
                .build(pathProjectBuildDir, primaryFile);


            //  Perform the relocation of the service providers when serviceProviderCombination is defined
            if (serviceProviderCombination == null || serviceProviderCombination != CombinationStrategy.none) {
                packageHelper.combineServiceProviders(project,
                    pathProjectBuildDir, fatJarFile);
            }

            ArtifactHandler handler = new DefaultArtifactHandler("jar");
            if (classifier != null) {
                Artifact vertxJarArtifact = new DefaultArtifact(artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getScope()
                    , VERTX_PACKAGING, classifier, handler);
                vertxJarArtifact.setFile(fatJarFile);
                if (attach) {
                    this.project.addAttachedArtifact(vertxJarArtifact);
                }
            }
        } catch (Exception e) {
            throw new MojoFailureException("Unable to build fat jar", e);
        }

    }

}
