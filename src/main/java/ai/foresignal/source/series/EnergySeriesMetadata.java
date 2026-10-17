package ai.foresignal.source.series;

import ai.foresignal.source.spi.NormalizedRecord;
import java.util.List;

/**
 * The series metadata of prompt 093's energy and markets series (prompt 117, task 1): a small mapping table from the
 * source id and the series id (or dataset) to the family, transform hint and release lag.
 *
 * <p>The 093 adapters apply it to their {@code series-latest} records, and the {@code /v1/series} catalog falls back
 * to it for a {@code series-latest} written before prompt 117, so every 093 series has its metadata at once rather
 * than after its next poll.
 *
 * <table>
 *   <caption>The mapping</caption>
 *   <tr><th>Source</th><th>Series</th><th>Family</th><th>Transform</th><th>Release lag (d)</th></tr>
 *   <tr><td>eia-petroleum</td><td>spot prices ({@code petroleum/pri/spt})</td><td>price</td><td>log-return</td>
 *       <td>5</td></tr>
 *   <tr><td>eia-petroleum</td><td>weekly stocks ({@code petroleum/stoc/wstk})</td><td>volume</td>
 *       <td>difference</td><td>5</td></tr>
 *   <tr><td>eia-930</td><td>demand, generation, fuel mix</td><td>count</td><td>log-level</td><td>1</td></tr>
 *   <tr><td>eia-930</td><td>interchange ({@code :TI})</td><td>count</td><td>level</td><td>1</td></tr>
 *   <tr><td>ercot-public</td><td>settlement point prices</td><td>price</td><td>log-return</td><td>0</td></tr>
 *   <tr><td>ercot-public</td><td>load</td><td>count</td><td>log-level</td><td>1</td></tr>
 *   <tr><td>gie-agsi</td><td>storage fill</td><td>index</td><td>difference</td><td>1</td></tr>
 *   <tr><td>ember-electricity</td><td>demand, generation</td><td>count</td><td>log-level</td><td>45</td></tr>
 *   <tr><td>ember-electricity</td><td>carbon intensity</td><td>index</td><td>level</td><td>45</td></tr>
 *   <tr><td>gb-carbon-intensity</td><td>intensity, generation mix</td><td>index</td><td>level</td><td>0</td></tr>
 * </table>
 */
public final class EnergySeriesMetadata {

    /** The 093 sources this table covers. */
    public static final List<String> SOURCES = List.of("eia-petroleum", "eia-930", "ercot-public", "gie-agsi",
            "ember-electricity", "gb-carbon-intensity");

    private EnergySeriesMetadata() {}

    /**
     * The metadata of one 093 series.
     *
     * @param sourceId the source id
     * @param seriesId the series id (payload {@code series})
     * @param dataset the payload {@code dataset}, or null
     * @return the metadata (without egress, which comes from the descriptor), or null for a source or series the
     *     table does not cover
     */
    public static SeriesMeta lookup(String sourceId, String seriesId, String dataset) {
        if (sourceId == null || seriesId == null) {
            return null;
        }
        return switch (sourceId) {
            case "eia-petroleum" -> dataset != null && dataset.contains("stoc")
                    ? SeriesMeta.of(SeriesMeta.VOLUME, SeriesMeta.DIFFERENCE, 5)
                    : SeriesMeta.of(SeriesMeta.PRICE, SeriesMeta.LOG_RETURN, 5);
            case "eia-930" -> seriesId.endsWith(":TI")
                    ? SeriesMeta.of(SeriesMeta.COUNT, SeriesMeta.LEVEL, 1)
                    : SeriesMeta.of(SeriesMeta.COUNT, SeriesMeta.LOG_LEVEL, 1);
            case "ercot-public" -> seriesId.startsWith("ercot:spp:")
                    ? SeriesMeta.of(SeriesMeta.PRICE, SeriesMeta.LOG_RETURN, 0)
                    : SeriesMeta.of(SeriesMeta.COUNT, SeriesMeta.LOG_LEVEL, 1);
            case "gie-agsi" -> SeriesMeta.of(SeriesMeta.INDEX, SeriesMeta.DIFFERENCE, 1);
            case "ember-electricity" -> seriesId.endsWith(":carbon-intensity")
                    ? SeriesMeta.of(SeriesMeta.INDEX, SeriesMeta.LEVEL, 45)
                    : SeriesMeta.of(SeriesMeta.COUNT, SeriesMeta.LOG_LEVEL, 45);
            case "gb-carbon-intensity" -> SeriesMeta.of(SeriesMeta.INDEX, SeriesMeta.LEVEL, 0);
            default -> null;
        };
    }

    /**
     * The metadata of the series a 093 {@code series-point} belongs to, read from its payload.
     *
     * @return the metadata, or null when the table does not cover it
     */
    public static SeriesMeta lookup(String sourceId, NormalizedRecord point) {
        Object series = point.payload().get("series");
        Object dataset = point.payload().get("dataset");
        return lookup(sourceId, series == null ? null : String.valueOf(series),
                dataset == null ? null : String.valueOf(dataset));
    }

    /**
     * The metadata function a 093 adapter passes to {@link SeriesPoint#latestPerSeries(List,
     * java.util.function.Function)}: the table's entry with the descriptor's egress.
     *
     * @param descriptor the adapter's descriptor
     */
    public static java.util.function.Function<NormalizedRecord, SeriesMeta> forSource(
            ai.foresignal.source.spi.SourceDescriptor descriptor) {
        return point -> {
            SeriesMeta meta = lookup(descriptor.id(), point);
            return meta == null ? null : meta.withEgress(descriptor.egress());
        };
    }
}
