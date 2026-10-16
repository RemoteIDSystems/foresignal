package ai.foresignal.source.adapters.seismic;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ReconnectPolicy;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.WebSocketOptions;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code emsc-seismic} (prompt 089): the European-Mediterranean Seismological Centre's real-time WebSocket,
 * {@code wss://www.seismicportal.eu/standing_order/websocket}, with an FDSN gap-fill on every (re)connect so no
 * event is lost across a drop: the gap-fill pages until complete, and the cursor ({@value #CURSOR_KEY}) does not
 * move past a gap that is not yet filled ({@link EmscSession#gapFill}).
 *
 * <p>Quiet periods are ordinary ({@link SourceDescriptor#zeroResultsNormal()} is true); a connection silent for
 * {@value #SILENCE_RECYCLE} seconds is recycled, which is one step of the supervisor's ladder.
 */
@Component
public final class EmscSeismicAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = "emsc-seismic";

    /** The default WebSocket endpoint. */
    public static final String DEFAULT_URL = "wss://www.seismicportal.eu/standing_order/websocket";

    /** Test-only override of the WebSocket endpoint. */
    public static final String URL_PROPERTY = "foresignal.test.adapters.emsc-seismic.url";

    /** The default FDSN gap-fill endpoint. */
    public static final String DEFAULT_FDSN_URL = "https://www.seismicportal.eu/fdsnws/event/1/query";

    /** Test-only override of the FDSN gap-fill endpoint. */
    public static final String FDSN_URL_PROPERTY = "foresignal.test.adapters.emsc-seismic.fdsn-url";

    /** The keep-alive silence before the socket is recycled. */
    static final Duration SILENCE_RECYCLE = Duration.ofMinutes(10);

    /** The gap-fill window overlap. */
    static final Duration GAP_OVERLAP = Duration.ofMinutes(5);

    /** The gap-fill page size. */
    static final int GAP_LIMIT = 500;

    /** The gap-fill pages per attempt (with the connect, within the 12-a-minute budget). */
    static final int GAP_MAX_PAGES = 10;

    /** How long after a failed or capped gap-fill attempt the watchdog tries again. */
    static final Duration GAP_RETRY = Duration.ofMinutes(2);

    /** The adapter-state key of the update cursor. */
    static final String CURSOR_KEY = "emsc:lastUpdate";

    /** The adapter-state key of an unfinished gap-fill: {@code <updatedafter>|<next offset>|<first attempt>}. */
    static final String GAP_KEY = "emsc:gap";

    /** The most {@code unid}s remembered to drop duplicates between the stream and its gap-fill. */
    private static final int MAX_SEEN = 50_000;

    private static final ReconnectPolicy RECONNECT =
            new ReconnectPolicy(Duration.ofSeconds(15), Duration.ofMinutes(10), 0.2);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final URI wsUri;
    private final URI fdsnUri;
    private final Set<String> seen = Collections.synchronizedSet(new LinkedHashSet<>());

    /** Creates the adapter. */
    @Autowired
    public EmscSeismicAdapter(
            @Value("${" + URL_PROPERTY + ":}") String urlOverride,
            @Value("${" + FDSN_URL_PROPERTY + ":}") String fdsnOverride) {
        this.wsUri = URI.create(blankTo(urlOverride, DEFAULT_URL));
        this.fdsnUri = URI.create(blankTo(fdsnOverride, DEFAULT_FDSN_URL));
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }

    /** The FDSN gap-fill query endpoint. */
    URI fdsnUri() {
        return fdsnUri;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        EmscSession session = new EmscSession(this, ctx, sink);
        // The socket opens first, so the stream covers anything updated while the gap-fill pages are read; the
        // version keys drop what both deliver.
        ctx.http().openWebSocket(wsUri, session, java.util.Map.of(), WebSocketOptions.DEFAULT);
        session.gapFill();
        session.startWatchdog();
        return session;
    }

    /**
     * Marks a version key ({@code unid@lastupdate}) as seen.
     *
     * @return true when it is new (emit it), false when the stream or its gap-fill already emitted the same
     *     version of that event; a later {@code lastupdate} for the same {@code unid} is new
     */
    synchronized boolean markSeen(String key) {
        if (key == null) {
            return false;
        }
        if (!seen.add(key)) {
            return false;
        }
        while (seen.size() > MAX_SEEN) {
            var oldest = seen.iterator();
            oldest.next();
            oldest.remove();
        }
        return true;
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("EMSC real-time earthquakes")
                .description("Real-time earthquakes from the European-Mediterranean Seismological Centre "
                        + "(seismicportal.eu standing_order WebSocket), with an FDSN gap-fill on every reconnect so "
                        + "no event is lost. Depth is in payload depthKm; the point is 2D.")
                .kind(SeismicRecords.KIND)
                .mode(Mode.STREAMING)
                .homepage("https://www.seismicportal.eu/realtime.html")
                .attribution("European-Mediterranean Seismological Centre (EMSC)")
                .licence("EMSC data (CC BY 4.0)")
                .termsUrl("https://www.emsc-csem.org/Page/legal.php")
                .defaultBudget(12, 288)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(Duration.ofDays(1825))
                .searchFields("place", "magType")
                .zeroResultsNormal(true)
                .reconnect(RECONNECT)
                .kindDisplay(SeismicRecords.KIND, new KindDisplay("M{mag} {place}", "circle", "#E03131", null))
                .build();
    }
}
