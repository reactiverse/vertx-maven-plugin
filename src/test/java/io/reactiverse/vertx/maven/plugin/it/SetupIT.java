package io.reactiverse.vertx.maven.plugin.it;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SetupIT extends VertxMojoTestBase {

    private Verifier verifier;
    private File testDir;

    private void initVerifier(File root) throws VerificationException {
        verifier = new Verifier(root.getAbsolutePath());
        verifier.setAutoclean(false);
        verifier.setDebug(true);
        verifier.setForkJvm(true);
        verifier.setMavenDebug(true);
        installPluginToLocalRepository(verifier.getLocalRepository());
    }

    @Test
    public void testProjectGenerationFromScratch() throws VerificationException {
        testDir = initEmptyProject("projects/project-generation");
        assertThat(testDir).isDirectory();
        initVerifier(testDir);
        setup(verifier, "-DprojectGroupId=org.acme", "-DprojectArtifactId=acme");
        assertThat(new File(testDir, "pom.xml")).isFile();
        assertThat(new File(testDir, "src/main/java")).isDirectory();
    }

    @Test
    public void testProjectGenerationFromEmptyPom() throws VerificationException, IOException {
        testDir = initProject("projects/simple-pom-it", "projects/project-generation-from-empty-pom");
        assertThat(testDir).isDirectory();
        initVerifier(testDir);
        setup(verifier, "-DprojectGroupId=org.acme", "-DprojectArtifactId=acme");
        assertThat(new File(testDir, "pom.xml")).isFile();
        assertThat(FileUtils.readFileToString(new File(testDir, "pom.xml"), "UTF-8"))
            .contains("vertx-core", "vertx-maven-plugin");
        assertThat(new File(testDir, "src/main/java")).isDirectory();
    }

    @Test
    public void testProjectGenerationFromScratchWithVerticle() throws VerificationException {
        testDir = initEmptyProject("projects/project-generation-with-verticle");
        assertThat(testDir).isDirectory();
        initVerifier(testDir);
        setup(verifier, "-DprojectGroupId=org.acme", "-DprojectArtifactId=acme",
            "-Dverticle=org.acme.MyVerticle.java");
        assertThat(new File(testDir, "pom.xml")).isFile();
        assertThat(new File(testDir, "src/main/java")).isDirectory();
        assertThat(new File(testDir, "src/main/java/org/acme/MyVerticle.java")).isFile();
    }

    @Test
    public void testProjectGenerationFromEmptyPomWithVerticle() throws VerificationException, IOException {
        testDir = initProject("projects/simple-pom-it", "projects/project-generation-from-empty-pom-with-verticle");
        assertThat(testDir).isDirectory();
        initVerifier(testDir);
        setup(verifier, "-DprojectGroupId=org.acme", "-DprojectArtifactId=acme",
            "-Dverticle=org.acme.MyVerticle.java");
        assertThat(new File(testDir, "pom.xml")).isFile();
        assertThat(FileUtils.readFileToString(new File(testDir, "pom.xml"), "UTF-8"))
            .contains("vertx-core", "vertx-maven-plugin", "<vertx.verticle>org.acme.MyVerticle</vertx.verticle>");
        assertThat(new File(testDir, "src/main/java")).isDirectory();
        assertThat(new File(testDir, "src/main/java/org/acme/MyVerticle.java")).isFile();

    }

    private void setup(Verifier verifier, String... params) throws VerificationException {
        verifier.setLogFileName("build-setup.log");
        List<String> goals = new ArrayList<>();
        goals.add("io.reactiverse:vertx-maven-plugin:" + VertxMojoTestBase.VERSION + ":setup");
        goals.addAll(Arrays.asList(params));
        verifier.executeGoals(goals, getEnv());
    }

}
