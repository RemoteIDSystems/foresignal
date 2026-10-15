package ai.foresignal.backtest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shapes backtest jobs and precursor tests for the API and MCP (prompt 103). */
public final class BacktestViews {

    private BacktestViews() {}

    /** A job. */
    public static Map<String, Object> job(BacktestJobEntity job) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", job.getId());
        out.put("type", job.getType());
        out.put("targetId", job.getTargetId());
        out.put("status", job.getStatus());
        out.put("progress", job.getProgress());
        out.put("fromSeq", job.getFromSeq());
        out.put("toSeq", job.getToSeq());
        out.put("rangeFrom", job.getRangeFrom());
        out.put("rangeTo", job.getRangeTo());
        out.put("startedAt", job.getStartedAt());
        out.put("finishedAt", job.getFinishedAt());
        out.put("definitionHash", job.getDefinitionHash());
        out.put("detectorVersion", job.getDetectorVersion());
        out.put("createdBy", job.getCreatedBy());
        out.put("createdAt", job.getCreatedAt());
        out.put("cancelled", job.isCancelled());
        out.put("caveats", job.getCaveats() == null ? List.of() : job.getCaveats());
        out.put("result", job.getResult());
        return out;
    }

    /** A precursor test. */
    public static Map<String, Object> precursorTest(PrecursorTestEntity test) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", test.getId());
        out.put("name", test.getName());
        out.put("statement", test.getStatement());
        out.put("antecedent", test.getAntecedent());
        out.put("consequent", test.getConsequent());
        out.put("minLag", java.time.Duration.ofSeconds(test.getMinLagSeconds()).toString());
        out.put("maxLag", java.time.Duration.ofSeconds(test.getMaxLagSeconds()).toString());
        out.put("spatial", test.getSpatial());
        out.put("dedupWindow", test.getDedupWindowSeconds() == null ? null
                : java.time.Duration.ofSeconds(test.getDedupWindowSeconds()).toString());
        out.put("createdBy", test.getCreatedBy());
        out.put("createdAt", test.getCreatedAt());
        return out;
    }

    /** A page. */
    public static Map<String, Object> page(List<Map<String, Object>> items) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items == null ? List.of() : items);
        out.put("nextCursor", null);
        return out;
    }
}
