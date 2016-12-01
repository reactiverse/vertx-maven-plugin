package io.fabric8.vertx.maven.plugin;

import io.fabric8.vertx.maven.plugin.utils.FileUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

/**
 * @author kameshs
 */

public class FileUtilsTest {

    FileOutputStream fileOut;
    File jar1;

    @Before
    public void setup() throws Exception {
        jar1 = new File("target/fileutils.jar");
        fileOut = new FileOutputStream(jar1);
    }

    @Test
    public void testBackup() throws Exception {

        JavaArchive jarArchive1 = ShrinkWrap.create(JavaArchive.class);
        jarArchive1.addAsServiceProvider("com.test.demo.DemoSPI",
                "com.test.demo.DemoSPI.impl.DemoSPIImpl");

        jarArchive1.as(ZipExporter.class).exportTo(fileOut);

        assertNotNull(Files.exists(Paths.get(jar1.toURI())));

        FileUtils.backup(jar1, new File("target"));

        assertNotNull(Files.exists(Paths.get(jar1.toString() + ".original")));

    }

    @After
    public void tearDown() throws Exception {
        fileOut.close();
        fileOut = null;
        jar1.delete();
    }
}
