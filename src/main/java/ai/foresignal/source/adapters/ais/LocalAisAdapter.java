package ai.foresignal.source.adapters.ais;

import ai.foresignal.codec.ais.VesselStateMerger;
import ai.foresignal.source.codec.AivdmSocketCodec;
import ai.foresignal.source.codec.CodecContext;
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
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * One {@code local-ais-{n}} slot (prompt 085): AIS heard by the operator's own AIS-catcher, read as NMEA lines over
 * TCP (AIS-catcher started with {@code -S <port>} as a TCP server) and decoded by the shared
 * {@value AivdmSocketCodec#ID} codec.
 *
 * <ul>
 *   <li><strong>Params:</strong> {@code endpoint}, a {@code tcp://host:port} {@code LAN_URL} with no default port;
 *       {@code stationName}, the observer of every record (default {@value #DEFAULT_STATION});
 *       {@code publishStationPosition} (default false) with {@code stationLat}/{@code stationLon}.
 *   <li><strong>Receive-only:</strong> {@link SocketOptions#receiveOnly()}; nothing is ever written to the socket.
 *   <li><strong>Time:</strong> {@code observedAtMode} RECEIVED: AIS-catcher's NMEA carries no timestamp and LAN latency
 *       is negligible.
 *   <li><strong>Station position, private by default:</strong> only with {@code publishStationPosition=true} is one
 *       {@value #RECEIVER_KIND} record emitted with the station's position (on the first line of a connection and every
 *       10 minutes, so it stays live within the TTL). Otherwise the station's location is neither emitted nor stored:
 *       {@code stationLat} and {@code stationLon} are refused unless publishing is on.
 *   <li><strong>Health details:</strong> messages per minute, distinct MMSIs in 10 minutes and the last message.
 *   <li><strong>Egress:</strong> {@code LOCAL_ONLY}, overridable (prompt 069's rule for the operator's own receivers).
 *       For a remote station, the 059 AIS-catcher HTTP preset ({@code aiscatcher}) remains the alternative.
 * </ul>
 */
public class LocalAisAdapter implements StreamingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "local-ais-";

    /** The endpoint parameter. */
    public static final String ENDPOINT = "endpoint";

    /** The station name parameter (the observer). */
    public static final String STATION_NAME = AivdmSocketCodec.STATION_PARAM;

    /** The parameter that publishes the station's position. */
    public static final String PUBLISH_POSITION = "publishStationPosition";

    /** The station latitude parameter. */
    public static final String STATION_LAT = "stationLat";

    /** The station longitude parameter. */
    public static final String STATION_LON = "stationLon";

    /** The default station name. */
    public static final String DEFAULT_STATION = "local";

    /** The kind of the station record. */
    public static final String RECEIVER_KIND = "ais-receiver";

    /** How often the station record is repeated while connected. */
    static final Duration STATION_REPEAT = Duration.ofMinutes(10);

    /** The reconnect ladder: 5 s doubling to 5 min. */
    static final ReconnectPolicy RECONNECT = ReconnectPolicy.exponential(Duration.ofSeconds(5), Duration.ofMinutes(5));

    /** A station name: letters, digits, space, dot, underscore and hyphen, 1 to 40. */
    static final Pattern STATION = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 ._-]{0,39}$");

    private final SourceDescriptor descriptor;
    private final AivdmSocketCodec codec;
    private final AisStationStats stats = new AisStationStats();
    private volatile AivdmSocketCodec.Session session;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, from 1
     * @param codec the {@value AivdmSocketCodec#ID} codec
     */
    public LocalAisAdapter(int slot, AivdmSocketCodec codec) {
        this.descriptor = buildDescriptor(slot);
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Checks the station name, and the station position: required with {@code publishStationPosition}, refused without
     * it, and within range.
     */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String station = params.string(STATION_NAME);
        if (station != null && !station.isBlank() && !STATION.matcher(station.strip()).matches()) {
            problems.put(STATION_NAME, "must be 1 to 40 letters, digits, spaces, dots, underscores or hyphens");
        }
        boolean publish = params.bool(PUBLISH_POSITION, false);
        Double lat = params.number(STATION_LAT);
        Double lon = params.number(STATION_LON);
        if (!publish) {
            if (lat != null) {
                problems.put(STATION_LAT, "is stored only with publishStationPosition=true; leave it empty");
            }
            if (lon != null) {
                problems.put(STATION_LON, "is stored only with publishStationPosition=true; leave it empty");
            }
            return problems;
        }
        if (lat == null || lat < -90 || lat > 90) {
            problems.put(STATION_LAT, "is required with publishStationPosition and must be within [-90, 90]");
        }
        if (lon == null || lon < -180 || lon > 180) {
            problems.put(STATION_LON, "is required with publishStationPosition and must be within [-180, 180]");
        }
        return problems;
    }

    /**
     * Connects receive-only and decodes until the handle is closed.
     *
     * @throws AdapterException when the parameters are invalid, the budget is spent or AIS-catcher cannot be reached
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        ValidatedParams params = ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ctx.sourceId() + ": " + problems);
        }
        LanEndpoint endpoint = LanEndpoint.fromParams(params, ENDPOINT);
        Connection connection = newConnection(ctx.sourceId(), params);
        return ctx.sockets().tcp(endpoint, SocketOptions.receiveOnly(), codec.framing(),
                (frame, receivedAt) -> sink.acceptAll(connection.onFrame(frame, receivedAt)));
    }

    /** One connection's decoding and station record (package-visible for the tests). */
    Connection newConnection(String sourceId, ValidatedParams params) {
        String station = station(params);
        AivdmSocketCodec.Session next = codec.newSession(new CodecContext(sourceId, params),
                new AivdmSocketCodec.Options(AivdmSocketCodec.TimeBase.RECEIVED, false, station), stats::message);
        session = next;
        NormalizedRecord receiver = params.bool(PUBLISH_POSITION, false) ? stationRecord(station,
                params.number(STATION_LAT), params.number(STATION_LON)) : null;
        return new Connection(next, receiver);
    }

    /** The station name: the parameter, else {@value #DEFAULT_STATION}. */
    static String station(ValidatedParams params) {
        String given = params.string(STATION_NAME);
        return given == null || given.isBlank() ? DEFAULT_STATION : given.strip();
    }

    /** The {@value #RECEIVER_KIND} record of a published station, or null without a position. */
    static NormalizedRecord stationRecord(String station, Double lat, Double lon) {
        if (lat == null || lon == null) {
            return null;
        }
        return NormalizedRecord.builder(station, RECEIVER_KIND)
                .point(lon, lat)
                .put("name", station)
                .quality(Quality.positionSource(PositionSource.STATIC))
                .build();
    }

    /** The health details: the receiving statistics and the vessels held by the current connection. */
    public Map<String, Object> healthDetails(Instant now) {
        Map<String, Object> details = new LinkedHashMap<>(stats.details(now));
        AivdmSocketCodec.Session current = session;
        if (current != null) {
            VesselStateMerger merger = current.merger();
            details.put("vessels", merger.size());
        }
        return details;
    }

    /** One connection: the codec session and, when published, the station record repeated every 10 minutes. */
    static final class Connection {

        private final AivdmSocketCodec.Session session;
        private final NormalizedRecord receiver;
        private Instant receiverSentAt;

        Connection(AivdmSocketCodec.Session session, NormalizedRecord receiver) {
            this.session = session;
            this.receiver = receiver;
        }

        /** The records of one line, preceded by the station record when it is due. */
        List<NormalizedRecord> onFrame(byte[] frame, Instant receivedAt) {
            List<NormalizedRecord> decoded = session.onFrame(frame, receivedAt);
            if (receiver == null || (receiverSentAt != null && receivedAt.isBefore(receiverSentAt.plus(STATION_REPEAT)))) {
                return decoded;
            }
            receiverSentAt = receivedAt;
            List<NormalizedRecord> out = new ArrayList<>(decoded.size() + 1);
            out.add(receiver);
            out.addAll(decoded);
            return out;
        }

        /** The codec session. */
        AivdmSocketCodec.Session session() {
            return session;
        }
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID_PREFIX + slot)
                .name("Local AIS (AIS-catcher) " + slot)
                .description("AIS heard by the operator's own AIS-catcher, read as NMEA lines over TCP from a"
                        + " tcp://host:port on the operator's network (start AIS-catcher with -S <port>; the port is"
                        + " always explicit). Receive-only: nothing is written to the socket. stationName names the"
                        + " observer. The station's position is private by default: only with"
                        + " publishStationPosition=true (and stationLat/stationLon) is one ais-receiver record"
                        + " emitted. For a remote station, the aiscatcher HTTP preset remains the alternative."
                        + " Disabled by default; egress LOCAL_ONLY (the operator may set it to OPEN).")
                .kinds(VesselDescriptors.KIND, RECEIVER_KIND)
                .mode(Mode.STREAMING)
                .homepage("https://github.com/jvde-github/AIS-catcher")
                .attribution("The operator's own AIS receiver")
                .licence("Operator's own data (no upstream license)")
                .defaultBudget(Budget.of(2, 200))
                .reconnect(RECONNECT)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .egress(Egress.LOCAL_ONLY)
                .egressOverridable(true)
                .param(ParamSpec.lanUrl(ENDPOINT, "AIS-catcher's NMEA TCP server, tcp://host:port, for example"
                        + " tcp://192.168.1.30:5012 for AIS-catcher started with -S 5012; the port is always explicit",
                        true, "tcp"))
                .param(ParamSpec.optional(STATION_NAME, ParamType.STRING, "Your station's name, the observer of every"
                        + " record", DEFAULT_STATION))
                .param(ParamSpec.optional(PUBLISH_POSITION, ParamType.BOOLEAN, "Emit one ais-receiver record with the"
                        + " station's position (stationLat, stationLon); off by default, and the position is then"
                        + " neither stored nor emitted", false))
                .param(ParamSpec.optional(STATION_LAT, ParamType.NUMBER, "The station's latitude; only with"
                        + " publishStationPosition", null))
                .param(ParamSpec.optional(STATION_LON, ParamType.NUMBER, "The station's longitude; only with"
                        + " publishStationPosition", null));
        VesselDescriptors.apply(b);
        b.kindDisplay(RECEIVER_KIND, new KindDisplay("{name}", "antenna", "#2B8A3E", null, null, null, null));
        return b.build();
    }
}
