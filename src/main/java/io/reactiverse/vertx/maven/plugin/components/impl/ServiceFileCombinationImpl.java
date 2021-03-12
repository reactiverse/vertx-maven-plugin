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

import io.reactiverse.vertx.maven.plugin.components.ServiceFileCombinationConfig;
import io.reactiverse.vertx.maven.plugin.components.ServiceFileCombiner;
import io.reactiverse.vertx.maven.plugin.components.ServiceUtils;
import io.reactiverse.vertx.maven.plugin.components.impl.merge.MergeResult;
import io.reactiverse.vertx.maven.plugin.components.impl.merge.MergingStrategy;
import io.reactiverse.vertx.maven.plugin.model.CombinationStrategy;
import io.reactiverse.vertx.maven.plugin.mojos.DependencySet;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.resolve.ScopeFilter;
import org.apache.maven.shared.artifact.filter.resolve.transform.ArtifactIncludeFilterTransformer;
import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.apache.maven.shared.utils.io.SelectorUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This component is used to perform Services relocation - typically moving came Service Providers found in
 * META-INF/services to a single file
 * Right now it supports only COMBINE - wherein all same service providers are combined into on file with one
 * line entry for each Service Provider implementation
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@Component(
    role = ServiceFileCombiner.class
)
public class ServiceFileCombinationImpl implements ServiceFileCombiner {

    @Override
    public void doCombine(ServiceFileCombinationConfig config) {

        if (config.getStrategy() == CombinationStrategy.NONE) {
            return;
        }

        List<String> patterns = config.getArchive().getFileCombinationPatterns();
        if (patterns.isEmpty()) {
            return; // Should not happen, by default contains spring and services.
        }

        Log logger = Objects.requireNonNull(config.getMojo().getLog());

        List<DependencySet> sets = config.getArchive().getDependencySets();
        if (sets.isEmpty()) {
            DependencySet set = new DependencySet();
            set.addInclude("*");
            sets.add(set);
        }

        for (DependencySet ds : sets) {
            ScopeFilter scopeFilter = ServiceUtils.newScopeFilter(ds.getScope());
            ArtifactFilter filter = new ArtifactIncludeFilterTransformer().transform(scopeFilter);
            Set<Artifact> artifacts = ServiceUtils.filterArtifacts(config.getArtifacts(),
                ds.getIncludes(), ds.getExcludes(),
                ds.isUseTransitiveDependencies(), logger, filter);
            try {
                List<File> files = artifacts.stream().map(Artifact::getFile)
                    .filter(File::isFile)
                    .filter(f -> f.getName().endsWith(".jar"))
                    .collect(Collectors.toList());

                combine(config.getProject(), patterns, logger, files);
            } catch (Exception e) {
                throw new RuntimeException("Unable to combine SPI files for " + config.getProject().getArtifactId(), e);
            }
        }
    }

    /**
     * The method to perform the service provider combining
     *
     * @param project      the Maven project
     * @param patterns     the set of patterns
     * @param logger       the logger
     * @param dependencies the dependencies
     */
    private void combine(MavenProject project, List<String> patterns, Log logger, List<File> dependencies) {
        Map<String, Asset> locals = findLocalDescriptors(project, patterns);
        Map<String, List<Asset>> deps = findDescriptorsFromDependencies(dependencies, patterns);

        // Keys are path relative to the archive root.
        logger.debug("Descriptors declared in the project: " + locals.keySet());
        logger.debug("Descriptors declared in dependencies: " + deps.keySet());

        Set<String> descriptorsToMerge = new LinkedHashSet<>(locals.keySet());
        descriptorsToMerge.addAll(deps.keySet());

        Map<String, MergeResult> descriptors = new HashMap<>();
        for (String name : descriptorsToMerge) {
            MergingStrategy strategy = MergingStrategy.forName(name);
            descriptors.put(name, strategy.merge(project, locals.get(name), deps.get(name)));
        }

        // Write the new files in target/classes
        File out = new File(project.getBuild().getOutputDirectory());

        descriptors.forEach((name, content) -> {
            File merged = new File(out, name);
            try {
                content.writeTo(merged);
                logger.debug("Descriptor combined into " + merged.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("Cannot write combined Descriptor files", e);
            }
        });
    }

    private static Map<String, Asset> findLocalDescriptors(MavenProject project, List<String> patterns) {
        Map<String, Asset> map = new LinkedHashMap<>();

        File classes = new File(project.getBuild().getOutputDirectory());
        if (!classes.isDirectory()) {
            return map;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(classes);
        scanner.setIncludes(patterns.toArray(new String[0]));
        scanner.scan();

        String[] paths = scanner.getIncludedFiles();
        for (String p : paths) {
            File file = new File(classes, p);
            if (file.isFile()) {
                // Compute the descriptor path in the archive - linux style.
                String relative = classes.toURI().relativize(file.toURI()).getPath().replace("\\", "/");
                map.put("/" + relative, new FileAsset(file));
            }
        }
        return map;
    }

    private static Map<String, List<Asset>> findDescriptorsFromDependencies(List<File> deps, List<String> patterns) {
        Map<String, List<Asset>> map = new LinkedHashMap<>();

        for (File file : deps) {
            JavaArchive archive = ShrinkWrap.createFromZipFile(JavaArchive.class, file);
            Map<ArchivePath, Node> content = getMatchingFilesFromJar(patterns, archive);

            for (Map.Entry<ArchivePath, Node> entry : content.entrySet()) {
                Asset asset = entry.getValue().getAsset();
                if (asset != null) {
                    String path = entry.getKey().get();
                    List<Asset> items = map.computeIfAbsent(path, k -> new ArrayList<>());
                    items.add(asset);
                    map.put(path, items);
                }
            }
        }
        return map;
    }

    private static Map<ArchivePath, Node> getMatchingFilesFromJar(List<String> patterns, JavaArchive archive) {
        return archive.getContent(path -> {
                    for (String pattern : patterns) {
                        if (SelectorUtils.match(pattern, path.get())) {
                            return true;
                        }
                    }
                    return false;
                });
    }
}
