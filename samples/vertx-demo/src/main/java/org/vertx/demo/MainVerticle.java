package org.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.*;

public class MainVerticle extends AbstractVerticle {

	@Override
	public void start() {
		System.out.println("Hello World!");
		vertx.createHttpServer()
				.requestHandler(req -> req.response().end("Hello World 2!"))
				.listen(8040);
	}
}