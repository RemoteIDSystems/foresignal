package ai.foresignal.source.adapters.sst;

import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.raster.decode.DecoderCapability;
import ai.foresignal.raster.decode.DecoderDependent;
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NOAA Geo-Polar Blended Sea Surface Temperature via CoastWatch ERDDAP (prompt 106;
 * Weather-Imagery-Design §5.3): the daily night analysis ({@code noaacwBLENDEDsstDNDaily},
 * {@code analysed_sst} in °C on a 0.05° equirectangular grid), subset over a configurable bbox
 * (default the AOI envelope + 1000 km) and stored as u16 codes (scale 0.01, offset −2 °C). Daily.
 *
 * <p><strong>Verified 2026-10-15</strong>: the dataset id is {@code noaacwBLENDEDsstDNDaily},
 * {@code analysed_sst} is {@code degree_C} (fill −327.68, valid −2.0..40.0), with 0.05° latitude and
 * longitude. Off by default on the small profile.
 */
@Component
public class SstAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "sst-geopolar";

    /** The product. */
    public static final String PRODUCT = "sst-geopolar";

    /** The ERDDAP dataset. */
    public static final String DATASET = "noaacwBLENDEDsstDNDaily";

    /** The variable. */
    public static final String VARIABLE = "analysed_sst";

    /** The ERDDAP base. */
    public static final String BASE_URL = "https://coastwatch.noaa.gov/erddap/griddap/";

    /** Test-only base URL override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.sst.base-url";

    /** The default bbox margin around the AOI, km. */
    static final double MARGIN_KM = 1000;

    /** The fill value of the source. */
    static final float FILL = -327.68f;

    /** The response cap. */
    static final int MAX_RESPONSE_BYTES = 32 * 1024 * 1024;

    private static final String STATE_KEY = "lastTime";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final NetcdfDecoder decoder;
    private final String baseUrl;
    private final String profile;
    private final ObjectProvider<AreaOfInterest> aoi;

    /** The bbox param. */
    public static final String BBOX_PARAM = "bbox";

    /** Creates the adapter. */
    @Autowired
    public SstAdapter(NetcdfDecoder decoder, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${foresignal.profile:small}") String profile, ObjectProvider<AreaOfInterest> aoi) {
        this.decoder = decoder;
        this.baseUrl = (baseUrlOverride == null || baseUrlOverride.isBlank()) ? BASE_URL
                : (baseUrlOverride.endsWith("/") ? baseUrlOverride : baseUrlOverride + "/");
        this.profile = profile;
        this.aoi = aoi;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Map<String, Set<DecoderCapability>> decoderRequirements() {
        return Map.of(PRODUCT, Set.of(DecoderCapability.NETCDF4));
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Set<String> enabled = ai.foresignal.raster.ProductEnablement.enabled(ctx.params(), profile, List.of(PRODUCT),
                List.of(), List.of(PRODUCT));
        if (!enabled.contains(PRODUCT)) {
            return FetchResult.of(List.of());
        }
        double[] box = bbox(ctx);
        String url = subsetUrl(box);
        Path file;
        try {
            file = ctx.http().download(URI.create(url), Path.of(ID + "-" + System.nanoTime() + ".nc"), MAX_RESPONSE_BYTES)
                    .file();
        } catch (AdapterException e) {
            throw e;
        }
        try (NetcdfDecoder.Reader reader = decoder.open(file, ID)) {
            if (!reader.has(VARIABLE)) {
                throw new AdapterException("SST file has no " + VARIABLE);
            }
            float[] values = reader.floats(VARIABLE);
            float[] lat = reader.floats("latitude");
            float[] lon = reader.floats("longitude");
            Instant time = reader.globalTime("time");
            String last = ctx.state().get(STATE_KEY).orElse(null);
            if (time != null && time.toString().equals(last)) {
                return FetchResult.unchanged();
            }
            int nx = lon.length;
            int ny = lat.length;
            if (values.length != nx * ny) {
                throw new AdapterException("SST grid " + values.length + " != " + nx + "x" + ny);
            }
            boolean northToSouth = ny < 2 || lat[0] > lat[ny - 1];
            int[] codes = new int[values.length];
            for (int r = 0; r < ny; r++) {
                int row = northToSouth ? r : ny - 1 - r;
                for (int c = 0; c < nx; c++) {
                    float v = values[row * nx + c];
                    codes[r * nx + c] = (Float.isNaN(v) || Math.abs(v - FILL) < 0.01f || v < -20f) ? 65535
                            : Math.max(0, Math.min(65534, Math.round((v + 2f) * 100f)));
                }
            }
            GridDefinition grid = latLonGrid(lon, lat, nx, ny, northToSouth);
            NormalizedRecord record = ctx.rasters().commit(FrameInput.builder(PRODUCT)
                    .validTime(time == null ? ctx.now() : time)
                    .grid(grid)
                    .bands(DecodedBands.codes(codes))
                    .upstreamKey("sst:" + box[0] + "," + box[1] + "," + box[2] + "," + box[3])
                    .variable(VARIABLE)
                    .build());
            if (time != null) {
                ctx.state().put(STATE_KEY, time.toString());
            }
            return FetchResult.of(record == null ? List.of() : List.of(record));
        } finally {
            ai.foresignal.raster.decode.GzipInputs.deleteQuietly(file);
        }
    }

    private double[] bbox(FetchContext ctx) {
        if (ctx.params() != null && ctx.params().has(BBOX_PARAM)) {
            var env = ctx.params().bbox(BBOX_PARAM);
            if (env != null) {
                return new double[] {env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY()};
            }
        }
        if (aoi != null && aoi.getIfAvailable() != null) {
            double[] env = aoi.getIfAvailable().envelope();
            double midLat = (env[1] + env[3]) / 2;
            double dLat = MARGIN_KM / 111.32;
            double dLon = MARGIN_KM / (111.32 * Math.max(0.2, Math.cos(Math.toRadians(midLat))));
            return new double[] {Math.max(-180, env[0] - dLon), Math.max(-90, env[1] - dLat),
                    Math.min(180, env[2] + dLon), Math.min(90, env[3] + dLat)};
        }
        return new double[] {-100, 15, -75, 40};
    }

    private String subsetUrl(double[] box) {
        return baseUrl + DATASET + ".nc?" + VARIABLE + "[(last)][" + box[1] + ":1:" + box[3] + "][" + box[0] + ":1:"
                + box[2] + "]";
    }

    private static GridDefinition latLonGrid(float[] lon, float[] lat, int nx, int ny, boolean northToSouth) {
        double dx = nx > 1 ? Math.abs(lon[nx - 1] - lon[0]) / (nx - 1) : 0.05;
        double dy = ny > 1 ? Math.abs(lat[ny - 1] - lat[0]) / (ny - 1) : 0.05;
        double lo1 = lon[0];
        double la1 = northToSouth ? lat[0] : lat[ny - 1];
        double west = Math.max(-180, lo1 - dx / 2);
        double east = Math.min(180, lo1 + nx * dx);
        double north = Math.min(90, la1 + dy / 2);
        double south = Math.max(-90, la1 - ny * dy + dy / 2);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("lo1", ((lo1 % 360) + 360) % 360);
        params.put("la1", la1);
        params.put("dx", dx);
        params.put("dy", dy);
        return new GridDefinition(GridDefinition.LATLON, nx, ny, west, south, east, north, params);
    }

    private static SourceDescriptor buildDescriptor() {
        ProductSpec.Attribution attribution = new ProductSpec.Attribution(
                "NOAA CoastWatch Geo-Polar Blended Sea Surface Temperature (via ERDDAP)", null,
                "https://coastwatch.noaa.gov/erddap/", "US-PD", null, false);
        ProductSpec product = ProductSpec.builder(PRODUCT)
                .title("Geo-Polar Blended SST")
                .category("satellite")
                .units("C")
                .dtype(RasterDtype.U16)
                .scaleOffset(0.01, -2.0)
                .nodata(65535.0)
                .defaultStyle("sst@1")
                .temporal(ProductSpec.Temporal.observation(Duration.ofHours(24), Duration.ofHours(48)))
                .spatial(new ProductSpec.Spatial("global", 4, 0, 3, null))
                .retention(ProductSpec.RetentionLadder.maxAge(Duration.ofDays(7)))
                .attribution(attribution)
                .coverageMeaning("The configured ocean bbox; transparent over land and missing data")
                .legendSummary("Sea surface temperature, °C (daily night analysis)")
                .build();
        return SourceDescriptor.builder(ID)
                .name("Geo-Polar Blended SST")
                .description("NOAA Geo-Polar Blended Sea Surface Temperature, daily, subset over a configurable bbox"
                        + " (default the AOI + 1000 km) from CoastWatch ERDDAP. Off by default on the small profile.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://coastwatch.noaa.gov/erddap/")
                .attribution("NOAA CoastWatch Geo-Polar Blended SST")
                .licence("US-PD")
                .termsUrl("https://www.noaa.gov/information-technology/open-data-dissemination")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(6), Duration.ofHours(6))
                .defaultBudget(Budget.of(10, 200, 2_000))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .param(ParamSpec.optional(BBOX_PARAM, ParamType.BBOX,
                        "The ocean bbox to subset (default the AOI + 1000 km)", null))
                .param(ParamSpec.optional(ai.foresignal.raster.ProductEnablement.PRODUCTS_PARAM, ParamType.STRING_LIST,
                        "Which products to ingest (default none on small, all on large)", List.of(PRODUCT)))
                .product(product)
                .build();
    }
}
