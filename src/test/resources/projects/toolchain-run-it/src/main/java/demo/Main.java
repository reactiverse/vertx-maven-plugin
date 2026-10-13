package demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> vertx.close().await()));

        vertx.deployVerticle(SimpleVerticle.class.getName(), new DeploymentOptions()
            .setThreadingModel(ThreadingModel.EVENT_LOOP));
    }
}
