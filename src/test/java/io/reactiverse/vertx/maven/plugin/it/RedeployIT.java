package io.reactiverse.vertx.maven.plugin.it;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.reactiverse.vertx.maven.plugin.it.invoker.RunningVerifier;
import org.apache.commons.io.IOUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test checking that the redeployment is triggered correctly both in Maven and in Vert.x.
 */
public class RedeployIT extends VertxMojoTestBase {

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
    public void testRedeployOnJavaChange() throws Exception {
        File testDir = initProject("projects/redeploy-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");

        // Touch the java source code
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, ImmutableMap.of("aloha", uuid));

        // Wait until we get "uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid));
    }

    @Test
    public void testRedeployOnJavaChangeWithClean() throws Exception {
        File testDir = initProject("projects/redeploy-it", "projects/redeploy-without-classes-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier, "clean");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");

        // Touch the java source code
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, ImmutableMap.of("aloha", uuid));

        // Wait until we get "uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid));
    }

    @Test
    public void testRedeployWithJvmArgs() throws Exception {
        File testDir = initProject("projects/redeploy-with-jvmArgs-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha prop");

        // Touch the java source code
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, ImmutableMap.of("aloha", uuid));

        // Wait until we get "uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid + " prop"));
    }

    @Test
    public void testRedeployOnJavaChangeWithCustomLauncher() throws Exception {
        File testDir = initProject("projects/redeploy-with-custom-launcher-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("Bonjour vert.x");

        // Touch the java source code (verticle)
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, ImmutableMap.of("vert.x", uuid));

        // Wait until we get "uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().equalsIgnoreCase("Bonjour " + uuid));

        // Touch the launcher class
        source = new File(testDir, "src/main/java/demo/Main.java");
        String uuid2 = UUID.randomUUID().toString();
        filter(source, ImmutableMap.of("Bonjour", uuid2));

        // Wait until we get "uuid uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid2 + " " + uuid));
    }

    @Test
    public void testRedeployOnJavaChangeWithExtendedLauncher() throws Exception {
        File testDir = initProject("projects/redeploy-with-extended-launcher-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier);

        String response = getHttpResponse();
        assertThat(response).isEqualTo("Buongiorno vert.x");

        // Touch the java source code (verticle)
        File source = new File(testDir, "src/main/java/demo/SimpleVerticle.java");
        String uuid = UUID.randomUUID().toString();
        filter(source, ImmutableMap.of("vert.x", uuid));

        // Wait until we get "uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().equalsIgnoreCase("Buongiorno " + uuid));

        // Touch the launcher class
        source = new File(testDir, "src/main/java/demo/Main.java");
        String uuid2 = UUID.randomUUID().toString();
        filter(source, ImmutableMap.of("Buongiorno", uuid2));

        // Wait until we get "uuid uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().equalsIgnoreCase(uuid2 + " " + uuid));
    }

    @Test
    public void testRedeployOnResourceChange() throws Exception {
        File testDir = initProject("projects/redeploy-on-resource-change-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier);

        String response = getHttpResponse();
        assertThat(response).startsWith("Hello");

        // Touch the java source code
        File source = new File(testDir, "src/main/resources/some-text.txt");
        String uuid = UUID.randomUUID().toString();
        filter(source, ImmutableMap.of("Hello", uuid));

        // Wait until we get "uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().startsWith(uuid));
    }

    @Test
    public void testRedeployOnResourceChangeManagedBySomeOtherPlugin() throws Exception {
        File testDir = initProject("projects/redeploy-with-some-plugin-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier);

        String response = getHttpResponse();
        assertThat(response).contains("color: #f938ab");

        // Touch the java source code
        File source = new File(testDir, "src/main/less/style.less");
        filter(source, ImmutableMap.of("#f938ab;", "green;"));

        // Wait until we get "uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().contains("color: #008000;"));
    }

    //FIXME - need to find a way to test this scenario for redeploy* properties in more robust pattern
    @Test
    public void testRedeployScanPeriod() throws Exception {
        File testDir = initProject("projects/redeploy-scan-period-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier);

        String response = getHttpResponse();
        assertThat(response).startsWith("Hello");

        //Start timer and check the next redeploy happens after only 300 ms secs
        // Touch the java source code
        File source = new File(testDir, "src/main/resources/some-text.txt");
        String uuid = UUID.randomUUID().toString();
        filter(source, ImmutableMap.of("Hello", uuid));

        // Wait until we get "uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().startsWith(uuid));

        source = new File(testDir, "src/main/resources/some-text.txt");
        String uuid2 = UUID.randomUUID().toString();
        filter(source, ImmutableMap.of(uuid, uuid2));

        // Wait until we get "uuid"
        await().atMost(1, TimeUnit.MINUTES).until(() -> getHttpResponse().startsWith(uuid2));

        final String redeployingLogRegex = "^(\\[INFO])\\s*INFO: Redeploying!";
        Pattern pattern = Pattern.compile(redeployingLogRegex);

        File buildLog = new File(testDir, "build-run.log");
        assertThat(buildLog).isNotNull();
        assertThat(buildLog.exists()).isTrue();
        assertThat(buildLog.isFile()).isTrue();
        List<String> lines = IOUtils.readLines(new FileReader(buildLog));
        assertThat(lines.isEmpty()).isFalse();
        long redeployCount = lines.stream()
            .filter(s -> pattern.matcher(s).matches())
            .map(pattern::matcher)
            .filter(Matcher::find)
            .count();
        assertThat(redeployCount).isEqualTo(2);
    }

    private void run(Verifier verifier) throws VerificationException {
        verifier.setLogFileName("build-run.log");

        verifier.executeGoals(ImmutableList.of("compile", "vertx:run"), getEnv());
    }

    private void run(Verifier verifier, String... previous) throws VerificationException {
        verifier.setLogFileName("build-run.log");
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add(previous);
        builder.add("vertx:run");
        verifier.executeGoals(builder.build(), getEnv());
    }

}
