package ai.foresignal.source.kit;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * URL rules shared by the kit validators and adapters (prompt 073).
 *
 * <ul>
 *   <li>a spec URL is an absolute {@code https} URL with a host, no user info and no fragment;
 *   <li>it carries no credential-looking query parameter ({@code key}, {@code token}, {@code apikey}, …): a
 *       credential belongs in the spec's {@code credential} entry, where it is entered at runtime and redacted;
 *   <li>a link the upstream hands back (an OGC {@code next} link) must stay on the scheme, host and port it came
 *       from.
 * </ul>
 */
public final class KitUrls {

    /** Query parameter names that look like credentials, lowercase. */
    public static final Set<String> CREDENTIAL_PARAMS = Set.of("key", "apikey", "api_key", "api-key", "app_key",
            "appkey", "appid", "app_id", "token", "access_token", "accesstoken", "auth", "authorization", "password",
            "passwd", "pwd", "secret", "client_secret", "clientsecret", "sig", "signature", "subscription-key",
            "subscription_key", "sessionid");

    /** A long run of letters, digits, {@code -} and {@code _} with at least one digit: an opaque token value. */
    private static final Pattern TOKEN_VALUE = Pattern.compile("^(?=.*\\d)(?=.*[A-Za-z])[A-Za-z0-9_\\-]{32,}$");

    private KitUrls() {}

    /**
     * The problems of a spec URL, each prefixed with {@code field}.
     *
     * @param field the field path, for messages
     * @param url the URL text
     * @param required true when a missing URL is a problem
     * @return the problems; empty when the URL is acceptable
     */
    public static List<String> problems(String field, String url, boolean required) {
        if (url == null || url.isBlank()) {
            return required ? List.of(field + ": is required") : List.of();
        }
        URI uri;
        try {
            uri = new URI(url.strip());
        } catch (URISyntaxException e) {
            return List.of(field + ": is not a URL (" + e.getMessage() + ")");
        }
        java.util.ArrayList<String> problems = new java.util.ArrayList<>();
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            problems.add(field + ": must be an https URL");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            problems.add(field + ": must name a host");
        }
        if (uri.getRawUserInfo() != null) {
            problems.add(field + ": must not carry user info");
        }
        if (uri.getRawFragment() != null) {
            problems.add(field + ": must not carry a fragment");
        }
        String query = uri.getRawQuery();
        if (query != null) {
            for (String pair : query.split("&")) {
                int eq = pair.indexOf('=');
                String name = (eq < 0 ? pair : pair.substring(0, eq)).toLowerCase(Locale.ROOT);
                String value = eq < 0 ? "" : pair.substring(eq + 1);
                if (CREDENTIAL_PARAMS.contains(name)) {
                    problems.add(field + ": the query parameter '" + name + "' looks like a credential; declare it"
                            + " as the spec's credential instead");
                } else if (TOKEN_VALUE.matcher(value).matches()) {
                    problems.add(field + ": the value of query parameter '" + name + "' looks like a credential");
                }
            }
        }
        return problems;
    }

    /**
     * {@code url} moved under a test base URL as {@code {base}/{host}{path}?{query}} (the
     * {@value #BASE_URL_PROPERTY} property), or unchanged when {@code base} is blank.
     *
     * @param url the spec URL
     * @param base the test base URL, or null
     */
    public static String rebased(String url, String base) {
        if (base == null || base.isBlank() || url == null) {
            return url;
        }
        URI uri = URI.create(url.strip());
        String trimmed = base.strip();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        return trimmed + "/" + uri.getHost() + path + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
    }

    /** The test-only property moving every kit spec URL under one base. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.kits.base-url";

    /**
     * True when {@code next} has the scheme, host and port of {@code first} and no user info: a paging link the
     * upstream hands back may not move the source to another server.
     */
    public static boolean sameOrigin(URI first, URI next) {
        return next != null && next.isAbsolute() && first.getScheme() != null
                && first.getScheme().equalsIgnoreCase(next.getScheme())
                && first.getHost() != null && first.getHost().equalsIgnoreCase(next.getHost())
                && port(first) == port(next) && next.getRawUserInfo() == null;
    }

    private static int port(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(Objects.toString(uri.getScheme(), "")) ? 443 : 80;
    }

    /**
     * Appends {@code name=value} to {@code url}, URL-encoding both.
     *
     * @param url the URL so far
     * @param name the parameter name
     * @param value the value; nothing is appended when null
     */
    public static String withParam(String url, String name, Object value) {
        if (value == null) {
            return url;
        }
        String separator = url.indexOf('?') < 0 ? "?" : (url.endsWith("?") || url.endsWith("&") ? "" : "&");
        return url + separator + encode(name) + "=" + encode(String.valueOf(value));
    }

    /** {@code text} encoded for a query string ({@code %20} for a space, never {@code +}). */
    public static String encode(String text) {
        return java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
