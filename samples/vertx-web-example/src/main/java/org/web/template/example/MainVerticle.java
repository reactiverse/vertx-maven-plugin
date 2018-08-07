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

package org.web.template.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        final Router router = Router.router(vertx);

        final ThymeleafTemplateEngine templateEngine = ThymeleafTemplateEngine.create();

        router.get("/assets*").handler(StaticHandler.create());

        router.get("/").handler(ctx -> {
            ctx.put("welcome", "Hello vert.x!");

            templateEngine.render(ctx, "templates/index.html", res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });

        });

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
