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
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.S3Object;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
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
import java.util.HashMap;
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
 * {@value #PRECIP_RATE} (with PrecipFlag as band 2) and {@value #MESH}, every 2-minute frame; and the
 * second wave (prompt 053, §5.2): {@value #ROTATION}, {@value #QPE_1H}, {@value #QPE_24H} and
 * {@value #FFG_RATIO}.
 *
 * <p><b>Second wave thinning</b>: each second-wave product has a fixed {@code takeEvery} parameter (never
 * adaptive cadence, design §11): the first listed file of each {@code 2 min × takeEvery} bucket of the
 * key timestamps is committed and the rest are passed over ({@link #thin}). Defaults: rotation every frame,
 * QPE 1 h and the FFG ratio every 10 minutes, QPE 24 h every (hourly) file. Measured 2026-10-12 from the full
 * day's listings: rotation 187 MB, QPE 1 h 607 MB (121 MB at every fifth file), QPE 24 h 130 MB and the FFG
 * ratio 104 MB (21 MB), so about 0.46 GB/day at the defaults, and the whole source about 2.5 GB/day.
 *
 * <p><b>Rotation</b> is published on a 0.005° grid (14,000 × 7,000); the adapter reduces it 2 × 2 by maximum
 * to the 0.01° grid of the other MRMS products before it is stored ({@link #coarsen}), which keeps a decoded
 * frame at 49 MB instead of 196 MB and keeps every peak.
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

    /** 60-minute rotation track, 0–2 km AGL (prompt 053). */
    public static final String ROTATION = "mrms-rotation-60m";

    /** Radar-only 1-hour precipitation accumulation (prompt 053). */
    public static final String QPE_1H = "mrms-qpe-1h";

    /** Multi-sensor 24-hour precipitation accumulation, pass 2 (prompt 053). */
    public static final String QPE_24H = "mrms-qpe-24h";

    /** FLASH QPE-to-flash-flood-guidance ratio over 1 hour, percent (prompt 053). */
    public static final String FFG_RATIO = "mrms-ffg-ratio-1h";

    /** Lightning probability, next 30 minutes, percent (prompt 106). */
    public static final String LIGHTNING_PROB = "mrms-lightning-prob";

    /** Vertically integrated liquid, kg/m² (prompt 106). */
    public static final String VIL = "mrms-vil";

    /** 50 dBZ echo top height, km (prompt 106). */
    public static final String ECHO_TOP = "mrms-echo-top-50";

    /** The {@code takeEvery} parameter of {@value #LIGHTNING_PROB}. */
    public static final String LTNG_PROB_TAKE_EVERY = "ltgProbTakeEvery";

    /** The {@code takeEvery} parameter of {@value #VIL}. */
    public static final String VIL_TAKE_EVERY = "vilTakeEvery";

    /** The {@code takeEvery} parameter of {@value #ECHO_TOP}. */
    public static final String ECHO_TOP_TAKE_EVERY = "echotopTakeEvery";

    /** The {@code takeEvery} parameter of {@value #ROTATION}. */
    public static final String ROTATION_TAKE_EVERY = "rotationTakeEvery";

    /** The {@code takeEvery} parameter of {@value #QPE_1H}. */
    public static final String QPE_1H_TAKE_EVERY = "qpe1hTakeEvery";

    /** The {@code takeEvery} parameter of {@value #QPE_24H}. */
    public static final String QPE_24H_TAKE_EVERY = "qpe24hTakeEvery";

    /** The {@code takeEvery} parameter of {@value #FFG_RATIO}. */
    public static final String FFG_TAKE_EVERY = "ffgTakeEvery";

    /** The largest {@code takeEvery}. */
    public static final int MAX_TAKE_EVERY = 30;

    /** The upstream file spacing that {@code takeEvery} counts in. */
    static final Duration FILE_SPACING = Duration.ofMinutes(2);

    /** The resolution every stored MRMS product shares, degrees. */
    static final double STORED_RESOLUTION_DEG = 0.01;

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
    // Prompt 053; parameters verified on the 2026-10-13 fixtures (src/test/resources/fixtures/mrms/README.md).
    static final Upstream ROTATION_UPSTREAM = new Upstream("RotationTrack60min_00.50",
            DecodeRequest.grib2(ROTATION, "RotationTrack60min", 209, 3, 3, 102, 500.0, "500 m"));
    static final Upstream QPE_1H_UPSTREAM = new Upstream("RadarOnly_QPE_01H_00.00",
            DecodeRequest.grib2(QPE_1H, "RadarOnly_QPE_01H", 209, 6, 2, 102, 0.0, "0 m"));
    static final Upstream QPE_24H_UPSTREAM = new Upstream("MultiSensor_QPE_24H_Pass2_00.00",
            DecodeRequest.grib2(QPE_24H, "MultiSensor_QPE_24H_Pass2", 209, 6, 41, 102, 0.0, "0 m"));
    static final Upstream FFG_UPSTREAM = new Upstream("FLASH_QPE_FFG01H_00.00",
            DecodeRequest.grib2(FFG_RATIO, "FLASH_QPE_FFG01H", 209, 12, 26, 1, 0.0, "surface"));
    // Prompt 106; parameters verified on the 2026-10-15 files (disc 209).
    static final Upstream LIGHTNING_PROB_UPSTREAM = new Upstream("LightningProbabilityNext30minGrid_scale_1",
            DecodeRequest.grib2(LIGHTNING_PROB, "LightningProbabilityNext30minGrid", 209, 2, 5, 102, 500.0, "500 m"));
    static final Upstream VIL_UPSTREAM = new Upstream("VIL_00.50",
            DecodeRequest.grib2(VIL, "VIL", 209, 3, 41, 102, 500.0, "500 m"));
    static final Upstream ECHO_TOP_UPSTREAM = new Upstream("EchoTop_50_00.50",
            DecodeRequest.grib2(ECHO_TOP, "EchoTop_50", 209, 3, 46, 102, 500.0, "500 m"));

    /**
     * A second-wave product (prompt 053): one file per frame, thinned by a fixed {@code takeEvery}.
     *
     * @param productId the product
     * @param upstream its upstream directory and field
     * @param codec MRMS values to codes
     * @param takeEveryParam the parameter holding its {@code takeEvery}
     * @param defaultTakeEvery the default {@code takeEvery}
     * @param reduce true to reduce a finer grid to {@value #STORED_RESOLUTION_DEG}° by block maximum
     */
    record SecondWave(String productId, Upstream upstream, Function<float[], int[]> codec, String takeEveryParam,
            int defaultTakeEvery, boolean reduce) {

        /** The effective {@code takeEvery}: the parameter, else the default, clamped to 1..{@value #MAX_TAKE_EVERY}. */
        int takeEvery(ValidatedParams params) {
            int v = params == null || !params.has(takeEveryParam) || params.integer(takeEveryParam) == null
                    ? defaultTakeEvery : params.integer(takeEveryParam);
            return Math.max(1, Math.min(MAX_TAKE_EVERY, v));
        }
    }

    /** The four second-wave products, in fetch order. */
    static final List<SecondWave> SECOND_WAVE = List.of(
            new SecondWave(ROTATION, ROTATION_UPSTREAM, MrmsCodec::rotation, ROTATION_TAKE_EVERY, 1, true),
            new SecondWave(QPE_1H, QPE_1H_UPSTREAM, MrmsCodec::qpe, QPE_1H_TAKE_EVERY, 5, false),
            new SecondWave(QPE_24H, QPE_24H_UPSTREAM, MrmsCodec::qpe, QPE_24H_TAKE_EVERY, 1, false),
            new SecondWave(FFG_RATIO, FFG_UPSTREAM, MrmsCodec::ffgPercent, FFG_TAKE_EVERY, 5, false),
            // Prompt 106.
            new SecondWave(LIGHTNING_PROB, LIGHTNING_PROB_UPSTREAM, MrmsCodec::lightningProbability,
                    LTNG_PROB_TAKE_EVERY, 2, false),
            new SecondWave(VIL, VIL_UPSTREAM, MrmsCodec::vil, VIL_TAKE_EVERY, 2, false),
            new SecondWave(ECHO_TOP, ECHO_TOP_UPSTREAM, MrmsCodec::echoTop, ECHO_TOP_TAKE_EVERY, 2, false));

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
        Map<String, Set<DecoderCapability>> out = new HashMap<>();
        for (String product : List.of(REFLECTIVITY, PRECIP_RATE, MESH, ROTATION, QPE_1H, QPE_24H, FFG_RATIO,
                LIGHTNING_PROB, VIL, ECHO_TOP)) {
            out.put(product, Set.of(DecoderCapability.GRIB2_PNG));
        }
        return Map.copyOf(out);
    }

    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        for (SecondWave product : SECOND_WAVE) {
            if (params != null && params.has(product.takeEveryParam()) && params.integer(product.takeEveryParam()) != null) {
                int v = params.integer(product.takeEveryParam());
                if (v < 1 || v > MAX_TAKE_EVERY) {
                    problems.put(product.takeEveryParam(), "must be between 1 and " + MAX_TAKE_EVERY);
                }
            }
        }
        return problems;
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
        for (SecondWave product : SECOND_WAVE) {
            try {
                records.addAll(fetchSecondWave(ctx, product));
            } catch (AdapterException e) {
                failure = failure == null ? e : failure;
            }
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
        return fetchSingle(ctx, productId, upstream, codec, 1, false);
    }

    /** Fetches the new frames of a second-wave product at its {@code takeEvery} (prompt 053). */
    List<NormalizedRecord> fetchSecondWave(FetchContext ctx, SecondWave product) throws AdapterException {
        return fetchSingle(ctx, product.productId(), product.upstream(), product.codec(), product.takeEvery(ctx.params()),
                product.reduce());
    }

    /**
     * Fetches the new frames of a single-file product.
     *
     * @param takeEvery commit the first file of each {@code 2 min × takeEvery} bucket only (1 = every file)
     * @param reduce reduce a grid finer than {@value #STORED_RESOLUTION_DEG}° by block maximum
     */
    List<NormalizedRecord> fetchSingle(FetchContext ctx, String productId, Upstream upstream,
            Function<float[], int[]> codec, int takeEvery, boolean reduce) throws AdapterException {
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
        Set<String> taken = Set.copyOf(thin(listed.frames(), cursor == null ? null : timeOf(upstream, cursor).orElse(null),
                takeEvery).stream().map(Frame::key).toList());
        int committed = 0;
        for (Frame frame : listed.frames()) {
            if (committed >= MAX_FRAMES_PER_RUN) {
                break;
            }
            if (!taken.contains(frame.key())) {
                // Thinned out (prompt 053): passed over, never fetched.
                cursor = frame.key();
                continue;
            }
            if (frame.size() > MAX_RESPONSE_BYTES) {
                ctx.log().warn("MRMS {}: skipping {} ({} bytes is over the {}-byte cap)", productId, frame.key(),
                        frame.size(), MAX_RESPONSE_BYTES);
                cursor = frame.key();
                continue;
            }
            try {
                NormalizedRecord record = commitSingle(ctx, productId, upstream, codec, frame, reduce);
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
            Function<float[], int[]> codec, Frame frame, boolean reduce) throws AdapterException {
        Path grib = downloadInflated(ctx, productId, frame);
        try {
            DecodedGrid header = decoder.decode(grib, upstream.request().header());
            DecodeRequest request = upstream.request();
            GridDefinition native_ = header.gridDefinition();
            int factor = reduce ? reduction(native_) : 1;
            GridDefinition stored = factor > 1 ? coarsen(native_, factor) : native_;
            FrameInput.Builder input = FrameInput.builder(productId)
                    .validTime(frame.time())
                    .grid(stored)
                    .upstreamKey(frame.key())
                    .variable(request.label());
            if (factor > 1) {
                input.file(grib, file -> {
                    DecodedGrid grid = decoder.decode(file, request);
                    return DecodedBands.codes(MrmsCodec.rotationCoarsened(grid.floatData(), grid.nx(), grid.ny(), factor));
                });
                input.stat("reducedFrom", native_.nx() + "x" + native_.ny());
            } else {
                input.file(grib, file -> DecodedBands.codes(codec.apply(decoder.decode(file, request).floatData())));
            }
            return ctx.rasters().commit(input.build());
        } finally {
            GzipInputs.deleteQuietly(grib);
        }
    }

    /**
     * The block size that reduces {@code grid} to {@value #STORED_RESOLUTION_DEG}° (2 for MRMS's 0.005° rotation
     * grid), or 1 when it is not finer or not an exact multiple.
     */
    static int reduction(GridDefinition grid) {
        Object dx = grid.params().get("dx");
        Object dy = grid.params().get("dy");
        if (!GridDefinition.LATLON.equals(grid.type()) || !(dx instanceof Number x) || !(dy instanceof Number y)) {
            return 1;
        }
        long fx = Math.round(STORED_RESOLUTION_DEG / x.doubleValue());
        long fy = Math.round(STORED_RESOLUTION_DEG / y.doubleValue());
        if (fx < 2 || fx != fy || Math.abs(fx * x.doubleValue() - STORED_RESOLUTION_DEG) > 1e-9
                || grid.nx() % fx != 0 || grid.ny() % fx != 0) {
            return 1;
        }
        return (int) fx;
    }

    /**
     * {@code grid} reduced by {@code factor}: the same box, {@code factor} times fewer columns and rows, first cell
     * centers moved half a coarse cell inside the box (the decoder's {@code la1}, {@code lo1}, {@code dx},
     * {@code dy} convention), so the result equals the decoder's grid of a native 0.01° file.
     */
    static GridDefinition coarsen(GridDefinition grid, int factor) {
        if (factor <= 1) {
            return grid;
        }
        double dx = ((Number) grid.params().get("dx")).doubleValue();
        double dy = ((Number) grid.params().get("dy")).doubleValue();
        double lo1 = ((Number) grid.params().get("lo1")).doubleValue();
        double la1 = ((Number) grid.params().get("la1")).doubleValue();
        Map<String, Object> params = new LinkedHashMap<>(grid.params());
        params.put("dx", round9(dx * factor));
        params.put("dy", round9(dy * factor));
        params.put("lo1", round9(lo1 + dx * (factor - 1) / 2.0));
        params.put("la1", round9(la1 - dy * (factor - 1) / 2.0));
        return new GridDefinition(grid.type(), grid.nx() / factor, grid.ny() / factor, grid.west(), grid.south(),
                grid.east(), grid.north(), params);
    }

    private static double round9(double v) {
        return Math.round(v * 1e9) / 1e9;
    }

    /**
     * Thinning (prompt 053): the frames to commit at {@code takeEvery}, oldest first. The key timestamps are cut
     * into fixed buckets of {@code 2 min × takeEvery} (epoch-aligned, so 10-minute buckets start at :00, :10, …),
     * and the first listed frame of each bucket after the cursor's bucket is taken. Fixed: it depends only on the
     * keys, never on the weather or on what a frame holds.
     *
     * @param frames listed frames after the cursor, oldest first
     * @param cursorTime the timestamp of the last key passed (taken or thinned out), or null before the first
     * @param takeEvery 1 takes every frame
     */
    static List<Frame> thin(List<Frame> frames, Instant cursorTime, int takeEvery) {
        if (takeEvery <= 1) {
            return frames;
        }
        long bucketSeconds = FILE_SPACING.toSeconds() * takeEvery;
        long lastBucket = cursorTime == null ? Long.MIN_VALUE : Math.floorDiv(cursorTime.getEpochSecond(), bucketSeconds);
        List<Frame> out = new ArrayList<>();
        for (Frame frame : frames) {
            long bucket = Math.floorDiv(frame.time().getEpochSecond(), bucketSeconds);
            if (bucket > lastBucket) {
                out.add(frame);
                lastBucket = bucket;
            }
        }
        return out;
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
        ProductSpec.Spatial conus = new ProductSpec.Spatial("CONUS", 8, 0, 7, null);
        ProductSpec rotation = ProductSpec.builder(ROTATION)
                .title("MRMS rotation track (60 min, 0–2 km AGL)")
                .category("radar")
                .units("s-1")
                .dtype(RasterDtype.U16)
                .scaleOffset(0.0001, -0.0001)
                .nodata((double) MrmsCodec.ROTATION_NO_COVERAGE)
                .code(MrmsCodec.ROTATION_NO_COVERAGE, "noCoverage")
                .code(MrmsCodec.ROTATION_NONE, "noEcho")
                .defaultStyle("rotation@1")
                .temporal(temporal)
                .spatial(conus)
                .retention(ladder)
                .attribution(attribution)
                .coverageMeaning("CONUS; MRMS marks no radar coverage on this product, so 0 means no rotation detected"
                        + " or no radar there, never evidence of calm weather")
                .legendSummary("Maximum 0–2 km AGL azimuthal shear over the last 60 minutes, s⁻¹ (reduced 2 × 2 by maximum"
                        + " from MRMS's 0.005° grid)")
                .build();
        ProductSpec.Temporal tenMinutes = ProductSpec.Temporal.observation(Duration.ofMinutes(10), Duration.ofMinutes(30));
        ProductSpec qpe1h = ProductSpec.builder(QPE_1H)
                .title("MRMS radar-only precipitation (1 h)")
                .category("radar")
                .units("mm")
                .dtype(RasterDtype.U16)
                .scaleOffset(0.1, 0.0)
                .nodata((double) MrmsCodec.U16_MISSING)
                .code(MrmsCodec.U16_MISSING, "noCoverage")
                .defaultStyle("qpe@1")
                .temporal(tenMinutes)
                .spatial(conus)
                .retention(ladder)
                .attribution(attribution)
                .coverageMeaning("CONUS radar coverage; no coverage and missing data are transparent, never zero rain")
                .legendSummary("Radar-only precipitation accumulated over the last hour, mm (every 10 minutes by default)")
                .build();
        ProductSpec qpe24h = ProductSpec.builder(QPE_24H)
                .title("MRMS multi-sensor precipitation (24 h)")
                .category("radar")
                .units("mm")
                .dtype(RasterDtype.U16)
                .scaleOffset(0.1, 0.0)
                .nodata((double) MrmsCodec.U16_MISSING)
                .code(MrmsCodec.U16_MISSING, "noCoverage")
                .defaultStyle("qpe@1")
                // Hourly files arriving about 59 minutes after their valid time (measured 2026-10-13): the newest
                // frame is 59 to 119 minutes old, so the 90-minute gap the prompt names would flap every hour.
                .temporal(ProductSpec.Temporal.observation(Duration.ofHours(1), Duration.ofMinutes(150)))
                .spatial(conus)
                .retention(ProductSpec.RetentionLadder.maxAge(Duration.ofHours(48)))
                .attribution(attribution)
                .coverageMeaning("CONUS radar and gauge coverage; no coverage and missing data are transparent")
                .legendSummary("Multi-sensor (radar and gauge, pass 2) precipitation over the last 24 hours, mm, hourly")
                .build();
        ProductSpec ffg = ProductSpec.builder(FFG_RATIO)
                .title("MRMS 1-hour rainfall as a percentage of flash-flood guidance")
                .category("radar")
                .units("%")
                .dtype(RasterDtype.U16)
                .scaleOffset(0.1, 0.0)
                .nodata((double) MrmsCodec.U16_MISSING)
                .code(MrmsCodec.U16_MISSING, "noCoverage")
                .defaultStyle("ffg-ratio@1")
                .temporal(tenMinutes)
                .spatial(conus)
                .retention(ladder)
                .attribution(attribution)
                .coverageMeaning("CONUS where flash-flood guidance exists; elsewhere transparent, never read as safe")
                .legendSummary("FLASH radar QPE over 1 hour as a percentage of the 1-hour flash-flood guidance; 100 % or"
                        + " more means rainfall exceeds it (every 10 minutes by default)")
                .build();
        ProductSpec ltngProb = ProductSpec.builder(LIGHTNING_PROB)
                .title("MRMS lightning probability (next 30 min)")
                .category("radar")
                .units("%")
                .dtype(RasterDtype.U8)
                .scaleOffset(1.0, 0.0)
                .nodata((double) MrmsCodec.LTNG_PROB_NO_DATA)
                .code(MrmsCodec.LTNG_PROB_NO_DATA, "noCoverage")
                .defaultStyle("ltg-prob@1")
                .temporal(temporal)
                .spatial(conus)
                .retention(ladder)
                .attribution(attribution)
                .coverageMeaning("CONUS; transparent where no data, never read as no lightning risk")
                .legendSummary("Probability of cloud-to-ground lightning in the next 30 minutes, %")
                .build();
        ProductSpec vil = ProductSpec.builder(VIL)
                .title("MRMS vertically integrated liquid")
                .category("radar")
                .units("kg/m2")
                .dtype(RasterDtype.U16)
                .scaleOffset(0.01, 0.0)
                .nodata((double) MrmsCodec.U16_MISSING)
                .code(MrmsCodec.U16_MISSING, "noCoverage")
                .defaultStyle("vil@1")
                .temporal(temporal)
                .spatial(conus)
                .retention(ladder)
                .attribution(attribution)
                .coverageMeaning("CONUS radar coverage; transparent where no radar sees")
                .legendSummary("Vertically integrated liquid, kg/m²; large values mark severe storms")
                .build();
        ProductSpec echoTop = ProductSpec.builder(ECHO_TOP)
                .title("MRMS 50 dBZ echo top height")
                .category("radar")
                .units("km")
                .dtype(RasterDtype.U16)
                .scaleOffset(0.1, 0.0)
                .nodata((double) MrmsCodec.U16_MISSING)
                .code(MrmsCodec.U16_MISSING, "noCoverage")
                .defaultStyle("echotop@1")
                .temporal(temporal)
                .spatial(conus)
                .retention(ladder)
                .attribution(attribution)
                .coverageMeaning("CONUS radar coverage; transparent where no radar sees")
                .legendSummary("Height of the 50 dBZ echo top, km; very tall echo marks a strong updraft")
                .build();
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("NOAA MRMS radar mosaics")
                .description("NOAA Multi-Radar/Multi-Sensor composite reflectivity, precipitation rate with its type"
                        + " flag and 60-minute maximum hail size over CONUS, every 2-minute frame; plus the 60-minute"
                        + " rotation track, 1-hour and 24-hour precipitation and the 1-hour flash-flood guidance ratio,"
                        + " each at a fixed takeEvery.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://registry.opendata.aws/noaa-mrms-pds/")
                .attribution("NOAA/NSSL Multi-Radar/Multi-Sensor System (MRMS)")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-mrms-pds/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(2), Duration.ofMinutes(2))
                .defaultBudget(Budget.of(30, 12_000, 4_500))
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
                .product(rotation)
                .product(qpe1h)
                .product(qpe24h)
                .product(ffg)
                .product(ltngProb)
                .product(vil)
                .product(echoTop);
        for (SecondWave product : SECOND_WAVE) {
            builder.param(ParamSpec.optional(product.takeEveryParam(), ParamType.INTEGER,
                    "Commit one " + product.productId() + " file per 2 min x takeEvery (fixed, 1 to " + MAX_TAKE_EVERY
                            + "; never adaptive)", product.defaultTakeEvery()));
        }
        return builder.build();
    }
}
