package ai.foresignal.stats;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.store.query.PgArrays;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Computes and writes the {@code activity_rollup} rows of one bucket from raw {@code record_version}
 * rows (prompt 038).
 *
 * <p>One pass over the bucket's seq range: each version's centroid is mapped to its H3 cell in Java
 * (rows are streamed with the template's fetch size, so nothing is loaded whole), and the measures are
 * aggregated per {@code (source_id, kind, cell)} plus one {@link H3Cells#ALL_CELLS} totals row per
 * {@code (source_id, kind)}. HEARTBEAT and KEYFRAME versions are skipped. Writing replaces the
 * bucket's rows for the sources computed, so recomputing a bucket is idempotent.
 */
public final class RollupComputer {

    /** The persist reasons never counted. */
    static final String SKIPPED_REASONS = PersistReason.HEARTBEAT.code() + ", " + PersistReason.KEYFRAME.code();

    static final String SELECT = "SELECT v.source_id, v.kind, v.external_id, v.persist_reason,"
            + " ST_X(ST_Centroid(v.geom)) AS lon, ST_Y(ST_Centroid(v.geom)) AS lat"
            + " FROM record_version v WHERE v.seq > :lo AND v.seq <= :hi"
            + " AND v.source_id = ANY(CAST(:sources AS text[]))"
            + " AND v.persist_reason NOT IN (" + SKIPPED_REASONS + ")";

    static final String DELETE = "DELETE FROM activity_rollup WHERE resolution = :resolution"
            + " AND bucket_start = :bucket AND source_id = ANY(CAST(:sources AS text[]))";

    static final String INSERT = "INSERT INTO activity_rollup (resolution, bucket_start, source_id, kind, cell,"
            + " cell_lon, cell_lat, versions, entities, firsts, tombstones, edges, changes) VALUES (:resolution,"
            + " :bucket, :source, :kind, :cell, :lon, :lat, :versions, :entities, :firsts, :tombstones, :edges,"
            + " :changes)";

    private final H3Cells cells;
    private final int cellRes;

    /**
     * Creates a computer.
     *
     * @param cells the H3 helper
     * @param cellRes the rollup cell resolution
     */
    public RollupComputer(H3Cells cells, int cellRes) {
        this.cells = cells;
        this.cellRes = cellRes;
    }

    /**
     * One aggregated row.
     *
     * @param sourceId the source
     * @param kind the kind
     * @param cell the cell, {@link H3Cells#NO_CELL} or {@link H3Cells#ALL_CELLS}
     * @param lon the cell center longitude, or null
     * @param lat the cell center latitude, or null
     * @param versions counted versions
     * @param entities distinct entities
     * @param firsts FIRST and REAPPEAR
     * @param tombstones TOMBSTONE
     * @param edges TRIGGER_EDGE
     * @param changes SIGNIFICANT and CHANGE
     */
    public record Row(String sourceId, String kind, long cell, Double lon, Double lat, long versions, long entities,
            long firsts, long tombstones, long edges, long changes) {}

    /** A group key. */
    private record Key(String sourceId, String kind, long cell) {}

    /** Running measures of one group. */
    private static final class Acc {
        long versions;
        long firsts;
        long tombstones;
        long edges;
        long changes;
        final Set<String> entities = new HashSet<>();

        void add(String externalId, int reason) {
            versions++;
            entities.add(externalId);
            if (reason == PersistReason.FIRST.code() || reason == PersistReason.REAPPEAR.code()) {
                firsts++;
            } else if (reason == PersistReason.TOMBSTONE.code()) {
                tombstones++;
            } else if (reason == PersistReason.TRIGGER_EDGE.code()) {
                edges++;
            } else if (reason == PersistReason.SIGNIFICANT.code() || reason == PersistReason.CHANGE.code()) {
                changes++;
            }
        }
    }

    /**
     * Aggregates the versions with {@code lo < seq <= hi} of {@code sources}.
     *
     * @param jdbc a template inside the caller's transaction (a fetch size makes it stream)
     */
    public List<Row> compute(NamedParameterJdbcTemplate jdbc, long lo, long hi, Collection<String> sources) {
        if (hi <= lo || sources.isEmpty()) {
            return List.of();
        }
        Map<Key, Acc> groups = new HashMap<>();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lo", lo)
                .addValue("hi", hi)
                .addValue("sources", PgArrays.text(sources));
        jdbc.query(SELECT, params, rs -> {
            String source = rs.getString(1);
            String kind = rs.getString(2);
            String externalId = rs.getString(3);
            int reason = rs.getInt(4);
            double lon = rs.getDouble(5);
            boolean noGeometry = rs.wasNull();
            double lat = rs.getDouble(6);
            noGeometry |= rs.wasNull();
            long cell = noGeometry ? H3Cells.NO_CELL : cells.cell(lon, lat, cellRes);
            groups.computeIfAbsent(new Key(source, kind, cell), k -> new Acc()).add(externalId, reason);
            groups.computeIfAbsent(new Key(source, kind, H3Cells.ALL_CELLS), k -> new Acc()).add(externalId, reason);
        });
        List<Row> rows = new ArrayList<>(groups.size());
        groups.forEach((key, acc) -> {
            Double lon = null;
            Double lat = null;
            if (key.cell() > 0) {
                double[] center = cells.center(key.cell());
                lon = center[0];
                lat = center[1];
            }
            rows.add(new Row(key.sourceId(), key.kind(), key.cell(), lon, lat, acc.versions, acc.entities.size(),
                    acc.firsts, acc.tombstones, acc.edges, acc.changes));
        });
        return rows;
    }

    /**
     * Replaces the rows of {@code sources} in the bucket with {@code rows}.
     *
     * @return the rows written
     */
    public static int write(NamedParameterJdbcTemplate jdbc, RollupResolution resolution, Instant bucket,
            Collection<String> sources, List<Row> rows) {
        OffsetDateTime start = OffsetDateTime.ofInstant(bucket, ZoneOffset.UTC);
        if (!sources.isEmpty()) {
            jdbc.update(DELETE, new MapSqlParameterSource()
                    .addValue("resolution", resolution.code())
                    .addValue("bucket", start)
                    .addValue("sources", PgArrays.text(sources)));
        }
        if (rows.isEmpty()) {
            return 0;
        }
        SqlParameterSource[] batch = new SqlParameterSource[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            batch[i] = new MapSqlParameterSource()
                    .addValue("resolution", resolution.code())
                    .addValue("bucket", start)
                    .addValue("source", r.sourceId())
                    .addValue("kind", r.kind())
                    .addValue("cell", r.cell())
                    .addValue("lon", r.lon())
                    .addValue("lat", r.lat())
                    .addValue("versions", r.versions())
                    .addValue("entities", r.entities())
                    .addValue("firsts", r.firsts())
                    .addValue("tombstones", r.tombstones())
                    .addValue("edges", r.edges())
                    .addValue("changes", r.changes());
        }
        jdbc.batchUpdate(INSERT, batch);
        return rows.size();
    }
}
