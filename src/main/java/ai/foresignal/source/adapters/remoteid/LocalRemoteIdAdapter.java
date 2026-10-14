package ai.foresignal.source.adapters.remoteid;

import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttEndpoint;
import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.mqtt.MqttSessionOptions;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.LocalAddresses;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code local-remoteid} (prompt 086): drone Remote ID (ASTM F3411 / ASD-STAN) heard by the operator's own WarDragon
 * kit, read from DragonSync's MQTT output on the operator's own broker through 060's {@link MqttConnections}.
 *
 * <ul>
 *   <li><strong>Params:</strong> {@code brokerUrl}, a {@code LAN_URL} {@code mqtt://} or {@code mqtts://} with an
 *       explicit port (083's extension); {@code topics} (default {@value #DEFAULT_TOPIC}, DragonSync's aggregate drone
 *       topic, which carries {@code seen_by}); {@code keepOperatorPosition} (default false). Optional
 *       {@code username}/{@code password} credentials.
 *   <li><strong>Connection:</strong> a non-reconnecting subscriber session; one budget unit per CONNECT and per
 *       SUBSCRIBE; the supervisor reconnects 5 s to 5 min.
 *   <li><strong>Privacy, enforced in code:</strong> DJI DroneID is dropped and counted
 *       ({@value #DROPPED_METRIC}{@code {reason=dji}}); pilot and home positions are stripped before the record is
 *       built ({@link RemoteIdMapper}). {@code keepOperatorPosition} is accepted only while the source's effective
 *       privacy (prompt 039) is on: {@code PUT /v1/sources/local-remoteid} refuses it otherwise with 400
 *       {@code PRIVATE_REQUIRED} ({@link #paramsRequiringPrivacy}), and the pilot position is then stored as payload
 *       {@code operatorPoint}, never geometry.
 *   <li><strong>Records:</strong> kind {@value RemoteIdMapper#KIND}, externalId {@code rid:<id>}, TRACK, SAMPLED
 *       {5 s, keyframe 60 s, 25 m, edge 2 s}, TTL 3 min, significant {@code idType, uaType, operatorId}, motion
 *       {@code speed} m/s and {@code course}; the kit is the observer with RSSI and frequency.
 *   <li><strong>Egress:</strong> disabled by default, {@code LOCAL_ONLY}, overridable.
 * </ul>
 */
@Component
public class LocalRemoteIdAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = "local-remoteid";

    /** The broker parameter. */
    public static final String BROKER_URL = "brokerUrl";

    /** The topics parameter. */
    public static final String TOPICS = "topics";

    /** The operator position parameter. */
    public static final String KEEP_OPERATOR_POSITION = "keepOperatorPosition";

    /** DragonSync's aggregate drone topic (docs/mqtt-schema.md, verified 2026-10-13). */
    public static final String DEFAULT_TOPIC = "wardragon/drones";

    /** The user name credential. */
    public static final String USERNAME = "username";

    /** The password credential. */
    public static final String PASSWORD = "password";

    /** The counter of dropped messages, tagged by reason. */
    public static final String DROPPED_METRIC = "foresignal.remoteid.dropped";

    /** The most topics. */
    public static final int MAX_TOPICS = 4;

    /** The record TTL. */
    static final Duration TTL = Duration.ofMinutes(3);

    /** The keep-alive of the MQTT session. */
    static final Duration KEEPALIVE = Duration.ofSeconds(60);

    /** The reconnect ladder. */
    static final ReconnectPolicy RECONNECT = new ReconnectPolicy(Duration.ofSeconds(5), Duration.ofMinutes(5), 0.2);

    /** The connection key in {@link MqttConnections}. */
    static final String CONNECTION_KEY = "source:" + ID;

    /** An MQTT topic filter: plain segments, {@code +} segments and a final {@code #}. */
    static final Pattern TOPIC = Pattern.compile("^(?:[A-Za-z0-9_.-]+|\\+)(?:/(?:[A-Za-z0-9_.-]+|\\+))*(?:/#)?$|^#$");

    private final MqttConnections connections;
    private final Supplier<String> installId;
    private final Supplier<MeterRegistry> meters;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<RemoteIdMapper.Drop, Long> drops = new ConcurrentHashMap<>();
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter for Spring.
     *
     * @param connections the shared MQTT connections (prompt 060)
     * @param install the install id of the client id
     * @param meters the registry of the drop counter, resolved lazily
     */
    @Autowired
    public LocalRemoteIdAdapter(MqttConnections connections, InstallIdService install,
            ObjectProvider<MeterRegistry> meters) {
        this(connections, install::installId,
                meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter from plain parts (tests).
     *
     * @param connections the MQTT connections
     * @param installId supplies the install id
     * @param meters supplies the registry, or null
     */
    public LocalRemoteIdAdapter(MqttConnections connections, Supplier<String> installId, Supplier<MeterRegistry> meters) {
        this.connections = connections;
        this.installId = installId;
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Checks the topic filters. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        List<String> topics = topics(params);
        if (topics.size() > MAX_TOPICS) {
            problems.put(TOPICS, "at most " + MAX_TOPICS + " topics");
        }
        for (String topic : topics) {
            if (!TOPIC.matcher(topic).matches()) {
                problems.put(TOPICS, "'" + topic + "' is not an MQTT topic filter (plain segments, + and a final #)");
                break;
            }
        }
        return problems;
    }

    /**
     * {@code keepOperatorPosition=true} needs the source's effective privacy (prompt 039): the API refuses it with 400
     * {@code PRIVATE_REQUIRED} on a source that is not private.
     */
    @Override
    public List<String> paramsRequiringPrivacy(Map<String, Object> params) {
        if (params == null) {
            return List.of();
        }
        Object keep = params.get(KEEP_OPERATOR_POSITION);
        return Boolean.TRUE.equals(keep) || "true".equalsIgnoreCase(String.valueOf(keep))
                ? List.of(KEEP_OPERATOR_POSITION) : List.of();
    }

    /** The subscriptions: the parameter, else {@value #DEFAULT_TOPIC}. */
    static List<String> topics(ValidatedParams params) {
        List<String> given = params.stringList(TOPICS);
        List<String> out = new ArrayList<>();
        if (given != null) {
            for (String t : given) {
                if (t != null && !t.isBlank() && !out.contains(t.strip())) {
                    out.add(t.strip());
                }
            }
        }
        return out.isEmpty() ? List.of(DEFAULT_TOPIC) : out;
    }

    /**
     * The broker endpoint: the pinned address for {@code mqtt://}; the host name for {@code mqtts://}, once it still
     * resolves only to private addresses (083's rule).
     *
     * @throws AdapterException when an {@code mqtts://} host now resolves to a non-private address
     */
    static MqttEndpoint endpoint(LanEndpoint lan) throws AdapterException {
        if (lan.scheme() == LanEndpoint.Scheme.MQTTS) {
            try {
                for (InetAddress address : InetAddress.getAllByName(lan.host())) {
                    if (!LocalAddresses.isPrivate(address)) {
                        throw new AdapterException(ID + ": " + lan.host() + " now resolves to "
                                + address.getHostAddress() + ", which is not a private address");
                    }
                }
            } catch (UnknownHostException e) {
                throw new AdapterException(ID + ": " + lan.host() + " does not resolve", e);
            }
            return new MqttEndpoint("mqtts", lan.host(), lan.port(), true, false, "");
        }
        if (lan.scheme() != LanEndpoint.Scheme.MQTT) {
            throw new AdapterException(ID + ": brokerUrl must be mqtt:// or mqtts://");
        }
        return new MqttEndpoint("mqtt", lan.address().getHostAddress(), lan.port(), false, false, "");
    }

    /**
     * Connects to the operator's broker and streams records until the handle is closed.
     *
     * @throws AdapterException when the parameters are invalid, the budget is spent, or the broker cannot be reached
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        ValidatedParams params = ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ID + ": " + problems);
        }
        LanEndpoint lan = LanEndpoint.fromParams(params, BROKER_URL);
        Map<String, String> credentials = ctx.credentials() == null ? Map.of() : ctx.credentials();
        MqttConnectionSpec spec = new MqttConnectionSpec(CONNECTION_KEY, ID, "foresignal-" + installId.get() + "-rid",
                endpoint(lan), MqttConnectionSpec.V311, blankToNull(credentials.get(USERNAME)),
                blankToNull(credentials.get(PASSWORD)), null, null, MqttSessionOptions.subscriber(KEEPALIVE));
        boolean keep = params.bool(KEEP_OPERATOR_POSITION, false);
        return RemoteIdMqttSubscriber.open(ctx, connections, spec, topics(params),
                message -> sink.acceptAll(onMessage(message, keep)));
    }

    /** The records of one message, counting drops (package-visible for tests). */
    List<NormalizedRecord> onMessage(MqttInbound message, boolean keepOperatorPosition) {
        RemoteIdMapper.Result result = RemoteIdMapper.map(message.topic(), message.payload(), keepOperatorPosition);
        if (result.record() == null) {
            count(result.drop());
            return List.of();
        }
        return List.of(result.record());
    }

    /** Messages dropped for {@code reason} since start, for tests and health. */
    public long dropped(RemoteIdMapper.Drop reason) {
        return drops.getOrDefault(reason, 0L);
    }

    private void count(RemoteIdMapper.Drop reason) {
        drops.merge(reason, 1L, Long::sum);
        MeterRegistry registry = meters.get();
        if (registry == null) {
            return;
        }
        counters.computeIfAbsent(reason.tag(), tag -> Counter.builder(DROPPED_METRIC)
                .description("Remote ID messages dropped, by reason (dji: DJI DroneID is never ingested)")
                .tag("source", ID).tag("reason", tag).register(registry)).increment();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = RemoteIdMapper.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Local drone Remote ID (DragonSync)")
                .description("ASTM F3411 / ASD-STAN drone Remote ID heard by the operator's own WarDragon kit, read"
                        + " from DragonSync's MQTT output on the operator's broker (mqtt:// or mqtts:// on the LAN,"
                        + " explicit port; default topic wardragon/drones). DJI DroneID detections are never ingested."
                        + " Pilot and home positions are stripped; keepOperatorPosition keeps the pilot position as"
                        + " payload operatorPoint only while the source is private. Disabled by default; egress"
                        + " LOCAL_ONLY (the operator may set it to OPEN).")
                .kind(kind)
                .homepage("https://github.com/alphafox02/DragonSync")
                .attribution("The operator's own Remote ID receiver (WarDragon, DragonSync)")
                .licence("Operator's own data (no upstream license)")
                .mode(Mode.STREAMING)
                .defaultBudget(Budget.of(10, 1_000))
                .reconnect(RECONNECT)
                .historyTier(HistoryTier.TRACK)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(TTL)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .defaultIngestPolicy(new IngestPolicy.Sampled(Duration.ofSeconds(5), Duration.ofSeconds(60), 25.0, false,
                        Duration.ofSeconds(2)))
                .significantFields("idType", "uaType", "operatorId")
                .motion(new MotionSpec("speed", MotionSpec.SpeedUnit.METERS_PER_SECOND, "course"))
                .zeroResultsNormal(true)
                .coverageFromObservers(true)
                .egress(Egress.LOCAL_ONLY)
                .egressOverridable(true)
                .titleField("operatorId")
                .searchFields("operatorId", "idType")
                .credential(CredentialField.optional(USERNAME, "The broker user name, if the broker requires one"))
                .credential(CredentialField.optional(PASSWORD, "The broker password, if the broker requires one"))
                .param(ParamSpec.lanUrl(BROKER_URL, "The broker DragonSync publishes to: mqtt://host:port or"
                        + " mqtts://host:port on the LAN, explicit port, private address", true, "mqtt", "mqtts"))
                .param(ParamSpec.optional(TOPICS, ParamType.STRING_LIST, "Topic filters to subscribe to (at most "
                        + MAX_TOPICS + "); only drone state topics (<root>/drones, <root>/drone/<id>) are read",
                        List.of(DEFAULT_TOPIC)))
                .param(ParamSpec.optional(KEEP_OPERATOR_POSITION, ParamType.BOOLEAN, "Keep the pilot position as payload"
                        + " operatorPoint (never geometry); accepted only while the source is private (400"
                        + " PRIVATE_REQUIRED otherwise)", false));
        Set<String> kinds = Set.of(kind);
        b.fieldSpec("alt", new FieldSpec(FieldType.NUMBER, "m", "Altitude", "Geodetic altitude (HAE) in meters",
                "WGS84 ellipsoid", kinds));
        b.fieldSpec("agl", new FieldSpec(FieldType.NUMBER, "m", "Height", "Height above ground or takeoff in meters",
                null, kinds));
        b.fieldSpec("speed", new FieldSpec(FieldType.NUMBER, "m/s", "Speed", "Ground speed", null, kinds));
        b.fieldSpec("vspeed", new FieldSpec(FieldType.NUMBER, "m/s", "Vertical speed", "Climb rate, negative when"
                + " descending", null, kinds));
        b.fieldSpec("course", new FieldSpec(FieldType.NUMBER, "deg", "Course", "Track over ground, degrees from true"
                + " north", null, kinds));
        b.fieldSpec("uaType", new FieldSpec(FieldType.INTEGER, "1", "UA type", "ASTM F3411 UA category, 0 to 15",
                null, kinds));
        b.fieldSpec("idType", new FieldSpec(FieldType.STRING, null, "ID type", "Remote ID identifier type", null,
                kinds));
        b.fieldSpec("operatorId", new FieldSpec(FieldType.STRING, null, "Operator id", "Operator registration id from"
                + " the Operator ID message", null, kinds));
        b.fieldSpec("operatorPoint", new FieldSpec(FieldType.OBJECT, null, "Operator position", "The pilot position"
                + " {lat, lon}, only with keepOperatorPosition on a private source", null, kinds));
        b.kindDisplay(kind, new KindDisplay("{operatorId}", "heliport", "#C2255C", null));
        return b.build();
    }
}
