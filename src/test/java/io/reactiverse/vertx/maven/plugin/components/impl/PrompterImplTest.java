package io.reactiverse.vertx.maven.plugin.components.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeFalse;

/**
 * Tests the Prompter implementation.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PrompterImplTest {

    private PipedInputStream input;
    private PrintWriter pipeToInput;
    private ByteArrayOutputStream output;
    private PrompterImpl prompter;

    @Before
    public void setUp() throws Exception {
        assumeFalse(SystemUtils.IS_OS_WINDOWS);
        input = new PipedInputStream();
        pipeToInput = new PrintWriter(new PipedOutputStream(input), true);
        output = new ByteArrayOutputStream();
        prompter = new PrompterImpl(input, output);
    }

    @After
    public void tearDown() throws Exception {
        IOUtils.closeQuietly(input, pipeToInput, output);
        if (prompter != null) {
            IOUtils.closeQuietly(prompter.getConsole().getTerminal());
        }
    }

    @Test
    public void testPrompt() throws IOException {
        pipeToInput.println("good thanks!");

        String res = prompter.prompt("how are you? ");
        assertThat(res).isEqualToIgnoringCase("good thanks!");
        assertThat(output.toString()).contains("how are you?", "good thanks!");
    }

    @Test
    public void testPromptWithDefault() throws IOException {
        pipeToInput.println("good thanks!");

        String res = prompter.promptWithDefaultValue("how are you? ", "not too bad");
        assertThat(res).isEqualToIgnoringCase("good thanks!");
        assertThat(output.toString()).contains("how are you?", "good thanks!");
    }


    @Test
    public void testPromptWithDefaultValueAndNoInput() throws IOException {
        pipeToInput.println();

        String res = prompter.promptWithDefaultValue("how are you? ", "not too bad");
        assertThat(res).isEqualToIgnoringCase("not too bad");
        assertThat(output.toString()).contains("how are you?");
    }

    @Test
    public void testPromptWithDefaultValueAndEmptyInput() throws IOException {
        pipeToInput.println("   \t");

        String res = prompter.promptWithDefaultValue("how are you? ", "not too bad");
        assertThat(res).isEqualToIgnoringCase("not too bad");
        assertThat(output.toString()).contains("how are you?");
    }
}
