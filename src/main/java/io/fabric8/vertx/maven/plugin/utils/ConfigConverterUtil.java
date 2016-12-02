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

package io.fabric8.vertx.maven.plugin.utils;

import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * This class is used to handle the configuration conversions form YAML to JSON
 *
 * @author kameshs
 */
public class ConfigConverterUtil {

    @SuppressWarnings("unchecked")
    public static void convertYamlToJson(Path yamlFile, Path jsonFilePath) throws IOException {

        String yamlDoc = new String(Files.readAllBytes(yamlFile));
        Yaml yaml = new Yaml();
        Map<Object, Object> map = (Map<Object, Object>) yaml.load(yamlDoc);
        JSONObject jsonObject = new JSONObject(map);
        Files.write(jsonFilePath, jsonObject.toString().getBytes());
    }

}
