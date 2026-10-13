package ai.foresignal.source.adapters.hydro.eccc;

import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.kit.feature.FeatureDescriptors;
import ai.foresignal.source.kit.feature.FeatureSourceSpec;
import ai.foresignal.source.kit.feature.KitFeatureAdapter;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code eccc-hydrometric} (prompt 079, optional): Environment and Climate Change Canada's real-time hydrometric
 * stations in a box, as a prompt 073 feature-kit spec ({@link #spec}) run through {@link KitFeatureAdapter}.
 *
 * <p>The spec cannot live in {@code kits/features/*.json}: its id is not {@code feature-…}, its box is the operator's
 * {@value #BBOX_PARAM} parameter and its {@code datetime} moves with the clock. So this adapter builds the spec in
 * code, adds the parameter to the kit descriptor, and on each run rebuilds the spec with the box and
 * {@code datetime={now − 2 h}/..}, {@code sortby=-DATETIME}.
 *
 * <p>Verified 2026-10-13: {@code api.weather.gc.ca/collections/hydrometric-realtime/items} holds one row per station
 * per 5 minutes, with {@code STATION_NUMBER, STATION_NAME, PROV_TERR_STATE_LOC, DATETIME} (UTC), {@code LEVEL} (m),
 * {@code DISCHARGE} (m³/s) and the {@code *_SYMBOL_EN} codes, offset paging through {@code links[rel=next]}. The spec's
 * {@code latestPerKey} keeps the newest row per {@code STATION_NUMBER}.
 */
@Component
public class EcccHydrometricAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "eccc-hydrometric";

    /** The record kind. */
    public static final String KIND = "water-observation";

    /** The OGC API landing URL. */
    public static final String API_URL = "https://api.weather.gc.ca";

    /** The collection. */
    public static final String COLLECTION = "hydrometric-realtime";

    /** The box parameter. */
    public static final String BBOX_PARAM = "bbox";

    /** The largest box side, degrees. */
    public static final double MAX_BOX_DEGREES = 10.0;

    /** How far back each run reads. */
    public static final Duration WINDOW = Duration.ofHours(2);

    private final String baseUrl;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value HydroSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the kit's rejection counter, resolved lazily
     */
    @Autowired
    public EcccHydrometricAdapter(@Value("${" + HydroSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, HydroSources.meters(meters));
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public EcccHydrometricAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.baseUrl = baseUrlOverride == null ? "" : baseUrlOverride;
        this.meters = meters;
        SourceDescriptor.Builder b = FeatureDescriptors.builder(spec(null, null))
                .param(ParamSpec.required(BBOX_PARAM, ParamType.BBOX, "The box to read, west,south,east,north, at most"
                        + " 10° × 10°"));
        b.kindDisplay(KIND, new KindDisplay("{stationName}", "water", "#1971C2", null));
        this.descriptor = b.build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Requires a box of at most 10° × 10°. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String problem = HydroSources.boxProblem(params.has(BBOX_PARAM) ? params.bbox(BBOX_PARAM) : null,
                MAX_BOX_DEGREES);
        if (problem != null) {
            problems.put(BBOX_PARAM, problem);
        }
        return problems;
    }

    /**
     * Reads the newest row of every station in the box through the feature kit.
     *
     * @throws AdapterException when the box is missing or invalid, or a request fails
     * @throws UpstreamFormatException when a page is not a feature collection
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params() == null ? ValidatedParams.empty() : ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ID + ": " + problems);
        }
        FeatureSourceSpec spec = KitFeatureAdapter.rebased(spec(params.bbox(BBOX_PARAM), ctx.now()), baseUrl);
        return new KitFeatureAdapter(spec, meters).fetch(ctx);
    }

    /**
     * The feature spec.
     *
     * @param box the box, or null for the descriptor
     * @param now the fetch time, or null for the descriptor; the window starts {@link #WINDOW} earlier
     */
    public static FeatureSourceSpec spec(Envelope box, Instant now) {
        Map<String, FeatureSourceSpec.FieldMapping> fields = new LinkedHashMap<>();
        fields.put("STATION_NUMBER", new FeatureSourceSpec.FieldMapping("stationNumber", "string", null, null));
        fields.put("STATION_NAME", new FeatureSourceSpec.FieldMapping("stationName", "string", null, null));
        fields.put("PROV_TERR_STATE_LOC", new FeatureSourceSpec.FieldMapping("province", "string", null, null));
        fields.put("DATETIME", new FeatureSourceSpec.FieldMapping("measuredAt", "timestamp", null, null));
        fields.put("LEVEL", new FeatureSourceSpec.FieldMapping("levelM", "number", "m", null));
        fields.put("DISCHARGE", new FeatureSourceSpec.FieldMapping("dischargeM3s", "number", "m3/s", null));
        fields.put("LEVEL_SYMBOL_EN", new FeatureSourceSpec.FieldMapping("levelSymbol", "string", null, null));
        fields.put("DISCHARGE_SYMBOL_EN", new FeatureSourceSpec.FieldMapping("dischargeSymbol", "string", null,
                null));
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("sortby", "-DATETIME");
        String datetime = now == null ? null
                : now.minus(WINDOW).truncatedTo(ChronoUnit.SECONDS) + "/..";
        FeatureSourceSpec.Query query = new FeatureSourceSpec.Query(null, filters,
                box == null ? null : HydroSources.bboxList(box), null, datetime);
        return new FeatureSourceSpec(ID, "ECCC hydrometric stations",
                "Environment and Climate Change Canada real-time hydrometric stations in a box (at most 10° × 10°):"
                        + " the newest water level and discharge of each station from the last two hours, through"
                        + " the MSC GeoMet OGC API (hydrometric-realtime).",
                KIND, FeatureSourceSpec.OGC, API_URL, null, COLLECTION, null, null, query, "stationNumber",
                "stationName", fields, Map.of(),
                new FeatureSourceSpec.TimeSpec("measuredAt", "measuredAt", null, null, "iso", null),
                "3h", "OBSERVED", "15m", "5m", new FeatureSourceSpec.BudgetSpec(12, 2000, null),
                new FeatureSourceSpec.PagingSpec(1000, 10, null), null, false, false, List.of(), List.of(),
                List.of("stationName", "stationNumber", "province"), List.of("stationName", "levelM", "dischargeM3s"),
                "Open Government Licence - Canada", "https://eccc-msc.github.io/open-data/licence/readme_en/",
                "Environment and Climate Change Canada, Water Survey of Canada",
                "https://wateroffice.ec.gc.ca/", Map.of(), null, "365d", null, true);
    }
}
