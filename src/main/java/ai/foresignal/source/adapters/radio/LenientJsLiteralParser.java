package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads a JavaScript literal assignment such as {@code var kiwisdr_com = [ {...}, ... ];} as JSON (prompt 082:
 * {@code kiwisdr}). Pure.
 *
 * <ol>
 *   <li>{@code //} line comments and {@code /* *}{@code /} block comments are removed, but only outside string
 *       literals (a URL such as {@code "http://..."} is kept);
 *   <li>a leading {@code var|let|const name =} and a trailing {@code ;} are removed;
 *   <li>trailing commas before {@code ]} or {@code }} are removed, outside strings;
 *   <li>the rest is parsed by Jackson with single quotes and unquoted property names allowed.
 * </ol>
 * Input over {@value #MAX_BYTES} bytes is refused, so a runaway file cannot exhaust memory.
 */
public final class LenientJsLiteralParser {

    /** The largest input accepted. */
    public static final int MAX_BYTES = 8 * 1024 * 1024;

    private static final JsonMapper LENIENT = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
            .build();

    private LenientJsLiteralParser() {}

    /**
     * Parses {@code body}.
     *
     * @throws UpstreamFormatException when the input is too large, empty or not a literal
     */
    public static JsonNode parse(byte[] body) {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("The JavaScript literal is empty", "");
        }
        if (body.length > MAX_BYTES) {
            throw new UpstreamFormatException("The JavaScript literal is " + body.length + " bytes, over the "
                    + MAX_BYTES + " byte cap", "");
        }
        return parse(new String(body, StandardCharsets.UTF_8));
    }

    /**
     * Parses {@code text}.
     *
     * @throws UpstreamFormatException when the text is too large, empty or not a literal
     */
    public static JsonNode parse(String text) {
        if (text == null || text.isBlank()) {
            throw new UpstreamFormatException("The JavaScript literal is empty", "");
        }
        if (text.length() > MAX_BYTES) {
            throw new UpstreamFormatException("The JavaScript literal is over the " + MAX_BYTES + " byte cap", "");
        }
        String json = removeTrailingCommas(stripAssignment(stripComments(text)));
        if (json.isBlank()) {
            throw new UpstreamFormatException("The JavaScript literal holds no value", "");
        }
        try {
            JsonNode node = LENIENT.readTree(json);
            if (node == null || node.isMissingNode()) {
                throw new UpstreamFormatException("The JavaScript literal holds no value", "");
            }
            return node;
        } catch (JacksonException e) {
            throw new UpstreamFormatException("The JavaScript literal does not parse: " + e.getOriginalMessage(), "", e);
        }
    }

    /** {@code text} without comments outside string literals. */
    static String stripComments(String text) {
        StringBuilder out = new StringBuilder(text.length());
        int i = 0;
        int n = text.length();
        while (i < n) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'') {
                int end = stringEnd(text, i);
                out.append(text, i, end);
                i = end;
            } else if (c == '/' && i + 1 < n && text.charAt(i + 1) == '/') {
                while (i < n && text.charAt(i) != '\n') {
                    i++;
                }
            } else if (c == '/' && i + 1 < n && text.charAt(i + 1) == '*') {
                int end = text.indexOf("*/", i + 2);
                i = end < 0 ? n : end + 2;
                out.append(' ');
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    /** {@code text} without a leading {@code var|let|const name =} and a trailing {@code ;}. */
    static String stripAssignment(String text) {
        String value = text.strip();
        for (String keyword : new String[] {"var ", "let ", "const "}) {
            if (value.startsWith(keyword)) {
                int eq = value.indexOf('=');
                if (eq < 0) {
                    throw new UpstreamFormatException("The JavaScript assignment has no '='", "");
                }
                value = value.substring(eq + 1).strip();
                break;
            }
        }
        while (value.endsWith(";")) {
            value = value.substring(0, value.length() - 1).strip();
        }
        return value;
    }

    /** {@code text} without commas that precede only whitespace and a closing bracket, outside strings. */
    static String removeTrailingCommas(String text) {
        StringBuilder out = new StringBuilder(text.length());
        int i = 0;
        int n = text.length();
        while (i < n) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'') {
                int end = stringEnd(text, i);
                out.append(text, i, end);
                i = end;
                continue;
            }
            if (c == ',') {
                int j = i + 1;
                while (j < n && Character.isWhitespace(text.charAt(j))) {
                    j++;
                }
                if (j < n && (text.charAt(j) == ']' || text.charAt(j) == '}')) {
                    i++;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    /** The index just past the string literal that starts at {@code start}, or the end of the text. */
    private static int stringEnd(String text, int start) {
        char quote = text.charAt(start);
        int i = start + 1;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == quote) {
                return i + 1;
            }
            i++;
        }
        return text.length();
    }
}
