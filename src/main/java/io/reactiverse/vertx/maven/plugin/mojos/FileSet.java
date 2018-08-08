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


import java.util.List;

/**
 * A fileSet allows the inclusion of groups of files into the archive.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FileSet {

    /**
     * Whether standard exclusion patterns, such as those matching CVS and Subversion
     * metadata files, should be used when calculating the files affected by this set.
     * The default value is true.
     */
    private boolean useDefaultExcludes = true;

    /**
     * Sets the output directory relative to the root of the root directory of the archive. For
     * example, "bin" will put the specified files in the bin directory into the archive.
     */
    private String outputDirectory;

    /**
     * Set of includes.
     */
    private List<String> includes;

    /**
     * Set of excludes excludes.
     */
    private List<String> excludes;

    /**
     * Sets the absolute or relative location from the module's directory. For example,
     * "src/main/bin" would select this subdirectory of the project in which this
     * file set is defined.
     */
    private String directory;


    /**
     * Adds an exclusion
     *
     * @param pattern the exclusion
     */
    public FileSet addExclude(String pattern) {
        getExcludes().add(pattern);
        return this;
    }

    /**
     * Adds an inclusion
     *
     * @param pattern the pattern
     */
    public FileSet addInclude(String pattern) {
        getIncludes().add(pattern);
        return this;
    }

    /**
     * Get sets the absolute or relative location from the module's directory. For example, "src/main/bin" would
     * select this subdirectory of the project in which this set is defined.
     *
     * @return the directory
     */
    public String getDirectory() {
        return this.directory;
    }

    /**
     * @return the list of exclusion patterns, empty if not set.
     */
    public List<String> getExcludes() {
        if (this.excludes == null) {
            this.excludes = new java.util.ArrayList<>();
        }
        return this.excludes;
    }

    /**
     * @return the list of inclusion patterns, empty if not set.
     */
    public List<String> getIncludes() {
        if (this.includes == null) {
            this.includes = new java.util.ArrayList<>();
        }
        return this.includes;
    }

    /**
     * Get sets the output directory relative to the root of the root directory of the archive. For
     * example, "bin" will put the specified files in the bin directory.
     *
     * @return the output directory
     */
    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    /**
     * Get whether standard exclusion patterns, such as those matching CVS and Subversion
     * metadata files, should be used when calculating the files affected by this set.
     * The default value is true.
     *
     * @return whether or not the default excluded should be added to the set of exclusion patterns
     */
    public boolean isUseDefaultExcludes() {
        return this.useDefaultExcludes;
    }

    /**
     * Removes an exclusion pattern.
     *
     * @param pattern the pattern
     */
    public FileSet removeExclude(String pattern) {
        getExcludes().remove(pattern);
        return this;
    }

    /**
     * Removes an inclusion pattern.
     *
     * @param pattern the pattern
     */
    public FileSet removeInclude(String pattern) {
        getIncludes().remove(pattern);
        return this;
    }

    /**
     * Set sets the absolute or relative location from the module's directory. For example, "src/main/bin" would
     * select this subdirectory of the project in which this set is defined.
     *
     * @param directory the directory
     */
    public FileSet setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    /**
     * Set when &lt;exclude&gt; subelements are present, they define a set of
     * files and directory to exclude. If none is
     * present, then &lt;excludes&gt; represents no exclusions.
     *
     * @param excludes the set of exclusions
     */
    public FileSet setExcludes(List<String> excludes) {
        this.excludes = excludes;
        return this;
    }

    /**
     * Set when &lt;include&gt; subelements are present, they define a set of
     * files and directory to include. If none is present, then
     * &lt;includes&gt; represents all valid values.
     *
     * @param includes the set of inclusions
     */
    public FileSet setIncludes(List<String> includes) {
        this.includes = includes;
        return this;
    }

    /**
     * Set sets the output directory relative to the root of the root directory of the archive. For
     * example, "bin" will put the specified files in the bin directory.
     *
     * @param outputDirectory the output directory
     */
    public FileSet setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    /**
     * Set whether standard exclusion patterns, such as those matching CVS and Subversion
     * metadata files, should be used when calculating the files affected by this set.
     * the default value is true.
     *
     * @param useDefaultExcludes whether or not the default excluded should be added to the set of exclusions
     */
    public FileSet setUseDefaultExcludes(boolean useDefaultExcludes) {
        this.useDefaultExcludes = useDefaultExcludes;
        return this;
    }

}

