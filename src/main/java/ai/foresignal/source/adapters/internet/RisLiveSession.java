package ai.foresignal.source.adapters.internet;

import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.MeteredWebSocket;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * One {@code ris-live} connection (prompt 092): the WebSocket listener and the {@link StreamHandle}. It sends
 * one {@code ris_subscribe} per watched prefix (per prefix and host when {@code hosts} is set) plus a
 * {@code RIS_PEER_STATE} request, feeds every message into the watched prefixes' {@link PrefixVisibilityTracker}s,
 * and emits a record when the banded state changes or every ten minutes.
 *
 * <p>Announcements are matched to watched prefixes by CIDR containment ({@link Cidr}): an exact match updates the
 * prefix's peers, a more-specific one its {@code moreSpecifics}. When the connection fails, every prefix is emitted
 * once more with {@code status: no-data} and its last visibility; the trackers belong to the adapter, so the next
 * connection carries on from them rather than from zero.
 */
final class RisLiveSession implements StreamHandle, WebSocket.Listener {

    static final Duration TICK = Duration.ofMillis(250);
    static final int MAX_MESSAGE_BYTES = 4 * 1024 * 1024;
    static final int MAX_PREFIXES = 50;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final StreamContext ctx;
    private final RecordSink sink;
    private final List<String> prefixes;
    private final List<Watched> watched = new ArrayList<>();
    private final List<String> hosts;
    private final Set<String> hostFilter;
    private final Logger log;
    private volatile Map<String, List<Integer>> expected;

    private final List<Consumer<Throwable>> closeCallbacks = new CopyOnWriteArrayList<>();
    private final AtomicBoolean finished = new AtomicBoolean();
    private final Map<String, String> lastBand = new LinkedHashMap<>();
    private final Map<String, Instant> lastEmit = new LinkedHashMap<>();

    private volatile Throwable endedWith;
    private volatile boolean closedByUs;
    private volatile WebSocket socket;
    private volatile Instant lastLivenessAt;
    private volatile Instant newestTimestamp;
    private volatile Thread watchdog;

    private final StringBuilder text = new StringBuilder();
    private boolean discarding;
    private volatile long payloadBytes;
    private long reportedBytes;

    /** One watched prefix: its parsed form and its tracker. */
    private record Watched(Cidr cidr, PrefixVisibilityTracker tracker) {}

    RisLiveSession(RisLiveAdapter adapter, StreamContext ctx, RecordSink sink, List<String> prefixes,
            Map<String, List<Integer>> expected, List<String> hosts) {
        this.ctx = ctx;
        this.sink = sink;
        this.log = ctx.log();
        List<String> capped = prefixes.size() > MAX_PREFIXES ? prefixes.subList(0, MAX_PREFIXES) : prefixes;
        this.prefixes = List.copyOf(capped);
        Set<String> seen = new LinkedHashSet<>();
        for (String prefix : capped) {
            Optional<Cidr> cidr = Cidr.parse(prefix);
            if (cidr.isEmpty()) {
                log.warn("{}: ignoring {}, which is not a CIDR prefix", ctx.sourceId(), prefix);
                continue;
            }
            if (seen.add(cidr.get().toString())) {
                watched.add(new Watched(cidr.get(), adapter.tracker(cidr.get().toString())));
            }
        }
        adapter.retainTrackers(seen);
        this.expected = canonicalExpected(expected);
        this.hosts = hosts == null ? List.of() : hosts.stream().filter(h -> h != null && !h.isBlank())
                .map(String::strip).distinct().toList();
        this.hostFilter = new LinkedHashSet<>(this.hosts);
    }

    /** The subscribe messages this session sends: one per prefix, or one per prefix and host. */
    List<String> subscribeMessages() {
        List<String> out = new ArrayList<>();
        for (Watched w : watched) {
            if (hosts.isEmpty()) {
                out.add(subscribe(w.cidr().toString(), null));
            } else {
                for (String host : hosts) {
                    out.add(subscribe(w.cidr().toString(), host));
                }
            }
        }
        return out;
    }

