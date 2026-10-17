package ai.foresignal.baserate.reference;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.manifest.CanonicalJson;
import ai.foresignal.security.Ulids;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reference-history imports (prompt 121): start, list, read and delete them. Each import runs as a 103 job of type
 * {@value ReferenceImportJobRunner#TYPE} and is idempotent per {@code (dataset, period)}: the same dataset and
 * normalized parameters reuse one import row, returned as it is while queued or running and otherwise run again,
 * resuming at its first unfinished chunk. A dataset that cannot be read is refused with 409
 * {@code IMPORT_DATASET_UNAVAILABLE}; ACLED without credentials is recorded as {@code skipped}. A finished import asks
 * for a base-rate refresh so new rates exist without waiting for the monthly one.
 */
@Service
public class ReferenceImportService {

    /** The statuses. */
    public static final List<String> STATUSES = List.of("queued", "running", "done", "failed", "skipped", "cancelled");

    private static final Logger log = LoggerFactory.getLogger(ReferenceImportService.class);
    private static final List<String> OPEN = List.of("queued", "running");

    private final ReferenceImportRepository imports;
    private final ReferenceHistoryWriter writer;
    private final List<ReferenceImporter> importers;
    private final BacktestJobService jobs;
    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Called after an import finishes (the base-rate refresh request), or null. */
    @Autowired(required = false)
    private ImportListener listener;

    /** Notified when an import finishes successfully. */
    @FunctionalInterface
    public interface ImportListener {

        /** An import finished. */
        void finished(ReferenceImportEntity entity);
    }

    /** Creates the service. */
    public ReferenceImportService(ReferenceImportRepository imports, ReferenceHistoryWriter writer,
            List<ReferenceImporter> importers, BacktestJobService jobs, NamedParameterJdbcTemplate jdbc,
            JsonMapper json, Clock clock) {
        this.imports = imports;
        this.writer = writer;
        this.importers = List.copyOf(importers);
        this.jobs = jobs;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A page.
     *
     * @param items the imports
     * @param nextCursor the next cursor, or null
     */
    public record Page(List<ReferenceImportEntity> items, String nextCursor) {}

    /** The importer of a dataset, or null. */
    public ReferenceImporter importer(String dataset) {
        for (ReferenceImporter i : importers) {
            if (i.dataset().equals(dataset)) {
                return i;
            }
        }
        return null;
    }

    /**
     * Starts (or resumes) an import ({@code ReferenceImportRequest}).
     *
     * @param body the request
     * @param actor who asked
     * @return the import (queued, running, or skipped)
     * @throws ValidationException 400 for an unknown dataset or invalid parameters
     * @throws ConflictException 409 {@code IMPORT_DATASET_UNAVAILABLE}
     */
    @Transactional
    public ReferenceImportEntity create(JsonNode body, String actor) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a ReferenceImportRequest object.");
        }
        for (String field : body.propertyNames()) {
            if (!field.equals("dataset") && !field.equals("params")) {
                throw ValidationException.forField(field, "is not a ReferenceImportRequest member");
            }
        }
        String dataset = body.hasNonNull("dataset") ? body.get("dataset").asString() : null;
        ReferenceImporter importer = dataset == null ? null : importer(dataset);
        if (importer == null) {
            throw ValidationException.forField("dataset", "must be one of " + ReferenceFamilies.DATASETS);
        }
        JsonNode paramsNode = body.get("params");
        if (paramsNode != null && !paramsNode.isNull() && !paramsNode.isObject()) {
            throw ValidationException.forField("params", "must be an object");
        }
        Map<String, Object> raw = paramsNode == null || paramsNode.isNull() ? new LinkedHashMap<>()
                : json.readValue(json.writeValueAsString(paramsNode), new TypeReference<LinkedHashMap<String, Object>>() {});
        Map<String, Object> params = importer.normalize(raw);
        ReferenceImporter.Availability availability = importer.availability(params);
        if (availability.state() == ReferenceImporter.Availability.State.UNAVAILABLE) {
            throw new ConflictException(ErrorCodes.IMPORT_DATASET_UNAVAILABLE, "The " + dataset + " dataset is not"
                    + " available: " + availability.reason() + ".");
        }
        String key = key(dataset, params);
        List<ReferenceImportEntity> existing = imports.findByImportKeyOrderByCreatedAtDesc(key);
        ReferenceImportEntity entity;
        if (!existing.isEmpty()) {
            entity = existing.get(0);
            if (OPEN.contains(entity.getStatus())) {
                return entity;
            }
        } else {
            entity = new ReferenceImportEntity(Ulids.next(clock.instant()), dataset, key,
                    actor == null ? "operator" : actor, clock.instant());
            entity.setParams(params);
        }
        String source = importer.source();
        entity.setLicense(ReferenceFamilies.LICENSES.get(source));
        entity.setEgress(ReferenceFamilies.EGRESS.get(source));
        LocalDate[] period = importer.period(params);
        if (period != null && entity.getPeriodFrom() == null) {
            entity.setPeriodFrom(period[0]);
            entity.setPeriodTo(period[1]);
        }
        entity.setError(null);
        entity.setFinishedAt(null);
        if (availability.state() == ReferenceImporter.Availability.State.SKIP) {
            entity.setStatus("skipped");
            entity.setStatusReason(availability.reason());
            entity.setFinishedAt(clock.instant());
            return imports.save(entity);
        }
        entity.setStatus("queued");
        entity.setStatusReason(null);
        imports.save(entity);
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("importId", entity.getId());
        definition.put("dataset", dataset);
        definition.put("params", params);
        BacktestJobEntity job = jobs.submitRunnerJob(ReferenceImportJobRunner.TYPE, entity.getId(), definition, null);
        entity.setJobId(job.getId());
        return imports.save(entity);
    }

    /** The idempotency key: SHA-256 of the dataset and the canonical JSON of the normalized parameters. */
    public String key(String dataset, Map<String, Object> params) {
        return CanonicalJson.sha256(dataset + "|" + CanonicalJson.write(params, json));
    }

    /**
     * Lists imports newest first.
     *
     * @param dataset a dataset, or null
     * @param status a status, or null
     * @param cursor the cursor, or null
     * @param limit the page size
     * @return the page
     */
    @Transactional(readOnly = true)
    public Page list(String dataset, String status, String cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id FROM reference_import WHERE 1 = 1");
        MapSqlParameterSource p = new MapSqlParameterSource("limit", limit + 1);
        if (dataset != null) {
            sql.append(" AND dataset = :dataset");
            p.addValue("dataset", dataset);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            p.addValue("status", status);
        }
        String after = decode(cursor);
        if (after != null) {
            sql.append(" AND id < :after");
            p.addValue("after", after);
        }
        sql.append(" ORDER BY id DESC LIMIT :limit");
        List<String> ids = jdbc.queryForList(sql.toString(), p, String.class);
        List<ReferenceImportEntity> items = new ArrayList<>();
        for (int i = 0; i < ids.size() && i < limit; i++) {
            imports.findById(ids.get(i)).ifPresent(items::add);
        }
        String next = ids.size() > limit && !items.isEmpty() ? encode(items.get(items.size() - 1).getId()) : null;
        return new Page(items, next);
    }

    /**
     * One import.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_IMPORT}
     */
    @Transactional(readOnly = true)
    public ReferenceImportEntity get(String id) {
        return (id == null ? java.util.Optional.<ReferenceImportEntity>empty() : imports.findById(id))
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_IMPORT, "No reference import '" + id
                        + "'."));
    }

    /**
     * Deletes an import and the rows it wrote; a queued or running import's job is canceled first.
     *
     * @return the rows deleted
     * @throws NotFoundException 404 {@code UNKNOWN_IMPORT}
     */
    @Transactional
    public int delete(String id) {
        ReferenceImportEntity entity = get(id);
        if (OPEN.contains(entity.getStatus()) && entity.getJobId() != null) {
            try {
                jobs.cancel(entity.getJobId());
            } catch (RuntimeException e) {
                log.debug("Reference import {}: its job could not be canceled: {}", id, e.getMessage());
            }
        }
        int rows = writer.deleteImport(id);
        imports.delete(entity);
        log.info("Reference import {} ({}) deleted with {} weekly rows", id, entity.getDataset(), rows);
        return rows;
    }

    /**
     * Runs an import as its 103 job (the {@link ReferenceImportJobRunner}).
     *
     * @param importId the import
     * @param monitor the job monitor
     * @return the import's {@code ReferenceImport} view
     */
    public Map<String, Object> run(String importId, JobMonitor monitor) {
        ReferenceImportEntity entity = imports.findById(importId).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_IMPORT, "No reference import '" + importId + "'."));
        ReferenceImporter importer = importer(entity.getDataset());
        entity.setStatus("running");
        entity.setStartedAt(clock.instant());
        entity.setError(null);
        imports.save(entity);
        ImportRun run = new ImportRun(entity, writer, imports, monitor);
        try {
            importer.run(run);
            entity.setStatus("done");
            entity.setFinishedAt(clock.instant());
            imports.save(entity);
            log.info("Reference import {} ({}) done: {} rows read, {} weekly rows written", importId,
                    entity.getDataset(), entity.getRowsRead(), entity.getRowsWritten());
            notifyListener(entity);
            return ReferenceImportViews.view(entity);
        } catch (JobMonitor.Canceled e) {
            entity.setStatus("cancelled");
            entity.setFinishedAt(clock.instant());
            imports.save(entity);
            throw e;
        } catch (Exception e) {
            entity.setStatus("failed");
            entity.setError(String.valueOf(e.getMessage() == null ? e : e.getMessage()));
            entity.setFinishedAt(clock.instant());
            imports.save(entity);
            log.warn("Reference import {} ({}) failed: {}", importId, entity.getDataset(), e.toString());
            throw new IllegalStateException(entity.getError(), e);
        }
    }

    private void notifyListener(ReferenceImportEntity entity) {
        try {
            if (listener != null) {
                listener.finished(entity);
            }
        } catch (RuntimeException e) {
            log.debug("Reference import {}: the follow-up refresh was not started: {}", entity.getId(),
                    e.getMessage());
        }
    }

    private static String encode(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String id = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (Ulids.isUlid(id)) {
                return id;
            }
        } catch (IllegalArgumentException e) {
            // fall through
        }
        throw ValidationException.forField("cursor", "is not a cursor of this list");
    }
}
