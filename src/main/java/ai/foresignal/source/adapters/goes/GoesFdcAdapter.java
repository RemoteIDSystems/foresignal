package ai.foresignal.source.adapters.goes;

import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.raster.decode.GzipInputs;
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.PollingAdapter;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
 * GOES-19 ABI fire detections ({@code goes-fdc}, prompt 048, Weather-Imagery-Design §5.1).
 *
 * <p><b>Files:</b> bucket {@value #BUCKET}, prefix {@code ABI-L2-FDCC/{YYYY}/{DOY}/{HH}/}, one CONUS file
 * per 5-minute scan (about 0.52 MB, verified 2026-10-12). Each run lists the current hour with
 * {@code start-after} the last processed key (and the previous hour until a key of the current hour was
 * processed) and processes <strong>only the newest</strong> scan; a skipped scan is simply a scan that
 * was not processed.
 *
 * <p><b>{@code fire-detection}</b> per fire pixel (mask 10–13 or 30–33, {@link FdcParser}):
 * {@code externalId} {@code g19:{col}:{row}}, a Point at the pixel center, {@code observedAt} the scan's
 * {@code s} time, {@code eventTime} the pixel's first sighting (kept in the adapter state, stable across
 * scans). Param {@code scope}: {@code aoi} (default; the places-AOI envelope) or {@code conus}.
 *
 * <p><b>Lifecycle:</b> a pixel is tombstoned after {@value #MAX_MISSES} consecutive <em>processed</em>
 * scans without it (miss counters in the adapter state). A scan that was not processed (the feed is
 * down, the download failed, the file could not be decoded) counts for nothing, so an outage freezes
 * detections instead of clearing them. {@code fire-cluster} records follow the same rule
 * ({@link FireClusterer}).
 *
 * <p><b>Health:</b> degraded when the newest processed scan is older than {@value #MAX_GAP_MINUTES}
 * minutes ({@code InputAgeHealthRule}), or while a file is unreadable or a variable is missing.
 *
 * <p><b>Budget:</b> 288 listings and 288 downloads a day plus hour rollover listings leave no headroom
 * under the design's 600/day, so the default is 4/min and 800/day.
 */
@Component
public class GoesFdcAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "goes-fdc";

    /** The detection kind (shared with {@code hms-fire} and 065's FIRMS sources). */
    public static final String KIND = FdcParser.KIND;

    /** The cluster kind. */
    public static final String CLUSTER_KIND = FireClusterer.KIND;

    /** The GOES-19 bucket. */
    public static final String BUCKET = GoesAbiAdapter.BUCKET;

    /** Test-only: a path-style base URL for object downloads, shared with {@code goes-east-abi}. */
    public static final String BASE_URL_PROPERTY = GoesAbiAdapter.BASE_URL_PROPERTY;

    /** The {@code scope} parameter. */
    public static final String SCOPE_PARAM = "scope";

    /** The scope values. */
    public static final List<String> SCOPES = List.of("aoi", "conus");

    /** Processed scans without a pixel before its tombstone. */
    public static final int MAX_MISSES = 3;

    /** The largest tolerated age of the newest processed scan, minutes. */
    public static final int MAX_GAP_MINUTES = 15;

    /** The response cap. */
    static final int MAX_RESPONSE_BYTES = 4 * 1024 * 1024;

    /** The AOI used when no AOI service is wired (unit tests): the Texas box. */
    static final double[] DEFAULT_ENVELOPE = {-106.65, 25.84, -93.51, 36.50};

    /** Retention: Weather-Imagery-Design §2.4 keeps both kinds 7 days. */
    static final Duration RETENTION = Duration.ofDays(7);

    /** Adapter-state keys. */
    static final String LAST_KEY = "lastKey";
    static final String LAST_SCAN = "lastScan";
    static final String PIXELS = "pixels";
    static final String CLUSTERS = "clusters";

    /** A GOES-19 FDCC key; groups 1 and 2 are the {@code s} and {@code e} times. */
    static final Pattern KEY = Pattern.compile(
            "^ABI-L2-FDCC/\\d{4}/\\d{3}/\\d{2}/OR_ABI-L2-FDCC-M\\d_G19_s(\\d{14})_e(\\d{14})_c\\d{14}\\.nc$");

    /**
     * One listed scan.
     *
     * @param key the S3 key
     * @param start the {@code s} time
     * @param end the {@code e} time
     * @param size the listed size
     */
    record Scan(String key, Instant start, Instant end, long size) {}

    /**
     * A pixel's bookkeeping.
     *
     * @param firstSeenMs its first sighting
     * @param misses consecutive processed scans without it
     */
    record PixelState(long firstSeenMs, int misses) {}

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final NetcdfDecoder decoder;
    private final AreaOfInterest aoi;
    private final String baseUrl;

    /**
     * Creates the adapter for Spring.
     *
     * @param decoder the NetCDF decoder
     * @param aoi the places AOI
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real bucket
     */
    @Autowired
    public GoesFdcAdapter(NetcdfDecoder decoder, ObjectProvider<AreaOfInterest> aoi,
            @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(decoder, aoi == null ? null : aoi.getIfAvailable(), baseUrlOverride);
    }

    /**
     * Creates the adapter.
     *
     * @param decoder the NetCDF decoder
     * @param aoi the places AOI, or null for the default Texas box
     * @param baseUrlOverride a path-style base URL, or blank for the real bucket
     */
    public GoesFdcAdapter(NetcdfDecoder decoder, AreaOfInterest aoi, String baseUrlOverride) {
        this.decoder = decoder;
        this.aoi = aoi;
        String chosen = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        if (aoi != null) {
            aoi.registerConsumer(ID);
        }
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The URL of {@code key}. */
    public URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    /** The listing prefix of the hour holding {@code time}. */
    static String hourPrefix(Instant time) {
        LocalDateTime t = LocalDateTime.ofInstant(time, ZoneOffset.UTC);
        return String.format(Locale.ROOT, "ABI-L2-FDCC/%04d/%03d/%02d/", t.getYear(), t.getDayOfYear(), t.getHour());
    }

    /** The envelope for {@code scope}: null (every pixel) for {@code conus}. */
    double[] envelope(String scope) {
        if ("conus".equals(scope)) {
            return null;
        }
        return aoi == null ? DEFAULT_ENVELOPE.clone() : aoi.envelope();
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String scope = ctx.params().optionalString(SCOPE_PARAM).orElse("aoi");
        String lastKey = ctx.state().get(LAST_KEY).orElse(null);
        Instant now = ctx.now();
        Scan newest = newestScan(ctx, lastKey, now);
        if (ctx.rasters().probing()) {
            return probe(ctx, newest, scope);
        }
        if (newest == null) {
            return FetchResult.unchanged();
        }
        Instant lastScan = ctx.state().get(LAST_SCAN).map(Instant::parse).orElse(null);
        if (lastScan != null && !newest.start().isAfter(lastScan)) {
            ctx.log().debug("FDC {} is not newer than the last processed scan ({}); ignored", newest.key(), lastScan);
            ctx.state().put(LAST_KEY, newest.key());
            return FetchResult.unchanged();
        }
        if (newest.size() > MAX_RESPONSE_BYTES) {
            ctx.log().warn("FDC: skipping {} ({} bytes is over the {}-byte cap)", newest.key(), newest.size(),
                    MAX_RESPONSE_BYTES);
            ctx.state().put(LAST_KEY, newest.key());
            return FetchResult.unchanged();
        }
        Path file = download(ctx, newest);
        FdcParser.Result parsed;
        try {
            parsed = FdcParser.parse(decoder, file, ID, envelope(scope));
        } catch (SilentFormatChangeException e) {
            // Not processed: nothing advances but the cursor, so the miss counters stay as they are.
            ctx.state().degrade("upstream format changed: " + e.getMessage());
            ctx.state().put(LAST_KEY, newest.key());
            ctx.log().warn("FDC {}: {}; the scan is not processed", newest.key(), e.getMessage());
            return FetchResult.unchanged();
        } catch (RasterFormatException e) {
            ctx.state().degrade("FDC scan " + newest.key() + " could not be decoded: " + e.getMessage());
            ctx.state().put(LAST_KEY, newest.key());
            ctx.log().warn("FDC {}: cannot be read ({}); the scan is not processed", newest.key(), e.getMessage());
            return FetchResult.unchanged();
        } finally {
            GzipInputs.deleteQuietly(file);
        }

        // A processed scan: only now do misses count.
        List<NormalizedRecord> out = process(ctx, parsed, newest.start());
        ctx.state().put(LAST_KEY, newest.key());
        ctx.state().put(LAST_SCAN, newest.start().toString());
        ctx.state().recordInput(newest.end(), Duration.ofMinutes(MAX_GAP_MINUTES));
        ctx.state().clearDegraded();
        ctx.log().debug("FDC {}: {} detection(s) in scope of {} fire pixel(s)", newest.key(), parsed.detections().size(),
                parsed.fireCandidates());
        return FetchResult.complete(out);
    }

    /**
     * Turns one processed scan into records: detections with their first sightings, tombstones of
     * pixels missing for {@value #MAX_MISSES} processed scans, and the cluster records.
     */
    List<NormalizedRecord> process(FetchContext ctx, FdcParser.Result parsed, Instant scanStart) {
        Map<String, PixelState> pixels = readPixels(ctx.state().get(PIXELS).orElse(null));
        Map<String, PixelState> next = new LinkedHashMap<>();
        Map<String, Instant> firstSeen = new LinkedHashMap<>();
        List<NormalizedRecord> out = new ArrayList<>();
        for (FdcParser.Detection d : parsed.detections()) {
            PixelState previous = pixels.get(d.pixel());
            long first = previous == null ? scanStart.toEpochMilli() : previous.firstSeenMs();
            next.put(d.pixel(), new PixelState(first, 0));
            firstSeen.put(d.pixel(), Instant.ofEpochMilli(first));
            out.add(FdcParser.record(d, scanStart, Instant.ofEpochMilli(first)));
        }
        for (Map.Entry<String, PixelState> entry : pixels.entrySet()) {
            if (next.containsKey(entry.getKey())) {
                continue;
            }
            int misses = entry.getValue().misses() + 1;
            if (misses >= MAX_MISSES) {
                out.add(NormalizedRecord.tombstone("g19:" + entry.getKey(), KIND, scanStart));
            } else {
                next.put(entry.getKey(), new PixelState(entry.getValue().firstSeenMs(), misses));
            }
        }
        FireClusterer clusterer = new FireClusterer();
        clusterer.restore(ctx.state().get(CLUSTERS).orElse(null));
        List<String> emitted = new ArrayList<>();
        for (FireClusterer.Change change : clusterer.step(parsed.detections(), firstSeen, scanStart)) {
            NormalizedRecord record = change.record();
            if (record.tombstone() && !change.wasEmitted()) {
                continue;
            }
            out.add(record);
            if (!record.tombstone()) {
                emitted.add(record.externalId());
            }
        }
        clusterer.markEmitted(emitted);
        ctx.state().put(PIXELS, writePixels(next));
        ctx.state().put(CLUSTERS, clusterer.serialize());
        return out;
    }

    /** Pixel bookkeeping from adapter-state text ({@code col:row|firstSeenMs|misses} per line). */
    static Map<String, PixelState> readPixels(String text) {
        Map<String, PixelState> out = new LinkedHashMap<>();
        if (text == null || text.isBlank()) {
            return out;
        }
        for (String line : text.split("\n")) {
            String[] f = line.split("\\|");
            if (f.length != 3 || f[0].isBlank()) {
                continue;
            }
            try {
                out.put(f[0], new PixelState(Long.parseLong(f[1]), Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                // Dropped; a pixel that still burns is picked up again.
            }
        }
        return out;
    }

    /** Pixel bookkeeping as adapter-state text. */
    static String writePixels(Map<String, PixelState> pixels) {
        StringBuilder out = new StringBuilder();
        pixels.forEach((pixel, s) -> {
            if (!out.isEmpty()) {
                out.append('\n');
            }
            out.append(pixel).append('|').append(s.firstSeenMs()).append('|').append(s.misses());
        });
        return out.toString();
    }

    /** Probes: the newest scan, parsed; writes nothing. */
    FetchResult probe(FetchContext ctx, Scan newest, String scope) throws AdapterException {
        if (newest == null) {
            return FetchResult.of(List.of());
        }
        Path file = download(ctx, newest);
        try {
            FdcParser.Result parsed = FdcParser.parse(decoder, file, ID, envelope(scope));
            List<NormalizedRecord> preview = new ArrayList<>();
            for (FdcParser.Detection d : parsed.detections()) {
                preview.add(FdcParser.record(d, newest.start(), newest.start()));
            }
            return FetchResult.of(preview);
        } finally {
            GzipInputs.deleteQuietly(file);
        }
    }

    /**
     * The newest scan after {@code lastKey}: the current hour, and the previous hour too until a key of
     * the current hour has been processed.
     */
    Scan newestScan(FetchContext ctx, String lastKey, Instant now) throws AdapterException {
        String current = hourPrefix(now);
        Scan newest = newestUnder(ctx, current, lastKey);
        if (newest == null && (lastKey == null || lastKey.compareTo(current) < 0)) {
            newest = newestUnder(ctx, hourPrefix(now.minus(1, ChronoUnit.HOURS)), lastKey);
        }
        return newest;
    }

    private static Scan newestUnder(FetchContext ctx, String prefix, String lastKey) throws AdapterException {
        String startAfter = lastKey != null && lastKey.startsWith(prefix) ? lastKey : null;
        Instant lastStart = lastKey == null ? null : scan(lastKey, 0).map(Scan::start).orElse(null);
        Scan newest = null;
        S3Listing listing = ctx.http().listS3(BUCKET, prefix, startAfter);
        while (true) {
            for (S3Object object : listing.objects()) {
                Optional<Scan> s = scan(object.key(), object.size());
                if (s.isPresent() && (lastStart == null || s.get().start().isAfter(lastStart))
                        && (newest == null || s.get().start().isAfter(newest.start()))) {
                    newest = s.get();
                }
            }
            if (!listing.truncated() || listing.nextStartAfter() == null) {
                break;
            }
            listing = ctx.http().listS3(BUCKET, prefix, listing.nextStartAfter());
        }
        return newest;
    }

    /** The scan of a key, when it is a GOES-19 FDCC file. */
    static Optional<Scan> scan(String key, long size) {
        Matcher m = KEY.matcher(key);
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(new Scan(key, GoesAbiAdapter.parseStart(m.group(1)), GoesAbiAdapter.parseStart(m.group(2)),
                size));
    }

    private Path download(FetchContext ctx, Scan scan) throws AdapterException {
        String name = ID + "-" + scan.start().toEpochMilli() + "-" + Long.toHexString(System.nanoTime()) + ".nc";
        DownloadResult result = ctx.http().download(objectUri(scan.key()), Path.of(name), MAX_RESPONSE_BYTES, scan.size());
        if (scan.size() > 0 && result.bytes() != scan.size()) {
            GzipInputs.deleteQuietly(result.file());
            throw new AdapterException("FDC " + scan.key() + ": downloaded " + result.bytes()
                    + " bytes, the listing said " + scan.size());
        }
        return result.file();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("GOES-East fire detections")
                .description("GOES-19 ABI Fire/Hot Spot Characterization (FDC), CONUS sector, every 5-minute scan:"
                        + " one record per fire pixel and one per cluster of nearby pixels, inside the places area of"
                        + " interest (or CONUS). Detections are tombstoned after 3 processed scans without them; a"
                        + " stalled feed freezes them.")
                .kinds(KIND, CLUSTER_KIND)
                .homepage("https://registry.opendata.aws/noaa-goes/")
                .attribution("NOAA GOES-R Series, GOES-19 (GOES-East) ABI Fire/Hot Spot Characterization")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-goes/")
                .mode(Mode.POLLING)
                .param(ParamSpec.enumerated(SCOPE_PARAM, "aoi (the places-AOI envelope) or conus (the whole sector)", "aoi",
                        SCOPES))
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(Budget.of(4, 800, 300))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .kindRetention(CLUSTER_KIND, RETENTION)
                .titleField("label")
                .zeroResultsNormal(true)
                .tileProperties("frpMw", "confidence", "pixels", "frpTotalMw", "label")
                .kindDisplay(KIND, new KindDisplay("{label}", "fire", "#E03131", null))
                .kindDisplay(CLUSTER_KIND, new KindDisplay("{label}", "fire", "#C92A2A", null));
        Set<String> detection = Set.of(KIND);
        Set<String> cluster = Set.of(CLUSTER_KIND);
        builder.fieldSpec("maskCode", new FieldSpec(FieldType.INTEGER, "1", "Fire mask",
                "ABI FDC fire category: 10-13 or 30-33 (temporally filtered)", null, detection));
        builder.fieldSpec("confidence", new FieldSpec(FieldType.STRING, null, "Confidence",
                "processed, saturated, cloudy or high", null, detection));
        builder.fieldSpec("temporallyFiltered", new FieldSpec(FieldType.BOOLEAN, null, "Temporally filtered",
                "True for masks 30-33", null, detection));
        builder.fieldSpec("frpMw", new FieldSpec(FieldType.NUMBER, "MW", "Fire radiative power",
                "Fire radiative power, 2 significant figures", null, detection));
        builder.fieldSpec("tempK", new FieldSpec(FieldType.INTEGER, "K", "Fire temperature", "Rounded to 10 K", null,
                detection));
        builder.fieldSpec("areaKm2", new FieldSpec(FieldType.NUMBER, "km2", "Fire area",
                "Sub-pixel fire area, 2 significant figures", null, detection));
        builder.fieldSpec("satellite", new FieldSpec(FieldType.STRING, null, "Satellite", "G19", null, detection));
        builder.fieldSpec("method", new FieldSpec(FieldType.STRING, null, "Method", "FDC", null, detection));
        builder.fieldSpec("pixels", new FieldSpec(FieldType.INTEGER, "1", "Pixels", "Fire pixels in the cluster", null,
                cluster));
        builder.fieldSpec("frpTotalMw", new FieldSpec(FieldType.NUMBER, "MW", "Total fire radiative power",
                "Sum over the cluster, 2 significant figures", null, cluster));
        builder.fieldSpec("frpMaxMw", new FieldSpec(FieldType.NUMBER, "MW", "Largest fire radiative power",
                "Largest pixel value, 2 significant figures", null, cluster));
        builder.fieldSpec("firstSeenAt", new FieldSpec(FieldType.TIMESTAMP, null, "First seen",
                "When the cluster was first detected (ISO-8601)", null, cluster));
        builder.fieldSpec("label", new FieldSpec(FieldType.STRING, null, "Label",
                "Fire detection (GOES-19, high), 45 MW; Fire cluster, 4 pixels, 120 MW", null, Set.of()));
        return builder.build();
    }
}
