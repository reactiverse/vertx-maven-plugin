package io.reactiverse.vertx.maven.plugin.components.impl;

import io.reactiverse.vertx.maven.plugin.components.ManifestCustomizerService;
import io.reactiverse.vertx.maven.plugin.components.ServiceFileCombiner;
import io.reactiverse.vertx.maven.plugin.mojos.AbstractVertxMojo;
import io.reactiverse.vertx.maven.plugin.mojos.PackageMojo;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@Component(
    role = ManifestCustomizerService.class,
    hint = "project"
)
public class ProjectManifestCustomizer implements ManifestCustomizerService {

    private static String DEFAULT_DATE_PATTERN = "yyyyMMdd HH:mm:ss z";

    private static final SimpleDateFormat simpleDateFormat;

    static {
        simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
    }

    @Override
    public Map<String, String> getEntries(PackageMojo mojo, MavenProject project) {
        Map<String, String> attributes = new HashMap<>();
        Model model = project.getModel();

        attributes.put("Project-Name",
            model.getName() == null ? model.getArtifactId() : model.getName());
        attributes.put("Project-Group", model.getGroupId());
        attributes.put("Project-Version", model.getVersion());
        attributes.put("Build-Timestamp", manifestTimestampFormat(new Date()));

        if (project.getUrl() != null) {
            attributes.put("Project-Url", model.getUrl());
        }

        // TODO get the filtered lists.
        List<Dependency> dependencies = model.getDependencies();
        if (dependencies != null && !dependencies.isEmpty()) {
            String deps = dependencies.stream()
                .filter(d -> "compile".equals(d.getScope()) || null == d.getScope())
                .map(ProjectManifestCustomizer::asCoordinates)
                .collect(Collectors.joining(" "));
            attributes.put("Project-Dependencies", deps);
        }

        return attributes;
    }

    public static String manifestTimestampFormat(Date date) {
        return simpleDateFormat.format(date);
    }

    /**
     * utility method to return {@link Dependency} as G:V:A:C maven coordinates
     *
     * @param dependency - the maven {@link Dependency} whose coordinates need to be computed
     * @return - the {@link Dependency} info as G:V:A:C string
     */
    private static String asCoordinates(Dependency dependency) {
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

}
