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
package io.reactiverse.vertx.maven.plugin.components;

import io.reactiverse.vertx.maven.plugin.mojos.Archive;
import io.reactiverse.vertx.maven.plugin.mojos.DependencySet;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.resolve.ScopeFilter;

import java.util.*;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ServiceUtils {

    private ServiceUtils() {
        // avoid direct instantiation.
    }

    public static Archive getDefaultFatJar() {
        Archive archive = new Archive();
        archive.addDependencySet(DependencySet.ALL);
        archive.setIncludeClasses(true);
        archive.addFileCombinationPattern("META-INF/services/*");
        archive.addFileCombinationPattern("META-INF/spring.*");
        archive.addFileCombinationPattern("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat");
        return archive;
    }

    public static Set<Artifact> filterArtifacts(Set<Artifact> artifacts, List<String> includes,
                                                List<String> excludes,
                                                boolean actTransitively, Log logger,
                                                ArtifactFilter... additionalFilters) {

        final AndArtifactFilter filter = new AndArtifactFilter();

        if (additionalFilters != null && additionalFilters.length > 0) {
            for (final ArtifactFilter additionalFilter : additionalFilters) {
                if (additionalFilter != null) {
                    filter.add(additionalFilter);
                }
            }
        }

        if (!includes.isEmpty()) {
            final ArtifactFilter includeFilter = new PatternIncludesArtifactFilter(includes, actTransitively);
            filter.add(includeFilter);
        }

        if (!excludes.isEmpty()) {
            final ArtifactFilter excludeFilter = new PatternExcludesArtifactFilter(excludes, actTransitively);
            filter.add(excludeFilter);
        }

        Set<Artifact> copy = new LinkedHashSet<>(artifacts);
        for (final Iterator<Artifact> it = copy.iterator(); it.hasNext(); ) {

            final Artifact artifact = it.next();

            if (!filter.include(artifact)) {
                it.remove();
                if (logger.isDebugEnabled()) {
                    logger.debug(artifact.getId() + " was removed by one or more filters.");
                }
            }
        }

        return copy;
    }

    public static ScopeFilter newScopeFilter(String scope) {
        Set<String> scopes = new HashSet<>();

        String provided = "provided";
        String compile = "compile";
        String system = "system";

        if (Artifact.SCOPE_COMPILE.equals(scope)) {
            scopes.addAll(Arrays.asList(compile, provided, system));
        }
        if (Artifact.SCOPE_PROVIDED.equals(scope)) {
            scopes.add(provided);
        }
        if (Artifact.SCOPE_RUNTIME.equals(scope)) {
            scopes.addAll(Arrays.asList(compile, "runtime"));
        }
        if (Artifact.SCOPE_SYSTEM.equals(scope)) {
            scopes.add(system);
        }
        if (Artifact.SCOPE_TEST.equals(scope)) {
            scopes.addAll(Arrays.asList(compile, provided, "runtime", system, "test"));
        }

        return ScopeFilter.including(scopes);
    }
}
