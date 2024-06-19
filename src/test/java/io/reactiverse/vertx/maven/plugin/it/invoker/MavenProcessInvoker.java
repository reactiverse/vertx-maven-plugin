package io.reactiverse.vertx.maven.plugin.it.invoker;

import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamPumper;

import java.io.File;

/**
 * An implementation of {@link DefaultInvoker} launching Maven, but does not wait for the termination of the process.
 * The launched process is passed in the {@link InvocationResult}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MavenProcessInvoker extends DefaultInvoker {

    private static final InvocationOutputHandler DEFAULT_OUTPUT_HANDLER = new SystemOutHandler();

    @Override
    public InvocationResult execute(InvocationRequest request) throws MavenInvocationException {
        MavenCommandLineBuilder cliBuilder = new MavenCommandLineBuilder();

        InvokerLogger logger = getLogger();
        if (logger != null) {
            cliBuilder.setLogger(getLogger());
        }

        File localRepo = getLocalRepositoryDirectory();
        if (localRepo != null) {
            cliBuilder.setLocalRepositoryDirectory(getLocalRepositoryDirectory());
        }

        File mavenHome = getMavenHome();
        if (mavenHome != null) {
            cliBuilder.setMavenHome(getMavenHome());
        }

        File mavenExecutable = getMavenExecutable();
        if (mavenExecutable != null) {
            cliBuilder.setMavenExecutable(mavenExecutable);
        }

        File workingDirectory = getWorkingDirectory();
        if (workingDirectory != null) {
            cliBuilder.setWorkingDirectory(getWorkingDirectory());
        }


        Commandline cli;
        try {
            cli = cliBuilder.build(request);
        } catch (CommandLineConfigurationException e) {
            throw new MavenInvocationException("Error configuring command-line. Reason: " + e.getMessage(), e);
        }

        MavenProcessInvocationResult result = new MavenProcessInvocationResult();

        try {
            Process process = executeCommandLine(cli, request);
            result.setProcess(process);
        } catch (CommandLineException e) {
            result.setException(e);
        }

        return result;
    }

    private Process executeCommandLine(Commandline cli, InvocationRequest request)
        throws CommandLineException {

        assert !request.isInteractive();

        InvocationOutputHandler outputHandler = request.getOutputHandler(DEFAULT_OUTPUT_HANDLER);
        InvocationOutputHandler errorHandler = request.getErrorHandler(DEFAULT_OUTPUT_HANDLER);

        return executeCommandLine(cli, outputHandler, errorHandler);
    }


    private static Process executeCommandLine(Commandline cl, StreamConsumer systemOut, StreamConsumer systemErr)
        throws CommandLineException {
        if (cl == null) {
            throw new IllegalArgumentException("the command line cannot be null.");
        } else {
            final Process p = cl.execute();
            final StreamPumper outputPumper = new StreamPumper(p.getInputStream(), systemOut);
            final StreamPumper errorPumper = new StreamPumper(p.getErrorStream(), systemErr);

            outputPumper.start();
            errorPumper.start();

            new Thread(() -> {
                try {
                    // Wait for termination
                    p.waitFor();
                    outputPumper.waitUntilDone();
                    errorPumper.waitUntilDone();
                } catch (Exception e) {
                    outputPumper.disable();
                    errorPumper.disable();
                    e.printStackTrace();
                } finally {
                    outputPumper.close();
                    errorPumper.close();
                }
            }).start();

            return p;
        }
    }
}
