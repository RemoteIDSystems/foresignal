package ai.foresignal.source.spi;

import java.util.Collection;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;

/**
 * US county outlines for adapters (prompt 047): the union of the counties of the reference table
 * {@code ref_admin2} (prompt 040), looked up by five-digit FIPS code.
 *
 * <p>An SPI type, so an adapter can draw a county-based alert without touching the store
 * (Architecture §3); {@code ai.foresignal.reference.CountyUnionService} implements it.
 */
public interface CountyGeometries {

    /** The tolerance, in degrees, every returned union is simplified to. */
    double SIMPLIFY_DEGREES = 0.005;

    /** True when {@code ref_admin2} is loaded. */
    boolean available();

    /**
     * The union of the named counties, simplified to {@link #SIMPLIFY_DEGREES}, in SRID 4326.
     *
     * @param fips5 five-digit county FIPS codes
     * @return empty when the table is not loaded, no code is given, or none of the codes is known
     */
    Optional<Geometry> union(Collection<String> fips5);

    /**
     * The union of every county of one state (prompt 086: a SAME location {@code PSS000} means the whole state),
     * simplified to {@link #SIMPLIFY_DEGREES}, in SRID 4326. The default has no state outlines.
     *
     * @param stateFips2 the two-digit state FIPS code
     * @return empty when the table is not loaded, the code is malformed, or no county of the state is known
     */
    default Optional<Geometry> stateUnion(String stateFips2) {
        return Optional.empty();
    }

    /** A lookup that never has county data, for unit tests and contexts without the table. */
    static CountyGeometries none() {
        return new CountyGeometries() {
            @Override
            public boolean available() {
                return false;
            }

            @Override
            public Optional<Geometry> union(Collection<String> fips5) {
                return Optional.empty();
            }
        };
    }
}
