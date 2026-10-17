package ai.foresignal.outlook.regional;

import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingSeverities;
import ai.foresignal.analytics.math.Seeds;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.baserate.ReportingDensity;
import ai.foresignal.baserate.RegionProfileRepository;
import ai.foresignal.baserate.reference.ReferenceCounts;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastScoreEntity;
import ai.foresignal.forecast.ForecastScoreRepository;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.QuestionResolutionListener;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.observability.Place;
import ai.foresignal.outlook.index.RegionalIndexDefEntity;
import ai.foresignal.outlook.index.RegionalIndexDefRepository;
import ai.foresignal.outlook.index.RegionalIndexValueEntity;
import ai.foresignal.outlook.index.RegionalIndexValueRepository;
import ai.foresignal.outlook.series.QuantileSet;
import ai.foresignal.outlook.series.SeriesForecastService;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.security.Ulids;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Regional outlooks (prompt 126, {@code regional-outlook} 1.0.0, Intelligence-Design §9.2).
 *
 * <ul>
 *   <li><b>Regions</b> per family: countries with at least {@value RegionalOutlookModel#MIN_WEEKS} weeks of reference
 *       history and an event in the last two years, plus admin-1 regions with at least 52 weeks of 5 or more events
 *       ({@link #eligible}).
 *   <li><b>Fit</b> ({@link #refit}, monthly): the {@link RegionalOutlookModel} per region, intercepts pooled toward the
 *       continent class by empirical Bayes, and the rolling-origin evaluation over the last 26 weeks against seasonal
 *       naive and climatology; stored in {@code regional_outlook_model} with a manifest.
 *   <li><b>Publish</b> ({@link #publish}, Mondays 06:00 UTC): six 4-week windows starting next Monday, as a
 *       {@code regional_outlook} row, {@code foresignal-outlook} records (kind {@code regional-outlook}, one per
 *       window), the SSE event {@code outlook.published}, and per window a numeric ledger question (a numeric
 *       {@code count-threshold} over the same reference family and region) forecast by {@value
 *       SeriesForecastService#FORECASTER}. Each outlook carries the skill measured on its resolved questions and the
 *       flags {@code LOW_REPORTING_DENSITY} (121) and {@code LOW_DETECTABILITY} (116, when the counts come from
 *       Foresignal's own canonical events and their detectability is below {@code absenceMinDetect}).
 *   <li><b>Resolution</b> ({@link #questionResolved}): the window takes its actual count and p10–p90 hit; a count
 *       above p95 (or below p05 when detectability is adequate) becomes an {@code outlook-exceedance} finding.
 * </ul>
 */
@Service
public class RegionalOutlookService implements QuestionResolutionListener {

    private static final Logger log = LoggerFactory.getLogger(RegionalOutlookService.class);

    /** The forecaster label. */
    public static final String MODEL_VERSION = RegionalOutlookModel.NAME + " " + RegionalOutlookModel.VERSION;

    /** Days after which a family is refitted. */
    public static final int REFIT_DAYS = 30;

    /** Resolved questions behind an {@code ok} skill label. */
    public static final int SKILL_MIN = 20;

    /** The most weeks between the end of the history and the first window. */
    public static final int MAX_GAP_WEEKS = 52;

    private final OutlookFamilies families;
    private final ReferenceCounts counts;
    private final RegionNeighbors neighbors;
    private final RegionalOutlookModelRepository models;
    private final RegionalOutlookRepository outlooks;
    private final RegionalIndexDefRepository indexDefs;
    private final RegionalIndexValueRepository indexValues;
    private final RegionProfileRepository profiles;
    private final FindingService findings;
    private final ForecastScoreRepository scores;
    private final ObjectProvider<ForecastService> ledger;
    private final ObjectProvider<ForecasterRegistry> forecasters;
    private final ObjectProvider<ManifestService> manifests;
    private final ObjectProvider<ObservabilityService> observability;
    private final ObjectProvider<CountryCentroids> countries;
    private final SeqClockQuery seqClock;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public RegionalOutlookService(OutlookFamilies families, ReferenceCounts counts, RegionNeighbors neighbors,
            RegionalOutlookModelRepository models, RegionalOutlookRepository outlooks,
            RegionalIndexDefRepository indexDefs, RegionalIndexValueRepository indexValues,
            RegionProfileRepository profiles, FindingService findings, ForecastScoreRepository scores,
            ObjectProvider<ForecastService> ledger, ObjectProvider<ForecasterRegistry> forecasters,
            ObjectProvider<ManifestService> manifests, ObjectProvider<ObservabilityService> observability,
            ObjectProvider<CountryCentroids> countries, SeqClockQuery seqClock, NamedParameterJdbcTemplate jdbc,
            Clock clock) {
        this.families = families;
        this.counts = counts;
        this.neighbors = neighbors;
        this.models = models;
        this.outlooks = outlooks;
        this.indexDefs = indexDefs;
        this.indexValues = indexValues;
        this.profiles = profiles;
        this.findings = findings;
        this.scores = scores;
        this.ledger = ledger;
        this.forecasters = forecasters;
        this.manifests = manifests;
        this.observability = observability;
        this.countries = countries;
        this.seqClock = seqClock;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * What a run did.
     *
     * @param families families processed
     * @param regions regions fitted or published
     * @param questions ledger questions opened
     * @param errors regions that failed
     */
    public record RunResult(int families, int regions, int questions, int errors) {}

    // ------------------------------------------------------------------------- eligibility and inputs

    /** The Monday of the week holding {@code now}. */
    static LocalDate currentMonday(Instant now) {
        return now.atZone(ZoneOffset.UTC).toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Whether a region's weekly counts support an outlook: a country with at least 52 weeks and an event in the last
     * 104; an admin-1 region with at least 52 weeks of 5 or more events.
     */
    public static boolean eligible(ReferenceCounts.Weekly weekly) {
        if (weekly == null || "ZZ".equals(weekly.region())) {
            return false;
        }
        long[] c = weekly.counts();
        if (weekly.region().contains("-")) {
            int busy = 0;
            for (long v : c) {
                if (v >= 5) {
                    busy++;
                }
            }
            return busy >= 52;
        }
        if (c.length < RegionalOutlookModel.MIN_WEEKS) {
            return false;
        }
        for (int i = Math.max(0, c.length - 104); i < c.length; i++) {
            if (c[i] > 0) {
                return true;
            }
        }
        return false;
    }

    /** The model inputs of a region: its counts, the neighbor term and the index of an enabled index for the family. */
    RegionalOutlookModel.Input input(String family, ReferenceCounts.Weekly weekly,
            Map<String, ReferenceCounts.Weekly> all) {
        int n = weekly.size();
        double[] nb = new double[n];
        java.util.Arrays.fill(nb, Double.NaN);
        for (String neighbor : neighbors.of(weekly.region())) {
            ReferenceCounts.Weekly w = all.get(neighbor);
            if (w == null) {
                continue;
            }
            for (int t = 0; t < n; t++) {
                int j = w.indexOf(weekly.weekAt(t));
                if (j >= 0) {
                    nb[t] = (Double.isNaN(nb[t]) ? 0 : nb[t]) + Math.log1p(w.counts()[j]);
                }
            }
        }
        double[] index = null;
        List<RegionalIndexDefEntity> defs = indexDefs.findByEnabledTrueAndTarget(family);
        if (!defs.isEmpty()) {
            index = new double[n];
            java.util.Arrays.fill(index, Double.NaN);
            for (RegionalIndexValueEntity v : indexValues.findByDefIdAndRegionAndDayBetweenOrderByDayAsc(
                    defs.get(0).getId(), weekly.region(), weekly.firstWeek(), weekly.weekAt(n))) {
                int t = weekly.indexOf(ReferenceFamilies.weekStart(v.getDay()));
                if (t >= 0) {
                    index[t] = v.getValue();
                }
            }
        }
        return new RegionalOutlookModel.Input(weekly.counts(), weekly.firstWeek(), nb, index);
    }

    /** The class of a region for partial pooling: {@code continent:XX} of its country, else {@code unknown}. */
    String regionClass(String region) {
        CountryCentroids table = countries.getIfAvailable();
        String iso2 = region.length() >= 2 ? region.substring(0, 2) : region;
        if (table == null) {
            return "unknown";
        }
        return table.byIso2(iso2).map(CountryRef::continent).map(c -> "continent:" + c).orElse("unknown");
    }

    // ------------------------------------------------------------------------- fitting

    /**
     * Refits every family that has no fit yet (before the first outlooks). Refits of existing fits are a step of the
     * monthly maintenance of prompt 128 ({@link #refitAll}), which replaced the 30-day age rule.
     */
    public RunResult refitDue(Instant now) {
        return refit(now, false);
    }

    /** Refits every family at {@code now} (the monthly maintenance, prompt 128). */
    public RunResult refitAll(Instant now) {
        return refit(now, true);
    }

    private RunResult refit(Instant now, boolean all) {
        int fams = 0;
        int regions = 0;
        int errors = 0;
        for (OutlookFamilies.Family f : families.all()) {
            List<RegionalOutlookModelEntity> rows = models.findByFamily(f.id());
            boolean due = all || rows.isEmpty();
            if (!due) {
                continue;
            }
            fams++;
            try {
                regions += refit(f, now).size();
            } catch (RuntimeException e) {
                errors++;
                log.warn("Refit of outlook family {} failed: {}", f.id(), e.toString());
            }
        }
        return new RunResult(fams, regions, 0, errors);
    }

    /** Refits one family at {@code now}: per-region fits, pooled intercepts, evaluations. */
    public Map<String, RegionalOutlookModelEntity> refit(OutlookFamilies.Family family, Instant now) {
        LocalDate until = currentMonday(now);
        Map<String, ReferenceCounts.Weekly> all = counts.weekly(family.referenceFamily(), until, null);
        Map<String, RegionalOutlookModel.Fit> fits = new TreeMap<>();
        Map<String, RegionalOutlookModel.Input> inputs = new TreeMap<>();
        Map<String, String> classes = new LinkedHashMap<>();
        for (ReferenceCounts.Weekly w : all.values()) {
            if (!eligible(w)) {
                continue;
            }
            RegionalOutlookModel.Input in = input(family.id(), w, all);
            inputs.put(w.region(), in);
            fits.put(w.region(), RegionalOutlookModel.fit(in, in.size()));
            classes.put(w.region(), regionClass(w.region()));
        }
        Map<String, RegionalOutlookModel.Fit> pooled = RegionalOutlookModel.shrink(fits, classes);
        Map<String, RegionalOutlookModelEntity> out = new TreeMap<>();
        for (Map.Entry<String, RegionalOutlookModel.Fit> e : pooled.entrySet()) {
            String region = e.getKey();
            RegionalOutlookModel.Input in = inputs.get(region);
            RegionalOutlookModel.Evaluation eval = RegionalOutlookModel.evaluate(in, Seeds.of(
                    RegionalOutlookModel.NAME, "eval", region, family.id()), 40);
            RegionalOutlookModelEntity row = models.findById(RegionalOutlookModelEntity.key(region, family.id()))
                    .orElseGet(() -> new RegionalOutlookModelEntity(region, family.id(), now, MODEL_VERSION));
            Map<String, Object> fit = fitMap(e.getValue());
            fit.put("unpooledIntercept", fits.get(region).beta().length == 0 ? null
                    : QuantileSet.round(fits.get(region).beta()[0]));
            fit.put("regionClass", classes.get(region));
            row.setFit(fit);
            row.setEvaluation(eval.view());
            row.setFittedAt(now);
            row.setVersion(MODEL_VERSION);
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("terms", RegionalOutlookModel.terms(e.getValue().withNeighbors(), e.getValue().withIndex()));
            config.put("pooling", "empirical-bayes-intercept:continent");
            config.put("evaluationWeeks", RegionalOutlookModel.EVAL_WEEKS);
            row.setManifestId(manifest(RegionalOutlookModel.NAME + "-fit", config, List.of("reference-history:"
                    + family.referenceFamily() + "/" + region), Map.of("region", region, "family", family.id(),
                            "until", until.toString())));
            out.put(region, models.save(row));
        }
        return out;
    }

    /** The stored map of a fit (coefficients, covariance, dispersion, terms). */
    static Map<String, Object> fitMap(RegionalOutlookModel.Fit f) {
        Map<String, Object> out = f.view();
        List<List<Double>> cov = new ArrayList<>();
        for (double[] row : f.covariance()) {
            List<Double> r = new ArrayList<>();
            for (double v : row) {
                r.add(v);
            }
            cov.add(r);
        }
        out.put("covariance", cov);
        out.put("withNeighbors", f.withNeighbors());
        out.put("withIndex", f.withIndex());
        out.put("interceptVariance", Double.isFinite(f.interceptVariance()) ? f.interceptVariance() : null);
        return out;
    }

    /** A fit rebuilt from its stored map. */
    @SuppressWarnings("unchecked")
    static RegionalOutlookModel.Fit fromMap(Map<String, Object> m) {
        boolean fallback = "fallback-mean".equals(m.get("model"));
        List<Number> coefficients = (List<Number>) m.getOrDefault("coefficients", List.of());
        double[] beta = new double[coefficients.size()];
        for (int i = 0; i < beta.length; i++) {
            beta[i] = coefficients.get(i).doubleValue();
        }
        List<List<Number>> covList = (List<List<Number>>) m.getOrDefault("covariance", List.of());
        double[][] cov = new double[covList.size()][];
        for (int i = 0; i < cov.length; i++) {
            cov[i] = new double[covList.get(i).size()];
            for (int j = 0; j < cov[i].length; j++) {
                cov[i][j] = covList.get(i).get(j).doubleValue();
            }
        }
        double theta = m.get("theta") instanceof Number n ? n.doubleValue() : 1e6;
        double mean = m.get("fallbackMean") instanceof Number n ? n.doubleValue() : 0;
        int rows = m.get("rows") instanceof Number n ? n.intValue() : 0;
        double iv = m.get("interceptVariance") instanceof Number n ? n.doubleValue() : Double.NaN;
        return new RegionalOutlookModel.Fit(beta, cov, theta, fallback, mean,
                Boolean.TRUE.equals(m.get("withNeighbors")), Boolean.TRUE.equals(m.get("withIndex")), rows, iv);
    }

    // ------------------------------------------------------------------------- publishing

    /** Publishes every family's outlooks at {@code now} (refitting stale families first). */
    public RunResult publishAll(Instant now) {
        refitDue(now);
        int fams = 0;
        int regions = 0;
        int questions = 0;
        int errors = 0;
        for (OutlookFamilies.Family f : families.all()) {
            fams++;
            LocalDate until = currentMonday(now);
            Map<String, ReferenceCounts.Weekly> all = counts.weekly(f.referenceFamily(), until, null);
            for (ReferenceCounts.Weekly w : all.values()) {
                if (!eligible(w)) {
                    continue;
                }
                try {
                    RegionalOutlookEntity o = publish(f, w, all, now);
                    if (o != null) {
                        regions++;
                        questions += (int) o.getWindows().stream().filter(x -> x.get("questionId") != null).count();
                    }
                } catch (RuntimeException e) {
                    errors++;
                    log.warn("Outlook {} {} failed: {}", w.region(), f.id(), e.toString());
                }
            }
        }
        return new RunResult(fams, regions, questions, errors);
    }

    /** Publishes one region's outlook for a family at {@code now}, or null when its history is too stale. */
    public RegionalOutlookEntity publish(OutlookFamilies.Family family, ReferenceCounts.Weekly weekly,
            Map<String, ReferenceCounts.Weekly> all, Instant now) {
        LocalDate nextMonday = currentMonday(now).plusWeeks(1);
        int end = weekly.size();
        int offset = (int) ChronoUnit.WEEKS.between(weekly.weekAt(end), nextMonday);
        if (offset < 0 || offset > MAX_GAP_WEEKS) {
            return null;
        }
        RegionalOutlookModel.Input in = input(family.id(), weekly, all);
        RegionalOutlookModelEntity modelRow = models.findById(RegionalOutlookModelEntity.key(weekly.region(),
                family.id())).orElse(null);
        RegionalOutlookModel.Fit fit = modelRow == null ? RegionalOutlookModel.fit(in, end)
                : fromMap(modelRow.getFit());
        long seed = Seeds.of(RegionalOutlookModel.NAME, weekly.region(), family.id(), nextMonday.toString());
        double[][] q = RegionalOutlookModel.windows(fit, in, end, offset, RegionalOutlookModel.WINDOWS, seed,
                RegionalOutlookModel.DRAWS);
        long asOfSeq = seqClock.seqAt(jdbc, now);
        String region = weekly.region();
        RegionalOutlookEntity row = new RegionalOutlookEntity(Ulids.next(now), region, region.contains("-")
                ? "admin1" : "country", family.id(), family.category(), now, MODEL_VERSION, asOfSeq);
        row.setSource(weekly.source());
        boolean lowDensity = lowReportingDensity(region);
        boolean lowDetect = ReferenceFamilies.FORESIGNAL.equals(weekly.source())
                && lowDetectability(family, region, now);
        row.setLowReportingDensity(lowDensity);
        row.setLowDetectability(lowDetect);
        List<Map<String, Object>> caveats = new ArrayList<>();
        if (lowDensity) {
            caveats.add(FindingCaveats.of(FindingCaveats.LOW_REPORTING_DENSITY, "Media reporting of this region is in"
                    + " the bottom decile; event counts, and so the outlook, may understate activity."));
        }
        if (lowDetect) {
            caveats.add(FindingCaveats.of(FindingCaveats.LOW_DETECTABILITY, "The counts come from Foresignal's own"
                    + " sources, whose detection probability here is below the absence threshold; low counts are"
                    + " not read as low activity."));
        }
        if (offset > 4) {
            caveats.add(FindingCaveats.of(FindingCaveats.LAGGED_PUBLICATION, "The reference history ends "
                    + (offset - 1) + " weeks before the first window; the model simulates across the gap, which"
                    + " widens the quantiles."));
        }
        if (fit.fallback()) {
            caveats.add(FindingCaveats.of(FindingCaveats.SMALL_SAMPLE, "Too little history for the regression; the"
                    + " outlook uses the mean and dispersion of the last 52 weeks."));
        }
        row.setCaveats(caveats);
        List<Map<String, Object>> windows = new ArrayList<>();
        for (int w = 0; w < RegionalOutlookModel.WINDOWS; w++) {
            LocalDate start = nextMonday.plusWeeks((long) w * RegionalOutlookModel.WINDOW_WEEKS);
            Map<String, Object> window = new LinkedHashMap<>();
            window.put("index", w + 1);
            window.put("windowStart", start.toString());
            window.put("windowEnd", start.plusWeeks(RegionalOutlookModel.WINDOW_WEEKS).minusDays(1).toString());
            window.put("quantiles", countQuantiles(q[w]));
            window.put("tails", QuantileSet.tails(q[w]));
            window.put("questionId", null);
            window.put("status", "open");
            window.put("actual", null);
            window.put("withinP10P90", null);
            window.put("exceedance", null);
            windows.add(window);
        }
        row.setWindows(windows);
        row.setSkill(skill(region, family.id()));
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("fit", fit.view());
        config.put("windows", RegionalOutlookModel.WINDOWS);
        config.put("windowWeeks", RegionalOutlookModel.WINDOW_WEEKS);
        config.put("draws", RegionalOutlookModel.DRAWS);
        config.put("pathsPerDraw", RegionalOutlookModel.PATHS_PER_DRAW);
        row.setManifestId(manifest(RegionalOutlookModel.NAME, config, List.of("reference-history:"
                + family.referenceFamily() + "/" + region), Map.of("region", region, "family", family.id(),
                        "firstWindow", nextMonday.toString(), "source", String.valueOf(weekly.source()))));
        outlooks.findTop1ByRegionAndFamilyOrderByPublishedAtDescIdDesc(region, family.id()).ifPresent(prev -> {
            prev.setLatest(false);
            outlooks.save(prev);
        });
        row.setLatest(true);
        outlooks.save(row);
        openQuestions(row, family, now);
        outlooks.save(row);
        try {
            findings.announce(row.getId(), FindingEvent.OUTLOOK_PUBLISHED, RegionalOutlookModel.NAME, null);
        } catch (RuntimeException e) {
            log.warn("outlook.published for {} failed: {}", row.getId(), e.toString());
        }
        return row;
    }

    /** The ledger quantiles of a count window, rounded to whole events (monotone). */
    static Map<String, Double> countQuantiles(double[] q) {
        Map<String, Double> out = QuantileSet.ledger(q);
        out.replaceAll((k, v) -> (double) Math.round(v));
        return out;
    }

    /** Opens the numeric ledger question of each window and records the statistical forecast. */
    void openQuestions(RegionalOutlookEntity row, OutlookFamilies.Family family, Instant now) {
        ForecastService service = ledger.getIfAvailable();
        ForecasterRegistry registry = forecasters.getIfAvailable();
        if (service == null || registry == null) {
            return;
        }
        ForecasterEntity statistical = SeriesForecastService.ensureForecaster(registry);
        List<Map<String, Object>> windows = new ArrayList<>(row.getWindows());
        String name = regionName(row.getRegion());
        for (Map<String, Object> window : windows) {
            try {
                LocalDate start = LocalDate.parse(window.get("windowStart").toString());
                Instant from = start.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant to = start.plusWeeks(RegionalOutlookModel.WINDOW_WEEKS).atStartOfDay(ZoneOffset.UTC)
                        .toInstant();
                Map<String, Object> resolver = new LinkedHashMap<>();
                resolver.put("type", "count-threshold");
                resolver.put("selector", Map.of("referenceFamily", family.referenceFamily()));
                resolver.put("area", Map.of("regionCodes", List.of(row.getRegion())));
                resolver.put("window", Map.of("start", from.toString(), "end", to.toString()));
                Map<String, Object> validated = ResolverSpec.validate(resolver, Ledger.NUMERIC);
                ForecastInputs.QuestionDraft draft = new ForecastInputs.QuestionDraft("How many " + family.label()
                        .toLowerCase(java.util.Locale.ROOT) + " events will " + name + " see in the 4 weeks from "
                        + start + "?", Ledger.NUMERIC, Map.of("unit", "events", "lower", 0),
                        "The number of " + family.referenceFamily() + " events in " + row.getRegion() + " from " + from
                                + " to " + to + " in Foresignal's reference history (one source per region, as the"
                                + " outlook counts them), read 7 days after the window ends.", validated, now, from,
                        to, family.category(), List.of("regional-outlook", "family-" + family.id().replace(':', '-')
                                .replace('.', '-'), "region-" + row.getRegion().toLowerCase(java.util.Locale.ROOT)),
                        Map.of("outlookId", row.getId()), "regional-outlook:" + row.getRegion() + ":" + family.id()
                                + ":" + start, false);
                ForecastQuestionEntity q = service.openQuestion(draft, "system", RegionalOutlookModel.NAME)
                        .question();
                window.put("questionId", q.getId());
                if (q.acceptsForecasts(clock.instant())) {
                    @SuppressWarnings("unchecked")
                    Map<String, Double> quantiles = (Map<String, Double>) window.get("quantiles");
                    Map<String, Object> inputs = new LinkedHashMap<>();
                    inputs.put("outlookId", row.getId());
                    inputs.put("window", window.get("index"));
                    inputs.put("manifestId", row.getManifestId());
                    inputs.put("asOfSeq", row.getAsofSeq());
                    service.record(q, statistical, ForecastInputs.ForecastValue.quantiles(quantiles), "Statistical:"
                            + " regional outlook (NB GLM, CAST-style), window " + window.get("index") + ".",
                            List.of(Map.of("ref", "outlook:" + row.getId())), null, inputs, null, false);
                }
            } catch (RuntimeException e) {
                log.warn("The ledger question of outlook {} window {} failed: {}", row.getId(), window.get("index"),
                        e.toString());
            }
        }
        row.setWindows(windows);
    }

    /** True when the region's country is in the bottom reporting-density decile (121). */
    boolean lowReportingDensity(String region) {
        String iso2 = region.length() >= 2 ? region.substring(0, 2) : region;
        return profiles.findById(iso2).map(p -> p.getDensityDecile() <= ReportingDensity.LOW_DECILE).orElse(false);
    }

    /** True when the family's 116 detection probability in the region is below {@code absenceMinDetect}. */
    boolean lowDetectability(OutlookFamilies.Family family, String region, Instant now) {
        ObservabilityService service = observability.getIfAvailable();
        if (service == null) {
            return false;
        }
        try {
            return service.pDetect(family.observabilityFamily(), Place.region(region), now, null).p()
                    < service.absenceMinDetect();
        } catch (RuntimeException e) {
            return true;
        }
    }

    /** The display name of a region (the country name for countries). */
    String regionName(String region) {
        CountryCentroids table = countries.getIfAvailable();
        if (table == null || region.contains("-")) {
            return region;
        }
        return table.byIso2(region).map(CountryRef::name).orElse(region);
    }

    // ------------------------------------------------------------------------- skill

    /**
     * The skill of a region and family's outlooks: from the ledger, the {@value SeriesForecastService#FORECASTER}
     * forecaster's scores on the resolved window questions (mean pinball loss, CRPS, and the skill against the
     * {@code climatology} forecaster on shared questions) and the p10–p90 coverage of the resolved windows; from the
     * monthly rolling-origin evaluation, the skill against seasonal naive (the ledger has no seasonal-naive
     * forecaster). {@code label} is {@code insufficient} under {@value #SKILL_MIN} resolved questions.
     */
    public Map<String, Object> skill(String region, String family) {
        List<RegionalOutlookEntity> past = outlooks.findByRegionAndFamilyOrderByPublishedAtDescIdDesc(region, family,
                PageRequest.of(0, 60));
        List<String> questionIds = new ArrayList<>();
        int windows = 0;
        int inside = 0;
        for (RegionalOutlookEntity o : past) {
            for (Map<String, Object> w : o.getWindows()) {
                if ("resolved".equals(w.get("status"))) {
                    if (w.get("questionId") != null && !questionIds.contains(w.get("questionId").toString())) {
                        questionIds.add(w.get("questionId").toString());
                    }
                    if (w.get("withinP10P90") instanceof Boolean b) {
                        windows++;
                        inside += b ? 1 : 0;
                    }
                }
            }
        }
        double pinball = 0;
        double crps = 0;
        int n = 0;
        double sharedStat = 0;
        double sharedClim = 0;
        int shared = 0;
        for (String qid : questionIds) {
            Double stat = null;
            Double clim = null;
            for (ForecastScoreEntity s : scores.findByQuestionId(qid)) {
                Double mean = meanPinball(s.getPinball());
                if (SeriesForecastService.FORECASTER.equals(s.getForecasterId()) && mean != null) {
                    stat = mean;
                    pinball += mean;
                    crps += s.getCrps() == null ? 2 * mean : s.getCrps();
                    n++;
                } else if (ForecasterRegistry.CLIMATOLOGY.equals(s.getForecasterId()) && mean != null) {
                    clim = mean;
                }
            }
            if (stat != null && clim != null) {
                sharedStat += stat;
                sharedClim += clim;
                shared++;
            }
        }
        Map<String, Object> eval = models.findById(RegionalOutlookModelEntity.key(region, family))
                .map(RegionalOutlookModelEntity::getEvaluation).orElse(Map.of());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("evaluatedWeeks", eval.getOrDefault("evaluatedWeeks", RegionalOutlookModel.EVAL_WEEKS));
        out.put("n", n);
        out.put("meanPinballLoss", n == 0 ? null : QuantileSet.round(pinball / n));
        out.put("crps", n == 0 ? null : QuantileSet.round(crps / n));
        out.put("skillVsSeasonalNaive", eval.get("skillVsSeasonalNaive"));
        out.put("skillVsClimatology", shared == 0 || !(sharedClim > 0) ? null
                : QuantileSet.round(1 - sharedStat / sharedClim));
        out.put("intervalCoverageP10P90", windows == 0 ? null : QuantileSet.round((double) inside / windows));
        out.put("label", n >= SKILL_MIN ? "ok" : "insufficient");
        out.put("backtest", eval.isEmpty() ? null : eval);
        return out;
    }

    private static Double meanPinball(Map<String, Double> pinball) {
        if (pinball == null || pinball.isEmpty()) {
            return null;
        }
        double s = 0;
        for (double v : pinball.values()) {
            s += v;
        }
        return s / pinball.size();
    }

    // ------------------------------------------------------------------------- resolution

    @Override
    public void questionResolved(String questionId, String outcome) {
        ForecastService service = ledger.getIfAvailable();
        if (service == null) {
            return;
        }
        ForecastQuestionEntity q = service.find(questionId).orElse(null);
        if (q == null || q.getOutlookId() == null) {
            return;
        }
        try {
            onResolved(q, clock.instant());
        } catch (RuntimeException e) {
            log.warn("Outlook resolution of question {} failed: {}", questionId, e.toString());
        }
    }

    /** Records a resolved window question on its outlook; returns the exceedance finding written, or null. */
    public FindingService.Outcome onResolved(ForecastQuestionEntity q, Instant now) {
        RegionalOutlookEntity row = outlooks.findById(q.getOutlookId()).orElse(null);
        if (row == null) {
            return null;
        }
        Map<String, Object> resolution = q.getResolution();
        Object value = resolution == null ? null : resolution.get("value");
        List<Map<String, Object>> windows = new ArrayList<>(row.getWindows());
        FindingService.Outcome finding = null;
        for (Map<String, Object> w : windows) {
            if (!q.getId().equals(w.get("questionId"))) {
                continue;
            }
            if (!"resolved".equals(q.getStatus()) || !(value instanceof Number number)) {
                w.put("status", "annulled");
                continue;
            }
            double actual = number.doubleValue();
            @SuppressWarnings("unchecked")
            double[] quantiles = QuantileSet.fromMaps((Map<String, ?>) w.get("quantiles"),
                    (Map<String, ?>) w.get("tails"));
            w.put("status", "resolved");
            w.put("actual", Math.round(actual));
            w.put("withinP10P90", actual >= quantiles[QuantileSet.P10] && actual <= quantiles[QuantileSet.P90]);
            String exceedance = null;
            if (actual > quantiles[QuantileSet.P95]) {
                exceedance = "above-p95";
            } else if (actual < quantiles[QuantileSet.P05] && !row.isLowDetectability()) {
                exceedance = "below-p05";
            }
            w.put("exceedance", exceedance);
            if (exceedance != null) {
                finding = exceedanceFinding(row, w, actual, quantiles, exceedance, now);
            }
        }
        row.setWindows(windows);
        row.setUpdatedAt(now);
        outlooks.save(row);
        return finding;
    }

    private FindingService.Outcome exceedanceFinding(RegionalOutlookEntity row, Map<String, Object> window,
            double actual, double[] q, String exceedance, Instant now) {
        boolean above = "above-p95".equals(exceedance);
        boolean extreme = above ? actual > q[QuantileSet.P99] : actual < q[QuantileSet.P01];
        LocalDate start = LocalDate.parse(window.get("windowStart").toString());
        Instant from = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = start.plusWeeks(RegionalOutlookModel.WINDOW_WEEKS).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(FindingCaveats.notGroundTruth());
        caveats.addAll(row.getCaveats());
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("outlookId", row.getId());
        stats.put("window", window.get("index"));
        stats.put("family", row.getFamily());
        stats.put("actual", Math.round(actual));
        stats.put("p05", q[QuantileSet.P05]);
        stats.put("p50", q[QuantileSet.P50]);
        stats.put("p95", q[QuantileSet.P95]);
        stats.put("exceedance", exceedance);
        stats.put("questionId", window.get("questionId"));
        Map<String, Object> baseRate = new LinkedHashMap<>();
        baseRate.put("value", 0.05);
        baseRate.put("unit", "probability");
        baseRate.put("method", "nominal-interval-miss");
        baseRate.put("text", "a calibrated outlook falls " + (above ? "above p95" : "below p05") + " in 5% of windows by"
                + " construction; the outlook's measured coverage is in its skill");
        String explanation = row.getFamily() + " events in " + row.getRegion() + " in the 4 weeks from " + start
                + " came in " + (above ? "above" : "below") + " the outlook's " + (above ? "p95" : "p05") + " ("
                + Math.round(actual) + " against " + Math.round(above ? q[QuantileSet.P95] : q[QuantileSet.P05])
                + ").";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("model", MODEL_VERSION);
        return findings.record(new FindingDraft("outlook-exceedance", "Outlook exceedance: " + row.getFamily() + " in "
                + row.getRegion() + " " + (above ? "above p95" : "below p05"), extreme
                        ? FindingSeverities.SIGNIFICANT : FindingSeverities.NOTABLE, null, List.of(row.getRegion()), from,
                to, now, List.of(), List.of("outlook:" + row.getId()), row.getAsofSeq(), stats, baseRate, caveats,
                RegionalOutlookModel.NAME, RegionalOutlookModel.VERSION, params, explanation, false,
                ReferenceFamilies.root(row.getFamily()), List.of(), List.of(), row.getRegion() + "|" + row.getFamily()
                        + "|" + start));
    }

    // ------------------------------------------------------------------------- reads

    /**
     * A page of latest outlooks.
     *
     * @param items the outlooks
     * @param nextCursor the cursor of the next page, or null
     */
    public record Page(List<RegionalOutlookEntity> items, String nextCursor) {}

    /** The latest outlooks, filtered by family and region, by id. */
    public Page list(String family, String region, String cursor, int limit) {
        String after = cursor == null || cursor.isBlank() ? "" : decode(cursor);
        List<RegionalOutlookEntity> out = new ArrayList<>();
        String last = after;
        while (out.size() <= limit) {
            List<RegionalOutlookEntity> page = outlooks.findByLatestTrueAndIdGreaterThanOrderByIdAsc(last,
                    PageRequest.of(0, 500));
            if (page.isEmpty()) {
                break;
            }
            for (RegionalOutlookEntity o : page) {
                last = o.getId();
                if ((family == null || family.equals(o.getFamily())) && (region == null || region.equals(o.getRegion()))) {
                    out.add(o);
                    if (out.size() > limit) {
                        break;
                    }
                }
            }
            if (page.size() < 500) {
                break;
            }
        }
        String next = null;
        if (out.size() > limit) {
            out = new ArrayList<>(out.subList(0, limit));
            next = encode(out.get(out.size() - 1).getId());
        }
        return new Page(out, next);
    }

    /** The latest outlook of a region and family, or 404 {@code UNKNOWN_OUTLOOK}. */
    public RegionalOutlookEntity latest(String region, String family) {
        return outlooks.findTop1ByRegionAndFamilyOrderByPublishedAtDescIdDesc(region, family).orElseThrow(
                () -> new NotFoundException(ErrorCodes.UNKNOWN_OUTLOOK, "No outlook for " + region + " and " + family
                        + "."));
    }

    /** The past windows of a region and family with actuals, newest first (at most {@code limit}). */
    public List<Map<String, Object>> history(String region, String family, int limit) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (RegionalOutlookEntity o : outlooks.findByRegionAndFamilyOrderByPublishedAtDescIdDesc(region, family,
                PageRequest.of(0, 60))) {
            for (Map<String, Object> w : o.getWindows()) {
                if (!"open".equals(w.get("status"))) {
                    out.add(w);
                    if (out.size() >= limit) {
                        return out;
                    }
                }
            }
        }
        return out;
    }

    /** The family with {@code id}, or 404 {@code UNKNOWN_OUTLOOK}. */
    public OutlookFamilies.Family family(String id) {
        return families.find(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_OUTLOOK,
                "No outlook family '" + id + "'."));
    }

    /** The outlook with {@code id}, if any. */
    public Optional<RegionalOutlookEntity> find(String id) {
        return outlooks.findById(id);
    }

    /** The statistical forecast recorded on a window question, or null (tests). */
    ForecastEntity statisticalForecast(String questionId) {
        ForecastService service = ledger.getIfAvailable();
        return service == null ? null : service.latestByForecaster(questionId).get(SeriesForecastService.FORECASTER);
    }

    static String encode(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(java.nio.charset.StandardCharsets
                .UTF_8));
    }

    static String decode(String cursor) {
        try {
            return new String(Base64.getUrlDecoder().decode(cursor), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", "is not a cursor of this list");
        }
    }

    private String manifest(String component, Map<String, Object> config, List<String> inputs,
            Map<String, Object> query) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            return service.record(ManifestRequest.of(component, RegionalOutlookModel.VERSION).config(config)
                    .asOf(seqClock.seqAt(jdbc, clock.instant())).inputs(inputs).query(query));
        } catch (RuntimeException e) {
            log.warn("Could not record the manifest of {}: {}", component, e.toString());
            return null;
        }
    }
}
