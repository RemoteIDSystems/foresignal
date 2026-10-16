package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.stream.ChangeBus;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * {@code atSeq} snapshots (prompt 111): rebuild the state at a seq S from {@code record_version}
 * (Core-Data-Model §12) into a GeoParquet file with a signed manifest, under
 * {@code ${FORESIGNAL_DATA_DIR}/snapshots/{id}/}. Snapshots never expire unless {@code expiresAt} is
 * set. Below a source's {@code floorSeq} the state comes from {@link HoldService}'s held copies, or
 * the request fails {@code 400 SNAPSHOT_BEYOND_RETENTION}.
 */
@Service
public class SnapshotService {

    private static final Logger log = LoggerFactory.getLogger(SnapshotService.class);

    private final ExportJobRepository jobs;
    private final ExportQueryService query;
    private final ExportLicenceService licences;
    private final ExportManifests manifests;
    private final ExportStore store;
    private final ExportProperties properties;
    private final SourceRegistry registry;
    private final SeqClockQuery seqClock;
    private final RetentionState retention;
    private final HoldService holds;
    private final ChangeBus changeBus;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /** Creates the service. */
    public SnapshotService(ExportJobRepository jobs, ExportQueryService query, ExportLicenceService licences,
            ExportManifests manifests, ExportStore store, ExportProperties properties, SourceRegistry registry,
            SeqClockQuery seqClock, RetentionState retention, HoldService holds, ChangeBus changeBus,
            NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.jobs = jobs;
        this.query = query;
        this.licences = licences;
        this.manifests = manifests;
        this.store = store;
        this.properties = properties;
        this.registry = registry;
        this.seqClock = seqClock;
        this.retention = retention;
        this.holds = holds;
        this.changeBus = changeBus;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** Creates a snapshot job. */
    @Transactional
    public ExportJobEntity create(JsonNode body, String principal) {
        List<String> sources = strings(body, "sources");
        List<String> kinds = strings(body, "kinds");
        Long atSeq = longOrNull(body, "atSeq");
        Instant asOf = instant(body, "asOf");
        long s = atSeq != null ? atSeq
                : asOf != null ? seqClock.seqAt(jdbc, asOf) : changeBus.lastPublishedSeq();
        ExportSpec spec = new ExportSpec(text(body, "name"), ExportFormat.GEOPARQUET, sources, kinds,
                box(body.get("bbox")), null, null, null, "observed", "atSeq", null, s, List.of(), false, false, false);
        List<SourceDescriptor> candidates = new java.util.ArrayList<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (!sources.isEmpty() && !sources.contains(descriptor.id())) {
                continue;
            }
            if (!kinds.isEmpty() && descriptor.kinds().stream().noneMatch(kinds::contains)) {
                continue;
            }
            if (descriptor.privateSource()) {
                continue;
            }
            candidates.add(descriptor);
        }
        ExportLicenceService.Result licence = licences.summarize(candidates, false);
        for (String source : licence.allowedSources()) {
            long floor = retention.lowestRetainedSeq(Set.of(source));
            if (s < floor && !holds.heldBelow(source, s)) {
                throw new ValidationException(ErrorCodes.SNAPSHOT_BEYOND_RETENTION,
                        "atSeq " + s + " is below the retention floor of " + source + " (" + floor + ").", List.of())
                        .withProperty("sourceId", source).withProperty("floorSeq", floor);
            }
        }
        Instant now = clock.instant();
        ExportJobEntity job = new ExportJobEntity(Ulids.next(now), ExportJobEntity.SNAPSHOT, "geoparquet", now);
        job.setPrincipal(principal);
        job.setLicenceSummary(licence.summary());
        Map<String, Object> specMap = spec.toMap();
        specMap.put("allowedSources", licence.allowedSources());
        specMap.put("atSeq", s);
        specMap.put("asOf", asOf == null ? null : asOf.toString());
        job.setSpec(specMap);
        Instant expiresAt = instant(body, "expiresAt");
        job.setExpiresAt(expiresAt);
        jobs.save(job);
        executor.submit(() -> run(job.getId()));
        return job;
    }

