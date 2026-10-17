/**
 * The analysis-series registry (prompt 119, Intelligence-Design §5.6): which series the change-point detectors run on,
 * at which frequency, and which event-study watch pairs apply to them. Nightly auto-registration keeps the automatic
 * rows within {@code analysis.series.max}; operator rows are never removed automatically.
 */
package ai.foresignal.analytics.registry;
