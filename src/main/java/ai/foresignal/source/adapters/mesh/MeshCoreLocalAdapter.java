package ai.foresignal.source.adapters.mesh;

import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code meshcore-local} (prompt 083): the operator's own MeshCore companion radio running the WiFi companion firmware,
 * over its native TCP protocol.
 *
 * <ul>
 *   <li><strong>Endpoint:</strong> {@code endpoint}, a required {@code tcp://host:port} LAN URL. No port is assumed:
 *       the firmware listens on 5000 only when built without {@code TCP_PORT}.
 *   <li><strong>Link:</strong> the shared {@link MeshCoreCompanionLink} of that {@code host:port} ({@link MeshCoreLinks}),
 *       because the companion accepts one TCP client; the link owns reconnecting, the command queue and the startup
 *       sequence, and writes only allowlisted commands ({@code MeshCoreCommandGuard}).
 *   <li><strong>Records:</strong> {@link MeshCoreEventMapper}: contacts and the companion itself as {@code mesh-node}
 *       records; repeater health ({@code advertStatus}) from advert ages, re-evaluated every 15 minutes and emitted
 *       only on change. Receive-only health: no request is ever sent to a repeater.
 *   <li><strong>Messages:</strong> every channel and contact message goes to the {@link MeshMessageBus}; a
 *       {@code mesh-message} record only with {@code includeText}.
 *   <li>{@code observedAtMode} RECEIVED (node clocks drift); <strong>private</strong>.
 * </ul>
 *
 * <p>USB and BLE companions, or companions without the WiFi firmware, use the bridge script
 * ({@code contrib/meshcore-bridge}) and the {@code meshcore-bridge} source instead.
 */
@Component
public class MeshCoreLocalAdapter implements StreamingAdapter {

    private static final Logger log = LoggerFactory.getLogger(MeshCoreLocalAdapter.class);

    /** The source id. */
    public static final String ID = "meshcore-local";

    /** The endpoint parameter. */
    public static final String ENDPOINT = "endpoint";

    /** The repeater late-age parameter. */
    public static final String REPEATER_LATE_AFTER = "repeaterLateAfter";

    /** The repeater silent-age parameter. */
    public static final String REPEATER_SILENT_AFTER = "repeaterSilentAfter";

    private final MeshCoreLinks links;
    private final MeshMessageBus bus;
    private final Duration reevaluateEvery;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter for Spring.
     *
     * @param links the shared companion links
     * @param bus where every text message is published
     */
    @Autowired
    public MeshCoreLocalAdapter(MeshCoreLinks links, MeshMessageBus bus) {
        this(links, bus, RepeaterHealth.REEVALUATE_EVERY);
    }

    /**
     * Creates the adapter with another re-evaluation period (tests).
     *
     * @param links the shared companion links
     * @param bus the mesh message bus
     * @param reevaluateEvery how often repeater health is re-evaluated
     */
    public MeshCoreLocalAdapter(MeshCoreLinks links, MeshMessageBus bus, Duration reevaluateEvery) {
        this.links = links;
        this.bus = bus;
        this.reevaluateEvery = reevaluateEvery;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Checks that the repeater thresholds are ordered. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        try {
            health(params);
        } catch (IllegalArgumentException e) {
            problems.put(REPEATER_SILENT_AFTER, e.getMessage());
        }
        return problems;
    }

    /** The repeater thresholds of {@code params}. */
    static RepeaterHealth health(ValidatedParams params) {
        return new RepeaterHealth(params.duration(REPEATER_LATE_AFTER), params.duration(REPEATER_SILENT_AFTER));
    }

    /**
     * Attaches to the companion's link and streams records until the handle is closed.
     *
     * @throws AdapterException when the parameters are invalid
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        ValidatedParams params = ctx.params();
        RepeaterHealth health;
        try {
            health = health(params);
        } catch (IllegalArgumentException e) {
            throw new AdapterException(ID + ": " + e.getMessage());
        }
        LanEndpoint endpoint = LanEndpoint.fromParams(params, ENDPOINT);
        if (endpoint.scheme() != LanEndpoint.Scheme.TCP) {
            throw new AdapterException(ID + ": endpoint must be tcp://host:port");
        }
        MeshCoreEventMapper mapper = new MeshCoreEventMapper(ctx.sourceId(), params.bool(MeshRecords.INCLUDE_TEXT, false),
                health);
        MeshCoreCompanionLink link = links.link(endpoint);
        Handle handle = new Handle(link, ctx.sourceId());
        link.attach(ctx.sourceId(), ctx.sockets(), new MeshCoreCompanionLink.Listener() {
            @Override
            public void onFrame(byte[] frame, Instant receivedAt) {
                MeshCoreEventMapper.Mapped mapped = mapper.onFrame(frame, receivedAt);
                if (!mapped.records().isEmpty()) {
                    sink.acceptAll(mapped.records());
                }
                for (MeshTextMessage message : mapped.messages()) {
                    bus.publish(message);
                }
            }

            @Override
            public void onConnected() {
                ctx.connected();
            }
        });
        WorkThreads.start(WorkRole.COLLECT, "meshcore-repeaters-" + ctx.sourceId(), () -> {
            while (handle.isRunning()) {
                try {
                    Thread.sleep(reevaluateEvery.toMillis());
                } catch (InterruptedException e) {
                    return;
                }
                if (handle.isRunning()) {
                    try {
                        sink.acceptAll(mapper.reevaluate(ctx.clock().instant()));
                    } catch (RuntimeException e) {
                        log.warn("{}: repeater re-evaluation failed", ctx.sourceId(), e);
                    }
                }
            }
        });
        return handle;
    }

    /** The source's attachment to a link: closing it detaches; the link reconnects on its own meanwhile. */
    static final class Handle implements StreamHandle {

        private final MeshCoreCompanionLink link;
        private final String owner;
        private final AtomicBoolean closed = new AtomicBoolean();

        Handle(MeshCoreCompanionLink link, String owner) {
            this.link = link;
            this.owner = owner;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                link.detach(owner);
            }
        }

        @Override
        public boolean isRunning() {
            return !closed.get();
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Own MeshCore companion (TCP)")
                .description("The operator's own MeshCore companion radio over the WiFi companion firmware's TCP"
                        + " protocol: its contacts (chat nodes, repeaters, rooms, sensors) and itself as mesh-node"
                        + " records, repeater health from advert ages (ok, late, silent; receive-only), and channel and"
                        + " direct messages on the in-process mesh message bus (records only with includeText). Only"
                        + " allowlisted read commands are ever sent. The companion accepts one TCP client, shared with"
                        + " the meshcore channel. USB and BLE companions use the bridge and meshcore-bridge instead."
                        + " Private.")
                .kinds(MeshRecords.NODE_KIND, MeshRecords.MESSAGE_KIND)
                .homepage("https://github.com/meshcore-dev/MeshCore")
                .attribution("The operator's own MeshCore companion")
                .licence("Operator's own data (no upstream license)")
                .mode(Mode.STREAMING)
                .defaultBudget(Budget.of(1, 1_440))
                .reconnect(MeshCoreCompanionLink.RECONNECT)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .zeroResultsNormal(true)
                .coverageFromObservers(true)
                .privateSource(true)
                .param(ParamSpec.lanUrl(ENDPOINT, "The companion: tcp://host:port on the LAN (explicit port; the"
                        + " firmware uses 5000 only when built without TCP_PORT)", true, "tcp"))
                .param(ParamSpec.optional(MeshRecords.INCLUDE_TEXT, ParamType.BOOLEAN, "Keep channel and direct"
                        + " messages as mesh-message records (never embedded, never sent to an LLM)", false))
                .param(ParamSpec.optional(REPEATER_LATE_AFTER, ParamType.DURATION, "A repeater whose last advert is"
                        + " older is late", "13h"))
                .param(ParamSpec.optional(REPEATER_SILENT_AFTER, ParamType.DURATION, "A repeater whose last advert is"
                        + " older is silent", "26h"));
        MeshRecords.defaults(b, MeshRecords.TTL, MeshRecords.STREAM_KEYFRAME);
        return b.build();
    }
}
