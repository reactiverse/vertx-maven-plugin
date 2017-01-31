/*
 *    Copyright (c) 2016 Red Hat, Inc.
 *
 *    Red Hat licenses this file to you under the Apache License, version
 *    2.0 (the "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *    implied.  See the License for the specific language governing
 *    permissions and limitations under the License.
 */

package io.fabric8.vertx.maven.plugin.it;

import com.google.common.collect.ImmutableList;
import io.fabric8.vertx.maven.plugin.it.VertxMojoTestBase;
import io.fabric8.vertx.maven.plugin.it.invoker.RunningVerifier;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test checking the execution of vertx:run --instances <n>
 */
public class RunHaQuorumIT extends VertxMojoTestBase {

    private RunningVerifier verifier;

    private void initVerifier(File root) throws VerificationException {
        verifier = new RunningVerifier(root.getAbsolutePath());
        verifier.setAutoclean(false);
        verifier.setDebug(true);
        verifier.setForkJvm(true);
        verifier.setMavenDebug(true);
        installPluginToLocalRepository(verifier.getLocalRepository());
    }

    @After
    public void waitForStop() {
        if (verifier != null) {
            verifier.stop();
        }
        awaitUntilServerDown();
    }

    @Test
    public void testRunWithoutInstances() throws Exception {
        File testDir = initProject("projects/run-ha-quorum-it");
        assertThat(testDir).isDirectory();

        initVerifier(testDir);
        prepareProject(testDir, verifier);

        run(verifier, "package");

        String response = getHttpResponse();
        verifier.verifyTextInLog("io.vertx.core.impl.HAManager");
        //Though default quorum is 1 - we are just verifying that we are able to get the passed value in verticle arguments
        assertThat(response).isEqualTo("Quorum=1");
    }

    private void run(Verifier verifier, String... previous) throws VerificationException {
        verifier.setLogFileName("build-run.log");
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add(previous);
        builder.add("vertx:run");
        verifier.executeGoals(builder.build());
    }
}
