/*
 *    Copyright (c) 2016-2017 Red Hat, Inc.
 *
 *    Red Hat licenses this file to you under the Apache License, version
 *    2.0 (the "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *    implied.  See the License for the specific language governing
 *    permissions and limitations under the License.
 */

package io.reactiverse.vertx.maven.plugin.utils;

import com.google.common.base.Strings;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kameshs
 */
public class SetupTemplateUtils {

    static final Configuration cfg;
    static final Log logger = new SystemStreamLog();
    public static final String JAVA_EXTENSION = ".java";

    static {
        cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setTemplateLoader(new ClassTemplateLoader(SetupTemplateUtils.class, "/"));
    }

    public static void createPom(Map<String, String> context, File pomFile) throws MojoExecutionException {
        try {

            Template temp = cfg.getTemplate("templates/pom-template.ftl");
            Writer out = new FileWriter(pomFile);
            temp.process(context, out);
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate pom.xml", e);
        }
    }

    public static void createVerticle(MavenProject project, String verticle, Log log) throws MojoExecutionException {
        if (Strings.isNullOrEmpty(verticle)) {
            return;
        }
        log.info("Creating verticle " + verticle);

        File root = new File(project.getBasedir(), "src/main/java");

        String packageName = null;
        String className;
        if (verticle.endsWith(JAVA_EXTENSION)) {
            verticle = verticle.substring(0, verticle.length() - JAVA_EXTENSION.length());
        }

        if (verticle.contains(".")) {
            int idx = verticle.lastIndexOf('.');
            packageName = verticle.substring(0, idx);
            className = verticle.substring(idx + 1);
        } else {
            className = verticle;
        }

        if (packageName != null) {
            File packageDir = new File(root, packageName.replace('.', '/'));
            if (!packageDir.exists()) {
                packageDir.mkdirs();
                log.info("Creating directory " + packageDir.getAbsolutePath());
            }
            root = packageDir;
        }

        File classFile = new File(root, className + JAVA_EXTENSION);
        Map<String, String> context = new HashMap<>();
        context.put("className", className);
        if (packageName != null) {
            context.put("packageName", packageName);
        }
        try {
            Template temp = cfg.getTemplate("templates/verticle-template.ftl");
            Writer out = new FileWriter(classFile);
            temp.process(context, out);
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate verticle", e);
        }

    }

}
