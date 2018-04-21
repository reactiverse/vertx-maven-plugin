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

package io.reactiverse.vertx.maven.plugin.utils;


import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

/**
 * Replays a mojo execution.
 *
 * @author Clement Escoffier
 */
public class MojoExecutor {

    private final Xpp3Dom configuration;
    private final String goal;
    private final MavenSession session;
    private final MavenProject project;
    private final BuildPluginManager build;
    private final Plugin plugin;

    public MojoExecutor(MojoExecution execution, MavenProject project, MavenSession session, BuildPluginManager buildPluginManager) {
        this.build = buildPluginManager;
        this.project = project;
        this.session = session;
        this.plugin = execution.getPlugin();
        configuration = execution.getConfiguration();
        goal = execution.getGoal();
    }

    /**
     * Execute the mojo.
     */
    public void execute() throws MojoExecutionException {
        executeMojo(
            plugin,
            goal,
            configuration,
            executionEnvironment(project, session, build)
        );
    }
}
