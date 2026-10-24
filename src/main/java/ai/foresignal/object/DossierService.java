package ai.foresignal.object;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.baserate.analog.AnalogService;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastQuestionRepository;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.forecast.language.EstimativeLanguage;
import ai.foresignal.impact.ImpactViews;
import ai.foresignal.situation.SituationLogEntity;
import ai.foresignal.situation.SituationLogRepository;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The object dossier ({@code getObjectDossier}, prompt 129, docs/Client-Contract.md §3.1): the envelope plus bounded,
 * independently rendered sections so a "selected object" panel never fans out requests. Each section carries
 * {@code status} ({@code ok | empty | unavailable | not-installed}); with {@code facets}, evidence lists carry counts
 * by evidence class and by source. The whole response stays within {@value #MAX_BYTES} bytes by trimming the longest
 * lists first (and setting {@code truncated}).
 *
 * <p>Every probability is labeled with its issuer: {@code official} for upstream products (NHC track and cone),
 * {@code foresignal-ensemble} or a forecaster id for ledger forecasts, with the ICD 203 term and knowledge cutoff.
 */
@Service
public class DossierService {

    /** Largest dossier, in bytes. */
    public static final int MAX_BYTES = 65_536;

    /** The sections, in display order. */
    public static final List<String> SECTIONS = List.of("overview", "imagery", "forecast", "impacts", "related",
            "timeline", "analysis");

    /** Most imagery items. */
    static final int MAX_IMAGERY = 12;

    /** Most timeline items. */
    static final int MAX_TIMELINE = 50;

    /** Most items per related group. */
    static final int MAX_RELATED = 20;

    /** Nearby radius, km. */
    static final double NEARBY_KM = 100;

    private static final Logger log = LoggerFactory.getLogger(DossierService.class);
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};
    private static final Set<String> INFRA_TYPES = Set.of(ObjectTypes.DEPENDS_ON);

    private final ObjectService objects;
    private final RelationService relations;
    private final RecordQueryService records;
    private final SourceRegistry registry;
    private final ForecastQuestionRepository questions;
    private final ForecastRepository forecasts;
    private final AnalyticFindingRepository findings;
    private final SituationLogRepository situationLog;
    private final ObjectProvider<AnalogService> analogs;
    private final AnnotationRepository annotations;
    private final EvidenceClasses evidence;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public DossierService(ObjectService objects, RelationService relations, RecordQueryService records,
            SourceRegistry registry, ForecastQuestionRepository questions, ForecastRepository forecasts,
            AnalyticFindingRepository findings, SituationLogRepository situationLog,
            ObjectProvider<AnalogService> analogs, AnnotationRepository annotations, EvidenceClasses evidence,
            JdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.objects = objects;
        this.relations = relations;
        this.records = records;
        this.registry = registry;
        this.questions = questions;
        this.forecasts = forecasts;
        this.findings = findings;
        this.situationLog = situationLog;
        this.analogs = analogs;
        this.annotations = annotations;
        this.evidence = evidence;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /** The {@code include} values: the sections plus {@code coverage} (prompt 133, the top-level member). */
    public static final List<String> INCLUDES = List.of("overview", "imagery", "forecast", "impacts", "related",
            "timeline", "analysis", "coverage");

    private ai.foresignal.news.NewsCoverageService newsCoverage;
    private ai.foresignal.news.NewsStoryStore newsStories;

    /** Installs the news coverage (prompt 133: the dossier's {@code coverage} member). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setNewsCoverage(ai.foresignal.news.NewsCoverageService newsCoverage,
            ai.foresignal.news.NewsStoryStore newsStories) {
        this.newsCoverage = newsCoverage;
        this.newsStories = newsStories;
    }

    /** The requested sections of an {@code include} list (all when empty). */
    public static List<String> includes(List<String> include) {
        if (include == null || include.isEmpty()) {
            return SECTIONS;
        }
        List<String> out = new ArrayList<>();
        for (String s : include) {
            if (!INCLUDES.contains(s)) {
                throw ValidationException.forField("include", "'" + s + "' is not one of " + INCLUDES);
            }
            if (!out.contains(s)) {
                out.add(s);
            }
        }
        return out;
    }

    /**
     * The dossier of {@code ref}.
     *
     * @param ref the object
     * @param include the sections
     * @param facets true to add evidence facets
     * @param context the caller and {@code asOf}
     * @return the {@code Dossier} view
     */
    public Map<String, Object> dossier(ObjectRef ref, List<String> include, boolean facets, ResolveContext context) {
        ObjectEnvelope envelope = objects.get(ref, context);
        Map<String, Object> sections = new LinkedHashMap<>();
        for (String section : includes(include)) {
            if (!"coverage".equals(section)) {
                sections.put(section, section(section, envelope, facets, context));
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", envelope.ref());
        out.put("envelope", envelope);
        out.put("asOf", context.asOf());
        out.put("generatedAt", clock.instant());
        out.put("sections", sections);
        out.put("truncated", false);
        out.put("coverage", include == null || include.isEmpty() || include.contains("coverage")
                ? coverage(envelope.ref()) : null);
        cap(out);
        return out;
    }

    /**
     * The dossier's coverage member (prompt 133): a situation's or news story's coverage statistics with
     * {@code status} {@code ok}, {@code unavailable} when it has no news coverage; null for other types.
     */
    Map<String, Object> coverage(String ref) {
        ObjectRef parsed = ObjectRef.parse(ref);
        if (!ObjectTypes.SITUATION.equals(parsed.type()) && !ObjectTypes.NEWS_STORY.equals(parsed.type())) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, Object> stats = null;
        try {
            if (ObjectTypes.SITUATION.equals(parsed.type()) && newsCoverage != null) {
                stats = newsCoverage.situationCoverage(parsed.id());
            } else if (newsStories != null) {
                stats = newsStories.story(parsed.id()).map(ai.foresignal.news.NewsStoryStore.Story::coverage)
                        .orElse(null);
            }
        } catch (RuntimeException e) {
            log.debug("No news coverage for {}: {}", ref, e.toString());
        }
        out.put("status", stats == null || stats.isEmpty() ? "unavailable" : "ok");
        out.put("stats", stats == null || stats.isEmpty() ? null : stats);
        return out;
    }

    private Map<String, Object> section(String name, ObjectEnvelope e, boolean facets, ResolveContext context) {
        try {
            return switch (name) {
                case "overview" -> overview(e, context);
                case "imagery" -> imagery(e, facets, context);
                case "forecast" -> forecast(e, context);
                case "impacts" -> impacts(e, context);
                case "related" -> related(e, facets, context);
                case "timeline" -> timeline(e, facets, context);
                case "analysis" -> analysis(e, context);
                default -> status("unavailable", "unknown section");
            };
        } catch (RuntimeException ex) {
            log.warn("Dossier section {} of {} failed: {}", name, e.ref(), ex.toString());
            return status("unavailable", "This section could not be computed: " + ex.getClass().getSimpleName());
        }
    }

    // ------------------------------------------------------------------------- overview

    Map<String, Object> overview(ObjectEnvelope e, ResolveContext context) {
        Map<String, Object> fields = new LinkedHashMap<>();
        ObjectRef ref = e.objectRef();
        if (ref.isRecord()) {
            Optional<RecordRow> row = records.get(ref.recordSourceId(), ref.recordExternalId(), false,
                    FieldSelection.ALL, GeometryOptions.DEFAULT, context.caller().excludedSources());
            if (row.isPresent()) {
                Map<String, Object> payload = row.get().payload() == null ? Map.of()
                        : json.readValue(row.get().payload(), MAP);
                Map<String, FieldSpec> specs = registry.find(ref.recordSourceId())
                        .map(r -> r.descriptor().fieldSpecs()).orElse(Map.of());
                fields.put("kind", row.get().kind());
                fields.put("version", row.get().version());
                for (Map.Entry<String, FieldSpec> spec : specs.entrySet()) {
                    if (spec.getValue().appliesTo(row.get().kind()) && payload.containsKey(spec.getKey())) {
                        fields.put(spec.getKey(), payload.get(spec.getKey()));
                    }
                }
                if (specs.isEmpty()) {
                    payload.entrySet().stream().limit(20).forEach(x -> fields.put(x.getKey(), x.getValue()));
                }
            }
        } else {
            fields.put("type", e.type());
            fields.put("status", e.status());
            fields.put("severity", e.severity());
            fields.putAll(typeFields(ref, context));
        }
        Map<String, Object> out = status(fields.isEmpty() ? "empty" : "ok", null);
        out.put("fields", fields);
        out.put("links", e.links().get("self") == null ? null : Map.of("full", e.links().get("self")));
        return out;
    }

    private Map<String, Object> typeFields(ObjectRef ref, ResolveContext context) {
        Map<String, Object> out = new LinkedHashMap<>();
        switch (ref.type()) {
            case ObjectTypes.FINDING -> findings.findById(ref.id()).ifPresent(f -> {
                out.put("findingType", f.getFindingType());
                out.put("detector", f.getDetector());
                out.put("signalFamily", f.getSignalFamily());
                out.put("regionCodes", f.getRegionCodes());
                out.put("baseRate", f.getBaseRate());
            });
            case ObjectTypes.QUESTION -> questions.findById(ref.id()).ifPresent(q -> {
                out.put("questionType", q.getType());
                out.put("closesAt", q.getClosesAt());
                out.put("resolveBy", q.getResolveBy());
                out.put("resolutionCriteria", q.getResolutionCriteria());
                out.put("ensembleProbability", q.getEnsembleProbability());
            });
            case ObjectTypes.SITUATION -> jdbc.query("SELECT theme, member_count, to_jsonb(region_codes)::text,"
                    + " to_jsonb(signal_families)::text FROM situation WHERE id = ?", rs -> {
                        out.put("theme", rs.getString(1));
                        out.put("memberCount", rs.getInt(2));
                        out.put("regionCodes", readList(rs.getString(3)));
                        out.put("signalFamilies", readList(rs.getString(4)));
                    }, ref.id());
            case ObjectTypes.INTEL_EVENT -> jdbc.query("SELECT type, confidence, confidence_label, member_count"
                    + " FROM intelligence_event WHERE id = ?", rs -> {
                        out.put("eventType", rs.getString(1));
                        out.put("confidence", rs.getObject(2));
                        out.put("confidenceLabel", rs.getString(3));
                        out.put("memberCount", rs.getObject(4));
                    }, ref.id());
            default -> {
                // The envelope carries the rest.
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- imagery

    Map<String, Object> imagery(ObjectEnvelope e, boolean facets, ResolveContext context) {
        if (e.bbox() == null) {
            return status("empty", "The object has no geometry.");
        }
        Envelope area = new Envelope(e.bbox().get(0), e.bbox().get(2), e.bbox().get(1), e.bbox().get(3));
        if (area.getWidth() < 0.05) {
            area.expandBy(0.05);
        }
        Instant end = e.time() == null || e.time().end() == null ? clock.instant() : e.time().end();
        Instant start = e.time() == null || e.time().start() == null ? end.minus(Duration.ofDays(7))
                : e.time().start().minus(Duration.ofDays(1));
        CurrentQuery q = new CurrentQuery(null, List.of("raster-frame", "satellite-scene"), null,
                new BboxFilter(List.of(area)), null, start, end, TimeField.EFFECTIVE, null, true, ListOrder.OBSERVED,
                null, null, MAX_IMAGERY, FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false,
                context.caller().excludedSources());
        List<Map<String, Object>> items = new ArrayList<>();
        List<String[]> facetItems = new ArrayList<>();
        for (RecordRow row : records.list(q).rows()) {
            Map<String, Object> payload = row.payload() == null ? Map.of() : json.readValue(row.payload(), MAP);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("ref", ObjectRef.record(row.sourceId(), row.externalId(), row.seq()).format());
            boolean scene = "satellite-scene".equals(row.kind());
            item.put("kind", scene ? "stac-scene" : row.sourceId().startsWith("satdump-") ? "satdump-product"
                    : "raster-frame");
            item.put("title", row.title() == null ? row.externalId() : row.title());
            item.put("time", row.observedAt());
            item.put("sourceId", row.sourceId());
            if (scene) {
                item.put("frameUrl", text(payload.get("stacItemUrl")));
                item.put("thumbnailUrl", text(payload.get("thumbnailUrl")));
            } else {
                Object productId = payload.getOrDefault("productId", row.externalId());
                item.put("frameUrl", payload.get("tiles") instanceof String t ? t
                        : "/v1/products/" + productId + "/frames");
                item.put("thumbnailUrl", payload.get("frameKey") == null ? null
                        : "/v1/products/" + productId + "/image/" + payload.get("frameKey") + ".png");
            }
            item.put("bbox", row.bbox() == null ? null : List.of(row.bbox()[0], row.bbox()[1], row.bbox()[2],
                    row.bbox()[3]));
            items.add(item);
            facetItems.add(new String[] {row.sourceId(), row.kind()});
        }
        Map<String, Object> out = status(items.isEmpty() ? "empty" : "ok", null);
        out.put("items", items);
        out.put("links", Map.of("full", "/v1/records?kind=raster-frame,satellite-scene&bbox=" + area.getMinX() + ","
                + area.getMinY() + "," + area.getMaxX() + "," + area.getMaxY()));
        if (facets) {
            out.put("facets", evidence.facets(facetItems));
        }
        return out;
    }

    // ------------------------------------------------------------------------- forecast

    Map<String, Object> forecast(ObjectEnvelope e, ResolveContext context) {
        List<Map<String, Object>> official = officialForecasts(e, context);
        List<Map<String, Object>> briefs = new ArrayList<>();
        for (ForecastQuestionEntity q : questionsAbout(e, context)) {
            briefs.add(brief(q));
        }
        Map<String, Object> out = status(official.isEmpty() && briefs.isEmpty() ? "empty" : "ok", null);
        out.put("official", official);
        out.put("questions", briefs);
        out.put("links", Map.of("full", "/v1/forecasts/questions?linked=" + e.ref()));
        return out;
    }

    /** Official upstream forecast products about the object (NHC track and cone for a storm), issuer official. */
    List<Map<String, Object>> officialForecasts(ObjectEnvelope e, ResolveContext context) {
        List<Map<String, Object>> out = new ArrayList<>();
        Set<String> storms = new LinkedHashSet<>();
        ObjectRef ref = e.objectRef();
        if (ref.isRecord() && ref.recordSourceId().startsWith("nhc-")) {
            String external = ref.recordExternalId();
            storms.add(external.contains(":") ? external.substring(0, external.indexOf(':')) : external);
        } else if (ObjectTypes.SITUATION.equals(ref.type())) {
            jdbc.query("SELECT ref FROM situation_member WHERE situation_id = ? AND member_type = 'record'"
                    + " AND ref LIKE 'nhc-%' LIMIT 20", rs -> {
                        String r = rs.getString(1);
                        String external = r.substring(r.indexOf('/') + 1);
                        storms.add(external.contains(":") ? external.substring(0, external.indexOf(':')) : external);
                    }, ref.id());
        }
        for (String storm : storms) {
            for (String[] product : List.of(new String[] {storm, "cone"}, new String[] {storm + ":track", "track"})) {
                records.get("nhc-cyclone-cones", product[0], false, FieldSelection.ALL, GeometryOptions.DEFAULT,
                        context.caller().excludedSources()).filter(r -> !r.tombstone()).ifPresent(r -> {
                            Map<String, Object> payload = r.payload() == null ? Map.of()
                                    : json.readValue(r.payload(), MAP);
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("ref", ObjectRef.record(r.sourceId(), r.externalId(), null).format());
                            item.put("title", (payload.get("stormName") == null ? storm : payload.get("stormName"))
                                    + " " + product[1]);
                            item.put("kind", product[1]);
                            item.put("issuer", "official");
                            item.put("issuedAt", r.observedAt());
                            item.put("validFrom", r.validFrom() != null ? r.validFrom() : r.observedAt());
                            item.put("validTo", r.validUntil());
                            item.put("note", "NHC advisory " + (payload.get("advisoryNumber") == null ? ""
                                    : payload.get("advisoryNumber")) + "; official product, not a Foresignal"
                                    + " forecast");
                            out.add(item);
                        });
            }
        }
        return out;
    }

    /** Ledger questions about the object: linked columns, then {@code about} relations. */
    List<ForecastQuestionEntity> questionsAbout(ObjectEnvelope e, ResolveContext context) {
        ObjectRef ref = e.objectRef();
        Set<String> ids = new LinkedHashSet<>();
        String column = switch (ref.type()) {
            case ObjectTypes.SITUATION -> "situation_id";
            case ObjectTypes.HYPOTHESIS_SET -> "hypothesis_set_id";
            case ObjectTypes.PRECURSOR -> "precursor_id";
            case ObjectTypes.FINDING -> "finding_id";
            default -> null;
        };
        if (column != null) {
            ids.addAll(jdbc.queryForList("SELECT id FROM forecast_question WHERE " + column + " = ?"
                    + " ORDER BY id DESC LIMIT 20", String.class, ref.id()));
        }
        if (ObjectTypes.QUESTION.equals(ref.type())) {
            ids.add(ref.id());
        }
        for (RelationService.Related r : relations.neighbors(e, Set.of(ObjectTypes.ABOUT), "in", 0, 40, context)) {
            if (ObjectTypes.QUESTION.equals(r.object().type())) {
                ids.add(r.object().objectRef().id());
            }
        }
        List<ForecastQuestionEntity> out = new ArrayList<>();
        for (String id : ids) {
            questions.findById(id).filter(q -> !q.isPrivateQuestion() || context.seesPrivate()).ifPresent(out::add);
            if (out.size() >= 20) {
                break;
            }
        }
        return out;
    }

    /** The {@code LedgerQuestionBrief} of a question. */
    Map<String, Object> brief(ForecastQuestionEntity q) {
        Map<String, ForecastEntity> latest = new LinkedHashMap<>();
        for (ForecastEntity f : forecasts.findByQuestionIdOrderByMadeAtAscIdAsc(q.getId())) {
            latest.put(f.getForecasterId(), f);
        }
        ForecastEntity ensemble = latest.get("ensemble");
        Double p = q.getEnsembleProbability() != null ? q.getEnsembleProbability()
                : ensemble == null ? null : ensemble.getProbability();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", q.getId());
        out.put("ref", ObjectTypes.QUESTION + ":" + q.getId());
        out.put("title", q.getTitle());
        out.put("type", q.getType());
        out.put("status", q.getStatus());
        out.put("closesAt", q.getClosesAt());
        out.put("resolveBy", q.getResolveBy());
        out.put("resolutionCriteria", q.getResolutionCriteria());
        out.put("probability", p);
        out.put("estimativeTerm", EstimativeLanguage.termOrNull(p));
        out.put("distribution", ensemble == null ? null : ensemble.getDistribution());
        out.put("quantiles", ensemble == null ? null : ensemble.getQuantiles());
        out.put("issuer", "foresignal-ensemble");
        List<Map<String, Object>> forecasters = new ArrayList<>();
        for (ForecastEntity f : latest.values()) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("forecasterId", "ensemble".equals(f.getForecasterId()) ? "foresignal-ensemble" : f.getForecasterId());
            v.put("probability", f.getProbability());
            v.put("madeAt", f.getMadeAt());
            v.put("knowledgeCutoff", f.getKnowledgeCutoff());
            forecasters.add(v);
        }
        out.put("forecasters", forecasters);
        return out;
    }

    // ------------------------------------------------------------------------- impacts

    Map<String, Object> impacts(ObjectEnvelope e, ResolveContext context) {
        ObjectRef ref = e.objectRef();
        List<String> ids = new ArrayList<>();
        if (ObjectTypes.SITUATION.equals(ref.type())) {
            ids.addAll(jdbc.queryForList("SELECT id FROM analytic_finding WHERE finding_type = 'projected-impact'"
                    + " AND status = 'open' AND situation_id = ? ORDER BY id DESC LIMIT 20", String.class, ref.id()));
        } else if (ref.isRecord()) {
            String prefix = ref.withoutSeq().format();
            ids.addAll(jdbc.queryForList("SELECT id FROM analytic_finding WHERE finding_type = 'projected-impact'"
                    + " AND status = 'open' AND (stats->>'sourceRef' = ? OR stats->>'sourceRef' LIKE ?)"
                    + " ORDER BY id DESC LIMIT 20", String.class, prefix, prefix + "@%"));
        } else if (ObjectTypes.FINDING.equals(ref.type())) {
            ids.add(ref.id());
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (String id : ids) {
            findings.findById(id).filter(f -> "projected-impact".equals(f.getFindingType()))
                    .filter(f -> !f.isPrivateFinding() || context.seesPrivate())
                    .ifPresent(f -> items.add(impact(f)));
        }
        Map<String, Object> out = status(items.isEmpty() ? "empty" : "ok", null);
        out.put("items", items);
        out.put("links", Map.of("full", "/v1/analytics/impacts"));
        return out;
    }

    private Map<String, Object> impact(AnalyticFindingEntity f) {
        Map<String, Object> view = ImpactViews.projected(f, false);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("findingId", f.getId());
        out.put("impactFamily", view.get("impactFamily"));
        out.put("admin1", view.get("admin1"));
        out.put("pImpact", view.get("pImpact"));
        out.put("interval", view.get("interval"));
        Object paths = view.get("dependencyPaths");
        out.put("dependencyPaths", paths instanceof Number n ? n.intValue() : paths instanceof List<?> l ? l.size()
                : 0);
        out.put("labels", view.get("labels") instanceof List<?> l ? l : List.of());
        out.put("questionId", view.get("questionId"));
        return out;
    }

    // ------------------------------------------------------------------------- related

    Map<String, Object> related(ObjectEnvelope e, boolean facets, ResolveContext context) {
        List<Map<String, Object>> nearby = nearby(e, context);
        List<Map<String, Object>> operational = new ArrayList<>();
        List<Map<String, Object>> infrastructure = new ArrayList<>();
        for (RelationService.Related r : relations.neighbors(e, Set.of(), "both", 0, 200, context)) {
            Map<String, Object> view = RelationService.relatedView(r);
            String type = String.valueOf(r.relation().get("type"));
            if (INFRA_TYPES.contains(type)) {
                add(infrastructure, view);
            } else if (!ObjectTypes.LOCATED_IN.equals(type)) {
                add(operational, view);
            }
        }
        for (Map.Entry<String, String> f : relations.facilitiesNear(e).entrySet()) {
            ObjectRef facility = RelationService.parseQuietly(f.getKey());
            if (facility == null) {
                continue;
            }
            objects.find(facility, context).ifPresent(env -> add(infrastructure, RelationService.relatedView(
                    new RelationService.Related(RelationService.computed(e.ref(), env.ref(), ObjectTypes.NEAR,
                            f.getValue(), 0.5, null), "out", env, RelationService.distanceKm(e, env)))));
        }
        List<Map<String, Object>> historical = historical(e, context);
        Map<String, Object> out = status(nearby.isEmpty() && operational.isEmpty() && infrastructure.isEmpty()
                && historical.isEmpty() ? "empty" : "ok", null);
        out.put("nearby", nearby);
        out.put("operational", operational);
        out.put("infrastructure", infrastructure);
        out.put("historical", historical);
        out.put("truncated", nearby.size() >= MAX_RELATED || operational.size() >= MAX_RELATED
                || infrastructure.size() >= MAX_RELATED);
        out.put("links", Map.of("full", "/v1/objects/" + e.ref() + "/related"));
        if (facets) {
            List<String[]> items = new ArrayList<>();
            for (List<Map<String, Object>> group : List.of(nearby, operational, infrastructure)) {
                for (Map<String, Object> item : group) {
                    ObjectEnvelope env = (ObjectEnvelope) item.get("object");
                    ObjectRef r = env.objectRef();
                    items.add(new String[] {r.isRecord() ? r.recordSourceId()
                            : env.provenance().sourceIds().isEmpty() ? null : env.provenance().sourceIds().get(0),
                            r.isRecord() ? null : r.type()});
                }
            }
            out.put("facets", evidence.facets(items));
        }
        return out;
    }

    private static void add(List<Map<String, Object>> list, Map<String, Object> item) {
        if (list.size() < MAX_RELATED) {
            list.add(item);
        }
    }

    /** Open situations, intel events and findings within {@value #NEARBY_KM} km, nearest first. */
    List<Map<String, Object>> nearby(ObjectEnvelope e, ResolveContext context) {
        if (e.bbox() == null) {
            return List.of();
        }
        double lon = (e.bbox().get(0) + e.bbox().get(2)) / 2;
        double lat = (e.bbox().get(1) + e.bbox().get(3)) / 2;
        String point = "ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography";
        Map<String, Double> distances = new LinkedHashMap<>();
        List<Object[]> queries = List.of(
                new Object[] {ObjectTypes.SITUATION, "SELECT id, ST_Distance(geom::geography, " + point + ") / 1000"
                        + " FROM situation WHERE geom IS NOT NULL AND status <> 'closed' AND merged_into IS NULL"
                        + " AND ST_DWithin(geom::geography, " + point + ", ?) ORDER BY 2 LIMIT 10"},
                new Object[] {ObjectTypes.INTEL_EVENT, "SELECT id, ST_Distance(geom::geography, " + point + ") / 1000"
                        + " FROM intelligence_event WHERE geom IS NOT NULL AND status IN ('emerging', 'active')"
                        + " AND ST_DWithin(geom::geography, " + point + ", ?) ORDER BY 2 LIMIT 10"},
                new Object[] {ObjectTypes.FINDING, "SELECT id, ST_Distance(geom::geography, " + point + ") / 1000"
                        + " FROM analytic_finding WHERE geom IS NOT NULL AND status = 'open'"
                        + " AND ST_DWithin(geom::geography, " + point + ", ?) ORDER BY 2 LIMIT 10"});
        for (Object[] q : queries) {
            jdbc.query((String) q[1], rs -> {
                String ref = q[0] + ":" + rs.getString(1);
                if (!ref.equals(e.ref())) {
                    distances.put(ref, Math.round(rs.getDouble(2) * 100) / 100.0);
                }
            }, lon, lat, lon, lat, NEARBY_KM * 1000);
        }
        List<ObjectRef> refs = distances.keySet().stream().map(RelationService::parseQuietly)
                .filter(java.util.Objects::nonNull).toList();
        Map<ObjectRef, ObjectEnvelope> envelopes = objects.resolveRefs(refs, context);
        List<Map<String, Object>> out = new ArrayList<>();
        distances.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(d -> {
            ObjectRef r = RelationService.parseQuietly(d.getKey());
            ObjectEnvelope env = r == null ? null : envelopes.get(r);
            if (env != null && out.size() < MAX_RELATED) {
                out.add(RelationService.relatedView(new RelationService.Related(RelationService.computed(e.ref(),
                        env.ref(), ObjectTypes.NEAR, "confirmed", 1.0, null), "out", env, d.getValue())));
            }
        });
        return out;
    }

    /** 121 historical analogs of a situation. */
    List<Map<String, Object>> historical(ObjectEnvelope e, ResolveContext context) {
        AnalogService service = analogs.getIfAvailable();
        if (service == null || !ObjectTypes.SITUATION.equals(e.type())) {
            return List.of();
        }
        Map<String, Object> result = service.forSituation(e.objectRef().id(), 5, null, context.seesPrivate());
        List<Map<String, Object>> out = new ArrayList<>();
        if (result.get("analogs") instanceof List<?> list) {
            for (Object o : list) {
                if (!(o instanceof Map<?, ?> a) || a.get("situationId") == null) {
                    continue;
                }
                ObjectRef analog = ObjectRef.of(ObjectTypes.SITUATION, String.valueOf(a.get("situationId")));
                Optional<ObjectEnvelope> env = objects.find(analog, context);
                double similarity = a.get("similarity") instanceof Number n ? n.doubleValue() : 0.5;
                env.ifPresent(x -> out.add(RelationService.relatedView(new RelationService.Related(
                        RelationService.computed(e.ref(), x.ref(), ObjectTypes.RELATED_TO,
                                ObjectTypes.labelFor(similarity), Math.max(0, Math.min(1, similarity)), null),
                        "out", x, RelationService.distanceKm(e, x)))));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- timeline

    Map<String, Object> timeline(ObjectEnvelope e, boolean facets, ResolveContext context) {
        ObjectRef ref = e.objectRef();
        List<Map<String, Object>> items = new ArrayList<>();
        List<String[]> facetItems = new ArrayList<>();
        if (ref.isRecord()) {
            records.versions(ref.recordSourceId(), ref.recordExternalId(), null, null, null, 20, false,
                    context.caller().excludedSources()).ifPresent(slice -> {
                        for (RecordRow v : slice.rows()) {
                            items.add(item(v.observedAt(), "version", ObjectRef.record(v.sourceId(), v.externalId(),
                                    v.seq()).format(), "Version " + v.version(), null));
                            facetItems.add(new String[] {v.sourceId(), v.kind()});
                        }
                    });
            jdbc.query("SELECT id, fired_at, severity, message FROM alert WHERE source_id = ? AND external_id = ?"
                    + " AND NOT exercise ORDER BY fired_at DESC LIMIT 20", rs -> {
                        items.add(item(rs.getTimestamp(2).toInstant(), "alert", "alert:" + rs.getLong(1),
                                rs.getString(4) == null ? "Alert" : rs.getString(4), rs.getString(3)));
                    }, ref.recordSourceId(), ref.recordExternalId());
        }
        if (ObjectTypes.SITUATION.equals(ref.type())) {
            for (SituationLogEntity l : situationLog.findBySituationIdOrderByIdAsc(ref.id())) {
                items.add(item(l.getAt(), "situation-log", null, l.getAction() + (l.getActor() == null ? ""
                        : " by " + l.getActor()), null));
            }
        }
        for (RelationService.Related r : relations.neighbors(e, Set.of(ObjectTypes.DERIVED_FROM), "in", 0, 20,
                context)) {
            if (ObjectTypes.FINDING.equals(r.object().type())) {
                items.add(item(r.object().time().observedAt() != null ? r.object().time().observedAt()
                        : r.object().time().start(), "finding", r.object().ref(), r.object().title(),
                        r.object().severity()));
            }
        }
        for (AnnotationEntity a : annotations.findByTargetRefAndDeletedAtIsNullOrderByIdDesc(
                ref.withoutSeq().format(), org.springframework.data.domain.Limit.of(20))) {
            if (!a.isPrivateAnnotation() || context.seesPrivate()) {
                items.add(item(a.getCreatedAt(), "annotation", ObjectTypes.ANNOTATION + ":" + a.getId(),
                        "verdict".equals(a.getKind()) ? "Verdict: " + a.getVerdict() : "Note", null));
            }
        }
        items.removeIf(i -> i.get("at") == null || (context.asOf() != null
                && ((Instant) i.get("at")).isAfter(context.asOf())));
        items.sort(Comparator.comparing((Map<String, Object> i) -> (Instant) i.get("at")).reversed());
        boolean truncated = items.size() > MAX_TIMELINE;
        Map<String, Object> out = status(items.isEmpty() ? "empty" : "ok", null);
        out.put("items", truncated ? new ArrayList<>(items.subList(0, MAX_TIMELINE)) : items);
        out.put("truncated", truncated);
        out.put("links", e.links().get("self") == null ? null : Map.of("full", e.links().get("self")));
        if (facets) {
            out.put("facets", evidence.facets(facetItems));
        }
        return out;
    }

    private static Map<String, Object> item(Instant at, String kind, String ref, String title, String severity) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("at", at);
        out.put("kind", kind);
        out.put("ref", ref);
        out.put("title", title);
        out.put("severity", severity);
        return out;
    }

    // ------------------------------------------------------------------------- analysis

    Map<String, Object> analysis(ObjectEnvelope e, ResolveContext context) {
        Set<ObjectRef> findingRefs = new LinkedHashSet<>();
        Set<ObjectRef> setRefs = new LinkedHashSet<>();
        Set<ObjectRef> assessmentRefs = new LinkedHashSet<>();
        for (RelationService.Related r : relations.neighbors(e, Set.of(ObjectTypes.DERIVED_FROM, ObjectTypes.ABOUT,
                ObjectTypes.EVIDENCE_FOR, ObjectTypes.EVIDENCE_AGAINST), "both", 0, 100, context)) {
            ObjectRef other = r.object().objectRef();
            switch (other.type()) {
                case ObjectTypes.FINDING -> findingRefs.add(other);
                case ObjectTypes.HYPOTHESIS_SET -> setRefs.add(other);
                case ObjectTypes.HYPOTHESIS -> jdbc.queryForList("SELECT set_id FROM hypothesis WHERE id = ?",
                        String.class, other.id()).forEach(id -> setRefs.add(ObjectRef.of(ObjectTypes.HYPOTHESIS_SET,
                                id)));
                default -> {
                    // not analysis
                }
            }
        }
        ObjectRef ref = e.objectRef();
        if (ObjectTypes.SITUATION.equals(ref.type())) {
            jdbc.queryForList("SELECT id FROM hypothesis_set WHERE situation_id = ? ORDER BY id DESC LIMIT 10",
                    String.class, ref.id()).forEach(id -> setRefs.add(ObjectRef.of(ObjectTypes.HYPOTHESIS_SET, id)));
            jdbc.queryForList("SELECT id FROM assessment WHERE situation_id = ? ORDER BY id DESC LIMIT 10",
                    String.class, ref.id()).forEach(id -> assessmentRefs.add(ObjectRef.of(ObjectTypes.ASSESSMENT,
                            id)));
            jdbc.queryForList("SELECT id FROM analytic_finding WHERE situation_id = ? ORDER BY id DESC LIMIT 10",
                    String.class, ref.id()).forEach(id -> findingRefs.add(ObjectRef.of(ObjectTypes.FINDING, id)));
        }
        jdbc.queryForList("SELECT id FROM assessment WHERE jsonb_exists(citations::jsonb, ?) ORDER BY id DESC"
                + " LIMIT 10", String.class, ref.withoutSeq().format()).forEach(id ->
                        assessmentRefs.add(ObjectRef.of(ObjectTypes.ASSESSMENT, id)));
        Map<String, Object> out = new LinkedHashMap<>();
        List<ObjectEnvelope> f = envelopes(findingRefs, context);
        List<ObjectEnvelope> s = envelopes(setRefs, context);
        List<ObjectEnvelope> a = envelopes(assessmentRefs, context);
        out.putAll(status(f.isEmpty() && s.isEmpty() && a.isEmpty() ? "empty" : "ok", null));
        out.put("findings", f);
        out.put("hypothesisSets", s);
        out.put("assessments", a);
        return out;
    }

    private List<ObjectEnvelope> envelopes(Set<ObjectRef> refs, ResolveContext context) {
        Map<ObjectRef, ObjectEnvelope> found = objects.resolveRefs(refs, context);
        List<ObjectEnvelope> out = new ArrayList<>();
        for (ObjectRef r : refs) {
            if (found.containsKey(r) && out.size() < MAX_RELATED) {
                out.add(found.get(r));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    static Map<String, Object> status(String status, String message) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", status);
        out.put("message", message);
        out.put("links", null);
        out.put("facets", null);
        out.put("truncated", false);
        return out;
    }

    /** Trims the longest lists until the dossier fits in {@value #MAX_BYTES} bytes. */
    void cap(Map<String, Object> dossier) {
        for (int round = 0; round < 40 && size(dossier) > MAX_BYTES; round++) {
            List<Object> longest = null;
            Map<String, Object> owner = null;
            @SuppressWarnings("unchecked")
            Map<String, Object> sections = (Map<String, Object>) dossier.get("sections");
            for (Object sectionValue : sections.values()) {
                if (!(sectionValue instanceof Map<?, ?> section)) {
                    continue;
                }
                for (Object value : section.values()) {
                    if (value instanceof List<?> list && !list.isEmpty()
                            && (longest == null || list.size() > longest.size())) {
                        @SuppressWarnings("unchecked")
                        List<Object> l = (List<Object>) list;
                        longest = l;
                        @SuppressWarnings("unchecked")
                        Map<String, Object> m = (Map<String, Object>) section;
                        owner = m;
                    }
                }
            }
            if (longest == null) {
                break;
            }
            int keep = longest.size() / 2;
            List<Object> trimmed = new ArrayList<>(longest.subList(0, keep));
            for (Map.Entry<String, Object> entry : owner.entrySet()) {
                if (entry.getValue() == longest) {
                    entry.setValue(trimmed);
                }
            }
            owner.put("truncated", true);
            dossier.put("truncated", true);
        }
    }

    private int size(Object value) {
        return json.writeValueAsString(value).getBytes(StandardCharsets.UTF_8).length;
    }

    /** The ETag of a dossier: its content without {@code generatedAt}. */
    public String etag(Map<String, Object> dossier) {
        Map<String, Object> copy = new LinkedHashMap<>(dossier);
        copy.remove("generatedAt");
        return ai.foresignal.api.support.ETags.forDocument(json.writeValueAsString(copy));
    }

    private List<String> readList(String text) {
        if (text == null) {
            return List.of();
        }
        return json.readValue(text, new TypeReference<List<String>>() {});
    }

    private static String text(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
