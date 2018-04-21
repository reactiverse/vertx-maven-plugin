package io.reactiverse.vertx.maven.plugin.components;

import java.io.File;

/**
 * Component responsible for packaging the application.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface PackageService {

    /**
     * Type of packaging.
     *
     * @return the type of packaging
     */
    PackageType type();

    /**
     * Creates the package.
     *
     * @param config the configuration
     * @return the created package (or root in case of a directory)
     * @throws PackagingException thrown when the package cannot be built
     */
    File doPackage(PackageConfig config) throws PackagingException;

}
