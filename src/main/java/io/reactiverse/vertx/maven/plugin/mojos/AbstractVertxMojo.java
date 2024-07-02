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

package io.reactiverse.vertx.maven.plugin.mojos;

import io.reactiverse.vertx.maven.plugin.utils.MavenUtils;
import io.reactiverse.vertx.maven.plugin.utils.WebJars;
import org.apache.commons.lang3.ClassUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The base Mojo class that will be extended by the other plugin goals
 */
public abstract class AbstractVertxMojo extends AbstractMojo implements Contextualizable {

    /* ==== Maven deps ==== */

    /**
     * The Maven project which will define and configure the vertx-maven-plugin
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
     * The maven project classes directory, defaults to target/classes
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    File classesDirectory;

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

    /**
     * The Maven Session.
     */
    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    @Component
    protected BuildPluginManager buildPluginManager;

    @Component
    protected RepositorySystem repositorySystem;

    /**
     * The component used to execute the second Maven execution.
     */
    @Component
    protected LifecycleExecutor lifecycleExecutor;

    /**
     * Configure the packaging content.
     */
    @Parameter
    protected Archive archive;

    /* ==== Config ====  */

    /**
     * The verticle that will be the main entry point on to the vertx application, the same property will be used
     * as &quot;Main-Verticle;&quot; attribute value on the MANIFEST.MF
     */
    @Parameter(alias = "verticle", property = "vertx.verticle")
    protected String verticle;

    /**
     * The main launcher class that will be used when launching the Vert.X applications.
     */
    @Parameter(property = "vertx.launcher")
    protected String launcher;

    /**
     * Skip (globally) the processing made by this plugin. All mojos are impacted by this parameter.
     */
    @Parameter(property = "vertx.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * The Plexus container.
     */
    protected PlexusContainer container;

    private List<File> classPathElements;
    private VertxVersionAdapter vertxVersionAdapter;

    // Visible for testing
    public MavenProject getProject() {
        return project;
    }

    /**
     * Retrieves the Plexus container.
     *
     * @param context the context
     * @throws ContextException if the container cannot be retrieved.
     */
    @Override
    public void contextualize(Context context) throws ContextException {
        container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
    }

    /**
     * List of classpath elements (classes directory and dependencies).
     */
    public List<File> getClassPathElements() throws MojoExecutionException {
        if (classPathElements == null) {
            classPathElements = new ArrayList<>();
            classPathElements.add(this.classesDirectory);
            classPathElements.addAll(extractArtifactPaths(this.project.getArtifacts()));
            getLog().debug("Classpath elements: " + classPathElements);
        }
        return classPathElements;
    }

    /**
     * Adapter for mojos.
     */
    public VertxVersionAdapter getVertxVersionAdapter() throws MojoExecutionException {
        if (vertxVersionAdapter == null) {
            vertxVersionAdapter = selectRunAdapter();
        }
        return vertxVersionAdapter;
    }

    private VertxVersionAdapter selectRunAdapter() throws MojoExecutionException {
        for (Artifact artifact : project.getArtifacts()) {
            if ("io.vertx".equals(artifact.getGroupId()) && "vertx-core".equals(artifact.getArtifactId())) {
                String version = artifact.getVersion();
                if (version.startsWith("4.")) {
                    return createVertx4VersionAdapter();
                }
                throw new MojoExecutionException("Unsupported Vert.x version: " + version);
            }
        }
        throw new MojoExecutionException("Vert.x core not found, it should be a dependency of the project.");
    }

    private Vertx4VersionAdapter createVertx4VersionAdapter() throws MojoExecutionException {
        String mainClass = StringUtils.isBlank(launcher) ? Vertx4VersionAdapter.IO_VERTX_CORE_LAUNCHER : launcher.trim();
        String mainVerticle = StringUtils.isBlank(verticle) ? null : verticle.trim();
        boolean isVertxLauncher = Vertx4VersionAdapter.IO_VERTX_CORE_LAUNCHER.equals(mainClass) || isMainClassInstanceOfLauncher(launcher, Vertx4VersionAdapter.IO_VERTX_CORE_LAUNCHER, getClassPathElements());
        if (isVertxLauncher) {
            if (StringUtils.isBlank(verticle)) {
                throw new MojoExecutionException("Invalid configuration, the element `verticle` (`vertx.verticle` property) is required.");
            }
        }
        return new Vertx4VersionAdapter(mainClass, mainVerticle, isVertxLauncher);
    }

    private static boolean isMainClassInstanceOfLauncher(String mainClassName, String launcherClassName, List<File> classPathElements) throws MojoExecutionException {
        URL[] classPathUrls = new URL[classPathElements.size()];
        for (int i = 0; i < classPathElements.size(); i++) {
            try {
                classPathUrls[i] = classPathElements.get(i).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Invalid classpath element: " + classPathElements.get(i), e);
            }
        }
        try (URLClassLoader classLoader = new URLClassLoader(classPathUrls)) {
            Class<?> mainClass = classLoader.loadClass(mainClassName);
            for (Class<?> superClass : ClassUtils.getAllSuperclasses(mainClass)) {
                if (launcherClassName.equals(superClass.getName())) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new MojoExecutionException("Failure while inspecting main class hierarchy", e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Could not find main class: " + mainClassName);
        }
    }

    private Set<File> extractArtifactPaths(Set<Artifact> artifacts) throws MojoExecutionException {
        try {
            Set<File> files = new LinkedHashSet<>();
            for (Artifact e : artifacts) {
                if (e.getScope().equals("compile") || e.getScope().equals("runtime")) {
                    if (e.getType().equals("jar")) {
                        if (!WebJars.isWebJar(e.getFile())) {
                            File resolvedArtifact = resolveArtifact(MavenUtils.asMavenCoordinates(e));
                            if (resolvedArtifact != null) {
                                files.add(resolvedArtifact.getAbsoluteFile());
                            }
                        }
                    }
                }
            }
            return files;
        } catch (IOException | ArtifactResolutionException ex) {
            throw new MojoExecutionException("Unable to extract artifact paths", ex);
        }
    }

    private File resolveArtifact(String artifact) throws ArtifactResolutionException {
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(new DefaultArtifact(artifact));
        ArtifactResult artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest);
        if (artifactResult.isResolved()) {
            getLog().debug("Resolved: " + artifactResult.getArtifact().getArtifactId());
            return artifactResult.getArtifact().getFile();
        }
        getLog().warn("Unable to resolve artifact: " + artifact);
        return null;
    }
}
