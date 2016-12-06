/*
 *
 *   Copyright (c) 2016 Red Hat, Inc.
 *
 *   Red Hat licenses this file to you under the Apache License, version
 *   2.0 (the "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *   implied.  See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package io.fabric8.vertx.maven.plugin;

import io.fabric8.vertx.maven.plugin.utils.ServiceCombinerUtil;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.Assert.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author kameshs
 */
public class SPICombineTest {


    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

    @Before
    public void setup() throws Exception {


    }

    @Test
    public void testCombine() throws Exception {

        File outputJar = new File("target/testCombine-spi.jar");
        File jar1 = new File("target/testCombine1.jar");
        File jar2 = new File("target/testCombine2.jar");
        File jar3 = new File("target/testCombine3.jar");
        File jar4 = new File("target/testCombine4.jar");

        JavaArchive jarArchive1 = ShrinkWrap.create(JavaArchive.class);
        jarArchive1.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl");

        jarArchive1.as(ZipExporter.class).exportTo(jar1, true);


        JavaArchive jarArchive2 = ShrinkWrap.create(JavaArchive.class);
        jarArchive2.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl2");
        jarArchive2.addAsServiceProvider("com.test.demo.DemoSP2",
            "com.test.demo.DemoSPI2.impl.DemoSPI2Impl2");
        jarArchive2.as(ZipExporter.class).exportTo(jar2, true);

        JavaArchive jarArchive3 = ShrinkWrap.create(JavaArchive.class);
        jarArchive3.addClass(SPICombineTest.class);
        jarArchive3.as(ZipExporter.class).exportTo(jar3, true);

        JavaArchive jarArchive4 = ShrinkWrap.create(JavaArchive.class);
        jarArchive4.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl");

        jarArchive4.as(ZipExporter.class).exportTo(jar4, true);

        JavaArchive archive1 = ShrinkWrap.createFromZipFile(JavaArchive.class, jar1);
        JavaArchive archive2 = ShrinkWrap.createFromZipFile(JavaArchive.class, jar2);
        JavaArchive archive3 = ShrinkWrap.createFromZipFile(JavaArchive.class, jar3);
        JavaArchive archive4 = ShrinkWrap.createFromZipFile(JavaArchive.class, jar4);

        List<JavaArchive> jars = Stream.of(archive1, archive2,
            archive3, archive4).collect(Collectors.toList());

        JavaArchive combinedSpiArchive = new ServiceCombinerUtil()
            .withLog(new SystemStreamLog())
            .withProject("foo", "1.0")
            .combine(jars);
        combinedSpiArchive.as(ZipExporter.class).exportTo(outputJar, true);

        assertNotNull(combinedSpiArchive);

        String expected = "com.test.demo.DemoSPI.impl.DemoSPIImpl2\n" +
            "com.test.demo.DemoSPI.impl.DemoSPIImpl";

        assertTrue(Files.exists(Paths.get(outputJar.toString())));

        JavaArchive acutalOutput = ShrinkWrap.create(JavaArchive.class);
        acutalOutput.as(ZipImporter.class).importFrom(outputJar);

        Node outputNode = acutalOutput.get("/META-INF/services/com.test.demo.DemoSPI");

        InputStream in = outputNode.getAsset().openStream();
        String strContent = read(in);
        in.close();

        assertEquals(expected, strContent);

        Stream.of(jar1, jar2, jar3, jar4, outputJar).forEach(File::delete);
    }

    @Test
    public void testCombineDiffSPI() throws Exception {

        File outputJar = new File("target/testCombineDiff-spi.jar");
        File jar1 = new File("target/testCombineDiffSPI.jar");
        File jar2 = new File("target/testCombineDiffSPI2.jar");
        File jar3 = new File("target/testCombineDiffSPI3.jar");
        File jar4 = new File("target/testCombineDiffSPI4.jar");

        JavaArchive jarArchive1 = ShrinkWrap.create(JavaArchive.class);
        jarArchive1.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl");

        jarArchive1.as(ZipExporter.class).exportTo(jar1, true);


        JavaArchive jarArchive2 = ShrinkWrap.create(JavaArchive.class);
        jarArchive2.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl2");
        jarArchive2.as(ZipExporter.class).exportTo(jar2, true);

        JavaArchive jarArchive3 = ShrinkWrap.create(JavaArchive.class);
        jarArchive3.addClass(SPICombineTest.class);
        jarArchive3.as(ZipExporter.class).exportTo(jar3, true);

        JavaArchive jarArchive4 = ShrinkWrap.create(JavaArchive.class);
        jarArchive4.addAsServiceProvider("com.test.demo.DemoSPI",
            "com.test.demo.DemoSPI.impl.DemoSPIImpl4");
        jarArchive4.as(ZipExporter.class).exportTo(jar4, true);


        JavaArchive archive1 = ShrinkWrap.createFromZipFile(JavaArchive.class, jar1);
        JavaArchive archive2 = ShrinkWrap.createFromZipFile(JavaArchive.class, jar2);
        JavaArchive archive3 = ShrinkWrap.createFromZipFile(JavaArchive.class, jar3);
        JavaArchive archive4 = ShrinkWrap.createFromZipFile(JavaArchive.class, jar4);

        List<JavaArchive> jars = Stream.of(archive1, archive2,
            archive3, archive4).collect(Collectors.toList());

        JavaArchive combinedSpiArchive = new ServiceCombinerUtil()
            .withLog(new SystemStreamLog())
            .combine(jars);
        combinedSpiArchive.as(ZipExporter.class).exportTo(outputJar, true);

        assertNotNull(combinedSpiArchive);
        assertTrue(Files.exists(Paths.get(outputJar.toString())));

        String expected = "com.test.demo.DemoSPI.impl.DemoSPIImpl2\n" +
            "com.test.demo.DemoSPI.impl.DemoSPIImpl\n" +
            "com.test.demo.DemoSPI.impl.DemoSPIImpl4";

        JavaArchive acutalOutput = ShrinkWrap.create(JavaArchive.class);
        acutalOutput.as(ZipImporter.class).importFrom(outputJar);

        Node outputNode = acutalOutput.get("/META-INF/services/com.test.demo.DemoSPI");

        List<String> lines = IOUtils.readLines(outputNode.getAsset().openStream(), "UTF-8");

        assertThat(lines).hasSize(3).contains("com.test.demo.DemoSPI.impl.DemoSPIImpl2", "com.test.demo.DemoSPI.impl" +
            ".DemoSPIImpl", "com.test.demo.DemoSPI.impl.DemoSPIImpl4");

        Stream.of(jar1, jar2, jar3, jar4, outputJar).forEach(File::delete);
    }
}
