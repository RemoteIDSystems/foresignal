package ai.foresignal.source.adapters.ais;

import ai.foresignal.codec.ais.VesselStateMerger;
import ai.foresignal.source.codec.AivdmSocketCodec;
import ai.foresignal.source.codec.CodecContext;
import ai.foresignal.source.formats.ais.VesselDescriptors;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HostPort;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.support.ConnectRateLimiter;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * {@code ais-kystverket} (prompt 085): the Norwegian Coastal Administration's open AIS stream, NMEA lines with TAG
 * blocks over raw TCP from {@value #HOST}, decoded by the shared {@value AivdmSocketCodec#ID} codec.
 *
 * <ul>
 *   <li><strong>Connection:</strong> {@code ctx.sockets().tcpPublic(…)} to the descriptor's only {@code allowedHosts}
 *       entry, {@link SocketOptions#receiveOnly()} (nothing is ever written), connect timeout 20 s, a connection silent
 *       for 120 s is closed. Reconnects back off from 30 s to 15 min with ±20% jitter; one budget unit per connect
 *       (1/min, 200/day) and at most 12 connects in any hour.
 *   <li><strong>Best effort</strong> (verified 2026-10): Kystverket's access page still lists the endpoint as open
 *       and registration-free under NLOD 2.0, but a TCP connect from a US test host timed out. Health shows
 *       {@code failing} while it is unreachable; Foresignal never routes around it.
 *   <li><strong>Time:</strong> {@code observedAtMode} UPSTREAM: {@code observedAt} is the TAG block's {@code c} (seconds
 *       or milliseconds). A line without {@code c} is dropped and counted, never stamped with the receive time.
 *   <li><strong>Observer:</strong> the TAG block's {@code s}, the receiving base station.
 *   <li><strong>Coverage</strong> (open tier): the Norwegian economic zone and the Svalbard and Jan Mayen protection
 *       zones; fishing vessels under 15 m and leisure craft under 45 m are excluded upstream.
 *   <li><strong>License:</strong> NLOD 2.0, egress OPEN.
 * </ul>
 */
@Component
public class KystverketAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = "ais-kystverket";

    /** The open stream. */
    public static final String HOST = "153.44.253.27:5631";

    /** The attribution NLOD 2.0 asks for. */
    public static final String ATTRIBUTION = "Contains data under the Norwegian licence for Open Government data (NLOD)"
            + " distributed by the Norwegian Coastal Administration";

    /** The connect timeout. */
    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(20);

    /** The silence watchdog. */
    static final Duration SILENCE = Duration.ofSeconds(120);

    /** The reconnect ladder: 30 s doubling to 15 min, ±20% jitter. */
    static final ReconnectPolicy RECONNECT = new ReconnectPolicy(Duration.ofSeconds(30), Duration.ofMinutes(15), 0.2);

    /** The most connects in any hour. */
    static final int CONNECTS_PER_HOUR = 12;

    /** One unit per connect: one a minute, 200 a day. */
    static final Budget BUDGET = Budget.of(1, 200);

    private final AivdmSocketCodec codec;
    private final ConnectRateLimiter connects = new ConnectRateLimiter(CONNECTS_PER_HOUR, Duration.ofHours(1));
    private final AisStationStats stats = new AisStationStats();
    private final SourceDescriptor descriptor = buildDescriptor();
    private volatile AivdmSocketCodec.Session session;

    /**
     * Creates the adapter.
     *
     * @param codec the {@value AivdmSocketCodec#ID} codec
     */
    public KystverketAdapter(AivdmSocketCodec codec) {
        this.codec = codec;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Connects receive-only and decodes until the handle is closed.
     *
     * @throws AdapterException when the hourly connect cap or the budget is spent, or the host cannot be reached
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        Instant now = ctx.clock().instant();
        if (!connects.tryAcquire(now)) {
            throw new AdapterException(ID + ": " + CONNECTS_PER_HOUR + " connects in the last hour; waiting for the next"
                    + " attempt");
        }
        AivdmSocketCodec.Session next = newSession(ctx.sourceId());
        session = next;
        return ctx.sockets().tcpPublic(HostPort.parse(HOST), options(), codec.framing(),
                (frame, receivedAt) -> sink.acceptAll(next.onFrame(frame, receivedAt)));
    }

    /** The socket options: receive-only, 20 s connect timeout, 120 s silence watchdog. */
    static SocketOptions options() {
        return SocketOptions.receiveOnly().withConnectTimeout(CONNECT_TIMEOUT).withSilenceTimeout(SILENCE);
    }

    /** A session with TAG time and the TAG station as observer (tests and the ingest test). */
    AivdmSocketCodec.Session newSession(String sourceId) {
        return codec.newSession(CodecContext.of(sourceId),
                new AivdmSocketCodec.Options(AivdmSocketCodec.TimeBase.TAG, true, null), stats::message);
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

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Kystverket open AIS (Norway)")
                .description("Keyless official AIS from the Norwegian Coastal Administration: NMEA with TAG blocks over"
                        + " raw TCP from 153.44.253.27:5631, receive-only. Open tier: the Norwegian economic zone and"
                        + " the Svalbard and Jan Mayen protection zones; fishing vessels under 15 m and leisure craft"
                        + " under 45 m are excluded upstream. observedAt is the TAG time; the receiving base station is"
                        + " the observer. Best effort: the endpoint was listed as open in 2026-10 but did not answer"
                        + " from a US test host, so health shows failing while it is unreachable.")
                .kind(VesselDescriptors.KIND)
                .mode(Mode.STREAMING)
                .homepage("https://www.kystverket.no/en/sea-transport-and-ports/ais/access-to-ais-data/")
                .attribution(ATTRIBUTION)
                .licence("NLOD-2.0")
                .termsUrl("https://data.norge.no/nlod/en/2.0")
                .egress(Egress.OPEN)
                .defaultBudget(BUDGET)
                // Unreachable from a US host (TCP connect times out, 2026-10-22), so it is not switched on unasked.
                .autoEnable(false)
                .allowedHost(HOST)
                .reconnect(RECONNECT)
                .observedAtMode(ObservedAtMode.UPSTREAM);
        VesselDescriptors.apply(b);
        return b.build();
    }
}
