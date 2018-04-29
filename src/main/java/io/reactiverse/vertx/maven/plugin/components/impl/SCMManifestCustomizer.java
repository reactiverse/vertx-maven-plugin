package io.reactiverse.vertx.maven.plugin.components.impl;

import io.reactiverse.vertx.maven.plugin.components.ManifestCustomizerService;
import io.reactiverse.vertx.maven.plugin.model.ExtraManifestKeys;
import io.reactiverse.vertx.maven.plugin.mojos.PackageMojo;
import io.reactiverse.vertx.maven.plugin.utils.ScmSpy;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.codehaus.plexus.component.annotations.Component;

import java.io.File;
import java.io.IOException;
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

        Scm scm = project.getScm();
        if (mojo.isSkipScmMetadata() || scm == null) {
            return attributes;
        }

        String connectionUrl = addAttributesFromProject(attributes, scm);

        if (mojo.getScmManager() != null && connectionUrl != null) {
            try {
                addAttributeFromScmManager(mojo, attributes, connectionUrl, project.getBasedir());
            } catch (Exception e) {
                mojo.getLog().warn("Error while getting SCM Metadata `" + e.getMessage() + "`");
                mojo.getLog().warn("SCM metadata ignored");
                mojo.getLog().debug(e);
            }
        }
        return attributes;
    }

    private void addAttributeFromScmManager(PackageMojo mojo, Map<String, String> attributes, String connectionUrl, File baseDir) throws IOException, ScmException {
        ScmSpy scmSpy = new ScmSpy(mojo.getScmManager());
        Map<ExtraManifestKeys, String> scmChangeLogMap = scmSpy.getChangeLog(connectionUrl, baseDir);
        scmChangeLogMap.forEach((key, value) -> attributes.put(key.header(), value));
    }

    private String addAttributesFromProject(Map<String, String> attributes, Scm scm) {
        String connectionUrl = scm.getConnection() == null ? scm.getDeveloperConnection() : scm.getConnection();
        if (scm.getUrl() != null) {
            attributes.put(ExtraManifestKeys.SCM_URL.header(), scm.getUrl());
        }

        if (scm.getTag() != null) {
            attributes.put(ExtraManifestKeys.SCM_TAG.header(), scm.getTag());
        }
        return connectionUrl;
    }
}
