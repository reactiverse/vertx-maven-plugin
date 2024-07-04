package io.reactiverse.vertx.maven.plugin.components.impl;

import io.reactiverse.vertx.maven.plugin.components.PackageConfig;
import io.reactiverse.vertx.maven.plugin.components.PackageType;
import io.reactiverse.vertx.maven.plugin.components.PackagingException;
import io.reactiverse.vertx.maven.plugin.mojos.*;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the behavior of the {@link ShrinkWrapFatJarPackageService}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ShrinkWrapFatJarPackageServiceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File out;
    private ShrinkWrapFatJarPackageService service;

    @Before
    public void setUp() throws Exception {
        out = temporaryFolder.newFolder();
        service = new ShrinkWrapFatJarPackageService();
    }

    @Test
    public void checkPackagingType() {
        assertThat(service.type()).isEqualTo(PackageType.FAT_JAR);
    }

    @Test
    public void testEmpty() throws PackagingException, IOException {
        AbstractVertxMojo mojo = mock(AbstractVertxMojo.class);
        when(mojo.getLog()).thenReturn(new SystemStreamLog());

        Archive archive = new Archive();
        archive.setIncludeClasses(false);

        File output = new File(out, "test-empty.jar");
        PackageConfig config = new PackageConfig()
            .setMojo(mojo)
            .setOutput(output)
            .setArchive(archive);
        service.doPackage(config);

        assertThat(output).isFile();
        List<String> list;
        try (JarFile jar = new JarFile(output)) {
            list = jar.stream().map(ZipEntry::getName)
                .filter(s -> !s.endsWith("/")) // Directories
                .collect(Collectors.toList());
        }
        assertThat(list).containsExactly("META-INF/MANIFEST.MF");
    }

    @Test
    public void testEmbeddingDependencies() throws IOException, PackagingException {
        AbstractVertxMojo mojo = mock(AbstractVertxMojo.class);
        when(mojo.getLog()).thenReturn(new SystemStreamLog());

        Archive archive = new Archive();
        archive.setIncludeClasses(false);
        archive.setDependencySets(Collections.singletonList(new DependencySet()));

        Set<Artifact> artifacts = Stream.of(getFirstArtifact(), getSecondArtifact()).collect(toSet());

        File output = new File(out, "test-all-dependencies.jar");
        PackageConfig config = new PackageConfig()
            .setMojo(mojo)
            .setOutput(output)
            .setArtifacts(artifacts)
            .setArchive(archive);
        service.doPackage(config);

        assertThat(output).isFile();
        List<String> list;
        try (JarFile jar = new JarFile(output)) {
            list = jar.stream().map(ZipEntry::getName)
                .filter(s -> !s.endsWith("/")) // Directories
                .collect(Collectors.toList());
        }
        assertThat(list).containsOnly("META-INF/MANIFEST.MF", "testconfig.yaml", "out/some-config.yaml");
    }

    @Test
    public void testEmbeddingDependenciesWithAMissingArtifactFile() throws IOException, PackagingException {
        AbstractVertxMojo mojo = mock(AbstractVertxMojo.class);
        when(mojo.getLog()).thenReturn(new SystemStreamLog());

        Archive archive = new Archive();
        archive.setIncludeClasses(false);
        archive.setDependencySets(Collections.singletonList(new DependencySet()));

        DefaultArtifact artifact = getSecondArtifact();
        artifact.setFile(new File("missing-on-purpose"));
        Set<Artifact> artifacts = Stream.of(getFirstArtifact(), artifact).collect(toSet());

        File output = new File(out, "test-all-dependencies-missing-artifact-file.jar");
        PackageConfig config = new PackageConfig()
            .setMojo(mojo)
            .setOutput(output)
            .setArtifacts(artifacts)
            .setArchive(archive);
        service.doPackage(config);

        assertThat(output).isFile();
        List<String> list;
        try (JarFile jar = new JarFile(output)) {
            list = jar.stream().map(ZipEntry::getName)
                .filter(s -> !s.endsWith("/")) // Directories
                .collect(Collectors.toList());
        }
        assertThat(list).containsOnly("META-INF/MANIFEST.MF", "testconfig.yaml");
    }

    private DefaultArtifact getFirstArtifact() {
        JavaArchive jarArchive1 = ShrinkWrap.create(JavaArchive.class);
        jarArchive1.addAsResource(new File("src/test/resources/testconfig.yaml"));
        File jar1 = new File(out, "jar1.jar");
        jarArchive1.as(ZipExporter.class).exportTo(jar1, true);

        DefaultArtifact artifact = new DefaultArtifact("org.acme", "jar1", "1.0", "compile", "jar", "", null);
        artifact.setFile(jar1);
        return artifact;
    }

    private DefaultArtifact getSecondArtifact() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.addAsResource(new File("src/test/resources/testconfig2.yaml"), "out/some-config.yaml");
        File jar = new File(out, "jar2.jar");
        archive.as(ZipExporter.class).exportTo(jar, true);

        DefaultArtifact artifact = new DefaultArtifact("org.acme", "jar2", "1.0", "compile", "jar", "", null);
        artifact.setFile(jar);
        return artifact;
    }

    @Test
    public void testEmbeddingDependenciesUsingInclusion() throws IOException, PackagingException {
        AbstractVertxMojo mojo = mock(AbstractVertxMojo.class);
        when(mojo.getLog()).thenReturn(new SystemStreamLog());

        Archive archive = new Archive();
        archive.setIncludeClasses(false);
        archive.setDependencySets(Collections.singletonList(new DependencySet().addInclude("org.acme:jar1")));

        Set<Artifact> artifacts = Stream.of(getFirstArtifact(), getSecondArtifact()).collect(toSet());

        File output = new File(out, "test-inclusion.jar");
        PackageConfig config = new PackageConfig()
            .setMojo(mojo)
            .setOutput(output)
            .setArtifacts(artifacts)
            .setArchive(archive);
        service.doPackage(config);

        assertThat(output).isFile();
        List<String> list;
        try (JarFile jar = new JarFile(output)) {
            list = jar.stream().map(ZipEntry::getName)
                .filter(s -> !s.endsWith("/")) // Directories
                .collect(Collectors.toList());
        }
        assertThat(list).contains("META-INF/MANIFEST.MF", "testconfig.yaml").hasSize(2);
    }

    @Test
    public void testEmbeddingDependenciesUsingExclusion() throws IOException, PackagingException {
        AbstractVertxMojo mojo = mock(AbstractVertxMojo.class);
        when(mojo.getLog()).thenReturn(new SystemStreamLog());

        Archive archive = new Archive();
        archive.setIncludeClasses(false);
        archive.setDependencySets(Collections.singletonList(new DependencySet().addExclude("org.acme:jar2")));

        Set<Artifact> artifacts = Stream.of(getFirstArtifact(), getSecondArtifact()).collect(toSet());

        File output = new File(out, "test-exclusion.jar");
        PackageConfig config = new PackageConfig()
            .setMojo(mojo)
            .setOutput(output)
            .setArtifacts(artifacts)
            .setArchive(archive);
        service.doPackage(config);

        assertThat(output).isFile();
        List<String> list;
        try (JarFile jar = new JarFile(output)) {
            list = jar.stream().map(ZipEntry::getName)
                .filter(s -> !s.endsWith("/")) // Directories
                .collect(Collectors.toList());
        }
        assertThat(list).contains("META-INF/MANIFEST.MF", "testconfig.yaml").hasSize(2);
    }

    @Test
    public void testAddingFileUsingFileSets() throws IOException, PackagingException {
        AbstractVertxMojo mojo = mock(AbstractVertxMojo.class);
        MavenProject project = mock(MavenProject.class);
        when(mojo.getLog()).thenReturn(new SystemStreamLog());
        when(project.getBasedir()).thenReturn(new File("."));
        when(mojo.getProject()).thenReturn(project);

        Archive archive = new Archive();
        archive.setIncludeClasses(false);
        archive.setDependencySets(Collections.emptyList());
        archive.addFileSet(new FileSet()
            .setOutputDirectory("config")
            .addInclude("*.yaml")
            .setDirectory("src/test/resources"));


        File output = new File(out, "test-filesets.jar");
        PackageConfig config = new PackageConfig()
            .setProject(project)
            .setMojo(mojo)
            .setOutput(output)
            .setArtifacts(Collections.emptySet())
            .setArchive(archive);
        service.doPackage(config);

        assertThat(output).isFile();
        List<String> list;
        try (JarFile jar = new JarFile(output)) {
            list = jar.stream().map(ZipEntry::getName)
                .filter(s -> !s.endsWith("/")) // Directories
                .collect(Collectors.toList());
        }
        assertThat(list).contains("META-INF/MANIFEST.MF", "config/testconfig.yaml", "config/testconfig2.yaml").hasSize(3);
    }

    @Test
    public void testAddingFileUsingFileSetsAndExclusion() throws IOException, PackagingException {
        AbstractVertxMojo mojo = mock(AbstractVertxMojo.class);
        MavenProject project = mock(MavenProject.class);
        when(mojo.getLog()).thenReturn(new SystemStreamLog());
        when(project.getBasedir()).thenReturn(new File("."));
        when(mojo.getProject()).thenReturn(project);

        Archive archive = new Archive();
        archive.setIncludeClasses(false);
        archive.setDependencySets(Collections.emptyList());
        archive.addFileSet(new FileSet()
            .setOutputDirectory("config")
            .addInclude("*.yaml")
            .addExclude("*2.yaml")
            .setDirectory("src/test/resources"));


        File output = new File(out, "test-fileset-with-exclusion.jar");
        PackageConfig config = new PackageConfig()
            .setProject(project)
            .setMojo(mojo)
            .setOutput(output)
            .setArtifacts(Collections.emptySet())
            .setArchive(archive);
        service.doPackage(config);

        assertThat(output).isFile();
        List<String> list;
        try (JarFile jar = new JarFile(output)) {
            list = jar.stream().map(ZipEntry::getName)
                .filter(s -> !s.endsWith("/")) // Directories
                .collect(Collectors.toList());
        }
        assertThat(list).contains("META-INF/MANIFEST.MF", "config/testconfig.yaml").hasSize(2);
    }

    @Test
    public void testAddingFileUsingFileItem() throws IOException, PackagingException {
        AbstractVertxMojo mojo = mock(AbstractVertxMojo.class);
        MavenProject project = mock(MavenProject.class);
        when(mojo.getLog()).thenReturn(new SystemStreamLog());
        when(project.getBasedir()).thenReturn(new File("."));
        when(mojo.getProject()).thenReturn(project);

        Archive archive = new Archive();
        archive.setIncludeClasses(false);
        archive.setDependencySets(Collections.emptyList());
        archive.addFile(new FileItem()
            .setOutputDirectory("config")
            .setSource("src/test/resources/testconfig.yaml")
            .setDestName("some-config.yaml"));


        File output = new File(out, "test-file-item.jar");
        PackageConfig config = new PackageConfig()
            .setProject(project)
            .setMojo(mojo)
            .setOutput(output)
            .setArtifacts(Collections.emptySet())
            .setArchive(archive);
        service.doPackage(config);

        assertThat(output).isFile();
        List<String> list;
        try (JarFile jar = new JarFile(output)) {
            list = jar.stream().map(ZipEntry::getName)
                .filter(s -> !s.endsWith("/")) // Directories
                .collect(Collectors.toList());
        }
        assertThat(list).contains("META-INF/MANIFEST.MF", "config/some-config.yaml").hasSize(2);
    }

}
