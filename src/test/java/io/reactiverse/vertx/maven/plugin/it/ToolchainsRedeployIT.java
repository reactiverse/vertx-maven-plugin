package io.reactiverse.vertx.maven.plugin.it;

import io.reactiverse.vertx.maven.plugin.it.invoker.RunningMavenProcess;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ToolchainsRedeployIT extends VertxMojoTestBase {

    private RunningMavenProcess process;

    @After
    public void waitForStop() {
        if (process != null) {
            process.stop();
        }
        awaitUntilServerDown();
    }

    @Test
    public void testRedeployWithToolchain() throws Exception {
        assertThat(new File(System.getProperty("user.home"), ".m2/toolchains.xml")).exists();

        File testDir = initProject("projects/toolchain-redeploy-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");

        // Touch the java source code
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap("aloha", uuid));

        // Wait until we get the new response
        await().atMost(1, MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid));

        assertThat(process.outputContains("Toolchain in vertx-maven-plugin:")).isTrue();
    }
}
