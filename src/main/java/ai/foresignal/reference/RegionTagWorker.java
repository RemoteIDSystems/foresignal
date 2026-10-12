package ai.foresignal.reference;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.search.EmbeddingWorker;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.PgArrays;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveBatch;
import ai.foresignal.stream.LiveBus;
import ai.foresignal.stream.LiveListener;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Subscription;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Keeps {@code record_region} in step with the record store (prompt 040).
 *
 * <ul>
 *   <li><b>Persisted versions</b> arrive through a {@link ChangeBus} subscription with the durable cursor
 *       {@code app_setting['region.cursor']}, which joins retention's consumer clamp
 *       ({@link RegionCursorConsumer}). Gaps and anything the bus did not deliver are caught up from
 *       {@code record_version} on {@code onGap} and every {@linkplain #tick tick}.
 *   <li><b>Moving entities</b> arrive through a {@link LiveBus} subscription (live updates carry no
 *       version).
 *   <li>An entity is tagged again only when it moved more than the retag distance (2 km; 10 km for
 *       TRACK sources) from where it was tagged. A tombstone deletes its row.
 *   <li>Only sources in {@code eager} mode are tagged here; {@code lazy} sources are tagged on demand by
 *       {@link RegionTagger#tagRecord}, and {@code off} sources never. Tombstones delete rows whatever
 *       the mode.
 *   <li>After reference data changes, and on the first start, every current record of an eager source is
 *       tagged again in the background ({@link #retagAll()}).
 *   <li>When a record's country or first-level region changes, its search document is re-embedded so the
 *       embedding text's region suffix stays right (prompt 027); other retags never re-embed.
 * </ul>
 *
 * <p>The worker writes only {@code record_region} and its cursor, never a record table, so tagging never
 * creates a version or changes a hash (Core-Data-Model §1 I1, §4).
 */
@Component
public class RegionTagWorker implements ChangeListener, LiveListener, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(RegionTagWorker.class);

    /** The worker's change-bus queue. */
    public static final int QUEUE = 4_096;

    /** The worker's live-bus queue. */
    public static final int LIVE_QUEUE = 1_024;

    /** Seqs per catch-up chunk. */
    public static final long CHUNK = 2_000;

    /** Heads per page of {@link #retagAll()}. */
    static final int PAGE = 1_000;

    static final String COLUMNS = "v.seq, v.source_id, v.external_id, v.tombstone, ST_AsBinary(v.geom) AS geom";

    private record Row(long seq, String sourceId, String externalId, boolean tombstone, Geometry geometry) {}

    private final ChangeBus changeBus;
    private final LiveBus liveBus;
    private final RegionTagger tagger;
    private final RegionTagStore store;
    private final RegionTaggingService modes;
    private final SourceRegistry registry;
    private final AppSettingService settings;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final ObjectProvider<EmbeddingWorker> embeddings;
    private final AtomicBoolean retagging = new AtomicBoolean();

    private volatile Subscription changes;
    private volatile Subscription live;
    private long cursor = -1;
    private volatile String lastError;

    /** Creates the worker; nothing runs until {@link #start()}. */
    public RegionTagWorker(ChangeBus changeBus, LiveBus liveBus, RegionTagger tagger, RegionTagStore store,
            RegionTaggingService modes, SourceRegistry registry, AppSettingService settings, JdbcTemplate jdbc,
            Clock clock, ObjectProvider<EmbeddingWorker> embeddings) {
        this.changeBus = changeBus;
        this.liveBus = liveBus;
        this.tagger = tagger;
        this.store = store;
        this.modes = modes;
        this.registry = registry;
        this.settings = settings;
        this.jdbc = jdbc;
        this.clock = clock;
        this.embeddings = embeddings;
    }

    // ------------------------------------------------------------------------- lifecycle

    /** Loads the cursor, subscribes to both buses and catches up. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.REGION_TAGGING)
    public void start() {
        boolean fresh;
        synchronized (this) {
            if (changes != null) {
                return;
            }
            fresh = !storedCursor().isPresent();
            cursor = loadCursor();
            changes = changeBus.subscribe("region-tagger", this, QUEUE);
            live = liveBus.subscribe("region-tagger", this, LIVE_QUEUE);
            log.info("Region tag worker started at cursor {}", cursor);
        }
        catchUp();
        if (fresh) {
            retagAllAsync();
        }
    }

    /** Unsubscribes; the cursor stays where the last processed range left it. */
    @PreDestroy
    public synchronized void stop() {
        if (changes != null) {
            changes.close();
            changes = null;
        }
        if (live != null) {
            live.close();
            live = null;
        }
    }

    /** True while subscribed. */
    public boolean running() {
        return changes != null;
    }

    /** The last seq fully processed. */
    public synchronized long cursor() {
        return cursor;
    }

    /** True while a background {@link #retagAll()} runs. */
    public boolean retagging() {
        return retagging.get();
    }

    /** The last failure, or null. */
    public String lastError() {
        return lastError;
    }

    /** Re-reads the cursor (the test cleaner calls it after truncating). */
    @Override
    public synchronized void resetState() {
        lastError = null;
        if (changes != null) {
            cursor = loadCursor();
        }
    }

    // ------------------------------------------------------------------------- change bus

    @Override
    public synchronized void onBatch(ChangeBatch batch) {
        if (changes == null || cursor < 0 || batch.toSeq() <= cursor) {
            return;
        }
        if (batch.fromSeq() - 1 > cursor) {
            processRange(cursor, batch.toSeq());
            return;
        }
        try {
            // The latest change per entity decides: a version followed by a tombstone deletes the row.
            Map<String, ChangeSummary> latest = new LinkedHashMap<>();
            for (ChangeSummary change : batch.changes()) {
                if (change.seq() > cursor) {
                    latest.put(RegionTagStore.key(change.sourceId(), change.externalId()), change);
                }
            }
            List<Long> seqs = new ArrayList<>();
            for (ChangeSummary change : latest.values()) {
                if (change.tombstone()) {
                    store.delete(change.sourceId(), change.externalId());
                } else if (change.persistReason() != PersistReason.HEARTBEAT
                        && modes.mode(change.sourceId()) == RegionTagMode.EAGER) {
                    seqs.add(change.seq());
                }
            }
            if (!seqs.isEmpty()) {
                apply(jdbc.query("SELECT " + COLUMNS + " FROM record_version v WHERE v.seq = ANY(CAST(? AS bigint[]))"
                        + " ORDER BY v.seq", (rs, i) -> row(rs), PgArrays.bigint(seqs)), false);
            }
            advance(batch.toSeq());
        } catch (DataAccessException e) {
            failed(e);
        }
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        catchUp();
    }

    /** Picks up anything the bus did not deliver. */
    @Scheduled(fixedDelay = 5_000, initialDelay = 5_000)
    public void tick() {
        if (running()) {
            catchUp();
        }
    }

    /** Processes everything between the cursor and the bus watermark. */
    public synchronized void catchUp() {
        if (changes == null) {
            return;
        }
        long head = changeBus.lastPublishedSeq();
        while (cursor < head) {
            // Skip empty stretches of seq (a jumped allocator, retention) in one step rather than chunk by chunk.
            Long next;
            try {
                next = jdbc.queryForObject("SELECT min(seq) FROM record_version WHERE seq > ? AND seq <= ?",
                        Long.class, cursor, head);
            } catch (DataAccessException e) {
                failed(e);
                return;
            }
            if (next == null) {
                advance(head);
                return;
            }
            long from = Math.max(cursor, next - 1);
            long to = Math.min(head, from + CHUNK);
            if (!processRange(from, to)) {
                return;
            }
        }
    }

    private boolean processRange(long from, long to) {
        try {
            List<Row> rows = jdbc.query("SELECT " + COLUMNS + " FROM record_version v WHERE v.seq > ? AND v.seq <= ?"
                    + " AND v.persist_reason <> ? ORDER BY v.seq", (rs, i) -> row(rs), from, to,
                    PersistReason.HEARTBEAT.code());
            List<Row> eager = new ArrayList<>();
            for (Row row : latestPerEntity(rows)) {
                if (row.tombstone()) {
                    store.delete(row.sourceId(), row.externalId());
                } else if (modes.mode(row.sourceId()) == RegionTagMode.EAGER) {
                    eager.add(row);
                }
            }
            apply(eager, false);
            advance(to);
            return true;
        } catch (DataAccessException e) {
            failed(e);
            return false;
        }
    }

    // ------------------------------------------------------------------------- live bus

    @Override
    public void onBatch(LiveBatch batch) {
        if (live == null || batch.updates().isEmpty() || modes.mode(batch.sourceId()) != RegionTagMode.EAGER) {
            return;
        }
        try {
            List<Row> rows = new ArrayList<>();
            for (LiveUpdate update : batch.updates()) {
                if (update.geometry() != null) {
                    rows.add(new Row(0, update.sourceId(), update.externalId(), false, update.geometry()));
                }
            }
            synchronized (this) {
                apply(rows, false);
            }
        } catch (DataAccessException e) {
            failed(e);
        }
    }

    @Override
    public void onLiveDropped() {
        // Live state is conflated: the next update of a mover tags it, and versions are caught up anyway.
    }

    // ------------------------------------------------------------------------- tagging

    /**
     * Tags {@code rows} (the latest per entity), skipping entities within the retag distance of where they
     * were tagged unless {@code force}.
     */
    private void apply(List<Row> rows, boolean force) {
        if (rows.isEmpty()) {
            return;
        }
        List<Map.Entry<String, String>> keys = new ArrayList<>(rows.size());
        for (Row row : rows) {
            keys.add(new AbstractMap.SimpleEntry<>(row.sourceId(), row.externalId()));
        }
        Map<String, RegionTagStore.Stored> stored = store.findAll(keys);
        for (Row row : rows) {
            Point point = RegionTagger.representative(row.geometry());
            if (point == null) {
                continue;
            }
            RegionTagStore.Stored before = stored.get(RegionTagStore.key(row.sourceId(), row.externalId()));
            if (!force && before != null && RegionTagger.distanceKm(before.lon(), before.lat(), point.getX(),
                    point.getY()) <= tagger.retagKm(row.sourceId())) {
                continue;
            }
            RegionTags tags = tagger.tag(point.getX(), point.getY());
            store.upsert(row.sourceId(), row.externalId(), tags, point.getX(), point.getY(), clock.instant());
            if (before != null && (!Objects.equals(before.tags().iso2(), tags.iso2())
                    || !Objects.equals(before.tags().admin1(), tags.admin1()))) {
                reembed(row.sourceId(), row.externalId());
            } else if (before == null && !tags.isEmpty()) {
                reembed(row.sourceId(), row.externalId());
            }
        }
        lastError = null;
    }

    private void reembed(String sourceId, String externalId) {
        EmbeddingWorker worker = embeddings.getIfAvailable();
        if (worker != null && worker.running()) {
            try {
                worker.reembed(sourceId, externalId);
            } catch (RuntimeException e) {
                log.debug("Could not re-embed {}:{} after a region change: {}", sourceId, externalId, e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------- retag all

    /** Drops nothing and tags every current record of an eager source again, in the background. */
    @EventListener
    public void onReferenceChanged(ReferenceDataChangedEvent event) {
        retagAllAsync();
    }

    private void retagAllAsync() {
        if (retagging.compareAndSet(false, true)) {
            Thread.ofVirtual().name("region-retag").start(() -> {
                try {
                    retagAll();
                } finally {
                    retagging.set(false);
                }
            });
        }
    }

    /**
     * Tags every current, non-tombstoned record of an eager source again (after a reference load, and on
     * the first start); the number of records tagged.
     */
    public long retagAll() {
        Set<String> eager = new TreeSet<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (modes.mode(descriptor) == RegionTagMode.EAGER) {
                eager.add(descriptor.id());
            }
        }
        if (eager.isEmpty()) {
            return 0;
        }
        long total = 0;
        String afterSource = "";
        String afterId = "";
        try {
            while (true) {
                List<Row> page = jdbc.query("SELECT 0 AS seq, h.source_id, h.external_id, false AS tombstone,"
                                + " ST_AsBinary(l.geom) AS geom FROM record_head h JOIN record_live l"
                                + " ON l.source_id = h.source_id AND l.external_id = h.external_id"
                                + " WHERE NOT h.tombstone AND h.source_id = ANY(CAST(? AS text[]))"
                                + " AND (h.source_id, h.external_id) > (?, ?) ORDER BY h.source_id, h.external_id"
                                + " LIMIT " + PAGE,
                        (rs, i) -> row(rs), PgArrays.text(eager), afterSource, afterId);
                if (page.isEmpty()) {
                    break;
                }
                synchronized (this) {
                    apply(page, true);
                }
                total += page.size();
                Row last = page.getLast();
                afterSource = last.sourceId();
                afterId = last.externalId();
                if (page.size() < PAGE) {
                    break;
                }
            }
        } catch (DataAccessException e) {
            failed(e);
        }
        if (total > 0) {
            log.info("Region tags refreshed for {} current records", total);
        }
        return total;
    }

    // ------------------------------------------------------------------------- cursor

    private void advance(long to) {
        cursor = to;
        settings.put(SettingKeys.REGION_CURSOR, to);
    }

    private java.util.Optional<Long> storedCursor() {
        List<String> values = jdbc.queryForList("SELECT value::text FROM app_setting WHERE key = ?", String.class,
                SettingKeys.REGION_CURSOR);
        if (values.isEmpty() || values.getFirst() == null) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(Long.parseLong(values.getFirst().strip()));
        } catch (NumberFormatException e) {
            log.warn("Ignoring an unreadable region cursor '{}'", values.getFirst());
            return java.util.Optional.empty();
        }
    }

    private long loadCursor() {
        java.util.Optional<Long> stored = storedCursor();
        if (stored.isPresent()) {
            return stored.get();
        }
        // A fresh start follows from the current head; current records are tagged by retagAll.
        long head = changeBus.lastPublishedSeq();
        settings.put(SettingKeys.REGION_CURSOR, head);
        return head;
    }

    private void failed(RuntimeException e) {
        lastError = e.getMessage();
        log.warn("Region tag worker paused at cursor {}: {}", cursor, e.getMessage());
    }

    // ------------------------------------------------------------------------- rows

    private static Row row(java.sql.ResultSet rs) throws java.sql.SQLException {
        byte[] wkb = rs.getBytes("geom");
        Geometry geometry = null;
        if (wkb != null) {
            try {
                geometry = new WKBReader().read(wkb);
            } catch (ParseException e) {
                geometry = null;
            }
        }
        return new Row(rs.getLong("seq"), rs.getString("source_id"), rs.getString("external_id"),
                rs.getBoolean("tombstone"), geometry);
    }

    private static List<Row> latestPerEntity(List<Row> rows) {
        Map<String, Row> latest = new LinkedHashMap<>();
        for (Row row : rows) {
            latest.merge(RegionTagStore.key(row.sourceId(), row.externalId()), row,
                    (a, b) -> b.seq() >= a.seq() ? b : a);
        }
        return new ArrayList<>(latest.values());
    }
}
