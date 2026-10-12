package ai.foresignal.trigger;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The moving anchor of a "near me" rule (prompt 036, task 5): an ordinary record whose current live
 * position is the center of the rule's fence.
 *
 * <ul>
 *   <li>{@code radiusKm} 0 means "the anchor point lies inside the target's geometry" (for example a
 *       phone inside an NWS warning polygon); otherwise the target must lie within the radius.
 *   <li>The rule is suspended while the anchor is older than {@code maxAnchorAge}, tombstoned or
 *       expired.
 *   <li>The reverse evaluation runs when the anchor moved at least {@code minMoveMeters} (or 60 s
 *       passed) since it last ran.
 * </ul>
 *
 * @param source the anchor's source id
 * @param externalId the anchor's external id
 * @param radiusKm the radius in kilometers (0 to 2000)
 * @param maxAnchorAge how old the anchor may be before the rule suspends
 * @param minMoveMeters the movement that triggers a reverse evaluation
 */
public record NearAnchor(String source, String externalId, double radiusKm, Duration maxAnchorAge,
        double minMoveMeters) {

    /** The default {@code maxAnchorAge}. */
    public static final Duration DEFAULT_MAX_AGE = Duration.ofMinutes(30);

    /** The default {@code minMoveMeters}. */
    public static final double DEFAULT_MIN_MOVE = 200;

    /** The longest {@code radiusKm}. */
    public static final double MAX_RADIUS_KM = 2000;

    /** Requires the anchor; fills defaults. */
    public NearAnchor {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(externalId, "externalId");
        maxAnchorAge = maxAnchorAge == null ? DEFAULT_MAX_AGE : maxAnchorAge;
    }

    /** The radius in meters. */
    public double radiusMeters() {
        return radiusKm * 1000.0;
    }

    /** True when the radius is 0: the anchor point must lie inside the target geometry. */
    public boolean inside() {
        return radiusKm <= 0;
    }

    /** The stored {@code near} form. */
    public Map<String, Object> toMap() {
        Map<String, Object> anchor = new LinkedHashMap<>();
        anchor.put("source", source);
        anchor.put("externalId", externalId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("anchor", anchor);
        out.put("radiusKm", radiusKm);
        out.put("maxAnchorAge", maxAnchorAge.toString());
        out.put("minMoveMeters", minMoveMeters);
        return out;
    }

    /** The anchor of a stored {@code near} clause, or null when it has none. */
    @SuppressWarnings("unchecked")
    public static NearAnchor fromMap(Map<String, Object> near) {
        if (near == null || !(near.get("anchor") instanceof Map<?, ?> anchor)) {
            return null;
        }
        Object source = anchor.get("source");
        Object externalId = anchor.get("externalId");
        if (source == null || externalId == null) {
            return null;
        }
        double radius = near.get("radiusKm") instanceof Number n ? n.doubleValue() : 0;
        Duration age = DEFAULT_MAX_AGE;
        if (near.get("maxAnchorAge") instanceof String text) {
            try {
                age = Duration.parse(text);
            } catch (RuntimeException e) {
                age = DEFAULT_MAX_AGE;
            }
        }
        double move = near.get("minMoveMeters") instanceof Number n ? n.doubleValue() : DEFAULT_MIN_MOVE;
        return new NearAnchor(source.toString(), externalId.toString(), radius, age, move);
    }

    /** The anchor key {@code source + '\0' + externalId}. */
    public String key() {
        return key(source, externalId);
    }

    /** The anchor key of an entity. */
    public static String key(String source, String externalId) {
        return source + '\u0000' + externalId;
    }
}
