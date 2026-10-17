package ai.foresignal.object;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastQuestionRepository;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.forecast.language.EstimativeLanguage;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.Slices;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.locationtech.jts.geom.Envelope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * The context summary ({@code summarizeContext}, {@code getWorkspaceSummary}; prompt 129): for a viewport, a time
 * window and filters, the layer counts (current head ⋈ live, aggregated, never feature lists), a merged activity feed
 * tagged by signal family (alerts, findings, situation changes), insights (findings, ledger forecasts with issuer,
 * ICD 203 term, base rate and knowledge cutoff, and hypothesis band changes) and the top related situations, in at
 * most {@value #MAX_BYTES} bytes, cached per normalized context for {@value #CACHE_SECONDS} seconds.
 *
 * <p>Deterministic: no LLM text is produced here ({@code narrative} is null). Layer counts describe the current state;
 * {@code asOf} bounds the activity and insights.
 */
@Service
public class ContextSummaryService {

    /** Largest summary, in bytes. */
    public static final int MAX_BYTES = 32_768;

    /** Cache lifetime, seconds. */
    public static final int CACHE_SECONDS = 30;

    /** Most activity items. */
    static final int MAX_ACTIVITY = 50;

    /** Most insights. */
    static final int MAX_INSIGHTS = 15;

    /** Most related objects. */
    static final int MAX_RELATED = 10;

    private static final int MAX_CACHE = 500;
    private static final List<String> SEVERITY_ORDER = List.of("critical", "high", "warning", "medium", "info", "low");

    private final RecordQueryService records;
    private final ObjectService objects;
    private final ForecastQuestionRepository questions;
    private final ForecastRepository forecasts;
    private final EvidenceClasses evidence;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    record Cached(Instant at, Map<String, Object> summary) {}

    /** Creates the service. */
    public ContextSummaryService(RecordQueryService records, ObjectService objects,
            ForecastQuestionRepository questions, ForecastRepository forecasts, EvidenceClasses evidence,
            JdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.records = records;
        this.objects = objects;
        this.questions = questions;
        this.forecasts = forecasts;
        this.evidence = evidence;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /** A parsed context. */
    record Context(JsonNode request, Envelope area, Instant from, Instant to, Instant asOf, List<String> layers,
            Set<String> kinds, String minSeverity) {}

    /**
     * Summarizes a {@code ContextSummaryRequest} (or a workspace context with a viewport and time range).
     *
     * @param request the request
     * @param context the caller
     * @return the {@code ContextSummary} view
     */
    public Map<String, Object> summarize(JsonNode request, ResolveContext context) {
        Context c = parse(request);
        String key = cacheKey(c, context.caller());
        Instant now = clock.instant();
        Cached hit = cache.get(key);
        if (hit != null && Duration.between(hit.at(), now).getSeconds() < CACHE_SECONDS) {
            Map<String, Object> out = new LinkedHashMap<>(hit.summary());
            out.put("cacheAgeSeconds", Duration.between(hit.at(), now).getSeconds());
            return out;
        }
        Map<String, Object> summary = compute(c, context);
        if (cache.size() >= MAX_CACHE) {
            cache.entrySet().removeIf(e -> Duration.between(e.getValue().at(), now).getSeconds() >= CACHE_SECONDS);
            if (cache.size() >= MAX_CACHE) {
                cache.clear();
            }
        }
        cache.put(key, new Cached(now, summary));
        Map<String, Object> out = new LinkedHashMap<>(summary);
        out.put("cacheAgeSeconds", 0L);
        return out;
    }

    /** Drops every cached summary (tests). */
    public void clearCache() {
        cache.clear();
    }

    /** The request of a workspace context, or null when it has no viewport or time range. */
    public static JsonNode requestOf(Map<String, Object> workspaceContext, JsonMapper json) {
        if (workspaceContext.get("viewport") == null || workspaceContext.get("timeRange") == null) {
            return null;
        }
        ObjectNode out = JsonNodeFactory.instance.objectNode();
        out.set("viewport", json.valueToTree(workspaceContext.get("viewport")));
        out.set("timeRange", json.valueToTree(workspaceContext.get("timeRange")));
        if (workspaceContext.get("asOf") != null) {
            out.set("asOf", json.valueToTree(workspaceContext.get("asOf")));
        }
        if (workspaceContext.get("filters") != null) {
            out.set("filters", json.valueToTree(workspaceContext.get("filters")));
        }
        if (workspaceContext.get("layers") != null) {
            out.set("layers", json.valueToTree(workspaceContext.get("layers")));
        }
        return out;
    }

    Context parse(JsonNode request) {
        if (request == null || !request.isObject()) {
            throw new ValidationException("The body must be a ContextSummaryRequest object.");
        }
        for (String field : request.propertyNames()) {
            if (!Set.of("viewport", "timeRange", "asOf", "filters", "layers").contains(field)) {
                throw ValidationException.forField(field, "is not a ContextSummaryRequest member");
            }
        }
        if (!request.hasNonNull("viewport")) {
            throw ValidationException.forField("viewport", "is required");
        }
        if (!request.hasNonNull("timeRange")) {
            throw ValidationException.forField("timeRange", "is required");
        }
        WorkspaceContextValidator.validate(request, "request");
        JsonNode v = request.get("viewport");
        Envelope area;
        if (v.hasNonNull("bbox")) {
            JsonNode b = v.get("bbox");
            area = new Envelope(b.get(0).asDouble(), b.get(2).asDouble(), b.get(1).asDouble(), b.get(3).asDouble());
        } else {
            double lon = v.get("center").get(0).asDouble();
            double lat = v.get("center").get(1).asDouble();
            double r = v.get("radiusKm").asDouble();
            double dLat = r / 111.32;
            double dLon = r / Math.max(1e-6, 111.32 * Math.cos(Math.toRadians(lat)));
            area = new Envelope(Math.max(-180, lon - dLon), Math.min(180, lon + dLon), Math.max(-90, lat - dLat),
                    Math.min(90, lat + dLat));
        }
        Instant from = WorkspaceContextValidator.parse(request.get("timeRange").get("start"));
        JsonNode endNode = request.get("timeRange").get("end");
        Instant asOf = WorkspaceContextValidator.parse(request.get("asOf"));
        Instant to = endNode == null || endNode.isNull() ? (asOf != null ? asOf : clock.instant())
                : WorkspaceContextValidator.parse(endNode);
        if (asOf != null && to.isAfter(asOf)) {
            to = asOf;
        }
        List<String> layers = new ArrayList<>();
        if (request.hasNonNull("layers")) {
            request.get("layers").forEach(l -> layers.add(l.asString()));
        }
        Set<String> kinds = new LinkedHashSet<>();
        String minSeverity = null;
        JsonNode filters = request.get("filters");
        if (filters != null && filters.isObject()) {
            JsonNode k = filters.get("kinds");
            if (k != null && k.isArray()) {
                k.forEach(x -> kinds.add(x.asString()));
            }
            if (filters.hasNonNull("minSeverity")) {
                minSeverity = filters.get("minSeverity").asString();
            }
        }
        return new Context(request, area, from, to, asOf, layers, kinds, minSeverity);
    }

    private String cacheKey(Context c, ObjectCaller caller) {
        return json.writeValueAsString(c.request()) + "|" + caller.seesPrivate() + "|"
                + new TreeSet<>(caller.excludedSources()) + "|" + (caller.anonymous() ? "anon" : "token");
    }

    Map<String, Object> compute(Context c, ResolveContext context) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("context", json.convertValue(c.request(), Map.class));
        out.put("generatedAt", clock.instant());
        out.put("cacheAgeSeconds", 0L);
        out.put("layerCounts", layerCounts(c, context));
        out.put("activity", activity(c, context));
        out.put("insights", insights(c, context));
        out.put("related", related(c, context));
        out.put("narrative", null);
        out.put("truncated", false);
        cap(out);
        return out;
    }

    // ------------------------------------------------------------------------- layer counts

    List<Map<String, Object>> layerCounts(Context c, ResolveContext context) {
        List<Slices.AreaCount> rows = records.countsIn(new BboxFilter(List.of(c.area())),
                context.caller().excludedSources());
        Map<String, long[]> byKind = new LinkedHashMap<>();
        Map<String, long[]> bySource = new LinkedHashMap<>();
        for (Slices.AreaCount r : rows) {
            if (!c.kinds().isEmpty() && !c.kinds().contains(r.kind())) {
                continue;
            }
            add(byKind, r.kind(), r);
            add(bySource, r.sourceId(), r);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        if (c.layers().isEmpty()) {
            byKind.forEach((k, v) -> out.add(count(k, v[0], v[1])));
        } else {
            for (String layer : c.layers()) {
                long[] v = byKind.containsKey(layer) ? byKind.get(layer) : bySource.get(layer);
                if (v != null) {
                    out.add(count(layer, v[0], v[1]));
                } else if (!layer.startsWith("observability:")) {
                    out.add(count(layer, 0, 0));
                }
            }
        }
        return out;
    }

    private static void add(Map<String, long[]> map, String key, Slices.AreaCount r) {
        long[] v = map.computeIfAbsent(key, k -> new long[2]);
        v[0] += r.count();
        v[1] += r.live();
    }

    private static Map<String, Object> count(String layer, long count, long live) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("layer", layer);
        out.put("count", count);
        out.put("liveCount", live);
        return out;
    }

    // ------------------------------------------------------------------------- activity

    List<Map<String, Object>> activity(Context c, ResolveContext context) {
        List<Map<String, Object>> items = new ArrayList<>();
        Envelope a = c.area();
        Object[] window = {Timestamp.from(c.from()), Timestamp.from(c.to())};
        String env = "ST_MakeEnvelope(?, ?, ?, ?, 4326)";
        Object[] box = {a.getMinX(), a.getMinY(), a.getMaxX(), a.getMaxY()};
        Set<String> excluded = context.caller().excludedSources();
        jdbc.query("SELECT al.id, al.fired_at, al.severity, al.message, al.source_id, al.kind FROM alert al"
                + " JOIN record_live l ON l.source_id = al.source_id AND l.external_id = al.external_id"
                + " WHERE al.fired_at >= ? AND al.fired_at <= ? AND NOT al.exercise AND NOT al.suppressed"
                + " AND l.geom && " + env + " ORDER BY al.fired_at DESC LIMIT 100", rs -> {
                    String source = rs.getString(5);
                    if (excluded.contains(source) || !severityOk(rs.getString(3), c.minSeverity())) {
                        return;
                    }
                    items.add(activityItem(rs.getTimestamp(2).toInstant(), "alert:" + rs.getLong(1), "alert",
                            rs.getString(4) == null ? "Alert" : rs.getString(4), rs.getString(3),
                            evidence.family(source, rs.getString(6))));
                }, concat(window, box));
        jdbc.query("SELECT id, detected_at, severity, title, signal_family, private FROM analytic_finding"
                + " WHERE detected_at >= ? AND detected_at <= ? AND status = 'open' AND geom && " + env
                + " ORDER BY detected_at DESC LIMIT 100", rs -> {
                    if ((rs.getBoolean(6) && !context.seesPrivate()) || !severityOk(rs.getString(3), c.minSeverity())) {
                        return;
                    }
                    items.add(activityItem(rs.getTimestamp(2).toInstant(), "finding:" + rs.getString(1), "finding",
                            rs.getString(4), rs.getString(3), rs.getString(5)));
                }, concat(window, box));
        jdbc.query("SELECT s.id, l.at, l.action, s.title, s.severity, s.private, s.signal_families::text"
                + " FROM situation_log l JOIN situation s ON s.id = l.situation_id WHERE l.at >= ? AND l.at <= ?"
                + " AND s.geom && " + env + " ORDER BY l.at DESC LIMIT 100", rs -> {
                    if (rs.getBoolean(6) && !context.seesPrivate()) {
                        return;
                    }
                    List<String> families = rs.getString(7) == null ? List.of()
                            : json.readValue(rs.getString(7), new tools.jackson.core.type.TypeReference<List<String>>() {
                            });
                    items.add(activityItem(rs.getTimestamp(2).toInstant(), "situation:" + rs.getString(1),
                            "situation-change", rs.getString(4) + ": " + rs.getString(3), rs.getString(5),
                            families.isEmpty() ? null : families.get(0)));
                }, concat(window, box));
        items.sort(Comparator.comparing((Map<String, Object> i) -> (Instant) i.get("at")).reversed()
                .thenComparing(i -> String.valueOf(i.get("ref"))));
        return items.size() > MAX_ACTIVITY ? new ArrayList<>(items.subList(0, MAX_ACTIVITY)) : items;
    }

    private static Map<String, Object> activityItem(Instant at, String ref, String type, String title,
            String severity, String family) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("at", at);
        out.put("ref", ref);
        out.put("type", type);
        out.put("title", title);
        out.put("severity", severity);
        out.put("signalFamily", family);
        return out;
    }

    /** True when {@code severity} is at or above {@code min} (unknown severities pass). */
    static boolean severityOk(String severity, String min) {
        if (min == null || severity == null) {
            return true;
        }
        int s = SEVERITY_ORDER.indexOf(severity);
        int m = SEVERITY_ORDER.indexOf(min);
        return s < 0 || m < 0 || s <= m;
    }

    // ------------------------------------------------------------------------- insights

    List<Map<String, Object>> insights(Context c, ResolveContext context) {
        List<Map<String, Object>> out = new ArrayList<>();
        Envelope a = c.area();
        String env = "ST_MakeEnvelope(?, ?, ?, ?, 4326)";
        Object[] box = {a.getMinX(), a.getMinY(), a.getMaxX(), a.getMaxY()};
        Object[] window = {Timestamp.from(c.from()), Timestamp.from(c.to())};
        jdbc.query("SELECT id, title, detected_at, base_rate::text, private FROM analytic_finding WHERE status = 'open'"
                + " AND detected_at >= ? AND detected_at <= ? AND geom && " + env
                + " ORDER BY CASE severity WHEN 'critical' THEN 0 WHEN 'warning' THEN 1 ELSE 2 END, detected_at DESC"
                + " LIMIT 5", rs -> {
                    if (rs.getBoolean(5) && !context.seesPrivate()) {
                        return;
                    }
                    Map<String, Object> i = insight("finding:" + rs.getString(1), "finding", rs.getString(2));
                    i.put("baseRate", baseRateValue(rs.getString(4)));
                    i.put("at", rs.getTimestamp(3).toInstant());
                    out.add(i);
                }, concat(window, box));
        Set<String> situations = new LinkedHashSet<>(jdbc.queryForList("SELECT id FROM situation WHERE status <>"
                + " 'closed' AND merged_into IS NULL AND geom && " + env + " ORDER BY last_activity_at DESC LIMIT 50",
                String.class, box));
        if (!situations.isEmpty()) {
            String in = String.join(", ", java.util.Collections.nCopies(situations.size(), "?"));
            List<String> qids = jdbc.queryForList("SELECT id FROM forecast_question WHERE status = 'open' AND"
                    + " situation_id IN (" + in + ") ORDER BY resolve_by NULLS LAST LIMIT 5", String.class,
                    situations.toArray());
            for (String id : qids) {
                questions.findById(id).filter(q -> !q.isPrivateQuestion() || context.seesPrivate())
                        .ifPresent(q -> out.add(forecastInsight(q)));
            }
            jdbc.query("SELECT r.set_id, r.at, hs.question, hs.private FROM hypothesis_revision r JOIN hypothesis_set hs"
                    + " ON hs.id = r.set_id WHERE r.band_changed AND r.at >= ? AND r.at <= ? AND hs.situation_id IN ("
                    + in + ")"
                    + " ORDER BY r.at DESC LIMIT 5", rs -> {
                        if (rs.getBoolean(4) && !context.seesPrivate()) {
                            return;
                        }
                        Map<String, Object> i = insight("hypothesis-set:" + rs.getString(1), "band-change",
                                "Band change: " + rs.getString(3));
                        i.put("at", rs.getTimestamp(2).toInstant());
                        out.add(i);
                    }, concat(window, situations.toArray()));
        }
        return out.size() > MAX_INSIGHTS ? new ArrayList<>(out.subList(0, MAX_INSIGHTS)) : out;
    }

    private Map<String, Object> forecastInsight(ForecastQuestionEntity q) {
        ForecastEntity ensemble = null;
        ForecastEntity baseRate = null;
        for (ForecastEntity f : forecasts.findByQuestionIdOrderByMadeAtAscIdAsc(q.getId())) {
            if ("ensemble".equals(f.getForecasterId())) {
                ensemble = f;
            } else if ("base-rate".equals(f.getForecasterId())) {
                baseRate = f;
            }
        }
        Double p = q.getEnsembleProbability() != null ? q.getEnsembleProbability()
                : ensemble == null ? null : ensemble.getProbability();
        Map<String, Object> i = insight("question:" + q.getId(), "forecast", q.getTitle());
        i.put("probability", p);
        i.put("estimativeTerm", EstimativeLanguage.termOrNull(p));
        i.put("issuer", "foresignal-ensemble");
        i.put("baseRate", baseRate == null ? null : baseRate.getProbability());
        i.put("knowledgeCutoff", ensemble == null ? null : ensemble.getKnowledgeCutoff());
        i.put("at", ensemble == null ? q.getCreatedAt() : ensemble.getMadeAt());
        return i;
    }

    private static Map<String, Object> insight(String ref, String kind, String title) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", ref);
        out.put("kind", kind);
        out.put("title", title);
        out.put("probability", null);
        out.put("estimativeTerm", null);
        out.put("issuer", null);
        out.put("baseRate", null);
        out.put("knowledgeCutoff", null);
        out.put("at", null);
        out.put("textOrigin", "deterministic");
        return out;
    }

    private Double baseRateValue(String text) {
        if (text == null) {
            return null;
        }
        Map<?, ?> m = json.readValue(text, Map.class);
        for (String key : List.of("pAtLeastOne", "value", "rate")) {
            if (m.get(key) instanceof Number n) {
                return n.doubleValue();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------- related

    List<ObjectEnvelope> related(Context c, ResolveContext context) {
        Envelope a = c.area();
        List<String> ids = jdbc.queryForList("SELECT id FROM situation WHERE status <> 'closed' AND merged_into IS NULL"
                + " AND geom && ST_MakeEnvelope(?, ?, ?, ?, 4326) ORDER BY CASE severity WHEN 'critical' THEN 0"
                + " WHEN 'warning' THEN 1 ELSE 2 END, member_count DESC, last_activity_at DESC LIMIT 25", String.class,
                a.getMinX(), a.getMinY(), a.getMaxX(), a.getMaxY());
        List<ObjectRef> refs = ids.stream().map(id -> ObjectRef.of(ObjectTypes.SITUATION, id)).toList();
        Map<ObjectRef, ObjectEnvelope> found = objects.resolveRefs(refs, new ResolveContext(context.caller(),
                c.asOf()));
        List<ObjectEnvelope> out = new ArrayList<>();
        for (ObjectRef r : refs) {
            if (found.containsKey(r) && out.size() < MAX_RELATED) {
                out.add(found.get(r));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- size

    @SuppressWarnings("unchecked")
    void cap(Map<String, Object> summary) {
        for (String key : List.of("activity", "insights", "related", "layerCounts")) {
            while (size(summary) > MAX_BYTES && summary.get(key) instanceof List<?> list && !list.isEmpty()) {
                summary.put(key, new ArrayList<>(((List<Object>) list).subList(0, list.size() / 2)));
                summary.put("truncated", true);
            }
        }
    }

    private int size(Object value) {
        return json.writeValueAsString(value).getBytes(StandardCharsets.UTF_8).length;
    }

    private static Object[] concat(Object[] a, Object[] b) {
        Object[] out = new Object[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
