package ai.foresignal.source.adapters.nbm;

import ai.foresignal.raster.ProductEnablement;
import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecoderCapability;
import ai.foresignal.raster.decode.DecoderDependent;
import ai.foresignal.raster.model.ModelCycleSelector;
import ai.foresignal.raster.model.ModelDefinition;
import ai.foresignal.raster.model.ModelRunIngest;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NOAA National Blend of Models (NBM) CONUS core (prompt 106; Weather-Imagery-Design §5.3): the 051
 * forecast engine over {@code noaa-nbm-grib2-pds}, runs 01/07/13/19z (every 6 h), steps f001–f036
 * hourly and f039–f072 3-hourly. Fields TMP 2 m, WIND/GUST 10 m, the 6-hour APCP bucket, its
 * probability (PoP 6-h) and total sky cover (SKY). On by default on both profiles (prompt 106 task 1:
 * NBM is a light product). The cycles sit one hour after the 6-hour cadence boundaries
 * ({@link #CYCLE_OFFSET}), so {@link ModelCycleSelector} offers 01/07/13/19z, never 00/06/12/18z.
 *
 * <p><strong>Verified 2026-10-15</strong>: the {@code .idx} of
 * {@code blend.20261015/12/core/blend.t12z.core.f012.co.grib2} lists {@code TMP:2 m above ground},
 * {@code WIND:10 m above ground}, {@code GUST:10 m above ground} and both deterministic and probability
 * ({@code prob >0.254}) {@code APCP:surface} 6-hour buckets.
 *
 * <p><strong>Rechecked 2026-10-17</strong> ({@code fixtures/nbm/README.md}): total cloud cover is
 * {@code TCDC:surface} (level type 1) in both the 01z and the 12z files; there is no
 * {@code TCDC:entire atmosphere} line. The {@code 6-12 hour acc} buckets are in the 12z f012 file but
 * not in the 01z or 13z f012 files, which list only the 1-hour {@code 11-12 hour acc} buckets.
 */
@Component
public class NbmAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "nbm";

    /** The bucket. */
    public static final String BUCKET = "noaa-nbm-grib2-pds";

    /** Test-only: a path-style base URL for object reads. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.nbm.base-url";

    /** 2 m temperature. */
    public static final String T2M = "nbm-t2m";

    /** 10 m wind speed. */
    public static final String WIND = "nbm-wind";

    /** 10 m gust. */
    public static final String GUST = "nbm-gust";

    /** 6-hour precipitation. */
    public static final String APCP6 = "nbm-apcp6";

    /** 6-hour probability of precipitation. */
    public static final String POP6 = "nbm-pop6";

    /** Total sky cover. */
    public static final String SKY = "nbm-sky";

    /** The products. */
    public static final List<String> PRODUCTS = List.of(T2M, WIND, GUST, APCP6, POP6, SKY);

    /** The default object-key template. */
    public static final String DEFAULT_TEMPLATE = "blend.{yyyyMMdd}/{HH}/core/blend.t{HH}z.core.f{FFF}.co.grib2";

    /** The default step items. */
    public static final List<String> DEFAULT_STEPS = List.of("1-36/1", "39-72/3");

    /** The run cadence. */
    public static final Duration RUN_EVERY = Duration.ofHours(6);

    /** The availability lag. */
    public static final Duration AVAILABILITY_LAG = Duration.ofMinutes(90);

    /** The cycles sit one hour after each 6-hour boundary: 01/07/13/19z. */
    public static final Duration CYCLE_OFFSET = Duration.ofHours(1);

    /** The products on by default on the small profile (all: NBM is light, prompt 106 task 1). */
    public static final List<String> LIGHT_DEFAULT = PRODUCTS;

    /** The products on by default on the large profile. */
    public static final List<String> LARGE_DEFAULT = PRODUCTS;

    /** The model. */
    public static final ModelDefinition MODEL = new ModelDefinition(ID, "NBM", BUCKET, DEFAULT_TEMPLATE, DEFAULT_STEPS,
            RUN_EVERY, AVAILABILITY_LAG, List.of(
                    new ModelDefinition.Product(T2M, List.of(new ModelDefinition.Component("TMP", "2 m above ground",
                            false, DecodeRequest.grib2(T2M, "TMP", 0, 0, 0, 103, 2.0, "2 m above ground"))), true, 1.0,
                            null),
                    new ModelDefinition.Product(WIND, List.of(new ModelDefinition.Component("WIND", "10 m above ground",
                            false, DecodeRequest.grib2(WIND, "WIND", 0, 2, 1, 103, 10.0, "10 m above ground"))), false,
                            1.0, null),
                    new ModelDefinition.Product(GUST, List.of(new ModelDefinition.Component("GUST", "10 m above ground",
                            false, DecodeRequest.grib2(GUST, "GUST", 0, 2, 22, 103, 10.0, "10 m above ground"))), false,
                            1.0, null),
                    new ModelDefinition.Product(APCP6, List.of(ModelDefinition.Component.bucket("APCP", "surface", 6,
                            false, DecodeRequest.grib2(APCP6, "APCP", 0, 1, 8, 1, null, "surface")
                                    .withProbability(false))), false, 1.0, step -> step >= 6),
                    new ModelDefinition.Product(POP6, List.of(ModelDefinition.Component.bucket("APCP", "surface", 6,
                            true, DecodeRequest.grib2(POP6, "APCP", 0, 1, 8, 1, null, "surface")
                                    .withProbability(true))), false, 100.0, step -> step >= 6),
                    // NBM writes total cloud cover at level type 1 ("TCDC:surface" in the .idx), not 200
                    // (verified 2026-10-17 on the 01z and 12z core files; see fixtures/nbm/README.md).
                    new ModelDefinition.Product(SKY, List.of(new ModelDefinition.Component("TCDC", "surface",
                            false, DecodeRequest.grib2(SKY, "TCDC", 0, 6, 1, 1, null, "surface"))), false, 1.0,
                            null)),
            T2M, CYCLE_OFFSET);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final ModelRunIngest ingest;
    private final String baseUrl;
    private final String profile;

    /** Creates the adapter. */
    @Autowired
    public NbmAdapter(ModelRunIngest ingest, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${foresignal.profile:small}") String profile) {
        this.ingest = ingest;
        String chosen = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.profile = profile;
        ingest.registerDefault(ID, RUN_EVERY);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Map<String, Set<DecoderCapability>> decoderRequirements() {
        Set<DecoderCapability> complex = Set.of(DecoderCapability.GRIB2_COMPLEX_PACKING);
        return Map.of(T2M, complex, WIND, complex, GUST, complex, APCP6, complex, POP6, complex, SKY, complex);
    }

    private URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ModelDefinition model = MODEL;
        if (!ctx.rasters().probing()) {
            Set<String> enabled = ProductEnablement.enabled(ctx.params(), profile, PRODUCTS, LIGHT_DEFAULT,
                    LARGE_DEFAULT);
            if (enabled.isEmpty()) {
                return FetchResult.of(List.of());
            }
            if (enabled.size() != MODEL.products().size()) {
                List<ModelDefinition.Product> products = MODEL.products().stream()
                        .filter(p -> enabled.contains(p.productId()) || p.productId().equals(MODEL.probeProduct()))
                        .toList();
                model = new ModelDefinition(ID, "NBM", BUCKET, DEFAULT_TEMPLATE, DEFAULT_STEPS, RUN_EVERY,
                        AVAILABILITY_LAG, products, T2M, CYCLE_OFFSET);
            }
        }
        return ingest.fetch(ctx, model, this::objectUri);
    }

    private static SourceDescriptor buildDescriptor() {
        ProductSpec.Attribution attribution = new ProductSpec.Attribution(
                "NOAA/NWS National Blend of Models (NBM), via NOAA Open Data Dissemination", null,
                "https://registry.opendata.aws/noaa-nbm/", "US-PD", null, false);
        ProductSpec.Temporal temporal = ProductSpec.Temporal.forecast(Duration.ofHours(1), Duration.ofHours(3));
        ProductSpec.Spatial spatial = new ProductSpec.Spatial("CONUS", 6, 0, 5, null);
        ProductSpec.RetentionLadder ladder = ProductSpec.RetentionLadder.runs(2);
        String meaning = "CONUS blended forecast; transparent outside the model domain";
        ProductSpec t2m = scalar(T2M, "NBM 2 m temperature", "K", 0.01, 273.15, "temperature@1", temporal, spatial,
                ladder, attribution, meaning, "Air temperature 2 m above ground, K");
        ProductSpec wind = scalar(WIND, "NBM 10 m wind speed", "m/s", 0.01, 0.0, "wind-speed@1", temporal, spatial,
                ladder, attribution, meaning, "10 m wind speed, m/s");
        ProductSpec gust = scalar(GUST, "NBM 10 m gust", "m/s", 0.01, 0.0, "wind-speed@1", temporal, spatial, ladder,
                attribution, meaning, "10 m wind gust, m/s");
        ProductSpec apcp = scalar(APCP6, "NBM 6-hour precipitation", "mm", 0.1, 0.0, "qpe@1", temporal, spatial, ladder,
                attribution, meaning, "Precipitation over the preceding 6 hours, mm");
        ProductSpec pop = scalar(POP6, "NBM 6-hour probability of precipitation", "%", 1.0, 0.0, "ltg-prob@1", temporal,
                spatial, ladder, attribution, meaning, "Probability of measurable precipitation, %");
        ProductSpec sky = scalar(SKY, "NBM total sky cover", "%", 1.0, 0.0, "ir-gray@1", temporal, spatial, ladder,
                attribution, meaning, "Total cloud cover, %");
        return SourceDescriptor.builder(ID)
                .name("NOAA NBM CONUS core")
                .description("NOAA National Blend of Models CONUS core: 2 m temperature, 10 m wind and gust, the"
                        + " 6-hour precipitation and its probability, and total sky cover, runs 01/07/13/19z to 72 h,"
                        + " read with byte ranges from each step's .idx.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://registry.opendata.aws/noaa-nbm/")
                .attribution("NOAA/NWS National Blend of Models (NBM)")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-nbm/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(15))
                // A day is about 1,400 MiB; a run of 10 steps on the 3.7 M-point grid about 100-190 s (2026-10-22).
                .defaultBudget(Budget.of(60, 3_000, 2_500))
                .fetchTimeout(Duration.ofMinutes(5))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(ModelRunIngest.MAX_IDX_BYTES)
                .param(ParamSpec.optional(ModelRunIngest.PATH_TEMPLATE, ParamType.STRING,
                        "Object-key template with {yyyyMMdd}, {HH} and {FFF}", DEFAULT_TEMPLATE))
                .param(ParamSpec.optional(ModelRunIngest.STEPS, ParamType.STRING_LIST,
                        "Forecast steps in hours, as N or A-B/S items", DEFAULT_STEPS))
                .param(ParamSpec.optional(ModelRunIngest.RUN_EVERY, ParamType.DURATION,
                        "Run cadence: whole hours dividing 24 (cycles sit 1 h after each boundary)", "6h"))
                .param(ParamSpec.optional(ModelRunIngest.AVAILABILITY_LAG, ParamType.DURATION,
                        "How long after its reference time a run starts to appear", "90m"))
                .param(ParamSpec.optional(ProductEnablement.PRODUCTS_PARAM, ParamType.STRING_LIST,
                        "Which NBM products to ingest (default all on both profiles)", null))
                .product(t2m).product(wind).product(gust).product(apcp).product(pop).product(sky)
                .build();
    }

    private static ProductSpec scalar(String id, String title, String units, double scale, double offset, String style,
            ProductSpec.Temporal temporal, ProductSpec.Spatial spatial, ProductSpec.RetentionLadder ladder,
            ProductSpec.Attribution attribution, String meaning, String legend) {
        return ProductSpec.builder(id).title(title).kind(ProductSpec.KIND_RASTER).category("model").units(units)
                .dtype(RasterDtype.I16).scaleOffset(scale, offset).nodata(-32768.0).defaultStyle(style)
                .temporal(temporal).spatial(spatial).retention(ladder).attribution(attribution).coverageMeaning(meaning)
                .legendSummary(legend).resample(ProductSpec.RESAMPLE_BILINEAR).build();
    }
}
