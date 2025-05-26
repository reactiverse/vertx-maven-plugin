package demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.launcher.application.*;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main extends VertxApplication implements VertxApplicationHooks {

    public static void main(String[] args) {
        Main app = new Main(args);
        app.launch();
    }

    public Main(String[] args) {
        super(args);
    }

    @Override
    public void beforeDeployingVerticle(HookContext context) {
        String prefix = "Buongiorno";
        context.deploymentOptions().setConfig(new JsonObject().put("prefix", prefix));
    }

    @Override
    public void afterVerticleDeployed(HookContext context) {
        System.out.println("Hooray!");
    }
}
