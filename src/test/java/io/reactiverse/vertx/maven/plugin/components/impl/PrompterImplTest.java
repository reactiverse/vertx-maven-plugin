package io.reactiverse.vertx.maven.plugin.components.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the Prompter implementation.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PrompterImplTest {

    private PipedInputStream input;
    private PrintWriter pipeToInput;
    private ByteArrayOutputStream output;

    @Before
    public void setUp() throws Exception {
        input = new PipedInputStream();
        pipeToInput = new PrintWriter(new PipedOutputStream(input), true);
        output = new ByteArrayOutputStream();
    }

    @Test
    public void testPrompt() throws IOException {
        pipeToInput.println("good thanks!");

        PrompterImpl prompter = new PrompterImpl(input, output);
        String res = prompter.prompt("how are you? ");
        assertThat(res).isEqualToIgnoringCase("good thanks!");
        assertThat(output.toString()).contains("how are you?", "good thanks!");
    }

    @Test
    public void testPromptWithDefault() throws IOException {
        pipeToInput.println("good thanks!");

        PrompterImpl prompter = new PrompterImpl(input, output);
        String res = prompter.promptWithDefaultValue("how are you? ", "not too bad");
        assertThat(res).isEqualToIgnoringCase("good thanks!");
        assertThat(output.toString()).contains("how are you?", "good thanks!");
    }


    @Test
    public void testPromptWithDefaultValueAndNoInput() throws IOException {
        pipeToInput.println();

        PrompterImpl prompter = new PrompterImpl(input, output);
        String res = prompter.promptWithDefaultValue("how are you? ", "not too bad");
        assertThat(res).isEqualToIgnoringCase("not too bad");
        assertThat(output.toString()).contains("how are you?");
    }

    @Test
    public void testPromptWithDefaultValueAndEmptyInput() throws IOException {
        pipeToInput.println("   \t");

        PrompterImpl prompter = new PrompterImpl(input, output);
        String res = prompter.promptWithDefaultValue("how are you? ", "not too bad");
        assertThat(res).isEqualToIgnoringCase("not too bad");
        assertThat(output.toString()).contains("how are you?");
    }
}
