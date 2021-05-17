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
package io.reactiverse.vertx.maven.plugin.components.impl;

import io.reactiverse.vertx.maven.plugin.components.*;
import io.reactiverse.vertx.maven.plugin.mojos.Archive;
import io.reactiverse.vertx.maven.plugin.mojos.DependencySet;
import io.reactiverse.vertx.maven.plugin.mojos.FileItem;
import io.reactiverse.vertx.maven.plugin.mojos.FileSet;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.resolve.ScopeFilter;
import org.apache.maven.shared.artifact.filter.resolve.transform.ArtifactIncludeFilterTransformer;
import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.apache.maven.shared.utils.io.SelectorUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Service packaging the fat jar using ShrinkWrap.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@Component(
    role = PackageService.class,
    hint = "fat-jar")
public class ShrinkWrapFatJarPackageService implements PackageService {

    private static final List<String> DEFAULT_EXCLUDES;

    static {
        DEFAULT_EXCLUDES = new ArrayList<>(FileUtils.getDefaultExcludesAsList());
        DEFAULT_EXCLUDES.add("**/*.DSA");
        DEFAULT_EXCLUDES.add("**/*.RSA");
        DEFAULT_EXCLUDES.add("**/INDEX.LIST");
        DEFAULT_EXCLUDES.add("**/*.SF");
    }

    @Override
    public PackageType type() {
        return PackageType.FAT_JAR;
    }

    @Override
    public File doPackage(PackageConfig config) throws PackagingException {

        Log logger = Objects.requireNonNull(config.getMojo().getLog());
        Archive archive = Objects.requireNonNull(config.getArchive());

        JavaArchive jar = ShrinkWrap.create(JavaArchive.class);

        addDependencies(config, archive.getDependencySets(), jar);

        addFileSets(config, archive, jar);

        addProjectClasses(config, archive, jar);

        // File Items
        for (FileItem item : archive.getFiles()) {
            embedFile(config, jar, item);
        }

        // Generate manifest
        try {
            generateManifest(jar, archive.getManifest());
        } catch (IOException e) {
            throw new PackagingException(e);
        }

        // Generate output file
        File jarFile;

        try {
            jarFile = config.getOutput();

            boolean useTmpFile = false;
            File theCreatedFile = jarFile;
            if (jarFile.isFile()) {
                useTmpFile = true;
                theCreatedFile = new File(jarFile.getParentFile(), jarFile.getName() + ".tmp");
            }

            jar.as(ZipExporter.class).exportTo(theCreatedFile);

            if (useTmpFile) {
                boolean delete = Files.deleteIfExists(jarFile.toPath());
                boolean renameTo = theCreatedFile.renameTo(jarFile);
                logger.debug("Main jar file deleted: " + delete);
                logger.debug("Main jar file replaced by temporary file: " + renameTo);
            }

        } catch (Exception e) {
            throw new PackagingException(e);
        }

        return jarFile;
    }

    private void addFileSets(PackageConfig config, Archive archive, JavaArchive jar) {
        Log logger = config.getMojo().getLog();
        for (FileSet fs : archive.getFileSets()) {
            embedFileSet(logger, config.getProject(), fs, jar);
        }
    }

    private static void addProjectClasses(PackageConfig config, Archive archive, JavaArchive jar) {
        if (archive.isIncludeClasses()) {
            File classes = new File(config.getProject().getBuild().getOutputDirectory());
            if (classes.isDirectory()) {
                jar.addAsResource(classes, "/");
            }
        }
    }

    private void addDependencies(PackageConfig config, Collection<DependencySet> dependencies, JavaArchive jar) {
        Log logger = config.getMojo().getLog();
        for (DependencySet ds : dependencies) {
            ScopeFilter scopeFilter = ServiceUtils.newScopeFilter(ds.getScope());
            ArtifactFilter filter = new ArtifactIncludeFilterTransformer().transform(scopeFilter);
            Set<Artifact> artifacts = ServiceUtils.filterArtifacts(config.getArtifacts(),
                ds.getIncludes(), ds.getExcludes(),
                ds.isUseTransitiveDependencies(), logger, filter);

            for (Artifact artifact : artifacts) {
                File file = artifact.getFile();
                if (file.isFile()) {
                    logger.debug("Adding Dependency :" + artifact);
                    embedDependency(logger, ds, jar, file);
                } else {
                    logger.warn("Cannot embed artifact " + artifact
                        + " - the file does not exist");
                }
            }
        }
    }

