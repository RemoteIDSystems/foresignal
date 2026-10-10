package ai.elaro.source.spi;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * How much to trust a record's position and values (Core-Data-Model §14).
 *
 * <p>Deliberately never hashed: a change in reported accuracy is not a change in what the record
 * says, and hashing it would make every jitter in an upstream's confidence figure look like new
 * information. Adapters that need a quality transition to be significant compute an explicit
 * payload field for it (adapter rule 12).
 *
 * @param accuracyMeters horizontal accuracy, if the upstream states one
 * @param confidence 0..1 where the upstream expresses one
 * @param positionSource how the position was obtained
 * @param extra upstream-specific diagnostics, namespaced by source, for example
 *     {@code adsb.seen_pos}
 */
public record Quality(
        Double accuracyMeters, Double confidence, PositionSource positionSource, Map<String, Object> extra) {

    public Quality {
        extra = extra == null ? Map.of() : Map.copyOf(extra);
    }

    /** No quality information. */
    public static Quality none() {
        return new Quality(null, null, null, Map.of());
    }

    public static Quality positionSource(PositionSource source) {
        return new Quality(null, null, source, Map.of());
    }

    /** This quality plus one {@code extra} entry. */
    public Quality withExtra(String key, Object value) {
        Map<String, Object> merged = new LinkedHashMap<>(extra);
        merged.put(key, value);
        return new Quality(accuracyMeters, confidence, positionSource, merged);
    }

    public boolean isEmpty() {
        return accuracyMeters == null && confidence == null && positionSource == null && extra.isEmpty();
    }
}
