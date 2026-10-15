package org.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class SimpleModularVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startFuture) throws Exception {
        startFuture.complete();
    }
}
