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

import org.apache.maven.plugin.logging.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author kameshs
 */
public class ThreadUtil {

    private Log log;

    public ThreadUtil build(Log log) {
        this.log = log;
        return this;
    }

    public void joinNonDaemonThread(ThreadGroup threadGroup) {
        boolean isNotDaemon = false;
        do {

            Stream<Thread> activeThreads = getActiveThreads(threadGroup);

            Optional<Thread> nonDaemonThread = activeThreads
                    .filter(thread -> !thread.isDaemon()).findFirst();

            if (nonDaemonThread.isPresent()) {
                isNotDaemon = true;
                joinThread(nonDaemonThread.get(), 0);
            }


        } while (isNotDaemon);
    }

    public void joinThread(Thread thread, int timeout) {
        try {
            thread.join(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("interrupted while joining " + thread, e);
        }
    }

    public Stream<Thread> getActiveThreads(ThreadGroup threadGroup) {

        Thread[] threads = new Thread[threadGroup.activeCount()];

        int nThreads = threadGroup.enumerate(threads);
        Collection<Thread> activeThreads = new ArrayList<>(nThreads);

        for (int i = 0; i < threads.length && threads[i] != null; i++) {
            activeThreads.add(threads[i]);
        }

        return activeThreads.stream();
    }

}
