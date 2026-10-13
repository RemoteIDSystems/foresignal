package ai.foresignal.derive;

import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.place.PlacesAoiService;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.stereotype.Component;

/** {@link DerivedPlaces} over prompt 036's place cache and places AOI (prompt 052). */
@Component
public class ServiceDerivedPlaces implements DerivedPlaces {

    private final PlaceService places;
    private final PlacesAoiService aoi;

    /** Creates the view. */
    public ServiceDerivedPlaces(PlaceService places, PlacesAoiService aoi) {
        this.places = places;
        this.aoi = aoi;
    }

    @Override
    public List<PlaceView> all() {
        return places.all().stream().sorted(Comparator.comparing(PlaceView::id)).toList();
    }

    @Override
    public Optional<PlaceView> find(String id) {
        return places.find(id);
    }

    @Override
    public MultiPolygon aoi() {
        return aoi.aoi();
    }

    @Override
    public double[] aoiEnvelope() {
        return aoi.envelope().clone();
    }
}
