package ai.foresignal.benchmark;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The result shapes of a benchmark run (prompt 123): the metrics a component benchmark measured on one scenario, the
 * threshold checks applied to them, and the {@code BenchmarkComponentResult} of each component.
 */
public final class BenchmarkResults {

    private BenchmarkResults() {}

    /**
     * The run parameters every component benchmark receives.
     *
     * @param seed the suite seed
     * @param replicates the replicates per scenario
     * @param minBinCount the forecasts a calibration bin needs to count as populated
     */
    public record SuiteParams(long seed, int replicates, int minBinCount) {

        /** The parameters as stored with a run. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("seed", seed);
            out.put("replicates", replicates);
            out.put("minBinCount", minBinCount);
            return out;
        }
    }

    /**
     * What a component benchmark measured on one scenario (pooled over its replicates) or one historical case.
     *
     * @param scenario the scenario wire value ({@code historical} for a corpus case)
     * @param caseId the corpus case id, or null
     * @param seed the seed, or null for a historical case
     * @param metrics the named metrics ({@code NaN} where not measurable)
     * @param truth the planted truth (of the first replicate, plus counts) or the case expectations
     * @param notes notes for the reader
     */
    public record ScenarioMetrics(String scenario, String caseId, Long seed, Map<String, Double> metrics,
            Map<String, Object> truth, List<String> notes) {

        /** Copies the collections. */
        public ScenarioMetrics {
            metrics = metrics == null ? Map.of() : new LinkedHashMap<>(metrics);
            truth = truth == null ? Map.of() : new LinkedHashMap<>(truth);
            notes = notes == null ? List.of() : List.copyOf(notes);
        }
    }

    /**
     * One threshold check.
     *
     * @param scenario the scenario it applies to
     * @param metric the metric
     * @param comparator {@code le}, {@code ge}, {@code eq} or {@code within}
     * @param threshold the pass threshold
     * @param warn the warn threshold, or null (outside the pass threshold is then a failure)
     * @param observed the observed value, or null when not measurable
     * @param status the verdict
     * @param note what the threshold means, or null
     */
    public record Check(String scenario, String metric, String comparator, double threshold, Double warn,
            Double observed, Verdict status, String note) {

        /** The {@code BenchmarkComponentResult.thresholds} item. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("metric", metric);
            out.put("comparator", comparator);
            out.put("threshold", threshold);
            out.put("warn", warn);
            out.put("observed", observed);
            out.put("status", status.wire());
            out.put("scenario", scenario);
            if (note != null) {
                out.put("note", note);
            }
            return out;
        }
    }

    /**
     * One scenario's (or case's) metrics with their checks and verdict.
     *
     * @param metrics what was measured
     * @param checks the threshold checks
     * @param status the worst check verdict ({@code pass} without checks)
     */
    public record ScenarioResult(ScenarioMetrics metrics, List<Check> checks, Verdict status) {

        /** The {@code BenchmarkComponentResult.scenarios} item. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("scenario", metrics.scenario());
            out.put("caseId", metrics.caseId());
            out.put("seed", metrics.seed());
            out.put("status", status.wire());
            out.put("metrics", numbers(metrics.metrics()));
            List<Map<String, Object>> list = new ArrayList<>();
            for (Check c : checks) {
                list.add(c.toMap());
            }
            out.put("thresholds", list);
            out.put("truth", metrics.truth());
            out.put("notes", metrics.notes());
            return out;
        }
    }

    /**
     * One component's result in a run.
     *
     * @param component the component name ({@code change-point}, {@code forecaster:base-rate}, …)
     * @param componentVersion the component's name and version
     * @param scenarios the scenario results
     * @param caveats caveats ({@code {code, text}})
     */
    public record ComponentResult(String component, String componentVersion, List<ScenarioResult> scenarios,
            List<Map<String, Object>> caveats) {

        /** Copies the collections. */
        public ComponentResult {
            scenarios = scenarios == null ? List.of() : List.copyOf(scenarios);
            caveats = caveats == null ? List.of() : List.copyOf(caveats);
        }

        /** The worst scenario verdict. */
        public Verdict status() {
            return Verdict.worst(scenarios.stream().map(ScenarioResult::status).toList());
        }

        /**
         * The headline metrics: every checked metric with its observed value, keyed {@code metric} when it is checked
         * in one scenario only and {@code metric@scenario} otherwise.
         */
        public Map<String, Double> headline() {
            Map<String, Integer> uses = new LinkedHashMap<>();
            for (ScenarioResult s : scenarios) {
                for (Check c : s.checks()) {
                    uses.merge(c.metric(), 1, Integer::sum);
                }
            }
            Map<String, Double> out = new LinkedHashMap<>();
            for (ScenarioResult s : scenarios) {
                for (Check c : s.checks()) {
                    String key = uses.get(c.metric()) > 1 ? c.metric() + "@" + c.scenario() : c.metric();
                    out.put(key, c.observed());
                }
            }
            return out;
        }

        /** The {@code BenchmarkComponentResult} object. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("component", component);
            out.put("componentVersion", componentVersion);
            out.put("status", status().wire());
            out.put("metrics", numbers(headline()));
            List<Map<String, Object>> checks = new ArrayList<>();
            List<Map<String, Object>> list = new ArrayList<>();
            for (ScenarioResult s : scenarios) {
                for (Check c : s.checks()) {
                    checks.add(c.toMap());
                }
                list.add(s.toMap());
            }
            out.put("thresholds", checks);
            out.put("scenarios", list);
            out.put("caveats", caveats);
            return out;
        }
    }

    /** Metrics with non-finite values as null (JSON has no NaN), rounded to six decimals. */
    public static Map<String, Object> numbers(Map<String, Double> metrics) {
        Map<String, Object> out = new LinkedHashMap<>();
        metrics.forEach((k, v) -> out.put(k, v == null || !Double.isFinite(v) ? null
                : Math.round(v * 1e6) / 1e6));
        return out;
    }
}
