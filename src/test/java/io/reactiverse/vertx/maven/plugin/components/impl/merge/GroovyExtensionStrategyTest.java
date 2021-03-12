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
import org.apache.maven.project.MavenProject;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of {@link GroovyExtensionStrategy}.
 */
public class GroovyExtensionStrategyTest {

    private static final String NEWLINE = System.getProperty("line.separator");

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    GroovyExtensionStrategy strategy;
    MavenProject project;

    File actual;
    File expected;

    @Before
    public void setUp() throws Exception {
        strategy = new GroovyExtensionStrategy();

        project = new MavenProject();
        project.setArtifactId("project-name");
        project.setVersion("1.0");

        actual = temporaryFolder.newFile();
        expected = temporaryFolder.newFile();
    }

    @Test
    public void testSimple() throws Exception {
        Asset local = new StringAsset(String.join(NEWLINE, "a.A", "b.B"));
        List<Asset> deps = Arrays.asList(new StringAsset(String.join(NEWLINE, "a.A", "c.C")), new StringAsset("d.D"));
        List<String> expectedLines = Arrays.asList("moduleName=project-name", "moduleVersion=1.0");
        test(local, deps, expectedLines);
    }

    @Test
    public void testWithExtensions() throws Exception {
        Asset local = new StringAsset(String.join(NEWLINE, "staticExtensionClasses: a.A, b.B", "extensionClasses: c.C"));
        List<String> expectedLines = Arrays.asList("moduleName=project-name", "moduleVersion=1.0", "extensionClasses=c.C", "staticExtensionClasses=a.A,b.B");
        test(local, null, expectedLines);
    }

    private void test(Asset local, List<Asset> deps, List<String> expectedLines) throws IOException {
        MergeResult merge = strategy.merge(project, local, deps);
        merge.writeTo(actual);

        FileUtils.writeLines(expected, expectedLines);

        assertThat(actual).hasSameTextualContentAs(expected, StandardCharsets.UTF_8);
    }

}
