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

package io.fabric8.vertx.maven.plugin.utils;


import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * @author kameshs
 */
public class MojoUtils {

    /*===  Plugin Keys ====*/

    private static final String RESOURCES_PLUGIN_KEY = "org.apache.maven.plugins:maven-resources-plugin";

    /*===  Plugins ====*/

    private static final String G_MAVEN_RESOURCES_PLUGIN = "org.apache.maven.plugins";
    private static final String A_MAVEN_RESOURCES_PLUGIN = "maven-resources-plugin";
    private static final String V_MAVEN_RESOURCES_PLUGIN = "maven-resources-plugin-version";

    private static final String G_MAVEN_COMPILER_PLUGIN = "org.apache.maven.plugins";
    private static final String A_MAVEN_COMPILER_PLUGIN = "maven-compiler-plugin";
    private static final String V_MAVEN_COMPILER_PLUGIN = "maven-compiler-plugin-version";

    /*===  Goals ====*/
    private static final String GOAL_COMPILE = "compile";
    private static final String GOAL_RESOURCES = "resources";

    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private MojoUtils() {
        // Avoid direct instantiation
    }

    /**
     * Executes the Maven Resource Plugin to copy resources to `target/classes`
     *
     * @param project            the project
     * @param mavenSession       the maven session
     * @param buildPluginManager the build plugin manager
     * @throws MojoExecutionException if the copy cannot be completed successfully
     */
    public static void copyResources(MavenProject project, MavenSession mavenSession,
                                     BuildPluginManager buildPluginManager) throws MojoExecutionException {

        Optional<Plugin> resourcesPlugin = hasPlugin(project, RESOURCES_PLUGIN_KEY);

        Xpp3Dom pluginConfig = configuration(element("outputDirectory", "${project.build.outputDirectory}"));

        if (resourcesPlugin.isPresent()) {

            Optional<Xpp3Dom> optConfiguration = buildConfiguration(project, A_MAVEN_RESOURCES_PLUGIN, GOAL_RESOURCES);

            if (optConfiguration.isPresent()) {
                pluginConfig = optConfiguration.get();
            }

            executeMojo(
                resourcesPlugin.get(),
                goal(GOAL_RESOURCES),
                pluginConfig,
                executionEnvironment(project, mavenSession, buildPluginManager)
            );

        } else {
            executeMojo(
                plugin(G_MAVEN_RESOURCES_PLUGIN, A_MAVEN_RESOURCES_PLUGIN,
                    properties.getProperty(V_MAVEN_RESOURCES_PLUGIN)),
                goal(GOAL_RESOURCES),
                pluginConfig,
                executionEnvironment(project, mavenSession, buildPluginManager)
            );
        }

    }

    /**
     * Checks whether or not the given project has a plugin with the given key. The key is given using the
     * "groupId:artifactId" syntax.
     *
     * @param project   the project
     * @param pluginKey the plugin
     * @return an Optional completed if the plugin is found.
     */
    public static Optional<Plugin> hasPlugin(MavenProject project, String pluginKey) {
        Optional<Plugin> optPlugin = project.getBuildPlugins().stream()
            .filter(plugin -> pluginKey.equals(plugin.getKey()))
            .findFirst();

        if (!optPlugin.isPresent() && project.getPluginManagement() != null) {
            optPlugin = project.getPluginManagement().getPlugins().stream()
                .filter(plugin -> pluginKey.equals(plugin.getKey()))
                .findFirst();
        }
        return optPlugin;
    }

    /**
     * Checks whether the project has the dependency
     *
     * @param project    - the project to check existence of dependency
     * @param groupId    - the dependency groupId
     * @param artifactId - the dependency artifactId
     * @return true if the project has the dependency
     */
    public static boolean hasDependency(MavenProject project, String groupId, String artifactId) {

        Optional<Dependency> dep = project.getDependencies().stream()
            .filter(d -> groupId.equals(d.getGroupId())
                && artifactId.equals(d.getArtifactId())).findFirst();

        return dep.isPresent();
    }

    /**
     * Execute the Maven Compiler Plugin to compile java sources.
     *
     * @param project            the project
     * @param mavenSession       the session
     * @param buildPluginManager the build plugin manager
     * @throws Exception if the compilation fails for any reason
     */
    public static void compile(MavenProject project, MavenSession mavenSession,
                               BuildPluginManager buildPluginManager) throws Exception {

        Optional<Plugin> mvnCompilerPlugin = project.getBuildPlugins().stream()
            .filter(plugin -> A_MAVEN_COMPILER_PLUGIN.equals(plugin.getArtifactId()))
            .findFirst();

        String pluginVersion = properties.getProperty(V_MAVEN_COMPILER_PLUGIN);

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

    @SuppressWarnings("unchecked")
    private static List<String> goals(Object goals) {
        if (goals instanceof List) {
            return (List<String>) goals;
        } else {
            return null;
        }
    }

    /**
     * @param project
     * @param artifactId
     * @param goal
     * @return
     */
    public static Optional<Xpp3Dom> buildConfiguration(MavenProject project, String artifactId, String goal) {

        Optional<Plugin> pluginOptional = project.getBuildPlugins().stream()
            .filter(plugin -> artifactId
                .equals(plugin.getArtifactId())).findFirst();

        Plugin plugin;

        if (pluginOptional.isPresent()) {

            plugin = pluginOptional.get();

            //Goal Level Configuration
            List<String> goals = goals(plugin.getGoals());

            if (goals != null && goals.contains(goal)) {
                return Optional.ofNullable((Xpp3Dom) plugin.getConfiguration());
            }

            //Execution Configuration
            Optional<PluginExecution> executionOptional = plugin.getExecutions().stream()
                .filter(e -> e.getGoals().contains(goal)).findFirst();

            executionOptional
                .ifPresent(pluginExecution -> Optional.ofNullable((Xpp3Dom) pluginExecution.getConfiguration()));

        } else {
            return Optional.empty();
        }

        //Global Configuration
        return Optional.ofNullable((Xpp3Dom) plugin.getConfiguration());
    }

    private static void loadProperties() {
        URL url = MojoUtils.class.getClassLoader().getResource("vertx-maven-plugin.properties");
        Objects.requireNonNull(url);
        try (InputStream in = url.openStream()) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Invalid packaging of the vertx-maven-plugin, the vertx-maven-plugin" +
                ".properties file cannot be read", e);
        }
    }

    public static String getVersion(String key) {
        return properties.getProperty(key);
    }
}
