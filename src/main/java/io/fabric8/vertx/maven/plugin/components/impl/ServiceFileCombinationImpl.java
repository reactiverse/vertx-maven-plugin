package io.fabric8.vertx.maven.plugin.components.impl;

import io.fabric8.vertx.maven.plugin.components.ServiceUtils;
import io.fabric8.vertx.maven.plugin.components.ServiceFileCombinationConfig;
import io.fabric8.vertx.maven.plugin.components.ServiceFileCombiner;
import io.fabric8.vertx.maven.plugin.model.CombinationStrategy;
import io.fabric8.vertx.maven.plugin.mojos.DependencySet;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.resolve.ScopeFilter;
import org.apache.maven.shared.artifact.filter.resolve.transform.ArtifactIncludeFilterTransformer;
import org.codehaus.plexus.component.annotations.Component;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

            combine(config.getProject(), logger, files);
        } catch (Exception e) {
            throw new RuntimeException("Unable to combine SPI files for " + config.getProject().getArtifactId(), e);
        }

    }


    /**
     * The method to perform the service provider combining
     *
     * @param dependencies - the list of jars which needs to scanned for service provider entries
     */
    private void combine(MavenProject project, Log logger, List<File> dependencies) {
        Map<String, Set<String>> locals = findLocalSPI(project);
        Map<String, List<Set<String>>> deps = findSPIsFromDependencies(dependencies);

        if (logger.isDebugEnabled()) {
            logger.debug("SPI declared in the project: " + locals.keySet());
            logger.debug("SPI declared in dependencies: " + deps.keySet());
        }

        Set<String> spisToMerge = new LinkedHashSet<>(locals.keySet());
        spisToMerge.addAll(deps.keySet());

        Map<String, List<String>> spis = new HashMap<>();
        for (String spi : spisToMerge) {
            spis.put(spi, merge(project, spi, locals.get(spi), deps.get(spi)));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("SPI:" + spis.keySet());
        }

        // Write the new files in target/classes
        File out = new File(project.getBuild().getOutputDirectory(), "META-INF/services");

        spis.forEach((name, content) -> {
            File merged = new File(out, name);
            try {
                org.apache.commons.io.FileUtils.writeLines(merged, content);
                logger.debug("SPI file combined into " + merged.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("Cannot write combined SPI files", e);
            }
        });
    }

    private List<String> merge(MavenProject project, String name, Set<String> local, List<Set<String>> deps) {
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


    private Map<String, List<Set<String>>> findSPIsFromDependencies(List<File> deps) {
        Map<String, List<Set<String>>> map = new LinkedHashMap<>();

        ArchivePath spiPath = ArchivePaths.create("META-INF/services");

        Set<JavaArchive> jars = deps.stream()
            .map(f -> ShrinkWrap.createFromZipFile(JavaArchive.class, f))
            .filter(a -> a.contains(spiPath))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        for (JavaArchive jar : jars) {
            Node node = jar.get(spiPath);
            Set<Node> children = node.getChildren();
            for (Node child : children) {
                String name = child.getPath().get().substring(spiPath.get().length() + 1);
                try {
                    Asset asset = child.getAsset();
                    if (asset != null) {
                        List<String> lines = IOUtils.readLines(asset.openStream(), "UTF-8");
                        List<Set<String>> items = map.get(name);
                        if (items == null) {
                            items = new ArrayList<>();
                        }
                        items.add(new LinkedHashSet<>(lines));
                        map.put(name, items);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read  " + node.getPath().get(), e);
                }
            }
        }

        return map;
    }

    private Map<String, Set<String>> findLocalSPI(MavenProject project) {
        Map<String, Set<String>> map = new LinkedHashMap<>();

        File classes = new File(project.getBuild().getOutputDirectory());
        if (!classes.isDirectory()) {
            return map;
        }

        File spiRoot = new File(classes, "META-INF/services");
        if (!spiRoot.isDirectory()) {
            return map;
        }

        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(spiRoot, null, false);
        for (File file : files) {
            try {
                map.put(file.getName(), new LinkedHashSet<>(org.apache.commons.io.FileUtils.readLines(file, "UTF-8")));
            } catch (IOException e) {
                throw new RuntimeException("Cannot read  " + file.getAbsolutePath(), e);
            }
        }
        return map;
    }
}
