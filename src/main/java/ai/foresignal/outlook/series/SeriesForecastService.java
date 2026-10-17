package ai.foresignal.outlook.series;

import ai.foresignal.analytics.AnalyticsSupport;
import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingSeverities;
import ai.foresignal.analytics.registry.AnalysisSeriesEntity;
import ai.foresignal.analytics.registry.AnalysisSeriesRepository;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * The series forecaster (prompt 126, {@code series-forecast} 1.0.0, Intelligence-Design §9.1).
 *
 * <ul>
 *   <li><b>Eligible series</b>: enabled analysis-series rows whose {@code detectors} include {@code forecast}, of
 *       daily, weekly or hourly frequency (hourly series are summed or averaged to days).
 *   <li><b>Selection</b> ({@link #select}): the rolling-origin backtest of {@link MethodSelector} over the history
 *       known at the selection time; stored in {@code series_forecast_model} with a 116 manifest. A series without
 *       a model is selected on its first nightly run; re-selection is a step of the monthly maintenance of prompt 128
 *       ({@link #reselectAll}), which replaced the 30-day age rule.
 *   <li><b>Forecasts</b> ({@link #forecast}, nightly after the change-point run): horizons 1, 7 and 28 days, the
 *       nine quantiles from the selected method (monotone), stored in {@code series_forecast} with the seq read up
 *       to and a manifest.
 *   <li><b>Ledger</b>: for every ref in {@code analysis.forecasts.questionSeries} (default: the PortWatch Bab
 *       el-Mandeb, Hormuz and Suez transits and COT WTI managed-money net), one numeric {@code series-value}
 *       question per horizon and target period, category {@code markets} or {@code activity}, forecaster
 *       {@value #FORECASTER}; the 122 {@code climatology} and {@code persistence} baselines forecast them
 *       automatically when the question opens.
 *   <li><b>Exceedance</b> ({@link #checkExceedances}): a realized value outside the 1-step p05–p95 band becomes a
 *       {@code forecast-exceedance} finding, {@code notable}, or {@code significant} outside p01–p99 of the same
 *       predictive distribution; a fall of counts the sources could not have seen is {@code info} with
 *       {@code LOW_DETECTABILITY}.
 * </ul>
 */
@Service
public class SeriesForecastService {

    private static final Logger log = LoggerFactory.getLogger(SeriesForecastService.class);

    /** The forecaster name. */
    public static final String NAME = "series-forecast";

    /** The forecaster version. */
    public static final String VERSION = "1.0.0";

    /** The registry detector value. */
    public static final String DETECTOR = "forecast";

    /** The ledger forecaster of Foresignal's statistical forecasts. */
    public static final String FORECASTER = "statistical";

    /** The method age, in days, the monthly re-selection keeps (prompt 128 re-selects every eligible series monthly). */
    public static final int RESELECT_DAYS = 30;

    /** Days a 1-step target may stay unobserved before its exceedance check is given up. */
    static final int CHECK_GIVE_UP_DAYS = 14;

    /** The default ledger series. */
    public static final List<String> DEFAULT_QUESTION_SERIES = List.of(
            "series:imf-portwatch-chokepoints/chokepoint4", "series:imf-portwatch-chokepoints/chokepoint6",
            "series:imf-portwatch-chokepoints/chokepoint1", "series:cftc-cot/cot.067651.disaggregated.mm_net");

    private final AnalysisSeriesRepository registry;
    private final SeriesReader reader;
    private final SeriesForecastModelRepository models;
    private final SeriesForecastRepository forecasts;
    private final FindingService findings;
    private final AnalyticsSupport support;
    private final AppSettingService settings;
    private final ObjectProvider<ManifestService> manifests;
    private final ObjectProvider<ForecastService> ledger;
    private final ObjectProvider<ForecasterRegistry> forecasters;
    private final java.time.Clock clock;

    /** Creates the service. */
    public SeriesForecastService(AnalysisSeriesRepository registry, SeriesReader reader,
            SeriesForecastModelRepository models, SeriesForecastRepository forecasts, FindingService findings,
            AnalyticsSupport support, AppSettingService settings, ObjectProvider<ManifestService> manifests,
            ObjectProvider<ForecastService> ledger, ObjectProvider<ForecasterRegistry> forecasters,
            java.time.Clock clock) {
        this.registry = registry;
        this.reader = reader;
        this.models = models;
        this.forecasts = forecasts;
        this.findings = findings;
        this.support = support;
        this.settings = settings;
        this.manifests = manifests;
        this.ledger = ledger;
        this.forecasters = forecasters;
        this.clock = clock;
    }

    /**
     * What a nightly run did.
     *
     * @param series series evaluated
     * @param selected methods (re-)selected
     * @param forecasts forecast rows written
     * @param questions ledger questions opened or forecast
     * @param exceedances {@code forecast-exceedance} findings written
     * @param errors series that failed
     */
    public record RunResult(int series, int selected, int forecasts, int questions, int exceedances, int errors) {}

    /**
     * A series read for forecasting.
     *
     * @param data the series (days for hourly series)
     * @param frequency the forecasting frequency ({@code day} or {@code week})
     * @param grid the grid as known at the decision time
     * @param counts true for a count series
     * @param season the season length in buckets
     * @param bucketDays 1 or 7
     * @param origin the first bucket not in the history (the start of the current bucket)
     */
    record History(SeriesData data, SeriesFrequency frequency, SeriesData.Grid grid, boolean counts, int season,
            int bucketDays, Instant origin) {}

    // ------------------------------------------------------------------------- the nightly run

    /** The ledger series ({@code analysis.forecasts.questionSeries}, else the defaults). */
    public List<String> questionSeries() {
        try {
            Optional<String[]> configured = settings.get(SettingKeys.ANALYSIS_FORECASTS_QUESTION_SERIES,
                    String[].class);
            if (configured.isPresent()) {
                return List.of(configured.get());
            }
        } catch (RuntimeException e) {
            log.debug("analysis.forecasts.questionSeries is unreadable: {}", e.toString());
        }
        return DEFAULT_QUESTION_SERIES;
    }

    /** True when a registry row asks for forecasts and has a forecastable frequency. */
    static boolean eligible(AnalysisSeriesEntity row) {
        return row.isEnabled() && row.getDetectors().contains(DETECTOR)
                && List.of("day", "week", "hour").contains(row.getFrequency());
    }

    /** Runs the nightly forecasts and exceedance checks at {@code now}. */
    public RunResult runNightly(Instant now) {
        int series = 0;
        int selected = 0;
        int written = 0;
        int questions = 0;
        int errors = 0;
        List<String> ledgerRefs = questionSeries();
        for (AnalysisSeriesEntity row : registry.findByEnabledTrueOrderByRefAsc()) {
            if (!eligible(row)) {
                continue;
            }
            series++;
            try {
                SeriesForecastModelEntity model = models.findById(row.getRef()).orElse(null);
                if (model == null) {
                    model = select(row.getRef(), now);
                    selected++;
                }
                List<SeriesForecastEntity> rows = forecast(row.getRef(), now, model);
                written += rows.size();
                if (ledgerRefs.contains(row.getRef())) {
                    questions += openQuestions(rows, now);
                }
            } catch (RuntimeException e) {
                errors++;
                log.warn("Series forecast of {} failed: {}", row.getRef(), e.toString());
            }
        }
        int exceedances = checkExceedances(now);
        return new RunResult(series, selected, written, questions, exceedances, errors);
    }

    // ------------------------------------------------------------------------- selection

    /**
     * Re-selects the method of every eligible series at {@code now} (the monthly maintenance step
     * {@code series-method-reselection}, prompt 128).
     *
     * @return {@code selected}, {@code notReady} and {@code errors} counts
     */
    public Map<String, Object> reselectAll(Instant now) {
        int selected = 0;
        int notReady = 0;
        int errors = 0;
        for (AnalysisSeriesEntity row : registry.findByEnabledTrueOrderByRefAsc()) {
            if (!eligible(row)) {
                continue;
            }
            try {
                select(row.getRef(), now);
                selected++;
            } catch (ConflictException e) {
                notReady++;
            } catch (RuntimeException e) {
                errors++;
                log.warn("Method re-selection of {} failed: {}", row.getRef(), e.toString());
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("selected", selected);
        out.put("notReady", notReady);
        out.put("errors", errors);
        return out;
    }

    /**
     * Selects the method of a series by rolling-origin backtest at {@code now} and stores it.
     *
     * @throws ConflictException 409 {@code FORECAST_MODEL_NOT_READY} when no method can be scored yet
     */
    public SeriesForecastModelEntity select(String ref, Instant now) {
        History h = load(SeriesRef.parse(ref), now);
        MethodSelector.Selection selection = MethodSelector.select(h.grid().starts(), h.grid().values(),
                h.grid().knownAt(), now, h.counts(), h.season(), h.bucketDays());
        if (selection.method() == null) {
            throw new ConflictException(ErrorCodes.FORECAST_MODEL_NOT_READY, "The series " + ref
                    + " has too little history for a rolling-origin backtest.");
        }
        double[] history = SeriesPrep.fill(h.grid().values(), h.season());
        Map<String, Object> params = new LinkedHashMap<>();
        try {
            params.putAll(MethodSelector.method(selection.method()).forecast(history, h.season(), h.counts(),
                    new int[] {1}).params());
        } catch (RuntimeException e) {
            params.put("error", e.getMessage());
        }
        SeriesForecastModelEntity model = models.findById(ref)
                .orElseGet(() -> new SeriesForecastModelEntity(ref, selection.method(), now, versionLabel()));
        model.setMethod(selection.method());
        model.setParams(params);
        model.setBacktest(selection.backtest());
        model.setSelectedAt(now);
        model.setVersion(versionLabel());
        model.setCounts(h.counts());
        model.setSeason(h.season());
        model.setBucketDays(h.bucketDays());
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("methods", MethodSelector.METHODS.stream().map(ForecastMethod::id).toList());
        config.put("origins", MethodSelector.ORIGINS);
        config.put("selectionHorizon", MethodSelector.SELECTION_HORIZON);
        config.put("season", h.season());
        config.put("counts", h.counts());
        model.setManifestId(manifest(NAME + "-selection", config, h.data().asOfSeq(), ref,
                Map.of("ref", ref, "selectedAt", now.toString(), "method", selection.method())));
        return models.save(model);
    }

    /** {@code series-forecast 1.0.0}. */
    public static String versionLabel() {
        return NAME + " " + VERSION;
    }

    // ------------------------------------------------------------------------- forecasts

    /** Forecasts a series at {@code now} with its selected method (selecting one first when missing). */
    public List<SeriesForecastEntity> forecast(String ref, Instant now) {
        SeriesForecastModelEntity model = models.findById(ref).orElse(null);
        if (model == null) {
            model = select(ref, now);
        }
        return forecast(ref, now, model);
    }

    List<SeriesForecastEntity> forecast(String ref, Instant now, SeriesForecastModelEntity model) {
        History h = load(SeriesRef.parse(ref), now);
        double[] history = SeriesPrep.fill(h.grid().values(), h.season());
        ForecastMethod method = MethodSelector.method(model.getMethod());
        if (history.length < method.minHistory(h.season())) {
            method = new SeasonalNaive();
        }
        int[] steps = MethodSelector.steps(h.bucketDays());
        ForecastMethod.MethodForecast f = method.forecast(history, h.season(), h.counts(),
                MethodSelector.distinct(steps));
        Map<String, Double> standardization = standardization(history);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("method", f.method());
        config.put("params", f.params());
        config.put("horizons", Arrays.stream(MethodSelector.HORIZONS).boxed().toList());
        config.put("levels", Arrays.stream(QuantileSet.LEVELS).boxed().toList());
        String manifestId = manifest(NAME, config, h.data().asOfSeq(), ref, Map.of("ref", ref, "origin",
                h.origin().toString()));
        List<SeriesForecastEntity> out = new ArrayList<>();
        for (int i = 0; i < MethodSelector.HORIZONS.length; i++) {
            int horizon = MethodSelector.HORIZONS[i];
            Instant targetStart = h.frequency().plus(h.origin(), steps[i] - 1);
            Instant targetEnd = h.frequency().next(targetStart);
            if (!forecasts.findByRefAndHorizonAndTargetStart(ref, horizon, targetStart).isEmpty()) {
                continue;
            }
            double[] q = f.quantiles().get(steps[i]);
            SeriesForecastEntity row = new SeriesForecastEntity(Ulids.next(now), ref, now, h.data().asOfSeq(),
                    horizon, targetStart, targetEnd, f.method(), versionLabel());
            row.setQuantiles(QuantileSet.ledger(q));
            row.setTails(QuantileSet.tails(q));
            row.setStandardization(standardization);
            row.setUnit(h.data().unit());
            row.setLocalOnly(h.data().localOnly());
            row.setManifestId(manifestId);
            out.add(forecasts.save(row));
        }
        return out;
    }

    /** The mean and standard deviation of the last year of history (for standardized units). */
    static Map<String, Double> standardization(double[] history) {
        int from = Math.max(0, history.length - 365);
        double sum = 0;
        int n = 0;
        for (int i = from; i < history.length; i++) {
            sum += history[i];
            n++;
        }
        double mean = n == 0 ? 0 : sum / n;
        double ss = 0;
        for (int i = from; i < history.length; i++) {
            ss += (history[i] - mean) * (history[i] - mean);
        }
        double sd = n > 1 ? Math.sqrt(ss / (n - 1)) : 0;
        Map<String, Double> out = new LinkedHashMap<>();
        out.put("mean", QuantileSet.round(mean));
        out.put("sd", QuantileSet.round(sd));
        return out;
    }

    /** Reads a series for forecasting, as known at {@code now}. */
    History load(SeriesRef ref, Instant now) {
        SeriesReader.Meta meta = reader.meta(ref);
        SeriesFrequency native_ = meta.frequency();
        SeriesFrequency frequency = native_ == SeriesFrequency.HOUR ? SeriesFrequency.DAY : native_;
        if (frequency != SeriesFrequency.DAY && frequency != SeriesFrequency.WEEK) {
            throw new ConflictException(ErrorCodes.FORECAST_MODEL_NOT_READY, "The series " + ref.format() + " is "
                    + native_.wire() + "; only daily, weekly and hourly series are forecast.");
        }
        Instant origin = frequency.floor(now);
        int buckets = frequency == SeriesFrequency.DAY ? 1_100 : 260;
        Instant from = frequency.plus(origin, -buckets);
        if (native_ == SeriesFrequency.HOUR) {
            from = origin.minus(Duration.ofDays(400));
        }
        SeriesData data = reader.read(ref, from, origin, now);
        if (data.frequency() != frequency) {
            data = data.resample(frequency);
        }
        SeriesData.Grid grid = data.grid().knownBy(now);
        boolean counts = data.counts();
        int usable = SeriesPrep.usable(grid.values());
        int season = frequency == SeriesFrequency.DAY ? 7 : usable >= 156 ? 52 : 1;
        return new History(data, frequency, grid, counts, season, frequency == SeriesFrequency.DAY ? 1 : 7, origin);
    }

    // ------------------------------------------------------------------------- ledger

    /** Opens (or reuses) the ledger question of each forecast row and records the statistical forecast. */
    int openQuestions(List<SeriesForecastEntity> rows, Instant now) {
        ForecastService service = ledger.getIfAvailable();
        ForecasterRegistry registryBean = forecasters.getIfAvailable();
        if (service == null || registryBean == null) {
            return 0;
        }
        ForecasterEntity statistical = ensureForecaster(registryBean);
        int n = 0;
        for (SeriesForecastEntity row : rows) {
            try {
                SeriesRef ref = SeriesRef.parse(row.getRef());
                Map<String, Object> resolver = new LinkedHashMap<>();
                resolver.put("type", "series-value");
                resolver.put("ref", row.getRef());
                resolver.put("window", Map.of("start", row.getTargetStart().toString(), "end",
                        row.getTargetEnd().toString()));
                resolver.put("aggregate", "mean");
                Map<String, Object> validated = ResolverSpec.validate(resolver, Ledger.NUMERIC);
                Instant closesAt = row.getTargetStart().isAfter(now.plus(Duration.ofHours(1))) ? row.getTargetStart()
                        : now.plus(Duration.ofHours(1));
                Instant resolveBy = row.getTargetEnd().isBefore(closesAt) ? closesAt : row.getTargetEnd();
                Map<String, Object> outcomes = new LinkedHashMap<>();
                if (row.getUnit() != null && !row.getUnit().isBlank()) {
                    outcomes.put("unit", row.getUnit());
                }
                String day = row.getTargetStart().atZone(ZoneOffset.UTC).toLocalDate().toString();
                String label = ai.foresignal.analytics.AnalyticsSupport.label(ref);
                ForecastInputs.QuestionDraft draft = new ForecastInputs.QuestionDraft(
                        "What will " + label + " be for the period starting " + day + "?", Ledger.NUMERIC, outcomes,
                        "The mean of " + row.getRef() + " over [" + row.getTargetStart() + ", " + row.getTargetEnd()
                                + "), as read by the series-value resolver after the release lag.",
                        validated, now, closesAt, resolveBy, category(ref), List.of("series-forecast",
                                "horizon-" + row.getHorizon()), Map.of("seriesRef", row.getRef()),
                        "series-forecast:" + row.getRef() + ":" + row.getHorizon() + ":" + row.getTargetStart(),
                        false);
                ForecastQuestionEntity q = service.openQuestion(draft, "system", NAME).question();
                row.setQuestionId(q.getId());
                forecasts.save(row);
                if (q.acceptsForecasts(clock.instant())) {
                    Map<String, Object> inputs = new LinkedHashMap<>();
                    inputs.put("seriesForecastId", row.getId());
                    inputs.put("method", row.getMethod());
                    inputs.put("horizon", row.getHorizon());
                    inputs.put("asOfSeq", row.getAsofSeq());
                    inputs.put("manifestId", row.getManifestId());
                    service.record(q, statistical, ForecastInputs.ForecastValue.quantiles(row.getQuantiles()),
                            "Statistical: " + row.getMethod() + " (selected by rolling-origin backtest), "
                                    + row.getHorizon() + "-day horizon.",
                            List.of(Map.of("ref", row.getRef())),
                            null, inputs, null, false);
                }
                n++;
            } catch (RuntimeException e) {
                log.warn("The ledger question of series forecast {} failed: {}", row.getId(), e.toString());
            }
        }
        return n;
    }

    /** The ledger forecaster {@value #FORECASTER}, registered on first use. */
    public static ForecasterEntity ensureForecaster(ForecasterRegistry registryBean) {
        return registryBean.ensure(FORECASTER, FORECASTER, "Statistical (series forecaster and regional outlooks)",
                VERSION, Map.of("seriesForecast", versionLabel(), "regionalOutlook", "regional-outlook 1.0.0"));
    }

    /** The ledger category of a series: {@code markets} for market series, else {@code activity}. */
    String category(SeriesRef ref) {
        if ("cftc-cot".equals(ref.sourceId()) || "fred-series".equals(ref.sourceId())
                || "eia-petroleum".equals(ref.sourceId())) {
            return "markets";
        }
        try {
            SeriesReader.Meta meta = reader.meta(ref);
            if (ai.foresignal.analytics.AnalyticsSupport.MARKET_FAMILIES.contains(meta.seriesFamily())
                    || "markets".equals(meta.signalFamily())) {
                return "markets";
            }
        } catch (RuntimeException e) {
            // unknown series: activity
        }
        return "activity";
    }

    // ------------------------------------------------------------------------- exceedance

    /** Compares realized values with the 1-step bands whose target period has ended; returns findings written. */
    public int checkExceedances(Instant now) {
        int written = 0;
        for (SeriesForecastEntity row : forecasts
                .findByExceedanceCheckedFalseAndHorizonAndTargetEndLessThanEqualOrderByIdAsc(1, now)) {
            try {
                Double actual = actual(row, now);
                if (actual == null) {
                    if (now.isAfter(row.getTargetEnd().plus(Duration.ofDays(CHECK_GIVE_UP_DAYS)))) {
                        row.setExceedanceChecked(true);
                        forecasts.save(row);
                    }
                    continue;
                }
                FindingService.Outcome outcome = exceedance(row, actual, now);
                row.setExceedanceChecked(true);
                if (outcome != null) {
                    row.setFindingId(outcome.finding().getId());
                    written++;
                }
                forecasts.save(row);
            } catch (RuntimeException e) {
                log.warn("Exceedance check of {} failed: {}", row.getId(), e.toString());
            }
        }
        return written;
    }

    /** The realized value of a forecast's target period known at {@code now}, or null. */
    Double actual(SeriesForecastEntity row, Instant now) {
        SeriesRef ref = SeriesRef.parse(row.getRef());
        SeriesData data = reader.read(ref, row.getTargetStart(), row.getTargetEnd(), now);
        SeriesFrequency target = Duration.between(row.getTargetStart(), row.getTargetEnd()).toDays() >= 7
                ? SeriesFrequency.WEEK : SeriesFrequency.DAY;
        if (data.frequency() != target) {
            data = data.resample(target);
        }
        SeriesData.Grid grid = data.grid().knownBy(now);
        for (int i = 0; i < grid.size(); i++) {
            if (grid.starts()[i].equals(row.getTargetStart()) && !grid.gap()[i]) {
                return grid.values()[i];
            }
        }
        return null;
    }

    /** Records the {@code forecast-exceedance} finding of a realized value, or null inside the band. */
    FindingService.Outcome exceedance(SeriesForecastEntity row, double actual, Instant now) {
        double[] q = QuantileSet.fromMaps(row.getQuantiles(), row.getTails());
        boolean above = actual > q[QuantileSet.P95];
        boolean below = actual < q[QuantileSet.P05];
        if (!above && !below) {
            return null;
        }
        boolean extreme = actual > q[QuantileSet.P99] || actual < q[QuantileSet.P01];
        SeriesRef ref = SeriesRef.parse(row.getRef());
        SeriesData data = reader.read(ref, row.getTargetStart(), row.getTargetEnd(), now);
        String severity = extreme ? FindingSeverities.SIGNIFICANT : FindingSeverities.NOTABLE;
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(FindingCaveats.notGroundTruth());
        if (below && data.counts()) {
            AbsenceCheck check = support.canSeeDecline(data, row.getTargetStart(), row.getTargetEnd());
            if (check != null && !check.ok()) {
                severity = FindingSeverities.INFO;
                caveats.add(FindingCaveats.of(FindingCaveats.LOW_DETECTABILITY, "The sources could not have seen a"
                        + " fall reliably (" + check.reason() + "); a low count is not read as low activity."));
            }
        }
        if (AnalyticsSupport.market(data)) {
            caveats.add(FindingCaveats.dailyResolutionOnly());
        }
        double sd = row.getStandardization().getOrDefault("sd", 0.0);
        double mean = row.getStandardization().getOrDefault("mean", 0.0);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("forecastId", row.getId());
        stats.put("method", row.getMethod());
        stats.put("horizon", row.getHorizon());
        stats.put("direction", above ? "above" : "below");
        stats.put("band", extreme ? "outside-p01-p99" : "outside-p05-p95");
        stats.put("actual", QuantileSet.round(actual));
        stats.put("p01", q[QuantileSet.P01]);
        stats.put("p05", q[QuantileSet.P05]);
        stats.put("p50", q[QuantileSet.P50]);
        stats.put("p95", q[QuantileSet.P95]);
        stats.put("p99", q[QuantileSet.P99]);
        stats.put("standardizedActual", sd > 0 ? QuantileSet.round((actual - mean) / sd) : null);
        Map<String, Object> baseRate = new LinkedHashMap<>();
        baseRate.put("value", extreme ? 0.02 : 0.10);
        baseRate.put("unit", "probability");
        baseRate.put("method", "nominal-interval-miss");
        baseRate.put("text", "a calibrated " + (extreme ? "p01–p99" : "p05–p95") + " band misses "
                + (extreme ? "2%" : "10%") + " of periods by construction; the backtest coverage of this series is"
                + " in GET /v1/analytics/series/forecast/model");
        boolean localOnly = data.localOnly();
        String label = AnalyticsSupport.label(ref);
        String explanation = label + " came in " + (above ? "above" : "below") + " the 1-day "
                + (extreme ? "p01–p99" : "p05–p95") + " band of the " + row.getMethod() + " forecast for the period"
                + " starting " + row.getTargetStart()
                + (localOnly ? "" : " (actual " + AnalyticsSupport.num(actual) + ", band "
                        + AnalyticsSupport.num(above ? q[QuantileSet.P95] : q[QuantileSet.P05]) + ")") + ".";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("method", row.getMethod());
        params.put("horizon", 1);
        List<String> regions = data.regionCode() == null ? List.of() : List.of(data.regionCode());
        return findings.record(new FindingDraft("forecast-exceedance", "Forecast exceedance: " + label + " "
                + (above ? "above" : "below") + " its band", severity, null, regions, row.getTargetStart(),
                row.getTargetEnd(), now, List.of(row.getRef()), List.of(), data.asOfSeq(), stats, baseRate, caveats,
                NAME, VERSION, params, explanation, support.privateSeries(data), data.signalFamily(),
                support.localOnlySources(data), localOnly ? List.of("actual", "p01", "p05", "p50", "p95", "p99")
                        : List.of(), row.getRef() + "|" + row.getTargetStart()));
    }

    // ------------------------------------------------------------------------- reads

    /** The model of a series, or 409 {@code FORECAST_MODEL_NOT_READY}. */
    public SeriesForecastModelEntity model(String ref) {
        return models.findById(ref).orElseThrow(() -> notReady(ref));
    }

    /** The latest forecasts of a series (one per horizon), or 409 {@code FORECAST_MODEL_NOT_READY}. */
    public List<SeriesForecastEntity> latest(String ref) {
        SeriesForecastEntity newest = forecasts.findTop1ByRefOrderByMadeAtDescIdDesc(ref).orElseThrow(
                () -> notReady(ref));
        return forecasts.findByRefAndMadeAtOrderByHorizonAsc(ref, newest.getMadeAt());
    }

    /** The ledger questions opened for a series, newest first. */
    public List<String> questionIds(String ref) {
        List<String> out = new ArrayList<>();
        for (SeriesForecastEntity row : forecasts.findByRefAndQuestionIdIsNotNullOrderByMadeAtDesc(ref)) {
            if (!out.contains(row.getQuestionId())) {
                out.add(row.getQuestionId());
            }
            if (out.size() >= 30) {
                break;
            }
        }
        return out;
    }

    private static ConflictException notReady(String ref) {
        return new ConflictException(ErrorCodes.FORECAST_MODEL_NOT_READY, "No forecast method has been selected for "
                + ref + " yet.");
    }

    private String manifest(String component, Map<String, Object> config, long asOfSeq, String ref,
            Map<String, Object> query) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            return service.record(ManifestRequest.of(component, VERSION).config(config).asOf(asOfSeq)
                    .inputs(List.of(ref)).query(query));
        } catch (RuntimeException e) {
            log.warn("Could not record the manifest of {} for {}: {}", component, ref, e.toString());
            return null;
        }
    }
}
