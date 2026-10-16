package ai.foresignal.source.adapters.heatrisk;

import ai.foresignal.raster.ProductEnablement;
import ai.foresignal.raster.FrameStore;
import ai.foresignal.raster.decode.GzipInputs;
import ai.foresignal.raster.geotiff.GeoReference;
import ai.foresignal.raster.geotiff.GeoTiffException;
import ai.foresignal.raster.geotiff.GeoTiffValues;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.DecodedBands;
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
import ai.foresignal.source.spi.UpstreamFormatException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * WPC/NWS HeatRisk (prompt 106; Weather-Imagery-Design §5.3): the daily experimental heat-risk
 * GeoTIFFs for days 1–7. The data page is read once a day (conditional GET), each day's
 * {@code HeatRisk_{n}_Mercator.tif} (EPSG:3857) is downloaded, its palette index (0 Little/None …
 * 4 Extreme) is read as a categorical u8 product, and it is stored on the file's own EPSG:3857
 * {@code affine} grid. On by default on both profiles (prompt 106 task 1). The descriptor notes "experimental NWS product"; US-PD.
 *
 * <p><strong>Verified 2026-10-15</strong>: the page links {@code data/HeatRisk_{1..7}_Mercator.tif};
 * the day-1 file is 2866 × 1672, 8-bit palette, LZW, EPSG:3857, GDAL nodata 53.
 */
