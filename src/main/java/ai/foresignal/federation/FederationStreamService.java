package ai.foresignal.federation;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import ai.foresignal.stream.Bbox;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.LiveBatch;
import ai.foresignal.stream.LiveBus;
import ai.foresignal.stream.LiveListener;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Subscription;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code GET /v1/federation/stream} (prompt 113): the federation feed as server-sent events with the 015 lanes.
 *
 * <ul>
 *   <li>{@code change}: one full {@code FederationItem}, {@code id:} = seq; replayed from {@code after} (or
 *       {@code Last-Event-ID}) through {@link FederationFeedService#changes} pages, then woken by the
 *       {@link ChangeBus}, so the replay→live switch neither duplicates nor skips (SSE-1's rule, reading by seq).
 *   <li>{@code live} (only with {@code live=true}): a conflated live update of a source whose records may leave
 *       ({@code EgressPolicy} on {@code FEDERATION}, without a per-record license, so peer records never go out
 *       live); a source that reports {@code posSource} needs it in the update (MLAT never leaves).
 *   <li>{@code heartbeat} every {@value #HEARTBEAT_SECONDS} s; {@code reset} ({@code resync_required}) when the
 *       cursor falls below the purge horizon, after which the stream closes.
 * </ul>
 *
 * <p>Per-record egress, restrictions and the exercise rule are exactly the change feed's.
 *
 * <p>Left out in edge mode (prompt 113: federation serving).
 */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class FederationStreamService {

    /** Seconds between heartbeats. */
    public static final int HEARTBEAT_SECONDS = 15;

    private final FederationFeedService feed;
    private final ChangeBus changes;
    private final LiveBus live;
    private final EgressPolicy egress;
    private final SourceRegistry registry;
    private final JsonMapper json;
    private final int maxStreams;
    private final AtomicInteger open = new AtomicInteger();
    private final AtomicLong ids = new AtomicLong();

    /** Creates the service. */
    public FederationStreamService(FederationFeedService feed, ChangeBus changes, LiveBus live, EgressPolicy egress,
            SourceRegistry registry, JsonMapper json,
            @Value("${foresignal.federation.max-streams:16}") int maxStreams) {
        this.feed = feed;
        this.changes = changes;
        this.live = live;
        this.egress = egress;
        this.registry = registry;
        this.json = json;
        this.maxStreams = Math.max(1, maxStreams);
    }

    /**
     * Opens a stream.
     *
     * @throws ForesignalException {@code 429 STREAM_LIMIT} when too many federation streams are open,
     *     {@code 410 RESYNC_REQUIRED} when {@code after} is already below the purge horizon
     */
    public SseEmitter open(long after, boolean withLive, List<String> sources, List<String> kinds, BboxFilter bbox,
            FederationFeedService.Caller caller) {
        // Fails before the first byte when the cursor is below the horizon (410).
        FederationFeedService.Page first = feed.changes(after, FederationFeedService.MAX_LIMIT, sources, kinds, bbox,
                caller);
        if (open.incrementAndGet() > maxStreams) {
            open.decrementAndGet();
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.STREAM_LIMIT,
                    "Too many federation streams are open; retry later.").withHeader("Retry-After", "30");
        }
        SseEmitter emitter = new SseEmitter(0L);
        Stream stream = new Stream(emitter, after, withLive, sources, kinds, bbox, caller, first);
        emitter.onCompletion(stream::stop);
        emitter.onTimeout(stream::stop);
        emitter.onError(e -> stream.stop());
        Thread.ofVirtual().name("foresignal-federation-stream-" + ids.incrementAndGet()).start(stream);
        return emitter;
    }

    /** The number of open federation streams. */
    public int openStreams() {
        return open.get();
    }

    /** One client's stream. */
    private final class Stream implements Runnable, ChangeListener, LiveListener {

        private final SseEmitter emitter;
        private final boolean withLive;
        private final List<String> sources;
        private final List<String> kinds;
        private final BboxFilter bbox;
        private final FederationFeedService.Caller caller;
        private final Semaphore wake = new Semaphore(0);
        private final ConcurrentLinkedQueue<LiveUpdate> liveQueue = new ConcurrentLinkedQueue<>();
        private final AtomicBoolean stopped = new AtomicBoolean();
        private FederationFeedService.Page pending;
        private long cursor;
        private Subscription changeSub;
        private Subscription liveSub;

        Stream(SseEmitter emitter, long after, boolean withLive, List<String> sources, List<String> kinds,
                BboxFilter bbox, FederationFeedService.Caller caller, FederationFeedService.Page first) {
            this.emitter = emitter;
            this.cursor = after;
            this.withLive = withLive;
            this.sources = sources;
            this.kinds = kinds;
            this.bbox = bbox;
            this.caller = caller;
            this.pending = first;
        }

        @Override
        public void run() {
            try {
                changeSub = changes.subscribe("federation-stream", this, 64);
                if (withLive) {
                    liveSub = live.subscribe("federation-live", this, 256);
                }
                synchronized (emitter) {
                    emitter.send(SseEmitter.event().comment("open"));
                }
                while (!stopped.get()) {
                    FederationFeedService.Page page;
                    if (pending != null) {
                        page = pending;
                        pending = null;
                    } else {
                        try {
                            page = feed.changes(cursor, FederationFeedService.MAX_LIMIT, sources, kinds, bbox, caller);
                        } catch (ForesignalException e) {
                            if (ErrorCodes.RESYNC_REQUIRED.equals(e.getCode())) {
                                Map<String, Object> reset = new LinkedHashMap<>();
                                reset.put("reason", "resync_required");
                                reset.put("resumeAfter", null);
                                reset.put("watermarkSeq", feed.watermark());
                                send("reset", null, reset);
                                return;
                            }
                            throw e;
                        }
                    }
                    for (Map<String, Object> item : page.items()) {
                        send("change", String.valueOf(item.get("seq")), item);
                    }
                    cursor = Math.max(cursor, page.lastSeq());
                    drainLive();
                    if (!page.hasMore()) {
                        boolean woken = wake.tryAcquire(HEARTBEAT_SECONDS, TimeUnit.SECONDS);
                        wake.drainPermits();
                        if (!woken && liveQueue.isEmpty()) {
                            send("heartbeat", null, Map.of());
                        }
                    }
                }
            } catch (IOException | IllegalStateException e) {
                // the client went away
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                emitter.completeWithError(e);
                stop();
                return;
            } finally {
                stop();
            }
            emitter.complete();
        }

        private void drainLive() throws IOException {
            LiveUpdate update;
            Map<String, LiveUpdate> conflated = new LinkedHashMap<>();
            while ((update = liveQueue.poll()) != null) {
                conflated.put(update.sourceId() + "\u0000" + update.externalId(), update);
            }
            for (LiveUpdate u : conflated.values()) {
                if (allowed(u)) {
                    send("live", null, liveEvent(u));
                }
            }
        }

        private boolean allowed(LiveUpdate u) {
            if (!sources.isEmpty() && !sources.contains(u.sourceId())) {
                return false;
            }
            if (!kinds.isEmpty() && !kinds.contains(u.kind())) {
                return false;
            }
            if (!registry.contains(u.sourceId())) {
                return false;
            }
            Map<String, Object> properties = u.properties() == null ? Map.of() : u.properties();
            if (!properties.containsKey(EgressPolicy.POS_SOURCE)
                    && registry.descriptor(u.sourceId()).fieldSpecs().containsKey(EgressPolicy.POS_SOURCE)) {
                return false;
            }
            if (!egress.decide(EgressPath.FEDERATION, u.sourceId(), null, properties,
                    EgressPolicy.EgressContext.federation(caller.sameOperator())).allowed()) {
                return false;
            }
            if (u.geometry() != null && bbox != null && !bbox.envelopes().isEmpty()
                    && bbox.envelopes().stream().noneMatch(e -> e.intersects(u.geometry().getEnvelopeInternal()))) {
                return false;
            }
            return RestrictionClamp.test(caller.restriction(), u.sourceId(), u.kind(), u.geometry());
        }

        private Map<String, Object> liveEvent(LiveUpdate u) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("sourceId", u.sourceId());
            out.put("externalId", u.externalId());
            out.put("kind", u.kind());
            out.put("observedAt", u.observedAt() == null ? null : u.observedAt().toString());
            out.put("validUntil", u.validUntil() == null ? null : u.validUntil().toString());
            out.put("version", u.version());
            out.put("geometry", u.geometry() == null ? null : new GeoJsonWriter().write(u.geometry()));
            out.put("bbox", box(u.bbox()));
            out.put("prevBbox", box(u.prevBbox()));
            out.put("properties", u.properties() == null ? Map.of() : u.properties());
            out.put("quality", u.quality());
            return out;
        }

        private void send(String event, String id, Object data) throws IOException {
            if (stopped.get()) {
                throw new IOException("stopped");
            }
            SseEmitter.SseEventBuilder builder = SseEmitter.event().name(event).data(json.writeValueAsString(data));
            if (id != null) {
                builder.id(id);
            }
            synchronized (emitter) {
                emitter.send(builder);
            }
        }

        @Override
        public void onBatch(ChangeBatch batch) {
            wake.release();
        }

        @Override
        public void onGap(long lastDeliveredSeq) {
            wake.release();
        }

        @Override
        public void onBatch(LiveBatch batch) {
            if (batch != null && !stopped.get()) {
                liveQueue.addAll(batch.updates());
                wake.release();
            }
        }

        @Override
        public void onLiveDropped() {
            // Live state is conflated; the next updates replace what was lost.
        }

        void stop() {
            if (stopped.compareAndSet(false, true)) {
                open.decrementAndGet();
                if (changeSub != null) {
                    changeSub.close();
                }
                if (liveSub != null) {
                    liveSub.close();
                }
                wake.release();
            }
        }
    }

    private static List<Double> box(Bbox box) {
        return box == null ? null : List.of(box.minX(), box.minY(), box.maxX(), box.maxY());
    }
}
