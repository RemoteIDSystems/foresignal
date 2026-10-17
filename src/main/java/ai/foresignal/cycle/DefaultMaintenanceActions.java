package ai.foresignal.cycle;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.benchmark.BenchmarkInputs;
import ai.foresignal.benchmark.BenchmarkService;
import ai.foresignal.benchmark.HistoricalSuite;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.ensemble.EnsembleService;
import ai.foresignal.forecast.eval.EvaluationService;
import ai.foresignal.impact.rate.ImpactRateService;
import ai.foresignal.llm.LlmProviderEntity;
import ai.foresignal.llm.LlmProviderRepository;
import ai.foresignal.observability.ObservabilityEstimator;
import ai.foresignal.outlook.index.IndexFitService;
import ai.foresignal.outlook.index.RegionalIndexFitEntity;
import ai.foresignal.outlook.regional.RegionalOutlookService;
import ai.foresignal.outlook.series.SeriesForecastService;
import ai.foresignal.precursor.PrecursorService;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The default {@link MaintenanceActions} (prompt 128 §5): it triggers the monthly jobs the earlier prompts scheduled
 * themselves until this prompt (121's base-rate refresh, 120's re-test, 122's refit, 123's historical suite and 126's
 * fits and re-selection), the 116 re-estimation, archival proposals for hypothesis sets past their horizon (the set
 * is flagged for review; its next review archives it, nothing is deleted here), and the 122 retrospective evaluation
 * of the LLM forecasters when the provider has a {@code knowledgeCutoff}.
 */
@Component
public class DefaultMaintenanceActions implements MaintenanceActions {

    private final ObjectProvider<ObservabilityEstimator> observability;
    private final ObjectProvider<HistoricalSuite> historical;
    private final ObjectProvider<BenchmarkService> benchmarks;
    private final ObjectProvider<BaseRateService> baseRates;
    private final ObjectProvider<PrecursorService> precursors;
    private final ObjectProvider<EnsembleService> ensemble;
    private final ObjectProvider<IndexFitService> indexFits;
    private final ObjectProvider<RegionalOutlookService> outlooks;
    private final ObjectProvider<SeriesForecastService> series;
    private final ObjectProvider<ImpactRateService> impactRates;
    private final ObjectProvider<EvaluationService> evaluations;
    private final ObjectProvider<ForecasterRegistry> forecasters;
    private final ObjectProvider<LlmProviderRepository> providers;
    private final NamedParameterJdbcTemplate jdbc;

    /** Creates the actions. */
    public DefaultMaintenanceActions(ObjectProvider<ObservabilityEstimator> observability,
            ObjectProvider<HistoricalSuite> historical, ObjectProvider<BenchmarkService> benchmarks,
            ObjectProvider<BaseRateService> baseRates, ObjectProvider<PrecursorService> precursors,
            ObjectProvider<EnsembleService> ensemble, ObjectProvider<IndexFitService> indexFits,
            ObjectProvider<RegionalOutlookService> outlooks, ObjectProvider<SeriesForecastService> series,
            ObjectProvider<ImpactRateService> impactRates, ObjectProvider<EvaluationService> evaluations,
            ObjectProvider<ForecasterRegistry> forecasters, ObjectProvider<LlmProviderRepository> providers,
            NamedParameterJdbcTemplate jdbc) {
        this.observability = observability;
        this.historical = historical;
        this.benchmarks = benchmarks;
        this.baseRates = baseRates;
        this.precursors = precursors;
        this.ensemble = ensemble;
        this.indexFits = indexFits;
        this.outlooks = outlooks;
        this.series = series;
        this.impactRates = impactRates;
        this.evaluations = evaluations;
        this.forecasters = forecasters;
        this.providers = providers;
        this.jdbc = jdbc;
    }

    private static <T> T require(ObjectProvider<T> provider, String what) {
        T bean = provider.getIfAvailable();
        if (bean == null) {
            throw new IllegalStateException(what + " is not available");
        }
        return bean;
    }

    @Override
    public Map<String, Object> observabilityReestimation(Instant now) {
        ObservabilityEstimator.Result r = require(observability, "the observability estimator").run(now);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("captureRecapture", r.captureRecapture());
        out.put("presence", r.presence());
        out.put("receiverRange", r.receiverRange());
        return out;
    }

