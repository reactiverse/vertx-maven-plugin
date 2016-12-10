package io.fabric8.vertx.maven.plugin.it.invoker;


import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvokerLogger;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import org.apache.maven.shared.invoker.PrintStreamLogger;
import org.apache.maven.shared.utils.cli.CommandLineUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of verifier using a forked process that is still running while verifying. The process is stop when
 * {@link RunningVerifier#stop()} is called.
 */
public class RunningVerifier extends Verifier {

    private String defaultMavenHome;

    private MavenProcessInvocationResult result;

    public RunningVerifier(String basedir) throws VerificationException {
        super(basedir, true);
    }

    public void stop() {
        if (result == null) {
            return;
        }
        result.destroy();
    }

    @Override
    public void executeGoals(List<String> goals, Map<String, String> envVars) throws VerificationException {
        DefaultInvocationRequest request = new DefaultInvocationRequest();
        MavenProcessInvoker invoker = new MavenProcessInvoker();

        List<String> allGoals = new ArrayList<>();

        if (isAutoclean()) {
            allGoals.add("org.apache.maven.plugins:maven-clean-plugin:clean");
        }

        allGoals.addAll(goals);

        request.setGoals(allGoals);

        File logFile = new File(getBasedir(), getLogFileName());


        request.setDebug(isMavenDebug());

        /*
         * NOTE: Unless explicitly requested by the caller, the forked builds should use the current local
         * repository. Otherwise, the forked builds would in principle leave the sandbox environment which has been
         * setup for the current build. In particular, using "maven.repo.local" will make sure the forked builds use
         * the same local repo as the parent build even if a custom user settings is provided.
         */
        boolean useMavenRepoLocal = Boolean.valueOf(getVerifierProperties().getProperty("use.mavenRepoLocal", "true"));

        if (useMavenRepoLocal) {
            request.setLocalRepositoryDirectory(new File(getLocalRepository()));
            invoker.setLocalRepositoryDirectory(new File(getLocalRepository()));
        }

        try {
            request.setBaseDirectory(new File(getBasedir()));
            request.setPomFile(new File(getBasedir(), "pom.xml"));

            PrintStream log = new PrintStream(logFile);
            invoker.setErrorHandler(new PrintStreamHandler(log, true));
            invoker.setOutputHandler(new PrintStreamHandler(log, true));
            invoker.setLogger(new PrintStreamLogger(log, InvokerLogger.INFO));
            findDefaultMavenHome();
            invoker.setMavenHome(new File(defaultMavenHome));
            invoker.setWorkingDirectory(new File(getBasedir()));
            request.setOutputHandler(new PrintStreamHandler(log, true));

            result = (MavenProcessInvocationResult) invoker.execute(request);
        } catch (Exception e) {
            throw new VerificationException(e);
        }
    }

    private void findDefaultMavenHome()
        throws VerificationException {
        defaultMavenHome = System.getProperty("maven.home");

        if (defaultMavenHome == null) {
            Properties envVars = CommandLineUtils.getSystemEnvVars();
            defaultMavenHome = envVars.getProperty("M2_HOME");
        }
    }
}
