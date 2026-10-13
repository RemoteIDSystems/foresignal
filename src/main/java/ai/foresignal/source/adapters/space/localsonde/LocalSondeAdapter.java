package ai.foresignal.source.adapters.space.localsonde;

import ai.foresignal.net.Cidr;
import ai.foresignal.source.codec.AutoRxPayloadCodec;
import ai.foresignal.source.codec.CodecContext;
import ai.foresignal.source.codec.CodecRegistry;
import ai.foresignal.source.codec.CodecSession;
import ai.foresignal.source.codec.SocketCodec;
import ai.foresignal.source.formats.sonde.RadiosondeRecords;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
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
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code local-sonde} (prompt 077): the operator's own radiosonde_auto_rx receiver, through its
 * {@code PAYLOAD_SUMMARY} UDP datagrams, decoded by codec {@code sonde-udp} ({@link AutoRxPayloadCodec}) into the
 * shared {@code radiosonde} contract.
 *
 * <ul>
 *   <li><strong>Params</strong> (prompt 069 conventions): {@code endpoint}, a required {@code udp://bindHost:port}
 *       {@code LAN_URL} ({@code bindHost} {@code 0.0.0.0}, {@code ::} or a local interface address; auto_rx's
 *       default {@code payload_summary_port} is 55673, but the port is always explicit), and {@code allowSenders},
 *       a required list of IPs or CIDRs wholly inside loopback, private, link-local or unique-local space (checked by
 *       {@code ParamValidator}; a public address is refused at PUT).
 *   <li><strong>Listening:</strong> only through {@code ctx.sockets().udp(…)}; a datagram from an unlisted sender is
 *       dropped and counted by the transport and never parsed. One budget unit per bind.
 *   <li><strong>Records:</strong> {@code externalId} = the sonde serial, {@code observedAt} = the summary's
 *       {@code datetime}, {@code receivedAt} stamped by the sink, the auto_rx {@code station} as observer.
 *   <li><strong>Egress:</strong> {@code LOCAL_ONLY}, overridable (prompt 069's default for local receivers).
 *       Prompt 086 adds auto_rx presets and docs on this source and adds no second listener.
 * </ul>
 */
@Component
public class LocalSondeAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = "local-sonde";

    /** The endpoint parameter. */
    public static final String ENDPOINT_PARAM = "endpoint";

    /** The sender allowlist parameter (prompt 069's companion of a {@code udp://} endpoint). */
    public static final String ALLOW_SENDERS_PARAM = "allowSenders";

    /** auto_rx's default {@code payload_summary_port}, for documentation only; never assumed. */
    public static final int AUTO_RX_DEFAULT_PORT = 55673;

    /** The record TTL, from {@code observedAt}. */
    static final Duration TTL = Duration.ofMinutes(30);

    /** The keyframe: under {@code ttl - stream interval - 30 s}. */
    static final Duration KEYFRAME = Duration.ofMinutes(20);

    /** Raw retention: high-volume balloon telemetry, the {@code bulk-event} class (90 d). */
    static final Duration RETENTION = Duration.ofDays(90);

    private final SourceDescriptor descriptor = buildDescriptor();
    private final SocketCodec codec;

    /**
     * Creates the adapter for Spring.
     *
     * @param codecs every codec; {@value AutoRxPayloadCodec#ID} must be registered
     */
    @Autowired
    public LocalSondeAdapter(CodecRegistry codecs) {
        this(codecs.require(AutoRxPayloadCodec.ID));
    }

    /**
     * Creates the adapter.
     *
     * @param codec the {@value AutoRxPayloadCodec#ID} codec
     */
    public LocalSondeAdapter(SocketCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Binds the UDP listener and decodes each allowed datagram until the handle is closed.
     *
     * @throws AdapterException when the budget is spent or the bind fails
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        LanEndpoint bind = LanEndpoint.fromParams(ctx.params(), ENDPOINT_PARAM);
        Set<Cidr> senders = senders(ctx.params().stringList(ALLOW_SENDERS_PARAM));
        if (senders.isEmpty()) {
            throw new AdapterException(ID + ": allowSenders is empty; list the auto_rx host's IP or CIDR");
        }
        CodecSession session = codec.newSession(new CodecContext(ctx.sourceId(), ctx.params()));
        return ctx.sockets().udp(bind, senders, (datagram, sender, receivedAt) ->
                sink.acceptAll(session.onFrame(datagram, receivedAt)));
    }

    /** The parsed allowlist; entries that do not parse are left out (the validator refused them at PUT). */
    static Set<Cidr> senders(List<String> entries) {
        Set<Cidr> out = new LinkedHashSet<>();
        if (entries != null) {
            for (String e : entries) {
                Cidr.parse(e).ifPresent(out::add);
            }
        }
        return out;
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = RadiosondeRecords.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Local radiosonde receiver (auto_rx)")
                .description("Radiosondes decoded by the operator's own radiosonde_auto_rx station, from its"
                        + " PAYLOAD_SUMMARY UDP datagrams (auto_rx's default payload_summary_port is 55673; the port"
                        + " is always explicit). Listens on a udp://bindHost:port endpoint and reads only datagrams"
                        + " from the allowSenders blocks (private, loopback, link-local or unique-local only). Same"
                        + " radiosonde payload as sondehub; sensor values are uncalibrated. Disabled by default;"
                        + " egress LOCAL_ONLY (the operator may set it to OPEN).")
                .kind(kind)
                .homepage("https://github.com/projecthorus/radiosonde_auto_rx")
                .attribution("The operator's own radiosonde_auto_rx receiver")
                .licence("Operator's own data (no upstream license)")
                .mode(Mode.STREAMING)
                .defaultBudget(2, 200)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(KEYFRAME))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(TTL)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .zeroResultsNormal(true)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .egress(Egress.LOCAL_ONLY)
                .egressOverridable(true)
                .titleField("type")
                .searchFields("type")
                .param(ParamSpec.lanUrl(ENDPOINT_PARAM, "Where to listen, udp://bindHost:port, for example"
                        + " udp://0.0.0.0:55673 (auto_rx's default payload_summary_port); bindHost is 0.0.0.0, :: or"
                        + " a local interface address, and the port is always explicit", true, "udp"))
                .param(ParamSpec.required(ALLOW_SENDERS_PARAM, ParamType.STRING_LIST, "IPs or CIDRs of the auto_rx"
                        + " hosts whose datagrams are read, for example 192.168.1.40; private, loopback, link-local"
                        + " or unique-local only"));
        RadiosondeRecords.fieldSpecs(b);
        b.kindDisplay(kind, new KindDisplay("{type} {altitudeM} m", "marker", "#1C7ED6", null));
        return b.build();
    }
}
