package ai.foresignal.source.adapters.aisstream;

import ai.foresignal.source.formats.ais.VesselDescriptors;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.spi.WebSocketOptions;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code aisstream}: global AIS over aisstream.io's WebSocket (prompt 063), Foresignal's first streaming
 * adapter and the reference pattern for the later ones (Adapter Guide, "Streaming adapters").
 *
 * <p><strong>Connection.</strong> {@code ctx.http().openWebSocket(wss://stream.aisstream.io/v0/stream, …,
 * WebSocketOptions.DEFLATE)}: one budget unit per attempt, Foresignal's User-Agent, and
 * {@code permessage-deflate}, because aisstream limits the bandwidth of uncompressed connections since
 * September 2026. The subscription (key, lat-first boxes, message types, optional MMSIs) is sent on open; see
 * {@link AisStreamSession}. One connection per key: the supervisor runs one stream per source, and aisstream
 * allows 3 subscribed connections per account and 3 open connections per IP.
 *
 * <p><strong>Merge.</strong> Position and static messages are merged per MMSI by a {@link VesselStateStore}
 * held here, across reconnects, and every recognized message emits one complete vessel snapshot to the sink.
 * The {@code PolicyAwareRecordSink} keeps the newest {@code observedAt} per MMSI per 2 s flush.
 *
 * <p><strong>Credentials.</strong> {@code apiKey} is required, so the source stays disabled until a key is
 * set (409 {@code CREDENTIALS_REQUIRED}). aisstream answers an invalid key by closing the socket without a
 * word; two consecutive closes before any confirmation or envelope are therefore a
 * {@link CredentialsRejectedException}, and the supervisor retries hourly. The count resets on a confirmation,
 * an envelope or a different key. No key means no source: keyless official AIS ({@code ais-kystverket},
 * {@code ais-digitraffic}) and local AIS-catcher arrive in prompt 085.
 *
 * <p><strong>License.</strong> aisstream.io publishes no terms or redistribution clause (verified 2026-10),
 * and its FAQ says to proxy "only the information your clients need": {@code egress} LOCAL_ONLY, never
 * re-served by sinks, federation, share links or feeds (Gap-Analysis §9).
 */
