package ai.elaro.collect;

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
            Set.of("authorization", "proxy-authorization", "cookie", "set-cookie", "x-elaro-ingest-key");

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

    /** {@code text} with every occurrence of a credential value replaced. */
    public String redact(String text) {
        if (text == null || text.isEmpty() || secrets.isEmpty()) {
            return text;
        }
        String result = text;
        for (String secret : secrets) {
            result = result.replace(secret, MASK);
        }
        return result;
    }

    /**
     * A header value safe to store.
     *
     * <p>{@code Authorization} and friends are replaced entirely rather than searched for known
     * secrets: a bearer token Elaro did not store is still a bearer token.
     */
    public String redactHeader(String name, String value) {
        if (name != null && ALWAYS_REDACTED_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
            return MASK;
        }
        return redact(value);
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
