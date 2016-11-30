package io.fabric8.vertx.maven.plugin.utils;

import org.codehaus.plexus.util.CollectionUtils;
import org.jboss.shrinkwrap.api.*;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kameshs
 */
public class ServiceCombinerUtil {

    /**
     * @param jars
     * @return
     * @throws Exception
     */
    public static JavaArchive combine(List<JavaArchive> jars,
                                      Optional<String> spiServiceInterface) throws Exception {

        ArchivePath spiPath;

        if (spiServiceInterface.isPresent()) {
            spiPath = ArchivePaths.create(spiServiceInterface.get());
        } else {
            spiPath = ArchivePaths.create("META-INF/services");
        }

        Set<JavaArchive> serviceProviderArchives = jars.stream()
                .filter(a -> a.contains(spiPath))
                .collect(Collectors.toSet());

        Iterator<JavaArchive> archiveIterator = serviceProviderArchives.iterator();

        JavaArchive prev = null;

        Map<Node, Set<JavaArchive>> sameNodeArchives = new HashMap<>();


        while (archiveIterator.hasNext()) {

            JavaArchive javaArchive = archiveIterator.next();

            Set<Node> spiDefNodes = javaArchive.get(spiPath).getChildren();

            if (prev != null) {
                Set<Node> prevSpiDefNodes = prev.get(spiPath).getChildren();
                Collection<Node> intersection = CollectionUtils.intersection(spiDefNodes, prevSpiDefNodes);

                if (!intersection.isEmpty()) {

                    for (Node node : intersection) {
                        if (sameNodeArchives.containsKey(node)) {
                            sameNodeArchives.get(node).add(javaArchive);
                        } else {
                            sameNodeArchives.put(node, Stream.of(prev, javaArchive).collect(Collectors.toSet()));
                        }
                    }
                }
            }
            prev = javaArchive;
        }

        JavaArchive combinedSPIArchive = ShrinkWrap.create(JavaArchive.class);

        Map<Node, Set<String>> spiContent = new HashMap<>();

        sameNodeArchives.forEach((node, javaArchives) -> {

            Set<String> spiCombinedStrings = new HashSet<>();

            javaArchives.forEach(javaArchive -> {
                InputStream in = null;
                try {
                    Filter<ArchivePath> spiFilterPath = Filters.include(node.getPath().get());
                    Map<ArchivePath, Node> nodeMap = javaArchive.getContent(spiFilterPath);
                    Optional<Node> optNode = nodeMap.values().stream().findFirst();
                    if (optNode.isPresent()) {
                        Node archiveNode = optNode.get();
                        in = archiveNode.getAsset().openStream();
                        spiCombinedStrings.add(read(in));
                        in.close();
                    }
                } catch (IOException e) {
                    //ignore
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            //nothing i can do here :)
                        }
                    }
                }
            });
            spiContent.put(node, spiCombinedStrings);
        });

        spiContent.forEach((spiNode, content) -> {
            String strSpiPath = spiNode.toString();
            String spi = spiNode.toString().substring(strSpiPath.lastIndexOf("/") + 1, strSpiPath.length());
            combinedSPIArchive.addAsServiceProvider(spi, content.stream().toArray(size -> new String[size]));
        });

        return combinedSPIArchive;
    }

    private static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}
