package ai.foresignal.sink;

import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.provenance.LicenceResolver;
import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.ChangesQuery;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.LiveBatch;
import ai.foresignal.stream.LiveBus;
import ai.foresignal.stream.LiveListener;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Subscription;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * One virtual thread per enabled sink (prompt 110). The thread owns the sink's cursor row and its mirror state;
 * the ChangeBus and LiveBus listeners only queue work to it, so no database write and no socket write happens on
 * a bus delivery thread.
 *
 * <p><strong>CHANGE_FEED</strong> (CloudEvents): reads {@code seq > last_seq} with the sink filter through the
 * 014 changes service in pages of {@value #PAGE}, loads licenses for the page in one query, gates each record
 * through {@link SinkRecordGate}, delivers in the type's batches and persists {@code last_seq} after each
 * acknowledged batch. A record the filter or the gate refuses is <em>skipped and counted</em> and the cursor moves
 * past it, so one denied record never stalls the sink or holds back retention. A delivery the target does not
 * acknowledge leaves the cursor where it is and backs off (honoring {@code Retry-After}). Below the purge horizon,
 * on a brand-new cursor, on a cursor left {@code RESYNCING} by a crash, and on {@code POST /v1/sinks/{id}/resync},
 * the sink replays current state as synthetic {@code SNAPSHOT} events and then tails from the snapshot's watermark.
 *
 * <p><strong>STATE_MIRROR</strong> (MQTT state, CoT, readsb, SBS, AIVDM, APRS-IS): snapshots current state
 * (every page), then tails persisted changes from the snapshot watermark and applies LiveBus updates. Every update
 * is applied only when its {@code observedAt} is not older than the mirrored one (last writer wins,
 * Core-Data-Model §10.2); tombstones remove immediately; a record with {@code validUntil} is removed when it
 * passes ({@code DelayQueue}-style expiry on the run thread); a record that leaves the filter or stops passing the
 * gate is removed. A ChangeBus gap, a LiveBus overflow, a full inbox or {@code POST /v1/sinks/{id}/resync} drops
 * the mirror ({@link SinkInstance#resync()}) and snapshots again.
 *
 * <p><strong>Failure:</strong> the instance is closed and reopened with backoff (5 s doubling to 5 min, reset
 * after progress). After 10 minutes failing, one {@code system.sink_failing} notification is raised per failing
 * episode.
 */
@Component
public class SinkSupervisor {

    private static final Logger log = LoggerFactory.getLogger(SinkSupervisor.class);

    /** The first restart backoff. */
    static final Duration INITIAL_BACKOFF = Duration.ofSeconds(5);

    /** The restart backoff cap. */
    static final Duration MAX_BACKOFF = Duration.ofMinutes(5);

    /** Failing long enough to notify. */
    static final Duration NOTIFY_AFTER = Duration.ofMinutes(10);

    /** The failing time before the notification; {@link #NOTIFY_AFTER} except in tests. */
    private volatile Duration notifyAfter = NOTIFY_AFTER;

    /** The first restart backoff in force; {@link #INITIAL_BACKOFF} except in tests. */
    private volatile Duration initialBackoff = INITIAL_BACKOFF;

    /**
     * Shortens the notification delay and the first backoff (integration tests, which cannot wait ten minutes).
     *
     * @param notifyAfter failing time before the {@code system.sink_failing} notification; null restores
     * @param initialBackoff the first restart backoff; null restores
     */
    void timings(Duration notifyAfter, Duration initialBackoff) {
        this.notifyAfter = notifyAfter == null ? NOTIFY_AFTER : notifyAfter;
        this.initialBackoff = initialBackoff == null ? INITIAL_BACKOFF : initialBackoff;
    }

    /** The change page size. */
    static final int PAGE = 500;

    /** Live updates and wake-ups queued per sink before the sink resyncs instead. */
    static final int INBOX = 50_000;

    /** How long the run thread idles before re-checking the change feed. */
    static final Duration IDLE = Duration.ofSeconds(5);

    /** How often in-memory counters are written to the cursor row. */
    static final Duration FLUSH_EVERY = Duration.ofSeconds(5);

    /** How long {@link #stop} waits for the run thread to finish. */
    static final Duration STOP_WAIT = Duration.ofSeconds(5);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** The inbox token that asks the run thread to read the change feed. */
    private static final Object WAKE = new Object();

    private final SinkRepository sinks;
    private final SinkCursorRepository cursors;
    private final SinkTypeRegistry types;
    private final SinkRecordGate gate;
    private final CredentialCipher cipher;
    private final RecordQueryService queries;
    private final ChangeBus changeBus;
    private final LiveBus liveBus;
    private final LicenceResolver licences;
    private final SourceRegistry registry;
    private final OutboundUrlPolicy policy;
    private final ApplicationEventPublisher events;
    private final MeterRegistry meters;
    private final SinkScope scope;
    private final Map<String, Run> runs = new ConcurrentHashMap<>();

    /** Creates the supervisor. */
    public SinkSupervisor(SinkRepository sinks, SinkCursorRepository cursors, SinkTypeRegistry types,
            SinkRecordGate gate, CredentialCipher cipher, RecordQueryService queries, ChangeBus changeBus,
            LiveBus liveBus, LicenceResolver licences, SourceRegistry registry, OutboundUrlPolicy policy,
            ApplicationEventPublisher events, MeterRegistry meters, SinkScope scope) {
        this.sinks = sinks;
        this.cursors = cursors;
        this.types = types;
        this.gate = gate;
        this.cipher = cipher;
        this.queries = queries;
        this.changeBus = changeBus;
        this.liveBus = liveBus;
        this.licences = licences;
        this.registry = registry;
        this.policy = policy;
        this.events = events;
        this.meters = meters;
        this.scope = scope == null ? SinkScope.NONE : scope;
    }

    /** Starts every enabled sink at startup. */
    @EventListener(ApplicationReadyEvent.class)
    public void startEnabled() {
        for (SinkEntity sink : sinks.findByEnabledTrueOrderByIdAsc()) {
            start(sink.getId());
        }
    }

    /** Starts (or restarts) a sink. */
    public void start(String id) {
        stop(id);
        SinkEntity sink = sinks.findById(id).orElse(null);
        if (sink == null || !sink.isEnabled()) {
            return;
        }
        Run run = new Run(sink);
        runs.put(id, run);
        run.thread.start();
    }

    /** Restarts a sink if enabled, else stops it. */
    public void restart(String id) {
        SinkEntity sink = sinks.findById(id).orElse(null);
        if (sink == null || !sink.isEnabled()) {
            stop(id);
        } else {
            start(id);
        }
    }

    /** Stops a sink and waits (up to {@link #STOP_WAIT}) for its thread to write its final cursor state. */
    public void stop(String id) {
        Run run = runs.remove(id);
        if (run != null) {
            run.shutdown();
        }
    }

    /**
     * Forces a resync (prompt 110, {@code resyncSink}): a STATE_MIRROR drops its mirror and snapshots again; a
     * CHANGE_FEED replays current state as {@code SNAPSHOT} events and continues from the snapshot watermark. A
     * stopped sink resyncs on its next start (its cursor is marked {@code RESYNCING}).
     */
    public void resync(String id) {
        Run run = runs.get(id);
        if (run != null) {
            run.requestResync();
            return;
        }
        cursors.findById(id).ifPresent(cursor -> {
            cursor.setState(SinkCursorEntity.RESYNCING);
            cursors.save(cursor);
        });
    }

    /** Stops every sink at shutdown. */
    @EventListener(ContextClosedEvent.class)
    public void stopAll() {
        for (String id : List.copyOf(runs.keySet())) {
            stop(id);
        }
    }

    /** The number of running sinks. */
    public int running() {
        return runs.size();
    }

    /** The run of a sink, or null. */
    public Run run(String id) {
        return runs.get(id);
    }

    /** The number of failing sinks. */
    public int failing() {
        int failing = 0;
        for (Run run : runs.values()) {
            if (run.failingSince != null) {
                failing++;
            }
        }
        return failing;
    }

    /** The number of connected clients over all SERVE sinks. */
    public int clients() {
        int total = 0;
        for (Run run : runs.values()) {
            SinkInstance instance = run.instance;
            if (instance != null) {
                total += instance.clients();
            }
        }
        return total;
    }

    /** The last change seq the ChangeBus has published (the status endpoint's {@code lastPublishedSeq}). */
    public long lastPublishedSeq() {
        return changeBus.lastPublishedSeq();
    }


    /** A running sink. */
    public final class Run {
        private final SinkEntity sink;
        private final AtomicBoolean closed = new AtomicBoolean();
        private final AtomicBoolean resync = new AtomicBoolean();
        private final Thread thread;
        private final BlockingQueue<Object> inbox = new ArrayBlockingQueue<>(INBOX);
        private final MirrorState mirror = new MirrorState();
        private final LongAdder delivered = new LongAdder();
        private final LongAdder dropped = new LongAdder();
        private final Map<String, LongAdder> deniedByReason = new ConcurrentHashMap<>();
        private final Map<String, LongAdder> deniedBySource = new ConcurrentHashMap<>();
        private volatile SinkInstance instance;
        private volatile SinkMode mode = SinkMode.SERVE;
        private volatile boolean localTarget;
        private volatile SinkFilter filter = SinkFilter.ALL;
        private volatile Subscription changeSub;
        private volatile Subscription liveSub;
        private volatile Instant failingSince;
        private boolean notified;
        private boolean progressed;
        private Instant lastFlush = Instant.EPOCH;
        private SinkCursorEntity cursor;

        Run(SinkEntity sink) {
            this.sink = sink;
            this.thread = Thread.ofVirtual().name("sink-" + sink.getId()).unstarted(this::loop);
        }

        /** The running instance, or null before it opens. */
        public SinkInstance instance() {
            return instance;
        }

        /** When the current failing episode began, or null while healthy. */
        public Instant failingSince() {
            return failingSince;
        }

        void requestResync() {
            resync.set(true);
            inbox.offer(WAKE);
        }

        void shutdown() {
            closed.set(true);
            thread.interrupt();
            closeInstance();
            try {
                thread.join(STOP_WAIT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void loop() {
            Duration backoff = initialBackoff;
            try {
                while (!closed.get()) {
                    progressed = false;
                    try {
                        openAndRun();
                        return;
                    } catch (SinkDeliveryException e) {
                        closeInstance();
                        if (closed.get()) {
                            return;
                        }
                        markFailure(e);
                        if (progressed) {
                            backoff = initialBackoff;
                        }
                        Duration wait = e.retryAfter() != null ? min(max(e.retryAfter(), Duration.ofSeconds(1)),
                                MAX_BACKOFF) : backoff;
                        sleep(wait);
                        backoff = min(backoff.multipliedBy(2), MAX_BACKOFF);
                    } catch (RuntimeException e) {
                        closeInstance();
                        if (closed.get()) {
                            return;
                        }
                        markFailure(e);
                        if (progressed) {
                            backoff = initialBackoff;
                        }
                        sleep(backoff);
                        backoff = min(backoff.multipliedBy(2), MAX_BACKOFF);
                    }
                }
            } finally {
                closeInstance();
                finish();
            }
        }

        /** Writes the final counters and {@code STOPPED}, unless the sink was deleted meanwhile. */
        private void finish() {
            try {
                if (cursor != null && sinks.existsById(sink.getId())) {
                    flushCounters(true);
                    if (!SinkCursorEntity.RESYNCING.equals(cursor.getState())) {
                        cursor.setState(SinkCursorEntity.STOPPED);
                    }
                    save(cursor);
                }
            } catch (RuntimeException e) {
                log.debug("Could not write the final state of sink {}", sink.getId(), e);
            }
        }

        private void closeInstance() {
            Subscription change = changeSub;
            Subscription live = liveSub;
            changeSub = null;
            liveSub = null;
            closeQuietly(change);
            closeQuietly(live);
            SinkInstance current = instance;
            instance = null;
            if (current != null) {
                try {
                    current.close();
                } catch (RuntimeException e) {
                    log.warn("Closing sink {} failed", sink.getId(), e);
                }
            }
        }

        private void openAndRun() {
            SinkType type = types.get(sink.getType());
            mode = resolveMode(type, sink);
            localTarget = localTarget(sink, mode);
            SinkConfig config = config(sink, mode);
            filter = config.filter();
            cursor = cursor(sink.getId());
            boolean wasResyncing = SinkCursorEntity.RESYNCING.equals(cursor.getState());
            instance = type.open(config);
            inbox.clear();
            if (type.spec().kind() == SinkKind.CHANGE_FEED) {
                boolean fresh = cursor.getLastSeq() == 0 && cursor.getDelivered() == 0;
                if (fresh || wasResyncing) {
                    resync.set(true);
                }
                cursor.setState(SinkCursorEntity.RUNNING);
                save(cursor);
                changeSub = changeBus.subscribe("sink:" + sink.getId(), wakeOnChanges(), 1 << 10);
                runChangeFeed();
            } else {
                cursor.setState(SinkCursorEntity.RUNNING);
                save(cursor);
                resync.set(false);
                mirrorSnapshot();
                liveSub = liveBus.subscribe("sink:" + sink.getId(), new LiveListener() {
                    @Override
                    public void onBatch(LiveBatch batch) {
                        for (LiveUpdate update : batch.updates()) {
                            if (!inbox.offer(liveRecord(update))) {
                                dropped.increment();
                                requestResync();
                                return;
                            }
                        }
                    }

                    @Override
                    public void onLiveDropped() {
                        requestResync();
                    }
                }, 1 << 14);
                changeSub = changeBus.subscribe("sink:" + sink.getId(), wakeOnChanges(), 1 << 10);
                runMirror();
            }
        }

        private ChangeListener wakeOnChanges() {
            return new ChangeListener() {
                @Override
                public void onBatch(ChangeBatch batch) {
                    inbox.offer(WAKE);
                }

                @Override
                public void onGap(long lastDeliveredSeq) {
                    // The feed is re-read from the cursor; a mirror also resnapshots (it may have missed live state).
                    if (types.get(sink.getType()).spec().kind() == SinkKind.STATE_MIRROR) {
                        resync.set(true);
                    }
                    inbox.offer(WAKE);
                }
            };
        }

        // ------------------------------------------------------------------------- CHANGE_FEED

        private void runChangeFeed() {
            while (!closed.get()) {
                if (resync.getAndSet(false)) {
                    feedSnapshot();
                }
                drainFeed();
                healthy();
                flushCounters(false);
                awaitWork(IDLE);
            }
        }

        /**
         * Replays current state as synthetic {@code SNAPSHOT} events, every page, then moves the cursor to the
         * snapshot's watermark. The cursor stays {@code RESYNCING} until the whole snapshot is acknowledged, so a
         * crash or a refused delivery replays it from the start on the next attempt.
         */
        private void feedSnapshot() {
            cursor.setState(SinkCursorEntity.RESYNCING);
            cursor.setResyncCount(cursor.getResyncCount() + 1);
            save(cursor);
            long watermark = -1;
            String page = null;
            do {
                Slices.RecordSlice slice = queries.list(currentQuery(filter, page));
                if (watermark < 0) {
                    watermark = slice.watermarkSeq();
                }
                Map<Long, LicenceResolver.Resolution> resolved = licences.resolveAll(slice.rows());
                List<SinkRecord> batch = new ArrayList<>();
                for (RecordRow row : slice.rows()) {
                    SinkRecord record = rowRecord(row, resolved).asSnapshot();
                    if (passes(record)) {
                        batch.add(record);
                    }
                }
                for (int from = 0; from < batch.size() && !closed.get(); from += batchSize()) {
                    List<SinkRecord> chunk = batch.subList(from, Math.min(batch.size(), from + batchSize()));
                    int acked = current().deliverAll(chunk);
                    delivered.add(acked);
                    count(acked);
                    if (acked < chunk.size()) {
                        throw new SinkDeliveryException("the target did not acknowledge the snapshot", null, null,
                                false);
                    }
                    progressed = true;
                }
                page = slice.nextCursor();
            } while (page != null && !closed.get());
            if (closed.get()) {
                return;
            }
            cursor.setLastSeq(Math.max(cursor.getLastSeq(), Math.max(watermark, 0)));
            cursor.setWatermarkSeq(Math.max(watermark, 0));
            cursor.setState(SinkCursorEntity.RUNNING);
            cursor.setLastDeliveredAt(Instant.now());
            flushCounters(true);
        }

        /** Reads and delivers changes after the cursor until the feed is drained. */
        private void drainFeed() {
            while (!closed.get()) {
                long after = cursor.getLastSeq();
                long horizon = queries.purgeHorizon(filter.sources());
                if (after < horizon) {
                    feedSnapshot();
                    continue;
                }
                Slices.ChangeSlice slice = queries.changes(changesQuery(filter, after));
                List<RecordRow> rows = new ArrayList<>();
                for (RecordRow row : slice.rows()) {
                    if (row.seq() > after) {
                        rows.add(row);
                    }
                }
                if (rows.isEmpty()) {
                    if (slice.hasMore() && slice.lastSeq() > after) {
                        cursor.setLastSeq(slice.lastSeq());
                        save(cursor);
                        continue;
                    }
                    return;
                }
                deliverPage(rows);
                if (!slice.hasMore()) {
                    return;
                }
            }
        }

        /**
         * Delivers one page: refused records are skipped (counted) and the cursor moves past them; deliverable
         * records go out in the type's batches and the cursor is persisted after each acknowledged batch.
         */
        private void deliverPage(List<RecordRow> rows) {
            Map<Long, LicenceResolver.Resolution> resolved = licences.resolveAll(rows);
            List<SinkRecord> chunk = new ArrayList<>();
            long chunkStart = cursor.getLastSeq();
            for (RecordRow row : rows) {
                SinkRecord record = rowRecord(row, resolved);
                if (!passes(record)) {
                    if (chunk.isEmpty()) {
                        cursor.setLastSeq(row.seq());
                        chunkStart = row.seq();
                    }
                    continue;
                }
                chunk.add(record);
                if (chunk.size() >= batchSize()) {
                    flushChunk(chunk, chunkStart, row.seq());
                    chunk.clear();
                    chunkStart = cursor.getLastSeq();
                }
            }
            if (!chunk.isEmpty()) {
                flushChunk(chunk, chunkStart, chunk.getLast().seq());
            }
            long last = rows.getLast().seq();
            if (last > cursor.getLastSeq()) {
                cursor.setLastSeq(last);
            }
            save(cursor);
        }

        private void flushChunk(List<SinkRecord> chunk, long chunkStart, long through) {
            int acked;
            try {
                acked = current().deliverAll(List.copyOf(chunk));
            } catch (SinkDeliveryException e) {
                cursor.setLastSeq(chunkStart);
                save(cursor);
                throw e;
            }
            delivered.add(acked);
            count(acked);
            if (acked < chunk.size()) {
                cursor.setLastSeq(acked == 0 ? chunkStart : chunk.get(acked - 1).seq());
                save(cursor);
                throw new SinkDeliveryException("the target did not acknowledge the delivery", null, null, false);
            }
            progressed = true;
            cursor.setLastSeq(through);
            cursor.setLastDeliveredAt(Instant.now());
            cursor.setState(SinkCursorEntity.RUNNING);
            save(cursor);
        }

        // ------------------------------------------------------------------------- STATE_MIRROR

        private void runMirror() {
            while (!closed.get()) {
                if (resync.getAndSet(false)) {
                    current().resync();
                    mirror.clear();
                    inbox.removeIf(item -> item != WAKE);
                    cursor.setResyncCount(cursor.getResyncCount() + 1);
                    mirrorSnapshot();
                }
                tailMirror();
                expire(Instant.now());
                healthy();
                flushCounters(false);
                Object item = take(nextWait());
                while (item != null && !closed.get() && !resync.get()) {
                    if (item instanceof SinkRecord record) {
                        apply(record);
                    }
                    item = inbox.poll();
                }
            }
        }

        /** Snapshots current state into the mirror, every page; the cursor moves to the snapshot watermark. */
        private void mirrorSnapshot() {
            long watermark = -1;
            String page = null;
            do {
                Slices.RecordSlice slice = queries.list(currentQuery(filter, page));
                if (watermark < 0) {
                    watermark = slice.watermarkSeq();
                }
                Map<Long, LicenceResolver.Resolution> resolved = licences.resolveAll(slice.rows());
                for (RecordRow row : slice.rows()) {
                    apply(rowRecord(row, resolved));
                }
                page = slice.nextCursor();
            } while (page != null && !closed.get());
            cursor.setLastSeq(Math.max(watermark, 0));
            cursor.setWatermarkSeq(Math.max(watermark, 0));
            cursor.setState(SinkCursorEntity.RUNNING);
            progressed = true;
            flushCounters(true);
        }

        /** Applies persisted changes after the mirror's position (new event records, updates, tombstones). */
        private void tailMirror() {
            while (!closed.get() && !resync.get()) {
                long after = cursor.getLastSeq();
                if (after < queries.purgeHorizon(filter.sources())) {
                    resync.set(true);
                    return;
                }
                Slices.ChangeSlice slice = queries.changes(changesQuery(filter, after));
                List<RecordRow> rows = new ArrayList<>();
                for (RecordRow row : slice.rows()) {
                    if (row.seq() > after) {
                        rows.add(row);
                    }
                }
                Map<Long, LicenceResolver.Resolution> resolved = licences.resolveAll(rows);
                for (RecordRow row : rows) {
                    apply(rowRecord(row, resolved));
                    cursor.setLastSeq(row.seq());
                }
                if (rows.isEmpty() && slice.lastSeq() > after) {
                    cursor.setLastSeq(slice.lastSeq());
                }
                if (!slice.hasMore()) {
                    return;
                }
            }
        }

        /**
         * Applies one update to the mirror through {@link MirrorState}: filter and gate, tombstone, last writer
         * wins by {@code observedAt}, then the expiry schedule. A record that leaves the filter or stops passing
         * the gate is removed if mirrored; a removal is only ever sent for a record that was mirrored.
         */
        void apply(SinkRecord record) {
            SinkInstance target = current();
            boolean passes = filter.matches(record, scope) && (record.tombstone() || allowed(record));
            switch (mirror.apply(record, passes)) {
                case UPSERT -> {
                    target.upsert(record);
                    delivered.increment();
                    count(1);
                }
                case REMOVE -> target.remove(record.sourceId(), record.externalId());
                case STALE -> dropped.increment();
                case IGNORE -> {
                    // A removal of something never mirrored.
                }
            }
        }

        /** Removes mirrored records whose {@code validUntil} has passed (unless a newer update replaced them). */
        void expire(Instant now) {
            for (MirrorState.Removal due : mirror.expire(now)) {
                current().remove(due.sourceId(), due.externalId());
            }
        }

        private Duration nextWait() {
            Instant next = mirror.nextExpiry();
            if (next == null) {
                return IDLE;
            }
            Duration until = Duration.between(Instant.now(), next);
            return until.isNegative() ? Duration.ZERO : min(until, IDLE);
        }

        // ------------------------------------------------------------------------- shared

        private SinkInstance current() {
            SinkInstance current = instance;
            if (current == null) {
                throw new IllegalStateException("the sink is closed");
            }
            return current;
        }

        private int batchSize() {
            return Math.max(1, Math.min(PAGE, current().batchSize()));
        }

        /** True when the record passes the filter and the gate; a gate refusal is counted. */
        private boolean passes(SinkRecord record) {
            return filter.matches(record, scope) && allowed(record);
        }

        private boolean allowed(SinkRecord record) {
            SinkRecordGate.Decision decision = gate.allow(mode, localTarget, filter.includePrivate(), record);
            if (!decision.allowed()) {
                denied(decision.reason(), record.sourceId());
                return false;
            }
            return true;
        }

        private void denied(String reason, String sourceId) {
            String why = reason == null ? "denied" : reason;
            meters.counter("foresignal.sink.denied", "sink", sink.getId(), "reason", why).increment();
            deniedByReason.computeIfAbsent(why, k -> new LongAdder()).increment();
            if (sourceId != null) {
                deniedBySource.computeIfAbsent(sourceId, k -> new LongAdder()).increment();
            }
        }

        private void count(int n) {
            if (n > 0) {
                meters.counter("foresignal.sink.delivered", "sink", sink.getId(), "type", sink.getType()).increment(n);
            }
        }

        private void awaitWork(Duration timeout) {
            Object item = take(timeout);
            if (item != null) {
                inbox.clear();
            }
        }

        private Object take(Duration timeout) {
            try {
                return timeout.isZero() ? inbox.poll() : inbox.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                if (!closed.get()) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }
        }

        /** A successful pass: clears the failing episode. */
        private void healthy() {
            if (failingSince != null || cursor.getFailingSince() != null) {
                failingSince = null;
                notified = false;
                cursor.setFailingSince(null);
                cursor.setLastError(null);
                cursor.setState(resync.get() ? SinkCursorEntity.RESYNCING : SinkCursorEntity.RUNNING);
                save(cursor);
            }
        }

        /** Writes the in-memory counters to the cursor row at most every {@link #FLUSH_EVERY} (or now). */
        private void flushCounters(boolean force) {
            Instant now = Instant.now();
            if (!force && Duration.between(lastFlush, now).compareTo(FLUSH_EVERY) < 0) {
                return;
            }
            lastFlush = now;
            cursor.setDelivered(cursor.getDelivered() + delivered.sumThenReset());
            cursor.setDropped(cursor.getDropped() + dropped.sumThenReset());
            if (!deniedByReason.isEmpty() || !deniedBySource.isEmpty()) {
                cursor.setDenied(mergeDenied(cursor.getDenied(), drain(deniedByReason), drain(deniedBySource)));
            }
            save(cursor);
        }

        private void markFailure(RuntimeException e) {
            if (cursor == null) {
                cursor = cursor(sink.getId());
            }
            cursor.setState(SinkCursorEntity.FAILING);
            cursor.setLastError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            Instant now = Instant.now();
            if (failingSince == null) {
                failingSince = now;
                cursor.setFailingSince(now);
            }
            meters.counter("foresignal.sink.failures", "sink", sink.getId()).increment();
            try {
                flushCounters(true);
            } catch (RuntimeException ignored) {
                // The database may be the failure; the next pass retries.
            }
            if (!notified && Duration.between(failingSince, now).compareTo(notifyAfter) >= 0) {
                notified = true;
                events.publishEvent(new SystemEvent("system.sink_failing", "warning",
                        "Sink " + sink.getName() + " is failing", cursor.getLastError(), "/v1/system",
                        Map.of("sinkId", sink.getId())));
            }
        }

        private void sleep(Duration duration) {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                // stop() interrupts; the loop re-checks closed.
            }
        }

        private SinkRecord liveRecord(LiveUpdate update) {
            return new SinkRecord(update.sourceId(), update.externalId(), update.kind(), 0L, update.version(),
                    update.observedAt(), update.validUntil(), false, update.geometry(),
                    update.properties() == null ? Map.of() : update.properties(), null,
                    attribution(update.sourceId()));
        }

        private SinkRecord rowRecord(RecordRow row, Map<Long, LicenceResolver.Resolution> resolved) {
            LicenceResolver.Resolution resolution = resolved.get(row.seq());
            return new SinkRecord(row.sourceId(), row.externalId(), row.kind(), row.seq(),
                    row.version(), row.observedAt(), row.validUntil(), row.tombstone(), parseGeometry(row.geometry()),
                    payload(row.payload()), recordTerms(resolution), attribution(row.sourceId()));
        }
    }

    /**
     * The license to hand to the gate for a stored version: the resolved per-record terms (batch license, peer
     * origin, MLAT downgrade), or null when the resolver fell back to the descriptor, so {@code EgressPolicy}
     * applies the source's current terms with its operator overrides.
     */
    static LicenceTerms recordTerms(LicenceResolver.Resolution resolution) {
        if (resolution == null || LicenceResolver.BASIS_DESCRIPTOR.equals(resolution.basis())) {
            return null;
        }
        return resolution.terms();
    }

    private static Map<String, Long> drain(Map<String, LongAdder> counters) {
        Map<String, Long> out = new LinkedHashMap<>();
        for (Map.Entry<String, LongAdder> e : counters.entrySet()) {
            long n = e.getValue().sumThenReset();
            if (n > 0) {
                out.put(e.getKey(), n);
            }
        }
        return out;
    }

    /** Adds per-reason and per-source denial deltas to the stored {@code denied} map. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> mergeDenied(Map<String, Object> stored, Map<String, Long> byReason,
            Map<String, Long> bySource) {
        Map<String, Object> denied = new LinkedHashMap<>(stored == null ? Map.of() : stored);
        long total = denied.get("total") instanceof Number n ? n.longValue() : 0L;
        Map<String, Object> reasons = denied.get("byReason") instanceof Map<?, ?> m
                ? new LinkedHashMap<>((Map<String, Object>) m) : new LinkedHashMap<>();
        Map<String, Object> sources = denied.get("bySource") instanceof Map<?, ?> m
                ? new LinkedHashMap<>((Map<String, Object>) m) : new LinkedHashMap<>();
        for (Map.Entry<String, Long> e : byReason.entrySet()) {
            total += e.getValue();
            reasons.merge(e.getKey(), e.getValue(), (a, b) -> ((Number) a).longValue() + ((Number) b).longValue());
        }
        for (Map.Entry<String, Long> e : bySource.entrySet()) {
            sources.merge(e.getKey(), e.getValue(), (a, b) -> ((Number) a).longValue() + ((Number) b).longValue());
        }
        denied.put("total", total);
        denied.put("byReason", reasons);
        denied.put("bySource", sources);
        return denied;
    }

    private static void closeQuietly(Subscription subscription) {
        if (subscription != null) {
            try {
                subscription.close();
            } catch (RuntimeException e) {
                // Already closed.
            }
        }
    }

    private static Duration min(Duration a, Duration b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private static Duration max(Duration a, Duration b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    /** One page of current state under the sink's source and kind filters (the snapshot read). */
    static CurrentQuery currentQuery(SinkFilter filter, String page) {
        return new CurrentQuery(filter.sources(), filter.kinds(), List.of(), null, null, null, null,
                TimeField.EFFECTIVE, null, false, ListOrder.ID, null, page, PAGE, FieldSelection.ALL,
                GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false, Set.of(), ReadRestriction.NONE);
    }

    /** One page of persisted changes after {@code after} under the sink's source and kind filters. */
    static ChangesQuery changesQuery(SinkFilter filter, long after) {
        return new ChangesQuery(after, filter.sources(), filter.kinds(), List.of(), null, true, PAGE,
                FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, Set.of(), null, false,
                ReadRestriction.NONE);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> payload(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return JSON.readValue(json, Map.class);
        } catch (RuntimeException e) {
            return Map.of();
        }
    }

    private static org.locationtech.jts.geom.Geometry parseGeometry(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return new ai.foresignal.source.formats.geojson.GeoJsonReader().read(json);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private ai.foresignal.source.spi.SourceDescriptor descriptor(String sourceId) {
        return sourceId != null && registry.contains(sourceId) ? registry.descriptor(sourceId) : null;
    }

    private String attribution(String sourceId) {
        var descriptor = descriptor(sourceId);
        return descriptor == null ? null : descriptor.attribution();
    }

    /** The mode a sink runs in: its configured {@code mode} when the type supports it, else PUSH, else SERVE. */
    static SinkMode resolveMode(SinkType type, SinkEntity sink) {
        String configured = String.valueOf(sink.getConfig().getOrDefault("mode", ""));
        if ("SERVE".equalsIgnoreCase(configured) && type.spec().modes().contains(SinkMode.SERVE)) {
            return SinkMode.SERVE;
        }
        if ("PUSH".equalsIgnoreCase(configured) && type.spec().modes().contains(SinkMode.PUSH)) {
            return SinkMode.PUSH;
        }
        return type.spec().modes().contains(SinkMode.PUSH) ? SinkMode.PUSH : SinkMode.SERVE;
    }

    /**
     * True when the sink's destination stays on the operator's network: a SERVE sink (its bind and every peer
     * are checked by {@link ServeGuard}), a PUSH target whose host resolves only to loopback or private addresses
     * ({@code LAN_ONLY}), or a LAN-scoped multicast group with TTL 1 (CoT SA).
     */
    boolean localTarget(SinkEntity sink, SinkMode mode) {
        if (mode == SinkMode.SERVE) {
            return true;
        }
        Object group = sink.getConfig().get("group");
        if (group != null && host(sink) == null) {
            try {
                return ServeGuard.lanMulticast(InetAddress.getByName(String.valueOf(group)));
            } catch (java.net.UnknownHostException e) {
                return false;
            }
        }
        String host = host(sink);
        return host != null && policy.checkHost(host, OutboundUrlPolicy.Profile.LAN_ONLY).allowed();
    }

    private static String host(SinkEntity sink) {
        for (String key : List.of("brokerUrl", "url", "host")) {
            Object value = sink.getConfig().get(key);
            if (value != null) {
                try {
                    java.net.URI uri = java.net.URI.create(String.valueOf(value));
                    return uri.getHost() != null ? uri.getHost() : String.valueOf(value);
                } catch (IllegalArgumentException e) {
                    return String.valueOf(value);
                }
            }
        }
        return null;
    }

    private SinkConfig config(SinkEntity sink, SinkMode mode) {
        Map<String, String> secrets = sink.getSecretCiphertext() == null ? Map.of()
                : new LinkedHashMap<>(cipher.decryptToMap(
                        new Encrypted(sink.getSecretCiphertext(), sink.getSecretNonce())));
        SinkFilter filter = SinkFilter.parse(JSON.valueToTree(sink.getFilter() == null ? Map.of() : sink.getFilter()));
        return new SinkConfig(sink.getId(), sink.getType(), sink.getName(), mode,
                new LinkedHashMap<>(sink.getConfig()), secrets, filter);
    }

    SinkCursorEntity cursor(String sinkId) {
        return cursors.findById(sinkId).orElseGet(() -> cursors.save(new SinkCursorEntity(sinkId)));
    }

    void save(SinkCursorEntity cursor) {
        cursors.save(cursor);
    }

}
