package ai.foresignal.source.adapters.ais;

import ai.foresignal.net.Cidr;
import ai.foresignal.source.codec.AivdmSocketCodec;
import ai.foresignal.source.formats.ais.VesselDescriptors;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * {@code local-ais-aggregator} (prompt R2-020): the first-party AIS aggregator's read — the whole NMEA feed of the
 * operator's own AIS-catcher (or rtl-ais, or any decoder that emits AIVDM lines), from its own receivers, from feeds it
 * aggregates, or both.
 *
 * <p>It is a {@link LocalAisAdapter} with its own descriptor: the same {@value AivdmSocketCodec#ID} decoding, station
 * parameters, statistics and {@code LOCAL_ONLY} egress (overridable). Unlike a {@code local-ais-{n}} slot, its
 * {@code endpoint} may also be a {@code udp://bindHost:port} listener (AIS-catcher's {@code -u host port} output,
 * rtl-ais's default), read only from the {@code allowSenders} blocks; each datagram may carry several lines. The
 * {@code endpoint} and {@code allowSenders} are written by the AIS aggregator ({@code PUT /v1/aggregators/ais}), which
 * also turns the source on and off; an operator never needs to configure it directly. Its id carries the
 * {@code local-ais-} prefix, so every derived vessel layer reads it like any local receiver.
 */
@Component
public class LocalAisAggregatorAdapter extends LocalAisAdapter {

    /** The source id. */
    public static final String ID = "local-ais-aggregator";

    /** The sender allowlist parameter, the companion of a {@code udp://} endpoint (prompt 069). */
    public static final String ALLOW_SENDERS = "allowSenders";

    /** The budget: one unit per connection or bind, as for the slots but with room for a busier reconnect ladder. */
    static final Budget BUDGET = Budget.of(4, 400);

    /**
     * Creates the adapter.
     *
     * @param codec the {@value AivdmSocketCodec#ID} codec
     */
    public LocalAisAggregatorAdapter(AivdmSocketCodec codec) {
        super(buildDescriptor(), codec);
    }

    /**
     * Connects to a {@code tcp://} endpoint as a {@code local-ais-{n}} slot does, or binds a {@code udp://} listener
     * and decodes every line of each allowed datagram.
     *
     * @throws AdapterException when the parameters are invalid, the budget is spent, the endpoint cannot be reached or
     *     the bind fails
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        LanEndpoint endpoint = LanEndpoint.fromParams(ctx.params(), ENDPOINT);
        if (endpoint.scheme() != LanEndpoint.Scheme.UDP) {
            return super.start(ctx, sink);
        }
        var problems = checkParams(ctx.params());
        if (!problems.isEmpty()) {
            throw new AdapterException(ctx.sourceId() + ": " + problems);
        }
        Set<Cidr> senders = senders(ctx.params().stringList(ALLOW_SENDERS));
        if (senders.isEmpty()) {
            throw new AdapterException(ID + ": allowSenders is empty; list the AIS decoder host's IP or CIDR");
        }
        Connection connection = newConnection(ctx.sourceId(), ctx.params());
        return ctx.sockets().udp(endpoint, senders, (datagram, sender, receivedAt) ->
                sink.acceptAll(decode(connection, datagram, receivedAt)));
    }

    /** Every record of one datagram: its lines, decoded in order. */
    static List<NormalizedRecord> decode(Connection connection, byte[] datagram, Instant receivedAt) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (String line : new String(datagram, StandardCharsets.US_ASCII).split("\\r?\\n")) {
            if (!line.isBlank()) {
                out.addAll(connection.onFrame(line.getBytes(StandardCharsets.US_ASCII), receivedAt));
            }
        }
        return out;
    }

    /** The parsed allowlist; entries that do not parse are left out (the validator refused them at PUT). */
    static Set<Cidr> senders(List<String> entries) {
        Set<Cidr> out = new LinkedHashSet<>();
        if (entries != null) {
            for (String entry : entries) {
                Cidr.parse(entry).ifPresent(out::add);
            }
        }
        return out;
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Local AIS aggregator")
                .description("Every vessel the operator's own AIS decoder hears or aggregates — AIS-catcher, rtl-ais"
                        + " or any decoder that emits AIVDM NMEA lines — read either from its TCP server"
                        + " (tcp://host:port, AIS-catcher -S <port>) or as UDP datagrams sent to this server"
                        + " (udp://bindHost:port with allowSenders, AIS-catcher -u <host> <port>). Configured by the AIS"
                        + " aggregator (PUT /v1/aggregators/ais), not directly. Receive-only. The station's position is"
                        + " private by default. Egress LOCAL_ONLY (the operator may set it to OPEN).")
                .kinds(VesselDescriptors.KIND, RECEIVER_KIND)
                .mode(Mode.STREAMING)
                .homepage("https://github.com/jvde-github/AIS-catcher")
                .attribution("The operator's own AIS receiver")
                .licence("Operator's own data (no upstream license)")
                .defaultBudget(BUDGET)
                .reconnect(RECONNECT)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .egress(Egress.LOCAL_ONLY)
                .egressOverridable(true)
                .param(ParamSpec.lanUrl(ENDPOINT, "The AIS decoder's NMEA feed, set by the AIS aggregator:"
                        + " tcp://host:port (its TCP server) or udp://bindHost:port (where to listen for its"
                        + " datagrams); the port is always explicit", true, "tcp", "udp"))
                .param(ParamSpec.optional(ALLOW_SENDERS, ParamType.STRING_LIST, "With a udp:// endpoint, the IPs or"
                        + " CIDRs of the decoder hosts whose datagrams are read; private, loopback, link-local or"
                        + " unique-local only", null))
                .param(ParamSpec.optional(STATION_NAME, ParamType.STRING, "Your station's name, the observer of every"
                        + " record", DEFAULT_STATION))
                .param(ParamSpec.optional(PUBLISH_POSITION, ParamType.BOOLEAN, "Emit one ais-receiver record with the"
                        + " station's position (stationLat, stationLon); off by default", false))
                .param(ParamSpec.optional(STATION_LAT, ParamType.NUMBER, "The station's latitude; only with"
                        + " publishStationPosition", null))
                .param(ParamSpec.optional(STATION_LON, ParamType.NUMBER, "The station's longitude; only with"
                        + " publishStationPosition", null));
        VesselDescriptors.apply(b);
        b.kindDisplay(RECEIVER_KIND, new KindDisplay("{name}", "antenna", "#2B8A3E", null, null, null, null));
        return b.build();
    }
}
