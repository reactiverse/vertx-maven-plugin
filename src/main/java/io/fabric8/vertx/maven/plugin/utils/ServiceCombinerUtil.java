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

package io.fabric8.vertx.maven.plugin.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This utility is used to perform Services relocation - typically moving came Service Providers found in META-INF/services
 * to a single file
 * Right now it supports only combine - wherein all same service providers are combined into on file with one line entry
 * for each Service Provider implementation
 *
 * @author kameshs
 */
public class ServiceCombinerUtil {

    private Log logger = new SystemStreamLog();

    private String projectName = "no-name";
    private String projectVersion = "0.0";
    private File classes;

    public ServiceCombinerUtil withLog(Log logger) {
        this.logger = logger;
        return this;
    }

    public ServiceCombinerUtil withProject(String name, String version) {
        this.projectName = name;
        this.projectVersion = version;
        return this;
    }

    public ServiceCombinerUtil withClassesDirectory(File dir) {
        this.classes = dir;
        return this;
    }

    /**
     * The method to perform the service provider combining
     *
     * @param jars             - the list of jars which needs to scanned for service provider entries
     * @return - {@link JavaArchive} which has the same service provider entries combined
     */
    public JavaArchive combine(List<JavaArchive> jars) {
        Map<String, Set<String>> locals = findLocalSPI();
        Map<String, List<Set<String>>> deps = findSPIsFromDependencies(jars);

        if (logger.isDebugEnabled()) {
            logger.debug("SPI declared in the project: " + locals.keySet());
            logger.debug("SPI declared in dependencies: " + deps.keySet());
        }

        JavaArchive combinedSPIArchive = ShrinkWrap.create(JavaArchive.class);

        Set<String> spisToMerge = new HashSet<>(locals.keySet());
        spisToMerge.addAll(deps.keySet());

        Map<String, List<String>> spis = new HashMap<>();
        for (String spi : spisToMerge) {
            spis.put(spi, merge(spi, locals.get(spi), deps.get(spi)));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("SPI:" + spis.keySet());
        }

        spis.forEach((name, content) -> combinedSPIArchive.addAsServiceProvider(name, content.toArray(new String[]{})));

        return combinedSPIArchive;
    }

    private List<String> merge(String name, Set<String> local, List<Set<String>> deps) {
        if (name.equals("org.codehaus.groovy.runtime.ExtensionModule")) {
            return GroovyExtensionCombiner.merge(projectName, projectVersion, local, deps);
        } else {
            // Regular merge, concat things.
            // Start with deps
            Set<String> fromDeps = new LinkedHashSet<>();
            if (deps != null) {
                deps.forEach(fromDeps::addAll);
            }
            Set<String> lines = new LinkedHashSet<>();
            if (local != null) {
                if (local.isEmpty()) {
                    // Drop this SPI
                    return Collections.emptyList();
                }
                for (String line : local) {
                    if (line.trim().equalsIgnoreCase("${combine}")) {
                        //Copy the ones form the dependencies on this line
                        lines.addAll(fromDeps);
                    } else {
                        // Just copy the line
                        lines.add(line);
                    }
                }
                return new ArrayList<>(lines);
            } else {
                return new ArrayList<>(fromDeps);
            }
        }
    }


    private Map<String, List<Set<String>>> findSPIsFromDependencies(List<JavaArchive> jars) {
        Map<String, List<Set<String>>> map = new HashMap<>();

        ArchivePath spiPath = ArchivePaths.create("META-INF/services");

        Set<JavaArchive> serviceProviderArchives = jars.stream()
            .filter(a -> a.contains(spiPath))
            .collect(Collectors.toSet());

        for (JavaArchive archive : serviceProviderArchives) {
            Node node = archive.get(spiPath);
            Set<Node> children = node.getChildren();
            for (Node child : children) {
                String name = child.getPath().get().substring(spiPath.get().length() + 1);
                try {
                    List<String> lines = IOUtils.readLines(child.getAsset().openStream(), "UTF-8");
                    List<Set<String>> items = map.get(name);
                    if (items == null) {
                        items = new ArrayList<>();
                    }
                    items.add(new LinkedHashSet<>(lines));
                    map.put(name, items);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read  " + node.getPath().get(), e);
                }
            }
        }

        return map;
    }

    private Map<String, Set<String>> findLocalSPI() {
        Map<String, Set<String>> map = new HashMap<>();
        if (classes == null || !classes.isDirectory()) {
            return map;
        }

        File spiRoot = new File(classes, "META-INF/services");
        if (!spiRoot.isDirectory()) {
            return map;
        }

        Collection<File> files = FileUtils.listFiles(spiRoot, null, false);
        for (File file : files) {
            try {
                map.put(file.getName(), new LinkedHashSet<>(FileUtils.readLines(file, "UTF-8")));
            } catch (IOException e) {
                throw new RuntimeException("Cannot read  " + file.getAbsolutePath(), e);
            }
        }
        return map;
    }

}
