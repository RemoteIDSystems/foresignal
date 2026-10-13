package ai.foresignal.source.adapters.roads;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.AbsenceTombstoner;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code digitraffic-road} (prompt 074): Fintraffic's current road traffic messages (Finland) as {@code road-event}
 * records ({@link DigitrafficRoadParser}). Digitraffic also publishes these as DATEX II; that route is deliberately
 * not used, because the JSON carries the same content in a far simpler shape.
 *
 * <p>Each run makes one conditional GET per situation type of the {@value #TYPES_PARAM} parameter
 * ({@code TRAFFIC_ANNOUNCEMENT, ROAD_WORK} by default; also {@code WEIGHT_RESTRICTION}, {@code EXEMPTED_TRANSPORT}),
 * on Digitraffic's v2 per-type route ({@link #ROUTES}), with {@code getConditional(uri, "msg:" + type)} and the
 * header {@code Digitraffic-User: Foresignal/<version>}; responses are gzip. A type that answers 304 keeps its
 * events; when every type answers 304 the run is {@link FetchResult#unchanged()}. Absence is tracked per type (an
 * {@link AbsenceTombstoner} scope each), so a 304 for one type never retires another's events.
 *
 * <p>Interval 5 min (at least 1 min), budget 6/min and 3,000/day. License CC BY 4.0, attribution
 * "Fintraffic / digitraffic.fi".
 */
@Component
public class DigitrafficRoadAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "digitraffic-road";

    /** The production base URL. */
    public static final String DEFAULT_BASE_URL = "https://tie.digitraffic.fi";

    /** The parameter naming the situation types to fetch. */
    public static final String TYPES_PARAM = "situationTypes";

    /** The situation types fetched when the parameter is absent. */
    public static final List<String> DEFAULT_TYPES = List.of("TRAFFIC_ANNOUNCEMENT", "ROAD_WORK");

    /** Situation type → its v2 route (verified 2026-10-13; the v1 {@code messages} route is removed after 2026-10-20). */
    public static final Map<String, String> ROUTES = Map.of(
            "TRAFFIC_ANNOUNCEMENT", "/api/traffic-message/v2/traffic-announcements",
            "ROAD_WORK", "/api/traffic-message/v2/roadworks",
            "WEIGHT_RESTRICTION", "/api/traffic-message/v2/weight-restrictions",
            "EXEMPTED_TRANSPORT", "/api/traffic-message/v2/exempted-transports");

    /** The {@code getConditional} state key prefix; the type is appended. */
    public static final String STATE_PREFIX = "msg:";

    /** The default budget. */
    public static final Budget BUDGET = Budget.of(6, 3000);

    /** The response cap per type (the road-work document was 6.9 MB decompressed on capture day). */
    public static final int MAX_RESPONSE_BYTES = 32 * 1024 * 1024;

    private final String base;
    private final Supplier<String> version;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor;

    /**
     * Creates the source.
     *
     * @param baseUrlOverride the value of {@value RoadFetch#BASE_URL_PROPERTY}; blank in production
     * @param version Foresignal's version, for the {@code Digitraffic-User} header
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public DigitrafficRoadAdapter(@Value("${" + RoadFetch.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<ForesignalVersion> version, ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride,
                () -> {
                    ForesignalVersion v = version == null ? null : version.getIfAvailable();
                    return v == null ? ForesignalVersion.FALLBACK : v.version();
                },
                meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the source with plain suppliers (tests).
     *
     * @param baseUrlOverride the test base URL (the host becomes the first path segment), or blank
     * @param version supplies Foresignal's version, or null for {@code dev}
     * @param meters supplies the meter registry, or null
     */
    public DigitrafficRoadAdapter(String baseUrlOverride, Supplier<String> version, Supplier<MeterRegistry> meters) {
        this.base = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL
                : RoadFetch.rebased(URI.create(DEFAULT_BASE_URL + "/"), baseUrlOverride).toString()
                        .replaceAll("/+$", "");
        this.version = version == null ? () -> ForesignalVersion.FALLBACK : version;
        this.meters = meters;
        this.descriptor = RoadEventDescriptors.builder(ID, "Digitraffic road traffic messages (Finland)",
                        "Fintraffic's current road traffic messages for Finland (traffic announcements, road works,"
                                + " weight restrictions, exempted transports) from the Digitraffic JSON API, as"
                                + " road-event records. Digitraffic's DATEX II publication is deliberately not used."
                                + " A situation missing from two consecutive complete fetches is tombstoned; an"
                                + " ended, retracted or closed-early situation is tombstoned at once.",
                        Duration.ofMinutes(1), Duration.ofMinutes(5), BUDGET, MAX_RESPONSE_BYTES)
                .homepage("https://www.digitraffic.fi/en/road-traffic/")
                .attribution("Fintraffic / digitraffic.fi")
                .licence("CC BY 4.0")
                .termsUrl("https://www.digitraffic.fi/en/terms-of-service/")
                .param(new ParamSpec(TYPES_PARAM, ParamType.STRING_LIST, "The situation types to fetch, one request"
                        + " each", DEFAULT_TYPES, false, List.copyOf(new java.util.TreeSet<>(ROUTES.keySet()))))
                .build();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Fetches each configured situation type once (see the class note).
     *
     * @return {@link FetchResult#unchanged()} when every type answered 304; complete when every type arrived;
     *     otherwise partial
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException on a non-200 or truncated body, or a document that does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        List<String> types = types(ctx);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Digitraffic-User", "Foresignal/" + version.get());
        headers.put("Accept", "application/json");
        List<NormalizedRecord> out = new ArrayList<>();
        int unchanged = 0;
        for (String type : types) {
            String stateKey = STATE_PREFIX + type;
            UpstreamResponse response = ctx.http().getConditional(URI.create(base + ROUTES.get(type)), stateKey,
                    headers);
            if (response.notModified()) {
                unchanged++;
                continue;
            }
            byte[] body = RoadFetch.body(ctx, response, ID, stateKey, type);
            ParsedEvents parsed;
            try {
                parsed = DigitrafficRoadParser.parse(body, now);
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), stateKey);
                AbsenceTombstoner.update(ctx.state(), type, AbsenceTombstoner.Outcome.PARSE_ERROR, List.of());
                throw e;
            }
            RoadFetch.count(meters, ctx.sourceId(), parsed.skipped());
            ctx.log().debug("{} {}: {} situation(s), {} ended, skipped {}", ctx.sourceId(), type,
                    parsed.ids().size(), parsed.endedIds().size(), parsed.skipped());
            out.addAll(RoadFetch.records(ctx, parsed, type, true, now));
        }
        if (unchanged == types.size()) {
            return FetchResult.unchanged();
        }
        return unchanged == 0 ? FetchResult.complete(out) : FetchResult.of(out);
    }

    /** The configured situation types, known ones only, in a stable order; the defaults when none is set. */
    static List<String> types(FetchContext ctx) {
        List<String> configured = ctx.params() != null && ctx.params().has(TYPES_PARAM)
                ? ctx.params().stringList(TYPES_PARAM) : DEFAULT_TYPES;
        List<String> out = new ArrayList<>();
        for (String type : configured == null ? DEFAULT_TYPES : configured) {
            String t = type == null ? null : type.strip().toUpperCase(java.util.Locale.ROOT);
            if (t != null && ROUTES.containsKey(t) && !out.contains(t)) {
                out.add(t);
            }
        }
        return out.isEmpty() ? DEFAULT_TYPES : out;
    }
}
