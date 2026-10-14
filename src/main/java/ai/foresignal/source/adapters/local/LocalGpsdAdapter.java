package ai.foresignal.source.adapters.local;

import ai.foresignal.source.codec.CodecContext;
import ai.foresignal.source.codec.GpsdCodec;
import ai.foresignal.source.codec.GpsdRawSplitter;
import ai.foresignal.source.codec.SocketCodec;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One {@code local-gpsd-{n}} slot (prompt 069): the operator's GNSS receiver through gpsd's JSON protocol
 * over TCP, decoded by codec {@code gpsd}.
 *
 * <ul>
 *   <li><strong>Param:</strong> {@code endpoint}, a {@code tcp://host:port} {@code LAN_URL} (gpsd's port is
 *       conventionally 2947, never assumed). On connect {@code ?WATCH={"enable":true,"json":true};} is sent and
 *       a {@code VERSION} with {@code proto_major} 3 is expected.
 *   <li><strong>Records:</strong> kind {@code gnss-fix}, one per {@code device}; {@code observedAt} is the TPV
 *       {@code time} (UPSTREAM).
 *   <li><strong>Policy:</strong> STANDARD, LATEST {@code {keyframe 20m, edgeMin 15s}}, significant
 *       {@code mode, fixQuality, navDegraded}, TTL 30 min, retention 365 d ({@code state}).
 *   <li><strong>Private:</strong> it is the operator's own location, so prompt 039 keeps it out of
 *       {@code openReads}, tiles, embeddings and LLM context.
 *   <li><strong>GNSS integrity sentinel</strong> (prompt 086): a second kind, {@code gnss-integrity}, one record
 *       per device per closed 30 s window ({@code integrityState} OK, DEGRADED, JAMMED, SUSPECT_SPOOF or LOST,
 *       significant; the measurements volatile), kept 30 days. Params {@code fixedStation} (default true; enables
 *       the spoofing rule), {@code ubxMonitor} (default false; adds {@code "raw":2} to {@code ?WATCH} and reads
 *       u-blox MON-HW/MON-RF jamming from the passthrough) and {@code stationLabel} (names the station in log
 *       messages). A state change is logged once at INFO.
 *   <li><strong>Writes:</strong> only the {@code ?WATCH} of {@link GpsdCodec#watchCommand}, which passes
 *       {@code GpsdCommandGuard} ({@code ?WATCH}, {@code ?VERSION}, {@code ?DEVICES}, {@code ?POLL}); the socket
 *       writes nothing after it. UBX is decoded, never sent.
 * </ul>
 */
public class LocalGpsdAdapter implements StreamingAdapter {

    private static final Logger log = LoggerFactory.getLogger(LocalGpsdAdapter.class);

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "local-gpsd-";

    /** The endpoint parameter. */
    public static final String ENDPOINT_PARAM = "endpoint";

    /** The keyframe of the LATEST policy. */
    static final Duration KEYFRAME = Duration.ofMinutes(20);

    /** The trigger-edge interval. */
    static final Duration EDGE_MIN_INTERVAL = Duration.ofSeconds(15);

    /** The record TTL. */
    static final Duration TTL = Duration.ofMinutes(30);

    /** The {@code state} class retention. */
    static final Duration RETENTION = Duration.ofDays(365);

    /** The retention of the integrity kind, named by prompt 086. */
    static final Duration INTEGRITY_RETENTION = Duration.ofDays(30);

    /** The fixed-station parameter (prompt 086). */
    public static final String FIXED_STATION = GpsdCodec.FIXED_STATION_PARAM;

    /** The u-blox monitor parameter (prompt 086). */
    public static final String UBX_MONITOR = "ubxMonitor";

    /** The station label parameter (prompt 086). */
    public static final String STATION_LABEL = "stationLabel";

    private final SourceDescriptor descriptor;
    private final SocketCodec codec;
    private final Map<String, String> lastStates = new ConcurrentHashMap<>();

    /**
     * Creates one slot.
     *
     * @param slot the slot number, from 1
     * @param codec the {@code gpsd} codec
     */
    public LocalGpsdAdapter(int slot, SocketCodec codec) {
        this.descriptor = buildDescriptor(slot);
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Connects, sends the guarded {@code ?WATCH} and decodes until the handle is closed. With {@code ubxMonitor} the
     * stream is read raw and split into JSON lines and UBX frames.
     *
     * @throws AdapterException when the budget is spent or gpsd cannot be reached
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        ValidatedParams params = ctx.params();
        boolean ubx = params.bool(UBX_MONITOR, false);
        LanEndpoint endpoint = LanEndpoint.fromParams(params, ENDPOINT_PARAM);
        SocketOptions options = options(ubx);
        String label = label(ctx.sourceId(), params);
        if (!(codec instanceof GpsdCodec gpsd)) {
            return CodecStream.start(ctx, sink, codec, ENDPOINT_PARAM, options);
        }
        GpsdCodec.Session session = (GpsdCodec.Session) gpsd.newSession(new CodecContext(ctx.sourceId(), params));
        if (!ubx) {
            return ctx.sockets().tcp(endpoint, options, Framing.LINES,
                    (frame, receivedAt) -> sink.acceptAll(logStates(label, session.onFrame(frame, receivedAt))));
        }
        GpsdRawSplitter splitter = new GpsdRawSplitter();
        return ctx.sockets().tcp(endpoint, options, Framing.RAW, (chunk, receivedAt) -> {
            List<NormalizedRecord> out = new ArrayList<>();
            for (GpsdRawSplitter.Frame frame : splitter.feed(chunk)) {
                switch (frame) {
                    case GpsdRawSplitter.JsonLine line -> out.addAll(session.onFrame(line.bytes(), receivedAt));
                    case GpsdRawSplitter.Ubx ubxFrame -> out.addAll(session.onUbx(ubxFrame.bytes(), receivedAt));
                }
            }
            sink.acceptAll(logStates(label, out));
        });
    }

    /**
     * The connection options: the guarded {@code ?WATCH} once on connect, then nothing (package-visible for the
     * tests).
     */
    static SocketOptions options(boolean ubxMonitor) {
        return SocketOptions.receiveOnlyAfterConnect().withOnConnect(GpsdCodec.watchCommand(ubxMonitor));
    }

    /** The station label: the parameter, else the source id. */
    static String label(String sourceId, ValidatedParams params) {
        String given = params.string(STATION_LABEL);
        return given == null || given.isBlank() ? sourceId : given.strip();
    }

    /** Logs each device's integrity state when it changes; returns {@code records} unchanged. */
    List<NormalizedRecord> logStates(String label, List<NormalizedRecord> records) {
        for (NormalizedRecord r : records) {
            if (!GpsdCodec.INTEGRITY_KIND.equals(r.kind())) {
                continue;
            }
            String state = String.valueOf(r.payload().get("integrityState"));
            String previous = lastStates.put(r.externalId(), state);
            if (!state.equals(previous)) {
                Object reason = r.quality() == null ? null : r.quality().extra().get("gnss.integrity_reason");
                log.info("GNSS integrity at {} ({}): {} ({})", label, r.externalId(), state, reason);
            }
        }
        return records;
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID_PREFIX + slot)
                .name("Local GNSS receiver " + slot)
                .description("The operator's GNSS fix from gpsd's JSON protocol over TCP (gpsd's port is"
                        + " conventionally 2947). Private: it is the operator's own location, never served"
                        + " anonymously, tiled, embedded or sent to a remote LLM." + LocalReceivers.LOCAL_NOTE)
                .kinds(GpsdCodec.KIND, GpsdCodec.INTEGRITY_KIND)
                .mode(Mode.STREAMING)
                .defaultBudget(2, 200)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .defaultIngestPolicy(new IngestPolicy.Latest(KEYFRAME, EDGE_MIN_INTERVAL))
                .significantFields("mode", "fixQuality", "navDegraded", "integrityState")
                .volatileFields("cn0MeanDb", "jamIndicator", "clockOffsetNs")
                .kindRetention(GpsdCodec.INTEGRITY_KIND, INTEGRITY_RETENTION)
                .titleField("fixQuality")
                .searchFields()
                .privateSource(true)
                .param(ParamSpec.lanUrl(ENDPOINT_PARAM, "gpsd on the LAN, tcp://host:port, for example"
                        + " tcp://192.168.1.60:2947; the port is always explicit", true, "tcp"))
                .param(ParamSpec.optional(FIXED_STATION, ParamType.BOOLEAN, "The antenna does not move; enables the"
                        + " spoofing rule (a position jump over 500 m or a clock step over 1 s while C/N0 rises)", true))
                .param(ParamSpec.optional(UBX_MONITOR, ParamType.BOOLEAN, "Ask gpsd for its raw passthrough (\"raw\":2)"
                        + " and read a u-blox receiver's own jamming state from UBX-MON-HW and UBX-MON-RF; nothing is"
                        + " ever sent to the receiver", false))
                .param(ParamSpec.optional(STATION_LABEL, ParamType.STRING, "A name for the station in integrity"
                        + " messages; defaults to the source id", null));
        LocalReceivers.local(builder);
        fieldSpecs(builder);
        builder.kindDisplay(GpsdCodec.KIND, new KindDisplay("GNSS {fixQuality}", "marker", "#5F3DC4", null));
        builder.kindDisplay(GpsdCodec.INTEGRITY_KIND, new KindDisplay("GNSS integrity {integrityState}", "caution",
                "#E8590C", null));
        return builder.build();
    }

    private static void fieldSpecs(SourceDescriptor.Builder b) {
        b.fieldSpec("mode", new FieldSpec(FieldType.INTEGER, "1", "Fix mode", "gpsd mode: 0 unknown, 1 no fix, 2 2D, 3 3D",
                null, Set.of()));
        b.fieldSpec("fixQuality", new FieldSpec(FieldType.STRING, null, "Fix quality", "none, 2d or 3d", null, Set.of()));
        b.fieldSpec("navDegraded", new FieldSpec(FieldType.BOOLEAN, null, "Navigation degraded",
                "True when mode < 3, fewer than 4 satellites are used, or eph exceeds 50 m", null, Set.of()));
        b.fieldSpec("altHAE", new FieldSpec(FieldType.NUMBER, "m", "Height above ellipsoid",
                "Altitude above the WGS84 ellipsoid in meters, 3D fixes only", "WGS84 ellipsoid", Set.of()));
        b.fieldSpec("eph", new FieldSpec(FieldType.NUMBER, "m", "Horizontal error",
                "Estimated horizontal position error (95%) in meters", null, Set.of()));
        b.fieldSpec("epv", new FieldSpec(FieldType.NUMBER, "m", "Vertical error",
                "Estimated vertical error (95%) in meters", null, Set.of()));
        b.fieldSpec("ept", new FieldSpec(FieldType.NUMBER, "s", "Time error", "Estimated time error in seconds", null,
                Set.of()));
        b.fieldSpec("speed", new FieldSpec(FieldType.NUMBER, "m/s", "Speed", "Speed over ground in m/s", null, Set.of()));
        b.fieldSpec("track", new FieldSpec(FieldType.NUMBER, "deg", "Track", "Course over ground, degrees from true north",
                null, Set.of()));
        b.fieldSpec("nSat", new FieldSpec(FieldType.INTEGER, "1", "Satellites seen", "Satellites in view", null, Set.of()));
        b.fieldSpec("uSat", new FieldSpec(FieldType.INTEGER, "1", "Satellites used", "Satellites used in the fix", null,
                Set.of()));
        b.fieldSpec("hdop", new FieldSpec(FieldType.NUMBER, "1", "HDOP", "Horizontal dilution of precision", null,
                Set.of()));
        Set<String> integrity = Set.of(GpsdCodec.INTEGRITY_KIND);
        b.fieldSpec("integrityState", new FieldSpec(FieldType.STRING, null, "Integrity state",
                "OK, DEGRADED, JAMMED, SUSPECT_SPOOF or LOST, judged per 30 s window against a 15-minute baseline",
                null, integrity));
        b.fieldSpec("cn0MeanDb", new FieldSpec(FieldType.NUMBER, "dB", "Mean C/N0",
                "Mean carrier-to-noise density of the used satellites (dBHz)", null, integrity));
        b.fieldSpec("jamIndicator", new FieldSpec(FieldType.INTEGER, "1", "Jamming indicator",
                "The u-blox receiver's CW jamming indicator, 0 to 255 (with ubxMonitor)", null, integrity));
        b.fieldSpec("clockOffsetNs", new FieldSpec(FieldType.INTEGER, "ns", "Clock offset",
                "System clock minus GNSS time from gpsd's PPS or TOFF, in nanoseconds", null, integrity));
    }
}
