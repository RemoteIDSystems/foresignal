package ai.foresignal.chronicle;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Projects a chronicle layer's capture volume (prompt 131, {@code ChronicleEstimate}) from the last 24 hours of global
 * history: the versions of the layer's sources and kinds (or its linked records), their average size, how many are
 * event versions and how many distinct entities they cover, then the capture mode's share of them:
 *
 * <ul>
 *   <li>{@code full}: every version;</li>
 *   <li>{@code events-only}: the event versions;</li>
 *   <li>{@code downsampled}: per entity at most one per interval, plus the event versions;</li>
 *   <li>{@code latest-per-interval}: per entity at most one per interval;</li>
 *   <li>{@code rollup-only}: one small row per region and hour;</li>
 *   <li>{@code frames}: every Nth raster frame record.</li>
 * </ul>
 *
 * <p>The area and payload conditions are not applied, so the figure is an upper bound and says so in its basis.
 * Alerts layers project from the last 7 days of their rules' alerts; computed series from one point per hour.
 */
@Component
public class ChronicleEstimator {

    private static final Logger log = LoggerFactory.getLogger(ChronicleEstimator.class);

    /** The window projected from. */
    static final Duration WINDOW = Duration.ofHours(24);

    /** The bytes of one alert snapshot in the thread, roughly. */
    static final long ALERT_BYTES = 2_048;

    private final ChronicleLayerCompiler compiler;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the estimator. */
    public ChronicleEstimator(ChronicleLayerCompiler compiler, JdbcTemplate jdbc, Clock clock) {
        this.compiler = compiler;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * A layer's projection.
     *
     * @param rowsPerDay projected rows per day
     * @param bytesPerDay projected bytes per day
     * @param basis how it was computed
     */
    public record Projection(double rowsPerDay, double bytesPerDay, String basis) {

        /** The stored {@code estimate} shape. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("rowsPerDay", round(rowsPerDay));
            out.put("bytesPerDay", round(bytesPerDay));
            out.put("basis", basis);
            return out;
        }

        private static double round(double v) {
            return Math.round(v * 10.0) / 10.0;
        }
    }

    /**
     * What 24 hours of history held for a layer.
     *
     * @param versions versions
     * @param events event versions (FIRST, SIGNIFICANT, REAPPEAR, TOMBSTONE, TRIGGER_EDGE)
     * @param entities distinct entities
     * @param regions distinct regions
     * @param avgBytes the average captured size of a version
     */
    record Volume(long versions, long events, long entities, long regions, double avgBytes) {}

    /**
     * Projects one layer.
     *
     * @param chronicle the chronicle (its area does not narrow the projection)
     * @param selector the layer's selector
     * @param capture the layer's capture mode
     * @return the projection
     */
    public Projection project(ChronicleEntity chronicle, ChronicleSelector selector, CaptureMode capture) {
        try {
            if (selector instanceof ChronicleSelector.Alerts a) {
                return alerts(a);
            }
            if (selector instanceof ChronicleSelector.Series s && s.refs().stream().noneMatch(r -> r.startsWith(
                    "series:"))) {
                double points = s.refs().size() * 24.0;
                return new Projection(points, points * ChronicleSizes.SERIES_ROW,
                        "computed series sampled hourly: about one point per hour per ref");
            }
            CompiledLayer layer = compiler.compile(chronicle.getId(), "estimate", "estimate", selector, capture, null,
                    null, null);
            Volume v = volume(layer);
            return apply(v, capture, "last 24 h of global history for the layer's sources and kinds; area and"
                    + " conditions not applied (an upper bound)");
        } catch (RuntimeException e) {
            log.debug("Could not project a layer of chronicle {}: {}", chronicle.getId(), e.toString());
            return new Projection(0, 0, "no projection: " + e.getMessage());
        }
    }

    /** The mode's share of a volume. */
    static Projection apply(Volume v, CaptureMode capture, String basis) {
        double perDay = Duration.ofDays(1).toSeconds();
        double rows = switch (capture.mode()) {
            case CaptureMode.FULL -> v.versions();
            case CaptureMode.EVENTS_ONLY -> v.events();
            case CaptureMode.DOWNSAMPLED -> Math.min(v.versions(),
                    v.entities() * (perDay / Math.max(1, capture.interval().toSeconds())) + v.events());
            case CaptureMode.LATEST_PER_INTERVAL -> Math.min(v.versions(),
                    v.entities() * (perDay / Math.max(1, capture.interval().toSeconds())));
            case CaptureMode.ROLLUP_ONLY -> Math.max(1, v.regions()) * 24.0;
            case CaptureMode.FRAMES -> v.versions() / (double) Math.max(1, capture.everyNthFrame());
            default -> v.versions();
        };
        double bytes = CaptureMode.ROLLUP_ONLY.equals(capture.mode()) ? rows * ChronicleSizes.ROLLUP_ROW
                : rows * v.avgBytes();
        return new Projection(rows, bytes, basis);
    }

    private Volume volume(CompiledLayer layer) {
        Instant since = clock.instant().minus(WINDOW);
        StringBuilder sql = new StringBuilder("SELECT count(*), count(*) FILTER (WHERE v.persist_reason IN (1, 2, 3,"
                + " 4, 9)), count(DISTINCT (v.source_id, v.external_id)), count(DISTINCT rr.iso2),"
                + " coalesce(avg(" + ChronicleSizes.ROW_OVERHEAD + " + octet_length(v.payload::text)"
                + " + coalesce(octet_length(ST_AsBinary(v.geom)), 0)), 0) FROM record_version v"
                + " LEFT JOIN record_region rr ON rr.source_id = v.source_id AND rr.external_id = v.external_id"
                + " WHERE v.observed_at >= ?");
        List<Object> args = new ArrayList<>();
        args.add(Timestamp.from(since));
        Set<String> sources = layer.sources();
        if (!sources.isEmpty()) {
            sql.append(" AND v.source_id = ANY(?)");
            args.add(sources.toArray(String[]::new));
        }
        if (!layer.kinds().isEmpty()) {
            sql.append(" AND v.kind = ANY(?)");
            args.add(layer.kinds().toArray(String[]::new));
        }
        return jdbc.queryForObject(sql.toString(), (rs, n) -> new Volume(rs.getLong(1), rs.getLong(2), rs.getLong(3),
                rs.getLong(4), rs.getDouble(5)), args.toArray());
    }

    private Projection alerts(ChronicleSelector.Alerts a) {
        Instant since = clock.instant().minus(Duration.ofDays(7));
        StringBuilder sql = new StringBuilder("SELECT count(*) FROM alert WHERE fired_at >= ?");
        List<Object> args = new ArrayList<>();
        args.add(Timestamp.from(since));
        if (!a.ruleIds().isEmpty()) {
            sql.append(" AND CAST(rule_id AS text) = ANY(?)");
            args.add(a.ruleIds().toArray(String[]::new));
        }
        Long n = jdbc.queryForObject(sql.toString(), Long.class, args.toArray());
        double perDay = (n == null ? 0 : n) / 7.0;
        return new Projection(perDay, perDay * ALERT_BYTES, "alerts of the last 7 days" + (a.tags().isEmpty() ? ""
                : " (tags not applied: an upper bound)"));
    }
}
