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
 * supervisor holds. A create or update message is mapped to an {@code earthquake} record; an {@code unid}
 * already emitted (by the stream or the gap-fill) is dropped. A connection silent for
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

    /** Queries the FDSN event service for events since the cursor and merges them before the stream resumes. */
    void gapFill() {
        String cursor = ctx.state().get(EmscSeismicAdapter.CURSOR_KEY).orElse(null);
        Instant from;
        if (cursor == null) {
            from = ctx.now().minus(Duration.ofHours(2));
        } else {
            try {
                from = Instant.parse(cursor).minus(EmscSeismicAdapter.GAP_OVERLAP);
            } catch (RuntimeException e) {
                from = ctx.now().minus(Duration.ofHours(2));
            }
        }
        URI uri = URI.create(adapter.fdsnUri() + "?format=json&limit=" + EmscSeismicAdapter.GAP_LIMIT
                + "&updatedafter=" + from.truncatedTo(ChronoUnit.SECONDS));
        try {
            UpstreamResponse response = ctx.http().get(uri);
            if (response.status() != 200) {
                log.warn("{}: FDSN gap-fill answered HTTP {}", ctx.sourceId(), response.status());
                return;
            }
            JsonNode features = JSON.readTree(response.body()).get("features");
            if (features == null || !features.isArray()) {
                return;
            }
            int merged = 0;
            for (JsonNode feature : features) {
                if (emit(feature, ctx.now())) {
                    merged++;
                }
            }
            log.info("{}: gap-fill merged {} missed event(s)", ctx.sourceId(), merged);
        } catch (AdapterException e) {
            log.warn("{}: gap-fill failed: {}", ctx.sourceId(), e.getMessage());
        } catch (RuntimeException e) {
            log.warn("{}: gap-fill could not be parsed: {}", ctx.sourceId(), e.getMessage());
        }
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
        emit(data, null);
    }

    private boolean emit(JsonNode feature, Instant fallbackObservedAt) {
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
        advanceCursor(record);
        return true;
    }

    private void advanceCursor(NormalizedRecord record) {
        Instant newest = record.observedAt() != null ? record.observedAt() : record.eventTime();
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
