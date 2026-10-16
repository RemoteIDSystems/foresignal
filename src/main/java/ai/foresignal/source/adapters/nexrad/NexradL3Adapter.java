package ai.foresignal.source.adapters.nexrad;

import ai.foresignal.raster.decode.SafeNetcdf;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
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
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NEXRAD Level III for the 1–3 nearest WSR-88D sites (prompt 106; Weather-Imagery-Design §5.3):
 * super-resolution base reflectivity (N0B) and velocity (N0G) from {@code unidata-nexrad-level3}.
 *
 * <p>Per site and product the adapter lists {@code {SITE}_{PRODUCT}_{yyyy}_{MM}_{dd}_} (today's prefix,
 * and yesterday's within the first {@value #LOOKBACK_MINUTES} minutes after UTC midnight) with
 * {@code start-after} the last committed key (adapter state {@code lastKey:{SITE}_{PRODUCT}}), skips
 * scans older than {@value #LOOKBACK_MINUTES} minutes, and commits at most {@value #MAX_FRAMES_PER_RUN}
 * new scans per site and product, oldest first. Each scan is downloaded, decoded with NetCDF-Java's
 * NIDS IOSP ({@code edu.ucar:cdm-radial}, {@link NidsRadialReader}; products 153 and 154 verified
 * 2026-10-16), resampled to a 0.005° latlon grid within 230 km of the radar ({@link PolarResampler}) and
 * quantized to u8 (N0B: 0.5 dBZ from −32; N0G: 0.5 m/s from −64; code 0 nodata).
 *
 * <p>The source is opt-in like every source (sources start disabled); once enabled, both products are
 * ingested unless the {@code products} param lists a subset. With {@code siteCount} above 1 the sites'
 * scans share the two products, each frame on its own site's grid.
 *
 * <p><strong>Verified 2026-10-15</strong>: the bucket {@code unidata-nexrad-level3} is live (keys
 * {@code ABC_N0B_2026_01_01_00_01_47}, 3-letter site ids); the key grammar is
 * {@code {SITE}_{PRODUCT}_{YYYY}_{MM}_{DD}_{HH}_{MM}_{SS}}.
 */
@Component
public class NexradL3Adapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nexrad-l3";

    /** The bucket. */
    public static final String BUCKET = "unidata-nexrad-level3";

    /** Super-resolution base reflectivity. */
    public static final String N0B = "nexrad-l3-n0b";

    /** Super-resolution velocity. */
    public static final String N0G = "nexrad-l3-n0g";

    /** The products. */
    public static final List<String> PRODUCTS = List.of(N0B, N0G);

    /** The resampled grid's cell size, degrees. */
    static final double RESOLUTION_DEG = 0.005;

    /** The range kept around the radar, km. */
    static final double MAX_RANGE_KM = 230;

    /** Scans older than this are skipped, minutes. */
    static final int LOOKBACK_MINUTES = 60;

    /** New scans committed per site and product per run. */
    static final int MAX_FRAMES_PER_RUN = 3;

    /** The adapter-state key prefix of the last committed key. */
    static final String STATE_PREFIX = "lastKey:";

    /** N0B code scale, dBZ. */
    static final double N0B_SCALE = 0.5;

    /** N0B code offset, dBZ. */
    static final double N0B_OFFSET = -32.0;

    /** N0G code scale, m/s. */
    static final double N0G_SCALE = 0.5;

    /** N0G code offset, m/s. */
    static final double N0G_OFFSET = -64.0;

    /** The {@code sites} parameter. */
    public static final String SITES_PARAM = "sites";

    /** The {@code siteCount} parameter. */
    public static final String SITE_COUNT_PARAM = "siteCount";

    /** Test-only base URL override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.nexrad.base-url";

    /** The default number of nearest sites. */
    static final int DEFAULT_SITE_COUNT = 1;

    private static final int MAX_BYTES = 8 * 1024 * 1024;
    private static final Pattern KEY = Pattern.compile(
            "^(\\w{3,4})_(N0B|N0G)_(\\d{4})_(\\d{2})_(\\d{2})_(\\d{2})_(\\d{2})_(\\d{2})$");
    private static final Duration MAX_GAP = Duration.ofMinutes(20);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final ObjectProvider<AreaOfInterest> aoi;
    private final SafeNetcdf safe;

    /** Creates the adapter without a NetCDF guard (descriptor-only tests; fetching cannot decode). */
    public NexradL3Adapter(String baseUrlOverride, ObjectProvider<AreaOfInterest> aoi) {
        this(baseUrlOverride, aoi, null);
    }

    /** Creates the adapter. */
    @Autowired
    public NexradL3Adapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<AreaOfInterest> aoi, SafeNetcdf safe) {
        this.baseUrl = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.aoi = aoi;
        this.safe = safe;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    private URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    /** The sites to ingest: the {@code sites} param, else the nearest to the AOI centroid. */
    List<Wsr88dSites.Site> sites(FetchContext ctx) {
        Wsr88dSites table = Wsr88dSites.bundled();
        List<String> explicit = ctx.params() == null ? List.of() : ctx.params().stringList(SITES_PARAM);
        if (explicit != null && !explicit.isEmpty()) {
            List<Wsr88dSites.Site> out = new ArrayList<>();
            for (String id : explicit) {
                Wsr88dSites.Site site = table.find(id);
                if (site != null) {
                    out.add(site);
                }
            }
            return out;
        }
        double lon = -97.0;
        double lat = 31.0;
        if (aoi != null && aoi.getIfAvailable() != null) {
            double[] env = aoi.getIfAvailable().envelope();
            lon = (env[0] + env[2]) / 2;
            lat = (env[1] + env[3]) / 2;
        }
        int count = ctx.params() == null ? DEFAULT_SITE_COUNT
                : Math.max(1, Math.min(3, ctx.params().integer(SITE_COUNT_PARAM, DEFAULT_SITE_COUNT)));
        return table.nearest(lon, lat, count);
    }

    /** A listed NIDS file. */
    record Frame(String key, String site, String product, Instant time, long size) {}

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        if (ctx.rasters().probing()) {
            return probe(ctx);
        }
        java.util.Set<String> enabled = ai.foresignal.raster.ProductEnablement.enabled(ctx.params(), "small",
                PRODUCTS, PRODUCTS, PRODUCTS);
        List<NormalizedRecord> out = new ArrayList<>();
        AdapterException failure = null;
        for (Wsr88dSites.Site site : sites(ctx)) {
            for (String product : PRODUCTS) {
                if (!enabled.contains(product) || ctx.rasters().disabled(product)) {
                    continue;
                }
                try {
                    out.addAll(fetchSiteProduct(ctx, site, product));
                } catch (AdapterException e) {
                    failure = failure == null ? e : failure;
                    ctx.log().warn("nexrad-l3 {} {}: {}", site.id(), product, e.getMessage());
                }
            }
        }
        if (failure != null && out.isEmpty()) {
            throw failure;
        }
        return FetchResult.of(out);
    }

    /** The NIDS product code in the key ({@code N0B}, {@code N0G}) of a product id. */
    static String code(String productId) {
        return N0G.equals(productId) ? "N0G" : "N0B";
    }

    /** Lists, downloads, decodes and commits the new scans of one site and product. */
    List<NormalizedRecord> fetchSiteProduct(FetchContext ctx, Wsr88dSites.Site site, String productId)
            throws AdapterException {
        String stateKey = STATE_PREFIX + site.id() + "_" + code(productId);
        String last = ctx.state().get(stateKey).orElse(null);
        List<Frame> frames = listNew(ctx, site.id(), code(productId), last, ctx.now());
        List<NormalizedRecord> out = new ArrayList<>();
        String cursor = last;
        int committed = 0;
        try {
            for (Frame frame : frames) {
                if (committed >= MAX_FRAMES_PER_RUN) {
                    break;
                }
                if (frame.size() > MAX_BYTES) {
                    ctx.log().warn("nexrad-l3: skipping {} ({} bytes over the cap)", frame.key(), frame.size());
                    cursor = frame.key();
                    continue;
                }
                // A failed download leaves the cursor before this scan, so the next run retries it.
                Path file = ctx.http().download(objectUri(frame.key()), Path.of(ID + "-" + frame.key() + "-"
                        + Long.toHexString(System.nanoTime()) + ".l3"), MAX_BYTES).file();
                cursor = frame.key();
                try {
                    NormalizedRecord record = commit(ctx, productId, frame, file);
                    if (record != null) {
                        out.add(record);
                    }
                } catch (ai.foresignal.source.spi.SilentFormatChangeException e) {
                    ctx.rasters().reportFormatChange(productId, e.getMessage());
                    ctx.log().warn("nexrad-l3 {}: {}", frame.key(), e.getMessage());
                } catch (java.io.IOException | RuntimeException e) {
                    ctx.log().warn("nexrad-l3 {}: could not decode: {}", frame.key(), e.getMessage());
                } finally {
                    ai.foresignal.raster.decode.GzipInputs.deleteQuietly(file);
                }
                committed++;
            }
        } finally {
            if (cursor != null && !cursor.equals(last)) {
                ctx.state().put(stateKey, cursor);
            }
        }
        return out;
    }

    /** Decodes, resamples, quantizes and commits one scan. */
    private NormalizedRecord commit(FetchContext ctx, String productId, Frame frame, Path file)
            throws java.io.IOException, AdapterException {
        if (safe == null) {
            throw new IllegalStateException("nexrad-l3 was built without a NetCDF guard");
        }
        PolarScan scan = NidsRadialReader.read(safe, file);
        PolarResampler.Resampled grid = PolarResampler.resample(scan, RESOLUTION_DEG, MAX_RANGE_KM);
        int[] codes = N0G.equals(productId) ? PolarResampler.codes(grid.values(), N0G_SCALE, N0G_OFFSET)
                : PolarResampler.codes(grid.values(), N0B_SCALE, N0B_OFFSET);
        return ctx.rasters().commit(ai.foresignal.source.spi.FrameInput.builder(productId)
                .validTime(scan.time() != null ? scan.time() : frame.time())
                .grid(grid.grid())
                .bands(ai.foresignal.source.spi.DecodedBands.codes(codes))
                .upstreamKey(frame.key())
                .variable(scan.variable())
                .build());
    }

    /**
     * The scans of {@code site}/{@code code} newer than {@code last} and at most
     * {@value #LOOKBACK_MINUTES} minutes old, oldest first: today's day prefix (and yesterday's shortly
     * after midnight), listed with {@code start-after} the last key when it is in that prefix.
     */
    List<Frame> listNew(FetchContext ctx, String site, String code, String last, Instant now) throws AdapterException {
        Instant earliest = now.minus(Duration.ofMinutes(LOOKBACK_MINUTES));
        Instant lastTime = last == null ? null : frame(new S3Object(last, 0, null, null)).map(Frame::time).orElse(null);
        java.util.LinkedHashSet<String> prefixes = new java.util.LinkedHashSet<>();
        prefixes.add(dayPrefix(site, code, earliest));
        prefixes.add(dayPrefix(site, code, now));
        List<Frame> frames = new ArrayList<>();
        for (String prefix : prefixes) {
            String startAfter = last != null && last.startsWith(prefix) ? last : null;
            S3Listing listing = ctx.http().listS3(BUCKET, prefix, startAfter);
            while (true) {
                for (S3Object object : listing.objects()) {
                    frame(object).filter(f -> !f.time().isBefore(earliest))
                            .filter(f -> lastTime == null || f.time().isAfter(lastTime))
                            .ifPresent(frames::add);
                }
                if (!listing.truncated() || listing.nextStartAfter() == null) {
                    break;
                }
                listing = ctx.http().listS3(BUCKET, prefix, listing.nextStartAfter());
            }
        }
        frames.sort(java.util.Comparator.comparing(Frame::time).thenComparing(Frame::key));
        return frames;
    }

    /** {@code FWS_N0B_2026_10_16_}: the listing prefix of {@code time}'s UTC day. */
    static String dayPrefix(String site, String code, Instant time) {
        LocalDateTime t = LocalDateTime.ofInstant(time, ZoneOffset.UTC);
        return String.format(java.util.Locale.ROOT, "%s_%s_%04d_%02d_%02d_", site, code, t.getYear(),
                t.getMonthValue(), t.getDayOfMonth());
    }

    /**
     * A probe: the newest N0B scan of the first site that has one in the last
     * {@value #LOOKBACK_MINUTES} minutes, decoded and handed to the sink as a preview (a probing sink
     * writes nothing; adapter state is untouched).
     */
    FetchResult probe(FetchContext ctx) throws AdapterException {
        for (Wsr88dSites.Site site : sites(ctx)) {
            List<Frame> frames = listNew(ctx, site.id(), "N0B", null, ctx.now());
            if (frames.isEmpty()) {
                continue;
            }
            Frame newest = frames.get(frames.size() - 1);
            Path file = ctx.http().download(objectUri(newest.key()), Path.of(ID + "-probe-"
                    + Long.toHexString(System.nanoTime()) + ".l3"), MAX_BYTES).file();
            try {
                NormalizedRecord preview = commit(ctx, N0B, newest, file);
                return FetchResult.of(preview == null ? List.of() : List.of(preview));
            } catch (java.io.IOException e) {
                throw new AdapterException("nexrad-l3 probe " + newest.key() + ": " + e.getMessage(), e);
            } finally {
                ai.foresignal.raster.decode.GzipInputs.deleteQuietly(file);
            }
        }
        return FetchResult.of(List.of());
    }

    /** The scan of a listed object. */
    static Optional<Frame> frame(S3Object object) {
        Matcher m = KEY.matcher(object.key());
        if (!m.matches()) {
            return Optional.empty();
        }
        try {
            Instant time = LocalDateTime.of(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)),
                    Integer.parseInt(m.group(5)), Integer.parseInt(m.group(6)), Integer.parseInt(m.group(7)),
                    Integer.parseInt(m.group(8))).toInstant(ZoneOffset.UTC);
            return Optional.of(new Frame(object.key(), m.group(1), m.group(2), time, object.size()));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NEXRAD Level III (nearest sites)")
                .description("Super-resolution base reflectivity (N0B) and velocity (N0G) from the NEXRAD Level III"
                        + " archive for the 1–3 WSR-88D sites nearest the home, decoded with NetCDF-Java's NIDS reader"
                        + " and resampled to 0.005° within 230 km. Opt-in on both profiles.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://registry.opendata.aws/noaa-nexrad/")
                .attribution("NOAA/NWS NEXRAD Level III, via Unidata")
                .licence("US-PD")
                .termsUrl("https://www.noaa.gov/information-technology/open-data-dissemination")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(2), Duration.ofMinutes(2))
                .defaultBudget(Budget.of(10, 2_000, 2_000))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(MAX_BYTES)
                .param(ParamSpec.optional(SITES_PARAM, ParamType.STRING_LIST,
                        "The WSR-88D site ids to ingest (default the nearest to the home)", List.of()))
                .param(ParamSpec.optional(SITE_COUNT_PARAM, ParamType.INTEGER,
                        "How many nearest sites when sites is empty (1 to 3)", DEFAULT_SITE_COUNT))
                .param(ParamSpec.optional(ai.foresignal.raster.ProductEnablement.PRODUCTS_PARAM, ParamType.STRING_LIST,
                        "Which products to ingest once the source is enabled (default both)", null))
                .product(ProductSpec.builder(N0B)
                        .title("NEXRAD Level III base reflectivity (N0B)")
                        .category("radar").units("dBZ").dtype(RasterDtype.U8).scaleOffset(0.5, -32.0).nodata(0.0)
                        .defaultStyle("nws-reflectivity@1")
                        .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(5), Duration.ofMinutes(20)))
                        .spatial(new ProductSpec.Spatial("station-disc", 8, 0, 7, null))
                        .retention(ProductSpec.RetentionLadder.maxAge(Duration.ofHours(6)))
                        .attribution(new ProductSpec.Attribution("NOAA/NWS NEXRAD Level III (N0B), via Unidata", null,
                                "https://registry.opendata.aws/noaa-nexrad/", "US-PD", null, false))
                        .coverageMeaning("Within 230 km of the site; transparent outside")
                        .legendSummary("Super-resolution base reflectivity, dBZ").build())
                .product(ProductSpec.builder(N0G)
                        .title("NEXRAD Level III velocity (N0G)")
                        .category("radar").units("m/s").dtype(RasterDtype.U8).scaleOffset(0.5, -64.0).nodata(0.0)
                        .defaultStyle("velocity@1")
                        .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(5), Duration.ofMinutes(20)))
                        .spatial(new ProductSpec.Spatial("station-disc", 8, 0, 7, null))
                        .retention(ProductSpec.RetentionLadder.maxAge(Duration.ofHours(6)))
                        .attribution(new ProductSpec.Attribution("NOAA/NWS NEXRAD Level III (N0G), via Unidata", null,
                                "https://registry.opendata.aws/noaa-nexrad/", "US-PD", null, false))
                        .coverageMeaning("Within 230 km of the site; transparent outside")
                        .legendSummary("Super-resolution radial velocity, m/s").build());
        return b.build();
    }
}
