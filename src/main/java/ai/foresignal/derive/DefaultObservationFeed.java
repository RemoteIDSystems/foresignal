package ai.foresignal.derive;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.LiveBatch;
import ai.foresignal.stream.LiveBus;
import ai.foresignal.stream.LiveListener;
import ai.foresignal.stream.Subscription;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The {@link ObservationFeed} over current records (prompt 099): each read is
 * {@code record_head ⋈ record_live}, so a version payload (the {@code ChangeBus}'s content) is
 * already joined with the freshest live state (what the {@code LiveBus} keeps updated).
 *
 * <p>It subscribes to both buses only to learn about gaps: a {@code ChangeBus} gap or a
 * {@code LiveBus} drop records {@link #lastDegradedAt()}, and every generator whose window reaches
 * back past that time marks itself {@code degraded-input} and refuses to assert an absence.
 */
@Component
public class DefaultObservationFeed implements ObservationFeed, ChangeListener, LiveListener, DisposableBean,
        ai.foresignal.ingest.IngestStateReset {

    /** The change-bus subscriber name. */
    public static final String CHANGE_SUBSCRIBER = "derived-observation-feed";

    /** The live-bus subscriber name. */
    public static final String LIVE_SUBSCRIBER = "derived-observation-feed";

    /** Counts observation reads that reached their cap. */
    public static final String TRUNCATED_METRIC = "foresignal.derive.feed.truncated";

    private static final Logger log = LoggerFactory.getLogger(DefaultObservationFeed.class);

    private final DerivedRecords records;
    private final ChangeBus changeBus;
    private final LiveBus liveBus;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final MeterRegistry meters;
    private volatile Instant lastTruncationLog;

    private volatile Subscription changeSubscription;
    private volatile Subscription liveSubscription;
    private volatile Instant lastDegradedAt;

    /** Creates the feed. */
    public DefaultObservationFeed(DerivedRecords records, ChangeBus changeBus, LiveBus liveBus, JdbcTemplate jdbc,
            Clock clock, MeterRegistry meters) {
        this.meters = meters;
        this.records = records;
        this.changeBus = changeBus;
        this.liveBus = liveBus;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** Subscribes to the buses once the application is ready, before the DERIVED runtime. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.OBSERVATION_FEED)
    public synchronized void start() {
        if (changeSubscription == null) {
            changeSubscription = changeBus.subscribe(CHANGE_SUBSCRIBER, this, 512);
        }
        if (liveSubscription == null) {
            liveSubscription = liveBus.subscribe(LIVE_SUBSCRIBER, this, 512);
        }
    }

    @Override
    public synchronized void destroy() {
        if (changeSubscription != null) {
            changeSubscription.close();
            changeSubscription = null;
        }
        if (liveSubscription != null) {
            liveSubscription.close();
            liveSubscription = null;
        }
    }

    @Override
    public void onBatch(ChangeBatch batch) {
        // No per-change work: current records already carry the committed state.
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        lastDegradedAt = clock.instant();
    }

    @Override
    public void onBatch(LiveBatch batch) {
        // No per-live work: record_live already carries the freshest state.
    }

    @Override
    public void onLiveDropped() {
        lastDegradedAt = clock.instant();
    }

    @Override
    public List<Observation> recent(Collection<String> sources, Collection<String> kinds, Instant since, int limit) {
        return recent(sources, kinds, since, null, limit);
    }

    @Override
    public List<Observation> recent(Collection<String> sources, Collection<String> kinds, Instant since, Envelope bbox,
            int limit) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        int cap = Math.max(1, limit);
        List<HeadSnapshot> heads = records.currentSince(sources, kinds, bbox, since, cap);
        if (heads.size() >= cap) {
            // The cap was reached: older entities in the window were not read. Count it, so an operator
            // sees the truncation instead of a silently shorter window.
            meters.counter(TRUNCATED_METRIC).increment();
            Instant now = clock.instant();
            Instant last = lastTruncationLog;
            if (last == null || now.isAfter(last.plusSeconds(300))) {
                lastTruncationLog = now;
                log.warn("Observation feed read for {} hit its cap of {} rows; the stalest entities were dropped",
                        sorted(sources), cap);
            }
        }
        List<Observation> observations = new ArrayList<>(heads.size());
        for (HeadSnapshot head : heads) {
            if (head.tombstone() || head.observedAt() == null || (since != null && head.observedAt().isBefore(since))) {
                continue;
            }
            observations.add(of(head));
        }
        return observations;
    }

    @Override
    public Optional<Observation> latest(String sourceId, String externalId) {
        return records.get(sourceId, externalId)
                .filter(head -> !head.tombstone())
                .map(DefaultObservationFeed::of);
    }

    @Override
    public Instant lastDataAt(String sourceId) {
        return jdbc.queryForObject(
                "SELECT max(l.observed_at) FROM record_head h JOIN record_live l USING (source_id, external_id)"
                        + " WHERE h.source_id = ? AND NOT h.tombstone",
                (rs, n) -> {
                    var ts = rs.getTimestamp(1);
                    return ts == null ? null : ts.toInstant();
                },
                sourceId);
    }

    /** Forgets the last gap or drop once the history store was reset (the test database cleaner). */
    @Override
    public void resetState() {
        lastDegradedAt = null;
    }

    @Override
    public boolean degradedSince(Instant since) {
        Instant at = lastDegradedAt;
        return at != null && since != null && !at.isBefore(since);
    }

    @Override
    public Instant lastDegradedAt() {
        return lastDegradedAt;
    }

    /** Turns a head into an observation. */
    static Observation of(HeadSnapshot head) {
        return new Observation(head.sourceId(), head.externalId(), head.kind(), head.observedAt(), head.geometry(),
                head.payload(), positionSource(head.kind(), head.payload()));
    }

    /**
     * How the position was obtained, from the payload's {@code posSource} field (readsb/OpenSky) or,
     * for a vessel, always {@code reported}.
     */
    public static PositionSource positionSource(String kind, java.util.Map<String, Object> payload) {
        Object raw = payload == null ? null : payload.get("posSource");
        if (raw != null) {
            return switch (String.valueOf(raw).toLowerCase(java.util.Locale.ROOT)) {
                case "adsb", "adsr", "adsc", "flarm" -> PositionSource.REPORTED;
                case "mlat", "tisb" -> PositionSource.CALCULATED;
                default -> PositionSource.UNKNOWN;
            };
        }
        return "vessel".equals(kind) ? PositionSource.REPORTED : PositionSource.UNKNOWN;
    }

    /** The source ids of the given set, as a sorted list (for stable input declaration). */
    static List<String> sorted(Collection<String> sources) {
        return new ArrayList<>(new java.util.TreeSet<>(sources == null ? Set.of() : sources));
    }
}
