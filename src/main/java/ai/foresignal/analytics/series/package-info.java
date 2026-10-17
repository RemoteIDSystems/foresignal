/**
 * Series access for the discovery layer (prompt 119, Intelligence-Design §4.3): {@link
 * ai.foresignal.analytics.series.SeriesRef} names a {@code series:}, {@code rollup:} or {@code rule:} series and
 * {@link ai.foresignal.analytics.series.SeriesReader} reads any of them as regular points with {@code knownAt},
 * {@code coverageOk} and explicit gaps, honoring an {@code asOf} knowledge cutoff (INT-2).
 */
package ai.foresignal.analytics.series;
