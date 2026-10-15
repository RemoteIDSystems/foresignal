package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.LayerStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The payload every derived layer shares (prompt 099): {@code automated}, {@code notAWarning}, a
 * {@code detector{id,version}}, a banded {@code confidence}, a {@code status} and bounded
 * {@code evidence}, plus the quantisation helpers that keep a steady-state condition from writing
 * versions (distances 0.5 km, ratios 0.1, durations 5 min, confidence 0.1).
 */
public final class LayerPayload {

    /** The most evidence entries kept in a payload. */
    public static final int MAX_EVIDENCE = 20;

    /** The detector version all layers start at. */
    public static final String VERSION = "1";

    private LayerPayload() {}

    /** One evidence entry: a source, an entity and when it was observed. */
    public record Evidence(String sourceId, String externalId, Instant observedAt) {

        /** The payload map form. */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("sourceId", sourceId);
            map.put("externalId", externalId);
            if (observedAt != null) {
                map.put("observedAt", observedAt.toString());
            }
            return map;
        }
    }

    /**
     * The common payload fields.
     *
     * @param detectorId the generator's id
     * @param status the layer status
     * @param confidence the confidence, or null
     * @param evidence up to {@value #MAX_EVIDENCE} entries
     */
    public static Map<String, Object> base(String detectorId, LayerStatus status, Double confidence,
            List<Evidence> evidence) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("automated", true);
        payload.put("notAWarning", true);
        payload.put("detector", Map.of("id", detectorId, "version", VERSION));
        payload.put("status", status.wire());
        if (confidence != null) {
            payload.put("confidence", band01(confidence));
        }
        if (evidence != null && !evidence.isEmpty()) {
            List<Map<String, Object>> bounded = new ArrayList<>(Math.min(evidence.size(), MAX_EVIDENCE));
            for (Evidence entry : evidence) {
                if (bounded.size() >= MAX_EVIDENCE) {
                    break;
                }
                bounded.add(entry.toMap());
            }
            payload.put("evidence", bounded);
        }
        return payload;
    }

    /** Evidence from a list of observations. */
    public static List<Evidence> evidence(List<ai.foresignal.derive.Observation> observations) {
        List<Evidence> out = new ArrayList<>();
        for (ai.foresignal.derive.Observation observation : observations) {
            out.add(new Evidence(observation.sourceId(), observation.externalId(), observation.observedAt()));
        }
        return out;
    }

    /** Rounds to the nearest 0.1 in [0, 1]. */
    public static double band01(double value) {
        return Math.round(Math.max(0, Math.min(1, value)) * 10.0) / 10.0;
    }

    /** Rounds a distance to the nearest 0.5 km. */
    public static double km05(double value) {
        return Math.round(value * 2.0) / 2.0;
    }

    /** Rounds a ratio to the nearest 0.1. */
    public static double ratio1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /** Rounds a duration in minutes to the nearest 5 minutes. */
    public static long minutes5(double minutes) {
        return Math.round(minutes / 5.0) * 5L;
    }

    /** A banded confidence from a boolean test plus a strength in [0, 1]. */
    public static double confidence(double strength) {
        return band01(0.5 + 0.5 * Math.max(0, Math.min(1, strength)));
    }
}
