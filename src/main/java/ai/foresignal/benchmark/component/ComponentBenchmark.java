package ai.foresignal.benchmark.component;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.scenario.ScenarioType;
import java.util.List;

/**
 * One benchmarked intelligence component (prompt 123): it runs the component in its pure or isolated mode over the
 * synthetic scenarios that test it, pooled over the suite's replicates, and reports the metrics. It writes nothing.
 */
public interface ComponentBenchmark {

    /** The component name ({@code change-point}, {@code forecaster:base-rate}, …). */
    String name();

    /** The component's name and version as benchmarked (for example {@code changepoint-cusum 1.0.0}). */
    String version();

    /** The scenarios this component is measured on, in run order. */
    List<ScenarioType> scenarios();

    /**
     * Measures one scenario.
     *
     * @param scenario one of {@link #scenarios()}
     * @param params the seed, replicates and calibration bin size
     * @param monitor cancellation between replicates
     * @return the metrics and the truth of the scenario
     */
    ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor);
}
