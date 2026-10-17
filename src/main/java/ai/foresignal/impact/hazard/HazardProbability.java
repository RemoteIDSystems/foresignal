package ai.foresignal.impact.hazard;

import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * {@code P(hazard at asset)} (prompt 127 §2). Pure.
 *
 * <ul>
 *   <li><b>Published polygons</b> (NHC): the highest band containing the asset, or within {@code assetBufferKm} of it
 *       (default 5 km).
 *   <li><b>Published point probabilities</b> (SPC, WPC): the highest band containing the asset; the producer's value
 *       already means "within 25 miles of the point", so no buffer is added.
 *   <li><b>Gauge categories</b>: the calibrated probability within the gauge's area.
 * </ul>
 *
 * The probability used is each band's lower bound; the band's upper bound (1 when open) is the interval's top. Distances
 * are in degrees scaled by 111.32 km per degree of latitude, so a buffer is never wider than intended.
 */
public final class HazardProbability {

    /** Kilometers per degree of latitude. */
    public static final double KM_PER_DEGREE = 111.32;

    private HazardProbability() {}

    /**
     * The probability of the hazard at an asset.
     *
     * @param pHazard the probability used (the band's lower bound, or a calibrated rate)
     * @param lower the interval's lower bound
     * @param upper the interval's upper bound
     * @param band the band
     */
    public record At(double pHazard, double lower, double upper, HazardForecast.Band band) {}

    /**
     * The highest band at an asset, if any.
     *
     * @param forecast a probabilistic forecast
     * @param asset the asset's geometry (a point or an area)
     * @param assetBufferKm the buffer around the asset for published polygons
     */
    public static Optional<At> at(HazardForecast forecast, Geometry asset, double assetBufferKm) {
        if (forecast == null || asset == null || asset.isEmpty() || !forecast.probabilistic()) {
            return Optional.empty();
        }
        double buffer = HazardForecast.MODEL_POLYGONS.equals(forecast.probabilityModel())
                ? Math.max(0, assetBufferKm) / KM_PER_DEGREE : 0;
        for (HazardForecast.Band band : forecast.probabilityField()) {
            if (band.pMin() <= 0 || band.geometry() == null) {
                continue;
            }
            boolean inside = buffer > 0 ? band.geometry().isWithinDistance(asset, buffer)
                    : band.geometry().intersects(asset);
            if (inside) {
                return Optional.of(of(forecast, band));
            }
        }
        return Optional.empty();
    }

    /**
     * The highest band intersecting an area (the hazard's probability somewhere in an admin1 region), if any.
     *
     * @param forecast a probabilistic forecast
     * @param area the area
     */
    public static Optional<At> inArea(HazardForecast forecast, Geometry area) {
        return at(forecast, area, 0);
    }

    /** The probability and interval a band gives. */
    static At of(HazardForecast forecast, HazardForecast.Band band) {
        double lower = band.pMin();
        if (HazardForecast.MODEL_GAUGE.equals(forecast.probabilityModel())
                && forecast.details().get("pLower") instanceof Number n) {
            lower = n.doubleValue();
        }
        double upper = band.pMax() == null ? 1.0 : band.pMax();
        return new At(band.pMin(), Math.min(lower, band.pMin()), Math.max(upper, band.pMin()), band);
    }

    /** The approximate distance in kilometers between two geometries (0 when they touch). */
    public static double distanceKm(Geometry a, Geometry b) {
        return a.distance(b) * KM_PER_DEGREE;
    }

    /** A point's representative point (itself), or an area's interior point. */
    public static Point representative(Geometry g) {
        return g instanceof Point p ? p : g.getInteriorPoint();
    }
}
