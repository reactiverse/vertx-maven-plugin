package demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main {

    public static void main(String[] args) {
        String prefix = "Bonjour";
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(SimpleVerticle.class.getName(), new DeploymentOptions()
            .setConfig(new JsonObject().put("prefix", prefix)));
    }
}
