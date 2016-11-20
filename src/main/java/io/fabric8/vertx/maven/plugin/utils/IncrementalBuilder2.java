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
import io.fabric8.vertx.maven.plugin.mojos.Constants;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.plugin.logging.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Set;

/**
 * @author kameshs
 */
public class IncrementalBuilder2 extends FileAlterationListenerAdaptor implements Runnable, Closeable {

    private final Log logger;

    private final Callback<String, Path> callback;

    private FileAlterationMonitor monitor;

    private Hashtable<Path, FileAlterationObserver> observers = new Hashtable<>();

    public IncrementalBuilder2(Set<Path> inclDirs,
                               Callback<String, Path> callback,
                               Log logger, long watchTimeInterval)
            throws IOException {

        this.callback = callback;
        this.logger = logger;
        this.monitor = new FileAlterationMonitor(watchTimeInterval);
        inclDirs.forEach(this::buildObserver);

    }

    @Override
    public void run() {
        try {
            this.monitor.start();
        } catch (Exception e) {
            logger.error("Unable to start Incremental Builder", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.monitor != null) {
            try {
                this.monitor.stop();
            } catch (Exception e) {
                //ignore
            }
        }
    }

    /**
     * @param path
     */
    protected synchronized void buildObserver(Path path) {

        logger.info("Observing path:" + path.toString());

        FileAlterationObserver observer = new FileAlterationObserver(path.toFile());

        observer.addListener(this);

        observers.put(path, observer);

        this.monitor.addObserver(observer);
    }

    /**
     *
     */
    protected synchronized void syncMointor() {
        observers.forEach((path, observer) -> {
            this.monitor.getObservers().forEach(observer2 -> {
                Path path1 = Paths.get(observer2.getDirectory().toString());
                if (!observers.containsKey(path1)) {
                    this.monitor.removeObserver(observer2);
                }
            });
        });
    }


    @Override
    public void onDirectoryCreate(File directory) {
        buildObserver(Paths.get(directory.toString()));
        syncMointor();
    }


    @Override
    public void onDirectoryDelete(File directory) {
        observers.remove(Paths.get(directory.toString()));
        syncMointor();
    }

    @Override
    public void onFileCreate(File file) {
        if (logger.isDebugEnabled()) {
            logger.debug("File Created: " + file);
        }
        this.callback.call(Constants.FILE_CREATE, Paths.get(file.toString()));
    }

    @Override
    public void onFileChange(File file) {
        if (logger.isDebugEnabled()) {
            logger.debug("File Changed: " + file);
        }
        this.callback.call(Constants.FILE_CHANGE, Paths.get(file.toString()));
    }

    @Override
    public void onFileDelete(File file) {
        if (logger.isDebugEnabled()) {
            logger.debug("File Deleted: " + file);
        }
        this.callback.call(Constants.FILE_DELETE, Paths.get(file.toString()));
    }

}
