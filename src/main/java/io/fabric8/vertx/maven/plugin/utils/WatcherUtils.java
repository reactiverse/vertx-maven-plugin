/*
 *   Copyright 2016 Kamesh Sampath
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.fabric8.vertx.maven.plugin.utils;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.util.List;

/**
 * @author kameshs
 */
public class WatcherUtils {

    /**
     * @param threadGroup
     * @param project
     * @param mavenSession
     * @param buildPluginManager
     * @param redeployPatterns
     * @param logger
     * @param classLoader
     * @return
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public static Thread spinWatcher(ThreadGroup threadGroup, MavenProject project, MavenSession mavenSession,
                                     BuildPluginManager buildPluginManager, List<String> redeployPatterns,
                                     Log logger, ClassLoader classLoader)
            throws MojoExecutionException, MojoFailureException {

        logger.info("Watching for incremental builds");

        Thread redeployWatcher = null;

//        try {
//
//            redeployWatcher = new Thread(threadGroup, new IncrementalBuilder(project, mavenSession,
//                    buildPluginManager, redeployPatterns, logger));
//
//        } catch (IOException e) {
//            throw new MojoExecutionException("Error starting watcher :", e);
//        }
//
//        redeployWatcher.setContextClassLoader(classLoader);
//        redeployWatcher.start();

        return redeployWatcher;
    }

    /**
     * @param thread
     * @param timeout
     * @param logger
     */
    private static void joinThread(Thread thread, int timeout, Log logger) {
        try {
            thread.join(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while joining thread " + thread, e);
        }
    }

    /**
     * @param threadGroup
     * @param logger
     */
    public static void join(ThreadGroup threadGroup, Log logger) {
        boolean hasNoDaemonThreads;
        do {
            hasNoDaemonThreads = false;
            Thread[] threads = new Thread[threadGroup.activeCount()];
            threadGroup.enumerate(threads);
            for (Thread thread : threads) {
                if (thread != null && !thread.isDaemon()) {
                    hasNoDaemonThreads = true;
                    joinThread(thread, 0, logger);
                }
            }
        } while (hasNoDaemonThreads);
    }
}
