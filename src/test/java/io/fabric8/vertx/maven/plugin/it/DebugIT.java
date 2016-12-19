package io.fabric8.vertx.maven.plugin.it;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.vertx.maven.plugin.it.invoker.RunningVerifier;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
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

//    @Test
//    public void testRedeployOnJavaChangeWithCustomLauncher() throws Exception {
//        File testDir = initProject("projects/redeploy-with-custom-launcher-it");
//        assertThat(testDir).isDirectory();
//
//        initVerifier(testDir);
//        prepareProject(testDir, verifier);
//
//        run(verifier);
//
//        String response = getHttpResponse();
//        assertThat(response).isEqualTo("Bonjour vert.x");
//
//        // Touch the java source code (verticle)
//        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
//        String uuid = UUID.randomUUID().toString();
//        filter(source, ImmutableMap.of("vert.x", uuid));
//
//        // Wait until we get "uuid"
//        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().equalsIgnoreCase("Bonjour " + uuid));
//
//        // Touch the launcher class
//        source = new File(testDir, "src/main/java/demo/Main.java");
//        String uuid2 = UUID.randomUUID().toString();
//        filter(source, ImmutableMap.of("Bonjour", uuid2));
//
//        // Wait until we get "uuid uuid"
//        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid2 + " " + uuid));
//    }
//
//    @Test
//    public void testRedeployOnResourceChange() throws Exception {
//        File testDir = initProject("projects/redeploy-on-resource-change-it");
//        assertThat(testDir).isDirectory();
//
//        initVerifier(testDir);
//        prepareProject(testDir, verifier);
//
//        run(verifier);
//
//        String response = getHttpResponse();
//        assertThat(response).startsWith("Hello");
//
//        // Touch the java source code
//        File source = new File(testDir, "src/main/resources/some-text.txt");
//        String uuid = UUID.randomUUID().toString();
//        filter(source, ImmutableMap.of("Hello", uuid));
//
//        // Wait until we get "uuid"
//        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().startsWith(uuid));
//    }
//
//    @Test
//    public void testRedeployOnResourceChangeManagedBySomeOtherPlugin() throws Exception {
//        File testDir = initProject("projects/redeploy-with-some-plugin-it");
//        assertThat(testDir).isDirectory();
//
//        initVerifier(testDir);
//        prepareProject(testDir, verifier);
//
//        run(verifier);
//
//        String response = getHttpResponse();
//        assertThat(response).contains("color: #f938ab");
//
//        // Touch the java source code
//        File source = new File(testDir, "src/main/less/style.less");
//        filter(source, ImmutableMap.of("#f938ab;", "green;"));
//
//        // Wait until we get "uuid"
//        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().contains("color: #008000;"));
//    }

    private void debug(Verifier verifier) throws VerificationException, IOException {
        verifier.setLogFileName("build-run.log");
        verifier.executeGoals(ImmutableList.of("compile", "vertx:debug"));
    }

}
