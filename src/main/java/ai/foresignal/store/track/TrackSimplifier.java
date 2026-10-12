package ai.foresignal.store.track;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Track geometry in Java (prompt 037): longitude unwrapping, Douglas–Peucker simplification on
 * {@code (lon, lat, t)}, the {@code maxPoints} search and the antimeridian split (RFC 7946 §3.1.9).
 *
 * <p><strong>Distance.</strong> Positions are projected to meters with a local equirectangular
 * projection per segment (x scaled by the cosine of the segment's mean latitude). The time axis
 * enters through the synchronized Euclidean distance: a point is compared with the position the
 * simplified segment would have <em>at the point's own time</em> (linear interpolation by time
 * between the two kept endpoints), so a stop or a change of speed survives simplification even on
 * a straight road. When both endpoints share a time, the plain perpendicular distance is used.
 *
 * <p><strong>Kept points.</strong> Segment endpoints and points flagged {@link TrackPoint#keep()}
 * are always kept; the algorithm runs between consecutive kept points.
 *
 * <p><strong>{@code maxPoints}.</strong> When the result is larger, a larger tolerance is
 * bisected (at most {@value #SEARCH_ITERATIONS} iterations) up to the track's bounding-box diagonal.
 * If even that leaves too many points (many kept points or segments), the keep flags are dropped,
 * and as a last resort points are decimated evenly, so the result never exceeds {@code maxPoints}.
 */
public final class TrackSimplifier {

    /** Mean Earth radius in meters. */
    public static final double EARTH_RADIUS_M = 6_371_008.8;

    /** Bisection steps of the {@code maxPoints} search. */
    public static final int SEARCH_ITERATIONS = 8;

    /** Lower clamp of {@code tolerance=auto}. */
    public static final double AUTO_MIN_M = 5.0;

    /** Upper clamp of {@code tolerance=auto}. */
    public static final double AUTO_MAX_M = 2_000.0;

    private TrackSimplifier() {}

    /**
     * The simplified segments and the tolerance that produced them.
     *
     * @param segments the segments
     * @param toleranceMeters the tolerance finally used
     */
    public record Result(List<List<TrackPoint>> segments, double toleranceMeters) {

        /** The number of points over every segment. */
        public int points() {
            return count(segments);
        }
    }

    /**
     * {@code segment} with longitudes unwrapped: each longitude moved by a multiple of 360° so that
     * consecutive points are never more than 180° apart.
     */
    public static List<TrackPoint> unwrap(List<TrackPoint> segment) {
        List<TrackPoint> out = new ArrayList<>(segment.size());
        double previous = Double.NaN;
        for (TrackPoint point : segment) {
            double lon = point.lon();
            if (!Double.isNaN(previous)) {
                while (lon - previous > 180) {
                    lon -= 360;
                }
                while (lon - previous < -180) {
                    lon += 360;
                }
            }
            out.add(lon == point.lon() ? point : point.withLon(lon));
            previous = lon;
        }
        return out;
    }

    /** {@code tolerance=auto}: the bounding-box diagonal / 2000, clamped to 5 m–2 km. */
    public static double autoTolerance(List<List<TrackPoint>> segments) {
        double diagonal = diagonalMeters(segments);
        return Math.max(AUTO_MIN_M, Math.min(AUTO_MAX_M, diagonal / 2000.0));
    }

    /** The bounding-box diagonal of every point in meters (equirectangular at the mean latitude). */
    public static double diagonalMeters(List<List<TrackPoint>> segments) {
        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        for (List<TrackPoint> segment : segments) {
            for (TrackPoint p : segment) {
                minLon = Math.min(minLon, p.lon());
                maxLon = Math.max(maxLon, p.lon());
                minLat = Math.min(minLat, p.lat());
                maxLat = Math.max(maxLat, p.lat());
            }
        }
        if (minLon == Double.POSITIVE_INFINITY) {
            return 0;
        }
        double cos = Math.cos(Math.toRadians((minLat + maxLat) / 2));
        double dx = Math.toRadians(maxLon - minLon) * cos * EARTH_RADIUS_M;
        double dy = Math.toRadians(maxLat - minLat) * EARTH_RADIUS_M;
        return Math.hypot(dx, dy);
    }

    /**
     * Simplifies every segment with {@code toleranceMeters}, then searches a larger tolerance while
     * the result has more than {@code maxPoints} points.
     */
    public static Result simplify(List<List<TrackPoint>> segments, double toleranceMeters, int maxPoints) {
        List<List<TrackPoint>> first = simplifyAll(segments, toleranceMeters, true);
        if (count(first) <= maxPoints) {
            return new Result(first, toleranceMeters);
        }
        double hi = Math.max(diagonalMeters(segments), Math.max(toleranceMeters * 2, 1.0));
        List<List<TrackPoint>> atHi = simplifyAll(segments, hi, true);
        if (count(atHi) > maxPoints) {
            List<List<TrackPoint>> unforced = simplifyAll(segments, hi, false);
            if (count(unforced) > maxPoints) {
                return new Result(decimate(unforced, maxPoints), hi);
            }
            return new Result(unforced, hi);
        }
        double lo = toleranceMeters;
        List<List<TrackPoint>> best = atHi;
        double bestTolerance = hi;
        for (int i = 0; i < SEARCH_ITERATIONS; i++) {
            double mid = lo > 0 ? Math.sqrt(lo * hi) : hi / 2;
            List<List<TrackPoint>> candidate = simplifyAll(segments, mid, true);
            if (count(candidate) <= maxPoints) {
                best = candidate;
                bestTolerance = mid;
                hi = mid;
            } else {
                lo = mid;
            }
        }
        return new Result(best, bestTolerance);
    }

    /** Simplifies each segment; {@code honorKeep} false keeps only the endpoints as forced points. */
    static List<List<TrackPoint>> simplifyAll(List<List<TrackPoint>> segments, double toleranceMeters,
            boolean honorKeep) {
        List<List<TrackPoint>> out = new ArrayList<>(segments.size());
        for (List<TrackPoint> segment : segments) {
            out.add(simplify(segment, toleranceMeters, honorKeep));
        }
        return out;
    }

    /** One segment simplified with {@code toleranceMeters}, keeping endpoints and kept points. */
    public static List<TrackPoint> simplify(List<TrackPoint> segment, double toleranceMeters) {
        return simplify(segment, toleranceMeters, true);
    }

    static List<TrackPoint> simplify(List<TrackPoint> segment, double toleranceMeters, boolean honorKeep) {
        int n = segment.size();
        if (n <= 2) {
            return List.copyOf(segment);
        }
        double lat0 = 0;
        for (TrackPoint p : segment) {
            lat0 += p.lat();
        }
        double cos = Math.cos(Math.toRadians(lat0 / n));
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = Math.toRadians(segment.get(i).lon()) * cos * EARTH_RADIUS_M;
            y[i] = Math.toRadians(segment.get(i).lat()) * EARTH_RADIUS_M;
        }
        boolean[] kept = new boolean[n];
        kept[0] = true;
        kept[n - 1] = true;
        if (honorKeep) {
            for (int i = 1; i < n - 1; i++) {
                kept[i] = segment.get(i).keep();
            }
        }
        int start = 0;
        for (int i = 1; i < n; i++) {
            if (kept[i]) {
                douglasPeucker(segment, x, y, start, i, toleranceMeters, kept);
                start = i;
            }
        }
        List<TrackPoint> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (kept[i]) {
                out.add(segment.get(i));
            }
        }
        return out;
    }

    /** Iterative Douglas–Peucker between {@code first} and {@code last} (both kept). */
    private static void douglasPeucker(List<TrackPoint> segment, double[] x, double[] y, int first, int last,
            double tolerance, boolean[] kept) {
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[] {first, last});
        while (!stack.isEmpty()) {
            int[] range = stack.pop();
            int a = range[0];
            int b = range[1];
            if (b - a < 2) {
                continue;
            }
            double worst = -1;
            int index = -1;
            for (int i = a + 1; i < b; i++) {
                double d = distance(segment, x, y, a, b, i);
                if (d > worst) {
                    worst = d;
                    index = i;
                }
            }
            if (worst > tolerance) {
                kept[index] = true;
                stack.push(new int[] {a, index});
                stack.push(new int[] {index, b});
            }
        }
    }

    /**
     * The synchronized Euclidean distance of point {@code i} from segment {@code a → b}: the distance
     * to the time-interpolated position, or the perpendicular distance when {@code a} and {@code b}
     * share a time.
     */
    static double distance(List<TrackPoint> segment, double[] x, double[] y, int a, int b, int i) {
        long ta = segment.get(a).time();
        long tb = segment.get(b).time();
        if (tb != ta) {
            double ratio = (double) (segment.get(i).time() - ta) / (double) (tb - ta);
            double px = x[a] + ratio * (x[b] - x[a]);
            double py = y[a] + ratio * (y[b] - y[a]);
            return Math.hypot(x[i] - px, y[i] - py);
        }
        double dx = x[b] - x[a];
        double dy = y[b] - y[a];
        double length2 = dx * dx + dy * dy;
        if (length2 == 0) {
            return Math.hypot(x[i] - x[a], y[i] - y[a]);
        }
        double u = Math.max(0, Math.min(1, ((x[i] - x[a]) * dx + (y[i] - y[a]) * dy) / length2));
        return Math.hypot(x[i] - (x[a] + u * dx), y[i] - (y[a] + u * dy));
    }

    /**
     * The last resort of the {@code maxPoints} search: endpoints of the newest segments first, then
     * evenly spaced interior points, never more than {@code maxPoints} in total.
     */
    static List<List<TrackPoint>> decimate(List<List<TrackPoint>> segments, int maxPoints) {
        List<List<TrackPoint>> kept = new ArrayList<>(segments);
        int maxSegments = Math.max(1, maxPoints / 2);
        if (kept.size() > maxSegments) {
            kept = new ArrayList<>(kept.subList(kept.size() - maxSegments, kept.size()));
        }
        int endpoints = 0;
        int interior = 0;
        for (List<TrackPoint> segment : kept) {
            endpoints += Math.min(2, segment.size());
            interior += Math.max(0, segment.size() - 2);
        }
        int budget = Math.max(0, maxPoints - endpoints);
        double share = interior == 0 ? 0 : Math.min(1.0, (double) budget / interior);
        List<List<TrackPoint>> out = new ArrayList<>();
        int used = 0;
        for (List<TrackPoint> segment : kept) {
            if (segment.size() <= 2) {
                out.add(List.copyOf(segment));
                continue;
            }
            int inner = segment.size() - 2;
            int take = Math.min((int) Math.floor(inner * share), Math.max(0, budget - used));
            used += take;
            List<TrackPoint> s = new ArrayList<>(take + 2);
            s.add(segment.getFirst());
            for (int k = 1; k <= take; k++) {
                int index = 1 + (int) Math.floor((double) k * inner / (take + 1));
                s.add(segment.get(Math.min(index, segment.size() - 2)));
            }
            s.add(segment.getLast());
            out.add(s);
        }
        return out;
    }

    /** The number of times {@code segment} (unwrapped) crosses a multiple of ±180°. */
    public static int crossings(List<TrackPoint> segment) {
        int count = 0;
        for (int i = 1; i < segment.size(); i++) {
            count += Math.abs(cell(segment.get(i).lon()) - cell(segment.get(i - 1).lon()));
        }
        return count;
    }

    /**
     * Splits unwrapped segments where they cross ±180° (RFC 7946 §3.1.9): the crossing point is
     * interpolated (latitude, time and values), the segment ends at +180 or −180 and the next one
     * starts on the other side; every longitude is normalized to {@code [-180, 180]}.
     */
    public static List<List<TrackPoint>> splitAntimeridian(List<List<TrackPoint>> segments) {
        List<List<TrackPoint>> out = new ArrayList<>();
        for (List<TrackPoint> segment : segments) {
            List<TrackPoint> current = new ArrayList<>();
            for (int i = 0; i < segment.size(); i++) {
                TrackPoint point = segment.get(i);
                if (i > 0) {
                    TrackPoint previous = segment.get(i - 1);
                    int from = cell(previous.lon());
                    int to = cell(point.lon());
                    while (from != to) {
                        boolean east = to > from;
                        double boundary = east ? -180 + 360.0 * (from + 1) : -180 + 360.0 * from;
                        double f = (boundary - previous.lon()) / (point.lon() - previous.lon());
                        TrackPoint crossing = interpolate(previous, point, f, boundary);
                        current.add(crossing.withLon(east ? 180 : -180));
                        out.add(current);
                        current = new ArrayList<>();
                        current.add(crossing.withLon(east ? -180 : 180));
                        from += east ? 1 : -1;
                    }
                }
                current.add(point.withLon(normalize(point.lon())));
            }
            if (!current.isEmpty()) {
                out.add(current);
            }
        }
        return out;
    }

    /** {@code lon} normalized to {@code [-180, 180)}, keeping exactly ±180 unchanged. */
    static double normalize(double lon) {
        if (lon >= -180 && lon <= 180) {
            return lon;
        }
        double shifted = ((lon + 180) % 360 + 360) % 360 - 180;
        return shifted;
    }

    /** Which 360° cell an unwrapped longitude is in: 0 for {@code [-180, 180)}, plus or minus one per wrap. */
    static int cell(double lon) {
        if (lon == 180) {
            return 0;
        }
        return (int) Math.floor((lon + 180) / 360);
    }

    private static TrackPoint interpolate(TrackPoint a, TrackPoint b, double f, double lon) {
        double lat = a.lat() + f * (b.lat() - a.lat());
        long time = a.time() + Math.round(f * (b.time() - a.time()));
        Double[] values = new Double[Math.max(a.values().length, b.values().length)];
        for (int k = 0; k < values.length; k++) {
            Double va = k < a.values().length ? a.values()[k] : null;
            Double vb = k < b.values().length ? b.values()[k] : null;
            values[k] = va == null || vb == null ? (va != null ? va : vb) : va + f * (vb - va);
        }
        return new TrackPoint(lon, lat, time, true, values);
    }

    /** The number of points over {@code segments}. */
    public static int count(List<List<TrackPoint>> segments) {
        int n = 0;
        for (List<TrackPoint> segment : segments) {
            n += segment.size();
        }
        return n;
    }
}
