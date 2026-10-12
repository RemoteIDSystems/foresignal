package ai.foresignal.media;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Native SQL over {@code media} and {@code media_tombstone} (prompt 043). Every statement
 * autocommits; retention deletes return the deleted files so they can be unlinked afterwards.
 */
@Component
public class MediaQueries {

    static final String COLUMNS = "id, sha256, content_type, width, height, bytes, path, role, source_id,"
            + " record_source_id, record_external_id, licence, created_at, expires_at, variant_of, variant_width";

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;
    private final RowMapper<MediaRow> mapper = MediaQueries::map;

    /** Creates the queries. */
    public MediaQueries(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.named = new NamedParameterJdbcTemplate(jdbc);
    }

    private static MediaRow map(ResultSet rs, int rowNum) throws SQLException {
        return new MediaRow(rs.getString("id"), rs.getString("sha256").trim(), rs.getString("content_type"),
                rs.getInt("width"), rs.getInt("height"), rs.getLong("bytes"), rs.getString("path"),
                rs.getString("role"), rs.getString("source_id"), rs.getString("record_source_id"),
                rs.getString("record_external_id"), rs.getString("licence"), instant(rs, "created_at"),
                instant(rs, "expires_at"), rs.getString("variant_of"), (Integer) rs.getObject("variant_width"));
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    static OffsetDateTime ts(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /** Inserts a row. */
    public void insert(MediaRow row) {
        named.update("INSERT INTO media (" + COLUMNS + ") VALUES (:id, :sha256, :contentType, :width, :height,"
                + " :bytes, :path, :role, :sourceId, :recordSourceId, :recordExternalId, :licence, :createdAt,"
                + " :expiresAt, :variantOf, :variantWidth)", new MapSqlParameterSource()
                .addValue("id", row.id())
                .addValue("sha256", row.sha256())
                .addValue("contentType", row.contentType())
                .addValue("width", row.width())
                .addValue("height", row.height())
                .addValue("bytes", row.bytes())
                .addValue("path", row.path())
                .addValue("role", row.role())
                .addValue("sourceId", row.sourceId())
                .addValue("recordSourceId", row.recordSourceId())
                .addValue("recordExternalId", row.recordExternalId())
                .addValue("licence", row.licence())
                .addValue("createdAt", ts(row.createdAt()))
                .addValue("expiresAt", ts(row.expiresAt()))
                .addValue("variantOf", row.variantOf())
                .addValue("variantWidth", row.variantWidth()));
    }

    /** The row with {@code id}. */
    public Optional<MediaRow> find(String id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM media WHERE id = ?", mapper, id).stream().findFirst();
    }

    /** The oldest variant of {@code originalId} at {@code width}. */
    public Optional<MediaRow> findVariant(String originalId, int width) {
        return jdbc.query("SELECT " + COLUMNS + " FROM media WHERE variant_of = ? AND variant_width = ?"
                + " ORDER BY created_at, id LIMIT 1", mapper, originalId, width).stream().findFirst();
    }

    /** Every variant of {@code originalId}, by width. */
    public List<MediaRow> variants(String originalId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM media WHERE variant_of = ? ORDER BY variant_width, id", mapper,
                originalId);
    }

    /** True when {@code id} was deleted and its tombstone is still kept. */
    public boolean tombstoned(String id) {
        Boolean found = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM media_tombstone WHERE id = ?)",
                Boolean.class, id);
        return Boolean.TRUE.equals(found);
    }

    /** True when some row references the file of {@code sha256}. */
    public boolean referenced(String sha256) {
        Boolean found = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM media WHERE sha256 = ?)", Boolean.class,
                sha256);
        return Boolean.TRUE.equals(found);
    }

    /** The bytes on disk: each distinct file counted once. */
    public long totalBytes() {
        Long total = jdbc.queryForObject(
                "SELECT coalesce(sum(b), 0) FROM (SELECT max(bytes) AS b FROM media GROUP BY sha256) f", Long.class);
        return total == null ? 0 : total;
    }

    /** The number of rows. */
    public long count() {
        Long total = jdbc.queryForObject("SELECT count(*) FROM media", Long.class);
        return total == null ? 0 : total;
    }

    /**
     * A deleted row's file.
     *
     * @param id the deleted id
     * @param sha256 its file's hash
     * @param path its file, relative to {@code media/}
     */
    public record Deleted(String id, String sha256, String path) {}

    /**
     * Deletes the rows {@code ids} and every variant of them, and records a tombstone for each.
     *
     * @return the deleted rows' files, for unlinking once no row references them
     */
    public List<Deleted> deleteWithVariants(Collection<String> ids, Instant now) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Deleted> deleted = named.query("DELETE FROM media WHERE id IN (:ids) OR variant_of IN (:ids)"
                + " RETURNING id, sha256, path", new MapSqlParameterSource("ids", ids),
                (rs, i) -> new Deleted(rs.getString("id"), rs.getString("sha256").trim(), rs.getString("path")));
        if (!deleted.isEmpty()) {
            List<Object[]> args = new ArrayList<>();
            for (Deleted d : deleted) {
                args.add(new Object[] {d.id(), ts(now)});
            }
            jdbc.batchUpdate("INSERT INTO media_tombstone (id, deleted_at) VALUES (?, ?) ON CONFLICT (id) DO NOTHING",
                    args);
        }
        return deleted;
    }

    /** Up to {@code limit} originals or variants whose {@code expires_at} is before {@code now}. */
    public List<String> expiredIds(Instant now, int limit) {
        return named.queryForList("SELECT id FROM media WHERE expires_at < :now ORDER BY expires_at LIMIT :limit",
                new MapSqlParameterSource("now", ts(now)).addValue("limit", limit), String.class);
    }

    /**
     * A (source, kind) whose records own media.
     *
     * @param sourceId the owning records' source
     * @param kind their kind, from the record head
     */
    public record RecordGroup(String sourceId, String kind) {}

    /** The (source, kind) pairs of the heads that own record-following originals. */
    public List<RecordGroup> recordGroups() {
        return jdbc.query("""
                SELECT DISTINCT m.record_source_id, h.kind FROM media m
                  JOIN record_head h ON h.source_id = m.record_source_id AND h.external_id = m.record_external_id
                 WHERE m.expires_at IS NULL AND m.variant_of IS NULL
                 ORDER BY 1, 2
                """, (rs, i) -> new RecordGroup(rs.getString(1), rs.getString(2)));
    }

    /** Up to {@code limit} record-following originals of {@code group} created before {@code cutoff}. */
    public List<String> recordFollowingIds(RecordGroup group, Instant cutoff, int limit) {
        return named.queryForList("""
                SELECT m.id FROM media m
                  JOIN record_head h ON h.source_id = m.record_source_id AND h.external_id = m.record_external_id
                 WHERE m.expires_at IS NULL AND m.variant_of IS NULL AND m.record_source_id = :source
                   AND h.kind = :kind AND m.created_at < :cutoff
                 LIMIT :limit
                """, new MapSqlParameterSource("source", group.sourceId()).addValue("kind", group.kind())
                .addValue("cutoff", ts(cutoff)).addValue("limit", limit), String.class);
    }

    /** Up to {@code limit} record-following originals whose record head is gone, created before {@code cutoff}. */
    public List<String> headMissingIds(Instant cutoff, int limit) {
        return named.queryForList("""
                SELECT m.id FROM media m
                 WHERE m.expires_at IS NULL AND m.variant_of IS NULL AND m.record_source_id IS NOT NULL
                   AND m.created_at < :cutoff
                   AND NOT EXISTS (SELECT 1 FROM record_head h
                                    WHERE h.source_id = m.record_source_id AND h.external_id = m.record_external_id)
                 LIMIT :limit
                """, new MapSqlParameterSource("cutoff", ts(cutoff)).addValue("limit", limit), String.class);
    }

    /** The producing sources (null included) of originals with neither a record nor an expiry. */
    public List<String> unownedSources() {
        return jdbc.queryForList("SELECT DISTINCT source_id FROM media WHERE expires_at IS NULL AND variant_of IS NULL"
                + " AND record_source_id IS NULL", String.class);
    }

    /** Up to {@code limit} originals of {@code sourceId} with neither a record nor an expiry, created before {@code cutoff}. */
    public List<String> unownedIds(String sourceId, Instant cutoff, int limit) {
        String sourcePredicate = sourceId == null ? "source_id IS NULL" : "source_id = :source";
        return named.queryForList("SELECT id FROM media WHERE expires_at IS NULL AND variant_of IS NULL"
                + " AND record_source_id IS NULL AND " + sourcePredicate + " AND created_at < :cutoff LIMIT :limit",
                new MapSqlParameterSource("source", sourceId).addValue("cutoff", ts(cutoff)).addValue("limit", limit),
                String.class);
    }

    /** Up to {@code limit} variants whose original no longer exists. */
    public List<String> orphanVariantIds(int limit) {
        return jdbc.queryForList("""
                SELECT v.id FROM media v
                 WHERE v.variant_of IS NOT NULL
                   AND NOT EXISTS (SELECT 1 FROM media o WHERE o.id = v.variant_of)
                 LIMIT ?
                """, String.class, limit);
    }

    /** Up to {@code limit} rows after {@code afterId}, in id order, for the file-existence sweep. */
    public List<MediaRow> page(String afterId, int limit) {
        return jdbc.query("SELECT " + COLUMNS + " FROM media WHERE id > ? ORDER BY id LIMIT ?", mapper,
                afterId == null ? "" : afterId, limit);
    }

    /** The distinct hashes starting with the two hex characters {@code prefix}. */
    public List<String> hashesWithPrefix(String prefix) {
        return jdbc.queryForList("SELECT DISTINCT sha256 FROM media WHERE sha256 LIKE ?", String.class, prefix + "%")
                .stream().map(String::trim).toList();
    }

    /** Deletes tombstones older than {@code before}; returns how many. */
    public int pruneTombstones(Instant before) {
        return jdbc.update("DELETE FROM media_tombstone WHERE deleted_at < ?", ts(before));
    }
}
