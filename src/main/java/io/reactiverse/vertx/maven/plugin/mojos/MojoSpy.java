/*
 *
 *   Copyright (c) 2016-2018 Red Hat, Inc.
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

package io.reactiverse.vertx.maven.plugin.mojos;

import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.MojoExecution;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MojoSpy extends AbstractExecutionListener {

    private static final List<String> PHASES = Arrays.asList(
        "generate-sources",
        "process-sources",
        "generate-resources",
        "process-resources",
        "compile",
        "process-classes"
    );

    private final List<MojoExecution> mojos = new CopyOnWriteArrayList<>();
    private final MavenExecutionRequest request;
    private final ExecutionListener delegate;

    public MojoSpy(MavenExecutionRequest request) {
        this.delegate = request.getExecutionListener();
        this.request = request;
    }

    @Override
    public void projectDiscoveryStarted(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.projectDiscoveryStarted(executionEvent);
        }
    }

    @Override
    public void sessionStarted(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.sessionStarted(executionEvent);
        }
    }

    @Override
    public void sessionEnded(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.sessionEnded(executionEvent);
        }
    }

    @Override
    public void projectSkipped(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.projectSkipped(executionEvent);
        }
    }

    @Override
    public void projectStarted(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.projectStarted(executionEvent);
        }
    }

    @Override
    public void projectSucceeded(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.projectSucceeded(executionEvent);
        }
        // Cleanup
        request.setExecutionListener(delegate);
    }

    @Override
    public void projectFailed(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.projectFailed(executionEvent);
        }
        // Cleanup
        request.setExecutionListener(delegate);
    }

    @Override
    public void mojoSkipped(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.mojoSkipped(executionEvent);
        }
    }

    @Override
    public void mojoStarted(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.mojoStarted(executionEvent);
        }
    }

    @Override
    public void mojoSucceeded(ExecutionEvent executionEvent) {
        // Unlike mojoStarted, this callback has the lifecycle phase set.
        MojoExecution execution = executionEvent.getMojoExecution();
        addExecutionIfNotContainedAlready(execution);
        if (delegate != null) {
            delegate.mojoSucceeded(executionEvent);
        }
    }

    private void addExecutionIfNotContainedAlready(MojoExecution execution) {
        if (PHASES.contains(execution.getLifecyclePhase())) {
            String artifact = execution.getArtifactId();
            String id = execution.getExecutionId();
            for (MojoExecution exec : mojos) {
                if (exec.getArtifactId().equals(artifact) && exec.getExecutionId().equals(id)) {
                    return;
                }
            }
            mojos.add(execution);
        }
    }

    @Override
    public void mojoFailed(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.mojoFailed(executionEvent);
        }
    }

    @Override
    public void forkStarted(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.forkStarted(executionEvent);
        }
    }

    @Override
    public void forkSucceeded(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.forkSucceeded(executionEvent);
        }
    }

    @Override
    public void forkFailed(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.forkFailed(executionEvent);
        }
    }

    @Override
    public void forkedProjectStarted(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.forkedProjectStarted(executionEvent);
        }
    }

    @Override
    public void forkedProjectSucceeded(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.forkedProjectSucceeded(executionEvent);
        }
    }

    @Override
    public void forkedProjectFailed(ExecutionEvent executionEvent) {
        if (delegate != null) {
            delegate.forkedProjectFailed(executionEvent);
        }
    }

    public List<MojoExecution> getMojos() {
        return mojos;
    }
}
