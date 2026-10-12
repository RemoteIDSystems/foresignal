package ai.foresignal.place;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * An immutable snapshot of one place (prompt 036), as the service caches it.
 *
 * @param id the slug id
 * @param name the name
 * @param shape {@code point}, {@code polygon} or {@code route}
 * @param geometry the place's own geometry
 * @param radiusKm the radius of a point place, or null
 * @param bufferM the buffer of a route place, or null
 * @param area the derived area
 * @param timezone the IANA timezone
 * @param kind {@code user}, {@code airport} or {@code asset}
 * @param tags the tags
 * @param products weather product ids
 * @param attributes free attributes
 * @param sensitive whether the place is sensitive
 * @param version the version
 * @param createdAt when created
 * @param updatedAt when last saved
 */
public record PlaceView(String id, String name, String shape, Geometry geometry, Double radiusKm, Double bufferM,
        MultiPolygon area, String timezone, String kind, List<String> tags, List<String> products,
        Map<String, Object> attributes, boolean sensitive, int version, Instant createdAt, Instant updatedAt) {

    /** Copies the collections. */
    public PlaceView {
        tags = tags == null ? List.of() : List.copyOf(tags);
        products = products == null ? List.of() : List.copyOf(products);
        attributes = attributes == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(attributes));
    }

    /** The snapshot of {@code entity}. */
    public static PlaceView of(PlaceEntity entity) {
        return new PlaceView(entity.getId(), entity.getName(), entity.getShape(), entity.getGeom(), entity.getRadiusKm(),
                entity.getBufferM(), entity.getArea(), entity.getTimezone(), entity.getKind(), entity.getTags(),
                entity.getProducts(), entity.getAttributes(), entity.isSensitive(), entity.getVersion(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
