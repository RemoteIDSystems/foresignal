package ai.foresignal.source.adapters.seismic;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.MeteredWebSocket;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 * One {@code emsc-seismic} connection (prompt 089): the WebSocket listener and the {@link StreamHandle} the
 * supervisor holds. A create or update message is mapped to an {@code earthquake} record; a version
 * ({@code unid@lastupdate}) already emitted by the stream or the gap-fill is dropped. The cursor
 * ({@value EmscSeismicAdapter#CURSOR_KEY}) moves only after this connection's gap-fill completed ({@link #gapFill});
 * a failed gap-fill is retried every {@code GAP_RETRY} by the watchdog. A connection silent for
 * {@link EmscSeismicAdapter#SILENCE_RECYCLE} is recycled, one step of the supervisor's ladder.
 */
final class EmscSession implements StreamHandle, WebSocket.Listener {

    /** How often the watchdog reports bytes and checks silence. */
    static final Duration TICK = Duration.ofMillis(250);

    /** The largest message accepted. */
    static final int MAX_MESSAGE_BYTES = 2 * 1024 * 1024;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final EmscSeismicAdapter adapter;
    private final StreamContext ctx;
    private final RecordSink sink;
    private final Logger log;

    private final List<Consumer<Throwable>> closeCallbacks = new CopyOnWriteArrayList<>();
    private final AtomicBoolean finished = new AtomicBoolean();

    private volatile Throwable endedWith;
    private volatile boolean closedByUs;
    private volatile WebSocket socket;
    private volatile Instant lastLivenessAt;
    private volatile Thread watchdog;

    private final StringBuilder text = new StringBuilder();
    private boolean discarding;
    private volatile long payloadBytes;
    private long reportedBytes;

    private volatile boolean filled;
    private Instant pendingNewest;
    private volatile Instant lastGapAttempt;
    private final AtomicBoolean gapRunning = new AtomicBoolean();

    EmscSession(EmscSeismicAdapter adapter, StreamContext ctx, RecordSink sink) {
        this.adapter = adapter;
        this.ctx = ctx;
        this.sink = sink;
        this.log = ctx.log();
    }

    // ---------- StreamHandle ----------

    @Override
    public void close() {
        closedByUs = true;
        finished.set(true);
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

    // ---------- gap fill ----------

    /**
     * Queries the FDSN event service for every event updated since the cursor and merges it; called right after the
     * socket opens, so the stream covers everything the gap-fill's last page could miss.
     *
     * <p>The query is {@code orderby=time-asc} with {@code limit}/{@code offset} paging (offset is 1-based; verified
     * live on seismicportal.eu 2026-10-16), at most {@value EmscSeismicAdapter#GAP_MAX_PAGES} pages per attempt, until
     * a page holds fewer than {@value EmscSeismicAdapter#GAP_LIMIT} events; HTTP 204 (FDSN's default {@code nodata})
     * is an empty page. Progress ({@code updatedafter} and the next offset) is kept in the state
     * ({@value EmscSeismicAdapter#GAP_KEY}), so a failed or capped attempt resumes where it stopped, on the watchdog's
     * next retry or on the next connection. Until the gap-fill completes the cursor does not move, so a reconnect
     * never skips an unfilled gap; when it completes the cursor becomes the time its first attempt started (or the
     * newest streamed update, when later).
     *
     * @return true when the gap is filled
     */
    boolean gapFill() {
        if (!gapRunning.compareAndSet(false, true)) {
            return false;
        }
        try {
            return fillGap();
        } finally {
            gapRunning.set(false);
        }
    }

    private boolean fillGap() {
        Instant started = ctx.now();
        lastGapAttempt = ctx.clock().instant();
        Instant from = null;
        int offset = 1;
        String progress = ctx.state().get(EmscSeismicAdapter.GAP_KEY).orElse(null);
        if (progress != null) {
            try {
                String[] parts = progress.split("\\|");
                from = Instant.parse(parts[0]);
                offset = Math.max(1, Integer.parseInt(parts[1]));
                if (parts.length > 2) {
                    // A resumed pass covers what was updated since its first attempt started, not since this one.
                    started = Instant.parse(parts[2]);
                }
            } catch (RuntimeException e) {
                from = null;
                offset = 1;
                started = ctx.now();
            }
        }
        if (from == null) {
            from = cursorOrDefault().minus(EmscSeismicAdapter.GAP_OVERLAP).truncatedTo(ChronoUnit.SECONDS);
        }
        int merged = 0;
        try {
            for (int page = 0; page < EmscSeismicAdapter.GAP_MAX_PAGES; page++) {
                URI uri = URI.create(adapter.fdsnUri() + "?format=json&orderby=time-asc&limit="
                        + EmscSeismicAdapter.GAP_LIMIT + "&offset=" + offset + "&updatedafter=" + from);
                UpstreamResponse response = ctx.http().get(uri);
                int rows;
                if (response.status() == 204) {
                    rows = 0;
                } else if (response.status() != 200) {
                    log.warn("{}: FDSN gap-fill answered HTTP {}; the cursor stays until it completes", ctx.sourceId(),
                            response.status());
                    saveProgress(from, offset, started);
                    return false;
                } else {
                    JsonNode features = JSON.readTree(response.body()).get("features");
                    if (features == null || !features.isArray()) {
                        log.warn("{}: FDSN gap-fill page has no features array; the cursor stays", ctx.sourceId());
                        saveProgress(from, offset, started);
                        return false;
                    }
                    rows = features.size();
                    for (JsonNode feature : features) {
                        if (emit(feature, ctx.now())) {
                            merged++;
                        }
                    }
                }
                if (rows < EmscSeismicAdapter.GAP_LIMIT) {
                    complete(started);
                    log.info("{}: gap-fill merged {} missed event(s)", ctx.sourceId(), merged);
                    return true;
                }
                offset += rows;
            }
            saveProgress(from, offset, started);
            log.info("{}: gap-fill merged {} event(s) and stopped at the page cap; it resumes at offset {}",
                    ctx.sourceId(), merged, offset);
            return false;
        } catch (AdapterException e) {
            saveProgress(from, offset, started);
            log.warn("{}: gap-fill failed ({}); the cursor stays until it completes", ctx.sourceId(), e.getMessage());
            return false;
        } catch (RuntimeException e) {
            saveProgress(from, offset, started);
            log.warn("{}: gap-fill could not be parsed ({}); the cursor stays", ctx.sourceId(), e.getMessage());
            return false;
        }
    }

    /** The stored cursor, or two hours ago when none (or a damaged one) is stored. */
    private Instant cursorOrDefault() {
        String cursor = ctx.state().get(EmscSeismicAdapter.CURSOR_KEY).orElse(null);
        if (cursor != null) {
            try {
                return Instant.parse(cursor);
            } catch (RuntimeException e) {
                // fall through to the default window
            }
        }
        return ctx.now().minus(Duration.ofHours(2));
    }

    private void saveProgress(Instant from, int offset, Instant started) {
        ctx.state().put(EmscSeismicAdapter.GAP_KEY, from + "|" + offset + "|" + started);
    }

    /** Ends a gap-fill: the progress is dropped and the cursor moves to its start (or the newest streamed update). */
    private synchronized void complete(Instant started) {
        if (ctx.state().get(EmscSeismicAdapter.GAP_KEY).isPresent()) {
            ctx.state().remove(EmscSeismicAdapter.GAP_KEY);
        }
        Instant newest = pendingNewest != null && pendingNewest.isAfter(started) ? pendingNewest : started;
        filled = true;
        pendingNewest = null;
        advanceCursor(newest);
    }

    // ---------- WebSocket.Listener ----------

    @Override
    public void onOpen(WebSocket webSocket) {
        socket = webSocket;
        lastLivenessAt = ctx.clock().instant();
        log.info("{}: connected to {}", ctx.sourceId(), adapter.fdsnUri().getHost());
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
        int size = data.remaining();
        payloadBytes += size;
        if (!discarding) {
            if (text.length() + (long) size > MAX_MESSAGE_BYTES) {
                discarding = true;
                text.setLength(0);
            } else {
                byte[] chunk = new byte[size];
                data.get(chunk);
                text.append(new String(chunk, StandardCharsets.UTF_8));
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
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        end(new AdapterException("EMSC closed the connection (" + statusCode + ")"), true);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        end(new AdapterException("EMSC connection failed: " + error.getMessage(), error), true);
    }

    // ---------- watchdog ----------

    void startWatchdog() {
        watchdog = Thread.ofVirtual().name("foresignal-emsc-watchdog").start(() -> {
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
        Instant attempted = lastGapAttempt;
        if (!filled && attempted != null && Duration.between(attempted, ctx.clock().instant())
                .compareTo(EmscSeismicAdapter.GAP_RETRY) >= 0) {
            gapFill();
        }
        Instant since = lastLivenessAt;
        if (since != null && Duration.between(since, ctx.clock().instant())
                .compareTo(EmscSeismicAdapter.SILENCE_RECYCLE) >= 0) {
            log.warn("{}: silent for {} min; recycling the connection", ctx.sourceId(),
                    EmscSeismicAdapter.SILENCE_RECYCLE.toMinutes());
            end(new AdapterException("EMSC silent for " + EmscSeismicAdapter.SILENCE_RECYCLE.toMinutes()
                    + " min; the connection was recycled"), false);
            abortSocket();
        }
    }

    // ---------- internals ----------

    private void handle(String json) {
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (RuntimeException e) {
            log.debug("{}: dropping an unparsable message", ctx.sourceId());
            return;
        }
        JsonNode data = root.get("data");
        if (data == null || !data.isObject()) {
            return;
        }
        lastLivenessAt = ctx.clock().instant();
        emit(data, null, true);
    }

    private boolean emit(JsonNode feature, Instant fallbackObservedAt) {
        return emit(feature, fallbackObservedAt, false);
    }

    private boolean emit(JsonNode feature, Instant fallbackObservedAt, boolean fromStream) {
        NormalizedRecord record = SeismicRecords.fromFeature(feature, fallbackObservedAt);
        if (record == null) {
            return false;
        }
        Instant observedAt = record.observedAt() != null ? record.observedAt() : record.eventTime();
        String versionKey = record.externalId() + "@" + observedAt;
        if (!adapter.markSeen(versionKey)) {
            return false;
        }
        sink.accept(record);
        if (fromStream) {
            streamed(observedAt);
        }
        return true;
    }

    /** A streamed update: it moves the cursor once the gap is filled, and is remembered until then. */
    private synchronized void streamed(Instant observedAt) {
        if (observedAt == null) {
            return;
        }
        if (filled) {
            advanceCursor(observedAt);
        } else if (pendingNewest == null || observedAt.isAfter(pendingNewest)) {
            pendingNewest = observedAt;
        }
    }

    private synchronized void advanceCursor(Instant newest) {
        if (newest == null) {
            return;
        }
        Instant current = null;
        String stored = ctx.state().get(EmscSeismicAdapter.CURSOR_KEY).orElse(null);
        if (stored != null) {
            try {
                current = Instant.parse(stored);
            } catch (RuntimeException e) {
                current = null;
            }
        }
        if (current == null || newest.isAfter(current)) {
            ctx.state().put(EmscSeismicAdapter.CURSOR_KEY, newest.toString());
        }
    }

    private void end(Throwable failure, boolean closed) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        endedWith = failure;
        stopWatchdog();
        reportBytes();
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
