package io.reactiverse.vertx.maven.plugin.mojos;

import org.apache.maven.plugin.MojoExecutionException;
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
        mojo.scanAndLoadConfigs();
        assertThat(mojo.config).isFile()
            .hasName("testconfig.json");
    }

    @Test
    public void testMissingConfigFile() throws MojoExecutionException {
        AbstractRunMojo mojo = new AbstractRunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = "target/junk";
        mojo.config = new File("src/test/resources/missing.json");
        mojo.scanAndLoadConfigs();
        assertThat(mojo.config).doesNotExist();
    }

    @Test
    public void testMissingConfigYamlFile() throws MojoExecutionException {
        AbstractRunMojo mojo = new AbstractRunMojo();
        mojo.project = new MavenProject();
        mojo.projectBuildDir = "target/junk";
        mojo.config = new File("src/test/resources/missing.yml");
        mojo.scanAndLoadConfigs();
        assertThat(mojo.config).doesNotExist();
    }
}
