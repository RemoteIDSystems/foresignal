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
 * ECMWF IFS forecast ingest (prompt 105; Weather-Imagery-Design §5.3): the engine behind the
 * {@code ecmwf-ifs} source, mirroring {@link ModelRunIngest} but reading the ECMWF
 * {@code .index} inventory ({@link EcmwfIndex}) and planning ranges from its explicit
 * {@code _offset}/{@code _length} ({@link EcmwfRunPlanner}).
 *
 * <p>Each fetch takes the two newest candidate cycles (00z and 12z by default), works on those not
 * yet complete oldest first, and per step matches every product's fields by {@code param},
 * {@code levtype} and {@code levelist}. A product with {@code differenceHours > 0} is the difference
 * of the field at the step and at {@code step − differenceHours} (ECMWF's {@code tp} is a run total,
 * so {@code ifs-precip-3h} and {@code ifs-precip-24h} are differences). Messages are staged to
 * {@code rasters/tmp} and decoded on the decode pool with {@link Grib2Decoder}.
 *
 * <p>Adapter state mirrors the 051 engine: {@code done:{cycle}}, {@code steps:{productId}:{cycle}}
 * and {@code complete:{productId}}, plus {@code tries:{cycle}} (prompt 105 post-review): a step is
 * done only when every product that has it committed it; otherwise it is retried on later fetches, at
 * most {@value #MAX_STEP_ATTEMPTS} times. Difference products are clamped at zero.
 */
@Component
public class EcmwfRunIngest implements ForecastRunCadence {

    /** The most steps read per fetch. */
    public static final int MAX_STEPS_PER_FETCH = 10;

    /** The largest grid accepted, points (1440 × 721 = 1,038,240 fits). */
    public static final int MAX_POINTS = 2_000_000;

    /** The {@code .index} response cap, bytes. */
    public static final int MAX_INDEX_BYTES = 2 * 1024 * 1024;

    /** How long past {@code availabilityLag} a missing first {@code .index} is tolerated. */
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

    /** The resolution segment ({@code {res}}) param. */
    public static final String RESOLUTION = "resolution";

    static final String DONE_PREFIX = "done:";
    static final String STEPS_PREFIX = "steps:";
    static final String COMPLETE_PREFIX = "complete:";
    static final String ATTEMPTS_PREFIX = "tries:";

    /** How many fetches may retry a step whose products did not all commit before it is skipped. */
    public static final int MAX_STEP_ATTEMPTS = 3;

    private final Grib2Decoder decoder;
    private final RasterPaths paths;
    private final Map<String, Duration> cadences = new ConcurrentHashMap<>();

    /** Creates the engine. */
    public EcmwfRunIngest(Grib2Decoder decoder, RasterPaths paths) {
        this.decoder = decoder;
        this.paths = paths;
    }

    /** Records a source's default run cadence before its first fetch. */
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

    /** The settings from the source's params, falling back to the definition's defaults. */
    public static Settings settings(EcmwfDefinition def, ValidatedParams params) throws AdapterException {
        String template = params == null ? null : params.optionalString(PATH_TEMPLATE).filter(s -> !s.isBlank())
                .orElse(null);
        List<String> stepItems = params == null ? List.of() : params.stringList(STEPS);
        Duration runEvery = params == null ? null : params.duration(RUN_EVERY);
        Duration lag = params == null ? null : params.duration(AVAILABILITY_LAG);
        String res = params == null ? null : params.optionalString(RESOLUTION).filter(s -> !s.isBlank()).orElse(null);
        try {
            String chosenRes = res == null ? def.defaultResolution() : res;
            String chosenTemplate = (template == null ? def.defaultTemplate() : template).replace("{res}", chosenRes);
            Settings s = new Settings(chosenTemplate,
                    ForecastSteps.parse(stepItems.isEmpty() ? def.defaultSteps() : stepItems),
                    runEvery == null ? def.defaultRunEvery() : runEvery, lag == null ? def.defaultLag() : lag);
            ModelCycleSelector.requireCadence(s.runEvery());
            if (!s.template().contains("{yyyyMMdd}") || !s.template().contains("{HH}")
                    || !(s.template().contains("{FFF}") || s.template().contains("{FF}")
                            || s.template().contains("{step}"))) {
                throw new IllegalArgumentException(
                        "pathTemplate must contain {yyyyMMdd}, {HH} and {FFF}, {FF} or {step}");
            }
            return s;
        } catch (IllegalArgumentException e) {
            throw new AdapterException(def.label() + ": " + e.getMessage(), e);
        }
    }

    /** One fetch of the ECMWF source. */
    public FetchResult fetch(FetchContext ctx, EcmwfDefinition def, Function<String, URI> objectUri)
            throws AdapterException {
        Settings settings = settings(def, ctx.params());
        cadences.put(def.sourceId(), settings.runEvery());
        if (ctx.rasters().probing()) {
            return probe(ctx, def, settings, objectUri);
        }
        Instant now = ctx.now();
        List<Instant> candidates = ModelCycleSelector.candidates(now, settings.runEvery(), settings.availabilityLag(),
                CANDIDATES);
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
    static boolean allComplete(AdapterStateStore state, EcmwfDefinition def, Instant cycle) {
        for (EcmwfDefinition.Product p : def.products()) {
            Instant done = state.get(COMPLETE_PREFIX + p.productId()).map(ModelCycleSelector::parseCycleKey).orElse(null);
            if (done == null || done.isBefore(cycle)) {
                return false;
            }
        }
        return true;
    }

    private List<NormalizedRecord> fetchCycle(FetchContext ctx, EcmwfDefinition def, Settings settings, Instant cycle,
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
            Optional<EcmwfIndex> index = readIndex(ctx, def, objectUri.apply(key + ".index"));
            if (index.isEmpty()) {
                if (newest && step == settings.steps().get(0)) {
                    Instant expected = cycle.plus(settings.availabilityLag()).plus(MISSING_IDX_GRACE);
                    if (ctx.now().isAfter(expected)) {
                        state.degrade(def.label() + ": no .index for run " + cycleKey + " f" + step + " "
                                + Duration.between(cycle.plus(settings.availabilityLag()), ctx.now()).toMinutes()
                                + " min after its expected availability (" + key + ".index)");
                    }
                }
                break;
            }
            if (newest && step == settings.steps().get(0)) {
                state.clearDegraded();
            }
            if (readAttempts(state, cycleKey).getOrDefault(step, 0) >= MAX_STEP_ATTEMPTS) {
                // Earlier attempts failed outright (an exception before the step could be judged).
                done.add(step);
                clearAttempts(state, cycleKey, step);
                writeSteps(state, DONE_PREFIX + cycleKey, done);
                ctx.log().warn("{}: run {} f{} failed {} times; moving on", def.label(), cycleKey, step,
                        MAX_STEP_ATTEMPTS);
                continue;
            }
            budget[0]--;
            int attempt = recordAttempt(state, cycleKey, step);
            out.addAll(fetchStep(ctx, def, settings, cycle, step, key, index.get(), objectUri));
            if (stepComplete(state, def, ctx.rasters(), cycleKey, step)) {
                done.add(step);
                clearAttempts(state, cycleKey, step);
            } else if (attempt >= MAX_STEP_ATTEMPTS) {
                // Give up on this step after repeated partial results, so one permanently missing field
                // cannot starve the rest of the run; the run is then never marked complete for the
                // products that miss it, which is the honest outcome.
                done.add(step);
                clearAttempts(state, cycleKey, step);
                ctx.log().warn("{}: run {} f{} still incomplete after {} attempts; moving on", def.label(), cycleKey,
                        step, attempt);
            }
            writeSteps(state, DONE_PREFIX + cycleKey, done);
            out.addAll(completeRuns(ctx, def, settings, cycle));
        }
        return out;
    }

    /**
     * True when every product that has {@code step} (and is not disabled) has committed it. A step is marked
     * done only then (prompt 105 post-review): a missing reference message, a failed commit or a missing
     * field leaves the step to be retried on the next fetch.
     */
    static boolean stepComplete(AdapterStateStore state, EcmwfDefinition def, RasterSink sink, String cycleKey,
            int step) {
        for (EcmwfDefinition.Product product : def.products()) {
            if (!product.steps().test(step) || sink.disabled(product.productId())) {
                continue;
            }
            if (!readSteps(state, stepsKey(product, cycleKey)).contains(step)) {
                return false;
            }
        }
        return true;
    }

    /** Counts one more attempt at a step and returns the count. */
    static int recordAttempt(AdapterStateStore state, String cycleKey, int step) {
        Map<Integer, Integer> attempts = readAttempts(state, cycleKey);
        int n = attempts.getOrDefault(step, 0) + 1;
        attempts.put(step, n);
        writeAttempts(state, cycleKey, attempts);
        return n;
    }

    private static void clearAttempts(AdapterStateStore state, String cycleKey, int step) {
        Map<Integer, Integer> attempts = readAttempts(state, cycleKey);
        if (attempts.remove(step) != null) {
            writeAttempts(state, cycleKey, attempts);
        }
    }

    /** The {@code step:count} attempt map of a cycle. */
    static Map<Integer, Integer> readAttempts(AdapterStateStore state, String cycleKey) {
        Map<Integer, Integer> out = new java.util.TreeMap<>();
        for (String part : state.get(ATTEMPTS_PREFIX + cycleKey).orElse("").split(",")) {
            int colon = part.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            try {
                out.put(Integer.parseInt(part.substring(0, colon).strip()),
                        Integer.parseInt(part.substring(colon + 1).strip()));
            } catch (NumberFormatException ignored) {
                // A corrupted entry is dropped; the step counts from zero again.
            }
        }
        return out;
    }

    private static void writeAttempts(AdapterStateStore state, String cycleKey, Map<Integer, Integer> attempts) {
        String key = ATTEMPTS_PREFIX + cycleKey;
        if (attempts.isEmpty()) {
            if (state.get(key).isPresent()) {
                state.remove(key);
            }
            return;
        }
        state.put(key, attempts.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(",")));
    }

    /** The {@code .index} at {@code uri}, or empty when it is not published (403/404). */
    Optional<EcmwfIndex> readIndex(FetchContext ctx, EcmwfDefinition def, URI uri) throws AdapterException {
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
        if (response.truncated() || response.body().length > MAX_INDEX_BYTES) {
            throw new AdapterException(def.label() + " .index over " + MAX_INDEX_BYTES + " bytes: " + uri.getPath());
        }
        try {
            return Optional.of(EcmwfIndex.parse(new String(response.body(), StandardCharsets.UTF_8)));
        } catch (RasterFormatException e) {
            throw new AdapterException(def.label() + " " + uri.getPath() + ": " + e.getMessage(), e);
        }
    }

    /** One reference message of a difference product, read from an earlier step's file. */
    private record Reference(int step, EcmwfDefinition.Component component) {}

    /** One step: match, read and commit every product still missing it. */
    List<NormalizedRecord> fetchStep(FetchContext ctx, EcmwfDefinition def, Settings settings, Instant cycle, int step,
            String key, EcmwfIndex index, Function<String, URI> objectUri) throws AdapterException {
        RasterSink sink = ctx.rasters();
        AdapterStateStore state = ctx.state();
        String cycleKey = ModelCycleSelector.cycleKey(cycle);
        Map<EcmwfDefinition.Product, List<EcmwfIndex.Entry>> wanted = new LinkedHashMap<>();
        Map<EcmwfDefinition.Product, List<Reference>> references = new LinkedHashMap<>();
        for (EcmwfDefinition.Product product : def.products()) {
            if (!product.steps().test(step) || sink.disabled(product.productId())
                    || readSteps(state, stepsKey(product, cycleKey)).contains(step)) {
                continue;
            }
            List<EcmwfIndex.Entry> entries = new ArrayList<>();
            String missing = null;
            for (EcmwfDefinition.Component c : product.components()) {
                Optional<EcmwfIndex.Entry> m = index.find(c.param(), c.levtype(), c.levelist());
                if (m.isEmpty()) {
                    missing = c.param() + ":" + c.levtype() + (c.levelist() == null ? "" : ":" + c.levelist());
                    break;
                }
                entries.add(m.get());
            }
            if (missing != null) {
                String reason = "possible format change: " + def.label() + " index missing " + missing;
                sink.reportFormatChange(product.productId(), reason);
                ctx.log().warn("{}: {} ({} f{})", def.label(), reason, cycleKey, step);
                if (product.required()) {
                    return List.of();
                }
                continue;
            }
            if (product.differenceHours() > 0 && step >= product.differenceHours()) {
                references.put(product, List.of(new Reference(step - product.differenceHours(),
                        product.components().get(0))));
            }
            wanted.put(product, entries);
        }
        if (wanted.isEmpty()) {
            return List.of();
        }
        Map<EcmwfIndex.Entry, Path> current = new LinkedHashMap<>();
        Map<EcmwfDefinition.Product, Path> referenceFiles = new LinkedHashMap<>();
        List<NormalizedRecord> out = new ArrayList<>();
        try {
            List<EcmwfIndex.Entry> all = wanted.values().stream().flatMap(List::stream).distinct().toList();
            readRanges(ctx, def, key, all, current, cycleKey, step, objectUri);
            for (Map.Entry<EcmwfDefinition.Product, List<Reference>> e : references.entrySet()) {
                Reference ref = e.getValue().get(0);
                Path file = readReference(ctx, def, objectUri, settings, cycle, ref, cycleKey, step);
                if (file != null) {
                    referenceFiles.put(e.getKey(), file);
                }
            }
            AdapterException failure = null;
            for (Map.Entry<EcmwfDefinition.Product, List<EcmwfIndex.Entry>> e : wanted.entrySet()) {
                EcmwfDefinition.Product product = e.getKey();
                if (references.containsKey(product) && !referenceFiles.containsKey(product)) {
                    continue;
                }
                List<Path> files = new ArrayList<>();
                e.getValue().forEach(entry -> files.add(current.get(entry)));
                Path ref = referenceFiles.get(product);
                if (ref != null) {
                    files.add(ref);
                }
                try {
                    NormalizedRecord record = commit(ctx, def, product, cycle, step, key, e.getValue(), files);
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
            current.values().forEach(GzipInputs::deleteQuietly);
            referenceFiles.values().forEach(GzipInputs::deleteQuietly);
        }
        return out;
    }

    /** Reads the coalesced ranges that hold {@code entries} of the step file into {@code files}. */
    private void readRanges(FetchContext ctx, EcmwfDefinition def, String key, List<EcmwfIndex.Entry> entries,
            Map<EcmwfIndex.Entry, Path> files, String cycleKey, int step, Function<String, URI> objectUri)
            throws AdapterException {
        if (entries.isEmpty()) {
            return;
        }
        EcmwfRunPlanner.Plan plan;
        try {
            plan = EcmwfRunPlanner.plan(entries);
        } catch (RasterFormatException e) {
            throw new AdapterException(def.label() + " " + key + ": " + e.getMessage(), e);
        }
        URI uri = objectUri.apply(key);
        for (EcmwfRunPlanner.Range range : plan.ranges()) {
            UpstreamResponse response = ctx.http().getRange(uri, range.start(), range.end(), range.length());
            byte[] body = response.body();
            if (body.length < range.length()) {
                throw new AdapterException(def.label() + " " + key + ": range " + range.start() + "-" + range.end()
                        + " returned " + body.length + " bytes");
            }
            for (EcmwfRunPlanner.Span span : range.spans()) {
                int from = Math.toIntExact(span.start() - range.start());
                files.put(span.entry(), writeTemp(def, cycleKey, step, span.entry(),
                        Arrays.copyOfRange(body, from, from + Math.toIntExact(span.length()))));
            }
        }
    }

    /** Reads a difference product's reference message from an earlier step's file, or null when it is absent. */
    private Path readReference(FetchContext ctx, EcmwfDefinition def, Function<String, URI> objectUri, Settings settings,
            Instant cycle, Reference ref, String cycleKey, int step) throws AdapterException {
        EcmwfDefinition.Component c = ref.component();
        String refKey = ModelCycleSelector.objectKey(settings.template(), cycle, ref.step());
        Optional<EcmwfIndex> refIndex = readIndex(ctx, def, objectUri.apply(refKey + ".index"));
        if (refIndex.isEmpty()) {
            return null;
        }
        Optional<EcmwfIndex.Entry> entry = refIndex.get().find(c.param(), c.levtype(), c.levelist());
        if (entry.isEmpty()) {
            return null;
        }
        Map<EcmwfIndex.Entry, Path> files = new LinkedHashMap<>();
        readRanges(ctx, def, refKey, List.of(entry.get()), files, cycleKey, ref.step(), objectUri);
        return files.get(entry.get());
    }

    /** Commits one product's frame for one step. */
    private NormalizedRecord commit(FetchContext ctx, EcmwfDefinition def, EcmwfDefinition.Product product,
            Instant cycle, int step, String key, List<EcmwfIndex.Entry> entries, List<Path> files)
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
        String labels = entries.stream().map(EcmwfIndex.Entry::label).collect(Collectors.joining(" + "));
        return ctx.rasters().commit(FrameInput.builder(product.productId())
                .forecast(cycle, step)
                .grid(grid)
                .file(files.get(0), first -> decodeBands(def, product, grid, files, rotate))
                .upstreamKey(key + "#" + labels)
                .variable(labels)
                .build());
    }

    /** Decodes a product's fields into physical bands (on the raster decode pool). */
    DecodedBands decodeBands(EcmwfDefinition def, EcmwfDefinition.Product product, GridDefinition grid,
            List<Path> files, boolean rotate) {
        List<float[]> bands = new ArrayList<>();
        for (int k = 0; k < product.components().size(); k++) {
            EcmwfDefinition.Component c = product.components().get(k);
            DecodedGrid g = decoder.decode(files.get(k), c.request());
            if (g.nx() != grid.nx() || g.ny() != grid.ny()) {
                throw new RasterFormatException(def.label() + " " + c.param() + " grid " + g.nx() + "x" + g.ny()
                        + " differs from " + grid.nx() + "x" + grid.ny());
            }
            bands.add(finite(def, c, g.floatData(), product.factor()));
        }
        if (product.differenceHours() > 0) {
            float[] reference = finite(def, product.components().get(0), decoder
                    .decode(files.get(1), product.components().get(0).request()).floatData(), product.factor());
            float[] current = bands.get(0);
            for (int i = 0; i < current.length; i++) {
                // A run total never decreases; packing noise can make the difference slightly negative.
                current[i] = Math.max(0f, current[i] - reference[i]);
            }
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

    private static float[] finite(EcmwfDefinition def, EcmwfDefinition.Component c, float[] values, double factor) {
        for (int i = 0; i < values.length; i++) {
            float v = values[i];
            if (!Float.isFinite(v)) {
                throw new RasterFormatException(def.label() + " " + c.param() + ":" + c.levtype()
                        + " holds a non-finite value at cell " + i);
            }
            if (factor != 1.0) {
                values[i] = (float) (v * factor);
            }
        }
        return values;
    }

    private static void checkPoints(EcmwfDefinition def, DecodedGrid header) {
        if ((long) header.nx() * header.ny() > MAX_POINTS) {
            throw new RasterFormatException(def.label() + " grid " + header.nx() + "x" + header.ny() + " exceeds "
                    + MAX_POINTS + " points");
        }
    }

    /** Marks every product whose steps of {@code cycle} all have frames complete. */
    private List<NormalizedRecord> completeRuns(FetchContext ctx, EcmwfDefinition def, Settings settings, Instant cycle)
            throws AdapterException {
        AdapterStateStore state = ctx.state();
        String cycleKey = ModelCycleSelector.cycleKey(cycle);
        List<NormalizedRecord> out = new ArrayList<>();
        for (EcmwfDefinition.Product product : def.products()) {
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

    /** A probe: the newest published index, one range-read message of the probe product, its header decoded. */
    FetchResult probe(FetchContext ctx, EcmwfDefinition def, Settings settings, Function<String, URI> objectUri)
            throws AdapterException {
        EcmwfDefinition.Product product = def.product(def.probeProduct());
        int step = product.stepsOf(settings.steps()).stream().findFirst().orElse(settings.steps().get(0));
        String template = settings.template();
        for (Instant cycle : ModelCycleSelector.candidates(ctx.now(), settings.runEvery(), settings.availabilityLag(),
                CANDIDATES + 1)) {
            String key = ModelCycleSelector.objectKey(template, cycle, step);
            Optional<EcmwfIndex> index = readIndex(ctx, def, objectUri.apply(key + ".index"));
            if (index.isEmpty()) {
                continue;
            }
            EcmwfDefinition.Component c = product.components().get(0);
            EcmwfIndex.Entry entry = index.get().find(c.param(), c.levtype(), c.levelist()).orElseThrow(
                    () -> new AdapterException("possible format change: " + def.label() + " index missing "
                            + c.param() + ":" + c.levtype()));
            List<EcmwfRunPlanner.Range> ranges;
            try {
                ranges = EcmwfRunPlanner.plan(List.of(entry)).ranges();
            } catch (RasterFormatException e) {
                throw new AdapterException(def.label() + " " + key + ": " + e.getMessage(), e);
            }
            EcmwfRunPlanner.Range range = ranges.get(0);
            byte[] body = ctx.http().getRange(objectUri.apply(key), range.start(), range.end(), range.length()).body();
            Path file = writeTemp(def, ModelCycleSelector.cycleKey(cycle), step, entry, body);
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
                        .upstreamKey(key + "#" + entry.label())
                        .variable(entry.label())
                        .build());
                return FetchResult.of(preview == null ? List.of() : List.of(preview));
            } finally {
                GzipInputs.deleteQuietly(file);
            }
        }
        return FetchResult.of(List.of());
    }

    private Path writeTemp(EcmwfDefinition def, String cycleKey, int step, EcmwfIndex.Entry entry, byte[] bytes) {
        try {
            Path file = Files.createTempFile(paths.tmp(), def.sourceId() + "-" + cycleKey + "-f" + step + "-"
                    + entry.param() + "-", ".grib2");
            Files.write(file, bytes);
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not stage an " + def.label() + " message: " + e.getMessage(), e);
        }
    }

    /** Removes the state of cycles that left the candidate window. */
    static void cleanState(AdapterStateStore state, EcmwfDefinition def, List<Instant> candidates, Duration runEvery) {
        Instant oldest = candidates.get(candidates.size() - 1);
        long count = Math.max(1, Duration.ofHours(24).dividedBy(runEvery));
        for (long k = 1; k <= count; k++) {
            String key = ModelCycleSelector.cycleKey(oldest.minus(runEvery.multipliedBy(k)));
            if (state.get(DONE_PREFIX + key).isEmpty()) {
                continue;
            }
            state.remove(DONE_PREFIX + key);
            if (state.get(ATTEMPTS_PREFIX + key).isPresent()) {
                state.remove(ATTEMPTS_PREFIX + key);
            }
            for (EcmwfDefinition.Product p : def.products()) {
                if (state.get(stepsKey(p, key)).isPresent()) {
                    state.remove(stepsKey(p, key));
                }
            }
        }
    }

    static String stepsKey(EcmwfDefinition.Product product, String cycleKey) {
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

    /** {@code 2} for f002, used by health reasons. */
    static String stepLabel(int step) {
        return String.format(Locale.ROOT, "%d", step);
    }
}
