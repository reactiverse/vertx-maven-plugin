package io.fabric8.vertx.maven.plugin.it;


import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.it.Verifier;
import org.apache.maven.shared.utils.StringUtils;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class VertxMojoTestBase {
    static String VERSION;
    static ImmutableMap<String, String> VARIABLES;

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

    static void awaitUntilServerDown() {
        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            try {
                String resp = get();
                return false;
            } catch (Exception e) {
                return true;
            }
        });
    }

    static String getHttpResponse() {
        AtomicReference<String> resp = new AtomicReference<>();
        await().atMost(1, TimeUnit.MINUTES).until(() -> {
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
        return IOUtils.toString(url, "UTF-8");
    }

    public static File initProject(String name) {
        return initProject(name, name);
    }

    public static File initProject(String name, String output) {
        File tc = new File("target/test-classes");
        if (!tc.isDirectory()) {
            tc.mkdirs();
        }

        File in = new File("src/test/resources", name);
        if (!in.isDirectory()) {
            throw new RuntimeException("Cannot find directory: " + in.getAbsolutePath());
        }

        File out = new File(tc, output);
        if (out.isDirectory()) {
            FileUtils.deleteQuietly(out);
        }
        out.mkdirs();
        try {
            System.out.println("Copying " + in.getAbsolutePath() + " to " + out.getParentFile().getAbsolutePath());
            FileUtils.copyDirectoryToDirectory(in, out.getParentFile());
            File x = new File(out.getParentFile(), in.getName());
            if (! x.getName().equals(out.getName())) {
                x.renameTo(out);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy project resources", e);
        }
        return out;
    }

    static void installPluginToLocalRepository(String local) {
        File repo = new File(local, "io/fabric8/vertx-maven-plugin/" + VertxMojoTestBase.VERSION);
        if (!repo.isDirectory()) {
            repo.mkdirs();
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

    static void prepareProject(File testDir, Verifier verifier) throws IOException {
        File pom = new File(testDir, "pom.xml");
        assertThat(pom).isFile();
        verifier.filterFile("pom.xml", "pom.xml", "UTF-8", VertxMojoTestBase.VARIABLES);
    }

    public File filter(File input, Map<String, String> variables) throws IOException {
        assertThat(input).isFile();
        String data = FileUtils.readFileToString(input, "UTF-8");

        for (String token : variables.keySet()) {
            String value = String.valueOf(variables.get(token));
            data = StringUtils.replace(data, token, value);
        }
        FileUtils.write(input, data, "UTF-8");
        return input;
    }
}
