package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The bulk export engine (prompt 111): submit, stream, manifest, list, download, cancel. One bounded
 * worker pool ({@code exports.maxConcurrent}, default 1) on virtual threads; rows stream from
 * {@link ExportQueryService} through an {@link ExportWriter} and are never fully buffered.
 */
@Service
public class ExportJobService {

    private static final Logger log = LoggerFactory.getLogger(ExportJobService.class);

    /** Counter of exported rows, tagged {@code format}. */
    public static final String ROWS_METRIC = "foresignal.export.rows";

    /** Timer of export runs, tagged {@code format}. */
    public static final String DURATION_METRIC = "foresignal.export.duration";

    private final ExportJobRepository jobs;
    private final ExportQueryService query;
    private final ExportLicenceService licences;
    private final ExportManifests manifests;
    private final ExportStore store;
    private final ExportProperties properties;
    private final SourceRegistry registry;
    private final PlaceService places;
    private final JsonMapper jsonMapper;
    private final MeterRegistry meters;
    private final Clock clock;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore permits;
    private final Set<String> cancelled = ConcurrentHashMap.newKeySet();

    /** Creates the service. */
    public ExportJobService(ExportJobRepository jobs, ExportQueryService query, ExportLicenceService licences,
            ExportManifests manifests, ExportStore store, ExportProperties properties, SourceRegistry registry,
            PlaceService places, JsonMapper jsonMapper, MeterRegistry meters, Clock clock) {
        this.jobs = jobs;
        this.query = query;
        this.licences = licences;
        this.manifests = manifests;
        this.store = store;
        this.properties = properties;
        this.registry = registry;
        this.places = places;
        this.jsonMapper = jsonMapper;
        this.meters = meters;
        this.clock = clock;
        this.permits = new Semaphore(properties.maxConcurrent());
    }

    /** The caller context of an export request. */
    public record Caller(String principal, boolean admin, boolean privateAccess, ReadRestriction restriction) {}

