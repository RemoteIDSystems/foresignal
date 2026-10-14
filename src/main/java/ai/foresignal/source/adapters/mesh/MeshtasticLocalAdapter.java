package ai.foresignal.source.adapters.mesh;

import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttEndpoint;
import ai.foresignal.mqtt.MqttSessionOptions;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.LocalAddresses;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.ValidatedParams;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code meshtastic-local} (prompt 083): the operator's own Meshtastic node through the operator's own MQTT broker.
 *
 * <ul>
 *   <li><strong>Setup:</strong> the node's MQTT module points at a broker on the LAN (for example Mosquitto) with
 *       <em>JSON enabled</em> and <em>encryption disabled on uplink</em>. nRF52 nodes cannot emit JSON; the GPL
 *       protobuf path stays outside this repository (an operator may run an out-of-tree bridge that republishes JSON to
 *       their broker).
 *   <li><strong>Broker:</strong> {@code brokerUrl}, a {@code LAN_URL} {@code mqtt://} or {@code mqtts://} with an
 *       explicit port, pinned to a private address. {@code mqtt://} connects to the pinned address; {@code mqtts://}
 *       connects by name, so TLS can verify it, after checking that the name still resolves only to private addresses.
 *       Optional {@code username} and {@code password} credentials.
 *   <li><strong>Subscriptions:</strong> {@code <root>/2/json/#}, or {@code <root>/2/json/<channel>/#} per entry of
 *       {@code channels}; one budget unit per CONNECT and per SUBSCRIBE; the supervisor reconnects 5 s to 5 min.
 *   <li><strong>Records:</strong> the parser and merge of {@code meshtastic-mqtt}, without the public 100/min cap but
 *       with a 1,000/min safety cap. Every text message goes to the {@link MeshMessageBus}; text becomes a record only
 *       with {@code includeText}.
 *   <li><strong>Private</strong> (prompt 039): it reveals the operator's neighbors' positions.
 * </ul>
 */
@Component
public class MeshtasticLocalAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = "meshtastic-local";

    /** The broker parameter. */
    public static final String BROKER_URL = "brokerUrl";

    /** The root parameter. */
    public static final String ROOT = "root";

    /** The channels parameter. */
    public static final String CHANNELS = "channels";

    /** The default root. */
    public static final String DEFAULT_ROOT = "msh/local";

    /** The user name credential. */
    public static final String USERNAME = "username";

    /** The password credential. */
    public static final String PASSWORD = "password";

    /** The safety cap per minute. */
    public static final int SAFETY_PER_MINUTE = 1_000;

    /** A channel name: letters, digits, {@code _} and {@code -}, no wildcards. */
    static final Pattern CHANNEL = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");

    /** A root: one to three plain segments, no wildcards. */
    static final Pattern ROOT_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+(/[A-Za-z0-9_-]+){0,2}$");

    /** The reconnect ladder. */
    static final ReconnectPolicy RECONNECT = new ReconnectPolicy(Duration.ofSeconds(5), Duration.ofMinutes(5), 0.2);

    /** The connection key in {@link MqttConnections}. */
    static final String CONNECTION_KEY = "source:" + ID;

    private final MqttConnections connections;
    private final Supplier<String> installId;
    private final MeshMessageBus bus;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter for Spring.
     *
     * @param connections the shared MQTT connections (prompt 060)
     * @param install the install id of the client id
     * @param bus where every text message is published
     * @param meters the registry of the counters, resolved lazily
     */
    @Autowired
    public MeshtasticLocalAdapter(MqttConnections connections, InstallIdService install, MeshMessageBus bus,
            ObjectProvider<MeterRegistry> meters) {
        this(connections, install::installId, bus,
                meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter from plain parts (tests).
     *
     * @param connections the MQTT connections
     * @param installId supplies the install id
     * @param bus the mesh message bus
     * @param meters supplies the registry, or null
     */
    public MeshtasticLocalAdapter(MqttConnections connections, Supplier<String> installId, MeshMessageBus bus,
            Supplier<MeterRegistry> meters) {
        this.connections = connections;
        this.installId = installId;
        this.bus = bus;
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Checks the root and the channel names. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String root = root(params);
        if (!ROOT_PATTERN.matcher(root).matches()) {
            problems.put(ROOT, "must be one to three plain topic segments without wildcards, for example msh/local");
        }
        for (String channel : channels(params)) {
            if (!CHANNEL.matcher(channel).matches()) {
                problems.put(CHANNELS, "'" + channel + "' must be a channel name (letters, digits, _ and -), no"
                        + " wildcards");
                break;
            }
        }
        return problems;
    }

    private static String root(ValidatedParams params) {
        String root = params.string(ROOT);
        return root == null || root.isBlank() ? DEFAULT_ROOT : root.strip();
    }

    private static List<String> channels(ValidatedParams params) {
        List<String> channels = params.stringList(CHANNELS);
        return channels == null ? List.of() : channels.stream().filter(c -> c != null && !c.isBlank()).map(String::strip)
                .toList();
    }

    /** The subscriptions: the whole JSON tree, or one per listed channel. */
    static List<String> subscriptions(String root, List<String> channels) {
        if (channels.isEmpty()) {
            return List.of(root + MeshtasticMqttRules.JSON_SUFFIX);
        }
        List<String> out = new ArrayList<>();
        for (String channel : channels) {
            String filter = root + "/2/json/" + channel + "/#";
            if (!out.contains(filter)) {
                out.add(filter);
            }
        }
        return out;
    }

    /**
     * The broker endpoint: the pinned address for {@code mqtt://}; the host name for {@code mqtts://}, once it still
     * resolves only to private addresses.
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
        MqttConnectionSpec spec = new MqttConnectionSpec(CONNECTION_KEY, ID,
                MeshtasticMqttRules.clientId(installId.get()) + "-local", endpoint(lan), MqttConnectionSpec.V311,
                blankToNull(credentials.get(USERNAME)), blankToNull(credentials.get(PASSWORD)), null, null,
                MqttSessionOptions.subscriber(MeshtasticMqttRules.KEEPALIVE));
        Clock clock = ctx.clock();
        MeshtasticStream stream = new MeshtasticStream(ctx.sourceId(), params.bool(MeshRecords.INCLUDE_TEXT, false), bus,
                new SlidingWindowLimiter(SAFETY_PER_MINUTE, Duration.ofMinutes(1)), new MeshNodeStates(), meters);
        return MeshMqttSubscriber.open(ctx, connections, spec, subscriptions(root(params), channels(params)),
                message -> sink.acceptAll(stream.onMessage(message, clock.instant())));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Own Meshtastic node (private broker)")
                .description("The operator's own Meshtastic node and the nodes it hears, through the operator's own MQTT"
                        + " broker on the LAN. Configure the node's MQTT module with JSON enabled and encryption"
                        + " disabled on uplink (nRF52 nodes cannot emit JSON). Subscribes to <root>/2/json/# (or one"
                        + " topic per listed channel); a 1,000 messages a minute safety cap. Text messages go to the"
                        + " in-process mesh message bus and become mesh-message records only with includeText. Private:"
                        + " it reveals neighbors' positions.")
                .kinds(MeshRecords.NODE_KIND, MeshRecords.MESSAGE_KIND)
                .homepage("https://meshtastic.org/docs/configuration/module/mqtt/")
                .attribution("The operator's own Meshtastic node")
                .licence("Operator's own data (no upstream license)")
                .mode(Mode.STREAMING)
                .defaultBudget(Budget.of(20, 2_000))
                .reconnect(RECONNECT)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .zeroResultsNormal(true)
                .coverageFromObservers(true)
                .privateSource(true)
                .credential(CredentialField.optional(USERNAME, "The broker user name, if the broker requires one"))
                .credential(CredentialField.optional(PASSWORD, "The broker password, if the broker requires one"))
                .param(ParamSpec.lanUrl(BROKER_URL, "The broker: mqtt://host:port or mqtts://host:port on the LAN,"
                        + " explicit port, private address", true, "mqtt", "mqtts"))
                .param(ParamSpec.optional(ROOT, ParamType.STRING, "The root topic the node publishes under (its MQTT"
                        + " module's root topic)", DEFAULT_ROOT))
                .param(ParamSpec.optional(CHANNELS, ParamType.STRING_LIST, "Channel names to subscribe to; empty for"
                        + " every channel (<root>/2/json/#)", List.of()))
                .param(ParamSpec.optional(MeshRecords.INCLUDE_TEXT, ParamType.BOOLEAN, "Keep text messages as"
                        + " mesh-message records (never embedded, never sent to an LLM)", false));
        MeshRecords.defaults(b, MeshRecords.TTL, MeshRecords.STREAM_KEYFRAME);
        return b.build();
    }
}
