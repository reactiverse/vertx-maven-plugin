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

package io.fabric8.vertx.maven.plugin.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.ArrayList;
import java.util.List;

/**
 * This goal helps in debug a Vert.x applications as part of maven build.
 * Pressing <code>Ctrl+C</code> will then terminate the application
 *
 * @since 1.0.2
 */
@Mojo(name = "debug", threadSafe = true,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DebugMojo extends RunMojo {

    @Parameter(property = "debug.suspend", defaultValue = "false")
    boolean debugSuspend;

    @Parameter(property = "debug.port", defaultValue = "5005")
    int debugPort;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<String> debugOptions = computeDebugOptions();
        if (jvmArgs == null) {
            jvmArgs = new ArrayList<>();
        }
        jvmArgs.addAll(debugOptions);

        if (redeploy) {
            getLog().warn("Redeployment and Debug cannot be used together - disabling redeployment");
            redeploy = false;
        }

        getLog().info("The application will wait for a debugger to attach on debugPort " + debugPort);
        if (debugSuspend) {
            getLog().info("The application will wait for a debugger to attach");
        }

        super.execute();

    }

    private List<String> computeDebugOptions() {
        String debugger = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=" + (debugSuspend ? "y" : "n") + "," +
            "address=" + debugPort;
        String disableEventLoopchecker = "-Dvertx.options.maxEventLoopExecuteTime=" + Long.MAX_VALUE;
        String disableWorkerchecker = "-Dvertx.options.maxWorkerExecuteTime=" + Long.MAX_VALUE;
        String mark = "-Dvertx.debug=true";

        List<String> list = new ArrayList<>();
        list.add(debugger);
        list.add(disableEventLoopchecker);
        list.add(disableWorkerchecker);
        list.add(mark);
        return list;
    }

}