@Component
public class AisStreamAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = "aisstream";

    /** The record kind. */
    public static final String KIND = "vessel";

    /** The production stream. */
    public static final String DEFAULT_URL = "wss://stream.aisstream.io/v0/stream";

    /** Test-only override of {@link #DEFAULT_URL}. */
    public static final String URL_PROPERTY = "foresignal.test.adapters.aisstream.url";

    /** The API key credential. */
    public static final String API_KEY = "apiKey";

    /** The bounding boxes parameter: {@code minLon,minLat,maxLon,maxLat} strings. */
    public static final String BBOX_PARAM = "bbox";

    /** The message types parameter. */
    public static final String TYPES_PARAM = "messageTypes";

    /** The MMSI filter parameter. */
    public static final String MMSI_PARAM = "mmsi";

    /** The default box: the world. */
    public static final List<String> DEFAULT_BBOX = List.of("-180,-90,180,90");

    /** The most boxes a subscription carries. */
    public static final int MAX_BOXES = 10;

    /** The most MMSIs the filter carries. */
    public static final int MAX_MMSI = 50;

    /** The largest message accepted; larger ones are dropped and counted. */
    public static final int MAX_MESSAGE_BYTES = 256 * 1024;

    /** aisstream closes a connection whose subscription arrives later than this after the handshake. */
    public static final Duration SUBSCRIBE_WITHIN = Duration.ofSeconds(3);

    /** aisstream closes a connection whose subscription changes faster than this. */
    public static final Duration UPDATE_MIN_GAP = Duration.ofSeconds(1);

    /** Silence after which health reports {@code degraded} (report fast). */
    public static final Duration SILENCE_REPORT = Duration.ofSeconds(120);

    /** Silence after which the socket is recycled (act slowly: 2.5 times the report threshold). */
    public static final Duration SILENCE_RECYCLE = Duration.ofSeconds(300);

    /** Consecutive closes before any confirmation that count as a rejected key. */
    public static final int CLOSES_BEFORE_REJECTED = 2;

    /** The record TTL (Core-Data-Model §5.3, vessels). */
    public static final Duration TTL = Duration.ofMinutes(30);

    /** The significant fields (§5.3). */
    public static final List<String> SIGNIFICANT_FIELDS =
            List.of("name", "callsign", "imo", "shipType", "destination", "navStatus", "draught");

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final URI uri;
    private final VesselStateStore store;
    private final AisStreamMetrics metrics;
    private final AtomicInteger unconfirmedCloses = new AtomicInteger();
    private volatile String keyFingerprint;
    private volatile AisStreamSession session;

    /**
     * Creates the adapter.
     *
     * @param urlOverride the value of {@value #URL_PROPERTY}; blank means {@value #DEFAULT_URL}
     * @param clock drives the vessel store's idle eviction
     */
    public AisStreamAdapter(
            @Value("${" + URL_PROPERTY + ":}") String urlOverride, MeterRegistry meters, Clock clock) {
        this.uri = URI.create(urlOverride == null || urlOverride.isBlank() ? DEFAULT_URL : urlOverride.trim());
        this.store = new VesselStateStore(clock);
        this.metrics = new AisStreamMetrics(meters, store::size);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The stream URL. */
    public URI uri() {
        return uri;
    }

    /**
     * Checks each {@code bbox} entry (four numbers, latitudes in [-90, 90], longitudes in [-180, 180], min
     * not above max; 1 to 10 entries), {@code mmsi} (at most 50 nine-digit strings) and that
     * {@code messageTypes} is not empty.
     */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        List<String> boxes = params.stringList(BBOX_PARAM);
        if (params.has(BBOX_PARAM) && (boxes.isEmpty() || boxes.size() > MAX_BOXES)) {
            problems.put(BBOX_PARAM, "must have 1 to " + MAX_BOXES + " entries, but had " + boxes.size());
        } else {
            for (String box : boxes) {
                String problem = boxProblem(box);
                if (problem != null) {
                    problems.put(BBOX_PARAM, problem);
                    break;
                }
            }
        }
        if (params.has(TYPES_PARAM) && params.stringList(TYPES_PARAM).isEmpty()) {
            problems.put(TYPES_PARAM, "must name at least one message type");
        }
        List<String> mmsi = params.stringList(MMSI_PARAM);
        if (mmsi.size() > MAX_MMSI) {
            problems.put(MMSI_PARAM, "must have at most " + MAX_MMSI + " entries, but had " + mmsi.size());
        } else {
            for (String value : mmsi) {
                if (!value.matches("[0-9]{9}")) {
                    problems.put(MMSI_PARAM, "entries must be nine-digit strings, but one was '" + value + "'");
                    break;
                }
            }
        }
        return problems;
    }

    /**
     * Opens the stream.
     *
     * @throws CredentialsRejectedException when no key is stored
     * @throws AdapterException when the connection could not be opened (budget, backoff, network)
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        String key = ctx.credentials().get(API_KEY);
        if (key == null || key.isBlank()) {
            throw new CredentialsRejectedException("aisstream needs an apiKey");
        }
        String fingerprint = fingerprint(key);
        if (!fingerprint.equals(keyFingerprint)) {
            keyFingerprint = fingerprint;
            unconfirmedCloses.set(0);
        }
        AisSubscription subscription;
        try {
            subscription = AisSubscription.of(key, ctx.params());
        } catch (IllegalArgumentException e) {
            throw new AdapterException("aisstream parameters: " + e.getMessage(), e);
        }
        AisStreamSession next = new AisStreamSession(this, ctx, sink, subscription);
        ctx.http().openWebSocket(uri, next, Map.of(), WebSocketOptions.DEFLATE);
        session = next;
        next.startWatchdog();
        return next;
    }

    // ---------- state shared with the session and the health rule ----------

    /** The merged vessel state. */
    VesselStateStore store() {
        return store;
    }

    /** The meters. */
    AisStreamMetrics metrics() {
        return metrics;
    }

    /** The upstream confirmed the subscription or delivered an envelope: the key works. */
    void confirmed() {
        unconfirmedCloses.set(0);
    }

    /** Counts a close before any confirmation; returns the consecutive count. */
    int unconfirmedClose() {
        return unconfirmedCloses.incrementAndGet();
    }

    /** The current or most recent connection, or null before the first. */
    AisStreamSession session() {
        return session;
    }

    /**
     * How long the open connection has gone without a recognized envelope at {@code now}; empty when no
     * connection is open.
     */
    public Optional<Duration> silentFor(Instant now) {
        AisStreamSession current = session;
        return current == null ? Optional.empty() : Optional.ofNullable(current.silentFor(now));
    }

    /**
     * The health details: {@code compressionEnabled} once the subscription was confirmed,
     * {@code perMessageDeflate} (what the handshake negotiated) while connected, and {@code vessels}.
     */
    public Map<String, Object> healthDetails() {
        Map<String, Object> details = new LinkedHashMap<>();
        AisStreamSession current = session;
        if (current != null) {
            if (current.compressionEnabled() != null) {
                details.put("compressionEnabled", current.compressionEnabled());
            }
            if (current.isRunning()) {
                details.put("perMessageDeflate", current.deflateNegotiated());
            }
        }
        details.put("vessels", store.size());
        return details;
    }

    // ---------- parameters ----------

    /**
     * {@code minLon,minLat,maxLon,maxLat} as four numbers, or null when the entry is not four numbers.
     */
    static double[] parseBox(String entry) {
        if (entry == null) {
            return null;
        }
        String[] parts = entry.split(",");
        if (parts.length != 4) {
            return null;
        }
        double[] values = new double[4];
        for (int i = 0; i < 4; i++) {
            try {
                values[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                return null;
            }
            if (!Double.isFinite(values[i])) {
                return null;
            }
        }
        return values;
    }

    private static String boxProblem(String entry) {
        double[] b = parseBox(entry);
        if (b == null) {
            return "entries must be 'minLon,minLat,maxLon,maxLat' (send the list as a JSON array of strings),"
                    + " but one was '" + entry + "'";
        }
        if (b[0] < -180 || b[0] > 180 || b[2] < -180 || b[2] > 180) {
            return "longitudes must be within [-180, 180] in '" + entry + "'";
        }
        if (b[1] < -90 || b[1] > 90 || b[3] < -90 || b[3] > 90) {
            return "latitudes must be within [-90, 90] in '" + entry + "'";
        }
        if (b[0] > b[2] || b[1] > b[3]) {
            return "minimums must not exceed maximums in '" + entry + "' (split a box that crosses the antimeridian)";
        }
        return null;
    }

    private static String fingerprint(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    // ---------- descriptor ----------

    /**
     * The SAMPLED vessel policy of Core-Data-Model §5.3 ({@code 1200 ≤ 1800 − 2 − 30}), shared with the keyless vessel
     * sources since prompt 085 ({@link VesselDescriptors}).
     */
    public static IngestPolicy defaultPolicy() {
        return VesselDescriptors.defaultPolicy();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("aisstream.io AIS")
                .description("Global AIS vessel positions and static data over aisstream.io's WebSocket stream,"
                        + " merged per MMSI into complete vessel snapshots. Requires an apiKey (free account at"
                        + " aisstream.io): no key means no source; keyless official AIS (ais-kystverket,"
                        + " ais-digitraffic) and local AIS-catcher arrive later. One connection per key: aisstream"
                        + " allows 3 subscribed connections per account and 3 open connections per IP, so do not"
                        + " reuse the key elsewhere. Uses permessage-deflate (uncompressed connections are"
                        + " bandwidth limited); keep the bbox to what you need. LOCAL_ONLY: aisstream publishes no"
                        + " redistribution terms.")
                .kind(KIND)
                .mode(Mode.STREAMING)
                .homepage("https://aisstream.io")
                .attribution("aisstream.io")
                .licence("aisstream.io terms (unverified)")
                .termsUrl("https://aisstream.io")
                .egress(Egress.LOCAL_ONLY)
                .credential(CredentialField.required(API_KEY,
                        "aisstream.io API key (Account page); use it for this Foresignal instance only"))
                .param(ParamSpec.optional(BBOX_PARAM, ParamType.STRING_LIST,
                        "Bounding boxes, each 'minLon,minLat,maxLon,maxLat', as a JSON array of 1 to 10 strings;"
                                + " default the world. A smaller area uses less bandwidth.", DEFAULT_BBOX))
                .param(new ParamSpec(TYPES_PARAM, ParamType.STRING_LIST,
                        "AIS message types to receive; default all five the vessel merge uses",
                        AisStreamMessageParser.MERGED_TYPES, false, AisStreamMessageParser.MERGED_TYPES))
                .param(ParamSpec.optional(MMSI_PARAM, ParamType.STRING_LIST,
                        "Optional MMSI filter: at most 50 nine-digit strings; empty means every vessel", null))
                // Each connection attempt is one unit; the reconnect ladder (5 s, 15 s, 60 s, 300 s, 900 s)
                // fits in 2 a minute and 144 a day.
                .defaultBudget(2, 144)
                .historyTier(HistoryTier.TRACK)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultIngestPolicy(defaultPolicy())
                .significantFields(SIGNIFICANT_FIELDS.toArray(String[]::new))
                .motion(new MotionSpec("sog", MotionSpec.SpeedUnit.KNOTS, "cog"))
                .threshold(FieldThreshold.angular("cog", 15.0))
                .threshold(FieldThreshold.of("sog", 3.0))
                .tileProperties("name", "shipType", "sog", "cog", "heading", "navStatus")
                .searchFields("name", "callsign", "mmsi", "imo", "destination")
                .titleField("name")
                .entityKey("mmsi", "mmsi")
                .entityKey("imo", "imo")
                .entityKey("callsign", "callsign")
                .entityKeyPrefix("callsign", "sea")
                .entityAttribute("name", "name")
                .entityAttribute("shipType", "shipType")
                .kindDisplay(KIND, new KindDisplay("{name}", "ship", "#1098AD", null, "cog", null, null));
        VesselDescriptors.fieldSpecs(b);
        return b.build();
    }
}
