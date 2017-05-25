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

public class SimpleVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        int instances = vertx.getOrCreateContext().getInstanceCount();
        System.out.println("Instances="+instances);
        vertx.close();
    }
}
