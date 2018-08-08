/*
 *    Copyright (c) 2016-2018 Red Hat, Inc.
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

package io.reactiverse.vertx.maven.plugin.util;

import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * An utility class that is used to manipulate and extract info Git Repository of the project
 *
 * @author kameshs
 */
public class GitUtil {

    /**
     * Get the git {@link Repository} if the the project has one
     *
     * @param project - the {@link MavenProject} whose Git repo needs to be returned in {@link Repository}
     * @return Repository  - the Git Repository of the project
     * @throws IOException - any error that might occur finding the Git folder
     */
    public static Repository getGitRepository(MavenProject project) throws IOException {

        MavenProject rootProject = getRootProject(project);

        File baseDir = rootProject.getBasedir();

        if (baseDir == null) {
            baseDir = project.getBasedir();
        }

        File gitFolder = findGitFolder(baseDir);
        if (gitFolder == null) {
            // No git repository found
            return null;
        }
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder
            .readEnvironment()
            .setGitDir(gitFolder)
            .build();
        return repository;
    }

    /**
     * Retrieve the git commitId hash
     *
     * @param repository - the Git repository from where the latest commit will be retrieved
     * @return String of Git commit hash
     * @throws GitAPIException - any Git exception that might occur while getting commitId
     */
    public static String getGitCommitId(Repository repository) throws GitAPIException {
        try {
            if (repository != null) {
                Iterable<RevCommit> logs = new Git(repository).log().call();
                for (RevCommit rev : logs) {
                    return rev.getName();
                }
            }
        } finally {

        }
        return null;
    }

    /**
     * utility to find &quot;.git&quot; folder of the project
     *
     * @param basedir - the base dir as {@link File} where the git folder existence to be checked
     * @return - the {@link File} reference of the &quot;.git&quot; folder of the project
     */
    public static File findGitFolder(File basedir) {
        File gitDir = new File(basedir, ".git");
        if (gitDir.exists() && gitDir.isDirectory()) {
            return gitDir;
        } else {
            File parent = basedir.getParentFile();
            return parent != null ? findGitFolder(parent) : null;
        }
    }

    /**
     * utility to find the root project
     *
     * @param project - the project whose root {@link MavenProject} need to be determined
     * @return root MavenProject of the project
     */
    public static MavenProject getRootProject(MavenProject project) {
        while (project != null) {
            MavenProject parent = project.getParent();
            if (parent == null) {
                break;
            }
            project = parent;
        }
        return project;
    }
}
