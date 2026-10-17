package ai.foresignal.source.adapters.ercot;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.JsonProps;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code ercot-public} (prompt 093, optional, disabled by default): ERCOT's hourly actual system load by weather zone
 * and 15-minute settlement point prices at the trading hubs, as {@code series-point} records.
 *
 * <p>Needs a registered ERCOT account: credential {@code username}, {@code password} and {@code subscriptionKey}.
 * The API requires an {@code Ocp-Apim-Subscription-Key} header and an {@code Authorization: Bearer} token from the
 * Azure B2C ROPC flow; the token lasts one hour and cannot be refreshed, so it is cached in memory for 55 minutes
 * (never persisted) and a 401 re-fetches it. A 403 means the client is outside the US, which the operator sees as a
 * health reason. The operator must register the account personally (ERCOT's terms forbid bot registration).
 *
 * <p><strong>Time.</strong> {@code operatingDay}/{@code hourEnding} and {@code deliveryDate}/{@code deliveryHour}/
 * {@code deliveryInterval} are ERCOT market time, US Central ({@value #MARKET_ZONE_ID}). They are converted with the
 * zone's rules: on the fall-back day the repeated hour is told apart by {@code DSTFlag} (true for the second, standard
 * time, occurrence) and its period label gains {@value #REPEATED_SUFFIX}, so the two hours never share an
 * {@code externalId}; on the spring-forward day an hour inside the gap is skipped. {@code hourEnding} arrives as
 * {@code "HH:MM"} text (the API spec types it a string) or a number.
 *
 * <p><strong>Budget.</strong> The load report is hourly, so it is read at most once an hour; each run reads one price
 * report per hub. With the default five hubs that is 24 + 96 × 5 = 504 report requests a day plus about 26 token
 * requests (one per 55 minutes), inside the 600-a-day budget, and at most seven requests in one run (a token, the
 * load and five hubs), inside ten a minute.
 */
@Component
public class ErcotAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ercot-public";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://api.ercot.com/api/public-reports";

    /** Test-only override of the report base URL. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.ercot.base-url";

    /** Test-only override of the token endpoint. */
    public static final String TOKEN_URL_PROPERTY = "foresignal.test.adapters.ercot.token-url";

    /** The published public client id (documented by ERCOT's authentication guide). */
    public static final String CLIENT_ID = "fec253ea-0d06-4272-a5e6-b478baeecd70";

    /** The production token endpoint. */
    public static final String DEFAULT_TOKEN_URL =
            "https://ercotb2c.b2clogin.com/ercotb2c.onmicrosoft.com/B2C_1_PUBAPI-ROPC-FLOW/oauth2/v2.0/token";

    /** Credential: the account username. */
    public static final String USERNAME = "username";

    /** Credential: the account password. */
    public static final String PASSWORD = "password";

    /** Credential: the subscription key. */
    public static final String SUBSCRIPTION_KEY = "subscriptionKey";

    /** The hubs parameter. */
    public static final String HUBS_PARAM = "hubs";

    /** The default trading hubs. */
    public static final List<String> DEFAULT_HUBS =
            List.of("HB_HOUSTON", "HB_NORTH", "HB_SOUTH", "HB_WEST", "HB_BUSAVG");

    /** The weather-zone columns of the load report. */
    public static final List<String> ZONES =
            List.of("coast", "east", "farWest", "north", "northC", "southern", "southC", "west", "total");

    /** The load report path. */
    public static final String LOAD_PATH = "/np6-345-cd/act_sys_load_by_wzn";

    /** The settlement-point price report path. */
    public static final String SPP_PATH = "/np6-905-cd/spp_node_zone_hub";

    /** How long a token is reused. */
    static final Duration TOKEN_TTL = Duration.ofMinutes(55);

    /** The trailing window. */
    static final Duration WINDOW = Duration.ofDays(2);

    /** ERCOT market time. */
    static final String MARKET_ZONE_ID = "America/Chicago";

    /** ERCOT market time as a zone. */
    static final ZoneId MARKET_ZONE = ZoneId.of(MARKET_ZONE_ID);

    /** The period-label suffix of the repeated (second) hour of the fall-back day. */
    static final String REPEATED_SUFFIX = "-DST";

    /** How often the hourly load report is read. */
    static final Duration LOAD_INTERVAL = Duration.ofHours(1);

    /** Adapter state: when the load report was last read. */
    static final String LOAD_AT_KEY = "ercot:loadAt";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final String tokenUrl;
    private volatile String token;
    private volatile Instant tokenAt;

    /** Creates the adapter. */
    public ErcotAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${" + TOKEN_URL_PROPERTY + ":}") String tokenUrlOverride) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
        this.tokenUrl = tokenUrlOverride == null || tokenUrlOverride.isBlank()
                ? DEFAULT_TOKEN_URL : tokenUrlOverride.trim();
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String username = credential(ctx, USERNAME);
        String password = credential(ctx, PASSWORD);
        String subscriptionKey = credential(ctx, SUBSCRIPTION_KEY);
        if (username == null || password == null || subscriptionKey == null) {
            throw new AdapterException(ID + ": credentials " + USERNAME + ", " + PASSWORD + " and "
                    + SUBSCRIPTION_KEY + " are all required");
        }
        List<String> hubs = ctx.params() == null || ctx.params().stringList(HUBS_PARAM).isEmpty()
                ? DEFAULT_HUBS : ctx.params().stringList(HUBS_PARAM);
        List<NormalizedRecord> points = new ArrayList<>();
        Instant loadAt = ai.foresignal.source.adapters.humanitarian.Instants
                .parse(ctx.state().get(LOAD_AT_KEY).orElse(null));
        boolean loadDue = loadAt == null || !loadAt.plus(LOAD_INTERVAL).isAfter(ctx.now());
        if (loadDue) {
            points.addAll(readLoad(ctx, subscriptionKey, username, password));
        }
        for (String hub : hubs) {
            points.addAll(readSpp(ctx, subscriptionKey, username, password, hub));
        }
        if (loadDue) {
            ctx.state().put(LOAD_AT_KEY, ctx.now().toString());
        }
        List<NormalizedRecord> out = new ArrayList<>(points);
        out.addAll(SeriesPoint.latestPerSeries(points,
                ai.foresignal.source.series.EnergySeriesMetadata.forSource(DESCRIPTOR)));
        ctx.state().clearDegraded();
        ctx.log().debug("{}: {} record(s)", ID, out.size());
        // A run that skipped the hourly load report is not the full set of series.
        return loadDue ? FetchResult.complete(out) : FetchResult.of(out);
    }

    /**
     * The start of an ERCOT market interval: {@code minutesIntoDay} after local midnight of {@code day} in
     * {@value #MARKET_ZONE_ID}. On the fall-back day the repeated local time takes the later (standard-time) offset
     * when {@code repeated} is true and the earlier one otherwise.
     *
     * @return the instant, or null when the local time falls in the spring-forward gap
     */
    static Instant marketStart(LocalDate day, long minutesIntoDay, boolean repeated) {
        LocalDateTime local = day.atStartOfDay().plusMinutes(minutesIntoDay);
        List<ZoneOffset> offsets = MARKET_ZONE.getRules().getValidOffsets(local);
        if (offsets.isEmpty()) {
            return null;
        }
        ZoneOffset offset = offsets.size() > 1 && repeated ? offsets.get(offsets.size() - 1) : offsets.get(0);
        return local.toInstant(offset);
    }

    /** The hour ending as a number: {@code "01:00"} → 1, {@code "24:00"} → 24, a number as itself. */
    static Long hourEnding(Object raw) {
        if (raw == null) {
            return null;
        }
        String text = String.valueOf(raw).strip();
        int colon = text.indexOf(':');
        if (colon > 0) {
            text = text.substring(0, colon);
        }
        Double value = SeriesPoint.number(text);
        return value == null ? null : Math.round(value);
    }

    /** {@code DSTFlag} as a boolean: true, {@code "Y"}, {@code "true"}. */
    static boolean dstFlag(Object raw) {
        if (raw instanceof Boolean b) {
            return b;
        }
        if (raw == null) {
            return false;
        }
        String text = String.valueOf(raw).strip();
        return "y".equalsIgnoreCase(text) || "true".equalsIgnoreCase(text);
    }

    private List<NormalizedRecord> readLoad(FetchContext ctx, String subscriptionKey, String username,
            String password) throws AdapterException {
        LocalDate today = LocalDate.now(ctx.clock().withZone(MARKET_ZONE));
        String to = today.toString();
        String from = today.minusDays(WINDOW.toDays()).toString();
        URI uri = URI.create(baseUrl + LOAD_PATH + "?operatingDayFrom=" + from + "&operatingDayTo=" + to
                + "&size=1000");
        List<Map<String, Object>> rows = request(ctx, uri, subscriptionKey, username, password).rows();
        List<NormalizedRecord> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String day = text(row, "operatingDay");
            Long hourEnding = hourEnding(row.get("hourEnding"));
            if (day == null || hourEnding == null || hourEnding < 1 || hourEnding > 24) {
                continue;
            }
            boolean repeated = dstFlag(row.get("DSTFlag"));
            Instant start = marketStart(LocalDate.parse(day), (hourEnding - 1) * 60, repeated);
            if (start == null) {
                continue;
            }
            Instant end = start.plusSeconds(3600);
            String period = day + "T" + hourEnding + (repeated ? REPEATED_SUFFIX : "");
            for (String zone : ZONES) {
                Double value = SeriesPoint.number(row.get(zone));
                if (value == null) {
                    continue;
                }
                out.add(SeriesPoint.point("ercot:load:" + zone, zone + " load", "act_sys_load_by_wzn", "PT1H",
                        period, start, end, value, "MW", null, null, "final", end));
            }
        }
        return out;
    }

    private List<NormalizedRecord> readSpp(FetchContext ctx, String subscriptionKey, String username,
            String password, String hub) throws AdapterException {
        LocalDate today = LocalDate.now(ctx.clock().withZone(MARKET_ZONE));
        String to = today.toString();
        String from = today.minusDays(WINDOW.toDays()).toString();
        URI uri = URI.create(baseUrl + SPP_PATH + "?deliveryDateFrom=" + from + "&deliveryDateTo=" + to
                + "&settlementPoint=" + URLEncoder.encode(hub, StandardCharsets.UTF_8) + "&size=1000");
        List<Map<String, Object>> rows = request(ctx, uri, subscriptionKey, username, password).rows();
        List<NormalizedRecord> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String day = text(row, "deliveryDate");
            Long hour = integer(row, "deliveryHour");
            Long interval = integer(row, "deliveryInterval");
            Double price = SeriesPoint.number(row.get("settlementPointPrice"));
            if (day == null || hour == null || interval == null || price == null || hour < 1 || hour > 24
                    || interval < 1 || interval > 4) {
                continue;
            }
            boolean repeated = dstFlag(row.get("DSTFlag"));
            Instant start = marketStart(LocalDate.parse(day), (hour - 1) * 60 + (interval - 1) * 15, repeated);
            if (start == null) {
                continue;
            }
            Instant end = start.plusSeconds(900);
            String period = day + "T" + hour + ":" + interval + (repeated ? REPEATED_SUFFIX : "");
            out.add(SeriesPoint.point("ercot:spp:" + hub, hub + " settlement point price",
                    "spp_node_zone_hub", "PT15M", period, start, end, price, "[USD]/(MW.h)", hub, null, "final",
                    end));
        }
        return out;
    }

    private ErcotParser.Envelope request(FetchContext ctx, URI uri, String subscriptionKey, String username,
            String password) throws AdapterException {
        String bearer = token(ctx, username, password);
        UpstreamResponse response;
        try {
            // The client throws for any 4xx/5xx; a 401 means the cached token expired early, so refresh
            // it once and retry.
            response = ctx.http().get(uri, headers(subscriptionKey, bearer));
        } catch (UpstreamHttpException e) {
            if (e.getStatus() == 401) {
                bearer = refreshToken(ctx, username, password);
                response = ctx.http().get(uri, headers(subscriptionKey, bearer));
            } else if (e.getStatus() == 403) {
                ctx.state().degrade(
                        "geo-blocked: the ERCOT Public API is only reachable from inside the United States");
                throw new AdapterException(ID + ": ERCOT refused the client (HTTP 403); access from outside the US is"
                        + " blocked");
            } else {
                throw e;
            }
        }
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + ": " + uri.getPath() + (response.status() != 200
                    ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        return ErcotParser.parse(response.body());
    }

    private static Map<String, String> headers(String subscriptionKey, String bearer) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", subscriptionKey);
        headers.put("Authorization", "Bearer " + bearer);
        headers.put("Accept", "application/json");
        return headers;
    }

    private synchronized String token(FetchContext ctx, String username, String password) throws AdapterException {
        if (token != null && tokenAt != null && tokenAt.plus(TOKEN_TTL).isAfter(ctx.now())) {
            return token;
        }
        return refreshToken(ctx, username, password);
    }

    private synchronized String refreshToken(FetchContext ctx, String username, String password)
            throws AdapterException {
        String form = "grant_type=password"
                + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode("openid " + CLIENT_ID + " offline_access", StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
                + "&response_type=id_token";
        UpstreamResponse response = ctx.http().post(URI.create(tokenUrl),
                "application/x-www-form-urlencoded", form.getBytes(StandardCharsets.UTF_8), Map.of());
        if (response.status() != 200) {
            throw new AdapterException(ID + ": the ERCOT token endpoint answered HTTP " + response.status());
        }
        JsonNode root;
        try {
            root = JSON.readTree(response.body());
        } catch (JacksonException e) {
            throw new UpstreamFormatException("ERCOT token answer is not JSON", "", e);
        }
        String value = JsonProps.text(root, "id_token");
        if (value == null) {
            value = JsonProps.text(root, "access_token");
        }
        if (value == null) {
            throw new UpstreamFormatException("ERCOT token answer has no id_token", "");
        }
        token = value;
        tokenAt = ctx.now();
        return value;
    }

    private static String credential(FetchContext ctx, String name) {
        String value = ctx.credentials() == null ? null : ctx.credentials().get(name);
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Long integer(Map<String, Object> row, String key) {
        Double value = SeriesPoint.number(row.get(key));
        return value == null ? null : Math.round(value);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("ERCOT Public API")
                        .description("ERCOT hourly actual system load by weather zone and 15-minute settlement point"
                                + " prices at the trading hubs. Optional and disabled by default; needs a registered"
                                + " ERCOT account (username, password and subscription key) and is only reachable"
                                + " from inside the United States.")
                        .homepage("https://www.ercot.com/services/mdt/data-portal")
                        .attribution("ERCOT Public API")
                        .licence("ERCOT Terms of Use (https://www.ercot.com/help/terms)")
                        .termsUrl("https://www.ercot.com/help/terms")
                        .mode(Mode.POLLING)
                        .credential(CredentialField.required(USERNAME, "ERCOT API account username"))
                        .credential(CredentialField.required(PASSWORD, "ERCOT API account password"))
                        .credential(CredentialField.required(SUBSCRIPTION_KEY,
                                "Ocp-Apim-Subscription-Key from the ERCOT API Explorer"))
                        .param(new ParamSpec(HUBS_PARAM, ParamType.STRING_LIST,
                                "Trading hubs to read settlement point prices for", DEFAULT_HUBS, false, List.of()))
                        .interval(Duration.ofMinutes(15), Duration.ofMinutes(15))
                        .defaultBudget(Budget.of(10, 600))
                        .observedAtMode(ObservedAtMode.UPSTREAM))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        return builder.build();
    }
}
