package ai.foresignal.source.adapters.mesh;

import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What one Meshtastic MQTT connection does with each message (prompt 083), shared by {@code meshtastic-mqtt} and
 * {@code meshtastic-local}:
 *
 * <ol>
 *   <li>the inbound cap, <em>before</em> parsing: a message over it is dropped and counted
 *       ({@value #DROPPED_METRIC}{@code {source,reason=rate}}), with a WARN at most once per 10 minutes;
 *   <li>a protobuf topic is dropped by its topic, never parsed ({@code reason=protobuf});
 *   <li>{@link MeshtasticJsonParser}; unknown types ({@code reason=unknown-type}), malformed bodies and other topics
 *       are counted and dropped;
 *   <li>the per-node merge (adapter rule 7) into one {@code mesh-node} snapshot, with the gateway as observer;
 *   <li>text: published to the {@link MeshMessageBus} when the source publishes (own node only), and kept as a
 *       {@code mesh-message} record only with {@code includeText}.
 * </ol>
 *
 * <p>Thread-safe: messages arrive on concurrent virtual threads.
 */
public final class MeshtasticStream {

    private static final Logger log = LoggerFactory.getLogger(MeshtasticStream.class);

    /** The counter of dropped messages. */
    public static final String DROPPED_METRIC = "foresignal.mesh.dropped";

    /** The counter of unknown JSON types. */
    public static final String UNKNOWN_TYPE_METRIC = "foresignal.mesh.unknown_types";

    /** The rate-limit WARN interval. */
    static final Duration WARN_INTERVAL = Duration.ofMinutes(10);

    private final String sourceId;
    private final boolean includeText;
    private final MeshMessageBus bus;
    private final SlidingWindowLimiter inbound;
    private final MeshNodeStates states;
    private final Supplier<MeterRegistry> meters;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Long> drops = new ConcurrentHashMap<>();
    private final AtomicReference<Instant> lastRateWarn = new AtomicReference<>();

    /**
     * Creates the stream state of one connection.
     *
     * @param sourceId the source
     * @param includeText whether text messages become records
     * @param bus where text is published, or null when this source does not publish (the public broker)
     * @param inbound the inbound cap
     * @param states the node merge state
     * @param meters the registry of the counters, or a supplier of null
     */
    public MeshtasticStream(String sourceId, boolean includeText, MeshMessageBus bus, SlidingWindowLimiter inbound,
            MeshNodeStates states, Supplier<MeterRegistry> meters) {
        this.sourceId = sourceId;
        this.includeText = includeText;
        this.bus = bus;
        this.inbound = inbound;
        this.states = states;
        this.meters = meters == null ? () -> null : meters;
    }

    /** The records of one message, possibly none. */
    public List<NormalizedRecord> onMessage(MqttInbound message, Instant receivedAt) {
        if (!inbound.tryAcquire(receivedAt)) {
            drop("rate");
            warnRate(receivedAt);
            return List.of();
        }
        if (MeshtasticJsonParser.isProtobufTopic(message.topic())) {
            drop("protobuf");
            return List.of();
        }
        MeshtasticJsonParser.Result result = MeshtasticJsonParser.parse(message.topic(), message.payload());
        if (result.packet() == null) {
            switch (result.drop()) {
                case UNKNOWN_TYPE -> {
                    drop("unknown-type");
                    count(UNKNOWN_TYPE_METRIC, "unknown-type", "Meshtastic JSON messages of unknown types");
                }
                case PROTOBUF_TOPIC -> drop("protobuf");
                case NOT_JSON_TOPIC -> drop("topic");
                default -> drop("malformed");
            }
            return List.of();
        }
        return map(result.packet(), receivedAt);
    }

    /** The records of one parsed packet (package-visible for tests). */
    List<NormalizedRecord> map(MeshtasticPacket packet, Instant receivedAt) {
        Instant observedAt = packet.timestamp() != null ? packet.timestamp() : receivedAt;
        Observer observer = new Observer(packet.gatewayId(), null, packet.rssi(), packet.snr(), null);
        List<NormalizedRecord> out = new ArrayList<>(1);
        switch (packet.payload()) {
            case MeshtasticPacket.NodeInfo info -> out.add(states.update(packet.from(), MeshRecords.MESHTASTIC, s ->
                    MeshRecords.node(s.identity(info.longName(), info.shortName(), MeshRecords.meshtasticRole(info.role()),
                            info.hardware() == null ? null : Integer.toString(info.hardware()), null, null)
                            .heard(observedAt), observedAt, observer)));
            case MeshtasticPacket.Position position -> out.add(states.update(packet.from(), MeshRecords.MESHTASTIC, s ->
                    MeshRecords.node(s.position(position.latitude(), position.longitude(), position.precisionBits())
                            .heard(observedAt), observedAt, observer)));
            case MeshtasticPacket.Telemetry telemetry -> out.add(states.update(packet.from(), MeshRecords.MESHTASTIC, s ->
                    MeshRecords.node(s.telemetry(telemetry.batteryLevel(), telemetry.voltage(), telemetry.channelUtil(),
                            telemetry.airUtilTx(), telemetry.uptimeS()).heard(observedAt), observedAt, observer)));
            case MeshtasticPacket.Text text -> {
                states.update(packet.from(), MeshRecords.MESHTASTIC, s -> s.heard(observedAt));
                boolean broadcast = packet.to() == null || MeshtasticPacket.BROADCAST.equals(packet.to());
                MeshTextMessage message = new MeshTextMessage(sourceId, MeshRecords.MESHTASTIC,
                        broadcast ? (packet.channel() == null ? 0 : packet.channel()) : null, packet.channelName(),
                        packet.from(), broadcast ? null : packet.to(), text.text(), receivedAt, packet.rssi(),
                        packet.snr());
                if (bus != null) {
                    bus.publish(message);
                }
                if (includeText) {
                    String id = packet.from() + ":" + (packet.packetId() != null ? Long.toString(packet.packetId())
                            : MeshRecords.hash(text.text(), String.valueOf(packet.timestamp())));
                    out.add(MeshRecords.message(message, id, observedAt));
                }
            }
        }
        return out;
    }

    /** Messages dropped so far, by reason (tests, diagnostics). */
    public Map<String, Long> drops() {
        return Map.copyOf(drops);
    }

    private void drop(String reason) {
        drops.merge(reason, 1L, Long::sum);
        count(DROPPED_METRIC, reason, "Meshtastic MQTT messages dropped before or while parsing");
    }

    private void count(String name, String reason, String description) {
        MeterRegistry registry = meters.get();
        if (registry == null) {
            return;
        }
        counters.computeIfAbsent(name + "|" + reason, k -> {
            Counter.Builder builder = Counter.builder(name).description(description).tag("source", sourceId);
            if (name.equals(DROPPED_METRIC)) {
                builder = builder.tag("reason", reason);
            }
            return builder.register(registry);
        }).increment();
    }

    private void warnRate(Instant now) {
        Instant last = lastRateWarn.get();
        if ((last == null || !now.isBefore(last.plus(WARN_INTERVAL))) && lastRateWarn.compareAndSet(last, now)) {
            log.warn("{}: more than {} MQTT messages in {}; dropping the excess before parsing", sourceId,
                    inbound.limit(), inbound.window());
        }
    }
}
