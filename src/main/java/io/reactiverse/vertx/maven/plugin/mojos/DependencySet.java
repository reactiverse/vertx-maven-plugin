package io.reactiverse.vertx.maven.plugin.mojos;

import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DependencySet {

    /**
     * Set of dependencies to includes.
     */
    private List<String> includes;

    /**
     * Set of dependencies to excludes.
     */
    private List<String> excludes;

    /**
     * When specified as true, any include/exclude patterns which aren't used to filter an actual
     * artifact during package creation will cause the build to fail with an error. This is meant
     * to highlight obsolete inclusions or exclusions.
     */
    private boolean useStrictFiltering = false;

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
     * current dependency set. If true, includes/excludes/useTransitiveFiltering will apply
     * to transitive dependency artifacts in addition to the main project dependency artifacts.
     * If false, useTransitiveFiltering is meaningless, and includes/excludes only affect the
     * immediate dependencies of the project. By default, this value is true.
     */
    private boolean useTransitiveDependencies = true;

    /**
     * Determines whether the include/exclude patterns in this dependency set will be applied to
     * the transitive path of a given artifact. If true, and the current artifact is a transitive
     * dependency brought in by another artifact which matches an inclusion or exclusion pattern,
     * then the current artifact has the same inclusion/exclusion logic applied to it as well. By
     * default, this value is false. This means that includes/excludes only apply directly to the
     * current artifact, and not to the transitive set of artifacts which brought it in.
     */
    private boolean useTransitiveFiltering = false;

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
     * Get when specified as true, any include/exclude patterns which aren't used to filter an actual
     * artifact during package creation will cause the build to fail with an error. This is meant
     * to highlight obsolete inclusions or exclusions.
     *
     * @return boolean whether or not strict filtering is enabled
     */
    public boolean isUseStrictFiltering() {
        return this.useStrictFiltering;
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

    /**
     * Get determines whether the include/exclude patterns in this dependency set will be applied to
     * the transitive path of a given artifact. If true, and the current artifact is a transitive
     * dependency brought in by another artifact which matches an inclusion or exclusion pattern,
     * then the current artifact has the same inclusion/exclusion logic applied to it as well. By
     * default, this value is false. This means that includes/excludes only apply directly to the
     * current artifact, and not to the transitive set of artifacts which brought it in.
     *
     * @return whether or not transitive filtering is enabled
     */
    public boolean isUseTransitiveFiltering() {
        return this.useTransitiveFiltering;
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
     * Set when specified as true, any include/exclude patterns which aren't used to filter an actual
     * artifact during package creation will cause the build to fail with an error. This is meant
     * to highlight obsolete inclusions or exclusions.
     *
     * @param useStrictFiltering whether or not strict filtering is enabled
     * @return the current {@link DependencySetOptions}
     */
    public DependencySet setUseStrictFiltering(boolean useStrictFiltering) {
        this.useStrictFiltering = useStrictFiltering;
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
     * Set determines whether the include/exclude patterns in this dependency set will be applied to
     * the transitive path of a given artifact. If true, and the current artifact is a transitive
     * dependency brought in by another artifact which matches an inclusion or exclusion pattern,
     * then the current artifact has the same inclusion/exclusion logic applied to it as well. By
     * default, this value is false. This means that includes/excludes only apply directly to the
     * current artifact, and not to the transitive set of artifacts which brought it in.
     *
     * @param useTransitiveFiltering whether or not transitive filtering is enabled
     * @return the current {@link DependencySetOptions}
     */
    public DependencySet setUseTransitiveFiltering(boolean useTransitiveFiltering) {
        this.useTransitiveFiltering = useTransitiveFiltering;
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
