package io.reactiverse.vertx.maven.plugin.it;

import io.reactiverse.vertx.maven.plugin.it.invoker.RunningVerifier;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

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

    /**
     * Check the fix for https://github.com/fabric8io/vertx-maven-plugin/issues/129.
     */
    @Test
    public void testResourceOrdering() throws Exception {
        File testDir = initProject("projects/run-filtering-it", "projects/run-resource-ordering-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier, "clean");

        String response = getHttpResponse();
        assertThat(response).contains("aloha").contains("This file");
    }

    @Test
    public void testRunAppUsingVertxSnapshotVersion() throws Exception {
        assumeThat(javaSpecVersion()).isGreaterThanOrEqualTo(11);

        File testDir = initProject("projects/run-vertx-snapshot-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier, "clean");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");
    }

    private int javaSpecVersion() {
        String property = System.getProperty("java.specification.version");
        assumeThat(property).withFailMessage("java.specification.version is null").isNotNull();
        assumeThat(property).withFailMessage("%s is not a number", property).matches("\\d+");
        return Integer.parseInt(property);
    }

    private void run(Verifier verifier, String ... previous) throws VerificationException {
        verifier.setLogFileName("build-run.log");
        List<String> goals = new ArrayList<>(Arrays.asList(previous));
        goals.add("vertx:run");
        verifier.executeGoals(goals, getEnv());
    }

}
