package ai.foresignal.pol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Point;

/** The API shapes of the pattern-of-life objects (prompt 114: {@code PolSubject}, {@code PolPlace}, {@code PolStay}). */
public final class PolViews {

    static final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private PolViews() {}

    /** The {@code PolSubject} object. */
    public static Map<String, Object> subject(PolSubjectEntity s, long placeCount, long stayCount) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", s.getId());
        out.put("subjectType", s.getSubjectType());
        out.put("ref", s.getRef());
        out.put("enabled", s.isEnabled());
        out.put("params", PolParams.of(s.getParams()).toMap());
        out.put("private", s.isPrivateSubject());
        out.put("createdBy", s.getCreatedBy());
        out.put("createdAt", s.getCreatedAt());
        out.put("lastSeq", s.getLastSeq());
        out.put("lastComputedAt", s.getLastComputedAt());
        out.put("placeCount", placeCount);
        out.put("stayCount", stayCount);
        return out;
    }

    /** The {@code PolPlace} object. */
    public static Map<String, Object> place(PolPlaceEntity p, boolean privateSubject) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("subjectId", p.getSubjectId());
        out.put("clusterId", p.getClusterId());
        out.put("centroid", point(p.getCentroid()));
        out.put("radiusM", p.getRadiusM());
        out.put("visits", p.getVisits());
        out.put("totalDwellMinutes", p.getTotalDwellMinutes());
        List<Integer> histogram = new ArrayList<>();
        int[] h = p.getHourOfWeek() == null ? new int[PolStats.BUCKETS] : p.getHourOfWeek();
        for (int v : h) {
            histogram.add(v);
        }
        out.put("hourOfWeek", histogram);
        out.put("firstSeen", p.getFirstSeen());
        out.put("lastSeen", p.getLastSeen());
        out.put("label", p.getLabel());
        out.put("private", privateSubject);
        return out;
    }

    /** The {@code PolStay} object. */
    public static Map<String, Object> stay(PolStayEntity s) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", s.getId());
        out.put("subjectId", s.getSubjectId());
        out.put("arrive", s.getArrive());
        out.put("depart", s.getDepart());
        out.put("centroid", point(s.getCentroid()));
        out.put("placeId", s.getPlaceId());
        out.put("clusterId", s.getClusterId());
        out.put("fixes", s.getFixes());
        return out;
    }

    /** A GeoJSON Point. */
    static Map<String, Object> point(Point p) {
        return Map.of("type", "Point", "coordinates", List.of(p.getX(), p.getY()));
    }

    /**
     * The (at most three) hour-of-week buckets with the most dwell, as {@code "Mon 08:00"} (operator time), most
     * dwell first.
     */
    public static List<String> typicalHours(int[] histogram) {
        List<String> out = new ArrayList<>();
        if (histogram == null) {
            return out;
        }
        boolean[] used = new boolean[histogram.length];
        for (int n = 0; n < 3; n++) {
            int best = -1;
            for (int i = 0; i < histogram.length; i++) {
                if (!used[i] && histogram[i] > 0 && (best < 0 || histogram[i] > histogram[best])) {
                    best = i;
                }
            }
            if (best < 0) {
                break;
            }
            used[best] = true;
            out.add(DAYS[best / 24] + " " + String.format(java.util.Locale.ROOT, "%02d:00", best % 24));
        }
        return out;
    }
}
