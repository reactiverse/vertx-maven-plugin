package io.fabric8.vertx.maven.plugin.mojos;

import org.apache.maven.plugins.assembly.model.DependencySet;
import org.apache.maven.plugins.assembly.model.FileItem;
import org.apache.maven.plugins.assembly.model.FileSet;

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
        return this.dependencySets;
    }

    /**
     * Gets the file sets
     *
     * @return the list
     */
    public List<FileSet> getFileSets() {
        return this.fileSets;
    }

    /**
     * Get the file.
     *
     * @return the list
     */
    public List<FileItem> getFiles() {
        return this.files;
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
}
