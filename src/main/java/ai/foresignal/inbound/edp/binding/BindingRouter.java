package ai.foresignal.inbound.edp.binding;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.RecordPredicate;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The binding router (prompt 138): applies every enabled external binding to the EDP records that match it.
 *
 * <p>A {@link ChangeBus} subscriber with the durable cursor {@code external.binding.cursor} (a retention consumer
 * through {@link BindingCursorConsumer}, so version retention never outruns it). It catches up in chunks of
 * {@value #CHUNK} seqs on its own virtual thread, reading the versions of providers that have enabled bindings. A
 * version belongs to a stream when its payload carries that stream's EDP provenance and its kind is the stream's
 * kind; records the bindings themselves wrote (converted social posts and articles) carry none and are never routed.
 * Each binding whose filter matches is applied through {@link BindingApplier}, at most once per record version.
 *
 * <p>Atomic items do nothing until bound: nothing here runs for a stream without an enabled binding.
 */
@Component
public class BindingRouter implements ChangeListener, DisposableBean, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(BindingRouter.class);

    /** The change-bus subscriber name. */
    public static final String SUBSCRIBER = "external-binding-router";

    /** The seq chunk processed per pass. */
    static final long CHUNK = 10_000;

    private final ChangeBus changeBus;
    private final AppSettingService settings;
    private final BindingRecords records;
    private final ExternalBindingRepository bindings;
    private final BindingStreams streams;
    private final BindingFilter filters;
    private final BindingApplier applier;
    private final Clock clock;
    private final boolean autoStart;

    private volatile Subscription subscription;
    private volatile Thread thread;
    private volatile boolean running;
    private volatile long cursor = -1;

    /** Creates the router. */
    public BindingRouter(ChangeBus changeBus, AppSettingService settings, BindingRecords records,
            ExternalBindingRepository bindings, BindingStreams streams, BindingFilter filters, BindingApplier applier,
            Clock clock, @Value("${foresignal.external.bindings.auto-start:true}") boolean autoStart) {
        this.changeBus = changeBus;
        this.settings = settings;
        this.records = records;
        this.bindings = bindings;
        this.streams = streams;
        this.filters = filters;
        this.applier = applier;
        this.clock = clock;
        this.autoStart = autoStart;
    }

    /** True when the router runs in the background (otherwise callers drive {@link #catchUp()}). */
    public boolean autoStart() {
        return autoStart;
    }

    /** Starts the subscriber and the catch-up thread once the application is ready (unless disabled). */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.INTEL)
    public synchronized void start() {
        if (running || !autoStart) {
            return;
        }
        running = true;
        subscription = changeBus.subscribe(SUBSCRIBER, this, 1024);
        thread = WorkThreads.start(WorkRole.COLLECT, "external-binding-router", this::loop);
    }

    /** Stops the router. */
    @Override
    public synchronized void destroy() {
        running = false;
        if (subscription != null) {
            subscription.close();
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onBatch(ChangeBatch batch) {
        // The catch-up loop advances the cursor.
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        // The loop catches up from the durable cursor.
    }

    /** True while the background loop runs. */
    public boolean running() {
        return running;
    }

    /** The durable cursor. */
    public long cursor() {
        if (cursor < 0) {
            cursor = settings.get(SettingKeys.EXTERNAL_BINDING_CURSOR, Long.class).orElse(0L);
        }
        return cursor;
    }

    /** Drops the cached cursor so it is read again (after the tables were emptied). */
    @Override
    public void resetState() {
        cursor = -1;
    }

    /** Moves the durable cursor to {@code seq} (tests, and a restart simulated in place). */
    public synchronized void rewind(long seq) {
        storeCursor(seq);
    }

    private void loop() {
        while (running) {
            try {
                catchUp();
            } catch (RuntimeException e) {
                log.warn("External binding catch-up failed: {}", e.toString());
            }
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                if (!running) {
                    return;
                }
            }
        }
    }

    /**
     * One routable binding with its stream and compiled filter.
     *
     * @param binding the binding
     * @param stream its stream
     * @param filter its filter
     */
    record Route(ExternalBindingEntity binding, BoundStream stream, RecordPredicate filter) {}

    /** The routable bindings by provider and stream. */
    Map<String, Map<String, List<Route>>> routes() {
        Map<String, Map<String, List<Route>>> out = new LinkedHashMap<>();
        Map<String, Optional<BoundStream>> cache = new HashMap<>();
        for (ExternalBindingEntity b : bindings.findByEnabledTrueOrderByCreatedAtAscIdAsc()) {
            if (!b.routable()) {
                continue;
            }
            Optional<BoundStream> stream = cache.computeIfAbsent(b.getSourceId() + "\u0001" + b.getStreamId(),
                    k -> streams.stream(b));
            if (stream.isEmpty()) {
                continue;
            }
            RecordPredicate predicate;
            try {
                predicate = filters.compile(b.getFilter(), b.getSourceId());
            } catch (RuntimeException e) {
                log.warn("Binding {} has an unusable filter and is skipped: {}", b.getId(), e.toString());
                continue;
            }
            out.computeIfAbsent(b.getSourceId(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(b.getStreamId(), k -> new ArrayList<>())
                    .add(new Route(b, stream.get(), predicate));
        }
        return out;
    }

    /** Applies the enabled bindings to every version between the cursor and the published watermark. */
    public synchronized void catchUp() {
        long watermark = changeBus.lastPublishedSeq();
        long from = cursor();
        if (from > watermark) {
            // The history and record_seq were reset (a restore, or the test cleaner): start again.
            from = 0;
            storeCursor(from);
        }
        if (from >= watermark) {
            return;
        }
        Map<String, Map<String, List<Route>>> routes = routes();
        Set<String> sources = new LinkedHashSet<>(routes.keySet());
        while (from < watermark) {
            long to = Math.min(from + CHUNK, watermark);
            for (BoundItem item : records.versions(sources, from, to)) {
                route(item, routes.getOrDefault(item.sourceId(), Map.of()));
            }
            from = to;
            storeCursor(from);
        }
    }

    /** Routes one version to the bindings of its stream. */
    void route(BoundItem item, Map<String, List<Route>> byStream) {
        if (!item.edpItem()) {
            return;
        }
        List<Route> routes = byStream.get(item.streamId());
        if (routes == null) {
            return;
        }
        for (Route route : routes) {
            if (!route.stream().kind().equals(item.kind()) || !BindingFilter.matches(route.filter(), item)) {
                continue;
            }
            applier.apply(route.binding().getId(), route.stream(), item);
        }
    }

    /**
     * Backfills a binding from stored records within retention: the newest {@value BindingVocabulary#BACKFILL_CAP}
     * versions of its stream at or below the router's cursor (the router handles everything after it), oldest first.
     *
     * @return how many versions were applied
     */
    public int backfill(String bindingId) {
        ExternalBindingEntity binding = bindings.findById(bindingId).orElse(null);
        if (binding == null) {
            return 0;
        }
        Optional<BoundStream> stream = streams.stream(binding);
        int n = 0;
        if (stream.isPresent() && binding.routable()) {
            RecordPredicate predicate = filters.compile(binding.getFilter(), binding.getSourceId());
            long maxSeq = Math.max(cursor(), 0);
            for (BoundItem item : records.recent(binding.getSourceId(), stream.get().kind(), binding.getStreamId(),
                    BindingVocabulary.BACKFILL_CAP, maxSeq)) {
                if (!item.edpItem() || !BindingFilter.matches(predicate, item)) {
                    continue;
                }
                BindingEffect effect = applier.apply(bindingId, stream.get(), item);
                if (effect != null && !effect.skipped() && !effect.failed()) {
                    n++;
                }
            }
        }
        bindings.findById(bindingId).ifPresent(b -> {
            if (BindingVocabulary.BACKFILLING.equals(b.getState())) {
                b.setState(BindingVocabulary.ACTIVE);
                b.setUpdatedAt(clock.instant());
                bindings.save(b);
            }
        });
        return n;
    }

    private void storeCursor(long seq) {
        cursor = seq;
        settings.put(SettingKeys.EXTERNAL_BINDING_CURSOR, seq);
    }
}
