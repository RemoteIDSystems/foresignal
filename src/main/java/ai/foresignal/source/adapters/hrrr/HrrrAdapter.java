package ai.foresignal.source.adapters.hrrr;

import ai.foresignal.raster.RasterProfile;
import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecoderCapability;
import ai.foresignal.raster.decode.DecoderDependent;
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
 * NOAA HRRR CONUS (prompt 051; Weather-Imagery-Design §1.4, §2.5, §2.6, §5.1): runs every 3 h
 * (every 6 h with {@code FORESIGNAL_PROFILE=small}), steps f00–f18, read from {@value #BUCKET} with
 * byte ranges planned from each step's {@code .idx} (about 7.4 MB per step instead of the whole
 * 150 MB {@code wrfsfcf} file).
 *
 * <p>Products (all {@code temporal.type=forecast}, i16 on the native Lambert conformal 1799 × 1059
 * grid, native max zoom 8, the current and previous complete runs kept):
 * <ul>
 *   <li>{@value #WIND} ({@code kind=field}): {@code UGRD}/{@code VGRD} at 10 m as two bands. HRRR's
 *       winds are grid-relative (flag table 3.3 bit 5 is set, verified 2026-10-12), so the engine
 *       rotates them to earth-relative before storing; required;
 *   <li>{@value #REFC}: {@code REFC:entire atmosphere}, composite reflectivity (dBZ), style
 *       {@code nws-reflectivity};
 *   <li>{@value #GUST}: {@code GUST:surface};
 *   <li>{@value #SMOKE}: {@code MASSDEN:8 m above ground}, near-surface smoke. HRRR encodes it in
 *       kg m⁻³ (discipline 0, category 20, number 0; a 2026-10-12 f01 field ranged up to 9.1 × 10⁻⁸
 *       kg m⁻³); Foresignal stores µg m⁻³ (× 10⁹), style {@code smoke}.
 * </ul>
 * These exact {@code .idx} strings were present in {@code wrfsfcf01.grib2.idx} of the 2026-10-12 12z
 * run. The scalars are optional: a missing field drops only that product.
 *
 * <p>The engine is {@link ModelRunIngest}. Object URLs are
 * {@code https://noaa-hrrr-bdp-pds.s3.amazonaws.com/{key}}; the test property
 * {@value #BASE_URL_PROPERTY} switches them to path style under a stub.
 */
@Component
public class HrrrAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "hrrr";

    /** The bucket. */
    public static final String BUCKET = "noaa-hrrr-bdp-pds";

    /** Test-only: a path-style base URL for object reads ({@code {base}/{bucket}/{key}}). */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.hrrr.base-url";

    /** The 10 m wind field. */
    public static final String WIND = "hrrr";

    /** Composite reflectivity. */
    public static final String REFC = "hrrr-refc";

    /** Surface gust. */
    public static final String GUST = "hrrr-gust";

    /** Near-surface smoke. */
    public static final String SMOKE = "hrrr-smoke";

    /** Column-integrated smoke (prompt 106). */
    public static final String SMOKE_COLUMN = "hrrr-smoke-column";

    /** The products enabled by default on both profiles. */
    public static final java.util.List<String> DEFAULT_PRODUCTS =
            List.of(WIND, REFC, GUST, SMOKE, SMOKE_COLUMN);

    /** The default object-key template. */
    public static final String DEFAULT_TEMPLATE = "hrrr.{yyyyMMdd}/conus/hrrr.t{HH}z.wrfsfcf{FF}.grib2";

    /** The default step items. */
    public static final List<String> DEFAULT_STEPS = List.of("0-18/1");

    /** The default availability lag. */
    public static final Duration AVAILABILITY_LAG = Duration.ofMinutes(50);

    /** kg m⁻³ to µg m⁻³. */
    public static final double KG_TO_MICROGRAMS = 1e9;

    static final String TEN_METERS = "10 m above ground";

    private final ModelRunIngest ingest;
    private final String baseUrl;
    private final Duration runEvery;
    private final String profile;
    private final ModelDefinition model;
    private final SourceDescriptor descriptor;

    /**
     * Creates the adapter.
     *
     * @param ingest the forecast model engine
     * @param profile {@code foresignal.profile}: the default run cadence is 6 h for small, 3 h for large
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real bucket
     */
    @Autowired
    public HrrrAdapter(ModelRunIngest ingest, @Value("${foresignal.profile:small}") String profile,
            @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.ingest = ingest;
        String chosen = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.profile = profile;
        this.runEvery = RasterProfile.parse(profile).hrrrRunEvery();
        this.model = model(runEvery);
        this.descriptor = buildDescriptor(runEvery);
        ingest.registerDefault(ID, runEvery);
    }

    /** The model with only the enabled products (prompt 106), always keeping the probe product. */
    ModelDefinition effective(FetchContext ctx) {
        Set<String> enabled = ai.foresignal.raster.ProductEnablement.enabled(ctx.params(), profile,
                model.products().stream().map(ModelDefinition.Product::productId).toList(), DEFAULT_PRODUCTS,
                DEFAULT_PRODUCTS);
        if (enabled.size() == model.products().size()) {
            return model;
        }
        List<ModelDefinition.Product> products = model.products().stream()
                .filter(p -> enabled.contains(p.productId()) || p.productId().equals(model.probeProduct())).toList();
        return new ModelDefinition(model.sourceId(), model.label(), model.bucket(), model.defaultTemplate(),
                model.defaultSteps(), model.defaultRunEvery(), model.defaultLag(), products, model.probeProduct());
    }

    /** The model with the profile's default run cadence. */
    public static ModelDefinition model(Duration runEvery) {
        return new ModelDefinition(ID, "HRRR", BUCKET, DEFAULT_TEMPLATE, DEFAULT_STEPS, runEvery, AVAILABILITY_LAG,
                List.of(
                        new ModelDefinition.Product(WIND, List.of(
                                new ModelDefinition.Component("UGRD", TEN_METERS, false,
                                        DecodeRequest.grib2(WIND, "UGRD", 0, 2, 2, 103, 10.0, TEN_METERS)),
                                new ModelDefinition.Component("VGRD", TEN_METERS, false,
                                        DecodeRequest.grib2(WIND, "VGRD", 0, 2, 3, 103, 10.0, TEN_METERS))),
                                true, 1.0, null),
                        new ModelDefinition.Product(REFC, List.of(new ModelDefinition.Component("REFC",
                                "entire atmosphere", false,
                                DecodeRequest.grib2(REFC, "REFC", 0, 16, 196, 10, null, "entire atmosphere"))), false,
                                1.0, null),
                        new ModelDefinition.Product(GUST, List.of(new ModelDefinition.Component("GUST", "surface", false,
                                DecodeRequest.grib2(GUST, "GUST", 0, 2, 22, 1, null, "surface"))), false, 1.0, null),
                        new ModelDefinition.Product(SMOKE, List.of(new ModelDefinition.Component("MASSDEN",
                                "8 m above ground", false,
                                DecodeRequest.grib2(SMOKE, "MASSDEN", 0, 20, 0, 103, 8.0, "8 m above ground"))), false,
                                KG_TO_MICROGRAMS, null),
                        new ModelDefinition.Product(SMOKE_COLUMN, List.of(new ModelDefinition.Component("COLMD",
                                "entire atmosphere (considered as a single layer)", false,
                                DecodeRequest.grib2(SMOKE_COLUMN, "COLMD", 0, 20, 1, 200, 0.0,
                                        "entire atmosphere"))), false, KG_TO_MICROGRAMS, null)),
                WIND);
    }

    /** The model this instance ingests (profile-dependent default cadence). */
    public ModelDefinition model() {
        return model;
    }

    /** The default run cadence of this instance. */
    public Duration runEvery() {
        return runEvery;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public Map<String, Set<DecoderCapability>> decoderRequirements() {
        Set<DecoderCapability> complex = Set.of(DecoderCapability.GRIB2_COMPLEX_PACKING);
        return Map.of(WIND, complex, REFC, complex, GUST, complex, SMOKE, complex, SMOKE_COLUMN, complex);
    }

    /** The URL of {@code key}. */
    public URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        return ingest.fetch(ctx, effective(ctx), this::objectUri);
    }

    static SourceDescriptor buildDescriptor(Duration runEvery) {
        ProductSpec.Attribution attribution = new ProductSpec.Attribution(
                "NOAA/ESRL High-Resolution Rapid Refresh (HRRR), via NOAA Open Data Dissemination", null,
                "https://registry.opendata.aws/noaa-hrrr-pds/", "US-PD", null, false);
        ProductSpec.Temporal temporal = ProductSpec.Temporal.forecast(Duration.ofHours(1), Duration.ofHours(1));
        ProductSpec.Spatial spatial = new ProductSpec.Spatial("CONUS", 8, 0, 6, null);
        ProductSpec.RetentionLadder ladder = ProductSpec.RetentionLadder.runs(2);
        String meaning = "CONUS model forecast; transparent outside the model domain";
        ProductSpec wind = ProductSpec.builder(WIND)
                .title("HRRR 10 m wind")
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
                .legendSummary("10 m wind speed from earth-relative u/v, m/s; barbs at /vectors")
                .resample(ProductSpec.RESAMPLE_BILINEAR)
                .build();
        ProductSpec refc = scalar(REFC, "HRRR composite reflectivity", "dBZ", 0.01, 0.0, "nws-reflectivity@1",
                ProductSpec.RESAMPLE_NEAREST, temporal, spatial, ladder, attribution, meaning,
                "Simulated composite reflectivity, dBZ");
        ProductSpec gust = scalar(GUST, "HRRR surface wind gust", "m/s", 0.01, 0.0, "wind-speed@1",
                ProductSpec.RESAMPLE_BILINEAR, temporal, spatial, ladder, attribution, meaning, "Surface wind gust, m/s");
        ProductSpec smoke = scalar(SMOKE, "HRRR near-surface smoke", "ug/m3", 0.1, 0.0, "smoke@1",
                ProductSpec.RESAMPLE_BILINEAR, temporal, spatial, ladder, attribution, meaning,
                "Smoke mass density 8 m above ground, µg/m³ (HRRR-Smoke)");
        ProductSpec smokeColumn = scalar(SMOKE_COLUMN, "HRRR column smoke", "ug/m2", 10.0, 0.0, "smoke@2",
                ProductSpec.RESAMPLE_BILINEAR, temporal, spatial, ladder, attribution, meaning,
                "Column-integrated smoke mass density, µg/m² (HRRR-Smoke COLMD)");
        return SourceDescriptor.builder(ID)
                .name("NOAA HRRR CONUS forecast")
                .description("NOAA High-Resolution Rapid Refresh over CONUS: earth-relative 10 m wind (barb tiles),"
                        + " composite reflectivity, surface gust and near-surface smoke to 18 h, read with byte ranges"
                        + " from each step's .idx inventory.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://registry.opendata.aws/noaa-hrrr-pds/")
                .attribution("NOAA/ESRL High-Resolution Rapid Refresh (HRRR)")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-hrrr-pds/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(10))
                .defaultBudget(Budget.of(60, 4_000, 2_000))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(ModelRunIngest.MAX_IDX_BYTES)
                .param(ParamSpec.optional(ModelRunIngest.PATH_TEMPLATE, ParamType.STRING,
                        "Object-key template with {yyyyMMdd}, {HH} and {FF}", DEFAULT_TEMPLATE))
                .param(ParamSpec.optional(ModelRunIngest.STEPS, ParamType.STRING_LIST,
                        "Forecast steps in hours, as N or A-B/S items", DEFAULT_STEPS))
                .param(ParamSpec.optional(ModelRunIngest.RUN_EVERY, ParamType.DURATION,
                        "Run cadence: whole hours dividing 24 (profile default: 6h small, 3h large)",
                        runEvery.toHours() + "h"))
                .param(ParamSpec.optional(ModelRunIngest.AVAILABILITY_LAG, ParamType.DURATION,
                        "How long after its reference time a run starts to appear", "50m"))
                .param(ParamSpec.optional(ai.foresignal.raster.ProductEnablement.PRODUCTS_PARAM, ParamType.STRING_LIST,
                        "Which products to ingest (default all)", DEFAULT_PRODUCTS))
                .product(wind)
                .product(refc)
                .product(gust)
                .product(smoke)
                .product(smokeColumn)
                .build();
    }

    private static ProductSpec scalar(String id, String title, String units, double scale, double offset, String style,
            String resample, ProductSpec.Temporal temporal, ProductSpec.Spatial spatial,
            ProductSpec.RetentionLadder ladder, ProductSpec.Attribution attribution, String meaning, String legend) {
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
                .resample(resample)
                .build();
    }
}
