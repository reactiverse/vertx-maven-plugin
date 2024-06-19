package io.reactiverse.vertx.maven.plugin.mojos;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Check the {@link FileSet} class
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FileSetTest {

    @Test
    public void test() {
        FileSet set = new FileSet()
            .setDirectory("directory")
            .setExcludes(Arrays.asList("a", "b"))
            .setIncludes(Arrays.asList("c", "d"))
            .setOutputDirectory("output")
            .setUseDefaultExcludes(true);

        assertThat(set.getDirectory()).isEqualTo("directory");
        assertThat(set.getExcludes()).isEqualTo(Arrays.asList("a", "b"));
        assertThat(set.getIncludes()).isEqualTo(Arrays.asList("c", "d"));
        assertThat(set.getOutputDirectory()).isEqualTo("output");
        assertThat(set.isUseDefaultExcludes()).isTrue();
    }
}
