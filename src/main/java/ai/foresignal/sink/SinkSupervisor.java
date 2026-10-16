package ai.foresignal.sink;

import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.provenance.LicenceResolver;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.ChangesQuery;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveBatch;
import ai.foresignal.stream.LiveBus;
import ai.foresignal.stream.LiveListener;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Subscription;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * One virtual thread per enabled sink (prompt 110): opens the type, snapshots current state or tails the
 * change feed, gates every record through {@link SinkRecordGate} and persists the cursor. Restarts with
 * backoff and raises a {@code system} notification after 10 minutes failing.
 */
@Component
public class SinkSupervisor {

    private static final Logger log = LoggerFactory.getLogger(SinkSupervisor.class);

    /** The restart backoff cap. */
    static final Duration MAX_BACKOFF = Duration.ofMinutes(5);

    /** Failing long enough to notify. */
    static final Duration NOTIFY_AFTER = Duration.ofMinutes(10);

    /** The change page size. */
    static final int PAGE = 500;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final SinkRepository sinks;
    private final SinkCursorRepository cursors;
    private final SinkTypeRegistry types;
    private final SinkRecordGate gate;
    private final CredentialCipher cipher;
    private final RecordQueryService queries;
    private final ChangeBus changeBus;
    private final LiveBus liveBus;
    private final LicenceResolver licences;
    private final LicenceCatalog catalog;
    private final SourceRegistry registry;
    private final OutboundUrlPolicy policy;
    private final ApplicationEventPublisher events;
    private final MeterRegistry meters;
    private final Map<String, Run> runs = new ConcurrentHashMap<>();

    /** Creates the supervisor. */
    public SinkSupervisor(SinkRepository sinks, SinkCursorRepository cursors, SinkTypeRegistry types,
            SinkRecordGate gate, CredentialCipher cipher, RecordQueryService queries, ChangeBus changeBus,
            LiveBus liveBus, LicenceResolver licences, LicenceCatalog catalog, SourceRegistry registry,
            OutboundUrlPolicy policy, ApplicationEventPublisher events, MeterRegistry meters) {
        this.sinks = sinks;
        this.cursors = cursors;
        this.types = types;
        this.gate = gate;
        this.cipher = cipher;
        this.queries = queries;
        this.changeBus = changeBus;
        this.liveBus = liveBus;
        this.licences = licences;
        this.catalog = catalog;
        this.registry = registry;
        this.policy = policy;
        this.events = events;
        this.meters = meters;
    }

    /** Starts every enabled sink at startup. */
    @EventListener(ApplicationReadyEvent.class)
    public void startEnabled() {
        for (SinkEntity sink : sinks.findByEnabledTrueOrderByIdAsc()) {
            start(sink.getId());
        }
    }

    /** Starts (or restarts) a sink. */
    public void start(String id) {
        stop(id);
        SinkEntity sink = sinks.findById(id).orElse(null);
        if (sink == null || !sink.isEnabled()) {
            return;
        }
        Run run = new Run(sink);
        runs.put(id, run);
        run.thread.start();
    }

    /** Restarts a sink if enabled, else stops it. */
    public void restart(String id) {
        SinkEntity sink = sinks.findById(id).orElse(null);
        if (sink == null || !sink.isEnabled()) {
            stop(id);
        } else {
            start(id);
        }
    }

    /** Stops a sink. */
    public void stop(String id) {
        Run run = runs.remove(id);
        if (run != null) {
            run.closed.set(true);
            run.close();
        }
    }

    /** Forces a resync. */
    public void resync(String id) {
        Run run = runs.get(id);
        if (run != null) {
            run.resync.set(true);
        }
    }

    /** Stops every sink at shutdown. */
    @EventListener(ContextClosedEvent.class)
    public void stopAll() {
        for (String id : List.copyOf(runs.keySet())) {
            stop(id);
        }
    }

    /** The number of running sinks. */
    public int running() {
        return runs.size();
    }

    /** The run of a sink, or null. */
    public Run run(String id) {
        return runs.get(id);
    }

    /** The number of failing sinks. */
    public int failing() {
        int failing = 0;
        for (Run run : runs.values()) {
            if (run.failingSince != null) {
                failing++;
            }
        }
        return failing;
    }

    /** The number of connected clients over all SERVE sinks. */
    public int clients() {
        int total = 0;
        for (Run run : runs.values()) {
            SinkInstance instance = run.instance;
            if (instance != null) {
                total += instance.clients();
            }
        }
        return total;
    }

