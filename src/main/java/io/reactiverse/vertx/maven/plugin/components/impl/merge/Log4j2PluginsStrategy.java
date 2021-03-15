/*
 *   Copyright (c) 2016-2021 Red Hat, Inc.
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

package io.reactiverse.vertx.maven.plugin.components.impl.merge;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.core.config.plugins.processor.PluginCache;
import org.apache.maven.project.MavenProject;
import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Log4j2PluginsStrategy implements MergingStrategy {

    @Override
    public MergeResult merge(MavenProject project, Asset local, List<Asset> deps) {
        List<File> files = new ArrayList<>(1 + (deps == null ? 0 : deps.size()));
        try {
            if (local != null) {
                files.add(copy(local));
            }
            if (deps != null) {
                for (Asset dep : deps) {
                    files.add(copy(dep));
                }
            }

            List<URL> urls = new ArrayList<>(files.size());
            for (File resource : files) {
                urls.add(resource.toURI().toURL());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PluginCache cache = new PluginCache();

            cache.loadCacheFiles(Collections.enumeration(urls));
            cache.writeCache(baos);

            return new BinaryResult(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            files.forEach(File::delete);
        }
    }

    private File copy(Asset asset) {
        try (InputStream is = asset.openStream()) {
            File tempFile = File.createTempFile(Log4j2PluginsStrategy.class.getSimpleName(), null);
            FileUtils.copyInputStreamToFile(is, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
