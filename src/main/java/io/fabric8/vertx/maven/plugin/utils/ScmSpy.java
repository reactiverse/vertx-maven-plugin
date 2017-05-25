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

package io.fabric8.vertx.maven.plugin.utils;

import io.fabric8.vertx.maven.plugin.model.ExtraManifestKeys;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmUrlUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.fabric8.vertx.maven.plugin.components.impl.ProjectManifestCustomizer.manifestTimestampFormat;

/**
 * A simple SCM utility wrapper, that extracts some SCM related metadata using Apache SCM providers
 * @see <a href="https://maven.apache.org/components/scm/index.html">Apache Maven SCM</a>
 * @author kameshs
 */
public class ScmSpy {

    private final ScmManager scmManager;

    public ScmSpy(ScmManager scmManager) {
        this.scmManager = scmManager;

    }

    /**
     * The method that is used to determine the SCM type based on the SCM url pattern
     * @see <a href="https://maven.apache.org/components/scm/scm-url-format.html">Maven SCM URL Format</a>
     * @param scmUrl - the SCM url that needs to be parsed to determine the SCM type, the value of url is typically the
     *               "connection" or "developerConnection" element value of the maven <scm/>
     * @return String - the SCM type, e.g. for git it will be "git" , for subversion it will be "svn"
     * @throws ScmRepositoryException - any SCM related exceptions that might happen when checking the SCM type
     * @throws NoSuchScmProviderException - if an invalid url is given as part of maven <scm> "connection" or "developerConnection"
     */
    public String getScmType(String scmUrl) throws ScmRepositoryException, NoSuchScmProviderException {
        return ScmUrlUtils.getProvider(scmUrl);
    }

    /**
     * This method extracts the simple metadata such as revision, lastCommitTimestamp of the commit/hash, author of the commit
     * from the Changelog available in the SCM repository
     * @param scmUrl - the SCM url to get the SCM connection
     * @param workingDir - the Working Copy or Directory of the SCM repo
     * @return a {@link Map<String,String>} of values extracted form the changelog, with Keys from {@link ExtraManifestKeys}
     * @throws IOException - any error that might occur while manipulating the SCM Changelog
     * @throws ScmException - other SCM related exceptions
     */
    public Map<String, String> getChangeLog(String scmUrl, File workingDir) throws IOException, ScmException {
        Map<String, String> changeLogMap = new HashMap<>();
        ScmRepository scmRepository = scmManager.makeScmRepository(scmUrl);
        ChangeLogScmResult scmResult = scmManager.changeLog(new ChangeLogScmRequest(scmRepository,
            new ScmFileSet(workingDir, "*")));
        if (scmResult.isSuccess()) {
            List<ChangeSet> changeSetList = scmResult.getChangeLog().getChangeSets();
            if (changeSetList != null && !changeSetList.isEmpty()) {
                //get the latest changelog
                ChangeSet changeSet = changeSetList.get(0);
                changeLogMap.put(ExtraManifestKeys.scmType.name(), getScmType(scmUrl));
                changeLogMap.put(ExtraManifestKeys.scmRevision.name(), changeSet.getRevision());
                changeLogMap.put(ExtraManifestKeys.lastCommitTimestamp.name(),
                    manifestTimestampFormat(changeSet.getDate()));
                changeLogMap.put(ExtraManifestKeys.author.name(), changeSet.getAuthor());
            }
        }

        return changeLogMap;
    }
}
