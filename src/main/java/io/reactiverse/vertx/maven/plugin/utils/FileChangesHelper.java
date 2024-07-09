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

import io.reactiverse.vertx.maven.plugin.mojos.Redeployment;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.codehaus.plexus.util.SelectorUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.codehaus.plexus.util.SelectorUtils.ANT_HANDLER_PREFIX;
import static org.codehaus.plexus.util.SelectorUtils.PATTERN_HANDLER_SUFFIX;

/**
 * Monitors the filesystem for changes.
 */
public class FileChangesHelper implements AutoCloseable {

    private final FileAlterationObserver observer;

    private boolean updated;

    public FileChangesHelper(Redeployment redeployment) throws Exception {
        observer = new FileAlterationObserver(redeployment.getRootDirectory(), new RedeploymentFileFilter(redeployment));
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

    private static class RedeploymentFileFilter implements FileFilter {

        final File rootDirectory;
        final List<String> includes;
        final List<String> excludes;

        RedeploymentFileFilter(Redeployment redeployment) {
            rootDirectory = redeployment.getRootDirectory();
            includes = toAntPatterns(redeployment.getIncludes());
            excludes = toAntPatterns(redeployment.getExcludes());
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
            String relativePath = rootDirectory.toPath().relativize(pathname.toPath()).toString();
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
