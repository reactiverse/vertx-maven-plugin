package io.reactiverse.vertx.maven.plugin.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class JavaExecutorTest {


    @Test
    public void testInitialization() {
        JavaProcessExecutor executor = new JavaProcessExecutor();
        assertThat(executor).isNotNull();
    }


}
