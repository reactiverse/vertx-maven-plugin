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

package demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;

public class SimpleVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {

        List<String> processArgs  = vertx.getOrCreateContext().processArgs();

        Optional<String> haGroupOptional = processArgs.stream()
            .filter(arg -> arg.contains("quorum"))
            .findAny();

        String quorum = haGroupOptional.get().split("=")[1];

        System.out.println("Quorum="+quorum);
        vertx.close();
    }
}
