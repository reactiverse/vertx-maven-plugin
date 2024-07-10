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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileChangesHelperTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File nestedFile;

    private FileChangesHelper helper;

    @Before
    public void setUp() throws Exception {
        nestedFile = new File(temporaryFolder.getRoot(), "parent/child/nested.txt");
        FileUtils.write(nestedFile, "this is a test", StandardCharsets.UTF_8);
    }

    @After
    public void tearDown() throws Exception {
        if (helper != null) {
            helper.close();
        }
    }

    @Test
    public void shouldDetectFileChangesWithoutFilter() throws Exception {
        helper = new FileChangesHelper(temporaryFolder.getRoot(), null, null);
        assertFalse(helper.foundChanges());

        // File modification
        FileUtils.write(nestedFile, "Changed", StandardCharsets.UTF_8);
        assertTrue(helper.foundChanges());
        assertFalse(helper.foundChanges());

        // File creation
        File newFile = new File(temporaryFolder.getRoot(), "parent/child/newFile.txt");
        FileUtils.write(newFile, "Created", StandardCharsets.UTF_8);
        assertTrue(helper.foundChanges());
        assertFalse(helper.foundChanges());

        // File deletion
        FileUtils.deleteQuietly(newFile);
        assertTrue(helper.foundChanges());
        assertFalse(helper.foundChanges());

        // Directory creation
        File nestedDir = new File(temporaryFolder.getRoot(), "parent/empty");
        assertTrue(nestedDir.mkdir());
        assertTrue(helper.foundChanges());
        assertFalse(helper.foundChanges());

        // Directory modification
        File renamedDir = new File(temporaryFolder.getRoot(), "parent/renamed");
        assertTrue(nestedDir.renameTo(renamedDir));
        assertTrue(helper.foundChanges());
        assertFalse(helper.foundChanges());

        // Directory deletion
        FileUtils.deleteQuietly(renamedDir);
        assertTrue(helper.foundChanges());
        assertFalse(helper.foundChanges());
    }

    @Test
    public void shouldNotDetectNonIncludedFile() throws Exception {
        helper = new FileChangesHelper(temporaryFolder.getRoot(), Collections.singletonList("other"), null);
        assertFalse(helper.foundChanges());

        FileUtils.write(nestedFile, "Changed", StandardCharsets.UTF_8);
        assertFalse(helper.foundChanges());
    }

    @Test
    public void shouldNotDetectExcludedFile() throws Exception {
        helper = new FileChangesHelper(temporaryFolder.getRoot(), Collections.singletonList("parent/**"), Collections.singletonList("**/nested.txt"));
        assertFalse(helper.foundChanges());

        FileUtils.write(nestedFile, "Changed", StandardCharsets.UTF_8);
        assertFalse(helper.foundChanges());
    }

    @Test
    public void shouldDetectIncludedFile() throws Exception {
        helper = new FileChangesHelper(temporaryFolder.getRoot(), Collections.singletonList("parent/**"), null);
        assertFalse(helper.foundChanges());

        FileUtils.write(nestedFile, "Changed", StandardCharsets.UTF_8);
        assertTrue(helper.foundChanges());
        assertFalse(helper.foundChanges());
    }
}
