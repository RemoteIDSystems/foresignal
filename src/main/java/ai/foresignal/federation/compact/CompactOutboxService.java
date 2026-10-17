package ai.foresignal.federation.compact;

import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.federation.PeerDescriptors;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.intel.IntelEvent;
import ai.foresignal.intel.IntelEventBus;
import ai.foresignal.intel.IntelListener;
import ai.foresignal.intel.IntelligenceEventRepository;
import ai.foresignal.notify.mesh.cbor.MeshCborMessage;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.stream.Subscription;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertListener;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Feeds the {@link CompactOutbox} (prompt 113) from the event buses: fired and resolved alerts ({@link AlertBus},
 * 017) and intelligence events ({@link IntelEventBus}, 101). Never records, never tracks.
 *
 * <p>Gates, before a message is queued: a suppressed alert, an alert whose source may not leave on
 * {@code FEDERATION} (private, {@code LOCAL_ONLY} or non-redistributable sources, through the one
 * {@link EgressPolicy}), an alert on a {@code peer-*} source (received data is never re-federated: the loop rule
 * of keys 10 and 11) and a private intelligence event are all left out.
 */
@Component
public class CompactOutboxService implements AlertListener, IntelListener, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(CompactOutboxService.class);

    /** The metric of queued messages. */
    public static final String QUEUED_METRIC = "foresignal.federation.compact.queued";

    private final AlertBus alerts;
    private final IntelEventBus intel;
    private final IntelligenceEventRepository events;
    private final FederationIdentity identity;
    private final EgressPolicy egress;
    private final ObjectProvider<MeterRegistry> meters;
    private final Clock clock;
    private final CompactOutbox outbox = new CompactOutbox();
    private Subscription alertSub;
    private Subscription intelSub;

    /** Creates the service. */
    public CompactOutboxService(AlertBus alerts, IntelEventBus intel, IntelligenceEventRepository events,
            FederationIdentity identity, EgressPolicy egress, ObjectProvider<MeterRegistry> meters, Clock clock) {
        this.alerts = alerts;
        this.intel = intel;
        this.events = events;
        this.identity = identity;
        this.egress = egress;
        this.meters = meters;
        this.clock = clock;
    }

    /** Subscribes to the buses once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void start() {
        if (alertSub == null) {
            alertSub = alerts.subscribe("federation-compact", this, AlertBus.DEFAULT_QUEUE);
        }
        if (intelSub == null) {
            intelSub = intel.subscribe("federation-compact", this, IntelEventBus.DEFAULT_QUEUE);
        }
    }

    /** The outbox. */
    public CompactOutbox outbox() {
        return outbox;
    }

    @Override
    public void onAlert(AlertEvent event) {
        try {
            if (event.suppressed() || !alertMayLeave(event.alert().sourceId())) {
                return;
            }
            MeshCborMessage message = CompactMessages.alert(event, identity.shortId(), identity.instanceId());
            if (message != null) {
                queue(message);
            }
        } catch (RuntimeException e) {
            log.warn("Could not queue the compact message of alert {}: {}", event.alert().id(), e.getMessage());
        }
    }

    @Override
    public void onEvent(IntelEvent event) {
        try {
            if (event.summary() == null || Boolean.TRUE.equals(event.summary().get("private"))) {
                return;
            }
            Geometry centroid = events.findById(event.eventId()).map(e -> (Geometry) e.getCentroid()).orElse(null);
            MeshCborMessage message = CompactMessages.intel(event.change(), event.summary(), centroid,
                    identity.shortId(), identity.instanceId(), clock.instant());
            if (message != null) {
                queue(message);
            }
        } catch (RuntimeException e) {
            log.warn("Could not queue the compact message of intelligence event {}: {}", event.eventId(),
                    e.getMessage());
        }
    }

    /** Queues {@code message} (tests and the bus listeners). */
    public boolean queue(MeshCborMessage message) {
        boolean queued = outbox.offer(message, clock.instant());
        if (queued) {
            MeterRegistry registry = meters == null ? null : meters.getIfAvailable();
            if (registry != null) {
                registry.counter(QUEUED_METRIC, "type", Integer.toString(message.type())).increment();
            }
        }
        return queued;
    }

    /** The pending messages queued at or after {@code since} (all when null), in priority order. */
    public List<CompactOutbox.Entry> pending(Instant since) {
        return outbox.since(since, clock.instant());
    }

    /** True when an alert on {@code sourceId} may become a compact message. */
    boolean alertMayLeave(String sourceId) {
        if (sourceId == null || PeerDescriptors.isPeerSource(sourceId)) {
            return false;
        }
        return egress.decide(EgressPath.FEDERATION, sourceId, null, null, EgressPolicy.EgressContext.NONE).allowed();
    }

    /** A bus dropped events: compact messages are best effort, so nothing is replayed. */
    @Override
    public void onGap() {
        log.debug("The compact outbox missed bus events; they are not replayed");
    }

    @Override
    public void resetState() {
        outbox.clear();
    }
}
