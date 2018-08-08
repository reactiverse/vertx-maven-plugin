/*
 *    Copyright (c) 2016-2018 Red Hat, Inc.
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

package demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.cli.Argument;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        CLI cli = CLI.create("copy")
            .setSummary("A command line interface to copy files")
            .addOption(new Option()
                .setLongName("directory")
                .setShortName("R")
                .setDescription("enables directory support")
                .setFlag(true))
            .addArgument(new Argument()
                .setDescription("The source")
                .setArgName("source"))
            .addArgument(new Argument()
                .setDescription("The target")
                .setArgName("target"));

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);
        if (args.length < 3) {
            System.out.println(builder.toString());
            System.exit(0);
        }

        CommandLine commandLine = cli.parse(Arrays.asList(args));

        String sourceDir = commandLine.allArguments().get(0);
        String targetDir = commandLine.allArguments().get(1);

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(SimpleVerticle.class.getName(), new DeploymentOptions()
            .setConfig(new JsonObject()
                .put("sourceDir", sourceDir)
                .put("targetDir", targetDir)), event -> {
            vertx.close();
            System.exit(0);
        });

    }
}
