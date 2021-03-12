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

import org.apache.maven.project.MavenProject;
import org.jboss.shrinkwrap.api.asset.Asset;

import java.util.List;

public interface MergingStrategy {

    static MergingStrategy forName(String name) {
        MergingStrategy strategy;
        switch (name) {
            case "org.codehaus.groovy.runtime.ExtensionModule":
                strategy = new GroovyExtensionStrategy();
                break;
            case "/META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat":
                strategy = new Log4j2PluginsStrategy();
                break;
            default:
                strategy = new AppendStrategy();
        }
        return strategy;
    }

    MergeResult merge(MavenProject project, Asset local, List<Asset> deps);

}
