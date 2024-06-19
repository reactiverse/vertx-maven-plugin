package io.reactiverse.vertx.maven.plugin.it;


import io.reactiverse.vertx.maven.plugin.utils.VertxCoreVersion;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class VertxMojoTestBase {
    static String VERSION;
    private static Map<String, String> VARIABLES;

    @BeforeClass
    public static void init() {
        File constants = new File("target/classes/vertx-maven-plugin.properties");
        assertThat(constants).isFile();
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(constants)) {
            properties.load(fis);
        } catch (IOException e) {
            fail("Cannot load " + constants.getAbsolutePath(), e);
        }

        VERSION = properties.getProperty("vertx-maven-plugin-version");
        assertThat(VERSION).isNotNull();

        Map<String, String> variables = new HashMap<>();
        variables.put("@project.groupId@", "io.reactiverse");
        variables.put("@project.artifactId@", "vertx-maven-plugin");
        variables.put("@project.version@", VERSION);
        variables.put("@vertx-core.version@", VertxCoreVersion.VALUE);
        VARIABLES = Collections.unmodifiableMap(variables);
    }

    boolean isCoverage() {
        return System.getProperty("coverage") != null;
    }

    static void awaitUntilServerDown() {
        await().atMost(5, TimeUnit.MINUTES).until(() -> {
            try {
                get(); // Ignore result on purpose
                return false;
            } catch (Exception e) {
                return true;
            }
        });
    }

    static String getHttpResponse() {
        AtomicReference<String> resp = new AtomicReference<>();
        await().atMost(5, TimeUnit.MINUTES).until(() -> {
            try {
                String content = get();
                resp.set(content);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        return resp.get();
    }

    public static String get() throws IOException {
        URL url = new URL("http://localhost:8080");
        return IOUtils.toString(url, StandardCharsets.UTF_8);
    }

    static File initProject(String name) {
        return initProject(name, name);
    }

    static File initEmptyProject(String name) {
        File tc = new File("target/test-classes/" + name);
        if (tc.isDirectory()) {
            boolean delete = tc.delete();
            Logger.getLogger(VertxMojoTestBase.class.getName())
                .log(Level.FINE, "test-classes deleted? " + delete);
        }
        boolean mkdirs = tc.mkdirs();
        Logger.getLogger(VertxMojoTestBase.class.getName())
            .log(Level.FINE, "test-classes created? " + mkdirs);
        return tc;
    }

    public static File initProject(String name, String output) {
        File tc = new File("target/test-classes");
        if (!tc.isDirectory()) {
            boolean mkdirs = tc.mkdirs();
            Logger.getLogger(VertxMojoTestBase.class.getName())
                .log(Level.FINE, "test-classes created? " + mkdirs);
        }

        File in = new File("src/test/resources", name);
        if (!in.isDirectory()) {
            throw new RuntimeException("Cannot find directory: " + in.getAbsolutePath());
        }

        File out = new File(tc, output);
        if (out.isDirectory()) {
            FileUtils.deleteQuietly(out);
        }
        boolean mkdirs = out.mkdirs();
        Logger.getLogger(VertxMojoTestBase.class.getName())
            .log(Level.FINE, out.getAbsolutePath() + " created? " + mkdirs);
        try {
            System.out.println("Copying " + in.getAbsolutePath() + " to " + out.getParentFile().getAbsolutePath());
            org.codehaus.plexus.util.FileUtils.copyDirectoryStructure(in, out);
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy project resources", e);
        }
        return out;
    }

    static void installPluginToLocalRepository(String local) {
        File repo = new File(local, "io/reactiverse/vertx-maven-plugin/" + VertxMojoTestBase.VERSION);
        if (!repo.isDirectory()) {
            boolean mkdirs = repo.mkdirs();
            Logger.getLogger(VertxMojoTestBase.class.getName())
                .log(Level.FINE, repo.getAbsolutePath() + " created? " + mkdirs);
        }

        File plugin = new File("target", "vertx-maven-plugin-" + VertxMojoTestBase.VERSION + ".jar");

        try {
            FileUtils.copyFileToDirectory(plugin, repo);
            String installedPomName = "vertx-maven-plugin-" + VertxMojoTestBase.VERSION + ".pom";
            FileUtils.copyFile(new File("pom.xml"), new File(repo, installedPomName));
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy the plugin jar, or the pom file, to the local repository", e);
        }
    }

    static void installJarToLocalRepository(String local, String name, File jar) {
        File repo = new File(local, "org/acme/" + name + "/1.0");
        if (!repo.isDirectory()) {
            boolean mkdirs = repo.mkdirs();
            Logger.getLogger(VertxMojoTestBase.class.getName())
                .log(Level.FINE, repo.getAbsolutePath() + " created? " + mkdirs);
        }

        try {
            FileUtils.copyFileToDirectory(jar, repo);
            String installedPomName = name + "-1.0.pom";
            FileUtils.write(new File(repo, installedPomName), "<project>\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "  <groupId>org.acme</groupId>\n" +
                "  <artifactId>" + name + "</artifactId>\n" +
                "  <version>1.0</version>\n" +
                "</project>", "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy the jar, or the pom file, to the local repository", e);
        }
    }

    static void prepareProject(File testDir, Verifier verifier) throws IOException {
        File pom = new File(testDir, "pom.xml");
        assertThat(pom).isFile();
        verifier.filterFile("pom.xml", "pom.xml", "UTF-8", VertxMojoTestBase.VARIABLES);
    }

    void filter(File input, Map<String, String> variables) throws IOException {
        assertThat(input).isFile();
        String data = FileUtils.readFileToString(input, "UTF-8");

        for (String token : variables.keySet()) {
            String value = String.valueOf(variables.get(token));
            data = StringUtils.replace(data, token, value);
        }
        FileUtils.write(input, data, "UTF-8");
    }

    void runPackage(Verifier verifier) throws VerificationException {
        verifier.setLogFileName("build-package.log");
        verifier.executeGoal("package", getEnv());

        verifier.verifyFilePresent("target/vertx-demo-start-0.0.1.BUILD-SNAPSHOT.jar");
        verifier.resetStreams();
    }

    Map<String, String> getEnv() {
        String opts = System.getProperty("mavenOpts");
        Map<String, String> env = new HashMap<>();
        if (opts != null) {
            env.put("MAVEN_OPTS", opts);
        }
        return env;
    }
}