@Component
public class HeatRiskAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "wpc-heatrisk";

    /** The data page. */
    public static final String PAGE_URL = "https://www.wpc.ncep.noaa.gov/heatrisk/data.html";

    /** Test-only base URL override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.heatrisk.base-url";

    /** The products, days 1–7. */
    public static final List<String> PRODUCTS = List.of("heatrisk-day1", "heatrisk-day2", "heatrisk-day3",
            "heatrisk-day4", "heatrisk-day5", "heatrisk-day6", "heatrisk-day7");

    /** The five categories. */
    static final int CATEGORIES = 5;

    /** The nodata code (u8). */
    static final int NO_DATA = 255;

    /** The products on by default on the small profile (all: HeatRisk is light, prompt 106 task 1). */
    public static final List<String> LIGHT_DEFAULT = PRODUCTS;

    /** The products on by default on the large profile. */
    public static final List<String> LARGE_DEFAULT = PRODUCTS;

    private static final int MAX_BYTES = 8 * 1024 * 1024;
    private static final Pattern LINK = Pattern.compile("data/HeatRisk_(\\d)_Mercator\\.tif");

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final String profile;

    /** Creates the adapter. */
    @Autowired
    public HeatRiskAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${foresignal.profile:small}") String profile) {
        this.baseUrl = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.profile = profile;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    private URI uri(String path) {
        return baseUrl.isEmpty() ? URI.create(path) : URI.create(baseUrl + path);
    }

    /** The links of the data page: day number → absolute URL. */
    static Map<Integer, String> links(String html) {
        Map<Integer, String> out = new LinkedHashMap<>();
        Matcher m = LINK.matcher(html);
        while (m.find()) {
            int day = Integer.parseInt(m.group(1));
            out.putIfAbsent(day, "https://www.wpc.ncep.noaa.gov/heatrisk/data/HeatRisk_" + day + "_Mercator.tif");
        }
        return out;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Set<String> enabled = ProductEnablement.enabled(ctx.params(), profile, PRODUCTS, LIGHT_DEFAULT,
                LARGE_DEFAULT);
        if (enabled.isEmpty()) {
            return FetchResult.of(List.of());
        }
        String html;
        try {
            html = new String(ctx.http().get(uri(PAGE_URL)).body(), StandardCharsets.UTF_8);
        } catch (AdapterException e) {
            throw e;
        }
        Map<Integer, String> links = links(html);
        if (links.isEmpty()) {
            throw new AdapterException("HeatRisk data page has no GeoTIFF links");
        }
        Instant day = ctx.now().atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).toInstant();
        List<NormalizedRecord> out = new ArrayList<>();
        for (Map.Entry<Integer, String> e : links.entrySet()) {
            String product = "heatrisk-day" + e.getKey();
            if (!enabled.contains(product)) {
                continue;
            }
            Path file;
            try {
                file = ctx.http().download(uri(e.getValue()), Path.of(ID + "-d" + e.getKey() + "-"
                        + System.nanoTime() + ".tif"), MAX_BYTES).file();
            } catch (AdapterException ex) {
                ctx.log().warn("HeatRisk day {}: {}", e.getKey(), ex.getMessage());
                continue;
            }
            try {
                GeoTiffValues.Values values = GeoTiffValues.read(java.nio.file.Files.readAllBytes(file));
                String sha = FrameStore.sha256Hex(java.nio.file.Files.readAllBytes(file));
                String key = "heatrisk-day" + e.getKey();
                if (sha.equals(ctx.state().get("sha:" + key).orElse(null))) {
                    continue;
                }
                int[] codes = new int[values.width() * values.height()];
                for (int i = 0; i < codes.length; i++) {
                    int v = values.samples()[i];
                    codes[i] = v >= 0 && v < CATEGORIES ? v : NO_DATA;
                }
                NormalizedRecord record = ctx.rasters().commit(FrameInput.builder(product)
                        .validTime(day.plus(e.getKey() - 1L, ChronoUnit.DAYS))
                        .grid(grid(values))
                        .bands(DecodedBands.codes(codes))
                        .upstreamKey(e.getValue())
                        .variable("heatrisk")
                        .build());
                ctx.state().put("sha:" + key, sha);
                if (record != null) {
                    out.add(record);
                }
            } catch (GeoTiffException | java.io.IOException | UpstreamFormatException ex) {
                ctx.rasters().reportFormatChange(product, ex.getMessage());
                ctx.log().warn("HeatRisk day {}: {}", e.getKey(), ex.getMessage());
            } finally {
                GzipInputs.deleteQuietly(file);
            }
        }
        return FetchResult.of(out);
    }

    /**
     * The frame's grid: the GeoTIFF's own geotransform as an {@code affine} grid in its CRS
     * (EPSG:3857 for the WPC files). Rows are spaced uniformly in Mercator meters, not in latitude, so
     * a latlon grid with one {@code dy} would misplace mid-CONUS rows by about a degree; the affine grid
     * inverts the Mercator projection per row exactly. The box is the WGS84 envelope of the four pixel
     * corners.
     */
    static GridDefinition grid(GeoTiffValues.Values v) {
        GeoReference ref = v.georeference();
        double west = Double.POSITIVE_INFINITY;
        double east = Double.NEGATIVE_INFINITY;
        double south = Double.POSITIVE_INFINITY;
        double north = Double.NEGATIVE_INFINITY;
        for (double[] corner : new double[][] {{0, 0}, {v.width(), 0}, {0, v.height()}, {v.width(), v.height()}}) {
            double[] lonLat = ref.lonLat(corner[0], corner[1]);
            west = Math.min(west, lonLat[0]);
            east = Math.max(east, lonLat[0]);
            south = Math.min(south, lonLat[1]);
            north = Math.max(north, lonLat[1]);
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("crs", ref.crs());
        double[] gt = ref.gt();
        for (int k = 0; k < 6; k++) {
            params.put("gt" + k, gt[k]);
        }
        return new GridDefinition(GridDefinition.AFFINE, v.width(), v.height(), Math.max(-180, west),
                Math.max(-90, south), Math.min(180, east), Math.min(90, north), params);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("WPC HeatRisk (experimental)")
                .description("The NWS/WPC experimental HeatRisk for days 1–7: a categorical heat-risk index"
                        + " (0 Little/None … 4 Extreme) over CONUS, read from the daily Mercator GeoTIFFs. Experimental"
                        + " NWS product; US public domain. On by default on both profiles.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://www.wpc.ncep.noaa.gov/heatrisk/")
                .attribution("NOAA/NWS Weather Prediction Center HeatRisk (experimental)")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(6), Duration.ofHours(6))
                .defaultBudget(Budget.of(10, 200, 300))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(MAX_BYTES)
                .param(ParamSpec.optional(ProductEnablement.PRODUCTS_PARAM, ParamType.STRING_LIST,
                        "Which HeatRisk days to ingest (default days 1-7 on both profiles)", null));
        for (int d = 1; d <= 7; d++) {
            b.product(ProductSpec.builder("heatrisk-day" + d)
                    .title("HeatRisk day " + d)
                    .category("model")
                    .units("index")
                    .dtype(RasterDtype.U8)
                    .scaleOffset(1.0, 0.0)
                    .nodata((double) NO_DATA)
                    .code(0, "noCoverage")
                    .defaultStyle("heatrisk@1")
                    .temporal(ProductSpec.Temporal.observation(Duration.ofHours(24), Duration.ofHours(48)))
                    .spatial(new ProductSpec.Spatial("CONUS", 6, 0, 5, null))
                    .retention(ProductSpec.RetentionLadder.maxAge(Duration.ofDays(7)))
                    .attribution(new ProductSpec.Attribution("NOAA/NWS Weather Prediction Center HeatRisk"
                            + " (experimental)", null, "https://www.wpc.ncep.noaa.gov/heatrisk/", "US-PD", null, false))
                    .coverageMeaning("CONUS; categories 0 Little/None … 4 Extreme; experimental NWS product")
                    .legendSummary("HeatRisk category for day " + d)
                    .build());
        }
        return b.build();
    }
}
