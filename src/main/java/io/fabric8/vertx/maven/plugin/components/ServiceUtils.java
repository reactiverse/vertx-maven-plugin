package io.fabric8.vertx.maven.plugin.components;

import io.fabric8.vertx.maven.plugin.mojos.Archive;
import io.fabric8.vertx.maven.plugin.mojos.DependencySet;
import io.fabric8.vertx.maven.plugin.mojos.FileSet;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.resolve.ScopeFilter;

import java.util.*;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ServiceUtils {

    public static Archive getDefaultFatJar() {
        Archive archive = new Archive();
        DependencySet all = new DependencySet();
        archive.addDependencySet(all);
        archive.setIncludeClasses(true);
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

        if (Artifact.SCOPE_COMPILE.equals(scope)) {
            scopes.addAll(Arrays.asList("compile", "provided", "system"));
        }
        if (Artifact.SCOPE_PROVIDED.equals(scope)) {
            scopes.add("provided");
        }
        if (Artifact.SCOPE_RUNTIME.equals(scope)) {
            scopes.addAll(Arrays.asList("compile", "runtime"));
        }
        if (Artifact.SCOPE_SYSTEM.equals(scope)) {
            scopes.add("system");
        }
        if (Artifact.SCOPE_TEST.equals(scope)) {
            scopes.addAll(Arrays.asList("compile", "provided", "runtime", "system", "test"));
        }

        return ScopeFilter.including(scopes);
    }
}
