package ai.foresignal.exercise;

import java.util.LinkedHashMap;
import java.util.Map;

/** The {@code ExerciseRun} object of a run (prompt 115). */
public final class ExerciseViews {

    private ExerciseViews() {}

    /**
     * The {@code ExerciseRun} of {@code run}.
     *
     * @param run the run
     * @param dryRun true for a dry-run projection (not persisted)
     */
    public static Map<String, Object> run(ExerciseRunEntity run, boolean dryRun) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", run.getId());
        out.put("name", run.getName());
        out.put("ruleIds", run.getRuleIds());
        out.put("tag", run.getTag());
        out.put("from", run.getRangeFrom() == null ? null : run.getRangeFrom().toString());
        out.put("to", run.getRangeTo() == null ? null : run.getRangeTo().toString());
        out.put("fromSeq", run.getFromSeq());
        out.put("toSeq", run.getToSeq());
        out.put("speed", run.getSpeed());
        out.put("channelIds", run.getChannelIds());
        out.put("enrich", run.isEnrich());
        out.put("dryRun", dryRun);
        out.put("status", run.getStatus());
        out.put("createdBy", run.getCreatedBy());
        out.put("createdAt", run.getCreatedAt() == null ? null : run.getCreatedAt().toString());
        out.put("startedAt", run.getStartedAt() == null ? null : run.getStartedAt().toString());
        out.put("finishedAt", run.getFinishedAt() == null ? null : run.getFinishedAt().toString());
        Map<String, Object> stats = new LinkedHashMap<>(run.getStats());
        stats.putIfAbsent("versionsScanned", 0L);
        out.put("stats", stats);
        return out;
    }
}
