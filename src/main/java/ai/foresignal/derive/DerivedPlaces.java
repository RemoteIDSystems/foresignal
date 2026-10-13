package ai.foresignal.derive;

import ai.foresignal.place.PlaceView;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * The operator's places as a {@link DerivedGenerator} sees them (prompt 052): the snapshots of prompt
 * 036's place cache and the places AOI. Read-only.
 */
public interface DerivedPlaces {

    /** Every place, ordered by id. */
    List<PlaceView> all();

    /** One place, or empty when it does not exist (deleted). */
    Optional<PlaceView> find(String id);

    /** The places AOI (prompt 036): the union of the buffered places, or the default box. */
    MultiPolygon aoi();

    /** The places AOI's envelope {@code [minLon, minLat, maxLon, maxLat]}. */
    double[] aoiEnvelope();
}
