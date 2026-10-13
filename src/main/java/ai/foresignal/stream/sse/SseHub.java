package ai.foresignal.stream.sse;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.security.ApiTokenRevokedEvent;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Subscription;
import ai.foresignal.stream.Touch;
import jakarta.annotation.PreDestroy;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The SSE hub (Core-Data-Model §10.2, prompt 015): the registry of {@link EventTopic}s, the open
 * {@link SseClient}s, the stream limits and the fan-out from topics to clients.
 *
 * <p>Each topic subscribes to its own bus once, on {@code ApplicationReadyEvent}
 * ({@link StartupOrder#SSE_HUB}), and calls {@link #publish}, {@link #publishLive},
 * {@link #publishTouch}, {@link #gap} and {@link #liveDropped}; the hub hands each event to every
 * client subscribed to the topic, which filters and queues it. One {@code ChangeBus} and one
 * {@code LiveBus} subscription therefore serve every client, and change-before-live order is kept
 * per client because both are queued in arrival order.
 *
 * <p><b>Limits</b> ({@link StreamLimits}, app setting {@code stream.limits}): beyond
 * {@code maxClients} a connect is {@code 503 STREAM_LIMIT}; beyond {@code maxStreamsPerToken} (per
 * token principal) or {@code maxStreamsAnonymousPerAddress} (per anonymous address) it is
 * {@code 429 STREAM_LIMIT} with {@code Retry-After: 5}. Counts key on the
 * {@link StreamPrincipal#id()} from the {@link StreamPrincipalResolver}.
 */
@Component
public class SseHub {

    private static final Logger log = LoggerFactory.getLogger(SseHub.class);

    /** {@code Retry-After} on a per-principal limit. */
    static final String RETRY_AFTER_SECONDS = "5";

    /**
     * Random bytes behind a stream id: 17 bytes (136 bits) in base64url, truncated to the spec's 22
     * characters, which keep 132 random bits (at least the 129 the spec asks for).
     */
    static final int STREAM_ID_BYTES = 17;

    /** Characters of a stream id. */
    static final int STREAM_ID_LENGTH = 22;

    private final Map<String, EventTopic<?>> topics;
    private final Map<String, String> cursorKeys;
    private final AppSettingService settings;
    private final ChangeBus changeBus;
    /** Drives the stream schedule (live drains, heartbeats): monotonic, never the wall clock. */
    private final Clock clock = new MonotonicClock();
    private final JsonMapper json;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, SseClient> clients = new ConcurrentHashMap<>();
    private final Map<String, Integer> perPrincipal = new HashMap<>();
    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
    private volatile StreamLimits limits;

    /**
     * Creates the hub over every registered topic.
     *
     * @throws IllegalStateException when two topics share a name or a cursor key
     */
    public SseHub(
            List<EventTopic<?>> topics, AppSettingService settings, ChangeBus changeBus, JsonMapper json) {
        List<EventTopic<?>> ordered = new ArrayList<>(topics);
        ordered.sort(Comparator.comparingInt((EventTopic<?> t) -> t.cursorOrder()).thenComparing(EventTopic::name));
        Map<String, EventTopic<?>> byName = new LinkedHashMap<>();
        Map<String, String> keys = new LinkedHashMap<>();
        for (EventTopic<?> topic : ordered) {
            if (byName.put(topic.name(), topic) != null) {
                throw new IllegalStateException("Two event topics are named " + topic.name());
            }
            if (keys.containsValue(topic.cursorKey())) {
                throw new IllegalStateException("Two event topics use the cursor key " + topic.cursorKey());
            }
            keys.put(topic.name(), topic.cursorKey());
        }
        this.topics = Map.copyOf(byName);
        this.cursorKeys = Collections.unmodifiableMap(keys);
        this.settings = settings;
        this.changeBus = changeBus;
        this.json = json;
    }

    // ------------------------------------------------------------------------- lifecycle

    /** Attaches every topic to its bus. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.SSE_HUB)
    public void start() {
        if (!subscriptions.isEmpty()) {
            return;
        }
        for (EventTopic<?> topic : topics.values()) {
            subscriptions.add(topic.subscribe(this));
        }
        log.info("SSE hub started with topics {}", cursorKeys.keySet());
    }

    /** Closes the bus subscriptions and every open stream. */
    @PreDestroy
    public void stop() {
        for (Subscription subscription : subscriptions) {
            subscription.close();
        }
        subscriptions.clear();
        for (SseClient client : List.copyOf(clients.values())) {
            client.close();
        }
    }

    /** Re-reads {@code stream.limits} after it changes. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.STREAM_LIMITS.equals(event.key())) {
            limits = null;
        }
    }

    /** The current limits. */
    public StreamLimits limits() {
        StreamLimits current = limits;
        if (current == null) {
            current = settings.getOrDefault(SettingKeys.STREAM_LIMITS, StreamLimits.class, StreamLimits::defaults);
            limits = current;
        }
        return current;
    }

    // ------------------------------------------------------------------------- topics

    /** The topic named {@code name}, if registered. */
    public Optional<EventTopic<?>> topic(String name) {
        return Optional.ofNullable(topics.get(name));
    }

    /** Topic name to cursor key, in composite order. */
    public Map<String, String> cursorKeys() {
        return cursorKeys;
    }

    /** The change watermark ({@code ChangeBus.lastPublishedSeq()}). */
    public long watermark() {
        return changeBus.lastPublishedSeq();
    }

    // ------------------------------------------------------------------------- connect

    /**
     * What a connect asks for.
     *
     * @param principal who connects
     * @param mode how FIFO ids are written
     * @param filter the initial filter
     * @param resume topic to the resume cursor; a subscribed topic without one starts live from now
     * @param hello true to send {@code hello} first (the multiplexed endpoint)
     */
    public record OpenRequest(
            StreamPrincipal principal,
            SseClient.CursorMode mode,
            StreamFilter filter,
            Map<String, Long> resume,
            boolean hello) {}

    /**
     * Opens a stream: checks the limits, registers the client (before any replay, so nothing
     * published meanwhile is missed) and starts its sender.
     *
     * @param request the connect
     * @param sender where its frames go
     * @return the started client
     * @throws ForesignalException {@code 503} or {@code 429 STREAM_LIMIT}
     */
    public SseClient open(OpenRequest request, SseSender sender) {
        StreamLimits current = limits();
        StreamPrincipal principal = request.principal();
        Map<String, Long> initial = new LinkedHashMap<>();
        Map<String, ReplayRequest> replays = new LinkedHashMap<>();
        for (String name : request.filter().topics()) {
            EventTopic<?> topic = topics.get(name);
            Long resume = request.resume().get(name);
            if (resume != null) {
                initial.put(name, resume);
                replays.put(name, new ReplayRequest(resume, true));
            } else {
                // Live from now: read the head before registering, then replay the (usually empty)
                // range after it, so a batch published in between is neither lost nor duplicated.
                long head = topic.head();
                initial.put(name, head);
                replays.put(name, new ReplayRequest(head, false));
            }
        }
        String streamId = newStreamId();
        String streamKey = principal.anonymous() ? newStreamId() : null;
        Map<String, Object> hello = null;
        if (request.hello()) {
            hello = new LinkedHashMap<>();
            hello.put("streamId", streamId);
            hello.put("topics", request.filter().topics());
            hello.put("watermarkSeq", watermark());
            hello.put("filterVersion", request.filter().version());
            if (streamKey != null) {
                hello.put("streamKey", streamKey);
            }
        }
        SseClient.Settings clientSettings = new SseClient.Settings(
                streamId,
                principal,
                streamKey,
                request.mode(),
                cursorKeys,
                initial,
                replays,
                hello,
                current,
                clock,
                json,
                this::replay,
                this::loadPayloads,
                this::watermark,
                this::unregister);
        SseClient client = new SseClient(clientSettings, request.filter(), sender);
        register(client, current);
        client.start();
        return client;
    }

    private synchronized void register(SseClient client, StreamLimits current) {
        if (clients.size() >= current.maxClients()) {
            throw new ForesignalException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCodes.STREAM_LIMIT,
                    "The server already serves " + clients.size() + " streams (maxClients " + current.maxClients()
                            + "). Try again later.");
        }
        String id = client.principal().id();
        int open = perPrincipal.getOrDefault(id, 0);
        int allowed = current.perPrincipalLimit(client.principal());
        if (open >= allowed) {
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.STREAM_LIMIT,
                    "This caller already has " + open + " open streams (limit " + allowed + ").")
                    .withHeader(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS);
        }
        perPrincipal.put(id, open + 1);
        clients.put(client.streamId(), client);
    }

    private synchronized void unregister(SseClient client) {
        if (clients.remove(client.streamId(), client)) {
            perPrincipal.computeIfPresent(client.principal().id(), (k, v) -> v <= 1 ? null : v - 1);
        }
    }

    private String newStreamId() {
        byte[] bytes = new byte[STREAM_ID_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, STREAM_ID_LENGTH);
    }

    // ------------------------------------------------------------------------- revocation

    /**
     * Resets and closes every open stream of the revoked token, whether opened with the token itself or
     * with a ticket it minted (prompt 023), and of a revoked feed key (prompt 039) or tile key (prompt 050).
     */
    @EventListener
    public void onTokenRevoked(ApiTokenRevokedEvent event) {
        // A token (or a ticket it minted) and a feed key (prompt 035) are keyed differently; a revoked
        // grant loses its streams with a reset either way (prompt 039).
        int closed = revokePrincipal(StreamPrincipal.tokenId(event.tokenId()), "revoked")
                + revokePrincipal("feed:" + event.tokenId(), "revoked")
                + revokePrincipal("tilekey:" + event.tokenId(), "revoked");
        if (closed > 0) {
            log.info("Reset {} stream(s) of a revoked token or feed key", closed);
        }
    }

    /**
     * Sends {@code reset} with {@code reason} to every open stream of principal {@code principalId} and
     * closes them (prompt 039).
     *
     * @return how many were reset
     */
    public int revokePrincipal(String principalId, String reason) {
        int reset = 0;
        for (SseClient client : List.copyOf(clients.values())) {
            if (client.principal().id().equals(principalId)) {
                client.revoke(reason);
                reset++;
            }
        }
        return reset;
    }

    /**
     * Closes every open stream of principal {@code principalId}.
     *
     * @return how many were closed
     */
    public int closePrincipal(String principalId) {
        int closed = 0;
        for (SseClient client : List.copyOf(clients.values())) {
            if (client.principal().id().equals(principalId)) {
                client.close();
                closed++;
            }
        }
        return closed;
    }

    // ------------------------------------------------------------------------- ownership

    /**
     * The open stream {@code streamId} if {@code principal} owns it: the same principal id for a
     * token principal; for an anonymous stream, the matching {@code streamKey} (compared in
     * constant time). Anything else is empty, so ids cannot be probed.
     */
    public Optional<SseClient> owned(String streamId, StreamPrincipal principal, String streamKey) {
        SseClient client = streamId == null ? null : clients.get(streamId);
        if (client == null || client.isClosed()) {
            return Optional.empty();
        }
        if (client.principal().anonymous()) {
            String expected = client.streamKey();
            boolean match = expected != null && streamKey != null
                    && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), streamKey.getBytes(StandardCharsets.UTF_8));
            return match ? Optional.of(client) : Optional.empty();
        }
        return client.principal().id().equals(principal.id()) ? Optional.of(client) : Optional.empty();
    }

    // ------------------------------------------------------------------------- fan-out

    /**
     * Hands a FIFO event of {@code topic} to every subscribed client.
     *
     * @param topic the topic
     * @param render from a client's topic filter to its event, or null when it rejects the event
     */
    public void publish(String topic, Function<Object, FifoEvent> render) {
        for (SseClient client : clients.values()) {
            client.offerFifo(topic, render);
        }
    }

    /**
     * Hands an id-less control event to every client subscribed to {@code topic} whose topic filter
     * passes {@code test} (prompt 033: the inbox {@code badge} of one device).
     *
     * @return how many clients got it
     */
    public int publishControl(String topic, Predicate<Object> test, String event, Object data) {
        int sent = 0;
        for (SseClient client : clients.values()) {
            Object filter = client.filter().filterFor(topic);
            if (filter != null && test.test(filter)) {
                client.offerControl(event, data);
                sent++;
            }
        }
        return sent;
    }

    /** Hands a live update to every client's conflation lane. */
    public void publishLive(LiveUpdate update) {
        for (SseClient client : clients.values()) {
            client.offerLive(update);
        }
    }

    /** Hands a touch to every client's conflation lane. */
    public void publishTouch(Touch touch, Collection<String> possibleKinds) {
        for (SseClient client : clients.values()) {
            client.offerTouch(touch, possibleKinds);
        }
    }

    /**
     * A topic's bus dropped events: every subscribed client replays from its last cursor (the
     * resume path); for {@code changes}, clients with {@code live=true} also get
     * {@code resync {reason: gap}}, because live updates cannot be replayed.
     */
    public void gap(String topic) {
        for (SseClient client : clients.values()) {
            client.requestReplay(topic);
            if (ChangesTopic.NAME.equals(topic)) {
                ChangeFilter changes = client.filter().changes();
                if (changes != null && changes.live()) {
                    client.resync("gap");
                }
            }
        }
    }

    /** The live bus dropped batches: {@code resync {reason: live_dropped}} to every client with {@code live=true}. */
    public void liveDropped() {
        for (SseClient client : clients.values()) {
            ChangeFilter changes = client.filter().changes();
            if (changes != null && changes.live()) {
                client.resync("live_dropped");
            }
        }
    }

    /** True when any open client accepts a {@code changes} event per {@code test}. */
    public boolean anyChangesClient(Predicate<ChangeFilter> test) {
        for (SseClient client : clients.values()) {
            ChangeFilter changes = client.filter().changes();
            if (changes != null && test.test(changes)) {
                return true;
            }
        }
        return false;
    }

    private ReplayOutcome replay(String topic, ReplayRequest request, Object filter, ReplaySink sink) {
        EventTopic<?> registered = topics.get(topic);
        if (registered == null) {
            return new ReplayOutcome.Done(request.after());
        }
        return replayTyped(registered, request, filter, sink);
    }

    private static <F> ReplayOutcome replayTyped(EventTopic<F> topic, ReplayRequest request, Object filter, ReplaySink sink) {
        return topic.replay(request, topic.filterType().cast(filter), sink);
    }

    private Map<Long, String> loadPayloads(String topic, Collection<Long> seqs) {
        EventTopic<?> registered = topics.get(topic);
        return registered == null ? Map.of() : registered.loadPayloads(seqs);
    }

    // ------------------------------------------------------------------------- stats

    /**
     * Stream counts for {@code /v1/system.streams}.
     *
     * @param clients open streams
     * @param maxClients the server-wide limit
     * @param changeSubscribers open streams subscribed to {@code changes}
     * @param eventSubscribers open streams on {@code /v1/events/stream}
     * @param alertSubscribers open streams subscribed to {@code alerts} (prompt 019)
     */
    public record StreamStats(int clients, int maxClients, int changeSubscribers, int eventSubscribers,
            int alertSubscribers) {}

    /** The current counts. */
    public StreamStats stats() {
        int changes = 0;
        int events = 0;
        int alerts = 0;
        for (SseClient client : clients.values()) {
            if (client.filter().subscribes(ChangesTopic.NAME)) {
                changes++;
            }
            if (client.filter().subscribes(AlertsTopic.NAME)) {
                alerts++;
            }
            if (client.mode() == SseClient.CursorMode.COMPOSITE) {
                events++;
            }
        }
        return new StreamStats(clients.size(), limits().maxClients(), changes, events, alerts);
    }

    /** The open clients (for tests and stats). */
    public Collection<SseClient> clients() {
        return List.copyOf(clients.values());
    }
}
