package ai.foresignal.source.adapters.mastodon;

import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialRuntime;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.StreamLedger;
import ai.foresignal.social.TermIndex;
import ai.foresignal.source.formats.social.SocialJson;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.WebSocketOptions;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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
 * One {@code mastodon} run (prompt 135): a WebSocket per instance with hashtag subscriptions, plus a poller thread for
 * trends and followed accounts. A failed socket ends the run, so the supervisor reconnects every instance together.
 */
final class MastodonSession implements StreamHandle {

    /** How often the poller wakes. */
    static final Duration TICK = Duration.ofSeconds(30);

    /** The largest message accepted. */
    static final int MAX_MESSAGE_BYTES = 1024 * 1024;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final MastodonAdapter adapter;
    private final StreamContext ctx;
    private final RecordSink sink;
    private final SocialRuntime runtime;
    private final SocialCollector collector;
    private final StreamLedger ledger;
    private final List<String> instances;
    private final List<String> hashtags;
    private final Map<String, List<String>> accounts;
    private final Logger log;
    private final List<Consumer<Throwable>> closeCallbacks = new CopyOnWriteArrayList<>();
    private final List<WebSocket> sockets = new CopyOnWriteArrayList<>();
    private final AtomicBoolean finished = new AtomicBoolean();
    private volatile Throwable endedWith;
    private volatile boolean closedByUs;
    private volatile Thread poller;
    private volatile Instant lastEventAt;

    MastodonSession(MastodonAdapter adapter, StreamContext ctx, RecordSink sink, SocialRuntime runtime,
            List<String> instances, List<String> hashtags, Map<String, List<String>> accounts) {
        this.adapter = adapter;
        this.ctx = ctx;
        this.sink = sink;
        this.runtime = runtime;
        this.collector = runtime.collector();
        this.ledger = runtime.ledger();
        this.instances = List.copyOf(instances);
        this.hashtags = List.copyOf(hashtags);
        this.accounts = Map.copyOf(accounts);
        this.log = ctx.log();
    }

