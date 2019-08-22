package io.reactiverse.vertx.maven.plugin.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class AbstractRunMojoTest {

    @Test
    public void testConversionToJson() throws MojoExecutionException {
        AbstractRunMojo mojo = new AbstractRunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = "target/junk";
        mojo.config = new File("src/test/resources/testconfig.yaml");
        mojo.config = mojo.scanAndLoad("testconfig", mojo.config);
        assertThat(mojo.config).isFile().hasName("testconfig.json");
    }

    @Test
    public void testMissingConfigFile() throws MojoExecutionException {
        AbstractRunMojo mojo = new AbstractRunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = "target/junk";
        mojo.config = new File("src/test/resources/missing.json");
        mojo.config = mojo.scanAndLoad("missing", mojo.config);
        assertThat(mojo.config).doesNotExist();
    }

    @Test
    public void testMissingConfigYamlFile() throws MojoExecutionException {
        AbstractRunMojo mojo = new AbstractRunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = "target/junk";
        mojo.config = new File("src/test/resources/missing.yml");
        mojo.config = mojo.scanAndLoad("missing", mojo.config);
        assertThat(mojo.config).doesNotExist();
    }

    @Test(expected = MojoExecutionException.class)
    public void testWithBlankVerticleAndLauncher() throws MojoFailureException, MojoExecutionException {
        AbstractRunMojo mojo = new AbstractRunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = "target/junk";
        mojo.config = new File("src/test/resources/missing.yml");
        mojo.launcher = "";
        mojo.verticle = "";
        mojo.execute();
    }

    @Test(expected = MojoExecutionException.class)
    public void testWithNullVerticleAndLauncher() throws MojoFailureException, MojoExecutionException {
        AbstractRunMojo mojo = new AbstractRunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = "target/junk";
        mojo.config = new File("src/test/resources/missing.yml");
        mojo.launcher = null;
        mojo.verticle = null;
        mojo.execute();
    }

    @Test(expected = MojoExecutionException.class)
    public void testWithDefaultLauncherButNotVerticle() throws MojoFailureException, MojoExecutionException {
        AbstractRunMojo mojo = new AbstractRunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = "target/junk";
        mojo.config = new File("src/test/resources/missing.yml");
        mojo.launcher = "io.vertx.core.Launcher";
        mojo.verticle = null;
        mojo.execute();
    }

}
