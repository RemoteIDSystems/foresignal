package ai.foresignal.source.adapters.internet;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.MeteredWebSocket;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One {@code ris-live} connection (prompt 092): the WebSocket listener and the {@link StreamHandle}. It sends
 * one {@code ris_subscribe} per watched prefix plus a {@code RIS_PEER_STATE} request, feeds every message into a
 * {@link PrefixVisibilityTracker}, and emits a record when the banded state changes or every ten minutes.
 */
final class RisLiveSession implements StreamHandle, WebSocket.Listener {

    static final Duration TICK = Duration.ofMillis(250);
    static final int MAX_MESSAGE_BYTES = 4 * 1024 * 1024;
    static final int MAX_PREFIXES = 50;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final RisLiveAdapter adapter;
    private final StreamContext ctx;
    private final RecordSink sink;
    private final List<String> prefixes;
    private final Map<String, List<Integer>> expected;
    private final List<String> hosts;
    private final Logger log;

    private final List<Consumer<Throwable>> closeCallbacks = new CopyOnWriteArrayList<>();
    private final AtomicBoolean finished = new AtomicBoolean();
    private final Map<String, PrefixVisibilityTracker> trackers = new LinkedHashMap<>();
    private final Map<String, String> lastBand = new LinkedHashMap<>();
    private final Map<String, Instant> lastEmit = new LinkedHashMap<>();

    private volatile Throwable endedWith;
    private volatile boolean closedByUs;
    private volatile WebSocket socket;
    private volatile Instant lastLivenessAt;
    private volatile Instant newestTimestamp;
    private volatile Thread watchdog;
    private volatile boolean peerStateRequested;

    private final StringBuilder text = new StringBuilder();
    private boolean discarding;
    private volatile long payloadBytes;
    private long reportedBytes;

