package ai.foresignal.raster.model;

import ai.foresignal.raster.ForecastRunCadence;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.LccGrid;
import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.decode.GzipInputs;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.S3Object;
import ai.foresignal.source.spi.SilentFormatChangeException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Forecast model ingest through budgeted byte-range reads (prompt 051; Weather-Imagery-Design §1.4,
 * §2.5, §5.1): the engine behind the {@code gfs} and {@code hrrr} sources.
 *
 * <p>Each fetch:
 * <ol>
 *   <li>takes the two newest candidate cycles from the clock ({@link ModelCycleSelector}, run cadence
 *       and availability lag from the source's params) and works on those not yet complete, oldest
 *       first;
 *   <li>per step, in order, reads the step's {@code .idx} ({@link GribIdx}); a missing one (403/404)
 *       means the step is not published yet and ends that cycle for this fetch. When the first step's
 *       {@code .idx} is still missing {@code availabilityLag + 2 h} after the cycle, health degrades;
 *   <li>matches every product's fields by variable and level ({@link FieldSelector}), never by
 *       message number. A missing field of a required product writes no frame for the step and
 *       degrades health ("possible format change: GFS idx missing UGRD:10 m above ground"); a missing
 *       field of an optional product drops only that product, with the same health reason;
 *   <li>plans coalesced ranges ({@link IdxRangePlanner}; the last message ends at the object size
 *       from the listing), reads each through the budgeted {@code getRange} (charged in advance), and
 *       writes each message to its own file under {@code rasters/tmp};
 *   <li>commits one frame per product through {@code ctx.rasters()} with a decoder that runs on the
 *       raster decode pool: it decodes every field, checks the limits (at most
 *       {@value #MAX_POINTS} points, every value finite), applies the product's factor, and rotates
 *       grid-relative winds to earth-relative ({@link WindRotation});
 *   <li>marks a product's run complete once every one of its configured steps has a frame: the run
 *       gets its one {@code raster-frame} record version.
 * </ol>
 * At most {@value #MAX_STEPS_PER_FETCH} steps are read per fetch, so one check stays well inside the
 * per-minute budget; a run completes over a few checks.
 *
 * <p>Adapter state (per source): {@code done:{cycle}} (steps handled, whatever the outcome),
 * {@code steps:{productId}:{cycle}} (steps with a frame) and {@code complete:{productId}} (the newest
 * complete cycle). Keys of cycles that left the candidate window are removed.
 */
@Component
public class ModelRunIngest implements ForecastRunCadence {

    /** The most steps read per fetch. */
    public static final int MAX_STEPS_PER_FETCH = 10;

    /** The largest grid accepted, points (HRRR's 1799 × 1059 = 1.9 M fits). */
    public static final int MAX_POINTS = 2_000_000;

    /** The {@code .idx} response cap, bytes. */
    public static final int MAX_IDX_BYTES = 2 * 1024 * 1024;

    /** How long past {@code availabilityLag} a missing first {@code .idx} is tolerated. */
    public static final Duration MISSING_IDX_GRACE = Duration.ofHours(2);

    /** Candidate cycles considered per fetch. */
    public static final int CANDIDATES = 2;

    /** The path template param. */
    public static final String PATH_TEMPLATE = "pathTemplate";

    /** The step list param. */
    public static final String STEPS = "steps";

    /** The run cadence param. */
    public static final String RUN_EVERY = "runEvery";

    /** The availability lag param. */
    public static final String AVAILABILITY_LAG = "availabilityLag";

    static final String DONE_PREFIX = "done:";
    static final String STEPS_PREFIX = "steps:";
    static final String COMPLETE_PREFIX = "complete:";

    private final Grib2Decoder decoder;
    private final RasterPaths paths;
    private final Map<String, Duration> cadences = new ConcurrentHashMap<>();

    /** Creates the engine. */
    public ModelRunIngest(Grib2Decoder decoder, RasterPaths paths) {
        this.decoder = decoder;
        this.paths = paths;
    }

    /**
     * Records a source's default run cadence before its first fetch (adapters call it at construction).
     *
     * @param sourceId the source
     * @param runEvery its default run cadence
     */
    public void registerDefault(String sourceId, Duration runEvery) {
        cadences.putIfAbsent(sourceId, runEvery);
    }

    @Override
    public Optional<Duration> runCadence(String sourceId) {
        return Optional.ofNullable(cadences.get(sourceId));
    }

    /**
     * The effective settings of one fetch.
     *
     * @param template the object-key template
     * @param steps the configured steps, ascending
     * @param runEvery the run cadence
     * @param availabilityLag the availability lag
     */
    public record Settings(String template, List<Integer> steps, Duration runEvery, Duration availabilityLag) {}

    /**
     * The settings from the source's params, falling back to the definition's defaults.
     *
     * @throws AdapterException when a param is unusable (a bad step list or cadence)
     */
    public static Settings settings(ModelDefinition def, ValidatedParams params) throws AdapterException {
        String template = params == null ? null : params.optionalString(PATH_TEMPLATE).filter(s -> !s.isBlank())
                .orElse(null);
        List<String> stepItems = params == null ? List.of() : params.stringList(STEPS);
        Duration runEvery = params == null ? null : params.duration(RUN_EVERY);
        Duration lag = params == null ? null : params.duration(AVAILABILITY_LAG);
        try {
            Settings s = new Settings(template == null ? def.defaultTemplate() : template,
                    ForecastSteps.parse(stepItems.isEmpty() ? def.defaultSteps() : stepItems),
                    runEvery == null ? def.defaultRunEvery() : runEvery, lag == null ? def.defaultLag() : lag);
            ModelCycleSelector.requireCadence(s.runEvery());
            if (!s.template().contains("{yyyyMMdd}") || !s.template().contains("{HH}")
                    || !(s.template().contains("{FFF}") || s.template().contains("{FF}"))) {
                throw new IllegalArgumentException("pathTemplate must contain {yyyyMMdd}, {HH} and {FFF} or {FF}");
            }
            return s;
        } catch (IllegalArgumentException e) {
            throw new AdapterException(def.label() + ": " + e.getMessage(), e);
        }
    }

    /**
     * One fetch of a model source.
     *
     * @param ctx the fetch context
     * @param def the model
     * @param objectUri the URL of an object key
     * @return the records: run-in-progress upserts and completed runs
     * @throws AdapterException when nothing could be committed and a request or decode failed
     */
    public FetchResult fetch(FetchContext ctx, ModelDefinition def, Function<String, URI> objectUri)
            throws AdapterException {
        Settings settings = settings(def, ctx.params());
        cadences.put(def.sourceId(), settings.runEvery());
        if (ctx.rasters().probing()) {
            return probe(ctx, def, settings, objectUri);
        }
        Instant now = ctx.now();
        List<Instant> candidates = ModelCycleSelector.candidates(now, settings.runEvery(), def.cycleOffset(),
                settings.availabilityLag(), CANDIDATES);
        cleanState(ctx.state(), def, candidates, settings.runEvery());
        List<NormalizedRecord> records = new ArrayList<>();
        int[] budget = {MAX_STEPS_PER_FETCH};
        try {
            List<Instant> oldestFirst = new ArrayList<>(candidates);
            java.util.Collections.reverse(oldestFirst);
            for (Instant cycle : oldestFirst) {
                if (budget[0] <= 0) {
                    break;
                }
                if (allComplete(ctx.state(), def, cycle)) {
                    continue;
                }
                records.addAll(fetchCycle(ctx, def, settings, cycle, objectUri, budget, cycle.equals(candidates.get(0))));
            }
        } catch (AdapterException | RuntimeException e) {
            if (records.isEmpty()) {
                throw e;
            }
            ctx.log().warn("{}: committed {} record(s), then failed: {}", def.label(), records.size(), e.getMessage());
        }
        return FetchResult.of(records);
    }

    /** True when every product has completed {@code cycle} or a newer one. */
    static boolean allComplete(AdapterStateStore state, ModelDefinition def, Instant cycle) {
        for (ModelDefinition.Product p : def.products()) {
            Instant done = state.get(COMPLETE_PREFIX + p.productId()).map(ModelCycleSelector::parseCycleKey).orElse(null);
            if (done == null || done.isBefore(cycle)) {
                return false;
            }
        }
        return true;
    }

    private List<NormalizedRecord> fetchCycle(FetchContext ctx, ModelDefinition def, Settings settings, Instant cycle,
            Function<String, URI> objectUri, int[] budget, boolean newest) throws AdapterException {
        AdapterStateStore state = ctx.state();
        String cycleKey = ModelCycleSelector.cycleKey(cycle);
        Set<Integer> done = readSteps(state, DONE_PREFIX + cycleKey);
        List<NormalizedRecord> out = new ArrayList<>();
        for (int step : settings.steps()) {
            if (done.contains(step)) {
                continue;
            }
            if (budget[0] <= 0) {
                break;
            }
            String key = ModelCycleSelector.objectKey(settings.template(), cycle, step);
            Optional<GribIdx> idx = readIdx(ctx, def, objectUri.apply(key + ".idx"));
            if (idx.isEmpty()) {
                if (newest && step == settings.steps().get(0)) {
                    Instant expected = cycle.plus(settings.availabilityLag()).plus(MISSING_IDX_GRACE);
                    if (ctx.now().isAfter(expected)) {
                        state.degrade(def.label() + ": no .idx for run " + cycleKey + " f"
                                + String.format(Locale.ROOT, "%03d", step) + " "
                                + Duration.between(cycle.plus(settings.availabilityLag()), ctx.now()).toMinutes()
                                + " min after its expected availability (" + key + ".idx)");
                    }
                }
                break;
            }
            if (newest && step == settings.steps().get(0)) {
                state.clearDegraded();
            }
            budget[0]--;
            out.addAll(fetchStep(ctx, def, settings, cycle, step, key, idx.get(), objectUri));
            done.add(step);
            writeSteps(state, DONE_PREFIX + cycleKey, done);
            out.addAll(completeRuns(ctx, def, settings, cycle));
        }
        return out;
    }

    /** The {@code .idx} at {@code uri}, or empty when it is not published (403/404). */
    Optional<GribIdx> readIdx(FetchContext ctx, ModelDefinition def, URI uri) throws AdapterException {
        UpstreamResponse response;
        try {
            response = ctx.http().get(uri);
        } catch (AdapterException e) {
            int status = e.httpStatus().orElse(0);
            if (status == 404 || status == 403) {
                return Optional.empty();
            }
            throw e;
        }
        if (response.truncated() || response.body().length > MAX_IDX_BYTES) {
            throw new AdapterException(def.label() + " .idx over " + MAX_IDX_BYTES + " bytes: " + uri.getPath());
        }
        try {
            return Optional.of(GribIdx.parse(new String(response.body(), StandardCharsets.UTF_8)));
        } catch (RasterFormatException e) {
            throw new AdapterException(def.label() + " " + uri.getPath() + ": " + e.getMessage(), e);
        }
    }

    /**
     * One step: match, read and commit every product still missing it.
     *
     * @return the run-in-progress records
     */
    List<NormalizedRecord> fetchStep(FetchContext ctx, ModelDefinition def, Settings settings, Instant cycle, int step,
            String key, GribIdx idx, Function<String, URI> objectUri) throws AdapterException {
        RasterSink sink = ctx.rasters();
        AdapterStateStore state = ctx.state();
        String cycleKey = ModelCycleSelector.cycleKey(cycle);
        Map<ModelDefinition.Product, List<GribIdx.Message>> wanted = new LinkedHashMap<>();
        for (ModelDefinition.Product product : def.products()) {
            if (!product.steps().test(step) || sink.disabled(product.productId())
                    || readSteps(state, stepsKey(product, cycleKey)).contains(step)) {
                continue;
            }
            List<GribIdx.Message> messages = new ArrayList<>();
            String missing = null;
            for (ModelDefinition.Component c : product.components()) {
                FieldSelector selector = c.selector(step);
                Optional<GribIdx.Message> m = idx.find(selector);
                if (m.isEmpty()) {
                    missing = selector.label();
                    break;
                }
                messages.add(m.get());
            }
            if (missing != null) {
                String reason = "possible format change: " + def.label() + " idx missing " + missing;
                sink.reportFormatChange(product.productId(), reason);
                ctx.log().warn("{}: {} ({} f{})", def.label(), reason, cycleKey, step);
                if (product.required()) {
                    return List.of();
                }
                continue;
            }
            wanted.put(product, messages);
        }
        if (wanted.isEmpty()) {
            return List.of();
        }
        List<GribIdx.Message> all = wanted.values().stream().flatMap(List::stream).distinct().toList();
        Long size = IdxRangePlanner.needsObjectSize(idx, all) ? objectSize(ctx, def, key) : null;
        IdxRangePlanner.Plan plan;
        try {
            plan = IdxRangePlanner.plan(idx, all, size);
        } catch (RasterFormatException | IllegalArgumentException e) {
            throw new AdapterException(def.label() + " " + key + ": " + e.getMessage(), e);
        }
        Map<GribIdx.Message, Path> files = new LinkedHashMap<>();
        List<NormalizedRecord> out = new ArrayList<>();
        try {
            URI uri = objectUri.apply(key);
            for (IdxRangePlanner.Range range : plan.ranges()) {
                UpstreamResponse response = ctx.http().getRange(uri, range.start(), range.end(), range.length());
                byte[] body = response.body();
                if (body.length < range.length()) {
                    throw new AdapterException(def.label() + " " + key + ": range " + range.start() + "-" + range.end()
                            + " returned " + body.length + " bytes");
                }
                for (IdxRangePlanner.Span span : range.spans()) {
                    int from = Math.toIntExact(span.start() - range.start());
                    files.put(span.message(), writeTemp(def, cycleKey, step, span.message(),
                            Arrays.copyOfRange(body, from, from + Math.toIntExact(span.length()))));
                }
            }
            AdapterException failure = null;
            for (Map.Entry<ModelDefinition.Product, List<GribIdx.Message>> e : wanted.entrySet()) {
                ModelDefinition.Product product = e.getKey();
                List<Path> paths = e.getValue().stream().map(files::get).toList();
                try {
                    NormalizedRecord record = commit(ctx, def, product, cycle, step, key, e.getValue(), paths);
                    if (record != null) {
                        out.add(record);
                    }
                    Set<Integer> steps = readSteps(state, stepsKey(product, cycleKey));
                    steps.add(step);
                    writeSteps(state, stepsKey(product, cycleKey), steps);
                } catch (SilentFormatChangeException ex) {
                    sink.reportFormatChange(product.productId(), ex.getMessage());
                    ctx.log().warn("{}: {} ({} f{})", def.label(), ex.getMessage(), cycleKey, step);
                } catch (AdapterException ex) {
                    ctx.log().warn("{} {}: {} ({} f{})", def.label(), product.productId(), ex.getMessage(), cycleKey,
                            step);
                    failure = failure == null ? ex : failure;
                }
            }
            if (failure != null && out.isEmpty()) {
                throw failure;
            }
        } finally {
            files.values().forEach(GzipInputs::deleteQuietly);
        }
        return out;
    }

    /** Commits one product's frame for one step. */
    private NormalizedRecord commit(FetchContext ctx, ModelDefinition def, ModelDefinition.Product product,
            Instant cycle, int step, String key, List<GribIdx.Message> messages, List<Path> files)
            throws AdapterException {
        DecodedGrid header;
        try {
            header = decoder.decode(files.get(0), product.components().get(0).request().header());
        } catch (RasterFormatException e) {
            throw new AdapterException(def.label() + " " + product.productId() + ": " + e.getMessage(), e);
        }
        checkPoints(def, header);
        GridDefinition grid = header.gridDefinition();
        boolean rotate = product.wind() && Boolean.TRUE.equals(header.metadata().get("gridRelativeWinds"));
        String messageNumbers = messages.stream().map(m -> Integer.toString(m.number())).collect(Collectors.joining(","));
        String variables = messages.stream().map(GribIdx.Message::label).collect(Collectors.joining(" + "));
        return ctx.rasters().commit(FrameInput.builder(product.productId())
                .forecast(cycle, step)
                .grid(grid)
                .file(files.get(0), first -> decodeBands(def, product, grid, files, rotate))
                .upstreamKey(key + "#" + messageNumbers)
                .variable(variables)
                .build());
    }

    /**
     * Decodes a product's fields into physical bands (on the raster decode pool).
     *
     * @throws SilentFormatChangeException when a field is not in its message
     * @throws RasterFormatException when the grids differ, a limit is exceeded or a value is not finite
     */
    DecodedBands decodeBands(ModelDefinition def, ModelDefinition.Product product, GridDefinition grid, List<Path> files,
            boolean rotate) {
        List<float[]> bands = new ArrayList<>();
        for (int k = 0; k < product.components().size(); k++) {
            ModelDefinition.Component c = product.components().get(k);
            DecodedGrid g = decoder.decode(files.get(k), c.request());
            if (g.nx() != grid.nx() || g.ny() != grid.ny()) {
                throw new RasterFormatException(def.label() + " " + c.variable() + " grid " + g.nx() + "x" + g.ny()
                        + " differs from " + grid.nx() + "x" + grid.ny());
            }
            float[] values = g.floatData();
            for (int i = 0; i < values.length; i++) {
                float v = values[i];
                if (!Float.isFinite(v)) {
                    throw new RasterFormatException(def.label() + " " + c.variable() + ":" + c.level()
                            + " holds a non-finite value at cell " + i);
                }
                if (product.factor() != 1.0) {
                    values[i] = (float) (v * product.factor());
                }
            }
            bands.add(values);
        }
        if (rotate) {
            if (!(GridDef.of(grid) instanceof LccGrid lcc)) {
                throw new RasterFormatException(def.label() + " grid-relative winds on a " + grid.type()
                        + " grid cannot be rotated");
            }
            WindRotation.toEarthRelative(lcc, bands.get(0), bands.get(1));
        }
        return new DecodedBands(bands, null);
    }

    private static void checkPoints(ModelDefinition def, DecodedGrid header) {
        if ((long) header.nx() * header.ny() > MAX_POINTS) {
            throw new RasterFormatException(def.label() + " grid " + header.nx() + "x" + header.ny() + " exceeds "
                    + MAX_POINTS + " points");
        }
    }

    /** Marks every product whose steps of {@code cycle} all have frames complete. */
    private List<NormalizedRecord> completeRuns(FetchContext ctx, ModelDefinition def, Settings settings, Instant cycle)
            throws AdapterException {
        AdapterStateStore state = ctx.state();
        String cycleKey = ModelCycleSelector.cycleKey(cycle);
        List<NormalizedRecord> out = new ArrayList<>();
        for (ModelDefinition.Product product : def.products()) {
            String completeKey = COMPLETE_PREFIX + product.productId();
            Instant newest = state.get(completeKey).map(ModelCycleSelector::parseCycleKey).orElse(null);
            if (newest != null && !newest.isBefore(cycle)) {
                continue;
            }
            List<Integer> wanted = product.stepsOf(settings.steps());
            if (wanted.isEmpty() || !readSteps(state, stepsKey(product, cycleKey)).containsAll(wanted)) {
                continue;
            }
            out.addAll(ctx.rasters().completeRun(product.productId(), cycle));
            state.put(completeKey, cycleKey);
            ctx.log().info("{}: run {} of {} complete ({} steps)", def.label(), cycleKey, product.productId(),
                    wanted.size());
        }
        return out;
    }

    /** The object's size from a listing of its exact key. */
    private static long objectSize(FetchContext ctx, ModelDefinition def, String key) throws AdapterException {
        S3Listing listing = ctx.http().listS3(def.bucket(), key, null);
        for (S3Object o : listing.objects()) {
            if (o.key().equals(key)) {
                return o.size();
            }
        }
        throw new AdapterException(def.label() + " " + key + " is not listed; its last message has no end");
    }

    private Path writeTemp(ModelDefinition def, String cycleKey, int step, GribIdx.Message message, byte[] bytes) {
        try {
            Path file = Files.createTempFile(paths.tmp(), def.sourceId() + "-" + cycleKey + "-f" + step + "-m"
                    + message.number() + "-", ".grib2");
            Files.write(file, bytes);
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not stage a " + def.label() + " message: " + e.getMessage(), e);
        }
    }

    /**
     * A probe: the newest published {@code .idx}, one range-read message of the probe product, its
     * header decoded; returns the grid summary and writes nothing (adapter state included).
     */
    FetchResult probe(FetchContext ctx, ModelDefinition def, Settings settings, Function<String, URI> objectUri)
            throws AdapterException {
        ModelDefinition.Product product = def.product(def.probeProduct());
        int step = product.stepsOf(settings.steps()).stream().findFirst().orElse(settings.steps().get(0));
        for (Instant cycle : ModelCycleSelector.candidates(ctx.now(), settings.runEvery(), def.cycleOffset(),
                settings.availabilityLag(), CANDIDATES + 1)) {
            String key = ModelCycleSelector.objectKey(settings.template(), cycle, step);
            Optional<GribIdx> idx = readIdx(ctx, def, objectUri.apply(key + ".idx"));
            if (idx.isEmpty()) {
                continue;
            }
            ModelDefinition.Component c = product.components().get(0);
            GribIdx.Message message = idx.get().find(c.selector(step)).orElseThrow(() -> new AdapterException(
                    "possible format change: " + def.label() + " idx missing " + c.selector(step).label()));
            Long size = IdxRangePlanner.needsObjectSize(idx.get(), List.of(message)) ? objectSize(ctx, def, key) : null;
            IdxRangePlanner.Range range;
            try {
                range = IdxRangePlanner.plan(idx.get(), List.of(message), size).ranges().get(0);
            } catch (RasterFormatException | IllegalArgumentException e) {
                throw new AdapterException(def.label() + " " + key + ": " + e.getMessage(), e);
            }
            byte[] body = ctx.http().getRange(objectUri.apply(key), range.start(), range.end(), range.length()).body();
            Path file = writeTemp(def, ModelCycleSelector.cycleKey(cycle), step, message, body);
            try {
                DecodedGrid header;
                try {
                    header = decoder.decode(file, c.request().header());
                } catch (RasterFormatException e) {
                    throw new AdapterException(def.label() + " probe: " + e.getMessage(), e);
                }
                NormalizedRecord preview = ctx.rasters().commit(FrameInput.builder(product.productId())
                        .forecast(cycle, step)
                        .grid(header.gridDefinition())
                        .file(file, f -> {
                            throw new IllegalStateException("A probe never decodes samples");
                        })
                        .upstreamKey(key + "#" + message.number())
                        .variable(message.label())
                        .build());
                return FetchResult.of(preview == null ? List.of() : List.of(preview));
            } finally {
                GzipInputs.deleteQuietly(file);
            }
        }
        return FetchResult.of(List.of());
    }

    /**
     * Removes the state of cycles that left the candidate window: the day of cycles before the oldest
     * candidate, at the run cadence. A cycle's step keys go with its {@code done} key.
     */
    static void cleanState(AdapterStateStore state, ModelDefinition def, List<Instant> candidates, Duration runEvery) {
        Instant oldest = candidates.get(candidates.size() - 1);
        long count = Math.max(1, Duration.ofHours(24).dividedBy(runEvery));
        for (long k = 1; k <= count; k++) {
            String key = ModelCycleSelector.cycleKey(oldest.minus(runEvery.multipliedBy(k)));
            if (state.get(DONE_PREFIX + key).isEmpty()) {
                continue;
            }
            state.remove(DONE_PREFIX + key);
            for (ModelDefinition.Product p : def.products()) {
                if (state.get(stepsKey(p, key)).isPresent()) {
                    state.remove(stepsKey(p, key));
                }
            }
        }
    }

    static String stepsKey(ModelDefinition.Product product, String cycleKey) {
        return STEPS_PREFIX + product.productId() + ":" + cycleKey;
    }

    /** A comma-separated step set from adapter state. */
    static Set<Integer> readSteps(AdapterStateStore state, String key) {
        Set<Integer> out = new TreeSet<>();
        String value = state.get(key).orElse("");
        for (String part : value.split(",")) {
            String p = part.strip();
            if (!p.isEmpty()) {
                try {
                    out.add(Integer.parseInt(p));
                } catch (NumberFormatException ignored) {
                    // A corrupted entry is dropped; the step is fetched again.
                }
            }
        }
        return out;
    }

    private static void writeSteps(AdapterStateStore state, String key, Set<Integer> steps) {
        String value = new LinkedHashSet<>(new TreeSet<>(steps)).stream().map(String::valueOf)
                .collect(Collectors.joining(","));
        if (!value.equals(state.get(key).orElse(null))) {
            state.put(key, value);
        }
    }
}
