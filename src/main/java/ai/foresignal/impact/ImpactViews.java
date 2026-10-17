package ai.foresignal.impact;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Views of {@code projected-impact} findings (prompt 127): the {@code ProjectedImpact} schema and the
 * {@code ImpactProjectionEvent} payload of {@code impact.projected} and {@code impact.superseded}. Pure.
 */
public final class ImpactViews {

    private ImpactViews() {}

    /**
     * The {@code ProjectedImpact} view.
     *
     * @param f a {@code projected-impact} finding
     * @param withGeometry include the hazard's geometries (detail) or leave them out (lists)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> projected(AnalyticFindingEntity f, boolean withGeometry) {
        Map<String, Object> stats = f.getStats() == null ? Map.of() : f.getStats();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("findingId", f.getId());
        out.put("ref", "finding:" + f.getId());
        Object hazard = stats.get("hazard");
        Map<String, Object> hazardView = hazard instanceof Map<?, ?> m ? new LinkedHashMap<>((Map<String, Object>) m)
                : new LinkedHashMap<>();
        if (!withGeometry) {
            hazardView.put("geometry", null);
            Object field = hazardView.get("probabilityField");
            if (field instanceof List<?> bands) {
                List<Map<String, Object>> stripped = new ArrayList<>();
                for (Object b : bands) {
                    if (b instanceof Map<?, ?> band) {
                        Map<String, Object> copy = new LinkedHashMap<>((Map<String, Object>) band);
                        copy.remove("geometry");
                        stripped.add(copy);
                    }
                }
                hazardView.put("probabilityField", stripped);
            }
        }
        out.put("hazard", hazardView);
        out.put("impactFamily", stats.get("impactFamily"));
        out.put("admin1", stats.get("admin1"));
        out.put("admin1Name", stats.get("admin1Name"));
        out.put("regionClass", stats.get("regionClass"));
        out.put("pHazard", stats.get("pHazard"));
        out.put("pConditional", stats.get("pConditional"));
        out.put("pImpact", stats.get("pImpact"));
        out.put("interval", stats.get("interval"));
        out.put("n", stats.get("n"));
        out.put("lagHours", stats.get("lagHours"));
        out.put("severity", f.getSeverity());
        out.put("exposedAssets", stats.getOrDefault("exposedAssets", List.of()));
        out.put("dependencyPaths", stats.getOrDefault("dependencyPaths", List.of()));
        out.put("inputs", stats.getOrDefault("inputs", List.of()));
        out.put("skipped", stats.getOrDefault("skipped", List.of()));
        out.put("labels", stats.getOrDefault("labels", List.of(ImpactProjector.LABEL)));
        out.put("questionId", questionId(f));
        out.put("questionWindow", stats.get("questionWindow"));
        out.put("status", "open".equals(f.getStatus()) ? "active" : "superseded");
        out.put("supersededBy", f.getSupersededBy());
        out.put("detectedAt", f.getDetectedAt().toString());
        out.put("manifestId", f.getManifestId());
        Map<String, Object> detector = f.getDetector() == null ? Map.of() : f.getDetector();
        out.put("version", detector.getOrDefault("name", ImpactProjector.NAME) + " "
                + detector.getOrDefault("version", ImpactProjector.VERSION));
        out.put("caveats", f.getCaveats() == null ? List.of() : f.getCaveats());
        return out;
    }

    /**
     * The {@code ImpactProjectionEvent} payload.
     *
     * @param f a {@code projected-impact} finding
     * @param at when the event happened
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> event(AnalyticFindingEntity f, Instant at) {
        Map<String, Object> stats = f.getStats() == null ? Map.of() : f.getStats();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("findingId", f.getId());
        out.put("hazardType", stats.get("hazardType"));
        Object hazard = stats.get("hazard");
        out.put("sourceRef", stats.get("sourceRef") != null ? stats.get("sourceRef")
                : hazard instanceof Map<?, ?> m ? ((Map<String, Object>) m).get("sourceRef") : null);
        out.put("impactFamily", stats.get("impactFamily"));
        out.put("admin1", stats.get("admin1"));
        out.put("pImpact", stats.get("pImpact"));
        out.put("questionId", questionId(f));
        out.put("supersededBy", f.getSupersededBy());
        out.put("at", (at == null ? f.getUpdatedAt() : at).toString());
        return out;
    }

    /** The first linked question, or null. */
    static String questionId(AnalyticFindingEntity f) {
        List<String> ids = f.getQuestionIds();
        return ids == null || ids.isEmpty() ? null : ids.get(0);
    }
}