    /** A running sink. */
    public final class Run {
        private final SinkEntity sink;
        private final AtomicBoolean closed = new AtomicBoolean();
        private final AtomicBoolean resync = new AtomicBoolean();
        private final Thread thread;
        private volatile SinkInstance instance;
        private volatile SinkMode mode = SinkMode.SERVE;
        private volatile boolean localTarget;
        private Subscription changeSub;
        private Subscription liveSub;
        private volatile Instant failingSince;

        Run(SinkEntity sink) {
            this.sink = sink;
            this.thread = Thread.ofVirtual().name("sink-" + sink.getId()).unstarted(this::loop);
        }

        /** The running instance, or null before it opens. */
        public SinkInstance instance() {
            return instance;
        }

        void close() {
            closeQuietly(changeSub);
            closeQuietly(liveSub);
            SinkInstance current = instance;
            if (current != null) {
                try {
                    current.close();
                } catch (RuntimeException e) {
                    log.warn("Closing sink {} failed", sink.getId(), e);
                }
            }
            SinkCursorEntity cursor = cursor(sink.getId());
            cursor.setState(SinkCursorEntity.STOPPED);
            save(cursor);
        }

        private void loop() {
            Duration backoff = Duration.ofSeconds(5);
            while (!closed.get()) {
                try {
                    openAndRun();
                    return;
                } catch (RuntimeException e) {
                    markFailure(e);
                    if (closed.get()) {
                        return;
                    }
                    sleep(backoff);
                    backoff = min(backoff.multipliedBy(2), MAX_BACKOFF);
                }
            }
        }

        private void openAndRun() {
            SinkType type = types.get(sink.getType());
            mode = resolveMode(type, sink);
            localTarget = localTarget(sink, mode);
            SinkFilter filter = config(sink, mode).filter();
            instance = type.open(config(sink, mode));
            SinkCursorEntity cursor = cursor(sink.getId());
            cursor.setState(SinkCursorEntity.RUNNING);
            cursor.setFailingSince(null);
            cursor.setLastError(null);
            cursor.setWatermarkSeq(changeBus.lastPublishedSeq());
            save(cursor);
            failingSince = null;
            if (type.spec().kind() == SinkKind.CHANGE_FEED) {
                subscribeChanges(filter);
                drain(cursor, filter);
            } else {
                snapshot(filter);
                subscribeLive(filter);
                subscribeChanges(filter);
            }
        }

        private void snapshot(SinkFilter filter) {
            CurrentQuery query = new CurrentQuery(filter.sources(), filter.kinds(), List.of(), null, null, null, null,
                    TimeField.EFFECTIVE, null, false, ListOrder.ID, null, null, PAGE, FieldSelection.ALL,
                    GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false, Set.of(), ReadRestriction.NONE);
            Slices.RecordSlice slice = queries.list(query);
            for (RecordRow row : slice.rows()) {
                deliverRow(row, filter, false);
            }
        }

        private void subscribeLive(SinkFilter filter) {
            liveSub = liveBus.subscribe("sink:" + sink.getId(), new LiveListener() {
                @Override
                public void onBatch(LiveBatch batch) {
                    for (LiveUpdate update : batch.updates()) {
                        deliverLive(update, filter);
                    }
                }

                @Override
                public void onLiveDropped() {
                    resync.set(true);
                }
            }, 1 << 14);
        }

        private void subscribeChanges(SinkFilter filter) {
            changeSub = changeBus.subscribe("sink:" + sink.getId(), new ChangeListener() {
                @Override
                public void onBatch(ChangeBatch batch) {
                    if (types.get(sink.getType()).spec().kind() == SinkKind.CHANGE_FEED) {
                        drain(cursor(sink.getId()), filter);
                        return;
                    }
                    for (ChangeSummary change : batch.changes()) {
                        if (change.tombstone()) {
                            SinkRecordGate.Decision decision = gate.allow(mode, localTarget, filter.includePrivate(),
                                    stub(change));
                            if (!decision.allowed()) {
                                denied(sink.getId(), decision.reason(), change.sourceId());
                                continue;
                            }
                            SinkInstance current = instance;
                            if (current != null) {
                                current.remove(change.sourceId(), change.externalId());
                            }
                        }
                    }
                }

                @Override
                public void onGap(long lastDeliveredSeq) {
                    resync.set(true);
                }
            }, 1 << 16);
        }

