/*
 *
 *   Copyright (c) 2016-2017 Red Hat, Inc.
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

package io.reactiverse.vertx.maven.plugin.utils;

import io.reactiverse.vertx.maven.plugin.mojos.AbstractVertxMojo;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class WebJars {

    /**
     * A regex extracting the library name and version from Zip Entry names.
     */
    private static final Pattern WEBJAR_REGEX = Pattern.compile(".*META-INF/resources/webjars/([^/]+)/([^/]+)/.*");
    /**
     * The directory within jar file where webjar resources are located.
     */
    private static final String WEBJAR_LOCATION = "META-INF/resources/webjars/";

    /**
     * A regex to extract the different part of the path of a file from a library included in a webjar.
     */
    private static final Pattern WEBJAR_INTERNAL_PATH_REGEX = Pattern.compile("([^/]+)/([^/]+)/(.*)");


    /**
     * Checks whether the given file is a WebJar or not (http://www.webjars.org/documentation).
     * The check is based on the presence of {@literal META-INF/resources/webjars/} directory in the jar file.
     *
     * @param file the file.
     * @return {@literal true} if it's a bundle, {@literal false} otherwise.
     */
    public static boolean isWebJar(Log log, File file) {
        if (file == null) {
            return false;
        }
        Set<String> found = new LinkedHashSet<>();
        if (file.isFile() && file.getName().endsWith(".jar")) {
            try (JarFile jar = new JarFile(file)) {

                // Fast return if the base structure is not there
                if (jar.getEntry(WEBJAR_LOCATION) == null) {
                    return false;
                }

                search(found, jar);
            } catch (IOException e) {
                log.error("Cannot check if the file " + file.getName()
                    + " is a webjar, cannot open it", e);
                return false;
            }

            for (String lib : found) {
                log.info("Web Library found in " + file.getName() + " : " + lib);
            }

            return !found.isEmpty();
        }

        return false;
    }

    private static void search(Set<String> found, JarFile jar) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            Matcher matcher = WEBJAR_REGEX.matcher(entry.getName());
            if (matcher.matches()) {
                found.add(matcher.group(1) + "-" + matcher.group(2));
            }
        }
    }

    public static void extract(final AbstractVertxMojo mojo, File in, File out, boolean stripVersion) throws IOException {
        try (ZipFile file = new ZipFile(in)) {
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                unzipWebJarFile(mojo, out, stripVersion, file, entries);
            }
        }
    }

    private static void unzipWebJarFile(AbstractVertxMojo mojo, File out, boolean stripVersion, ZipFile file, Enumeration<? extends ZipEntry> entries) throws IOException {
        ZipEntry entry = entries.nextElement();
        if (entry.getName().startsWith(WEBJAR_LOCATION) && !entry.isDirectory()) {
            // Compute destination.
            File output = getOutput(mojo.getLog(), out, stripVersion,
                entry.getName().substring(WEBJAR_LOCATION.length()));
            try (InputStream stream = file.getInputStream(entry)) {
                boolean created = output.getParentFile().mkdirs();
                mojo.getLog().debug(out.getParentFile().getAbsolutePath() + " created? " + created);
                copyInputStreamToFile(stream, output);
            } catch (IOException e) {
                mojo.getLog().error("Cannot unpack " + entry.getName() + " from " + file.getName(), e);
                throw e;
            }
        }
    }

    private static File getOutput(Log log, File out, boolean stripVersion, String path) {
        if (stripVersion) {
            Matcher matcher = WEBJAR_INTERNAL_PATH_REGEX.matcher(path);
            if (matcher.matches()) {
                return new File(out, matcher.group(1) + "/" + matcher.group(3));
            } else {
                log.warn(path + " does not match the regex - did not strip the version for this file");
            }
        }
        return new File(out, path);
    }
}
