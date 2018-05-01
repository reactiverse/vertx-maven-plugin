package io.reactiverse.vertx.maven.plugin.mojos;

import org.junit.Test;

public class PackageMojoTest {

    @Test
    public void testSkip() throws Exception {
        PackageMojo mojo = new PackageMojo();
        mojo.skip = true;
        mojo.execute();
    }

}
