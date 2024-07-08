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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;

public class MavenUtils {

    public static String asMavenCoordinates(Artifact artifact) {
        StringBuilder artifactCords = new StringBuilder().
            append(artifact.getGroupId())
            .append(":")
            .append(artifact.getArtifactId());
        if (!"jar".equals(artifact.getType()) || artifact.hasClassifier()) {
            artifactCords.append(":").append(artifact.getType());
        }
        if (artifact.hasClassifier()) {
            artifactCords.append(":").append(artifact.getClassifier());
        }
        artifactCords.append(":").append(ArtifactUtils.toSnapshotVersion(artifact.getVersion()));
        return artifactCords.toString();
    }
}
