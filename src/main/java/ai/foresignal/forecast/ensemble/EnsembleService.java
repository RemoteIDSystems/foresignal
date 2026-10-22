package ai.foresignal.forecast.ensemble;

import ai.foresignal.forecast.ForecastCalibrationEntity;
import ai.foresignal.forecast.ForecastCalibrationRepository;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastQuestionRepository;
import ai.foresignal.forecast.ForecastScoreEntity;
import ai.foresignal.forecast.ForecastScoreRepository;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastStatus;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.score.QuestionScorer;
import ai.foresignal.forecast.score.ScoringService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The {@code ensemble} forecaster and its monthly refit (prompt 122 §6, Intelligence-Design §8.4).
 *
 * <ul>
 *   <li><b>Members</b>: the latest forecast of every active forecaster except the ensemble, leaving out forecasts older
 *       than {@code maxAgeDays} (default 7) unless the question itself is younger, and {@code external} forecasters
 *       with fewer than {@code externalMinResolved} (30) resolved questions (EXT-3), and {@code llm} forecasters while
 *       the 123 benchmark gate degrades their weight (BEN-2).</li>
 *   <li><b>Inputs</b>: an {@code llm} forecaster's probability is clamped to [0.03, 0.97], then a forecaster with a
 *       Platt fit in the question's category (≥ 50 resolved) is recalibrated; raw, capped and recalibrated values
 *       are all stored in the ensemble forecast's {@code inputs}.</li>
 *   <li><b>Combination</b> and <b>weights</b>: {@link EnsembleMath}; extremizing applies only when the last refit
 *       enabled it. The ensemble writes a new forecast only when its members or its value changed (by 0.005 or more).
 *       </li>
 *   <li><b>Refit</b> ({@link #refit}, monthly and on request): relative Brier scores (last 100 resolved questions per
 *       category), Platt fits and the extremizing gate: a fitted {@code a} is enabled only with ≥ 5 active forecasters
 *       of distinct types, ≥ 100 resolutions and a positive out-of-sample log-score gain (fitted on the older half,
 *       tested on the newer half), and only when {@code a > 1}.</li>
 * </ul>
 */
@Service
public class EnsembleService {

    private static final Logger log = LoggerFactory.getLogger(EnsembleService.class);

    /** The ensemble's component version. */
    public static final String VERSION = "1.0.0";

    /** The resolved forecasts a Platt fit needs. */
    public static final int PLATT_MIN = 50;

    /** The resolved questions per category behind the relative Brier score. */
    public static final int WEIGHT_WINDOW = 100;

    /** The resolutions the extremizing gate needs. */
    public static final int EXTREMIZE_MIN = 100;

    /** The distinct forecaster types the extremizing gate needs. */
    public static final int EXTREMIZE_TYPES = 5;

    /** The smallest change that writes a new ensemble forecast. */
    public static final double MIN_CHANGE = 0.005;

    private final ForecastService ledger;
    private final ForecasterRegistry forecasters;
    private final ForecastQuestionRepository questions;
    private final ForecastScoreRepository scores;
    private final ForecastCalibrationRepository calibrations;
    private final ScoringService scoring;
    private final AppSettingService settings;
    private final Clock clock;

    /** The 123 benchmark gate: LLM forecasters carry no ensemble weight while their dependencies fail (BEN-2). */
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.beans.factory.ObjectProvider<ai.foresignal.benchmark.BenchmarkGate> benchmarkGate;

    /** Creates the service. */
    public EnsembleService(ForecastService ledger, ForecasterRegistry forecasters,
            ForecastQuestionRepository questions, ForecastScoreRepository scores,
            ForecastCalibrationRepository calibrations, ScoringService scoring, AppSettingService settings,
            Clock clock) {
        this.ledger = ledger;
        this.forecasters = forecasters;
        this.questions = questions;
        this.scores = scores;
        this.calibrations = calibrations;
        this.scoring = scoring;
        this.settings = settings;
        this.clock = clock;
    }

    /**
     * The extremizing state of the last refit.
     *
     * @param a the factor
     * @param enabled whether it applies
     */
    public record Extremizing(double a, boolean enabled) {}

    /** The extremizing state in force ({@code a = 1}, off, before the first refit). */
    @SuppressWarnings("unchecked")
    public Extremizing extremizing() {
        try {
            Map<String, Object> state = settings.get(SettingKeys.FORECAST_ENSEMBLE, Map.class).orElse(Map.of());
            Object a = state.get("a");
            boolean enabled = Boolean.TRUE.equals(state.get("enabled"));
            return new Extremizing(a instanceof Number n ? n.doubleValue() : 1.0, enabled);
        } catch (RuntimeException e) {
            return new Extremizing(1.0, false);
        }
    }

    /**
     * One member of an ensemble.
     *
     * @param forecaster the forecaster
     * @param forecast its latest forecast
     */
    record Member(ForecasterEntity forecaster, ForecastEntity forecast) {}

    /**
     * Recomputes the ensemble of an open question and records it when it changed.
     *
     * @return the new ensemble forecast, or null when nothing was written
     */
    @Transactional
    public ForecastEntity refresh(ForecastQuestionEntity q) {
        Instant now = clock.instant();
        if (!q.acceptsForecasts(now)) {
            return null;
        }
        ForecasterEntity ensemble = forecasters.find(ForecasterRegistry.ENSEMBLE).orElse(null);
        if (ensemble == null || !ensemble.isActive()) {
            return null;
        }
        Map<String, ForecastEntity> latest = ledger.latestByForecaster(q.getId());
        List<Member> members = members(q, latest, ensemble, now);
        if (members.isEmpty()) {
            return null;
        }
        Map<String, ForecastCalibrationEntity> cal = scoring.calibrationMap();
        List<EnsembleMath.Track> tracks = new ArrayList<>();
        for (Member m : members) {
            ForecastCalibrationEntity c = cal.get(ForecastCalibrationEntity.key(m.forecaster().getId(),
                    q.getCategory()));
            tracks.add(c == null ? new EnsembleMath.Track(0.0, 0)
                    : new EnsembleMath.Track(Objects.requireNonNullElse(c.getRelBrier(), 0.0), c.getWeightN()));
        }
        double[] w = EnsembleMath.weights(tracks, ensemble.configNumber("eta", EnsembleMath.ETA),
                ensemble.configNumber("shrink", EnsembleMath.SHRINK));
        List<Map<String, Object>> memberViews = new ArrayList<>();
        boolean recalibrated = false;
        ForecastValue value;
        Map<String, Object> inputs = new LinkedHashMap<>();
        Extremizing ext = extremizing();
        double a = ext.enabled() ? ext.a() : 1.0;
        switch (q.getType()) {
            case Ledger.BINARY -> {
                double[] p = new double[members.size()];
                for (int i = 0; i < members.size(); i++) {
                    Member m = members.get(i);
                    double raw = m.forecast().getProbability();
                    double capped = "llm".equals(m.forecaster().getType()) ? EnsembleMath.capLlm(raw) : raw;
                    ForecastCalibrationEntity c = cal.get(ForecastCalibrationEntity.key(m.forecaster().getId(),
                            q.getCategory()));
                    double used = capped;
                    boolean platt = c != null && c.hasPlatt();
                    if (platt) {
                        used = EnsembleMath.platt(c.getPlattA(), c.getPlattB(), capped);
                        recalibrated = true;
                    }
                    p[i] = used;
                    Map<String, Object> view = memberView(m, w[i]);
                    view.put("raw", raw);
                    view.put("capped", capped);
                    view.put("recalibrated", platt ? used : null);
                    memberViews.add(view);
                }
                double x = EnsembleMath.aggregateLogit(p, w);
                inputs.put("aggregateLogit", x);
                value = ForecastValue.binary(EnsembleMath.combineBinary(p, w, a));
            }
            case Ledger.MULTIPLE, Ledger.ORDERED -> {
                List<Map<String, Double>> dists = new ArrayList<>();
                for (int i = 0; i < members.size(); i++) {
                    Member m = members.get(i);
                    Map<String, Double> d = m.forecast().getDistribution();
                    if ("llm".equals(m.forecaster().getType())) {
                        d = capDistribution(d);
                    }
                    dists.add(d);
                    memberViews.add(memberView(m, w[i]));
                }
                value = ForecastValue.distribution(EnsembleMath.combineDistribution(dists, w, q.labels()));
            }
            case Ledger.NUMERIC -> {
                List<Map<String, Double>> qs = new ArrayList<>();
                for (int i = 0; i < members.size(); i++) {
                    qs.add(members.get(i).forecast().getQuantiles());
                    memberViews.add(memberView(members.get(i), w[i]));
                }
                Map<String, Double> combined = EnsembleMath.combineQuantiles(qs, w);
                if (!combined.keySet().containsAll(Ledger.REQUIRED_QUANTILES)) {
                    return null;
                }
                value = ForecastValue.quantiles(combined);
            }
            default -> {
                return null;
            }
        }
        inputs.put("members", memberViews);
        inputs.put("weights", weightMap(members, w));
        inputs.put("extremizing", Map.of("a", a, "enabled", ext.enabled()));
        inputs.put("maxAgeDays", ensemble.configNumber("maxAgeDays", 7));
        inputs.put("llmEnsembleWeight", llmWeightAllowed() ? "allowed" : "degraded");
        ForecastEntity previous = latest.get(ForecasterRegistry.ENSEMBLE);
        if (previous != null && !changed(previous, value, memberViews)) {
            return null;
        }
        String rationale = "Track-record-weighted " + (Ledger.NUMERIC.equals(q.getType()) ? "quantile average"
                : "log-odds pool") + " of " + members.size() + " forecaster" + (members.size() == 1 ? "" : "s")
                + (recalibrated ? ", with Platt-recalibrated inputs" : "") + (ext.enabled() ? ", extremized (a = "
                + Math.round(a * 1000) / 1000.0 + ")" : "") + ".";
        return ledger.record(q, ensemble, value, rationale, List.of(), List.of(), inputs, null, recalibrated);
    }

    /** The members of a question's ensemble. */
    List<Member> members(ForecastQuestionEntity q, Map<String, ForecastEntity> latest, ForecasterEntity ensemble,
            Instant now) {
        Duration maxAge = Duration.ofSeconds((long) (ensemble.configNumber("maxAgeDays", 7) * 86_400));
        boolean youngQuestion = q.getOpensAt().isAfter(now.minus(maxAge));
        int externalMin = (int) ensemble.configNumber("externalMinResolved", 30);
        boolean llmAllowed = llmWeightAllowed();
        List<Member> out = new ArrayList<>();
        Map<String, Integer> resolved = null;
        for (ForecastEntity f : latest.values()) {
            if (ForecasterRegistry.ENSEMBLE.equals(f.getForecasterId())) {
                continue;
            }
            ForecasterEntity forecaster = forecasters.find(f.getForecasterId()).orElse(null);
            if (forecaster == null || !forecaster.isActive() || "ensemble".equals(forecaster.getType())) {
                continue;
            }
            if (!youngQuestion && f.getMadeAt().isBefore(now.minus(maxAge))) {
                continue;
            }
            if ("llm".equals(forecaster.getType()) && !llmAllowed) {
                continue;
            }
            if ("external".equals(forecaster.getType())) {
                ExternalForecasterCounts linked = externalCounts == null ? null : externalCounts.getIfAvailable();
                int count;
                if (linked != null && linked.handles(forecaster.getId())) {
                    // Prompt 138: an EDP forecaster earns its place on resolved linked questions only.
                    count = linked.resolvedLinked(forecaster.getId());
                } else {
                    if (resolved == null) {
                        resolved = resolvedCounts();
                    }
                    count = resolved.getOrDefault(forecaster.getId(), 0);
                }
                // Prompt 155: a named external forecaster may override the global externalMinResolved.
                int min = (int) forecaster.configNumber("externalMinResolved", externalMin);
                if (count < min) {
                    continue;
                }
            }
            boolean usable = switch (q.getType()) {
                case Ledger.BINARY -> f.getProbability() != null;
                case Ledger.MULTIPLE, Ledger.ORDERED -> f.getDistribution() != null;
                case Ledger.NUMERIC -> f.getQuantiles() != null;
                default -> false;
            };
            if (usable) {
                out.add(new Member(forecaster, f));
            }
        }
        out.sort(Comparator.comparing(m -> m.forecaster().getId()));
        return out;
    }

    /**
     * True when the 123 benchmark gate lets LLM forecasters carry ensemble weight: every component they depend on
     * passed or warned in its latest benchmark (true when the gate is absent).
     */
    boolean llmWeightAllowed() {
        ai.foresignal.benchmark.BenchmarkGate gate = benchmarkGate == null ? null : benchmarkGate.getIfAvailable();
        if (gate == null) {
            return true;
        }
        try {
            return gate.llmEnsembleWeightAllowed();
        } catch (RuntimeException e) {
            log.warn("The benchmark gate could not be read; LLM forecasters keep no ensemble weight: {}",
                    e.toString());
            return false;
        }
    }

    /** The resolved linked questions of EDP forecasters (prompt 138), when present. */
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.beans.factory.ObjectProvider<ExternalForecasterCounts> externalCounts;

    private Map<String, Integer> resolvedCounts() {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (ForecastScoreEntity s : scores.findAll()) {
            out.merge(s.getForecasterId(), 1, Integer::sum);
        }
        return out;
    }

    private static Map<String, Double> capDistribution(Map<String, Double> d) {
        Map<String, Double> out = new LinkedHashMap<>();
        double sum = 0;
        for (Map.Entry<String, Double> e : d.entrySet()) {
            double v = EnsembleMath.capLlm(e.getValue());
            out.put(e.getKey(), v);
            sum += v;
        }
        for (Map.Entry<String, Double> e : out.entrySet()) {
            e.setValue(e.getValue() / sum);
        }
        return out;
    }

    private static Map<String, Object> memberView(Member m, double weight) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("forecasterId", m.forecaster().getId());
        view.put("forecasterType", m.forecaster().getType());
        view.put("forecastId", m.forecast().getId());
        view.put("weight", weight);
        return view;
    }

    private static Map<String, Object> weightMap(List<Member> members, double[] w) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i < members.size(); i++) {
            out.put(members.get(i).forecaster().getId(), w[i]);
        }
        return out;
    }

    /** True when the members or the value moved enough to write a new ensemble forecast. */
    static boolean changed(ForecastEntity previous, ForecastValue value, List<Map<String, Object>> members) {
        Object before = previous.getInputs().get("members");
        Set<String> prevIds = new HashSet<>();
        if (before instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> m && m.get("forecastId") != null) {
                    prevIds.add(m.get("forecastId").toString());
                }
            }
        }
        Set<String> ids = new HashSet<>();
        for (Map<String, Object> m : members) {
            ids.add(String.valueOf(m.get("forecastId")));
        }
        if (!prevIds.equals(ids)) {
            return true;
        }
        if (value.probability() != null) {
            return previous.getProbability() == null
                    || Math.abs(previous.getProbability() - value.probability()) >= MIN_CHANGE;
        }
        Map<String, Double> now = value.distribution() != null ? value.distribution() : value.quantiles();
        Map<String, Double> was = value.distribution() != null ? previous.getDistribution() : previous.getQuantiles();
        if (was == null || now == null || !was.keySet().equals(now.keySet())) {
            return true;
        }
        for (Map.Entry<String, Double> e : now.entrySet()) {
            double a = e.getValue();
            double b = was.get(e.getKey());
            double scale = Math.max(1e-9, Math.max(Math.abs(a), Math.abs(b)));
            if (value.distribution() != null ? Math.abs(a - b) >= MIN_CHANGE : Math.abs(a - b) / scale >= MIN_CHANGE) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------- refit

    /**
     * The monthly check ({@code EnsembleRefitResult}): relative Brier scores and weights per category, Platt fits
     * (≥ {@value #PLATT_MIN} resolved per forecaster and category) and the extremizing gate.
     */
    @Transactional
    public Map<String, Object> refit() {
        Instant now = clock.instant();
        List<ForecastScoreEntity> rows = scoring.rows();
        Map<String, List<ForecastScoreEntity>> byCategory = new LinkedHashMap<>();
        for (ForecastScoreEntity s : rows) {
            byCategory.computeIfAbsent(s.getCategory(), k -> new ArrayList<>()).add(s);
        }
        Map<String, ForecastCalibrationEntity> existing = scoring.calibrationMap();
        List<Map<String, Object>> plattViews = new ArrayList<>();
        List<Map<String, Object>> weightViews = new ArrayList<>();
        for (Map.Entry<String, List<ForecastScoreEntity>> e : byCategory.entrySet()) {
            String category = e.getKey();
            List<ForecastScoreEntity> list = e.getValue();
            Map<String, double[]> rel = relativeBrier(list);
            Map<String, List<ForecastScoreEntity>> byForecaster = new LinkedHashMap<>();
            for (ForecastScoreEntity s : list) {
                byForecaster.computeIfAbsent(s.getForecasterId(), k -> new ArrayList<>()).add(s);
            }
            for (Map.Entry<String, List<ForecastScoreEntity>> f : byForecaster.entrySet()) {
                String forecasterId = f.getKey();
                if (ForecasterRegistry.ENSEMBLE.equals(forecasterId)) {
                    continue;
                }
                String key = ForecastCalibrationEntity.key(forecasterId, category);
                ForecastCalibrationEntity c = existing.getOrDefault(key, new ForecastCalibrationEntity(forecasterId,
                        category, now));
                double[] r = rel.get(forecasterId);
                c.setRelBrier(r == null ? null : r[0], r == null ? 0 : (int) r[1], now);
                List<double[]> pairs = new ArrayList<>();
                boolean llm = forecasters.find(forecasterId).map(x -> "llm".equals(x.getType())).orElse(false);
                for (ForecastScoreEntity s : f.getValue()) {
                    if (s.getFinalProbability() != null && s.getObserved() != null) {
                        double p = llm ? EnsembleMath.capLlm(s.getFinalProbability()) : s.getFinalProbability();
                        pairs.add(new double[] {p, s.getObserved()});
                    }
                }
                if (pairs.size() >= PLATT_MIN) {
                    double[] p = pairs.stream().mapToDouble(x -> x[0]).toArray();
                    double[] o = pairs.stream().mapToDouble(x -> x[1]).toArray();
                    double[] ab = EnsembleMath.fitPlatt(p, o);
                    c.setPlatt(ab[0], ab[1], pairs.size(), now);
                    Map<String, Object> pv = new LinkedHashMap<>();
                    pv.put("forecasterId", forecasterId);
                    pv.put("category", category);
                    pv.put("a", ab[0]);
                    pv.put("b", ab[1]);
                    pv.put("n", pairs.size());
                    plattViews.add(pv);
                } else {
                    c.setPlatt(null, null, pairs.size(), now);
                }
                calibrations.save(c);
            }
        }
        calibrations.flush();
        for (String category : byCategory.keySet()) {
            Map<String, Double> weights = scoring.weights(category);
            weights.forEach((id, w) -> {
                ForecastCalibrationEntity c = calibrations.findById(ForecastCalibrationEntity.key(id, category))
                        .orElse(null);
                Map<String, Object> wv = new LinkedHashMap<>();
                wv.put("forecasterId", id);
                wv.put("category", category);
                wv.put("weight", w);
                wv.put("n", c == null ? 0 : c.getWeightN());
                weightViews.add(wv);
            });
        }
        Map<String, Object> extremizing = extremizingGate(rows, now);
        settings.put(SettingKeys.FORECAST_ENSEMBLE, extremizing);
        scoring.invalidate();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fittedAt", now.toString());
        Map<String, Object> ext = new LinkedHashMap<>(extremizing);
        ext.remove("fittedAt");
        out.put("extremizing", ext);
        out.put("platt", plattViews);
        out.put("weights", weightViews);
        log.info("Ensemble refit: {} Platt fits, {} weights, extremizing {}", plattViews.size(), weightViews.size(),
                ext);
        return out;
    }

    /**
     * The relative Brier score of each forecaster over the last {@value #WEIGHT_WINDOW} resolved questions of one
     * category: the mean of its Brier score minus the question's median Brier score (forecasters other than the
     * ensemble).
     *
     * @return forecaster → {@code {relBrier, n}}
     */
    static Map<String, double[]> relativeBrier(List<ForecastScoreEntity> categoryRows) {
        Map<String, List<ForecastScoreEntity>> byQuestion = new LinkedHashMap<>();
        Map<String, Instant> resolvedAt = new LinkedHashMap<>();
        for (ForecastScoreEntity s : categoryRows) {
            if (s.getBrier() == null || ForecasterRegistry.ENSEMBLE.equals(s.getForecasterId())) {
                continue;
            }
            byQuestion.computeIfAbsent(s.getQuestionId(), k -> new ArrayList<>()).add(s);
            resolvedAt.put(s.getQuestionId(), s.getResolvedAt());
        }
        List<String> ids = new ArrayList<>(byQuestion.keySet());
        ids.sort(Comparator.comparing((String id) -> resolvedAt.get(id)).thenComparing(id -> id));
        if (ids.size() > WEIGHT_WINDOW) {
            ids = ids.subList(ids.size() - WEIGHT_WINDOW, ids.size());
        }
        Map<String, double[]> sums = new LinkedHashMap<>();
        for (String id : ids) {
            List<ForecastScoreEntity> list = byQuestion.get(id);
            double[] b = list.stream().mapToDouble(ForecastScoreEntity::getBrier).sorted().toArray();
            double median = b.length % 2 == 1 ? b[b.length / 2] : (b[b.length / 2 - 1] + b[b.length / 2]) / 2;
            for (ForecastScoreEntity s : list) {
                double[] acc = sums.computeIfAbsent(s.getForecasterId(), k -> new double[2]);
                acc[0] += s.getBrier() - median;
                acc[1]++;
            }
        }
        Map<String, double[]> out = new LinkedHashMap<>();
        sums.forEach((id, acc) -> out.put(id, new double[] {acc[0] / acc[1], acc[1]}));
        return out;
    }

    /** The extremizing gate over the resolved binary questions with an ensemble forecast. */
    Map<String, Object> extremizingGate(List<ForecastScoreEntity> rows, Instant now) {
        List<Object[]> samples = new ArrayList<>(); // {resolvedAt, x, o}
        for (ForecastScoreEntity s : rows) {
            if (!ForecasterRegistry.ENSEMBLE.equals(s.getForecasterId()) || s.getObserved() == null) {
                continue;
            }
            ForecastQuestionEntity q = questions.findById(s.getQuestionId()).orElse(null);
            if (q == null || !ForecastStatus.RESOLVED.equals(q.getStatus())) {
                continue;
            }
            Instant end = QuestionScorer.endOf(q);
            ForecastEntity last = null;
            for (ForecastEntity f : ledger.forecastsOf(q.getId())) {
                if (ForecasterRegistry.ENSEMBLE.equals(f.getForecasterId()) && !f.getMadeAt().isAfter(end)) {
                    last = f;
                }
            }
            Object x = last == null ? null : last.getInputs().get("aggregateLogit");
            if (x instanceof Number n) {
                samples.add(new Object[] {s.getResolvedAt(), n.doubleValue(), s.getObserved()});
            }
        }
        samples.sort(Comparator.comparing(o -> (Instant) o[0]));
        Set<String> types = new HashSet<>();
        for (ForecasterEntity f : forecasters.all()) {
            if (f.isActive() && !"ensemble".equals(f.getType())) {
                types.add(f.getType());
            }
        }
        double a = 1.0;
        Double improvement = null;
        boolean enabled = false;
        int n = samples.size();
        if (n >= 4) {
            int half = n / 2;
            double[] xTrain = new double[half];
            double[] oTrain = new double[half];
            double[] xTest = new double[n - half];
            double[] oTest = new double[n - half];
            for (int i = 0; i < n; i++) {
                double x = (Double) samples.get(i)[1];
                double o = (Double) samples.get(i)[2];
                if (i < half) {
                    xTrain[i] = x;
                    oTrain[i] = o;
                } else {
                    xTest[i - half] = x;
                    oTest[i - half] = o;
                }
            }
            a = EnsembleMath.fitExtremizing(xTrain, oTrain);
            improvement = EnsembleMath.meanLogScore(xTest, oTest, 1.0) - EnsembleMath.meanLogScore(xTest, oTest, a);
            enabled = gate(n, types.size(), improvement, a);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("a", enabled ? a : 1.0);
        out.put("fittedA", a);
        out.put("enabled", enabled);
        out.put("nResolutions", n);
        out.put("distinctForecasterTypes", types.size());
        out.put("logScoreImprovement", improvement);
        out.put("fittedAt", now.toString());
        return out;
    }

    /** The extremizing gate: ≥ 100 resolutions, ≥ 5 distinct types, a positive out-of-sample gain and {@code a > 1}. */
    static boolean gate(int resolutions, int distinctTypes, Double improvement, double a) {
        return resolutions >= EXTREMIZE_MIN && distinctTypes >= EXTREMIZE_TYPES && improvement != null
                && improvement > 0 && a > 1.0;
    }

    /** Refreshes the ensemble of every open question (daily, so stale members drop out); returns how many changed. */
    public int refreshAll() {
        int n = 0;
        for (ForecastQuestionEntity q : questions.findByStatusInOrderByResolveByAsc(List.of(ForecastStatus.OPEN))) {
            try {
                if (refresh(q) != null) {
                    n++;
                }
            } catch (RuntimeException e) {
                log.warn("Ensemble refresh of question {} failed: {}", q.getId(), e.toString());
            }
        }
        return n;
    }
}
