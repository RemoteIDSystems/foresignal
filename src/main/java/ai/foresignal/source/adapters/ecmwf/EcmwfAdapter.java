package ai.foresignal.source.adapters.ecmwf;

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
 * ECMWF IFS 0.25° open data (prompt 105; Weather-Imagery-Design §1.4, §5.3): 00z and 12z runs, steps
 * f000–f144 3-hourly (configurable to 240), read from the {@code ecmwf-forecasts} AWS mirror (or the
 * {@code data.ecmwf.int} fallback) with byte ranges planned from each step's JSON {@code .index}.
 *
 * <p>Products, all {@code temporal.type=forecast}, i16 on the native 1440 × 721 grid, the current and
 * previous complete runs kept:
 * <ul>
 *   <li>{@value #WIND} ({@code kind=field}): {@code 10u}/{@code 10v} as two bands, plus the God's Eye
 *       View fields the pipeline writes;
 *   <li>{@value #GUST}: {@code 10fg} (ECMWF writes {@code 10fg3} at longer steps; the parser folds
 *       them);
 *   <li>{@value #T2M}: {@code 2t} (K);
 *   <li>{@value #MSLP}: {@code msl} (Pa);
 *   <li>{@value #PRECIP_3H} and {@value #PRECIP_24H}: {@code tp} differenced against the previous 3
 *       or 24 h (ECMWF {@code tp} is accumulated from the run start).
 * </ul>
 * Every product carries the ECMWF CC BY 4.0 attribution with the "changes made" line.
 */
@Component
public class EcmwfAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "ecmwf-ifs";

    /** The S3 mirror bucket. */
    public static final String BUCKET = "ecmwf-forecasts";

    /** The open-data fallback host. */
    public static final String FALLBACK_HOST = "https://data.ecmwf.int/forecasts";

    /** Test-only: a path-style base URL for object reads ({@code {base}/{bucket}/{key}}). */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.ecmwf.base-url";

    /** The 10 m wind field. */
    public static final String WIND = "ifs";

    /** Surface gust. */
    public static final String GUST = "ifs-gust";

    /** 2 m temperature. */
    public static final String T2M = "ifs-t2m";

    /** Mean sea level pressure. */
    public static final String MSLP = "ifs-mslp";

    /** 3-hour precipitation (difference of two run totals). */
    public static final String PRECIP_3H = "ifs-precip-3h";

    /** 24-hour precipitation (difference of two run totals). */
    public static final String PRECIP_24H = "ifs-precip-24h";

    /** The default path template ({@code {res}} defaults to {@code 0p25}). */
    public static final String DEFAULT_TEMPLATE =
            "{yyyyMMdd}/{HH}z/ifs/{res}/oper/{yyyyMMdd}{HH}0000-{step}h-oper-fc.grib2";

    /** The default step items. */
    public static final List<String> DEFAULT_STEPS = List.of("0-144/3");

    /** The default run cadence: 00z and 12z. */
    public static final Duration RUN_EVERY = Duration.ofHours(12);

    /** The default availability lag. */
    public static final Duration AVAILABILITY_LAG = Duration.ofHours(7);

    /** The default resolution segment. */
    public static final String DEFAULT_RESOLUTION = "0p25";

    private static final String CHANGES_MADE = EcmwfDefinition.CHANGES_MADE;

    /** The model. */
    public static final EcmwfDefinition MODEL = new EcmwfDefinition(ID, "IFS", BUCKET, DEFAULT_TEMPLATE,
            DEFAULT_STEPS, RUN_EVERY, AVAILABILITY_LAG, DEFAULT_RESOLUTION, List.of(
                    new EcmwfDefinition.Product(WIND, List.of(
                            new EcmwfDefinition.Component("10u", "sfc", null,
                                    DecodeRequest.grib2(WIND, "10u", 0, 2, 2, 103, 10.0, "10 m")),
                            new EcmwfDefinition.Component("10v", "sfc", null,
                                    DecodeRequest.grib2(WIND, "10v", 0, 2, 3, 103, 10.0, "10 m"))),
                            true, 1.0, 0, null),
                    new EcmwfDefinition.Product(GUST, List.of(new EcmwfDefinition.Component("10fg", "sfc", null,
                            DecodeRequest.grib2(GUST, "10fg", 0, 2, 22, 103, 10.0, "10 m"))), false, 1.0, 0,
                            step -> step > 0),
                    new EcmwfDefinition.Product(T2M, List.of(new EcmwfDefinition.Component("2t", "sfc", null,
                            DecodeRequest.grib2(T2M, "2t", 0, 0, 0, 103, 2.0, "2 m"))), false, 1.0, 0, null),
                    new EcmwfDefinition.Product(MSLP, List.of(new EcmwfDefinition.Component("msl", "sfc", null,
                            DecodeRequest.grib2(MSLP, "msl", 0, 3, 0, 101, null, "mean sea level"))), false, 1.0, 0,
                            null),
                    new EcmwfDefinition.Product(PRECIP_3H, List.of(new EcmwfDefinition.Component("tp", "sfc", null,
                            DecodeRequest.grib2(PRECIP_3H, "tp", 0, 1, 193, 1, null, "surface"))), false, 1000.0, 3,
                            step -> step >= 3),
                    new EcmwfDefinition.Product(PRECIP_24H, List.of(new EcmwfDefinition.Component("tp", "sfc", null,
                            DecodeRequest.grib2(PRECIP_24H, "tp", 0, 1, 193, 1, null, "surface"))), false, 1000.0, 24,
                            step -> step >= 24)),
            WIND);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final EcmwfRunIngest ingest;
    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param ingest the ECMWF ingest engine
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real mirror
     */
    @Autowired
    public EcmwfAdapter(EcmwfRunIngest ingest, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        Set<DecoderCapability> ccsds = Set.of(DecoderCapability.GRIB2_CCSDS);
        return Map.of(WIND, ccsds, GUST, ccsds, T2M, ccsds, MSLP, ccsds, PRECIP_3H, ccsds, PRECIP_24H, ccsds);
    }

    /** The URL of {@code key}: the S3 mirror, or the fallback host, or the test base URL. */
    public URI objectUri(String key) {
        if (!baseUrl.isEmpty()) {
            return URI.create(baseUrl + "/" + BUCKET + "/" + key);
        }
        return URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        return ingest.fetch(ctx, MODEL, this::objectUri);
    }

    private static SourceDescriptor buildDescriptor() {
        ProductSpec.Attribution attribution = new ProductSpec.Attribution(CHANGES_MADE, null,
                "https://www.ecmwf.int/en/forecasts/datasets/open-data",
                "CC-BY-4.0", "https://www.ecmwf.int/en/forecasts/datasets/open-data", true);
        ProductSpec.Temporal temporal = ProductSpec.Temporal.forecast(Duration.ofHours(3), Duration.ofHours(6));
        ProductSpec.Spatial spatial = new ProductSpec.Spatial("global", 4, 0, 3, null);
        ProductSpec.RetentionLadder ladder = ProductSpec.RetentionLadder.runs(2);
        String meaning = "Global IFS forecast; values everywhere, never an observation";
        ProductSpec wind = ProductSpec.builder(WIND)
                .title("IFS 10 m wind")
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
        ProductSpec gust = scalar(GUST, "IFS surface wind gust", "m/s", 0.01, 0.0, "wind-speed@1", temporal, spatial,
                ladder, attribution, meaning, "Surface wind gust, m/s");
        ProductSpec t2m = scalar(T2M, "IFS 2 m temperature", "K", 0.01, 273.15, "temperature@1", temporal, spatial,
                ladder, attribution, meaning, "Air temperature 2 m above ground, K");
        ProductSpec mslp = scalar(MSLP, "IFS mean sea level pressure", "Pa", 1.0, 100000.0, "pressure@1", temporal,
                spatial, ladder, attribution, meaning, "Mean sea level pressure, Pa");
        ProductSpec precip3h = scalar(PRECIP_3H, "IFS 3-hour precipitation", "mm", 0.1, 0.0, "qpe@1", temporal,
                spatial, ladder, attribution, meaning, "Precipitation in the preceding 3 hours, mm");
        ProductSpec precip24h = scalar(PRECIP_24H, "IFS 24-hour precipitation", "mm", 0.1, 0.0, "qpe@1", temporal,
                spatial, ladder, attribution, meaning, "Precipitation in the preceding 24 hours, mm");
        return SourceDescriptor.builder(ID)
                .name("ECMWF IFS 0.25° open data")
                .description("ECMWF IFS 0.25° open data (CC BY 4.0): 10 m wind, surface gust, 2 m temperature,"
                        + " mean sea level pressure and 3-hour and 24-hour precipitation, 00z and 12z to 144 h, read"
                        + " with byte ranges from each step's JSON .index inventory.")
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
                        "Forecast steps in hours, as N or A-B/S items (up to 240)", DEFAULT_STEPS))
                .param(ParamSpec.optional(EcmwfRunIngest.RUN_EVERY, ParamType.DURATION,
                        "Run cadence: whole hours dividing 24", "12h"))
                .param(ParamSpec.optional(EcmwfRunIngest.AVAILABILITY_LAG, ParamType.DURATION,
                        "How long after its reference time a run starts to appear", "7h"))
                .param(ParamSpec.optional(EcmwfRunIngest.RESOLUTION, ParamType.STRING,
                        "The {res} path segment", DEFAULT_RESOLUTION))
                .product(wind)
                .product(gust)
                .product(t2m)
                .product(mslp)
                .product(precip3h)
                .product(precip24h)
                .build();
    }

    /** One scalar product. */
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
