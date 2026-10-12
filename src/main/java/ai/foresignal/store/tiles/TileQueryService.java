package ai.foresignal.store.tiles;

import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;

import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SqlParams;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Renders Mapbox Vector Tiles of current live state with PostGIS ({@code ST_AsMVT},
 * {@code ST_AsMVTGeom}, {@code ST_TileEnvelope}, {@code ST_HexagonGrid}).
 *
 * <p><strong>Raw tiles</strong> read {@code record_live l JOIN record_head h}: geometry and
 * {@code observedAt} come from {@code l}, so a LIVE position shows before any new version is
 * persisted; static properties come from {@code h}; tile properties from the merged payload
 * {@code jsonb_strip_nulls(h.payload || l.kinematic)}. Rows are selected with
 * {@code l.geom && ST_Transform(env, 4326)}, which uses the GiST index on {@code record_live.geom},
 * ordered by {@code l.observed_at DESC} (then source and external id, so equal data gives equal
 * bytes and an equal ETag) and capped. Only the {@code geom} and {@code props} columns
 * reach {@code ST_AsMVT}; {@code props} is {@code jsonb}, so it expands into feature properties with
 * native types.
 *
 * <p><strong>Aggregated tiles</strong> apply the same layer, private and expiry filters, then
 * reduce each record to {@code ST_PointOnSurface(l.geom)} in EPSG:3857 (a polygon counts once).
 * A record belongs to exactly one tile: the tile whose half-open envelope {@code [xmin, xmax) ×
 * [ymin, ymax)} holds that point, so a record on a shared tile edge is never counted twice. Within the
 * tile it belongs to exactly one cell:
 *
 * <ul>
 *   <li>grid: the {@code ST_SquareGrid} cell {@code (floor(x/size), floor(y/size))}, computed
 *       arithmetically so the grouping is a hash aggregate. With an even {@code cellsPerTile} the
 *       cells line up with tile edges at every zoom;
 *   <li>hex: an {@code ST_HexagonGrid} cell it intersects, the lowest {@code (i, j)} on a shared
 *       boundary.
 * </ul>
 *
 * <p>Expiry is evaluated against the application {@link Clock}, as the 014 read paths do.
 */
@Service
public class TileQueryService {

    /** Tile extent in tile coordinates. */
    public static final int EXTENT = 4096;

    /** Clipping buffer in tile coordinates. */
    public static final int BUFFER = 64;

    /** {@code jsonb_build_object} takes at most 100 arguments; properties are built in chunks. */
    static final int PAIRS_PER_OBJECT = 40;

    private static final String FROM = " FROM record_head h JOIN record_live l"
            + " ON l.source_id = h.source_id AND l.external_id = h.external_id";

    /**
     * The geometry clipped to the latitudes Web Mercator can show, so a polar vertex can never make
     * {@code ST_Transform(…, 3857)} fail; tiles only cover ±85.0511° anyway.
     */
    static final String CLIPPED_GEOM = "ST_ClipByBox2D(l.geom, ST_MakeEnvelope(-180, -85.06, 180, 85.06, 4326))";

    private static final String MERGED = " CROSS JOIN LATERAL (SELECT jsonb_strip_nulls(h.payload || l.kinematic) AS m) mp";

    /** The merged payload as an expression, for the request filter of prompt 036 (views, watchlists). */
    static final String MERGED_PAYLOAD = "jsonb_strip_nulls(h.payload || l.kinematic)";

    private final ReadQueryRunner runner;
    private final TileProperties properties;
    private final Clock clock;

    /** Creates the service. */
    public TileQueryService(ReadQueryRunner runner, TileProperties properties, Clock clock) {
        this.runner = runner;
        this.properties = properties;
        this.clock = clock;
    }

    /** Renders {@code coordinates} of {@code layer} in the shape {@code agg} selects. */
    public TileResult render(TileLayer layer, TileCoordinates coordinates, AggMode agg) {
        return render(layer, coordinates, agg, ReadRestriction.NONE);
    }

