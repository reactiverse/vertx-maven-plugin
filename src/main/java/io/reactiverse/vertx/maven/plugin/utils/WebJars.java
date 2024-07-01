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

package io.reactiverse.vertx.maven.plugin.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
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
     * The directory within jar file where webjar resources are located.
     */
    private static final String WEBJAR_LOCATION = "META-INF/resources/webjars/";

    /**
     * A regex to extract the different part of the path of a file from a library included in a webjar.
     */
    private static final Pattern WEBJAR_INTERNAL_PATH_REGEX = Pattern.compile("([^/]+)/([^/]+)/(.*)");


    /**
     * Checks whether the given file is a <a href="http://www.webjars.org/documentation">WebJar</a>.
     * The check is based on the presence of {@literal META-INF/resources/webjars/} directory in the jar file.
     *
     * @param file the file.
     * @return {@literal true} if it's a bundle, {@literal false} otherwise.
     */
    public static boolean isWebJar(File file) throws IOException {
        if (file != null && file.isFile() && file.getName().endsWith(".jar")) {
            try (JarFile jar = new JarFile(file)) {
                if (jar.getJarEntry(WEBJAR_LOCATION) == null) {
                    return false;
                }
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.isDirectory() && entry.getName().startsWith(WEBJAR_LOCATION)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void extract(File in, File out, boolean stripVersion) throws IOException {
        try (ZipFile file = new ZipFile(in)) {
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                unzipWebJarFile(out, stripVersion, file, entries);
            }
        }
    }

    private static void unzipWebJarFile(File out, boolean stripVersion, ZipFile file, Enumeration<? extends ZipEntry> entries) throws IOException {
        ZipEntry entry = entries.nextElement();
        if (entry.getName().startsWith(WEBJAR_LOCATION) && !entry.isDirectory()) {
            File output = getOutput(out, stripVersion, entry.getName().substring(WEBJAR_LOCATION.length()));
            try (InputStream stream = file.getInputStream(entry)) {
                output.getParentFile().mkdirs();
                copyInputStreamToFile(stream, output);
            }
        }
    }

    private static File getOutput(File out, boolean stripVersion, String path) {
        if (stripVersion) {
            Matcher matcher = WEBJAR_INTERNAL_PATH_REGEX.matcher(path);
            if (matcher.matches()) {
                return new File(out, matcher.group(1) + "/" + matcher.group(3));
            }
        }
        return new File(out, path);
    }
}
