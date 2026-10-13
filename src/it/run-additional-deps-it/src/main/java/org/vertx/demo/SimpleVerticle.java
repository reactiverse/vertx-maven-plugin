package org.vertx.demo;

import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(SimpleVerticle.class);

    @Override
    public void start() throws Exception {
        logger.info("extra-deps-check-message");
        vertx.setTimer(500, l -> vertx.close());
    }
}
