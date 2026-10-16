package ai.foresignal.source.support;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Streams the elements of one array inside a JSON document (prompt 094, 2026-10 review): Jackson's streaming
 * parser walks to the array at a property path ({@code features}, {@code ports}, {@code results.bindings}) and
 * reads one element tree at a time, so a reference dataset is never held as a whole document tree, and the
 * records built from it go straight to a {@link ReferenceSupport.ChunkEmitter}.
 */
public final class ReferenceStreams {

    /**
     * Reads one element tree at a time from a parser positioned inside the array. Jackson 3 enables
     * {@code FAIL_ON_TRAILING_TOKENS} by default, which refuses a tree read that leaves the next element behind
     * it; streaming needs it off (as in {@code reference.GeoJsonFeatures}).
     */
    private static final JsonMapper JSON = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    private ReferenceStreams() {}

    /**
     * Calls {@code consumer} with each object element of the array at {@code path}, in document order;
     * non-object elements are skipped.
     *
     * @param body the JSON document
     * @param what the document's name, for error messages
     * @param path the property names leading from the root object to the array, for example
     *     {@code "results", "bindings"}
     * @return the number of object elements read
     * @throws UpstreamFormatException when the document does not parse or has no array at {@code path}
     */
    public static int forEachElement(byte[] body, String what, Consumer<JsonNode> consumer, String... path) {
        if (path.length == 0) {
            throw new IllegalArgumentException("a path names at least one property");
        }
        int[] count = {0};
        boolean found;
        try (JsonParser parser = JSON.createParser(body)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new UpstreamFormatException(what + " is not a JSON object", sample(body));
            }
            found = walk(parser, path, 0, consumer, count);
        } catch (JacksonException e) {
            throw new UpstreamFormatException(what + " could not be parsed: " + e.getOriginalMessage(), sample(body),
                    e);
        }
        if (!found) {
            throw new UpstreamFormatException(what + " has no " + String.join(".", path) + " array", "");
        }
        return count[0];
    }

    /** Walks the object the parser is in; true once the array at {@code path} was streamed. */
    private static boolean walk(JsonParser parser, String[] path, int depth, Consumer<JsonNode> consumer,
            int[] count) {
        boolean found = false;
        while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
            String name = parser.currentName();
            JsonToken value = parser.nextToken();
            boolean onPath = !found && path[depth].equals(name);
            if (onPath && depth == path.length - 1 && value == JsonToken.START_ARRAY) {
                found = true;
                JsonToken token;
                while ((token = parser.nextToken()) != JsonToken.END_ARRAY && token != null) {
                    if (token == JsonToken.START_OBJECT) {
                        consumer.accept(JSON.readTree(parser));
                        count[0]++;
                    } else {
                        parser.skipChildren();
                    }
                }
            } else if (onPath && depth < path.length - 1 && value == JsonToken.START_OBJECT) {
                found = walk(parser, path, depth + 1, consumer, count);
            } else {
                parser.skipChildren();
            }
        }
        return found;
    }

    private static String sample(byte[] body) {
        return new String(body, 0, Math.min(500, body.length), StandardCharsets.UTF_8);
    }
}
