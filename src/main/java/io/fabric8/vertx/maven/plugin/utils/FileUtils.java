package io.fabric8.vertx.maven.plugin.utils;

import org.apache.maven.project.MavenProject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kameshs
 */
public class FileUtils {

    public static Set<Path> includedDirs(MavenProject project, Optional<List<String>> includes) {

        Set<Path> inclDirs = Stream.concat(project.getCompileSourceRoots().stream(),
                project.getResources().stream()
                        .map(resource -> resource.getDirectory()))
                .map(Paths::get)
                .filter(p -> Files.exists(p) && Files.isDirectory(p))
                .collect(Collectors.toSet());

        return inclDirs;
    }

}
