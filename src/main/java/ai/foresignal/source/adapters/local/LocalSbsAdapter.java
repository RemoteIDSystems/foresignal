package ai.foresignal.source.adapters.local;

import ai.foresignal.source.codec.SocketCodec;
import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.spi.AdapterException;
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
import java.util.Objects;

/**
 * One {@code local-sbs-{n}} slot (prompt 069): SBS-1 BaseStation lines from the operator's decoder over TCP,
 * decoded by codec {@code sbs1}.
 *
 * <ul>
 *   <li><strong>Param:</strong> {@code endpoint}, a {@code tcp://host:port} {@code LAN_URL}. readsb serves SBS
 *       only with {@code --net-sbs-port} (conventionally 30003, never assumed).
 *   <li><strong>Time base:</strong> SBS times are the decoder's local wall clock without a zone, so
 *       {@code observedAtMode} is RECEIVED (the framework imputes {@code observedAt} and flags
 *       {@code TIME_IMPUTED}) with {@code ttlAnchor} OBSERVED; the SBS times are never parsed.
 *   <li><strong>Policy:</strong> the same as {@code local-adsb}: TRACK, TTL 5 min, SAMPLED {@code {10s, 120s,
 *       300 m, thresholds, edgeMin 5s}}. Sessions forget a hex after 5 minutes of silence.
 *   <li><strong>Connection:</strong> one budget unit per connect, 2/min and 200/day by default; a connection
 *       silent for 120 s is closed and the supervisor's ladder reconnects.
 * </ul>
 */
public class LocalSbsAdapter implements StreamingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "local-sbs-";

    /** The endpoint parameter. */
    public static final String ENDPOINT_PARAM = "endpoint";

    private final SourceDescriptor descriptor;
    private final SocketCodec codec;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, from 1
     * @param codec the {@code sbs1} codec
     */
    public LocalSbsAdapter(int slot, SocketCodec codec) {
        this.descriptor = buildDescriptor(slot);
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Connects and decodes until the handle is closed.
     *
     * @throws AdapterException when the budget is spent or the decoder cannot be reached
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        return CodecStream.start(ctx, sink, codec, ENDPOINT_PARAM, SocketOptions.defaults());
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID_PREFIX + slot)
                .name("Local SBS feed " + slot)
                .description("Aircraft from the operator's decoder as SBS-1 (BaseStation) lines over TCP. readsb"
                        + " serves them only with --net-sbs-port (conventionally 30003). The SBS times have no zone,"
                        + " so observedAt is the receive time. TRACK, TTL 5 min." + LocalReceivers.LOCAL_NOTE)
                .mode(Mode.STREAMING)
                .defaultBudget(2, 200)
                .param(ParamSpec.lanUrl(ENDPOINT_PARAM, "The decoder's SBS output, tcp://host:port, for example"
                        + " tcp://192.168.1.50:30003; the port is always explicit", true, "tcp"));
        LocalReceivers.local(builder);
        AircraftDescriptors.local(builder, ObservedAtMode.RECEIVED);
        builder.kindDisplay(AircraftFields.KIND, new KindDisplay("{flight}", "plane", "#0B7285", null,
                AircraftFields.TRACK, null, null));
        return builder.build();
    }
}
