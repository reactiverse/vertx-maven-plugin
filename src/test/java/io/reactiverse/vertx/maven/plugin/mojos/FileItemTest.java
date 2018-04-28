package io.reactiverse.vertx.maven.plugin.mojos;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the {@link FileItem} class
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FileItemTest {


    @Test
    public void test() {
        FileItem item = new FileItem()
            .setDestName("destination")
            .setOutputDirectory("output")
            .setSource("source");
        assertThat(item.getDestName()).isEqualTo("destination");
        assertThat(item.getOutputDirectory()).isEqualTo("output");
        assertThat(item.getSource()).isEqualTo("source");
    }

}
