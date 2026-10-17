package ai.foresignal.outlook.index;

import ai.foresignal.outlook.OutlookRecords;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The JSON shapes of the regional index API (prompt 126): {@code RegionalIndexDef}, {@code RegionalIndexValue},
 * {@code IndexFitResult} and {@code RegionalIndexDetail}. Every definition carries its {@code skill} (latest
 * out-of-sample) and {@code weightsOrigin}; every value its decomposition.
 */
public final class IndexViews {

    private IndexViews() {}

    /** The {@code RegionalIndexDef} of a definition. */
    public static Map<String, Object> def(RegionalIndexDefEntity d, boolean ready) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", d.getId());
        out.put("name", d.getName());
        out.put("description", d.getDescription());
        List<Map<String, Object>> components = new ArrayList<>();
        for (Map<String, Object> c : d.getComponents()) {
            Map<String, Object> m = new LinkedHashMap<>(c);
            m.putIfAbsent("floor", null);
            components.add(m);
        }
        out.put("components", components);
        out.put("weights", d.getWeights());
        out.put("floors", d.getFloors());
        out.put("target", d.getTarget());
        out.put("weightsVersion", d.getWeightsVersion());
        out.put("weightsOrigin", d.getWeightsOrigin());
        out.put("autoApplyFittedWeights", d.isAutoApplyFittedWeights());
        out.put("enabled", d.isEnabled());
        out.put("builtIn", d.isBuiltIn());
        out.put("ready", ready);
        out.put("skill", d.getSkill());
        out.put("updatedAt", d.getUpdatedAt() == null ? null : d.getUpdatedAt().toString());
        return out;
    }

    /** The {@code RegionalIndexValue} of a stored value. */
    public static Map<String, Object> value(RegionalIndexValueEntity v) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("indexId", v.getDefId());
        out.put("region", v.getRegion());
        out.put("day", v.getDay().toString());
        out.put("value", v.getValue());
        out.put("components", v.getComponents());
        out.put("floorsApplied", v.getFloorsApplied());
        out.put("delta24h", v.getDelta24h());
        out.put("delta7d", v.getDelta7d());
        out.put("weightsVersion", v.getWeightsVersion());
        out.put("asOfSeq", v.getAsofSeq());
        out.put("manifestId", v.getManifestId());
        out.put("recordRef", OutlookRecords.recordRef(OutlookRecords.indexExternalId(v.getDefId(), v.getRegion())));
        return out;
    }

    /** The {@code IndexFitResult} of a fit. */
    public static Map<String, Object> fit(RegionalIndexFitEntity f) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", f.getId());
        out.put("indexId", f.getIndexId());
        out.put("status", f.getStatus());
        out.put("startedAt", f.getStartedAt() == null ? null : f.getStartedAt().toString());
        out.put("finishedAt", f.getFinishedAt() == null ? null : f.getFinishedAt().toString());
        out.put("method", IndexFitService.METHOD);
        out.put("lambda", f.getLambda());
        out.put("regions", f.getRegions());
        out.put("origins", f.getOrigins());
        out.put("proposedWeights", f.getProposedWeights());
        out.put("weightsVersion", f.getWeightsVersion());
        out.put("skill", f.getSkill());
        out.put("components", f.getComponents());
        out.put("applied", f.isApplied());
        out.put("appliedAt", f.getAppliedAt() == null ? null : f.getAppliedAt().toString());
        out.put("manifestId", f.getManifestId());
        out.put("error", f.getError());
        return out;
    }

    /** The {@code RegionalIndexDetail} of a definition with values. */
    public static Map<String, Object> detail(RegionalIndexDefEntity d, boolean ready, String region, LocalDate from,
            LocalDate to, List<RegionalIndexValueEntity> values, RegionalIndexFitEntity latestFit) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("index", def(d, ready));
        out.put("region", region);
        out.put("from", from == null ? null : from.toString());
        out.put("to", to == null ? null : to.toString());
        out.put("values", values.stream().map(IndexViews::value).toList());
        out.put("latestFit", latestFit == null ? null : fit(latestFit));
        return out;
    }
}
