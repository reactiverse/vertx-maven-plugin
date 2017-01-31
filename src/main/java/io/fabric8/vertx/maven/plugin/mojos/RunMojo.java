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
     * Whether to run the vert.x in cluster mode
     *
     * @since 1.0.3
     */
    @Parameter(name = "cluster", defaultValue = "false")
    protected boolean cluster;

    /**
     * host to bind to for cluster communication. If this is not specified vert.x will attempt to
     * choose one from the available interfaces.
     *
     * @since 1.0.3
     */
    @Parameter(name = "clusterHost")
    protected String clusterHost;

    /**
     * Port to use for cluster communication. Default is 0 which  means choose a spare random port.
     *
     * @since 1.0.3
     */
    @Parameter(name = "clusterPort", defaultValue = "0")
    protected int clusterPort;

    /**
     * If specified the verticle will be deployed as a high availability (HA) deployment. This means it can
     * fail over to any other nodes in the cluster started with the same HA group.
     *
     * @since 1.0.3
     */
    @Parameter(name = "ha", defaultValue = "false")
    protected boolean ha;

    /**
     * used in conjunction with -ha this specifies the HA group this node  will join. There can be multiple HA
     * groups in a cluster. Nodes will only failover to other nodes in the same group.
     *
     * @since 1.0.3
     */
    @Parameter(name = "haGroup")
    protected String haGroup;

    /**
     * Used in conjunction with -ha this specifies the minimum number of  nodes in the cluster for any HA
     * deploymentIDs to be active.
     *
     * @since 1.0.3
     */
    @Parameter(name = "quorum")
    protected int quorum;

    /**
     * Specifies how many instances of the verticle will be deployed.
     *
     * @since 1.0.3
     */
    @Parameter(name = "instances")
    protected int instances;

    /**
     * If specified then the verticle is a worker verticle.
     *
     * @since 1.0.3
     */
    @Parameter(name = "worker", defaultValue = "false")
    protected boolean worker;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (runExtraArgs == null) {
            runExtraArgs = new ArrayList<>();
        }

        if (ha) {
            runExtraArgs.add("--ha");
            if (haGroup != null) {
                runExtraArgs.add("--hagroup=\"" + haGroup+"\"");
            }
            if (quorum != 0) {
                runExtraArgs.add("--quorum=" + quorum);
            }
        }

        if (cluster) {
            //as ha by default will be clusterable, so we don't need to specify it explicitly
            if (!ha) {
                runExtraArgs.add("--cluster");
            }
            if (clusterHost != null) {
                runExtraArgs.add("--cluster-host=\"" + clusterHost+"\"");
            }
            if (clusterPort != 0) {
                runExtraArgs.add("--cluster-port=" + clusterPort);
            }
        }

        if (instances > 0) {
            runExtraArgs.add("--instances=" + instances);
        }

        if (worker) {
            runExtraArgs.add("--worker");
        }

        super.execute();
    }
}
