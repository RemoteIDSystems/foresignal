package ai.foresignal.stats;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity time series (OpenAPI {@code StatsSeries}, prompt 038).
 *
 * @param from the first bucket start (the request's {@code from}, floored to the bucket)
 * @param to the end (the request's {@code to}, ceiled to the bucket)
 * @param bucket {@code 1h} or {@code 1d}
 * @param measure the measure
 * @param groupBy {@code none}, {@code source} or {@code kind}
 * @param timeField the time axis
 * @param backend {@code rollup}, {@code raw} or {@code mixed}
 * @param series one series per group (one with a null key when ungrouped), every bucket present
 * @param retainedFrom the oldest instant every involved source still retains raw history, or null
 */
public record StatsSeries(Instant from, Instant to, String bucket, String measure, String groupBy, String timeField,
        String backend, List<Series> series, Instant retainedFrom) {

    /** Copies the series. */
    public StatsSeries {
        series = series == null ? List.of() : List.copyOf(series);
    }

    /**
     * One series.
     *
     * @param key the group key, or null when ungrouped
     * @param points one point per bucket, in time order
     */
    public record Series(String key, List<Point> points) {

        /** Copies the points. */
        public Series {
            points = points == null ? List.of() : List.copyOf(points);
        }

        /** The sum over every point. */
        public long total() {
            return points.stream().mapToLong(Point::value).sum();
        }
    }

    /**
     * One bucket's value.
     *
     * @param t the bucket start
     * @param value the measure
     */
    public record Point(Instant t, long value) {}

    /** The series of {@code key}, or null. */
    public Series series(String key) {
        for (Series s : series) {
            if (java.util.Objects.equals(s.key(), key)) {
                return s;
            }
        }
        return null;
    }

    /** The {@code StatsSeries} JSON. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("from", from.toString());
        out.put("to", to.toString());
        out.put("bucket", bucket);
        out.put("measure", measure);
        out.put("groupBy", groupBy);
        out.put("timeField", timeField);
        out.put("backend", backend);
        out.put("series", series.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", s.key());
            m.put("points", s.points().stream().map(p -> {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("t", p.t().toString());
                point.put("value", p.value());
                return point;
            }).toList());
            return m;
        }).toList());
        out.put("retainedFrom", retainedFrom == null ? null : retainedFrom.toString());
        return out;
    }
}
