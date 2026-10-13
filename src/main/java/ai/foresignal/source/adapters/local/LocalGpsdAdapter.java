package ai.foresignal.source.adapters.local;

import ai.foresignal.source.codec.GpsdCodec;
import ai.foresignal.source.codec.SocketCodec;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;

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
 *       {@code openReads}, tiles, embeddings and LLM context. Prompt 086 builds the GNSS-integrity sentinel on
 *       it.
 * </ul>
 */
public class LocalGpsdAdapter implements StreamingAdapter {

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

    private final SourceDescriptor descriptor;
    private final SocketCodec codec;

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
     * Connects, sends {@code ?WATCH} and decodes until the handle is closed.
     *
     * @throws AdapterException when the budget is spent or gpsd cannot be reached
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        return CodecStream.start(ctx, sink, codec, ENDPOINT_PARAM, SocketOptions.defaults());
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID_PREFIX + slot)
                .name("Local GNSS receiver " + slot)
                .description("The operator's GNSS fix from gpsd's JSON protocol over TCP (gpsd's port is"
                        + " conventionally 2947). Private: it is the operator's own location, never served"
                        + " anonymously, tiled, embedded or sent to a remote LLM." + LocalReceivers.LOCAL_NOTE)
                .kind(GpsdCodec.KIND)
                .mode(Mode.STREAMING)
                .defaultBudget(2, 200)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .defaultIngestPolicy(new IngestPolicy.Latest(KEYFRAME, EDGE_MIN_INTERVAL))
                .significantFields("mode", "fixQuality", "navDegraded")
                .titleField("fixQuality")
                .searchFields()
                .privateSource(true)
                .param(ParamSpec.lanUrl(ENDPOINT_PARAM, "gpsd on the LAN, tcp://host:port, for example"
                        + " tcp://192.168.1.60:2947; the port is always explicit", true, "tcp"));
        LocalReceivers.local(builder);
        fieldSpecs(builder);
        builder.kindDisplay(GpsdCodec.KIND, new KindDisplay("GNSS {fixQuality}", "marker", "#5F3DC4", null));
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
    }
}
