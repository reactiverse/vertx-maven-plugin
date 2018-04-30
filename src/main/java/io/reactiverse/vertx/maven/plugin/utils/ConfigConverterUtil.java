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

package io.reactiverse.vertx.maven.plugin.utils;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
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
    public static void convertYamlToJson(File yamlFile, File jsonFilePath) throws IOException {
        FileUtils.deleteQuietly(jsonFilePath);
        String content = FileUtils.readFileToString(yamlFile, "UTF-8");
        Yaml yaml = new Yaml();
        Map<Object, Object> map = (Map<Object, Object>) yaml.load(content);
        JSONObject jsonObject = new JSONObject(map);
        FileUtils.write(jsonFilePath, jsonObject.toString(), "UTF-8");
    }

}
