package ai.foresignal.social.correlation;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.inbound.edp.EdpSearchService;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.security.Ulids;
import ai.foresignal.social.CueRequests;
import ai.foresignal.social.SocialEvents;
import ai.foresignal.social.SocialPlatformService;
import ai.foresignal.social.SocialPlatformSettings;
import ai.foresignal.social.TermExpansion;
import ai.foresignal.social.XSpend;
import ai.foresignal.store.FetchTrigger;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Cue-driven social search (prompt 137, {@code SocialCueService}).
 *
 * <p>A cue builds exact queries from a place (names and multilingual aliases, nearby facilities), an entity or a
 * situation and the event terms of its families in the region's languages ({@link CueQueryBuilder}), and runs them on
 * the enabled search-capable adapters ({@code bluesky-search}, {@code reddit}, {@code x}, {@code threads}; optionally
 * X through {@code xai-x-search}) within {@code social.cues.maxPerDay} (50), the cue's cost cap
 * ({@code maxCostUsdPerCue}, 1.00) and the platforms' own budgets (X spend is estimated first and refused at the cap,
 * SOC-4). Adapters store what they find as posts with {@code collection = cue} and the cue id; those posts join
 * narratives (136) and are compared (137 comparator) like any others, but they are a targeted sample: they never feed
 * baselines, their comparisons record {@code foundBy: cue}, and a cue never corroborates its own trigger (SOC-3).
 *
 * <p>Automatic cues come from physical triggers ({@link CueTriggers}); manual cues from
 * {@code POST /v1/social/cues} (agent), answered 202 with the queued cue.
 */
@Component
public class SocialCueService {

    private static final Logger log = LoggerFactory.getLogger(SocialCueService.class);

    /** How long a trigger is not cued again. */
    public static final Duration TRIGGER_DEDUP = Duration.ofHours(24);

    /** How long after completion a cue's narratives are refreshed. */
    static final Duration NARRATIVE_REFRESH = Duration.ofHours(24);

    /** The default results per query. */
    static final int DEFAULT_MAX_RESULTS = 50;

    /** The facility search radius (m). */
    static final double FACILITY_RADIUS_M = 25_000;

    /**
     * What a cue searches around.
     *
     * @param placeId a 036 place, or null
     * @param regionCode a 040 region, or null
     * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
     * @param entityRef a 098 entity ref ({@code entity:…}), or null
     * @param situationId a 118 situation, or null
     * @param lon a point longitude (automatic triggers), or null
     * @param lat a point latitude, or null
     * @param families signal families whose lexicon terms are added
     * @param terms extra terms
     * @param platforms the adapters asked for (empty: every enabled one)
     * @param from the window start
     * @param to the window end, or null
     * @param maxCostUsd the cost cap, or null for the default
     */
    public record Target(String placeId, String regionCode, double[] bbox, String entityRef, String situationId,
            Double lon, Double lat, List<String> families, List<String> terms, List<String> platforms, Instant from,
            Instant to, Double maxCostUsd) {

        /** Copies the lists. */
        public Target {
            families = families == null ? List.of() : List.copyOf(families);
            terms = terms == null ? List.of() : List.copyOf(terms);
            platforms = platforms == null ? List.of() : List.copyOf(platforms);
        }
    }

