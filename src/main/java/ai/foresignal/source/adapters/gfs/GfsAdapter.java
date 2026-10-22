package ai.foresignal.source.adapters.gfs;

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
 * NOAA GFS 0.25° (prompt 051; Weather-Imagery-Design §1.4, §2.5, §5.1): four runs a day, steps
 * f000–f048 3-hourly and f054–f120 6-hourly (29 steps), read from {@value #BUCKET} with byte ranges
 * planned from each step's {@code .idx}, so a run costs about 3.5 MB per step instead of the whole
 * 0.5 GB file (about 0.5 GB/day).
 *
 * <p>Products (all {@code temporal.type=forecast}, i16 on the native 1440 × 721 grid, the current and
 * previous complete runs kept):
 * <ul>
 *   <li>{@value #WIND} ({@code kind=field}): {@code UGRD}/{@code VGRD} at 10 m as two bands (0.01 m/s),
 *       plus the God's Eye View fields at 1° and 0.25° the pipeline writes for the fields endpoints;
 *       required: a missing wind field writes no frame for the step;
 *   <li>{@value #GUST}: {@code GUST:surface};
 *   <li>{@value #T2M}: {@code TMP:2 m above ground} (K);
 *   <li>{@value #APCP}: {@code APCP:surface}, the run-total accumulation {@code 0-{step} hour acc}
 *       (NCEP writes whole days as {@code 0-N day acc}); no f000. Consumers difference steps.
 * </ul>
 * The three scalars are optional: a missing field drops only that product. Fields are matched by
 * variable and level, never by message number, and the path template is a param, so the GFSv17
 * layout (PNS 26-29/26-30) is a setting.
 *
 * <p>The engine is {@link ModelRunIngest}. Object URLs are
 * {@code https://noaa-gfs-bdp-pds.s3.amazonaws.com/{key}}; the test property {@value #BASE_URL_PROPERTY}
 * switches them to path style under a stub.
 */
@Component
public class GfsAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "gfs";

    /** The bucket. */
    public static final String BUCKET = "noaa-gfs-bdp-pds";

    /** Test-only: a path-style base URL for object reads ({@code {base}/{bucket}/{key}}). */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.gfs.base-url";

    /** The 10 m wind field. */
    public static final String WIND = "gfs";

    /** Surface gust. */
    public static final String GUST = "gfs-gust";

    /** 2 m temperature. */
    public static final String T2M = "gfs-t2m";

    /** Run-total precipitation. */
    public static final String APCP = "gfs-apcp";

    /** The default object-key template. */
    public static final String DEFAULT_TEMPLATE = "gfs.{yyyyMMdd}/{HH}/atmos/gfs.t{HH}z.pgrb2.0p25.f{FFF}";

    /** The default step items. */
    public static final List<String> DEFAULT_STEPS = List.of("0-48/3", "54-120/6");

    /** The default run cadence. */
    public static final Duration RUN_EVERY = Duration.ofHours(6);

    /** The default availability lag. */
    public static final Duration AVAILABILITY_LAG = Duration.ofMinutes(210);

    /** The level of the 10 m wind. */
    static final String TEN_METERS = "10 m above ground";

    /** The model. */
    public static final ModelDefinition MODEL = new ModelDefinition(ID, "GFS", BUCKET, DEFAULT_TEMPLATE, DEFAULT_STEPS,
            RUN_EVERY, AVAILABILITY_LAG, List.of(
                    new ModelDefinition.Product(WIND, List.of(
                            new ModelDefinition.Component("UGRD", TEN_METERS, false,
                                    DecodeRequest.grib2(WIND, "UGRD", 0, 2, 2, 103, 10.0, TEN_METERS)),
                            new ModelDefinition.Component("VGRD", TEN_METERS, false,
                                    DecodeRequest.grib2(WIND, "VGRD", 0, 2, 3, 103, 10.0, TEN_METERS))),
                            true, 1.0, null),
                    new ModelDefinition.Product(GUST, List.of(new ModelDefinition.Component("GUST", "surface", false,
                            DecodeRequest.grib2(GUST, "GUST", 0, 2, 22, 1, null, "surface"))), false, 1.0, null),
                    new ModelDefinition.Product(T2M, List.of(new ModelDefinition.Component("TMP", "2 m above ground", false,
                            DecodeRequest.grib2(T2M, "TMP", 0, 0, 0, 103, 2.0, "2 m above ground"))), false, 1.0, null),
                    new ModelDefinition.Product(APCP, List.of(new ModelDefinition.Component("APCP", "surface", true,
                            DecodeRequest.grib2(APCP, "APCP", 0, 1, 8, 1, null, "surface"))), false, 1.0,
                            step -> step > 0)),
            WIND);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final ModelRunIngest ingest;
    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param ingest the forecast model engine
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real bucket
     */
    @Autowired
    public GfsAdapter(ModelRunIngest ingest, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        return Map.of(WIND, complex, GUST, complex, T2M, complex, APCP, complex);
    }

    /** The URL of {@code key}. */
    public URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        return ingest.fetch(ctx, MODEL, this::objectUri);
    }

    private static SourceDescriptor buildDescriptor() {
        ProductSpec.Attribution attribution = new ProductSpec.Attribution(
                "NOAA/NCEP Global Forecast System (GFS), via NOAA Open Data Dissemination", null,
                "https://registry.opendata.aws/noaa-gfs-bdp-pds/", "US-PD", null, false);
        ProductSpec.Temporal temporal = ProductSpec.Temporal.forecast(Duration.ofHours(3), Duration.ofHours(6));
        ProductSpec.Spatial spatial = new ProductSpec.Spatial("global", 4, 0, 3, null);
        ProductSpec.RetentionLadder ladder = ProductSpec.RetentionLadder.runs(2);
        String meaning = "Global model forecast; values everywhere, never an observation";
        ProductSpec wind = ProductSpec.builder(WIND)
                .title("GFS 10 m wind")
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
                .legendSummary("10 m wind speed from u/v, m/s; barbs at /vectors, particles at /fields")
                .resample(ProductSpec.RESAMPLE_BILINEAR)
                .build();
        ProductSpec gust = scalar(GUST, "GFS surface wind gust", "m/s", 0.01, 0.0, "wind-speed@1", temporal, spatial,
                ladder, attribution, meaning, "Surface wind gust, m/s");
        ProductSpec t2m = scalar(T2M, "GFS 2 m temperature", "K", 0.01, 273.15, "temperature@1", temporal, spatial,
                ladder, attribution, meaning, "Air temperature 2 m above ground, K");
        ProductSpec apcp = scalar(APCP, "GFS total precipitation since the run", "mm", 0.1, 0.0, "qpe@1",
                temporal, spatial, ladder, attribution, meaning,
                "Precipitation accumulated from the run's start to the step, mm; difference two steps for an interval");
        return SourceDescriptor.builder(ID)
                .name("NOAA GFS 0.25° forecast")
                .description("NOAA Global Forecast System 0.25°: 10 m wind (with God's Eye View wind fields and barb"
                        + " tiles), surface gust, 2 m temperature and run-total precipitation, four runs a day to 120 h,"
                        + " read with byte ranges from each step's .idx inventory.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://registry.opendata.aws/noaa-gfs-bdp-pds/")
                .attribution("NOAA/NCEP Global Forecast System (GFS)")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-gfs-bdp-pds/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(60, 3_000, 1_000))
                // The slowest successful run took 58.8 s of the old 60 s default (2026-10-22).
                .fetchTimeout(Duration.ofMinutes(3))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(ModelRunIngest.MAX_IDX_BYTES)
                .param(ParamSpec.optional(ModelRunIngest.PATH_TEMPLATE, ParamType.STRING,
                        "Object-key template with {yyyyMMdd}, {HH} and {FFF}; override it when GFSv17 changes the layout",
                        DEFAULT_TEMPLATE))
                .param(ParamSpec.optional(ModelRunIngest.STEPS, ParamType.STRING_LIST,
                        "Forecast steps in hours, as N or A-B/S items", DEFAULT_STEPS))
                .param(ParamSpec.optional(ModelRunIngest.RUN_EVERY, ParamType.DURATION,
                        "Run cadence: whole hours dividing 24", "6h"))
                .param(ParamSpec.optional(ModelRunIngest.AVAILABILITY_LAG, ParamType.DURATION,
                        "How long after its reference time a run starts to appear", "210m"))
                .product(wind)
                .product(gust)
                .product(t2m)
                .product(apcp)
                .build();
    }

    /** One of the three scalar products. */
    static ProductSpec scalar(String id, String title, String units, double scale, double offset, String style,
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
