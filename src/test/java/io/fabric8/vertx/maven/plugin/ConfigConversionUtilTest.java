/*
 *   Copyright 2016 Kamesh Sampath
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.fabric8.vertx.maven.plugin;

import io.fabric8.vertx.maven.plugin.utils.ConfigConverterUtil;
import io.vertx.core.json.Json;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

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
        Map<Object, Object> jsonMap = Json.decodeValue(jsonDoc, Map.class);
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
        Map<Object, Object> jsonMap = Json.decodeValue(jsonDoc, Map.class);
        assertNotNull(jsonMap);
        assertNotNull(jsonMap.get("names"));
        List<String> names = (List<String>) jsonMap.get("names");
        assertTrue(names.size() == 4);
        assertEquals(names.get(0), "kamesh");

    }


}
