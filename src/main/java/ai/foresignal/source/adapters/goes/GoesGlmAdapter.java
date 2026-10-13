package ai.foresignal.source.adapters.goes;

import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.raster.decode.GzipInputs;
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * GOES-19 GLM lightning ({@code goes-glm}, prompt 048, Weather-Imagery-Design §5.1 and §7.2).
 *
 * <p><b>Files:</b> bucket {@value #BUCKET}, prefix {@code GLM-L2-LCFA/{YYYY}/{DOY}/{HH}/}, one 20-second
 * granule per key ({@code OR_GLM-L2-LCFA_G19_s…_e…_c….nc}, 0.25–0.43 MB, verified 2026-10-12). Each run
 * lists from the last processed key's hour with {@code start-after}, and processes every new granule in
 * key order, at most {@value #MAX_FILES_PER_RUN} per run (so a 30-second poll stays inside 10 requests a
 * minute while still catching up), and nothing that started more than {@value #CATCH_UP_MINUTES}
 * minutes ago: after an outage the adapter catches up at most an hour.
 *
 * <p><b>Each granule:</b> download into {@code rasters/tmp} (charging its listed size), read the flashes
 * with {@link GlmParser} (quality flag 0 only), append them to the flash store ({@code ctx.rasters()
 * .flashes()}: the daily archive of the full field of view, and the places-AOI ring), then cluster the
 * ring's last 5 minutes ({@link LightningClusterer}). A missing variable is a silent format change: the
 * source shows {@code degraded} with the reason and the file is skipped, unprocessed.
 *
 * <p><b>Stale is not clear:</b> clusters advance only with processed files. While no file is processed
 * (the feed stopped, downloads fail, files are unreadable) nothing is updated or tombstoned, the source
 * goes {@code degraded} once its newest processed file is older than {@value #MAX_GAP_MINUTES} minutes
 * ({@code InputAgeHealthRule}), and when files resume, the gap itself does not count toward a cluster's
 * 10 quiet minutes. Clusters in the adapter state survive a restart and are tombstoned if not re-matched
 * within 10 minutes of resumed processing.
 *
 * <p>Place metrics (nearest flash, counts within a radius) are 052's {@code wx-places}, read from the
 * flash store; nothing here writes place weather.
 */
@Component
public class GoesGlmAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "goes-glm";

    /** The record kind. */
    public static final String KIND = LightningClusterer.KIND;

    /** The GOES-19 bucket. */
    public static final String BUCKET = GoesAbiAdapter.BUCKET;

    /** The platform, naming the archive directory. */
    public static final String SATELLITE = "G19";

    /** Test-only: a path-style base URL for object downloads, shared with {@code goes-east-abi}. */
    public static final String BASE_URL_PROPERTY = GoesAbiAdapter.BASE_URL_PROPERTY;

    /** Granules processed per run at most. */
    public static final int MAX_FILES_PER_RUN = 4;

    /** How far back a run reaches, minutes. */
    public static final int CATCH_UP_MINUTES = 60;

    /** The largest tolerated gap between processed files, minutes. */
    public static final int MAX_GAP_MINUTES = 2;

    /** {@link #MAX_GAP_MINUTES} as a duration. */
    public static final Duration MAX_GAP = Duration.ofMinutes(MAX_GAP_MINUTES);

    /** The response cap, and the largest granule downloaded. */
    static final int MAX_RESPONSE_BYTES = 4 * 1024 * 1024;

    /** Adapter-state keys. */
    static final String LAST_KEY = "lastKey";
    static final String LAST_END = "lastEnd";
    static final String LAST_PROCESSED_AT = "lastProcessedAt";
    static final String CLUSTERS = "clusters";

    /** Retention: Weather-Imagery-Design §2.4 keeps {@code lightning-cluster} for 3 days. */
    static final Duration RETENTION = Duration.ofDays(3);

    /** A GOES-19 LCFA key; groups 1 and 2 are the {@code s} and {@code e} times. */
    static final Pattern KEY = Pattern.compile(
            "^GLM-L2-LCFA/\\d{4}/\\d{3}/\\d{2}/OR_GLM-L2-LCFA_G19_s(\\d{14})_e(\\d{14})_c\\d{14}\\.nc$");

    /**
     * One listed granule.
     *
     * @param key the S3 key
     * @param start the {@code s} time
     * @param end the {@code e} time
     * @param size the listed size
     */
    record Granule(String key, Instant start, Instant end, long size) {}

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final NetcdfDecoder decoder;
    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param decoder the NetCDF decoder
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real bucket
     */
    @Autowired
    public GoesGlmAdapter(NetcdfDecoder decoder, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.decoder = decoder;
        String chosen = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
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
        return String.format(Locale.ROOT, "GLM-L2-LCFA/%04d/%03d/%02d/", t.getYear(), t.getDayOfYear(), t.getHour());
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        if (ctx.rasters().probing()) {
            return probe(ctx);
        }
        FlashSink flashes = ctx.rasters().flashes();
        Instant now = ctx.now();
        String lastKey = ctx.state().get(LAST_KEY).orElse(null);
        Instant lastEnd = ctx.state().get(LAST_END).map(Instant::parse).orElse(null);
        LightningClusterer clusterer = new LightningClusterer();
        clusterer.restore(ctx.state().get(CLUSTERS).orElse(null));

        List<Granule> granules = listNew(ctx, lastKey, now);
        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        String cursor = lastKey;
        int processed = 0;
        int attempted = 0;
        AdapterException failure = null;
        for (Granule g : granules) {
            if (attempted >= MAX_FILES_PER_RUN) {
                break;
            }
            attempted++;
            if (g.size() > MAX_RESPONSE_BYTES) {
                ctx.log().warn("GLM: skipping {} ({} bytes is over the {}-byte cap)", g.key(), g.size(), MAX_RESPONSE_BYTES);
                cursor = g.key();
                continue;
            }
            GlmParser.Result parsed;
            Path file;
            try {
                file = download(ctx, g);
            } catch (AdapterException e) {
                failure = e;
                break;
            }
            try {
                parsed = GlmParser.parse(decoder, file, ID);
            } catch (SilentFormatChangeException e) {
                ctx.state().degrade("upstream format changed: " + e.getMessage());
                ctx.log().warn("GLM {}: {}; the file is skipped", g.key(), e.getMessage());
                cursor = g.key();
                continue;
            } catch (RasterFormatException e) {
                ctx.log().warn("GLM {}: cannot be read ({}); the file is skipped", g.key(), e.getMessage());
                cursor = g.key();
                continue;
            } finally {
                GzipInputs.deleteQuietly(file);
            }
            ctx.state().clearDegraded();
            flashes.append(SATELLITE, parsed.flashes(), g.end());
            long deltaMs = deltaMs(lastEnd, g);
            FlashBatch window = flashes.between(g.end().minus(LightningClusterer.WINDOW), g.end());
            for (LightningClusterer.Change change : clusterer.step(window, g.end().toEpochMilli(), deltaMs)) {
                NormalizedRecord record = change.record();
                if (record.tombstone() && !change.wasEmitted()) {
                    out.remove(record.externalId());
                } else {
                    out.put(record.externalId(), record);
                }
            }
            if (lastEnd == null || g.end().isAfter(lastEnd)) {
                lastEnd = g.end();
            }
            cursor = g.key();
            processed++;
            ctx.log().debug("GLM {}: {} of {} flashes kept, {} clusters", g.key(), parsed.flashes().size(),
                    parsed.total(), clusterer.tracked().size());
        }

        clusterer.markEmitted(out.values().stream().filter(r -> !r.tombstone()).map(NormalizedRecord::externalId)
                .toList());
        if (cursor != null && !cursor.equals(lastKey)) {
            ctx.state().put(LAST_KEY, cursor);
        }
        ctx.state().put(CLUSTERS, clusterer.serialize());
        if (processed > 0) {
            ctx.state().put(LAST_END, lastEnd.toString());
            ctx.state().put(LAST_PROCESSED_AT, now.toString());
            ctx.state().recordInput(lastEnd, MAX_GAP);
        }
        if (failure != null) {
            if (processed == 0) {
                throw failure;
            }
            ctx.log().warn("GLM: processed {} file(s), then failed: {}", processed, failure.getMessage());
        }
        if (processed == 0 && out.isEmpty()) {
            // Nothing new was processed: clusters stay frozen as they are.
            return FetchResult.unchanged();
        }
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /**
     * The processed data time granule {@code g} adds: the time since the previous processed file's end
     * when the gap before {@code g} is at most {@link #MAX_GAP}, otherwise only {@code g}'s own coverage
     * (an outage is never counted toward a cluster's quiet time).
     */
    static long deltaMs(Instant lastEnd, Granule g) {
        long own = Math.max(0, g.end().toEpochMilli() - g.start().toEpochMilli());
        if (lastEnd == null || g.start().isAfter(lastEnd.plus(MAX_GAP))) {
            return own;
        }
        return Math.max(0, g.end().toEpochMilli() - lastEnd.toEpochMilli());
    }

    /** Probes: the newest granule, parsed; writes nothing. */
    FetchResult probe(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        Granule newest = null;
        for (Instant hour : List.of(now, now.minus(1, ChronoUnit.HOURS))) {
            S3Listing listing = ctx.http().listS3(BUCKET, hourPrefix(hour), null);
            for (S3Object object : listing.objects()) {
                Optional<Granule> g = granule(object);
                if (g.isPresent() && (newest == null || g.get().key().compareTo(newest.key()) > 0)) {
                    newest = g.get();
                }
            }
            if (newest != null) {
                break;
            }
        }
        if (newest == null) {
            return FetchResult.of(List.of());
        }
        Path file = download(ctx, newest);
        try {
            GlmParser.Result parsed = GlmParser.parse(decoder, file, ID);
            ctx.log().info("GLM probe {}: {} of {} flashes have quality flag 0", newest.key(), parsed.flashes().size(),
                    parsed.total());
        } finally {
            GzipInputs.deleteQuietly(file);
        }
        return FetchResult.of(List.of());
    }

    /**
     * The new granules after {@code lastKey} that started at most {@value #CATCH_UP_MINUTES} minutes
     * before {@code now}, in key order. Hours are listed from the cursor's hour (or the catch-up limit)
     * and listing stops once a run's worth of granules is found.
     */
    List<Granule> listNew(FetchContext ctx, String lastKey, Instant now) throws AdapterException {
        Instant earliest = now.minus(Duration.ofMinutes(CATCH_UP_MINUTES));
        Instant lastStart = lastKey == null ? null : startTime(lastKey).orElse(null);
        Instant from = lastStart == null || lastStart.isBefore(earliest) ? earliest : lastStart;
        Instant nowHour = now.truncatedTo(ChronoUnit.HOURS);
        List<Granule> out = new ArrayList<>();
        for (Instant hour = from.truncatedTo(ChronoUnit.HOURS); !hour.isAfter(nowHour); hour = hour.plus(1, ChronoUnit.HOURS)) {
            String prefix = hourPrefix(hour);
            String startAfter = lastKey != null && lastKey.startsWith(prefix) ? lastKey : null;
            S3Listing listing = ctx.http().listS3(BUCKET, prefix, startAfter);
            while (true) {
                for (S3Object object : listing.objects()) {
                    granule(object).filter(g -> lastKey == null || g.key().compareTo(lastKey) > 0)
                            .filter(g -> !g.start().isBefore(earliest)).ifPresent(out::add);
                }
                if (!listing.truncated() || listing.nextStartAfter() == null) {
                    break;
                }
                listing = ctx.http().listS3(BUCKET, prefix, listing.nextStartAfter());
            }
            if (out.size() >= MAX_FILES_PER_RUN) {
                break;
            }
        }
        out.sort(Comparator.comparing(Granule::key));
        return out;
    }

    /** The granule of a listed object, when it is a GOES-19 LCFA file. */
    static Optional<Granule> granule(S3Object object) {
        Matcher m = KEY.matcher(object.key());
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(new Granule(object.key(), GoesAbiAdapter.parseStart(m.group(1)),
                GoesAbiAdapter.parseStart(m.group(2)), object.size()));
    }

    /** The {@code s} time of an LCFA key. */
    static Optional<Instant> startTime(String key) {
        Matcher m = KEY.matcher(key);
        return m.matches() ? Optional.of(GoesAbiAdapter.parseStart(m.group(1))) : Optional.empty();
    }

    private Path download(FetchContext ctx, Granule g) throws AdapterException {
        String name = ID + "-" + g.start().toEpochMilli() + "-" + Long.toHexString(System.nanoTime()) + ".nc";
        DownloadResult result = ctx.http().download(objectUri(g.key()), Path.of(name), MAX_RESPONSE_BYTES, g.size());
        if (g.size() > 0 && result.bytes() != g.size()) {
            GzipInputs.deleteQuietly(result.file());
            throw new AdapterException("GLM " + g.key() + ": downloaded " + result.bytes() + " bytes, the listing said "
                    + g.size());
        }
        return result.file();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("GOES-East GLM lightning")
                .description("GOES-19 Geostationary Lightning Mapper L2 flashes, every 20-second granule. Flashes go to"
                        + " the flash store (a 14-day archive and a 60-minute ring over the places area of interest);"
                        + " records are lightning clusters (DBSCAN, 8 km, 3 flashes, 5 minutes). GLM's footprint over"
                        + " Texas is about 8-10 km and its detection efficiency is below 100%: automated, not a"
                        + " warning.")
                .kind(KIND)
                .homepage("https://registry.opendata.aws/noaa-goes/")
                .attribution("NOAA GOES-R Series, GOES-19 (GOES-East) Geostationary Lightning Mapper")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-goes/")
                .mode(Mode.POLLING)
                .interval(Duration.ofSeconds(30), Duration.ofSeconds(30))
                .defaultBudget(Budget.of(10, 10_000, 3_000))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .titleField("label")
                .zeroResultsNormal(true)
                .tileProperties("flashes5m", "radiusKm", "label")
                .rankField("flashes5m")
                .kindDisplay(KIND, new KindDisplay("{label}", "lightning", "#F08C00", null));
        builder.fieldSpec("flashes5m", new FieldSpec(FieldType.INTEGER, "1", "Flashes in 5 min",
                "Member flashes in the last 5 minutes, as a bucket lower bound: 3, 5, 10, 20, 50, 100 or 200", null,
                Set.of()));
        builder.fieldSpec("radiusKm", new FieldSpec(FieldType.INTEGER, "km", "Radius",
                "Farthest member flash from the centroid, to 1 km", null, Set.of()));
        builder.fieldSpec("maxEnergyFj", new FieldSpec(FieldType.NUMBER, "fJ", "Brightest flash",
                "Radiant energy of the brightest member flash, as a decade lower bound (1, 10, 100 … fJ)", null,
                Set.of()));
        builder.fieldSpec("label", new FieldSpec(FieldType.STRING, null, "Label",
                "Lightning cluster, 20+ flashes/5 min", null, Set.of()));
        builder.fieldSpec("automated", new FieldSpec(FieldType.BOOLEAN, null, "Automated", "Always true", null, Set.of()));
        builder.fieldSpec("notAWarning", new FieldSpec(FieldType.BOOLEAN, null, "Not a warning",
                "Always true: automated detection; NWS warnings are authoritative", null, Set.of()));
        return builder.build();
    }
}
