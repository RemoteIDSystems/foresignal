package ai.foresignal.intel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The trust-threshold crossings of an intelligence event (prompt 151): the upgrades that warrant a first-class
 * confidence-upgrade signal, so a consumer never has to diff successive summaries to notice one. A crossing is only
 * ever an upgrade (a move up a tier); a downgrade or a terminal status change emits nothing here.
 *
 * <p>The tracked dimensions are the status {@code emerging → active}, the {@code confidenceLabel} tier
 * ({@code low < moderate < high}), the {@code evidenceQuality} tier
 * ({@code insufficient < weak < moderate < strong}), {@code uncorroborated → corroborated} (evidence quality reaching
 * two or more signal families, i.e. {@code moderate} or {@code strong}), and {@code calibrated} turning true.
 */
public final class TrustCrossing {

    private static final List<String> CONFIDENCE = List.of("low", "moderate", "high");

    private static final List<String> QUALITY = List.of("insufficient", "weak", "moderate", "strong");

    private TrustCrossing() {}

    /**
     * A snapshot of an event's trust-relevant fields, taken before and after a change.
     *
     * @param status the event status
     * @param confidenceLabel low / moderate / high, or null
     * @param evidenceQuality insufficient / weak / moderate / strong, or null
     * @param calibrated whether confidence is a calibrated probability
     */
    public record Level(String status, String confidenceLabel, String evidenceQuality, boolean calibrated) {}

    /** The trust snapshot of an event. */
    public static Level of(IntelligenceEventEntity event) {
        return new Level(event.getStatus(), event.getConfidenceLabel(), event.getEvidenceQuality(),
                event.isCalibrated());
    }

    /**
     * The upgrade crossings from {@code before} to {@code after}, each {@code {dimension, from, to}}; empty when
     * nothing was upgraded.
     */
    public static List<Map<String, Object>> crossings(Level before, Level after) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (before == null || after == null) {
            return out;
        }
        if ("emerging".equals(before.status()) && "active".equals(after.status())) {
            add(out, "status", before.status(), after.status());
        }
        if (rank(CONFIDENCE, after.confidenceLabel()) > rank(CONFIDENCE, before.confidenceLabel())) {
            add(out, "confidenceLabel", before.confidenceLabel(), after.confidenceLabel());
        }
        if (rank(QUALITY, after.evidenceQuality()) > rank(QUALITY, before.evidenceQuality())) {
            add(out, "evidenceQuality", before.evidenceQuality(), after.evidenceQuality());
        }
        if (!corroborated(before.evidenceQuality()) && corroborated(after.evidenceQuality())) {
            add(out, "corroboration", "uncorroborated", "corroborated");
        }
        if (!before.calibrated() && after.calibrated()) {
            add(out, "calibrated", false, true);
        }
        return out;
    }

    private static boolean corroborated(String quality) {
        return rank(QUALITY, quality) >= QUALITY.indexOf("moderate");
    }

    private static int rank(List<String> order, String value) {
        return value == null ? -1 : order.indexOf(value);
    }

    private static void add(List<Map<String, Object>> out, String dimension, Object from, Object to) {
        Map<String, Object> crossing = new LinkedHashMap<>();
        crossing.put("dimension", dimension);
        crossing.put("from", from);
        crossing.put("to", to);
        out.add(crossing);
    }
}
