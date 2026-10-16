package ai.foresignal.source.adapters.nexrad;

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
 * <p><strong>Decoding gap (documented, per the prompt).</strong> The prompt allows N0B-only decoding
 * when NetCDF-Java cannot decode products 153/154. Verified 2026-10-15: this build's NetCDF-Java
 * (5.11.0, {@code cdm-core}) ships no NIDS IOSP (no {@code ucar.nc2.iosp.nids}), and
 * {@code NetcdfFiles.open} refuses a NIDS file, so no decoder is available. The source still lists the
 * sites, selects the nearest, downloads and probes (parsing the NIDS product description, which is
 * verified), but commits no frame and degrades with the reason
 * {@value #DECODING_GAP}, so no wrong radar data is ever stored.
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

    /** The documented decoding gap reason. */
    public static final String DECODING_GAP = "NIDS decoding is unavailable in this build (no NetCDF-Java NIDS"
            + " IOSP); nexrad-l3 is listed but ingests no frames";

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

    /** Creates the adapter. */
    @Autowired
    public NexradL3Adapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<AreaOfInterest> aoi) {
        this.baseUrl = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.aoi = aoi;
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
        // Decoding is unavailable in this build; record the gap so health shows it, and ingest nothing.
        ctx.state().degrade(DECODING_GAP);
        if (ctx.rasters().probing()) {
            return probe(ctx);
        }
        return FetchResult.of(List.of());
    }

    /** A probe of the newest N0B file, parsing the NIDS product description. */
    FetchResult probe(FetchContext ctx) throws AdapterException {
        for (Wsr88dSites.Site site : sites(ctx)) {
            S3Listing listing = ctx.http().listS3(BUCKET, site.id() + "_N0B_", null);
            Frame newest = null;
            for (S3Object object : listing.objects()) {
                Optional<Frame> frame = frame(object);
                if (frame.isPresent() && (newest == null || frame.get().time().isAfter(newest.time()))) {
                    newest = frame.get();
                }
            }
            if (newest == null) {
                continue;
            }
            Path file = ctx.http().download(objectUri(newest.key()), Path.of(ID + "-probe.l3"), MAX_BYTES).file();
            try {
                byte[] bytes = java.nio.file.Files.readAllBytes(file);
                NidsDecoder.Description description = NidsDecoder.describe(bytes);
                ctx.log().info("nexrad-l3 {} header {}", newest.key(),
                        description == null ? "(unparsed)" : ("product " + description.productCode()));
            } catch (java.io.IOException e) {
                ctx.log().warn("nexrad-l3 probe: {}", e.getMessage());
            } finally {
                ai.foresignal.raster.decode.GzipInputs.deleteQuietly(file);
            }
            return FetchResult.of(List.of());
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
                        + " archive for the 1–3 WSR-88D sites nearest the home. Opt-in on both profiles. Decoding is a"
                        + " documented gap in this build (no NetCDF-Java NIDS reader).")
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
                .product(ProductSpec.builder(N0B)
                        .title("NEXRAD Level III base reflectivity (N0B)")
                        .category("radar").units("dBZ").dtype(RasterDtype.U8).scaleOffset(0.5, -32.0).nodata(0.0)
                        .defaultStyle("nws-reflectivity@3")
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
