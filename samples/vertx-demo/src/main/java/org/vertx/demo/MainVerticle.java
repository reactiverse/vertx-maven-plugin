package org.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.*;

public class MainVerticle extends AbstractVerticle {

	@Override
	public void start() {
		System.out.println("Hello World 23!");
		vertx.createHttpServer()
				.requestHandler(req -> req.response().end("Hello World, it works !"))
				.listen(8040);
	}
}