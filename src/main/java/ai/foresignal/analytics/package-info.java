/**
 * The discovery layer (prompt 119, Intelligence-Design §5): region rollups feed one coverage-aware
 * {@link ai.foresignal.analytics.series.SeriesReader}, an analysis-series registry chooses what the detectors run on,
 * and three pure, versioned detectors (change points, event studies, co-occurrence) write
 * {@link ai.foresignal.analytics.finding.FindingService analytic findings}, each with its base rate and caveats, which
 * are also records of the DERIVED source {@code foresignal-analytics}.
 */
package ai.foresignal.analytics;
