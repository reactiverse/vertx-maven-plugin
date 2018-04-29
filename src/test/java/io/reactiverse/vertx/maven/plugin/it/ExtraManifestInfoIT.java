/*
 *    Copyright (c) 2016-2017 Red Hat, Inc.
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

package io.reactiverse.vertx.maven.plugin.it;

import io.reactiverse.vertx.maven.plugin.model.ExtraManifestKeys;
import io.reactiverse.vertx.maven.plugin.util.GitUtil;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kameshs
 */
public class ExtraManifestInfoIT extends VertxMojoTestBase {

    String GIT_PROJECT_ROOT = "projects/manifest-git-it";
    String SVN_PROJECT_ROOT = "projects/manifest-svn-it";

    private Verifier verifier;


    public void initVerifier(File root) throws VerificationException {
        verifier = new Verifier(root.getAbsolutePath());
        verifier.setAutoclean(false);

        installPluginToLocalRepository(verifier.getLocalRepository());
    }

    private Git prepareGitSCM(File testDir, Verifier verifier) throws IOException, GitAPIException {
        Git git = Git.init().setDirectory(testDir).call();
        File gitFolder = GitUtil.findGitFolder(testDir);
        assertThat(gitFolder).isNotNull();
        return git;
    }

    @Test
    public void testGITSCM() throws IOException, VerificationException, GitAPIException {
        File testDir = initProject(GIT_PROJECT_ROOT);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);

        prepareProject(testDir, verifier);

        File gitFolder = GitUtil.findGitFolder(testDir);

        assertThat(testDir).isNotNull();
        assertThat(testDir.getName()).endsWith("manifest-git-it");
        assertThat(gitFolder).isNull();

        Git git = prepareGitSCM(testDir, verifier);
        gitFolder = git.getRepository().getDirectory();
        assertThat(gitFolder.getParentFile().getName()).isEqualTo(testDir.getName());
        assertThat(git.status().call().getUntracked()).contains("pom.xml", "src/main/java/demo/SimpleVerticle.java");

        //Now add and commit the file
        DirCache index = git.add().addFilepattern(".").call();
        assertThat(index.getEntryCount()).isEqualTo(2);

        git.commit().setMessage("First Import").call();

        runPackage(verifier);
        assertManifest(testDir, "git");

    }

    //@Test TODO since CI does not have svn, its not possible to build this
    public void testSVNSCM() throws IOException, VerificationException {
        File testDir = initProject(SVN_PROJECT_ROOT);
        assertThat(testDir).isDirectory();

        initVerifier(testDir);

        prepareProject(testDir, verifier);
        assertThat(testDir).isNotNull();
        assertThat(testDir.getName()).endsWith("manifest-svn-it");

        //Since SVN is a centralized repo the SCM provider will always get the change log from remote only
        // we dont need to check for any local commits

        runPackage(verifier);
        assertManifest(testDir, "svn");

    }

    private void assertManifest(File testDir, String scm) throws IOException {
        verifier.assertFilePresent("target/vertx-demo-start-0.0.1.BUILD-SNAPSHOT.jar");
        File jarFile = new File(testDir, "target/vertx-demo-start-0.0.1.BUILD-SNAPSHOT.jar");
        assertThat(jarFile).isNotNull();

        Manifest manifest = new JarFile(jarFile).getManifest();
        //Extract and Check Manifest for details
        assertThat(manifest).isNotNull();

        //Check some manifest attributes
        String projectName = manifest.getMainAttributes().getValue(
            ExtraManifestKeys.PROJECT_NAME.header());
        assertThat(projectName).isEqualTo("vertx-demo-start");

        String projectGroupId = manifest.getMainAttributes().getValue(
            ExtraManifestKeys.PROJECT_GROUP_ID.header());
        assertThat(projectGroupId).isEqualTo("io.reactiverse.vmp.it");

        String projectVersion = manifest.getMainAttributes().getValue(
            ExtraManifestKeys.PROJECT_VERSION.header());
        assertThat(projectVersion).isEqualTo("0.0.1.BUILD-SNAPSHOT");

        String projectDeps = manifest.getMainAttributes().getValue(
            ExtraManifestKeys.PROJECT_DEPS.header());
        assertThat(projectDeps).isNotNull();


        if ("git".equalsIgnoreCase(scm)) {

            String scmType = manifest.getMainAttributes().getValue(
                ExtraManifestKeys.SCM_TYPE.header());
            assertThat(scmType).isNotNull();
            assertThat(scmType).isEqualToIgnoringCase("Git");

            String scmRevision = manifest.getMainAttributes().getValue(
                ExtraManifestKeys.SCM_REVISION.header());
            assertThat(scmRevision).isNotNull();

            Pattern pattern = Pattern.compile("^\\w*$");
            Matcher matcher = pattern.matcher(scmRevision);
            assertThat(matcher.matches()).isTrue();

            assertThat(projectDeps)
                .isEqualToIgnoringWhitespace("io.vertx:vertx-core:3.4.2 io.vertx:vertx-web:3.4.2 io" +
                    ".vertx:vertx-jdbc-client:3.4.2");
        } else if ("svn".equalsIgnoreCase(scm)) {
            String scmType = manifest.getMainAttributes().getValue(
                ExtraManifestKeys.SCM_TYPE.header());
            assertThat(scmType).isNotNull();
            assertThat(scmType).isEqualToIgnoringCase("SVN");
            String revision = manifest.getMainAttributes().getValue(
                ExtraManifestKeys.SCM_REVISION.header());
            assertThat(revision).isNotNull();
            assertThat(revision).isEqualTo("1381106");
            assertThat(projectDeps)
                .isEqualToIgnoringWhitespace("io.vertx:vertx-core:3.4.2 io.vertx:vertx-web:3.4.2");
        }

    }


}
