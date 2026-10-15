package ai.foresignal.source.adapters.spacetrack;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code spacetrack-tip}: re-entry predictions (Tracking and Impact Prediction messages) from Space-Track
 * (prompt 064). Optional and disabled by default; it needs the operator's own account.
 *
 * <p><b>Login:</b> a form POST of {@code identity} and {@code password} to {@code /ajaxauth/login}. The
 * session cookie from {@code Set-Cookie} is sent back as a {@code Cookie} header ({@code SourceHttp} has no
 * cookie jar) and kept in memory only, keyed by a fingerprint of the credentials. A 401 on the query logs in
 * once more and retries; credentials Space-Track refuses throw {@link CredentialsRejectedException}.
 *
 * <p><b>Query:</b> {@code /basicspacedata/query/class/tip/INSERT_EPOCH/%3Enow-0.042/format/json} (messages of
 * the last hour) hourly: TIP is limited to once an hour, or every 10 minutes while a re-entry is within 12 h;
 * the overall limits are 30 a minute and 300 an hour.
 *
 * <p><b>License:</b> the Space-Track User Agreement, so {@code egress: LOCAL_ONLY}: data may not be passed to
 * other entities without approval, and blanket approval for TIP is not confirmed. One account per person or
 * entity; credentials are never shared.
 */
@Component
public class SpaceTrackTipAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "spacetrack-tip";

    /** The production host. */
    public static final String DEFAULT_BASE_URL = "https://www.space-track.org";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.spacetrack.base-url";

    /** The account identity (user name or e-mail) credential. */
    public static final String IDENTITY = "identity";

    /** The password credential. */
    public static final String PASSWORD = "password";

    /** The login path. */
    static final String LOGIN_PATH = "/ajaxauth/login";

    /** The TIP query path. */
    static final String QUERY_PATH = "/basicspacedata/query/class/tip/INSERT_EPOCH/%3Enow-0.042/format/json";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** The session cookie per source, keyed by credential fingerprint; memory only. */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /** A logged-in session. */
    private record Session(String fingerprint, String cookie) {}

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     */
    public SpaceTrackTipAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The TIP query URL. */
    public URI queryUri() {
        return URI.create(baseUrl + QUERY_PATH);
    }

    /** The login URL. */
    public URI loginUri() {
        return URI.create(baseUrl + LOGIN_PATH);
    }

    /**
     * Logs in when needed and queries the last hour's TIP messages.
     *
     * @throws CredentialsRejectedException when Space-Track refuses the credentials
     * @throws AdapterException when the upstream fails otherwise
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String identity = ctx.credentials().get(IDENTITY);
        String password = ctx.credentials().get(PASSWORD);
        if (blank(identity) || blank(password)) {
            throw new CredentialsRejectedException("spacetrack-tip needs both identity and password");
        }
        String fingerprint = fingerprint(identity, password);
        UpstreamResponse response;
        try {
            response = ctx.http().get(queryUri(), Map.of("Cookie", cookie(ctx, identity, password, fingerprint)));
        } catch (AdapterException e) {
            if (e.httpStatus().orElse(0) != 401) {
                throw e;
            }
            sessions.remove(ctx.sourceId());
            response = ctx.http().get(queryUri(), Map.of("Cookie", cookie(ctx, identity, password, fingerprint)));
        }
        if (response.truncated()) {
            throw new UpstreamFormatException("Space-Track TIP response exceeded maxResponseBytes", null);
        }
        TipParser.Parsed parsed = TipParser.parse(response.body());
        if (parsed.skipped() > 0) {
            ctx.log().info("{}: skipped {} unreadable TIP message(s)", ctx.sourceId(), parsed.skipped());
        }
        return FetchResult.of(parsed.records());
    }

    /** True while a session cookie is held for {@code sourceId}. */
    boolean hasSession(String sourceId) {
        return sessions.containsKey(sourceId);
    }

    private String cookie(FetchContext ctx, String identity, String password, String fingerprint)
            throws AdapterException {
        Session session = sessions.get(ctx.sourceId());
        if (session != null && session.fingerprint().equals(fingerprint)) {
            return session.cookie();
        }
        String cookie = login(ctx, identity, password);
        sessions.put(ctx.sourceId(), new Session(fingerprint, cookie));
        return cookie;
    }

    /** Posts the login form and returns the {@code Cookie} header value. */
    private String login(FetchContext ctx, String identity, String password) throws AdapterException {
        byte[] form = ("identity=" + URLEncoder.encode(identity, StandardCharsets.UTF_8) + "&password="
                + URLEncoder.encode(password, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
        UpstreamResponse response;
        try {
            response = ctx.http().post(loginUri(), "application/x-www-form-urlencoded", form, Map.of());
        } catch (AdapterException e) {
            int status = e.httpStatus().orElse(0);
            if (status == 401 || status == 403) {
                throw new CredentialsRejectedException("Space-Track refused the login (HTTP " + status + ")");
            }
            throw e;
        }
        String body = response.bodyAsString();
        if (body.contains("\"Login\"") && body.toLowerCase(java.util.Locale.ROOT).contains("failed")) {
            throw new CredentialsRejectedException("Space-Track refused the login: identity or password is wrong");
        }
        List<String> cookies = new ArrayList<>();
        for (String setCookie : response.headers().getOrDefault("set-cookie", List.of())) {
            String pair = setCookie.split(";", 2)[0].strip();
            if (pair.contains("=")) {
                cookies.add(pair);
            }
        }
        if (cookies.isEmpty()) {
            throw new CredentialsRejectedException("Space-Track login returned no session cookie");
        }
        return String.join("; ", cookies);
    }

    private static String fingerprint(String identity, String password) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(identity.getBytes(StandardCharsets.UTF_8));
            sha.update((byte) 0);
            sha.update(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(sha.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every JVM", e);
        }
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Space-Track TIP re-entry predictions")
                .description("Tracking and Impact Prediction messages from Space-Track: predicted re-entry times"
                        + " (DECAY_EPOCH) with their window, and the predicted position at 10 km altitude when"
                        + " given (not an impact point). Needs your own Space-Track account (identity and password;"
                        + " one account per person, never shared). Hourly: TIP allows one query an hour. LOCAL_ONLY:"
                        + " the Space-Track User Agreement forbids passing the data on without approval.")
                .kind(TipParser.KIND)
                .mode(Mode.POLLING)
                .homepage("https://www.space-track.org")
                .attribution("Space-Track.org (18th and 19th Space Defense Squadrons)")
                .licence("Space-Track User Agreement")
                .termsUrl("https://www.space-track.org/documentation#user_agree")
                .egress(Egress.LOCAL_ONLY)
                .credential(CredentialField.required(IDENTITY, "Space-Track account user name or e-mail"))
                .credential(CredentialField.required(PASSWORD, "Space-Track account password"))
                .interval(Duration.ofHours(1), Duration.ofHours(1))
                .defaultBudget(2, 60)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                // Re-entry predictions are events (Conventions), 1825 days.
                .defaultRetention(Duration.ofDays(1825))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .zeroResultsNormal(true)
                .build();
    }
}
