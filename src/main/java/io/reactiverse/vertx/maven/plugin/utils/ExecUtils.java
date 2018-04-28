package io.reactiverse.vertx.maven.plugin.utils;

import java.io.File;

/**
 * Some helper methods related to program execution.
 */
public class ExecUtils {

    /**
     * The set of extensions (including the empty extension) to append to the searched executable name.
     */
    private static final String[] EXECUTABLE_EXTENSIONS = new String[]{
        // The command itself (no extension)
        "",
        // Linux and Unix (scripts)
        ".sh", ".bash",
        // Windows
        ".exe", ".bat", ".cmd"
    };

    /**
     * Tries to find an executable with the given name. It first checks in of the given directories (if any) and
     * then tries in the system's path. The lookup tries several extensions (.sh, .bash, .exe. .cmd. and .bat).
     *
     * @param executable the name of the program to find, generally without the extension
     * @param dirs       optional set of directories in which the program need to be searched before checking the system's
     *                   path.
     * @return the executable's file, {@code null} if not found
     */
    public static File find(String executable, File... dirs) {
        // First try using the hints.
        if (dirs != null) {
            for (File hint : dirs) {
                File file = findExecutableInDirectory(executable, hint);
                if (file != null) {
                    return file;
                }
            }
        }

        // Not found, try to use the system path.
        return findExecutableInSystemPath(executable);
    }

    /**
     * Tries to find the given executable (specified by its name) in the given directory. It checks for a file having
     * one of the extensions contained in {@link #EXECUTABLE_EXTENSIONS}, and checks that this file is executable.
     *
     * @param executable the name of the program to find, generally without the extension
     * @param directory  the directory in which the program is searched.
     * @return the file of the program to be searched for if found. {@code null} otherwise. If the given directory is
     * {@code null} or not a real directory, it also returns {@code null}.
     */
    private static File findExecutableInDirectory(String executable, File directory) {
        if (directory == null || !directory.isDirectory()) {
            // if the directory is null or not a directory => returning null.
            return null;
        }
        for (String extension : EXECUTABLE_EXTENSIONS) {
            File file = new File(directory, executable + extension);
            if (file.isFile() && file.canExecute()) {
                return file;
            }
        }
        // Not found.
        return null;
    }

    /**
     * Tries to find the given executable (specified by its name) in the system's path. It checks for a file having
     * one of the extensions contained in {@link #EXECUTABLE_EXTENSIONS}, and checks that this file is executable.
     *
     * @param executable the name of the program to find, generally without the extension
     * @return the file of the program to be searched for if found. {@code null} otherwise.
     */
    public static File findExecutableInSystemPath(String executable) {
        String systemPath = System.getenv("PATH");

        // Fast failure if we don't have the PATH defined.
        if (systemPath == null) {
            return null;
        }

        String[] pathDirs = systemPath.split(File.pathSeparator);

        for (String pathDir : pathDirs) {
            File dir = new File(pathDir);
            if (dir.isDirectory()) {
                File file = findExecutableInDirectory(executable, dir);
                if (file != null) {
                    return file;
                }
            }
        }
        // :-(
        return null;
    }
}
