package io.reactiverse.vertx.maven.plugin.it;

import com.google.common.collect.ImmutableList;
import io.reactiverse.vertx.maven.plugin.it.invoker.RunningVerifier;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test checking the execution of vertx:debug
 */
public class DebugIT extends VertxMojoTestBase {

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
    public void testDebug() throws Exception {
        File testDir = initProject("projects/debug-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        debug(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha true");

        verifier.verifyTextInLog("Listening for transport dt_socket at address: 5005");
    }

    @Test
    public void testDebugWithJVMArgsAndCustomPort() throws Exception {
        File testDir = initProject("projects/debug-with-jvmArgs-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        debug(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha prop true");

        verifier.verifyTextInLog("Listening for transport dt_socket at address: 5000");
    }

    @Test
    public void testDebugWithMainClass() throws Exception {
        File testDir = initProject("projects/debug-with-main-class-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        debug(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("bonjour true");

        verifier.verifyTextInLog("Listening for transport dt_socket at address: 5005");
    }

    private void debug(Verifier verifier) throws VerificationException {
        verifier.setLogFileName("build-run.log");
        verifier.executeGoals(ImmutableList.of("compile", "vertx:debug"), getEnv());
    }

}
