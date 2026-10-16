package ai.foresignal.collect;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Removes credentials from anything that will be stored or logged.
 *
 * <p>Many upstreams take their key in the query string, so a recorded URL is the most likely place
 * for a secret to leak: into a fetch log, a probe response, an error message, a bug report. The
 * redactor is given this source's actual credential values and replaces them wherever they appear —
 * query value, path segment, or header — rather than trying to guess which parameter names are
 * sensitive. Guessing by name fails the moment an upstream calls its key {@code token} or
 * {@code u}.
 *
 * <p>The one exception to "values, not names" is a pre-signed URL (prompt 091): its signature is a temporary
 * credential minted by the upstream, never stored, so the whole query of a URL carrying an AWS or Azure
 * signature parameter is masked instead.
 */
public final class UrlRedactor {

    /** What a redacted value is replaced with. */
    public static final String MASK = "***";

    /**
     * Headers always redacted whole, whatever they contain.
     *
     * <p>Lowercased for comparison.
     */
    private static final Set<String> ALWAYS_REDACTED_HEADERS =
            Set.of("authorization", "proxy-authorization", "cookie", "set-cookie", "x-foresignal-ingest-key");

    /** Shortest value worth masking; masking "1" would redact half of every URL. */
    private static final int MIN_SECRET_LENGTH = 4;

    private final List<String> secrets;

    /** @param secretValues the source's credential values, in any order */
    public UrlRedactor(Collection<String> secretValues) {
        Set<String> usable = new TreeSet<>(java.util.Comparator.comparingInt(String::length).reversed()
                .thenComparing(java.util.Comparator.naturalOrder()));
        if (secretValues != null) {
            for (String value : secretValues) {
                if (value != null && value.length() >= MIN_SECRET_LENGTH) {
                    usable.add(value);
                }
            }
        }
        // Longest first, so a secret that contains another is masked whole.
        this.secrets = List.copyOf(usable);
    }

    /** A redactor that masks nothing but the always-redacted headers. */
    public static UrlRedactor none() {
        return new UrlRedactor(List.of());
    }

    /** {@code uri} with every occurrence of a credential value replaced. */
    public String redact(URI uri) {
        return uri == null ? "" : redact(uri.toString());
    }

    /**
     * {@code text} with every occurrence of a credential value replaced, and the query string of every
     * pre-signed URL in it masked ({@link #maskPresignedQueries}).
     */
    public String redact(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = maskPresignedQueries(text);
        if (secrets.isEmpty()) {
            return result;
        }
        for (String secret : secrets) {
            result = result.replace(secret, MASK);
        }
        return result;
    }

    /**
     * A header value safe to store.
     *
     * <p>{@code Authorization} and friends are replaced entirely rather than searched for known
     * secrets: a bearer token Foresignal did not store is still a bearer token.
     */
    public String redactHeader(String name, String value) {
        if (name != null && ALWAYS_REDACTED_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
            return MASK;
        }
        return redact(value);
    }

    /** A URL with a query string: scheme, everything up to {@code ?}, then the query. */
    private static final java.util.regex.Pattern URL_WITH_QUERY =
            java.util.regex.Pattern.compile("(https?://[^\\s\"'<>?]+)\\?([^\\s\"'<>#]*)");

    /**
     * A query that carries a pre-signed signature: an AWS SigV4 pre-signed URL ({@code X-Amz-Signature},
     * {@code X-Amz-Credential}, {@code X-Amz-Security-Token}) or an Azure Blob SAS ({@code sig}).
     */
    private static final java.util.regex.Pattern PRESIGNED_PARAM = java.util.regex.Pattern.compile(
            "(?i)(?:^|&)(?:x-amz-signature|x-amz-credential|x-amz-security-token|sig)=");

    /**
     * {@code text} with the whole query string of every pre-signed URL replaced by {@value #MASK} (prompt 091:
     * OFAC's S3 redirect and the UN list's Azure SAS redirect carry a temporary credential in the query, which is
     * not a stored credential value, so the value-based masking cannot know it). Other URLs are unchanged.
     */
    public static String maskPresignedQueries(String text) {
        if (text == null || text.indexOf('?') < 0) {
            return text;
        }
        java.util.regex.Matcher matcher = URL_WITH_QUERY.matcher(text);
        StringBuilder out = null;
        while (matcher.find()) {
            if (!PRESIGNED_PARAM.matcher(matcher.group(2)).find()) {
                continue;
            }
            if (out == null) {
                out = new StringBuilder(text.length());
            }
            matcher.appendReplacement(out, java.util.regex.Matcher.quoteReplacement(matcher.group(1) + "?" + MASK));
        }
        if (out == null) {
            return text;
        }
        matcher.appendTail(out);
        return out.toString();
    }

    /** The JSON members of an OAuth token response whose string values are masked in recorded bodies. */
    private static final java.util.regex.Pattern TOKEN_MEMBERS = java.util.regex.Pattern.compile(
            "(\"(?:access_token|refresh_token|id_token)\"\\s*:\\s*\")[^\"]*(\")");

    /**
     * {@code body} with the string values of the JSON members {@code access_token}, {@code refresh_token} and
     * {@code id_token} masked (prompt 081). A token an adapter obtained at run time (ACLED's, OpenSky's) is not a
     * stored credential value, so {@link #redact(String)} cannot know it; a recorded token response must still not
     * carry it.
     */
    public static String redactTokenMembers(String body) {
        if (body == null || body.isEmpty() || !body.contains("_token\"")) {
            return body;
        }
        return TOKEN_MEMBERS.matcher(body).replaceAll("$1" + MASK + "$2");
    }

    /** True when {@code name} is redacted whole regardless of its value. */
    public static boolean isAlwaysRedacted(String name) {
        return name != null && ALWAYS_REDACTED_HEADERS.contains(name.toLowerCase(Locale.ROOT));
    }

    /** How many credential values this redactor is masking. */
    public int secretCount() {
        return secrets.size();
    }
}
