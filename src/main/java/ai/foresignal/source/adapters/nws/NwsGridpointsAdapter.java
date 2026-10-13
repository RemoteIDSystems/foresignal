package ai.foresignal.source.adapters.nws;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.ForecastSeriesSink;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code nws-gridpoints} (prompt 053; Weather-Imagery-Design §5.1): the official NWS gridpoint forecast for
 * each place, as one {@code nws-forecast} record per place (externalId {@code place:{placeId}}) plus a cached
 * 72-hour hourly series ({@link ForecastSeriesSink}, provider {@value #PROVIDER}).
 *
 * <p><b>Places</b>: every place ({@code allPlaces=true}, the default) or only places whose {@code products}
 * contain {@code nws}; at most {@code maxPlaces} (default 50, by id), else a health warning. A place leaving the
 * selection (deleted, or no longer selected) is tombstoned and its cached series deleted on the next run.
 *
 * <p><b>Resolution</b>: {@code GET /points/{lat},{lon}} (coordinates to 4 decimals) gives the office, the grid
 * cell, {@code forecastGridData} and the time zone; it is cached in adapter state under
 * {@code point:{placeId}} and resolved again weekly, after a 404 from the gridpoint URL, or when the place's
 * point moves. A 404 from {@code /points} means the place is outside NWS coverage: it is retried weekly and
 * named in the health warning. The {@code forecastGridData} URL is used only when it is an
 * {@code https://api.weather.gov/gridpoints/…} URL; its path is requested from the configured base.
 *
 * <p><b>Fetching</b>: each place hourly at a fixed minute of the hour derived from its id, so places spread over
 * the hour; at most {@value #MAX_PLACES_PER_RUN} places per run (interval 60 s) keep the source under 10
 * requests a minute. {@code GET {forecastGridData}} is conditional: verified 2026-10, the API returns
 * {@code Last-Modified} and {@code Cache-Control: public, max-age≈3600} but no ETag, and answers 304 to
 * {@code If-Modified-Since}; a 304 keeps the record. The identifiable User-Agent with the operator's contact is
 * the client's, as for {@code nws-alerts}; 429 and 503 honor {@code Retry-After} through the source backoff and
 * end the run. Other failures of one place (the gridpoint endpoint is known to answer 500 transiently) keep its
 * last good forecast, retry it in 10 minutes, and degrade the source after
 * {@value #FAILURES_BEFORE_DEGRADED} in a row.
 *
 * <p><b>Record</b>: {@code place}, {@code wfo}, {@code gridX}, {@code gridY}, {@code popMax24h},
 * {@code qpf24hMm}, {@code tMaxC24h}, {@code tMinC24h}, {@code gustMaxMs24h}, {@code hazards[]} and
 * {@code forecastUpdated}; {@code observedAt} = the forecast's {@code updateTime}. No geometry: a place's
 * position is sensitive (as for {@code place-weather}, prompt 052).
 */
@Component
public class NwsGridpointsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nws-gridpoints";

    /** The record kind. */
    public static final String KIND = "nws-forecast";

    /** The series provider. */
    public static final String PROVIDER = "nws";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://api.weather.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}; the same property as {@code nws-alerts}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.nws.base-url";

    /** The {@code allPlaces} parameter. */
    public static final String ALL_PLACES_PARAM = "allPlaces";

    /** The {@code maxPlaces} parameter. */
    public static final String MAX_PLACES_PARAM = "maxPlaces";

    /** The default {@code maxPlaces}. */
    public static final int DEFAULT_MAX_PLACES = 50;

    /** The product name that selects a place when {@code allPlaces} is false. */
    public static final String PRODUCT = "nws";

    /** Places fetched per run. */
    public static final int MAX_PLACES_PER_RUN = 4;

    /** Consecutive failures of one place that degrade the source. */
    public static final int FAILURES_BEFORE_DEGRADED = 3;

    /** How long a resolution is trusted. */
    public static final Duration RESOLVE_EVERY = Duration.ofDays(7);

    /** The retry delay after a failed place. */
    public static final Duration RETRY_AFTER_FAILURE = Duration.ofMinutes(10);

    /** The shortest gap between two scheduled fetches of a place. */
    static final Duration MIN_SPACING = Duration.ofMinutes(45);

    /** Retention: kept 7 days (prompt 053). */
    static final Duration RETENTION = Duration.ofDays(7);

    /** The media type asked for. */
    static final String ACCEPT = "application/geo+json";

    /** Prefix of the state key holding a place's resolution. */
    static final String POINT_PREFIX = "point:";

    /** Prefix of the state key holding a place's next due time. */
    static final String NEXT_PREFIX = "next:";

    /** Prefix of the state key holding a place's consecutive failures. */
    static final String FAILURES_PREFIX = "failures:";

    /** Prefix of the conditional-GET state key of a place's gridpoint. */
    static final String GRID_PREFIX = "grid:";

    /** The {@link LiveIdSet} scope of the selected places. */
    static final String PLACES_SCOPE = "places";

    private static final String GRIDPOINTS_PREFIX = DEFAULT_BASE_URL + "/gridpoints/";
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final Supplier<PlacePoints> places;
    private final Supplier<ForecastSeriesSink> series;

    /**
     * Creates the adapter for Spring.
     *
     * @param places the places, when the place module is present
     * @param series the forecast series cache, when present
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real API
     */
    @Autowired
    public NwsGridpointsAdapter(ObjectProvider<PlacePoints> places, ObjectProvider<ForecastSeriesSink> series,
            @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(() -> places == null ? null : places.getIfAvailable(), () -> series == null ? null : series.getIfAvailable(),
                baseUrlOverride);
    }

    /**
     * Creates the adapter.
     *
     * @param places supplies the places (null or a null result means none)
     * @param series supplies the series sink (null or a null result keeps nothing)
     * @param baseUrlOverride a base URL, or blank for the real API
     */
    public NwsGridpointsAdapter(Supplier<PlacePoints> places, Supplier<ForecastSeriesSink> series, String baseUrlOverride) {
        this.places = places == null ? () -> null : places;
        this.series = series == null ? () -> null : series;
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params != null && params.has(MAX_PLACES_PARAM) && params.integer(MAX_PLACES_PARAM) != null) {
            int v = params.integer(MAX_PLACES_PARAM);
            if (v < 1 || v > 500) {
                problems.put(MAX_PLACES_PARAM, "must be between 1 and 500");
            }
        }
        return problems;
    }

    /** The {@code /points} URL of a coordinate pair, 4 decimals, trailing zeros removed. */
    public URI pointsUri(double lat, double lon) {
        return URI.create(baseUrl + "/points/" + coordinate(lat) + "," + coordinate(lon));
    }

    /** A coordinate to at most 4 decimals ({@code 32.7767}, {@code -96.797}). */
    static String coordinate(double v) {
        BigDecimal d = BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
        String s = d.toPlainString();
        return s.equals("-0") ? "0" : s;
    }

    /**
     * The URL to request for an upstream {@code forecastGridData}: its path under the configured base, and only
     * when it is an {@code https://api.weather.gov/gridpoints/} URL (an upstream document never sends the client
     * anywhere else).
     */
    Optional<URI> gridUri(String forecastGridData) {
        if (forecastGridData == null || !forecastGridData.startsWith(GRIDPOINTS_PREFIX)) {
            return Optional.empty();
        }
        String path = forecastGridData.substring(DEFAULT_BASE_URL.length());
        if (!path.matches("/gridpoints/[A-Z]{3}/\\d{1,4},\\d{1,4}")) {
            return Optional.empty();
        }
        return Optional.of(URI.create(baseUrl + path));
    }

    /** The minute of the hour at which a place is fetched: fixed per place id. */
    static int slotMinute(String placeId) {
        return Math.floorMod(placeId.hashCode(), 60);
    }

    /** The next scheduled fetch of a place after {@code after}: its slot minute, at least 45 minutes later. */
    static Instant nextDue(String placeId, Instant after) {
        Instant candidate = after.truncatedTo(ChronoUnit.HOURS).plus(Duration.ofMinutes(slotMinute(placeId)));
        while (candidate.isBefore(after.plus(MIN_SPACING))) {
            candidate = candidate.plus(Duration.ofHours(1));
        }
        return candidate;
    }

    /** The selected places: all, or those with product {@value #PRODUCT}, by id. */
    static List<PlacePoints.Summary> selected(List<PlacePoints.Summary> all, boolean allPlaces) {
        return all.stream()
                .filter(p -> allPlaces || p.products().stream().anyMatch(PRODUCT::equalsIgnoreCase))
                .sorted(Comparator.comparing(PlacePoints.Summary::id))
                .toList();
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        AdapterStateStore state = ctx.state();
        PlacePoints placePoints = places.get();
        ForecastSeriesSink sink = Optional.ofNullable(series.get()).orElse(ForecastSeriesSink.none());
        boolean allPlaces = ctx.params().bool(ALL_PLACES_PARAM, true);
        int maxPlaces = ctx.params().integer(MAX_PLACES_PARAM, DEFAULT_MAX_PLACES);
        List<PlacePoints.Summary> chosen = selected(placePoints == null ? List.of() : placePoints.summaries(), allPlaces);
        List<String> problems = new ArrayList<>();
        if (chosen.size() > maxPlaces) {
            problems.add(chosen.size() + " places selected; only the first " + maxPlaces + " by id get an NWS forecast"
                    + " (parameter maxPlaces)");
            chosen = chosen.subList(0, maxPlaces);
        }

        // Places that left the selection: tombstone their records and drop their state and series.
        List<NormalizedRecord> out = new ArrayList<>();
        List<String> ids = chosen.stream().map(PlacePoints.Summary::id).toList();
        for (String gone : LiveIdSet.absentSince(state, PLACES_SCOPE, ids)) {
            out.add(NormalizedRecord.tombstone(externalId(gone), KIND, now));
            sink.remove(gone, PROVIDER);
            forget(state, gone);
        }

        List<PlacePoints.Summary> due = chosen.stream()
                .filter(p -> due(state, p.id(), now))
                .sorted(Comparator.comparing((PlacePoints.Summary p) -> nextDueStored(state, p.id()).orElse(Instant.MIN))
                        .thenComparing(PlacePoints.Summary::id))
                .limit(MAX_PLACES_PER_RUN)
                .toList();
        AdapterException stop = null;
        for (PlacePoints.Summary place : due) {
            try {
                fetchPlace(ctx, place, sink, now).ifPresent(out::add);
            } catch (AdapterException e) {
                if (stoppingFailure(e)) {
                    // Throttled, out of budget or unreachable: end the run; the backoff and budget decide the next.
                    stop = e;
                    break;
                }
                int failures = recordFailure(state, place.id(), now);
                ctx.log().warn("nws-gridpoints {}: {} (failure {} in a row)", place.id(), e.getMessage(), failures);
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(state, GRID_PREFIX + place.id());
                int failures = recordFailure(state, place.id(), now);
                ctx.log().warn("nws-gridpoints {}: unreadable forecast: {} (failure {} in a row)", place.id(),
                        e.getMessage(), failures);
            }
        }
        for (PlacePoints.Summary place : chosen) {
            int failures = failures(state, place.id());
            if (failures >= FAILURES_BEFORE_DEGRADED) {
                problems.add("place " + place.id() + ": the gridpoint forecast failed " + failures
                        + " times in a row; the last good forecast is kept");
            }
            if (resolution(state, place.id()).map(r -> r.path("outside").asBoolean(false)).orElse(false)) {
                problems.add("place " + place.id() + " is outside NWS gridpoint coverage");
            }
        }
        if (stop != null && out.isEmpty()) {
            throw stop;
        }
        if (problems.isEmpty()) {
            state.clearDegraded();
        } else {
            state.degrade(String.join("; ", problems));
        }
        return FetchResult.of(out);
    }

    /** True for a failure that ends the run rather than one place: no HTTP status (budget, backoff, I/O), 429, 503. */
    static boolean stoppingFailure(AdapterException e) {
        int status = e.httpStatus().orElse(0);
        return status == 0 || status == 429 || status == 503;
    }

    /**
     * Fetches one place: resolves it when needed, then the conditional gridpoint request.
     *
     * @return the new record, or empty on 304 or when the place cannot be forecast
     */
    Optional<NormalizedRecord> fetchPlace(FetchContext ctx, PlacePoints.Summary place, ForecastSeriesSink sink,
            Instant now) throws AdapterException {
        AdapterStateStore state = ctx.state();
        Optional<ObjectNode> resolved = resolve(ctx, place, now);
        if (resolved.isEmpty() || resolved.get().path("outside").asBoolean(false)) {
            state.put(NEXT_PREFIX + place.id(), nextDue(place.id(), now).toString());
            return Optional.empty();
        }
        ObjectNode r = resolved.get();
        Optional<URI> grid = gridUri(r.path("forecastGridData").asString(null));
        if (grid.isEmpty()) {
            state.remove(POINT_PREFIX + place.id());
            throw new UpstreamFormatException("NWS /points gave an unexpected forecastGridData URL", null);
        }
        UpstreamResponse response;
        try {
            response = ctx.http().getConditional(grid.get(), GRID_PREFIX + place.id());
        } catch (AdapterException e) {
            if (e.httpStatus().orElse(0) == 404) {
                // The gridpoint moved or no longer exists: resolve the place again on the next attempt.
                state.remove(POINT_PREFIX + place.id());
                SourceHttp.forgetValidators(state, GRID_PREFIX + place.id());
            }
            throw e;
        }
        if (response.notModified()) {
            succeeded(state, place.id(), now);
            return Optional.empty();
        }
        if (response.truncated()) {
            throw new UpstreamFormatException("NWS gridpoint forecast exceeded maxResponseBytes", null);
        }
        NwsGridpointsParser.Forecast forecast = NwsGridpointsParser.parseForecast(response.body(), now);
        sink.put(place.id(), PROVIDER, forecast.updateTime(), forecast.start(), forecast.series());
        succeeded(state, place.id(), now);
        return Optional.of(record(place, forecast));
    }

    /** The record of one place's forecast. */
    static NormalizedRecord record(PlacePoints.Summary place, NwsGridpointsParser.Forecast forecast) {
        List<Map<String, Object>> hazards = new ArrayList<>();
        for (NwsGridpointsParser.Hazard h : forecast.hazards()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", h.type());
            if (h.validTime() != null) {
                m.put("validTime", h.validTime());
            }
            hazards.add(m);
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId(place.id()), KIND)
                .observedAt(forecast.updateTime())
                .put("place", place.name())
                .put("wfo", forecast.wfo())
                .put("gridX", forecast.gridX())
                .put("gridY", forecast.gridY());
        forecast.summary().forEach(b::put);
        return b.put("hazards", hazards)
                .put("forecastUpdated", forecast.updateTime().toString())
                .build();
    }

    /** The externalId of a place's record. */
    public static String externalId(String placeId) {
        return "place:" + placeId;
    }

    /**
     * The place's resolution: the cached one while it is fresh and the point has not moved, else a new
     * {@code /points} request.
     */
    Optional<ObjectNode> resolve(FetchContext ctx, PlacePoints.Summary place, Instant now) throws AdapterException {
        AdapterStateStore state = ctx.state();
        String lat = coordinate(place.lat());
        String lon = coordinate(place.lon());
        Optional<ObjectNode> cached = resolution(state, place.id());
        if (cached.isPresent()) {
            ObjectNode c = cached.get();
            Instant at = parseInstant(c.path("resolvedAt").asString(null));
            boolean fresh = at != null && Duration.between(at, now).compareTo(RESOLVE_EVERY) < 0;
            boolean samePoint = lat.equals(c.path("lat").asString(null)) && lon.equals(c.path("lon").asString(null));
            if (fresh && samePoint) {
                return cached;
            }
            if (!samePoint) {
                // The place moved: its old gridpoint's validators must not answer for the new one.
                SourceHttp.forgetValidators(state, GRID_PREFIX + place.id());
            }
        }
        ObjectNode node = JSON.createObjectNode();
        node.put("lat", lat);
        node.put("lon", lon);
        node.put("resolvedAt", now.toString());
        UpstreamResponse response;
        try {
            response = ctx.http().get(pointsUri(place.lat(), place.lon()), Map.of("Accept", ACCEPT));
        } catch (AdapterException e) {
            if (e.httpStatus().orElse(0) == 404) {
                node.put("outside", true);
                state.put(POINT_PREFIX + place.id(), node.toString());
                return Optional.of(node);
            }
            throw e;
        }
        NwsGridpointsParser.Point point = NwsGridpointsParser.parsePoint(response.body());
        node.put("gridId", point.gridId());
        node.put("gridX", point.gridX());
        node.put("gridY", point.gridY());
        node.put("forecastGridData", point.forecastGridData());
        if (point.timeZone() != null) {
            node.put("timeZone", point.timeZone());
        }
        state.put(POINT_PREFIX + place.id(), node.toString());
        return Optional.of(node);
    }

    /** The cached resolution of a place, or empty. */
    static Optional<ObjectNode> resolution(AdapterStateStore state, String placeId) {
        Optional<String> raw = state.get(POINT_PREFIX + placeId);
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        try {
            JsonNode node = JSON.readTree(raw.get());
            return node instanceof ObjectNode o ? Optional.of(o) : Optional.empty();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static boolean due(AdapterStateStore state, String placeId, Instant now) {
        return nextDueStored(state, placeId).map(next -> !now.isBefore(next)).orElse(true);
    }

    private static Optional<Instant> nextDueStored(AdapterStateStore state, String placeId) {
        return state.get(NEXT_PREFIX + placeId).map(NwsGridpointsAdapter::parseInstant);
    }

    private static void succeeded(AdapterStateStore state, String placeId, Instant now) {
        state.remove(FAILURES_PREFIX + placeId);
        state.put(NEXT_PREFIX + placeId, nextDue(placeId, now).toString());
    }

    private static int recordFailure(AdapterStateStore state, String placeId, Instant now) {
        int failures = failures(state, placeId) + 1;
        state.put(FAILURES_PREFIX + placeId, Integer.toString(failures));
        state.put(NEXT_PREFIX + placeId, now.plus(RETRY_AFTER_FAILURE).toString());
        return failures;
    }

    static int failures(AdapterStateStore state, String placeId) {
        try {
            return state.get(FAILURES_PREFIX + placeId).map(Integer::parseInt).orElse(0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void forget(AdapterStateStore state, String placeId) {
        state.remove(POINT_PREFIX + placeId);
        state.remove(NEXT_PREFIX + placeId);
        state.remove(FAILURES_PREFIX + placeId);
        SourceHttp.forgetValidators(state, GRID_PREFIX + placeId);
    }

    private static Instant parseInstant(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Instant.parse(s);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("NWS gridpoint forecasts")
                .description("The official National Weather Service gridpoint forecast for each place (api.weather.gov"
                        + " /points then /gridpoints, hourly, conditional): 24-hour precipitation probability and amount,"
                        + " temperature range, peak gust and hazards, plus a cached 72-hour hourly series.")
                .kind(KIND)
                .homepage("https://www.weather.gov/documentation/services-web-api")
                .attribution("NOAA National Weather Service")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .param(ParamSpec.optional(ALL_PLACES_PARAM, ParamType.BOOLEAN,
                        "Forecast every place; false limits it to places whose products contain nws", true))
                .param(ParamSpec.optional(MAX_PLACES_PARAM, ParamType.INTEGER,
                        "The most places forecast (by id); more raise a health warning", DEFAULT_MAX_PLACES))
                .interval(Duration.ofSeconds(60), Duration.ofSeconds(60))
                .defaultBudget(10, 3_000)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .titleField("place")
                .searchFields("place", "wfo")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{place}", "forecast", "#1C7ED6", null));
        builder.fieldSpec("place", new FieldSpec(FieldType.STRING, null, "Place", "Place name", null, Set.of()));
        builder.fieldSpec("wfo", new FieldSpec(FieldType.STRING, null, "Office", "NWS forecast office (gridId)", null,
                Set.of()));
        builder.fieldSpec("gridX", new FieldSpec(FieldType.INTEGER, null, "Grid X", "Gridpoint column", null, Set.of()));
        builder.fieldSpec("gridY", new FieldSpec(FieldType.INTEGER, null, "Grid Y", "Gridpoint row", null, Set.of()));
        builder.fieldSpec("popMax24h", new FieldSpec(FieldType.INTEGER, "%", "Max precipitation chance",
                "Highest probability of precipitation in the next 24 hours", null, Set.of()));
        builder.fieldSpec("qpf24hMm", new FieldSpec(FieldType.NUMBER, "mm", "Precipitation (24 h)",
                "Forecast precipitation over the next 24 hours", null, Set.of()));
        builder.fieldSpec("tMaxC24h", new FieldSpec(FieldType.NUMBER, "Cel", "High (24 h)",
                "Highest forecast temperature in the next 24 hours", null, Set.of()));
        builder.fieldSpec("tMinC24h", new FieldSpec(FieldType.NUMBER, "Cel", "Low (24 h)",
                "Lowest forecast temperature in the next 24 hours", null, Set.of()));
        builder.fieldSpec("gustMaxMs24h", new FieldSpec(FieldType.NUMBER, "m/s", "Peak gust (24 h)",
                "Highest forecast wind gust in the next 24 hours", null, Set.of()));
        builder.fieldSpec("hazards", new FieldSpec(FieldType.ARRAY, null, "Hazards",
                "At most 10 NWS hazards: type (phenomenon.significance, for example FZ.W) and validTime (ISO-8601"
                        + " interval)", null, Set.of()));
        builder.fieldSpec("forecastUpdated", new FieldSpec(FieldType.TIMESTAMP, null, "Forecast updated",
                "The forecast's updateTime (observedAt)", null, Set.of()));
        return builder.build();
    }
}
