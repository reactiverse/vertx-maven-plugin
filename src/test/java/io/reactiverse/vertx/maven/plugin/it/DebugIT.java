package io.reactiverse.vertx.maven.plugin.it;

import io.reactiverse.vertx.maven.plugin.it.invoker.RunningMavenProcess;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Test checking the execution of vertx:debug
 */
public class DebugIT extends VertxMojoTestBase {

    private RunningMavenProcess process;

    @After
    public void waitForStop() {
        if (process != null) {
            process.stop();
        }
        awaitUntilServerDown();
    }

    @Test
    public void testDebug() throws Exception {
        File testDir = initProject("projects/debug-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");

        await().atMost(1, MINUTES).until(() -> process.outputContains("Listening for transport dt_socket at address: 5005"));
    }

    @Test
    public void testDebugWithJVMArgsAndCustomPort() throws Exception {
        File testDir = initProject("projects/debug-with-jvmArgs-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha prop");

        await().atMost(1, MINUTES).until(() -> process.outputContains("Listening for transport dt_socket at address: 5000"));
    }

    @Test
    public void testDebugWithMainClass() throws Exception {
        File testDir = initProject("projects/debug-with-main-class-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("bonjour");

        await().atMost(1, MINUTES).until(() -> process.outputContains("Listening for transport dt_socket at address: 5005"));
    }
}
