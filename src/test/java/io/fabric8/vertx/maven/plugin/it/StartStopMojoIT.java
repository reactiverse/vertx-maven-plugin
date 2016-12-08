package io.fabric8.vertx.maven.plugin.it;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class StartStopMojoIT {

    private static String VERSION;
    private static ImmutableMap<String, String> VARIABLES;

    String ROOT_START = "projects/start-it";
    String ROOT_OPTS = "projects/start-java-opts-it";
    String ROOT_MAIN = "projects/start-with-main-class-it";
    String ROOT_MAIN_EXPLODED = "projects/start-with-main-class-exploded-it";
    String ROOT_EXPLODED = "projects/start-exploded-it";
    String ROOT_WITH_CONF = "projects/start-with-conf-it";
    String ROOT_WITH_CONF_EXPLODED = "projects/start-with-conf-exploded-it";
    private Verifier verifier;


    @BeforeClass
    public static void init() {
        File constants = new File("target/classes/vertx-maven-plugin.properties");
        assertThat(constants.isFile());
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(constants)) {
            properties.load(fis);
        } catch (IOException e) {
            fail("Cannot load " + constants.getAbsolutePath(), e);
        }

        VERSION = properties.getProperty("vertx-maven-plugin-version");
        assertThat(VERSION).isNotNull();

        VARIABLES = ImmutableMap.of(
            "@project.groupId@", "io.fabric8",
            "@project.artifactId@", "vertx-maven-plugin",
            "@project.version@", VERSION);
    }

    public void initVerifier(File root) throws VerificationException {
        verifier = new Verifier(root.getAbsolutePath());
        verifier.setAutoclean(false);

        File repo = new File(verifier.getLocalRepository(), "io/fabric8/vertx-maven-plugin/" + VERSION);
        if (!repo.isDirectory()) {
            repo.mkdirs();
        }


        File plugin = new File("target", "vertx-maven-plugin-" + VERSION + ".jar");
        try {
            FileUtils.copyFileToDirectory(plugin, repo);
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy the plugin jar to the local repository", e);
        }
    }

    @After
    public void waitForStop() {
        verifier.resetStreams();
        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            try {
                RestAssured.get("http://localhost:8080").asString();
                return false;
            } catch (Exception e) {
                return true;
            }
        });
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

        runStop(verifier);
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

        runStop(verifier);
    }

    private void prepareProject(File testDir, Verifier verifier) throws IOException {
        File pom = new File(testDir, "pom.xml");
        assertThat(pom).isFile();
        verifier.filterFile("pom.xml", "pom.xml", "UTF-8", VARIABLES);
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

        runStop(verifier);
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

        runStop(verifier);
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

        runStop(verifier);
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

        runStop(verifier);
    }

    private void runStart(Verifier verifier) throws VerificationException, IOException {
        verifier.setLogFileName("build-start.log");
        verifier.executeGoal("vertx:start");
        assertInLog(verifier, "BUILD SUCCESS", "Starting vert.x application...");
        verifier.resetStreams();
    }

    private String getHttpResponse() {
        System.out.println("Waiting for process to be ready...");
        AtomicReference<String> resp = new AtomicReference<>();
        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            try {
                Response response = RestAssured.get("http://localhost:8080").andReturn();
                if (response.statusCode() == 200) {
                    resp.set(response.asString());
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        });
        System.out.println("Ready, got '" + resp.get() + "'");
        return resp.get();
    }

    private void runStop(Verifier verifier) throws VerificationException, IOException {
        verifier.setLogFileName("build-stop.log");
        verifier.executeGoal("vertx:stop");
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

        runStop(verifier);
    }

    private void runPackage(Verifier verifier) throws VerificationException, IOException {
        verifier.setLogFileName("build-package.log");
        verifier.executeGoal("package");

        assertInLog(verifier, "BUILD SUCCESS");
        verifier.assertFilePresent("target/vertx-demo-start-0.0.1.BUILD-SNAPSHOT.jar");
        verifier.resetStreams();
    }

    public void assertInLog(Verifier verifier, String... snippets) throws IOException {
        File log = new File(verifier.getBasedir(), verifier.getLogFileName());
        assertThat(log).isFile();

        String content = FileUtils.readFileToString(log, "UTF-8");
        for (String snippet : snippets) {
            assertThat(content).contains(snippet);
        }
    }

    public File initProject(String name) {
        File tc = new File("target/test-classes");
        if (!tc.isDirectory()) {
            tc.mkdirs();
        }

        File in = new File("src/test/resources", name);
        if (!in.isDirectory()) {
            throw new RuntimeException("Cannot find directory: " + in.getAbsolutePath());
        }

        File out = new File(tc, name);
        if (out.isDirectory()) {
            FileUtils.deleteQuietly(out);
        }
        out.mkdirs();
        try {
            System.out.println("Copying " + in.getAbsolutePath() + " to " + out.getParentFile().getAbsolutePath());
            FileUtils.copyDirectoryToDirectory(in, out.getParentFile());
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy project resources", e);
        }
        return out;
    }

}
