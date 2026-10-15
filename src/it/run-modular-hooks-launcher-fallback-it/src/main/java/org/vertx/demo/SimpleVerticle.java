package org.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class SimpleVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startFuture) throws Exception {
        if (getClass().getModule().isNamed()) {
            startFuture.fail("Expected to run in unnamed module (classpath) but running as named module: " + getClass().getModule().getName());
            return;
        }
        System.out.println("Running in unnamed module (classpath mode)");
        startFuture.complete();

        vertx.setTimer(500, l -> vertx.close());
    }
}
