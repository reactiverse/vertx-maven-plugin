package demo;

import io.vertx.core.AbstractVerticle;

public class SimpleVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
            .requestHandler(req -> req.response().end("aloha"))
            .listen(8080);
    }
}
