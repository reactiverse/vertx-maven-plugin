package io.reactiverse.vertx.maven.plugin.mojos;

import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DependencySet {

    /**
     * Includes all compile dependencies.
     */
    public static final DependencySet ALL = new DependencySet();

    /**
     * Set of dependencies to includes.
     */
    private List<String> includes;

    /**
     * Set of dependencies to excludes.
     */
    private List<String> excludes;

    /**
     * Sets the dependency scope for this {@link DependencySet}.
     * Default scope value is "runtime".
     */
    private String scope = "runtime";

    /**
     * The dependency set options
     */
    private DependencySetOptions options;


    /**
     * Determines whether transitive dependencies will be included in the processing of the
     * current dependency set. If true, includes/excludes will apply
     * to transitive dependency artifacts in addition to the main project dependency artifacts.
     * If false, useTransitiveFiltering is meaningless, and includes/excludes only affect the
     * immediate dependencies of the project. By default, this value is true.
     */
    private boolean useTransitiveDependencies = true;

    public DependencySet addExclude(String string) {
        getExcludes().add(string);
        return this;
    }


    public DependencySet addInclude(String string) {
        getIncludes().add(string);
        return this;
    }

    public List<String> getExcludes() {
        if (this.excludes == null) {
            this.excludes = new java.util.ArrayList<>();
        }

        return this.excludes;
    }

    public List<String> getIncludes() {
        if (this.includes == null) {
            this.includes = new java.util.ArrayList<>();
        }

        return this.includes;
    }

    /**
     * Get sets the dependency scope for this {@link DependencySet}.
     * Default scope value is "runtime".
     *
     * @return String the scope
     */
    public String getScope() {
        return this.scope;
    }

    /**
     * Get determines whether transitive dependencies will be included in the processing of
     * the current dependency set. If true, includes/excludes/useTransitiveFiltering
     * will apply to transitive dependency artifacts. If false, useTransitiveFiltering is
     * meaningless, and includes/excludes only affect the immediate dependencies of the project.
     * By default, this value is true.
     *
     * @return boolean whether or not transitive dependencies are embedded
     */
    public boolean isUseTransitiveDependencies() {
        return this.useTransitiveDependencies;
    }

    public DependencySet removeExclude(String string) {
        getExcludes().remove(string);
        return this;
    }

    public DependencySet removeInclude(String string) {
        getIncludes().remove(string);
        return this;
    }

    /**
     * Set when &lt;exclude&gt; subelements are present, they define a set of dependency artifact
     * coordinates to exclude. If none is present, then &lt;excludes&gt; represents no exclusions.
     * <p>
     * Artifact coordinates may be given in simple groupId:artifactId form, or they may be fully
     * qualified in the form groupId:artifactId:type[:classifier]:version.
     * Additionally, wildcards can be used, as in *:vertx-*.
     *
     * @param excludes the list of excludes
     * @return the current {@link DependencySetOptions}
     */
    public DependencySet setExcludes(List<String> excludes) {
        this.excludes = excludes;
        return this;
    }

    /**
     * Set when &lt;include&gt; subelements are present, they define a set of artifact coordinates
     * to include. If none is present, then &lt;includes&gt; represents all valid values.
     * <p>
     * Artifact coordinates may be given in simple groupId:artifactId form, or they may be fully
     * qualified in the form groupId:artifactId:type[:classifier]:version. Additionally, wildcards
     * can be used, as in *:vertx-*.
     *
     * @param includes the list of includes
     * @return the current {@link DependencySetOptions}
     */
    public DependencySet setIncludes(List<String> includes) {
        this.includes = includes;
        return this;
    }

    /**
     * Set sets the dependency scope for this {@link DependencySet}.
     * Default scope value is "runtime".
     *
     * @param scope the scope, must not be {@code null}
     * @return the current {@link DependencySetOptions}
     */
    public DependencySet setScope(String scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Set determines whether transitive dependencies will be included in the processing of
     * the current dependency set. If true, includes/excludes/useTransitiveFiltering
     * will apply to transitive dependency artifacts in addition to the main project
     * dependency artifacts. If false, useTransitiveFiltering is meaningless, and
     * includes/excludes only affect the immediate dependencies of the project.
     * By default, this value is true.
     *
     * @param useTransitiveDependencies whether or not transitive dependencies are embedded
     * @return the current {@link DependencySetOptions}
     */
    public DependencySet setUseTransitiveDependencies(boolean useTransitiveDependencies) {
        this.useTransitiveDependencies = useTransitiveDependencies;
        return this;
    }

    /**
     * @return the dependency set options used to configure the included and excluded files.
     */
    public DependencySetOptions getOptions() {
        if (options == null) {
            return new DependencySetOptions();
        }
        return options;
    }

    /**
     * Sets the dependency set options used to configure the included and excluded files.
     *
     * @param options the options
     * @return the current {@link DependencySetOptions}
     */
    public DependencySet setOptions(DependencySetOptions options) {
        this.options = options;
        return this;
    }
}
