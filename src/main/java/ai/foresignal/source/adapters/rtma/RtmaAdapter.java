package ai.foresignal.source.adapters.rtma;

import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.LccGrid;
import ai.foresignal.raster.ProductEnablement;
import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.DecoderCapability;
import ai.foresignal.raster.decode.DecoderDependent;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.decode.GzipInputs;
import ai.foresignal.raster.model.FieldSelector;
import ai.foresignal.raster.model.GribIdx;
import ai.foresignal.raster.model.IdxRangePlanner;
import ai.foresignal.raster.model.WindRotation;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
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
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.S3Object;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NOAA RTMA-RU 2.5 km (prompt 106; Weather-Imagery-Design §5.3): the <em>Rapid Update</em> Real-Time
 * Mesoscale Analysis for CONUS, every 15 minutes, an <strong>observation</strong> product on a Lambert
 * conformal grid. One file per analysis,
 * {@code rtma2p5_ru.{yyyyMMdd}/rtma2p5_ru.t{HHmm}z.2dvaranl_ndfd.grb2} + {@code .idx}, read with byte
 * ranges planned from the {@code .idx} (like the 051 models). Products: 2 m temperature, 2 m dew
 * point, 10 m wind (field), gust and visibility. Grid-relative winds (GRIB2 resolution and component
 * flag bit 0x08) are rotated to earth-relative with {@link WindRotation}, as the 051 engine does.
 *
 * <p>Each fetch looks back {@value #CANDIDATES} analysis times ({@code analysisEvery} apart, default
 * 15 min) and commits, oldest first, up to {@value #MAX_PER_FETCH} of those newer than the last one
 * committed whose {@code .idx} exists.
 *
 * <p><strong>Verified 2026-10-16</strong> against a live listing: the bucket holds
 * {@code rtma2p5_ru.20261016/rtma2p5_ru.t1945z.2dvaranl_ndfd.grb2} and its {@code .idx} at :00, :15,
 * :30 and :45; the {@code .idx} lists {@code TMP:2 m above ground}, {@code DPT:2 m above ground},
 * {@code UGRD}/{@code VGRD:10 m above ground}, {@code GUST:10 m above ground} and
 * {@code VIS:surface}, all {@code anl}, about 5.6–6.6 MB per message (about 35 MB per analysis for the
 * five products, 3.4 GB/day at 15 minutes). The {@code d=} field of the {@code .idx} carries only the
 * hour, so the analysis time comes from the key. The hourly {@code rtma2p5.…2dvaranl_ndfd.grb2_wexp}
 * file the first version of this adapter read is the plain RTMA, not RTMA-RU.
 */
@Component
public class RtmaAdapter implements PollingAdapter, DecoderDependent {

    /** The source id. */
    public static final String ID = "rtma-ru";

    /** The bucket. */
    public static final String BUCKET = "noaa-rtma-pds";

    /** Test-only: a path-style base URL for object reads. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.rtma.base-url";

    /** 2 m temperature. */
    public static final String T2M = "rtma-t2m";

    /** 2 m dew point. */
    public static final String DPT = "rtma-dpt";

    /** 10 m wind. */
    public static final String WIND = "rtma-wind";

    /** 10 m gust. */
    public static final String GUST = "rtma-gust";

    /** Surface visibility. */
    public static final String VIS = "rtma-vis";

    /** The products. */
    public static final List<String> PRODUCTS = List.of(T2M, DPT, WIND, GUST, VIS);

    /** The object-key template, with {@code {yyyyMMdd}}, {@code {HH}} and {@code {mm}}. */
    public static final String TEMPLATE = "rtma2p5_ru.{yyyyMMdd}/rtma2p5_ru.t{HH}{mm}z.2dvaranl_ndfd.grb2";

    /** The analysis cadence param. */
    public static final String ANALYSIS_EVERY = "analysisEvery";

    /** The RTMA-RU analysis cadence. */
    public static final Duration DEFAULT_EVERY = Duration.ofMinutes(15);

    /** The analysis times considered per fetch (two hours at 15 minutes). */
    static final int CANDIDATES = 8;

    /** The analyses committed per fetch at most. */
    static final int MAX_PER_FETCH = 4;

    /** The {@code .idx} cap. */
    static final int MAX_IDX_BYTES = 2 * 1024 * 1024;

    private static final String STATE_KEY = "lastCycle";

    /** One product field. */
    private record Field(String productId, String variable, String level, DecodeRequest request) {}

    private static final List<Field> FIELDS = List.of(
            new Field(T2M, "TMP", "2 m above ground",
                    DecodeRequest.grib2(T2M, "TMP", 0, 0, 0, 103, 2.0, "2 m above ground")),
            new Field(DPT, "DPT", "2 m above ground",
                    DecodeRequest.grib2(DPT, "DPT", 0, 0, 6, 103, 2.0, "2 m above ground")),
            new Field(WIND, "UGRD", "10 m above ground",
                    DecodeRequest.grib2(WIND, "UGRD", 0, 2, 2, 103, 10.0, "10 m above ground")),
            new Field(WIND, "VGRD", "10 m above ground",
                    DecodeRequest.grib2(WIND, "VGRD", 0, 2, 3, 103, 10.0, "10 m above ground")),
            new Field(GUST, "GUST", "10 m above ground",
                    DecodeRequest.grib2(GUST, "GUST", 0, 2, 22, 103, 10.0, "10 m above ground")),
            new Field(VIS, "VIS", "surface", DecodeRequest.grib2(VIS, "VIS", 0, 19, 0, 1, null, "surface")));

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final Grib2Decoder decoder;
    private final RasterPaths paths;
    private final String baseUrl;
    private final String profile;

    /** Creates the adapter. */
    @Autowired
    public RtmaAdapter(Grib2Decoder decoder, RasterPaths paths,
            @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${foresignal.profile:small}") String profile) {
        this.decoder = decoder;
        this.paths = paths;
        String chosen = baseUrlOverride == null ? "" : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.profile = profile;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Map<String, Set<DecoderCapability>> decoderRequirements() {
        Set<DecoderCapability> complex = Set.of(DecoderCapability.GRIB2_COMPLEX_PACKING);
        return Map.of(T2M, complex, DPT, complex, WIND, complex, GUST, complex, VIS, complex);
    }

    private URI objectUri(String key) {
        return baseUrl.isEmpty() ? URI.create("https://" + BUCKET + ".s3.amazonaws.com/" + key)
                : URI.create(baseUrl + "/" + BUCKET + "/" + key);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        if (ctx.rasters().probing()) {
            return FetchResult.of(List.of());
        }
        Set<String> enabled = ProductEnablement.enabled(ctx.params(), profile, PRODUCTS, PRODUCTS, PRODUCTS);
        if (enabled.isEmpty()) {
            return FetchResult.of(List.of());
        }
        Duration every = analysisEvery(ctx.params() == null ? null : ctx.params().duration(ANALYSIS_EVERY));
        String last = ctx.state().get(STATE_KEY).orElse(null);
        List<NormalizedRecord> out = new ArrayList<>();
        int processed = 0;
        for (Instant analysis : candidates(ctx.now(), every)) {
            if (processed >= MAX_PER_FETCH) {
                break;
            }
            if (last != null && last.compareTo(cycleKey(analysis)) >= 0) {
                continue;
            }
            String key = ModelKeys.objectKey(TEMPLATE, analysis);
            Optional<GribIdx> idx = readIdx(ctx, key);
            if (idx.isEmpty()) {
                continue;
            }
            out.addAll(process(ctx, enabled, analysis, key, idx.get()));
            last = cycleKey(analysis);
            ctx.state().put(STATE_KEY, last);
            processed++;
        }
        return FetchResult.of(out);
    }

    /**
     * The analysis cadence: the param when it is a whole number of minutes, a multiple of 15 and a
     * divisor of 60, else {@link #DEFAULT_EVERY}.
     */
    static Duration analysisEvery(Duration requested) {
        if (requested == null || requested.isNegative() || requested.toSeconds() % 60 != 0) {
            return DEFAULT_EVERY;
        }
        long minutes = requested.toMinutes();
        return minutes >= 15 && minutes % 15 == 0 && 60 % minutes == 0 ? requested : DEFAULT_EVERY;
    }

    /**
     * The {@value #CANDIDATES} analysis times at or before {@code now} on the {@code every} cadence,
     * oldest first.
     */
    static List<Instant> candidates(Instant now, Duration every) {
        long step = every.toMinutes();
        ZonedDateTime t = ZonedDateTime.ofInstant(now, ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
        ZonedDateTime newest = t.withMinute((int) (t.getMinute() / step * step));
        List<Instant> out = new ArrayList<>();
        for (int k = CANDIDATES - 1; k >= 0; k--) {
            out.add(newest.minusMinutes(step * k).toInstant());
        }
        return out;
    }

    private List<NormalizedRecord> process(FetchContext ctx, Set<String> enabled, Instant cycle, String key,
            GribIdx idx) throws AdapterException {
        RasterSink sink = ctx.rasters();
        Map<Field, GribIdx.Message> wanted = new LinkedHashMap<>();
        for (Field field : FIELDS) {
            if (!enabled.contains(field.productId())) {
                continue;
            }
            Optional<GribIdx.Message> m = idx.find(FieldSelector.of(field.variable(), field.level()));
            if (m.isEmpty()) {
                sink.reportFormatChange(field.productId(),
                        "possible format change: RTMA idx missing " + field.variable() + ":" + field.level());
                continue;
            }
            wanted.put(field, m.get());
        }
        if (wanted.isEmpty()) {
            return List.of();
        }
        List<GribIdx.Message> all = wanted.values().stream().distinct().toList();
        Long size = IdxRangePlanner.needsObjectSize(idx, all) ? objectSize(ctx, key) : null;
        IdxRangePlanner.Plan plan;
        try {
            plan = IdxRangePlanner.plan(idx, all, size);
        } catch (RuntimeException e) {
            throw new AdapterException("RTMA " + key + ": " + e.getMessage(), e);
        }
        Map<GribIdx.Message, Path> files = new LinkedHashMap<>();
        List<NormalizedRecord> out = new ArrayList<>();
        try {
            for (IdxRangePlanner.Range range : plan.ranges()) {
                UpstreamResponse response = ctx.http().getRange(objectUri(key), range.start(), range.end(), range.length());
                byte[] body = response.body();
                for (IdxRangePlanner.Span span : range.spans()) {
                    int from = Math.toIntExact(span.start() - range.start());
                    files.put(span.message(), writeTemp(key, span.message(),
                            Arrays.copyOfRange(body, from, from + Math.toIntExact(span.length()))));
                }
            }
            Map<String, List<Field>> byProduct = new LinkedHashMap<>();
            for (Field field : wanted.keySet()) {
                byProduct.computeIfAbsent(field.productId(), p -> new ArrayList<>()).add(field);
            }
            for (Map.Entry<String, List<Field>> e : byProduct.entrySet()) {
                try {
                    NormalizedRecord record = commit(ctx, cycle, key, e.getKey(), e.getValue(), files);
                    if (record != null) {
                        out.add(record);
                    }
                } catch (SilentFormatChangeException ex) {
                    sink.reportFormatChange(e.getKey(), ex.getMessage());
                } catch (AdapterException ex) {
                    ctx.log().warn("RTMA {}: {} ({})", e.getKey(), ex.getMessage(), key);
                }
            }
        } finally {
            files.values().forEach(GzipInputs::deleteQuietly);
        }
        return out;
    }

    private NormalizedRecord commit(FetchContext ctx, Instant cycle, String key, String productId, List<Field> fields,
            Map<GribIdx.Message, Path> files) throws AdapterException {
        DecodedGrid header;
        try {
            header = decoder.decode(files.get(fields.get(0)), fields.get(0).request().header());
        } catch (RuntimeException e) {
            throw new AdapterException("RTMA " + productId + ": " + e.getMessage(), e);
        }
        GridDefinition grid = header.gridDefinition();
        boolean rotate = fields.size() == 2 && Boolean.TRUE.equals(header.metadata().get("gridRelativeWinds"));
        String variables = fields.stream().map(Field::variable).collect(Collectors.joining(" + "));
        return ctx.rasters().commit(FrameInput.builder(productId)
                .validTime(cycle)
                .grid(grid)
                .file(files.get(fields.get(0)), f -> {
                    List<float[]> bands = new ArrayList<>();
                    for (Field field : fields) {
                        bands.add(decoder.decode(files.get(field), field.request()).floatData());
                    }
                    if (rotate) {
                        rotateToEarth(grid, bands);
                    }
                    return new DecodedBands(bands, null);
                })
                .upstreamKey(key)
                .variable(variables)
                .build());
    }

    /**
     * Rotates grid-relative {@code u}/{@code v} bands to earth-relative in place.
     *
     * @throws RasterFormatException when the grid is not Lambert conformal (nothing else is rotated)
     */
    static void rotateToEarth(GridDefinition grid, List<float[]> bands) {
        if (!(GridDef.of(grid) instanceof LccGrid lcc)) {
            throw new RasterFormatException("RTMA grid-relative winds on a " + grid.type() + " grid cannot be rotated");
        }
        WindRotation.toEarthRelative(lcc, bands.get(0), bands.get(1));
    }

    private Optional<GribIdx> readIdx(FetchContext ctx, String key) throws AdapterException {
        UpstreamResponse response;
        try {
            response = ctx.http().get(objectUri(key + ".idx"));
        } catch (AdapterException e) {
            int status = e.httpStatus().orElse(0);
            if (status == 404 || status == 403) {
                return Optional.empty();
            }
            throw e;
        }
        if (response.truncated() || response.body().length > MAX_IDX_BYTES) {
            return Optional.empty();
        }
        try {
            return Optional.of(GribIdx.parse(new String(response.body(), StandardCharsets.UTF_8)));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static long objectSize(FetchContext ctx, String key) throws AdapterException {
        S3Listing listing = ctx.http().listS3(BUCKET, key, null);
        for (S3Object o : listing.objects()) {
            if (o.key().equals(key)) {
                return o.size();
            }
        }
        throw new AdapterException("RTMA " + key + " is not listed");
    }

    private Path writeTemp(String key, GribIdx.Message message, byte[] bytes) {
        try {
            Path file = Files.createTempFile(paths.tmp(), "rtma-" + message.number() + "-", ".grib2");
            Files.write(file, bytes);
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not stage an RTMA message: " + e.getMessage(), e);
        }
    }

    /** {@code yyyyMMddHHmm}: the analysis time as the adapter state names it. */
    static String cycleKey(Instant cycle) {
        return ZonedDateTime.ofInstant(cycle, ZoneOffset.UTC).format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm", Locale.ROOT));
    }

    private static SourceDescriptor buildDescriptor() {
        ProductSpec.Attribution attribution = new ProductSpec.Attribution(
                "NOAA/NWS Real-Time Mesoscale Analysis (RTMA-RU), via NOAA Open Data Dissemination", null,
                "https://registry.opendata.aws/noaa-rtma-pds/", "US-PD", null, false);
        ProductSpec.Temporal temporal = ProductSpec.Temporal.observation(DEFAULT_EVERY, Duration.ofMinutes(45));
        ProductSpec.Spatial spatial = new ProductSpec.Spatial("CONUS", 8, 0, 7, null);
        ProductSpec.RetentionLadder ladder = new ProductSpec.RetentionLadder(
                List.of(new ProductSpec.RetentionLadder.Rung(Duration.ofHours(24), Duration.ofMinutes(60))),
                Duration.ofHours(48), null);
        String meaning = "2.5 km CONUS analysis; an observation, transparent outside the domain";
        ProductSpec wind = ProductSpec.builder(WIND)
                .title("RTMA 10 m wind").kind(ProductSpec.KIND_FIELD).category("analysis").units("m/s")
                .dtype(RasterDtype.I16).scaleOffset(0.01, 0.0).nodata(-32768.0).bands("u", "v")
                .defaultStyle("wind-speed@1").temporal(temporal).spatial(spatial).retention(ladder)
                .attribution(attribution).coverageMeaning(meaning).legendSummary("10 m wind, m/s")
                .resample(ProductSpec.RESAMPLE_BILINEAR).build();
        ProductSpec t2m = scalar(T2M, "RTMA 2 m temperature", "K", 0.01, 273.15, "temperature@1", temporal, spatial,
                ladder, attribution, meaning, "Air temperature 2 m above ground, K");
        ProductSpec dpt = scalar(DPT, "RTMA 2 m dew point", "K", 0.01, 273.15, "temperature@1", temporal, spatial,
                ladder, attribution, meaning, "Dew point 2 m above ground, K");
        ProductSpec gust = scalar(GUST, "RTMA 10 m gust", "m/s", 0.01, 0.0, "wind-speed@1", temporal, spatial, ladder,
                attribution, meaning, "10 m wind gust, m/s");
        ProductSpec vis = scalar(VIS, "RTMA visibility", "m", 1.0, 0.0, "temperature@1", temporal, spatial, ladder,
                attribution, meaning, "Surface visibility, m");
        return SourceDescriptor.builder(ID)
                .name("NOAA RTMA-RU 2.5 km analysis")
                .description("NOAA Real-Time Mesoscale Analysis Rapid Update (RTMA-RU) for CONUS, every 15"
                        + " minutes: 2 m temperature and dew point, 10 m wind (field, earth-relative), gust and"
                        + " visibility, read with byte ranges from the .idx. An observation product; about 3.4 GB/day"
                        + " for all five products at 15 minutes.")
                .kind(ProductSpec.RECORD_KIND)
                .homepage("https://registry.opendata.aws/noaa-rtma-pds/")
                .attribution("NOAA/NWS Real-Time Mesoscale Analysis (RTMA-RU)")
                .licence("US-PD")
                .termsUrl("https://registry.opendata.aws/noaa-rtma-pds/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(60, 3_000, 3_600))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(ProductSpec.RECORD_KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(8 * 1024 * 1024)
                .param(ParamSpec.optional(ProductEnablement.PRODUCTS_PARAM, ParamType.STRING_LIST,
                        "Which RTMA products to ingest (default all on both profiles)", null))
                .param(ParamSpec.optional(ANALYSIS_EVERY, ParamType.DURATION,
                        "Analysis cadence: 15m (every RTMA-RU analysis), 30m or 60m to thin the download", "15m"))
                .product(wind).product(t2m).product(dpt).product(gust).product(vis)
                .build();
    }

    private static ProductSpec scalar(String id, String title, String units, double scale, double offset, String style,
            ProductSpec.Temporal temporal, ProductSpec.Spatial spatial, ProductSpec.RetentionLadder ladder,
            ProductSpec.Attribution attribution, String meaning, String legend) {
        return ProductSpec.builder(id).title(title).kind(ProductSpec.KIND_RASTER).category("analysis").units(units)
                .dtype(RasterDtype.I16).scaleOffset(scale, offset).nodata(-32768.0).defaultStyle(style)
                .temporal(temporal).spatial(spatial).retention(ladder).attribution(attribution).coverageMeaning(meaning)
                .legendSummary(legend).resample(ProductSpec.RESAMPLE_BILINEAR).build();
    }

    /** Object-key helpers mirroring {@code ModelCycleSelector.objectKey}, plus a {@code {mm}} token. */
    static final class ModelKeys {
        private ModelKeys() {}

        static String objectKey(String template, Instant cycle) {
            ZonedDateTime t = ZonedDateTime.ofInstant(cycle, ZoneOffset.UTC);
            return template.replace("{yyyyMMdd}", t.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd",
                    Locale.ROOT))).replace("{HH}", String.format(Locale.ROOT, "%02d", t.getHour()))
                    .replace("{mm}", String.format(Locale.ROOT, "%02d", t.getMinute()));
        }
    }

    /** A decode function (kept for symmetry). */
    interface Decode extends Function<Path, DecodedBands> {}
}
