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

import org.apache.logging.log4j.core.config.plugins.processor.PluginCache;
import org.apache.logging.log4j.core.config.plugins.processor.PluginEntry;
import org.apache.maven.project.MavenProject;
import org.jboss.shrinkwrap.api.asset.UrlAsset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.util.Map;

import static java.util.Collections.enumeration;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class Log4j2PluginsStrategyTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    Log4j2PluginsStrategy strategy;
    MavenProject project;

    File actual;

    @Before
    public void setUp() throws Exception {
        strategy = new Log4j2PluginsStrategy();

        project = new MavenProject();
        project.setArtifactId("project-name");
        project.setVersion("1.0");

        actual = temporaryFolder.newFile();
    }

    @Test
    public void testMerge() throws Exception {
        URL local = getClass().getClassLoader().getResource("unit/merge/Log4j2Plugins.dat");
        assertNotNull(local);
        URL builtin = getClass().getClassLoader().getResource("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat");
        assertNotNull(builtin);

        MergeResult result = strategy.merge(project, new UrlAsset(local), singletonList(new UrlAsset(builtin)));
        result.writeTo(actual);

        PluginCache cache = new PluginCache();
        cache.loadCacheFiles(enumeration(singletonList(actual.toURI().toURL())));

        Map<String, Map<String, PluginEntry>> categories = cache.getAllCategories();
        assertThat(categories).hasSizeGreaterThan(1).containsKeys("converter");

        Map<String, PluginEntry> converters = categories.get("converter");
        assertThat(converters).hasSizeGreaterThan(3)
            // builtin
            .containsKeys("black", "blue")
            // custom
            .containsKeys("hellopatternconverter");
        assertThat(converters.get("hellopatternconverter").getClassName()).isEqualTo("io.vertx.example.HelloPatternConverter");
    }
}
