package io.reactiverse.vertx.maven.plugin.mojos;

/**
 * A file allows individual file inclusion with the option to change the destination filename not supported
 * by fileSets. The source file may be already in the archive, in this case the file is "moved" to the new location.
 */
public class FileItem {


    /**
     * Sets the absolute or relative path from the project's directory of the file to be included in the archive.
     */
    private String source;

    /**
     * Sets the output directory relative to the root of the root directory of the archive. For
     * example, "bin" will put the specified files in the bin directory.
     */
    private String outputDirectory;

    /**
     * Sets the destination filename in the outputDirectory.
     * Default is the same name as the source's file.
     */
    private String destName;

    /**
     * Get sets the destination filename in the outputDirectory.
     * Default is the same name as the source's file.
     *
     * @return the file name
     */
    public String getDestName() {
        return this.destName;
    }

    /**
     * Get sets the output directory relative to the root of the root directory of the archive. For
     * example, "bin" will put the specified files in the bin directory.
     *
     * @return the output directory, {@code null} meaning the archive's root
     */
    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    /**
     * Get sets the absolute or relative path from the project's directory of the file to be included in the archive.
     *
     * @return the source file
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Set sets the destination filename in the outputDirectory.
     * Default is the same name as the source's file.
     *
     * @param destName the file name
     * @return the current {@link FileItem}
     */
    public FileItem setDestName(String destName) {
        this.destName = destName;
        return this;
    }

    /**
     * Set sets the output directory relative to the root of the root directory of the archive. For
     * example, "bin" will put the specified files in the bin directory.
     *
     * @param outputDirectory the output directory
     * @return the current {@link FileItem}
     */
    public FileItem setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    /**
     * Set sets the absolute or relative path from the project's directory of the file to be included in the archive.
     *
     * @param source the source
     * @return the current {@link FileItem}
     */
    public FileItem setSource(String source) {
        this.source = source;
        return this;
    }

}