    /**
     * Renders {@code coordinates} of {@code layer} for a caller under {@code restriction}: the
     * restriction's predicate is part of the MVT SQL itself, never a post-filter (prompt 035).
     */
    public TileResult render(TileLayer layer, TileCoordinates coordinates, AggMode agg, ReadRestriction restriction) {
        TileShape shape = agg.shapeFor(coordinates.z(), layer.aggBelowZoom());
        return shape.aggregated()
                ? aggregated(layer, coordinates, shape, restriction)
                : raw(layer, coordinates, properties.maxFeatures(), restriction);
    }

    /**
     * A raw tile: at most {@code cap} features, newest {@code observedAt} first; {@code truncated}
     * when more records matched.
     */
    public TileResult raw(TileLayer layer, TileCoordinates coordinates, int cap) {
        return raw(layer, coordinates, cap, ReadRestriction.NONE);
    }

    /** {@link #raw(TileLayer, TileCoordinates, int)} under {@code restriction}. */
    public TileResult raw(TileLayer layer, TileCoordinates coordinates, int cap, ReadRestriction restriction) {
        SqlParams p = new SqlParams();
        String env = envelope(coordinates, p);
        String limit = p.bigint(cap + 1L);
        String innerLimit = p.bigint(cap);
        String layerName = p.text(layer.id());
        String sql = "WITH bounds AS (SELECT " + env + " AS env),"
                + " matched AS ("
                + "SELECT ST_AsMVTGeom(ST_Transform(" + CLIPPED_GEOM + ", 3857), bounds.env, " + EXTENT + ", " + BUFFER + ", true) AS geom,"
                + " " + rawProperties(layer) + " AS props,"
                + " l.observed_at AS observed_at, h.source_id AS sid, h.external_id AS eid"
                + FROM
                + (layer.tileProperties().isEmpty() ? "" : MERGED)
                + " CROSS JOIN bounds"
                + " WHERE " + commonFilter(layer, p, restriction)
                + " AND l.geom && ST_Transform(bounds.env, 4326)"
                + " ORDER BY l.observed_at DESC, h.source_id, h.external_id"
                + " LIMIT " + limit
                + ")"
                + " SELECT (SELECT ST_AsMVT(t.*, " + layerName + ", " + EXTENT + ", 'geom')"
                + " FROM (SELECT geom, props FROM matched WHERE geom IS NOT NULL"
                + " ORDER BY observed_at DESC, sid, eid LIMIT " + innerLimit + ") t) AS mvt,"
                + " (SELECT count(*) FROM matched) AS matched";
        return runner.read(jdbc -> jdbc.queryForObject(sql, p.source(), (rs, n) ->
                new TileResult(rs.getBytes("mvt"), rs.getLong("matched") > cap, TileShape.RAW)));
    }

    /** An aggregated tile of grid or hex cells. */
    public TileResult aggregated(TileLayer layer, TileCoordinates coordinates, TileShape shape) {
        return aggregated(layer, coordinates, shape, ReadRestriction.NONE);
    }

