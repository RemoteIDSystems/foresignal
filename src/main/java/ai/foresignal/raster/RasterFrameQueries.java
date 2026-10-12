package ai.foresignal.raster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Native SQL over {@code raster_frame} (prompt 042). Commits are upserts, aliases are ordered
 * lookups on {@code (product_id, valid_time DESC)}, and runs group by {@code reference_time}.
 *
 * <p>Every statement runs in its own short auto-committed transaction: nothing here holds locks
 * across a decode or a file operation.
 */
@Component
public class RasterFrameQueries {

    static final String COLUMNS = "product_id, frame_id, valid_time, reference_time, forecast_step, obs_start, obs_end,"
            + " grid_id, path, bytes, sha256, dtype, codec, stats::text AS stats, upstream_key, status, thinned,"
            + " run_complete, ingested_at, expires_at";

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;
    private final JsonMapper json;
    private final RowMapper<RasterFrameRow> mapper;

    /** Creates the queries. */
    public RasterFrameQueries(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.named = new NamedParameterJdbcTemplate(jdbc);
        this.json = json;
        this.mapper = this::map;
    }

    private RasterFrameRow map(ResultSet rs, int rowNum) throws SQLException {
        String stats = rs.getString("stats");
        Integer step = (Integer) rs.getObject("forecast_step");
        return new RasterFrameRow(rs.getString("product_id"), rs.getString("frame_id"), instant(rs, "valid_time"),
                instant(rs, "reference_time"), step, instant(rs, "obs_start"), instant(rs, "obs_end"),
                rs.getString("grid_id"), rs.getString("path"), rs.getLong("bytes"), rs.getString("sha256"),
                rs.getString("dtype"), rs.getInt("codec"),
                stats == null ? null : json.readValue(stats, new TypeReference<Map<String, Object>>() {}),
                rs.getString("upstream_key"), rs.getString("status"), rs.getBoolean("thinned"),
                rs.getBoolean("run_complete"), instant(rs, "ingested_at"), instant(rs, "expires_at"));
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    static OffsetDateTime ts(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /**
     * Inserts or replaces a frame. A reprocessed frame keeps its key and gets the new content, hash,
     * statistics and status; {@code thinned} and {@code run_complete} are kept.
     *
     * @param footprintWkt the footprint as WKT, or null
     */
    public void upsert(RasterFrameRow row, String footprintWkt) {
        jdbc.update("INSERT INTO raster_frame (product_id, frame_id, valid_time, reference_time, forecast_step,"
                        + " obs_start, obs_end, grid_id, path, bytes, sha256, dtype, codec, stats, footprint,"
                        + " upstream_key, status, thinned, run_complete, ingested_at, expires_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb,"
                        + " CASE WHEN ?::text IS NULL THEN NULL ELSE ST_Multi(ST_GeomFromText(?::text, 4326)) END,"
                        + " ?, ?, false, false, ?, ?)"
                        + " ON CONFLICT (product_id, frame_id) DO UPDATE SET valid_time = EXCLUDED.valid_time,"
                        + " reference_time = EXCLUDED.reference_time, forecast_step = EXCLUDED.forecast_step,"
                        + " obs_start = EXCLUDED.obs_start, obs_end = EXCLUDED.obs_end, grid_id = EXCLUDED.grid_id,"
                        + " path = EXCLUDED.path, bytes = EXCLUDED.bytes, sha256 = EXCLUDED.sha256,"
                        + " dtype = EXCLUDED.dtype, codec = EXCLUDED.codec, stats = EXCLUDED.stats,"
                        + " footprint = EXCLUDED.footprint, upstream_key = EXCLUDED.upstream_key,"
                        + " status = EXCLUDED.status, ingested_at = EXCLUDED.ingested_at,"
                        + " expires_at = CASE WHEN raster_frame.thinned THEN raster_frame.expires_at"
                        + " ELSE EXCLUDED.expires_at END",
                row.productId(), row.frameId(), ts(row.validTime()), ts(row.referenceTime()), row.forecastStep(),
                ts(row.obsStart()), ts(row.obsEnd()), row.gridId(), row.path(), row.bytes(), row.sha256(), row.dtype(),
                row.codec(), row.stats() == null ? null : json.writeValueAsString(row.stats()), footprintWkt,
                footprintWkt, row.upstreamKey(), row.status(), ts(row.ingestedAt()), ts(row.expiresAt()));
    }

    /** One frame by id. */
    public Optional<RasterFrameRow> find(String productId, String frameId) {
        return first(jdbc.query("SELECT " + COLUMNS + " FROM raster_frame WHERE product_id = ? AND frame_id = ?",
                mapper, productId, frameId));
    }

    /**
     * The frames valid at exactly {@code validTime}, newest run first (observations have one).
     */
    public List<RasterFrameRow> atValidTime(String productId, Instant validTime) {
        return jdbc.query("SELECT " + COLUMNS + " FROM raster_frame WHERE product_id = ? AND valid_time = ?"
                + " ORDER BY reference_time DESC NULLS LAST", mapper, productId, ts(validTime));
    }

    /** The newest frame, complete only unless {@code includePartial}. */
    public Optional<RasterFrameRow> newest(String productId, boolean includePartial) {
        return first(jdbc.query("SELECT " + COLUMNS + " FROM raster_frame WHERE product_id = ?"
                + statusClause(includePartial) + " ORDER BY valid_time DESC LIMIT 1", mapper, productId));
    }

    /** The newest frame valid at or before {@code at}, complete only unless {@code includePartial}. */
    public Optional<RasterFrameRow> newestAtOrBefore(String productId, Instant at, boolean includePartial) {
        return first(jdbc.query("SELECT " + COLUMNS + " FROM raster_frame WHERE product_id = ? AND valid_time <= ?"
                + statusClause(includePartial) + " ORDER BY valid_time DESC LIMIT 1", mapper, productId, ts(at)));
    }

    /** The oldest retained frame's valid time. */
    public Optional<Instant> earliest(String productId) {
        return first(jdbc.query("SELECT min(valid_time) AS t FROM raster_frame WHERE product_id = ?",
                (rs, n) -> instant(rs, "t"), productId)).filter(java.util.Objects::nonNull);
    }

    /** The newest complete frame's valid time. */
    public Optional<Instant> latestComplete(String productId) {
        return first(jdbc.query("SELECT max(valid_time) AS t FROM raster_frame WHERE product_id = ? AND status = 'complete'",
                (rs, n) -> instant(rs, "t"), productId)).filter(java.util.Objects::nonNull);
    }

    /**
     * A page of frames, newest first.
     *
     * @param from valid time ≥ from, or null
     * @param to valid time ≤ to, or null
     * @param run only this forecast run, or null for all
     * @param afterTime continue after this (validTime, frameId) position, or null
     * @param afterFrameId the frame id of that position
     * @param limit rows to return
     */
    public List<RasterFrameRow> page(String productId, Instant from, Instant to, Instant run, boolean includePartial,
            Instant afterTime, String afterFrameId, int limit) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM raster_frame WHERE product_id = :p");
        MapSqlParameterSource params = new MapSqlParameterSource("p", productId);
        if (from != null) {
            sql.append(" AND valid_time >= :from");
            params.addValue("from", ts(from));
        }
        if (to != null) {
            sql.append(" AND valid_time <= :to");
            params.addValue("to", ts(to));
        }
        if (run != null) {
            sql.append(" AND reference_time = :run");
            params.addValue("run", ts(run));
        }
        if (!includePartial) {
            sql.append(" AND status = 'complete'");
        }
        if (afterTime != null) {
            sql.append(" AND (valid_time < :at OR (valid_time = :at AND frame_id < :af))");
            params.addValue("at", ts(afterTime)).addValue("af", afterFrameId);
        }
        sql.append(" ORDER BY valid_time DESC, frame_id DESC LIMIT :limit");
        params.addValue("limit", limit);
        return named.query(sql.toString(), params, mapper);
    }

    /**
     * A forecast run's summary.
     *
     * @param run the reference time
     * @param complete true when the run was marked complete
     * @param steps the steps present, ascending
     * @param validFrom the first valid time
     * @param validTo the last valid time
     * @param partialFrames frames still partial
     */
    public record RunSummary(Instant run, boolean complete, List<Integer> steps, Instant validFrom, Instant validTo,
            int partialFrames) {}

    /** Every run of a forecast product, newest first. */
    public List<RunSummary> runs(String productId) {
        return jdbc.query("SELECT reference_time, bool_or(run_complete) AS complete,"
                        + " array_agg(forecast_step ORDER BY forecast_step) AS steps, min(valid_time) AS vf,"
                        + " max(valid_time) AS vt, count(*) FILTER (WHERE status = 'partial') AS partials"
                        + " FROM raster_frame WHERE product_id = ? AND reference_time IS NOT NULL"
                        + " GROUP BY reference_time ORDER BY reference_time DESC",
                (rs, n) -> {
                    Integer[] steps = (Integer[]) rs.getArray("steps").getArray();
                    return new RunSummary(instant(rs, "reference_time"), rs.getBoolean("complete"),
                            List.of(steps), instant(rs, "vf"), instant(rs, "vt"), rs.getInt("partials"));
                }, productId);
    }

    /** The frames of one run, by step. */
    public List<RasterFrameRow> runFrames(String productId, Instant run, boolean includePartial) {
        return jdbc.query("SELECT " + COLUMNS + " FROM raster_frame WHERE product_id = ? AND reference_time = ?"
                + statusClause(includePartial) + " ORDER BY forecast_step", mapper, productId, ts(run));
    }

    /**
     * Marks every frame of a run complete-run.
     *
     * @return frames updated
     */
    public int markRunComplete(String productId, Instant run) {
        return jdbc.update("UPDATE raster_frame SET run_complete = true WHERE product_id = ? AND reference_time = ?",
                productId, ts(run));
    }

    /**
     * The newest frame time per product, for frame-age health: the newest complete valid time for an
     * observation product, the newest ingest time for a forecast product (whose valid times lie in
     * the future).
     */
    public Map<String, Instant> newestFrameTimes(Collection<String> observationIds, Collection<String> forecastIds) {
        Map<String, Instant> out = new LinkedHashMap<>();
        if (!observationIds.isEmpty()) {
            named.query("SELECT product_id, max(valid_time) AS t FROM raster_frame WHERE product_id IN (:ids)"
                            + " AND status = 'complete' GROUP BY product_id",
                    new MapSqlParameterSource("ids", observationIds),
                    (ResultSet rs) -> {
                        out.put(rs.getString("product_id"), instant(rs, "t"));
                    });
        }
        if (!forecastIds.isEmpty()) {
            named.query("SELECT product_id, max(ingested_at) AS t FROM raster_frame WHERE product_id IN (:ids)"
                            + " GROUP BY product_id",
                    new MapSqlParameterSource("ids", forecastIds),
                    (ResultSet rs) -> {
                        out.put(rs.getString("product_id"), instant(rs, "t"));
                    });
        }
        return out;
    }

    /**
     * Frame count and stored bytes.
     *
     * @param frames rows
     * @param bytes sum of {@code bytes}
     */
    public record Totals(long frames, long bytes) {}

    /** Totals over every frame. */
    public Totals totals() {
        return jdbc.queryForObject("SELECT count(*) AS n, coalesce(sum(bytes), 0) AS b FROM raster_frame",
                (rs, n) -> new Totals(rs.getLong("n"), rs.getLong("b")));
    }

    /** Every frame of {@code productId}, oldest first (retention). */
    public List<RasterFrameRow> allFrames(String productId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM raster_frame WHERE product_id = ? ORDER BY valid_time, frame_id",
                mapper, productId);
    }

