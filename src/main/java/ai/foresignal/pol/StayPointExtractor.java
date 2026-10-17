package ai.foresignal.pol;

import ai.foresignal.kinematics.GeoMath;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stay-point extraction (prompt 114), before clustering. Pure and incremental.
 *
 * <p>A candidate starts at an anchor fix; following fixes join it while they are within {@code stayRadiusM} of the
 * anchor and no gap between consecutive fixes exceeds {@code maxGap}. When a fix breaks the candidate (too far, or
 * after a gap), the candidate becomes a stay if it spans at least {@code minDwell}, and the breaking fix anchors
 * the next candidate. A stay is {@code {centroid (mean of its fixes), arrive, depart, fixes}}. Jitter within the
 * radius stays in one candidate; a drive-through never spans the dwell, so it makes no stay.
 *
 * <p>The open candidate is carried between passes ({@link Candidate#toMap}/{@link Candidate#fromMap}), so a stay
 * that spans two passes is found once. {@link #flush} closes it when no fix arrived for longer than {@code maxGap}.
 * Fixes not later than the candidate's last fix are ignored (out of order or duplicates).
 */
public final class StayPointExtractor {

    private StayPointExtractor() {}

    /**
     * One position of the subject.
     *
     * @param at its observed time
     * @param lon the longitude
     * @param lat the latitude
     * @param recordRef the record {@code {sourceId}/{externalId}} it came from
     * @param seq its persisted seq (0 when unknown)
     */
    public record Fix(Instant at, double lon, double lat, String recordRef, long seq) {}

    /**
     * A stay.
     *
     * @param arrive the first fix
     * @param depart the last fix
     * @param lon the centroid longitude
     * @param lat the centroid latitude
     * @param fixes the number of fixes
     * @param recordRef the record of the anchor fix
     */
    public record Stay(Instant arrive, Instant depart, double lon, double lat, int fixes, String recordRef) {

        /** The dwell. */
        public Duration dwell() {
            return Duration.between(arrive, depart);
        }
    }

    /** The open candidate: an anchor and the running sums of its fixes. */
    public static final class Candidate {

        double anchorLon;
        double anchorLat;
        Instant anchorAt;
        double sumLon;
        double sumLat;
        int count;
        Instant lastAt;
        String recordRef;

        static Candidate start(Fix fix) {
            Candidate c = new Candidate();
            c.anchorLon = fix.lon();
            c.anchorLat = fix.lat();
            c.anchorAt = fix.at();
            c.sumLon = fix.lon();
            c.sumLat = fix.lat();
            c.count = 1;
            c.lastAt = fix.at();
            c.recordRef = fix.recordRef();
            return c;
        }

        void add(Fix fix) {
            sumLon += fix.lon();
            sumLat += fix.lat();
            count++;
            lastAt = fix.at();
        }

        /** True when the candidate spans at least {@code minDwell}. */
        public boolean qualifies(Duration minDwell) {
            return Duration.between(anchorAt, lastAt).compareTo(minDwell) >= 0;
        }

        /** The candidate as a stay (meaningful once it qualifies). */
        public Stay toStay() {
            return new Stay(anchorAt, lastAt, sumLon / count, sumLat / count, count, recordRef);
        }

        /** The anchor longitude. */
        public double anchorLon() {
            return anchorLon;
        }

        /** The anchor latitude. */
        public double anchorLat() {
            return anchorLat;
        }

        /** The last fix's time. */
        public Instant lastAt() {
            return lastAt;
        }

        /** The stored form. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("anchorLon", anchorLon);
            out.put("anchorLat", anchorLat);
            out.put("anchorAt", anchorAt.toString());
            out.put("sumLon", sumLon);
            out.put("sumLat", sumLat);
            out.put("count", count);
            out.put("lastAt", lastAt.toString());
            out.put("recordRef", recordRef);
            return out;
        }

        /** The candidate of a stored form, or null when there is none or it is unreadable. */
        public static Candidate fromMap(Object value) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            try {
                Candidate c = new Candidate();
                c.anchorLon = ((Number) map.get("anchorLon")).doubleValue();
                c.anchorLat = ((Number) map.get("anchorLat")).doubleValue();
                c.anchorAt = Instant.parse(String.valueOf(map.get("anchorAt")));
                c.sumLon = ((Number) map.get("sumLon")).doubleValue();
                c.sumLat = ((Number) map.get("sumLat")).doubleValue();
                c.count = ((Number) map.get("count")).intValue();
                c.lastAt = Instant.parse(String.valueOf(map.get("lastAt")));
                c.recordRef = map.get("recordRef") == null ? null : String.valueOf(map.get("recordRef"));
                return c.count > 0 ? c : null;
            } catch (RuntimeException e) {
                return null;
            }
        }
    }

    /**
     * The result of a pass.
     *
     * @param stays the stays closed in this pass, in arrive order
     * @param open the open candidate, or null
     */
    public record Result(List<Stay> stays, Candidate open) {}

    /**
     * Extracts the stays of {@code fixes} (in time order) after the open candidate {@code open}.
     *
     * @param open the candidate carried from the previous pass, or null
     * @param fixes the new fixes in observed-time order
     * @param params the parameters
     */
    public static Result extract(Candidate open, List<Fix> fixes, PolParams params) {
        List<Stay> stays = new ArrayList<>();
        Candidate c = open;
        for (Fix fix : fixes) {
            if (c == null) {
                c = Candidate.start(fix);
                continue;
            }
            if (!fix.at().isAfter(c.lastAt)) {
                continue;
            }
            Duration gap = Duration.between(c.lastAt, fix.at());
            double distance = GeoMath.distanceMeters(c.anchorLon, c.anchorLat, fix.lon(), fix.lat());
            if (gap.compareTo(params.maxGap()) > 0 || distance > params.stayRadiusM()) {
                if (c.qualifies(params.minDwell())) {
                    stays.add(c.toStay());
                }
                c = Candidate.start(fix);
            } else {
                c.add(fix);
            }
        }
        return new Result(stays, c);
    }

    /**
     * Closes the open candidate when no fix arrived for longer than {@code maxGap} before {@code now}.
     *
     * @return the stays it closed (none or one) and the candidate that stays open (null once closed)
     */
    public static Result flush(Candidate open, Instant now, PolParams params) {
        if (open == null || Duration.between(open.lastAt, now).compareTo(params.maxGap()) <= 0) {
            return new Result(List.of(), open);
        }
        return new Result(open.qualifies(params.minDwell()) ? List.of(open.toStay()) : List.of(), null);
    }
}