    private final ComparisonStore store;
    private final SocialSettingsService settings;
    private final ObjectProvider<SocialPlatformService> platforms;
    private final ObjectProvider<XSpend> spend;
    private final CueRequests requests;
    private final ObjectProvider<FetchExecutor> fetches;
    private final TermExpansion names;
    private final XaiXSearchBackend xai;
    private final SocialEvents events;
    private final ObjectProvider<ManifestService> manifests;
    private final ObjectProvider<EdpSearchService> edpSearch;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final boolean async;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /** Creates the service. */
    public SocialCueService(ComparisonStore store, SocialSettingsService settings,
            ObjectProvider<SocialPlatformService> platforms, ObjectProvider<XSpend> spend, CueRequests requests,
            ObjectProvider<FetchExecutor> fetches, TermExpansion names, XaiXSearchBackend xai, SocialEvents events,
            ObjectProvider<ManifestService> manifests, ObjectProvider<EdpSearchService> edpSearch, JdbcTemplate jdbc,
            Clock clock, @Value("${foresignal.social.cues-async:true}") boolean async) {
        this.store = store;
        this.settings = settings;
        this.platforms = platforms;
        this.spend = spend;
        this.requests = requests;
        this.fetches = fetches;
        this.names = names;
        this.xai = xai;
        this.events = events;
        this.manifests = manifests;
        this.edpSearch = edpSearch;
        this.jdbc = jdbc;
        this.clock = clock;
        this.async = async;
    }

    /** Stops the cue executor. */
    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    // ------------------------------------------------------------------------- input

    /**
     * Parses a {@code SocialCueInput}.
     *
     * @throws ValidationException 400 {@code VALIDATION_FAILED}
     */
    public static Target parse(JsonNode body) {
        List<FieldError> errors = new ArrayList<>();
        if (body == null || !body.isObject()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The body must be a SocialCueInput object.",
                    List.of(new FieldError("body", "must be an object")));
        }
        Set<String> allowed = Set.of("placeId", "regionCode", "bbox", "entityRef", "situationId", "terms", "families",
                "platforms", "window", "maxCostUsd");
        for (String f : body.propertyNames()) {
            if (!allowed.contains(f)) {
                errors.add(new FieldError(f, "is not a SocialCueInput member"));
            }
        }
        String placeId = text(body, "placeId");
        String region = text(body, "regionCode");
        if (region != null && !region.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
            errors.add(new FieldError("regionCode", "must be a 040 region code"));
        }
        double[] bbox = null;
        String bboxText = text(body, "bbox");
        if (bboxText != null) {
            String[] parts = bboxText.split(",");
            try {
                if (parts.length != 4) {
                    throw new NumberFormatException();
                }
                bbox = new double[4];
                for (int i = 0; i < 4; i++) {
                    bbox[i] = Double.parseDouble(parts[i].strip());
                }
                if (bbox[0] < -180 || bbox[2] > 180 || bbox[1] < -90 || bbox[3] > 90 || bbox[0] > bbox[2]
                        || bbox[1] > bbox[3]) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                errors.add(new FieldError("bbox", "must be minLon,minLat,maxLon,maxLat"));
                bbox = null;
            }
        }
        String entityRef = text(body, "entityRef");
        if (entityRef != null && !entityRef.startsWith("entity:")) {
            errors.add(new FieldError("entityRef", "must be an entity ref (entity:{id})"));
        }
        String situationId = text(body, "situationId");
        if (situationId != null && !Ulids.isUlid(situationId)) {
            errors.add(new FieldError("situationId", "must be a ULID"));
        }
        if (placeId == null && region == null && bboxText == null && entityRef == null && situationId == null) {
            errors.add(new FieldError("placeId", "give a placeId, regionCode, bbox, entityRef or situationId"));
        }
        List<String> terms = strings(body, "terms", errors);
        if (terms.size() > 20) {
            errors.add(new FieldError("terms", "holds more than 20 terms"));
        }
        for (String t : terms) {
            if (t.length() > 100) {
                errors.add(new FieldError("terms", "terms are at most 100 characters"));
            }
        }
        List<String> families = strings(body, "families", errors);
        List<String> requested = strings(body, "platforms", errors);
        for (String p : requested) {
            if (!CorrelationVocabulary.CUE_ADAPTERS.contains(p)) {
                errors.add(new FieldError("platforms", "'" + p + "' is not a search-capable adapter; one of "
                        + CorrelationVocabulary.CUE_ADAPTERS));
            }
        }
        Instant from = null;
        Instant to = null;
        JsonNode window = body.get("window");
        if (window == null || !window.isObject() || !window.hasNonNull("start")) {
            errors.add(new FieldError("window.start", "is required"));
        } else {
            try {
                from = Instant.parse(window.get("start").asString());
                to = window.hasNonNull("end") ? Instant.parse(window.get("end").asString()) : null;
                if (to != null && !to.isAfter(from)) {
                    errors.add(new FieldError("window.end", "must be after window.start"));
                }
            } catch (RuntimeException e) {
                errors.add(new FieldError("window", "start and end must be RFC 3339 times"));
            }
        }
        Double maxCost = null;
        if (body.hasNonNull("maxCostUsd")) {
            if (!body.get("maxCostUsd").isNumber() || body.get("maxCostUsd").asDouble() < 0) {
                errors.add(new FieldError("maxCostUsd", "must be a number ≥ 0"));
            } else {
                maxCost = body.get("maxCostUsd").asDouble();
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The cue is not valid.", errors);
        }
        return new Target(placeId, region, bbox, entityRef, situationId, null, null, families, terms, requested, from,
                to, maxCost);
    }

