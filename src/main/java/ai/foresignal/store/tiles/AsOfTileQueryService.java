package ai.foresignal.store.tiles;

import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfPlan;
import ai.foresignal.store.history.VersionSql;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SqlParams;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Vector tiles as known at an instant (prompt 037): {@code GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt?asOf=}.
 *
 * <p>Reads {@code record_version} on the transaction axis with the {@link AsOfPlan} of the layer's
 * sources. Per involved tier:
 *
 * <pre>
 * cand AS (SELECT DISTINCT source_id, external_id FROM record_version
 *          WHERE history_tier = :tier AND seq BETWEEN :L AND :S
 *            AND source_id = ANY(:sources) AND geom &amp;&amp; :env4326),
 * pick AS (SELECT v.* FROM cand c CROSS JOIN LATERAL (
 *            SELECT * FROM record_version v
 *            WHERE v.history_tier = :tier AND v.source_id = c.source_id AND v.external_id = c.external_id
 *              AND v.seq BETWEEN :L AND :S ORDER BY v.seq DESC LIMIT 1) v)
 * </pre>
 *
 * then the picks of both tiers are joined to the per-source validity table and kept when not a
 * tombstone, still inside the tile and valid at {@code t} (Core-Data-Model §12; an explicit
 * {@code validUntil} is ignored on TRACK). A plain {@code DISTINCT ON} over the tile's box is not
 * used: an entity that moved out of the tile would be drawn at its last in-tile position.
 *
 * <p>Features carry {@code source}, {@code externalId}, {@code kind}, {@code title} (resolved from
 * the version payload), {@code observedAt} (epoch seconds), {@code version} and the layer's
 * {@code tileProperties} from the version payload; the same feature cap applies, newest first, and
 * the same {@code agg=grid|hex} aggregation as current tiles. The caller's restriction (prompts
 * 035, 036) is part of the SQL, applied to the chosen version. Each tile runs with
 * {@code statement_timeout = 15s} ({@code 503 QUERY_TIMEOUT} with {@code Retry-After: 5}).
 */
@Service
public class AsOfTileQueryService {

    /** The statement timeout of one asOf tile. */
    public static final Duration STATEMENT_TIMEOUT = Duration.ofSeconds(15);

    private final ReadQueryRunner runner;
    private final TileProperties properties;

    /** Creates the service. */
    public AsOfTileQueryService(ReadQueryRunner runner, TileProperties properties) {
        this.runner = runner;
        this.properties = properties;
    }

    /** Renders {@code coordinates} of {@code layer} at {@code plan.t()} in the shape {@code agg} selects. */
    public TileResult render(TileLayer layer, TileCoordinates coordinates, AggMode agg, AsOfPlan plan,
            ReadRestriction restriction) {
        TileShape shape = agg.shapeFor(coordinates.z(), layer.aggBelowZoom());
        return shape.aggregated()
                ? aggregated(layer, coordinates, shape, plan, restriction)
                : raw(layer, coordinates, properties.maxFeatures(), plan, restriction);
    }

    /** A raw asOf tile with at most {@code cap} features, newest {@code observedAt} first. */
    public TileResult raw(TileLayer layer, TileCoordinates coordinates, int cap, AsOfPlan plan,
            ReadRestriction restriction) {
        if (plan.empty()) {
            return new TileResult(new byte[0], false, TileShape.RAW);
        }
        SqlParams p = new SqlParams();
        String env = TileQueryService.envelope(coordinates, p);
        String limit = p.bigint(cap + 1L);
        String innerLimit = p.bigint(cap);
        String layerName = p.text(layer.id());
        String sql = "WITH bounds AS (SELECT " + env + " AS env), "
                + matchedCtes(layer, plan, restriction, p) + ","
                + " features AS (SELECT ST_AsMVTGeom(ST_Transform(" + TileQueryService.clipped("h.geom")
                + ", 3857), bounds.env, " + TileQueryService.EXTENT + ", " + TileQueryService.BUFFER + ", true) AS geom,"
                + " " + properties(layer, plan, p) + " AS props,"
                + " h.observed_at AS observed_at, h.source_id AS sid, h.external_id AS eid"
                + " FROM matched h CROSS JOIN bounds"
                + " ORDER BY h.observed_at DESC, h.source_id, h.external_id LIMIT " + limit + ")"
                + " SELECT (SELECT ST_AsMVT(t.*, " + layerName + ", " + TileQueryService.EXTENT + ", 'geom')"
                + " FROM (SELECT geom, props FROM features WHERE geom IS NOT NULL"
                + " ORDER BY observed_at DESC, sid, eid LIMIT " + innerLimit + ") t) AS mvt,"
                + " (SELECT count(*) FROM features) AS matched";
        return runner.read(STATEMENT_TIMEOUT, jdbc -> jdbc.queryForObject(sql, p.source(), (rs, n) ->
                new TileResult(rs.getBytes("mvt"), rs.getLong("matched") > cap, TileShape.RAW)));
    }

