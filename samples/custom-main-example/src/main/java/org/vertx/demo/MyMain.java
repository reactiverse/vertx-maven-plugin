package org.vertx.demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class MyMain {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(MainVerticle.class.getName(),
            new DeploymentOptions().setConfig(new JsonObject().put("message", "Ola")));
    }
}