        private void drain(SinkCursorEntity cursor, SinkFilter filter) {
            boolean progressed = true;
            while (!closed.get() && progressed) {
                long after = cursor.getLastSeq();
                long horizon = queries.purgeHorizon(filter.sources());
                if (after < horizon) {
                    cursor.setState(SinkCursorEntity.RESYNCING);
                    after = horizon;
                    cursor.setLastSeq(after);
                    cursor.setResyncCount(cursor.getResyncCount() + 1);
                }
                Slices.ChangeSlice slice = queries.changes(changesQuery(after, filter));
                for (RecordRow row : slice.rows()) {
                    if (row.seq() <= cursor.getLastSeq()) {
                        continue;
                    }
                    if (!deliverRow(row, filter, true)) {
                        save(cursor);
                        return;
                    }
                    cursor.setLastSeq(row.seq());
                    cursor.setLastDeliveredAt(Instant.now());
                    cursor.setDelivered(cursor.getDelivered() + 1);
                    cursor.setState(SinkCursorEntity.RUNNING);
                }
                save(cursor);
                progressed = slice.hasMore();
            }
        }

        private boolean deliverRow(RecordRow row, SinkFilter filter, boolean feed) {
            LicenceResolver.Resolution resolution = licences.resolve(row);
            SinkRecord record = new SinkRecord(row.sourceId(), row.externalId(), row.kind(), row.seq(), row.version(),
                    row.observedAt(), row.validUntil(), row.tombstone(), parseGeometry(row.geometry()),
                    payload(row.payload()), resolution.terms(), attribution(row.sourceId()));
            return deliver(record, filter, feed ? Delivery.FEED : Delivery.MIRROR);
        }

        private void deliverLive(LiveUpdate update, SinkFilter filter) {
            LicenceTermsHolder terms = new LicenceTermsHolder(catalog.forDescriptor(descriptor(update.sourceId())));
            SinkRecord record = new SinkRecord(update.sourceId(), update.externalId(), update.kind(), 0L,
                    update.version(), update.observedAt(), update.validUntil(), false, update.geometry(),
                    update.properties() == null ? Map.of() : update.properties(), terms.terms(),
                    attribution(update.sourceId()));
            deliver(record, filter, Delivery.MIRROR);
        }

        private boolean deliver(SinkRecord record, SinkFilter filter, Delivery delivery) {
            SinkInstance current = instance;
            if (current == null) {
                return false;
            }
            if (!filter.matches(record)) {
                return true;
            }
            SinkRecordGate.Decision decision = gate.allow(mode, localTarget, filter.includePrivate(), record);
            if (!decision.allowed()) {
                denied(sink.getId(), decision.reason(), record.sourceId());
                return delivery != Delivery.FEED;
            }
            meters.counter("foresignal.sink.delivered", "sink", sink.getId(), "type", sink.getType()).increment();
            return switch (delivery) {
                case FEED -> current.deliver(record);
                case MIRROR -> {
                    if (record.tombstone()) {
                        current.remove(record.sourceId(), record.externalId());
                    } else {
                        current.upsert(record);
                    }
                    yield true;
                }
            };
        }

        private SinkRecord stub(ChangeSummary change) {
            return new SinkRecord(change.sourceId(), change.externalId(), change.kind(), change.seq(), 0, null, null,
                    true, null, Map.of(), catalog.forDescriptor(descriptor(change.sourceId())),
                    attribution(change.sourceId()));
        }

        private void markFailure(RuntimeException e) {
            SinkCursorEntity cursor = cursor(sink.getId());
            cursor.setState(SinkCursorEntity.FAILING);
            cursor.setLastError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            if (failingSince == null) {
                failingSince = Instant.now();
                cursor.setFailingSince(failingSince);
            }
            save(cursor);
            meters.counter("foresignal.sink.denied", "sink", sink.getId(), "reason", "error").increment();
            if (failingSince != null && Duration.between(failingSince, Instant.now()).compareTo(NOTIFY_AFTER) >= 0
                    && cursor.getResyncCount() == 0) {
                events.publishEvent(new SystemEvent("system.sink_failing", "warning",
                        "Sink " + sink.getName() + " is failing", cursor.getLastError(), "/v1/system",
                        Map.of("sinkId", sink.getId())));
            }
        }

