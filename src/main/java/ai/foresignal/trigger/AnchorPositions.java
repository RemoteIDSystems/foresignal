package ai.foresignal.trigger;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.stream.Bbox;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveBatch;
import ai.foresignal.stream.LiveBus;
import ai.foresignal.stream.LiveListener;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Subscription;
import ai.foresignal.stream.Touch;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The current position of every moving anchor an enabled rule names (prompt 036, task 5): the
 * snapshot that the target-side SQL, the reverse evaluation and the persist gate all read, so the
 * three use the same anchor position.
 *
 * <p>Fed from the change lane (the trigger engine hands it each batch before evaluating) and the
 * live lane (its own {@link LiveBus} subscription; touches only refresh the time). A newly watched
 * anchor is loaded once from {@code record_head ⋈ record_live}. Moved anchors are marked dirty so
 * the {@link AnchorEvaluator} re-evaluates their rules.
 *
 * <p>Anchor coordinates never leave this class except as bound SQL parameters and distances: they are
 * never rendered into templates, notifications, embeddings or LLM prompts.
 */
@Component
public class AnchorPositions implements LiveListener, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(AnchorPositions.class);

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    static final String LOAD = """
            SELECT h.seq, l.observed_at, l.valid_until, h.tombstone,
                   ST_X(ST_PointOnSurface(l.geom)) AS lon, ST_Y(ST_PointOnSurface(l.geom)) AS lat
              FROM record_head h JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id
             WHERE h.source_id = ? AND h.external_id = ?""";

    /**
     * One anchor's latest known state.
     *
     * @param point its position, or null when it has no geometry
     * @param observedAt when it was last observed
     * @param validUntil its expiry, or null for never
     * @param seq its latest persisted seq
     * @param tombstone true when upstream said it is gone
     */
    public record Fix(Point point, Instant observedAt, Instant validUntil, long seq, boolean tombstone) {

        /**
         * True when the anchor may drive a rule at {@code now}: it has a position, is not tombstoned or
         * expired, and was observed within {@code maxAge}.
         */
        public boolean fresh(NearAnchor anchor, Instant now) {
            return point != null && !tombstone
                    && (validUntil == null || validUntil.isAfter(now))
                    && observedAt != null && !observedAt.isBefore(now.minus(anchor.maxAnchorAge()));
        }
    }

    private final JdbcTemplate jdbc;
    private final LiveBus liveBus;
    private final Map<String, Fix> fixes = new ConcurrentHashMap<>();
    private final Set<String> dirty = ConcurrentHashMap.newKeySet();
    private volatile Set<String> watched = Set.of();
    private volatile Subscription subscription;

    /** Creates the snapshot; it subscribes to the live bus on the first {@link #watch}. */
    public AnchorPositions(JdbcTemplate jdbc, LiveBus liveBus) {
        this.jdbc = jdbc;
        this.liveBus = liveBus;
    }

    /**
     * Watches exactly {@code anchors} (the anchors of the enabled rules), loading the ones not seen
     * before from the current state.
     */
    public synchronized void watch(Collection<NearAnchor> anchors) {
        Set<String> keys = new HashSet<>();
        for (NearAnchor anchor : anchors) {
            keys.add(anchor.key());
            if (!fixes.containsKey(anchor.key())) {
                load(anchor.source(), anchor.externalId()).ifPresent(fix -> fixes.put(anchor.key(), fix));
            }
        }
        fixes.keySet().retainAll(keys);
        watched = Set.copyOf(keys);
        if (!keys.isEmpty() && subscription == null) {
            subscription = liveBus.subscribe("anchor-positions", this, 1_024);
        }
    }

    /** Unsubscribes from the live bus. */
    @PreDestroy
    public synchronized void stop() {
        Subscription current = subscription;
        subscription = null;
        if (current != null) {
            current.close();
        }
    }

    /** True when {@code (source, externalId)} anchors an enabled rule. */
    public boolean watches(String source, String externalId) {
        return watched.contains(NearAnchor.key(source, externalId));
    }

    /** The latest state of an anchor, if known. */
    public Optional<Fix> fix(String source, String externalId) {
        return Optional.ofNullable(fixes.get(NearAnchor.key(source, externalId)));
    }

    /** The anchor's position when it is fresh at {@code now}, else null (the rule is then suspended). */
    public Point current(NearAnchor anchor, Instant now) {
        Fix fix = fixes.get(anchor.key());
        return fix != null && fix.fresh(anchor, now) ? fix.point() : null;
    }

    /** Applies a committed change batch; returns the anchor keys it moved or changed. */
    public Set<String> apply(ChangeBatch batch) {
        Set<String> changed = new HashSet<>();
        Set<String> keys = watched;
        if (keys.isEmpty()) {
            return changed;
        }
        for (ChangeSummary change : batch.changes()) {
            String key = NearAnchor.key(change.sourceId(), change.externalId());
            if (!keys.contains(key)) {
                continue;
            }
            Fix previous = fixes.get(key);
            Point point = change.tombstone() ? (previous == null ? null : previous.point()) : center(change.bbox());
            fixes.put(key, new Fix(point, change.observedAt(), change.validUntil(), change.seq(), change.tombstone()));
            changed.add(key);
            dirty.add(key);
        }
        return changed;
    }

    @Override
    public void onBatch(LiveBatch batch) {
        Set<String> keys = watched;
        if (keys.isEmpty()) {
            return;
        }
        for (LiveUpdate update : batch.updates()) {
            String key = NearAnchor.key(update.sourceId(), update.externalId());
            if (keys.contains(key)) {
                Fix previous = fixes.get(key);
                Point point = update.bbox() == null ? (previous == null ? null : previous.point()) : center(update.bbox());
                fixes.put(key, new Fix(point, update.observedAt(), update.validUntil(),
                        previous == null ? 0 : previous.seq(), false));
                dirty.add(key);
            }
        }
        for (Touch touch : batch.touches()) {
            String key = NearAnchor.key(touch.sourceId(), touch.externalId());
            Fix previous = fixes.get(key);
            if (keys.contains(key) && previous != null) {
                fixes.put(key, new Fix(previous.point(), touch.observedAt(), touch.validUntil(), previous.seq(),
                        previous.tombstone()));
                dirty.add(key);
            }
        }
    }

    @Override
    public void onLiveDropped() {
        // Conflated state: reload every watched anchor from the database.
        for (String key : watched) {
            int split = key.indexOf('\u0000');
            load(key.substring(0, split), key.substring(split + 1)).ifPresent(fix -> fixes.put(key, fix));
            dirty.add(key);
        }
    }

    /** The anchor keys that changed since the last call, cleared. */
    public Set<String> drainDirty() {
        Set<String> out = new HashSet<>(dirty);
        dirty.removeAll(out);
        return out;
    }

    /** Forgets every fix (the test cleaner truncated the tables). */
    @Override
    public void resetState() {
        fixes.clear();
        dirty.clear();
        watched = Set.of();
    }

    /** Records a fix directly (tests). */
    void put(String source, String externalId, Fix fix) {
        fixes.put(NearAnchor.key(source, externalId), fix);
    }

    private Optional<Fix> load(String source, String externalId) {
        try {
            List<Fix> rows = jdbc.query(LOAD, (rs, i) -> {
                double lon = rs.getDouble("lon");
                boolean noGeom = rs.wasNull();
                double lat = rs.getDouble("lat");
                OffsetDateTime observed = rs.getObject("observed_at", OffsetDateTime.class);
                OffsetDateTime until = rs.getObject("valid_until", OffsetDateTime.class);
                return new Fix(noGeom ? null : point(lon, lat), observed == null ? null : observed.toInstant(),
                        until == null ? null : until.toInstant(), rs.getLong("seq"), rs.getBoolean("tombstone"));
            }, source, externalId);
            return rows.stream().findFirst();
        } catch (RuntimeException e) {
            log.warn("Could not load anchor {}:{}: {}", source, externalId, e.getMessage());
            return Optional.empty();
        }
    }

    /** The center of {@code box} (exact for a point), or null. */
    static Point center(Bbox box) {
        if (box == null) {
            return null;
        }
        return point((box.minX() + box.maxX()) / 2, (box.minY() + box.maxY()) / 2);
    }

    /** A WGS84 point. */
    static Point point(double lon, double lat) {
        Point p = WGS84.createPoint(new Coordinate(lon, lat));
        p.setSRID(4326);
        return p;
    }
}
