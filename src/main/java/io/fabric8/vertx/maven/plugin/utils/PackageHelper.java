/*
 *   Copyright 2016 Kamesh Sampath
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.fabric8.vertx.maven.plugin.utils;


import io.fabric8.vertx.maven.plugin.mojos.Constants;
import org.apache.maven.plugin.logging.Log;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class PackageHelper {

    private final JavaArchive archive;
    private final Attributes.Name MAIN_VERTICLE = new Attributes.Name("Main-Verticle");
    private String mainVerticle;
    private String mainClass;
    private Set<Optional<File>> compileAndRuntimeDeps;
    private Set<Optional<File>> transitiveDeps;
    private Log log;

    public PackageHelper(String mainClass, String mainVerticle) {
        this.archive = ShrinkWrap.create(JavaArchive.class);
        this.mainClass = mainClass;
        this.mainVerticle = mainVerticle;
    }


    public PackageHelper mainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public PackageHelper mainVerticle(String mainVerticle) {
        this.mainVerticle = mainVerticle;
        return this;
    }

    /**
     * @param baseName
     * @param dir
     * @param primaryArtifactFile
     * @return
     * @throws IOException
     */
    public File build(String baseName, Path dir, File primaryArtifactFile) throws IOException {
        build(primaryArtifactFile);
        return createFatJar(baseName, dir);
    }

    /**
     * @param primaryArtifactFile
     */
    private synchronized void build(File primaryArtifactFile) {
        this.archive.as(ZipImporter.class).importFrom(primaryArtifactFile);
        addDependencies();
        generateManifest();
    }

    /**
     *
     */
    protected void addDependencies() {

        compileAndRuntimeDeps.stream()
                .filter(dep -> dep.isPresent())
                .forEach(dep -> {
                    File f = dep.get();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding Dependency :" + f.toString());
                    }
                    this.archive.as(ZipImporter.class).importFrom(f);
                });

        transitiveDeps.stream()
                .filter(dep -> dep.isPresent())
                .forEach(dep -> {
                    File f = dep.get();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding Dependency :" + f.toString());
                    }
                    this.archive.as(ZipImporter.class).importFrom(f);
                });

    }

    /**
     *
     */
    protected void generateManifest() {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.MAIN_CLASS, Constants.IO_VERTX_CORE_LAUNCHER);
        //This is a typical situation when application is launched with custom launcher
        if (mainVerticle != null) {
            attributes.put(MAIN_VERTICLE, mainVerticle);
        }

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            manifest.write(bout);
            bout.close();
            byte[] bytes = bout.toByteArray();
            //TODO: merge existing manifest with current one
            this.archive.setManifest(new ByteArrayAsset(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param baseName
     * @param dir
     * @return
     */
    private synchronized File createFatJar(String baseName, Path dir) {

        File jarFile = null;

        try {

            jarFile = new File(dir.toFile(), baseName + "-fat.jar");

            if (!jarFile.getParentFile().exists() && !jarFile.getParentFile().mkdirs()) {
                log.error("Failed to create parent directories for :" + jarFile.getAbsolutePath());
            }

            ZipExporter zipExporter = this.archive.as(ZipExporter.class);

            try (FileOutputStream jarOut = new FileOutputStream(jarFile)) {
                zipExporter.exportTo(jarOut);
            }
        } catch (IOException e) {
            log.error("Error building fat jar ", e);
        }

        return jarFile;
    }

    /**
     * @param compileAndRuntimeDeps
     * @return
     */
    public PackageHelper compileAndRuntimeDeps(Set<Optional<File>> compileAndRuntimeDeps) {

        this.compileAndRuntimeDeps = compileAndRuntimeDeps;

        return this;
    }

    /**
     * @param transitiveDeps
     * @return
     */

    public PackageHelper transitiveDeps(Set<Optional<File>> transitiveDeps) {

        this.transitiveDeps = transitiveDeps;

        return this;
    }

    /**
     * @param log
     * @return
     */
    public PackageHelper log(Log log) {
        this.log = log;
        return this;
    }
}
