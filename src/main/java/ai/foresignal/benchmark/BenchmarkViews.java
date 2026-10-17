package ai.foresignal.benchmark;

import ai.foresignal.backtest.BacktestJobEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The API shapes of benchmark runs and cases (prompt 123): {@code BenchmarkRun} and {@code BenchmarkCase}. */
public final class BenchmarkViews {

    private BenchmarkViews() {}

    /**
     * The {@code BenchmarkRun} object.
     *
     * @param run the run
     * @param job its 103 job, or null when the job was purged
     */
    public static Map<String, Object> run(BenchmarkRunEntity run, BacktestJobEntity job) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", run.getId());
        out.put("suite", run.getSuite());
        out.put("suiteVersion", run.getSuiteVersion());
        out.put("scorerVersion", run.getScorerVersion());
        out.put("seed", run.getSeed());
        out.put("components", run.getComponents());
        String jobStatus = job != null ? job.getStatus() : run.getFinishedAt() == null ? "queued"
                : run.getError() != null ? "failed" : "done";
        out.put("jobStatus", jobStatus);
        out.put("progress", job != null ? job.getProgress() : run.getFinishedAt() == null ? 0.0 : 1.0);
        out.put("status", run.getStatus());
        out.put("results", run.getResults());
        out.put("manifestId", run.getManifestId());
        out.put("createdBy", run.getCreatedBy());
        out.put("startedAt", run.getStartedAt() == null ? null : run.getStartedAt().toString());
        out.put("finishedAt", run.getFinishedAt() == null ? null : run.getFinishedAt().toString());
        out.put("error", run.getError());
        return out;
    }

    /** The {@code BenchmarkCase} object. */
    public static Map<String, Object> benchmarkCase(BenchmarkCaseEntity c) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", c.getId());
        out.put("name", c.getName());
        out.put("description", c.getDescription());
        out.put("windowFrom", c.getWindowFrom().toString());
        out.put("windowTo", c.getWindowTo().toString());
        out.put("regionCodes", c.getRegionCodes());
        out.put("expectations", c.getExpectations());
        out.put("source", c.getSource());
        out.put("createdBy", c.getCreatedBy());
        out.put("createdAt", c.getCreatedAt().toString());
        return out;
    }

    /** A {@code BenchmarkCasePage}. */
    public static Map<String, Object> casePage(List<BenchmarkCaseEntity> items, String nextCursor) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BenchmarkCaseEntity c : items) {
            list.add(benchmarkCase(c));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", list);
        out.put("nextCursor", nextCursor);
        return out;
    }
}