    private static String text(JsonNode body, String field) {
        JsonNode v = body.get(field);
        return v == null || v.isNull() || !v.isString() || v.asString().isBlank() ? null : v.asString().strip();
    }

    private static List<String> strings(JsonNode body, String field, List<FieldError> errors) {
        JsonNode v = body.get(field);
        List<String> out = new ArrayList<>();
        if (v == null || v.isNull()) {
            return out;
        }
        if (!v.isArray()) {
            errors.add(new FieldError(field, "must be an array of strings"));
            return out;
        }
        for (JsonNode i : v) {
            if (i.isString() && !i.asString().isBlank() && !out.contains(i.asString().strip())) {
                out.add(i.asString().strip());
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- create

    /**
     * Queues a manual cue (agent).
     *
     * @throws ValidationException 400 when the cue builds no query
     * @throws NotFoundException 404 {@code UNKNOWN_SITUATION} or {@code NOT_FOUND}
     * @throws ForesignalException 409 {@code PLATFORM_DISABLED}, 429 {@code CUE_LIMIT_REACHED} or
     *     {@code BUDGET_EXCEEDED}
     */
    public Map<String, Object> createManual(Target target, String actor) {
        ComparisonStore.Cue cue = create("manual", null, target, actor == null ? "agent" : actor, true);
        return CorrelationViews.cue(cue);
    }

    /**
     * Queues an automatic cue for a physical trigger, unless cues are off, the trigger was cued in the last 24 hours, the
     * daily limit is reached or no platform can run it.
     *
     * @return the cue id, or null when none was queued
     */
    public String createAutomatic(String triggerType, String triggerRef, Target target) {
        SocialSettingsService.Cues c = settings.cues();
        if (!c.enabled() || (triggerRef != null && store.cuedSince(triggerRef, clock.instant().minus(TRIGGER_DEDUP)))) {
            return null;
        }
        try {
            return create(triggerType, triggerRef, target, "system", false).id();
        } catch (ForesignalException e) {
            log.debug("Automatic cue for {} not queued: {}", triggerRef, e.getMessage());
            return null;
        }
    }

    /** Validates, builds the queries, checks caps and budgets, and stores the cue (queued, or refused by budget). */
    ComparisonStore.Cue create(String triggerType, String triggerRef, Target target, String actor, boolean manual) {
        Instant now = clock.instant();
        SocialSettingsService.Cues c = settings.cues();
        Instant midnight = now.truncatedTo(ChronoUnit.DAYS);
        if (store.cuesSince(midnight) >= c.maxPerDay()) {
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.CUE_LIMIT_REACHED, "The daily cue"
                    + " limit of " + c.maxPerDay() + " is reached; it resets at 00:00 UTC.")
                    .withHeader("Retry-After", Long.toString(Math.max(1, Duration.between(now, midnight.plus(
                            Duration.ofDays(1))).toSeconds())));
        }
        double cap = target.maxCostUsd() == null ? c.maxCostUsdPerCue() : Math.min(target.maxCostUsd(),
                c.maxCostUsdPerCue());
        Resolved where = resolve(target);
        List<String> usable = usablePlatforms(target.platforms(), c.platforms());
        boolean viaXai = c.xaiEnabled() && (target.platforms().isEmpty() || target.platforms().contains("x"));
        if (viaXai && !usable.contains("x")) {
            usable.add("x");
        }
        if (usable.isEmpty()) {
            throw new ForesignalException(HttpStatus.CONFLICT, ErrorCodes.PLATFORM_DISABLED, "No requested platform is"
                    + " enabled and search-capable; enable one of " + CorrelationVocabulary.CUE_ADAPTERS + ".");
        }
        List<String> families = new ArrayList<>(target.families());
        if (families.isEmpty()) {
            families.addAll(where.families());
        }
        List<CueQueryBuilder.Query> queries = CueQueryBuilder.build(new CueQueryBuilder.Spec(where.names(),
                where.facilities(), where.entities(), families, target.terms(), CueQueryBuilder.languages(
                        where.regionCode())), usable);
        if (queries.isEmpty()) {
            throw ValidationException.forField("placeId", "no place, facility or entity name could be resolved to search"
                    + " for");
        }
        int maxResults = DEFAULT_MAX_RESULTS;
        List<Object> queryViews = new ArrayList<>();
        BigDecimal budget = BigDecimal.valueOf(cap).setScale(4, RoundingMode.HALF_UP);
        BigDecimal planned = BigDecimal.ZERO;
        List<CueQueryBuilder.Query> kept = new ArrayList<>();
        String refusal = null;
        for (CueQueryBuilder.Query q : queries) {
            BigDecimal estimate = estimate(q.platform(), viaXai, maxResults);
            if (planned.add(estimate).compareTo(budget) > 0) {
                refusal = "BUDGET_EXCEEDED: the cue's cost cap of $" + budget.toPlainString() + " leaves no room for "
                        + q.platform();
                continue;
            }
            if ("x".equals(q.platform()) && !viaXai && estimate.signum() > 0) {
                XSpend x = spend.getIfAvailable();
                ForesignalException refused = x == null ? null : x.refusal("x", xSettings(), planned.add(estimate));
                if (refused != null) {
                    if (manual && usable.size() == 1) {
                        throw refused;
                    }
                    refusal = "BUDGET_EXCEEDED: " + refused.getMessage();
                    continue;
                }
            }
            planned = planned.add(estimate);
            kept.add(q);
            queryViews.add(q.toMap(estimate.doubleValue()));
        }
        if (kept.isEmpty() && manual) {
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.BUDGET_EXCEEDED, refusal == null
                    ? "The cue's cost cap leaves no room for any query." : refusal).withProperty("maxCostUsd", cap);
        }
        Set<String> used = new LinkedHashSet<>();
        kept.forEach(q -> used.add(q.platform()));
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("placeId", target.placeId());
        input.put("regionCode", target.regionCode());
        input.put("bbox", target.bbox() == null ? null : List.of(target.bbox()[0], target.bbox()[1], target.bbox()[2],
                target.bbox()[3]));
        input.put("entityRef", target.entityRef());
        input.put("situationId", target.situationId());
        input.put("families", families);
        input.put("terms", target.terms());
        input.put("maxResults", maxResults);
        input.put("viaXai", viaXai);
        Instant to = target.to();
        ComparisonStore.Cue cue = new ComparisonStore.Cue(Ulids.next(now), triggerType, triggerRef,
                viaXai && used.size() == 1 && used.contains("x") ? CorrelationVocabulary.BACKEND_XAI
                        : CorrelationVocabulary.BACKEND_PLATFORMS, queryViews, new ArrayList<>(used), target.from(), to,
                kept.isEmpty() ? CorrelationVocabulary.CUE_BUDGET_REFUSED : CorrelationVocabulary.CUE_QUEUED,
                kept.isEmpty() ? refusal : null, List.of(), List.of(), BigDecimal.ZERO, budget, input, actor, now, null,
                kept.isEmpty() ? now : null, null, List.of());
        store.insertCue(cue);
        if (kept.isEmpty()) {
            events.emit(CorrelationVocabulary.EVENT_CUE, cueEvent(cue), false);
            return cue;
        }
        if (async) {
            String id = cue.id();
            ai.foresignal.forecast.ForecastEvents.afterCommit(() -> executor.submit(() -> {
                try {
                    run(id);
                } catch (RuntimeException e) {
                    log.warn("Cue {} failed: {}", id, e.toString());
                }
            }));
        }
        return cue;
    }

    /** The estimated cost of one query. */
    BigDecimal estimate(String platform, boolean viaXai, int maxResults) {
        if (!"x".equals(platform)) {
            return BigDecimal.ZERO;
        }
        if (viaXai) {
            return XaiXSearchBackend.estimate(maxResults);
        }
        return XSpend.estimateSearch(xSettings(), Math.max(10, Math.min(100, maxResults)));
    }

    private SocialPlatformSettings xSettings() {
        SocialPlatformService service = platforms.getIfAvailable();
        return service == null ? null : service.settings("x");
    }

    /** The asked-for adapters that are allowed by the settings and enabled. */
    List<String> usablePlatforms(List<String> requested, List<String> allowed) {
        List<String> out = new ArrayList<>();
        SocialPlatformService service = platforms.getIfAvailable();
        for (String p : requested.isEmpty() ? CorrelationVocabulary.CUE_ADAPTERS : requested) {
            if (!allowed.isEmpty() && !allowed.contains(p)) {
                continue;
            }
            try {
                if (service != null) {
                    service.requireEnabled(p);
                    out.add(p);
                }
            } catch (RuntimeException e) {
                // disabled or unknown: not usable
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- place resolution

    /**
     * What a target resolves to.
     *
     * @param names place and region names and aliases
     * @param facilities nearby facility names
     * @param entities entity names
     * @param families families of a situation target
     * @param regionCode the region (for languages), or null
     */
    record Resolved(List<String> names, List<String> facilities, List<String> entities, List<String> families,
            String regionCode) {}

    /** Resolves a target's names (404 for an unknown situation or place). */
    Resolved resolve(Target t) {
        Set<String> placeNames = new LinkedHashSet<>();
        Set<String> facilities = new LinkedHashSet<>();
        Set<String> entities = new LinkedHashSet<>();
        Set<String> families = new LinkedHashSet<>();
        String region = t.regionCode();
        Double lon = t.lon();
        Double lat = t.lat();
        if (t.situationId() != null) {
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT region_codes, signal_families, entity_ids,"
                    + " ST_X(centroid) AS lon, ST_Y(centroid) AS lat FROM situation WHERE id = ? AND merged_into IS NULL",
                    t.situationId());
            if (rows.isEmpty()) {
                throw new NotFoundException(ErrorCodes.UNKNOWN_SITUATION, "No situation '" + t.situationId() + "'.");
            }
            Map<String, Object> row = rows.get(0);
            List<String> regions = array(row.get("region_codes"));
            if (region == null && !regions.isEmpty()) {
                region = regions.get(0);
            }
            for (String r : regions.subList(0, Math.min(2, regions.size()))) {
                placeNames.addAll(names.region(r));
            }
            families.addAll(array(row.get("signal_families")));
            for (String e : array(row.get("entity_ids"))) {
                entities.addAll(names.entity(e));
            }
            if (lon == null && row.get("lon") instanceof Number x && row.get("lat") instanceof Number y) {
                lon = x.doubleValue();
                lat = y.doubleValue();
            }
        }
        if (t.placeId() != null) {
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT ST_X(ST_PointOnSurface(geom)) AS lon,"
                    + " ST_Y(ST_PointOnSurface(geom)) AS lat FROM place WHERE id = ?", t.placeId());
            if (rows.isEmpty()) {
                throw new NotFoundException(ErrorCodes.NOT_FOUND, "No place '" + t.placeId() + "'.");
            }
            List<String> n = names.place(t.placeId());
            placeNames.addAll(n);
            n.forEach(name -> placeNames.addAll(CueQueryBuilder.aliases(name)));
            if (rows.get(0).get("lon") instanceof Number x && rows.get(0).get("lat") instanceof Number y) {
                lon = x.doubleValue();
                lat = y.doubleValue();
            }
        }
        if (t.regionCode() != null) {
            List<String> n = names.region(t.regionCode());
            placeNames.addAll(n);
            n.forEach(name -> placeNames.addAll(CueQueryBuilder.aliases(name)));
        }
        if (t.bbox() != null) {
            double[] b = t.bbox();
            placeNames.addAll(jdbc.queryForList("SELECT name FROM place WHERE name IS NOT NULL AND geom && "
                    + "ST_MakeEnvelope(?, ?, ?, ?, 4326) ORDER BY name LIMIT 5", String.class, b[0], b[1], b[2], b[3]));
            if (lon == null) {
                lon = (b[0] + b[2]) / 2;
                lat = (b[1] + b[3]) / 2;
            }
        }
        if (t.entityRef() != null) {
            entities.addAll(names.entity(t.entityRef().substring("entity:".length())));
        }
        if (lon != null && lat != null) {
            try {
                facilities.addAll(jdbc.queryForList("SELECT name FROM infra_node WHERE name IS NOT NULL AND point IS NOT"
                        + " NULL AND ST_DWithin(point::geography, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?)"
                        + " ORDER BY ST_Distance(point::geography, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography)"
                        + " LIMIT 3", String.class, lon, lat, FACILITY_RADIUS_M, lon, lat));
            } catch (RuntimeException e) {
                log.debug("No facilities near {},{}: {}", lon, lat, e.toString());
            }
        }
        return new Resolved(new ArrayList<>(placeNames), new ArrayList<>(facilities), new ArrayList<>(entities),
                new ArrayList<>(families), region);
    }

    private static List<String> array(Object value) {
        List<String> out = new ArrayList<>();
        try {
            if (value instanceof java.sql.Array a) {
                for (Object o : (Object[]) a.getArray()) {
                    if (o != null) {
                        out.add(o.toString());
                    }
                }
            } else if (value instanceof Object[] arr) {
                for (Object o : arr) {
                    if (o != null) {
                        out.add(o.toString());
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            return out;
        }
        return out;
    }

    // ------------------------------------------------------------------------- run

    /** Runs the queued cues (the scheduled fallback, and tests). */
    public int runQueued() {
        int n = 0;
        for (ComparisonStore.Cue c : store.queued(20)) {
            run(c.id());
            n++;
        }
        return n;
    }

    /** Runs one cue: hands its queries to the adapters, fetches each adapter once and records what they found. */
    @SuppressWarnings("unchecked")
    public void run(String cueId) {
        ComparisonStore.Cue cue = store.cue(cueId).orElse(null);
        if (cue == null || !CorrelationVocabulary.CUE_QUEUED.equals(cue.status())) {
            return;
        }
        Instant started = clock.instant();
        store.updateCue(copy(cue, CorrelationVocabulary.CUE_RUNNING, null, List.of(), List.of(), BigDecimal.ZERO,
                started, null, null));
        boolean viaXai = Boolean.TRUE.equals(cue.input() == null ? null : cue.input().get("viaXai"));
        int maxResults = cue.input() != null && cue.input().get("maxResults") instanceof Number m ? m.intValue()
                : DEFAULT_MAX_RESULTS;
        Map<String, BigDecimal> extraCost = new LinkedHashMap<>();
        Map<String, String> extraError = new LinkedHashMap<>();
        for (Object o : cue.queries()) {
            if (!(o instanceof Map<?, ?> q)) {
                continue;
            }
            String platform = String.valueOf(q.get("platform"));
            String query = String.valueOf(q.get("query"));
            String language = q.get("language") == null ? null : q.get("language").toString();
            BigDecimal estimate = q.get("estimatedCostUsd") instanceof Number e ? BigDecimal.valueOf(e.doubleValue())
                    : BigDecimal.ZERO;
            if ("x".equals(platform) && viaXai) {
                XaiXSearchBackend.Found found = xai.search(query, cue.windowStart(), cue.windowEnd(), maxResults);
                extraCost.merge(platform, found.costUsd(), BigDecimal::add);
                if (found.error() != null) {
                    extraError.putIfAbsent(platform, found.error());
                }
                requests.submit(new CueRequests.Request(cue.id(), platform, query, language, cue.windowStart(),
                        cue.windowEnd(), maxResults, BigDecimal.ZERO, found.posts()));
            } else {
                requests.submit(new CueRequests.Request(cue.id(), platform, query, language, cue.windowStart(),
                        cue.windowEnd(), maxResults, "x".equals(platform) ? estimate : null, List.of()));
            }
        }
        FetchExecutor executorBean = fetches.getIfAvailable();
        for (String platform : cue.platforms()) {
            if (executorBean == null) {
                extraError.putIfAbsent(platform, "no fetch executor");
                continue;
            }
            try {
                executorBean.execute(platform, FetchTrigger.MANUAL);
            } catch (RuntimeException e) {
                extraError.putIfAbsent(platform, e.getMessage() == null ? e.toString() : e.getMessage());
            }
            if (requests.waiting(platform)) {
                extraError.putIfAbsent(platform, "the adapter was busy; its cue queries run on its next fetch");
            }
        }
        List<CueRequests.Result> results = requests.take(cue.id());
        List<Object> perPlatform = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int failed = 0;
        int budgetRefused = 0;
        for (String platform : cue.platforms()) {
            int posts = 0;
            BigDecimal cost = extraCost.getOrDefault(platform, BigDecimal.ZERO);
            String error = extraError.get(platform);
            boolean any = false;
            for (CueRequests.Result r : results) {
                if (!platform.equals(r.sourceId())) {
                    continue;
                }
                any = true;
                posts += r.posts();
                cost = cost.add(r.costUsd() == null ? BigDecimal.ZERO : r.costUsd());
                if (r.error() != null && error == null) {
                    error = r.error();
                }
            }
            if (!any && error == null) {
                error = "no result reported";
            }
            if (error != null && posts == 0) {
                failed++;
                if (error.startsWith("BUDGET_EXCEEDED")) {
                    budgetRefused++;
                }
            }
            total = total.add(cost);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("platform", platform);
            m.put("posts", posts);
            m.put("costUsd", cost.doubleValue());
            m.put("error", error);
            perPlatform.add(m);
        }
        List<EdpSearchService.Result> external = searchProviders(cue, maxResults);
        boolean externalFound = external.stream().anyMatch(r -> r.error() == null || r.items() > 0);
        List<Object> externalResults = new ArrayList<>(external.stream().map(EdpSearchService.Result::toMap).toList());
        String status = failed < cue.platforms().size() || externalFound ? CorrelationVocabulary.CUE_DONE
                : budgetRefused == cue.platforms().size() ? CorrelationVocabulary.CUE_BUDGET_REFUSED
                : CorrelationVocabulary.CUE_FAILED;
        Instant done = clock.instant();
        String manifestId = manifest(cue);
        ComparisonStore.Cue finished = copy(cue, status, CorrelationVocabulary.CUE_DONE.equals(status) ? null
                : "every platform failed", perPlatform, store.narrativesOfCue(cue.id()), total, started, done,
                manifestId, externalResults);
        store.updateCue(finished);
        events.emit(CorrelationVocabulary.EVENT_CUE, cueEvent(finished), false);
    }

    /** Refreshes the narratives of cues completed in the last day (their posts join narratives after ingestion). */
    public int refreshNarratives() {
        int n = 0;
        for (ComparisonStore.Cue c : store.completedSince(clock.instant().minus(NARRATIVE_REFRESH), 200)) {
            List<String> ids = store.narrativesOfCue(c.id());
            if (!ids.equals(c.narrativeIds())) {
                store.updateCue(copy(c, c.status(), c.statusReason(), c.results(), ids, c.costUsd(), c.startedAt(),
                        c.completedAt(), c.manifestId()));
                n++;
            }
        }
        return n;
    }

    private String manifest(ComparisonStore.Cue cue) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("queries", cue.queries());
            config.put("platforms", cue.platforms());
            config.put("window", Map.of("start", cue.windowStart().toString(), "end", cue.windowEnd() == null ? ""
                    : cue.windowEnd().toString()));
            config.put("backend", cue.backend());
            return service.record(ManifestRequest.of(CorrelationVocabulary.CUE_COMPONENT,
                    CorrelationVocabulary.CUE_VERSION).config(config).inputs(cue.triggerRef() == null ? List.of()
                    : List.of(cue.triggerRef())));
        } catch (RuntimeException e) {
            log.debug("Could not record the manifest of cue {}: {}", cue.id(), e.toString());
            return null;
        }
    }

    private static ComparisonStore.Cue copy(ComparisonStore.Cue c, String status, String reason, List<Object> results,
            List<String> narrativeIds, BigDecimal cost, Instant started, Instant completed, String manifestId) {
        return copy(c, status, reason, results, narrativeIds, cost, started, completed, manifestId,
                c.externalResults());
    }

    private static ComparisonStore.Cue copy(ComparisonStore.Cue c, String status, String reason, List<Object> results,
            List<String> narrativeIds, BigDecimal cost, Instant started, Instant completed, String manifestId,
            List<Object> externalResults) {
        return new ComparisonStore.Cue(c.id(), c.triggerType(), c.triggerRef(), c.backend(), c.queries(), c.platforms(),
                c.windowStart(), c.windowEnd(), status, reason, results, narrativeIds, cost, c.maxCostUsd(), c.input(),
                c.createdBy(), c.createdAt(), started, completed, manifestId, externalResults);
    }

    /**
     * Searches the EDP providers that declare {@code transport.search} with the cue's distinct queries
     * ({@code edpProviderSearch}, EDP §4.3); their records are written as cue results.
     */
    @SuppressWarnings("unchecked")
    private List<EdpSearchService.Result> searchProviders(ComparisonStore.Cue cue, int maxResults) {
        EdpSearchService search = edpSearch.getIfAvailable();
        if (search == null) {
            return List.of();
        }
        Map<String, EdpSearchService.Query> distinct = new LinkedHashMap<>();
        for (Object o : cue.queries()) {
            if (o instanceof Map<?, ?> q && q.get("query") != null) {
                String text = q.get("query").toString();
                String language = q.get("language") == null ? null : q.get("language").toString();
                distinct.putIfAbsent(text + "|" + language, new EdpSearchService.Query(text, language));
            }
        }
        if (distinct.isEmpty()) {
            return List.of();
        }
        List<Double> bbox = null;
        if (cue.input() != null && cue.input().get("bbox") instanceof List<?> b && b.size() == 4) {
            bbox = ((List<Object>) b).stream().map(v -> ((Number) v).doubleValue()).toList();
        }
        try {
            return search.search(new EdpSearchService.Search(cue.id(), List.copyOf(distinct.values()),
                    cue.windowStart(), cue.windowEnd(), bbox, maxResults));
        } catch (RuntimeException e) {
            log.warn("EDP search for cue {} failed: {}", cue.id(), e.toString());
            return List.of();
        }
    }

    /** The {@code SocialCueEvent} body. */
    public static Map<String, Object> cueEvent(ComparisonStore.Cue cue) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", CorrelationVocabulary.EVENT_CUE);
        body.put("cue", CorrelationViews.cue(cue));
        return body;
    }
}