    /** Opens the sockets (when hashtags are followed) and starts the poller. */
    void open() throws AdapterException {
        if (!hashtags.isEmpty()) {
            for (String instance : instances) {
                String token = MastodonAdapter.token(ctx.credentials(), instance);
                if (token == null) {
                    log.warn("{}: no token for {}; its hashtag stream is skipped", ctx.sourceId(), instance);
                    continue;
                }
                URI uri = URI.create(streamingBase(instance) + "/api/v1/streaming");
                Listener listener = new Listener(instance);
                sockets.add(ctx.http().openWebSocket(uri, listener, Map.of("Authorization", "Bearer " + token),
                        WebSocketOptions.DEFAULT));
            }
        }
        if (ledger != null) {
            ledger.connected(ctx.sourceId(), ctx.clock().instant(), false);
        }
        ctx.connected();
        lastEventAt = ctx.clock().instant();
        poller = WorkThreads.start(WorkRole.COLLECT, "foresignal-mastodon-poller", () -> {
            while (!finished.get()) {
                try {
                    poll();
                } catch (RuntimeException e) {
                    log.debug("{}: poll failed: {}", ctx.sourceId(), e.toString());
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(TICK.toMillis());
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
    }

    /** The streaming base of an instance ({@code /api/v2/instance} {@code configuration.urls.streaming}). */
    String streamingBase(String instance) {
        if (adapter.overridden()) {
            return adapter.httpBase(instance).replaceFirst("^http", "ws");
        }
        try {
            UpstreamResponse r = ctx.http().get(URI.create(adapter.httpBase(instance) + "/api/v2/instance"),
                    Map.of("Accept", "application/json"));
            if (r.status() == 200) {
                String streaming = SocialJson.text(SocialJson.at(JSON.readTree(r.body()), "configuration", "urls"),
                        "streaming");
                if (streaming != null) {
                    return streaming.replaceAll("/+$", "");
                }
            }
        } catch (AdapterException | RuntimeException e) {
            log.debug("{}: no streaming URL from {}: {}", ctx.sourceId(), instance, e.toString());
        }
        return "wss://" + instance;
    }

    // ---------- polling ----------

    /** One poller pass: trends hourly per instance, followed accounts every 15 minutes. */
    void poll() {
        Instant now = ctx.clock().instant();
        for (String instance : instances) {
            String trendsKey = "trends." + instance;
            Instant last = parse(ctx.state().get(trendsKey).orElse(null));
            if (last == null || !last.isAfter(now.minus(MastodonAdapter.TRENDS_EVERY))) {
                try {
                    for (NormalizedRecord r : trends(instance, now)) {
                        sink.accept(r);
                    }
                    ctx.state().put(trendsKey, now.toString());
                } catch (AdapterException | RuntimeException e) {
                    log.debug("{}: trends of {} failed: {}", ctx.sourceId(), instance, e.toString());
                }
            }
            String accountsKey = "accounts." + instance;
            Instant lastAccounts = parse(ctx.state().get(accountsKey).orElse(null));
            if (accounts.containsKey(instance) && (lastAccounts == null
                    || !lastAccounts.isAfter(now.minus(Duration.ofMinutes(MastodonAdapter.ACCOUNT_POLL_MINUTES))))) {
                try {
                    for (String user : accounts.get(instance)) {
                        accountStatuses(instance, user);
                    }
                    ctx.state().put(accountsKey, now.toString());
                } catch (AdapterException | RuntimeException e) {
                    log.debug("{}: accounts of {} failed: {}", ctx.sourceId(), instance, e.toString());
                }
            }
        }
    }

    /**
     * The trending tags of an instance that match a tracked topic, as daily {@code series-point} records of their
     * {@code uses} (with {@code accounts} as an extra).
     */
    List<NormalizedRecord> trends(String instance, Instant now) throws AdapterException {
        UpstreamResponse r = ctx.http().get(URI.create(adapter.httpBase(instance) + "/api/v1/trends/tags?limit=20"),
                Map.of("Accept", "application/json"));
        if (r.status() != 200) {
            throw new AdapterException(ctx.sourceId() + ": trends of " + instance + " answered HTTP " + r.status());
        }
        JsonNode tags = JSON.readTree(r.body());
        List<NormalizedRecord> out = new ArrayList<>();
        if (!tags.isArray()) {
            return out;
        }
        TermIndex index = collector.index();
        Instant today = now.truncatedTo(ChronoUnit.DAYS);
        for (JsonNode tag : tags) {
            String name = SocialJson.text(tag, "name");
            if (name == null) {
                continue;
            }
            TermIndex.Match match = index.match(new TermIndex.Input(SocialVocabulary.MASTODON, null, List.of(name),
                    null, instance, SocialVocabulary.COLLECTION_STREAM));
            if (!match.matched()) {
                continue;
            }
            JsonNode history = tag.get("history");
            if (history == null || !history.isArray()) {
                continue;
            }
            String seriesId = "mastodon-trend:" + instance + ":" + name.toLowerCase(java.util.Locale.ROOT);
            for (JsonNode day : history) {
                Instant start = SocialJson.instant(SocialJson.text(day, "day"));
                Long uses = SocialJson.number(day, "uses");
                if (start == null || uses == null) {
                    continue;
                }
                Map<String, Object> extra = new LinkedHashMap<>();
                extra.put("accounts", SocialJson.number(day, "accounts"));
                NormalizedRecord point = SeriesPoint.point(seriesId, "#" + name + " on " + instance,
                        "mastodon-trends", "P1D", start.atZone(ZoneOffset.UTC).toLocalDate().toString(), start,
                        start.plus(Duration.ofDays(1)), uses.doubleValue(), "1", instance, extra,
                        start.isBefore(today) ? "final" : "preliminary", now);
                point = SeriesPoint.withPayload(point, "family", "attention");
                point = SeriesPoint.withPayload(point, "topicId", match.topicIds().get(0));
                out.add(point);
            }
        }
        return out;
    }

    /** Polls one followed account's new statuses. */
    void accountStatuses(String instance, String user) throws AdapterException {
        String token = MastodonAdapter.token(ctx.credentials(), instance);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        if (token != null) {
            headers.put("Authorization", "Bearer " + token);
        }
        String idKey = "acct." + instance + "/" + user;
        String accountId = ctx.state().get(idKey).orElse(null);
        if (accountId == null) {
            UpstreamResponse r = ctx.http().get(URI.create(adapter.httpBase(instance) + "/api/v1/accounts/lookup?acct="
                    + URLEncoder.encode(user, StandardCharsets.UTF_8)), headers);
            if (r.status() != 200) {
                return;
            }
            accountId = SocialJson.text(JSON.readTree(r.body()), "id");
            if (accountId == null) {
                return;
            }
            ctx.state().put(idKey, accountId);
        }
        String sinceKey = "since." + instance + "/" + accountId;
        String since = ctx.state().get(sinceKey).orElse(null);
        UpstreamResponse r = ctx.http().get(URI.create(adapter.httpBase(instance) + "/api/v1/accounts/" + accountId
                + "/statuses?limit=40" + (since == null ? "" : "&since_id=" + since)), headers);
        if (r.status() != 200) {
            return;
        }
        JsonNode statuses = JSON.readTree(r.body());
        String newest = since;
        if (statuses.isArray()) {
            for (JsonNode status : statuses) {
                String id = SocialJson.text(status, "id");
                if (id != null && (newest == null || compareIds(id, newest) > 0)) {
                    newest = id;
                }
                status(instance, status, SocialVocabulary.COLLECTION_SEARCH, SocialVocabulary.PAGED);
            }
        }
        if (newest != null) {
            ctx.state().put(sinceKey, newest);
        }
    }

    private static int compareIds(String a, String b) {
        if (a.length() != b.length()) {
            return Integer.compare(a.length(), b.length());
        }
        return a.compareTo(b);
    }

    // ---------- events ----------

    /** Handles one status: a boost counts for the boosted status; anything else is observed. */
    void status(String instance, JsonNode status, String collection, String collectionType) {
        JsonNode reblog = MastodonStatuses.reblog(status);
        if (reblog != null) {
            String id = SocialJson.text(reblog, "id");
            if (id != null) {
                collector.observeRepost(new SocialPost.PostRef(SocialVocabulary.MASTODON, instance + "/" + id,
                        SocialJson.text(reblog, "url")), SocialJson.instant(status, "created_at"), collectionType);
            }
            return;
        }
        SocialPost post = MastodonStatuses.post(instance, status, collection);
        if (post == null) {
            return;
        }
        SocialCollector.Observation obs = collector.observe(post, ctx.sourceId(), collectionType, false);
        if (obs.record() != null) {
            sink.accept(obs.record());
        }
    }

    /** Handles one streaming message; package-private for tests. */
    void handle(String instance, String json) {
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (RuntimeException e) {
            return;
        }
        lastEventAt = ctx.clock().instant();
        String event = SocialJson.text(root, "event");
        String payload = SocialJson.text(root, "payload");
        if (event == null || payload == null) {
            return;
        }
        try {
            switch (event) {
                case "update", "status.update" -> status(instance, JSON.readTree(payload),
                        SocialVocabulary.COLLECTION_STREAM, SocialVocabulary.COMPLETE);
                case "delete" -> {
                    if (runtime.settings(MastodonAdapter.ID).deletionSync()) {
                        NormalizedRecord r = collector.observeDelete(ctx.sourceId(), SocialVocabulary.MASTODON,
                                instance + "/" + payload.strip());
                        if (r != null) {
                            sink.accept(r);
                        }
                    }
                }
                default -> {
                    // notifications and other events are not followed
                }
            }
        } catch (RuntimeException e) {
            log.debug("{}: dropping a message it could not map: {}", ctx.sourceId(), e.toString());
        }
    }

    /** The WebSocket listener of one instance. */
    final class Listener implements WebSocket.Listener {

        private final String instance;
        private final StringBuilder text = new StringBuilder();
        private boolean discarding;

        Listener(String instance) {
            this.instance = instance;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            for (String tag : hashtags) {
                webSocket.sendText("{\"type\":\"subscribe\",\"stream\":\"hashtag\",\"tag\":\""
                        + tag.replace("\\", "").replace("\"", "") + "\"}", true);
            }
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            ctx.reportBytes(data.length());
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
                    handle(instance, message);
                }
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onPing(WebSocket webSocket, java.nio.ByteBuffer message) {
            lastEventAt = ctx.clock().instant();
            return WebSocket.Listener.super.onPing(webSocket, message);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            end(new AdapterException(instance + " closed the stream (" + statusCode + ")"));
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            end(new AdapterException(instance + " stream failed: " + error.getMessage(), error));
        }
    }

    // ---------- StreamHandle ----------

    @Override
    public void close() {
        closedByUs = true;
        finished.set(true);
        for (WebSocket ws : sockets) {
            try {
                ws.abort();
            } catch (RuntimeException e) {
                log.debug("{}: aborting a socket failed", ctx.sourceId(), e);
            }
        }
        Thread t = poller;
        if (t != null) {
            t.interrupt();
        }
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

    private void end(Throwable failure) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        endedWith = failure;
        for (WebSocket ws : sockets) {
            try {
                ws.abort();
            } catch (RuntimeException e) {
                log.debug("{}: aborting a socket failed", ctx.sourceId(), e);
            }
        }
        Thread t = poller;
        if (t != null && t != Thread.currentThread()) {
            t.interrupt();
        }
        if (ledger != null && !closedByUs) {
            ledger.disconnected(ctx.sourceId(), ctx.clock().instant(), failure == null ? null : failure.getMessage());
        }
        for (Consumer<Throwable> callback : closeCallbacks) {
            try {
                callback.accept(failure);
            } catch (RuntimeException e) {
                log.debug("{}: a close callback failed", ctx.sourceId(), e);
            }
        }
    }

    private static Instant parse(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(s.strip());
        } catch (RuntimeException e) {
            return null;
        }
    }
}
