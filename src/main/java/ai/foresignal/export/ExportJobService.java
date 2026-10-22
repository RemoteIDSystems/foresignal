package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfPlan;
import ai.foresignal.store.history.AsOfResolver;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The bulk export engine (prompt 111): submit, run, list, download, cancel and delete export jobs,
 * and run the query jobs of snapshots ({@link SnapshotService}) too.
 *
 * <p>A job is decided at submit time (sources allowed by {@link ExportLicenceService}, the point in
 * time of an asOf or atSeq read), saved {@code QUEUED} and handed to the bounded {@link ExportWorker}
 * after the transaction commits. The worker streams rows from {@link ExportQueryService} through the
 * format's {@link ExportWriter} into {@code {dir}/tmp/}, fsyncs and renames the file into the job
 * directory, then writes the signed manifest and the license and attribution sidecars. Limits:
 * {@code exports.maxRows} rows and {@code exports.diskQuotaGb} for the export directories
 * ({@code 409 EXPORT_QUOTA} at submit, a failed job when crossed while writing). A cancellation is
 * seen between pages. Metrics {@value #ROWS_METRIC} and {@value #DURATION_METRIC}, tagged by format.
 *
 * <p>The caller's read restriction is kept in memory for its queued job; a job that a restart
 * interrupted is marked {@code FAILED} when the application starts.
 *
 * <p>The bean is named {@code bulkExportJobService} so it does not clash with the 108 raster
 * {@code ExportJobService}.
 */
@Service("bulkExportJobService")
public class ExportJobService {

    private static final Logger log = LoggerFactory.getLogger(ExportJobService.class);

    /** Counter of exported rows, tagged {@code format}. */
    public static final String ROWS_METRIC = "foresignal.export.rows";

    /** Timer of export and snapshot runs, tagged {@code format}. */
    public static final String DURATION_METRIC = "foresignal.export.duration";

    /** The cached zip bundle of a job. */
    static final String BUNDLE = "bundle.zip";

    private final BulkExportJobRepository jobs;
    private final ExportQueryService query;
    private final ExportLicenceService licenses;
    private final ExportManifests manifests;
    private final ExportStore store;
    private final ExportProperties properties;
    private final ExportWorker worker;
    private final SourceRegistry registry;
    private final PlaceService places;
    private final AsOfResolver asOf;
    private final PointInTimePlanner pointInTime;
    private final JsonMapper jsonMapper;
    private final MeterRegistry meters;
    private final Clock clock;

    private final Map<String, ReadRestriction> restrictions = new ConcurrentHashMap<>();
    private final Map<String, CustomExport> customs = new ConcurrentHashMap<>();
    private final Set<String> canceled = ConcurrentHashMap.newKeySet();

    /** Creates the service. */
    public ExportJobService(BulkExportJobRepository jobs, ExportQueryService query, ExportLicenceService licenses,
            ExportManifests manifests, ExportStore store, ExportProperties properties, ExportWorker worker,
            SourceRegistry registry, PlaceService places, AsOfResolver asOf, PointInTimePlanner pointInTime,
            JsonMapper jsonMapper, MeterRegistry meters, Clock clock) {
        this.jobs = jobs;
        this.query = query;
        this.licenses = licenses;
        this.manifests = manifests;
        this.store = store;
        this.properties = properties;
        this.worker = worker;
        this.registry = registry;
        this.places = places;
        this.asOf = asOf;
        this.pointInTime = pointInTime;
        this.jsonMapper = jsonMapper;
        this.meters = meters;
        this.clock = clock;
    }

    /**
     * The caller of an export request.
     *
     * @param principal the token id, or null
     * @param admin true for an admin token
     * @param viewer the caller as the egress policy sees it
     * @param restriction the caller's read restriction
     */
    public record Caller(String principal, boolean admin, Viewer viewer, ReadRestriction restriction) {}

    // =====================================================================================
    // Submit
    // =====================================================================================

    /**
     * Submits a bulk export.
     *
     * @throws ForesignalException {@code 403} for {@code includePrivate} without admin,
     *     {@code 400 EXPORT_SPEC_INVALID} for an unknown source or place or when no source may be
     *     exported, {@code 409 EXPORT_QUOTA} when the export directories are full
     */
    @Transactional
    public ExportJobEntity submit(ExportSpec raw, Caller caller) {
        requirePrivateRights(raw.includePrivate(), caller);
        ExportSpec spec = resolvePlace(raw);
        List<SourceDescriptor> candidates = candidates(spec.sources(), spec.kinds());
        ExportLicenceService.Decision decision = licenses.decide(candidates, caller.viewer(), caller.admin(),
                spec.includePrivate(), spec.personalUse());
        requireSources(decision);
        Map<String, Object> plan = basePlan(decision);
        if ("asOf".equals(spec.mode())) {
            AsOfPlan asOfPlan = asOf.plan(spec.asOf(), decision.allowed(), spec.kinds(), Set.of());
            plan.put("pointInTime", PointInTimePlanner.toMap(PointInTimePlanner.fromAsOf(asOfPlan)));
        } else if ("atSeq".equals(spec.mode())) {
            PointInTime pit = pointInTime.plan(spec.atSeq(), null, decision.allowed(), spec.kinds());
            plan.put("pointInTime", PointInTimePlanner.toMap(pit));
        }
        checkQuota();
        Instant now = clock.instant();
        ExportJobEntity job = new ExportJobEntity(Ulids.next(now), ExportJobEntity.EXPORT, spec.format().wire(), now);
        job.setPrincipal(caller.principal());
        job.setPersonalUse(!decision.personalUseSources().isEmpty());
        job.setExpiresAt(now.plus(properties.ttl()));
        job.setSpec(spec.toMap());
        job.setPlan(plan);
        job.setLicenceSummary(licenses.summary(decision, Map.of()));
        job.setDir(store.dirOf(job).toString());
        jobs.save(job);
        enqueue(job.getId(), caller.restriction());
        return job;
    }

    /**
     * Submits an export whose rows a {@link CustomExport} writes (prompt 131: chronicle storage), with the decision
     * already taken by the caller (sources allowed, excluded and for personal use). The job is saved {@code QUEUED}
     * and runs on the worker after the transaction commits, like {@link #submit}.
     *
     * @param format the wire format recorded on the job (for example {@code geoparquet} or {@code json-bundle})
     * @param spec the job's spec, stored as given
     * @param decision the license decision
     * @param caller the caller
     * @param export writes the file
     * @return the queued job
     * @throws ForesignalException {@code 409 EXPORT_QUOTA} when the export directories are full
     */
    @Transactional
    public ExportJobEntity submitCustom(String format, Map<String, Object> spec, ExportLicenceService.Decision decision,
            Caller caller, CustomExport export) {
        checkQuota();
        Instant now = clock.instant();
        ExportJobEntity job = new ExportJobEntity(Ulids.next(now), ExportJobEntity.EXPORT, format, now);
        job.setPrincipal(caller.principal());
        job.setPersonalUse(!decision.personalUseSources().isEmpty());
        job.setExpiresAt(now.plus(properties.ttl()));
        job.setSpec(new LinkedHashMap<>(spec));
        job.setPlan(basePlan(decision));
        job.setLicenceSummary(licenses.summary(decision, Map.of()));
        job.setDir(store.dirOf(job).toString());
        jobs.save(job);
        customs.put(job.getId(), export);
        worker.submit(() -> runCustom(job.getId()));
        return job;
    }

    /** Runs the custom export job {@code id} on this thread (the worker calls this). */
    void runCustom(String id) {
        ExportJobEntity job = jobs.findById(id).orElse(null);
        CustomExport export = customs.remove(id);
        if (job == null || job.terminal()) {
            return;
        }
        if (canceled.remove(id)) {
            finish(job, ExportJobEntity.CANCELED, null);
            return;
        }
        if (export == null) {
            finish(job, ExportJobEntity.FAILED, "The job lost its caller context (restart); submit it again.");
            return;
        }
        long start = System.nanoTime();
        job.setStatus(ExportJobEntity.RUNNING);
        job.setStartedAt(clock.instant());
        job.setProgress(1);
        jobs.save(job);
        Path dir = store.dirOf(job);
        Path tmp = store.tmpDir(job);
        Path tmpFile = tmp.resolve(id + "." + export.extension());
        try {
            Files.createDirectories(tmp);
            CustomExport.Result result = export.write(tmpFile, () -> canceled.contains(id));
            if (canceled.contains(id)) {
                throw new CanceledExport();
            }
            if (usedBytes() > quotaBytes()) {
                throw new ExportLimitException("The export directories crossed the " + properties.diskQuotaGb()
                        + " GiB quota (exports.diskQuotaGb) while writing.");
            }
            Path finalFile = publish(tmpFile, dir.resolve(tmpFile.getFileName()));
            ExportStore.deleteTree(tmp);
            long bytes = Files.size(finalFile);
            ExportManifests.ExportFile file = new ExportManifests.ExportFile(finalFile.getFileName().toString(),
                    export.mediaType(), bytes, ExportManifests.sha256(finalFile), result.rows());
            ExportLicenceService.Decision decision = decision(job.getPlan());
            Map<String, Object> summary = licenses.summary(decision, result.rowsBySource());
            job.setWatermarkSeq(result.watermarkSeq());
            job.setRowCount(result.rows());
            job.setBytes(bytes);
            job.setLicenceSummary(summary);
            job.setManifestSha256(manifests.write(dir, job, List.of(file), result.watermarkSeq(), null, summary));
            job.setProgress(100);
            finish(job, ExportJobEntity.DONE, null);
            meters.counter(ROWS_METRIC, "format", job.getFormat()).increment(result.rows());
            Timer.builder(DURATION_METRIC).tag("format", job.getFormat()).description("Export and snapshot run time")
                    .register(meters).record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        } catch (CanceledExport e) {
            deleteQuietly(dir);
            canceled.remove(id);
            finish(job, ExportJobEntity.CANCELED, null);
        } catch (IOException | RuntimeException e) {
            log.warn("Custom export job {} failed", id, e);
            deleteQuietly(dir);
            finish(job, ExportJobEntity.FAILED, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    /** {@code 403} when {@code includePrivate} is asked without admin. */
    static void requirePrivateRights(boolean includePrivate, Caller caller) {
        if (includePrivate && !caller.admin()) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "includePrivate needs an admin token.");
        }
    }

    /** {@code 400 EXPORT_SPEC_INVALID} when no source survived the rules. */
    static void requireSources(ExportLicenceService.Decision decision) {
        if (decision.allowed().isEmpty()) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID,
                    "No requested source may be exported with these rights.", List.of())
                    .withProperty("excluded", decision.excluded());
        }
    }

    /** The plan entries every job stores: the allowed sources and the exclusions. */
    static Map<String, Object> basePlan(ExportLicenceService.Decision decision) {
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("allowedSources", decision.allowed());
        plan.put("personalUseSources", decision.personalUseSources());
        plan.put("excluded", decision.excluded());
        return plan;
    }

    /** Queues {@code id} on the worker with the caller's restriction. */
    void enqueue(String id, ReadRestriction restriction) {
        restrictions.put(id, restriction == null ? ReadRestriction.NONE : restriction);
        worker.submit(() -> run(id));
    }

    /**
     * The registered sources named in {@code sources} (every registered source when empty) that
     * declare one of {@code kinds} (any when empty).
     *
     * @throws ValidationException {@code EXPORT_SPEC_INVALID} for an unknown source id
     */
    List<SourceDescriptor> candidates(Collection<String> sources, Collection<String> kinds) {
        for (String id : sources) {
            if (!registry.contains(id)) {
                throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID, "Unknown source " + id + ".", List.of());
            }
        }
        Set<String> requested = sources.isEmpty() ? null : Set.copyOf(sources);
        List<SourceDescriptor> out = new ArrayList<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (requested != null && !requested.contains(descriptor.id())) {
                continue;
            }
            if (!kinds.isEmpty() && descriptor.kinds().stream().noneMatch(kinds::contains)) {
                continue;
            }
            out.add(descriptor);
        }
        return out;
    }

    private ExportSpec resolvePlace(ExportSpec spec) {
        if (spec.bbox() != null || spec.placeId() == null) {
            return spec;
        }
        double[] box = places.geometry(spec.placeId()).map(ExportQueryService::envelopeOf).orElse(null);
        if (box == null) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID, "Unknown place " + spec.placeId() + ".",
                    List.of());
        }
        return spec.withBbox(box);
    }

    /** {@code 409 EXPORT_QUOTA} when the export and snapshot directories hold more than the quota. */
    void checkQuota() {
        long used = usedBytes();
        if (used > quotaBytes()) {
            throw new ForesignalException(HttpStatus.CONFLICT, ErrorCodes.EXPORT_QUOTA,
                    "The export directories hold " + used + " bytes, over the " + properties.diskQuotaGb()
                            + " GiB quota (exports.diskQuotaGb).");
        }
    }

    long usedBytes() {
        return ExportStore.bytesUnder(store.exportsRoot()) + ExportStore.bytesUnder(store.snapshotsRoot());
    }

    long quotaBytes() {
        return properties.diskQuotaGb() * 1024L * 1024L * 1024L;
    }

    // =====================================================================================
    // Run
    // =====================================================================================

    /** Runs the export or snapshot job {@code id} on this thread (the worker calls this). */
    public void run(String id) {
        ExportJobEntity job = jobs.findById(id).orElse(null);
        ReadRestriction restriction = restrictions.remove(id);
        if (job == null || job.terminal()) {
            return;
        }
        if (canceled.remove(id)) {
            finish(job, ExportJobEntity.CANCELED, null);
            return;
        }
        if (restriction == null) {
            finish(job, ExportJobEntity.FAILED, "The job lost its caller context (restart); submit it again.");
            return;
        }
        long start = System.nanoTime();
        job.setStatus(ExportJobEntity.RUNNING);
        job.setStartedAt(clock.instant());
        job.setProgress(1);
        jobs.save(job);
        ExportFormat format = ExportFormat.parse(job.getFormat());
        Path dir = store.dirOf(job);
        Path tmp = store.tmpDir(job);
        Path tmpFile = tmp.resolve(id + "." + format.extension());
        ExportWriter writer = ExportWriterFactory.create(format);
        try {
            ExportSpec spec = ExportSpec.fromMap(job.getSpec());
            Map<String, Object> plan = job.getPlan();
            List<String> sources = strings(plan.get("allowedSources"));
            PointInTime pit = plan.get("pointInTime") instanceof Map<?, ?> map ? PointInTimePlanner.fromMap(map) : null;
            List<String> flattened = spec.flatten() ? flattenedFields(spec, sources) : List.of();
            ExportSchema schema = ExportSchema.standard(flattened, true);
            writer.open(tmpFile, schema, writerMetadata(job, spec));
            Map<String, String> elevationFields = format == ExportFormat.GPX ? elevationFields(sources) : Map.of();
            Map<String, Long> rowsBySource = new LinkedHashMap<>();
            long usedAtStart = usedBytes();
            long[] count = {0};
            long[] pages = {0};
            query.stream(spec, sources, restriction, pit, new ExportQueryService.Sink() {
                @Override
                public void begin(long watermarkSeq) {
                    job.setWatermarkSeq(watermarkSeq);
                }

                @Override
                public void row(Map<String, Object> row) {
                    if (++count[0] > properties.maxRows()) {
                        throw new ExportLimitException("The export exceeds exports.maxRows (" + properties.maxRows()
                                + " rows); narrow the spec.");
                    }
                    enrich(row, spec, flattened, format, elevationFields);
                    rowsBySource.merge(String.valueOf(row.get("source_id")), 1L, Long::sum);
                    try {
                        writer.write(row);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }

                @Override
                public void page() {
                    if (canceled.contains(id)) {
                        throw new CanceledExport();
                    }
                    pages[0]++;
                    if (usedAtStart + sizeOf(tmpFile) > quotaBytes()) {
                        throw new ExportLimitException("The export directories crossed the "
                                + properties.diskQuotaGb() + " GiB quota (exports.diskQuotaGb) while writing.");
                    }
                    job.setProgress((int) Math.min(95, 5 + pages[0]));
                    jobs.save(job);
                }
            });
            writer.close();
            Path finalFile = publish(tmpFile, dir.resolve(tmpFile.getFileName()));
            ExportStore.deleteTree(tmp);
            long bytes = Files.size(finalFile);
            ExportManifests.ExportFile file = new ExportManifests.ExportFile(finalFile.getFileName().toString(),
                    format.mediaType(), bytes, ExportManifests.sha256(finalFile), writer.rows());
            ExportLicenceService.Decision decision = decision(plan);
            Map<String, Object> summary = licenses.summary(decision, rowsBySource);
            job.setRowCount(writer.rows());
            job.setBytes(bytes);
            job.setLicenceSummary(summary);
            job.setManifestSha256(manifests.write(dir, job, List.of(file), job.getWatermarkSeq(),
                    pit == null ? null : pit.s(), summary));
            job.setProgress(100);
            finish(job, ExportJobEntity.DONE, null);
            meters.counter(ROWS_METRIC, "format", job.getFormat()).increment(writer.rows());
            Timer.builder(DURATION_METRIC).tag("format", job.getFormat()).description("Export and snapshot run time")
                    .register(meters).record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        } catch (CanceledExport e) {
            closeQuietly(writer);
            deleteQuietly(dir);
            canceled.remove(id);
            finish(job, ExportJobEntity.CANCELED, null);
        } catch (IOException | RuntimeException e) {
            log.warn("Export job {} failed", id, e);
            closeQuietly(writer);
            deleteQuietly(dir);
            finish(job, ExportJobEntity.FAILED, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    /** fsyncs {@code tmp} and moves it to {@code target} atomically where the file system allows. */
    static Path publish(Path tmp, Path target) throws IOException {
        try (FileChannel channel = FileChannel.open(tmp, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
        try {
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    /** Sets a terminal status on a fresh copy of the job and saves it. */
    void finish(ExportJobEntity job, String status, String error) {
        job.setStatus(status);
        job.setError(error);
        job.setFinishedAt(clock.instant());
        jobs.save(job);
    }

    private Map<String, String> writerMetadata(ExportJobEntity job, ExportSpec spec) {
        Map<String, Object> summary = job.getLicenceSummary() == null ? Map.of() : job.getLicenceSummary();
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("name", spec.name() == null || spec.name().isBlank() ? "Foresignal export " + job.getId()
                : spec.name());
        metadata.put("licence", new String(jsonMapper.writeValueAsBytes(summary), java.nio.charset.StandardCharsets.UTF_8));
        metadata.put("attribution", String.join("; ", ExportLicenceService.attributionLines(summary)));
        String termsUrl = ExportLicenceService.firstTermsUrl(summary);
        if (termsUrl != null) {
            metadata.put("licenseUrl", termsUrl);
        }
        String licenseName = ExportLicenceService.firstLicenseName(summary);
        if (licenseName != null) {
            metadata.put("licenseName", licenseName);
        }
        return metadata;
    }

    /** The flattened columns: {@code fields}, else every {@code fieldSpecs} key of the sources, sorted. */
    List<String> flattenedFields(ExportSpec spec, List<String> sources) {
        if (!spec.fields().isEmpty()) {
            return ExportSchema.flattenedNames(spec.fields());
        }
        TreeSet<String> keys = new TreeSet<>();
        for (String id : sources) {
            registry.find(id).ifPresent(r -> keys.addAll(r.descriptor().fieldSpecs().keySet()));
        }
        return ExportSchema.flattenedNames(new ArrayList<>(keys));
    }

    /**
     * Per source, the payload field holding its altitude in meters (Adapter-Guide §5): a
     * {@code fieldSpecs} entry with unit {@code m} and a datum, preferring the WGS84 ellipsoid, then
     * mean sea level, then any other datum.
     */
    Map<String, String> elevationFields(List<String> sources) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String id : sources) {
            SourceDescriptor descriptor = registry.find(id).map(r -> r.descriptor()).orElse(null);
            if (descriptor == null) {
                continue;
            }
            String best = null;
            int bestRank = Integer.MAX_VALUE;
            for (Map.Entry<String, FieldSpec> field : new java.util.TreeMap<>(descriptor.fieldSpecs()).entrySet()) {
                FieldSpec spec = field.getValue();
                if (spec == null || !"m".equals(spec.unit()) || spec.datum() == null) {
                    continue;
                }
                String datum = spec.datum().toUpperCase(java.util.Locale.ROOT);
                int rank = datum.contains("WGS84") || datum.contains("WGS 84") ? 0 : datum.contains("MSL") ? 1 : 2;
                if (rank < bestRank) {
                    best = field.getKey();
                    bestRank = rank;
                }
            }
            if (best != null) {
                out.put(id, best);
            }
        }
        return out;
    }

    /** Applies the payload allowlist, the flattened columns and the GPX keys to one row. */
    void enrich(Map<String, Object> row, ExportSpec spec, List<String> flattened, ExportFormat format,
            Map<String, String> elevationFields) {
        String elevationField = format == ExportFormat.GPX ? elevationFields.get(String.valueOf(row.get("source_id")))
                : null;
        boolean needsPayload = !flattened.isEmpty() || !spec.fields().isEmpty() || elevationField != null;
        if (format == ExportFormat.GPX) {
            row.put(ExportWriter.TRACK, registry.find(String.valueOf(row.get("source_id")))
                    .map(r -> r.descriptor().isTrack()).orElse(false));
        }
        if (!needsPayload || !(row.get("payload") instanceof String payload)) {
            return;
        }
        JsonNode node;
        try {
            node = jsonMapper.readTree(payload);
        } catch (RuntimeException e) {
            return;
        }
        if (node == null || !node.isObject()) {
            return;
        }
        for (String field : flattened) {
            JsonNode value = node.get(field);
            if (value != null && !value.isNull()) {
                row.put(field, value.isValueNode() ? value.asString() : value.toString());
            }
        }
        if (elevationField != null && node.get(elevationField) != null && node.get(elevationField).isNumber()) {
            row.put(ExportWriter.ELEVATION, node.get(elevationField).asDouble());
        }
        if (!spec.fields().isEmpty()) {
            Map<String, Object> projected = new LinkedHashMap<>();
            for (String field : spec.fields()) {
                JsonNode value = node.get(field);
                if (value != null) {
                    projected.put(field, value);
                }
            }
            row.put("payload", new String(jsonMapper.writeValueAsBytes(projected), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    static ExportLicenceService.Decision decision(Map<String, Object> plan) {
        List<Map<String, Object>> excluded = new ArrayList<>();
        if (plan.get("excluded") instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    map.forEach((k, v) -> copy.put(String.valueOf(k), v));
                    excluded.add(copy);
                }
            }
        }
        return new ExportLicenceService.Decision(strings(plan.get("allowedSources")),
                strings(plan.get("personalUseSources")), excluded);
    }

    static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            list.forEach(item -> out.add(String.valueOf(item)));
        }
        return out;
    }

    private static long sizeOf(Path file) {
        try {
            return Files.exists(file) ? Files.size(file) : 0L;
        } catch (IOException e) {
            return 0L;
        }
    }

    // =====================================================================================
    // Read, list, cancel, delete
    // =====================================================================================

    /** A job, {@code 404 UNKNOWN_EXPORT} when unknown. */
    public ExportJobEntity get(String id) {
        return jobs.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_EXPORT,
                "Unknown export " + id + "."));
    }

    /** True when a job with {@code id} exists. */
    public boolean exists(String id) {
        return jobs.existsById(id);
    }

    /** A job the caller may see: its own, or any for admin; {@code 404} otherwise. */
    public ExportJobEntity visible(String id, String principal, boolean admin) {
        ExportJobEntity job = get(id);
        if (!admin && (job.getPrincipal() == null || !job.getPrincipal().equals(principal))) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_EXPORT, "Unknown export " + id + ".");
        }
        return job;
    }

    /**
     * A page of jobs: the caller's own, or every job for admin; newest first.
     *
     * @param kind {@code EXPORT}, {@code SNAPSHOT}, {@code PACK} or null
     * @param status a status or null
     * @param cursor the id of the previous page's last job, or null
     */
    public Page list(String principal, boolean admin, String kind, String status, String cursor, int limit) {
        int size = Math.max(1, Math.min(500, limit));
        List<ExportJobEntity> rows = jobs.page(admin ? null : principal == null ? "" : principal, kind, status,
                cursor == null || cursor.isBlank() ? null : cursor, Limit.of(size + 1));
        String next = null;
        if (rows.size() > size) {
            rows = rows.subList(0, size);
            next = rows.getLast().getId();
        }
        return new Page(List.copyOf(rows), next);
    }

    /**
     * A page of jobs.
     *
     * @param items the jobs
     * @param nextCursor the next page's cursor, or null
     */
    public record Page(List<ExportJobEntity> items, String nextCursor) {}

    /** Cancels a queued or running job (seen between pages), or deletes a finished one and its files. */
    @Transactional
    public void delete(String id) {
        ExportJobEntity job = get(id);
        if (ExportJobEntity.QUEUED.equals(job.getStatus()) || ExportJobEntity.RUNNING.equals(job.getStatus())) {
            canceled.add(id);
            return;
        }
        deleteQuietly(store.dirOf(job));
        jobs.delete(job);
    }

    /** True when the owner asked to cancel the job {@code id} (packs check it between tiles). */
    public boolean cancelRequested(String id) {
        return canceled.contains(id);
    }

    /** Forgets a cancellation request once the job has stopped. */
    public void clearCancel(String id) {
        canceled.remove(id);
    }

    /** Marks jobs a restart interrupted as failed. */
    @EventListener(ApplicationReadyEvent.class)
    public void failInterrupted() {
        try {
            for (ExportJobEntity job : jobs.findByStatusIn(List.of(ExportJobEntity.QUEUED, ExportJobEntity.RUNNING))) {
                if (!restrictions.containsKey(job.getId()) && !customs.containsKey(job.getId())) {
                    deleteQuietly(store.dirOf(job).resolve("tmp"));
                    finish(job, ExportJobEntity.FAILED, "Interrupted by a restart; submit it again.");
                }
            }
        } catch (RuntimeException e) {
            log.warn("Could not mark interrupted export jobs as failed", e);
        }
    }

    // =====================================================================================
    // Download
    // =====================================================================================

    /**
     * A file to download.
     *
     * @param path the file
     * @param sha256 its SHA-256 (the ETag)
     * @param mediaType its media type
     */
    public record Download(Path path, String sha256, String mediaType) {}

    /**
     * The download of a job: its single data file, a named data file, or the zip bundle.
     *
     * @throws ForesignalException {@code 409 EXPORT_NOT_READY} before the job is done,
     *     {@code 410 EXPORT_EXPIRED} after it expired or when its files are gone, {@code 404} for an
     *     unknown file name
     */
    public Download download(ExportJobEntity job, String file, boolean bundle) {
        requireDone(job);
        Map<String, Object> manifest = manifest(job);
        List<Map<String, Object>> files = manifestFiles(manifest);
        Path dir = store.dirOf(job);
        if (bundle) {
            return bundle(job, dir, files);
        }
        Map<String, Object> chosen = null;
        if (file == null || file.isBlank()) {
            chosen = files.isEmpty() ? null : files.getFirst();
        } else {
            for (Map<String, Object> entry : files) {
                if (file.equals(entry.get("name"))) {
                    chosen = entry;
                }
            }
            if (chosen == null && ExportManifests.SIDECARS.contains(file)) {
                Path sidecar = dir.resolve(file);
                return new Download(requireFile(sidecar), sha256Quietly(sidecar),
                        file.endsWith(".txt") ? "text/plain" : file.endsWith(".jws") ? "application/jose" : "application/json");
            }
        }
        if (chosen == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_EXPORT, "The export has no file " + file + ".");
        }
        Path path = requireFile(dir.resolve(String.valueOf(chosen.get("name"))));
        return new Download(path, String.valueOf(chosen.get("sha256")), String.valueOf(chosen.get("mediaType")));
    }

    /** The bytes of {@code manifest.json} and its detached JWS. */
    public ManifestBytes manifestBytes(ExportJobEntity job) {
        requireDone(job);
        Path dir = store.dirOf(job);
        try {
            return new ManifestBytes(Files.readAllBytes(requireFile(dir.resolve(ExportManifests.MANIFEST))),
                    Files.readString(requireFile(dir.resolve(ExportManifests.SIGNATURE))));
        } catch (IOException e) {
            throw gone("The manifest of " + job.getId() + " is gone.");
        }
    }

    /**
     * A manifest and its signature.
     *
     * @param manifest the {@code manifest.json} bytes
     * @param jws the detached JWS
     */
    public record ManifestBytes(byte[] manifest, String jws) {}

    private void requireDone(ExportJobEntity job) {
        if (ExportJobEntity.EXPIRED.equals(job.getStatus())) {
            throw gone("The export " + job.getId() + " expired.");
        }
        if (!ExportJobEntity.DONE.equals(job.getStatus())) {
            throw new ForesignalException(HttpStatus.CONFLICT, ErrorCodes.EXPORT_NOT_READY,
                    "The export " + job.getId() + " is " + job.getStatus().toLowerCase(java.util.Locale.ROOT)
                            + (job.getError() == null ? "." : ": " + job.getError()));
        }
    }

    private Map<String, Object> manifest(ExportJobEntity job) {
        Path manifest = store.dirOf(job).resolve(ExportManifests.MANIFEST);
        try {
            return jsonMapper.readValue(Files.readAllBytes(requireFile(manifest)),
                    new tools.jackson.core.type.TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (IOException e) {
            throw gone("The manifest of " + job.getId() + " is gone.");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> manifestFiles(Map<String, Object> manifest) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (manifest != null && manifest.get("files") instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    out.add((Map<String, Object>) map);
                }
            }
        }
        return out;
    }

    /** Builds the zip bundle once (deterministic entry times, so its ETag is stable) and reuses it. */
    private synchronized Download bundle(ExportJobEntity job, Path dir, List<Map<String, Object>> files) {
        Path zip = dir.resolve(BUNDLE);
        Path digest = dir.resolve(BUNDLE + ".sha256");
        try {
            if (!Files.isRegularFile(zip) || !Files.isRegularFile(digest)) {
                Path tmp = dir.resolve(BUNDLE + ".tmp");
                FileTime time = FileTime.from(job.getCreatedAt());
                try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(tmp))) {
                    List<String> names = new ArrayList<>();
                    files.forEach(f -> names.add(String.valueOf(f.get("name"))));
                    names.addAll(ExportManifests.SIDECARS);
                    for (String name : names) {
                        Path file = requireFile(dir.resolve(name));
                        ZipEntry entry = new ZipEntry(name);
                        entry.setLastModifiedTime(time);
                        out.putNextEntry(entry);
                        Files.copy(file, out);
                        out.closeEntry();
                    }
                }
                publish(tmp, zip);
                Files.writeString(digest, ExportManifests.sha256(zip));
            }
            return new Download(zip, Files.readString(digest).strip(), "application/zip");
        } catch (IOException e) {
            throw new UncheckedIOException("Could not build the export bundle", e);
        }
    }

    private Path requireFile(Path path) {
        if (!Files.isRegularFile(path)) {
            throw gone("The export file " + path.getFileName() + " is gone.");
        }
        return path;
    }

    private static String sha256Quietly(Path path) {
        try {
            return ExportManifests.sha256(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ForesignalException gone(String detail) {
        return new ForesignalException(HttpStatus.GONE, ErrorCodes.EXPORT_EXPIRED, detail);
    }

    // =====================================================================================
    // Views
    // =====================================================================================

    /** The {@code ExportJob} view; {@code base} is the public base URL (empty for relative URLs). */
    public Map<String, Object> view(ExportJobEntity job, String base) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", job.getId());
        out.put("kind", job.getKind());
        out.put("format", job.getFormat());
        out.put("spec", job.getSpec());
        out.put("status", job.getStatus());
        out.put("progress", job.getProgress());
        out.put("principal", job.getPrincipal());
        out.put("rowCount", job.getRowCount());
        out.put("bytes", job.getBytes());
        out.put("manifestSha256", job.getManifestSha256());
        out.put("licenceSummary", job.getLicenceSummary());
        out.put("watermarkSeq", job.getWatermarkSeq());
        out.put("personalUse", job.isPersonalUse());
        out.put("createdAt", job.getCreatedAt().toString());
        out.put("startedAt", job.getStartedAt() == null ? null : job.getStartedAt().toString());
        out.put("finishedAt", job.getFinishedAt() == null ? null : job.getFinishedAt().toString());
        out.put("expiresAt", job.getExpiresAt() == null ? null : job.getExpiresAt().toString());
        out.put("error", job.getError());
        boolean done = ExportJobEntity.DONE.equals(job.getStatus());
        String prefix = (base == null ? "" : base) + "/v1/exports/" + job.getId();
        out.put("downloadUrl", done ? prefix + "/download" : null);
        out.put("manifestUrl", done ? prefix + "/manifest" : null);
        List<Map<String, Object>> files = new ArrayList<>();
        if (done) {
            try {
                for (Map<String, Object> entry : manifestFiles(manifest(job))) {
                    Map<String, Object> file = new LinkedHashMap<>();
                    file.put("name", entry.get("name"));
                    file.put("url", prefix + "/download?file=" + entry.get("name"));
                    file.put("bytes", entry.get("bytes"));
                    file.put("sha256", entry.get("sha256"));
                    file.put("mediaType", entry.get("mediaType"));
                    file.put("expiresAt", job.getExpiresAt() == null ? null : job.getExpiresAt().toString());
                    files.add(file);
                }
            } catch (ForesignalException e) {
                // The files were swept; the job still shows.
            }
        }
        out.put("files", files);
        return out;
    }

    // =====================================================================================
    // helpers
    // =====================================================================================

    private static void closeQuietly(ExportWriter writer) {
        try {
            writer.close();
        } catch (IOException | RuntimeException ignored) {
            // The job fails or is canceled anyway.
        }
    }

    static void deleteQuietly(Path dir) {
        try {
            ExportStore.deleteTree(dir);
        } catch (RuntimeException e) {
            log.debug("Could not delete {}", dir, e);
        }
    }

    /** Thrown between pages when the running job was canceled. */
    static final class CanceledExport extends RuntimeException {
        CanceledExport() {
            super("canceled", null, false, false);
        }
    }

    /** Thrown when a job crosses {@code exports.maxRows} or the disk quota while writing. */
    static final class ExportLimitException extends RuntimeException {
        ExportLimitException(String message) {
            super(message);
        }
    }
}
