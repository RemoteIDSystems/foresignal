package ai.foresignal.source.adapters.acars;

import ai.foresignal.source.codec.AcarsJsonCodec;
import ai.foresignal.source.codec.CodecContext;
import ai.foresignal.source.codec.CodecSession;
import ai.foresignal.source.formats.acars.AcarsMetadataMapper;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
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
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * One {@code local-acars-{n}} slot (prompt 086): aviation datalink <em>metadata</em> from the operator's own
 * acars_router (https://github.com/sdr-enthusiasts/acars_router), decoded by codec {@value AcarsJsonCodec#ID}.
 *
 * <ul>
 *   <li><strong>Params:</strong> {@code acarsEndpoint}, {@code vdlm2Endpoint}, {@code hfdlEndpoint}, each an optional
 *       {@code tcp://host:port} {@code LAN_URL} to one of acars_router's TCP servers (its defaults are 15550, 15555
 *       and 15556; no port is assumed), at least one required; {@code stationId}, the observer.
 *   <li><strong>Connections:</strong> one receive-only connection per configured endpoint
 *       ({@link SocketOptions#receiveOnly()}), each paying one budget unit; when any drops, all close and the
 *       supervisor reconnects them together on its ladder.
 *   <li><strong>Metadata only:</strong> message text is never stored, logged, embedded or forwarded, and no parameter
 *       enables it (interception-disclosure laws such as 18 U.S.C. §2511 and the UK Wireless Telegraphy Act 2006 s.48).
 *   <li><strong>Records:</strong> kind {@value AcarsMetadataMapper#KIND}, externalId
 *       {@code protocol|station|epochMillis|tail|msgNum}, {@code eventTime = observedAt} = the decoder's time, STANDARD,
 *       ALL, kept 7 days, no TTL, no embedding.
 *   <li><strong>Private by descriptor</strong> (prompt 039): effective privacy is descriptor OR settings, so no
 *       setting can make it public; excluded from {@code openReads}, tiles, embeddings, LLM context and sinks, and its
 *       {@code LOCAL_ONLY} egress is not overridable.
 * </ul>
 */
public class LocalAcarsAdapter implements StreamingAdapter {

    /** The id prefix of the slots. */
    public static final String ID_PREFIX = "local-acars-";

    /** The ACARS endpoint parameter. */
    public static final String ACARS_ENDPOINT = "acarsEndpoint";

    /** The VDL Mode 2 endpoint parameter. */
    public static final String VDLM2_ENDPOINT = "vdlm2Endpoint";

    /** The HFDL endpoint parameter. */
    public static final String HFDL_ENDPOINT = "hfdlEndpoint";

    /** The endpoint parameters, in connection order. */
    public static final List<String> ENDPOINTS = List.of(ACARS_ENDPOINT, VDLM2_ENDPOINT, HFDL_ENDPOINT);

    /** The station parameter (the observer). */
    public static final String STATION_ID = AcarsJsonCodec.STATION_PARAM;

    /** The retention named by prompt 086. */
    static final Duration RETENTION = Duration.ofDays(7);

    /** The reconnect ladder: 5 s doubling to 5 min. */
    static final ReconnectPolicy RECONNECT = ReconnectPolicy.exponential(Duration.ofSeconds(5), Duration.ofMinutes(5));

    /** A station id: letters, digits, dot, underscore and hyphen, 1 to 32. */
    static final Pattern STATION = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,31}$");

    private final SourceDescriptor descriptor;
    private final AcarsJsonCodec codec;

    /**
     * Creates one slot.
     *
     * @param slot the slot number, from 1
     * @param codec the {@value AcarsJsonCodec#ID} codec
     */
    public LocalAcarsAdapter(int slot, AcarsJsonCodec codec) {
        this.descriptor = buildDescriptor(slot);
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** At least one endpoint, and a well-formed station id. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (configured(params).isEmpty()) {
            problems.put(ACARS_ENDPOINT, "set at least one of acarsEndpoint, vdlm2Endpoint and hfdlEndpoint");
        }
        String station = params.string(STATION_ID);
        if (station != null && !station.isBlank() && !STATION.matcher(station.strip()).matches()) {
            problems.put(STATION_ID, "must be 1 to 32 letters, digits, dots, underscores or hyphens");
        }
        return problems;
    }

    /** The configured endpoint parameters, in order. */
    static List<String> configured(ValidatedParams params) {
        List<String> out = new ArrayList<>();
        for (String name : ENDPOINTS) {
            if (params.lanUrl(name) != null) {
                out.add(name);
            }
        }
        return out;
    }

    /**
     * Opens one receive-only connection per configured endpoint and decodes until the handle is closed.
     *
     * @throws AdapterException when the parameters are invalid, the budget is spent or a connection fails (any
     *     connection already opened is closed again)
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        ValidatedParams params = ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ctx.sourceId() + ": " + problems);
        }
        List<StreamHandle> handles = new ArrayList<>();
        try {
            for (String name : configured(params)) {
                LanEndpoint endpoint = LanEndpoint.fromParams(params, name);
                CodecSession session = codec.newSession(new CodecContext(ctx.sourceId(), params));
                handles.add(ctx.sockets().tcp(endpoint, SocketOptions.receiveOnly(), codec.framing(),
                        (frame, receivedAt) -> sink.acceptAll(session.onFrame(frame, receivedAt))));
            }
        } catch (AdapterException | RuntimeException e) {
            handles.forEach(StreamHandle::close);
            throw e;
        }
        return new CompositeHandle(handles);
    }

    /** Several connections run as one stream: closing closes all, and the first drop ends all (package-visible). */
    static final class CompositeHandle implements StreamHandle {

        private final List<StreamHandle> handles;
        private final AtomicBoolean ended = new AtomicBoolean();
        private volatile Consumer<Throwable> callback;
        private volatile Throwable cause;

        CompositeHandle(List<StreamHandle> handles) {
            this.handles = List.copyOf(handles);
            for (StreamHandle h : this.handles) {
                h.onClose(this::childEnded);
            }
        }

        private void childEnded(Throwable error) {
            if (ended.compareAndSet(false, true)) {
                cause = error == null ? new java.io.IOException("an acars_router connection closed") : error;
                handles.forEach(StreamHandle::close);
                Consumer<Throwable> listener = callback;
                if (listener != null) {
                    listener.accept(cause);
                }
            }
        }

        @Override
        public void close() {
            ended.set(true);
            handles.forEach(StreamHandle::close);
        }

        @Override
        public boolean isRunning() {
            return !ended.get() && handles.stream().allMatch(StreamHandle::isRunning);
        }

        @Override
        public void onClose(Consumer<Throwable> listener) {
            callback = listener;
            if (ended.get() && cause != null && listener != null) {
                listener.accept(cause);
            }
        }

        /** The connections, for tests. */
        List<StreamHandle> handles() {
            return handles;
        }
    }

    private static SourceDescriptor buildDescriptor(int slot) {
        String kind = AcarsMetadataMapper.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID_PREFIX + slot)
                .name("Local aviation datalink (acars_router) " + slot)
                .description("ACARS, VDL Mode 2 and HFDL message metadata from the operator's own acars_router TCP"
                        + " servers (its defaults are 15550, 15555 and 15556; the ports are always explicit)."
                        + " Metadata only: message text is never stored, logged, embedded or forwarded, because"
                        + " interception-disclosure laws (18 U.S.C. §2511, UK Wireless Telegraphy Act 2006 s.48) forbid"
                        + " redistributing it. Private by descriptor: never served anonymously, tiled, embedded, sent to"
                        + " an LLM or forwarded to a sink. Receive-only. Disabled by default; egress LOCAL_ONLY, not"
                        + " overridable.")
                .kind(kind)
                .mode(Mode.STREAMING)
                .homepage("https://github.com/sdr-enthusiasts/acars_router")
                .attribution("The operator's own aviation datalink receivers")
                .licence("Operator's own data (no upstream license)")
                .defaultBudget(Budget.of(6, 600))
                .reconnect(RECONNECT)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .defaultRetention(RETENTION)
                .zeroResultsNormal(true)
                .privateSource(true)
                .egress(Egress.LOCAL_ONLY)
                .egressOverridable(false)
                .titleField("flight")
                .searchFields("tail", "flight", "label")
                .param(ParamSpec.lanUrl(ACARS_ENDPOINT, "acars_router's ACARS TCP server, tcp://host:port (its default"
                        + " port is 15550; always explicit)", false, "tcp"))
                .param(ParamSpec.lanUrl(VDLM2_ENDPOINT, "acars_router's VDLM2 TCP server, tcp://host:port (its default"
                        + " port is 15555; always explicit)", false, "tcp"))
                .param(ParamSpec.lanUrl(HFDL_ENDPOINT, "acars_router's HFDL TCP server, tcp://host:port (its default"
                        + " port is 15556; always explicit)", false, "tcp"))
                .param(ParamSpec.optional(STATION_ID, ParamType.STRING, "Your station's id, the observer of every"
                        + " record; defaults to the decoder's station_id", null));
        Set<String> kinds = Set.of(kind);
        b.fieldSpec("protocol", new FieldSpec(FieldType.STRING, null, "Protocol", "acars, vdl2 or hfdl", null, kinds));
        b.fieldSpec("tail", new FieldSpec(FieldType.STRING, null, "Registration", "Aircraft registration", null, kinds));
        b.fieldSpec("flight", new FieldSpec(FieldType.STRING, null, "Flight", "Flight number", null, kinds));
        b.fieldSpec("label", new FieldSpec(FieldType.STRING, null, "Label", "ACARS message label", null, kinds));
        b.fieldSpec("mode", new FieldSpec(FieldType.STRING, null, "Mode", "ACARS mode character", null, kinds));
        b.fieldSpec("blockId", new FieldSpec(FieldType.STRING, null, "Block id", "ACARS block id", null, kinds));
        b.fieldSpec("msgNum", new FieldSpec(FieldType.STRING, null, "Message number", "ACARS message sequence number",
                null, kinds));
        b.fieldSpec("freqMhz", new FieldSpec(FieldType.NUMBER, "MHz", "Frequency", "Receive frequency", null, kinds));
        b.fieldSpec("signalDbm", new FieldSpec(FieldType.NUMBER, "dB", "Signal level",
                "Signal level as the decoder reports it (dB or dBFS)", null, kinds));
        b.fieldSpec("icaoHex", new FieldSpec(FieldType.STRING, null, "ICAO address", "Aircraft 24-bit address, hex",
                null, kinds));
        b.fieldSpec("groundStation", new FieldSpec(FieldType.STRING, null, "Ground station",
                "VDL2 ground station address or HFDL ground station id", null, kinds));
        b.fieldSpec("hasText", new FieldSpec(FieldType.BOOLEAN, null, "Has text",
                "The message carried free text (never stored)", null, kinds));
        b.kindDisplay(kind, new KindDisplay("{flight} {label}", "plane", "#495057", null));
        return b.build();
    }
}
