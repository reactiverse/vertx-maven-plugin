package io.fabric8.vertx.maven.plugin.it.invoker;

import org.apache.maven.shared.invoker.InvocationResult;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ControlledInvocationResult implements InvocationResult {

    private Process process;
    private CommandLineException exception;

    void destroy() {
        if (process != null  && process.isAlive()) {
            process.destroy();
        }
    }

    ControlledInvocationResult setProcess(Process process) {
        this.process = process;
        return this;
    }

    public ControlledInvocationResult setException(CommandLineException exception) {
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
