/*
 *    Copyright (c) 2016-2018 Red Hat, Inc.
 *
 *    Red Hat licenses this file to you under the Apache License, version
 *    2.0 (the "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *    implied.  See the License for the specific language governing
 *    permissions and limitations under the License.
 */

package io.reactiverse.vertx.maven.plugin.model;

/**
 * List of additional entries added to the manifest of the resulting artifacts
 */
public enum ExtraManifestKeys {

    PROJECT_ARTIFACT_ID ("Maven-ArtifactId"),
    PROJECT_GROUP_ID ("Maven-ArtifactId"),
    PROJECT_VERSION ("Maven-Version"),
    PROJECT_NAME ("Project-Name"),
    PROJECT_URL ("Project-URL"),

    PROJECT_DEPS ("Project-Dependencies"),

    BUILD_TIMESTAMP ("Build-Timestamp");

    private final String header;

    ExtraManifestKeys(String header) {
        this.header  = header;
    }

    public String header() {
        return header;
    }
}
