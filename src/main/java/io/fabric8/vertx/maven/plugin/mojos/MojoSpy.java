/*
 *
 *   Copyright (c) 2016 Red Hat, Inc.
 *
 *   Red Hat licenses this file to you under the Apache License, version
 *   2.0 (the "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *   implied.  See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package io.fabric8.vertx.maven.plugin.mojos;

import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.logging.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MojoSpy extends AbstractExecutionListener {

    public static final List<MojoExecution> MOJOS = new CopyOnWriteArrayList<>();

    public static final List<String> PHASES = Arrays.asList(
        "generate-sources",
        "process-sources",
        "generate-resources",
        "process-resources",
        "compile",
        "process-classes"
    );

    private final Log log;
    private ExecutionListener delegate;

    public MojoSpy(ExecutionListener listener, Log log) {
        this.delegate = listener;
        this.log = log;
    }

    @Override
    public void projectDiscoveryStarted(ExecutionEvent executionEvent) {
        delegate.projectDiscoveryStarted(executionEvent);
    }

    @Override
    public void sessionStarted(ExecutionEvent executionEvent) {
        delegate.sessionStarted(executionEvent);
    }

    @Override
    public void sessionEnded(ExecutionEvent executionEvent) {
        delegate.sessionEnded(executionEvent);
    }

    @Override
    public void projectSkipped(ExecutionEvent executionEvent) {
        delegate.projectSkipped(executionEvent);
    }

    @Override
    public void projectStarted(ExecutionEvent executionEvent) {
        delegate.projectStarted(executionEvent);
    }

    @Override
    public void projectSucceeded(ExecutionEvent executionEvent) {
        // TODO should we cleanup ?
        delegate.projectSucceeded(executionEvent);
    }

    @Override
    public void projectFailed(ExecutionEvent executionEvent) {
        // TODO should we cleanup ?
        delegate.projectFailed(executionEvent);
    }

    @Override
    public void mojoSkipped(ExecutionEvent executionEvent) {
        delegate.mojoSkipped(executionEvent);
    }

    @Override
    public void mojoStarted(ExecutionEvent executionEvent) {
        delegate.mojoStarted(executionEvent);
    }

    @Override
    public void mojoSucceeded(ExecutionEvent executionEvent) {
        // Unlike mojoStarted, this callback has the lifecycle phase set.
        MojoExecution execution = executionEvent.getMojoExecution();
        MOJOS.add(execution);
        delegate.mojoSucceeded(executionEvent);
    }

    @Override
    public void mojoFailed(ExecutionEvent executionEvent) {
        delegate.mojoFailed(executionEvent);
    }

    @Override
    public void forkStarted(ExecutionEvent executionEvent) {
        delegate.forkStarted(executionEvent);
    }

    @Override
    public void forkSucceeded(ExecutionEvent executionEvent) {
        delegate.forkSucceeded(executionEvent);
    }

    @Override
    public void forkFailed(ExecutionEvent executionEvent) {
        delegate.forkFailed(executionEvent);
    }

    @Override
    public void forkedProjectStarted(ExecutionEvent executionEvent) {
        delegate.forkedProjectStarted(executionEvent);
    }

    @Override
    public void forkedProjectSucceeded(ExecutionEvent executionEvent) {
        delegate.forkedProjectSucceeded(executionEvent);
    }

    @Override
    public void forkedProjectFailed(ExecutionEvent executionEvent) {
        delegate.forkedProjectFailed(executionEvent);
    }
}
