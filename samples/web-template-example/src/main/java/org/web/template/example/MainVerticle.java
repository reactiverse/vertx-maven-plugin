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