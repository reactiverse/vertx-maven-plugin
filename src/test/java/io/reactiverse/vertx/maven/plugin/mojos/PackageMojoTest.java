package io.reactiverse.vertx.maven.plugin.mojos;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PackageMojoTest {

    @Test
    public void testSkip() throws Exception {
        PackageMojo mojo = new PackageMojo();
        mojo.skip = true;
        mojo.execute();
        assertThat(mojo.skip).isTrue();
    }

}
