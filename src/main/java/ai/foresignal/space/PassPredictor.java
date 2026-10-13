package ai.foresignal.space;

import ai.foresignal.orbit.OrekitData;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.frames.Frame;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.ElevationExtremumDetector;
import org.orekit.propagation.events.handlers.RecordAndContinue;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

/**
 * Predicts satellite passes over a ground observer with Orekit's SGP4 (prompt 064, Weather-Imagery-Design
 * §3 "Orekit").
 *
 * <ul>
 *   <li>The element set becomes an Orekit {@link TLE} through the <em>field</em> constructor, so no TLE text is
 *       involved and six-digit catalog numbers (five-digit numbers ran out on 2026-07-11) are accepted. The
 *       unit conversions are those of Orekit's own TLE parser: mean motion rev/day → rad/s,
 *       {@code MEAN_MOTION_DOT} × π / 1.86624e9 (rad/s²), {@code MEAN_MOTION_DDOT} × π / 5.3747712e13 (rad/s³),
 *       angles in radians; verified locally against a parsed CelesTrak TLE of the same element set.
 *   <li>A {@link TLEPropagator} carries an {@link ElevationDetector} on a {@link TopocentricFrame} over the
 *       WGS84 {@link OneAxisEllipsoid} (60 s max check, 1 ms threshold) for AOS and LOS, and an
 *       {@link ElevationExtremumDetector} for the culmination.
 *   <li>SGP4 needs UTC−TAI and TEME→ITRF needs EOP: both come from {@link OrekitData}'s pinned
 *       {@code orekit-data}. Outside the EOP file's range Orekit applies no corrections (the no-EOP fallback),
 *       an error far below a kilometer for pass times.
 * </ul>
 *
 * <p>Pure, apart from Orekit's data loading.
 */
public final class PassPredictor {

    /** The most ground-track points of one pass. */
    public static final int MAX_TRACK_POINTS = 60;

    /** The elevation detector's largest step between checks, seconds. */
    public static final double MAX_CHECK_SECONDS = 60;

    /** The elevation detector's convergence threshold, seconds. */
    public static final double THRESHOLD_SECONDS = 0.001;

    /** How far before {@code from} propagation starts, so a pass already in progress keeps its AOS. */
    static final Duration LOOKBACK = Duration.ofMinutes(30);

    private PassPredictor() {}

