package io.reactiverse.vertx.maven.plugin.components.impl;

import jline.console.ConsoleReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the Prompter implementation.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PrompterImplTest {


    @Test
    public void testPrompt() throws IOException {
        String entries = "good thanks!\n";
        ByteArrayInputStream input = new ByteArrayInputStream(entries.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        PrompterImpl prompter = new PrompterImpl(input, output);
        String res = prompter.prompt("how are you? ");
        assertThat(res).isEqualToIgnoringCase("good thanks!");
        assertThat(output.toString()).contains("how are you?", "good thanks!");
    }

    @Test
    public void testPromptWithDefault() throws IOException {
        String entries = "good thanks!\n";
        ByteArrayInputStream input = new ByteArrayInputStream(entries.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        PrompterImpl prompter = new PrompterImpl(input, output);
        String res = prompter.promptWithDefaultValue("how are you? ", "not too bad");
        assertThat(res).isEqualToIgnoringCase("good thanks!");
        assertThat(output.toString()).contains("how are you?", "good thanks!");
    }


    @Test
    public void testPromptWithDefaultValueAndNoInput() throws IOException {
        String entries = "\n";
        ByteArrayInputStream input = new ByteArrayInputStream(entries.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        PrompterImpl prompter = new PrompterImpl(input, output);
        String res = prompter.promptWithDefaultValue("how are you? ", "not too bad");
        assertThat(res).isEqualToIgnoringCase("not too bad");
        assertThat(output.toString()).contains("how are you?");
    }

    @Test
    public void testPromptWithDefaultValueAndEmptyInput() throws IOException {
        String entries = "   \t\n";
        ByteArrayInputStream input = new ByteArrayInputStream(entries.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        PrompterImpl prompter = new PrompterImpl(input, output);
        String res = prompter.promptWithDefaultValue("how are you? ", "not too bad");
        assertThat(res).isEqualToIgnoringCase("not too bad");
        assertThat(output.toString()).contains("how are you?");
    }

}
