package org.vertx.demo;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        if (!Main.class.getModule().isNamed()) {
            System.err.println("Expected to run as a named module but running in unnamed module");
            System.exit(1);
        }
        System.out.println("Running in module: " + Main.class.getModule().getName());

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new SimpleVerticle()).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("Succeeded in deploying verticle");
            } else {
                System.err.println("Failed to deploy verticle");
                ar.cause().printStackTrace();
            }
            vertx.setTimer(500, l -> vertx.close());
        });
    }
}
