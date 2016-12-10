package io.fabric8.vertx.maven.plugin.it.invoker;

import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * Result of {@link MavenProcessInvoker#execute(InvocationRequest)}. It keeps a reference on the created process.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MavenProcessInvocationResult implements InvocationResult {

    private Process process;
    private CommandLineException exception;

    void destroy() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    MavenProcessInvocationResult setProcess(Process process) {
        this.process = process;
        return this;
    }

    public MavenProcessInvocationResult setException(CommandLineException exception) {
        this.exception = exception;
        return this;
    }

    @Override
    public CommandLineException getExecutionException() {
        return exception;
    }

    @Override
    public int getExitCode() {
        if (process == null) {
            throw new IllegalStateException("No process");
        } else {
            return process.exitValue();
        }
    }
}
