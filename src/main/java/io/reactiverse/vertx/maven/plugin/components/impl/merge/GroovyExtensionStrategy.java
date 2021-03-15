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

import com.google.common.base.Joiner;
import org.apache.maven.project.MavenProject;
import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class GroovyExtensionStrategy implements MergingStrategy {

    @Override
    public MergeResult merge(MavenProject project, Asset local, List<Asset> deps) {
        List<String> extensionClassesList = new ArrayList<>();
        List<String> staticExtensionClassesList = new ArrayList<>();

        List<Properties> all = new ArrayList<>();
        all.add(asProperties(local));
        if (deps != null) {
            deps.forEach(s -> all.add(asProperties(s)));
        }

        for (Properties properties : all) {
            String staticExtensionClasses = properties.getProperty("staticExtensionClasses", "").trim();
            String extensionClasses = properties.getProperty("extensionClasses", "").trim();
            if (extensionClasses.length() > 0) {
                append(extensionClasses, extensionClassesList);
            }
            if (staticExtensionClasses.length() > 0) {
                append(staticExtensionClasses, staticExtensionClassesList);
            }
        }

        List<String> desc = new ArrayList<>();
        desc.add("moduleName=" + project.getArtifactId());
        desc.add("moduleVersion=" + project.getVersion());
        if (!extensionClassesList.isEmpty()) {
            desc.add("extensionClasses=" + join(extensionClassesList));
        }
        if (!staticExtensionClassesList.isEmpty()) {
            desc.add("staticExtensionClasses=" + join(staticExtensionClassesList));
        }

        return new TextResult(desc);
    }

    private static void append(String entry, List<String> list) {
        if (entry != null) {
            Collections.addAll(list, entry.split("\\s*,\\s*"));
        }
    }

    private static String join(List<String> strings) {
        return Joiner.on(",").join(strings);
    }

    private static Properties asProperties(Asset asset) {
        Properties properties = new Properties();
        try (InputStream is = asset.openStream()) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
