/*
 * Copyright 2024 The Vert.x Community.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactiverse.vertx.maven.plugin.mojos;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

public class Redeployment {

    /**
     * Whether redeployment is enabled.
     */
    @Parameter(property = "vertx.redeploy.enabled", defaultValue = "true")
    private boolean enabled;

    /**
     * The root directory to scan for changes.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main")
    private File rootDirectory;

    /**
     * A list of <a href="https://ant.apache.org/manual/dirtasks.html#patterns">Ant-like</a> patterns of files/directories to include in change monitoring.
     * <p>
     * The patterns must be expressed relatively to the {@link #rootDirectory}.
     */
    @Parameter
    private List<String> includes;

    /**
     * A list of <a href="https://ant.apache.org/manual/dirtasks.html#patterns">Ant-like</a> patterns of files/directories to exclude from change monitoring.
     * <p>
     * The patterns must be expressed relatively to the {@link #rootDirectory}.
     */
    @Parameter
    private List<String> excludes;

    /**
     * How often, in milliseconds, should the source files be scanned for file changes.
     */
    @Parameter(property = "vertx.redeploy.scan.period", defaultValue = "250")
    private long scanPeriod;

    /**
     * How long, in milliseconds, the plugin should wait between two redeployments.
     */
    @Parameter(property = "vertx.redeploy.grace.period", defaultValue = "1000")
    private long gracePeriod;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public long getScanPeriod() {
        return scanPeriod;
    }

    public void setScanPeriod(long scanPeriod) {
        this.scanPeriod = scanPeriod;
    }

    public long getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(long gracePeriod) {
        this.gracePeriod = gracePeriod;
    }
}
