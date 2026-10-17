package ai.foresignal.pol;

import ai.foresignal.pol.StayPointExtractor.Stay;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Place statistics of the pattern-of-life analysis (prompt 114): the hour-of-week dwell histogram (168 buckets,
 * Monday 00:00 in the operator's timezone first) and a place's centroid, p90 radius, visits and dwell. Pure.
 */
public final class PolStats {

    /** Buckets of an hour-of-week histogram. */
    public static final int BUCKETS = 168;

    private PolStats() {}

    /** The hour-of-week bucket of {@code t} in {@code zone}: {@code (day − Monday) × 24 + hour}. */
    public static int bucket(Instant t, ZoneId zone) {
        ZonedDateTime z = t.atZone(zone);
        return (z.getDayOfWeek().getValue() - 1) * 24 + z.getHour();
    }

    /** The start of the hour-of-week bucket containing {@code t}. */
    public static Instant bucketStart(Instant t, ZoneId zone) {
        return t.atZone(zone).truncatedTo(ChronoUnit.HOURS).toInstant();
    }

    /**
     * Adds a stay's dwell to {@code histogram}, spread over the local hours it covers, in whole minutes per
     * bucket (rounded).
     */
    public static void addDwell(int[] histogram, Instant arrive, Instant depart, ZoneId zone) {
        Instant t = arrive;
        while (t.isBefore(depart)) {
            Instant hourEnd = t.atZone(zone).truncatedTo(ChronoUnit.HOURS).plusHours(1).toInstant();
            Instant end = hourEnd.isBefore(depart) ? hourEnd : depart;
            long seconds = Duration.between(t, end).toSeconds();
            histogram[bucket(t, zone)] += (int) Math.round(seconds / 60.0);
            t = end;
        }
    }

    /** The sum of a histogram. */
    public static long total(int[] histogram) {
        long sum = 0;
        for (int v : histogram) {
            sum += v;
        }
        return sum;
    }

    /**
     * A place's statistics.
     *
     * @param lon the centroid longitude (mean of the member centroids)
     * @param lat the centroid latitude
     * @param radiusM the p90 distance of member centroids from the centroid, meters
     * @param visits the number of stays
     * @param totalDwellMinutes the total dwell, minutes
     * @param hourOfWeek the dwell histogram
     * @param firstSeen the first arrival
     * @param lastSeen the last departure
     */
    public record PlaceStats(double lon, double lat, double radiusM, int visits, double totalDwellMinutes,
            int[] hourOfWeek, Instant firstSeen, Instant lastSeen) {}

    /** The statistics of a non-empty group of stays, computed in {@code frame}. */
    public static PlaceStats place(List<Stay> stays, LocalFrame frame, ZoneId zone) {
        double sx = 0;
        double sy = 0;
        List<double[]> xy = new ArrayList<>();
        int[] histogram = new int[BUCKETS];
        double dwell = 0;
        Instant first = null;
        Instant last = null;
        for (Stay s : stays) {
            double[] p = frame.toXy(s.lon(), s.lat());
            xy.add(p);
            sx += p[0];
            sy += p[1];
            addDwell(histogram, s.arrive(), s.depart(), zone);
            dwell += s.dwell().toSeconds() / 60.0;
            first = first == null || s.arrive().isBefore(first) ? s.arrive() : first;
            last = last == null || s.depart().isAfter(last) ? s.depart() : last;
        }
        double cx = sx / stays.size();
        double cy = sy / stays.size();
        double[] distances = xy.stream().mapToDouble(p -> Math.hypot(p[0] - cx, p[1] - cy)).toArray();
        double[] centroid = frame.toLonLat(cx, cy);
        return new PlaceStats(centroid[0], centroid[1], percentile90(distances), stays.size(), dwell, histogram, first,
                last);
    }

    /** The 90th percentile (nearest rank) of {@code values}; 0 when empty. */
    public static double percentile90(double[] values) {
        if (values.length == 0) {
            return 0;
        }
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        int rank = (int) Math.ceil(0.9 * sorted.length);
        return sorted[Math.max(0, rank - 1)];
    }

    /** {@code value} rounded to {@code decimals} decimal places (the private precision cap). */
    public static double round(double value, int decimals) {
        double f = Math.pow(10, decimals);
        return Math.round(value * f) / f;
    }
}