    /** Submits a bulk export job. */
    @Transactional
    public ExportJobEntity submit(ExportSpec rawSpec, Caller caller) {
        ExportSpec spec = resolvePlace(rawSpec);
        List<SourceDescriptor> candidates = candidates(spec, caller);
        ExportLicenceService.Result licence = licences.summarize(candidates, spec.personalUse());
        if (licence.allowedSources().isEmpty()) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID,
                    "No source may be exported with these rights.", List.of());
        }
        checkQuota();
        Instant now = clock.instant();
        ExportJobEntity job = new ExportJobEntity(Ulids.next(now), ExportJobEntity.EXPORT, spec.format().wire(), now);
        job.setPrincipal(caller.principal());
        job.setPersonalUse(spec.personalUse());
        job.setExpiresAt(now.plus(properties.ttl()));
        job.setLicenceSummary(licence.summary());
        Map<String, Object> specMap = spec.toMap();
        specMap.put("allowedSources", licence.allowedSources());
        job.setSpec(specMap);
        jobs.save(job);
        schedule(job.getId());
        return job;
    }

    private void schedule(String id) {
        executor.submit(() -> {
            try {
                permits.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            try {
                run(id);
            } catch (RuntimeException e) {
                log.warn("Export job {} failed", id, e);
                fail(id, e);
            } finally {
                permits.release();
            }
        });
    }

    /** Runs a bulk export job on this thread. */
    public void run(String id) {
        ExportJobEntity job = jobs.findById(id).orElse(null);
        if (job == null || terminal(job.getStatus())) {
            return;
        }
        long start = System.nanoTime();
        job.setStatus(ExportJobEntity.RUNNING);
        job.setStartedAt(clock.instant());
        job.setProgress(1);
        jobs.save(job);
        Path tmp = store.tmpDir(id);
        Path fileName = tmp.resolve(id + "." + job.getFormat());
        ExportFormat format = ExportFormat.parse(job.getFormat());
        ExportWriter writer = ExportWriterFactory.create(format);
        try {
            ExportSpec spec = specOf(job);
            List<String> sources = allowedSources(job);
            boolean geometry = true;
            List<String> flattened = spec.flatten() ? spec.fields() : List.of();
            ExportSchema schema = ExportSchema.standard(flattened, geometry);
            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("licence", licenceId(job));
            metadata.put("attribution", String.join("; ", attributions(job)));
            writer.open(fileName, schema, metadata);
            AtomicLong rows = new AtomicLong();
            query.stream(spec, sources, ReadRestriction.NONE, new ExportQueryService.Sink() {
                @Override
                public void begin(long watermarkSeq) {
                    job.setWatermarkSeq(watermarkSeq);
                }

                @Override
                public void row(Map<String, Object> row) {
                    if (cancelled.contains(id)) {
                        throw new CancelledExport();
                    }
                    long count = rows.incrementAndGet();
                    if (count > properties.maxRows()) {
                        throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.EXPORT_SPEC_INVALID,
                                "The export exceeds foresignal.exports.max-rows (" + properties.maxRows() + ").");
                    }
                    enrich(row, spec, format);
                    try {
                        writer.write(row);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    if (count % 10_000 == 0) {
                        job.setProgress((int) Math.min(95, 5 + (100L * count / properties.maxRows())));
                    }
                }
            });
            writer.close();
            fsync(fileName);
            Path finalFile = store.jobDir(id).resolve(fileName.getFileName().toString());
            Files.move(fileName, finalFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            long bytes = Files.size(finalFile);
            String sha256 = ExportManifests.sha256(finalFile);
            long rowCount = rows.get();
            job.setRowCount(rowCount);
            job.setBytes(bytes);
            List<ExportManifests.ExportFile> files = List.of(new ExportManifests.ExportFile(
                    finalFile.getFileName().toString(), format.mediaType(), bytes, sha256, rowCount));
            String manifestSha = manifests.write(store.jobDir(id), job, files, job.getWatermarkSeq() == null ? 0L
                    : job.getWatermarkSeq(), spec.atSeq(), job.getLicenceSummary(), attributions(job));
            job.setManifestSha256(manifestSha);
            job.setStatus(ExportJobEntity.DONE);
            job.setProgress(100);
            job.setFinishedAt(clock.instant());
            jobs.save(job);
            meters.counter(ROWS_METRIC, "format", job.getFormat()).increment(rowCount);
            Timer.builder(DURATION_METRIC).tag("format", job.getFormat()).register(meters)
                    .record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        } catch (CancelledExport e) {
            closeQuietly(writer);
            deleteQuietly(store.jobDir(id));
            cancelled.remove(id);
            job.setStatus(ExportJobEntity.CANCELLED);
            job.setFinishedAt(clock.instant());
            jobs.save(job);
        } catch (IOException | RuntimeException e) {
            closeQuietly(writer);
            deleteQuietly(store.jobDir(id));
            fail(id, e);
        }
    }

    private void fail(String id, Exception e) {
        jobs.findById(id).ifPresent(job -> {
            job.setStatus(ExportJobEntity.FAILED);
            job.setError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            job.setFinishedAt(clock.instant());
            jobs.save(job);
        });
    }

    /** Cancels or deletes a job. */
    @Transactional
    public void delete(String id) {
        ExportJobEntity job = get(id);
        if (ExportJobEntity.RUNNING.equals(job.getStatus()) || ExportJobEntity.QUEUED.equals(job.getStatus())) {
            cancelled.add(id);
        } else {
            deleteQuietly(store.jobDir(id));
            jobs.delete(job);
        }
    }

    /** Fetches a job, 404 when unknown. */
    public ExportJobEntity get(String id) {
        return jobs.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_EXPORT,
                "Unknown export " + id + "."));
    }

    /** Lists jobs: the caller's own, or every job for an admin. */
    public List<ExportJobEntity> list(String principal, boolean admin) {
        return admin ? jobs.findAllByOrderByCreatedAtDesc() : jobs.findByPrincipalOrderByCreatedAtDesc(principal);
    }

    /** The path of a job's data file (410 when expired or gone). */
    public Path file(String id, String name) {
        ExportJobEntity job = get(id);
        if (ExportJobEntity.EXPIRED.equals(job.getStatus()) || ExportJobEntity.FAILED.equals(job.getStatus())) {
            throw new ForesignalException(HttpStatus.GONE, ErrorCodes.EXPORT_EXPIRED,
                    "The export " + id + " is " + job.getStatus().toLowerCase() + ".");
        }
        Path path = store.jobDir(id).resolve(name.replace("/", "").replace("\\", ""));
        if (!Files.isRegularFile(path)) {
            throw new ForesignalException(HttpStatus.GONE, ErrorCodes.EXPORT_EXPIRED, "The export file is gone.");
        }
        return path;
    }

    /** The first data file of a job. */
    public Path primaryFile(ExportJobEntity job) {
        try (var stream = Files.list(store.jobDir(job.getId()))) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith("manifest")
                            && !p.getFileName().toString().endsWith(".json")
                            && !p.getFileName().toString().endsWith(".txt")
                            && !p.getFileName().toString().endsWith(".jws"))
                    .sorted()
                    .findFirst()
                    .orElseThrow(() -> new ForesignalException(HttpStatus.GONE, ErrorCodes.EXPORT_EXPIRED,
                            "The export has no data file."));
        } catch (IOException e) {
            throw new ForesignalException(HttpStatus.GONE, ErrorCodes.EXPORT_EXPIRED, "The export directory is gone.");
        }
    }

    /** A zip bundle of the data file plus the manifest, licence and attribution. */
    public Path bundle(ExportJobEntity job) {
        Path dir = store.jobDir(job.getId());
        Path zip = dir.resolve("bundle.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            try (var stream = Files.list(dir)) {
                for (Path file : stream.filter(Files::isRegularFile).sorted().toList()) {
                    String name = file.getFileName().toString();
                    if (name.equals("bundle.zip")) {
                        continue;
                    }
                    out.putNextEntry(new ZipEntry(name));
                    Files.copy(file, out);
                    out.closeEntry();
                }
            }
            return zip;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not build the export bundle", e);
        }
    }

    /** The job's JSON view. */
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
        out.put("downloadUrl", base + "/v1/exports/" + job.getId() + "/download");
        out.put("manifestUrl", base + "/v1/exports/" + job.getId() + "/manifest");
        out.put("files", fileViews(job));
        return out;
    }

    private List<Map<String, Object>> fileViews(ExportJobEntity job) {
        List<Map<String, Object>> files = new ArrayList<>();
        try (var stream = Files.list(store.jobDir(job.getId()))) {
            for (Path file : stream.filter(Files::isRegularFile).sorted().toList()) {
                String name = file.getFileName().toString();
                if (name.equals("bundle.zip")) {
                    continue;
                }
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", name);
                entry.put("bytes", Files.size(file));
                entry.put("mediaType", name.endsWith(".parquet") ? "application/vnd.apache.parquet"
                        : name.endsWith(".gpkg") ? "application/geopackage+sqlite3"
                                : name.endsWith(".pmtiles") ? "application/vnd.pmtiles"
                                        : name.endsWith(".gpx") ? "application/gpx+xml" : "application/octet-stream");
                files.add(entry);
            }
        } catch (IOException e) {
            return List.of();
        }
        return files;
    }

    // =====================================================================================
    // helpers
    // =====================================================================================

    private ExportSpec resolvePlace(ExportSpec spec) {
        if (spec.bbox() != null || spec.placeId() == null) {
            return spec;
        }
        var geometry = places.geometry(spec.placeId());
        if (geometry.isEmpty()) {
            throw ValidationException.forField("placeId", "unknown place");
        }
        double[] box = ExportQueryService.envelopeOf(geometry.get());
        return box == null ? spec : spec.withBbox(box);
    }

    private List<SourceDescriptor> candidates(ExportSpec spec, Caller caller) {
        List<SourceDescriptor> out = new ArrayList<>();
        Set<String> requested = spec.sources().isEmpty() ? null : Set.copyOf(spec.sources());
        for (SourceDescriptor descriptor : registry.descriptors()) {
            boolean requestedSource = requested == null || requested.contains(descriptor.id());
            if (!requestedSource) {
                continue;
            }
            if (!spec.kinds().isEmpty() && descriptor.kinds().stream().noneMatch(spec.kinds()::contains)) {
                continue;
            }
            if (descriptor.privateSource()) {
                if (!(caller.admin() && spec.includePrivate())) {
                    continue;
                }
            }
            out.add(descriptor);
        }
        return out;
    }

    private void checkQuota() {
        long used = directoryBytes(store.exportsRoot());
        long limit = properties.diskQuotaGb() * 1024L * 1024L * 1024L;
        if (used > limit) {
            throw new ForesignalException(HttpStatus.CONFLICT, ErrorCodes.EXPORT_QUOTA,
                    "The export directory holds " + used + " bytes, over the "
                            + properties.diskQuotaGb() + " GiB quota (foresignal.exports.disk-quota-gb).");
        }
    }

    private static long directoryBytes(Path root) {
        if (!Files.isDirectory(root)) {
            return 0L;
        }
        try (var walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile).mapToLong(p -> {
                try {
                    return Files.size(p);
                } catch (IOException e) {
                    return 0L;
                }
            }).sum();
        } catch (IOException e) {
            return 0L;
        }
    }

    @SuppressWarnings("unchecked")
    private ExportSpec specOf(ExportJobEntity job) {
        Map<String, Object> spec = job.getSpec();
        List<String> sources = (List<String>) spec.getOrDefault("sources", List.of());
        List<String> kinds = (List<String>) spec.getOrDefault("kinds", List.of());
        List<Number> bboxRaw = (List<Number>) spec.get("bbox");
        double[] bbox = bboxRaw == null ? null
                : new double[] {bboxRaw.get(0).doubleValue(), bboxRaw.get(1).doubleValue(),
                    bboxRaw.get(2).doubleValue(), bboxRaw.get(3).doubleValue()};
        List<String> fields = (List<String>) spec.getOrDefault("fields", List.of());
        boolean flatten = Boolean.TRUE.equals(spec.get("flatten"));
        boolean includePrivate = Boolean.TRUE.equals(spec.get("includePrivate"));
        boolean personalUse = Boolean.TRUE.equals(spec.get("personalUse"));
        Long atSeq = spec.get("atSeq") == null ? null : ((Number) spec.get("atSeq")).longValue();
        return new ExportSpec((String) spec.get("name"), ExportFormat.parse((String) spec.get("format")), sources,
                kinds, bbox, (String) spec.get("placeId"), instant(spec.get("from")), instant(spec.get("to")),
                (String) spec.get("timeField"), (String) spec.get("mode"), instant(spec.get("asOf")), atSeq, fields,
                flatten, includePrivate, personalUse);
    }

    private List<String> allowedSources(ExportJobEntity job) {
        Object raw = job.getSpec().get("allowedSources");
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private String licenceId(ExportJobEntity job) {
        Object raw = job.getLicenceSummary() == null ? null : job.getLicenceSummary().get("licences");
        if (raw instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> first) {
            return String.valueOf(first.get("id"));
        }
        return "unknown";
    }

    @SuppressWarnings("unchecked")
    private List<String> attributions(ExportJobEntity job) {
        Object raw = job.getLicenceSummary() == null ? null : job.getLicenceSummary().get("attributions");
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private void enrich(Map<String, Object> row, ExportSpec spec, ExportFormat format) {
        if (spec.flatten() && !spec.fields().isEmpty() && row.get("payload") instanceof String payload) {
            try {
                JsonNode node = jsonMapper.readTree(payload);
                for (String field : spec.fields()) {
                    JsonNode value = node.get(field);
                    if (value != null && !value.isNull()) {
                        row.put(field, value.isValueNode() ? value.asString() : value.toString());
                    }
                }
            } catch (RuntimeException ignored) {
                // A malformed payload leaves the flattened columns null.
            }
        }
        if (format == ExportFormat.GPX) {
            row.put("track", registry.find(String.valueOf(row.get("source_id")))
                    .map(r -> r.descriptor().isTrack()).orElse(false));
            if (row.get("bbox") instanceof double[] b) {
                row.put("lon", (b[0] + b[2]) / 2);
                row.put("lat", (b[1] + b[3]) / 2);
            }
        }
    }

    private static void fsync(Path file) throws IOException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
    }

    private static Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean terminal(String status) {
        return ExportJobEntity.DONE.equals(status) || ExportJobEntity.FAILED.equals(status)
                || ExportJobEntity.CANCELLED.equals(status) || ExportJobEntity.EXPIRED.equals(status);
    }

    private static void closeQuietly(ExportWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
                // best effort
            }
        }
    }

    private static void deleteQuietly(Path dir) {
        try {
            if (Files.isDirectory(dir)) {
                try (var walk = Files.walk(dir)) {
                    for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                        Files.deleteIfExists(path);
                    }
                }
            }
        } catch (IOException ignored) {
            // best effort
        }
    }

    /** Thrown by the sink when a running export was cancelled. */
    static final class CancelledExport extends RuntimeException {}
}
