package ai.foresignal.benchmark;

import ai.foresignal.benchmark.component.ComponentBenchmark;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The benchmark gate (prompt 123, BEN-2): the latest benchmark status of every component, and what it allows.
 *
 * <ul>
 *   <li>{@link #status(String)}: {@code pass}, {@code warn}, {@code fail}, or {@code unknown} when never
 *       benchmarked, from the newest finished run that covered the component.</li>
 *   <li>128's proposer auto-execution and the LLM forecasters' weight in the 122 ensemble are allowed only while every
 *       component they depend on (the {@code gating} lists of the thresholds) is {@code pass} or {@code warn};
 *       otherwise they degrade (proposals stay pending; LLM forecasters leave the ensemble).</li>
 *   <li>Detectors keep running when they fail; their findings carry the caveat {@value #BENCHMARK_FAIL} until the
 *       next run in which their component passes or warns ({@link #findingCaveat}).</li>
 * </ul>
 *
 * <p>The statuses are cached for {@value #CACHE_SECONDS} seconds (wall time) and refreshed when a run finishes. The
 * overall status is the worst known component status ({@code unknown} when nothing was benchmarked).
 */
@Component
public class BenchmarkGate implements ai.foresignal.ingest.IngestStateReset {

    /** The caveat code findings of a failing component carry. */
    public static final String BENCHMARK_FAIL = "BENCHMARK_FAIL";

    /** The feature name of 128's proposer auto-execution. */
    public static final String PROPOSER_AUTO_EXECUTION = "proposerAutoExecution";

    /** The feature name of the LLM forecasters' ensemble weight. */
    public static final String LLM_ENSEMBLE_WEIGHT = "llmEnsembleWeight";

    /** How long statuses are cached. */
    public static final long CACHE_SECONDS = 60;

    private final BenchmarkRunRepository runs;
    private final BenchmarkThresholds thresholds;
    private final ObjectProvider<ComponentBenchmark> components;
    private final Clock clock;

    private volatile Map<String, ComponentStatus> cached;
    private volatile long cachedAt;

    /** Creates the gate. */
    public BenchmarkGate(BenchmarkRunRepository runs, BenchmarkThresholds thresholds,
            ObjectProvider<ComponentBenchmark> components, Clock clock) {
        this.runs = runs;
        this.thresholds = thresholds;
        this.components = components;
        this.clock = clock;
    }

    /**
     * The latest status of one component.
     *
     * @param component the component
     * @param version the version last benchmarked, or null
     * @param status {@code pass}, {@code warn}, {@code fail} or {@code unknown}
     * @param runId the run, or null
     * @param suite the run's suite, or null
     * @param finishedAt when the run finished, or null
     */
    public record ComponentStatus(String component, String version, String status, String runId, String suite,
            Instant finishedAt) {}

    /** Drops the cache (a run finished, or a test changed the runs). */
    public void invalidate() {
        cached = null;
    }

    /** Drops the cache when the tables were reset (tests truncate them between methods). */
    @Override
    public void resetState() {
        invalidate();
    }

    /** Every benchmarked component's latest status, by component. */
    public Map<String, ComponentStatus> latest() {
        Map<String, ComponentStatus> current = cached;
        if (current != null && System.nanoTime() - cachedAt < CACHE_SECONDS * 1_000_000_000L) {
            return current;
        }
        Map<String, ComponentStatus> out = new LinkedHashMap<>();
        for (BenchmarkRunEntity run : runs.findByStatusNotNullOrderByFinishedAtDescIdDesc()) {
            for (Map<String, Object> result : run.getResults()) {
                Object component = result.get("component");
                Object status = result.get("status");
                if (component == null || status == null || out.containsKey(String.valueOf(component))) {
                    continue;
                }
                out.put(String.valueOf(component), new ComponentStatus(String.valueOf(component),
                        result.get("componentVersion") == null ? null : String.valueOf(result.get("componentVersion")),
                        String.valueOf(status), run.getId(), run.getSuite(), run.getFinishedAt()));
            }
        }
        cached = out;
        cachedAt = System.nanoTime();
        return out;
    }

    /** The latest status of a component: {@code pass}, {@code warn}, {@code fail} or {@code unknown}. */
    public String status(String component) {
        ComponentStatus s = latest().get(component);
        return s == null ? "unknown" : s.status();
    }

    /** True when every dependency's latest status is {@code pass} or {@code warn}. */
    public boolean allows(Collection<String> dependencies) {
        for (String d : dependencies) {
            String s = status(d);
            if (!"pass".equals(s) && !"warn".equals(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether 128's proposer may auto-execute a proposal that rests on {@code dependsOn} (every gated component when
     * null).
     */
    public boolean proposerAutoExecutionAllowed(Collection<String> dependsOn) {
        return allows(dependsOn != null ? dependsOn : thresholds.dependencies(PROPOSER_AUTO_EXECUTION));
    }

    /** Whether LLM forecasters may carry weight in the 122 ensemble. */
    public boolean llmEnsembleWeightAllowed() {
        return allows(thresholds.dependencies(LLM_ENSEMBLE_WEIGHT));
    }

    /** {@value #BENCHMARK_FAIL} when the component behind a finding type failed its latest run, else null. */
    public String findingCaveat(String findingType) {
        String component = thresholds.componentOfFindingType(findingType);
        return component != null && "fail".equals(status(component)) ? BENCHMARK_FAIL : null;
    }

    /** The components whose current version differs from the version last benchmarked (never benchmarked counts). */
    public List<String> versionChanged() {
        List<String> out = new ArrayList<>();
        Map<String, ComponentStatus> latest = latest();
        for (ComponentBenchmark c : components.orderedStream().toList()) {
            ComponentStatus s = latest.get(c.name());
            if (s == null || !c.version().equals(s.version())) {
                out.add(c.name());
            }
        }
        return out;
    }

    /** The {@code BenchmarkStatus} object. */
    public Map<String, Object> view() {
        Map<String, ComponentStatus> latest = latest();
        Map<String, String> versions = new LinkedHashMap<>();
        for (ComponentBenchmark c : components.orderedStream().toList()) {
            versions.put(c.name(), c.version());
        }
        java.util.Set<String> names = new TreeSet<>(versions.keySet());
        names.addAll(latest.keySet());
        List<String> ordered = new ArrayList<>();
        for (String n : BenchmarkInputs.COMPONENTS) {
            if (names.remove(n)) {
                ordered.add(n);
            }
        }
        ordered.addAll(names);
        List<String> proposerDeps = thresholds.dependencies(PROPOSER_AUTO_EXECUTION);
        List<String> llmDeps = thresholds.dependencies(LLM_ENSEMBLE_WEIGHT);
        boolean proposerAllowed = allows(proposerDeps);
        boolean llmAllowed = allows(llmDeps);
        List<Map<String, Object>> items = new ArrayList<>();
        Verdict overall = null;
        for (String name : ordered) {
            ComponentStatus s = latest.get(name);
            String status = s == null ? "unknown" : s.status();
            Verdict v = Verdict.fromWire(status);
            if (v != null) {
                overall = overall == null ? v : overall.worse(v);
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("component", name);
            item.put("componentVersion", versions.containsKey(name) ? versions.get(name)
                    : s == null ? null : s.version());
            item.put("status", status);
            item.put("runId", s == null ? null : s.runId());
            item.put("suite", s == null ? null : s.suite());
            item.put("finishedAt", s == null || s.finishedAt() == null ? null : s.finishedAt().toString());
            item.put("versionChanged", s == null || versions.get(name) == null || !versions.get(name)
                    .equals(s.version()));
            Map<String, Object> gating = new LinkedHashMap<>();
            gating.put("proposerAutoExecution", proposerDeps.contains(name) && !proposerAllowed ? "degraded"
                    : "allowed");
            gating.put("llmEnsembleWeight", llmDeps.contains(name) && !llmAllowed ? "degraded" : "allowed");
            gating.put("findingCaveat", "fail".equals(status) ? BENCHMARK_FAIL : null);
            item.put("gating", gating);
            items.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("generatedAt", clock.instant().toString());
        out.put("overall", overall == null ? "unknown" : overall.wire());
        out.put("components", items);
        return out;
    }
}
