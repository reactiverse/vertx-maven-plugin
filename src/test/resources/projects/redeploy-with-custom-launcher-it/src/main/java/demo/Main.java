package demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.launcher.commands.BareCommand;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String prefix = "Bonjour";
        Vertx vertx = Vertx.vertx();
        Runtime.getRuntime().addShutdownHook(new Thread(BareCommand.getTerminationRunnable(vertx, log, null)));

        vertx.deployVerticle(SimpleVerticle.class.getName(), new DeploymentOptions()
            .setConfig(new JsonObject().put("prefix", prefix)));
    }
}