    private void run(String id) {
        ExportJobEntity job = jobs.findById(id).orElse(null);
        if (job == null) {
            return;
        }
        job.setStatus(ExportJobEntity.RUNNING);
        job.setStartedAt(clock.instant());
        job.setProgress(1);
        jobs.save(job);
        ExportWriter writer = ExportWriterFactory.create(ExportFormat.GEOPARQUET);
        try {
            long s = ((Number) job.getSpec().get("atSeq")).longValue();
            List<String> sources = allowed(job);
            Path dir = store.snapshotDir(id);
            Path file = dir.resolve(id + ".parquet");
            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("licence", "snapshot");
            writer.open(file, ExportSchema.standard(List.of(), true), metadata);
            AtomicLong rows = new AtomicLong();
            ExportSpec spec = spec(job, s);
            query.stream(spec, sources, ai.foresignal.security.restrict.ReadRestriction.NONE,
                    new ExportQueryService.Sink() {
                        @Override
                        public void begin(long watermarkSeq) {
                            job.setWatermarkSeq(watermarkSeq);
                        }

                        @Override
                        public void row(Map<String, Object> row) {
                            if (rows.incrementAndGet() > properties.maxRows()) {
                                throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.EXPORT_SPEC_INVALID,
                                        "The snapshot exceeds foresignal.exports.max-rows.");
                            }
                            try {
                                writer.write(row);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                    });
            writer.close();
            try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            long bytes = Files.size(file);
            String sha256 = ExportManifests.sha256(file);
            List<ExportManifests.ExportFile> files = List.of(new ExportManifests.ExportFile(file.getFileName().toString(),
                    "application/vnd.apache.parquet", bytes, sha256, rows.get()));
            String manifestSha = manifests.write(dir, job, files, s, s, job.getLicenceSummary(), attributions(job));
            job.setRowCount(rows.get());
            job.setBytes(bytes);
            job.setManifestSha256(manifestSha);
            job.setStatus(ExportJobEntity.DONE);
            job.setProgress(100);
            job.setFinishedAt(clock.instant());
            jobs.save(job);
        } catch (IOException | RuntimeException e) {
            log.warn("Snapshot {} failed", id, e);
            job.setStatus(ExportJobEntity.FAILED);
            job.setError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            job.setFinishedAt(clock.instant());
            jobs.save(job);
        }
    }

    /** The snapshot JSON view (ExportJob plus atSeq/asOf/name/heldSources). */
    public Map<String, Object> view(ExportJobEntity job) {
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
        out.put("personalUse", false);
        out.put("atSeq", job.getSpec().get("atSeq"));
        out.put("asOf", job.getSpec().get("asOf"));
        out.put("name", job.getSpec().get("name"));
        out.put("heldSources", List.of());
        out.put("createdAt", job.getCreatedAt().toString());
        out.put("finishedAt", job.getFinishedAt() == null ? null : job.getFinishedAt().toString());
        out.put("expiresAt", job.getExpiresAt() == null ? null : job.getExpiresAt().toString());
        out.put("error", job.getError());
        return out;
    }

    private ExportSpec spec(ExportJobEntity job, long s) {
        Map<String, Object> map = job.getSpec();
        @SuppressWarnings("unchecked")
        List<String> sources = (List<String>) map.getOrDefault("sources", List.of());
        @SuppressWarnings("unchecked")
        List<String> kinds = (List<String>) map.getOrDefault("kinds", List.of());
        @SuppressWarnings("unchecked")
        List<Number> bboxRaw = (List<Number>) map.get("bbox");
        double[] bbox = bboxRaw == null ? null
                : new double[] {bboxRaw.get(0).doubleValue(), bboxRaw.get(1).doubleValue(),
                    bboxRaw.get(2).doubleValue(), bboxRaw.get(3).doubleValue()};
        return new ExportSpec((String) map.get("name"), ExportFormat.GEOPARQUET, sources, kinds, bbox, null, null, null,
                "observed", "atSeq", null, s, List.of(), false, false, false);
    }

    @SuppressWarnings("unchecked")
    private static List<String> allowed(ExportJobEntity job) {
        Object raw = job.getSpec().get("allowedSources");
        return raw instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
    }

    @SuppressWarnings("unchecked")
    private static List<String> attributions(ExportJobEntity job) {
        Object raw = job.getLicenceSummary() == null ? null : job.getLicenceSummary().get("attributions");
        return raw instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
    }

    private static String text(JsonNode body, String field) {
        JsonNode node = body == null ? null : body.get(field);
        return node == null || node.isNull() ? null : node.asString();
    }

    private static List<String> strings(JsonNode body, String field) {
        JsonNode node = body == null ? null : body.get(field);
        if (node == null || node.isNull() || !node.isArray()) {
            return List.of();
        }
        List<String> out = new java.util.ArrayList<>();
        for (JsonNode item : node) {
            out.add(item.asString());
        }
        return out;
    }

    private static double[] box(JsonNode node) {
        if (node == null || node.isNull() || !node.isArray() || node.size() != 4) {
            return null;
        }
        return new double[] {node.get(0).asDouble(), node.get(1).asDouble(), node.get(2).asDouble(),
            node.get(3).asDouble()};
    }

    private static Instant instant(JsonNode body, String field) {
        String value = text(body, field);
        return value == null || value.isBlank() ? null : Instant.parse(value);
    }

    private static Long longOrNull(JsonNode body, String field) {
        JsonNode node = body == null ? null : body.get(field);
        return node == null || node.isNull() ? null : node.asLong();
    }
}
