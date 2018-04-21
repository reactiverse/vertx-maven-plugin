package io.reactiverse.vertx.maven.plugin.components;

/**
 * Exception thrown when the application package cannot be built correctly.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PackagingException extends Exception {

    /**
     * Creates a new {@link PackagingException}.
     *
     * @param e the cause
     */
    public PackagingException(Exception e) {
        super("Unable to build the package", e);
    }

    /**
     * Creates a new {@link PackagingException}.
     *
     * @param msg the message
     */
    public PackagingException(String msg) {
        super(msg);
    }

    /**
     * Creates a new {@link PackagingException}.
     *
     * @param msg   the message
     * @param cause the cause
     */
    public PackagingException(String msg, Exception cause) {
        super(msg, cause);
    }
}
