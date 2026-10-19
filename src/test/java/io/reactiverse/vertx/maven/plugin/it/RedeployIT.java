package io.reactiverse.vertx.maven.plugin.it;

import io.reactiverse.vertx.maven.plugin.it.invoker.RunningMavenProcess;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Test checking that the redeployment is triggered correctly both in Maven and in Vert.x.
 */
public class RedeployIT extends VertxMojoTestBase {

    private RunningMavenProcess process;

    @After
    public void waitForStop() {
        if (process != null) {
            process.stop();
        }
        awaitUntilServerDown();
    }

    @Test
    public void testRedeployOnJavaChange() throws Exception {
        File testDir = initProject("projects/redeploy-it");
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

        // Wait until we get "uuid"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid));
    }

    @Test
    public void testRedeployDisabled() throws Exception {
        File testDir = initProject("projects/redeploy-disabled-it");
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

        Thread.sleep(5000);
        assertThat(getHttpResponse()).isEqualToIgnoringCase("aloha");
    }

    @Test
    public void testRedeployOnJavaChangeWithClean() throws Exception {
        File testDir = initProject("projects/redeploy-it", "projects/redeploy-without-classes-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "clean", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");

        // Touch the java source code
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap("aloha", uuid));

        // Wait until we get "uuid"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid));
    }

    @Test
    public void testRedeployWithJvmArgs() throws Exception {
        File testDir = initProject("projects/redeploy-with-jvmArgs-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha prop");

        // Touch the java source code
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap("aloha", uuid));

        // Wait until we get "uuid"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid + " prop"));
    }

    @Test
    public void testRedeployOnJavaChangeWithCustomLauncher() throws Exception {
        File testDir = initProject("projects/redeploy-with-custom-launcher-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("Bonjour vert.x");

        // Touch the java source code (verticle)
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap("vert.x", uuid));

        // Wait until we get "uuid"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().equalsIgnoreCase("Bonjour " + uuid));

        // Touch the launcher class
        source = new File(testDir, "src/main/java/demo/Main.java");
        String uuid2 = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap("Bonjour", uuid2));

        // Wait until we get "uuid uuid"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid2 + " " + uuid));
    }

    @Test
    public void testRedeployOnJavaChangeWithExtendedLauncher() throws Exception {
        File testDir = initProject("projects/redeploy-with-extended-launcher-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("Buongiorno vert.x");

        // Touch the java source code (verticle)
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap("vert.x", uuid));

        // Wait until we get "uuid"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().equalsIgnoreCase("Buongiorno " + uuid));

        // Touch the launcher class
        source = new File(testDir, "src/main/java/demo/Main.java");
        String uuid2 = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap("Buongiorno", uuid2));

        // Wait until we get "uuid uuid"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid2 + " " + uuid));
    }

    @Test
    public void testRedeployOnResourceChange() throws Exception {
        File testDir = initProject("projects/redeploy-on-resource-change-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).startsWith("Hello");

        // Touch the resource file
        File source = new File(testDir, "src/main/resources/some-text.txt");
        String uuid = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap("Hello", uuid));

        // Wait until we get "uuid"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().startsWith(uuid));
    }

    @Test
    public void testRedeployOnResourceChangeManagedBySomeOtherPlugin() throws Exception {
        File testDir = initProject("projects/redeploy-with-some-plugin-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).contains("color: #f938ab");

        // Touch the less source
        File source = new File(testDir, "src/main/less/style.less");
        filter(source, Collections.singletonMap("#f938ab;", "green;"));

        // Wait until we get "uuid"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().contains("color: #008000;"));
    }

    @Test
    public void testRedeployScanPeriod() throws Exception {
        File testDir = initProject("projects/redeploy-scan-period-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).startsWith("Hello");

        // Touch the resource file
        File source = new File(testDir, "src/main/resources/some-text.txt");
        String uuid = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap("Hello", uuid));

        // Wait until we get "uuid"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().startsWith(uuid));

        source = new File(testDir, "src/main/resources/some-text.txt");
        String uuid2 = UUID.randomUUID().toString();
        filter(source, Collections.singletonMap(uuid, uuid2));

        // Wait until we get "uuid2"
        await().atMost(1, MINUTES).until(() -> getHttpResponse().startsWith(uuid2));

        long redeployCount = Arrays.stream(process.getOutput().split(System.lineSeparator()))
            .filter(s -> s.contains("Redeploying Vert.x Application"))
            .count();
        assertThat(redeployCount).isEqualTo(2);
    }
}
