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

package io.fabric8.vertx.maven.plugin;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kameshs
 */
public class FileFilterMain {

    public static void main(String[] args) {

        Commandline commandline = new Commandline();
        commandline.setExecutable("java");
        commandline.createArg().setValue("io.vertx.core.Launcher");
        commandline.createArg().setValue("--redeploy=target/**/*");

        System.out.println(commandline);

        File baseDir = new File("/Users/kameshs/git/fabric8io/vertx-maven-plugin/samples/vertx-demo");
        List<String> includes = new ArrayList<>();
        includes.add("src/**/*.java");
        //FileAlterationMonitor monitor  = null;
        try {

            Set<Path> inclDirs = new HashSet<>();

            includes.forEach(s -> {
                try {

                    if (s.startsWith("**")) {
                        Path rootPath = Paths.get(baseDir.toString());
                        if (Files.exists(rootPath)) {
                            File[] dirs = rootPath.toFile()
                                    .listFiles((dir, name) -> dir.isDirectory());
                            Objects.requireNonNull(dirs);
                            Stream.of(dirs).forEach(f -> inclDirs.add(Paths.get(f.toString())));
                        }
                    } else if (s.contains("**")) {
                        String root = s.substring(0, s.indexOf("/**"));
                        Path rootPath = Paths.get(baseDir.toString(), root);
                        if (Files.exists(rootPath)) {
                            File[] dirs = rootPath.toFile()
                                    .listFiles((dir, name) -> dir.isDirectory());
                            Objects.requireNonNull(dirs);
                            Stream.of(dirs).forEach(f -> inclDirs.add(Paths.get(f.toString())));
                        }
                    }

                    List<Path> dirs = FileUtils.getFileAndDirectoryNames(baseDir,
                            s, null, true, true, true, true)
                            .stream().map(FileUtils::dirname).map(Paths::get)
                            .filter(p -> Files.exists(p) && Files.isDirectory(p))
                            .collect(Collectors.toList());

                    inclDirs.addAll(dirs);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            FileAlterationMonitor monitor = fileWatcher(inclDirs);

            Runnable monitorTask = () -> {
                try {
                    monitor.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            monitorTask.run();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private static FileAlterationMonitor fileWatcher(Set<Path> inclDirs) {
        FileAlterationMonitor monitor = new FileAlterationMonitor(1000);

        inclDirs
                .forEach(path -> {
                    System.out.println("Adding Observer to " + path.toString());
                    FileAlterationObserver observer = new FileAlterationObserver(path.toFile());
                    observer.addListener(new FileAlterationListenerAdaptor() {
                        @Override
                        public void onFileCreate(File file) {
                            System.out.println("File Create:" + file.toString());
                        }

                        @Override
                        public void onFileChange(File file) {
                            System.out.println("File Change:" + file.toString());
                        }

                        @Override
                        public void onFileDelete(File file) {
                            System.out.println("File Delete:" + file.toString());
                        }

                    });
                    monitor.addObserver(observer);
                });

        return monitor;

    }


}
