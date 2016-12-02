package io.fabric8.vertx.maven.plugin.model;

/**
 * @author kameshs
 */
public enum CombinationStrategy {
    /**
     * Combine all service providers
     */
    combine,
    /**
     * Skip the combination and use only the ones defined in the current project.
     */
    none
}
