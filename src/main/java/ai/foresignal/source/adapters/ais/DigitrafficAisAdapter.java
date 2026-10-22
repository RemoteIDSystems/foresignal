package ai.foresignal.source.adapters.ais;

import ai.foresignal.codec.ais.VesselStateMerger;
import ai.foresignal.codec.ais.VesselUpdate;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttEndpoint;
import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.mqtt.MqttSessionOptions;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.formats.ais.VesselDescriptors;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HostPort;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.ConnectRateLimiter;
import java.net.URI;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ais-digitraffic} (prompt 085): Finland's and the northern Baltic's AIS from Fintraffic's Digitraffic marine
 * MQTT over WSS, merged per MMSI by the shared {@link VesselStateMerger} into prompt 063's vessel snapshots.
 *
 * <ul>
 *   <li><strong>Connection:</strong> {@value #BROKER_URL} through prompt 060's {@link MqttConnections} (HiveMQ
 *       WebSocket transport, MQTT 3.1.1, a clean subscriber session that never reconnects on its own and never
 *       publishes). The host is fixed in the descriptor ({@code allowedHosts}), never operator input. Client id
 *       {@code Foresignal; {installId}-{random6}} (the format of Fintraffic's examples); the WebSocket upgrade carries
 *       {@code Digitraffic-User: Foresignal/{version} ({installId})}.
 *   <li><strong>Subscriptions</strong> (3): {@code vessels-v2/+/location}, {@code vessels-v2/+/metadata},
 *       {@code vessels-v2/status}. Each CONNECT and SUBSCRIBE costs one budget unit; at most 12 connects in any hour;
 *       reconnects back off from 15 s to 10 min with ±20% jitter.
 *   <li><strong>Mapping</strong> ({@link DigitrafficAisParser}): the MMSI from the topic; {@code refA + refB} →
 *       {@code lengthM}, {@code refC + refD} → {@code beamM}; the draft from tenths of a meter; the AIS ETA bit field.
 *       {@code observedAt} is the location's {@code time} or the metadata's {@code timestamp} (UPSTREAM).
 *   <li><strong>Stale fixes:</strong> a location older than 24 h at arrival is dropped and counted (Velocity found many
 *       stale fixes on the REST side; the guard applies to MQTT too). The REST {@code locations} endpoint is never
 *       used.
 *   <li><strong>Metadata bootstrap:</strong> {@code GET} {@value #METADATA_URL} when the stream starts and every 12 h
 *       while it runs, with {@code Digitraffic-User}; the upstream client always sends {@code Accept-Encoding: gzip},
 *       without which the API answers 406 (verified 2026-10). The list seeds the merger without emitting anything.
 *   <li><strong>Coverage:</strong> {@code vessels-v2/status} {@code updated} older than 10 min marks the source
 *       {@code degraded} ({@link #statusProblem}).
 *   <li><strong>License:</strong> CC BY 4.0, egress OPEN.
 * </ul>
 */
@Component
public class DigitrafficAisAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = "ais-digitraffic";

    /** The broker. */
    public static final String BROKER_URL = "wss://meri.digitraffic.fi:443/mqtt";

    /** The broker's host and port, the descriptor's only {@code allowedHosts} entry. */
    public static final String BROKER_HOST = "meri.digitraffic.fi:443";

    /** The metadata list. */
    public static final String METADATA_URL = "https://meri.digitraffic.fi/api/ais/v1/vessels";

    /** Test-only override of {@link #BROKER_URL} ({@code ws://host:port/mqtt}); blank in production. */
    public static final String BROKER_URL_PROPERTY = "foresignal.test.adapters.ais-digitraffic.broker-url";

    /** Test-only override of {@link #METADATA_URL}; blank in production. */
    public static final String METADATA_URL_PROPERTY = "foresignal.test.adapters.ais-digitraffic.metadata-url";

    /** The subscriptions. */
    public static final List<String> TOPICS = List.of("vessels-v2/+/location", "vessels-v2/+/metadata",
            "vessels-v2/status");

    /** The header Digitraffic asks clients to send. */
    public static final String USER_HEADER = "Digitraffic-User";

    /** The attribution CC BY 4.0 asks for. */
    public static final String ATTRIBUTION = "Source: Fintraffic / digitraffic.fi, licence CC 4.0 BY";

    /** Location fixes older than this at arrival are dropped. */
    static final Duration MAX_FIX_AGE = Duration.ofHours(24);

    /** A status {@code updated} older than this marks the source degraded. */
    static final Duration STATUS_STALE = Duration.ofMinutes(10);

    /** How often the metadata list is fetched. */
    static final Duration BOOTSTRAP_EVERY = Duration.ofHours(12);

    /** The keepalive. */
    static final Duration KEEPALIVE = Duration.ofSeconds(60);

    /** The reconnect ladder: 15 s doubling to 10 min, ±20% jitter. */
    static final ReconnectPolicy RECONNECT = new ReconnectPolicy(Duration.ofSeconds(15), Duration.ofMinutes(10), 0.2);

    /** The most connects in any hour. */
    static final int CONNECTS_PER_HOUR = 12;

    /** One CONNECT, three SUBSCRIBEs and a metadata GET per minute at most; 400 units a day. */
    static final Budget BUDGET = Budget.of(5, 400);

    /** The connection key in {@link MqttConnections}. */
    static final String CONNECTION_KEY = "source:" + ID;

    private static final String RANDOM_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    private final MqttConnections connections;
    private final Supplier<String> installId;
    private final Supplier<String> version;
    private final String brokerUrl;
    private final URI metadataUri;
    private final VesselStateMerger merger = new VesselStateMerger(true);
    private final AisStationStats stats = new AisStationStats();
    private final ConnectRateLimiter connects = new ConnectRateLimiter(CONNECTS_PER_HOUR, Duration.ofHours(1));
    private final AtomicBoolean bootstrapping = new AtomicBoolean();
    private final AtomicLong staleDropped = new AtomicLong();
    private final AtomicLong malformed = new AtomicLong();
    private final SecureRandom random = new SecureRandom();
    private final SourceDescriptor descriptor = buildDescriptor();
    private volatile Instant statusUpdated;
    private volatile Instant lastBootstrap;

    /**
     * Creates the adapter for Spring.
     *
     * @param connections the shared MQTT connections (prompt 060)
     * @param install the install id of the client id and the user header
     * @param version Foresignal's version, for the user header
     * @param brokerOverride the value of {@value #BROKER_URL_PROPERTY}; blank in production
     * @param metadataOverride the value of {@value #METADATA_URL_PROPERTY}; blank in production
     */
    @Autowired
    public DigitrafficAisAdapter(MqttConnections connections, InstallIdService install,
            ObjectProvider<ForesignalVersion> version, @Value("${" + BROKER_URL_PROPERTY + ":}") String brokerOverride,
            @Value("${" + METADATA_URL_PROPERTY + ":}") String metadataOverride) {
        this(connections, install::installId, () -> {
            ForesignalVersion v = version == null ? null : version.getIfAvailable();
            return v == null ? ForesignalVersion.FALLBACK : v.version();
        }, brokerOverride, metadataOverride);
    }

    /**
     * Creates the adapter from plain parts (tests).
     *
     * @param connections the MQTT connections
     * @param installId supplies the install id
     * @param version supplies Foresignal's version
     * @param brokerOverride another broker URL, or blank for {@value #BROKER_URL}
     * @param metadataOverride another metadata URL, or blank for {@value #METADATA_URL}
     */
    public DigitrafficAisAdapter(MqttConnections connections, Supplier<String> installId, Supplier<String> version,
            String brokerOverride, String metadataOverride) {
        this.connections = connections;
        this.installId = installId;
        this.version = version == null ? () -> ForesignalVersion.FALLBACK : version;
        this.brokerUrl = brokerOverride == null || brokerOverride.isBlank() ? BROKER_URL : brokerOverride.strip();
        this.metadataUri = URI.create(metadataOverride == null || metadataOverride.isBlank() ? METADATA_URL
                : metadataOverride.strip());
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The client id: {@code Foresignal; {installId}-{random6}}. */
    String clientId() {
        StringBuilder suffix = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            suffix.append(RANDOM_ALPHABET.charAt(random.nextInt(RANDOM_ALPHABET.length())));
        }
        return "Foresignal; " + installId.get() + "-" + suffix;
    }

    /** The user header value: {@code Foresignal/{version} ({installId})}. */
    String userHeader() {
        return "Foresignal/" + version.get() + " (" + installId.get() + ")";
    }

    /** The connection: the fixed broker, MQTT 3.1.1, the subscriber session and the user header on the upgrade. */
    MqttConnectionSpec spec() {
        return new MqttConnectionSpec(CONNECTION_KEY, ID, clientId(), MqttEndpoint.parse(brokerUrl),
                MqttConnectionSpec.V311, null, null, null, null, MqttSessionOptions.subscriber(KEEPALIVE),
                Map.of(USER_HEADER, userHeader()));
    }

    /**
     * Seeds the metadata when due, connects and subscribes.
     *
     * @throws AdapterException when the hourly connect cap or the budget is spent, or the broker cannot be reached
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        if (BROKER_URL.equals(brokerUrl) && !descriptor.allowsHost(HostPort.parse(BROKER_HOST))) {
            throw new AdapterException(ID + ": the broker is not in allowedHosts");
        }
        Clock clock = ctx.clock();
        if (!connects.tryAcquire(clock.instant())) {
            throw new AdapterException(ID + ": " + CONNECTS_PER_HOUR + " connects in the last hour; waiting for the next"
                    + " attempt");
        }
        bootstrapIfDue(ctx);
        Object order = new Object();
        return AisMqttSubscriber.open(ctx, connections, spec(), TOPICS, message -> {
            synchronized (order) {
                List<NormalizedRecord> records = onMessage(message, clock.instant());
                if (!records.isEmpty()) {
                    sink.acceptAll(records);
                }
            }
            if (bootstrapDue(clock.instant()) && bootstrapping.compareAndSet(false, true)) {
                WorkThreads.start(WorkRole.COLLECT, "ais-digitraffic-bootstrap", () -> {
                    try {
                        bootstrapIfDue(ctx);
                    } finally {
                        bootstrapping.set(false);
                    }
                });
            }
        });
    }

    /**
     * The records of one message at {@code now}: a location or metadata snapshot, nothing for status messages (they
     * update {@link #statusUpdated()}), stale fixes, malformed messages or vessels without a position yet.
     */
    List<NormalizedRecord> onMessage(MqttInbound message, Instant now) {
        String topic = message.topic();
        switch (DigitrafficAisParser.kind(topic)) {
            case STATUS -> {
                Instant updated = DigitrafficAisParser.statusUpdated(message.payload());
                if (updated != null) {
                    statusUpdated = updated;
                } else {
                    malformed.incrementAndGet();
                }
                return List.of();
            }
            case LOCATION -> {
                String mmsi = DigitrafficAisParser.mmsiFromTopic(topic);
                VesselUpdate.Position position = mmsi == null ? null
                        : DigitrafficAisParser.location(mmsi, message.payload());
                if (position == null) {
                    malformed.incrementAndGet();
                    return List.of();
                }
                if (position.time().isBefore(now.minus(MAX_FIX_AGE))) {
                    staleDropped.incrementAndGet();
                    return List.of();
                }
                stats.message(mmsi, now);
                return emit(merger.merge(position, null, now));
            }
            case METADATA -> {
                String mmsi = DigitrafficAisParser.mmsiFromTopic(topic);
                VesselUpdate.Static data = mmsi == null ? null
                        : DigitrafficAisParser.metadata(mmsi, message.payload());
                if (data == null) {
                    malformed.incrementAndGet();
                    return List.of();
                }
                stats.message(mmsi, now);
                return emit(merger.merge(data, null, now));
            }
            default -> {
                return List.of();
            }
        }
    }

    private static List<NormalizedRecord> emit(Optional<NormalizedRecord> snapshot) {
        return snapshot.map(List::of).orElse(List.of());
    }

    /** True when the metadata list has never been fetched, or not within the last 12 h. */
    boolean bootstrapDue(Instant now) {
        Instant last = lastBootstrap;
        return last == null || !now.isBefore(last.plus(BOOTSTRAP_EVERY));
    }

    /**
     * Fetches the metadata list and seeds the merger when due; a failure is logged and retried at the next due check
     * (metadata also arrives on the stream).
     *
     * @return the vessels seeded, or -1 when nothing was due or the fetch failed
     */
    int bootstrapIfDue(StreamContext ctx) {
        Instant now = ctx.clock().instant();
        if (!bootstrapDue(now)) {
            return -1;
        }
        lastBootstrap = now;
        try {
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put(USER_HEADER, userHeader());
            headers.put("Accept", "application/json");
            headers.put("Accept-Encoding", "gzip");
            UpstreamResponse response = ctx.http().get(metadataUri, headers);
            int seeded = 0;
            for (VesselUpdate.Static data : DigitrafficAisParser.vesselList(response.body())) {
                if (merger.seed(data, now)) {
                    seeded++;
                }
            }
            ctx.log().debug("{}: seeded metadata of {} vessels", ID, seeded);
            return seeded;
        } catch (AdapterException | RuntimeException e) {
            ctx.log().info("{}: metadata bootstrap failed; metadata still arrives on the stream: {}", ID, e.getMessage());
            return -1;
        }
    }

    /** The newest status {@code updated}, or null before the first status message. */
    public Instant statusUpdated() {
        return statusUpdated;
    }

    /**
     * Why coverage is degraded at {@code now}: the status was last updated more than 10 min ago; empty while fresh or
     * before the first status message.
     */
    public Optional<String> statusProblem(Instant now) {
        Instant updated = statusUpdated;
        if (updated == null || !updated.isBefore(now.minus(STATUS_STALE))) {
            return Optional.empty();
        }
        return Optional.of("Digitraffic AIS status last updated " + Duration.between(updated, now).toMinutes()
                + " min ago");
    }

    /** The health details: the receiving statistics, the vessels held, and the drop counters. */
    public Map<String, Object> healthDetails(Instant now) {
        Map<String, Object> details = new LinkedHashMap<>(stats.details(now));
        details.put("vessels", merger.size());
        details.put("staleFixesDropped", staleDropped.get());
        details.put("malformedDropped", malformed.get());
        Instant updated = statusUpdated;
        if (updated != null) {
            details.put("statusUpdated", updated.toString());
        }
        return details;
    }

    /** Location fixes dropped as older than 24 h. */
    long staleDropped() {
        return staleDropped.get();
    }

    /** The shared merger (tests). */
    VesselStateMerger merger() {
        return merger;
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Digitraffic marine AIS (Finland)")
                .description("Keyless official AIS for Finland and the northern Baltic from Fintraffic's Digitraffic"
                        + " marine MQTT over WSS (meri.digitraffic.fi, vessels-v2 location, metadata and status), with"
                        + " the vessel metadata list fetched every 12 h. Location fixes older than 24 h are dropped; the"
                        + " REST locations endpoint is never used. A status update older than 10 min marks the source"
                        + " degraded.")
                .kind(VesselDescriptors.KIND)
                .mode(Mode.STREAMING)
                .homepage("https://www.digitraffic.fi/en/marine-traffic/")
                .attribution(ATTRIBUTION)
                .licence("CC-BY-4.0")
                .termsUrl("https://www.digitraffic.fi/en/terms-of-service/")
                .egress(Egress.OPEN)
                .defaultBudget(BUDGET)
                .allowedHost(BROKER_HOST)
                .reconnect(RECONNECT)
                .observedAtMode(ObservedAtMode.UPSTREAM);
        VesselDescriptors.apply(b);
        return b.build();
    }
}
