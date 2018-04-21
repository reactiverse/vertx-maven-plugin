package io.reactiverse.vertx.maven.plugin.components;

import io.reactiverse.vertx.maven.plugin.mojos.PackageMojo;
import org.apache.maven.project.MavenProject;

import java.util.Map;

/**
 * Implementation of this service are able to provide custom entries to the "fat-jar" manifest file.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ManifestCustomizerService {

    /**
     * Returns the entries to add to the manifest.
     *
     * @param mojo    the mojo
     * @param project the project
     * @return a non-null map with the entries
     */
    Map<String, String> getEntries(PackageMojo mojo, MavenProject project);
}