    /** {@link #aggregated(TileLayer, TileCoordinates, TileShape)} under {@code restriction}. */
    public TileResult aggregated(TileLayer layer, TileCoordinates coordinates, TileShape shape,
            ReadRestriction restriction) {
        if (!shape.aggregated()) {
            throw new IllegalArgumentException("not an aggregate shape: " + shape);
        }
        int cells = properties.cellsPerTile();
        SqlParams p = new SqlParams();
        String env = envelope(coordinates, p);
        String size = p.dbl(coordinates.cellSize(cells));
        String layerName = p.text(layer.id());
        String agg = p.text(shape.aggValue());

        StringBuilder sql = new StringBuilder("WITH bounds AS (SELECT ").append(env).append(" AS env),")
                .append(" pts AS (SELECT h.source_id, h.external_id, h.kind, l.observed_at, ")
                .append(rankExpression(layer, p)).append(" AS rank,")
                .append(" ST_Transform(ST_PointOnSurface(").append(CLIPPED_GEOM).append("), 3857) AS pt")
                .append(FROM)
                .append(layer.rankable() ? MERGED : "")
                .append(" CROSS JOIN bounds")
                .append(" WHERE ").append(commonFilter(layer, p, restriction))
                .append(" AND l.geom IS NOT NULL AND l.geom && ST_Transform(bounds.env, 4326)),")
                // Half-open tile membership: a point on a shared edge belongs to one tile only.
                .append(" inside AS (SELECT pts.* FROM pts CROSS JOIN bounds")
                .append(" WHERE ST_X(pts.pt) >= ST_XMin(bounds.env) AND ST_X(pts.pt) < ST_XMax(bounds.env)")
                .append(" AND ST_Y(pts.pt) >= ST_YMin(bounds.env) AND ST_Y(pts.pt) < ST_YMax(bounds.env)),");
        String cellGeometry;
        if (shape == TileShape.GRID) {
            String i0 = p.bigint(coordinates.firstColumn(cells));
            String j0 = p.bigint(coordinates.firstRow(cells));
            String last = p.bigint(cells - 1L);
            // Clamped to the tile's own cell range, so a rounding error at the edge cannot move a
            // point into a cell of the neighboring tile.
            sql.append(" assigned AS (SELECT")
                    .append(" LEAST(GREATEST(floor(ST_X(pt) / ").append(size).append(")::bigint, ").append(i0)
                    .append("), ").append(i0).append(" + ").append(last).append(") AS i,")
                    .append(" LEAST(GREATEST(floor(ST_Y(pt) / ").append(size).append(")::bigint, ").append(j0)
                    .append("), ").append(j0).append(" + ").append(last).append(") AS j,")
                    .append(" kind, observed_at, rank FROM inside),");
            cellGeometry = "ST_MakeEnvelope(a.i * " + size + ", a.j * " + size + ", (a.i + 1) * " + size
                    + ", (a.j + 1) * " + size + ", 3857)";
        } else {
            sql.append(" cells AS (SELECT c.i, c.j, c.geom FROM bounds, ST_HexagonGrid(").append(size)
                    .append(", bounds.env) AS c),")
                    .append(" assigned AS (SELECT DISTINCT ON (inside.source_id, inside.external_id)")
                    .append(" c.i, c.j, inside.kind, inside.observed_at, inside.rank")
                    .append(" FROM inside JOIN cells c ON c.geom && inside.pt AND ST_Intersects(c.geom, inside.pt)")
                    .append(" ORDER BY inside.source_id, inside.external_id, c.i, c.j),");
            cellGeometry = "(SELECT c.geom FROM cells c WHERE c.i = a.i AND c.j = a.j)";
        }
        sql.append(" per_kind AS (SELECT i, j, kind, count(*) AS n FROM assigned GROUP BY i, j, kind),")
                .append(" kinds AS (SELECT i, j, jsonb_object_agg(kind, n ORDER BY kind)::text AS kinds")
                .append(" FROM per_kind GROUP BY i, j),")
                .append(" agg AS (SELECT i, j, count(*) AS cnt, max(observed_at) AS latest, max(rank) AS max_rank")
                .append(" FROM assigned GROUP BY i, j)")
                .append(" SELECT ST_AsMVT(t.*, ").append(layerName).append(", ").append(EXTENT).append(", 'geom')")
                .append(" FROM (SELECT * FROM (SELECT ST_AsMVTGeom(").append(cellGeometry)
                .append(", bounds.env, ").append(EXTENT).append(", ").append(BUFFER).append(", true) AS geom,")
                .append(" jsonb_strip_nulls(jsonb_build_object('count', a.cnt,")
                .append(" 'latestObservedAt', floor(extract(epoch from a.latest))::bigint,")
                .append(" 'kinds', k.kinds, 'agg', ").append(agg)
                .append(layer.rankable() ? ", 'maxRank', a.max_rank" : "")
                .append(")) AS props")
                .append(" FROM agg a JOIN kinds k ON k.i = a.i AND k.j = a.j CROSS JOIN bounds")
                // A fixed feature order keeps the bytes, and so the ETag, stable for unchanged data.
                .append(" ORDER BY a.i, a.j) cell")
                .append(" WHERE cell.geom IS NOT NULL) t");
        String statement = sql.toString();
        return runner.read(jdbc -> new TileResult(
                jdbc.queryForObject(statement, p.source(), byte[].class), false, shape));
    }

