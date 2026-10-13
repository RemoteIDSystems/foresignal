package ai.foresignal.source.adapters.opensky;

import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamQuotaDeferredException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code opensky-states}: worldwide civil aircraft state vectors from the OpenSky Network (prompt 062).
 *
 * <p><strong>Request:</strong> {@code GET /api/states/all?extended=1}, plus {@code lamin, lomin, lamax,
 * lomax} when the {@code bbox} parameter is set (default: the world).
 *
 * <p><strong>Auth:</strong> optional {@code clientId} and {@code clientSecret}, stored together or not at
 * all. With them, an OAuth2 client-credentials token ({@link OpenSkyTokens}), cached for its 30 minutes and
 * refreshed 60 s early; a 401 from {@code /states/all} refreshes it once and retries. Without them, anonymous.
 * Basic authentication ended on 2026-03-18 and is never sent.
 *
 * <p><strong>Credits:</strong> each response's {@code X-Rate-Limit-Remaining} is stored with its time; a
 * fetch whose cost ({@link OpenSkyCredits#cost}: 1 to 4 by bbox area) plus {@code creditReserve} (default
 * 40) exceeds today's stored balance throws {@link UpstreamQuotaDeferredException} and sends nothing. A 429
 * with {@code X-Rate-Limit-Retry-After-Seconds} goes to backoff in {@code UpstreamClient}.
 *
 * <p><strong>Interval:</strong> 300 s by default, 90 s minimum ({@code 86400 / (4000 / 4)} ≈ 86 s for a
 * standard account at world cost); anonymous world polling needs at least 900 s (400 credits ÷ 4 = 100 calls
 * a day). The TTL is 15 minutes, so the 240 s keyframe holds up to a 630 s interval.
 *
 * <p><strong>License:</strong> OpenSky's terms (non-commercial research), so {@code egress: LOCAL_ONLY}:
 * never re-served by sinks, federation, share links or feeds (Gap-Analysis §9).
 */
@Component
public class OpenSkyStatesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "opensky-states";

    /** The production API. */
    public static final String DEFAULT_BASE_URL = "https://opensky-network.org";

    /** The production identity provider (verified 2026-10: the path includes {@code /auth/}). */
    public static final String DEFAULT_AUTH_URL = "https://auth.opensky-network.org";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.opensky.base-url";

    /** Test-only override of {@link #DEFAULT_AUTH_URL}. */
    public static final String AUTH_URL_PROPERTY = "foresignal.test.adapters.opensky.auth-url";

    /** The optional bounding box parameter. */
    public static final String BBOX_PARAM = "bbox";

    /** The credit reserve parameter. */
    public static final String RESERVE_PARAM = "creditReserve";

    /** The default credit reserve. */
    public static final int DEFAULT_RESERVE = 40;

    /** The client id credential. */
    public static final String CLIENT_ID = "clientId";

    /** The client secret credential. */
    public static final String CLIENT_SECRET = "clientSecret";

    /** The record TTL (not §5.3's 5 minutes: a 300 s poll would fail the keyframe constraint). */
    public static final Duration TTL = Duration.ofMinutes(15);

    /** The states path. */
    static final String STATES_PATH = "/api/states/all";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final OpenSkyTokens tokens;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     * @param authUrlOverride the value of {@value #AUTH_URL_PROPERTY}; blank means {@value #DEFAULT_AUTH_URL}
     */
    public OpenSkyStatesAdapter(
            @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${" + AUTH_URL_PROPERTY + ":}") String authUrlOverride) {
        this.baseUrl = trimmed(baseUrlOverride, DEFAULT_BASE_URL);
        this.tokens = new OpenSkyTokens(trimmed(authUrlOverride, DEFAULT_AUTH_URL));
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The token endpoint. */
    public URI tokenUri() {
        return tokens.tokenUri();
    }

    /** The states URL for {@code bbox}; null means the world. */
    public URI statesUri(Envelope bbox) {
        StringBuilder url = new StringBuilder(baseUrl).append(STATES_PATH).append("?extended=1");
        if (bbox != null && !bbox.isNull()) {
            url.append("&lamin=").append(plain(bbox.getMinY()))
                    .append("&lomin=").append(plain(bbox.getMinX()))
                    .append("&lamax=").append(plain(bbox.getMaxY()))
                    .append("&lomax=").append(plain(bbox.getMaxX()));
        }
        return URI.create(url.toString());
    }

    /** The credits one fetch costs for {@code bbox}; null means the world (4). */
    public static int cost(Envelope bbox) {
        return OpenSkyCredits.cost(bbox);
    }

    /** {@code creditReserve} must not be negative. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        Integer reserve = params.integer(RESERVE_PARAM);
        if (reserve != null && reserve < 0) {
            problems.put(RESERVE_PARAM, "must not be negative, but was " + reserve);
        }
        return problems;
    }

    /** {@code clientId} and {@code clientSecret} are stored together or not at all. */
    @Override
    public Map<String, String> checkCredentials(Map<String, String> values) {
        boolean id = values.containsKey(CLIENT_ID);
        boolean secret = values.containsKey(CLIENT_SECRET);
        if (id && !secret) {
            return Map.of(CLIENT_SECRET, "is required together with clientId");
        }
        if (secret && !id) {
            return Map.of(CLIENT_ID, "is required together with clientSecret");
        }
        return Map.of();
    }

    /**
     * Fetches the state vectors once.
     *
     * @throws UpstreamQuotaDeferredException when today's credits would fall below the reserve
     * @throws ai.foresignal.source.spi.CredentialsRejectedException when the token endpoint refuses the
     *     credentials
     * @throws AdapterException when the upstream fails otherwise
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Envelope bbox = ctx.params().has(BBOX_PARAM) ? ctx.params().bbox(BBOX_PARAM) : null;
        int cost = cost(bbox);
        int reserve = ctx.params().integer(RESERVE_PARAM, DEFAULT_RESERVE);
        Instant now = ctx.clock().instant();
        Optional<Instant> deferUntil = OpenSkyCredits.deferral(ctx.state(), now, cost, reserve);
        if (deferUntil.isPresent()) {
            throw new UpstreamQuotaDeferredException(
                    "OpenSky credits: " + OpenSkyCredits.remainingToday(ctx.state(), now).orElse(0L)
                            + " left today, a fetch costs " + cost + " and the reserve is " + reserve,
                    deferUntil.get());
        }

        URI uri = statesUri(bbox);
        String clientId = ctx.credentials().get(CLIENT_ID);
        String clientSecret = ctx.credentials().get(CLIENT_SECRET);
        boolean authenticated = present(clientId) && present(clientSecret);

        UpstreamResponse response;
        if (!authenticated) {
            response = ctx.http().get(uri);
        } else {
            try {
                response = ctx.http().get(uri, bearer(tokens.token(ctx, clientId, clientSecret)));
            } catch (AdapterException e) {
                if (e.httpStatus().orElse(0) != 401) {
                    throw e;
                }
                // The token may have been revoked or expired early: refresh once and retry.
                tokens.invalidate(ctx.sourceId());
                response = ctx.http().get(uri, bearer(tokens.token(ctx, clientId, clientSecret)));
            }
        }
        OpenSkyCredits.record(ctx.state(), response.header(OpenSkyCredits.REMAINING_HEADER), ctx.clock().instant());
        if (response.truncated()) {
            throw new UpstreamFormatException("OpenSky response exceeded maxResponseBytes and was truncated", null);
        }
        OpenSkyStatesParser.ParseResult parsed = OpenSkyStatesParser.parseWithDiagnostics(response.body());
        if (parsed.skipped() > 0) {
            ctx.log().debug("{}: skipped {} of {} state(s) without icao24 or last_contact", ctx.sourceId(),
                    parsed.skipped(), parsed.states());
        }
        return FetchResult.complete(parsed.records());
    }

    private static Map<String, String> bearer(String token) {
        return Map.of("Authorization", "Bearer " + token);
    }

    private static boolean present(String value) {
        return value != null && !value.isBlank();
    }

    private static String trimmed(String override, String fallback) {
        String chosen = override == null || override.isBlank() ? fallback : override.trim();
        return chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    private static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("OpenSky Network states")
                .description("Worldwide civil aircraft state vectors from the OpenSky Network (/api/states/all,"
                        + " extended). Credit-governed: a standard account has 4,000 credits a day and a world query"
                        + " costs 4, so the floor is 90 s; anonymous world polling needs an interval of at least"
                        + " 900 s (400 credits / 4 = 100 calls a day). Optional OAuth2 client credentials (clientId"
                        + " and clientSecret together). TTL 15 minutes, so the 240 s keyframe holds up to a 630 s"
                        + " interval. LOCAL_ONLY: OpenSky's terms forbid redistribution.")
                .mode(Mode.POLLING)
                .homepage("https://opensky-network.org")
                .attribution("The OpenSky Network, https://opensky-network.org. Matthias Schäfer, Martin Strohmeier,"
                        + " Vincent Lenders, Ivan Martinovic and Matthias Wilhelm, \"Bringing Up OpenSky: A"
                        + " Large-scale ADS-B Sensor Network for Research\", IPSN 2014")
                .licence("OpenSky Network terms (non-commercial research)")
                .termsUrl("https://opensky-network.org/about/terms-of-use")
                .egress(Egress.LOCAL_ONLY)
                .credential(CredentialField.optional(CLIENT_ID,
                        "OpenSky API client id (Account page → API client); set together with clientSecret"))
                .credential(CredentialField.optional(CLIENT_SECRET,
                        "OpenSky API client secret; set together with clientId"))
                .param(ParamSpec.optional(BBOX_PARAM, ParamType.BBOX,
                        "Optional minLon,minLat,maxLon,maxLat; unset means the world (4 credits per call)", null))
                .param(ParamSpec.optional(RESERVE_PARAM, ParamType.INTEGER,
                        "Credits kept in reserve: a fetch is deferred when it would leave fewer", DEFAULT_RESERVE))
                .interval(Duration.ofSeconds(90), Duration.ofSeconds(300))
                // The token refresh and the fetch can fall in the same minute.
                .defaultBudget(2, 1_500);
        AircraftDescriptors.global(builder, TTL);
        builder.kindDisplay(AircraftFields.KIND, AircraftDescriptors.civilDisplay());
        return builder.build();
    }
}
