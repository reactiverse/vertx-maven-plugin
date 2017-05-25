package io.fabric8.vertx.maven.plugin.components;

/**
 * Service responsible for combining SPI files.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ServiceFileCombiner {

    /**
     * Applies the combination strategy.
     *
     * @param config the non-null configuration
     */
    void doCombine(ServiceFileCombinationConfig config);

}
