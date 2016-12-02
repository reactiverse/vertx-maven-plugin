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

import io.fabric8.vertx.maven.plugin.model.CombinationStrategy;
import io.fabric8.vertx.maven.plugin.utils.MojoUtils;
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


    final MojoUtils mojoUtils = new MojoUtils();

    /**
     * The service provider combination strategy.
     */
    @Parameter(name = "serviceProviderCombination", defaultValue = "combine")
    protected CombinationStrategy serviceProviderCombination;

    /**
     * The artifact classifier. If not set, the plugin uses the default final name.
     */
    @Parameter(name = "classifier")
    protected String classifier;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        final Artifact artifact = this.project.getArtifact();

        Optional<File> primaryArtifactFile = getArtifactFile(artifact);

        if (!primaryArtifactFile.isPresent() || !primaryArtifactFile.get().exists()) {
            mojoUtils.withLog(getLog()).buildPrimaryArtifact(this.project, this.mavenSession, this.buildPluginManager);
        }

        //Step 0: Resolve and Collect Dependencies as g:a:v:t:c coordinates

        Set<Optional<File>> compileAndRuntimeDeps = extractArtifactPaths(this.project.getDependencyArtifacts());
        Set<Optional<File>> transitiveDeps = extractArtifactPaths(this.project.getArtifacts());

        PackageHelper packageHelper = new PackageHelper(this.launcher, this.verticle)
                .withOutputName(computeOutputName(project, classifier))
                .compileAndRuntimeDeps(compileAndRuntimeDeps)
                .transitiveDeps(transitiveDeps);

        //Step 1: build the jar add classifier and add it to project

        try {

            String fatJarName = this.project.getBuild().getFinalName();
            if (fatJarName == null) {
                fatJarName = this.project.getArtifactId();
            }

            Path pathProjectBuildDir = Paths.get(this.projectBuildDir);

            File fatJarFile = packageHelper
                    .log(getLog())
                    .build(pathProjectBuildDir, primaryArtifactFile.get());


            //  Perform the relocation of the service providers when serviceProviderCombination is defined
            if (serviceProviderCombination == null  || serviceProviderCombination != CombinationStrategy.none) {
                packageHelper.combineServiceProviders(project,
                    primaryArtifactFile.get(),
                        pathProjectBuildDir, fatJarFile);
            }

            ArtifactHandler handler = new DefaultArtifactHandler("jar");

            Artifact vertxJarArtifact = new DefaultArtifact(artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getScope()
                    , VERTX_PACKAGING, VERTX_CLASSIFIER, handler);
            vertxJarArtifact.setFile(fatJarFile);

            this.project.addAttachedArtifact(vertxJarArtifact);

        } catch (Exception e) {
            throw new MojoFailureException("Unable to build fat jar", e);
        }

    }

    public static String computeOutputName(MavenProject project, String classifier) {
        String finalName = project.getBuild().getFinalName();
        if (finalName != null) {
            if (finalName.endsWith(".jar")) {
                finalName = finalName.substring(0, finalName.length() -4);
            }
            if (classifier != null  && ! classifier.isEmpty()) {
                finalName += "-" + classifier;
            }
            finalName += ".jar";
            return finalName;
        } else {
            finalName = project.getArtifactId() + "-" + project.getVersion();
            if (classifier != null  && ! classifier.isEmpty()) {
                finalName += "-" + classifier;
            }
            finalName += ".jar";
            return finalName;
        }
    }

}
