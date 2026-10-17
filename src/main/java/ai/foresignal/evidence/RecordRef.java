package ai.foresignal.evidence;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A record reference in the Client Contract grammar {@code record:{sourceId}/{externalId}[@{seq}]}, the
 * external id percent-encoded (prompt 116 uses it for observation refs).
 *
 * @param sourceId the source
 * @param externalId the external id
 * @param seq the pinned seq, or null
 */
public record RecordRef(String sourceId, String externalId, Long seq) {

    /** The ref prefix. */
    public static final String PREFIX = "record:";

    /** Formats a pinned ref. */
    public static String format(String sourceId, String externalId, long seq) {
        return PREFIX + sourceId + "/" + encode(externalId) + "@" + seq;
    }

    /** Formats an unpinned ref. */
    public static String format(String sourceId, String externalId) {
        return PREFIX + sourceId + "/" + encode(externalId);
    }

    /** This ref as text. */
    public String text() {
        return seq == null ? format(sourceId, externalId) : format(sourceId, externalId, seq);
    }

    /**
     * Parses {@code record:{source}/{externalId}[@seq]}, or the short form {@code {source}/{externalId}}.
     *
     * @throws IllegalArgumentException when it does not parse
     */
    public static RecordRef parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("empty record ref");
        }
        String body = text.startsWith(PREFIX) ? text.substring(PREFIX.length()) : text;
        int slash = body.indexOf('/');
        if (slash <= 0 || slash == body.length() - 1) {
            throw new IllegalArgumentException("not a record ref: " + text);
        }
        String source = body.substring(0, slash);
        String rest = body.substring(slash + 1);
        Long seq = null;
        int at = rest.lastIndexOf('@');
        if (at > 0) {
            try {
                seq = Long.parseLong(rest.substring(at + 1));
                rest = rest.substring(0, at);
            } catch (NumberFormatException e) {
                seq = null;
            }
        }
        return new RecordRef(source, URLDecoder.decode(rest, StandardCharsets.UTF_8), seq);
    }

    private static String encode(String externalId) {
        return URLEncoder.encode(externalId, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
