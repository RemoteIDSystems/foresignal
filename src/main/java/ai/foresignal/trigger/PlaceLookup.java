package ai.foresignal.trigger;

import java.util.Optional;
import org.locationtech.jts.geom.Geometry;

/**
 * Resolves a place id for a rule's {@code near.placeId}/{@code near.placeIds} (prompt 036 provides
 * the bean and re-materializes rules when a place is edited). Without a bean, a rule that names a
 * place is rejected with {@code 400 PLACES_UNAVAILABLE}.
 */
public interface PlaceLookup {

    /**
     * The place's area (a MultiPolygon derived on save, SRID 4326; a bare point from older lookups is
     * buffered by the rule's radius), or empty when there is no such place.
     */
    Optional<Geometry> geometry(String placeId);

    /** The place's version (bumped on every save), or empty when there is no such place. */
    default Optional<Integer> version(String placeId) {
        return Optional.empty();
    }

    /** The place's name, for {@code {{place.name}}}; empty when there is no such place. */
    default Optional<String> name(String placeId) {
        return Optional.empty();
    }

    /**
     * The place's own geometry (its point, polygon or route, not the derived area), for
     * {@code {{place.distanceKm}}}; never rendered anywhere.
     */
    default Optional<Geometry> origin(String placeId) {
        return Optional.empty();
    }
}
