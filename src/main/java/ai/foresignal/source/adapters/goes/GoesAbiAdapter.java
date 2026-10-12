package ai.foresignal.source.adapters.goes;

import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.DecoderCapability;
import ai.foresignal.raster.decode.DecoderDependent;
import ai.foresignal.raster.decode.GzipInputs;
import ai.foresignal.raster.decode.NetcdfDecoder;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * GOES-East (GOES-19) ABI band 13, clean longwave IR (prompt 046, Weather-Imagery-Design §5.1):
 * product {@value #IR}, one frame per 5-minute CONUS scan.
 *
 * <p>Objects: bucket {@value #BUCKET}, prefix {@code ABI-L2-CMIPC/{YYYY}/{DOY}/{HH}/}. Keys sort by
 * scan mode and channel before time, so each hour is listed with the prefix
 * {@code …/OR_ABI-L2-CMIPC-M} and {@code start-after} the last committed key (when it is in that
 * hour), and filtered to {@code OR_ABI-L2-CMIPC-M\dC13_G19_s(\d{14})_}: the scan mode is never
 * hard-coded. Each run lists the hours from the last committed key's hour (at most
 * {@value #MAX_HOURS_BACK} hours back) to now, and commits at most {@value #MAX_FRAMES_PER_RUN} new
 * frames, oldest first.
 *
 * <p>Each frame: download the NetCDF4 file into {@code rasters/tmp} (charging its listed size against
 * {@code mbPerDay}), read the {@code goes_imager_projection} grid and coverage times from the header,
 * then commit with a decoder that reads {@code CMI} in enhanced mode, drops samples whose {@code DQF}
 * exceeds 1, and quantizes to u16 ({@link GoesCodec}). {@code frame_id} comes from the key's
 * {@code s} start time; {@code obs_start}/{@code obs_end} from {@code time_coverage_start}/
 * {@code time_coverage_end}; {@code valid_time = obs_start}.
 *
 * <p>Health: a missing {@code CMI} ("variable CMI missing") or projection attribute degrades the source
 * and writes no frame. Probe: the newest C13 object, header only; nothing written.
 *
 * <p>A scan-mode change in the middle of an hour (M6 to M3, say) sorts the new mode's keys before the
 * last committed one; those frames of that hour are skipped, and the next hour lists normally.
 */
@Component
public class GoesAbiAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "goes-east-abi";

    /** The GOES-19 bucket. */
    public static final String BUCKET = "noaa-goes19";

    /** The product. */
    public static final String IR = "goes-east-ir";

    /** Test-only: a path-style base URL for object downloads ({@code {base}/{bucket}/{key}}). */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.goes.base-url";

    /** New frames committed per run. */
    public static final int MAX_FRAMES_PER_RUN = 15;

    /** Hours listed back from now at most. */
    public static final int MAX_HOURS_BACK = 3;

    /** The response cap, and the largest object downloaded. */
    static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    /** The adapter-state key of the last committed key. */
    static final String STATE_KEY = "lastKey:" + IR;

    /** The band 13 key pattern of GOES-19; group 1 is the {@code s} start time. */
    static final Pattern KEY = Pattern.compile(
            "^ABI-L2-CMIPC/\\d{4}/\\d{3}/\\d{2}/OR_ABI-L2-CMIPC-M\\dC13_G19_s(\\d{14})_e\\d{14}_c\\d{14}\\.nc$");

    /** What is decoded. */
    static final DecodeRequest REQUEST = DecodeRequest.netcdf(IR, "CMI").withQuality("DQF", 1);

    /**
     * One listed scan.
     *
     * @param key the S3 key
     * @param time the {@code s} start time
     * @param size the object size from the listing
     */
    record Frame(String key, Instant time, long size) {}

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
    public GoesAbiAdapter(NetcdfDecoder decoder, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        return Map.of(IR, Set.of(DecoderCapability.NETCDF4));
    }

    /** The URL of {@code key}. */
    public URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    /** The listing prefix of the hour holding {@code time}. */
    static String hourPrefix(Instant time) {
        LocalDateTime t = LocalDateTime.ofInstant(time, ZoneOffset.UTC);
        return String.format(Locale.ROOT, "ABI-L2-CMIPC/%04d/%03d/%02d/OR_ABI-L2-CMIPC-M", t.getYear(), t.getDayOfYear(),
                t.getHour());
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        if (ctx.rasters().probing()) {
            return probe(ctx);
        }
        if (ctx.rasters().disabled(IR)) {
            return FetchResult.of(List.of());
        }
        String last = ctx.state().get(STATE_KEY).orElse(null);
        Instant lastTime = last == null ? null : startTime(last).orElse(null);
        Instant now = ctx.now();
        List<Frame> frames = listNew(ctx, last, lastTime, now);
        List<NormalizedRecord> out = new ArrayList<>();
        String cursor = last;
        int committed = 0;
        try {
            for (Frame frame : frames) {
                if (committed >= MAX_FRAMES_PER_RUN) {
                    break;
                }
                if (frame.size() > MAX_RESPONSE_BYTES) {
                    ctx.log().warn("GOES {}: skipping {} ({} bytes is over the {}-byte cap)", IR, frame.key(), frame.size(),
                            MAX_RESPONSE_BYTES);
                    cursor = frame.key();
                    continue;
                }
                try {
                    NormalizedRecord record = commit(ctx, frame);
                    if (record != null) {
                        out.add(record);
                    }
                } catch (SilentFormatChangeException e) {
                    ctx.rasters().reportFormatChange(IR, e.getMessage());
                    ctx.log().warn("GOES {}: {} ({})", IR, e.getMessage(), frame.key());
                }
                cursor = frame.key();
                committed++;
            }
        } catch (AdapterException e) {
            if (out.isEmpty()) {
                saveCursor(ctx, cursor, last);
                throw e;
            }
            ctx.log().warn("GOES {}: committed {} frame(s), then failed: {}", IR, out.size(), e.getMessage());
        }
        saveCursor(ctx, cursor, last);
        return FetchResult.of(out);
    }

    private NormalizedRecord commit(FetchContext ctx, Frame frame) throws AdapterException {
        Path file = download(ctx, frame);
        try {
            DecodedGrid header = decoder.decode(file, REQUEST.header());
            Instant obsStart = instant(header.metadata().get("obsStart")).orElse(frame.time());
            Instant obsEnd = instant(header.metadata().get("obsEnd")).orElse(null);
            return ctx.rasters().commit(FrameInput.builder(IR)
                    .validTime(frame.time())
                    .observationPeriod(obsStart, obsEnd)
                    .grid(header.gridDefinition())
                    .file(file, f -> DecodedBands.codes(GoesCodec.brightness(decoder.decode(f, REQUEST).floatData())))
                    .upstreamKey(frame.key())
                    .variable(REQUEST.variable())
                    .build());
        } finally {
            GzipInputs.deleteQuietly(file);
        }
    }

    /** Probes: the newest band 13 scan, header only; writes nothing. */
    FetchResult probe(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        Frame newest = null;
        for (Instant hour : List.of(now, now.minus(1, ChronoUnit.HOURS))) {
            S3Listing listing = ctx.http().listS3(BUCKET, hourPrefix(hour), null);
            for (S3Object object : listing.objects()) {
                Optional<Frame> frame = frame(object);
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
        Path file = download(ctx, newest);
        try {
            DecodedGrid header = decoder.decode(file, REQUEST.header());
            NormalizedRecord preview = ctx.rasters().commit(FrameInput.builder(IR)
                    .validTime(newest.time())
                    .grid(header.gridDefinition())
                    .file(file, f -> {
                        throw new IllegalStateException("A probe never decodes samples");
                    })
                    .upstreamKey(newest.key())
                    .variable(REQUEST.variable())
                    .build());
            return FetchResult.of(preview == null ? List.of() : List.of(preview));
        } finally {
            GzipInputs.deleteQuietly(file);
        }
    }

    /** The new band 13 scans after {@code lastTime}, oldest first. */
    List<Frame> listNew(FetchContext ctx, String last, Instant lastTime, Instant now) throws AdapterException {
        Instant nowHour = now.truncatedTo(ChronoUnit.HOURS);
        Instant earliest = nowHour.minus(MAX_HOURS_BACK, ChronoUnit.HOURS);
        Instant firstHour = lastTime == null ? nowHour.minus(1, ChronoUnit.HOURS) : lastTime.truncatedTo(ChronoUnit.HOURS);
        if (firstHour.isBefore(earliest)) {
            firstHour = earliest;
        }
        List<Frame> frames = new ArrayList<>();
        for (Instant hour = firstHour; !hour.isAfter(nowHour); hour = hour.plus(1, ChronoUnit.HOURS)) {
            String prefix = hourPrefix(hour);
            String startAfter = last != null && last.startsWith(prefix) ? last : null;
            S3Listing listing = ctx.http().listS3(BUCKET, prefix, startAfter);
            while (true) {
                for (S3Object object : listing.objects()) {
                    frame(object).filter(f -> lastTime == null || f.time().isAfter(lastTime)).ifPresent(frames::add);
                }
                if (!listing.truncated() || listing.nextStartAfter() == null) {
                    break;
                }
                listing = ctx.http().listS3(BUCKET, prefix, listing.nextStartAfter());
            }
        }
        frames.sort(Comparator.comparing(Frame::time).thenComparing(Frame::key));
        return frames;
    }

    /** The scan of a listed object, when it is a GOES-19 CMIPC band 13 file. */
    static Optional<Frame> frame(S3Object object) {
        return startTime(object.key()).map(time -> new Frame(object.key(), time, object.size()));
    }

    /** The {@code s} start time of a band 13 key. */
    static Optional<Instant> startTime(String key) {
        Matcher m = KEY.matcher(key);
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(parseStart(m.group(1)));
    }

    /** {@code YYYYDDDHHMMSSt} (year, day of year, time, tenths of a second) as an instant. */
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

    private Path download(FetchContext ctx, Frame frame) throws AdapterException {
        String name = IR + "-" + frame.time().toEpochMilli() + "-" + Long.toHexString(System.nanoTime()) + ".nc";
        DownloadResult result = ctx.http().download(objectUri(frame.key()), Path.of(name), MAX_RESPONSE_BYTES, frame.size());
        if (frame.size() > 0 && result.bytes() != frame.size()) {
            GzipInputs.deleteQuietly(result.file());
            throw new AdapterException("GOES " + frame.key() + ": downloaded " + result.bytes() + " bytes, the listing said "
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

    private static void saveCursor(FetchContext ctx, String cursor, String last) {
        if (cursor != null && !cursor.equals(last)) {
            ctx.state().put(STATE_KEY, cursor);
        }
    }

    private static SourceDescriptor buildDescriptor() {
        ProductSpec ir = ProductSpec.builder(IR)
                .title("GOES-East clean infrared (ABI band 13)")
                .category("satellite")
                .units("K")
                .dtype(RasterDtype.U16)
                .scaleOffset(GoesCodec.SCALE_K, GoesCodec.OFFSET_K)
                .nodata((double) GoesCodec.NO_DATA)
                .defaultStyle("ir-gray@1")
                .style("ir-enhanced@1")
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(5), Duration.ofMinutes(15)))
                .spatial(new ProductSpec.Spatial("CONUS", 7, 0, 6, null))
                .retention(new ProductSpec.RetentionLadder(
                        List.of(new ProductSpec.RetentionLadder.Rung(Duration.ofHours(24), Duration.ofMinutes(30))),
                        Duration.ofHours(72), null))
                .attribution(new ProductSpec.Attribution(
                        "NOAA GOES-19 ABI L2 Cloud and Moisture Imagery, band 13, via NOAA Open Data Dissemination", null,
                        "https://registry.opendata.aws/noaa-goes/", "US-PD", null, false))
                .coverageMeaning("The GOES-East CONUS sector; transparent outside the scan or where quality is poor")
                .legendSummary("Brightness temperature at 10.3 µm, K; cold (bright) is high cloud")
                .build();
        return SourceDescriptor.builder(ID)
                .name("GOES-East ABI imagery")
                .description("GOES-19 ABI Level 2 Cloud and Moisture Imagery, CONUS sector, band 13 (clean longwave"
                        + " infrared), every 5-minute scan.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://registry.opendata.aws/noaa-goes/")
                .attribution("NOAA GOES-R Series, GOES-19 (GOES-East)")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-goes/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(Budget.of(10, 2_000, 2_500))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .product(ir)
                .build();
    }
}