        private void sleep(Duration duration) {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** The delivery semantics. */
    private enum Delivery {
        FEED, MIRROR
    }

    private record LicenceTermsHolder(ai.foresignal.provenance.LicenceTerms terms) {}

    private static void closeQuietly(Subscription subscription) {
        if (subscription != null) {
            subscription.close();
        }
    }

    private static Duration min(Duration a, Duration b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private ChangesQuery changesQuery(long after, SinkFilter filter) {
        return new ChangesQuery(after, filter.sources(), filter.kinds(), List.of(), null, true, PAGE,
                FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, Set.of(), null, false,
                ReadRestriction.NONE);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> payload(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return JSON.readValue(json, Map.class);
        } catch (RuntimeException e) {
            return Map.of();
        }
    }

    private static org.locationtech.jts.geom.Geometry parseGeometry(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return new ai.foresignal.source.formats.geojson.GeoJsonReader().read(json);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private ai.foresignal.source.spi.SourceDescriptor descriptor(String sourceId) {
        return sourceId != null && registry.contains(sourceId) ? registry.descriptor(sourceId) : null;
    }

    private String attribution(String sourceId) {
        var descriptor = descriptor(sourceId);
        return descriptor == null ? null : descriptor.attribution();
    }

    private SinkMode resolveMode(SinkType type, SinkEntity sink) {
        String configured = String.valueOf(sink.getConfig().getOrDefault("mode", ""));
        if ("SERVE".equalsIgnoreCase(configured) && type.spec().modes().contains(SinkMode.SERVE)) {
            return SinkMode.SERVE;
        }
        if ("PUSH".equalsIgnoreCase(configured) && type.spec().modes().contains(SinkMode.PUSH)) {
            return SinkMode.PUSH;
        }
        return type.spec().modes().contains(SinkMode.PUSH) ? SinkMode.PUSH : SinkMode.SERVE;
    }

    private boolean localTarget(SinkEntity sink, SinkMode mode) {
        if (mode == SinkMode.SERVE) {
            return true;
        }
        String host = host(sink);
        return host != null && policy.checkHost(host, OutboundUrlPolicy.Profile.LAN_ONLY).allowed();
    }

    private String host(SinkEntity sink) {
        for (String key : List.of("brokerUrl", "url", "host")) {
            Object value = sink.getConfig().get(key);
            if (value != null) {
                try {
                    java.net.URI uri = java.net.URI.create(String.valueOf(value));
                    return uri.getHost() != null ? uri.getHost() : String.valueOf(value);
                } catch (IllegalArgumentException e) {
                    return String.valueOf(value);
                }
            }
        }
        return null;
    }

    private SinkConfig config(SinkEntity sink, SinkMode mode) {
        Map<String, String> secrets = sink.getSecretCiphertext() == null ? Map.of()
                : new LinkedHashMap<>(cipher.decryptToMap(
                        new Encrypted(sink.getSecretCiphertext(), sink.getSecretNonce())));
        SinkFilter filter = SinkFilter.parse(JSON.valueToTree(sink.getFilter() == null ? Map.of() : sink.getFilter()));
        return new SinkConfig(sink.getId(), sink.getType(), sink.getName(), mode,
                new LinkedHashMap<>(sink.getConfig()), secrets, filter);
    }

    @Transactional
    SinkCursorEntity cursor(String sinkId) {
        return cursors.findById(sinkId).orElseGet(() -> cursors.save(new SinkCursorEntity(sinkId)));
    }

    @Transactional
    void save(SinkCursorEntity cursor) {
        cursors.save(cursor);
    }

    void denied(String sinkId, String reason, String sourceId) {
        meters.counter("foresignal.sink.denied", "sink", sinkId, "reason", reason).increment();
        SinkCursorEntity cursor = cursor(sinkId);
        Map<String, Object> denied = new LinkedHashMap<>(cursor.getDenied());
        denied.merge("total", 1L, (a, b) -> ((Number) a).longValue() + 1);
        @SuppressWarnings("unchecked")
        Map<String, Object> byReason = denied.get("byReason") instanceof Map<?, ?> m
                ? new LinkedHashMap<>((Map<String, Object>) m) : new LinkedHashMap<>();
        byReason.merge(reason, 1L, (a, b) -> ((Number) a).longValue() + 1);
        denied.put("byReason", byReason);
        @SuppressWarnings("unchecked")
        Map<String, Object> bySource = denied.get("bySource") instanceof Map<?, ?> m
                ? new LinkedHashMap<>((Map<String, Object>) m) : new LinkedHashMap<>();
        if (sourceId != null) {
            bySource.merge(sourceId, 1L, (a, b) -> ((Number) a).longValue() + 1);
        }
        denied.put("bySource", bySource);
        cursor.setDenied(denied);
        save(cursor);
    }
}
