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

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;

/**
 * Monitors the filesystem for changes.
 */
public class FileChangesHelper implements AutoCloseable {

    private final FileAlterationObserver observer;

    private boolean updated;

    public FileChangesHelper(File root) throws Exception {
        observer = new FileAlterationObserver(root);
        observer.initialize();
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onDirectoryChange(File directory) {
                updated = true;
            }

            @Override
            public void onDirectoryCreate(File directory) {
                updated = true;
            }

            @Override
            public void onDirectoryDelete(File directory) {
                updated = true;
            }

            @Override
            public void onFileChange(File file) {
                updated = true;
            }

            @Override
            public void onFileCreate(File file) {
                updated = true;
            }

            @Override
            public void onFileDelete(File file) {
                updated = true;
            }
        });
    }

    public boolean foundChanges() {
        updated = false;
        observer.checkAndNotify();
        return updated;
    }

    @Override
    public void close() throws Exception {
        observer.destroy();
    }
}
