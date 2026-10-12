package ai.foresignal.briefing;

import ai.foresignal.security.restrict.RestrictionClamp;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.SqlParams;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The SQL {@link DigestStatsProvider} (prompt 029): counts from {@code record_version} by
 * {@code persist_reason}, activity from {@code record_live ⋈ record_head}, notable records from the
 * current head and live rows, alerts by id. Prompt 038 replaces the counts with a rollup-backed
 * provider.
 */
@Component
public class SqlDigestStatsProvider implements DigestStatsProvider {

    /** The kind reported for versions without one. */
    static final String NO_KIND = "";

    private final SourceRegistry registry;

    /** Creates the provider. */
    public SqlDigestStatsProvider(SourceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Map<Key, ReasonCounts> counts(NamedParameterJdbcTemplate jdbc, Filter filter, long fromSeq, long toSeq) {
        Map<Key, ReasonCounts> out = new LinkedHashMap<>();
        if (toSeq <= fromSeq) {
            return out;
        }
        SqlParams p = new SqlParams();
        StringBuilder sql = new StringBuilder("SELECT v.source_id, coalesce(v.kind, '') AS kind");
        for (int code = 1; code <= 9; code++) {
            sql.append(", count(*) FILTER (WHERE v.persist_reason = ").append(code).append(") AS r").append(code);
        }
        sql.append(", count(DISTINCT v.external_id) FILTER (WHERE v.persist_reason IN (1, 2)) AS e_new")
                .append(", count(DISTINCT v.external_id) FILTER (WHERE v.persist_reason IN (3, 5)) AS e_changed")
                .append(", count(DISTINCT v.external_id) FILTER (WHERE v.persist_reason = 4) AS e_removed")
                .append(", count(DISTINCT v.external_id) FILTER (WHERE v.persist_reason = 9) AS e_edges")
                .append(" FROM record_version v WHERE v.seq > ").append(p.bigint(fromSeq))
                .append(" AND v.seq <= ").append(p.bigint(toSeq));
        List<String> where = new ArrayList<>();
        scope(where, filter, "v.source_id", "v.kind", "v.geom", p);
        where.forEach(w -> sql.append(" AND ").append(w));
        sql.append(" GROUP BY v.source_id, coalesce(v.kind, '') ORDER BY 1, 2");
        jdbc.query(sql.toString(), p.source(), (ResultSet rs) -> {
            long[] byCode = new long[10];
            for (int code = 1; code <= 9; code++) {
                byCode[code] = rs.getLong("r" + code);
            }
            out.put(new Key(rs.getString("source_id"), rs.getString("kind")), new ReasonCounts(byCode,
                    rs.getLong("e_new"), rs.getLong("e_changed"), rs.getLong("e_removed"), rs.getLong("e_edges")));
        });
        return out;
    }

    @Override
    public Map<Key, Long> active(NamedParameterJdbcTemplate jdbc, Filter filter, Instant since) {
        SqlParams p = new SqlParams();
        StringBuilder sql = new StringBuilder("SELECT l.source_id, h.kind, count(*) AS n FROM record_live l"
                + " JOIN record_head h ON h.source_id = l.source_id AND h.external_id = l.external_id"
                + " WHERE l.last_received_at >= ").append(p.instant(since)).append(" AND NOT h.tombstone");
        List<String> where = new ArrayList<>();
        scope(where, filter, "l.source_id", "h.kind", "l.geom", p);
        where.forEach(w -> sql.append(" AND ").append(w));
        sql.append(" GROUP BY l.source_id, h.kind ORDER BY 1, 2");
        Map<Key, Long> out = new LinkedHashMap<>();
        jdbc.query(sql.toString(), p.source(),
                (ResultSet rs) -> {
                    out.put(new Key(rs.getString("source_id"), rs.getString("kind")), rs.getLong("n"));
                });
        return out;
    }

    @Override
    public List<Row> notable(NamedParameterJdbcTemplate jdbc, Filter filter, long fromSeq, long toSeq, int perKind) {
        if (toSeq <= fromSeq || perKind < 1) {
            return List.of();
        }
        SqlParams p = new SqlParams();
        String merged = "(h.payload || l.kinematic)";
        String rank = rankExpression(filter, merged, p);
        String effective = "coalesce(h.event_time, l.observed_at)";
        StringBuilder inner = new StringBuilder("SELECT h.source_id, h.external_id, h.kind, h.seq, h.title, l.dirty,"
                + " l.observed_at, " + effective + " AS effective_time, " + rank + " AS rank_value,"
                + " row_number() OVER (PARTITION BY h.kind ORDER BY " + rank + " DESC NULLS LAST, " + effective
                + " DESC NULLS LAST, h.seq DESC) AS rn"
                + " FROM record_head h JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id"
                + " WHERE h.seq > ").append(p.bigint(fromSeq)).append(" AND h.seq <= ").append(p.bigint(toSeq))
                .append(" AND NOT h.tombstone");
        List<String> where = new ArrayList<>();
        scope(where, filter, "h.source_id", "h.kind", "l.geom", p);
        if (!filter.hiddenSources().isEmpty()) {
            where.add("NOT (h.source_id = ANY(" + p.textArray(sorted(filter.hiddenSources())) + "))");
        }
        where.forEach(w -> inner.append(" AND ").append(w));
        String sql = "SELECT * FROM (" + inner + ") ranked WHERE rn <= " + p.bigint(perKind) + " ORDER BY kind, rn";
        return jdbc.query(sql, p.source(), (rs, i) -> {
            double rankValue = rs.getDouble("rank_value");
            Double rankBoxed = rs.wasNull() ? null : rankValue;
            return new Row(rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"),
                    rs.getLong("seq"), rs.getString("title"), rs.getBoolean("dirty"), instant(rs, "observed_at"),
                    instant(rs, "effective_time"), rankBoxed);
        });
    }

    @Override
    public AlertWindow alerts(NamedParameterJdbcTemplate jdbc, Filter filter, long sinceAlertId, long untilAlertId,
            int limit) {
        if (untilAlertId <= sinceAlertId) {
            return new AlertWindow(List.of(), List.of());
        }
        SqlParams p = new SqlParams();
        List<String> where = new ArrayList<>();
        where.add("a.id > " + p.bigint(sinceAlertId));
        where.add("a.id <= " + p.bigint(untilAlertId));
        where.add("NOT a.suppressed");
        if (!filter.ruleIds().isEmpty()) {
            where.add("a.rule_id = ANY(CAST(" + p.add(uuidArray(filter.ruleIds())) + " AS uuid[]))");
        }
        scope(where, filter, "a.source_id", "a.kind", null, p);
        RestrictionClamp.addTo(where, "a.source_id", "a.kind", RestrictionClamp.ALERT_GEOMETRY_SQL,
                filter.restriction(), p);
        if (filter.bbox() != null) {
            String geom = "(CASE WHEN jsonb_typeof(a.record_snapshot -> 'geometry') = 'object' THEN"
                    + " ST_SetSRID(ST_GeomFromGeoJSON((a.record_snapshot -> 'geometry')::text), 4326) END)";
            where.add(RecordQueryService.bboxClause(geom, filter.bbox(), p));
        }
        String clause = " WHERE " + String.join(" AND ", where);
        List<AlertGroup> groups = jdbc.query("SELECT a.severity, a.state, a.rule_id, r.name AS rule_name,"
                + " count(*) AS n FROM alert a LEFT JOIN trigger_rule r ON r.id = a.rule_id" + clause
                + " GROUP BY a.severity, a.state, a.rule_id, r.name", p.source(),
                (rs, i) -> new AlertGroup(rs.getString("severity"), rs.getString("state"),
                        rs.getObject("rule_id", UUID.class), rs.getString("rule_name"), rs.getLong("n")));
        String hidden = "";
        if (!filter.hiddenSources().isEmpty()) {
            hidden = " AND NOT (a.source_id = ANY(" + p.textArray(sorted(filter.hiddenSources())) + "))";
        }
        List<Long> ids = jdbc.queryForList("SELECT a.id FROM alert a" + clause + hidden
                + " ORDER BY CASE a.severity WHEN 'critical' THEN 0 WHEN 'warning' THEN 1 ELSE 2 END, a.id DESC"
                + " LIMIT " + p.bigint(Math.max(0, limit)), p.source(), Long.class);
        return new AlertWindow(groups, ids);
    }

    @Override
    public long maxAlertId(NamedParameterJdbcTemplate jdbc) {
        Long id = jdbc.queryForObject("SELECT coalesce(max(id), 0) FROM alert", new MapSqlParameterSource(), Long.class);
        return id == null ? 0 : id;
    }

    @Override
    public long alertCursorAt(NamedParameterJdbcTemplate jdbc, Instant t) {
        Long first = jdbc.queryForObject("SELECT min(id) FROM alert WHERE fired_at >= :t",
                new MapSqlParameterSource("t", OffsetDateTime.ofInstant(t, ZoneOffset.UTC)), Long.class);
        return first == null ? maxAlertId(jdbc) : first - 1;
    }

    // ------------------------------------------------------------------------- helpers

    /**
     * {@code CASE} over the scope's sources (all sources when unscoped) that declare a
     * {@code rankField}: the field's value when it is a JSON number, else null.
     */
    String rankExpression(Filter filter, String payload, SqlParams p) {
        StringBuilder out = new StringBuilder();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            String field = descriptor.rankField();
            if (field == null || field.isBlank()
                    || (!filter.sources().isEmpty() && !filter.sources().contains(descriptor.id()))) {
                continue;
            }
            String f = p.text(field);
            out.append(" WHEN h.source_id = ").append(p.text(descriptor.id())).append(" THEN (CASE WHEN jsonb_typeof(")
                    .append(payload).append(" -> ").append(f).append(") = 'number' THEN (").append(payload)
                    .append(" ->> ").append(f).append(")::double precision END)");
        }
        return out.isEmpty() ? "CAST(NULL AS double precision)" : "(CASE" + out + " END)";
    }

    /** Source, kind and bbox filters. */
    static void scope(List<String> where, Filter filter, String sourceColumn, String kindColumn, String geomColumn,
            SqlParams p) {
        if (!filter.sources().isEmpty()) {
            where.add(sourceColumn + " = ANY(" + p.textArray(filter.sources()) + ")");
        }
        if (!filter.kinds().isEmpty()) {
            where.add(kindColumn + " = ANY(" + p.textArray(filter.kinds()) + ")");
        }
        if (geomColumn != null && filter.bbox() != null) {
            where.add(RecordQueryService.bboxClause(geomColumn, filter.bbox(), p));
        }
        if (geomColumn != null) {
            // The caller's read restriction (prompt 035); alerts add theirs with the snapshot geometry.
            RestrictionClamp.addTo(where, sourceColumn, kindColumn, geomColumn, filter.restriction(), p);
        }
    }

    private static List<String> sorted(Collection<String> values) {
        return values.stream().sorted().toList();
    }

    private static String uuidArray(Collection<UUID> ids) {
        return "{" + String.join(",", ids.stream().map(UUID::toString).sorted().toList()) + "}";
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}
