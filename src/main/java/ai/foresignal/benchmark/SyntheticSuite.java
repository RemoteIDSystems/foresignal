package ai.foresignal.benchmark;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.Check;
import ai.foresignal.benchmark.BenchmarkResults.ComponentResult;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioResult;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.component.ComponentBenchmark;
import ai.foresignal.benchmark.scenario.ScenarioType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The synthetic suite (prompt 123): every selected component benchmark over each of its scenarios, checked against
 * the thresholds. Pure: it reads no table and writes nothing, so the same components, suite version, thresholds and
 * seed always give the same results. A component that throws gets a failing scenario that names the error.
 */
public final class SyntheticSuite {

    private SyntheticSuite() {}

    /**
     * Runs the suite.
     *
     * @param components the component benchmarks, in run order
     * @param params the seed and parameters
     * @param thresholds the thresholds in force
     * @param monitor cancellation and progress
     * @param progressFrom the progress at the start
     * @param progressTo the progress at the end
     * @return one result per component
     */
    public static List<ComponentResult> run(List<ComponentBenchmark> components, SuiteParams params,
            BenchmarkThresholds thresholds, JobMonitor monitor, double progressFrom, double progressTo) {
        List<ComponentResult> out = new ArrayList<>();
        int total = 0;
        for (ComponentBenchmark c : components) {
            total += c.scenarios().size();
        }
        int done = 0;
        for (ComponentBenchmark c : components) {
            List<ScenarioResult> scenarios = new ArrayList<>();
            for (ScenarioType scenario : c.scenarios()) {
                JobMonitor sub = scaled(monitor, progressFrom + (progressTo - progressFrom) * done / Math.max(1, total),
                        (progressTo - progressFrom) / Math.max(1, total));
                ScenarioMetrics metrics;
                try {
                    metrics = c.run(scenario, params, sub);
                } catch (JobMonitor.Canceled e) {
                    throw e;
                } catch (RuntimeException e) {
                    metrics = null;
                    Check failed = new Check(scenario.wire(), "error", "eq", 0, null, 1.0, Verdict.FAIL,
                            "the benchmark could not run: " + e);
                    scenarios.add(new ScenarioResult(new ScenarioMetrics(scenario.wire(), null, params.seed(),
                            Map.of(), Map.of(), List.of("Error: " + e)), List.of(failed), Verdict.FAIL));
                }
                if (metrics != null) {
                    scenarios.add(thresholds.evaluate(c.name(), metrics));
                }
                done++;
                monitor.checkpoint(progressFrom + (progressTo - progressFrom) * done / Math.max(1, total));
            }
            out.add(new ComponentResult(c.name(), c.version(), scenarios, List.of()));
        }
        return out;
    }

    /** A monitor that maps a sub-task's 0–1 progress onto {@code [from, from + width]} of {@code parent}. */
    static JobMonitor scaled(JobMonitor parent, double from, double width) {
        return new JobMonitor() {
            @Override
            public boolean canceled() {
                return parent.canceled();
            }

            @Override
            public void progress(double fraction) {
                parent.progress(from + width * Math.max(0, Math.min(1, fraction)));
            }
        };
    }
}
