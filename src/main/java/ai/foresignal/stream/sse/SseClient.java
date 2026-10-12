package ai.foresignal.stream.sse;

import ai.foresignal.api.support.RawJson;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Touch;
import java.io.IOException;
import java.time.Clock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

/**
 * One open SSE stream: its filter, its two lanes and its dedicated virtual sender thread
 * (Core-Data-Model §10.2, prompt 015).
 *
 * <h2>Lanes</h2>
 *
 * <ul>
 *   <li><b>FIFO</b> for id-bearing events ({@code change}, later {@code alert} and
 *       {@code notification}): a queue bounded by {@code clientBuffer}. When it would overflow the
 *       queue is dropped, the client gets {@code reset {reason: buffer_overflow, resumeAfter,
 *       watermarkSeq}} and the stream closes.
 *   <li><b>Conflation</b> for {@code live} and {@code touch}: a map keyed by
 *       {@code (sourceId, externalId)}. A {@code live} replaces whatever is pending; a {@code touch}
 *       updates only {@code observedAt}, {@code validUntil} and {@code bbox} of a pending
 *       {@code live}, or replaces a pending {@code touch}. The map is drained every
 *       {@code liveIntervalMs}; when it would grow past {@code liveMaxEntities} it is cleared and
 *       {@code resync {reason: live_cap}} is sent.
 * </ul>
 *
 * <p>Only FIFO events carry an {@code id:} line ({@code <seq>}, or the composite cursor on
 * {@code /v1/events/stream}). {@code live}, {@code touch}, {@code resync}, {@code heartbeat},
 * {@code reset}, {@code hello} and {@code filter-applied} never do, so {@code Last-Event-ID} always
 * names a FIFO cursor.
 *
 * <h2>Replay then live</h2>
 *
 * <p>The client is registered with the hub <em>before</em> it replays. While a replay runs the
 * client is <em>buffering</em>: live FIFO events queue but are not sent, and live-lane updates go
 * into the conflation map, which is not drained. The replay sends the matching events from the
 * database (up to {@code replayCap}; beyond it {@code reset {reason: replay_cap}} and close) and
 * reports the cursor it scanned through. Then the buffer is flushed, dropping every event whose
 * cursor is at or below the cursor already delivered, and the client is live. A bus gap re-enters
 * the same path from the last delivered cursor.
 *
 * <h2>Filter swap</h2>
 *
 * <p>The filter lives in an {@link AtomicReference}. Events are filtered when they are queued, under
 * the client lock; a swap sets the new filter and queues a marker under the same lock, so every
 * event before the marker was filtered with the old filter and every event after it with the new
 * one. When the sender reaches the marker it drops the conflation entries the new filter rejects and
 * sends {@code filter-applied {atSeq, filterVersion}}.
 *
 * <h2>Threads</h2>
 *
 * <p>Bus threads call the {@code offer*} methods, HTTP threads call {@link #applyFilter}; all of
 * them only queue work under the lock. Everything that writes to the connection runs in
 * {@link #pump()} on the sender thread (tests call it directly with a fake {@link SseSender} and a
 * mutable clock). The lock is a {@link ReentrantLock}, not {@code synchronized}, so a waiting
 * virtual thread never pins its carrier.
 */
public final class SseClient {

    private static final Logger log = LoggerFactory.getLogger(SseClient.class);

    /** FIFO events taken per sender iteration (also the payload batch size). */
    static final int SEND_BATCH = 64;

    /** The longest the sender sleeps without work, so it notices a close promptly. */
    static final long MAX_WAIT_MS = 1_000;

    /** How FIFO ids are written. */
    public enum CursorMode {
        /** {@code id: <seq>} ({@code /v1/changes/stream}). */
        SINGLE,
        /** {@code id: c=…;a=…} ({@code /v1/events/stream}). */
        COMPOSITE
    }

    /** Runs a topic replay on the sender thread. */
    @FunctionalInterface
    public interface ReplayDriver {

        /** Replays {@code topic} with {@code filter} (of the topic's filter type) into {@code sink}. */
        ReplayOutcome replay(String topic, ReplayRequest request, Object filter, ReplaySink sink);
    }

    /** Loads deferred payloads for a topic. */
    @FunctionalInterface
    public interface PayloadLoader {

        /** Payload JSON text keyed by seq. */
        Map<Long, String> load(String topic, Collection<Long> seqs);
    }

