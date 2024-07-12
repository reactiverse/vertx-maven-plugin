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
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.SelectorUtils;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.codehaus.plexus.util.SelectorUtils.ANT_HANDLER_PREFIX;
import static org.codehaus.plexus.util.SelectorUtils.PATTERN_HANDLER_SUFFIX;

/**
 * Monitors the filesystem for changes.
 */
public class FileChangesHelper implements AutoCloseable {

    private final Log log;
    private final FileAlterationObserver observer;

    private boolean updated;

    public FileChangesHelper(Log log, File redeployRootDirectory, List<String> redeployIncludes, List<String> redeployExcludes) throws Exception {
        this.log = log;
        observer = new FileAlterationObserver(redeployRootDirectory, new RedeploymentFileFilter(redeployRootDirectory, redeployIncludes, redeployExcludes));
        observer.initialize();
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onDirectoryChange(File directory) {
                onListenerEvent(directory);
            }

            @Override
            public void onDirectoryCreate(File directory) {
                onListenerEvent(directory);
            }

            @Override
            public void onDirectoryDelete(File directory) {
                onListenerEvent(directory);
            }

            @Override
            public void onFileChange(File file) {
                onListenerEvent(file);
            }

            @Override
            public void onFileCreate(File file) {
                onListenerEvent(file);
            }

            @Override
            public void onFileDelete(File file) {
                onListenerEvent(file);
            }
        });
    }

    private void onListenerEvent(File event) {
        if (log.isDebugEnabled()) {
            log.debug("Changed file event: " + event);
        }
        if (!updated) {
            updated = true;
        }
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

    private static class RedeploymentFileFilter implements FileFilter {

        final Path rootDirectoryPath;
        final List<String> includes;
        final List<String> excludes;

        RedeploymentFileFilter(File redeployRootDirectory, List<String> redeployIncludes, List<String> redeployExcludes) {
            rootDirectoryPath = redeployRootDirectory.toPath();
            includes = toAntPatterns(redeployIncludes);
            excludes = toAntPatterns(redeployExcludes);
        }

        private static List<String> toAntPatterns(List<String> patterns) {
            if (patterns == null || patterns.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> res = new ArrayList<>(patterns.size());
            for (String pattern : patterns) {
                res.add(ANT_HANDLER_PREFIX + pattern + PATTERN_HANDLER_SUFFIX);
            }
            return res;
        }

        @Override
        public boolean accept(File pathname) {
            String relativePath = rootDirectoryPath.relativize(pathname.toPath()).toString();
            boolean accepted = includes.isEmpty();
            for (String include : includes) {
                if (SelectorUtils.matchPath(include, relativePath)) {
                    accepted = true;
                    break;
                }
            }
            if (accepted) {
                for (String exclude : excludes) {
                    if (SelectorUtils.matchPath(exclude, relativePath)) {
                        accepted = false;
                        break;
                    }
                }
            }
            return accepted;
        }
    }
}
