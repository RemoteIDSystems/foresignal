package ai.foresignal.export;

import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads and writes {@code archive_manifest} (prompt R2-016) with native SQL; Hibernate does not map it. A write
 * replaces the whole row.
 */
@Component
public class ArchiveManifestStore {

    private static final String COLUMNS = "id, status, trigger, domain, target, location, cutoff, sources, from_seq,"
            + " to_seq, from_time, to_time, size_bytes, item_count, checksum, files::text AS files, purged_from_hot,"
            + " error, created_at, updated_at, completed_at, purged_at, restored_at";

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the store. */
    public ArchiveManifestStore(NamedParameterJdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * A page of segments.
     *
     * @param items the segments, newest first
     * @param nextCursor the cursor of the next page, or null
     */
    public record Page(List<ArchiveSegment> items, String nextCursor) {}

    /** Inserts or replaces {@code segment}. */
    public void save(ArchiveSegment segment) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", segment.id()).addValue("status", segment.status())
                .addValue("trigger", segment.trigger()).addValue("domain", segment.domain())
                .addValue("target", segment.target()).addValue("location", segment.location())
                .addValue("cutoff", ts(segment.cutoff()))
                .addValue("sources", String.join(",", segment.sources()))
                .addValue("fromSeq", segment.fromSeq()).addValue("toSeq", segment.toSeq())
                .addValue("fromTime", ts(segment.fromTime())).addValue("toTime", ts(segment.toTime()))
                .addValue("sizeBytes", segment.sizeBytes()).addValue("itemCount", segment.itemCount())
                .addValue("checksum", segment.checksum())
                .addValue("files", new String(json.writeValueAsBytes(segment.files()), StandardCharsets.UTF_8))
                .addValue("purged", segment.purgedFromHot()).addValue("error", segment.error())
                .addValue("createdAt", ts(segment.createdAt())).addValue("updatedAt", ts(segment.updatedAt()))
                .addValue("completedAt", ts(segment.completedAt())).addValue("purgedAt", ts(segment.purgedAt()))
                .addValue("restoredAt", ts(segment.restoredAt()));
        jdbc.update("""
                INSERT INTO archive_manifest (id, status, trigger, domain, target, location, cutoff, sources, from_seq,
                    to_seq, from_time, to_time, size_bytes, item_count, checksum, files, purged_from_hot, error,
                    created_at, updated_at, completed_at, purged_at, restored_at)
                VALUES (:id, :status, :trigger, :domain, :target, :location, :cutoff,
                    COALESCE(string_to_array(NULLIF(:sources, ''), ','), '{}'), :fromSeq, :toSeq, :fromTime,
                    :toTime, :sizeBytes, :itemCount, :checksum, CAST(:files AS jsonb), :purged, :error, :createdAt,
                    :updatedAt, :completedAt, :purgedAt, :restoredAt)
                ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status, trigger = EXCLUDED.trigger,
                    domain = EXCLUDED.domain, target = EXCLUDED.target, location = EXCLUDED.location,
                    cutoff = EXCLUDED.cutoff, sources = EXCLUDED.sources,
                    from_seq = EXCLUDED.from_seq, to_seq = EXCLUDED.to_seq, from_time = EXCLUDED.from_time,
                    to_time = EXCLUDED.to_time, size_bytes = EXCLUDED.size_bytes, item_count = EXCLUDED.item_count,
                    checksum = EXCLUDED.checksum, files = EXCLUDED.files, purged_from_hot = EXCLUDED.purged_from_hot,
                    error = EXCLUDED.error, updated_at = EXCLUDED.updated_at, completed_at = EXCLUDED.completed_at,
                    purged_at = EXCLUDED.purged_at, restored_at = EXCLUDED.restored_at
                """, p);
    }

    /** The segment {@code id}. */
    public Optional<ArchiveSegment> find(String id) {
        List<ArchiveSegment> found = jdbc.query("SELECT " + COLUMNS + " FROM archive_manifest WHERE id = :id",
                new MapSqlParameterSource("id", id), (rs, i) -> map(rs));
        return found.stream().findFirst();
    }

    /** The segments in {@code status}, oldest first. */
    public List<ArchiveSegment> withStatus(String status) {
        return jdbc.query("SELECT " + COLUMNS + " FROM archive_manifest WHERE status = :status ORDER BY created_at, id",
                new MapSqlParameterSource("status", status), (rs, i) -> map(rs));
    }

    /**
     * One page, newest first.
     *
     * @param status a status, or null for all
     * @param cursor the previous page's cursor, or null
     * @param limit the page size
     * @throws IllegalArgumentException for a cursor this store did not issue
     */
    public Page page(String status, String cursor, int limit) {
        MapSqlParameterSource p = new MapSqlParameterSource("limit", limit + 1);
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM archive_manifest WHERE TRUE");
        if (status != null) {
            sql.append(" AND status = :status");
            p.addValue("status", status);
        }
        if (cursor != null) {
            String[] parts = decode(cursor);
            sql.append(" AND (created_at, id) < (:afterAt, :afterId)");
            p.addValue("afterAt", Timestamp.from(Instant.parse(parts[0]))).addValue("afterId", parts[1]);
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT :limit");
        List<ArchiveSegment> rows = jdbc.query(sql.toString(), p, (rs, i) -> map(rs));
        String next = null;
        if (rows.size() > limit) {
            rows = new ArrayList<>(rows.subList(0, limit));
            ArchiveSegment last = rows.getLast();
            next = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    (last.createdAt() + "|" + last.id()).getBytes(StandardCharsets.UTF_8));
        }
        return new Page(rows, next);
    }

    private static String[] decode(String cursor) {
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = text.split("\\|", 2);
            Instant.parse(parts[0]);
            if (parts.length != 2 || parts[1].isBlank()) {
                throw new IllegalArgumentException("bad cursor");
            }
            return parts;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("The cursor is not one this endpoint issued", e);
        }
    }

    private ArchiveSegment map(ResultSet rs) throws SQLException {
        Array sources = rs.getArray("sources");
        List<String> sourceList = sources == null ? List.of() : List.of((String[]) sources.getArray());
        List<ArchiveSegment.SegmentFile> files = json.readValue(rs.getString("files"),
                new TypeReference<List<ArchiveSegment.SegmentFile>>() {});
        return new ArchiveSegment(rs.getString("id"), rs.getString("status"), rs.getString("trigger"),
                rs.getString("domain"), rs.getString("target"), rs.getString("location"), instant(rs, "cutoff"),
                sourceList, longOrNull(rs, "from_seq"), longOrNull(rs, "to_seq"), instant(rs, "from_time"),
                instant(rs, "to_time"), longOrNull(rs, "size_bytes"), longOrNull(rs, "item_count"),
                rs.getString("checksum"), files, rs.getBoolean("purged_from_hot"), rs.getString("error"),
                instant(rs, "created_at"), instant(rs, "updated_at"), instant(rs, "completed_at"),
                instant(rs, "purged_at"), instant(rs, "restored_at"));
    }

    private static Long longOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }

    private static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