    /**
     * Everything a client needs besides its sender and filter.
     *
     * @param streamId the stream id ({@code hello.streamId})
     * @param principal who opened it
     * @param streamKey the anonymous PATCH key, or null
     * @param mode how FIFO ids are written
     * @param cursorKeys topic name to cursor key, in composite order, for every registered topic
     * @param initialCursors topic to the cursor already delivered (the resume point or the head)
     * @param replays topic to replay before going live
     * @param hello the {@code hello} data, or null to send none
     * @param limits the limits at connect
     * @param clock drives the live and heartbeat schedule
     * @param json serializes event data
     * @param replayDriver runs replays
     * @param payloadLoader loads deferred payloads
     * @param watermark the current change watermark, for {@code reset} and {@code resync}
     * @param onClose called once when the client closes (the hub unregisters it)
     */
    public record Settings(
            String streamId,
            StreamPrincipal principal,
            String streamKey,
            CursorMode mode,
            Map<String, String> cursorKeys,
            Map<String, Long> initialCursors,
            Map<String, ReplayRequest> replays,
            Map<String, Object> hello,
            StreamLimits limits,
            Clock clock,
            JsonMapper json,
            ReplayDriver replayDriver,
            PayloadLoader payloadLoader,
            LongSupplier watermark,
            Consumer<SseClient> onClose) {

        /** Copies the maps (keeping their order). */
        public Settings {
            Objects.requireNonNull(streamId, "streamId");
            Objects.requireNonNull(principal, "principal");
            Objects.requireNonNull(mode, "mode");
            cursorKeys = new LinkedHashMap<>(cursorKeys);
            initialCursors = new LinkedHashMap<>(initialCursors);
            replays = new LinkedHashMap<>(replays);
            Objects.requireNonNull(limits, "limits");
            Objects.requireNonNull(clock, "clock");
            Objects.requireNonNull(json, "json");
            onClose = onClose == null ? c -> {} : onClose;
        }
    }

    /** The FIFO marker of a filter swap. */
    private record FilterSwap(StreamFilter filter) {}

    /** A conflation key. */
    private record EntityKey(String sourceId, String externalId) {}

    /** A pending live-lane entry: a live update (with its match) or a standalone touch. */
    private record LiveEntry(LiveUpdate live, FilterMatch match, Touch touch, Collection<String> touchKinds) {}

    private final Settings settings;
    private final SseSender sender;
    private final AtomicReference<StreamFilter> filter;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition wake = lock.newCondition();
    private final ArrayDeque<Object> fifo = new ArrayDeque<>();
    private final ArrayDeque<SseFrame> control = new ArrayDeque<>();
    private final LinkedHashMap<EntityKey, LiveEntry> conflation = new LinkedHashMap<>();
    private final Map<String, ReplayRequest> pendingReplays = new LinkedHashMap<>();
    private final Map<String, Long> cursors = new ConcurrentHashMap<>();
    private int fifoEvents;
    private boolean buffering;
    private boolean overflowed;
    private boolean signaled;
    private volatile boolean closed;
    private boolean started;
    private long nextLiveDrain;
    private long nextHeartbeat;
    private Thread thread;

