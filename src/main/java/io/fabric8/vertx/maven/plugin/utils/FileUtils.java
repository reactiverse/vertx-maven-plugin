package io.fabric8.vertx.maven.plugin.utils;

import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A bunch of file utility that will be used by the plugin to perform common file operations
 *
 * @author kameshs
 */
public class FileUtils {

    /**
     * Get a list of {@link Path} typically compileSourceRoots and resources path of the {@link MavenProject}
     *
     * @param project  - the project for which the compile source roots and resource roots needs to be retrieved
     * @param includes - an optional {@link List<String>} for scanning patterns under the directory
     * @return - {@link Set<Path>}
     */
    public static Set<Path> includedDirs(MavenProject project, Optional<List<String>> includes) {

        Set<Path> inclDirs = Stream.concat(project.getCompileSourceRoots().stream(),
                project.getResources().stream()
                        .map(resource -> resource.getDirectory()))
                .map(Paths::get)
                .filter(p -> Files.exists(p) && Files.isDirectory(p))
                .collect(Collectors.toSet());

        return inclDirs;
    }

    /**
     * Utility method to perform move of {@link File} source under target directory, the default backup file name
     * will be  source base name with &quot;.original.jar&quot; appended to it
     *
     * @param source    - the {@link File} that needs to be backed up
     * @param backupDir - the {@link File} the destination directory where the file will be backed up
     * @return the {@link Path} of the backup file
     * @throws IOException - the exception that might occur while backing up
     */
    public static Path backup(File source, File backupDir) throws IOException {

        Path sourceFilePath = Paths.get(source.toURI());
        String targetName = org.codehaus.plexus.util.FileUtils.basename(source.toString()) + "original.jar";

        Path backupFilePath = Paths.get(backupDir.toURI().resolve(targetName));

        if (backupDir.exists() && backupDir.isDirectory()) {

            Files.move(sourceFilePath, Paths.get(backupDir.toString(), targetName)
                    , StandardCopyOption.ATOMIC_MOVE);
        }

        return backupFilePath;
    }

    /**
     * Utility method to perform copy of {@link File} source under target directory, the default backup file name
     * will be  source base name with &quot;.original.jar&quot; appended to it
     *
     * @param source  - the {@link File} that needs to be backed up
     * @param destDir - the {@link File} the destination directory where the file will be copied to
     * @return the {@link Path} of the backup file
     * @throws IOException - the exception that might occur while backing up
     */
    public static Path copy(File source, File destDir) throws IOException {

        Path sourceFilePath = Paths.get(source.toURI());
        String targetName = org.codehaus.plexus.util.FileUtils.basename(source.toString()) + "original.jar";

        Path backupFilePath = Paths.get(destDir.toURI().resolve(targetName));

        if (destDir.exists() && destDir.isDirectory()) {

            Files.copy(sourceFilePath, Paths.get(destDir.toString(), targetName)
                    , StandardCopyOption.REPLACE_EXISTING);
        }

        return backupFilePath;
    }

    /**
     * A small utility method to read lines from the {@link InputStream}
     *
     * @param input - the input stream from which the lines needs to be read
     * @return the {@link String} with lines of the file concatenated
     * @throws IOException - any exception while reading the file
     */
    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}
