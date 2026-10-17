package ai.foresignal.analytics.finding;

import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;

/**
 * The JSON shapes of findings (prompt 119): the {@code AnalyticFinding} object in evidence, inference and caveats
 * sections, the {@code FindingPage}, and the summary the {@code analytics} SSE topic sends.
 */
public final class FindingViews {

    private static final GeoJsonWriter GEOJSON = new GeoJsonWriter();

    private FindingViews() {}

    /** A GeoJSON geometry, or null. */
    static Map<String, Object> geometry(Geometry geometry) {
        return geometry == null || geometry.isEmpty() ? null : GEOJSON.write(geometry);
    }

    /** The statistics without the raw-value members when {@code withheld}. */
    public static Map<String, Object> stats(AnalyticFindingEntity f, boolean withheld) {
        Map<String, Object> out = new LinkedHashMap<>(f.getStats());
        if (withheld) {
            for (String key : f.getRawStatKeys()) {
                out.remove(key);
            }
        }
        return out;
    }

    /**
     * The {@code AnalyticFinding} JSON.
     *
     * @param situationId the linked situation, or null
     * @param withheld true to leave out raw values of {@code LOCAL_ONLY} series
     */
    public static Map<String, Object> full(AnalyticFindingEntity f, String situationId, boolean withheld) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", f.getId());
        out.put("findingType", f.getFindingType());
        out.put("title", f.getTitle());
        out.put("severity", f.getSeverity());
        out.put("status", f.getStatus());
        out.put("detectedAt", f.getDetectedAt().toString());
        out.put("windowStart", f.getWindowStart() == null ? null : f.getWindowStart().toString());
        out.put("windowEnd", f.getWindowEnd() == null ? null : f.getWindowEnd().toString());
        out.put("regionCodes", f.getRegionCodes());
        out.put("geometry", geometry(f.getGeom()));
        out.put("private", f.isPrivateFinding());
        out.put("situationId", situationId);
        out.put("precursorId", f.getPrecursorId());
        out.put("questionIds", f.getQuestionIds());
        out.put("supersededBy", f.getSupersededBy());
        out.put("dismissal", f.getDismissal());
        out.put("asOfSeq", f.getAsOfSeq());
        out.put("manifestId", f.getManifestId());
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("seriesRefs", f.getSeriesRefs());
        evidence.put("members", f.getMembers());
        evidence.put("valuesWithheld", withheld && !f.getRawStatKeys().isEmpty());
        out.put("evidence", evidence);
        Map<String, Object> inference = new LinkedHashMap<>();
        inference.put("detector", f.getDetector());
        inference.put("stats", stats(f, withheld));
        inference.put("baseRate", f.getBaseRate());
        inference.put("explanation", f.getExplanation() == null ? "" : f.getExplanation());
        out.put("inference", inference);
        out.put("caveats", f.getCaveats());
        out.put("createdAt", f.getCreatedAt().toString());
        out.put("updatedAt", f.getUpdatedAt().toString());
        return out;
    }

    /** The summary of the {@code FindingEvent} payload. */
    public static Map<String, Object> summary(AnalyticFindingEntity f, String situationId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", f.getId());
        out.put("findingType", f.getFindingType());
        out.put("title", f.getTitle());
        out.put("severity", f.getSeverity());
        out.put("status", f.getStatus());
        out.put("regionCodes", f.getRegionCodes());
        out.put("detectedAt", f.getDetectedAt().toString());
        out.put("situationId", situationId);
        out.put("private", f.isPrivateFinding());
        return out;
    }

    /**
     * The {@code FindingPage} JSON.
     *
     * @param view renders one finding
     */
    public static Map<String, Object> page(FindingService.Page page,
            Function<AnalyticFindingEntity, Map<String, Object>> view) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<Map<String, Object>> items = page.items().stream().map(view).toList();
        out.put("items", items);
        out.put("nextCursor", page.nextCursor());
        return out;
    }
}
