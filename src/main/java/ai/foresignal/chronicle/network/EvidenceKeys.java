package ai.foresignal.chronicle.network;

import ai.foresignal.geo.H3Cells;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Global evidence keys (prompt 134): text keys derived only from captured content, so two instances that captured the
 * same upstream content derive the same key. Pure.
 *
 * <ul>
 *   <li>a canonical event with an upstream id: {@code ev:{family}:{originator}:{upstreamId}};</li>
 *   <li>a canonical event without one: {@code cell:{family}:{h3 res 5}:{utc hour}};</li>
 *   <li>an entity: {@code ent:{keyType}:{value}:{utc day}} for ICAO hex ({@code icao24}), MMSI, IMO and NORAD
 *       keys only;</li>
 *   <li>an area-time cell: {@code at:{h3 res 4}:{utc day}};</li>
 *   <li>a topic: {@code topic:{hash of the normalized keyword set}}.</li>
 * </ul>
 */
public final class EvidenceKeys {

    /** The H3 resolution of {@code cell:} keys. */
    public static final int CELL_RESOLUTION = 5;

    /** The H3 resolution of {@code at:} keys. */
    public static final int AREA_TIME_RESOLUTION = 4;

    /** The entity key types that make {@code ent:} keys. */
    public static final Set<String> ENTITY_KEY_TYPES = Set.of("icao24", "mmsi", "imo", "norad");

    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH", Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    private EvidenceKeys() {}

    /**
     * The key of a canonical event with an upstream id.
     *
     * @param family the 116 canonical-event family
     * @param originator the 116 originator of the observation that carried the id
     * @param upstreamId the upstream's own event id
     * @return the key, or null when a part is missing
     */
    public static String event(String family, String originator, String upstreamId) {
        if (blank(family) || blank(originator) || blank(upstreamId)) {
            return null;
        }
        return "ev:" + part(family) + ":" + originator.strip() + ":" + upstreamId.strip();
    }

    /**
     * The key of a canonical event without an upstream id: its family, H3 resolution-5 cell and UTC hour.
     *
     * @return the key, or null without a family, place or time
     */
    public static String cell(String family, Double lat, Double lon, Instant time) {
        if (blank(family) || lat == null || lon == null || time == null || !valid(lat, lon)) {
            return null;
        }
        return "cell:" + part(family) + ":" + h3(lat, lon, CELL_RESOLUTION) + ":" + HOUR.format(time);
    }

    /**
     * The key of an entity seen on a UTC day.
     *
     * @param keyType the 098 key type
     * @param value the normalized key value
     * @param time when it was observed
     * @return the key, or null for other key types or without a value or time
     */
    public static String entity(String keyType, String value, Instant time) {
        if (keyType == null || !ENTITY_KEY_TYPES.contains(keyType) || blank(value) || time == null) {
            return null;
        }
        return "ent:" + keyType + ":" + value.strip() + ":" + DAY.format(time);
    }

    /**
     * The key of an H3 resolution-4 cell on a UTC day.
     *
     * @return the key, or null without a valid place or time
     */
    public static String areaTime(Double lat, Double lon, Instant time) {
        if (lat == null || lon == null || time == null || !valid(lat, lon)) {
            return null;
        }
        return "at:" + h3(lat, lon, AREA_TIME_RESOLUTION) + ":" + DAY.format(time);
    }

    /**
     * The key of a keyword set: the first 32 hex digits of the SHA-256 of the normalized keywords (NFKC, lower case,
     * whitespace collapsed, distinct, sorted, joined by newlines), so the order and case of the keywords do not matter.
     *
     * @return the key, or null when no keyword is left
     */
    public static String topic(Collection<String> keywords) {
        if (keywords == null) {
            return null;
        }
        TreeSet<String> normalized = new TreeSet<>();
        for (String k : keywords) {
            String n = normalizeKeyword(k);
            if (n != null) {
                normalized.add(n);
            }
        }
        if (normalized.isEmpty()) {
            return null;
        }
        return "topic:" + sha256(String.join("\n", normalized)).substring(0, 32);
    }

    /** A keyword in NFKC, lower case, with whitespace collapsed; null when blank. */
    static String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String n = Normalizer.normalize(keyword, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).strip()
                .replaceAll("\\s+", " ");
        return n.isEmpty() ? null : n;
    }

    /** The key type of a key text ({@code ev:} and {@code cell:} are evidence), or null when unknown. */
    public static String keyType(String key) {
        if (key == null) {
            return null;
        }
        if (key.startsWith("ev:") || key.startsWith("cell:")) {
            return NetworkVocabulary.EVIDENCE;
        }
        if (key.startsWith("ent:")) {
            return NetworkVocabulary.ENTITIES;
        }
        if (key.startsWith("at:")) {
            return NetworkVocabulary.AREA_TIME;
        }
        if (key.startsWith("topic:")) {
            return NetworkVocabulary.TOPICS;
        }
        return null;
    }

    private static String h3(double lat, double lon, int res) {
        return H3Cells.cellToAddress(H3Cells.latLngToCell(lat, lon, res));
    }

    private static boolean valid(double lat, double lon) {
        return Double.isFinite(lat) && Double.isFinite(lon) && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String part(String s) {
        return s.strip().toLowerCase(Locale.ROOT);
    }

    private static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
