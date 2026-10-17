package ai.foresignal.forecast.eval;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs.EvaluationRequest;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastQuestionRepository;
import ai.foresignal.forecast.ForecastScoreEntity;
import ai.foresignal.forecast.ForecastStatus;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.JsonValues;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.baseline.BaselineForecaster;
import ai.foresignal.forecast.baseline.BaselineRunner;
import ai.foresignal.forecast.resolve.ResolverEngine;
import ai.foresignal.forecast.score.QuestionScorer;
import ai.foresignal.forecast.score.Scorecards;
import ai.foresignal.llm.LlmProviderEntity;
import ai.foresignal.llm.LlmProviderRepository;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Retrospective evaluation (prompt 122 §7, Intelligence-Design §8.5): a 103 job of type {@value #TYPE} runs one
 * forecaster as of each resolved question's {@code opens_at} over a range of opening times, scores the forecasts
 * against the actual resolutions and builds a scorecard marked {@code retrospective}. Nothing is written to the
 * ledger.
 *
 * <ul>
 *   <li>The baselines read only data known by {@code opens_at} (the {@code SeriesReader} {@code asOf}, base rates
 *       from earlier weeks), so a record stored later changes nothing (INT-2); the {@code analog} forecaster cannot be
 *       run as of a past time and skips every question.</li>
 *   <li>An {@code llm} forecaster needs its provider's {@code knowledgeCutoff} (026): without one the request is
 *       refused with 409 {@code KNOWLEDGE_CUTOFF_REQUIRED}; questions opened before it are skipped and counted under
 *       {@code LEAKAGE_RISK} (LED-2). LLM forecasters run through a {@link RetrospectiveRunner} (prompt 128).</li>
 *   <li>The scorecard's skill uses the {@code base-rate} (or {@code climatology}) forecaster run the same way.</li>
 * </ul>
 */
@Service
public class EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationService.class);

    /** The 103 job type. */
    public static final String TYPE = "forecast-evaluation";

    /** The component version recorded in the manifest. */
    public static final String VERSION = "1.0.0";

    private final BacktestJobService jobs;
    private final ForecasterRegistry forecasters;
    private final ForecastQuestionRepository questions;
    private final BaselineRunner baselines;
    private final ObjectProvider<RetrospectiveRunner> runners;
    private final ObjectProvider<LlmProviderRepository> providers;
    private final ObjectProvider<LlmSettingsService> llmSettings;
    private final ObjectProvider<ManifestService> manifests;
    private final Clock clock;

    /** Creates the service. */
    public EvaluationService(BacktestJobService jobs, ForecasterRegistry forecasters,
            ForecastQuestionRepository questions, BaselineRunner baselines, ObjectProvider<RetrospectiveRunner> runners,
            ObjectProvider<LlmProviderRepository> providers, ObjectProvider<LlmSettingsService> llmSettings,
            ObjectProvider<ManifestService> manifests, Clock clock) {
        this.jobs = jobs;
        this.forecasters = forecasters;
        this.questions = questions;
        this.baselines = baselines;
        this.runners = runners;
        this.providers = providers;
        this.llmSettings = llmSettings;
        this.manifests = manifests;
        this.clock = clock;
    }

    /**
     * Submits an evaluation.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_FORECASTER}
     * @throws ConflictException 409 {@code KNOWLEDGE_CUTOFF_REQUIRED} for an LLM forecaster without a cutoff, or
     *     {@code BACKTEST_IN_PROGRESS} when the caller has a job queued or running
     */
    public BacktestJobEntity submit(EvaluationRequest request, String tokenId) {
        ForecasterEntity forecaster = forecasters.get(request.forecasterId());
        LocalDate cutoff = null;
        if ("llm".equals(forecaster.getType())) {
            cutoff = knowledgeCutoff(forecaster);
            if (cutoff == null) {
                throw new ConflictException(ErrorCodes.KNOWLEDGE_CUTOFF_REQUIRED, "Forecaster " + forecaster.getId()
                        + " is an LLM forecaster and its provider has no knowledgeCutoff; set it"
                        + " (PUT /v1/llm/providers/{id}) before a retrospective evaluation.");
            }
        }
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("forecasterId", forecaster.getId());
        definition.put("from", request.from().toString());
        definition.put("to", request.to().toString());
        definition.put("categories", request.categories());
        definition.put("maxQuestions", request.maxQuestions());
        definition.put("knowledgeCutoff", cutoff == null ? null : cutoff.toString());
        return jobs.submitRunnerJob(TYPE, forecaster.getId(), definition, tokenId);
    }

    /** The provider cutoff of an LLM forecaster ({@code config.provider}, else the chat provider), or null. */
    LocalDate knowledgeCutoff(ForecasterEntity forecaster) {
        String providerId = forecaster.configText("provider");
        LlmSettingsService settings = llmSettings.getIfAvailable();
        if (providerId == null && settings != null) {
            providerId = settings.get().chatProvider();
        }
        LlmProviderRepository repo = providers.getIfAvailable();
        if (providerId == null || repo == null) {
            return null;
        }
        return repo.findById(providerId).map(LlmProviderEntity::getKnowledgeCutoff).orElse(null);
    }

    /**
     * An evaluation by id.
     *
     * @throws NotFoundException 404 when no evaluation has that id
     */
    public BacktestJobEntity get(String id) {
        BacktestJobEntity job;
        try {
            job = jobs.get(id);
        } catch (NotFoundException e) {
            throw new NotFoundException("No forecast evaluation '" + id + "'.");
        }
        if (!TYPE.equals(job.getType())) {
            throw new NotFoundException("No forecast evaluation '" + id + "'.");
        }
        return job;
    }

    /** The {@code ForecastEvaluation} view of a job. */
    public static Map<String, Object> view(BacktestJobEntity job) {
        Map<String, Object> d = job.getDefinition();
        Map<String, Object> r = job.getResult() == null ? Map.of() : job.getResult();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", job.getId());
        out.put("forecasterId", d.get("forecasterId"));
        out.put("from", d.get("from"));
        out.put("to", d.get("to"));
        out.put("categories", d.get("categories") == null ? List.of() : d.get("categories"));
        out.put("maxQuestions", d.get("maxQuestions"));
        out.put("status", job.getStatus());
        out.put("progress", job.getProgress());
        out.put("questionsConsidered", r.get("questionsConsidered") instanceof Number n ? n.intValue() : 0);
        out.put("questionsEvaluated", r.get("questionsEvaluated") instanceof Number n ? n.intValue() : 0);
        Object skipped = r.get("skipped");
        out.put("skipped", skipped instanceof Map<?, ?> ? skipped : Map.of("leakageRisk", 0, "other", 0));
        out.put("scorecard", r.get("scorecard"));
        out.put("caveats", r.get("caveats") instanceof List<?> l ? l : List.of());
        out.put("error", r.get("error"));
        out.put("createdBy", job.getCreatedBy() == null ? "operator" : job.getCreatedBy());
        out.put("createdAt", job.getCreatedAt().toString());
        out.put("startedAt", job.getStartedAt() == null ? null : job.getStartedAt().toString());
        out.put("finishedAt", job.getFinishedAt() == null ? null : job.getFinishedAt().toString());
        out.put("manifestId", r.get("manifestId") != null ? r.get("manifestId") : job.getManifestId());
        return out;
    }

    /**
     * Runs an evaluation (the job's body).
     *
     * @param definition the job definition
     * @param monitor cancellation and progress
     * @return the result
     */
    public Map<String, Object> run(Map<String, Object> definition, JobMonitor monitor) {
        Instant now = clock.instant();
        String forecasterId = JsonValues.text(definition, "forecasterId");
        ForecasterEntity forecaster = forecasters.get(forecasterId);
        Instant from = JsonValues.instant(JsonValues.text(definition, "from"));
        Instant to = JsonValues.instant(JsonValues.text(definition, "to"));
        List<String> categories = JsonValues.strings(definition, "categories");
        Integer max = definition.get("maxQuestions") instanceof Number n ? n.intValue() : null;
        String cutoffText = JsonValues.text(definition, "knowledgeCutoff");
        Instant cutoff = cutoffText == null ? null : LocalDate.parse(cutoffText).atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        boolean llm = "llm".equals(forecaster.getType());
        List<ForecastQuestionEntity> candidates = new ArrayList<>();
        for (ForecastQuestionEntity q : questions.findByStatusOrderByIdAsc(ForecastStatus.RESOLVED)) {
            if (q.getOpensAt().isBefore(from) || !q.getOpensAt().isBefore(to)) {
                continue;
            }
            if (!categories.isEmpty() && !categories.contains(q.getCategory())) {
                continue;
            }
            candidates.add(q);
        }
        candidates.sort(Comparator.comparing(ForecastQuestionEntity::getOpensAt)
                .thenComparing(ForecastQuestionEntity::getId));
        if (max != null && candidates.size() > max) {
            candidates = candidates.subList(0, max);
        }
        int leakage = 0;
        int other = 0;
        int evaluated = 0;
        List<ForecastScoreEntity> rows = new ArrayList<>();
        List<Map<String, Object>> caveats = new ArrayList<>();
        Map<String, Integer> reasons = new LinkedHashMap<>();
        for (int i = 0; i < candidates.size(); i++) {
            monitor.checkpoint((double) i / Math.max(1, candidates.size()));
            ForecastQuestionEntity q = candidates.get(i);
            if (llm && cutoff != null && q.getOpensAt().isBefore(cutoff)) {
                leakage++;
                continue;
            }
            BaselineForecaster.Outcome outcome = forecastAsOf(forecaster, q);
            if (outcome.skipped()) {
                other++;
                reasons.merge(outcome.skipReason(), 1, Integer::sum);
                continue;
            }
            List<ForecastEntity> list = new ArrayList<>();
            list.add(transientForecast(q, forecaster, outcome));
            String referenceId = Ledger.NUMERIC.equals(q.getType()) ? ForecasterRegistry.CLIMATOLOGY
                    : ForecasterRegistry.BASE_RATE;
            if (!referenceId.equals(forecaster.getId())) {
                BaselineForecaster reference = baselines.baseline(referenceId);
                ForecasterEntity referenceEntity = forecasters.find(referenceId).orElse(null);
                if (reference != null && referenceEntity != null && reference.supports(q)) {
                    BaselineForecaster.Outcome ref = safe(() -> reference.forecast(q, q.getOpensAt(), true));
                    if (!ref.skipped()) {
                        list.add(transientForecast(q, referenceEntity, ref));
                    }
                }
            }
            rows.addAll(QuestionScorer.score(q, list, now));
            evaluated++;
        }
        if (leakage > 0) {
            caveats.add(ResolverEngine.caveat("LEAKAGE_RISK", leakage + " question" + (leakage == 1 ? " was" : "s were")
                    + " opened before the model's knowledge cutoff (" + cutoffText + ") and skipped: the model may"
                    + " already know their outcomes."));
        }
        reasons.forEach((reason, count) -> caveats.add(ResolverEngine.caveat("SKIPPED", count + " skipped: "
                + reason)));
        if (evaluated < Scorecards.MIN_RESOLVED) {
            caveats.add(ResolverEngine.caveat("SMALL_SAMPLE", "Only " + evaluated + " questions were evaluated; the"
                    + " scorecard is labeled insufficient under " + Scorecards.MIN_RESOLVED + "."));
        }
        Map<String, Object> scorecard = Scorecards.scorecard(rows, new Scorecards.Filter(forecasterId, null, null,
                null), true, Map.of(), Map.of(), now);
        Map<String, Object> skipped = new LinkedHashMap<>();
        skipped.put("leakageRisk", leakage);
        skipped.put("other", other);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("questionsConsidered", candidates.size());
        result.put("questionsEvaluated", evaluated);
        result.put("skipped", skipped);
        result.put("scorecard", scorecard);
        result.put("caveats", caveats);
        result.put("manifestId", manifest(definition, candidates));
        return result;
    }

    /** Runs the forecaster as of the question's opening time. */
    BaselineForecaster.Outcome forecastAsOf(ForecasterEntity forecaster, ForecastQuestionEntity q) {
        BaselineForecaster baseline = baselines.baseline(forecaster.getId());
        if (baseline != null && baseline.id().equals(forecaster.getType())) {
            if (!baseline.supports(q)) {
                return BaselineForecaster.Outcome.skip(forecaster.getId() + " does not forecast this kind of"
                        + " question");
            }
            return safe(() -> baseline.forecast(q, q.getOpensAt(), true));
        }
        RetrospectiveRunner runner = runners.orderedStream().filter(r -> r.handles(forecaster)).findFirst()
                .orElse(null);
        if (runner == null) {
            return BaselineForecaster.Outcome.skip("no retrospective runner is registered for " + forecaster.getType()
                    + " forecasters");
        }
        return safe(() -> runner.forecast(forecaster, q, q.getOpensAt()));
    }

    private static BaselineForecaster.Outcome safe(java.util.function.Supplier<BaselineForecaster.Outcome> call) {
        try {
            return call.get();
        } catch (RuntimeException e) {
            return BaselineForecaster.Outcome.skip("failed: " + e.getMessage());
        }
    }

    /** A forecast made at the question's opening time, never saved. */
    static ForecastEntity transientForecast(ForecastQuestionEntity q, ForecasterEntity forecaster,
            BaselineForecaster.Outcome outcome) {
        ForecastEntity f = new ForecastEntity(Ulids.next(q.getOpensAt()), q.getId(), forecaster.getId(),
                forecaster.getType(), q.getOpensAt(), 0L);
        f.setProbability(outcome.value().probability());
        f.setDistribution(outcome.value().distribution());
        f.setQuantiles(outcome.value().quantiles());
        f.setInputs(outcome.inputs());
        return f;
    }

    private String manifest(Map<String, Object> definition, List<ForecastQuestionEntity> considered) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        List<String> refs = new ArrayList<>();
        for (ForecastQuestionEntity q : considered) {
            refs.add("question:" + q.getId());
        }
        try {
            return service.record(ManifestRequest.of(TYPE, VERSION).config(definition).inputs(refs));
        } catch (RuntimeException e) {
            log.warn("Could not record the evaluation manifest: {}", e.toString());
            return null;
        }
    }
}
