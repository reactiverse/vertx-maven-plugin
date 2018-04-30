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
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author kameshs
 */
public class ConfigConversionUtilTest {

    @Test
    public void convertSimpleYamlToJson() throws Exception {
        File yamlFile = new File("src/test/resources/testconfig.yaml");
        File jsonFilePath = File.createTempFile("testconfig", ".json");
        assertTrue(yamlFile.isFile());
        ConfigConverterUtil.convertYamlToJson(yamlFile, jsonFilePath);
        assertNotNull(jsonFilePath);
        String jsonDoc = FileUtils.readFileToString(jsonFilePath, "UTF-8");
        assertNotNull(jsonDoc);
        JSONObject jsonMap = new JSONObject(jsonDoc);
        assertNotNull(jsonMap);
        assertEquals(jsonMap.get("http.port"), 8080);

    }

    @Test
    public void convertArrayYamlToJson() throws Exception {
        File yamlFile = new File("src/test/resources/testconfig2.yaml");
        File jsonFilePath = File.createTempFile("testconfig2", ".json");
        assertTrue(yamlFile.isFile());
        ConfigConverterUtil.convertYamlToJson(yamlFile, jsonFilePath);
        assertNotNull(jsonFilePath);
        String jsonDoc = FileUtils.readFileToString(jsonFilePath, "UTF-8");
        assertNotNull(jsonDoc);
        JSONObject jsonMap = new JSONObject(jsonDoc);
        assertNotNull(jsonMap);
        assertNotNull(jsonMap.get("names"));
        JSONArray names = jsonMap.getJSONArray("names");
        assertThat(names.length() == 4).isTrue();
        assertEquals(names.get(0), "kamesh");

    }


}
