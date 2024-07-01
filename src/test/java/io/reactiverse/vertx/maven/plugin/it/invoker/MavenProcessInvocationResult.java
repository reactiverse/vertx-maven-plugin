package io.reactiverse.vertx.maven.plugin.it.invoker;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.jvnet.winp.WinProcess;

import java.lang.reflect.Method;

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
            if (SystemUtils.IS_OS_WINDOWS) {
                WinProcess winProcess = getWinProcess(process);
                winProcess.killRecursively();
            } else {
                process.destroy();
            }
        }
    }

    private WinProcess getWinProcess(Process process) {
        Method pidMethod = findPidMethod();
        WinProcess winProcess;
        if (pidMethod != null) {
            // Java 9+
            // To avoid reflection access warnings we get the pid with builtin method
            int pid;
            try {
                pid = Math.toIntExact((long) pidMethod.invoke(process, new Object[]{}));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            winProcess = new WinProcess(pid);
        } else {
            winProcess = new WinProcess(process);
        }
        return winProcess;
    }

    private Method findPidMethod() {
        for (Method method : Process.class.getMethods()) {
            if ("pid".equals(method.getName()) && (method.getParameterCount() == 0)) {
                return method;
            }
        }
        return null;
    }

    MavenProcessInvocationResult setProcess(Process process) {
        this.process = process;
        return this;
    }

    public MavenProcessInvocationResult setException(CommandLineException exception) {
        // Print the stack trace immediately to give some feedback early
        // In intellij, the used `mvn` executable is not "executable" by default on Mac and probably linux.
        // You need to chmod +x the file.
        exception.printStackTrace();
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
