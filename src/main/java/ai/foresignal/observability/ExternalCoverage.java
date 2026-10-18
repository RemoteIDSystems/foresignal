package ai.foresignal.observability;

import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Declared coverage of external (EDP) streams bound to canonical-event families (prompt 138): the kinds a
 * {@code canonical-family} binding adds to a family, and each bound stream's manifest {@code coverage} (footprint and
 * detection probability), used as a low-confidence {@code declared} estimate until Foresignal can measure the source.
 * The binding package provides the bean; without it nothing is added.
 */
public interface ExternalCoverage {

    /** The record kinds external bindings link into {@code family} (empty when none). */
    Set<String> kindsOf(String family);

    /** The declared coverage of {@code sourceId} for {@code family}, or null when its bound streams declare none. */
    Declared coverage(String sourceId, String family);

    /**
     * A stream's declared coverage.
     *
     * @param area the footprint (WGS84), or null for everywhere
     * @param pDetect the detection probability inside the footprint
     */
    record Declared(Geometry area, double pDetect) {

        private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

        /** Clamps the probability to [0, 1]. */
        public Declared {
            pDetect = Double.isFinite(pDetect) ? Math.max(0, Math.min(1, pDetect)) : 0;
        }

        /**
         * The detection probability at {@code place}: {@link #pDetect()} inside the footprint (or everywhere without
         * one), 0 outside it, and 0 for a place without a point when a footprint is declared (absence then stays
         * unproven).
         */
        public double p(Place place) {
            if (area == null) {
                return pDetect;
            }
            if (place == null || !place.located()) {
                return 0;
            }
            return area.covers(FACTORY.createPoint(new Coordinate(place.lon(), place.lat()))) ? pDetect : 0;
        }
    }
}
