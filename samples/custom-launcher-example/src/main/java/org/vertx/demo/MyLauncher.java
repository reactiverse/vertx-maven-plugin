package org.vertx.demo;

import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class MyLauncher extends Launcher {

    public static void main(String[] args) {
        // IMPORTANT
        // This is required to use our custom launcher.
        new MyLauncher().dispatch(args);
    }


    @Override
    public void beforeStartingVertx(VertxOptions options) {
        // Customize the options
        options.setWorkerPoolSize(20);
    }

    @Override
    public void afterConfigParsed(JsonObject config) {
        // Inject a specific config
        config.put("message", "Bonjour");
    }
}
