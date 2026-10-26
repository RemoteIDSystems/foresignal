package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.codec.CodecContext;
import ai.foresignal.source.codec.KissAx25Codec;
import ai.foresignal.source.formats.aprs.AprsPacket;
import ai.foresignal.source.formats.aprs.AprsPacketDedup;
import ai.foresignal.source.formats.aprs.AprsRecords;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * One {@code local-aprs-{n}} slot (prompt 082): APRS heard on RF by the operator's own TNC (Direwolf), read as KISS
 * over TCP and decoded by codec {@value KissAx25Codec#ID}.
 *
 * <ul>
 *   <li><strong>Param:</strong> {@code endpoint}, a {@code tcp://host:port} {@code LAN_URL} (Direwolf's conventional
 *       {@code KISSPORT} is 8001, never assumed); optional {@code stationCallsign}, the observer of every record
 *       (else the source id); optional {@code includeMessages}.
 *   <li><strong>Provably receive-only:</strong> the socket is opened with {@link SocketOptions#receiveOnly()}, so
 *       the transport never touches its output stream and the handle has no write method. No KISS command is sent,
 *       and Foresignal never igates or transmits.
 *   <li><strong>Frames that are not AX.25 UI</strong> (for example a MeshCore KISS modem, which carries raw MeshCore
 *       packets) are counted ({@value #NON_AX25_METRIC}) and dropped.
 *   <li><strong>First-hand copy:</strong> every packet is registered with {@link AprsPacketDedup}, so
 *       {@code aprs-is} drops its igated copy.
 *   <li><strong>Egress:</strong> {@code LOCAL_ONLY}, overridable (prompt 069's rule for the operator's own receivers);
 *       the operator may also mark the source private (prompt 039), which caps its precision.
 *   <li><strong>Connection:</strong> one budget unit per connect (1/min, 1,440/day: 60 an hour); reconnects back off
 *       from 5 s to 5 min. A connection silent for 120 s is closed and reopened.
 * </ul>
 */
public class LocalAprsAdapter implements StreamingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "local-aprs-";

    /** The endpoint parameter. */
    public static final String ENDPOINT = "endpoint";

    /** The counter of frames that were not AX.25 UI. */
    public static final String NON_AX25_METRIC = "foresignal.aprs.local.non_ax25";

    /** The reconnect ladder: 5 s doubling to 5 min. */
    static final ReconnectPolicy RECONNECT = ReconnectPolicy.exponential(Duration.ofSeconds(5), Duration.ofMinutes(5));

    /** A station call sign with optional SSID. */
    static final Pattern STATION = Pattern.compile("^[A-Za-z0-9]{3,9}(-[A-Za-z0-9]{1,2})?$");

    private final SourceDescriptor descriptor;
    private final KissAx25Codec codec;
    private final AprsPacketDedup dedup;
    private final Supplier<MeterRegistry> meters;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, from 1
     * @param codec the {@value KissAx25Codec#ID} codec
     * @param dedup the shared filter every packet is registered with
     * @param meters supplies the meter registry, or null
     */
    public LocalAprsAdapter(int slot, KissAx25Codec codec, AprsPacketDedup dedup, Supplier<MeterRegistry> meters) {
        this(buildDescriptor(slot), codec, dedup, meters);
    }

    /**
     * Creates a KISS TNC read with its own descriptor (prompt R2-021: the datalink aggregator's
     * {@code local-aprs-aggregator}). The descriptor must come from {@link #describe}.
     *
     * @param descriptor the source's descriptor
     * @param codec the {@value KissAx25Codec#ID} codec
     * @param dedup the shared filter every packet is registered with
     * @param meters supplies the meter registry, or null
     */
    protected LocalAprsAdapter(SourceDescriptor descriptor, KissAx25Codec codec, AprsPacketDedup dedup,
            Supplier<MeterRegistry> meters) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.dedup = dedup;
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Checks the station call sign. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String station = params.string(KissAx25Codec.STATION_PARAM);
        if (station != null && !station.isBlank() && !STATION.matcher(station.strip()).matches()) {
            problems.put(KissAx25Codec.STATION_PARAM, "must be a call sign with an optional -SSID");
        }
        return problems;
    }

    /**
     * Connects receive-only and decodes until the handle is closed.
     *
     * @throws AdapterException when the budget is spent or the TNC cannot be reached
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        LanEndpoint endpoint = LanEndpoint.fromParams(ctx.params(), ENDPOINT);
        KissAx25Codec.Session session = codec.newSession(new CodecContext(ctx.sourceId(), ctx.params()),
                new Listener(ctx.sourceId()));
        return ctx.sockets().tcp(endpoint, SocketOptions.receiveOnly(), codec.framing(),
                (frame, receivedAt) -> sink.acceptAll(session.onFrame(frame, receivedAt)));
    }

    /** Registers packets with the duplicate filter and counts dropped frames. */
    final class Listener implements KissAx25Codec.PacketListener {

        private final String sourceId;
        private Counter nonAx25;
        private Counter unsupported;

        Listener(String sourceId) {
            this.sourceId = sourceId;
        }

        @Override
        public void packet(AprsPacket packet, Instant receivedAt) {
            if (dedup != null) {
                dedup.register(packet.source(), packet.info(), receivedAt);
            }
        }

        @Override
        public void unsupported(AprsPacket packet, Instant receivedAt) {
            packet(packet, receivedAt);
            unsupported = increment(unsupported, AprsIsAdapter.UNSUPPORTED_METRIC,
                    "APRS packets of unsupported formats, dropped");
        }

        @Override
        public void nonAx25() {
            nonAx25 = increment(nonAx25, NON_AX25_METRIC, "KISS frames that were not AX.25 UI, dropped");
        }

        private Counter increment(Counter counter, String name, String description) {
            Counter c = counter;
            if (c == null) {
                MeterRegistry registry = meters.get();
                if (registry == null) {
                    return null;
                }
                c = Counter.builder(name).description(description).tag("source", sourceId).register(registry);
            }
            c.increment();
            return c;
        }
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        return describe(ID_PREFIX + slot, "Local APRS (KISS TNC) " + slot, "APRS heard on RF by the operator's own"
                + " TNC (for example Direwolf's KISSPORT), read as"
                + " KISS over TCP from a tcp://host:port on the operator's network. Provably receive-only:"
                + " nothing is ever written to the socket, no KISS command is sent and Foresignal never"
                + " igates. Frames that are not AX.25 UI (a MeshCore KISS modem, for example) are counted"
                + " and dropped. stationCallsign names the observer. Same records as aprs-is; aprs-is drops"
                + " its copy of a packet heard here first. Disabled by default; egress LOCAL_ONLY (the"
                + " operator may set it to OPEN).");
    }

    /**
     * The descriptor of a KISS TNC read: the slots' parameters, fields, retention and overridable {@code LOCAL_ONLY}
     * egress under {@code id} (prompt R2-021 shares it with {@code local-aprs-aggregator}).
     *
     * @param id the source id
     * @param name the display name
     * @param description the description
     * @return the descriptor
     */
    protected static SourceDescriptor describe(String id, String name, String description) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(id)
                .name(name)
                .description(description)
                .kinds(AprsRecords.STATION_KIND, AprsRecords.MESSAGE_KIND)
                .homepage("https://github.com/wb2osz/direwolf")
                .attribution("The operator's own APRS receiver")
                .licence("Operator's own data (no upstream license)")
                .mode(Mode.STREAMING)
                .defaultBudget(Budget.of(1, 1440))
                .reconnect(RECONNECT)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(AprsIsAdapter.KEYFRAME))
                .observedAtMode(ObservedAtMode.RECEIVED)
                .recordTtl(AprsIsAdapter.TTL)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .zeroResultsNormal(true)
                .defaultRetention(AprsIsAdapter.RETENTION)
                .kindRetention(AprsRecords.STATION_KIND, AprsIsAdapter.RETENTION)
                .kindRetention(AprsRecords.MESSAGE_KIND, AprsIsAdapter.RETENTION)
                .egress(Egress.LOCAL_ONLY)
                .egressOverridable(true)
                .param(ParamSpec.lanUrl(ENDPOINT, "The TNC's KISS TCP port, tcp://host:port, for example"
                        + " tcp://192.168.1.20:8001 (Direwolf's KISSPORT); the port is always explicit", true, "tcp"))
                .param(ParamSpec.optional(KissAx25Codec.STATION_PARAM, ParamType.STRING, "Your station's call sign,"
                        + " the observer of every record; empty uses the source id", null))
                .param(ParamSpec.optional(KissAx25Codec.INCLUDE_MESSAGES_PARAM, ParamType.BOOLEAN, "Also keep APRS"
                        + " messages heard on RF as aprs-message records", false));
        AprsSourceFields.declare(b);
        return b.build();
    }
}
