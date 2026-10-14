/**
 * The shared energy and markets series framework (prompt 093): the {@link ai.foresignal.source.series.SeriesPoint}
 * builder for {@code series-point} and {@code series-latest} records, the
 * {@link ai.foresignal.source.series.SeriesPeriods} helper that turns an upstream period label into a start and end,
 * and the pure {@link ai.foresignal.source.series.FrequencyExcursionDetector} shared by the grid-frequency sources.
 *
 * <p>Not an adapter package: the adapters under {@code source.adapters.*} use these types and never import each other.
 */
package ai.foresignal.source.series;
