package ai.foresignal.source.adapters.mrms;

import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.DecoderCapability;
import ai.foresignal.raster.decode.DecoderDependent;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.decode.GzipInputs;
import ai.foresignal.source.spi.AdapterException;
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
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.S3Object;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NOAA MRMS radar mosaics (prompt 046, Weather-Imagery-Design §5.1): {@value #REFLECTIVITY},
 * {@value #PRECIP_RATE} (with PrecipFlag as band 2) and {@value #MESH}, every 2-minute frame.
 *
 * <p><b>One access path</b>: anonymous S3 ListObjectsV2 on {@value #BUCKET} with {@code start-after},
 * prefix {@code CONUS/{Product}/{YYYYMMDD}/}; no NCEP {@code .latest} file and no adaptive cadence.
 * Per product and run the adapter lists after the last key it committed (kept in adapter state as
 * {@code lastKey:{productId}}), also lists yesterday's prefix while it is still catching up within
 * six hours after UTC midnight, skips frames older than the ladder's first rung (6 h), and commits
 * at most {@value #MAX_FRAMES_PER_RUN} new frames, oldest first.
 *
 * <p>Each frame: download the {@code .grib2.gz} into {@code rasters/tmp} (charging the listing's
 * {@code Size} against {@code mbPerDay} in advance), inflate it beside itself, read the grid and
 * times from the header, and commit it through {@code ctx.rasters()} with a decoder that runs on the
 * raster decode pool and maps MRMS values to codes ({@link MrmsCodec}). {@code frame_id} and
 * {@code valid_time} come from the key's timestamp ({@code 20261010-000041} becomes
 * {@code 20261010T000041Z}); {@code upstream_key} is the S3 key.
 *
 * <p><b>Precipitation</b> pairs {@code PrecipRate_00.00} and {@code PrecipFlag_00.00} files with the
 * identical timestamp (both land on even 2-minute marks). A rate file commits when its flag file
 * exists, or once it is {@value #FLAG_WAIT_MINUTES} minutes old with band 2 all missing and
 * {@code stats.flagMissing = true}.
 *
 * <p><b>Health</b>: a field missing from a file ("field MergedReflectivityQCComposite missing at level
 * 500 m") degrades the source and writes no frame; the key is still passed so the next run does not
 * retry it. A product the decoder self-test disabled is skipped.
 *
 * <p><b>Probe</b>: lists the newest reflectivity object, downloads it (one budgeted request), decodes
 * the header only and returns the grid summary; nothing is written, adapter state included.
 *
 * <p>Object URLs are {@code https://noaa-mrms-pds.s3.amazonaws.com/{key}}; the test property
 * {@value #BASE_URL_PROPERTY} switches them to path style under a stub.
 */
@Component
public class MrmsAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "mrms";

    /** The bucket. */
    public static final String BUCKET = "noaa-mrms-pds";

    /** The CONUS domain prefix. */
    public static final String DOMAIN = "CONUS";

    /** Test-only: a path-style base URL for object downloads ({@code {base}/{bucket}/{key}}). */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.mrms.base-url";

    /** Composite reflectivity. */
    public static final String REFLECTIVITY = "mrms-reflectivity";

    /** Precipitation rate with the PrecipFlag band. */
    public static final String PRECIP_RATE = "mrms-precip-rate";

    /** 60-minute maximum estimated size of hail. */
    public static final String MESH = "mrms-mesh-60m";

    /** New frames committed per product per run. */
    public static final int MAX_FRAMES_PER_RUN = 15;

    /** How long a rate file waits for its flag file. */
    public static final int FLAG_WAIT_MINUTES = 4;

    /** Pages listed per prefix per run (1,000 keys each; a day holds 720 per product). */
    static final int MAX_PAGES = 3;

    /** The ladder's first rung: frames older than this are not fetched. */
    static final Duration FETCH_WINDOW = Duration.ofHours(6);

    /** The response cap, and the largest object downloaded. */
    static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    /** Prefix of the adapter-state keys holding each product's last committed key. */
    static final String STATE_PREFIX = "lastKey:";

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT);
    private static final DateTimeFormatter KEY_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT);

    /**
     * One upstream MRMS product directory.
     *
     * @param directory the product directory, for example {@code MergedReflectivityQCComposite_00.50}
     * @param request the GRIB2 field the files hold
     */
    record Upstream(String directory, DecodeRequest request) {

        /** The key pattern; group 1 is the timestamp. */
        Pattern keyPattern() {
            return Pattern.compile("^" + DOMAIN + "/" + Pattern.quote(directory) + "/\\d{8}/MRMS_"
                    + Pattern.quote(directory) + "_(\\d{8}-\\d{6})\\.grib2\\.gz$");
        }

        /** The day prefix. */
        String prefix(LocalDate day) {
            return DOMAIN + "/" + directory + "/" + DAY.format(day) + "/";
        }

        /** The key of the file stamped {@code time}. */
        String key(Instant time) {
            LocalDateTime t = LocalDateTime.ofInstant(time, ZoneOffset.UTC);
            return prefix(t.toLocalDate()) + "MRMS_" + directory + "_" + KEY_TIME.format(t) + ".grib2.gz";
        }
    }

    static final Upstream REFLECTIVITY_UPSTREAM = new Upstream("MergedReflectivityQCComposite_00.50",
            DecodeRequest.grib2(REFLECTIVITY, "MergedReflectivityQCComposite", 209, 10, 0, 102, 500.0, "500 m"));
    static final Upstream PRECIP_RATE_UPSTREAM = new Upstream("PrecipRate_00.00",
            DecodeRequest.grib2(PRECIP_RATE, "PrecipRate", 209, 6, 1, 102, 0.0, "0 m"));
    static final Upstream PRECIP_FLAG_UPSTREAM = new Upstream("PrecipFlag_00.00",
            DecodeRequest.grib2(PRECIP_RATE, "PrecipFlag", 209, 6, 0, 102, 0.0, "0 m"));
    static final Upstream MESH_UPSTREAM = new Upstream("MESH_Max_60min_00.50",
            DecodeRequest.grib2(MESH, "MESH_Max_60min", 209, 3, 30, 102, 500.0, "500 m"));

    /**
     * One listed frame.
     *
     * @param key the S3 key
     * @param time the key's timestamp
     * @param size the object size from the listing
     */
    record Frame(String key, Instant time, long size) {}

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final Grib2Decoder decoder;
    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param decoder the GRIB2 decoder
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real bucket
     */
    @Autowired
    public MrmsAdapter(Grib2Decoder decoder, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.decoder = decoder;
        String chosen = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Map<String, Set<DecoderCapability>> decoderRequirements() {
        return Map.of(REFLECTIVITY, Set.of(DecoderCapability.GRIB2_PNG), PRECIP_RATE, Set.of(DecoderCapability.GRIB2_PNG),
                MESH, Set.of(DecoderCapability.GRIB2_PNG));
    }

    /** The URL of {@code key}. */
    public URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        if (ctx.rasters().probing()) {
            return probe(ctx);
        }
        List<NormalizedRecord> records = new ArrayList<>();
        AdapterException failure = null;
        try {
            records.addAll(fetchSingle(ctx, REFLECTIVITY, REFLECTIVITY_UPSTREAM, MrmsCodec::reflectivity));
        } catch (AdapterException e) {
            failure = e;
        }
        try {
            records.addAll(fetchPrecip(ctx));
        } catch (AdapterException e) {
            failure = failure == null ? e : failure;
        }
        try {
            records.addAll(fetchSingle(ctx, MESH, MESH_UPSTREAM, MrmsCodec::mesh));
        } catch (AdapterException e) {
            failure = failure == null ? e : failure;
        }
        if (failure != null) {
            if (records.isEmpty()) {
                throw failure;
            }
            ctx.log().warn("MRMS: committed {} frame(s), but a product failed: {}", records.size(), failure.getMessage());
        }
        return FetchResult.of(records);
    }

    /** Fetches the new frames of a single-file product. */
    List<NormalizedRecord> fetchSingle(FetchContext ctx, String productId, Upstream upstream,
            Function<float[], int[]> codec) throws AdapterException {
        RasterSink sink = ctx.rasters();
        if (sink.disabled(productId)) {
            return List.of();
        }
        String stateKey = STATE_PREFIX + productId;
        String last = ctx.state().get(stateKey).orElse(null);
        Instant now = ctx.now();
        Listed listed = listNew(ctx, upstream, last, now);
        List<NormalizedRecord> out = new ArrayList<>();
        String cursor = listed.passedOldKey(last);
        int committed = 0;
        for (Frame frame : listed.frames()) {
            if (committed >= MAX_FRAMES_PER_RUN) {
                break;
            }
            if (frame.size() > MAX_RESPONSE_BYTES) {
                ctx.log().warn("MRMS {}: skipping {} ({} bytes is over the {}-byte cap)", productId, frame.key(),
                        frame.size(), MAX_RESPONSE_BYTES);
                cursor = frame.key();
                continue;
            }
            try {
                NormalizedRecord record = commitSingle(ctx, productId, upstream, codec, frame);
                if (record != null) {
                    out.add(record);
                }
            } catch (SilentFormatChangeException e) {
                sink.reportFormatChange(productId, e.getMessage());
                ctx.log().warn("MRMS {}: {} ({})", productId, e.getMessage(), frame.key());
            } catch (AdapterException e) {
                saveCursor(ctx, stateKey, cursor, last);
                throw e;
            }
            cursor = frame.key();
            committed++;
        }
        saveCursor(ctx, stateKey, cursor, last);
        return out;
    }

    private NormalizedRecord commitSingle(FetchContext ctx, String productId, Upstream upstream,
            Function<float[], int[]> codec, Frame frame) throws AdapterException {
        Path grib = downloadInflated(ctx, productId, frame);
        try {
            DecodedGrid header = decoder.decode(grib, upstream.request().header());
            DecodeRequest request = upstream.request();
            return ctx.rasters().commit(FrameInput.builder(productId)
                    .validTime(frame.time())
                    .grid(header.gridDefinition())
                    .file(grib, file -> DecodedBands.codes(codec.apply(decoder.decode(file, request).floatData())))
                    .upstreamKey(frame.key())
                    .variable(request.label())
                    .build());
        } finally {
            GzipInputs.deleteQuietly(grib);
        }
    }

    /** Fetches the new precipitation frames, pairing rate and flag files. */
    List<NormalizedRecord> fetchPrecip(FetchContext ctx) throws AdapterException {
        RasterSink sink = ctx.rasters();
        if (sink.disabled(PRECIP_RATE)) {
            return List.of();
        }
        String stateKey = STATE_PREFIX + PRECIP_RATE;
        String last = ctx.state().get(stateKey).orElse(null);
        Instant now = ctx.now();
        Listed rates = listNew(ctx, PRECIP_RATE_UPSTREAM, last, now);
        String cursor = rates.passedOldKey(last);
        List<NormalizedRecord> out = new ArrayList<>();
        if (rates.frames().isEmpty()) {
            saveCursor(ctx, stateKey, cursor, last);
            return out;
        }
        String flagStart = last == null ? null : PRECIP_FLAG_UPSTREAM.key(timeOf(PRECIP_RATE_UPSTREAM, last).orElse(now));
        Map<Instant, Frame> flags = new LinkedHashMap<>();
        for (Frame flag : listNew(ctx, PRECIP_FLAG_UPSTREAM, flagStart, now).frames()) {
            flags.put(flag.time(), flag);
        }
        int committed = 0;
        for (Frame rate : rates.frames()) {
            if (committed >= MAX_FRAMES_PER_RUN) {
                break;
            }
            Frame flag = flags.get(rate.time());
            boolean waited = !Duration.between(rate.time(), now).minusMinutes(FLAG_WAIT_MINUTES).isNegative();
            if (flag == null && !waited) {
                break;
            }
            try {
                NormalizedRecord record = commitPrecip(ctx, rate, flag);
                if (record != null) {
                    out.add(record);
                }
            } catch (SilentFormatChangeException e) {
                sink.reportFormatChange(PRECIP_RATE, e.getMessage());
                ctx.log().warn("MRMS {}: {} ({})", PRECIP_RATE, e.getMessage(), rate.key());
            } catch (AdapterException e) {
                saveCursor(ctx, stateKey, cursor, last);
                throw e;
            }
            cursor = rate.key();
            committed++;
        }
        saveCursor(ctx, stateKey, cursor, last);
        return out;
    }

    private NormalizedRecord commitPrecip(FetchContext ctx, Frame rate, Frame flag) throws AdapterException {
        Path rateGrib = null;
        Path flagGrib = null;
        try {
            if (rate.size() > MAX_RESPONSE_BYTES || (flag != null && flag.size() > MAX_RESPONSE_BYTES)) {
                throw new AdapterException("MRMS precipitation file over the " + MAX_RESPONSE_BYTES + "-byte cap: " + rate.key());
            }
            rateGrib = downloadInflated(ctx, PRECIP_RATE, rate);
            flagGrib = flag == null ? null : downloadInflated(ctx, PRECIP_RATE, flag);
            DecodedGrid header = decoder.decode(rateGrib, PRECIP_RATE_UPSTREAM.request().header());
            Path flagFile = flagGrib;
            FrameInput.Builder frame = FrameInput.builder(PRECIP_RATE)
                    .validTime(rate.time())
                    .grid(header.gridDefinition())
                    .file(rateGrib, file -> precipBands(file, flagFile))
                    .upstreamKey(rate.key())
                    .variable(PRECIP_RATE_UPSTREAM.request().label());
            if (flag == null) {
                frame.stat("flagMissing", true);
            }
            return ctx.rasters().commit(frame.build());
        } finally {
            GzipInputs.deleteQuietly(rateGrib);
            GzipInputs.deleteQuietly(flagGrib);
        }
    }

    /** Band 1 from the rate file, band 2 from the flag file (all missing without one). */
    DecodedBands precipBands(Path rateFile, Path flagFile) {
        try {
            DecodedGrid rate = decoder.decode(rateFile, PRECIP_RATE_UPSTREAM.request());
            int[] flags;
            if (flagFile == null) {
                flags = MrmsCodec.missingFlags(rate.cells());
            } else {
                DecodedGrid flag = decoder.decode(flagFile, PRECIP_FLAG_UPSTREAM.request());
                if (flag.nx() != rate.nx() || flag.ny() != rate.ny()) {
                    throw new SilentFormatChangeException(PRECIP_RATE, "PrecipFlag grid " + flag.nx() + "x" + flag.ny()
                            + " differs from PrecipRate grid " + rate.nx() + "x" + rate.ny());
                }
                flags = MrmsCodec.precipFlag(flag.floatData());
            }
            return DecodedBands.codes(MrmsCodec.precipRate(rate.floatData()), flags);
        } finally {
            GzipInputs.deleteQuietly(flagFile);
        }
    }

    /** Probes: the newest reflectivity object, header only; writes nothing. */
    FetchResult probe(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        Optional<Frame> newest = Optional.empty();
        for (LocalDate day : List.of(today, today.minusDays(1))) {
            newest = newestIn(ctx, REFLECTIVITY_UPSTREAM, day);
            if (newest.isPresent()) {
                break;
            }
        }
        if (newest.isEmpty()) {
            return FetchResult.of(List.of());
        }
        Frame frame = newest.get();
        Path grib = downloadInflated(ctx, REFLECTIVITY, frame);
        try {
            DecodedGrid header = decoder.decode(grib, REFLECTIVITY_UPSTREAM.request().header());
            NormalizedRecord preview = ctx.rasters().commit(FrameInput.builder(REFLECTIVITY)
                    .validTime(frame.time())
                    .grid(header.gridDefinition())
                    .file(grib, file -> {
                        throw new IllegalStateException("A probe never decodes samples");
                    })
                    .upstreamKey(frame.key())
                    .variable(REFLECTIVITY_UPSTREAM.request().label())
                    .build());
            return FetchResult.of(preview == null ? List.of() : List.of(preview));
        } finally {
            GzipInputs.deleteQuietly(grib);
        }
    }

    private Optional<Frame> newestIn(FetchContext ctx, Upstream upstream, LocalDate day) throws AdapterException {
        Frame newest = null;
        String startAfter = null;
        for (int page = 0; page < MAX_PAGES; page++) {
            S3Listing listing = ctx.http().listS3(BUCKET, upstream.prefix(day), startAfter);
            for (S3Object object : listing.objects()) {
                Optional<Frame> frame = frame(upstream, object);
                if (frame.isPresent() && (newest == null || frame.get().time().isAfter(newest.time()))) {
                    newest = frame.get();
                }
            }
            if (!listing.truncated() || listing.nextStartAfter() == null) {
                break;
            }
            startAfter = listing.nextStartAfter();
        }
        return Optional.ofNullable(newest);
    }

    /**
     * The frames listed after a key.
     *
     * @param frames frames inside the fetch window, oldest first
     * @param newestTooOld the newest matching key older than the window, or null
     */
    record Listed(List<Frame> frames, String newestTooOld) {

        /** The cursor after passing the too-old keys: the newer of {@code last} and those keys. */
        String passedOldKey(String last) {
            if (newestTooOld == null) {
                return last;
            }
            return last == null || newestTooOld.compareTo(last) > 0 ? newestTooOld : last;
        }
    }

    /** Lists {@code upstream}'s keys after {@code last} on today's prefix (and yesterday's while catching up). */
    Listed listNew(FetchContext ctx, Upstream upstream, String last, Instant now) throws AdapterException {
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        Instant midnight = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant oldest = now.minus(FETCH_WINDOW);
        List<LocalDate> days = new ArrayList<>();
        boolean caughtUpToday = last != null && last.startsWith(upstream.prefix(today));
        if (!caughtUpToday && Duration.between(midnight, now).compareTo(FETCH_WINDOW) < 0) {
            days.add(today.minusDays(1));
        }
        days.add(today);
        List<Frame> frames = new ArrayList<>();
        String newestTooOld = null;
        for (LocalDate day : days) {
            String prefix = upstream.prefix(day);
            String startAfter = last != null && last.startsWith(prefix) ? last : null;
            if (last != null && startAfter == null && last.compareTo(prefix) > 0) {
                continue;
            }
            for (int page = 0; page < MAX_PAGES; page++) {
                S3Listing listing = ctx.http().listS3(BUCKET, prefix, startAfter);
                for (S3Object object : listing.objects()) {
                    Optional<Frame> frame = frame(upstream, object);
                    if (frame.isEmpty() || (last != null && frame.get().key().compareTo(last) <= 0)) {
                        continue;
                    }
                    if (frame.get().time().isBefore(oldest)) {
                        if (newestTooOld == null || frame.get().key().compareTo(newestTooOld) > 0) {
                            newestTooOld = frame.get().key();
                        }
                        continue;
                    }
                    frames.add(frame.get());
                }
                if (!listing.truncated() || listing.nextStartAfter() == null) {
                    break;
                }
                startAfter = listing.nextStartAfter();
            }
        }
        frames.sort(Comparator.comparing(Frame::time).thenComparing(Frame::key));
        return new Listed(List.copyOf(frames), newestTooOld);
    }

    /** The frame of a listed object, when its key is one of {@code upstream}'s files. */
    static Optional<Frame> frame(Upstream upstream, S3Object object) {
        Matcher m = upstream.keyPattern().matcher(object.key());
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(new Frame(object.key(), parseKeyTime(m.group(1)), object.size()));
    }

    /** The timestamp of a key of {@code upstream}. */
    static Optional<Instant> timeOf(Upstream upstream, String key) {
        Matcher m = upstream.keyPattern().matcher(key);
        return m.matches() ? Optional.of(parseKeyTime(m.group(1))) : Optional.empty();
    }

    /** {@code 20261010-000041} as an instant (UTC). */
    static Instant parseKeyTime(String stamp) {
        return LocalDateTime.parse(stamp, KEY_TIME).toInstant(ZoneOffset.UTC);
    }

    private Path downloadInflated(FetchContext ctx, String productId, Frame frame) throws AdapterException {
        String name = productId + "-" + KEY_TIME.format(LocalDateTime.ofInstant(frame.time(), ZoneOffset.UTC))
                + "-" + Long.toHexString(System.nanoTime()) + ".grib2.gz";
        DownloadResult download = ctx.http().download(objectUri(frame.key()), Path.of(name), MAX_RESPONSE_BYTES,
                frame.size());
        try {
            if (frame.size() > 0 && download.bytes() != frame.size()) {
                throw new AdapterException("MRMS " + frame.key() + ": downloaded " + download.bytes() + " bytes, the listing said "
                        + frame.size());
            }
            return GzipInputs.inflateSibling(download.file());
        } finally {
            GzipInputs.deleteQuietly(download.file());
        }
    }

    private static void saveCursor(FetchContext ctx, String stateKey, String cursor, String last) {
        if (cursor != null && !cursor.equals(last)) {
            ctx.state().put(stateKey, cursor);
        }
    }

    private static SourceDescriptor buildDescriptor() {
        ProductSpec.Attribution attribution = new ProductSpec.Attribution(
                "NOAA/NSSL Multi-Radar/Multi-Sensor System (MRMS), via NOAA Open Data Dissemination", null,
                "https://registry.opendata.aws/noaa-mrms-pds/", "US-PD", null, false);
        ProductSpec.RetentionLadder ladder = new ProductSpec.RetentionLadder(
                List.of(new ProductSpec.RetentionLadder.Rung(Duration.ofHours(6), Duration.ofMinutes(10))),
                Duration.ofHours(48), null);
        ProductSpec.Temporal temporal = ProductSpec.Temporal.observation(Duration.ofMinutes(2), Duration.ofMinutes(6));
        ProductSpec reflectivity = ProductSpec.builder(REFLECTIVITY)
                .title("MRMS composite reflectivity")
                .category("radar")
                .units("dBZ")
                .dtype(RasterDtype.U8)
                .scaleOffset(0.5, -33.0)
                .nodata(0.0)
                .code(MrmsCodec.REFLECTIVITY_NO_COVERAGE, "noCoverage")
                .code(MrmsCodec.REFLECTIVITY_NO_ECHO, "noEcho")
                .defaultStyle("nws-reflectivity@1")
                .temporal(temporal)
                .spatial(new ProductSpec.Spatial("CONUS", 8, 0, 7, null))
                .retention(ladder)
                .attribution(attribution)
                .coverageMeaning("CONUS radar coverage; transparent where no radar sees, never read as clear weather")
                .legendSummary("Merged composite reflectivity (QC), dBZ")
                .build();
        ProductSpec precip = ProductSpec.builder(PRECIP_RATE)
                .title("MRMS precipitation rate")
                .category("radar")
                .units("mm/h")
                .dtype(RasterDtype.U16)
                .scaleOffset(0.1, 0.0)
                .nodata((double) MrmsCodec.U16_MISSING)
                .code(MrmsCodec.U16_MISSING, "noCoverage")
                .bands("rate", "flag")
                .defaultStyle("precip-rate@1")
                .temporal(temporal)
                .spatial(new ProductSpec.Spatial("CONUS", 8, 0, 7, null))
                .retention(ladder)
                .attribution(attribution)
                .legendSummary("Instantaneous precipitation rate, mm/h; band 2 is the PrecipFlag type code")
                .build();
        ProductSpec mesh = ProductSpec.builder(MESH)
                .title("MRMS maximum hail size (60 min)")
                .category("radar")
                .units("mm")
                .dtype(RasterDtype.U8)
                .scaleOffset(1.0, -1.0)
                .nodata(0.0)
                .code(MrmsCodec.MESH_NO_COVERAGE, "noCoverage")
                .code(MrmsCodec.MESH_ZERO, "noEcho")
                .defaultStyle("mesh@1")
                .temporal(temporal)
                .spatial(new ProductSpec.Spatial("CONUS", 8, 0, 7, null))
                .retention(ladder)
                .attribution(attribution)
                .legendSummary("Maximum estimated size of hail over the last 60 minutes, mm")
                .build();
        return SourceDescriptor.builder(ID)
                .name("NOAA MRMS radar mosaics")
                .description("NOAA Multi-Radar/Multi-Sensor composite reflectivity, precipitation rate with its type"
                        + " flag, and 60-minute maximum hail size over CONUS, every 2-minute frame.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://registry.opendata.aws/noaa-mrms-pds/")
                .attribution("NOAA/NSSL Multi-Radar/Multi-Sensor System (MRMS)")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-mrms-pds/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(2), Duration.ofMinutes(2))
                .defaultBudget(Budget.of(30, 12_000, 4_000))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .product(reflectivity)
                .product(precip)
                .product(mesh)
                .build();
    }
}
