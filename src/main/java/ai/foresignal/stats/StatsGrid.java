package ai.foresignal.stats;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A spatial activity aggregate (OpenAPI {@code StatsGrid}, prompt 038): a GeoJSON
 * {@code FeatureCollection} of H3 cells (rollup mode) or EPSG:3857 hexagons (raw mode).
 *
 * @param cellSystem {@code h3} or {@code hex3857}
 * @param res the H3 resolution (rollup mode), else null
 * @param sizeKm the hexagon size (raw mode), else null
 * @param measure the measure
 * @param backend {@code rollup} or {@code raw}
 * @param cells the occupied cells, by id
 */
public record StatsGrid(String cellSystem, Integer res, Double sizeKm, String measure, String backend,
        List<Cell> cells) {

    /** Copies the cells. */
    public StatsGrid {
        cells = cells == null ? List.of() : List.copyOf(cells);
    }

    /**
     * One cell.
     *
     * @param id the H3 address, or {@code i,j} of a raw hexagon
     * @param geometry its GeoJSON geometry
     * @param value the measure
     */
    public record Cell(String id, Map<String, Object> geometry, long value) {}

    /** The GeoJSON document. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "FeatureCollection");
        out.put("cellSystem", cellSystem);
        out.put("res", res);
        out.put("sizeKm", sizeKm);
        out.put("measure", measure);
        out.put("backend", backend);
        out.put("features", cells.stream().map(c -> {
            Map<String, Object> feature = new LinkedHashMap<>();
            feature.put("type", "Feature");
            feature.put("id", c.id());
            feature.put("geometry", c.geometry());
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("cell", c.id());
            properties.put("value", c.value());
            feature.put("properties", properties);
            return feature;
        }).toList());
        return out;
    }
}
