/**
 * The analytic benchmark harness (prompt 123, Intelligence-Design §8.7): deterministic synthetic scenarios with planted
 * truth and a curated historical corpus measure, per intelligence component, false discoveries, recall, precision,
 * detection delay, coverage handling, duplicate resistance, look-ahead safety, precursor validity and forecaster
 * calibration and skill against naive baselines. Components run in their pure or dry-run modes; only
 * {@code benchmark_*} tables are written. Results are {@code pass | warn | fail} against documented thresholds and
 * gate autonomous features through {@link ai.foresignal.benchmark.BenchmarkGate}.
 */
package ai.foresignal.benchmark;
