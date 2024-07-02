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

package io.reactiverse.vertx.maven.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactiverse.vertx.maven.plugin.utils.ConfigConverterUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kameshs
 */
public class ConfigConversionUtilTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void convertSimpleYamlToJson() throws Exception {
        File yamlFile = new File("src/test/resources/testconfig.yaml");
        assertThat(yamlFile).exists().canRead();
        File jsonFilePath = tempFolder.newFile("testconfig.json");
        ConfigConverterUtil.convertYamlToJson(yamlFile, jsonFilePath);
        assertThat(jsonFilePath).exists().canRead();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonFilePath);
        assertThat(jsonNode.get("http.port").asInt()).isEqualTo(8080);
    }

    @Test
    public void convertArrayYamlToJson() throws Exception {
        File yamlFile = new File("src/test/resources/testconfig2.yaml");
        assertThat(yamlFile).exists().canRead();
        File jsonFilePath = tempFolder.newFile("testconfig2.json");
        ConfigConverterUtil.convertYamlToJson(yamlFile, jsonFilePath);
        assertThat(jsonFilePath).exists().canRead();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonFilePath);
        JsonNode names = jsonNode.get("names");
        assertThat(names.isArray()).isTrue();
        assertThat(names)
            .hasSize(4)
            .first().extracting(JsonNode::asText).isEqualTo("kamesh");
    }
}
