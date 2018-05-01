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

package io.reactiverse.vertx.maven.plugin.mojos;

import io.reactiverse.vertx.maven.plugin.components.*;
import io.reactiverse.vertx.maven.plugin.model.CombinationStrategy;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.util.List;


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
    @Parameter(name = "serviceProviderCombination", defaultValue = "COMBINE")
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

    @Component
    protected PackageService packageService;

    @Component
    protected ServiceFileCombiner combiner;

    @Parameter(alias = "skipScmMetadata", property = "vertx.skipScmMetadata", defaultValue = "false")
    protected boolean skipScmMetadata;

    private static final String JAR_EXTENSION = ".jar";

    public static String computeOutputName(MavenProject project, String classifier) {
        String finalName = project.getBuild().getFinalName();
        if (finalName != null) {
            if (finalName.endsWith(JAR_EXTENSION)) {
                finalName = finalName.substring(0, finalName.length() - JAR_EXTENSION.length());
            }
            if (classifier != null && !classifier.isEmpty()) {
                finalName += "-" + classifier;
            }
            finalName += JAR_EXTENSION;
            return finalName;
        } else {
            finalName = project.getArtifactId() + "-" + project.getVersion();
            if (classifier != null && !classifier.isEmpty()) {
                finalName += "-" + classifier;
            }
            finalName += JAR_EXTENSION;
            return finalName;
        }
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("vertx:package skipped by configuration");
            return;
        }

        fixEmptyClassifier();


        if (classifier == null && !attach) {
            throw new MojoExecutionException("Cannot disable attachment of the created archive when it's the main " +
                "artifact");
        }

        //TODO Archive should be a parameter.
        Archive archive = ServiceUtils.getDefaultFatJar();

        extendManifest(archive);

        List<ManifestCustomizerService> customizers = getManifestCustomizers();
        customizers.forEach(customizer ->
            archive.getManifest().putAll(customizer.getEntries(this, project)));

        // Manage SPI combination
        combiner.doCombine(new ServiceFileCombinationConfig()
            .setStrategy(serviceProviderCombination)
            .setProject(project)
            .setArchive(archive)
            .setMojo(this)
            .setArtifacts(project.getArtifacts()));

        File jar;
        try {
            jar = packageService.doPackage(
                new PackageConfig()
                    .setArtifacts(project.getArtifacts())
                    .setMojo(this)
                    .setOutput(new File(projectBuildDir, computeOutputName(project, classifier)))
                    .setProject(project)
                    .setArchive(archive));
        } catch (PackagingException e) {
            throw new MojoExecutionException("Unable to build the fat jar", e);
        }

        attachIfNeeded(jar);

    }

    private void extendManifest(Archive archive) {
        if (launcher != null && !launcher.trim().isEmpty()) {
            archive.getManifest().putIfAbsent("Main-Class", launcher);
        }

        if (verticle != null && !verticle.trim().isEmpty()) {
            archive.getManifest().putIfAbsent("Main-Verticle", verticle);
        }
    }

    private void attachIfNeeded(File jar) {
        if (jar.isFile() && classifier != null && attach) {
            ArtifactHandler handler = new DefaultArtifactHandler("jar");
            Artifact vertxJarArtifact = new DefaultArtifact(project.getGroupId(),
                project.getArtifactId(), project.getVersion(), "compile",
                "jar", classifier, handler);
            vertxJarArtifact.setFile(jar);
            this.project.addAttachedArtifact(vertxJarArtifact);
        }
    }

    private void fixEmptyClassifier() {
        // Fix empty classifier.
        if (classifier != null && classifier.trim().isEmpty()) {
            getLog().debug("The classifier is empty, it won't be used");
            classifier = null;
        }
    }

    private List<ManifestCustomizerService> getManifestCustomizers() throws MojoExecutionException {
        List<ManifestCustomizerService> customizers;
        try {
            customizers = container.lookupList(ManifestCustomizerService.class);
        } catch (ComponentLookupException e) {
            getLog().debug("ManifestCustomerService lookup failed", e);
            throw new MojoExecutionException("Unable to retrieve the " +
                ManifestCustomizerService.class.getName() + " components");
        }
        return customizers;
    }

    public boolean isSkipScmMetadata() {
        return skipScmMetadata;
    }
}
