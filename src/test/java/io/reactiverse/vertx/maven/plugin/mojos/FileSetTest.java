package io.reactiverse.vertx.maven.plugin.mojos;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

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
            .setExcludes(ImmutableList.of("a", "b"))
            .setIncludes(ImmutableList.of("c", "d"))
            .setOutputDirectory("output")
            .setUseDefaultExcludes(true);

        assertThat(set.getDirectory()).isEqualTo("directory");
        assertThat(set.getExcludes()).isEqualTo(ImmutableList.of("a", "b"));
        assertThat(set.getIncludes()).isEqualTo(ImmutableList.of("c", "d"));
        assertThat(set.getOutputDirectory()).isEqualTo("output");
        assertThat(set.isUseDefaultExcludes()).isTrue();
    }
}
