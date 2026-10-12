package ai.foresignal.place;

import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Maps places to the {@code Place}, {@code PlacePage} and {@code PlacesAoi} schemas (prompt 036).
 * Only token holders reach these (places are always authenticated); MCP read tools use the compact
 * form without coordinates ({@link #compact}).
 */
@Component
public class PlaceMapper {

    private final GeoJsonWriter geoJson = new GeoJsonWriter();

    /** The {@code Place} object. */
    public Map<String, Object> place(PlaceView p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.id());
        out.put("name", p.name());
        out.put("shape", p.shape());
        out.put("geometry", geoJson.write(p.geometry()));
        if (p.radiusKm() != null) {
            out.put("radiusKm", p.radiusKm());
        }
        if (p.bufferM() != null) {
            out.put("bufferM", p.bufferM());
        }
        out.put("timezone", p.timezone());
        out.put("kind", p.kind());
        out.put("tags", p.tags());
        out.put("products", p.products());
        out.put("sensitive", p.sensitive());
        out.put("attributes", p.attributes());
        out.put("area", geoJson.write(p.area()));
        out.put("version", p.version());
        out.put("createdAt", p.createdAt());
        out.put("updatedAt", p.updatedAt());
        return out;
    }

    /**
     * A place without any coordinate: id, name, shape, kind, radius or buffer, tags, timezone and
     * version (MCP short output and {@code foresignal://places} for read tokens).
     */
    public Map<String, Object> compact(PlaceView p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.id());
        out.put("name", p.name());
        out.put("shape", p.shape());
        out.put("kind", p.kind());
        if (p.radiusKm() != null) {
            out.put("radiusKm", p.radiusKm());
        }
        if (p.bufferM() != null) {
            out.put("bufferM", p.bufferM());
        }
        out.put("tags", p.tags());
        out.put("timezone", p.timezone());
        out.put("version", p.version());
        return out;
    }

    /** The {@code PlacePage} object. */
    public Map<String, Object> page(List<PlaceView> items, String nextCursor) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items.stream().map(this::place).toList());
        out.put("nextCursor", nextCursor);
        return out;
    }

    /** The {@code PlacesAoi} object. */
    public Map<String, Object> aoi(PlacesAoiService.Aoi aoi) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("version", aoi.version());
        out.put("geometry", geoJson.write(aoi.geometry()));
        out.put("envelope", List.of(aoi.envelope()[0], aoi.envelope()[1], aoi.envelope()[2], aoi.envelope()[3]));
        out.put("placeCount", aoi.placeCount());
        out.put("bufferKm", aoi.bufferKm());
        out.put("isDefault", aoi.isDefault());
        return out;
    }
}
