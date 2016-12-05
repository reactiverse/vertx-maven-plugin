package io.fabric8.vertx.maven.plugin.mojos;

import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@Mojo(name = "initialize",
    defaultPhase = LifecyclePhase.INITIALIZE,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class InitializeMojo extends AbstractMojo {

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MavenExecutionRequest request = session.getRequest();
        getLog().info("Request : " + request);
        ExecutionListener listener = request.getExecutionListener();
        request.setExecutionListener(new MojoSpy(listener, getLog()));
    }
}
