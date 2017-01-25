package io.fabric8.vertx.maven.plugin.it.invoker;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.jvnet.winp.WinProcess;

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
                for (WinProcess wp : WinProcess.all()) {
                    try {
                        String cli = wp.getCommandLine();
                        if (cli.contains("java")) {
                            //System.out.println("CLI :"+cli);
                            if (cli.contains("-Dvertx.debug")) {
                                System.out.println("Killing Background Vertx Debug Process ..." + wp.getPid());
                                wp.killRecursively();
                            }

                            if (cli.contains("-Dvertx.id")) {
                                System.out.println("Killing Background Vertx Process  ..." + wp.getPid());
                                wp.killRecursively();
                            }

                            if (cli.contains("--launcher-class")) {
                                System.out.println("Killing Background Vertx Run Process ..." + wp.getPid());
                                wp.killRecursively();
                            }

                        }

                    } catch (Exception e) {
                        //Ignore
                    }
                }
                return;
            } else {
                process.destroy();
            }
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
