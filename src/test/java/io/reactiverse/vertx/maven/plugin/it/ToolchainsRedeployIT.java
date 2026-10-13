package io.reactiverse.vertx.maven.plugin.it;

import io.reactiverse.vertx.maven.plugin.it.invoker.RunningVerifier;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ToolchainsRedeployIT extends VertxMojoTestBase {

    private RunningVerifier verifier;

    private void initVerifier(File root) throws VerificationException {
        verifier = new RunningVerifier(root.getAbsolutePath());
        verifier.setAutoclean(false);
        verifier.setDebug(true);
        verifier.setForkJvm(true);
        verifier.setMavenDebug(true);
        installPluginToLocalRepository(verifier.getLocalRepository());
    }

    @After
    public void waitForStop() {
        if (verifier != null) {
            verifier.stop();
        }
        awaitUntilServerDown();
    }

    @Test
    public void testRedeployWithToolchain() throws Exception {
        assertThat(new File(System.getProperty("user.home"), ".m2/toolchains.xml")).exists();

        File testDir = initProject("projects/toolchain-redeploy-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");

        // Touch the java source code
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap("aloha", uuid));

        // Wait until we get the new response
        await().atMost(1, MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid));

        File buildLog = new File(testDir, "build-run.log");
        assertThat(buildLog).content().contains("Toolchain in vertx-maven-plugin:");
    }

    private void run(Verifier verifier) throws VerificationException {
        verifier.setLogFileName("build-run.log");
        verifier.executeGoals(Arrays.asList("compile", "vertx:run"), getEnv());
    }
}
