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

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamPumper;
import io.fabric8.vertx.maven.plugin.utils.StreamToLogConsumer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kameshs
 */
public class FileFilterMain {

    public static void main(String[] args) {

        File baseDir = new File("/Users/kameshs/git/maven/plugins/vertx-maven-plugin/samples/vertx-demo");
        List<String> includes = new ArrayList<>();
        includes.add("src/**/*.java");
        try {
            List<Path> inclDirs = FileUtils.getFileAndDirectoryNames(baseDir,
                    "src/**/*.java", null, true, true, true, false)
                    .stream().map(FileUtils::dirname).map(Paths::get)
                    .filter(p -> Files.exists(p) && Files.isDirectory(p))
                    .collect(Collectors.toList());

            Object[] arrDirs = inclDirs.stream().map(Path::toString).toArray();
            Commandline cli = new Commandline();
            cli.setExecutable("java");


            for (int i = 0; i < arrDirs.length; i++) {
                System.out.println(arrDirs[i]);
            }


            System.out.println(cli);

            Process process = cli.execute();

            StreamToLogConsumer logConsumer = line -> System.out.println(line);

            StreamPumper outPumper = new StreamPumper(process.getInputStream(), logConsumer);
            StreamPumper errPumper = new StreamPumper(process.getErrorStream(), logConsumer);

            outPumper.setPriority(Thread.MIN_PRIORITY + 1);
            errPumper.setPriority(Thread.MIN_PRIORITY + 1);

            outPumper.start();
            errPumper.start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void startWatcher() {
        List<String> redeployPatterns = new ArrayList<>();
        redeployPatterns.add("src/main/**/*.java");

        Hashtable<WatchKey, Path> keys = new Hashtable<>();

        try {

            File baseDir = new File("/Users/kameshs/git/maven/plugins/vertx-maven-plugin/samples/vertx-demo");

            List<Path> inclDirs = FileUtils.getFileAndDirectoryNames(baseDir,
                    "src/main/**/*.java,src/test/**/*.java", null, true, true, true, false)
                    .stream().map(FileUtils::dirname).map(Paths::get)
                    .filter(p -> Files.exists(p) && Files.isDirectory(p))
                    .collect(Collectors.toList());

            FileSystem fs = FileSystems.getDefault();

            WatchService watcher = fs.newWatchService();

            inclDirs.forEach(path -> {
                try {
                    WatchKey key = path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                    keys.put(key, path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            while (true) {

                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                Path dir = keys.get(key);

                WatchEvent.Kind<?> kind = null;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    kind = watchEvent.kind();
                    if (StandardWatchEventKinds.ENTRY_DELETE == kind ||
                            StandardWatchEventKinds.ENTRY_CREATE == kind ||
                            StandardWatchEventKinds.ENTRY_DELETE == kind) {

                        Path path = ((WatchEvent<Path>) watchEvent).context();

                    } else {
                        continue;
                    }
                }

                if (!key.reset()) {
                    break;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
