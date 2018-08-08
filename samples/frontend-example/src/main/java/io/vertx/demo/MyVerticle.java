package io.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class MyVerticle extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.get("/*").handler(StaticHandler.create().setCachingEnabled(false));

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8080, ar -> System.out.println("Server started on port: " + ar.result().actualPort()));
    }
}
