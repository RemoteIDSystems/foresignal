package ai.foresignal.inbound;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds the redacted URL of an inbound push for {@code fetch_log.url_redacted} and log lines
 * (prompt 024): the values of the compatibility credentials {@code auth} and {@code token} are
 * replaced with {@code ***}, so a key passed in the query string is never stored or logged.
 *
 * <p>Prompt 039 adds the key-in-path ingest URLs: the {@code {key}} segment of
 * {@code /v1/ingest/{sourceId}/k/{key}/{format}} becomes {@link #REDACTED_KEY}, and a {@code key}
 * query parameter is masked like {@code auth} and {@code token}.
 */
public final class IngestUrls {

    /** Query parameters that carry credentials. */
    public static final Set<String> SECRET_PARAMETERS = Set.of("auth", "token", "key");

    /** What replaces a key carried in an ingest path. */
    public static final String REDACTED_KEY = "redacted";

    private static final Pattern PARAMETER = Pattern.compile("(^|&)([^=&]+)=([^&]*)");

    /** {@code /v1/ingest/{sourceId}/k/{key}} with the key in group 2. */
    private static final Pattern PATH_KEY = Pattern.compile("^(.*?/v1/ingest/[^/]+/k/)([^/?]+)");

    private IngestUrls() {}

    /** The request path and query with credential values masked. */
    public static String redacted(HttpServletRequest request) {
        return redacted(request.getRequestURI(), request.getQueryString());
    }

    /** {@code path?query} with credential values masked; {@code query} may be null. */
    public static String redacted(String path, String query) {
        String p = redactPath(path);
        if (query == null || query.isEmpty()) {
            return p;
        }
        return p + "?" + redactQuery(query);
    }

    /** {@code path} with the key of a key-in-path ingest URL replaced by {@link #REDACTED_KEY}; null stays null. */
    public static String redactPath(String path) {
        if (path == null || !path.contains("/k/")) {
            return path;
        }
        Matcher matcher = PATH_KEY.matcher(path);
        if (!matcher.find()) {
            return path;
        }
        return matcher.group(1) + REDACTED_KEY + path.substring(matcher.end());
    }

    /** The key carried in a key-in-path ingest URL, or null when {@code path} has none. */
    public static String pathKey(String path) {
        if (path == null || !path.contains("/k/")) {
            return null;
        }
        Matcher matcher = PATH_KEY.matcher(path);
        return matcher.find() ? matcher.group(2) : null;
    }

    /** The query string with the values of {@link #SECRET_PARAMETERS} masked. */
    public static String redactQuery(String query) {
        Matcher matcher = PARAMETER.matcher(query);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(2);
            String replacement = SECRET_PARAMETERS.contains(name.toLowerCase(Locale.ROOT))
                    ? matcher.group(1) + name + "=***"
                    : matcher.group(0);
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