    RisLiveSession(RisLiveAdapter adapter, StreamContext ctx, RecordSink sink, List<String> prefixes,
            Map<String, List<Integer>> expected, List<String> hosts) {
        this.adapter = adapter;
        this.ctx = ctx;
        this.sink = sink;
        this.prefixes = prefixes.size() > MAX_PREFIXES ? prefixes.subList(0, MAX_PREFIXES) : prefixes;
        this.expected = expected;
        this.hosts = hosts;
        this.log = ctx.log();
        for (String prefix : this.prefixes) {
            trackers.put(prefix, new PrefixVisibilityTracker(prefix));
        }
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

    @Override
    public boolean update(ai.foresignal.source.spi.ValidatedParams params) {
        List<String> next = params.stringList("prefixes");
        return next.equals(prefixes);
    }

    // ---------- WebSocket.Listener ----------

    @Override
    public void onOpen(WebSocket webSocket) {
        socket = webSocket;
        lastLivenessAt = ctx.clock().instant();
        for (String prefix : prefixes) {
            send("{\"type\":\"ris_subscribe\",\"data\":{\"prefix\":\"" + prefix
                    + "\",\"moreSpecific\":true,\"type\":\"UPDATE\"}}");
        }
        send("{\"type\":\"RIS_PEER_STATE\"}");
        peerStateRequested = true;
        log.info("{}: subscribed to {} prefix(es)", ctx.sourceId(), prefixes.size());
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
    public void onError(WebSocket webSocket, Throwable error) {
        end(new AdapterException("RIS Live connection failed: " + error.getMessage(), error));
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        end(new AdapterException("RIS Live closed the connection (" + statusCode + ")"));
        return null;
    }

    // ---------- internals ----------

    private void handle(String json) {
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (RuntimeException e) {
            return;
        }
        String type = root.path("type").asString(null);
        JsonNode data = root.get("data");
        if ("ris_error".equals(type)) {
            String message = data == null ? "" : data.path("msg").asString("");
            log.warn("{}: RIS Live error: {}", ctx.sourceId(), message);
            if (message.toLowerCase(java.util.Locale.ROOT).contains("too slow")) {
                end(new AdapterException("RIS Live asked the client to slow down; reconnecting on the next rung"));
                abortSocket();
            }
            return;
        }
        if ("ris_peer_state".equals(type)) {
            applyPeerState(data);
            return;
        }
        if (!"ris_message".equals(type) || data == null) {
            return;
        }
        lastLivenessAt = ctx.clock().instant();
        Instant at = timestamp(data);
        if (at != null && (newestTimestamp == null || at.isAfter(newestTimestamp))) {
            newestTimestamp = at;
        }
        String host = data.path("host").asString(null);
        String peer = data.path("peer").asString(null);
        if (data.get("announcements") != null && data.get("announcements").isArray()) {
            for (JsonNode announcement : data.get("announcements")) {
                String prefix = announcement.path("prefix").asString(null);
                PrefixVisibilityTracker tracker = prefix == null ? null : trackers.get(prefix);
                if (tracker != null) {
                    tracker.announce(host, peer, path(announcement.get("path")), at);
                }
            }
        }
        if (data.get("withdrawals") != null && data.get("withdrawals").isArray()) {
            for (JsonNode withdrawal : data.get("withdrawals")) {
                String prefix = withdrawal.path("prefix").asString(null);
                PrefixVisibilityTracker tracker = prefix == null ? null : trackers.get(prefix);
                if (tracker != null) {
                    tracker.withdraw(host, peer, at);
                }
            }
        }
        emit();
    }

    private static String path(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        StringBuilder out = new StringBuilder();
        for (JsonNode hop : node) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(hop.asString());
        }
        return out.toString();
    }

    private void applyPeerState(JsonNode data) {
        if (data == null || !data.isArray()) {
            return;
        }
        Instant at = ctx.clock().instant();
        for (JsonNode entry : data) {
            String host = entry.path("host").asString(null);
            String peer = entry.path("peer").asString(null);
            boolean down = "down".equalsIgnoreCase(entry.path("state").asString(""));
            for (PrefixVisibilityTracker tracker : trackers.values()) {
                tracker.peerState(host, peer, down, at);
            }
        }
        emit();
    }

    private void emit() {
        Instant now = ctx.clock().instant();
        for (Map.Entry<String, PrefixVisibilityTracker> entry : trackers.entrySet()) {
            String prefix = entry.getKey();
            PrefixVisibilityTracker.Snapshot snapshot = entry.getValue().snapshot(
                    expectedOrigins(prefix), prefixes, now, false);
            String band = snapshot.visibility() + "|" + snapshot.origins() + "|" + snapshot.unexpectedOrigin()
                    + "|" + snapshot.status();
            Instant last = lastEmit.get(prefix);
            boolean changed = !band.equals(lastBand.get(prefix));
            boolean due = last == null || last.plus(RisLiveAdapter.EMIT_INTERVAL).isBefore(now);
            if (!changed && !due) {
                continue;
            }
            lastBand.put(prefix, band);
            lastEmit.put(prefix, now);
            sink.accept(record(prefix, snapshot, now));
        }
    }

    private java.util.Set<Integer> expectedOrigins(String prefix) {
        List<Integer> asns = expected.get(prefix);
        return asns == null ? java.util.Set.of() : new java.util.LinkedHashSet<>(asns);
    }

    private NormalizedRecord record(String prefix, PrefixVisibilityTracker.Snapshot snapshot, Instant now) {
        NormalizedRecord.Builder b = NormalizedRecord.builder("prefix:" + prefix, RisLiveAdapter.KIND)
                .observedAt(newestTimestamp != null ? newestTimestamp : now);
        b.put("prefix", prefix);
        b.put("visibility", snapshot.visibility());
        b.put("status", snapshot.status());
        List<Map<String, Object>> origins = new ArrayList<>();
        for (PrefixVisibilityTracker.Origin origin : snapshot.origins()) {
            origins.add(Map.of("asn", origin.asn(), "asSet", origin.asSet()));
        }
        b.put("origins", origins);
        b.put("unexpectedOrigin", snapshot.unexpectedOrigin());
        b.put("moreSpecifics", snapshot.moreSpecifics());
        return b.build();
    }

    private static Instant timestamp(JsonNode data) {
        JsonNode value = data.get("timestamp");
        if (value == null || !value.isNumber()) {
            return null;
        }
        return Instant.ofEpochMilli((long) (value.asDouble() * 1000));
    }

    private void send(String value) {
        WebSocket ws = socket;
        if (ws != null) {
            ws.sendText(value, true).exceptionally(error -> {
                log.warn("{}: could not send: {}", ctx.sourceId(), error.getMessage());
                return null;
            });
        }
    }

    void startWatchdog() {
        watchdog = Thread.ofVirtual().name("foresignal-ris-watchdog").start(() -> {
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
                .compareTo(RisLiveAdapter.SILENCE_RECYCLE) >= 0) {
            end(new AdapterException("RIS Live was silent for five minutes; reconnecting"));
            abortSocket();
        }
    }

    private void end(Throwable failure) {
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
