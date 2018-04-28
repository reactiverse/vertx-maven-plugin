package io.reactiverse.vertx.maven.plugin.components.impl;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of {@link GroovyExtensionCombiner}.
 */
public class GroovyExtensionCombinerTest {

    @Test
    public void testSimple() {
        List<String> merge = GroovyExtensionCombiner.merge("project-name", "1.0", ImmutableList.of("a.A", "b.B"), ImmutableList.of(
            ImmutableList.of("a.A", "c.C"), ImmutableList.of("d.D")));
        assertThat(merge).containsExactly("moduleName=project-name", "moduleVersion=1.0");
    }

    @Test
    public void testWithExtensions() {
        List<String> merge = GroovyExtensionCombiner.merge("project-name", "1.0",
            ImmutableList.of("staticExtensionClasses: a.A, b.B", "extensionClasses: c.C"), null);
        assertThat(merge).contains("moduleName=project-name", "moduleVersion=1.0",
            "extensionClasses=c.C", "staticExtensionClasses=a.A,b.B").hasSize(4);

    }

}
