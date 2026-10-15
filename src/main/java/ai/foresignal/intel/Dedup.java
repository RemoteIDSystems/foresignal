package ai.foresignal.intel;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;

/**
 * Same-event dedup (prompt 101). A candidate merges into an open event of the <em>same type</em>
 * when their geometries intersect after buffering by {@code radiusKm} and their times overlap within
 * {@code window}, or when they share a member. Never across types.
 */
public final class Dedup {

    private Dedup() {}

    /** True when the two member-key sets share any member. */
    public static boolean sharesMember(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        for (String key : a) {
            if (b.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /** True when the buffered geometries intersect and the intervals overlap within {@code window}. */
    public static boolean intersectsAndOverlaps(Geometry a, Instant aStart, Instant aEnd, Geometry b, Instant bStart,
            Instant bEnd, double radiusKm, Duration window) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return false;
        }
        double degrees = Math.max(0, radiusKm) / 111.0;
        Geometry buffered = degrees == 0 ? a : a.buffer(degrees);
        if (!buffered.intersects(b)) {
            return false;
        }
        Instant aFrom = aStart;
        Instant aTo = aEnd == null ? Instant.MAX : aEnd;
        Instant bFrom = bStart;
        Instant bTo = bEnd == null ? Instant.MAX : bEnd;
        Instant windowStart = bFrom.minus(window);
        Instant windowEnd = bTo.plus(window);
        return !aTo.isBefore(windowStart) && !aFrom.isAfter(windowEnd);
    }

    /** True when a candidate of {@code type} merges into an open event. */
    public static boolean merges(String candidateType, Geometry candidateGeom, Instant candidateStart,
            Instant candidateEnd, Set<String> candidateMembers, IntelligenceEventEntity open, Set<String> openMembers,
            double radiusKm, Duration window) {
        if (!open.getType().equals(candidateType)) {
            return false;
        }
        if (sharesMember(candidateMembers, openMembers)) {
            return true;
        }
        return intersectsAndOverlaps(candidateGeom, candidateStart, candidateEnd, open.getGeom(), open.getStartTime(),
                open.getEndTime(), radiusKm, window);
    }
}
