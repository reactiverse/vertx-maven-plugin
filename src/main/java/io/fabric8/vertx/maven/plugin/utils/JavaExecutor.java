/*
 *   Copyright 2016 Kamesh Sampath
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.fabric8.vertx.maven.plugin.utils;

import org.apache.maven.plugin.MojoExecutionException;
import io.fabric8.vertx.maven.plugin.functions.Executor;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author kameshs
 */
public abstract class JavaExecutor implements Executor<Optional<Process>> {


    Collection<URL> classPathUrls = Collections.emptyList();

    Path javaPath;

    public JavaExecutor() {
        this.javaPath = findJava();
    }

    /**
     * This add or build the classpath that will be passed to the forked process JVM i.e &quot;-cp&quot;
     *
     * @param argsList - the forked process argument list to which the classpath will be appended
     * @throws MojoExecutionException - any error that might occur while building or adding classpath
     */
    protected void addClasspath(List<String> argsList) throws MojoExecutionException {
        try {

            StringBuilder classpath = new StringBuilder();

            for (URL ele : this.classPathUrls) {
                classpath = classpath
                    .append(classpath.length() > 0 ? File.pathSeparator : "")
                    .append(new File(ele.toURI()));
            }

            String oldClasspath = System.getProperty("java.class.path");

            if (oldClasspath != null) {
                classpath.append(File.pathSeparator);
                classpath.append(oldClasspath);
            }

            argsList.add(0, "-cp");
            argsList.add(1, classpath.toString());

        } catch (Exception ex) {
            throw new MojoExecutionException("Could not build classpath", ex);
        }
    }

    /**
     * An utility to find the Java Executable from the host
     *
     * @return - the {@link Path} representing the Java executable path
     */
    private Path findJava() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new RuntimeException("unable to locate java binary");
        }

        Path binDir = FileSystems.getDefault().getPath(javaHome, "bin");

        Path java = binDir.resolve("java.exe");
        if (java.toFile().exists()) {
            return java;
        }

        java = binDir.resolve("java");
        if (java.toFile().exists()) {
            return java;
        }

        throw new RuntimeException("unable to locate java binary");
    }
}
