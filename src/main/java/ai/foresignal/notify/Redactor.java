package ai.foresignal.notify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Removes a channel's secrets from anything stored or logged (prompt 018).
 *
 * <p>Several destinations embed secrets in the URL itself (Slack, Discord, Teams and Home Assistant
 * webhook URLs, the Telegram bot token in the path), so every error message and URL is passed through
 * {@link #redact} with the channel's secret values before it reaches {@code last_error}, a test
 * result or a log line. A secret that is a URL also masks its path, its query and every path segment
 * of eight or more characters, because receivers echo those parts on their own ("invalid token
 * …"). Longer secrets are replaced first, so a secret containing another is fully removed.
 */
public final class Redactor {

    /** What a secret is replaced with. */
    public static final String MASK = "***";

    /** The longest stored error. */
    public static final int MAX_ERROR = 500;

    /** Secrets shorter than this are left alone (they would mask ordinary words). */
    static final int MIN_SECRET = 4;

    /** URL path segments at least this long are treated as secret tokens. */
    static final int MIN_SEGMENT = 8;

    private Redactor() {}

    /** {@code text} with every value of {@code secrets} masked. */
    public static String redact(String text, Collection<String> secrets) {
        if (text == null || secrets == null || secrets.isEmpty()) {
            return text;
        }
        List<String> values = new ArrayList<>();
        for (String secret : secrets) {
            if (secret != null && secret.length() >= MIN_SECRET) {
                values.add(secret);
                values.addAll(urlParts(secret));
            }
        }
        values.sort(Comparator.comparingInt(String::length).reversed());
        String out = text;
        for (String value : values) {
            out = out.replace(value, MASK);
        }
        return out;
    }

    /** The path, query and long path segments of a URL secret; empty for anything else. */
    static List<String> urlParts(String secret) {
        List<String> parts = new ArrayList<>();
        if (!secret.startsWith("http://") && !secret.startsWith("https://")) {
            return parts;
        }
        try {
            java.net.URI uri = java.net.URI.create(secret.strip());
            String path = uri.getRawPath();
            if (path != null && path.length() >= MIN_SECRET) {
                parts.add(path);
                for (String segment : path.split("/")) {
                    if (segment.length() >= MIN_SEGMENT) {
                        parts.add(segment);
                    }
                }
            }
            if (uri.getRawQuery() != null && uri.getRawQuery().length() >= MIN_SECRET) {
                parts.add(uri.getRawQuery());
            }
        } catch (IllegalArgumentException e) {
            // not a URL after all
        }
        return parts;
    }

    /** {@link #redact} and cut to {@value #MAX_ERROR} characters, for {@code last_error}. */
    public static String error(String text, Collection<String> secrets) {
        String redacted = redact(text, secrets);
        if (redacted == null) {
            return null;
        }
        return redacted.length() <= MAX_ERROR ? redacted : redacted.substring(0, MAX_ERROR - 1) + "…";
    }
}
