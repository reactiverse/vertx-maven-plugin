package io.fabric8.vertx.maven.plugin.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kameshs
 */
public class FileUtils {

    public static Set<Path> includedDirs(File baseDir, List<String> includes) {

        Set<Path> inclDirs = new HashSet<>();

        includes.forEach(includePattern -> {
            try {

                if (includePattern.startsWith("**")) {
                    Path rootPath = Paths.get(baseDir.toString());
                    if (Files.exists(rootPath)) {
                        File[] dirs = rootPath.toFile()
                                .listFiles((dir, name) -> dir.isDirectory());
                        Stream.of(dirs).forEach(f -> {
                            inclDirs.add(Paths.get(f.toString()));
                        });
                    }
                } else if (includePattern.contains("**")) {
                    String root = includePattern.substring(0, includePattern.indexOf("/**"));
                    Path rootPath = Paths.get(baseDir.toString(), root);
                    if (Files.exists(rootPath)) {
                        File[] dirs = rootPath.toFile()
                                .listFiles((dir, name) -> dir.isDirectory());
                        Stream.of(dirs).forEach(f -> {
                            inclDirs.add(Paths.get(f.toString()));
                        });
                    }
                } else {

                    List<Path> dirs = org.codehaus.plexus.util.FileUtils.getFileAndDirectoryNames(baseDir,
                            includePattern, null, true, true, true, true)
                            .stream().map(org.codehaus.plexus.util.FileUtils::dirname).map(Paths::get)
                            .filter(p -> Files.exists(p) && Files.isDirectory(p))
                            .collect(Collectors.toList());

                    inclDirs.addAll(dirs);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return inclDirs;

    }

}
