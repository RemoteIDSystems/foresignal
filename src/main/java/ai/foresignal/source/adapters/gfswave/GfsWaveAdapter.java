package ai.foresignal.source.adapters.gfswave;

import ai.foresignal.raster.ProductEnablement;
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
 * NOAA GFS-Wave 0.25° (prompt 106; Weather-Imagery-Design §5.3): the global wave model, read with the
 * same 051 byte-range engine as GFS. Products are {@code temporal.type=forecast}, i16 on the native
 * 1440 × 721 grid:
 * <ul>
 *   <li>{@value #WAVE}: {@code HTSGW:surface}, significant wave height (m);
 *   <li>{@value #PERIOD}: {@code PERPW:surface}, primary wave mean period (s);
 *   <li>{@value #DIRECTION}: {@code DIRPW:surface}, primary wave direction (degrees).
 * </ul>
 * <strong>Verified 2026-10-15</strong>: the {@code .idx} of
 * {@code gfs.20261015/12/wave/gridded/gfswave.t12z.global.0p25.f006.grib2} lists
 * {@code HTSGW:surface}, {@code PERPW:surface} and {@code DIRPW:surface}; HTSGW is discipline 10,
 * category 0, number 3 on a latlon 1440 × 721 grid. The {@code gfs-wave} product feeds the
 * place-weather {@code marine} section for places tagged {@code coastal}.
 */
@Component
public class GfsWaveAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "gfs-wave";

    /** The bucket. */
    public static final String BUCKET = "noaa-gfs-bdp-pds";

    /** Test-only: a path-style base URL for object reads ({@code {base}/{bucket}/{key}}). */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.gfswave.base-url";

    /** Significant wave height. */
    public static final String WAVE = "gfs-wave";

    /** Primary wave mean period. */
    public static final String PERIOD = "gfs-wave-period";

    /** Primary wave direction. */
    public static final String DIRECTION = "gfs-wave-direction";

    /** The default object-key template. */
    public static final String DEFAULT_TEMPLATE = "gfs.{yyyyMMdd}/{HH}/wave/gridded/gfswave.t{HH}z.global.0p25.f{FFF}.grib2";

    /** The default step items. */
    public static final List<String> DEFAULT_STEPS = List.of("0-120/3");

    /** The default run cadence. */
    public static final Duration RUN_EVERY = Duration.ofHours(6);

    /** The default availability lag. */
    public static final Duration AVAILABILITY_LAG = Duration.ofMinutes(240);

    /** The products. */
    public static final List<String> DEFAULT_PRODUCTS = List.of(WAVE, PERIOD, DIRECTION);

    /** The model. */
    public static final ModelDefinition MODEL = new ModelDefinition(ID, "GFS-Wave", BUCKET, DEFAULT_TEMPLATE,
            DEFAULT_STEPS, RUN_EVERY, AVAILABILITY_LAG, List.of(
                    new ModelDefinition.Product(WAVE, List.of(new ModelDefinition.Component("HTSGW", "surface", false,
                            DecodeRequest.grib2(WAVE, "HTSGW", 10, 0, 3, 1, null, "surface"))), true, 1.0, null),
                    new ModelDefinition.Product(PERIOD, List.of(new ModelDefinition.Component("PERPW", "surface", false,
                            DecodeRequest.grib2(PERIOD, "PERPW", 10, 0, 11, 1, null, "surface"))), false, 1.0, null),
                    new ModelDefinition.Product(DIRECTION, List.of(new ModelDefinition.Component("DIRPW", "surface",
                            false, DecodeRequest.grib2(DIRECTION, "DIRPW", 10, 0, 10, 1, null, "surface"))), false, 1.0,
                            null)),
            WAVE);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final ModelRunIngest ingest;
    private final String baseUrl;

    /** Creates the adapter. */
    @Autowired
    public GfsWaveAdapter(ModelRunIngest ingest, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.ingest = ingest;
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
        Set<DecoderCapability> complex = Set.of(DecoderCapability.GRIB2_COMPLEX_PACKING);
        return Map.of(WAVE, complex, PERIOD, complex, DIRECTION, complex);
    }

    /** The URL of {@code key}. */
    public URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ModelDefinition model = MODEL;
        if (!ctx.rasters().probing()) {
            Set<String> enabled = ProductEnablement.enabled(ctx.params(), "small",
                    MODEL.products().stream().map(ModelDefinition.Product::productId).toList(), DEFAULT_PRODUCTS,
                    DEFAULT_PRODUCTS);
            if (enabled.size() != MODEL.products().size()) {
                List<ModelDefinition.Product> products = MODEL.products().stream()
                        .filter(p -> enabled.contains(p.productId()) || p.productId().equals(MODEL.probeProduct()))
                        .toList();
                model = new ModelDefinition(ID, "GFS-Wave", BUCKET, DEFAULT_TEMPLATE, DEFAULT_STEPS, RUN_EVERY,
                        AVAILABILITY_LAG, products, WAVE);
            }
        }
        return ingest.fetch(ctx, model, this::objectUri);
    }

    private static SourceDescriptor buildDescriptor() {
        ProductSpec.Attribution attribution = new ProductSpec.Attribution(
                "NOAA/NCEP GFS-Wave, via NOAA Open Data Dissemination", null,
                "https://registry.opendata.aws/noaa-gfs-bdp-pds/", "US-PD", null, false);
        ProductSpec.Temporal temporal = ProductSpec.Temporal.forecast(Duration.ofHours(3), Duration.ofHours(6));
        ProductSpec.Spatial spatial = new ProductSpec.Spatial("global", 4, 0, 3, null);
        ProductSpec.RetentionLadder ladder = ProductSpec.RetentionLadder.runs(2);
        String meaning = "Global wave model forecast; values over water only";
        ProductSpec wave = scalar(WAVE, "GFS-Wave significant height", "m", 0.01, "wave@1", temporal, spatial,
                ladder, attribution, meaning, "Significant combined wave height, m");
        ProductSpec period = scalar(PERIOD, "GFS-Wave primary period", "s", 0.1, "wave@1", temporal, spatial, ladder,
                attribution, meaning, "Primary wave mean period, s");
        ProductSpec direction = scalar(DIRECTION, "GFS-Wave primary direction", "deg", 0.1, "wave@1", temporal,
                spatial, ladder, attribution, meaning, "Primary wave direction, degrees from");
        return SourceDescriptor.builder(ID)
                .name("NOAA GFS-Wave 0.25°")
                .description("NOAA GFS global wave model 0.25°: significant wave height, primary period and"
                        + " direction, four runs a day to 120 h, read with byte ranges from each step's .idx. Feeds"
                        + " the place-weather marine section for coastal places.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://registry.opendata.aws/noaa-gfs-bdp-pds/")
                .attribution("NOAA/NCEP GFS-Wave")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-gfs-bdp-pds/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(60, 3_000, 1_000))
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
                        "Run cadence: whole hours dividing 24", "6h"))
                .param(ParamSpec.optional(ModelRunIngest.AVAILABILITY_LAG, ParamType.DURATION,
                        "How long after its reference time a run starts to appear", "240m"))
                .param(ParamSpec.optional(ProductEnablement.PRODUCTS_PARAM, ParamType.STRING_LIST,
                        "Which products to ingest (default all)", DEFAULT_PRODUCTS))
                .product(wave)
                .product(period)
                .product(direction)
                .build();
    }

    private static ProductSpec scalar(String id, String title, String units, double scale, String style,
            ProductSpec.Temporal temporal, ProductSpec.Spatial spatial, ProductSpec.RetentionLadder ladder,
            ProductSpec.Attribution attribution, String meaning, String legend) {
        return ProductSpec.builder(id)
                .title(title)
                .kind(ProductSpec.KIND_RASTER)
                .category("model")
                .units(units)
                .dtype(RasterDtype.I16)
                .scaleOffset(scale, 0.0)
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