    /** An aggregated asOf tile of grid or hex cells. */
    public TileResult aggregated(TileLayer layer, TileCoordinates coordinates, TileShape shape, AsOfPlan plan,
            ReadRestriction restriction) {
        if (!shape.aggregated()) {
            throw new IllegalArgumentException("not an aggregate shape: " + shape);
        }
        if (plan.empty()) {
            return new TileResult(new byte[0], false, shape);
        }
        SqlParams p = new SqlParams();
        String env = TileQueryService.envelope(coordinates, p);
        String pts = matchedCtes(layer, plan, restriction, p) + ","
                + " pts AS (SELECT h.source_id, h.external_id, h.kind, h.observed_at, "
                + TileQueryService.rankExpression(layer, p) + " AS rank,"
                + " ST_Transform(ST_PointOnSurface(" + TileQueryService.clipped("h.geom") + "), 3857) AS pt"
                + " FROM matched h CROSS JOIN LATERAL (SELECT h.payload AS m) mp)";
        String statement = TileQueryService.aggregationSql(env, pts, layer, coordinates, shape,
                properties.cellsPerTile(), p);
        return runner.read(STATEMENT_TIMEOUT, jdbc -> new TileResult(
                jdbc.queryForObject(statement, p.source(), byte[].class), false, shape));
    }

    /**
     * The candidate, pick and {@code matched} CTEs (comma separated, no trailing comma):
     * {@code matched} holds the chosen version per entity, visible at {@code t} and inside the tile,
     * with every {@code record_version} column. References the {@code bounds} CTE.
     */
    static String matchedCtes(TileLayer layer, AsOfPlan plan, ReadRestriction restriction, SqlParams p) {
        List<String> ctes = new ArrayList<>();
        List<String> picks = new ArrayList<>();
        String upper = p.bigint(plan.s());
        for (Map.Entry<HistoryTier, List<SourceDescriptor>> tier : plan.sources().entrySet()) {
            String n = tier.getKey().wireValue();
            String code = "CAST(" + p.add((int) tier.getKey().code()) + " AS smallint)";
            String lower = p.bigint(plan.lower(tier.getKey()));
            List<String> ids = tier.getValue().stream().map(SourceDescriptor::id).toList();
            ctes.add("cand_" + n + " AS (SELECT DISTINCT v.source_id, v.external_id FROM record_version v CROSS JOIN bounds"
                    + " WHERE v.history_tier = " + code + " AND v.seq BETWEEN " + lower + " AND " + upper
                    + " AND v.source_id = ANY(" + p.textArray(ids) + ")"
                    + " AND v.geom && ST_Transform(bounds.env, 4326))");
            ctes.add("pick_" + n + " AS (SELECT v.* FROM cand_" + n + " c CROSS JOIN LATERAL ("
                    + "SELECT * FROM record_version v WHERE v.history_tier = " + code
                    + " AND v.source_id = c.source_id AND v.external_id = c.external_id"
                    + " AND v.seq BETWEEN " + lower + " AND " + upper + " ORDER BY v.seq DESC LIMIT 1) v)");
            picks.add("SELECT * FROM pick_" + n);
        }
        ctes.add("picked AS (" + String.join(" UNION ALL ", picks) + ")");
        List<String> where = new ArrayList<>();
        where.add("NOT h.tombstone");
        where.add("h.geom && ST_Transform(bounds.env, 4326)");
        where.add(VersionSql.validAt("h", "s.ttl", plan.t(), p));
        if (layer.type() == TileLayerType.KIND) {
            where.add("h.kind = " + p.text(layer.id()));
        }
        RestrictionClamp.sqlPredicate("h.source_id", "h.kind", "h.geom", "h.payload", restriction, p)
                .ifPresent(where::add);
        ctes.add("matched AS (SELECT h.* FROM picked h JOIN " + VersionSql.validityValues(plan.allSources(), plan, p)
                + " s(source_id, ttl) ON s.source_id = h.source_id CROSS JOIN bounds WHERE "
                + String.join(" AND ", where) + ")");
        return String.join(", ", ctes);
    }

    /**
     * The {@code props} expression of a raw asOf feature: the layer's tile properties from the version
     * payload, then the base properties ({@code source}, {@code externalId}, {@code kind},
     * {@code title}, {@code version}, {@code observedAt}), which win; nulls stripped.
     */
    static String properties(TileLayer layer, AsOfPlan plan, SqlParams p) {
        return "jsonb_strip_nulls(" + VersionSql.tileProperties("h.payload", layer.tileProperties())
                + " || jsonb_build_object('source', h.source_id, 'externalId', h.external_id, 'kind', h.kind,"
                + " 'title', " + VersionSql.title("h", plan.allSources(), p) + ", 'version', h.version,"
                + " 'observedAt', floor(extract(epoch from h.observed_at))::bigint))";
    }
}
