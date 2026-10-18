package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.retention.RetentionStateStore;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Legal-style holds (prompt 111): create, list, read held versions, delete, and the bounds and
 * coverage questions the hold copy and snapshots ask.
 *
 * <p>A hold's range is given as seqs ({@code fromSeq}, {@code toSeq}, inclusive) or as times
 * ({@code from}, {@code to}) on the transaction axis: a version is in range when it was ingested at
 * or after {@code from} and at or before {@code to}. Times are translated through {@code seq_clock}
 * when the copy runs ({@link #lowerSeq}, {@link #upperSeq}); a {@code to} still in the future leaves
 * the range open.
 */
@Service
public class HoldService {

    private final HoldRepository holds;
    private final NamedParameterJdbcTemplate jdbc;
    private final SeqClockQuery seqClock;
    private final SourceRegistry registry;
    private final RetentionStateStore retention;
    private final ExportProperties properties;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    /** Creates the service. */
    public HoldService(HoldRepository holds, NamedParameterJdbcTemplate jdbc, SeqClockQuery seqClock,
            SourceRegistry registry, RetentionStateStore retention, ExportProperties properties, JsonMapper jsonMapper,
            Clock clock) {
        this.holds = holds;
        this.jdbc = jdbc;
        this.seqClock = seqClock;
        this.registry = registry;
        this.retention = retention;
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    /**
     * Creates a hold from a {@code HoldInput} body.
     *
     * @throws ValidationException {@code 400} for a missing name or reason, an unknown source, a bad
     *     date or reversed bounds
     * @throws ForesignalException {@code 409 HOLD_LIMIT} when {@code holds.max} holds exist
     */
    @Transactional
    public HoldEntity create(JsonNode body, String principal) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a HoldInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        String name = text(body, "name");
        String reason = text(body, "reason");
        if (name == null || name.isBlank() || name.length() > 200) {
            errors.add(new FieldError("name", "is required, at most 200 characters"));
        }
        if (reason == null || reason.isBlank() || reason.length() > 2000) {
            errors.add(new FieldError("reason", "is required, at most 2000 characters"));
        }
        List<String> sources = strings(body, "sources", errors);
        for (String source : sources) {
            if (!registry.contains(source)) {
                errors.add(new FieldError("sources", "unknown source " + source));
            }
        }
        List<String> kinds = strings(body, "kinds", errors);
        Instant from = instant(body, "from", errors);
        Instant to = instant(body, "to", errors);
        Instant expiresAt = instant(body, "expiresAt", errors);
        Long fromSeq = longOrNull(body, "fromSeq", errors);
        Long toSeq = longOrNull(body, "toSeq", errors);
        if (from != null && to != null && to.isBefore(from)) {
            errors.add(new FieldError("to", "must not be before from"));
        }
        if (fromSeq != null && toSeq != null && toSeq < fromSeq) {
            errors.add(new FieldError("toSeq", "must not be below fromSeq"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The hold is not valid.", errors);
        }
        if (holds.count() >= properties.holdsMax()) {
            throw new ForesignalException(HttpStatus.CONFLICT, ErrorCodes.HOLD_LIMIT,
                    "At most " + properties.holdsMax() + " holds may exist (holds.max).");
        }
        Instant now = clock.instant();
        HoldEntity hold = new HoldEntity(Ulids.next(now), name, reason, now);
        hold.setCreatedBy(principal);
        hold.setSources(sources);
        hold.setKinds(kinds);
        hold.setFrom(from);
        hold.setTo(to);
        hold.setFromSeq(fromSeq);
        hold.setToSeq(toSeq);
        hold.setExpiresAt(expiresAt);
        Map<String, Long> floors = new TreeMap<>();
        Collection<String> covered = sources.isEmpty()
                ? registry.descriptors().stream().map(SourceDescriptor::id).toList() : sources;
        for (String source : covered) {
            floors.put(source, retention.floorSeq(source));
        }
        hold.setFloorsAtCreate(floors);
        holds.save(hold);
        return hold;
    }

    /** One page of holds, newest first, keyset on the id. */
    public Page list(String cursor, int limit) {
        int size = Math.max(1, Math.min(500, limit));
        List<HoldEntity> rows = cursor == null || cursor.isBlank()
                ? holds.findAllByOrderByIdDesc(Limit.of(size + 1))
                : holds.findByIdLessThanOrderByIdDesc(cursor, Limit.of(size + 1));
        String next = null;
        if (rows.size() > size) {
            rows = rows.subList(0, size);
            next = rows.getLast().getId();
        }
        return new Page(List.copyOf(rows), next);
    }

    /**
     * A page of holds.
     *
     * @param items the holds
     * @param nextCursor the cursor of the next page, or null
     */
    public record Page(List<HoldEntity> items, String nextCursor) {}

    /** A hold, {@code 404 UNKNOWN_HOLD} when unknown. */
    public HoldEntity get(String id) {
        return holds.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_HOLD,
                "Unknown hold " + id + "."));
    }

    /** Deletes a hold and every row it copied. */
    @Transactional
    public void delete(String id) {
        HoldEntity hold = get(id);
        jdbc.update("DELETE FROM " + HeldVersionsTable.NAME + " WHERE hold_id = :id",
                new MapSqlParameterSource("id", id));
        holds.delete(hold);
    }

    /** The holds that have not expired at {@code now}. */
    public List<HoldEntity> active(Instant now) {
        List<HoldEntity> out = new ArrayList<>();
        for (HoldEntity hold : holds.findAll()) {
            if (!hold.expired(now)) {
                out.add(hold);
            }
        }
        return out;
    }

    /**
     * The inclusive lower seq bound of {@code hold}: {@code fromSeq}, else the first seq ingested at
     * or after {@code from}; null when unbounded.
     */
    public Long lowerSeq(HoldEntity hold, NamedParameterJdbcTemplate template) {
        Long bound = hold.getFromSeq();
        if (hold.getFrom() != null) {
            long fromTime = seqClock.lastSeqBefore(template, hold.getFrom()) + 1;
            bound = bound == null ? fromTime : Math.max(bound, fromTime);
        }
        return bound;
    }

    /**
     * The inclusive upper seq bound of {@code hold} at {@code now}: {@code toSeq}, else the last seq
     * ingested at or before {@code to}; null when open (no bound, or {@code to} still in the future).
     */
    public Long upperSeq(HoldEntity hold, NamedParameterJdbcTemplate template, Instant now) {
        Long bound = hold.getToSeq();
        if (hold.getTo() != null && !hold.getTo().isAfter(now)) {
            long toTime = seqClock.seqAt(template, hold.getTo());
            bound = bound == null ? toTime : Math.min(bound, toTime);
        }
        return bound;
    }

    /**
     * The active holds whose copies hold every version of {@code sourceId} (of {@code kinds}, empty
     * for any) with a seq in {@code [lower, upper]}: the hold covers the source and those kinds, its
     * range includes the interval, and the source's retention floor was at or below {@code lower}
     * when the hold was created (versions already gone then were never copied). Empty when none.
     */
    public List<HoldEntity> covering(String sourceId, Collection<String> kinds, long lower, long upper) {
        Instant now = clock.instant();
        List<HoldEntity> out = new ArrayList<>();
        for (HoldEntity hold : active(now)) {
            if (!hold.coversSource(sourceId)) {
                continue;
            }
            if (!hold.getKinds().isEmpty() && (kinds == null || kinds.isEmpty() || !hold.getKinds().containsAll(kinds))) {
                continue;
            }
            Long holdLower = lowerSeq(hold, jdbc);
            Long holdUpper = upperSeq(hold, jdbc, now);
            if (holdLower != null && holdLower > lower) {
                continue;
            }
            if (holdUpper != null && holdUpper < upper) {
                continue;
            }
            Long floorAtCreate = hold.getFloorsAtCreate().get(sourceId);
            if (floorAtCreate == null || floorAtCreate > lower) {
                continue;
            }
            out.add(hold);
        }
        return out;
    }

    /** The JSON view of a hold ({@code Hold}). */
    public Map<String, Object> view(HoldEntity hold) {
        Counts counts = counts(hold.getId());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", hold.getId());
        out.put("name", hold.getName());
        out.put("reason", hold.getReason());
        out.put("sources", hold.getSources());
        out.put("kinds", hold.getKinds());
        out.put("from", hold.getFrom() == null ? null : hold.getFrom().toString());
        out.put("to", hold.getTo() == null ? null : hold.getTo().toString());
        out.put("fromSeq", hold.getFromSeq());
        out.put("toSeq", hold.getToSeq());
        out.put("expiresAt", hold.getExpiresAt() == null ? null : hold.getExpiresAt().toString());
        out.put("createdAt", hold.getCreatedAt().toString());
        out.put("createdBy", hold.getCreatedBy());
        out.put("heldRows", counts.rows());
        out.put("heldBytes", counts.bytes());
        return out;
    }

    /** Held rows and approximate bytes of one hold. */
    public Counts counts(String holdId) {
        return jdbc.queryForObject("SELECT count(*) AS n, coalesce(sum(pg_column_size(h.*)), 0) AS b FROM "
                        + HeldVersionsTable.NAME + " h WHERE h.hold_id = :id",
                new MapSqlParameterSource("id", holdId), (rs, i) -> new Counts(rs.getLong("n"), rs.getLong("b")));
    }

    /** Held rows and bytes over every hold, for {@code /v1/system}. */
    public Counts totals() {
        return jdbc.queryForObject("SELECT count(*) AS n, coalesce(pg_total_relation_size('"
                        + HeldVersionsTable.NAME + "'), 0) AS b FROM " + HeldVersionsTable.NAME,
                new MapSqlParameterSource(), (rs, i) -> new Counts(rs.getLong("n"), rs.getLong("b")));
    }

    /**
     * One keyset page of a hold's versions, by seq, as {@code RecordVersion} items.
     *
     * @param after the seq of the previous page's last item, or null for the first page
     */
    public Map<String, Object> versions(String holdId, Long after, int limit) {
        get(holdId);
        int size = Math.max(1, Math.min(500, limit));
        List<Map<String, Object>> items = jdbc.query(
                "SELECT source_id, external_id, kind, seq, version, observed_at, received_at, event_time, valid_until,"
                        + " ingested_at, tombstone, persist_reason, prev_seq, history_tier, batch_id,"
                        + " payload::text AS payload, ST_AsGeoJSON(geom) AS geometry"
                        + " FROM " + HeldVersionsTable.NAME
                        + " WHERE hold_id = :id AND seq > :after ORDER BY seq LIMIT " + (size + 1),
                new MapSqlParameterSource("id", holdId).addValue("after", after == null ? -1L : after),
                (rs, i) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getString("source_id") + ":" + rs.getString("external_id"));
                    row.put("source", rs.getString("source_id"));
                    row.put("externalId", rs.getString("external_id"));
                    row.put("kind", rs.getString("kind"));
                    row.put("version", rs.getInt("version"));
                    row.put("seq", rs.getLong("seq"));
                    row.put("observedAt", iso(rs.getObject("observed_at", OffsetDateTime.class)));
                    row.put("eventTime", iso(rs.getObject("event_time", OffsetDateTime.class)));
                    row.put("validUntil", iso(rs.getObject("valid_until", OffsetDateTime.class)));
                    row.put("ingestedAt", iso(rs.getObject("ingested_at", OffsetDateTime.class)));
                    row.put("receivedAt", iso(rs.getObject("received_at", OffsetDateTime.class)));
                    row.put("tombstone", rs.getBoolean("tombstone"));
                    row.put("persistReason", PersistReason.fromCode(rs.getInt("persist_reason")).name());
                    long prev = rs.getLong("prev_seq");
                    row.put("prevSeq", rs.wasNull() ? null : prev);
                    row.put("historyTier", HistoryTier.fromCode(rs.getShort("history_tier")).name());
                    long batch = rs.getLong("batch_id");
                    row.put("batchId", rs.wasNull() ? null : batch);
                    String geometry = rs.getString("geometry");
                    row.put("geometry", geometry == null ? null : jsonMapper.readTree(geometry));
                    String payload = rs.getString("payload");
                    row.put("payload", payload == null ? null : jsonMapper.readTree(payload));
                    return row;
                });
        String next = null;
        if (items.size() > size) {
            items = new ArrayList<>(items.subList(0, size));
            next = String.valueOf(items.getLast().get("seq"));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", next);
        return out;
    }

    /**
     * Held rows and bytes.
     *
     * @param rows the row count
     * @param bytes the approximate bytes
     */
    public record Counts(long rows, long bytes) {}

    private static String iso(OffsetDateTime value) {
        return value == null ? null : value.toInstant().toString();
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
            if (!item.isString() || item.asString().isBlank()) {
                errors.add(new FieldError(field, "must be an array of non-blank strings"));
                return List.of();
            }
            if (!out.contains(item.asString())) {
                out.add(item.asString());
            }
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

    private static Long longOrNull(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.canConvertToLong() || !node.isIntegralNumber() || node.asLong() < 0) {
            errors.add(new FieldError(field, "must be a non-negative integer"));
            return null;
        }
        return node.asLong();
    }
}
