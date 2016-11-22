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
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

import io.fabric8.vertx.maven.plugin.utils.MojoUtils;
import io.fabric8.vertx.maven.plugin.utils.PackageHelper;


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
                .compileAndRuntimeDeps(compileAndRuntimeDeps)
                .transitiveDeps(transitiveDeps);

        //Step 1: build the jar add classifier and add it to project

        try {

            String fatJarName = this.project.getBuild().getFinalName();
            if (fatJarName == null) {
                fatJarName = this.project.getArtifactId();
            }
            File fatJarFile = packageHelper
                    .log(getLog())
                    .build(fatJarName, /* name is always != null */
                            Paths.get(this.projectBuildDir), primaryArtifactFile.get());

            ArtifactHandler handler = new DefaultArtifactHandler("jar");

            Artifact vertxJarArtifact = new DefaultArtifact(artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getScope()
                    , Constants.VERTX_PACKAGING, Constants.VERTX_CLASSIFIER, handler);
            vertxJarArtifact.setFile(fatJarFile);

            this.project.addAttachedArtifact(vertxJarArtifact);

        } catch (Exception e) {
            throw new MojoFailureException("Unable to build fat jar", e);
        }

    }

}
