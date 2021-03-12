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

import org.apache.commons.io.IOUtils;
import org.apache.maven.project.MavenProject;
import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AppendStrategy implements MergingStrategy {

    @Override
    public MergeResult merge(MavenProject project, Asset local, List<Asset> deps) {
        // Regular merge, concat things.

        // Start with deps
        Set<String> fromDeps = new LinkedHashSet<>();
        if (deps != null) {
            deps.stream().map(this::readLines).forEach(fromDeps::addAll);
        }
        if (local != null) {
            List<String> lines = readLines(local);
            if (lines.isEmpty()) {
                // Drop this SPI
                return new TextResult(Collections.emptyList());
            }
            return computeOutput(lines, fromDeps);
        } else {
            return new TextResult(new ArrayList<>(fromDeps));
        }
    }

    private List<String> readLines(Asset asset) {
        try (InputStream is = asset.openStream()) {
            return IOUtils.readLines(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MergeResult computeOutput(List<String> local, Set<String> fromDeps) {
        Set<String> lines = new LinkedHashSet<>();
        for (String line : local) {
            if (line.trim().equalsIgnoreCase("${COMBINE}")) {
                //Copy the ones form the dependencies on this line
                lines.addAll(fromDeps);
            } else {
                // Just copy the line
                lines.add(line);
            }
        }
        return new TextResult(new ArrayList<>(lines));
    }
}