    @Override
    public Map<String, Object> benchmarkHistorical(Instant now) {
        int sampled = require(historical, "the historical benchmark suite").sampleNullCases(now);
        var run = require(benchmarks, "the benchmark service").submit(new BenchmarkInputs.RunRequest("historical",
                BenchmarkInputs.COMPONENTS, null), "maintenance", "schedule");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("nullCasesSampled", sampled);
        out.put("jobId", run.getId());
        return out;
    }

    @Override
    public Map<String, Object> baseRateRefresh(Instant now) {
        var job = require(baseRates, "the base-rate service").submitRefresh("cycle", null);
        ImpactRateService rates = impactRates.getIfAvailable();
        if (rates != null) {
            rates.clearCache();
        }
        return Map.of("jobId", job.getId());
    }

    @Override
    public Map<String, Object> precursorRetest(Instant now) {
        return Map.of("jobId", require(precursors, "the precursor service").submit("retest", "cycle", null).getId());
    }

    @Override
    public Map<String, Object> plattRefit(Instant now) {
        return require(ensemble, "the ensemble service").refit();
    }

    @Override
    public Map<String, Object> ensembleWeights(Instant now) {
        return Map.of("questionsRefreshed", require(ensemble, "the ensemble service").refreshAll());
    }

    @Override
    public Map<String, Object> extremizingCheck(Instant now) {
        EnsembleService.Extremizing e = require(ensemble, "the ensemble service").extremizing();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("a", e.a());
        out.put("enabled", e.enabled());
        return out;
    }

    @Override
    public Map<String, Object> indexWeightFit(Instant now) {
        List<String> fits = new ArrayList<>();
        for (RegionalIndexFitEntity fit : require(indexFits, "the index fit service").submitMonthly()) {
            fits.add(fit.getId());
        }
        RegionalOutlookService.RunResult refit = require(outlooks, "the regional outlook service").refitAll(now);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("indexFitIds", fits);
        out.put("outlookRefit", String.valueOf(refit));
        return out;
    }

    @Override
    public Map<String, Object> seriesMethodReselection(Instant now) {
        return require(series, "the series forecaster").reselectAll(now);
    }

    @Override
    public Map<String, Object> hypothesisArchivalProposals(Instant now) {
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        List<String> ids = jdbc.queryForList("UPDATE hypothesis_set SET review_flagged = true, version = version + 1"
                + " WHERE status = 'active' AND horizon IS NOT NULL AND horizon < :today AND NOT review_flagged"
                + " RETURNING id", new MapSqlParameterSource("today", Date.valueOf(today)), String.class);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("flaggedForArchivalReview", ids);
        return out;
    }

    @Override
    public Map<String, Object> retrospectiveEvaluation(Instant now, String providerId) {
        LlmProviderRepository repo = providers.getIfAvailable();
        LocalDate cutoff = repo == null || providerId == null ? null
                : repo.findById(providerId).map(LlmProviderEntity::getKnowledgeCutoff).orElse(null);
        if (cutoff == null) {
            return null;
        }
        EvaluationService service = require(evaluations, "the evaluation service");
        List<Map<String, Object>> jobs = new ArrayList<>();
        Instant from = now.atZone(ZoneOffset.UTC).minusMonths(1).toInstant().truncatedTo(ChronoUnit.DAYS);
        for (ForecasterEntity f : require(forecasters, "the forecaster registry").all()) {
            if (!"llm".equals(f.getType()) || !f.isActive()) {
                continue;
            }
            Map<String, Object> job = new LinkedHashMap<>();
            job.put("forecasterId", f.getId());
            try {
                job.put("jobId", service.submit(new ForecastInputs.EvaluationRequest(f.getId(), from, now, List.of(),
                        null), null).getId());
            } catch (ForesignalException e) {
                job.put("error", e.getMessage());
            }
            jobs.add(job);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("knowledgeCutoff", cutoff.toString());
        out.put("from", from.toString());
        out.put("to", now.toString());
        out.put("evaluations", jobs);
        return out;
    }
}
