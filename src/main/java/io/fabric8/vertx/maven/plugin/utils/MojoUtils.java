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

package io.fabric8.vertx.maven.plugin.utils;


import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.List;
import java.util.Optional;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

/**
 * @author kameshs
 */
public class MojoUtils {

    public static final String JAR_PLUGIN_KEY = "org.apache.maven.plugins:maven-jar-plugin";
    public static final String VERTX_PACKAGE_PLUGIN_KEY = "io.fabric8:vertx-maven-plugin";

    public static final String G_MAVEN_COMPILER_PLUGIN = "org.apache.maven.plugins";
    public static final String A_MAVEN_COMPILER_PLUGIN = "maven-compiler-plugin";
    public static final String V_MAVEN_COMPILER_PLUGIN = "3.1";
    public static final String GOAL_COMPILE = "compile";

    private Log logger;

    public MojoUtils withLog(Log log) {
        this.logger = log;
        return this;
    }

    /**
     * @param project
     * @param mavenSession
     * @param buildPluginManager
     * @throws MojoExecutionException
     */
    public void buildPrimaryArtifact(MavenProject project, MavenSession mavenSession,
                                     BuildPluginManager buildPluginManager) throws MojoExecutionException {

        if (logger != null && logger.isDebugEnabled()) {
            logger.debug("Primary artifact does not exist, building ...");
        }

        String packaging = project.getPackaging();

        if ("jar".equals(packaging)) {

            Optional<Plugin> jarPlugin = hasJarPlugin(project);

            if (jarPlugin.isPresent()) {
                executeMojo(
                        jarPlugin.get(),
                        goal("jar:jar"),
                        configuration(element("outputDirectory", "${project.build.outputDir}"),
                                element("classesDirectory", "${project.build.outputDirectory}")),
                        executionEnvironment(project, mavenSession, buildPluginManager)
                );
            } else {
                executeMojo(
                        plugin("org.apache.maven.plugins", "maven-jar-plugin"),
                        goal("jar:jar"),
                        configuration(element("outputDirectory", "${project.build.outputDir}"),
                                element("classesDirectory", "${project.build.outputDirectory}")),
                        executionEnvironment(project, mavenSession, buildPluginManager)
                );
            }


        } else {
            throw new MojoExecutionException("The packaging :" + packaging + " is not supported as of now");
        }

        throw new MojoExecutionException("The packaging :" + packaging + " is not supported as of now");
    }

    /**
     * @param project
     * @return
     */
    private Optional<Plugin> hasJarPlugin(MavenProject project) {
        Optional<Plugin> jarPlugin = project.getBuildPlugins().stream()
                .filter(plugin -> JAR_PLUGIN_KEY.equals(plugin.getKey()))
                .findFirst();
        return jarPlugin;
    }

    public void buildVertxArtifact(MavenProject project, MavenSession mavenSession,
                                   BuildPluginManager buildPluginManager) throws MojoExecutionException {

        Plugin vertxMavenPlugin = project.getPlugin(VERTX_PACKAGE_PLUGIN_KEY);

        if (vertxMavenPlugin == null) {
            throw new MojoExecutionException("Plugin :"+VERTX_PACKAGE_PLUGIN_KEY
                    +" not found or configured");
        }

        executeMojo(
                vertxMavenPlugin,
                goal("package"),
                configuration(),
                executionEnvironment(project, mavenSession, buildPluginManager)
        );
    }

    public void compile(MavenProject project, MavenSession mavenSession,
                        BuildPluginManager buildPluginManager) throws Exception {

        Optional<Plugin> mvnCompilerPlugin = project.getBuildPlugins().stream()
                .filter(plugin -> A_MAVEN_COMPILER_PLUGIN.equals(plugin.getArtifactId()))
                .findFirst();

        String pluginVersion = V_MAVEN_COMPILER_PLUGIN;

        if (mvnCompilerPlugin.isPresent()) {
            pluginVersion = mvnCompilerPlugin.get().getVersion();
        }

        Optional<Xpp3Dom> optConfiguration = buildConfiguration(project, A_MAVEN_COMPILER_PLUGIN, GOAL_COMPILE);

        if (optConfiguration.isPresent()) {

            Xpp3Dom configuration = optConfiguration.get();

            executeMojo(
                    plugin(G_MAVEN_COMPILER_PLUGIN, A_MAVEN_COMPILER_PLUGIN, pluginVersion),
                    goal(GOAL_COMPILE),
                    configuration,
                    executionEnvironment(project, mavenSession, buildPluginManager)
            );
        }
    }

    public Optional<Xpp3Dom> buildConfiguration(MavenProject project, String artifactId, String goal) {

        Optional<Plugin> pluginOptional = project.getBuildPlugins().stream()
                .filter(plugin -> artifactId
                        .equals(plugin.getArtifactId())).findFirst();

        Plugin plugin;

        if (pluginOptional.isPresent()) {

            plugin = pluginOptional.get();

            //Goal Level Configuration
            List<String> goals = (List<String>) plugin.getGoals();

            if (goals != null && goals.contains(goal)) {
                return Optional.of((Xpp3Dom) plugin.getConfiguration());
            }

            //Execution Configuration
            Optional<PluginExecution> executionOptional = plugin.getExecutions().stream()
                    .filter(e -> e.getGoals().contains(goal)).findFirst();

            if (executionOptional.isPresent()) {
                Optional.of((Xpp3Dom) executionOptional.get().getConfiguration());
            }

        } else {
            return Optional.empty();
        }

        //Global Configuration
        return Optional.of((Xpp3Dom) plugin.getConfiguration());
    }
}
