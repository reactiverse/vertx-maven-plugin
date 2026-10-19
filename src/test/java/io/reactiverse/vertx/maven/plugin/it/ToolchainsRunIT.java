package io.reactiverse.vertx.maven.plugin.it;

import io.reactiverse.vertx.maven.plugin.it.invoker.RunningMavenProcess;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolchainsRunIT extends VertxMojoTestBase {

    private RunningMavenProcess process;

    @After
    public void waitForStop() {
        if (process != null) {
            process.stop();
        }
        awaitUntilServerDown();
    }

    @Test
    public void testRunWithToolchain() throws Exception {
        assertThat(new File(System.getProperty("user.home"), ".m2/toolchains.xml")).exists();

        File testDir = initProject("projects/toolchain-run-it");
        assertThat(testDir).isDirectory();

        installPluginToLocalRepository(getLocalRepository());
        prepareProject(testDir);

        process = RunningMavenProcess.start(testDir.toPath(), "compile", "vertx:run");

        String response = getHttpResponse();
        assertThat(response).isEqualTo("aloha");

        assertThat(process.outputContains("Toolchain in vertx-maven-plugin:")).isTrue();
    }
}
