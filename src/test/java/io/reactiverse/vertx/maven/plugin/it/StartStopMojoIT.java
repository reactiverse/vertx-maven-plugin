package io.reactiverse.vertx.maven.plugin.it;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class StartStopMojoIT extends VertxMojoTestBase {

    String ROOT_START = "projects/start-it";
    String ROOT_OPTS = "projects/start-java-opts-it";
    String ROOT_MAIN = "projects/start-with-main-class-it";
    String ROOT_CUSTOM = "projects/start-with-custom-launcher-it";
    String ROOT_CUSTOM_EXPLODED = "projects/start-with-custom-launcher-exploded-it";
    String ROOT_MAIN_EXPLODED = "projects/start-with-main-class-exploded-it";
    String ROOT_EXPLODED = "projects/start-exploded-it";
    String ROOT_WITH_CONF = "projects/start-with-conf-it";
    String ROOT_WITH_CONF_EXPLODED = "projects/start-with-conf-exploded-it";
    private Verifier verifier;


    public void initVerifier(File root) throws VerificationException {
        verifier = new Verifier(root.getAbsolutePath());
        verifier.setAutoclean(false);

        installPluginToLocalRepository(verifier.getLocalRepository());
    }

    @After
    public void waitForStop() {
        try {
            runStop(verifier);
        } catch (VerificationException | IOException e) {
            e.printStackTrace();
        }
        verifier.resetStreams();

        awaitUntilServerDown();
    }

    @Test
    public void testJVMOptions() throws IOException, VerificationException {
        File testDir = initProject(ROOT_OPTS);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);

        prepareProject(testDir, verifier);

        runPackage(verifier);

        runStart(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("123 hello");
    }

    @Test
    public void testWithMainClass() throws IOException, VerificationException {
        File testDir = initProject(ROOT_MAIN);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        verifier.displayStreamBuffers();

        prepareProject(testDir, verifier);

        runPackage(verifier);
        runStart(verifier);
        String response = getHttpResponse();
        assertThat(response).isEqualTo("bonjour");
    }

    @Test
    public void testWithCustomLauncher() throws IOException, VerificationException {
        File testDir = initProject(ROOT_CUSTOM);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        verifier.displayStreamBuffers();

        prepareProject(testDir, verifier);

        runPackage(verifier);
        runStart(verifier);
        String response = getHttpResponse();
        assertThat(response).isEqualTo("Buongiorno");
    }

    @Test
    public void testWithMainClassInExplodedMode() throws IOException, VerificationException {
        File testDir = initProject(ROOT_MAIN_EXPLODED);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);

        prepareProject(testDir, verifier);

        runPackage(verifier);

        runStart(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("hello");
    }

    @Test
    public void testWithCustomLauncherInExplodedMode() throws IOException, VerificationException {
        File testDir = initProject(ROOT_CUSTOM_EXPLODED);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);

        prepareProject(testDir, verifier);

        runPackage(verifier);

        runStart(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("hello");
    }

    @Test
    public void testInExplodedMode() throws IOException, VerificationException {
        File testDir = initProject(ROOT_EXPLODED);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);

        prepareProject(testDir, verifier);

        runPackage(verifier);

        runStart(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("guten tag");
    }

    @Test
    public void testStartStop() throws IOException, VerificationException {
        File testDir = initProject(ROOT_START);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);

        prepareProject(testDir, verifier);

        runPackage(verifier);

        runStart(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");
    }

    @Test
    public void testStartWithConf() throws IOException, VerificationException {
        File testDir = initProject(ROOT_WITH_CONF);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);

        prepareProject(testDir, verifier);

        runPackage(verifier);

        runStart(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("Hello Vert.x");

    }

    private void runStart(Verifier verifier) throws VerificationException, IOException {
        verifier.setLogFileName("build-start.log");
        verifier.executeGoal("vertx:start", getEnv());
        assertInLog(verifier, "BUILD SUCCESS", "Starting vert.x application...");
        verifier.resetStreams();
    }

    private void runStop(Verifier verifier) throws VerificationException, IOException {
        verifier.setLogFileName("build-stop.log");
        verifier.executeGoal("vertx:stop", getEnv());
        assertInLog(verifier, "BUILD SUCCESS", "terminated with status 0");
        verifier.resetStreams();
    }

    @Test
    public void testStartWithConfExploded() throws IOException, VerificationException {
        File testDir = initProject(ROOT_WITH_CONF_EXPLODED);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);

        prepareProject(testDir, verifier);

        runPackage(verifier);

        runStart(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("Hello !");
    }

    public void assertInLog(Verifier verifier, String... snippets) throws IOException {
        File log = new File(verifier.getBasedir(), verifier.getLogFileName());
        assertThat(log).isFile();

        String content = FileUtils.readFileToString(log, "UTF-8");
        for (String snippet : snippets) {
            assertThat(content).contains(snippet);
        }
    }

}
