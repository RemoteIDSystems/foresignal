package ai.foresignal.source.adapters.radio;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.formats.aprs.AprsPacket;
import ai.foresignal.source.formats.aprs.AprsPacketDedup;
import ai.foresignal.source.formats.aprs.AprsParser;
import ai.foresignal.source.formats.aprs.AprsRecords;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.HostPort;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.Observer;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code aprs-is} (prompt 082): a narrow, opt-in, receive-only APRS-IS feed from {@value #HOST}.
 *
 * <ul>
 *   <li><strong>Connection:</strong> raw TCP through {@code ctx.sockets().tcpPublic(…)} to the descriptor's only
 *       {@code allowedHosts} entry, {@link Framing#LINES}. One budget unit per connect attempt (default 1/min and
 *       100/day); reconnects back off from 30 s to 15 min with ±20% jitter (the descriptor's
 *       {@link ReconnectPolicy}); a connection silent for 120 s is closed (aprsc sends a {@code #} comment about every
 *       20 s).
 *   <li><strong>Receive-only:</strong> the login line {@code user {login} pass -1 vers Foresignal {version} filter
 *       {filter}} is the only thing ever written, once, as the {@code onConnect} bytes under
 *       {@link SocketOptions#receiveOnlyAfterConnect()}. The passcode is always {@code -1} (an unverified,
 *       receive-only client); the connection handle has no write method, so nothing can be sent after the login.
 *   <li><strong>Filter:</strong> required and narrow ({@link AprsIsFilter}); the source cannot be enabled without
 *       one. {@code login} defaults to {@code EL} plus the first six install-id characters in upper case; aprsc
 *       refuses {@code N0CALL} ("Login by user not allowed", verified 2026-10) and accepts an unverified non-call
 *       login, so {@code N0CALL} and {@code NOCALL} are refused here.
 *   <li><strong>Records:</strong> {@link AprsRecords}; the igate after the q-construct is the observer. Messages
 *       only with {@code includeMessages}. Packets of unsupported formats are counted ({@value #UNSUPPORTED_METRIC})
 *       and dropped.
 *   <li><strong>Duplicates of RF:</strong> with {@code dedupLocal} (default), a packet a {@code local-aprs-{n}} TNC
 *       heard within the last 30 s ({@link AprsPacketDedup}) is dropped and counted ({@value #DEDUP_METRIC}): the
 *       first-hand RF copy wins.
 * </ul>
 */
@Component
public class AprsIsAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = "aprs-is";

    /** The APRS-IS rotation and user-defined filter port. */
    public static final String HOST = "rotate.aprs2.net:14580";

    /** The filter parameter. */
    public static final String FILTER = "filter";

    /** The login parameter. */
    public static final String LOGIN = "login";

    /** The parameter that turns on {@code aprs-message} records. */
    public static final String INCLUDE_MESSAGES = "includeMessages";

    /** The parameter that drops packets a local TNC already delivered. */
    public static final String DEDUP_LOCAL = "dedupLocal";

    /** The counter of packets dropped as duplicates of a local RF copy. */
    public static final String DEDUP_METRIC = "foresignal.aprs.dedup";

    /** The counter of packets of unsupported formats. */
    public static final String UNSUPPORTED_METRIC = "foresignal.aprs.unsupported";

    /** The passcode of a receive-only client. */
    public static final String RECEIVE_ONLY_PASSCODE = "-1";

    /** The idle watchdog. */
    static final Duration SILENCE = Duration.ofSeconds(120);

    /** The reconnect ladder: 30 s doubling to 15 min, ±20% jitter. */
    static final ReconnectPolicy RECONNECT = new ReconnectPolicy(Duration.ofSeconds(30), Duration.ofMinutes(15), 0.2);

    /** The record TTL, from the receive time. */
    static final Duration TTL = Duration.ofMinutes(60);

    /** The keyframe: under {@code ttl - stream interval (2 s) - 30 s}. */
    static final Duration KEYFRAME = Duration.ofMinutes(55);

    /** Raw retention: APRS beacons are high-volume reports, the {@code bulk-event} class (90 d). */
    static final Duration RETENTION = Duration.ofDays(90);

    /** A login: 3 to 9 letters or digits and an optional {@code -SSID}. */
    static final Pattern LOGIN_PATTERN = Pattern.compile("^[A-Z0-9]{3,9}(-\\d{1,2})?$");

    private final Supplier<String> installId;
    private final Supplier<String> version;
    private final AprsPacketDedup dedup;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter for Spring.
     *
     * @param install supplies the install id of the default login
     * @param version the Foresignal version sent in the login line
     * @param dedup the filter of packets a local TNC heard
     * @param meters the registry of the counters, resolved lazily
     */
    @Autowired
    public AprsIsAdapter(InstallIdService install, ForesignalVersion version, AprsPacketDedup dedup,
            ObjectProvider<MeterRegistry> meters) {
        this(install::installId, version::version, dedup,
                meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter from plain suppliers (tests).
     *
     * @param installId supplies the install id
     * @param version supplies the Foresignal version
     * @param dedup the duplicate filter
     * @param meters supplies the meter registry, or null
     */
    public AprsIsAdapter(Supplier<String> installId, Supplier<String> version, AprsPacketDedup dedup,
            Supplier<MeterRegistry> meters) {
        this.installId = installId;
        this.version = version;
        this.dedup = dedup;
        this.meters = meters == null ? () -> null : meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Checks the filter and the login. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String filterProblem = AprsIsFilter.problem(params.string(FILTER));
        if (filterProblem != null) {
            problems.put(FILTER, filterProblem);
        }
        String login = params.string(LOGIN);
        if (login != null && !login.isBlank()) {
            String problem = loginProblem(login.strip());
            if (problem != null) {
                problems.put(LOGIN, problem);
            }
        }
        return problems;
    }

    /** The problem with a login, or null. */
    static String loginProblem(String login) {
        if (!LOGIN_PATTERN.matcher(login).matches()) {
            return "must be 3 to 9 upper-case letters or digits with an optional -SSID";
        }
        String base = login.contains("-") ? login.substring(0, login.indexOf('-')) : login;
        if (base.equals("N0CALL") || base.equals("NOCALL")) {
            return "N0CALL and NOCALL are refused by APRS-IS servers; use your call sign or leave it empty";
        }
        return null;
    }

    /** The login: the parameter, else {@code EL} and the first six install-id characters in upper case. */
    String login(ValidatedParams params) {
        String given = params.string(LOGIN);
        if (given != null && !given.isBlank()) {
            return given.strip();
        }
        String id = installId.get() == null ? "" : installId.get().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return "EL" + id.substring(0, Math.min(6, id.length()));
    }

    /**
     * The login line, CR LF terminated: {@code user {login} pass -1 vers Foresignal {version} filter {filter}}.
     *
     * @throws AdapterException when the filter or login is not valid (the validator should have refused them)
     */
    public String loginLine(ValidatedParams params) throws AdapterException {
        String filter = params.string(FILTER);
        String problem = AprsIsFilter.problem(filter);
        if (problem != null) {
            throw new AdapterException(ID + ": the filter " + problem);
        }
        String login = login(params);
        String loginProblem = loginProblem(login);
        if (loginProblem != null) {
            throw new AdapterException(ID + ": the login " + loginProblem);
        }
        String vers = version.get() == null || version.get().isBlank() ? "dev" : version.get().strip().replace(' ', '_');
        return "user " + login + " pass " + RECEIVE_ONLY_PASSCODE + " vers Foresignal " + vers + " filter "
                + filter.strip().replaceAll("\\s+", " ") + "\r\n";
    }

    /**
     * Connects, logs in once and streams records until the handle is closed.
     *
     * @throws AdapterException when the parameters are invalid, the budget is spent or the connect fails
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        ValidatedParams params = ctx.params();
        byte[] login = loginLine(params).getBytes(StandardCharsets.US_ASCII);
        SocketOptions options = SocketOptions.receiveOnlyAfterConnect().withOnConnect(login).withSilenceTimeout(SILENCE);
        Session session = new Session(ctx.sourceId(), params.bool(INCLUDE_MESSAGES, false),
                params.bool(DEDUP_LOCAL, true));
        return ctx.sockets().tcpPublic(HostPort.parse(HOST), options, Framing.LINES,
                (frame, receivedAt) -> sink.acceptAll(session.onLine(frame, receivedAt)));
    }

    /** One connection's state: the record mapping and the counters. Package-visible for the ingest test. */
    final class Session {

        private final String sourceId;
        private final boolean includeMessages;
        private final boolean dedupLocal;
        private final AprsRecords records = new AprsRecords();
        private Counter dedupCounter;
        private Counter unsupportedCounter;
        private volatile String logresp;

        Session(String sourceId, boolean includeMessages, boolean dedupLocal) {
            this.sourceId = sourceId;
            this.includeMessages = includeMessages;
            this.dedupLocal = dedupLocal;
        }

        /** The records of one line; server comments ({@code #}) produce none. */
        List<NormalizedRecord> onLine(byte[] frame, Instant receivedAt) {
            String line = new String(frame, StandardCharsets.UTF_8);
            if (line.startsWith("#")) {
                if (line.startsWith("# logresp")) {
                    logresp = line.strip();
                }
                return List.of();
            }
            AprsPacket packet = AprsParser.parse(line);
            if (packet.type() == AprsPacket.Type.UNSUPPORTED) {
                unsupportedCounter = increment(unsupportedCounter, UNSUPPORTED_METRIC,
                        "APRS packets of unsupported formats, dropped");
                return List.of();
            }
            if (dedupLocal && dedup != null && dedup.seenRecently(packet.source(), packet.info(), receivedAt)) {
                dedupCounter = increment(dedupCounter, DEDUP_METRIC,
                        "APRS-IS packets dropped because a local TNC heard them first");
                return List.of();
            }
            Observer observer = packet.igate() == null ? null : Observer.of(packet.igate());
            return records.map(packet, observer, includeMessages, receivedAt);
        }

        /** The server's login response, or null before it arrived. */
        String logresp() {
            return logresp;
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

    /** A session for tests and the ingest IT, without a socket. */
    Session newSession(String sourceId, boolean includeMessages, boolean dedupLocal) {
        return new Session(sourceId, includeMessages, dedupLocal);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("APRS-IS (narrow filter)")
                .description("Amateur APRS stations, objects and items from the APRS-IS network (rotate.aprs2.net,"
                        + " port 14580), receive-only: the login uses passcode -1 and nothing is ever sent after it."
                        + " Opt-in and narrow: filter is required (r/lat/lon/km up to 250 km, b/, p/, t/types/call/km"
                        + " or f/call/km; at most 5 terms, no bare wildcards). One subscription. The igate that"
                        + " heard a packet is an observer (include=observer). With dedupLocal, packets a local-aprs"
                        + " TNC heard first are dropped. Messages only with includeMessages; never embedded.")
                .kinds(AprsRecords.STATION_KIND, AprsRecords.MESSAGE_KIND)
                .homepage("https://www.aprs-is.net/")
                .attribution("APRS-IS (aprs2.net servers) and the stations heard")
                .licence("APRS-IS network data (amateur radio, public; no license stated)")
                .termsUrl("https://www.aprs-is.net/Connecting.aspx")
                .mode(Mode.STREAMING)
                .defaultBudget(Budget.of(1, 100))
                .allowedHost(HOST)
                .reconnect(RECONNECT)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(KEYFRAME))
                .observedAtMode(ObservedAtMode.RECEIVED)
                .recordTtl(TTL)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .zeroResultsNormal(true)
                .defaultRetention(RETENTION)
                .kindRetention(AprsRecords.STATION_KIND, RETENTION)
                .kindRetention(AprsRecords.MESSAGE_KIND, RETENTION)
                .param(ParamSpec.required(FILTER, ParamType.STRING, "The APRS-IS server-side filter, narrow: for"
                        + " example r/37.77/-122.42/50 (latitude, longitude, km; at most 250 km). Terms r/, b/, p/,"
                        + " t/types/call/km and f/call/km, at most 5, no bare wildcards"))
                .param(ParamSpec.optional(LOGIN, ParamType.STRING, "The login sent with passcode -1 (receive-only);"
                        + " your call sign, or empty for EL plus the first six install-id characters. N0CALL is"
                        + " refused", null))
                .param(ParamSpec.optional(INCLUDE_MESSAGES, ParamType.BOOLEAN, "Also keep APRS messages as"
                        + " aprs-message records", false))
                .param(ParamSpec.optional(DEDUP_LOCAL, ParamType.BOOLEAN, "Drop packets a local-aprs TNC heard in"
                        + " the last 30 s", true));
        AprsSourceFields.declare(b);
        return b.build();
    }
}