    /** The {@code limit} oldest frames across every product (quota eviction). */
    public List<RasterFrameRow> oldest(int limit) {
        return jdbc.query("SELECT " + COLUMNS + " FROM raster_frame ORDER BY valid_time, product_id, frame_id LIMIT ?",
                mapper, limit);
    }

    /** Every distinct product id with frames. */
    public List<String> productIds() {
        return jdbc.queryForList("SELECT DISTINCT product_id FROM raster_frame ORDER BY product_id", String.class);
    }

    /**
     * Deletes one row.
     *
     * @return the deleted row's path, or empty when it was already gone
     */
    public Optional<String> delete(String productId, String frameId) {
        return first(jdbc.query("DELETE FROM raster_frame WHERE product_id = ? AND frame_id = ? RETURNING path",
                (rs, n) -> rs.getString(1), productId, frameId));
    }

    /** Marks a frame kept by thinning, with its new expiry. */
    public void markThinned(String productId, String frameId, Instant expiresAt) {
        jdbc.update("UPDATE raster_frame SET thinned = true, expires_at = ? WHERE product_id = ? AND frame_id = ?",
                ts(expiresAt), productId, frameId);
    }

    /** The paths of every row of {@code productId}. */
    public List<String> paths(String productId) {
        return jdbc.queryForList("SELECT path FROM raster_frame WHERE product_id = ?", String.class, productId);
    }

    /** {@code persisted_observed_at} of the record head of ({@code sourceId}, {@code externalId}). */
    public Optional<Instant> recordHeadObservedAt(String sourceId, String externalId) {
        return first(jdbc.query("SELECT persisted_observed_at AS t FROM record_head WHERE source_id = ?"
                        + " AND external_id = ? AND NOT tombstone",
                (rs, n) -> instant(rs, "t"), sourceId, externalId));
    }

    private static String statusClause(boolean includePartial) {
        return includePartial ? "" : " AND status = 'complete'";
    }

    private static <T> Optional<T> first(List<T> rows) {
        return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.get(0));
    }
}