    /**
     * Creates the client; {@link #start()} launches its sender thread.
     *
     * @param settings the configuration
     * @param initialFilter the filter at connect (version 1)
     * @param sender where frames go
     */
    public SseClient(Settings settings, StreamFilter initialFilter, SseSender sender) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.sender = Objects.requireNonNull(sender, "sender");
        this.filter = new AtomicReference<>(Objects.requireNonNull(initialFilter, "initialFilter"));
        this.cursors.putAll(settings.initialCursors());
        this.pendingReplays.putAll(settings.replays());
        this.buffering = !pendingReplays.isEmpty();
    }

    // ------------------------------------------------------------------------- identity

    /** The stream id. */
    public String streamId() {
        return settings.streamId();
    }

    /** Who opened the stream. */
    public StreamPrincipal principal() {
        return settings.principal();
    }

    /** The anonymous PATCH key, or null for a token principal. */
    public String streamKey() {
        return settings.streamKey();
    }

    /** How this stream writes FIFO ids. */
    public CursorMode mode() {
        return settings.mode();
    }

    /** The current filter. */
    public StreamFilter filter() {
        return filter.get();
    }

    /** True once the client has closed. */
    public boolean isClosed() {
        return closed;
    }

    /** The cursor last delivered (or scanned through) for {@code topic}, or null. */
    public Long cursor(String topic) {
        return cursors.get(topic);
    }

    // ------------------------------------------------------------------------- lifecycle

    /** Starts the virtual sender thread. */
    public void start() {
        thread = Thread.ofVirtual().name("sse-" + settings.streamId()).start(this::run);
    }

    /** Closes the stream: ends the response, unregisters and stops the sender. Idempotent. */
    public void close() {
        lock.lock();
        try {
            if (closed) {
                return;
            }
            closed = true;
            signaled = true;
            wake.signalAll();
        } finally {
            lock.unlock();
        }
        sender.complete();
        try {
            settings.onClose().accept(this);
        } catch (RuntimeException e) {
            log.warn("SSE stream {} failed to unregister", settings.streamId(), e);
        }
    }

    private void run() {
        try {
            while (pump()) {
                awaitWork();
            }
        } catch (RuntimeException e) {
            log.warn("SSE stream {} stopped after an error", settings.streamId(), e);
        } finally {
            close();
        }
    }

    private void awaitWork() {
        lock.lock();
        try {
            if (!signaled && !closed) {
                long wait = Math.min(nextLiveDrain, nextHeartbeat) - settings.clock().millis();
                if (wait > 0) {
                    wake.await(Math.min(wait, MAX_WAIT_MS), TimeUnit.MILLISECONDS);
                }
            }
            signaled = false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            closed = true;
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------------------- producers

    /**
     * Queues a FIFO event of {@code topic}, rendered for this client's filter of that topic.
     *
     * @param topic the topic
     * @param render from this client's topic filter to its event, or null when the filter rejects it
     */
    public void offerFifo(String topic, Function<Object, FifoEvent> render) {
        lock.lock();
        try {
            if (closed || overflowed) {
                return;
            }
            Object topicFilter = filter.get().filterFor(topic);
            if (topicFilter == null) {
                return;
            }
            FifoEvent event = render.apply(topicFilter);
            if (event == null) {
                return;
            }
            if (fifoEvents >= settings.limits().clientBuffer()) {
                overflowed = true;
                fifo.clear();
                fifoEvents = 0;
            } else {
                fifo.add(event);
                fifoEvents++;
            }
            signal();
        } finally {
            lock.unlock();
        }
    }

    /** Puts a live update into the conflation lane when the {@code changes} filter accepts it with {@code live=true}. */
    public void offerLive(LiveUpdate update) {
        lock.lock();
        try {
            if (closed) {
                return;
            }
            ChangeFilter changes = filter.get().changes();
            if (changes == null || !changes.live()) {
                return;
            }
            FilterMatch match = changes.matchLive(update);
            if (!match.delivered()) {
                return;
            }
            put(new EntityKey(update.sourceId(), update.externalId()), new LiveEntry(update, match, null, null));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Puts a touch into the conflation lane: merged into a pending {@code live} of the entity, or,
     * with {@code touches=true}, as a standalone {@code touch}.
     *
     * @param touch the touch
     * @param possibleKinds the kinds the touch's source declares (a touch has no kind)
     */
    public void offerTouch(Touch touch, Collection<String> possibleKinds) {
        lock.lock();
        try {
            if (closed) {
                return;
            }
            ChangeFilter changes = filter.get().changes();
            if (changes == null) {
                return;
            }
            EntityKey key = new EntityKey(touch.sourceId(), touch.externalId());
            LiveEntry pending = conflation.get(key);
            if (pending != null && pending.live() != null) {
                conflation.put(key, new LiveEntry(LiveEvents.merge(pending.live(), touch), pending.match(), null, null));
                return;
            }
            if (!changes.touches() || !changes.matchTouch(touch, possibleKinds).delivered()) {
                return;
            }
            put(key, new LiveEntry(null, FilterMatch.MATCH, touch, possibleKinds));
        } finally {
            lock.unlock();
        }
    }

    private void put(EntityKey key, LiveEntry entry) {
        if (!conflation.containsKey(key) && conflation.size() >= settings.limits().liveMaxEntities()) {
            conflation.clear();
            control.add(resyncFrame("live_cap"));
            signal();
            return;
        }
        conflation.put(key, entry);
    }

    /** Queues {@code resync {reason, watermarkSeq}}. */
    public void resync(String reason) {
        lock.lock();
        try {
            if (closed) {
                return;
            }
            control.add(resyncFrame(reason));
            signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Queues an id-less control event (prompt 033: the inbox {@code badge}), sent ahead of FIFO
     * events and never carrying an {@code id:} line.
     *
     * @param event the SSE event name
     * @param data the JSON data
     */
    public void offerControl(String event, Object data) {
        lock.lock();
        try {
            if (closed) {
                return;
            }
            control.add(SseFrame.control(event, json(data)));
            signal();
        } finally {
            lock.unlock();
        }
    }

    /** Replays {@code topic} from the last delivered cursor (a bus gap), with the floor check. */
    public void requestReplay(String topic) {
        lock.lock();
        try {
            if (closed || !filter.get().subscribes(topic)) {
                return;
            }
            pendingReplays.put(topic, new ReplayRequest(cursors.getOrDefault(topic, 0L), true));
            signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Swaps the filter. Called after full validation; topics that are new start at
     * {@code addedHeads} (live from now, no replay).
     *
     * @param next the new filter
     * @param addedHeads the head cursor of each newly subscribed topic
     */
    public void applyFilter(StreamFilter next, Map<String, Long> addedHeads) {
        lock.lock();
        try {
            if (closed) {
                return;
            }
            filter.set(next);
            addedHeads.forEach(cursors::putIfAbsent);
            fifo.add(new FilterSwap(next));
            signal();
        } finally {
            lock.unlock();
        }
    }

    private void signal() {
        signaled = true;
        wake.signalAll();
    }

    // ------------------------------------------------------------------------- sender

    /**
     * Does all pending work once, on the sender thread: {@code hello}, replays, control events, the
     * overflow reset, FIFO events, the live drain when due and the heartbeat when due.
     *
     * @return false once the client is closed
     */
    public boolean pump() {
        if (closed) {
            return false;
        }
        long now = settings.clock().millis();
        if (!started) {
            started = true;
            nextLiveDrain = now + settings.limits().liveIntervalMs();
            nextHeartbeat = now + settings.limits().heartbeatSeconds() * 1000L;
            if (settings.hello() != null && !send(SseFrame.control("hello", json(settings.hello())))) {
                return false;
            }
        }
        if (!runReplays()) {
            return false;
        }
        for (SseFrame frame : drainControl()) {
            if (!send(frame)) {
                return false;
            }
        }
        if (takeOverflow()) {
            sendReset("buffer_overflow", cursors.get(ChangesTopic.NAME), settings.watermark().getAsLong());
            close();
            return false;
        }
        if (!drainFifo()) {
            return false;
        }
        now = settings.clock().millis();
        if (now >= nextLiveDrain) {
            nextLiveDrain = now + settings.limits().liveIntervalMs();
            if (!drainLive()) {
                return false;
            }
        }
        if (now >= nextHeartbeat) {
            nextHeartbeat = now + settings.limits().heartbeatSeconds() * 1000L;
            if (!send(SseFrame.control("heartbeat", "{}"))) {
                return false;
            }
        }
        return !closed;
    }

    private boolean runReplays() {
        Map<String, ReplayRequest> replays;
        lock.lock();
        try {
            if (pendingReplays.isEmpty()) {
                return true;
            }
            replays = new LinkedHashMap<>(pendingReplays);
            pendingReplays.clear();
            buffering = true;
        } finally {
            lock.unlock();
        }
        int cap = settings.limits().replayCap();
        int[] sent = {0};
        boolean[] failed = {false};
        ReplaySink sink = event -> {
            if (failed[0] || sent[0] >= cap) {
                return false;
            }
            if (!sendFifo(event)) {
                failed[0] = true;
                return false;
            }
            sent[0]++;
            return true;
        };
        for (Map.Entry<String, ReplayRequest> entry : replays.entrySet()) {
            String topic = entry.getKey();
            Object topicFilter = filter.get().filterFor(topic);
            if (topicFilter == null) {
                continue;
            }
            ReplayOutcome outcome = settings.replayDriver().replay(topic, entry.getValue(), topicFilter, sink);
            if (failed[0]) {
                close();
                return false;
            }
            switch (outcome) {
                case ReplayOutcome.Done done -> cursors.merge(topic, done.scannedThrough(), Math::max);
                case ReplayOutcome.CapReached reached -> {
                    sendReset("replay_cap", cursors.get(topic), settings.watermark().getAsLong());
                    close();
                    return false;
                }
                case ReplayOutcome.ResyncRequired resync -> {
                    sendReset("resync_required", null, resync.watermarkSeq());
                    close();
                    return false;
                }
            }
        }
        lock.lock();
        try {
            buffering = !pendingReplays.isEmpty();
            signal();
        } finally {
            lock.unlock();
        }
        return true;
    }

    private List<SseFrame> drainControl() {
        lock.lock();
        try {
            List<SseFrame> frames = new ArrayList<>(control);
            control.clear();
            return frames;
        } finally {
            lock.unlock();
        }
    }

    private boolean takeOverflow() {
        lock.lock();
        try {
            return overflowed;
        } finally {
            lock.unlock();
        }
    }

    private boolean drainFifo() {
        while (true) {
            List<Object> batch = new ArrayList<>(SEND_BATCH);
            lock.lock();
            try {
                if (buffering || overflowed) {
                    return true;
                }
                while (batch.size() < SEND_BATCH && !fifo.isEmpty()) {
                    Object item = fifo.poll();
                    if (item instanceof FifoEvent) {
                        fifoEvents--;
                    }
                    batch.add(item);
                }
            } finally {
                lock.unlock();
            }
            if (batch.isEmpty()) {
                return true;
            }
            loadPayloads(batch);
            for (Object item : batch) {
                boolean ok = item instanceof FilterSwap swap ? completeSwap(swap) : sendFifo((FifoEvent) item);
                if (!ok) {
                    return false;
                }
            }
        }
    }

    private void loadPayloads(List<Object> batch) {
        Map<String, List<FifoEvent>> wanted = new LinkedHashMap<>();
        for (Object item : batch) {
            if (item instanceof FifoEvent event && event.payloadSeq() != null) {
                wanted.computeIfAbsent(event.topic(), t -> new ArrayList<>()).add(event);
            }
        }
        for (Map.Entry<String, List<FifoEvent>> entry : wanted.entrySet()) {
            List<Long> seqs = entry.getValue().stream().map(FifoEvent::payloadSeq).toList();
            Map<Long, String> payloads = settings.payloadLoader().load(entry.getKey(), seqs);
            for (FifoEvent event : entry.getValue()) {
                event.data().put("payload", RawJson.of(payloads.get(event.payloadSeq())));
            }
        }
    }

    private boolean completeSwap(FilterSwap swap) {
        lock.lock();
        try {
            ChangeFilter changes = swap.filter().changes();
            conflation.entrySet().removeIf(e -> !stillMatches(changes, e.getValue()));
        } finally {
            lock.unlock();
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("atSeq", cursors.getOrDefault(ChangesTopic.NAME, 0L));
        data.put("filterVersion", swap.filter().version());
        return send(SseFrame.control("filter-applied", json(data)));
    }

    private static boolean stillMatches(ChangeFilter changes, LiveEntry entry) {
        if (changes == null) {
            return false;
        }
        if (entry.live() != null) {
            return changes.live() && changes.matchLive(entry.live()).delivered();
        }
        return changes.touches() && changes.matchTouch(entry.touch(), entry.touchKinds()).delivered();
    }

    private boolean drainLive() {
        List<LiveEntry> entries;
        lock.lock();
        try {
            if (buffering || conflation.isEmpty()) {
                return true;
            }
            entries = new ArrayList<>(conflation.values());
            conflation.clear();
        } finally {
            lock.unlock();
        }
        for (LiveEntry entry : entries) {
            SseFrame frame = entry.live() != null
                    ? SseFrame.control("live", json(LiveEvents.live(entry.live(), entry.match() == FilterMatch.LEFT)))
                    : SseFrame.control("touch", json(LiveEvents.touch(entry.touch())));
            if (!send(frame)) {
                return false;
            }
        }
        return true;
    }

    private boolean sendFifo(FifoEvent event) {
        Long delivered = cursors.get(event.topic());
        if (delivered != null && event.cursor() <= delivered) {
            return true;
        }
        cursors.put(event.topic(), event.cursor());
        String id = settings.mode() == CursorMode.SINGLE ? Long.toString(event.cursor()) : compositeId();
        return send(new SseFrame(id, event.event(), json(event.data())));
    }

    /** The composite id over the subscribed topics, in cursor-key order. */
    String compositeId() {
        StreamFilter current = filter.get();
        Map<String, Long> ordered = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : settings.cursorKeys().entrySet()) {
            if (current.subscribes(entry.getKey())) {
                ordered.put(entry.getValue(), cursors.getOrDefault(entry.getKey(), 0L));
            }
        }
        return CompositeCursor.format(ordered);
    }

    private void sendReset(String reason, Long resumeAfter, Long watermarkSeq) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reason", reason);
        data.put("resumeAfter", resumeAfter);
        data.put("watermarkSeq", watermarkSeq);
        send(SseFrame.control("reset", json(data)));
    }

    private SseFrame resyncFrame(String reason) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reason", reason);
        data.put("watermarkSeq", settings.watermark().getAsLong());
        return SseFrame.control("resync", json(data));
    }

    private boolean send(SseFrame frame) {
        if (closed) {
            return false;
        }
        try {
            sender.send(frame);
            return true;
        } catch (IOException | RuntimeException e) {
            log.debug("SSE stream {} is gone: {}", settings.streamId(), e.toString());
            close();
            return false;
        }
    }

    private String json(Object value) {
        return settings.json().writeValueAsString(value);
    }
}
