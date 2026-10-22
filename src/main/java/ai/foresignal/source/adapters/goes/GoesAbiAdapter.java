package ai.foresignal.source.adapters.goes;

import ai.foresignal.raster.GeosGrid;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.ProductEnablement;
import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.DecoderCapability;
import ai.foresignal.raster.decode.DecoderDependent;
import ai.foresignal.raster.decode.GeosGrids;
import ai.foresignal.raster.decode.GzipInputs;
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.S3Object;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * GOES-East (GOES-19) ABI Cloud and Moisture Imagery (prompt 046, extended in prompt 106):
 * <ul>
 *   <li>{@value #IR} (band 13, clean IR) — the v1 product;</li>
 *   <li>{@value #WV8} and {@value #WV9} (bands 8/9, water vapor), u16 0.01 K;</li>
 *   <li>{@value #VIS} (band 2, 0.5 km visible), u8 reflectance, cropped in scan-angle space to the
 *       places AOI + 300 km, and skipped when the solar zenith exceeds 85° over the whole AOI;</li>
 *   <li>{@value #MESO1} and {@value #MESO2} (mesoscale sectors 1/2, 1-minute CMIPM band 13), ingested
 *       only while their footprint intersects the AOI, keeping every 2nd frame ({@code mesoStride}).</li>
 * </ul>
 * Objects are listed per hour with {@code start-after} the last committed key and filtered by the
 * channel's key pattern; the scan mode is never hard-coded. Products are enabled per profile (small:
 * C13/C08/C09; large adds C02; mesoscale opt-in) and overridable with the {@code products} param; the
 * param has no descriptor default, so an unset param means the profile default.
 */
@Component
public class GoesAbiAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "goes-east-abi";

    /** The GOES-19 bucket. */
    public static final String BUCKET = "noaa-goes19";

    /** The band 13 product. */
    public static final String IR = "goes-east-ir";

    /** The band 8 water-vapor product (prompt 106). */
    public static final String WV8 = "goes-east-wv8";

    /** The band 9 water-vapor product (prompt 106). */
    public static final String WV9 = "goes-east-wv9";

    /** The band 2 visible product (prompt 106). */
    public static final String VIS = "goes-east-vis";

    /** The mesoscale sector 1 product (prompt 106). */
    public static final String MESO1 = "goes-east-meso1";

    /** The mesoscale sector 2 product (prompt 106). */
    public static final String MESO2 = "goes-east-meso2";

    /** Test-only: a path-style base URL for object downloads ({@code {base}/{bucket}/{key}}). */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.goes.base-url";

    /** New frames committed per channel per run. */
    public static final int MAX_FRAMES_PER_RUN = 15;

    /** Hours listed back from now at most. */
    public static final int MAX_HOURS_BACK = 3;

    /** The response cap, and the largest object downloaded. */
    static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    /** The adapter-state key prefix of the last committed key. */
    static final String STATE_PREFIX = "lastKey:";

    /** The C02 crop margin around the AOI, km. */
    static final double VIS_MARGIN_KM = 300;

    /** The solar zenith above which a visible frame is skipped, degrees. */
    static final double VIS_MAX_SOLAR_ZENITH = 85;

    /** The band 13 key pattern of GOES-19; group 1 is the {@code s} start time. */
    static final Pattern KEY = Pattern.compile(
            "^ABI-L2-CMIPC/\\d{4}/\\d{3}/\\d{2}/OR_ABI-L2-CMIPC-M\\dC13_G19_s(\\d{14})_e\\d{14}_c\\d{14}\\.nc$");

    /** What band 13 is decoded with (kept for the 046 tests). */
    static final DecodeRequest REQUEST = DecodeRequest.netcdf(IR, "CMI").withQuality("DQF", 1);

    /**
     * One listed scan.
     *
     * @param key the S3 key
     * @param time the {@code s} start time
     * @param size the object size from the listing
     */
    public record Frame(String key, Instant time, long size) {}

    /**
     * One ABI channel.
     *
     * @param productId the product id
     * @param kind {@code CMIPC} or {@code CMIPM}
     * @param band the channel number ({@code 13}, {@code 08}, …)
     * @param sector {@code ""} for CMIPC, {@code 1} or {@code 2} for CMIPM
     * @param code {@code ir}/{@code wv} for u16 kelvin, {@code vis} for u8 reflectance
     * @param temporal the temporal spec
     * @param spatial the spatial spec
     * @param ladder the retention ladder
     * @param title product title
     * @param units units
     * @param scale code scale
     * @param offset code offset
     * @param nodata nodata code
     * @param styles styles (first is the default)
     * @param meaning coverage meaning
     * @param legend legend summary
     * @param cropAoi true to crop to the AOI + {@value #VIS_MARGIN_KM} km
     * @param daytimeOnly true to skip a frame whose solar zenith exceeds {@value #VIS_MAX_SOLAR_ZENITH}°
     * @param aoiGated true to ingest only while the footprint intersects the AOI
     * @param stride keep every Nth frame (1 = all)
     */
    public record Channel(String productId, String kind, String band, String sector, String code,
            ProductSpec.Temporal temporal, ProductSpec.Spatial spatial, ProductSpec.RetentionLadder ladder,
            String title, String units, double scale, double offset, Double nodata, List<String> styles,
            String meaning, String legend, boolean cropAoi, boolean daytimeOnly, boolean aoiGated, int stride) {

        /** The decode request. */
        DecodeRequest request() {
            return DecodeRequest.netcdf(productId, "CMI").withQuality("DQF", 1);
        }

        /** The S3 key pattern for {@code sat}. */
        Pattern pattern(String sat) {
            return Pattern.compile("^ABI-L2-" + kind + "/\\d{4}/\\d{3}/\\d{2}/OR_ABI-L2-" + kind + sector
                    + "-M\\dC" + band + "_" + sat + "_s(\\d{14})_e\\d{14}_c\\d{14}\\.nc$");
        }

        /** The listing prefix of {@code time}'s hour. */
        String hourPrefix(Instant time) {
            LocalDateTime t = LocalDateTime.ofInstant(time, ZoneOffset.UTC);
            return String.format(Locale.ROOT, "ABI-L2-%s/%04d/%03d/%02d/OR_ABI-L2-%s%s-M", kind, t.getYear(),
                    t.getDayOfYear(), t.getHour(), kind, sector);
        }

        /** The state key of the last committed object. */
        String stateKey() {
            return STATE_PREFIX + productId;
        }
    }

    /** The east channels, in descriptor order. */
    static final List<Channel> CHANNELS = List.of(
            channel(IR, "CMIPC", "13", "", "ir", "CONUS", 7, 6, "ir-gray@1", "ir-enhanced@1",
                    "GOES-East clean infrared (ABI band 13)", "Brightness temperature at 10.3 µm, K; cold (bright) is"
                            + " high cloud", Duration.ofHours(24), false, false, 1),
            channel(WV8, "CMIPC", "08", "", "wv", "CONUS", 7, 6, "wv-gray@1", "wv-enhanced@1",
                    "GOES-East water vapor (ABI band 8)", "Brightness temperature at 6.2 µm, K; mid-level water"
                            + " vapor", Duration.ofHours(24), false, false, 1),
            channel(WV9, "CMIPC", "09", "", "wv", "CONUS", 7, 6, "wv-gray@1", "wv-enhanced@1",
                    "GOES-East water vapor (ABI band 9)", "Brightness temperature at 6.9 µm, K; low/mid water"
                            + " vapor", Duration.ofHours(24), false, false, 1),
            channel(VIS, "CMIPC", "02", "", "vis", "CONUS", 8, 7, "vis-gray@1", null,
                    "GOES-East visible (ABI band 2)", "Reflectance at 0.64 µm (daytime), cropped to the AOI",
                    Duration.ofHours(6), true, false, 1),
            channel(MESO1, "CMIPM", "13", "1", "ir", "CONUS", 8, 7, "ir-gray@1", "ir-enhanced@1",
                    "GOES-East mesoscale sector 1 (ABI band 13)", "Brightness temperature at 10.3 µm, K", Duration.ofHours(24),
                    false, true, 2),
            channel(MESO2, "CMIPM", "13", "2", "ir", "CONUS", 8, 7, "ir-gray@1", "ir-enhanced@1",
                    "GOES-East mesoscale sector 2 (ABI band 13)", "Brightness temperature at 10.3 µm, K", Duration.ofHours(24),
                    false, true, 2));

    /** The products on by default for the small profile (prompt 106 task 1: C13 plus C08/C09). */
    public static final List<String> LIGHT_DEFAULT = List.of(IR, WV8, WV9);

    /**
     * The products on by default for the large profile: also C02. The mesoscale sectors are opt-in on
     * both profiles.
     */
    public static final List<String> LARGE_DEFAULT = List.of(IR, WV8, WV9, VIS);

    private final NetcdfDecoder decoder;
    private final String baseUrl;
    private final String profile;
    private final ObjectProvider<AreaOfInterest> aoi;
    private final String sourceId;
    private final String bucket;
    private final String sat;
    private final String label;
    private final List<Channel> channels;
    private final List<String> lightDefault;
    private final List<String> largeDefault;
    private final SourceDescriptor descriptor;

    /** Creates the GOES-East adapter. */
    @Autowired
    public GoesAbiAdapter(NetcdfDecoder decoder, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${foresignal.profile:small}") String profile, ObjectProvider<AreaOfInterest> aoi) {
        this(decoder, baseUrlOverride, profile, aoi, ID, BUCKET, "G19", "GOES-East (GOES-19)", CHANNELS,
                LIGHT_DEFAULT, LARGE_DEFAULT);
    }

    /** Creates the adapter against a stubbed bucket (tests). */
    public GoesAbiAdapter(NetcdfDecoder decoder, String baseUrlOverride) {
        this(decoder, baseUrlOverride, "small", null, ID, BUCKET, "G19", "GOES-East (GOES-19)", CHANNELS,
                LIGHT_DEFAULT, LARGE_DEFAULT);
    }

    /** Creates an adapter for a satellite. */
    protected GoesAbiAdapter(NetcdfDecoder decoder, String baseUrl, String profile, ObjectProvider<AreaOfInterest> aoi,
            String sourceId, String bucket, String sat, String label, List<Channel> channels, List<String> lightDefault,
            List<String> largeDefault) {
        this.decoder = decoder;
        String chosen = baseUrl == null ? "" : baseUrl.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.profile = profile;
        this.aoi = aoi;
        this.sourceId = sourceId;
        this.bucket = bucket;
        this.sat = sat;
        this.label = label;
        this.channels = List.copyOf(channels);
        this.lightDefault = List.copyOf(lightDefault);
        this.largeDefault = List.copyOf(largeDefault);
        this.descriptor = buildDescriptor(sourceId, bucket, label, channels, this.lightDefault, this.largeDefault);
    }

    /** One channel helper. */
    static Channel channel(String productId, String kind, String band, String sector, String code,
            String coverage, int nativeMaxZoom, int maxZoomForAnimation, String style, String altStyle, String title,
            String legend, Duration maxAge, boolean daytimeOnly, boolean aoiGated, int stride) {
        ProductSpec.Temporal temporal = ProductSpec.Temporal.observation(
                "CMIPM".equals(kind) ? Duration.ofMinutes(1) : Duration.ofMinutes(5),
                "CMIPM".equals(kind) ? Duration.ofMinutes(4) : Duration.ofMinutes(15));
        List<String> styles = altStyle == null ? List.of(style) : List.of(style, altStyle);
        boolean vis = "vis".equals(code);
        RasterDtype dtype = vis ? RasterDtype.U8 : RasterDtype.U16;
        double scale = vis ? 0.01 : 0.01;
        double offset = vis ? 0.0 : GoesCodec.OFFSET_K;
        Double nodata = vis ? (double) GoesCodec.VIS_NO_DATA : (double) GoesCodec.NO_DATA;
        return new Channel(productId, kind, band, sector, code, temporal,
                new ProductSpec.Spatial(coverage, nativeMaxZoom, 0, maxZoomForAnimation, null),
                new ProductSpec.RetentionLadder(List.of(new ProductSpec.RetentionLadder.Rung(maxAge,
                        Duration.ofMinutes(30))), Duration.ofHours(72), null), title,
                vis ? "reflectance" : "K", scale, offset, nodata, styles,
                "The " + label(coverage) + "; transparent outside the scan or where quality is poor", legend,
                vis, daytimeOnly, aoiGated, stride);
    }

    private static String label(String coverage) {
        return "CONUS sector";
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public Map<String, Set<DecoderCapability>> decoderRequirements() {
        java.util.LinkedHashMap<String, Set<DecoderCapability>> out = new java.util.LinkedHashMap<>();
        for (Channel c : channels) {
            out.put(c.productId(), Set.of(DecoderCapability.NETCDF4));
        }
        return out;
    }

    /** The URL of {@code key}. */
    public URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + bucket + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + bucket + "/" + key);
    }

    /** The listing prefix of the hour holding {@code time} (band 13, for the 046 tests). */
    public static String hourPrefix(Instant time) {
        return CHANNELS.get(0).hourPrefix(time);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        if (ctx.rasters().probing()) {
            return probe(ctx);
        }
        Set<String> enabled = ProductEnablement.enabled(ctx.params(), profile,
                channels.stream().map(Channel::productId).toList(), lightDefault, largeDefault);
        List<NormalizedRecord> out = new ArrayList<>();
        for (Channel channel : channels) {
            if (!enabled.contains(channel.productId())
                    || (ctx.rasters().disabled(channel.productId()) && channels.size() == 1)) {
                continue;
            }
            out.addAll(fetchChannel(ctx, channel));
        }
        return FetchResult.of(out);
    }

    private List<NormalizedRecord> fetchChannel(FetchContext ctx, Channel channel) throws AdapterException {
        if (ctx.rasters().disabled(channel.productId())) {
            return List.of();
        }
        String last = ctx.state().get(channel.stateKey()).orElse(null);
        Instant lastTime = last == null ? null : startTimeOf(channel, last).orElse(null);
        Instant now = ctx.now();
        List<Frame> frames = listNew(ctx, channel, last, lastTime, now);
        List<NormalizedRecord> out = new ArrayList<>();
        String cursor = last;
        int committed = 0;
        try {
            for (Frame frame : frames) {
                if (committed >= MAX_FRAMES_PER_RUN) {
                    break;
                }
                cursor = frame.key();
                if (frame.size() > MAX_RESPONSE_BYTES) {
                    ctx.log().warn("GOES {}: skipping {} ({} bytes over the cap)", channel.productId(), frame.key(),
                            frame.size());
                    continue;
                }
                if (channel.stride() > 1 && (frame.time().getEpochSecond() / 60) % channel.stride() != 0) {
                    continue;
                }
                if (channel.daytimeOnly() && solarZenithOverAoi(ctx, frame.time()) > VIS_MAX_SOLAR_ZENITH) {
                    continue;
                }
                try {
                    NormalizedRecord record = commit(ctx, channel, frame);
                    if (record != null) {
                        out.add(record);
                    }
                } catch (SilentFormatChangeException e) {
                    ctx.rasters().reportFormatChange(channel.productId(), e.getMessage());
                    ctx.log().warn("GOES {}: {} ({})", channel.productId(), e.getMessage(), frame.key());
                }
                committed++;
            }
        } catch (AdapterException e) {
            if (out.isEmpty()) {
                saveCursor(ctx, channel, cursor, last);
                throw e;
            }
            ctx.log().warn("GOES {}: committed {} frame(s), then failed: {}", channel.productId(), out.size(),
                    e.getMessage());
        }
        saveCursor(ctx, channel, cursor, last);
        return out;
    }

    private NormalizedRecord commit(FetchContext ctx, Channel channel, Frame frame) throws AdapterException {
        Path file = download(ctx, channel, frame);
        try {
            DecodedGrid header = decoder.decode(file, channel.request().header());
            if (channel.aoiGated() && !intersectsAoi(header)) {
                return null;
            }
            int[] window = channel.cropAoi() ? cropWindow(header) : null;
            var grid = window == null ? header.gridDefinition() : croppedGrid(header, window);
            NormalizedRecord record = ctx.rasters().commit(FrameInput.builder(channel.productId())
                    .validTime(frame.time())
                    .observationPeriod(instant(header.metadata().get("obsStart")).orElse(frame.time()),
                            instant(header.metadata().get("obsEnd")).orElse(null))
                    .grid(grid)
                    .file(file, f -> {
                        DecodedGrid g = decoder.decode(f, channel.request());
                        return DecodedBands.codes(quantize(channel, g, window));
                    })
                    .upstreamKey(frame.key())
                    .variable(channel.request().variable())
                    .build());
            return record;
        } finally {
            GzipInputs.deleteQuietly(file);
        }
    }

    /** Quantizes a decoded channel, cropping to {@code window} when one is given. */
    private int[] quantize(Channel channel, DecodedGrid grid, int[] window) {
        float[] data = grid.floatData();
        if (window != null) {
            data = crop(data, grid.nx(), grid.ny(), window);
        }
        return "vis".equals(channel.code()) ? GoesCodec.visible(data) : GoesCodec.brightness(data);
    }

    /** {@code {col0, row0, cols, rows}} of the AOI + {@value #VIS_MARGIN_KM} km, or null when no AOI. */
    int[] cropWindow(DecodedGrid header) {
        if (aoi == null || aoi.getIfAvailable() == null) {
            return null;
        }
        return cropWindow((GeosGrid) GridDef.of(header.gridDefinition()), aoi.getIfAvailable().envelope());
    }

    /** Points sampled along each side of the AOI box when it is mapped to scan angles. */
    static final int CROP_EDGE_SAMPLES = 32;

    /**
     * The crop window of {@code env} (the AOI envelope {@code [west, south, east, north]}) widened by
     * {@value #VIS_MARGIN_KM} km, worked out in scan-angle space: points along the four sides of the
     * widened box (a geographic box's sides curve in the geostationary view, so its corners alone do
     * not bound it) are mapped to scan angles with {@link GeosGrid#scanAngles}, which, unlike
     * {@link GeosGrid#lonLatToIndex}, keeps points beyond the image so the window clamps to the image
     * instead of shrinking; the min/max scan angles become column and row indices.
     *
     * @return {@code {col0, row0, cols, rows}}, or null when the box is not on the visible disc or does
     *     not overlap the image
     */
    static int[] cropWindow(GeosGrid grid, double[] env) {
        double midLat = (env[1] + env[3]) / 2;
        double dLat = VIS_MARGIN_KM / 111.32;
        double dLon = VIS_MARGIN_KM / (111.32 * Math.max(0.2, Math.cos(Math.toRadians(midLat))));
        double w = env[0] - dLon;
        double e = env[2] + dLon;
        double s = Math.max(-90, env[1] - dLat);
        double n = Math.min(90, env[3] + dLat);
        double minCol = Double.POSITIVE_INFINITY;
        double maxCol = Double.NEGATIVE_INFINITY;
        double minRow = Double.POSITIVE_INFINITY;
        double maxRow = Double.NEGATIVE_INFINITY;
        for (int k = 0; k <= CROP_EDGE_SAMPLES; k++) {
            double f = (double) k / CROP_EDGE_SAMPLES;
            double lon = w + f * (e - w);
            double lat = s + f * (n - s);
            for (double[] p : new double[][] {{lon, s}, {lon, n}, {w, lat}, {e, lat}, {lon, lat}}) {
                double[] xy = grid.scanAngles(p[0], p[1]);
                if (Double.isNaN(xy[0]) || Double.isNaN(xy[1])) {
                    continue;
                }
                double col = (xy[0] - grid.x0()) / grid.dx();
                double row = (xy[1] - grid.y0()) / grid.dy();
                minCol = Math.min(minCol, col);
                maxCol = Math.max(maxCol, col);
                minRow = Math.min(minRow, row);
                maxRow = Math.max(maxRow, row);
            }
        }
        if (minCol > maxCol) {
            return null;
        }
        int col0 = (int) Math.max(0, Math.floor(minCol) - 1);
        int row0 = (int) Math.max(0, Math.floor(minRow) - 1);
        int col1 = (int) Math.min(grid.nx() - 1, Math.ceil(maxCol) + 1);
        int row1 = (int) Math.min(grid.ny() - 1, Math.ceil(maxRow) + 1);
        if (col1 < col0 || row1 < row0) {
            return null;
        }
        return new int[] {col0, row0, col1 - col0 + 1, row1 - row0 + 1};
    }

    /**
     * The {@code geos} grid definition of a cropped window: the scan angles of the window's first
     * column and row ({@code x0 + col0·dx}, {@code y0 + row0·dy}, radians) with the same steps, and the
     * WGS84 box of the cropped grid projected back from scan angles ({@link GeosGrids#boxOf}). The box
     * is never derived from the parent box plus scan-angle steps: {@code dx}/{@code dy} are radians, not
     * degrees.
     */
    static ai.foresignal.source.spi.GridDefinition croppedGrid(DecodedGrid header, int[] window) {
        var def = header.gridDefinition();
        java.util.Map<String, Object> params = new java.util.LinkedHashMap<>(def.params());
        GeosGrid parent = (GeosGrid) GridDef.of(def);
        double x0 = parent.x0() + window[0] * parent.dx();
        double y0 = parent.y0() + window[1] * parent.dy();
        params.put("x0", x0);
        params.put("y0", y0);
        GeosGrid cropped = new GeosGrid(window[2], window[3], parent.lon0(), parent.h(), parent.a(), parent.b(),
                parent.sweepX(), x0, parent.dx(), y0, parent.dy(), new double[] {-180, -90, 180, 90});
        double[] box = GeosGrids.boxOf(cropped);
        return new ai.foresignal.source.spi.GridDefinition(ai.foresignal.source.spi.GridDefinition.GEOS, window[2],
                window[3], box[0], box[1], box[2], box[3], params);
    }

    /** Crops a row-major north→south field. */
    static float[] crop(float[] data, int nx, int ny, int[] window) {
        int cols = window[2];
        int rows = window[3];
        float[] out = new float[cols * rows];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(data, (window[1] + r) * nx + window[0], out, r * cols, cols);
        }
        return out;
    }

    /** True when a mesoscale grid's box intersects the AOI envelope. */
    private boolean intersectsAoi(DecodedGrid header) {
        if (aoi == null || aoi.getIfAvailable() == null) {
            return true;
        }
        var box = header.gridDefinition();
        return AreaOfInterest.intersects(aoi.getIfAvailable().envelope(), box.west(), box.south(), box.east(),
                box.north());
    }

    /**
     * The smallest solar zenith over the AOI at {@code time}, degrees: the zenith at the corners, edge
     * midpoints and center of the AOI envelope (a 3 × 3 lattice), so a frame is skipped only when the
     * sun is more than {@value #VIS_MAX_SOLAR_ZENITH}° from the zenith over the <em>whole</em> AOI
     * (prompt 106 task 2). Without an AOI, a central Texas point is used.
     */
    double solarZenithOverAoi(FetchContext ctx, Instant time) {
        double[] env = aoi != null && aoi.getIfAvailable() != null ? aoi.getIfAvailable().envelope()
                : new double[] {-97.0, 31.0, -97.0, 31.0};
        return minSolarZenith(env, time);
    }

    /** The smallest solar zenith over a 3 × 3 lattice of {@code env}, degrees. */
    static double minSolarZenith(double[] env, Instant time) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                double lon = env[0] + (env[2] - env[0]) * i / 2.0;
                double lat = env[1] + (env[3] - env[1]) * j / 2.0;
                min = Math.min(min, solarZenith(lon, lat, time));
            }
        }
        return min;
    }

    /** The solar zenith at a point at {@code time}, degrees (NOAA approximate formula). */
    static double solarZenith(double lon, double lat, Instant time) {
        double dayOfYear = time.atZone(ZoneOffset.UTC).getDayOfYear();
        double hour = time.atZone(ZoneOffset.UTC).getHour() + time.atZone(ZoneOffset.UTC).getMinute() / 60.0;
        double gamma = 2 * Math.PI / 365 * (dayOfYear - 1 + (hour - 12) / 24);
        double eqTime = 229.18 * (0.000075 + 0.001868 * Math.cos(gamma) - 0.032077 * Math.sin(gamma)
                - 0.014615 * Math.cos(2 * gamma) - 0.040849 * Math.sin(2 * gamma));
        double decl = 0.006918 - 0.399912 * Math.cos(gamma) + 0.070257 * Math.sin(gamma)
                - 0.006758 * Math.cos(2 * gamma) + 0.000907 * Math.sin(2 * gamma)
                - 0.002697 * Math.cos(3 * gamma) + 0.00148 * Math.sin(3 * gamma);
        double trueSolarTime = hour * 60 + eqTime + 4 * lon;
        double hourAngle = Math.toRadians(trueSolarTime / 4 - 180);
        double latRad = Math.toRadians(lat);
        double cosZenith = Math.sin(latRad) * Math.sin(decl) + Math.cos(latRad) * Math.cos(decl) * Math.cos(hourAngle);
        return Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, cosZenith))));
    }

    /** Probes: the newest band 13 scan, header only; writes nothing. */
    FetchResult probe(FetchContext ctx) throws AdapterException {
        Channel channel = channels.get(0);
        Instant now = ctx.now();
        Frame newest = null;
        for (Instant hour : List.of(now, now.minus(1, ChronoUnit.HOURS))) {
            S3Listing listing = ctx.http().listS3(bucket, channel.hourPrefix(hour), null);
            for (S3Object object : listing.objects()) {
                Optional<Frame> frame = frameOf(channel, object);
                if (frame.isPresent() && (newest == null || frame.get().time().isAfter(newest.time()))) {
                    newest = frame.get();
                }
            }
            if (newest != null) {
                break;
            }
        }
        if (newest == null) {
            return FetchResult.of(List.of());
        }
        Path file = download(ctx, channel, newest);
        try {
            DecodedGrid header = decoder.decode(file, channel.request().header());
            NormalizedRecord preview = ctx.rasters().commit(FrameInput.builder(channel.productId())
                    .validTime(newest.time())
                    .grid(header.gridDefinition())
                    .file(file, f -> {
                        throw new IllegalStateException("A probe never decodes samples");
                    })
                    .upstreamKey(newest.key())
                    .variable(channel.request().variable())
                    .build());
            return FetchResult.of(preview == null ? List.of() : List.of(preview));
        } finally {
            GzipInputs.deleteQuietly(file);
        }
    }

    /** The new band 13 scans after {@code lastTime}, oldest first (kept for the 046 tests). */
    List<Frame> listNew(FetchContext ctx, String last, Instant lastTime, Instant now) throws AdapterException {
        return listNew(ctx, channels.get(0), last, lastTime, now);
    }

    /** The new scans of {@code channel} after {@code lastTime}, oldest first. */
    List<Frame> listNew(FetchContext ctx, Channel channel, String last, Instant lastTime, Instant now)
            throws AdapterException {
        Instant nowHour = now.truncatedTo(ChronoUnit.HOURS);
        Instant earliest = nowHour.minus(MAX_HOURS_BACK, ChronoUnit.HOURS);
        Instant firstHour = lastTime == null ? nowHour.minus(1, ChronoUnit.HOURS) : lastTime.truncatedTo(ChronoUnit.HOURS);
        if (firstHour.isBefore(earliest)) {
            firstHour = earliest;
        }
        Pattern pattern = channel.pattern(sat);
        List<Frame> frames = new ArrayList<>();
        for (Instant hour = firstHour; !hour.isAfter(nowHour); hour = hour.plus(1, ChronoUnit.HOURS)) {
            String prefix = channel.hourPrefix(hour);
            String startAfter = last != null && last.startsWith(prefix) ? last : null;
            S3Listing listing = ctx.http().listS3(bucket, prefix, startAfter);
            while (true) {
                for (S3Object object : listing.objects()) {
                    frame(channel, pattern, object).filter(f -> lastTime == null || f.time().isAfter(lastTime))
                            .ifPresent(frames::add);
                }
                if (!listing.truncated() || listing.nextStartAfter() == null) {
                    break;
                }
                listing = ctx.http().listS3(bucket, prefix, listing.nextStartAfter());
            }
        }
        frames.sort(Comparator.comparing(Frame::time).thenComparing(Frame::key));
        return frames;
    }

    /** The scan of a listed object for band 13 (kept for the 046 tests). */
    static Optional<Frame> frame(S3Object object) {
        return frame(CHANNELS.get(0), KEY, object);
    }

    /** The scan of a listed object for this satellite's channel. */
    Optional<Frame> frameOf(Channel channel, S3Object object) {
        return frame(channel, channel.pattern(sat), object);
    }

    static Optional<Frame> frame(Channel channel, Pattern pattern, S3Object object) {
        return startTime(pattern, object.key()).map(time -> new Frame(object.key(), time, object.size()));
    }

    /** The {@code s} start time of a band 13 key (kept for the 046 tests). */
    static Optional<Instant> startTime(String key) {
        return startTime(KEY, key);
    }

    /** The {@code s} start time of {@code key} for this satellite's channel. */
    Optional<Instant> startTimeOf(Channel channel, String key) {
        return startTime(channel.pattern(sat), key);
    }

    static Optional<Instant> startTime(Pattern pattern, String key) {
        Matcher m = pattern.matcher(key);
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(parseStart(m.group(1)));
    }

    /** {@code YYYYDDDHHMMSSt} as an instant (kept for the 046 tests). */
    static Instant parseStart(String s) {
        int year = Integer.parseInt(s.substring(0, 4));
        int doy = Integer.parseInt(s.substring(4, 7));
        int hour = Integer.parseInt(s.substring(7, 9));
        int minute = Integer.parseInt(s.substring(9, 11));
        int second = Integer.parseInt(s.substring(11, 13));
        int tenths = Integer.parseInt(s.substring(13, 14));
        return LocalDateTime.of(year, 1, 1, hour, minute, second).plusDays(doy - 1L).toInstant(ZoneOffset.UTC)
                .plusMillis(tenths * 100L);
    }

    private Path download(FetchContext ctx, Channel channel, Frame frame) throws AdapterException {
        String name = channel.productId() + "-" + frame.time().toEpochMilli() + "-" + Long.toHexString(System.nanoTime())
                + ".nc";
        DownloadResult result = ctx.http().download(objectUri(frame.key()), Path.of(name), MAX_RESPONSE_BYTES, frame.size());
        if (frame.size() > 0 && result.bytes() != frame.size()) {
            GzipInputs.deleteQuietly(result.file());
            throw new AdapterException("GOES " + frame.key() + ": downloaded " + result.bytes() + " bytes, listing said "
                    + frame.size());
        }
        return result.file();
    }

    private static Optional<Instant> instant(Object value) {
        if (!(value instanceof String text)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(text));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private static void saveCursor(FetchContext ctx, Channel channel, String cursor, String last) {
        if (cursor != null && !cursor.equals(last)) {
            ctx.state().put(channel.stateKey(), cursor);
        }
    }

    static SourceDescriptor buildDescriptor(String sourceId, String bucket, String label, List<Channel> channels,
            List<String> lightDefault, List<String> largeDefault) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(sourceId)
                .name(label + " ABI imagery")
                .description(label + " ABI Level 2 Cloud and Moisture Imagery, CONUS sector: clean IR (band 13),"
                        + " water vapor (bands 8/9), visible (band 2, daytime, cropped to the AOI) and the mesoscale"
                        + " sectors, at the profile's default cadence.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://registry.opendata.aws/noaa-goes/")
                .attribution("NOAA GOES-R Series, " + label)
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-goes/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                // A catch-up run is about 51 requests; a normal day about 2,725 MiB (2026-10-22).
                .defaultBudget(Budget.of(60, 4_000, 4_000))
                .fetchTimeout(Duration.ofMinutes(5))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .param(ParamSpec.optional(ProductEnablement.PRODUCTS_PARAM, ParamType.STRING_LIST,
                        "Which ABI products to ingest (default: small " + lightDefault + ", large " + largeDefault
                                + ")", null));
        for (Channel c : channels) {
            ProductSpec.Builder p = ProductSpec.builder(c.productId())
                    .title(c.title())
                    .category("satellite")
                    .units(c.units())
                    .dtype("vis".equals(c.code()) ? RasterDtype.U8 : RasterDtype.U16)
                    .scaleOffset(c.scale(), c.offset())
                    .nodata(c.nodata())
                    .temporal(c.temporal())
                    .spatial(c.spatial())
                    .retention(c.ladder())
                    .attribution(new ProductSpec.Attribution(
                            "NOAA GOES ABI L2 Cloud and Moisture Imagery, " + c.title() + ", via NOAA Open Data"
                                    + " Dissemination", null, "https://registry.opendata.aws/noaa-goes/", "US-PD", null,
                            false))
                    .coverageMeaning(c.meaning())
                    .legendSummary(c.legend());
            boolean first = true;
            for (String style : c.styles()) {
                if (first) {
                    p.defaultStyle(style);
                    first = false;
                } else {
                    p.style(style);
                }
            }
            builder.product(p.build());
        }
        return builder.build();
    }
}
