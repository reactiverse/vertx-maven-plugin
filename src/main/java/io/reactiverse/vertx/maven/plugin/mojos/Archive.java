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


import java.util.*;


/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Archive {

    private String outputFileName;

    private Map<String, String> manifest = new LinkedHashMap<>();

    private List<FileSet> fileSets = new ArrayList<>();

    private List<FileItem> files = new ArrayList<>();

    private List<DependencySet> dependencySets = new ArrayList<>();

    private boolean includeClasses = true;

    private List<String> fileCombinationPatterns = new ArrayList<>();

    /**
     * Adds a dependency set
     *
     * @param dependencySet the set, must not be {@code null}
     */
    public Archive addDependencySet(DependencySet dependencySet) {
        getDependencySets().add(dependencySet);
        return this;
    }

    /**
     * Adds a specific file
     *
     * @param fileItem the file, must not be {@code null}
     */
    public Archive addFile(FileItem fileItem) {
        getFiles().add(fileItem);
        return this;
    }

    /**
     * Adds a file set
     *
     * @param fileSet the set, must not be {@code null}
     */
    public Archive addFileSet(FileSet fileSet) {
        getFileSets().add(fileSet);
        return this;
    }

    /**
     * Gets the dependency sets.
     *
     * @return the list
     */
    public List<DependencySet> getDependencySets() {
        return dependencySets;
    }

    /**
     * Gets the file sets
     *
     * @return the list
     */
    public List<FileSet> getFileSets() {
        return fileSets;
    }

    /**
     * Get the file.
     *
     * @return the list
     */
    public List<FileItem> getFiles() {
        return files;
    }

    /**
     * Remove a dependency set.
     *
     * @param dependencySet the set to remove, must not be {@code null}
     */
    public Archive removeDependencySet(DependencySet dependencySet) {
        getDependencySets().remove(dependencySet);
        return this;
    }

    /**
     * Remove a file item set.
     *
     * @param fileItem the file to remove, must not be {@code null}
     */
    public Archive removeFile(FileItem fileItem) {
        getFiles().remove(fileItem);
        return this;
    }

    /**
     * Remove a file set.
     *
     * @param fileSet the file to remove, must not be {@code null}
     */
    public Archive removeFileSet(FileSet fileSet) {
        getFileSets().remove(fileSet);
        return this;
    }

    /**
     * Set the dependency sets.
     *
     * @param dependencySets the set, must not be {@code null} or contain {@code null} element
     */
    public Archive setDependencySets(List<DependencySet> dependencySets) {
        this.dependencySets = Objects.requireNonNull(dependencySets);
        return this;
    }

    /**
     * Set the file sets.
     *
     * @param fileSets the set, must not be {@code null} or contain {@code null} element
     */
    public Archive setFileSets(List<FileSet> fileSets) {
        this.fileSets = Objects.requireNonNull(fileSets);
        return this;
    }

    /**
     * Set the file items.
     *
     * @param files the set, must not be {@code null} or contain {@code null} element
     */
    public Archive setFiles(List<FileItem> files) {
        this.files = files;
        return this;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public Archive setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
        return this;
    }

    public Map<String, String> getManifest() {
        return manifest;
    }

    public Archive setManifest(Map<String, String> manifest) {
        this.manifest = manifest;
        return this;
    }

    public boolean isIncludeClasses() {
        return includeClasses;
    }

    public Archive setIncludeClasses(boolean includeClasses) {
        this.includeClasses = includeClasses;
        return this;
    }

    public Archive addFileCombinationPattern(String pattern) {
        this.fileCombinationPatterns.add(Objects.requireNonNull(pattern));
        if (! pattern.startsWith("/")) {
            this.fileCombinationPatterns.add("/" + pattern);
        }
        return this;
    }

    public Archive removeFileCombinationPattern(String pattern) {
        this.fileCombinationPatterns.remove(Objects.requireNonNull(pattern));
        return this;
    }

    public Archive setFileCombinationPatterns(List<String> patterns) {
        Objects.requireNonNull(patterns).forEach(this::addFileCombinationPattern);
        return this;
    }

    public List<String> getFileCombinationPatterns() {
        return fileCombinationPatterns;
    }
}