    /** {@code ST_TileEnvelope(z, x, y)} with bound integers. */
    private static String envelope(TileCoordinates c, SqlParams p) {
        return "ST_TileEnvelope(CAST(" + p.add(c.z()) + " AS integer), CAST(" + p.add(c.x())
                + " AS integer), CAST(" + p.add(c.y()) + " AS integer))";
    }

    /** Layer filter, tombstones and expiry. */
    private String commonFilter(TileLayer layer, SqlParams p, ReadRestriction restriction) {
        return layer.filterSql(p)
                + RestrictionClamp.sqlPredicate("h.source_id", "h.kind", "l.geom", MERGED_PAYLOAD, restriction, p)
                        .map(w -> " AND " + w).orElse("")
                + " AND NOT h.tombstone"
                + " AND (l.valid_until IS NULL OR l.valid_until > " + p.instant(clock.instant()) + ")";
    }

    /**
     * The {@code props} expression: tile properties from the merged payload, then the base
     * properties, which win over a tile property of the same name; nulls are stripped.
     * Property names are validated identifiers ({@link TileLayerResolver#PROPERTY_PATTERN}).
     */
    static String rawProperties(TileLayer layer) {
        List<String> parts = new ArrayList<>();
        List<String> pairs = new ArrayList<>();
        for (String property : layer.tileProperties()) {
            requireValid(property);
            pairs.add("'" + property + "', mp.m -> '" + property + "'");
            if (pairs.size() == PAIRS_PER_OBJECT) {
                parts.add("jsonb_build_object(" + String.join(", ", pairs) + ")");
                pairs.clear();
            }
        }
        if (!pairs.isEmpty()) {
            parts.add("jsonb_build_object(" + String.join(", ", pairs) + ")");
        }
        parts.add("jsonb_build_object('source', h.source_id, 'externalId', h.external_id, 'kind', h.kind,"
                + " 'title', h.title, 'version', h.version, 'live', l.dirty,"
                + " 'observedAt', floor(extract(epoch from l.observed_at))::bigint)");
        return "jsonb_strip_nulls(" + String.join(" || ", parts) + ")";
    }

    /**
     * The numeric rank of a record: its source's {@code rankField} from the merged payload when that
     * value is a JSON number, else null. {@code NULL} when the layer is not rankable.
     */
    static String rankExpression(TileLayer layer, SqlParams p) {
        if (!layer.rankable()) {
            return "CAST(NULL AS double precision)";
        }
        StringBuilder out = new StringBuilder("CASE h.source_id");
        for (Map.Entry<String, String> entry : layer.rankFields().entrySet()) {
            String field = requireValid(entry.getValue());
            out.append(" WHEN ").append(p.text(entry.getKey()))
                    .append(" THEN CASE WHEN jsonb_typeof(mp.m -> '").append(field)
                    .append("') = 'number' THEN (mp.m ->> '").append(field).append("')::double precision END");
        }
        return out.append(" END").toString();
    }

    private static String requireValid(String property) {
        if (!TileLayerResolver.validProperty(property)) {
            throw new IllegalArgumentException("invalid tile property name: " + property);
        }
        return property;
    }
}
