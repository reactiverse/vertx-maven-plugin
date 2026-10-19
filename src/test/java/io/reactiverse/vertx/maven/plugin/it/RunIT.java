package io.reactiverse.vertx.maven.plugin.it;

import io.reactiverse.vertx.maven.plugin.it.invoker.RunningMavenProcess;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test checking that the redeployment is triggered correctly both in Maven and in Vert.x.
 */
public class RunIT extends VertxMojoTestBase {

    private RunningMavenProcess process;

    @After
    public void waitForStop() {
        if (process != null) {
            process.stop();
        }
        awaitUntilServerDown();
    }

    @Test
    public void testRunWithoutClasses() throws Exception {
        File testDir = initProject("projects/start-it", "projects/run-without-classes-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "clean", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");
    }
}
