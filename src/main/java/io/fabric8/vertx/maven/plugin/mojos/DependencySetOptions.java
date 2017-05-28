package io.fabric8.vertx.maven.plugin.mojos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DependencySetOptions {

    /**
     * Sets of included files.
     */
    private List<String> includes;

    /**
     * Sets of excluded files.
     */
    private List<String> excludes;

    /**
     * Whether standard exclusion patterns, such as those matching CVS and Subversion
     * metadata files, signature files should be used when calculating the files affected by this set.
     * The default value is true.
     */
    private boolean useDefaultExcludes = true;


    public void addExclude(String string) {
        getExcludes().add(string);
    }

    public void addInclude(String string) {
        getIncludes().add(string);
    }

    public List<String> getExcludes() {
        if (this.excludes == null) {
            this.excludes = new ArrayList<>();
        }

        return this.excludes;
    }

    public List<String> getIncludes() {
        if (this.includes == null) {
            this.includes = new ArrayList<>();
        }

        return this.includes;
    }


    /**
     * Get whether standard exclusion patterns, such as those matching CVS and Subversion metadata
     * files, signature files should be used when calculating the files affected by this set.
     * The default value is true.
     *
     * @return whether of not default excluded are used when copying files.
     */
    public boolean isUseDefaultExcludes() {
        return this.useDefaultExcludes;
    }

    public void removeExclude(String string) {
        getExcludes().remove(string);
    }

    public void removeInclude(String string) {
        getIncludes().remove(string);
    }

    /**
     * Set the set of file and/or directory patterns for matching items to be excluded from an archive
     * as it is embedded. Each item is specified as &lt;exclude&gt;some/path&lt;/exclude&gt;.
     *
     * @param excludes the list of excludes
     */
    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    /**
     * Set set of file and/or directory patterns for matching items to be included from an archive
     * as it is embedded. Each item is specified as &lt;include&gt;some/path&lt;/include&gt;.
     *
     * @param includes the list of includes
     */
    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    /**
     * Set whether standard exclusion patterns, such as those matching CVS and Subversion
     * metadata files, signature files , should be used when calculating the files affected by this set.
     * The default value is true.
     *
     * @param useDefaultExcludes whether or not the default excludes should be used when computing the file set.
     */
    public void setUseDefaultExcludes(boolean useDefaultExcludes) {
        this.useDefaultExcludes = useDefaultExcludes;
    }
}
