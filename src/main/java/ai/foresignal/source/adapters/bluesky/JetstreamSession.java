package ai.foresignal.source.adapters.bluesky;

import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialRuntime;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.StreamLedger;
import ai.foresignal.source.formats.social.SocialJson;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.MeteredWebSocket;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One {@code bluesky-jetstream} connection (prompt 135): the WebSocket listener and the {@link StreamHandle}.
 *
 * <p>Frames are {@code {"$type": "message", "payload": {...}}} (Jetstream v2); a {@code #commit} payload carries
 * {@code seq}, {@code did}, {@code time}, {@code operation}, {@code collection}, {@code rkey} and {@code record}. The v1
 * shape ({@code kind: "commit"}, {@code time_us}, {@code commit{...}}) is read as well. Events at or below the last
 * seq are dropped (the cursor is inclusive). The seq and time are stored every few seconds; the stream ledger learns
 * of the connection, the lag and the cursor, and a drop records an open gap.
 */
final class JetstreamSession implements StreamHandle, WebSocket.Listener {

    /** How often the watchdog runs. */
    static final Duration TICK = Duration.ofSeconds(1);

    /** A dropped event is logged the first time and then once every this many. */
    static final long DROP_LOG_EVERY = 10_000;

    /** How often the cursor is stored. */
    static final Duration SAVE_EVERY = Duration.ofSeconds(5);

    /** A connection silent this long is recycled. */
    static final Duration SILENCE = Duration.ofMinutes(2);

    /** The largest message accepted. */
    static final int MAX_MESSAGE_BYTES = 2 * 1024 * 1024;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final StreamContext ctx;
    private final RecordSink sink;
    private final SocialRuntime runtime;
    private final SocialCollector collector;
    private final StreamLedger ledger;
    private final Logger log;
    private final StreamLedger.Gap pendingGap;
    private final List<Consumer<Throwable>> closeCallbacks = new CopyOnWriteArrayList<>();
    private final AtomicBoolean finished = new AtomicBoolean();
    private final StringBuilder text = new StringBuilder();

    private volatile Throwable endedWith;
    private volatile boolean closedByUs;
    private volatile WebSocket socket;
    private volatile Thread watchdog;
    private volatile Instant lastMessageAt;
    private volatile Instant lastSavedAt;
    private volatile long lastSeq;
    private volatile Instant lastTime;
    private volatile long payloadBytes;
    private long reportedBytes;
    private boolean discarding;
    private volatile boolean deletionSync;
    private volatile long posts;
    private volatile long stored;
    /** Events that could not be mapped this session. */
    private long dropped;

    JetstreamSession(StreamContext ctx, RecordSink sink, SocialRuntime runtime, Long storedSeq,
            StreamLedger.Gap pendingGap) {
        this.ctx = ctx;
        this.sink = sink;
        this.runtime = runtime;
        this.collector = runtime.collector();
        this.ledger = runtime.ledger();
        this.log = ctx.log();
        this.pendingGap = pendingGap;
        this.lastSeq = storedSeq == null ? 0 : storedSeq;
        this.deletionSync = runtime.settings(BlueskyJetstreamAdapter.ID).deletionSync();
    }

    // ---------- StreamHandle ----------

    @Override
    public void close() {
        closedByUs = true;
        finished.set(true);
        saveCursor();
        abortSocket();
        stopWatchdog();
        reportBytes();
    }

    @Override
    public boolean isRunning() {
        return !finished.get();
    }

    @Override
    public void onClose(Consumer<Throwable> callback) {
        closeCallbacks.add(callback);
        if (finished.get() && !closedByUs) {
            callback.accept(endedWith);
        }
    }

    // ---------- WebSocket.Listener ----------

    @Override
    public void onOpen(WebSocket webSocket) {
        socket = webSocket;
        Instant now = ctx.clock().instant();
        lastMessageAt = now;
        if (ledger != null) {
            if (pendingGap != null) {
                ledger.gap(ctx.sourceId(), pendingGap.from(), pendingGap.to(), pendingGap.replayed(),
                        pendingGap.reason());
            }
            ledger.connected(ctx.sourceId(), now, pendingGap == null || pendingGap.replayed());
        }
        ctx.connected();
        log.info("{}: connected to Jetstream", ctx.sourceId());
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        payloadBytes += data.length();
        if (!discarding) {
            text.append(data);
            if (text.length() > MAX_MESSAGE_BYTES) {
                discarding = true;
                text.setLength(0);
            }
        }
        if (last) {
            String message = discarding ? null : text.toString();
            discarding = false;
            text.setLength(0);
            if (message != null) {
                handle(message);
            }
        }
        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        byte[] chunk = new byte[data.remaining()];
        data.get(chunk);
        return onText(webSocket, new String(chunk, StandardCharsets.UTF_8), last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        end(new AdapterException("Jetstream closed the connection (" + statusCode + " " + reason + ")"));
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        end(new AdapterException("Jetstream connection failed: " + error.getMessage(), error));
    }

    // ---------- events ----------

    /** Handles one frame; package-private for tests. */
    void handle(String json) {
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (RuntimeException e) {
            return;
        }
        Instant receivedAt = ctx.clock().instant();
        lastMessageAt = receivedAt;
        JsonNode payload = root.has("payload") && root.get("payload").isObject() ? root.get("payload") : root;
        String type = SocialJson.text(payload, "$type");
        if (type != null && type.endsWith("#info")) {
            log.info("{}: Jetstream info {}: {}", ctx.sourceId(), SocialJson.text(payload, "name"),
                    SocialJson.text(payload, "message"));
            return;
        }
        Long seq = SocialJson.number(payload, "seq");
        Instant time = SocialJson.instant(payload, "time");
        Long timeUs = SocialJson.number(payload, "time_us");
        if (time == null && timeUs != null) {
            time = Instant.EPOCH.plus(timeUs, java.time.temporal.ChronoUnit.MICROS);
        }
        long position = seq != null ? seq : timeUs != null ? timeUs : 0;
        if (position > 0 && position <= lastSeq) {
            return;
        }
        JsonNode commit = payload;
        if ("commit".equals(SocialJson.text(payload, "kind")) && payload.get("commit") != null) {
            commit = payload.get("commit");
        } else if (SocialJson.text(payload, "operation") == null) {
            advance(position, time, receivedAt);
            return;
        }
        String did = SocialJson.text(payload, "did");
        String operation = SocialJson.text(commit, "operation");
        String collection = SocialJson.text(commit, "collection");
        String rkey = SocialJson.text(commit, "rkey");
        if (did != null && operation != null && collection != null && rkey != null) {
            try {
                commit(did, operation, collection, rkey, commit.get("record"), time == null ? receivedAt : time);
            } catch (RuntimeException e) {
                // Logged loudly: from 2026-10-19 the live stream counted no posts while staying connected and healthy,
                // and this line, then at debug, was the only place the cause could have shown.
                dropped++;
                if (dropped == 1 || dropped % DROP_LOG_EVERY == 0) {
                    log.warn("{}: dropped {} event(s) it could not map; latest {} {}", ctx.sourceId(), dropped,
                            collection, operation, e);
                }
            }
        }
        advance(position, time, receivedAt);
    }

    private void commit(String did, String operation, String collection, String rkey, JsonNode record, Instant time) {
        String atUri = BlueskyRecords.atUri(did, collection, rkey);
        if (BlueskyRecords.POST.equals(collection)) {
            if ("delete".equals(operation)) {
                if (deletionSync) {
                    NormalizedRecord tombstoned = collector.observeDelete(ctx.sourceId(), SocialVocabulary.BLUESKY,
                            atUri);
                    if (tombstoned != null) {
                        sink.accept(tombstoned);
                    }
                }
                return;
            }
            SocialPost post = BlueskyRecords.post(atUri, new SocialPost.Author(did, null, did, null, null, null),
                    record, time, null, null, SocialVocabulary.COLLECTION_STREAM);
            if (post == null) {
                return;
            }
            posts++;
            SocialCollector.Observation obs = collector.observe(post, ctx.sourceId(), SocialVocabulary.COMPLETE);
            if (obs.record() != null) {
                stored++;
                sink.accept(obs.record());
            }
        } else if (BlueskyRecords.REPOST.equals(collection) && "create".equals(operation)) {
            String subject = SocialJson.text(SocialJson.at(record, "subject"), "uri");
            SocialPost.PostRef ref = BlueskyRecords.ref(subject);
            if (ref != null) {
                collector.observeRepost(ref, time, SocialVocabulary.COMPLETE);
            }
        }
    }

    private void advance(long position, Instant time, Instant receivedAt) {
        if (position > 0) {
            lastSeq = position;
        }
        if (time != null) {
            lastTime = time;
            if (ledger != null) {
                ledger.lag(ctx.sourceId(), Math.max(0, Duration.between(time, receivedAt).toMillis() / 1000.0));
            }
        }
    }

    /** The posts seen and stored on this connection (tests and logs). */
    long[] counts() {
        return new long[] {posts, stored};
    }

    // ---------- watchdog ----------

    void startWatchdog() {
        watchdog = WorkThreads.start(WorkRole.COLLECT, "foresignal-jetstream-watchdog", () -> {
            while (!finished.get()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(TICK.toMillis());
                } catch (InterruptedException e) {
                    return;
                }
                tick();
            }
        });
    }

    void tick() {
        if (finished.get()) {
            return;
        }
        reportBytes();
        Instant now = ctx.clock().instant();
        if (lastSavedAt == null || Duration.between(lastSavedAt, now).compareTo(SAVE_EVERY) >= 0) {
            saveCursor();
            lastSavedAt = now;
            deletionSync = runtime.settings(BlueskyJetstreamAdapter.ID).deletionSync();
        }
        Instant since = lastMessageAt;
        if (since != null && Duration.between(since, now).compareTo(SILENCE) >= 0) {
            log.warn("{}: silent for {} s; recycling the connection", ctx.sourceId(), SILENCE.toSeconds());
            end(new AdapterException("Jetstream silent for " + SILENCE.toSeconds() + " s; recycled"));
            abortSocket();
        }
    }

    /** Stores the seq and time (the resume point) and tells the ledger. */
    void saveCursor() {
        long seq = lastSeq;
        Instant time = lastTime;
        if (seq > 0) {
            ctx.state().put(BlueskyJetstreamAdapter.SEQ_KEY, Long.toString(seq));
            if (ledger != null) {
                ledger.cursor(ctx.sourceId(), Long.toString(seq));
            }
        }
        if (time != null) {
            ctx.state().put(BlueskyJetstreamAdapter.TIME_KEY, time.toString());
        }
    }

    private void end(Throwable failure) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        endedWith = failure;
        saveCursor();
        stopWatchdog();
        reportBytes();
        if (ledger != null && !closedByUs) {
            Instant from = lastTime != null ? lastTime : ctx.clock().instant();
            ledger.disconnected(ctx.sourceId(), from, failure == null ? null : failure.getMessage());
        }
        for (Consumer<Throwable> callback : closeCallbacks) {
            try {
                callback.accept(failure);
            } catch (RuntimeException e) {
                log.debug("{}: a close callback failed", ctx.sourceId(), e);
            }
        }
    }

    private void reportBytes() {
        WebSocket ws = socket;
        long total = ws instanceof MeteredWebSocket metered ? metered.wireBytesReceived() : payloadBytes;
        long delta;
        synchronized (this) {
            delta = total - reportedBytes;
            reportedBytes = total;
        }
        if (delta > 0) {
            ctx.reportBytes(delta);
        }
    }

    private void abortSocket() {
        WebSocket ws = socket;
        if (ws != null) {
            try {
                ws.abort();
            } catch (RuntimeException e) {
                log.debug("{}: aborting the socket failed", ctx.sourceId(), e);
            }
        }
    }

    private void stopWatchdog() {
        Thread thread = watchdog;
        if (thread != null && thread != Thread.currentThread()) {
            thread.interrupt();
        }
    }
}
