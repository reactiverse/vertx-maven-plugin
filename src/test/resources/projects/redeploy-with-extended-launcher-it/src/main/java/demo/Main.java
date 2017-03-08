package demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main extends Launcher {

    public static void main(String[] args) {
        new Main().dispatch(args);
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        String prefix = "Buongiorno";
        deploymentOptions.setConfig(new JsonObject().put("prefix", prefix));
    }

}
