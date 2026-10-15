package org.vertx.demo;

import io.vertx.core.Deployable;
import io.vertx.launcher.application.VertxApplication;
import io.vertx.launcher.application.VertxApplicationHooks;

import java.util.function.Supplier;

public class Main extends VertxApplication implements VertxApplicationHooks {

    public Main(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        if (!Main.class.getModule().isNamed()) {
            System.err.println("Expected to run as a named module but running in unnamed module");
            System.exit(1);
        }
        System.out.println("Running in module: " + Main.class.getModule().getName());
        new Main(args).launch();
    }

    @Override
    public Supplier<? extends Deployable> verticleSupplier() {
        return SimpleVerticle::new;
    }
}
