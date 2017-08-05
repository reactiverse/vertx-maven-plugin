package io.fabric8.vertx.maven.plugin.components.impl;

import io.fabric8.vertx.maven.plugin.components.ManifestCustomizerService;
import io.fabric8.vertx.maven.plugin.model.ExtraManifestKeys;
import io.fabric8.vertx.maven.plugin.mojos.PackageMojo;
import io.fabric8.vertx.maven.plugin.utils.ScmSpy;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@Component(
    role = ManifestCustomizerService.class,
    hint = "scm"
)
public class SCMManifestCustomizer implements ManifestCustomizerService {

    @Override
    public Map<String, String> getEntries(PackageMojo mojo, MavenProject project) {
        Map<String, String> attributes = new HashMap<>();

        if (mojo.isSkipScmMetadata()) {
            return attributes;
        }

        //Add SCM Metadata only when <scm> is configured in the pom.xml
        if (project.getScm() != null) {
            Scm scm = project.getScm();
            String connectionUrl = scm.getConnection() == null ? scm.getDeveloperConnection() : scm.getConnection();

            if (scm.getUrl() != null) {
                attributes.put("Scm-Url", scm.getUrl());
            }

            if (scm.getTag() != null) {
                attributes.put("Scm-Tag", scm.getTag());
            }
            if (mojo.getScmManager() != null && connectionUrl != null) {
                try {
                    //SCM metadata
                    File baseDir = project.getBasedir();
                    ScmSpy scmSpy = new ScmSpy(mojo.getScmManager());

                    Map<String, String> scmChangeLogMap = scmSpy.getChangeLog(connectionUrl, baseDir);

                    if (!scmChangeLogMap.isEmpty()) {
                        attributes.put("Scm-Type",
                            scmChangeLogMap.get(ExtraManifestKeys.scmType.name()));
                        attributes.put("Scm-Revision",
                            scmChangeLogMap.get(ExtraManifestKeys.scmRevision.name()));
                        attributes.put("Last-Commit-Timestamp",
                            scmChangeLogMap.get(ExtraManifestKeys.lastCommitTimestamp.name()));
                        attributes.put("Author",
                            scmChangeLogMap.get(ExtraManifestKeys.author.name()));
                    }

                } catch (Exception e) {
                    mojo.getLog().warn("Error while getting SCM Metadata `" + e.getMessage() + "`");
                    mojo.getLog().warn("SCM metadata ignored");
                    mojo.getLog().debug(e);
                }
            }
        }
        return attributes;
    }
}