    /**
     * Predicts the passes whose LOS is after {@code from} and whose AOS is before {@code from + horizon}.
     *
     * @param e the element set
     * @param observer where the observer is
     * @param from the start of the window
     * @param horizon how far ahead to look
     * @param minElevationDeg the elevation a pass must rise above, degrees
     * @return the passes in AOS order; a pass whose LOS falls after the horizon is left out
     * @throws IllegalArgumentException when the elements cannot be propagated
     */
    public static List<PredictedPass> predict(OmmElements e, Observer observer, Instant from, Duration horizon,
            double minElevationDeg) {
        DataContext context = OrekitData.context();
        TimeScale utc = context.getTimeScales().getUTC();
        Frame teme = context.getFrames().getTEME();
        Frame itrf = context.getFrames().getITRF(IERSConventions.IERS_2010, true);
        OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, itrf);
        TopocentricFrame topo = new TopocentricFrame(earth, new GeodeticPoint(Math.toRadians(observer.lat()),
                Math.toRadians(observer.lon()), observer.altM()), "observer");
        TLE tle = toTle(e, utc);

        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle, teme);
        RecordAndContinue horizonEvents = new RecordAndContinue();
        RecordAndContinue extrema = new RecordAndContinue();
        propagator.addEventDetector(new ElevationDetector(MAX_CHECK_SECONDS, THRESHOLD_SECONDS, topo)
                .withConstantElevation(Math.toRadians(minElevationDeg)).withHandler(horizonEvents));
        propagator.addEventDetector(new ElevationExtremumDetector(MAX_CHECK_SECONDS, THRESHOLD_SECONDS, topo)
                .withHandler(extrema));
        AbsoluteDate start = new AbsoluteDate(from.minus(LOOKBACK), utc);
        AbsoluteDate end = new AbsoluteDate(from.plus(horizon), utc);
        try {
            propagator.propagate(start, end);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("NORAD " + e.norad() + " could not be propagated: " + ex.getMessage(), ex);
        }

        TLEPropagator plain = TLEPropagator.selectExtrapolator(tle, teme);
        List<PredictedPass> passes = new ArrayList<>();
        AbsoluteDate aos = null;
        for (RecordAndContinue.Event event : horizonEvents.getEvents()) {
            AbsoluteDate date = event.getState().getDate();
            if (event.isIncreasing()) {
                aos = date;
                continue;
            }
            if (aos == null) {
                continue; // a pass already in progress before the lookback: no AOS
            }
            AbsoluteDate los = date;
            Instant losInstant = los.toDate(utc).toInstant();
            if (losInstant.isAfter(from)) {
                passes.add(pass(aos, los, extrema, plain, topo, teme, earth, utc, minElevationDeg));
            }
            aos = null;
        }
        return passes;
    }

    private static PredictedPass pass(AbsoluteDate aos, AbsoluteDate los, RecordAndContinue extrema,
            TLEPropagator plain, TopocentricFrame topo, Frame teme, OneAxisEllipsoid earth, TimeScale utc,
            double minElevationDeg) {
        AbsoluteDate tca = null;
        double maxEl = Double.NEGATIVE_INFINITY;
        for (RecordAndContinue.Event x : extrema.getEvents()) {
            SpacecraftState s = x.getState();
            AbsoluteDate d = s.getDate();
            if (x.isIncreasing() || d.isBefore(aos) || d.isAfter(los)) {
                continue;
            }
            double el = Math.toDegrees(topo.getElevation(s.getPosition(), s.getFrame(), d));
            if (el > maxEl) {
                maxEl = el;
                tca = d;
            }
        }
        if (tca == null) {
            tca = aos.shiftedBy(los.durationFrom(aos) / 2);
            maxEl = elevation(plain, topo, teme, tca);
        }
        double aosAz = azimuth(plain, topo, teme, aos);
        double losAz = azimuth(plain, topo, teme, los);
        return new PredictedPass(aos.toDate(utc).toInstant(), tca.toDate(utc).toInstant(), los.toDate(utc).toInstant(),
                Math.max(maxEl, minElevationDeg), aosAz, losAz, track(plain, teme, earth, aos, los));
    }

    /**
     * Builds the Orekit TLE of an element set through the field constructor (no text, so six-digit catalog
     * numbers work).
     */
    static TLE toTle(OmmElements e, TimeScale utc) {
        int year = 0;
        int number = 0;
        String piece = "A";
        String id = e.objectId();
        if (id != null && id.length() >= 9 && id.charAt(4) == '-') {
            try {
                year = Integer.parseInt(id.substring(0, 4));
                number = Integer.parseInt(id.substring(5, 8));
                piece = id.substring(8);
            } catch (NumberFormatException ignored) {
                year = 0;
                number = 0;
                piece = "A";
            }
        }
        double meanMotion = e.meanMotion() * 2 * Math.PI / Constants.JULIAN_DAY;
        double ndot = e.meanMotionDot() * Math.PI / 1.86624e9;
        double nddot = e.meanMotionDdot() * Math.PI / 5.3747712e13;
        return new TLE((int) e.norad(), e.classification().charAt(0), year, number, piece, e.ephemerisType(),
                e.elementSetNo(), new AbsoluteDate(e.epoch(), utc), meanMotion, ndot, nddot, e.eccentricity(),
                Math.toRadians(e.inclination()), Math.toRadians(e.argOfPericenter()), Math.toRadians(e.raan()),
                Math.toRadians(e.meanAnomaly()), (int) Math.min(Integer.MAX_VALUE, e.revAtEpoch()), e.bstar(), utc);
    }

    private static double elevation(TLEPropagator p, TopocentricFrame topo, Frame teme, AbsoluteDate d) {
        Vector3D position = p.getPVCoordinates(d, teme).getPosition();
        return Math.toDegrees(topo.getElevation(position, teme, d));
    }

    private static double azimuth(TLEPropagator p, TopocentricFrame topo, Frame teme, AbsoluteDate d) {
        Vector3D position = p.getPVCoordinates(d, teme).getPosition();
        double az = Math.toDegrees(topo.getAzimuth(position, teme, d));
        return az < 0 ? az + 360 : az;
    }

    /** At most {@value #MAX_TRACK_POINTS} sub-satellite points, evenly spaced from AOS to LOS. */
    static List<double[]> track(TLEPropagator p, Frame teme, OneAxisEllipsoid earth, AbsoluteDate aos,
            AbsoluteDate los) {
        double seconds = Math.max(0, los.durationFrom(aos));
        int n = (int) Math.max(2, Math.min(MAX_TRACK_POINTS, Math.floor(seconds / 10) + 1));
        List<double[]> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            AbsoluteDate d = aos.shiftedBy(seconds * i / (n - 1));
            Vector3D position = p.getPVCoordinates(d, teme).getPosition();
            GeodeticPoint g = earth.transform(position, teme, d);
            out.add(new double[] {Math.toDegrees(g.getLongitude()), Math.toDegrees(g.getLatitude())});
        }
        return out;
    }
}
