package org.vertx.demo;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        if (Main.class.getModule().isNamed()) {
            System.err.println("Expected to run in unnamed module (classpath) but running as named module: " + Main.class.getModule().getName());
            System.exit(1);
        }
        System.out.println("Running in unnamed module (classpath mode)");

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new SimpleModularVerticle()).onComplete(ar -> {
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
