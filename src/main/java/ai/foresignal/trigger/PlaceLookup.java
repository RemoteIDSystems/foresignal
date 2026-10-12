package ai.foresignal.trigger;

import java.util.Optional;
import org.locationtech.jts.geom.Geometry;

/**
 * Resolves a place id for a rule's {@code near.placeId} (prompt 036 provides the bean and
 * re-materializes rules when a place is edited). Without a bean, a rule that names a place is
 * rejected with {@code 400 PLACES_UNAVAILABLE}.
 */
public interface PlaceLookup {

    /** The place's geometry (a point or an area, SRID 4326), or empty when there is no such place. */
    Optional<Geometry> geometry(String placeId);
}
