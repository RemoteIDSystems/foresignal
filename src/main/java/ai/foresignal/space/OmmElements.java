package ai.foresignal.space;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * One CCSDS OMM general-perturbations element set, as CelesTrak publishes it in {@code FORMAT=CSV} and
 * {@code FORMAT=JSON} (prompt 064; {@code https://celestrak.org/NORAD/documentation/gp-data-formats.php}).
 *
 * <p>Units are CelesTrak's: mean motion in revolutions per day, angles in degrees, {@code MEAN_MOTION_DOT}
 * and {@code MEAN_MOTION_DDOT} exactly as the TLE fields carry them (the first and second derivatives
 * divided by 2 and 6), {@code BSTAR} in inverse Earth radii. The catalog number may have up to nine digits:
 * five-digit numbers ran out on 2026-07-11.
 *
 * @param objectName {@code OBJECT_NAME}
 * @param objectId {@code OBJECT_ID}, the international designator ({@code 1998-067A}), or null
 * @param epoch {@code EPOCH}, UTC
 * @param meanMotion {@code MEAN_MOTION}, rev/day
 * @param eccentricity {@code ECCENTRICITY}
 * @param inclination {@code INCLINATION}, degrees
 * @param raan {@code RA_OF_ASC_NODE}, degrees
 * @param argOfPericenter {@code ARG_OF_PERICENTER}, degrees
 * @param meanAnomaly {@code MEAN_ANOMALY}, degrees
 * @param ephemerisType {@code EPHEMERIS_TYPE}
 * @param classification {@code CLASSIFICATION_TYPE} ({@code U}, {@code C} or {@code S})
 * @param norad {@code NORAD_CAT_ID}
 * @param elementSetNo {@code ELEMENT_SET_NO}
 * @param revAtEpoch {@code REV_AT_EPOCH}
 * @param bstar {@code BSTAR}
 * @param meanMotionDot {@code MEAN_MOTION_DOT}
 * @param meanMotionDdot {@code MEAN_MOTION_DDOT}
 */
public record OmmElements(
        String objectName,
        String objectId,
        Instant epoch,
        double meanMotion,
        double eccentricity,
        double inclination,
        double raan,
        double argOfPericenter,
        double meanAnomaly,
        int ephemerisType,
        String classification,
        long norad,
        int elementSetNo,
        long revAtEpoch,
        double bstar,
        double meanMotionDot,
        double meanMotionDdot) {

    /** The 17 OMM keys, in CelesTrak's CSV column order. */
    public static final List<String> KEYS = List.of("OBJECT_NAME", "OBJECT_ID", "EPOCH", "MEAN_MOTION",
            "ECCENTRICITY", "INCLINATION", "RA_OF_ASC_NODE", "ARG_OF_PERICENTER", "MEAN_ANOMALY", "EPHEMERIS_TYPE",
            "CLASSIFICATION_TYPE", "NORAD_CAT_ID", "ELEMENT_SET_NO", "REV_AT_EPOCH", "BSTAR", "MEAN_MOTION_DOT",
            "MEAN_MOTION_DDOT");

    /** The largest catalog number a TLE can carry without alpha-5 (which CelesTrak does not use). */
    public static final long MAX_TLE_NORAD = 99_999;

    /** CelesTrak's epoch format: no zone (UTC), microseconds. */
    public static final DateTimeFormatter EPOCH_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.ROOT);

    /** Requires a name, an epoch and a positive catalog number. */
    public OmmElements {
        Objects.requireNonNull(epoch, "epoch");
        objectName = objectName == null ? "" : objectName;
        classification = classification == null || classification.isBlank() ? "U" : classification;
        if (norad <= 0 || norad > 999_999_999L) {
            throw new IllegalArgumentException("NORAD_CAT_ID must be 1 to 999999999, was " + norad);
        }
    }

    /**
     * Parses CelesTrak's {@code EPOCH}: an ISO-8601 local date-time in UTC (no zone, up to microseconds);
     * a trailing {@code Z} or offset is accepted too.
     *
     * @throws IllegalArgumentException when it is not a date-time
     */
    public static Instant parseEpoch(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("EPOCH is missing");
        }
        String t = text.strip();
        try {
            return LocalDateTime.parse(t).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            try {
                return java.time.OffsetDateTime.parse(t).toInstant();
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("EPOCH '" + t + "' is not an ISO-8601 date-time", e2);
            }
        }
    }

    /** {@link #epoch} in CelesTrak's format. */
    public String epochText() {
        return EPOCH_FORMAT.format(epoch.truncatedTo(ChronoUnit.MICROS).atOffset(ZoneOffset.UTC));
    }

    /** True when a TLE can carry this object's catalog number (no alpha-5). */
    public boolean tleCompatible() {
        return norad <= MAX_TLE_NORAD;
    }

    /**
     * The OMM payload: the 17 keys verbatim, numbers as JSON numbers, in CelesTrak's order. Null-valued keys
     * ({@code OBJECT_ID} of an analyst object) are left out.
     */
    public Map<String, Object> toPayload() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("OBJECT_NAME", objectName);
        if (objectId != null && !objectId.isBlank()) {
            m.put("OBJECT_ID", objectId);
        }
        m.put("EPOCH", epochText());
        m.put("MEAN_MOTION", meanMotion);
        m.put("ECCENTRICITY", eccentricity);
        m.put("INCLINATION", inclination);
        m.put("RA_OF_ASC_NODE", raan);
        m.put("ARG_OF_PERICENTER", argOfPericenter);
        m.put("MEAN_ANOMALY", meanAnomaly);
        m.put("EPHEMERIS_TYPE", ephemerisType);
        m.put("CLASSIFICATION_TYPE", classification);
        m.put("NORAD_CAT_ID", norad);
        m.put("ELEMENT_SET_NO", elementSetNo);
        m.put("REV_AT_EPOCH", revAtEpoch);
        m.put("BSTAR", bstar);
        m.put("MEAN_MOTION_DOT", meanMotionDot);
        m.put("MEAN_MOTION_DDOT", meanMotionDdot);
        return m;
    }

    /**
     * Reads an element set back from a {@code satellite} payload (or a CelesTrak JSON object), with values
     * as numbers or numeric strings.
     *
     * @throws IllegalArgumentException when a required key is missing or malformed
     */
    public static OmmElements fromPayload(Map<String, ?> p) {
        if (p == null) {
            throw new IllegalArgumentException("no payload");
        }
        return new OmmElements(
                text(p.get("OBJECT_NAME")),
                text(p.get("OBJECT_ID")),
                parseEpoch(text(p.get("EPOCH"))),
                number(p, "MEAN_MOTION"),
                number(p, "ECCENTRICITY"),
                number(p, "INCLINATION"),
                number(p, "RA_OF_ASC_NODE"),
                number(p, "ARG_OF_PERICENTER"),
                number(p, "MEAN_ANOMALY"),
                (int) optionalNumber(p, "EPHEMERIS_TYPE", 0),
                text(p.get("CLASSIFICATION_TYPE")),
                (long) number(p, "NORAD_CAT_ID"),
                (int) optionalNumber(p, "ELEMENT_SET_NO", 999),
                (long) optionalNumber(p, "REV_AT_EPOCH", 0),
                optionalNumber(p, "BSTAR", 0),
                optionalNumber(p, "MEAN_MOTION_DOT", 0),
                optionalNumber(p, "MEAN_MOTION_DDOT", 0));
    }

    /**
     * Parses a CelesTrak numeric field: plain or scientific ({@code .11499036E-3}), with or without a
     * leading digit.
     *
     * @throws IllegalArgumentException when it is not a finite number
     */
    public static double parseNumber(String key, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(key + " is missing");
        }
        try {
            double v = new BigDecimal(text.strip()).doubleValue();
            if (!Double.isFinite(v)) {
                throw new IllegalArgumentException(key + " '" + text + "' is not finite");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " '" + text.strip() + "' is not a number", e);
        }
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }

    private static double number(Map<String, ?> p, String key) {
        Object v = p.get(key);
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return parseNumber(key, text(v));
    }

    private static double optionalNumber(Map<String, ?> p, String key, double fallback) {
        Object v = p.get(key);
        if (v == null || (v instanceof String s && s.isBlank())) {
            return fallback;
        }
        return number(p, key);
    }
}
