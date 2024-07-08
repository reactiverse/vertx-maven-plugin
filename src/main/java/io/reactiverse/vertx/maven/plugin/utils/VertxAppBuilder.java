/*
 * Copyright 2024 The Vert.x Community.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactiverse.vertx.maven.plugin.utils;

import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.*;

import static java.lang.ProcessBuilder.Redirect.INHERIT;

public class VertxAppBuilder {

    private final File java;
    private final List<String> jvmArgs = new ArrayList<>();
    private final List<File> classpathElements = new ArrayList<>();
    private final String mainClass;
    private final List<String> appArgs = new ArrayList<>();
    private File workDir;
    private final Map<String, String> env = new HashMap<>();

    public VertxAppBuilder(File java, String mainClass) {
        this.java = Objects.requireNonNull(java);
        if (!java.canExecute()) {
            throw new IllegalArgumentException("Java command not executable: " + java.getAbsolutePath());
        }
        if (StringUtils.isBlank(mainClass)) {
            throw new IllegalArgumentException("mainClass must not be blank");
        }
        this.mainClass = mainClass;
    }

    public VertxAppBuilder addJvmArg(String jvmArg) {
        jvmArgs.add(Objects.requireNonNull(jvmArg));
        return this;
    }

    public VertxAppBuilder addClasspathElement(File classpathElement) {
        classpathElements.add(Objects.requireNonNull(classpathElement));
        return this;
    }

    public VertxAppBuilder addAppArg(String appArg) {
        appArgs.add(Objects.requireNonNull(appArg));
        return this;
    }

    public VertxAppBuilder workDir(File workDir) {
        this.workDir = workDir;
        return this;
    }

    public VertxAppBuilder env(String key, String value) {
        env.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
        return this;
    }

    public ProcessBuilder processBuilder() {
        ProcessBuilder builder = new ProcessBuilder(getCommand())
            .redirectOutput(INHERIT)
            .redirectError(INHERIT)
            .redirectInput(INHERIT)
            .directory(workDir);
        StringJoiner joiner = new StringJoiner(File.pathSeparator);
        for (File elem : classpathElements) {
            joiner.add(StringUtils.quoteAndEscape(elem.getAbsolutePath(), '"'));
        }
        Map<String, String> environment = builder.environment();
        environment.put("CLASSPATH", joiner.toString());
        environment.putAll(env);
        return builder;
    }

    private List<String> getCommand() {
        List<String> command = new ArrayList<>();
        command.add(StringUtils.quoteAndEscape(java.getAbsolutePath(), '"'));
        command.addAll(jvmArgs);
        command.add(mainClass);
        command.addAll(appArgs);
        return command;
    }
}
