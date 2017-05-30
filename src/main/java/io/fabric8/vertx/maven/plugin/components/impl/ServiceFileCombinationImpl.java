package io.fabric8.vertx.maven.plugin.components.impl;

import io.fabric8.vertx.maven.plugin.components.ServiceFileCombinationConfig;
import io.fabric8.vertx.maven.plugin.components.ServiceFileCombiner;
import io.fabric8.vertx.maven.plugin.components.ServiceUtils;
import io.fabric8.vertx.maven.plugin.model.CombinationStrategy;
import io.fabric8.vertx.maven.plugin.mojos.DependencySet;
import org.apache.commons.io.IOUtils;
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
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * This component is used to perform Services relocation - typically moving came Service Providers found in
 * META-INF/services to a single file
 * Right now it supports only combine - wherein all same service providers are combined into on file with one
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

        if (config.getStrategy() == CombinationStrategy.none) {
            return;
        }

        List<String> patterns = config.getArchive().getDescriptorCombinationPatterns();
        if (patterns.isEmpty()) {
            return;
        }

        Log logger = Objects.requireNonNull(config.getMojo().getLog());

        // TODO this should reuse the packaging configuration
        DependencySet set = new DependencySet();
        set.addInclude("*");
        ScopeFilter scopeFilter = ServiceUtils.newScopeFilter("runtime");
        ArtifactFilter filter = new ArtifactIncludeFilterTransformer().transform(scopeFilter);

        try {
            Set<Artifact> artifacts = ServiceUtils.filterArtifacts(config.getArtifacts(), set.getIncludes(),
                Collections.emptyList(), true, logger, filter);

            List<File> files = artifacts.stream().map(Artifact::getFile)
                .filter(File::isFile)
                .filter(f -> f.getName().endsWith(".jar"))
                .collect(Collectors.toList());

            combine(config.getProject(), patterns, logger, files);
        } catch (Exception e) {
            throw new RuntimeException("Unable to combine SPI files for " + config.getProject().getArtifactId(), e);
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
        Map<String, List<String>> locals = findLocalDescriptors(project, patterns);
        Map<String, List<List<String>>> deps = findDescriptorsFromDependencies(dependencies, patterns);

        // Keys are path relative to the archive root.
        if (logger.isDebugEnabled()) {
            logger.debug("Descriptors declared in the project: " + locals.keySet());
            logger.debug("Descriptors declared in dependencies: " + deps.keySet());
        }

        Set<String> descriptorsToMerge = new LinkedHashSet<>(locals.keySet());
        descriptorsToMerge.addAll(deps.keySet());

        Map<String, List<String>> descriptors = new HashMap<>();
        for (String spi : descriptorsToMerge) {
            descriptors.put(spi, merge(project, spi, locals.get(spi), deps.get(spi)));
        }

        // Write the new files in target/classes
        File out = new File(project.getBuild().getOutputDirectory());

        descriptors.forEach((name, content) -> {
            File merged = new File(out, name);
            try {
                org.apache.commons.io.FileUtils.writeLines(merged, content);
                logger.debug("Descriptor combined into " + merged.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("Cannot write combined Descriptor files", e);
            }
        });
    }

    private List<String> merge(MavenProject project, String name, List<String> local, List<List<String>> deps) {
        if (name.equals("org.codehaus.groovy.runtime.ExtensionModule")) {
            return GroovyExtensionCombiner.merge(project.getArtifactId(), project.getVersion(), local, deps);
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

    private Map<String, List<String>> findLocalDescriptors(MavenProject project, List<String> patterns) {
        Map<String, List<String>> map = new LinkedHashMap<>();

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
                try {
                    // Compute the descriptor path in the archive - linux style.
                    String relative = classes.toURI().relativize(file.toURI()).getPath().replace("\\", "/");
                    map.put("/" + relative, org.apache.commons.io.FileUtils.readLines(file, "UTF-8"));
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read " + file.getAbsolutePath(), e);
                }

            }
        }
        return map;
    }

    private Map<String, List<List<String>>> findDescriptorsFromDependencies(List<File> deps, List<String> patterns) {
        Map<String, List<List<String>>> map = new LinkedHashMap<>();

        for (File file : deps) {
            JavaArchive archive = ShrinkWrap.createFromZipFile(JavaArchive.class, file);
            Map<ArchivePath, Node> content = archive.getContent(path -> {
                for (String pattern : patterns) {
                    if (SelectorUtils.match(pattern, path.get())) {
                        return true;
                    }
                }
                return false;
            });

            for (Map.Entry<ArchivePath, Node> entry : content.entrySet()) {
                Asset asset = entry.getValue().getAsset();
                if (asset != null) {
                    List<String> lines;
                    InputStream input = null;
                    String path = entry.getKey().get();
                    try {
                        input = asset.openStream();
                        lines = IOUtils.readLines(input, "UTF-8");
                    } catch (IOException e) {
                        throw new RuntimeException("Cannot read " + path, e);
                    } finally {
                        closeQuietly(input);
                    }

                    List<List<String>> items = map.get(path);
                    if (items == null) {
                        items = new ArrayList<>();
                    }
                    items.add(lines);
                    map.put(path, items);
                }
            }
        }
        return map;
    }
}
