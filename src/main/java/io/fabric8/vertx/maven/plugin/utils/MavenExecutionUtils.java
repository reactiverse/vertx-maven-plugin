package io.fabric8.vertx.maven.plugin.utils;

import com.google.common.collect.ImmutableList;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MavenExecutionUtils {


    public static void execute(String phase, MavenProject project, MavenSession session, LifecycleExecutor executor,
                               PlexusContainer container) {
        MavenExecutionRequest request = getMavenExecutionRequest(session, phase);
        MavenSession newSession = getMavenSession(session, project, request, container);
        executor.execute(newSession);
    }

    private static MavenExecutionRequest getMavenExecutionRequest(MavenSession session, String phase) {
        MavenExecutionRequest request = DefaultMavenExecutionRequest.copy(session.getRequest());
        request.setStartTime(session.getStartTime());
        request.setExecutionListener(null);
        request.setGoals(ImmutableList.of(phase));
        return request;
    }


    private static MavenSession getMavenSession(MavenSession session, MavenProject project,
                                         MavenExecutionRequest request, PlexusContainer container) {
        MavenSession newSession = new MavenSession(container,
            session.getRepositorySession(),
            request,
            session.getResult());
        newSession.setAllProjects(session.getAllProjects());
        newSession.setCurrentProject(project);
        newSession.setParallel(session.isParallel());
        // Update project map to update the current project
        Map<String, MavenProject> projectMaps = new LinkedHashMap<>(session.getProjectMap());
        projectMaps.put(ArtifactUtils.key(project.getGroupId(), project.getArtifactId(),
            project.getVersion()), project);
        newSession.setProjectMap(projectMaps);

        /*
          Fake implementation of the project dependency graph, as we don't support reactor.
         */
        ProjectDependencyGraph graph = new ProjectDependencyGraph() {

            @Override
            public List<MavenProject> getSortedProjects() {
                return ImmutableList.of(project);
            }

            @Override
            public List<MavenProject> getDownstreamProjects(MavenProject project, boolean transitive) {
                return Collections.emptyList();
            }

            @Override
            public List<MavenProject> getUpstreamProjects(MavenProject project, boolean transitive) {
                return Collections.emptyList();
            }
        };
        newSession.setProjectDependencyGraph(graph);
        newSession.setProjects(ImmutableList.of(project));
        return newSession;
    }

}
