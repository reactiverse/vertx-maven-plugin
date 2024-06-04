package io.reactiverse.vertx.maven.plugin.utils;

public class VertxCoreVersion {

    public static final String VALUE;

    static {
        VALUE = System.getProperty("vertx-core.version");
        if (VALUE == null) {
            throw new RuntimeException("vertx-core.version not set");
        }
    }

    private VertxCoreVersion() {
        // Constant class
    }
}
