package ai.foresignal.backup;

import java.nio.charset.StandardCharsets;
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
 * Reads and writes {@code backup_run} (prompt R2-017) with native SQL; Hibernate does not map it. A write replaces the
 * whole row.
 */
@Component
public class BackupRunStore {

    private static final String COLUMNS = "id, kind, status, trigger, label, target, root, path, bytes, checksum,"
            + " files::text AS files, error, created_at, updated_at, started_at, finished_at, restored_at";

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the store. */
    public BackupRunStore(NamedParameterJdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * A page of backups.
     *
     * @param items the backups, newest first
     * @param nextCursor the cursor of the next page, or null
     */
    public record Page(List<BackupRun> items, String nextCursor) {}

    /** Inserts or replaces {@code run}. */
    public void save(BackupRun run) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", run.id()).addValue("kind", run.type()).addValue("status", run.status())
                .addValue("trigger", run.trigger()).addValue("label", run.label()).addValue("target", run.target())
                .addValue("root", run.root()).addValue("path", run.location()).addValue("bytes", run.sizeBytes())
                .addValue("checksum", run.checksum())
                .addValue("files", new String(json.writeValueAsBytes(run.files()), StandardCharsets.UTF_8))
                .addValue("error", run.error())
                .addValue("createdAt", ts(run.createdAt())).addValue("updatedAt", ts(run.updatedAt()))
                .addValue("startedAt", ts(run.startedAt())).addValue("finishedAt", ts(run.finishedAt()))
                .addValue("restoredAt", ts(run.restoredAt()));
        jdbc.update("""
                INSERT INTO backup_run (id, kind, status, trigger, label, target, root, path, bytes, checksum, files,
                    error, created_at, updated_at, started_at, finished_at, restored_at)
                VALUES (:id, :kind, :status, :trigger, :label, :target, :root, :path, :bytes, :checksum,
                    CAST(:files AS jsonb), :error, :createdAt, :updatedAt, :startedAt, :finishedAt, :restoredAt)
                ON CONFLICT (id) DO UPDATE SET kind = EXCLUDED.kind, status = EXCLUDED.status,
                    trigger = EXCLUDED.trigger, label = EXCLUDED.label, target = EXCLUDED.target,
                    root = EXCLUDED.root, path = EXCLUDED.path, bytes = EXCLUDED.bytes, checksum = EXCLUDED.checksum,
                    files = EXCLUDED.files, error = EXCLUDED.error, updated_at = EXCLUDED.updated_at,
                    started_at = EXCLUDED.started_at, finished_at = EXCLUDED.finished_at,
                    restored_at = EXCLUDED.restored_at
                """, p);
    }

    /** The backup {@code id}. */
    public Optional<BackupRun> find(String id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM backup_run WHERE id = :id",
                new MapSqlParameterSource("id", id), (rs, i) -> map(rs)).stream().findFirst();
    }

    /** The backups of {@code type}, newest first. */
    public List<BackupRun> ofType(String type) {
        return jdbc.query("SELECT " + COLUMNS + " FROM backup_run WHERE kind = :kind ORDER BY created_at DESC, id DESC",
                new MapSqlParameterSource("kind", type), (rs, i) -> map(rs));
    }

    /** The backups in {@code status}, oldest first. */
    public List<BackupRun> withStatus(String status) {
        return jdbc.query("SELECT " + COLUMNS + " FROM backup_run WHERE status = :status ORDER BY created_at, id",
                new MapSqlParameterSource("status", status), (rs, i) -> map(rs));
    }

    /** When the newest backup was asked for, or null when there is none. */
    public Instant lastCreatedAt() {
        Timestamp last = jdbc.queryForObject("SELECT max(created_at) FROM backup_run", new MapSqlParameterSource(),
                Timestamp.class);
        return last == null ? null : last.toInstant();
    }

    /** Deletes the row {@code id}. */
    public void delete(String id) {
        jdbc.update("DELETE FROM backup_run WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    /**
     * One page, newest first.
     *
     * @param type a type, or null for all
     * @param cursor the previous page's cursor, or null
     * @param limit the page size
     * @throws IllegalArgumentException for a cursor this store did not issue
     */
    public Page page(String type, String cursor, int limit) {
        MapSqlParameterSource p = new MapSqlParameterSource("limit", limit + 1);
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM backup_run WHERE TRUE");
        if (type != null) {
            sql.append(" AND kind = :kind");
            p.addValue("kind", type);
        }
        if (cursor != null) {
            String[] parts = decode(cursor);
            sql.append(" AND (created_at, id) < (:afterAt, :afterId)");
            p.addValue("afterAt", Timestamp.from(Instant.parse(parts[0]))).addValue("afterId", parts[1]);
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT :limit");
        List<BackupRun> rows = jdbc.query(sql.toString(), p, (rs, i) -> map(rs));
        String next = null;
        if (rows.size() > limit) {
            rows = new ArrayList<>(rows.subList(0, limit));
            BackupRun last = rows.getLast();
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

    private BackupRun map(ResultSet rs) throws SQLException {
        List<BackupRun.BackupFile> files = json.readValue(rs.getString("files"),
                new TypeReference<List<BackupRun.BackupFile>>() {});
        long bytes = rs.getLong("bytes");
        Long size = rs.wasNull() ? null : bytes;
        return new BackupRun(rs.getString("id"), rs.getString("kind"), rs.getString("status"),
                rs.getString("trigger"), rs.getString("label"), rs.getString("target"), rs.getString("root"),
                rs.getString("path"), size, rs.getString("checksum"), files, rs.getString("error"),
                instant(rs, "created_at"), instant(rs, "updated_at"), instant(rs, "started_at"),
                instant(rs, "finished_at"), instant(rs, "restored_at"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }

    private static Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
