package ai.foresignal.source.spi;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;

/**
 * One upstream HTTP response, already read and capped.
 *
 * @param status HTTP status code
 * @param headers response headers, keys lowercased
 * @param body the bytes read, never null
 * @param contentType the declared content type, or null
 * @param truncated true when the body hit the descriptor's byte cap and is incomplete
 * @param notModified true when the upstream answered 304
 */
public record UpstreamResponse(
        int status,
        Map<String, List<String>> headers,
        byte[] body,
        String contentType,
        boolean truncated,
        boolean notModified) {

    public UpstreamResponse {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        body = body == null ? new byte[0] : body;
    }

    /** A 304 with no body. */
    public static UpstreamResponse notModified(Map<String, List<String>> headers) {
        return new UpstreamResponse(304, headers, new byte[0], null, false, true);
    }

    /**
     * The body decoded as text.
     *
     * <p>Uses the charset from {@code Content-Type} when the upstream declares a usable one, and
     * UTF-8 otherwise. A truncated body may end mid-character, so decoding is lenient rather than
     * throwing.
     */
    public String bodyAsString() {
        return new String(body, charset());
    }

    /** The first value of a header, case-insensitively, or null. */
    public String header(String name) {
        List<String> values = headers.get(name.toLowerCase());
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    public int size() {
        return body.length;
    }

    private Charset charset() {
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        for (String part : contentType.split(";")) {
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, "charset=", 0, 8)) {
                String name = trimmed.substring(8).trim().replace("\"", "");
                try {
                    return Charset.forName(name);
                } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
                    return StandardCharsets.UTF_8;
                }
            }
        }
        return StandardCharsets.UTF_8;
    }
}
