package ai.foresignal.raster;

import ai.foresignal.source.spi.GridDefinition;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Dedupes native grids into {@code raster_grid} by the SHA-256 of their canonical definition
 * (Weather-Imagery-Design §2.3). A grid is stored data only here; prompt 045 adds the projection
 * maths.
 *
 * <p>The canonical definition is a JSON object with sorted keys ({@code east}, {@code north},
 * {@code nx}, {@code ny}, {@code params}, {@code south}, {@code type}, {@code west}), so the same grid
 * always hashes the same. Every commit upserts with {@code ON CONFLICT DO NOTHING}: cheap, and correct
 * even if the table was emptied underneath a running process.
 */
@Component
public class GridStore {

    static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Map<String, GridDefinition> byId = new ConcurrentHashMap<>();

    /** Creates the store. */
    public GridStore(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /** The canonical definition of {@code grid}, keys sorted. */
    public static Map<String, Object> canonical(GridDefinition grid) {
        Map<String, Object> out = new TreeMap<>();
        out.put("type", grid.type());
        out.put("nx", grid.nx());
        out.put("ny", grid.ny());
        out.put("west", grid.west());
        out.put("south", grid.south());
        out.put("east", grid.east());
        out.put("north", grid.north());
        out.put("params", new TreeMap<>(grid.params()));
        return out;
    }

    /** The grid id: hex SHA-256 of the canonical definition JSON. */
    public String id(GridDefinition grid) {
        return FrameStore.sha256Hex(json.writeValueAsBytes(canonical(grid)));
    }

    /**
     * Stores {@code grid} unless a row with its id exists.
     *
     * @return the grid id
     */
    public String upsert(GridDefinition grid) {
        String text = json.writeValueAsString(canonical(grid));
        String id = FrameStore.sha256Hex(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        jdbc.update("INSERT INTO raster_grid (id, type, definition, nx, ny, bbox) VALUES (?, ?, ?::jsonb, ?, ?,"
                        + " ST_GeomFromText(?, 4326)) ON CONFLICT (id) DO NOTHING",
                id, grid.type(), text, grid.nx(), grid.ny(), bbox(grid).toText());
        byId.put(id, grid);
        return id;
    }

    /** The grid with {@code id}, from memory or the table. */
    public Optional<GridDefinition> find(String id) {
        GridDefinition known = byId.get(id);
        if (known != null) {
            return Optional.of(known);
        }
        List<String> rows = jdbc.queryForList("SELECT definition::text FROM raster_grid WHERE id = ?", String.class, id);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        GridDefinition grid = fromCanonical(json.readValue(rows.get(0), new TypeReference<Map<String, Object>>() {}));
        byId.put(id, grid);
        return Optional.of(grid);
    }

    /** Rebuilds a grid from its canonical definition. */
    @SuppressWarnings("unchecked")
    static GridDefinition fromCanonical(Map<String, Object> m) {
        Object params = m.get("params");
        return new GridDefinition((String) m.get("type"), ((Number) m.get("nx")).intValue(),
                ((Number) m.get("ny")).intValue(), ((Number) m.get("west")).doubleValue(),
                ((Number) m.get("south")).doubleValue(), ((Number) m.get("east")).doubleValue(),
                ((Number) m.get("north")).doubleValue(), params instanceof Map<?, ?> p ? (Map<String, Object>) p : Map.of());
    }

    /**
     * The grid's bounding box as a polygon. A box crossing the antimeridian ({@code west > east}) is
     * stored as the full longitude range, since the column holds one polygon.
     */
    public static Polygon bbox(GridDefinition grid) {
        double west = grid.west() > grid.east() ? -180 : grid.west();
        double east = grid.west() > grid.east() ? 180 : grid.east();
        return box(west, grid.south(), east, grid.north());
    }

    /** A WGS84 box polygon. */
    static Polygon box(double west, double south, double east, double north) {
        return GEOMETRY.createPolygon(new Coordinate[] {
                new Coordinate(west, south), new Coordinate(east, south), new Coordinate(east, north),
                new Coordinate(west, north), new Coordinate(west, south)});
    }
}
