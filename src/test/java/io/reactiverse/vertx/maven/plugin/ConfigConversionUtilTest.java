/*
 *
 *   Copyright (c) 2016-2017 Red Hat, Inc.
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

package io.reactiverse.vertx.maven.plugin;

import io.reactiverse.vertx.maven.plugin.utils.ConfigConverterUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author kameshs
 */
@SuppressWarnings("unchecked")
public class ConfigConversionUtilTest {

    @Test
    public void convertSimpleYamlToJson() throws Exception {
        Path yamlFile = Paths.get(this.getClass().getResource("/testconfig.yaml").toURI());
        Path jsonFilePath = Files.createTempFile("testconfig", ".json");
        assertNotNull(yamlFile);
        assertTrue(yamlFile.toFile().isFile());
        assertTrue(yamlFile.toFile().exists());
        ConfigConverterUtil.convertYamlToJson(yamlFile, jsonFilePath);
        assertNotNull(jsonFilePath);
        String jsonDoc = new String(Files.readAllBytes(jsonFilePath));
        assertNotNull(jsonDoc);
        JSONObject jsonMap = new JSONObject(jsonDoc);
        assertNotNull(jsonMap);
        assertEquals(jsonMap.get("http.port"), 8080);

    }

    @Test
    public void convertArrayYamlToJson() throws Exception {
        Path yamlFile = Paths.get(this.getClass().getResource("/testconfig2.yaml").toURI());
        Path jsonFilePath = Files.createTempFile("testconfig2", ".json");
        assertNotNull(yamlFile);
        assertTrue(yamlFile.toFile().isFile());
        assertTrue(yamlFile.toFile().exists());
        ConfigConverterUtil.convertYamlToJson(yamlFile, jsonFilePath);
        assertNotNull(jsonFilePath);
        String jsonDoc = new String(Files.readAllBytes(jsonFilePath));
        assertNotNull(jsonDoc);
        JSONObject jsonMap = new JSONObject(jsonDoc);
        assertNotNull(jsonMap);
        assertNotNull(jsonMap.get("names"));
        JSONArray names = jsonMap.getJSONArray("names");
        assertTrue(names.length() == 4);
        assertEquals(names.get(0), "kamesh");

    }


}
