package ai.foresignal.source.adapters.nhc;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.kit.feature.FeatureFetch;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.source.support.ScopedFetchContext;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NHC forecast cones, tracks, forecast points and coastal watches and warnings for every active storm (prompt 075),
 * from NOAA's {@code NHC_tropical_weather_summary} MapServer. Extends {@code nhc-cyclones} (prompt 067): every record
 * carries the same {@code stormId}.
 *
 * <p><strong>Each run:</strong>
 * <ol>
 *   <li>the layer ids are resolved by name ({@link NhcLayerResolver}, at most once per 24 h; a missing name is an
 *       {@link UpstreamFormatException});
 *   <li>each of the four layers is queried with the prompt 073 {@link ArcGisQueryClient}
 *       ({@code where=1=1&outFields=*&returnGeometry=true&outSR=4326&f=geojson}); the layers publish no
 *       {@code editingInfo}, so they are queried every run. Each layer keeps its own client state through a
 *       {@link ScopedFetchContext} (scope {@code layer-<key>});
 *   <li>features become records through {@link NhcConeMapper}; skips are counted in {@value #SKIPPED_METRIC};
 *   <li>per kind, a record missing from two consecutive complete fetches is tombstoned ({@link AbsenceTombstoner},
 *       scope = kind): a dissipated storm disappears from the service. An empty layer is a complete answer (out of
 *       season every layer is empty, {@code zeroResultsNormal}), so the last storm of a season is retired too.
 * </ol>
 * A new advisory changes the content and yields new versions of the same ids.
 *
 * <p><strong>Coverage:</strong> the Atlantic, eastern and central Pacific (NHC and CPHC); not the western Pacific.
 * The per-slot service {@code NHC_tropical_weather} (the same layers per AT1–AT5, EP1–EP5 and CP slot) is not used.
 */
@Component
public class NhcCycloneConesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nhc-cyclone-cones";

    /** The production MapServer. */
    public static final String SERVICE_URL = "https://mapservices.weather.noaa.gov/tropical/rest/services/tropical/"
            + "NHC_tropical_weather_summary/MapServer";

    /** The counter of features the mapper skipped. */
    public static final String SKIPPED_METRIC = "foresignal.nhc.cones.skipped";

    /** Raw retention: cyclones are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    /** The response cap of one query (two cones were 114 KB on 2026-10-13). */
    static final int MAX_RESPONSE_BYTES = 16 * 1024 * 1024;

    private final URI service;
    private final Supplier<MeterRegistry> meters;
    private final ArcGisQueryClient client = new ArcGisQueryClient();
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value NhcCyclonesAdapter#BASE_URL_PROPERTY}, under which the service
     *     moves as {@code {base}/{host}{path}}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public NhcCycloneConesAdapter(@Value("${" + NhcCyclonesAdapter.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public NhcCycloneConesAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.service = URI.create(KitUrls.rebased(SERVICE_URL, baseUrlOverride));
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The MapServer URL in use. */
    public URI serviceUrl() {
        return service;
    }

    /**
     * Fetches the four layers once (see the class note).
     *
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException when a layer name is missing or a response is not a usable document
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Map<NhcLayerResolver.Layer, Integer> ids = NhcLayerResolver.resolve(ctx, service);
        List<NormalizedRecord> out = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        boolean complete = true;
        for (NhcLayerResolver.Layer layer : NhcLayerResolver.Layer.values()) {
            URI layerUrl = URI.create(service.toString().replaceAll("/+$", "") + "/" + ids.get(layer));
            FeatureFetch fetch = client.fetch(new ScopedFetchContext(ctx, "layer-" + layer.key()),
                    new ArcGisQueryClient.Request(layerUrl, "1=1", List.of(), 2000, 2, 4000, null, null, false,
                            Map.of(), Map.of()));
            List<NormalizedRecord> records = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (MappedRecord mapped : NhcConeMapper.map(layer, fetch.features())) {
                if (!mapped.accepted()) {
                    skipped.merge(mapped.reason(), 1, Integer::sum);
                } else if (seen.add(mapped.record().externalId())) {
                    records.add(mapped.record());
                } else {
                    skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
                }
            }
            out.addAll(records);
            if (!fetch.complete()) {
                complete = false;
                ctx.log().warn("{}: layer {} stopped early ({}); tombstoning nothing for {}", ctx.sourceId(),
                        layer.serviceName(), fetch.stopReason(), layer.kind());
                continue;
            }
            List<String> gone = AbsenceTombstoner.update(ctx.state(), layer.kind(),
                    AbsenceTombstoner.Outcome.COMPLETE, records.stream().map(NormalizedRecord::externalId).toList());
            out.addAll(AbsenceTombstoner.tombstones(gone, layer.kind(), ctx.now()));
        }
        SkipCounter.count(meters, SKIPPED_METRIC, "NHC cone features the mapper skipped", ctx.sourceId(), skipped);
        ctx.log().debug("{}: {} record(s) and tombstone(s), skipped {}", ctx.sourceId(), out.size(), skipped);
        return complete ? FetchResult.complete(out) : FetchResult.of(out);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NHC forecast cones, tracks and warnings")
                .description("National Hurricane Center forecast geometry for every active storm in the Atlantic,"
                        + " eastern and central Pacific (NOAA NHC_tropical_weather_summary MapServer): the cone of"
                        + " uncertainty, the forecast track, the forecast points (intensity, pressure, motion,"
                        + " development) and coastal watch and warning segments. Every record carries the storm id of"
                        + " nhc-cyclones. A storm's geometry is tombstoned once it is missing from two consecutive"
                        + " complete fetches; out of season every layer is empty.")
                .kinds(NhcConeMapper.CONE_KIND, NhcConeMapper.TRACK_KIND, NhcConeMapper.WARNING_KIND,
                        NhcConeMapper.POINT_KIND)
                .homepage("https://www.nhc.noaa.gov/gis/")
                .attribution("NOAA National Hurricane Center and Central Pacific Hurricane Center")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(10))
                .defaultBudget(Budget.of(10, 1200))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .fetchTimeout(Duration.ofSeconds(60))
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultRetention(RETENTION)
                .zeroResultsNormal(true)
                .titleField("stormName")
                .searchFields("stormName", "stormId")
                .tileProperties("stormId", "stormName", "stormType", "advisoryNumber")
                .kindDisplay(NhcConeMapper.CONE_KIND, new KindDisplay("{stormName} cone", "storm", "#7048E8", null))
                .kindDisplay(NhcConeMapper.TRACK_KIND, new KindDisplay("{stormName} track", "storm", "#5F3DC4", null))
                .kindDisplay(NhcConeMapper.WARNING_KIND, new KindDisplay("{stormName} {warningType}", "warning",
                        "#E03131", null))
                .kindDisplay(NhcConeMapper.POINT_KIND, new KindDisplay("{stormName} +{tau} h", "storm", "#9775FA",
                        null));
        for (String kind : List.of(NhcConeMapper.CONE_KIND, NhcConeMapper.TRACK_KIND, NhcConeMapper.WARNING_KIND,
                NhcConeMapper.POINT_KIND)) {
            b.kindRetention(kind, RETENTION);
        }
        text(b, "stormId", "Storm", "The storm id, as nhc-cyclones: basin, two-digit number and year (ep202026)",
                Set.of());
        text(b, "stormName", "Name", "The storm name as the layer gives it (points prefix the classification)",
                Set.of());
        text(b, "stormType", "Type", "The classification code (TD, TS, HU, MH, STD, STS, PTC, …)", Set.of());
        text(b, "basin", "Basin", "AL, EP or CP", Set.of());
        text(b, "advisoryNumber", "Advisory", "The advisory number (advisnum, for example 25A)", Set.of());
        text(b, "advisoryDate", "Advisory time", "The advisory time as NHC writes it (advdate)", Set.of());
        number(b, "forecastPeriod", FieldType.INTEGER, "h", "Forecast period", "The forecast period (fcstprd)",
                Set.of());
        text(b, "warningType", "Watch or warning", "The tcww code: TWA tropical storm watch, TWR tropical storm"
                + " warning, HWA hurricane watch, HWR hurricane warning", Set.of(NhcConeMapper.WARNING_KIND));
        Set<String> points = Set.of(NhcConeMapper.POINT_KIND);
        number(b, "tau", FieldType.INTEGER, "h", "Forecast hour", "Hours after the advisory (tau)", points);
        b.fieldSpec("validTime", new FieldSpec(FieldType.TIMESTAMP, null, "Valid", "When the forecast position is"
                + " valid (validtime)", null, points));
        number(b, "maxWindKt", FieldType.INTEGER, "[kn_i]", "Max wind", "Maximum sustained wind (maxwind)", points);
        number(b, "gustKt", FieldType.INTEGER, "[kn_i]", "Gust", "Wind gust (gust)", points);
        number(b, "mslpHpa", FieldType.INTEGER, "hPa", "Pressure", "Minimum central pressure (mslp); first point"
                + " only", points);
        text(b, "development", "Development", "The development stage in words (tcdvlp)", points);
        text(b, "label", "Label", "The one-letter development label (dvlbl)", points);
        number(b, "motionDirDeg", FieldType.INTEGER, "deg", "Motion direction", "Direction of motion (tcdir); first"
                + " point only", points);
        number(b, "motionSpeedKt", FieldType.INTEGER, "[kn_i]", "Motion speed", "Speed of motion (tcspd); first point"
                + " only", points);
        number(b, "saffirSimpson", FieldType.INTEGER, null, "Saffir-Simpson", "The Saffir-Simpson category (ssnum)",
                points);
        b.fieldSpec(FeatureGeometry.SIMPLIFIED_KEY, new FieldSpec(FieldType.BOOLEAN, null, "Geometry simplified",
                "Present and true when the geometry was simplified to fit the 10,000-vertex limit", null, Set.of()));
        b.fieldSpec(FeatureGeometry.TOLERANCE_KEY, new FieldSpec(FieldType.NUMBER, "deg", "Simplification tolerance",
                "The tolerance the geometry was simplified with", null, Set.of()));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description,
            Set<String> kinds) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, kinds));
    }

    private static void number(SourceDescriptor.Builder b, String key, FieldType type, String unit, String label,
            String description, Set<String> kinds) {
        b.fieldSpec(key, new FieldSpec(type, unit, label, description, null, kinds));
    }
}
