package io.fabric8.vertx.maven.plugin.it;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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

    @Test
    public void testProjectGenerationFromScratchWithDependencies() throws VerificationException, IOException {
        testDir = initEmptyProject("projects/project-generation-with-verticle-and-deps");
        assertThat(testDir).isDirectory();
        initVerifier(testDir);
        setup(verifier, "-DprojectGroupId=org.acme", "-DprojectArtifactId=acme",
            "-Dverticle=org.acme.MyVerticle.java", "-Ddependencies=vertx-web,jmx,missing");
        assertThat(new File(testDir, "pom.xml")).isFile();
        assertThat(new File(testDir, "src/main/java")).isDirectory();
        assertThat(new File(testDir, "src/main/java/org/acme/MyVerticle.java")).isFile();
        assertThat(FileUtils.readFileToString(new File(testDir, "pom.xml"), "UTF-8"))
            .contains("vertx-web", "vertx-dropwizard-metrics").doesNotContain("missing");
    }

    @Test
    public void testProjectGenerationFromScratchWithCustomDependencies() throws VerificationException, IOException {
        testDir = initEmptyProject("projects/project-generation-with-verticle-and-custom-deps");
        assertThat(testDir).isDirectory();
        initVerifier(testDir);
        setup(verifier, "-DprojectGroupId=org.acme", "-DprojectArtifactId=acme",
            "-Dverticle=org.acme.MyVerticle.java", "-Ddependencies=io.vertx:codetrans,commons-io:commons-io:2.5,io" +
                ".vertx:vertx-template-engines:3.4.1:shaded");
        assertThat(new File(testDir, "pom.xml")).isFile();
        assertThat(new File(testDir, "src/main/java")).isDirectory();
        assertThat(new File(testDir, "src/main/java/org/acme/MyVerticle.java")).isFile();
        assertThat(FileUtils.readFileToString(new File(testDir, "pom.xml"), "UTF-8"))
            .contains("<artifactId>codetrans</artifactId>")
            .contains("<artifactId>commons-io</artifactId>", "<version>2.5</version>", "<groupId>commons-io</groupId>")
            .contains("<artifactId>vertx-template-engines</artifactId>", "<version>3.4.1</version>",
                "<classifier>shaded</classifier>");;
    }

    @Test
    public void testProjectGenerationFromEmptyPomWithDependencies() throws VerificationException, IOException {
        testDir = initProject("projects/simple-pom-it",
            "projects/project-generation-from-empty-pom-with-dependencies");
        assertThat(testDir).isDirectory();
        initVerifier(testDir);
        setup(verifier, "-DprojectGroupId=org.acme", "-DprojectArtifactId=acme",
            "-Dverticle=org.acme.MyVerticle.java", "-Ddependencies=vertx-web,jmx,missing");
        assertThat(new File(testDir, "pom.xml")).isFile();
        assertThat(FileUtils.readFileToString(new File(testDir, "pom.xml"), "UTF-8"))
            .contains("vertx-core", "vertx-maven-plugin", "<vertx.verticle>org.acme.MyVerticle</vertx.verticle>")
            .contains("vertx-web", "vertx-dropwizard-metrics").doesNotContain("missing");
        assertThat(new File(testDir, "src/main/java")).isDirectory();
        assertThat(new File(testDir, "src/main/java/org/acme/MyVerticle.java")).isFile();

    }

    private void setup(Verifier verifier, String... params) throws VerificationException {
        verifier.setLogFileName("build-setup.log");
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add("io.fabric8:vertx-maven-plugin:" + VertxMojoTestBase.VERSION + ":setup");
        for (String p : params) {
            builder.add(p);
        }
        verifier.executeGoals(builder.build());
    }

}
