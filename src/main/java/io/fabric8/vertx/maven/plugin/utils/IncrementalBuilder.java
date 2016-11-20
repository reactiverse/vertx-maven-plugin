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

import io.fabric8.vertx.maven.plugin.callbacks.Callback;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author kameshs
 * @deprecated use {@link IncrementalBuilder2}
 */
public class IncrementalBuilder implements Runnable {

    private final WatchService fileWatcherService;

    private final Callback<WatchEvent.Kind<?>, Path> callback;

    private final Log logger;

    Hashtable<WatchKey, Path> watchPathKeys = new Hashtable<>();

    public IncrementalBuilder(List<Path> inclDirs,
                              Callback<WatchEvent.Kind<?>, Path> callback,
                              Log logger)
            throws IOException {

        this.fileWatcherService = FileSystems.getDefault().newWatchService();
        this.callback = callback;
        this.logger = logger;


        Consumer<Path> fnPathRegistration = path -> {
            try {
                register(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };

        inclDirs.forEach(fnPathRegistration);
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    @Override
    public void run() {

        logger.info("Incremental builder watching paths: " + watchPathKeys.values());

        while (true) {

            WatchKey key;

            try {
                key = fileWatcherService.take();
            } catch (InterruptedException e) {
                return;
            }

            watchPathKeys.get(key);

            WatchEvent.Kind<?> kind = null;
            for (WatchEvent<?> watchEvent : key.pollEvents()) {
                kind = watchEvent.kind();

                if (StandardWatchEventKinds.ENTRY_MODIFY == kind ||
                        StandardWatchEventKinds.ENTRY_CREATE == kind ||
                        StandardWatchEventKinds.ENTRY_DELETE == kind) {

                    WatchEvent<Path> pathEvent = cast(watchEvent);
                    Path path = pathEvent.context();

                    this.callback.call(kind, path);

                } else {
                    continue;
                }
            }

            if (!key.reset()) {
                break;
            }
        }
    }

    /**
     * @param path
     * @throws IOException
     */
    protected void register(Path path) throws IOException {
        try {
            WatchKey key = path.register(fileWatcherService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            watchPathKeys.put(key, path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
