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

package io.fabric8.vertx.maven.plugin.utils;

import com.google.common.base.CaseFormat;
import io.fabric8.vertx.maven.plugin.model.ExtraManifestKeys;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.manager.ScmManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

/**
 * The utility class that takes care of adding information to MANIFEST.MF, this information are usually
 * additional metadata about that application that can be used by tools and IDE
 * This plugin uses the <a href="https://maven.apache.org/components/scm/index.html">Apache Maven SCM</a> providers
 * and extracts the metadata in SCM agnostic manner
 *
 * @author kameshs
 */
public class ManifestUtils {

    public static String DEFAULT_DATE_PATTERN = "yyyyMMdd HH:mm:ss z";

    private static final SimpleDateFormat simpleDateFormat;

    static {
        simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
    }

    /**
     * This method adds the extra MANIFEST.MF headers with header keys in {@link ExtraManifestKeys}
     *
     * @param project    - the maven project which is packaged as jar
     * @param attributes - the MANIFEST.MF {@link Attributes}
     * @param scmManager - the Maven SCM Provider Plugin {@link ScmManager} instance
     * @throws MojoExecutionException - incase of any error that might occur during SCM metadata extraction
     */
    public static void addExtraManifestInfo(MavenProject project, Attributes attributes, ScmManager scmManager)
        throws MojoExecutionException {

        Model model = project.getModel();

        attributes.put(attributeName(ExtraManifestKeys.projectName.name()),
            model.getName() == null ? model.getArtifactId() : model.getName());
        attributes.put(attributeName(ExtraManifestKeys.projectGroup.name()), model.getGroupId());
        attributes.put(attributeName(ExtraManifestKeys.projectVersion.name()), model.getVersion());
        attributes.put(attributeName(ExtraManifestKeys.buildTimestamp.name()), manifestTimestampFormat(new Date()));

        //Add SCM Metadata only when <scm> is configured in the pom.xml
        if (project.getScm() != null) {
            Scm scm = project.getScm();
            String connectionUrl = scm.getConnection() == null ? scm.getDeveloperConnection() : scm.getConnection();

            if (scm.getUrl() != null) {
                attributes.put(attributeName(ExtraManifestKeys.scmUrl.name()), scm.getUrl());
            }

            if (scm.getTag() != null) {
                attributes.put(attributeName(ExtraManifestKeys.scmTag.name()), scm.getTag());
            }
            if (scmManager != null && connectionUrl != null) {
                try {
                    //SCM metadata
                    File baseDir = project.getBasedir();
                    ScmSpy scmSpy = new ScmSpy(scmManager);

                    Map<String, String> scmChangeLogMap = scmSpy.getChangeLog(connectionUrl, baseDir);

                    if (!scmChangeLogMap.isEmpty()) {

                        attributes.put(attributeName(ExtraManifestKeys.scmType.name()),
                            scmChangeLogMap.get(ExtraManifestKeys.scmType.name()));
                        attributes.put(attributeName(ExtraManifestKeys.scmRevision.name()),
                            scmChangeLogMap.get(ExtraManifestKeys.scmRevision.name()));
                        attributes.put(attributeName(ExtraManifestKeys.lastCommitTimestamp.name()),
                            scmChangeLogMap.get(ExtraManifestKeys.lastCommitTimestamp.name()));
                        attributes.put(attributeName(ExtraManifestKeys.author.name()),
                            scmChangeLogMap.get(ExtraManifestKeys.author.name()));
                    }

                } catch (IOException e) {
                    throw new MojoExecutionException("Error while getting SCM Metadata:", e);
                } catch (ScmException e) {
                    throw new MojoExecutionException("Error while getting SCM Metadata:", e);
                }
            }
        }

        if (project.getUrl() != null) {
            attributes.put(attributeName(ExtraManifestKeys.projectUrl.name()), model.getUrl());
        }

        List<Dependency> dependencies = model.getDependencies();

        if (dependencies != null && !dependencies.isEmpty()) {

            String deps = dependencies.stream()
                .filter(d -> "compile".equals(d.getScope()) || null == d.getScope())
                .map((d) -> asCoordinates(d))
                .collect(Collectors.joining(" "));
            attributes.put(attributeName(ExtraManifestKeys.projectDependencies.name()), deps);
        }
    }

    /**
     * utility method to return {@link Dependency} as G:V:A:C maven coordinates
     *
     * @param dependency - the maven {@link Dependency} whose coordinates need to be computed
     * @return - the {@link Dependency} info as G:V:A:C string
     */
    protected static String asCoordinates(Dependency dependency) {

        StringBuilder dependencyCoordinates = new StringBuilder().
            append(dependency.getGroupId())
            .append(":")
            .append(dependency.getArtifactId())
            .append(":")
            .append(dependency.getVersion());

        if (dependency.getClassifier() != null) {
            dependencyCoordinates.append(":").append(dependency.getClassifier());
        }

        return dependencyCoordinates.toString();
    }

    /**
     * The method will convert the camelCase names as MANIFEST.MF {@link Attributes.Name}
     * e.g.
     * if the camelCasedName is &quot;projectName&quot; - then this method will return &quot;Project-Name&quot;
     *
     * @param camelCasedName - the camel cased name that needs to be converted
     * @return converted {@link Attributes.Name} name
     */
    public static Attributes.Name attributeName(String camelCasedName) {
        return new Attributes.Name(WordUtils.capitalize(CaseFormat.LOWER_CAMEL
            .to(CaseFormat.LOWER_HYPHEN, camelCasedName), '-'));
    }

    public static String manifestTimestampFormat(Date date) {
        return simpleDateFormat.format(date);
    }
}
