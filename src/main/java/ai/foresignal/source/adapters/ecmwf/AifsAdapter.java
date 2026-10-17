package ai.foresignal.source.adapters.ecmwf;

import ai.foresignal.raster.ProductEnablement;
import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecoderCapability;
import ai.foresignal.raster.decode.DecoderDependent;
import ai.foresignal.raster.model.EcmwfDefinition;
import ai.foresignal.raster.model.EcmwfRunIngest;
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
 * ECMWF AIFS Single 0.25° (prompt 106; Weather-Imagery-Design §5.3), the machine-learning forecast,
 * read through the 105 {@link EcmwfRunIngest} engine (JSON {@code .index}, {@code GRIB2_CCSDS}, CC BY
 * 4.0). Off by default on the small profile.
 *
 * <p>Products: {@value #WIND} ({@code kind=field}, {@code 10u}/{@code 10v}), {@value #T2M}
 * ({@code 2t}), {@value #MSLP} ({@code msl}) and {@value #TP} ({@code tp}, a run total). Steps 0–240
 * by 6. <strong>Verified 2026-10-15</strong>: the {@code
 * 20261015/00z/aifs-single/0p25/oper/20261015000000-24h-oper-fc.index} holds {@code 10u}, {@code 10v},
 * {@code 2t}, {@code tp} and {@code msl} at {@code sfc}; the GRIB2 encodings are {@code 10u}
 * (0,2,2) at 103/10 and {@code tp} (0,1,52) at surface on a latlon 1440 × 721 grid.
 */
@Component
public class AifsAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "aifs";

    /** The S3 mirror bucket. */
    public static final String BUCKET = "ecmwf-forecasts";

    /** Test-only: a path-style base URL for object reads. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.aifs.base-url";

    /** The 10 m wind field. */
    public static final String WIND = "aifs";

    /** 2 m temperature. */
    public static final String T2M = "aifs-t2m";

    /** Mean sea level pressure. */
    public static final String MSLP = "aifs-mslp";

    /** Total precipitation (run total). */
    public static final String TP = "aifs-tp";

    /** The default object-key template. */
    public static final String DEFAULT_TEMPLATE =
            "{yyyyMMdd}/{HH}z/aifs-single/{res}/oper/{yyyyMMdd}{HH}0000-{step}h-oper-fc.grib2";

    /** The default step items. */
    public static final List<String> DEFAULT_STEPS = List.of("0-240/6");

    /** The default run cadence. */
    public static final Duration RUN_EVERY = Duration.ofHours(6);

    /** The default availability lag. */
    public static final Duration AVAILABILITY_LAG = Duration.ofHours(7);

    /** The products (all off by default on the small profile). */
    public static final List<String> PRODUCTS = List.of(WIND, T2M, MSLP, TP);

    /** The products on by default on the small profile: none (AIFS is a large-profile product). */
    public static final List<String> LIGHT_DEFAULT = List.of();

    /** The products on by default on the large profile: all (prompt 106 task 1). */
    public static final List<String> LARGE_DEFAULT = PRODUCTS;

    private static final String CHANGES_MADE =
            "Contains modified Copernicus/ECMWF data: ECMWF AIFS open data, CC BY 4.0."
                    + " Changes made: resampled, quantised, derived fields.";

    /** The model. */
    public static final EcmwfDefinition MODEL = new EcmwfDefinition(ID, "AIFS", BUCKET, DEFAULT_TEMPLATE,
            DEFAULT_STEPS, RUN_EVERY, AVAILABILITY_LAG, "0p25", List.of(
                    new EcmwfDefinition.Product(WIND, List.of(
                            new EcmwfDefinition.Component("10u", "sfc", null,
                                    DecodeRequest.grib2(WIND, "10u", 0, 2, 2, 103, 10.0, "10 m")),
                            new EcmwfDefinition.Component("10v", "sfc", null,
                                    DecodeRequest.grib2(WIND, "10v", 0, 2, 3, 103, 10.0, "10 m"))), true, 1.0, 0, null),
                    new EcmwfDefinition.Product(T2M, List.of(new EcmwfDefinition.Component("2t", "sfc", null,
                            DecodeRequest.grib2(T2M, "2t", 0, 0, 0, 103, 2.0, "2 m"))), false, 1.0, 0, null),
                    new EcmwfDefinition.Product(MSLP, List.of(new EcmwfDefinition.Component("msl", "sfc", null,
                            DecodeRequest.grib2(MSLP, "msl", 0, 3, 0, 101, null, "mean sea level"))), false, 1.0, 0,
                            null),
                    // AIFS writes tp as (0,1,52) in kg m-2, which is already mm: unlike the IFS (0,1,193) tp
                    // in meters it takes no ×1000 (fixtures/aifs/README.md: 0 … 205.02 at f024 on
                    // 2026-10-17 00z, where the IFS tp of the same run peaks at 0.291 m).
                    new EcmwfDefinition.Product(TP, List.of(new EcmwfDefinition.Component("tp", "sfc", null,
                            DecodeRequest.grib2(TP, "tp", 0, 1, 52, 1, null, "surface"))), false, 1.0, 0, null)),
            WIND);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final EcmwfRunIngest ingest;
    private final String baseUrl;
    private final String profile;

    /** Creates the adapter. */
    @Autowired
    public AifsAdapter(EcmwfRunIngest ingest, @Value("${foresignal.profile:small}") String profile,
            @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.ingest = ingest;
        this.profile = profile;
        String chosen = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        ingest.registerDefault(ID, RUN_EVERY);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Map<String, Set<DecoderCapability>> decoderRequirements() {
        Set<DecoderCapability> ccsds = Set.of(DecoderCapability.GRIB2_CCSDS);
        return Map.of(WIND, ccsds, T2M, ccsds, MSLP, ccsds, TP, ccsds);
    }

    /** The URL of {@code key}. */
    public URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        EcmwfDefinition model = MODEL;
        if (!ctx.rasters().probing()) {
            Set<String> enabled = ProductEnablement.enabled(ctx.params(), profile,
                    MODEL.products().stream().map(EcmwfDefinition.Product::productId).toList(), LIGHT_DEFAULT,
                    LARGE_DEFAULT);
            if (enabled.isEmpty()) {
                return FetchResult.of(List.of());
            }
            if (enabled.size() != MODEL.products().size()) {
                List<EcmwfDefinition.Product> products = MODEL.products().stream()
                        .filter(p -> enabled.contains(p.productId()) || p.productId().equals(MODEL.probeProduct()))
                        .toList();
                model = new EcmwfDefinition(ID, "AIFS", BUCKET, DEFAULT_TEMPLATE, DEFAULT_STEPS, RUN_EVERY,
                        AVAILABILITY_LAG, "0p25", products, WIND);
            }
        }
        return ingest.fetch(ctx, model, this::objectUri);
    }

    private static SourceDescriptor buildDescriptor() {
        ProductSpec.Attribution attribution = new ProductSpec.Attribution(CHANGES_MADE, null,
                "https://www.ecmwf.int/en/forecasts/datasets/open-data", "CC-BY-4.0",
                "https://www.ecmwf.int/en/forecasts/datasets/open-data", true);
        ProductSpec.Temporal temporal = ProductSpec.Temporal.forecast(Duration.ofHours(6), Duration.ofHours(12));
        ProductSpec.Spatial spatial = new ProductSpec.Spatial("global", 4, 0, 3, null);
        ProductSpec.RetentionLadder ladder = ProductSpec.RetentionLadder.runs(2);
        String meaning = "Global AIFS machine-learning forecast; values everywhere, never an observation";
        ProductSpec wind = ProductSpec.builder(WIND)
                .title("AIFS 10 m wind")
                .kind(ProductSpec.KIND_FIELD)
                .category("model")
                .units("m/s")
                .dtype(RasterDtype.I16)
                .scaleOffset(0.01, 0.0)
                .nodata(-32768.0)
                .bands("u", "v")
                .defaultStyle("wind-speed@1")
                .temporal(temporal)
                .spatial(spatial)
                .retention(ladder)
                .attribution(attribution)
                .coverageMeaning(meaning)
                .legendSummary("10 m wind speed from u/v, m/s")
                .resample(ProductSpec.RESAMPLE_BILINEAR)
                .build();
        ProductSpec t2m = scalar(T2M, "AIFS 2 m temperature", "K", 0.01, 273.15, "temperature@1", temporal, spatial,
                ladder, attribution, meaning, "Air temperature 2 m above ground, K");
        ProductSpec mslp = scalar(MSLP, "AIFS mean sea level pressure", "Pa", 1.0, 100000.0, "pressure@1", temporal,
                spatial, ladder, attribution, meaning, "Mean sea level pressure, Pa");
        ProductSpec tp = scalar(TP, "AIFS total precipitation since the run", "mm", 0.1, 0.0, "qpe@1", temporal,
                spatial, ladder, attribution, meaning,
                "Precipitation accumulated from the run's start to the step, mm");
        return SourceDescriptor.builder(ID)
                .name("ECMWF AIFS Single 0.25°")
                .description("ECMWF AIFS Single 0.25° (CC BY 4.0): 10 m wind, 2 m temperature, mean sea level"
                        + " pressure and total precipitation, four runs a day to 240 h, read with byte ranges from"
                        + " each step's JSON .index. Off by default on the small profile.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://www.ecmwf.int/en/forecasts/datasets/open-data")
                .attribution("Contains modified Copernicus/ECMWF data, CC BY 4.0")
                .licence("CC-BY-4.0")
                .termsUrl("https://www.ecmwf.int/en/forecasts/datasets/open-data")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(20), Duration.ofMinutes(20))
                .defaultBudget(Budget.of(20, 3_000, 1_200))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(EcmwfRunIngest.MAX_INDEX_BYTES)
                .param(ParamSpec.optional(EcmwfRunIngest.PATH_TEMPLATE, ParamType.STRING,
                        "Object-key template with {yyyyMMdd}, {HH}, {res} and {step}", DEFAULT_TEMPLATE))
                .param(ParamSpec.optional(EcmwfRunIngest.STEPS, ParamType.STRING_LIST,
                        "Forecast steps in hours, as N or A-B/S items", DEFAULT_STEPS))
                .param(ParamSpec.optional(EcmwfRunIngest.RUN_EVERY, ParamType.DURATION,
                        "Run cadence: whole hours dividing 24", "6h"))
                .param(ParamSpec.optional(EcmwfRunIngest.AVAILABILITY_LAG, ParamType.DURATION,
                        "How long after its reference time a run starts to appear", "7h"))
                .param(ParamSpec.optional(EcmwfRunIngest.RESOLUTION, ParamType.STRING, "The {res} path segment",
                        "0p25"))
                .param(ParamSpec.optional(ProductEnablement.PRODUCTS_PARAM, ParamType.STRING_LIST,
                        "Which products to ingest (default none on small, all on large)", null))
                .product(wind)
                .product(t2m)
                .product(mslp)
                .product(tp)
                .build();
    }

    private static ProductSpec scalar(String id, String title, String units, double scale, double offset, String style,
            ProductSpec.Temporal temporal, ProductSpec.Spatial spatial, ProductSpec.RetentionLadder ladder,
            ProductSpec.Attribution attribution, String meaning, String legend) {
        return ProductSpec.builder(id)
                .title(title)
                .kind(ProductSpec.KIND_RASTER)
                .category("model")
                .units(units)
                .dtype(RasterDtype.I16)
                .scaleOffset(scale, offset)
                .nodata(-32768.0)
                .defaultStyle(style)
                .temporal(temporal)
                .spatial(spatial)
                .retention(ladder)
                .attribution(attribution)
                .coverageMeaning(meaning)
                .legendSummary(legend)
                .resample(ProductSpec.RESAMPLE_BILINEAR)
                .build();
    }
}
