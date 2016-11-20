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

package io.fabric8.vertx.maven.plugin.mojos;

/**
 * Bunch of constants across the plugin
 *
 * @author kameshs
 */
public interface Constants {

    /**
     *
     */
    String FILE_CREATE = "FILE_CREATE";

    /**
     *
     */
    String FILE_CHANGE = "FILE_CHANGE";
    /**
     *
     */
    String FILE_DELETE = "FILE_DELETE";

    /**
     * The vert.x Core Launcher class
     */
    String IO_VERTX_CORE_LAUNCHER = "io.vertx.core.Launcher";

    /**
     * vert.x java-opt argument
     */
    String VERTX_ARG_JAVA_OPT = "--java-opts";

    /**
     * vert.x launcher argument
     */
    String VERTX_ARG_LAUNCHER_CLASS = "--launcher-class";

    /**
     * vert.x redeploy argument
     */
    String VERTX_ARG_REDEPLOY = "--redeploy=";

    /**
     * vert.x redeploy pattern
     */
    String VERTX_REDEPLOY_DEFAULT_PATTERN = "src/main/**/*.java";

    /**
     * vert.x redeploy pattern
     */
    String VERTX_REDEPLOY_DEFAULT_CLASSES_PATTERN = "target/classes/**/*";

    /**
     * vert.x configuration option
     */
    String VERTX_ARG_CONF = "-conf";

    /**
     * vert.x artifact classifier
     */
    String VERTX_CLASSIFIER = "vertx";

    /**
     *
     */
    String VERTX_CONFIG_FILE_JSON = "application.json";

    /**
     *
     */
    String VERTX_CONFIG_FILE_YAML = "application.yaml";


    /**
     *
     */
    String VERTX_CONFIG_FILE_YML = "application.yml";

    /**
     * vert.x command run
     */
    String VERTX_COMMAND_RUN = "run";

    /**
     * vert.x command stop
     */
    String VERTX_COMMAND_STOP = "stop";

    /**
     * vert.x command start
     */
    String VERTX_COMMAND_START = "start";

    /**
     *
     */
    String VERTX_PACKAGING = "jar";

    /**
     *
     */
    String VERTX_PID_FILE = "vertx-start-process.id";

    /**
     *
     */
    String VERTX_RUN_MODE_JAR = "jar";


}
