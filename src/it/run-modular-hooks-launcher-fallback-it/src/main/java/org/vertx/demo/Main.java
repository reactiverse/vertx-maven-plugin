package org.vertx.demo;

import io.vertx.launcher.application.VertxApplication;

public class Main extends VertxApplication {

    public Main(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        new Main(args).launch();
    }
}
