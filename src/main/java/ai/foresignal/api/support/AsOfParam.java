package ai.foresignal.api.support;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.StringJoiner;

/**
 * The {@code asOf} parameter of tiles and TileJSON (prompt 037), normalized to whole seconds.
 *
 * <p>Cacheable time-travel URLs need one spelling per instant, so every other form (fractional
 * seconds, an offset other than {@code Z}) is answered {@code 301} to the canonical URL, as
 * Weather-Imagery-Design §6.1 does for time keys. The canonical form is
 * {@code yyyy-MM-ddTHH:mm:ssZ} ({@link Instant#toString()} of a whole second); fractions are
 * truncated.
 *
 * @param instant the instant, truncated to whole seconds
 * @param canonical its canonical text
 * @param redirect true when the request spelled it differently and must be redirected
 */
public record AsOfParam(Instant instant, String canonical, boolean redirect) {

    /** The parameter name. */
    public static final String NAME = "asOf";

    /**
     * Parses {@code raw} (RFC 3339); null when absent.
     *
     * @throws ai.foresignal.api.error.ValidationException when malformed
     */
    public static AsOfParam parse(String raw) {
        Instant parsed = QueryParams.instant(NAME, raw);
        if (parsed == null) {
            return null;
        }
        Instant whole = parsed.truncatedTo(ChronoUnit.SECONDS);
        String canonical = whole.toString();
        return new AsOfParam(whole, canonical, !canonical.equals(raw));
    }

    /**
     * The relative URL of {@code request} as the client sent it (the key-in-path form and a
     * {@code ticket} included, so the redirect keeps working), with every {@code asOf} pair replaced by
     * the canonical one.
     */
    public static String redirectLocation(HttpServletRequest request, String canonical) {
        HttpServletRequest original = unwrap(request);
        String query = original.getQueryString();
        if (query == null || query.isEmpty()) {
            query = fromParameters(original);
        }
        StringJoiner out = new StringJoiner("&");
        boolean replaced = false;
        if (query != null && !query.isEmpty()) {
            for (String pair : query.split("&")) {
                String name = pair.contains("=") ? pair.substring(0, pair.indexOf('=')) : pair;
                if (NAME.equals(name)) {
                    if (!replaced) {
                        out.add(NAME + "=" + canonical);
                        replaced = true;
                    }
                } else if (!pair.isEmpty()) {
                    out.add(pair);
                }
            }
        }
        if (!replaced) {
            out.add(NAME + "=" + canonical);
        }
        return original.getRequestURI() + "?" + out;
    }

    /** The query string rebuilt from the parameters, for requests that carry none (form-encoded). */
    private static String fromParameters(HttpServletRequest request) {
        StringJoiner out = new StringJoiner("&");
        request.getParameterMap().forEach((name, values) -> {
            for (String value : values) {
                out.add(URLEncoder.encode(name, StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        });
        return out.toString();
    }

    /** The innermost request: wrappers (key-in-path forwarding, ticket stripping) removed. */
    static HttpServletRequest unwrap(HttpServletRequest request) {
        ServletRequest current = request;
        while (current instanceof HttpServletRequestWrapper wrapper
                && wrapper.getRequest() instanceof HttpServletRequest) {
            current = wrapper.getRequest();
        }
        return (HttpServletRequest) current;
    }
}
