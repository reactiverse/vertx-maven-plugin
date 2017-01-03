package io.fabric8.vertx.maven.plugin.it;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.vertx.maven.plugin.it.invoker.RunningVerifier;
import org.apache.commons.io.IOUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test checking that the redeployment is triggered correctly both in Maven and in Vert.x.
 */
public class RunIT extends VertxMojoTestBase {

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
    public void testRunWithoutClasses() throws Exception {
        File testDir = initProject("projects/start-it", "projects/run-without-classes-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier, "clean");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");
    }

    private void run(Verifier verifier, String ... previous) throws VerificationException {
        verifier.setLogFileName("build-run.log");
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add(previous);
        builder.add("vertx:run");
        verifier.executeGoals(builder.build());
    }

}
