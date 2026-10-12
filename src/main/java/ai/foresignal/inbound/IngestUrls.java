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
 */
public final class IngestUrls {

    /** Query parameters that carry credentials. */
    public static final Set<String> SECRET_PARAMETERS = Set.of("auth", "token");

    private static final Pattern PARAMETER = Pattern.compile("(^|&)([^=&]+)=([^&]*)");

    private IngestUrls() {}

    /** The request path and query with credential values masked. */
    public static String redacted(HttpServletRequest request) {
        return redacted(request.getRequestURI(), request.getQueryString());
    }

    /** {@code path?query} with credential values masked; {@code query} may be null. */
    public static String redacted(String path, String query) {
        if (query == null || query.isEmpty()) {
            return path;
        }
        return path + "?" + redactQuery(query);
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
