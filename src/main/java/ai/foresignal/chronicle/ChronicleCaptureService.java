package ai.foresignal.chronicle;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.retention.RetentionStateStore;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.Subscription;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Captures what active chronicles' layers match into chronicle storage (prompt 131, CHR-1, CHR-2).
 *
 * <p><b>Never blocks ingest.</b> The change-bus subscriber only wakes the capture loop; the loop reads
 * {@code record_version} by seq from each chronicle's durable cursor ({@code chronicle.cursor.{id}}, the
 * {@code cursor_seq} column, in 019's consumer clamp through {@link ChronicleCursorConsumers}). A burst therefore
 * never waits for capture, and a capture that falls behind simply reads further back while the versions are still
 * retained (the catch-up mode is the only mode). Writes are batched per seq chunk, and a chunk's versions, counters and
 * cursors commit in one transaction, so a restart re-reads at most one chunk and the primary key makes the re-capture
 * a no-op (exactly once).
 *
 * <p>Per chunk, every row is pre-filtered by the in-memory {@link ChronicleIndex} (sources), then each candidate
 * layer's full test runs (selector, conditions, membership, area with prepared geometries), then the layer's capture
 * mode decides ({@link CaptureDecider}). A layer applies from its {@code added_at_seq}. At 90% of the size cap the
 * owner is notified; at the cap, full and downsampled layers capture events only, and the thread says so (never
 * silent loss).
 *
 * <p><b>Gaps.</b> When global retention released the clamp and deleted versions the cursor had not reached (the
 * retention floor of a layer source passed the cursor), the interval is recorded as a {@code capture-gap} item, the
 * owner gets {@code chronicle.gap}, and capture continues from the floor.
 *
 * <p>The loop starts on {@link ApplicationReadyEvent} unless {@code foresignal.chronicles.auto-start} is false (tests
 * call {@link #catchUp()}). Lifecycle changes run under the same lock as a chunk ({@link #exclusive}).
 */
@Service
public class ChronicleCaptureService implements ChangeListener, DisposableBean, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(ChronicleCaptureService.class);

    /** The change-bus subscriber name. */
    public static final String SUBSCRIBER = "chronicle-capture";

    /** How long an index may live before it is rebuilt (entity links and situation members move). */
    static final long INDEX_TTL_SECONDS = 60;

    /** The most entities the decider remembers per chronicle before it forgets them. */
    static final int STATE_MAX = 200_000;

    private final ChronicleRepository chronicles;
    private final ChronicleLayerRepository layers;
    private final ChronicleLayerCompiler compiler;
    private final ChronicleStorage storage;
    private final ChronicleThreadService thread;
    private final ChronicleNotices notices;
    private final ChronicleSeriesSampler sampler;
    private final ChangeBus changeBus;
    private final RetentionStateStore retention;
    private final SourceVisibility visibility;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private final ChronicleProperties properties;
    private final TransactionTemplate tx;

    private final ReentrantLock passLock = new ReentrantLock();
    private final Semaphore wake = new Semaphore(0);
    private final AtomicBoolean dirty = new AtomicBoolean(true);
    private final Map<String, CaptureDecider.State> states = new ConcurrentHashMap<>();
    private final Map<String, Integer> firstAppearances = new ConcurrentHashMap<>();
    private volatile ChronicleIndex index = ChronicleIndex.EMPTY;
    private volatile Subscription subscription;
    private volatile Thread thread0;
    private volatile boolean running;

    /** Creates the service. */
    public ChronicleCaptureService(ChronicleRepository chronicles, ChronicleLayerRepository layers,
            ChronicleLayerCompiler compiler, ChronicleStorage storage, ChronicleThreadService thread,
            ChronicleNotices notices, ChronicleSeriesSampler sampler, ChangeBus changeBus,
            RetentionStateStore retention, SourceVisibility visibility, JdbcTemplate jdbc, JsonMapper json, Clock clock,
            ChronicleProperties properties, PlatformTransactionManager transactions) {
        this.chronicles = chronicles;
        this.layers = layers;
        this.compiler = compiler;
        this.storage = storage;
        this.thread = thread;
        this.notices = notices;
        this.sampler = sampler;
        this.changeBus = changeBus;
        this.retention = retention;
        this.visibility = visibility;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
        this.properties = properties;
        this.tx = new TransactionTemplate(transactions);
    }

    // ------------------------------------------------------------------------- lifecycle

    /** Starts the subscriber and the capture loop once the application is ready (unless disabled). */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.CHRONICLES)
    public synchronized void start() {
        if (running || !properties.autoStart()) {
            return;
        }
        running = true;
        subscription = changeBus.subscribe(SUBSCRIBER, this, 64);
        thread0 = Thread.ofVirtual().name("chronicle-capture").start(this::loop);
    }

    /** Stops the loop. */
    @Override
    public synchronized void destroy() {
        running = false;
        if (subscription != null) {
            subscription.close();
        }
        if (thread0 != null) {
            thread0.interrupt();
        }
    }

    /** True while the background loop runs. */
    public boolean running() {
        return running;
    }

    @Override
    public void onBatch(ChangeBatch batch) {
        if (wake.availablePermits() == 0) {
            wake.release();
        }
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        onBatch(null);
    }

    /** Forgets the in-memory state (the test cleaner truncates what it describes). */
    @Override
    public void resetState() {
        states.clear();
        firstAppearances.clear();
        index = ChronicleIndex.EMPTY;
        dirty.set(true);
    }

    /** Marks the index stale: a chronicle or layer changed. */
    public void markDirty() {
        dirty.set(true);
        onBatch(null);
    }

    /**
     * Runs {@code work} under the capture lock, so no chunk is half-way while a lifecycle change or a layer edit
     * applies; marks the index stale afterwards.
     */
    public <T> T exclusive(Supplier<T> work) {
        passLock.lock();
        try {
            return work.get();
        } finally {
            passLock.unlock();
            markDirty();
        }
    }

    private void loop() {
        while (running) {
            try {
                catchUp();
                sampler.sampleDue(index());
            } catch (RuntimeException e) {
                log.warn("Chronicle capture pass failed: {}", e.toString());
            }
            try {
                wake.tryAcquire(properties.pollInterval().toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                if (!running) {
                    return;
                }
            }
        }
    }

    // ------------------------------------------------------------------------- index

    /** The current index, rebuilt when stale. */
    public ChronicleIndex index() {
        ChronicleIndex current = index;
        if (dirty.get() || current.builtAt().isBefore(clock.instant().minusSeconds(INDEX_TTL_SECONDS))) {
            synchronized (this) {
                if (dirty.getAndSet(false) || index.builtAt().isBefore(clock.instant().minusSeconds(INDEX_TTL_SECONDS))) {
                    index = build();
                }
                current = index;
            }
        }
        return current;
    }

    private ChronicleIndex build() {
        List<ChronicleIndex.Entry> entries = new ArrayList<>();
        List<ChronicleEntity> active = chronicles.findByStatusOrderByCreatedAtAscIdAsc(ChronicleVocabulary.ACTIVE);
        Map<String, List<ChronicleLayerEntity>> byChronicle = new HashMap<>();
        if (!active.isEmpty()) {
            for (ChronicleLayerEntity l : layers.findByChronicleIdIn(active.stream().map(ChronicleEntity::getId)
                    .toList())) {
                byChronicle.computeIfAbsent(l.getChronicleId(), k -> new ArrayList<>()).add(l);
            }
        }
        for (ChronicleEntity c : active) {
            List<CompiledLayer> compiled = new ArrayList<>();
            CaptureDecider.State state = states.computeIfAbsent(c.getId(), k -> new CaptureDecider.State());
            for (ChronicleLayerEntity l : byChronicle.getOrDefault(c.getId(), List.of())) {
                if (!l.live() || !l.isEnabled()) {
                    continue;
                }
                compiler.compile(c, l, false).ifPresent(compiled::add);
                if (!stateHasFrames(state, l.getId())) {
                    state.frames(l.getId(), l.getFrameCounter());
                }
            }
            entries.add(new ChronicleIndex.Entry(c, compiled));
        }
        return new ChronicleIndex(entries, clock.instant());
    }

    private static boolean stateHasFrames(CaptureDecider.State state, String layerId) {
        return state.frames(layerId) > 0;
    }

    // ------------------------------------------------------------------------- capture

    /**
     * What one {@link #catchUp()} did.
     *
     * @param chunks seq chunks processed
     * @param versions versions inserted (all chronicles)
     * @param rollups rollup increments
     * @param gaps capture gaps recorded
     */
    public record PassResult(int chunks, long versions, long rollups, int gaps) {}

    /**
     * Captures from every active chronicle's cursor up to the change bus's watermark.
     *
     * @return what the pass did
     */
    public PassResult catchUp() {
        long watermark = changeBus.lastPublishedSeq();
        int chunks = 0;
        long versions = 0;
        long rollups = 0;
        int gaps = 0;
        while (true) {
            passLock.lock();
            try {
                ChronicleIndex idx = index();
                if (idx.isEmpty()) {
                    break;
                }
                Map<String, Cursor> cursors = cursors();
                gaps += detectGaps(idx, cursors);
                long min = Long.MAX_VALUE;
                for (ChronicleIndex.Entry e : idx.entries()) {
                    Cursor c = cursors.get(e.id());
                    if (c != null) {
                        min = Math.min(min, c.seq());
                    }
                }
                if (min == Long.MAX_VALUE || min >= watermark) {
                    break;
                }
                long lo = min;
                long hi = Math.min(lo + properties.chunkSeqs(), watermark);
                Set<String> filter = idx.sourceFilter();
                List<CapturedRow> rows = filter != null && filter.isEmpty() ? List.of()
                        : storage.readVersions(lo, hi, filter);
                ChunkResult result = tx.execute(status -> chunk(idx, cursors, rows, hi));
                chunks++;
                if (result != null) {
                    versions += result.versions();
                    rollups += result.rollups();
                    checkCaps(idx, result.wrote());
                }
            } finally {
                passLock.unlock();
            }
        }
        return new PassResult(chunks, versions, rollups, gaps);
    }

    /**
     * A chronicle's capture position.
     *
     * @param seq the cursor
     * @param capState the cap state
     */
    record Cursor(long seq, String capState) {}

    private Map<String, Cursor> cursors() {
        Map<String, Cursor> out = new HashMap<>();
        jdbc.query("SELECT id, cursor_seq, cap_state FROM chronicle WHERE status = ? AND cursor_seq IS NOT NULL",
                rs -> {
                    out.put(rs.getString(1), new Cursor(rs.getLong(2), rs.getString(3)));
                }, ChronicleVocabulary.ACTIVE);
        return out;
    }

    /**
     * What one chunk wrote.
     *
     * @param versions versions inserted
     * @param rollups rollup increments
     * @param wrote the chronicles that wrote anything
     */
    record ChunkResult(long versions, long rollups, Set<String> wrote) {}

    private ChunkResult chunk(ChronicleIndex idx, Map<String, Cursor> cursors, List<CapturedRow> rows, long hi) {
        Instant now = clock.instant();
        long versions = 0;
        long rollups = 0;
        Set<String> wrote = new LinkedHashSet<>();
        Map<String, List<Match>> matches = new LinkedHashMap<>();
        for (CapturedRow row : rows) {
            for (CompiledLayer layer : idx.candidates(row.sourceId())) {
                Cursor c = cursors.get(layer.chronicleId());
                if (c == null || row.seq() <= c.seq() || !layer.matches(row)) {
                    continue;
                }
                matches.computeIfAbsent(layer.chronicleId(), k -> new ArrayList<>()).add(new Match(layer, row));
            }
        }
        for (ChronicleIndex.Entry e : idx.entries()) {
            Cursor c = cursors.get(e.id());
            if (c == null || c.seq() >= hi) {
                continue;
            }
            boolean exceeded = ChronicleVocabulary.CAP_EXCEEDED.equals(c.capState());
            Written w = write(e, matches.getOrDefault(e.id(), List.of()), exceeded, false, now);
            storage.advance(e.id(), hi, now, w.rows(), w.bytes());
            versions += w.versions();
            rollups += w.rollups();
            if (w.rows() != 0 || w.bytes() != 0) {
                wrote.add(e.id());
            }
        }
        return new ChunkResult(versions, rollups, wrote);
    }

    /**
     * A row a layer matched.
     *
     * @param layer the layer
     * @param row the row
     */
    record Match(CompiledLayer layer, CapturedRow row) {}

    /**
     * What a write did.
     *
     * @param rows the change in stored rows
     * @param bytes the change in estimated bytes
     * @param versions versions inserted
     * @param rollups rollup increments
     */
    record Written(long rows, long bytes, long versions, long rollups) {}

    /**
     * Writes the matches of one chronicle with its capture modes (also used by the backfill).
     *
     * @param entry the chronicle
     * @param found the matches, in seq order
     * @param exceeded whether the chronicle is over its cap (full and downsampled capture events only)
     * @param backfilled true for a backfill (no thread items; rows marked)
     * @param now the write time
     * @return what was written
     */
    Written write(ChronicleIndex.Entry entry, List<Match> found, boolean exceeded, boolean backfilled, Instant now) {
        String chronicleId = entry.id();
        CaptureDecider.State state = backfilled ? new CaptureDecider.State()
                : states.computeIfAbsent(chronicleId, k -> new CaptureDecider.State());
        if (state.size() > STATE_MAX) {
            state.clear();
        }
        prefill(chronicleId, found, state, exceeded);
        List<ChronicleStorage.VersionWrite> inserts = new ArrayList<>();
        Map<String, long[]> layerDelta = new HashMap<>();
        long rollups = 0;
        long rows = 0;
        long bytes = 0;
        Map<String, Long> rollupCounts = new LinkedHashMap<>();
        for (Match m : found) {
            CompiledLayer layer = m.layer();
            CapturedRow row = m.row();
            CaptureMode mode = layer.capture().effective(exceeded);
            CaptureDecider.Decision d = CaptureDecider.decide(mode, layer.layerId(), row, state);
            switch (d.action()) {
                case INSERT -> {
                    inserts.add(new ChronicleStorage.VersionWrite(layer.layerId(), row, backfilled, null));
                    long[] extra = afterCapture(chronicleId, layer, row, backfilled, now);
                    add(layerDelta, layer.layerId(), extra[0], extra[1]);
                    rows += extra[0];
                    bytes += extra[1];
                }
                case REPLACE -> {
                    long[] delta = storage.replaceLatest(chronicleId, new ChronicleStorage.VersionWrite(
                            layer.layerId(), row, backfilled, d.bucketStart()), now);
                    long[] extra = afterCapture(chronicleId, layer, row, backfilled, now);
                    add(layerDelta, layer.layerId(), delta[0] + extra[0], delta[1] + extra[1]);
                    rows += delta[0] + extra[0];
                    bytes += delta[1] + extra[1];
                }
                case ROLLUP -> {
                    String region = row.region() == null ? "ZZ" : row.region().substring(0, 2);
                    rollupCounts.merge(layer.layerId() + '\u0001' + region + '\u0001' + d.bucketStart(), 1L,
                            Long::sum);
                    rollups++;
                }
                default -> {
                    // skipped
                }
            }
        }
        for (Map.Entry<String, Long> r : rollupCounts.entrySet()) {
            String[] parts = r.getKey().split("\u0001", 3);
            boolean fresh = storage.upsertRollup(chronicleId, parts[0], parts[1], Instant.parse(parts[2]),
                    r.getValue(), backfilled);
            if (fresh) {
                add(layerDelta, parts[0], 1, ChronicleSizes.ROLLUP_ROW);
                rows++;
                bytes += ChronicleSizes.ROLLUP_ROW;
            }
        }
        long versions = 0;
        for (Map.Entry<String, long[]> t : storage.insertVersions(chronicleId, inserts, now).entrySet()) {
            add(layerDelta, t.getKey(), t.getValue()[0], t.getValue()[1]);
            rows += t.getValue()[0];
            bytes += t.getValue()[1];
            versions += t.getValue()[0];
        }
        for (CompiledLayer layer : entry.layers()) {
            long[] d = layerDelta.get(layer.layerId());
            Long frames = CaptureMode.FRAMES.equals(layer.capture().mode()) && !backfilled
                    ? state.frames(layer.layerId()) : null;
            if (d != null || frames != null) {
                storage.layerCounters(layer.layerId(), d == null ? 0 : d[0], d == null ? 0 : d[1], frames);
            }
        }
        return new Written(rows, bytes, versions, rollups);
    }

    private static void add(Map<String, long[]> deltas, String layerId, long rows, long bytes) {
        long[] t = deltas.computeIfAbsent(layerId, k -> new long[2]);
        t[0] += rows;
        t[1] += bytes;
    }

    /** Fills the decider's memory of {@code downsampled} layers from storage for entities it does not know. */
    private void prefill(String chronicleId, List<Match> found, CaptureDecider.State state, boolean exceeded) {
        Map<String, Set<String>> missing = new HashMap<>();
        for (Match m : found) {
            if (!CaptureMode.DOWNSAMPLED.equals(m.layer().capture().effective(exceeded).mode())) {
                continue;
            }
            if (!state.knows(m.layer().layerId(), m.row().entityKey())) {
                missing.computeIfAbsent(m.layer().layerId(), k -> new LinkedHashSet<>()).add(m.row().entityKey());
            }
        }
        for (Map.Entry<String, Set<String>> e : missing.entrySet()) {
            Map<String, Instant> last = storage.lastCaptured(chronicleId, e.getKey(), e.getValue());
            for (String key : e.getValue()) {
                state.captured(e.getKey(), key, last.get(key));
            }
        }
    }

    /**
     * What follows a captured version: a series point for {@code series:} layers, a frame pin for raster products,
     * and the thread items (first appearances, findings).
     *
     * @return {@code [rows, bytes]} the side writes added
     */
    private long[] afterCapture(String chronicleId, CompiledLayer layer, CapturedRow row, boolean backfilled,
            Instant now) {
        long rows = 0;
        long bytes = 0;
        ChronicleSelector selector = layer.selector();
        if (selector instanceof ChronicleSelector.Series s && !row.tombstone()) {
            Map<String, Object> p = row.payloadMap(json);
            String ref = "series:" + row.sourceId() + "/" + p.get("series");
            if (s.refs().contains(ref)) {
                Instant start = ChronicleSeriesSampler.instant(p.get("periodStart"), row.eventTime());
                if (start != null) {
                    boolean fresh = storage.upsertSeriesPoint(chronicleId, new ChronicleStorage.SeriesPointWrite(
                            layer.layerId(), ref, text(p.get("period")), start,
                            ChronicleSeriesSampler.instant(p.get("periodEnd"), null), number(p.get("value")),
                            text(p.get("unit")), text(p.get("status")), row.version(), row.ingestedAt(), Boolean.TRUE,
                            row.seq(), backfilled), now);
                    if (fresh) {
                        rows++;
                        bytes += ChronicleSizes.SERIES_ROW;
                    }
                }
            }
        }
        if (selector instanceof ChronicleSelector.RasterProducts) {
            Object frameKey = row.payloadMap(json).get("frameKey");
            if (frameKey != null && storage.pinFrame(chronicleId, layer.layerId(), row.externalId(),
                    frameKey.toString(), row.observedAt(), row.seq(), now)) {
                rows++;
                bytes += ChronicleSizes.FRAME_ROW;
            }
        }
        if (!backfilled) {
            threadItems(chronicleId, layer, row);
        }
        return new long[] {rows, bytes};
    }

    private void threadItems(String chronicleId, CompiledLayer layer, CapturedRow row) {
        PersistReason reason = row.persistReason();
        ChronicleSelector selector = layer.selector();
        boolean privateItem = isPrivate(row.sourceId());
        if (selector instanceof ChronicleSelector.Findings
                && (reason == PersistReason.FIRST || reason == PersistReason.SIGNIFICANT)) {
            Map<String, Object> p = row.payloadMap(json);
            ChronicleEventEntity item = thread.item(chronicleId, ChronicleVocabulary.ITEM_FINDING, row.observedAt(),
                    "Finding: " + Optional.ofNullable(text(p.get("title"))).orElse(row.externalId()));
            item.setSeq(row.seq());
            item.setLayerId(layer.layerId());
            item.setRef("finding:" + row.externalId());
            item.setSummary(text(p.get("findingType")));
            item.setSnapshot(snapshot(row));
            item.setPrivateItem(privateItem);
            thread.append(item);
            return;
        }
        boolean firstKind = selector instanceof ChronicleSelector.Records
                || selector instanceof ChronicleSelector.ExternalStream
                || selector instanceof ChronicleSelector.SavedView || selector instanceof ChronicleSelector.Watchlist
                || selector instanceof ChronicleSelector.Entities;
        if (firstKind && reason == PersistReason.FIRST && properties.firstAppearancesPerHour() > 0) {
            String hourKey = layer.layerId() + '\u0001' + row.observedAt().truncatedTo(ChronoUnit.HOURS);
            int n = firstAppearances.merge(hourKey, 1, Integer::sum);
            if (firstAppearances.size() > 10_000) {
                firstAppearances.clear();
            }
            if (n > properties.firstAppearancesPerHour()) {
                return;
            }
            ChronicleEventEntity item = thread.item(chronicleId, ChronicleVocabulary.ITEM_FIRST_APPEARANCE,
                    row.observedAt(), "First appearance in " + layer.name() + ": " + recordTitle(row));
            item.setSeq(row.seq());
            item.setLayerId(layer.layerId());
            item.setRef(ObjectRef.record(row.sourceId(), row.externalId(), row.seq()).format());
            item.setSnapshot(snapshot(row));
            item.setPrivateItem(privateItem);
            thread.append(item);
        }
    }

    /** The thread snapshot of a captured version. */
    Map<String, Object> snapshot(CapturedRow row) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", row.sourceId());
        out.put("externalId", row.externalId());
        out.put("kind", row.kind());
        out.put("seq", row.seq());
        out.put("observedAt", row.observedAt().toString());
        out.put("persistReason", row.persistReason().name());
        out.put("payload", row.payloadMap(json));
        return out;
    }

    /** A record's display title: {@code title}, {@code name}, {@code callsign} or {@code place}, else its id. */
    String recordTitle(CapturedRow row) {
        Map<String, Object> p = row.payloadMap(json);
        for (String f : List.of("title", "name", "callsign", "place")) {
            Object v = p.get(f);
            if (v instanceof String s && !s.isBlank()) {
                return s.length() > 200 ? s.substring(0, 200) : s;
            }
        }
        return row.externalId();
    }

    private boolean isPrivate(String sourceId) {
        try {
            return visibility.isPrivate(sourceId);
        } catch (RuntimeException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------------- caps and gaps

    private void checkCaps(ChronicleIndex idx, Set<String> wrote) {
        for (String id : wrote) {
            long[] usage = storage.usage(id);
            ChronicleIndex.Entry e = idx.entry(id);
            if (usage == null || e == null) {
                continue;
            }
            capTransition(e.chronicle(), usage[0], usage[1]);
        }
    }

    /**
     * Moves a chronicle's cap state to what its usage says, writing the {@code cap-warning} or {@code cap-fallback}
     * thread item and the owner notice on the way up.
     */
    void capTransition(ChronicleEntity c, long bytesUsed, long cap) {
        String target = capState(bytesUsed, cap);
        if (!storage.capState(c.getId(), target)) {
            return;
        }
        if (ChronicleVocabulary.CAP_WARNING.equals(target)) {
            ChronicleEventEntity item = thread.item(c.getId(), ChronicleVocabulary.ITEM_CAP_WARNING, clock.instant(),
                    "Storage at 90% of the size cap");
            item.setSummary(bytesUsed + " of " + cap + " bytes used; at the cap, full and downsampled layers switch to"
                    + " events-only.");
            item.setSnapshot(Map.of("bytesUsed", bytesUsed, "sizeCapBytes", cap));
            thread.append(item);
            notices.capWarning(c, bytesUsed, cap, false);
        } else if (ChronicleVocabulary.CAP_EXCEEDED.equals(target)) {
            ChronicleEventEntity item = thread.item(c.getId(), ChronicleVocabulary.ITEM_CAP_FALLBACK, clock.instant(),
                    "Size cap reached: full and downsampled layers now capture events only");
            item.setSummary(bytesUsed + " of " + cap + " bytes used. Raise the cap to restore full capture.");
            item.setSnapshot(Map.of("bytesUsed", bytesUsed, "sizeCapBytes", cap));
            thread.append(item);
            notices.capWarning(c, bytesUsed, cap, true);
        }
    }

    /** The cap state of a usage: {@code exceeded} at the cap, {@code warning} at 90%, else {@code ok}. */
    public static String capState(long bytesUsed, long cap) {
        if (cap > 0 && bytesUsed >= cap) {
            return ChronicleVocabulary.CAP_EXCEEDED;
        }
        if (cap > 0 && bytesUsed >= (long) Math.ceil(cap * ChronicleVocabulary.CAP_WARNING_SHARE)) {
            return ChronicleVocabulary.CAP_WARNING;
        }
        return ChronicleVocabulary.CAP_OK;
    }

    /**
     * Records a capture gap for every chronicle whose cursor fell behind the retention floor of a source it reads, and
     * moves its cursor to the floor.
     *
     * @return the gaps recorded
     */
    int detectGaps(ChronicleIndex idx, Map<String, Cursor> cursors) {
        int gaps = 0;
        for (ChronicleIndex.Entry e : idx.entries()) {
            Cursor c = cursors.get(e.id());
            if (c == null) {
                continue;
            }
            Set<String> sources = e.sources();
            if (sources != null && sources.isEmpty()) {
                continue;
            }
            long floor = retention.lowestRetainedSeq(sources);
            if (floor <= c.seq() + 1) {
                continue;
            }
            Instant now = clock.instant();
            ChronicleEntity chronicle = e.chronicle();
            Instant from = chronicle.getLastCaptureAt() != null ? chronicle.getLastCaptureAt()
                    : chronicle.getStartedAt() != null ? chronicle.getStartedAt() : now;
            Map<String, Object> gap = new LinkedHashMap<>();
            gap.put("from", from.toString());
            gap.put("to", now.toString());
            gap.put("reason", "global retention removed versions before capture reached them (seq " + (c.seq() + 1)
                    + " to " + (floor - 1) + "): the retention clamp was released after twice the retention window");
            tx.executeWithoutResult(s -> {
                ChronicleEventEntity item = thread.item(e.id(), ChronicleVocabulary.ITEM_CAPTURE_GAP, now,
                        "Capture gap: versions were lost to global retention");
                item.setGap(gap);
                item.setSummary(String.valueOf(gap.get("reason")));
                thread.append(item);
                storage.advance(e.id(), floor - 1, now, 0, 0);
            });
            cursors.put(e.id(), new Cursor(floor - 1, c.capState()));
            notices.gap(chronicle, from.toString(), now.toString());
            gaps++;
        }
        return gaps;
    }

    private static String text(Object v) {
        return v == null ? null : v.toString();
    }

    private static Double number(Object v) {
        if (v instanceof Number n) {
            double d = n.doubleValue();
            return Double.isFinite(d) ? d : null;
        }
        if (v instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
