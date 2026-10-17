package ai.foresignal.impact;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingSeverities;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.impact.hazard.GaugeCalibration;
import ai.foresignal.impact.hazard.HazardAdapters;
import ai.foresignal.impact.hazard.HazardForecast;
import ai.foresignal.impact.hazard.HazardReader;
import ai.foresignal.impact.hazard.HazardRecord;
import ai.foresignal.impact.rate.ImpactRateService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Runs the impact projection for one hazard (prompt 127 §4) and writes its outputs.
 *
 * <ol>
 *   <li><b>Hazard:</b> the current records behind the hazard key become a {@link HazardForecast}
 *       ({@link HazardAdapters}; a gauge is calibrated by its own history first).
 *   <li><b>Exposure:</b> the admin1 regions, facilities with dependency paths, places and the 101 exposure summary of
 *       the hazard's area ({@link ImpactExposure}).
 *   <li><b>Projection:</b> {@link ImpactProjector} with the conditional rates of {@link ImpactRateService}.
 *   <li><b>Findings:</b> one {@code projected-impact} finding per (hazard, impact family, admin1) with
 *       {@code stats {pHazard, pConditional, pImpact, interval, n}}, the exposed assets, the dependency paths, the
 *       inputs, the skipped pairs and the label; each stores its 116 manifest. A finding of the hazard that the run no
 *       longer produces is superseded (by the new finding of its family and region when there is one).
 *       {@code impact.projected} and {@code impact.superseded} go to the {@code analytics} topic.
 *   <li><b>Ledger:</b> a projection at or above {@code impact.questionMinProbability} opens a binary question through
 *       {@link ForecastService#openQuestion}: "Will {family} be reported in {admin1} between {validFrom} and
 *       {validTo + lag}?", an {@code event-exists} resolver over the family's trigger rule ({@link ImpactRules}) in the
 *       region, category {@value #CATEGORY}, deduplicated per hazard issuance, family and region; the {@value #FORECASTER}
 *       forecaster records {@code pImpact}. The 122 baselines add theirs when the question opens.
 * </ol>
 */
@Service
public class ImpactProjectionService {

    private static final Logger log = LoggerFactory.getLogger(ImpactProjectionService.class);

    /** The finding type. */
    public static final String FINDING_TYPE = "projected-impact";

    /** The ledger forecaster id and type. */
    public static final String FORECASTER = "impact";

    /** The ledger category. */
    public static final String CATEGORY = "weather-impact";

    /** The days of gauge history read for a calibration. */
    public static final int GAUGE_HISTORY_DAYS = 365;

    /** The most assets and paths stored per finding. */
    static final int MAX_LISTED = 50;

    /** The simplification tolerance of hazard geometries stored with a finding, in degrees. */
    static final double STORED_TOLERANCE = 0.02;

    private static final DateTimeFormatter TITLE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm'Z'",
            Locale.ROOT).withZone(ZoneOffset.UTC);

    private final HazardReader reader;
    private final ImpactExposure exposure;
    private final ImpactPairs pairs;
    private final ImpactRateService rates;
    private final ImpactRules rules;
    private final FindingService findings;
    private final AnalyticFindingRepository findingRepository;
    private final AppSettingService settings;
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectProvider<ForecastService> ledger;
    private final ObjectProvider<ForecasterRegistry> forecasters;
    private final ObjectProvider<IntelVisibility> visibility;
    private final Clock clock;
    private final GeoJsonWriter geoJson = new GeoJsonWriter();

    /** Creates the service. */
    public ImpactProjectionService(HazardReader reader, ImpactExposure exposure, ImpactPairs pairs,
            ImpactRateService rates, ImpactRules rules, FindingService findings,
            AnalyticFindingRepository findingRepository, AppSettingService settings, NamedParameterJdbcTemplate jdbc,
            ObjectProvider<ForecastService> ledger, ObjectProvider<ForecasterRegistry> forecasters,
            ObjectProvider<IntelVisibility> visibility, Clock clock) {
        this.reader = reader;
        this.exposure = exposure;
        this.pairs = pairs;
        this.rates = rates;
        this.rules = rules;
        this.findings = findings;
        this.findingRepository = findingRepository;
        this.settings = settings;
        this.jdbc = jdbc;
        this.ledger = ledger;
        this.forecasters = forecasters;
        this.visibility = visibility;
        this.clock = clock;
    }

    /**
     * What one run did.
     *
     * @param hazardKey the hazard
     * @param forecast the hazard forecast, or null when its records are gone
     * @param projected findings created or superseding an earlier one
     * @param unchanged findings identical to an open one
     * @param superseded findings superseded
     * @param questions ledger questions opened
     * @param skipped the pairs skipped
     */
    public record RunResult(String hazardKey, HazardForecast forecast, List<String> projected, int unchanged,
            List<String> superseded, List<String> questions, List<ImpactProjector.Skipped> skipped) {}

    /** The projector's settings from the app settings, else the defaults. */
    public ImpactProjector.Settings settings() {
        ImpactProjector.Settings d = ImpactProjector.Settings.DEFAULTS;
        return new ImpactProjector.Settings(number(SettingKeys.IMPACT_MIN_PROBABILITY, d.minProbability()),
                number(SettingKeys.IMPACT_QUESTION_MIN_PROBABILITY, d.questionMinProbability()),
                number(SettingKeys.IMPACT_ASSET_BUFFER_KM, d.assetBufferKm()));
    }

    /** The hazard keys with open projections. */
    public Set<String> activeHazards() {
        return new LinkedHashSet<>(jdbc.queryForList("SELECT DISTINCT stats ->> 'hazardKey' FROM analytic_finding"
                + " WHERE finding_type = :type AND status = 'open' AND stats ->> 'hazardKey' IS NOT NULL",
                new MapSqlParameterSource("type", FINDING_TYPE), String.class));
    }

    /**
     * The hazard forecast behind a key, from its current records.
     *
     * @param hazardKey the key
     * @return the forecast, or empty when the records are gone or feed no hazard
     */
    public Optional<HazardForecast> forecast(String hazardKey) {
        List<HazardRecord> records = reader.current(hazardKey);
        if (records.isEmpty()) {
            return Optional.empty();
        }
        String source = HazardAdapters.sourceOf(hazardKey);
        String rest = hazardKey.length() > source.length() ? hazardKey.substring(source.length() + 1) : "";
        Instant now = clock.instant();
        try {
            return switch (source) {
                case HazardAdapters.NHC_WIND_PROBS -> HazardAdapters.windProbabilities(Integer.parseInt(rest),
                        records);
                case HazardAdapters.SPC_OUTLOOKS -> {
                    String[] parts = rest.split(":");
                    yield HazardAdapters.spc(Integer.parseInt(parts[0].substring(3)), parts[1], records);
                }
                case HazardAdapters.NWPS_GAUGES -> {
                    HazardRecord gauge = records.get(records.size() - 1);
                    ImpactPairs.HazardClass hazardClass = pairs.hazardClass(HazardForecast.RIVER_FLOOD);
                    String min = hazardClass == null || hazardClass.minCategory() == null ? "moderate"
                            : hazardClass.minCategory();
                    String basis = HazardAdapters.gaugeBasis(gauge, now);
                    GaugeCalibration.Result calibration = GaugeCalibration.calibrate(reader.gaugeHistory(
                            gauge.externalId(), now.minus(Duration.ofDays(GAUGE_HISTORY_DAYS))), basis, min, now);
                    yield Optional.of(HazardAdapters.gauge(gauge, calibration, min, settings().assetBufferKm(), now));
                }
                case HazardAdapters.NHC_CONES -> Optional.of(HazardAdapters.cone(records.get(records.size() - 1)));
                case HazardAdapters.STORM_TRACKS -> Optional.of(HazardAdapters.stormCell(records.get(
                        records.size() - 1)));
                default -> HazardAdapters.WPC_ERO.contains(source) ? HazardAdapters.ero(source, records)
                        : Optional.empty();
            };
        } catch (RuntimeException e) {
            log.warn("Hazard {} could not be adapted: {}", hazardKey, e.toString());
            return Optional.empty();
        }
    }

    /**
     * Projects one hazard and writes its findings, events and ledger questions.
     *
     * @param hazardKey the hazard key
     */
    public synchronized RunResult run(String hazardKey) {
        Instant now = clock.instant();
        String actor = ImpactProjector.NAME + ":" + ImpactProjector.VERSION;
        List<AnalyticFindingEntity> before = openFindings(hazardKey);
        HazardForecast forecast = forecast(hazardKey).orElse(null);
        ImpactProjector.Settings s = settings();
        List<String> projected = new ArrayList<>();
        List<String> superseded = new ArrayList<>();
        List<String> questions = new ArrayList<>();
        int unchanged = 0;
        List<ImpactProjector.Skipped> skipped = List.of();
        Map<String, String> successors = new LinkedHashMap<>();
        Set<String> produced = new LinkedHashSet<>();
        if (forecast != null && forecast.probabilistic()) {
            Geometry area = forecast.geometry();
            List<ImpactExposure.Admin1> regions = exposure.admin1(area);
            Geometry assetArea = HazardForecast.MODEL_POLYGONS.equals(forecast.probabilityModel()) && area != null
                    ? area.buffer(s.assetBufferKm() / 111.32) : area;
            ImpactExposure.Infrastructure infra = exposure.infrastructure(assetArea);
            List<ImpactExposure.Asset> assets = new ArrayList<>(infra.facilities());
            assets.addAll(exposure.places(assetArea));
            ImpactProjector.Result result = ImpactProjector.project(forecast, regions, assets, infra.paths(),
                    pairs.pairsFor(forecast.hazardType()), pairs, rates::regionClassOf, rates::rate, s);
            skipped = result.skipped();
            boolean privateHazard = privateHazard(forecast);
            List<Map<String, Object>> context = context(forecast);
            for (ImpactProjector.Projection p : result.projections()) {
                FindingDraft draft = draft(forecast, p, result, infra, context, privateHazard, now);
                FindingService.Outcome outcome = findings.record(draft);
                String id = outcome.finding().getId();
                produced.add(id);
                successors.put(p.family().id() + "|" + p.admin1().code(), id);
                if (outcome.change() == FindingService.Change.UNCHANGED) {
                    unchanged++;
                } else {
                    projected.add(id);
                    findings.announce(id, FindingEvent.IMPACT_PROJECTED, actor, null);
                }
                if (p.pImpact() >= s.questionMinProbability()) {
                    String q = question(forecast, p, outcome.finding(), privateHazard, now);
                    if (q != null) {
                        questions.add(q);
                    }
                }
            }
        }
        for (AnalyticFindingEntity old : before) {
            if (produced.contains(old.getId())) {
                continue;
            }
            AnalyticFindingEntity current = findingRepository.findById(old.getId()).orElse(null);
            if (current == null) {
                continue;
            }
            boolean done = "superseded".equals(current.getStatus());
            if (!done && "open".equals(current.getStatus())) {
                Object family = current.getStats().get("impactFamily");
                Object admin1 = current.getStats().get("admin1");
                String successor = successors.get(family + "|" + admin1);
                done = findings.supersede(current.getId(), successor, actor, successor == null
                        ? "the next run of hazard " + hazardKey + " no longer projects this impact"
                        : "superseded by " + successor);
            }
            if (done && !"superseded".equals(old.getStatus())) {
                superseded.add(current.getId());
                findings.announce(current.getId(), FindingEvent.IMPACT_SUPERSEDED, actor, null);
            }
        }
        return new RunResult(hazardKey, forecast, projected, unchanged, superseded, questions, skipped);
    }

    // ------------------------------------------------------------------------- findings

    private FindingDraft draft(HazardForecast forecast, ImpactProjector.Projection p, ImpactProjector.Result result,
            ImpactExposure.Infrastructure infra, List<Map<String, Object>> context, boolean privateHazard,
            Instant now) {
        Instant windowEnd = questionEnd(forecast, p);
        String family = p.family().label();
        String title = label(forecast.hazardType()) + ": " + percent(p.pImpact()) + " chance of " + family
                + " reported in " + p.admin1().name() + " by " + TITLE_TIME.format(windowEnd);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("hazardKey", forecast.hazardKey());
        stats.put("hazardType", forecast.hazardType());
        stats.put("hazardClass", p.pair().hazardClass());
        stats.put("sourceRef", forecast.sourceRef());
        stats.put("impactFamily", p.family().id());
        stats.put("admin1", p.admin1().code());
        stats.put("admin1Name", p.admin1().name());
        stats.put("regionClass", p.regionClass());
        stats.put("pHazard", p.pHazard());
        stats.put("pHazardInterval", interval(p.pHazardLower(), p.pHazardUpper()));
        stats.put("band", p.band());
        stats.put("pConditional", p.rate().rate());
        stats.put("pConditionalInterval", interval(p.rate().lower(), p.rate().upper()));
        stats.put("pImpact", p.pImpact());
        stats.put("interval", interval(p.lower(), p.upper()));
        stats.put("n", p.rate().n());
        stats.put("nImpacts", p.rate().impacts());
        stats.put("lagHours", p.pair().lagHours());
        stats.put("questionWindow", Map.of("start", forecast.validFrom().toString(), "end", windowEnd.toString()));
        stats.put("hazard", forecast.view(true, g -> geoJson.write(TopologyPreservingSimplifier.simplify(g,
                STORED_TOLERANCE))));
        stats.put("conditionalRate", p.rate().view());
        stats.put("exposedAssets", assets(p));
        stats.put("dependencyPaths", paths(p));
        stats.put("inputs", inputs(forecast, p, context));
        stats.put("skipped", result.skipped().stream().map(ImpactProjector.Skipped::view).toList());
        stats.put("labels", List.of(ImpactProjector.LABEL));
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(FindingCaveats.of(FindingCaveats.MODELED, ImpactProjector.LABEL));
        if (p.rate().n() < 2L * ImpactRateService.MIN_N) {
            caveats.add(FindingCaveats.of(FindingCaveats.SMALL_SAMPLE, "The conditional rate rests on "
                    + p.rate().n() + " hazard occurrences in " + p.regionClass() + "."));
        }
        caveats.addAll(infra.caveats());
        Map<String, Object> params = new LinkedHashMap<>();
        ImpactProjector.Settings s = settings();
        params.put("minProbability", s.minProbability());
        params.put("questionMinProbability", s.questionMinProbability());
        params.put("assetBufferKm", s.assetBufferKm());
        params.put("pairs", pairs.version());
        params.put("lagHours", p.pair().lagHours());
        String explanation = explanation(forecast, p, windowEnd);
        List<String> members = new ArrayList<>(forecast.sourceRefs());
        long asOfSeq = forecast.sourceRefs().stream().mapToLong(ImpactProjectionService::seqOf).max().orElse(0);
        return new FindingDraft(FINDING_TYPE, title, severity(p.pImpact()), null, List.of(p.admin1().code()),
                forecast.validFrom(), forecast.validTo(), now, List.of(), members, asOfSeq, stats, null, caveats,
                ImpactProjector.NAME, ImpactProjector.VERSION, params, explanation, privateHazard, null, List.of(),
                List.of(), forecast.hazardKey() + "|" + p.family().id() + "|" + p.admin1().code());
    }

    private static List<Map<String, Object>> assets(ImpactProjector.Projection p) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ImpactProjector.ExposedAsset a : p.assets()) {
            if (out.size() >= MAX_LISTED) {
                break;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ref", a.asset().ref());
            m.put("title", a.asset().title());
            m.put("kind", a.asset().kind());
            m.put("pHazard", a.pHazard());
            m.put("band", a.band());
            m.put("distanceKm", a.distanceKm());
            m.put("population", null);
            out.add(m);
        }
        return out;
    }

    private static List<Map<String, Object>> paths(ImpactProjector.Projection p) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ImpactExposure.Path path : p.paths()) {
            if (out.size() >= MAX_LISTED) {
                break;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("from", path.from());
            m.put("to", path.to());
            m.put("hops", path.hops());
            m.put("weakestEdgeLabel", path.weakestEdgeLabel());
            m.put("depth", path.depth());
            out.add(m);
        }
        return out;
    }

    private List<Map<String, Object>> inputs(HazardForecast forecast, ImpactProjector.Projection p,
            List<Map<String, Object>> context) {
        List<Map<String, Object>> out = new ArrayList<>();
        out.add(input("hazardProbability", Map.of("pHazard", p.pHazard(), "band", String.valueOf(p.band()),
                "model", forecast.probabilityModel()), forecast.sourceRef()));
        out.add(input("conditionalRate", p.rate().view(), null));
        out.add(input("impactPair", Map.of("hazardClass", p.pair().hazardClass(), "impactFamily",
                p.family().id(), "lagHours", p.pair().lagHours()), null));
        Geometry regionArea = forecast.geometry() == null ? p.admin1().geometry()
                : safeIntersection(forecast.geometry(), p.admin1().geometry());
        out.add(input("exposure", ImpactExposure.compact(exposure.summary(regionArea)), null));
        out.addAll(context);
        return out;
    }

    private static Geometry safeIntersection(Geometry a, Geometry b) {
        try {
            return a.intersection(b);
        } catch (RuntimeException e) {
            return b;
        }
    }

    private static Map<String, Object> input(String name, Object value, String ref) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("value", value);
        m.put("ref", ref);
        return m;
    }

    /** The cones of active storms as context (never a probability) for tropical wind hazards. */
    private List<Map<String, Object>> context(HazardForecast forecast) {
        if (!forecast.hazardType().startsWith("tropical-wind-")) {
            return List.of();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        try {
            for (HazardRecord cone : reader.currentCones()) {
                HazardForecast c = HazardAdapters.cone(cone);
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("hazardType", c.hazardType());
                value.put("probabilityModel", c.probabilityModel());
                value.put("stormId", c.details().get("stormId"));
                value.put("stormName", c.details().get("stormName"));
                value.put("advisoryNumber", c.details().get("advisoryNumber"));
                value.put("validFrom", c.validFrom() == null ? null : c.validFrom().toString());
                value.put("validTo", c.validTo() == null ? null : c.validTo().toString());
                value.put("note", c.note());
                out.add(input("cone", value, c.sourceRef()));
            }
        } catch (RuntimeException e) {
            log.debug("Cone context unavailable: {}", e.toString());
        }
        return out;
    }

    private static String explanation(HazardForecast forecast, ImpactProjector.Projection p, Instant windowEnd) {
        ImpactRateService.ImpactRate r = p.rate();
        return label(forecast.hazardType()) + ": " + percent(p.pHazard()) + " chance of the hazard in "
                + p.admin1().name() + " (band " + p.band() + ", " + forecast.probabilityModel() + ", valid "
                + forecast.validFrom() + " to " + forecast.validTo() + "); given the hazard, " + p.family().label()
                + " was reported in the same admin1 region within " + p.pair().lagHours() + " h in " + percent(r.rate())
                + " of " + r.n() + " comparable cases (" + p.regionClass() + ", 95% interval " + percent(r.lower())
                + "–" + percent(r.upper()) + ") → " + percent(p.pImpact()) + " chance of " + p.family().label()
                + " reported by " + TITLE_TIME.format(windowEnd) + " (interval " + percent(p.lower()) + "–"
                + percent(p.upper()) + "). " + ImpactProjector.LABEL + ".";
    }

    // ------------------------------------------------------------------------- ledger

    /** Opens (or finds) the projection's question, links it and records the impact forecast; null on failure. */
    private String question(HazardForecast forecast, ImpactProjector.Projection p, AnalyticFindingEntity finding,
            boolean privateHazard, Instant now) {
        ForecastService service = ledger.getIfAvailable();
        ForecasterRegistry registry = forecasters.getIfAvailable();
        if (service == null || registry == null) {
            return null;
        }
        try {
            ForecastInputs.QuestionDraft draft = questionDraft(forecast, p, rules.ensure(p.family().id()).toString(),
                    finding.getId(), privateHazard, now);
            if (draft == null) {
                return null;
            }
            ForecastService.Opened opened = service.openQuestion(draft, "system", ImpactProjector.NAME);
            ForecastQuestionEntity q = opened.question();
            findings.linkQuestion(finding.getId(), q.getId());
            if (q.acceptsForecasts(clock.instant())) {
                Map<String, Object> inputs = new LinkedHashMap<>();
                inputs.put("findingId", finding.getId());
                inputs.put("manifestId", finding.getManifestId());
                inputs.put("pHazard", p.pHazard());
                inputs.put("pConditional", p.rate().rate());
                inputs.put("n", p.rate().n());
                inputs.put("model", ImpactProjector.NAME + " " + ImpactProjector.VERSION);
                service.record(q, ensureForecaster(registry), ForecastInputs.ForecastValue.binary(p.pImpact()),
                        "P(impact) = P(hazard) " + p.pHazard() + " × P(impact | hazard class) " + p.rate().rate()
                                + " (n = " + p.rate().n() + ", " + p.regionClass() + ")",
                        List.of(Map.of("ref", "finding:" + finding.getId()), Map.of("ref", forecast.sourceRef())),
                        null, inputs, null, false);
            }
            return q.getId();
        } catch (RuntimeException e) {
            log.warn("The ledger question of projection {} failed: {}", finding.getId(), e.toString());
            return null;
        }
    }

    /** The ledger forecaster {@value #FORECASTER}, registered on first use. */
    public static ForecasterEntity ensureForecaster(ForecasterRegistry registry) {
        return registry.ensure(FORECASTER, FORECASTER, "Impact projection", ImpactProjector.VERSION,
                Map.of("component", ImpactProjector.NAME + " " + ImpactProjector.VERSION));
    }

    /**
     * The question draft of a projection, or null when its window has passed. Pure apart from the resolver
     * validation.
     *
     * @param forecast the hazard forecast
     * @param p the projection
     * @param ruleId the impact family's trigger rule
     * @param findingId the projection's finding
     * @param privateQuestion true for a private hazard
     * @param now the decision time
     */
    public static ForecastInputs.QuestionDraft questionDraft(HazardForecast forecast, ImpactProjector.Projection p,
            String ruleId, String findingId, boolean privateQuestion, Instant now) {
        Instant start = forecast.validFrom();
        Instant end = questionEnd(forecast, p);
        if (start == null || !end.isAfter(now.plus(Duration.ofHours(1)))) {
            return null;
        }
        Instant closes = start.isAfter(now.plus(Duration.ofHours(1))) ? start : now.plus(Duration.ofHours(1));
        String where = p.admin1().name() + " (" + p.admin1().code() + ")";
        String title = "Will " + p.family().label() + " be reported in " + where + " between "
                + TITLE_TIME.format(start) + " and " + TITLE_TIME.format(end) + "?";
        if (title.length() > 300) {
            title = title.substring(0, 297) + "...";
        }
        Map<String, Object> resolver = new LinkedHashMap<>();
        resolver.put("type", "event-exists");
        resolver.put("selector", Map.of("alertRuleId", ruleId));
        resolver.put("area", Map.of("regionCodes", List.of(p.admin1().code())));
        resolver.put("window", Map.of("start", start.toString(), "end", end.toString()));
        Map<String, Object> validated = ResolverSpec.validate(resolver, Ledger.BINARY);
        String note = p.family().note() == null ? "" : " " + p.family().note();
        String criteria = "Resolves yes if Foresignal's impact rule for " + p.family().label() + " fires at least once"
                + " for a record in " + where + " between " + start + " and " + end + " (UTC);" + note + " Resolves no"
                + " otherwise, provided the rule's sources covered the window and could have seen such a report (else"
                + " the question is annulled).";
        if (criteria.length() > ForecastInputs.MAX_CRITERIA) {
            criteria = criteria.substring(0, ForecastInputs.MAX_CRITERIA);
        }
        Map<String, String> links = new LinkedHashMap<>();
        links.put("findingId", findingId);
        String issued = forecast.issuedAt() == null ? "unknown" : Long.toString(forecast.issuedAt().getEpochSecond());
        String dedup = "impact:" + forecast.hazardKey() + ":" + issued + ":" + p.family().id() + ":"
                + p.admin1().code();
        if (dedup.length() > 200) {
            dedup = dedup.substring(0, 200);
        }
        return new ForecastInputs.QuestionDraft(title, Ledger.BINARY, Map.of(), criteria, validated, now, closes, end,
                CATEGORY, List.of("impact", forecast.hazardType(), p.family().id()), links, dedup, privateQuestion);
    }

    /** The end of a projection's question window: the hazard's valid end plus the pair's lag. */
    static Instant questionEnd(HazardForecast forecast, ImpactProjector.Projection p) {
        Instant to = forecast.validTo() != null ? forecast.validTo() : forecast.validFrom();
        return to.plus(Duration.ofHours(p.pair().lagHours()));
    }

    // ------------------------------------------------------------------------- helpers

    private List<AnalyticFindingEntity> openFindings(String hazardKey) {
        List<String> ids = jdbc.queryForList("SELECT id FROM analytic_finding WHERE finding_type = :type AND status ="
                + " 'open' AND stats ->> 'hazardKey' = :key ORDER BY id", new MapSqlParameterSource("type",
                        FINDING_TYPE).addValue("key", hazardKey), String.class);
        List<AnalyticFindingEntity> out = new ArrayList<>();
        for (String id : ids) {
            findingRepository.findById(id).ifPresent(out::add);
        }
        return out;
    }

    private boolean privateHazard(HazardForecast forecast) {
        IntelVisibility v = visibility.getIfAvailable();
        if (v == null) {
            return false;
        }
        for (String ref : forecast.sourceRefs()) {
            String source = ref.substring("record:".length(), ref.indexOf('/'));
            if (v.isPrivateSource(source)) {
                return true;
            }
        }
        return false;
    }

    private double number(String key, double fallback) {
        try {
            Optional<Double> v = settings.get(key, Double.class);
            return v.filter(Double::isFinite).orElse(fallback);
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    /** The severity of a projection: significant at 50% or more, notable at 20% or more, else info. */
    static String severity(double pImpact) {
        return pImpact >= 0.5 ? FindingSeverities.SIGNIFICANT : pImpact >= 0.2 ? FindingSeverities.NOTABLE
                : FindingSeverities.INFO;
    }

    static Map<String, Object> interval(Double lo, Double hi) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("lo", lo);
        m.put("hi", hi);
        return m;
    }

    static String percent(Double p) {
        if (p == null) {
            return "?";
        }
        double v = p * 100;
        return (v >= 10 || v == Math.rint(v) ? String.valueOf(Math.round(v)) : String.format(Locale.ROOT, "%.1f", v))
                + "%";
    }

    /** The hazard type's label for titles. */
    static String label(String hazardType) {
        return switch (hazardType) {
            case HazardForecast.TROPICAL_WIND_34 -> "Tropical-storm-force winds (34 kt)";
            case HazardForecast.TROPICAL_WIND_50 -> "50-kt winds";
            case HazardForecast.TROPICAL_WIND_64 -> "Hurricane-force winds (64 kt)";
            case HazardForecast.TORNADO -> "Tornado outlook";
            case HazardForecast.WIND -> "Damaging-wind outlook";
            case HazardForecast.HAIL -> "Hail outlook";
            case HazardForecast.EXCESSIVE_RAIN -> "Excessive rainfall outlook";
            case HazardForecast.RIVER_FLOOD -> "River flood forecast";
            default -> hazardType;
        };
    }

    static long seqOf(String ref) {
        int at = ref.lastIndexOf('@');
        try {
            return at < 0 ? 0 : Long.parseLong(ref.substring(at + 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
