package ai.foresignal.source.adapters.cdc;

import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.Geo;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Places a CDC NWSS record without any network call (prompt 095).
 *
 * <ul>
 *   <li>A detection names a {@code county_fips}: the detection's geometry is the interior point of that county's
 *       040 outline.</li>
 *   <li>A site level names a {@code state} (and a list of counties by name): the geometry is the interior point
 *       of the state's county union, via {@link CountyGeometries#stateUnion}. The county names are not FIPS, so
 *       they cannot be looked up offline (see the 095 report, Deviations).</li>
 * </ul>
 *
 * <p>Every geometry is approximate, so each placed record carries {@code geometryApproximate: true}. When the
 * reference data is not loaded, the geometry is null.
 */
public final class NwssGeometry {

    private final CountyGeometries counties;

    /** Creates the resolver. */
    public NwssGeometry(CountyGeometries counties) {
        this.counties = counties;
    }

    /** The interior point of the county with five-digit FIPS {@code fips5}, or null. */
    public Point forCounty(String fips5) {
        if (counties == null || fips5 == null || !fips5.strip().matches("\\d{5}")) {
            return null;
        }
        return counties.union(List.of(fips5.strip())).flatMap(NwssGeometry::centre).orElse(null);
    }

    /** The interior point of the state's county union, given a USPS code or a full state name, or null. */
    public Point forState(String state) {
        String fips2 = UsStates.fips2(state);
        if (counties == null || fips2 == null) {
            return null;
        }
        return counties.stateUnion(fips2).flatMap(NwssGeometry::centre).orElse(null);
    }

    private static Optional<Point> centre(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return Optional.empty();
        }
        Point point = geometry.getInteriorPoint();
        if (point == null || point.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Geo.point(point.getX(), point.getY()));
    }
}