    private static String subscribe(String prefix, String host) {
        ObjectNode message = JSON.createObjectNode();
        message.put("type", "ris_subscribe");
        ObjectNode data = message.putObject("data");
        data.put("prefix", prefix);
        data.put("moreSpecific", true);
        data.put("type", "UPDATE");
        if (host != null) {
            data.put("host", host);
        }
        return JSON.writeValueAsString(message);
    }

    private static Map<String, List<Integer>> canonicalExpected(Map<String, List<Integer>> expected) {
        Map<String, List<Integer>> out = new LinkedHashMap<>();
        if (expected != null) {
            expected.forEach((prefix, asns) -> Cidr.parse(prefix).ifPresent(c -> out.put(c.toString(), asns)));
        }
        return out;
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

    /**
     * Applies new expected origins in place; a change of prefixes or hosts needs new subscriptions, so it asks for
     * a restart.
     */
    @Override
    public boolean update(ai.foresignal.source.spi.ValidatedParams params) {
        if (!params.stringList("prefixes").equals(prefixes) || !normalizedHosts(params.stringList("hosts")).equals(hosts)) {
            return false;
        }
        expected = canonicalExpected(RisLiveAdapter.expectedOrigins(params.string("expectedOrigins")));
        return true;
    }

    private static List<String> normalizedHosts(List<String> values) {
        return values == null ? List.of() : values.stream().filter(h -> h != null && !h.isBlank())
                .map(String::strip).distinct().toList();
    }

    // ---------- WebSocket.Listener ----------

    @Override
    public void onOpen(WebSocket webSocket) {
        socket = webSocket;
        lastLivenessAt = ctx.clock().instant();
        List<String> messages = subscribeMessages();
        for (String message : messages) {
            send(message);
        }
        send("{\"type\":\"RIS_PEER_STATE\"}");
        log.info("{}: sent {} subscription(s) for {} prefix(es)", ctx.sourceId(), messages.size(), watched.size());
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

    private synchronized void handle(String json) {
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (RuntimeException e) {
            return;
        }
        String type = root.path("type").asString(null);
        JsonNode data = root.get("data");
        if ("ris_error".equals(type)) {
            String message = data == null ? "" : data.path("message").asString(data.path("msg").asString(""));
            log.warn("{}: RIS Live error: {}", ctx.sourceId(), message);
            if (message.toLowerCase(Locale.ROOT).contains("too slow")) {
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
        String host = data.path("host").asString(null);
        if (!hostFilter.isEmpty() && (host == null || !hostFilter.contains(host))) {
            return;
        }
        Instant at = timestamp(data);
        if (at != null && (newestTimestamp == null || at.isAfter(newestTimestamp))) {
            newestTimestamp = at;
        }
        // peer_asn arrives as a string; the peer address identifies the session.
        String peer = data.path("peer").asString(null);
        JsonNode announcements = data.get("announcements");
        if (announcements != null && announcements.isArray()) {
            for (JsonNode announcement : announcements) {
                String path = path(data.get("path"));
                JsonNode prefixList = announcement.get("prefixes");
                if (prefixList != null && prefixList.isArray()) {
                    for (JsonNode p : prefixList) {
                        announce(p.asString(null), host, peer, path, at);
                    }
                } else {
                    // An older message shape carries one prefix and its own path per announcement.
                    String own = path(announcement.get("path"));
                    announce(announcement.path("prefix").asString(null), host, peer, own != null ? own : path, at);
                }
            }
        }
        JsonNode withdrawals = data.get("withdrawals");
        if (withdrawals != null && withdrawals.isArray()) {
            for (JsonNode withdrawal : withdrawals) {
                String prefix = withdrawal.isString() ? withdrawal.asString() : withdrawal.path("prefix").asString(null);
                withdraw(prefix, host, peer, at);
            }
        }
        emit(false);
    }

    private void announce(String prefix, String host, String peer, String path, Instant at) {
        Optional<Cidr> cidr = Cidr.parse(prefix);
        if (cidr.isEmpty()) {
            return;
        }
        for (Watched w : watched) {
            if (w.cidr().equals(cidr.get())) {
                w.tracker().announce(host, peer, path, at);
            } else if (w.cidr().strictlyContains(cidr.get())) {
                w.tracker().announceMoreSpecific(cidr.get().toString(), host, peer, at);
            }
        }
    }

    private void withdraw(String prefix, String host, String peer, Instant at) {
        Optional<Cidr> cidr = Cidr.parse(prefix);
        if (cidr.isEmpty()) {
            return;
        }
        for (Watched w : watched) {
            if (w.cidr().equals(cidr.get())) {
                w.tracker().withdraw(host, peer, at);
            } else if (w.cidr().strictlyContains(cidr.get())) {
                w.tracker().withdrawMoreSpecific(cidr.get().toString(), host, peer);
            }
        }
    }

    /** An AS path as space-separated ASNs, a nested array (an AS_SET) rendered as {@code {a,b}}. */
    static String path(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        StringBuilder out = new StringBuilder();
        for (JsonNode hop : node) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            if (hop.isArray()) {
                List<String> members = new ArrayList<>();
                hop.forEach(m -> members.add(m.asString()));
                out.append('{').append(String.join(",", members)).append('}');
            } else {
                out.append(hop.asString());
            }
        }
        return out.toString();
    }

    private void applyPeerState(JsonNode data) {
        if (data == null) {
            return;
        }
        Instant at = ctx.clock().instant();
        List<JsonNode> entries = new ArrayList<>();
        if (data.isArray()) {
            data.forEach(entries::add);
        } else {
            entries.add(data);
        }
        for (JsonNode entry : entries) {
            String host = entry.path("host").asString(null);
            String peer = entry.path("peer").asString(null);
            boolean down = "down".equalsIgnoreCase(entry.path("state").asString(""));
            for (Watched w : watched) {
                w.tracker().peerState(host, peer, down, at);
            }
        }
        emit(false);
    }

    /**
     * Emits each prefix whose banded state changed, or whose last emission is ten minutes old.
     *
     * @param disconnected true when the stream has failed: every prefix then reports {@code no-data}
     */
    synchronized void emit(boolean disconnected) {
        Instant now = ctx.clock().instant();
        for (Watched w : watched) {
            String prefix = w.cidr().toString();
            PrefixVisibilityTracker.Snapshot snapshot = w.tracker().snapshot(expectedOrigins(prefix), now,
                    disconnected);
            String band = snapshot.visibility() + "|" + snapshot.origins() + "|" + snapshot.unexpectedOrigin()
                    + "|" + snapshot.moreSpecifics() + "|" + snapshot.status();
            Instant last = lastEmit.get(prefix);
            boolean changed = !band.equals(lastBand.get(prefix));
            boolean due = last == null || !last.plus(RisLiveAdapter.EMIT_INTERVAL).isAfter(now);
            if (!changed && !due) {
                continue;
            }
            lastBand.put(prefix, band);
            lastEmit.put(prefix, now);
            try {
                sink.accept(record(prefix, snapshot, now));
            } catch (RuntimeException e) {
                log.debug("{}: the sink refused a record for {}", ctx.sourceId(), prefix, e);
            }
        }
    }

    private Set<Integer> expectedOrigins(String prefix) {
        List<Integer> asns = expected.get(prefix);
        return asns == null ? Set.of() : new LinkedHashSet<>(asns);
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
        watchdog = WorkThreads.start(WorkRole.COLLECT, "foresignal-ris-watchdog", () -> {
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

    /** One watchdog tick: meters bytes, recycles a silent connection, and emits any prefix due its ten-minute record. */
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
            return;
        }
        emit(false);
    }

    private void end(Throwable failure) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        endedWith = failure;
        stopWatchdog();
        reportBytes();
        if (!closedByUs) {
            // The monitor is down, not the prefixes: say so, keeping the last visibility.
            emit(true);
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
