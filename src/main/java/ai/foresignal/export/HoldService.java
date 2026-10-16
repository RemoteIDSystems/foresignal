package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Ulids;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Legal holds (prompt 111): create, list, delete and read held versions. A hold survives retention
 * because {@link HoldCopyStep} copies the rows it covers into {@link ExportSchemaInitializer#HELD_TABLE}
 * before retention deletes them.
 */
@Service
public class HoldService {

    /** The largest number of holds. */
    public static final int MAX_HOLDS = 20;

    private final HoldRepository holds;
    private final NamedParameterJdbcTemplate jdbc;
    private final SeqClockQuery seqClock;
    private final Clock clock;

    /** Creates the service. */
    public HoldService(HoldRepository holds, NamedParameterJdbcTemplate jdbc, SeqClockQuery seqClock, Clock clock) {
        this.holds = holds;
        this.jdbc = jdbc;
        this.seqClock = seqClock;
        this.clock = clock;
    }

    /** Creates a hold. */
    @Transactional
    public HoldEntity create(JsonNode body, String principal) {
        if (holds.count() >= MAX_HOLDS) {
            throw new ai.foresignal.api.error.ForesignalException(org.springframework.http.HttpStatus.CONFLICT,
                    ErrorCodes.HOLD_LIMIT, "At most " + MAX_HOLDS + " legal holds are allowed.");
        }
        String name = text(body, "name");
        String reason = text(body, "reason");
        if (name == null || name.isBlank()) {
            throw ValidationException.forField("name", "is required");
        }
        if (reason == null || reason.isBlank()) {
            throw ValidationException.forField("reason", "is required");
        }
        Instant now = clock.instant();
        HoldEntity hold = new HoldEntity(Ulids.next(now), name, reason, now);
        hold.setCreatedBy(principal);
        hold.setSources(strings(body, "sources"));
        hold.setKinds(strings(body, "kinds"));
        Instant from = instant(body, "from");
        Instant to = instant(body, "to");
        hold.setFrom(from);
        hold.setTo(to);
        Long fromSeq = longOrNull(body, "fromSeq");
        Long toSeq = longOrNull(body, "toSeq");
        if (fromSeq == null && from != null) {
            fromSeq = seqClock.seqAt(jdbc, from);
        }
        if (toSeq == null && to != null) {
            toSeq = seqClock.seqAt(jdbc, to);
        }
        hold.setFromSeq(fromSeq);
        hold.setToSeq(toSeq);
        hold.setExpiresAt(instant(body, "expiresAt"));
        holds.save(hold);
        return hold;
    }

    /** Every hold, newest first. */
    public List<HoldEntity> list() {
        return holds.findAllByOrderByCreatedAtDesc();
    }

    /** A hold, 404 when unknown. */
    public HoldEntity get(String id) {
        return holds.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_HOLD,
                "Unknown hold " + id + "."));
    }

    /** Deletes a hold and its held rows. */
    @Transactional
    public void delete(String id) {
        HoldEntity hold = get(id);
        jdbc.update("DELETE FROM " + ExportSchemaInitializer.HELD_TABLE + " WHERE hold_id = :id",
                new MapSqlParameterSource("id", id));
        holds.delete(hold);
    }

    /** The holds that cover records now. */
    public List<HoldEntity> active(Instant now) {
        List<HoldEntity> out = new ArrayList<>();
        for (HoldEntity hold : holds.findAll()) {
            if (!hold.expired(now)) {
                out.add(hold);
            }
        }
        return out;
    }

    /** The JSON view of a hold. */
    public Map<String, Object> view(HoldEntity hold) {
        Counts counts = counts(hold.getId());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", hold.getId());
        out.put("name", hold.getName());
        out.put("reason", hold.getReason());
        out.put("sources", hold.getSources() == null ? List.of() : hold.getSources());
        out.put("kinds", hold.getKinds() == null ? List.of() : hold.getKinds());
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

    /** Held rows and bytes per hold, for {@code /v1/system}. */
    public Counts counts(String holdId) {
        return jdbc.queryForObject("SELECT count(*) AS rows,"
                        + " coalesce(sum(coalesce(pg_column_size(payload), 0)), 0) AS bytes"
                        + " FROM " + ExportSchemaInitializer.HELD_TABLE + " WHERE hold_id = :id",
                new MapSqlParameterSource("id", holdId),
                (rs, i) -> new Counts(rs.getLong("rows"), rs.getLong("bytes")));
    }

    /** Totals over every hold. */
    public Counts totals() {
        return jdbc.queryForObject("SELECT count(*) AS rows,"
                        + " coalesce(sum(coalesce(pg_column_size(payload), 0)), 0) AS bytes"
                        + " FROM " + ExportSchemaInitializer.HELD_TABLE,
                new MapSqlParameterSource(),
                (rs, i) -> new Counts(rs.getLong("rows"), rs.getLong("bytes")));
    }

    /** True when any held row of {@code sourceId} at or below {@code seq} exists (a snapshot can read it). */
    public boolean heldBelow(String sourceId, long seq) {
        Boolean exists = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM " + ExportSchemaInitializer.HELD_TABLE
                        + " WHERE source_id = :source AND seq <= :seq)",
                new MapSqlParameterSource("source", sourceId).addValue("seq", seq), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    /** One page of held versions, keyset by seq. */
    public Map<String, Object> versions(String holdId, Long after, int limit) {
        get(holdId);
        int size = Math.max(1, Math.min(500, limit));
        List<Map<String, Object>> items = jdbc.query(
                "SELECT source_id, external_id, kind, seq, version, observed_at, event_time, valid_until,"
                        + " ingested_at, tombstone, payload::text AS payload"
                        + " FROM " + ExportSchemaInitializer.HELD_TABLE
                        + " WHERE hold_id = :id AND seq > :after ORDER BY seq LIMIT " + (size + 1),
                new MapSqlParameterSource("id", holdId).addValue("after", after == null ? 0L : after),
                (rs, i) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sourceId", rs.getString("source_id"));
                    row.put("externalId", rs.getString("external_id"));
                    row.put("kind", rs.getString("kind"));
                    row.put("seq", rs.getLong("seq"));
                    row.put("version", rs.getInt("version"));
                    row.put("observedAt", rs.getObject("observed_at", java.time.OffsetDateTime.class));
                    row.put("eventTime", rs.getObject("event_time", java.time.OffsetDateTime.class));
                    row.put("validUntil", rs.getObject("valid_until", java.time.OffsetDateTime.class));
                    row.put("ingestedAt", rs.getObject("ingested_at", java.time.OffsetDateTime.class));
                    row.put("tombstone", rs.getBoolean("tombstone"));
                    row.put("payload", rs.getString("payload"));
                    return row;
                });
        Long next = null;
        if (items.size() > size) {
            next = ((Number) items.get(size - 1).get("seq")).longValue();
            items = new ArrayList<>(items.subList(0, size));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", next == null ? null : String.valueOf(next));
        return out;
    }

    /**
     * Held rows and bytes.
     *
     * @param rows the row count
     * @param bytes the approximate bytes
     */
    public record Counts(long rows, long bytes) {}

    private static String text(JsonNode body, String field) {
        JsonNode node = body == null ? null : body.get(field);
        return node == null || node.isNull() ? null : node.asString();
    }

    private static List<String> strings(JsonNode body, String field) {
        JsonNode node = body == null ? null : body.get(field);
        if (node == null || node.isNull() || !node.isArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            out.add(item.asString());
        }
        return out;
    }

    private static Instant instant(JsonNode body, String field) {
        String value = text(body, field);
        return value == null ? null : Instant.parse(value);
    }

    private static Long longOrNull(JsonNode body, String field) {
        JsonNode node = body == null ? null : body.get(field);
        return node == null || node.isNull() ? null : node.asLong();
    }
}
