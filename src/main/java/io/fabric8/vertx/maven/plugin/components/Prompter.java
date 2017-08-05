package io.fabric8.vertx.maven.plugin.components;

import java.io.IOException;
import java.util.List;

/**
 * A simple component to retrieve user input.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface Prompter {

    /**
     * Prompt user for a string.
     */
    String prompt(String message) throws IOException;

    /**
     * Prompt user for a string; if user response is blank use a default value.
     */
    String promptWithDefaultValue(String message, String defaultValue) throws IOException;
}
