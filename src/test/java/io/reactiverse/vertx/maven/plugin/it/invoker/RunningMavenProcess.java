package io.reactiverse.vertx.maven.plugin.it.invoker;

import org.apache.commons.lang3.SystemUtils;
import org.jvnet.winp.WinProcess;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RunningMavenProcess implements AutoCloseable {

    private final Process process;
    private final StringBuffer output = new StringBuffer();

    private RunningMavenProcess(Process process) {
        this.process = process;
        startPump(process.getInputStream());
        startPump(process.getErrorStream());
    }

    private void startPump(InputStream is) {
        Thread t = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = r.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            } catch (IOException ignored) {
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public boolean outputContains(String text) {
        return output.toString().contains(text);
    }

    public String getOutput() {
        return output.toString();
    }

    public void stop() {
        if (SystemUtils.IS_OS_WINDOWS) {
            killProcessTreeOnWindows();
        } else {
            process.destroy(); // SIGTERM — lets Maven shutdown hooks kill child processes
        }
        try {
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    private void killProcessTreeOnWindows() {
        try {
            Method pidMethod = null;
            for (Method m : Process.class.getMethods()) {
                if ("pid".equals(m.getName()) && m.getParameterCount() == 0) {
                    pidMethod = m;
                    break;
                }
            }
            WinProcess wp = pidMethod != null
                ? new WinProcess(Math.toIntExact((long) pidMethod.invoke(process)))
                : new WinProcess(process);
            wp.killRecursively();
        } catch (Exception e) {
            process.destroyForcibly();
        }
    }

    @Override
    public void close() {
        stop();
    }

    public static ExecutionResult execute(Path baseDir, String... args) throws IOException, InterruptedException {
        RunningMavenProcess rmp = start(baseDir, args);
        rmp.process.waitFor();
        int exitCode = rmp.process.exitValue();
        return new ExecutionResult(exitCode, rmp.getOutput());
    }

    public static RunningMavenProcess start(Path baseDir, String... args) throws IOException {
        String mavenHome = System.getProperty("maven.home");
        if (mavenHome == null || mavenHome.trim().isEmpty()) {
            throw new IllegalStateException("System property 'maven.home' is not set");
        }
        String mvn = mavenHome + File.separator + "bin" + File.separator + "mvn" +
            (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win") ? ".cmd" : "");

        List<String> command = new ArrayList<>();
        command.add(mvn);
        command.addAll(Arrays.asList(args));

        String localRepo = System.getProperty("maven.repo.local",
            System.getProperty("user.home") + "/.m2/repository");
        command.add("-Dmaven.repo.local=" + localRepo);

        ProcessBuilder pb = new ProcessBuilder(command)
            .directory(baseDir.toFile())
            .redirectErrorStream(false);

        String mavenOpts = System.getProperty("mavenOpts");
        if (mavenOpts != null && !mavenOpts.trim().isEmpty()) {
            pb.environment().put("MAVEN_OPTS", mavenOpts);
        }

        return new RunningMavenProcess(pb.start());
    }

    public static class ExecutionResult {
        private final int exitCode;
        private final String output;

        public ExecutionResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }
    }
}
