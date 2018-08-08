package org.vertx.demo;

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router

class MyKotlinVerticle : AbstractVerticle() {

    override fun start() {

        val server = vertx.createHttpServer()

        val router = Router.router(vertx)

        router.route().handler({ routingContext ->
            val response = routingContext.response()
            response.putHeader("content-type", "text/plain")
            response.end("Hello World from Kotlin!")
        })
        server.requestHandler({ router.accept(it) }).listen(8080)

    }

}
