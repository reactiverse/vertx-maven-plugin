package org.vertx.demo;

import io.vertx.core.AbstractVerticle;

public class SimpleVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        System.out.println("MY_TEST_VAR=" + System.getenv("MY_TEST_VAR"));
        vertx.setTimer(500, l -> vertx.close());
    }
}
