package io.reactiverse.vertx.maven.plugin.dependencies;

import org.apache.maven.model.Dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class VertxDependency {

    private String artifactId;
    private String groupId;
    private String scope;

    private String type;
    private String classifier;

    private String name;
    private String[] labels;


    public String getArtifactId() {
        return artifactId;
    }

    public VertxDependency setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public VertxDependency setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public VertxDependency setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String getType() {
        return type;
    }

    public VertxDependency setType(String type) {
        this.type = type;
        return this;
    }

    public String getClassifier() {
        return classifier;
    }

    public VertxDependency setClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    public String[] getLabels() {
        return labels;
    }

    public VertxDependency setLabels(String[] labels) {
        this.labels = labels;
        return this;
    }

    public String getName() {
        return name;
    }

    public VertxDependency setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> labels() {
        List<String> list = new ArrayList<>();
        if (labels != null) {
            Stream.of(labels).map(String::toLowerCase).forEach(list::add);
        }
        list.add(artifactId.toLowerCase());
        return list;
    }

    public Dependency toDependency() {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        if (scope != null && !scope.isEmpty()) {
            dependency.setScope(scope);
        }
        if (classifier != null && !classifier.isEmpty()) {
            dependency.setClassifier(classifier);
        }
        return dependency;
    }

    public String toCoordinates() {
        return getGroupId() + ":" + getArtifactId();
    }
}

