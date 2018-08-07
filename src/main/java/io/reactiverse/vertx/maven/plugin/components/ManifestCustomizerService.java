/*
 *
 *   Copyright (c) 2016-2018 Red Hat, Inc.
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
package io.reactiverse.vertx.maven.plugin.components;

import io.reactiverse.vertx.maven.plugin.mojos.AbstractVertxMojo;
import org.apache.maven.project.MavenProject;

import java.util.Map;

/**
 * Implementation of this service are able to provide custom entries to the "fat-jar" manifest file.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ManifestCustomizerService {

    /**
     * Returns the entries to add to the manifest.
     *
     * @param mojo    the mojo
     * @param project the project
     * @return a non-null map with the entries
     */
    Map<String, String> getEntries(AbstractVertxMojo mojo, MavenProject project);
}
