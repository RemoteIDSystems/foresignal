package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * {@code atSeq} snapshots (prompt 111, {@code createSnapshot}): the state at seq {@code S} (the
 * request's {@code atSeq}, else {@code S(asOf)}, else the current watermark), rebuilt from
 * {@code record_version} with the Core-Data-Model §12 query per tier and written as GeoParquet with
 * the signed manifest and sidecars under {@code ${FORESIGNAL_DATA_DIR}/snapshots/{id}/}.
 *
 * <p>Validity is judged at the transaction time of {@code S} (or the requested {@code asOf}). Where
 * the needed versions are below a source's retention floor they are read from the copies of holds
 * that cover them, else the request fails {@code 400 SNAPSHOT_BEYOND_RETENTION}
 * ({@link PointInTimePlanner}). A snapshot is an export job of kind {@code SNAPSHOT}: listed, polled
 * and downloaded through {@code /v1/exports}, run by the same bounded worker, and kept until its
 * {@code expiresAt} (never, when unset). The endpoint is admin-only; private sources are included
 * only with {@code includePrivate=true}, and {@code FORBIDDEN}/{@code LOCAL_ONLY} sources only with
 * {@code personalUse=true}, as for exports.
 */
@Service
public class SnapshotService {

    private final ExportJobService exports;
    private final BulkExportJobRepository jobs;
    private final ExportLicenceService licenses;
    private final PointInTimePlanner planner;
    private final ExportStore store;
    private final Clock clock;

    /** Creates the service. */
    public SnapshotService(ExportJobService exports, BulkExportJobRepository jobs, ExportLicenceService licenses,
            PointInTimePlanner planner, ExportStore store, Clock clock) {
        this.exports = exports;
        this.jobs = jobs;
        this.licenses = licenses;
        this.planner = planner;
        this.store = store;
        this.clock = clock;
    }

    /**
     * Creates a snapshot job from a {@code SnapshotInput} body.
     *
     * @throws ValidationException {@code 400} for a bad body, {@code SNAPSHOT_BEYOND_RETENTION} when
     *     a source's state at {@code S} is gone and not held, {@code EXPORT_SPEC_INVALID} when no
     *     source may be exported
     */
    @Transactional
    public ExportJobEntity create(JsonNode body, ExportJobService.Caller caller) {
        if (body == null || !body.isObject()) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID, "The request body must be a SnapshotInput.",
                    List.of());
        }
        List<FieldError> errors = new ArrayList<>();
        String name = text(body, "name");
        List<String> sources = strings(body, "sources", errors);
        List<String> kinds = strings(body, "kinds", errors);
        Long atSeq = null;
        if (body.hasNonNull("atSeq")) {
            if (body.get("atSeq").isIntegralNumber() && body.get("atSeq").asLong() >= 0) {
                atSeq = body.get("atSeq").asLong();
            } else {
                errors.add(new FieldError("atSeq", "must be a non-negative integer"));
            }
        }
        Instant asOf = instant(body, "asOf", errors);
        Instant expiresAt = instant(body, "expiresAt", errors);
        if (atSeq != null && asOf != null) {
            errors.add(new FieldError("asOf", "give atSeq or asOf, not both"));
        }
        boolean includePrivate = body.has("includePrivate") && body.get("includePrivate").asBoolean(false);
        boolean personalUse = body.has("personalUse") && body.get("personalUse").asBoolean(false);
        double[] bbox = null;
        if (body.hasNonNull("bbox")) {
            JsonNode node = body.get("bbox");
            if (node.isArray() && node.size() == 4) {
                bbox = new double[] {node.get(0).asDouble(), node.get(1).asDouble(), node.get(2).asDouble(),
                    node.get(3).asDouble()};
            } else {
                errors.add(new FieldError("bbox", "must be [minLon, minLat, maxLon, maxLat]"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID, "The snapshot request is not valid.", errors);
        }
        ExportJobService.requirePrivateRights(includePrivate, caller);
        long s = atSeq != null ? atSeq : asOf != null ? planner.seqAt(asOf) : planner.watermark();
        ExportLicenceService.Decision decision = licenses.decide(exports.candidates(sources, kinds), caller.viewer(),
                caller.admin(), includePrivate, personalUse);
        ExportJobService.requireSources(decision);
        PointInTime pit = planner.plan(s, asOf, decision.allowed(), kinds);
        exports.checkQuota();
        ExportSpec spec = new ExportSpec(name, ExportFormat.GEOPARQUET, sources, kinds, bbox, null, null, null,
                "effective", "atSeq", asOf, s, List.of(), false, includePrivate, personalUse);
        Instant now = clock.instant();
        ExportJobEntity job = new ExportJobEntity(Ulids.next(now), ExportJobEntity.SNAPSHOT,
                ExportFormat.GEOPARQUET.wire(), now);
        job.setPrincipal(caller.principal());
        job.setPersonalUse(!decision.personalUseSources().isEmpty());
        job.setExpiresAt(expiresAt);
        Map<String, Object> specMap = spec.toMap();
        specMap.put("expiresAt", expiresAt == null ? null : expiresAt.toString());
        job.setSpec(specMap);
        Map<String, Object> plan = ExportJobService.basePlan(decision);
        plan.put("pointInTime", PointInTimePlanner.toMap(pit));
        job.setPlan(plan);
        job.setLicenceSummary(licenses.summary(decision, Map.of()));
        job.setWatermarkSeq(s);
        job.setDir(store.dirOf(job).toString());
        jobs.save(job);
        exports.enqueue(job.getId(), caller.restriction());
        return job;
    }

    /** The {@code Snapshot} view: the {@code ExportJob} view plus the position and the held sources. */
    public Map<String, Object> view(ExportJobEntity job, String base) {
        Map<String, Object> out = exports.view(job, base);
        Map<String, Object> spec = job.getSpec() == null ? Map.of() : job.getSpec();
        out.put("atSeq", spec.get("atSeq"));
        out.put("asOf", spec.get("asOf"));
        out.put("name", spec.get("name"));
        List<String> held = new ArrayList<>();
        if (job.getPlan().get("pointInTime") instanceof Map<?, ?> pit && pit.get("held") instanceof Map<?, ?> map) {
            map.keySet().forEach(source -> held.add(String.valueOf(source)));
        }
        out.put("heldSources", held);
        return out;
    }

    private static String text(JsonNode body, String field) {
        JsonNode node = body.get(field);
        return node == null || node.isNull() ? null : node.asString();
    }

    private static List<String> strings(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            errors.add(new FieldError(field, "must be an array of strings"));
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            out.add(item.asString());
        }
        return out;
    }

    private static Instant instant(JsonNode body, String field, List<FieldError> errors) {
        String value = text(body, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (RuntimeException e) {
            errors.add(new FieldError(field, "must be an ISO-8601 date-time"));
            return null;
        }
    }
}
