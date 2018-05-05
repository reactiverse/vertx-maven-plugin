package io.reactiverse.vertx.maven.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */

public class ArtifactBuilder {

    private String group = "org.acme";
    private String artifact;
    private String version = "1.0";
    private String type = "jar";
    private String classifier = "";
    private String scope;

    private JavaArchive archive;
    private File file;

    public ArtifactBuilder group(String group) {
        this.group = group;
        return this;
    }

    public ArtifactBuilder artifact(String artifact) {
        this.artifact = artifact;
        return this;
    }

    public ArtifactBuilder version(String version) {
        this.version = version;
        return this;
    }

    public ArtifactBuilder type(String type) {
        this.type = type;
        return this;
    }

    public ArtifactBuilder classifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    public ArtifactBuilder scope(String scope) {
        this.scope = scope;
        return this;
    }

    public ArtifactBuilder file(File file) {
        this.file = file;
        return this;
    }

    public ArtifactBuilder file(String file) {
        return file(new File(file));
    }

    public Artifact build() throws IOException {
        Artifact artifact = new DefaultArtifact(group, this.artifact, version, scope, type, classifier, null);
        if (archive != null) {
            File file = File.createTempFile("vmp-test-" + group + "-" + artifact + "-" + version, "." + type);
            archive.as(ZipExporter.class).exportTo(file, true);
            artifact.setFile(file);
        } else if (file != null) {
            artifact.setFile(file);
        } else {
            // Generate content
            File file = File.createTempFile("vmp-test-" + group + "-" + artifact + "-" + version, "." + type);
            FileUtils.write(file, "// " + UUID.randomUUID().toString(), "UTF-8");
            artifact.setFile(file);
        }
        return artifact;
    }

}
