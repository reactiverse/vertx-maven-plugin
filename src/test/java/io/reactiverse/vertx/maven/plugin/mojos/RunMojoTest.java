package io.reactiverse.vertx.maven.plugin.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RunMojoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File projectBuildDir;

    @Before
    public void setUp() throws Exception {
        projectBuildDir = temporaryFolder.newFolder();
    }

    @Test
    public void testConversionToJson() throws MojoExecutionException {
        RunMojo mojo = new RunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = projectBuildDir.getAbsolutePath();
        mojo.config = new File("src/test/resources/testconfig.yaml");
        assertThat(mojo.scanAndLoad("testconfig", mojo.config))
            .isFile()
            .hasName("testconfig.json");
    }

    @Test
    public void testMissingConfigFile() throws MojoExecutionException {
        RunMojo mojo = new RunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = projectBuildDir.getAbsolutePath();
        mojo.config = new File("src/test/resources/missing.json");
        assertThat(mojo.scanAndLoad("missing", mojo.config)).doesNotExist();
    }

    @Test
    public void testMissingConfigYamlFile() throws MojoExecutionException {
        RunMojo mojo = new RunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = projectBuildDir.getAbsolutePath();
        mojo.config = new File("src/test/resources/missing.yml");
        mojo.config = mojo.scanAndLoad("missing", mojo.config);
        assertThat(mojo.config).doesNotExist();
    }
}
