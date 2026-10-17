package ai.foresignal.outlook.regional;

import ai.foresignal.outlook.OutlookRecords;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The JSON shapes of the regional outlook API (prompt 126): {@code RegionalOutlook} with its {@code OutlookWindow}s,
 * and the {@code OutlookPublishedEvent} of the {@code analytics} SSE topic.
 */
public final class OutlookViews {

    private OutlookViews() {}

    /** The {@code OutlookWindow} of a stored window (internal members left out). */
    public static Map<String, Object> window(Map<String, Object> w) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("index", w.get("index"));
        out.put("windowStart", w.get("windowStart"));
        out.put("windowEnd", w.get("windowEnd"));
        out.put("quantiles", w.get("quantiles"));
        out.put("questionId", w.get("questionId"));
        out.put("status", w.get("status") == null ? "open" : w.get("status"));
        out.put("actual", w.get("actual"));
        out.put("withinP10P90", w.get("withinP10P90"));
        out.put("exceedance", w.get("exceedance"));
        return out;
    }

    /** The windows of an outlook. */
    public static List<Map<String, Object>> windows(RegionalOutlookEntity o) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> w : o.getWindows()) {
            out.add(window(w));
        }
        return out;
    }

    /** The record reference of an outlook's first window, or null. */
    public static String recordRef(RegionalOutlookEntity o) {
        if (o.getWindows().isEmpty()) {
            return null;
        }
        return OutlookRecords.recordRef(OutlookRecords.outlookExternalId(o.getRegion(), o.getFamily(),
                String.valueOf(o.getWindows().get(0).get("windowStart"))));
    }

    /**
     * The {@code RegionalOutlook} of an outlook.
     *
     * @param history the past windows with actuals (detail endpoint), or null to leave it out
     */
    public static Map<String, Object> outlook(RegionalOutlookEntity o, List<Map<String, Object>> history) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", o.getId());
        out.put("region", o.getRegion());
        out.put("regionLevel", o.getRegionLevel());
        out.put("family", o.getFamily());
        out.put("category", o.getCategory());
        out.put("publishedAt", o.getPublishedAt().toString());
        out.put("modelVersion", o.getModelVersion());
        out.put("asOfSeq", o.getAsofSeq());
        out.put("manifestId", o.getManifestId());
        out.put("windows", windows(o));
        if (history != null) {
            out.put("history", history.stream().map(OutlookViews::window).toList());
        }
        out.put("skill", o.getSkill());
        out.put("lowReportingDensity", o.isLowReportingDensity());
        out.put("lowDetectability", o.isLowDetectability());
        out.put("caveats", o.getCaveats());
        out.put("source", o.getSource());
        out.put("recordRef", recordRef(o));
        return out;
    }

    /** The {@code OutlookPublishedEvent} payload. */
    public static Map<String, Object> published(RegionalOutlookEntity o) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("region", o.getRegion());
        out.put("family", o.getFamily());
        out.put("publishedAt", o.getPublishedAt().toString());
        out.put("windows", windows(o));
        out.put("recordRef", recordRef(o));
        return out;
    }
}
