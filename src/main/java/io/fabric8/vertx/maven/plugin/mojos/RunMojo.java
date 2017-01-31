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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.ArrayList;
import java.util.List;

/**
 * This goal helps in running Vert.x applications as part of maven build.
 * Pressing <code>Ctrl+C</code> will then terminate the application
 *
 * @since 1.0.0
 */
@Mojo(name = "run", threadSafe = true,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RunMojo extends AbstractRunMojo {

    /**
     * The custom or additional run arguments that can be passed to the Launcher
     * e.g. --cluster --ha etc., which is typically passed via vert.x CLI
     *
     * @since 1.0.3
     */
    @Parameter(name = "runArgs", property = "vertx.runArgs")
    protected List<String> runArgs;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (optionalRunExtraArgs == null) {
            optionalRunExtraArgs = new ArrayList<>();
        }

        if (runArgs != null && !runArgs.isEmpty()) {
            optionalRunExtraArgs.addAll(runArgs);
        }

        super.execute();
    }
}
