package io.fabric8.vertx.maven.plugin.dependencies;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class VertxDependencies {

    public static List<VertxDependency> get() {
        ObjectMapper mapper = new ObjectMapper()
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        URL url = VertxDependencies.class.getClassLoader().getResource("dependencies.json");
        try {
            return mapper.readValue(url, new TypeReference<List<VertxDependency>>() {
                // Do nothing.
            });
        } catch (IOException e) {
            throw new RuntimeException("Unable to load the dependencies.json file", e);
        }
    }
}
