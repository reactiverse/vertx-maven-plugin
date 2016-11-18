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

package io.fabric8.vertx.maven.plugin.runners;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * This class will help in launching the VertX application as non-forked within the same JVM i.e. Maven JVM
 *
 * @author kameshs
 */
public class LaunchRunner {

    private final String launcherClass;
    private final List<String> argList;
    private final IsolatedThreadGroup threadGroup;
    private final Log logger;
    private final List<URL> classPathUrls;

    public LaunchRunner(String launcherClass, List<String> argList, Log logger, List<URL> classPathUrls) {
        this.launcherClass = launcherClass;
        this.argList = argList;
        this.threadGroup = new IsolatedThreadGroup(launcherClass, logger);
        this.logger = logger;
        this.classPathUrls = classPathUrls;
    }

    /**
     * This will allow the process thread to join the existing non daemon threads
     */
    public void join() {
        boolean hasNoDaemonThreads;
        do {
            hasNoDaemonThreads = false;
            Thread[] threads = new Thread[threadGroup.activeCount()];
            threadGroup.enumerate(threads);
            for (Thread thread : threads) {
                if (thread != null && !thread.isDaemon()) {
                    hasNoDaemonThreads = true;
                    joinThread(thread, 0);
                }
            }
        } while (hasNoDaemonThreads);
    }

    /**
     * This will allow the thread to join the currentThread with the specified timeout in millis
     *
     * @param thread  - the thread that will be joining the curent thread
     * @param timeout - the timeout in millis
     */
    private void joinThread(Thread thread, int timeout) {
        try {
            thread.join(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while joining thread " + thread, e);
        }
    }

    /**
     * This will create a seperate thread that will invoke the Launcher
     *
     * @return - the thread that will run with in the current thread and responsible for launching the app
     */
    public Thread run() {
        StringBuilder str = new StringBuilder();
        this.argList.forEach(s -> {
            str.append(s);
            str.append(" ");

        });

        //if (logger.isDebugEnabled()) {
        logger.info("Running Command: " + str);
        // }

        //Ensure that all necessary jars are there in classpath
        String classPath = System.getProperty("java.class.path");
        StringBuilder newClasspath = new StringBuilder();
        for (URL ele : classPathUrls) {
            try {
                newClasspath = newClasspath
                        .append((newClasspath.length() > 0 ? File.pathSeparator : "")
                                + new File(ele.toURI()));
            } catch (URISyntaxException e) {
                //very abnormal
            }
        }
        newClasspath.append(File.pathSeparator);
        newClasspath.append(classPath);

        System.setProperty("java.class.path", newClasspath.toString());

        Thread thread = new Thread(threadGroup, () -> {

            try {
                Class<?> launcherClazz = Thread.currentThread().getContextClassLoader().loadClass(launcherClass);
                Method method = launcherClazz.getMethod("main", String[].class);
                final String[] args = new String[argList.size()];
                argList.toArray(args);
                method.invoke(null, new Object[]{args});
            } catch (ClassNotFoundException e) {
                getThreadGroup().uncaughtException(Thread.currentThread(), e);
            } catch (NoSuchMethodException e) {
                Exception wrappedEx = new Exception("The class " + launcherClass + " does not have static main method " +
                        " that accepts String[]", e);
                getThreadGroup().uncaughtException(Thread.currentThread(), wrappedEx);
            } catch (InvocationTargetException e) {
                getThreadGroup().uncaughtException(Thread.currentThread(), e);
            } catch (IllegalAccessException e) {
                getThreadGroup().uncaughtException(Thread.currentThread(), e);
            } catch (Exception e) {
                getThreadGroup().uncaughtException(Thread.currentThread(), e);
            }

        });
        return thread;
    }

    public IsolatedThreadGroup getThreadGroup() {
        return threadGroup;
    }

    /**
     * Isolated ThreadGroup to catch uncaught exceptions {@link ThreadGroup}
     */
    public static final class IsolatedThreadGroup extends ThreadGroup {

        private final Log logger;
        private Object monitor = new Object();
        private Throwable exception;

        public IsolatedThreadGroup(String name, Log logger) {
            super(name);
            this.logger = logger;
        }

        public void rethrowException() throws MojoExecutionException {
            synchronized (this.monitor) {
                if (this.exception != null) {
                    throw new MojoExecutionException("Error occurred while running.." +
                            this.exception.getMessage(), this.exception);
                }
            }
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (!(e instanceof ThreadDeath)) {
                synchronized (this.monitor) {
                    this.exception = (this.exception != null ? e : this.exception);
                }
                logger.warn(e);
            }
        }
    }

}