    private static void embedFile(PackageConfig config, JavaArchive jar, FileItem item) throws PackagingException {
        String path;
        if (item.getOutputDirectory() == null) {
            path = "/";
        } else {
            path = item.getOutputDirectory();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (!path.endsWith("/")) {
                path = path + "/";
            }
        }

        File source = new File(config.getProject().getBasedir(), item.getSource());
        if (!source.isFile()) {
            Node node = jar.get(item.getSource());
            if (node == null) {
                throw new PackagingException("Unable to handle the file item " + item.getSource() + ", " +
                    "file not found in the project or in the archive.");
            }

            String name = item.getDestName();
            if (name == null) {
                name = node.getPath().get().substring(node.getPath().getParent().get().length() + 1);
            }

            String out = path + name;
            jar.add(node.getAsset(), out);
            jar.delete(node.getPath());
        } else {
            String name = item.getDestName();
            if (name == null) {
                name = source.getName();
            }
            String out = path + name;
            jar.addAsResource(source, out);
        }
    }

    private static void embedFileSet(Log log, MavenProject project, FileSet fs, JavaArchive jar) {
        File directory = new File(fs.getDirectory());
        if (!directory.isAbsolute()) {
            directory = new File(project.getBasedir(), fs.getDirectory());
        }

        if (!directory.isDirectory()) {
            log.warn("File set root directory (" + directory.getAbsolutePath() + ") does not exist " +
                "- skipping");
            return;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(directory);


        if (fs.getOutputDirectory() == null) {
            fs.setOutputDirectory("/");
        }
        if (!fs.getOutputDirectory().startsWith("/")) {
            fs.setOutputDirectory("/" + fs.getOutputDirectory());
        }
        if (!fs.getOutputDirectory().endsWith("/")) {
            fs.setOutputDirectory(fs.getOutputDirectory() + "/");
        }

        List<String> excludes = fs.getExcludes();
        if (fs.isUseDefaultExcludes()) {
            excludes.addAll(FileUtils.getDefaultExcludesAsList());
        }
        if (!excludes.isEmpty()) {
            scanner.setExcludes(excludes.toArray(new String[0]));
        }
        if (!fs.getIncludes().isEmpty()) {
            scanner.setIncludes(fs.getIncludes().toArray(new String[0]));
        }
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        for (String path : files) {
            File file = new File(directory, path);
            log.debug("Adding " + fs.getOutputDirectory() + path + " to the archive");
            jar.addAsResource(file, fs.getOutputDirectory() + path);
        }
    }


    private boolean toExclude(DependencySet set, ArchivePath path) {
        String name = path.get();

        // Check whether the file is explicitly included
        if (isExplicitlyIncluded(set, name)) {
            return true;
        }

        if (set.getOptions().isUseDefaultExcludes()) {
            for (String pattern : DEFAULT_EXCLUDES) {
                if (SelectorUtils.match(pattern, name)) {
                    return true;
                }
            }
        }

        if (name.equalsIgnoreCase("/META-INF/MANIFEST.MF")) {
            return true;
        }

        if (name.equals("/module-info.class")){
            return true;
        }

        if (set.getOptions().getExcludes() != null) {
            for (String pattern : set.getOptions().getExcludes()) {
                if (SelectorUtils.match(pattern, name)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isExplicitlyIncluded(DependencySet set, String name) {
        List<String> includes = set.getOptions().getIncludes();
        if (includes != null && !includes.isEmpty()) {
            boolean included = false;

            // Check for each include pattern whether or not the path is explicitly included
            for (String pattern : includes) {
                if (SelectorUtils.match(pattern, name)) {
                    included = true;
                }
            }

            // If the path is not included, exclude the file
            // otherwise apply the excludes pattern on it.
            return !included;
        }
        return false;
    }

    /**
     * Import from file and make sure the file is closed.
     *
     * @param log  the logger
     * @param set  the dependency set
     * @param jar  the archive
     * @param file the file, must not be {@code null}
     */
    private void embedDependency(Log log, DependencySet set, JavaArchive jar, File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            jar.as(ZipImporter.class).importFrom(fis, path -> {
                if (jar.contains(path)) {
                    log.debug(path.get() + " already embedded in the jar");
                    return false;
                }
                if (!toExclude(set, path)) {
                    return true;
                } else {
                    log.debug("Excluding " + path.get() + " from " + file.getName());
                    return false;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Unable to read the file " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Generate the manifest for the über jar.
     */
    private static void generateManifest(JavaArchive jar, Map<String, String> entries) throws IOException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        if (entries != null) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                attributes.put(new Attributes.Name(entry.getKey()), entry.getValue());
            }
        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        manifest.write(bout);
        bout.close();
        byte[] bytes = bout.toByteArray();
        //TODO: merge existing manifest with current one
        jar.setManifest(new ByteArrayAsset(bytes));

    }
}
