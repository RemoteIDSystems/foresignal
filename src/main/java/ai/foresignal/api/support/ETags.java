package ai.foresignal.api.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 * Strong entity tags and conditional GETs for the read endpoints.
 *
 * <ul>
 *   <li>one record: {@code "<version>.<observedAt epoch ms>"};
 *   <li>lists and kinds: a hash of the normalized query, {@code ChangeBus.lastPublishedSeq()}, the
 *       {@code LiveBus} batch number and the current minute, so a tag never outlives a write or a
 *       minute (expiry is evaluated against the clock);
 *   <li>documents such as the kind schema: a hash of the document.
 * </ul>
 *
 * <p>Responses carry {@code Vary: Accept, Accept-Encoding, Authorization} and
 * {@code Cache-Control: private, no-cache}, or {@code public, no-cache} while {@code openReads} is on.
 */
public final class ETags {

    /** The {@code Vary} value of every cacheable read response. */
    public static final String VARY = "Accept, Accept-Encoding, Authorization";

    private static final HexFormat HEX = HexFormat.of();

    private ETags() {}

    /** The tag of one record. */
    public static String forRecord(int version, Instant observedAt) {
        return "\"" + version + "." + observedAt.toEpochMilli() + "\"";
    }

    /** The tag of a list or kinds response. */
    public static String forList(String normalizedQuery, long lastPublishedSeq, long liveBatchNo, Instant now) {
        return "\"l-" + hash(normalizedQuery + "\n" + lastPublishedSeq + "\n" + liveBatchNo + "\n"
                + now.truncatedTo(ChronoUnit.MINUTES)) + "\"";
    }

    /** The tag of a document. */
    public static String forDocument(String document) {
        return "\"d-" + hash(document) + "\"";
    }

    /**
     * True when {@code ifNoneMatch} matches {@code etag} (weak comparison, as RFC 9110 specifies for
     * {@code If-None-Match}; {@code *} matches anything).
     */
    public static boolean matches(String ifNoneMatch, String etag) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank() || etag == null) {
            return false;
        }
        String target = opaque(etag);
        for (String candidate : ifNoneMatch.split(",")) {
            String value = candidate.strip();
            if ("*".equals(value) || opaque(value).equals(target)) {
                return true;
            }
        }
        return false;
    }

    /** The {@code Cache-Control} value for the caller. */
    public static String cacheControl(boolean openReads) {
        return openReads ? "public, no-cache" : "private, no-cache";
    }

    /** Adds {@code ETag}, {@code Cache-Control} and {@code Vary} to {@code headers}. */
    public static HttpHeaders cacheHeaders(HttpHeaders headers, String etag, boolean openReads) {
        if (etag != null) {
            headers.setETag(etag);
        }
        headers.setCacheControl(cacheControl(openReads));
        headers.set(HttpHeaders.VARY, VARY);
        return headers;
    }

    /** A {@code 304 Not Modified} carrying the tag and cache headers. */
    public static ResponseEntity<Object> notModified(String etag, boolean openReads) {
        return ResponseEntity.status(304).headers(cacheHeaders(new HttpHeaders(), etag, openReads)).build();
    }

    private static String opaque(String tag) {
        String value = tag.strip();
        return value.startsWith("W/") ? value.substring(2) : value;
    }

    private static String hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(digest).substring(0, 24);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
