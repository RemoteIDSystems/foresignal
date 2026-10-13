package ai.foresignal.mapping;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.Jackson3JsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.Jackson3MappingProvider;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.MissingNode;

/**
 * The JSONPath dialect of a {@code RecordMapping} (prompt 059): Jayway JsonPath 3.0.0 over Jackson 3 trees.
 *
 * <p>The configuration always names the library's own {@link Jackson3JsonNodeJsonProvider} and {@link
 * Jackson3MappingProvider}, so the json-smart default (excluded from the application) is never touched. Reads
 * suppress exceptions: a path that does not resolve reads as absent, never as an error.
 *
 * <p>Jayway semantics, not RFC 9535: {@code $} is the document (or the current item under {@code select}),
 * {@code ['a-b']} quotes a member name, {@code [0]} and {@code [*]} index arrays. Refused when compiled: deep scan
 * ({@code ..}), any function call (such as {@code length()} or {@code min()}, in a filter or at the end of a path),
 * and regex filters ({@code =~}), so a mapping can neither scan unbounded trees nor run patterns.
 */
public final class MappingJsonPath {

    /** A function call outside quotes: an identifier followed by an opening parenthesis. */
    private static final Pattern FUNCTION_CALL = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*\\s*\\(");

    private final Configuration configuration;

    /** A dialect over {@code json}. */
    public MappingJsonPath(JsonMapper json) {
        this.configuration = Configuration.builder()
                .jsonProvider(new Jackson3JsonNodeJsonProvider(json))
                .mappingProvider(new Jackson3MappingProvider(json))
                .options(Option.SUPPRESS_EXCEPTIONS)
                .build();
    }

    /** The Jayway configuration (Jackson 3 providers, exceptions suppressed). */
    public Configuration configuration() {
        return configuration;
    }

    /**
     * Compiles {@code path}.
     *
     * @return the compiled path
     * @throws IllegalArgumentException with a short reason when the path is not allowed or not valid
     */
    public static JsonPath compile(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("must be a JSONPath such as $.id");
        }
        String trimmed = path.strip();
        if (!trimmed.startsWith("$")) {
            throw new IllegalArgumentException("must start with $");
        }
        String unquoted = stripQuoted(trimmed);
        if (unquoted.contains("..")) {
            throw new IllegalArgumentException("deep scan (..) is not allowed");
        }
        if (FUNCTION_CALL.matcher(unquoted).find()) {
            throw new IllegalArgumentException("function calls are not allowed");
        }
        if (unquoted.contains("=~")) {
            throw new IllegalArgumentException("regex filters (=~) are not allowed");
        }
        try {
            return JsonPath.compile(trimmed);
        } catch (InvalidPathException | IllegalArgumentException e) {
            throw new IllegalArgumentException("is not a valid JSONPath: " + e.getMessage());
        }
    }

    /**
     * Reads {@code path} from {@code document}.
     *
     * @return the value, or a {@link MissingNode} when it does not resolve (an indefinite path returns an array)
     */
    public JsonNode read(JsonPath path, JsonNode document) {
        if (document == null || document.isMissingNode()) {
            return MissingNode.getInstance();
        }
        try {
            Object value = path.read(document, configuration);
            return value instanceof JsonNode node ? node : MissingNode.getInstance();
        } catch (PathNotFoundException | ClassCastException e) {
            return MissingNode.getInstance();
        }
    }

    /** {@code text} with the contents of single- and double-quoted strings removed. */
    static String stripQuoted(String text) {
        StringBuilder out = new StringBuilder(text.length());
        char quote = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quote != 0) {
                if (c == '\\' && i + 1 < text.length()) {
                    i++;
                } else if (c == quote) {
                    quote = 0;
                    out.append(c);
                }
                continue;
            }
            if (c == '\'' || c == '"') {
                quote = c;
            }
            out.append(c);
        }
        return out.toString();
    }
}
