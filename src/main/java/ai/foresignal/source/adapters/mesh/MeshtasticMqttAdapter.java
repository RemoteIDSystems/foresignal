package ai.foresignal.source.adapters.mesh;

import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttEndpoint;
import ai.foresignal.mqtt.MqttSessionOptions;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HostPort;
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
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code meshtastic-mqtt} (prompt 083): Meshtastic nodes from the public broker {@value #BROKER}, strictly within the
 * Mesh.md rules ({@link MeshtasticMqttRules}), which no setting can loosen.
 *
 * <ul>
 *   <li><strong>Disabled by default</strong>; starting the stream logs a WARN summarizing the rules.
 *   <li><strong>Connection:</strong> 060's {@link MqttConnections}, MQTT 3.1.1, the public credentials
 *       {@value #USERNAME} / {@value #PASSWORD} (descriptor constants, not operator credentials), client id
 *       {@code foresignal-<installId>}, keepalive 120 s, clean session, nothing ever published (no Will, no status
 *       topic). Each CONNECT and each SUBSCRIBE costs one budget unit; at most 12 connects in any hour; the descriptor's
 *       reconnect ladder is 15 s to 300 s with jitter, and the connection never reconnects on its own.
 *   <li><strong>Subscriptions:</strong> {@code <root>/2/json/#} per distinct root, QoS 0; protobuf topics are never
 *       subscribed and, if delivered, dropped unparsed.
 *   <li><strong>Inbound cap:</strong> 100 messages per minute before parsing; the excess is dropped and counted.
 *   <li><strong>Records:</strong> {@link MeshtasticStream}: one merged {@code mesh-node} snapshot per node, the gateway
 *       as observer, {@code Quality.accuracyMeters} from the precision bits (the broker reduces public positions to
 *       10–16 bits). Text only with {@code includeText}, never published elsewhere.
 *   <li><strong>Expect sparse data</strong> (verified 2026-10): most public traffic is protobuf on {@code /2/e/}, so
 *       an empty flush is normal ({@code zeroResultsNormal}).
 * </ul>
 */
@Component
public class MeshtasticMqttAdapter implements StreamingAdapter {

    private static final Logger log = LoggerFactory.getLogger(MeshtasticMqttAdapter.class);

    /** The source id. */
    public static final String ID = "meshtastic-mqtt";

    /** The public broker. */
    public static final String BROKER = "mqtt.meshtastic.org:1883";

    /** The public broker's published user name (Meshtastic documentation). */
    public static final String USERNAME = "meshdev";

    /** The public broker's published password (Meshtastic documentation). */
    public static final String PASSWORD = "large4cats";

    /** The root parameter. */
    public static final String ROOT = "root";

    /** The extra roots parameter. */
    public static final String EXTRA_ROOTS = "extraRoots";

    /** The subscription cap parameter. */
    public static final String MAX_SUBSCRIPTIONS = "maxSubscriptions";

    /** The default root. */
    public static final String DEFAULT_ROOT = "msh/US";

    /** The test-only property naming another broker URL ({@code mqtt://host:port}); blank in production. */
    public static final String BROKER_URL_PROPERTY = "foresignal.test.adapters.mesh.public-broker-url";

    /** The budget: one connection with up to three subscriptions per minute, 100 units a day. */
    static final Budget BUDGET = Budget.of(1 + MeshtasticMqttRules.MAX_SUBSCRIPTIONS, 100);

    /** The connection key in {@link MqttConnections}. */
    static final String CONNECTION_KEY = "source:" + ID;

    private final MqttConnections connections;
    private final Supplier<String> installId;
    private final MeshMessageBus bus;
    private final Supplier<MeterRegistry> meters;
    private final String brokerUrlOverride;
    private final SlidingWindowLimiter connects = new SlidingWindowLimiter(MeshtasticMqttRules.CONNECTS_PER_HOUR,
            Duration.ofHours(1));
    private final AtomicReference<Instant> lastRulesWarn = new AtomicReference<>();
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter for Spring.
     *
     * @param connections the shared MQTT connections (prompt 060)
     * @param install the install id of the client id
     * @param bus the mesh message bus (not used by this public source)
     * @param meters the registry of the counters, resolved lazily
     * @param brokerUrlOverride the value of {@value #BROKER_URL_PROPERTY}; blank in production
     */
    @Autowired
    public MeshtasticMqttAdapter(MqttConnections connections, InstallIdService install, MeshMessageBus bus,
            ObjectProvider<MeterRegistry> meters, @Value("${" + BROKER_URL_PROPERTY + ":}") String brokerUrlOverride) {
        this(connections, install::installId, bus,
                meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable, brokerUrlOverride);
    }

    /**
     * Creates the adapter from plain parts (tests).
     *
     * @param connections the MQTT connections
     * @param installId supplies the install id
     * @param bus the mesh message bus
     * @param meters supplies the registry, or null
     * @param brokerUrlOverride another broker URL, or blank for {@value #BROKER}
     */
    public MeshtasticMqttAdapter(MqttConnections connections, Supplier<String> installId, MeshMessageBus bus,
            Supplier<MeterRegistry> meters, String brokerUrlOverride) {
        this.connections = connections;
        this.installId = installId;
        this.bus = bus;
        this.meters = meters == null ? () -> null : meters;
        this.brokerUrlOverride = brokerUrlOverride == null ? "" : brokerUrlOverride.strip();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Refuses anything outside the Mesh.md limits (400 {@code VALIDATION_FAILED} at PUT). */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        return MeshtasticMqttRules.problems(root(params), params.stringList(EXTRA_ROOTS), params.integer(MAX_SUBSCRIPTIONS));
    }

    private static String root(ValidatedParams params) {
        String root = params.string(ROOT);
        return root == null || root.isBlank() ? DEFAULT_ROOT : root.strip();
    }

    /** The connection of this source: the public broker, the public credentials and the subscriber session. */
    MqttConnectionSpec spec() {
        String url = brokerUrlOverride.isEmpty() ? "mqtt://" + BROKER : brokerUrlOverride;
        return new MqttConnectionSpec(CONNECTION_KEY, ID, MeshtasticMqttRules.clientId(installId.get()),
                MqttEndpoint.parse(url), MqttConnectionSpec.V311, USERNAME, PASSWORD, null, null,
                MqttSessionOptions.subscriber(MeshtasticMqttRules.KEEPALIVE));
    }

    /**
     * Connects to the public broker within the rules and streams records until the handle is closed.
     *
     * @throws AdapterException when the parameters break a rule, the hourly connect cap or the budget is spent, or the
     *     broker cannot be reached
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        ValidatedParams params = ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ID + ": " + problems);
        }
        if (brokerUrlOverride.isEmpty() && !descriptor.allowsHost(HostPort.parse(BROKER))) {
            throw new AdapterException(ID + ": the broker is not in allowedHosts");
        }
        Clock clock = ctx.clock();
        Instant now = clock.instant();
        if (!connects.tryAcquire(now)) {
            throw new AdapterException(ID + ": " + MeshtasticMqttRules.CONNECTS_PER_HOUR
                    + " connects in the last hour; waiting for the next attempt");
        }
        warnRules(now);
        List<String> filters = MeshtasticMqttRules.subscriptions(root(params), params.stringList(EXTRA_ROOTS));
        MeshtasticStream stream = new MeshtasticStream(ctx.sourceId(), params.bool(MeshRecords.INCLUDE_TEXT, false), null,
                new SlidingWindowLimiter(MeshtasticMqttRules.INBOUND_PER_MINUTE, Duration.ofMinutes(1)),
                new MeshNodeStates(), meters);
        return MeshMqttSubscriber.open(ctx, connections, spec(), filters,
                message -> sink.acceptAll(stream.onMessage(message, clock.instant())));
    }

    /** The WARN that summarizes the rules, at most once an hour. */
    void warnRules(Instant now) {
        Instant last = lastRulesWarn.get();
        if ((last == null || !now.isBefore(last.plus(Duration.ofHours(1)))) && lastRulesWarn.compareAndSet(last, now)) {
            log.warn("{} is enabled: connecting to the public Meshtastic broker {} under fixed rules: JSON topics only"
                    + " (<root>/2/json/#, QoS 0), at most {} subscriptions, no wildcard above the root, keepalive {} s,"
                    + " clean session, reconnect {} s to {} s with jitter, at most {} connects an hour, at most {}"
                    + " messages a minute (the excess is dropped). Expect sparse data: most public traffic is"
                    + " protobuf, which Foresignal never decodes.", ID, BROKER, MeshtasticMqttRules.MAX_SUBSCRIPTIONS,
                    MeshtasticMqttRules.KEEPALIVE.toSeconds(), MeshtasticMqttRules.RECONNECT.initial().toSeconds(),
                    MeshtasticMqttRules.RECONNECT.max().toSeconds(), MeshtasticMqttRules.CONNECTS_PER_HOUR,
                    MeshtasticMqttRules.INBOUND_PER_MINUTE);
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Meshtastic public broker (strictly limited)")
                .description("Meshtastic nodes heard by public gateways, from the firmware's JSON output on the public"
                        + " broker mqtt.meshtastic.org. Fixed rules (the Mesh.md lesson): <root>/2/json/# only, QoS 0,"
                        + " one subscription per root and at most 3, no wildcard above the root, keepalive 120 s, clean"
                        + " session, reconnect 15 s to 300 s with jitter, 12 connects an hour, 100 messages a minute"
                        + " (excess dropped before parsing). Most public traffic is protobuf, so JSON is sparse; public"
                        + " positions are reduced to 10-16 bits. Text only with includeText.")
                .kinds(MeshRecords.NODE_KIND, MeshRecords.MESSAGE_KIND)
                .homepage("https://meshtastic.org/docs/software/integrations/mqtt/")
                .attribution("Meshtastic public MQTT broker and the gateways that uplink to it")
                .licence("unspecified (Meshtastic community broker)")
                .termsUrl("https://meshtastic.org/docs/software/integrations/mqtt/")
                .mode(Mode.STREAMING)
                .defaultBudget(BUDGET)
                .allowedHost(BROKER)
                .reconnect(MeshtasticMqttRules.RECONNECT)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .zeroResultsNormal(true)
                .coverageFromObservers(true)
                .param(ParamSpec.optional(ROOT, ParamType.STRING, "The region or channel root, matching "
                        + MeshtasticMqttRules.ROOT_REGEX + " (msh/US, msh/EU_868, msh/US/CA); wildcards are refused",
                        DEFAULT_ROOT))
                .param(ParamSpec.optional(EXTRA_ROOTS, ParamType.STRING_LIST, "Further roots, one subscription each;"
                        + " root and extraRoots together must fit in maxSubscriptions", List.of()))
                .param(ParamSpec.optional(MAX_SUBSCRIPTIONS, ParamType.INTEGER, "The most subscriptions, 1 to 3 (the"
                        + " public broker's limit)", 1))
                .param(ParamSpec.optional(MeshRecords.INCLUDE_TEXT, ParamType.BOOLEAN, "Keep public channel text as"
                        + " mesh-message records (never embedded, never sent to an LLM, never posted anywhere)", false));
        MeshRecords.defaults(b, MeshRecords.TTL, MeshRecords.STREAM_KEYFRAME);
        return b.build();
    }
}
