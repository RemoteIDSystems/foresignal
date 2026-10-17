package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastStatus;
import ai.foresignal.forecast.ForecastViews;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.ensemble.EnsembleService;
import ai.foresignal.forecast.eval.EvaluationService;
import ai.foresignal.forecast.score.ScoringService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The forecast ledger API (prompt 122; OpenAPI tag {@code Forecasts}). Questions, forecasts, forecasters, scorecards,
 * calibration and comparisons are reads; submitting a forecast is a read-scope write recorded under the caller's
 * operator forecaster (a token is required); opening, changing, annulling and resolving questions, evaluations and the
 * ensemble refit are {@code agent}; forecaster settings are {@code admin} ({@code AccessPolicy}). The ledger holds
 * questions, numbers and refs, never record content; private questions are invisible without private access.
 */
@RestController
@Tag(name = "Forecasts")
public class ForecastController {

    private final ForecastService ledger;
    private final ForecasterRegistry forecasters;
    private final ScoringService scoring;
    private final EnsembleService ensemble;
    private final EvaluationService evaluations;
    private final IntelVisibility visibility;
    private final Clock clock;

    /** Creates the controller. */
    public ForecastController(ForecastService ledger, ForecasterRegistry forecasters, ScoringService scoring,
            EnsembleService ensemble, EvaluationService evaluations, IntelVisibility visibility, Clock clock) {
        this.ledger = ledger;
        this.forecasters = forecasters;
        this.scoring = scoring;
        this.ensemble = ensemble;
        this.evaluations = evaluations;
        this.visibility = visibility;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- questions

    /** {@code listForecastQuestions}: newest first. */
    @GetMapping("/v1/forecasts/questions")
    @RestrictedRead("ledger questions hold numbers and refs, no record content; private questions need private access")
    @Operation(operationId = "listForecastQuestions", tags = "Forecasts")
    public Map<String, Object> listForecastQuestions(@RequestParam(required = false) List<String> status,
            @RequestParam(required = false) List<String> category, @RequestParam(required = false) String linked,
            @RequestParam(required = false) List<String> resolver, @RequestParam(required = false) String dueBefore,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        ForecastService.ListQuery query = new ForecastService.ListQuery(
                List.copyOf(QueryParams.enumSet("status", status, ForecastStatus.ALL)),
                QueryParams.multi("category", category), linked,
                List.copyOf(QueryParams.enumSet("resolver", resolver, Ledger.RESOLVER_TYPES)),
                QueryParams.instant("dueBefore", dueBefore));
        ForecastService.Page page = ledger.list(query, cursor, QueryParams.intInRange("limit", limit, 1, 500, 100),
                visibility.seesPrivate(request));
        List<Map<String, Object>> items = new ArrayList<>();
        for (ForecastQuestionEntity q : page.items()) {
            items.add(ledger.summary(q));
        }
        return ForecastViews.page(items, page.nextCursor());
    }

    /** {@code createForecastQuestion}: 201, or 200 with the existing question of the same {@code dedupKey}. */
    @PostMapping("/v1/forecasts/questions")
    @Operation(operationId = "createForecastQuestion", tags = "Forecasts")
    public ResponseEntity<Map<String, Object>> createForecastQuestion(@RequestBody JsonNode body,
            HttpServletRequest request) {
        ForecastInputs.QuestionDraft draft = ForecastInputs.question(body, clock.instant());
        boolean admin = CurrentToken.of(request).map(t -> t.scope() == Scope.ADMIN).orElse(true);
        ForecastService.Opened opened = ledger.openQuestion(draft, admin ? "operator" : "agent", actor(request));
        return ResponseEntity.status(opened.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(ledger.view(opened.question()));
    }

    /** {@code getForecastQuestion}. */
    @GetMapping("/v1/forecasts/questions/{questionId}")
    @RestrictedRead("ledger questions hold numbers and refs, no record content; private questions need private access")
    @Operation(operationId = "getForecastQuestion", tags = "Forecasts")
    public Map<String, Object> getForecastQuestion(@PathVariable String questionId, HttpServletRequest request) {
        return ledger.view(ledger.get(questionId, visibility.seesPrivate(request)));
    }

    /** {@code updateForecastQuestion}: a merge patch of title, tags and {@code closesAt} (criteria until forecasts). */
    @PatchMapping("/v1/forecasts/questions/{questionId}")
    @Operation(operationId = "updateForecastQuestion", tags = "Forecasts")
    public Map<String, Object> updateForecastQuestion(@PathVariable String questionId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        boolean seesPrivate = visibility.seesPrivate(request);
        ForecastQuestionEntity q = ledger.get(questionId, seesPrivate);
        return ledger.view(ledger.patch(questionId, ForecastInputs.patch(body, q.getType()), seesPrivate));
    }

    /** {@code annulForecastQuestion}. */
    @PostMapping("/v1/forecasts/questions/{questionId}/annul")
    @Operation(operationId = "annulForecastQuestion", tags = "Forecasts")
    public Map<String, Object> annulForecastQuestion(@PathVariable String questionId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        String reason = ForecastInputs.annulReason(body);
        return ledger.view(ledger.annul(questionId, "OPERATOR", reason, actor(request),
                visibility.seesPrivate(request)));
    }

    /** {@code resolveForecastQuestion}: manual and {@code hypothesis-outcome} questions. */
    @PostMapping("/v1/forecasts/questions/{questionId}/resolve")
    @Operation(operationId = "resolveForecastQuestion", tags = "Forecasts")
    public Map<String, Object> resolveForecastQuestion(@PathVariable String questionId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        boolean seesPrivate = visibility.seesPrivate(request);
        ForecastQuestionEntity q = ledger.get(questionId, seesPrivate);
        return ledger.view(ledger.resolveManually(questionId, ForecastInputs.resolution(body, q), actor(request),
                seesPrivate));
    }

    /** {@code getForecastQuestionHistory}: the standing forecast per day. */
    @GetMapping("/v1/forecasts/questions/{questionId}/history")
    @RestrictedRead("ledger forecasts are numbers, no record content; private questions need private access")
    @Operation(operationId = "getForecastQuestionHistory", tags = "Forecasts")
    public Map<String, Object> getForecastQuestionHistory(@PathVariable String questionId,
            @RequestParam(required = false) List<String> forecaster, @RequestParam(required = false) String step,
            HttpServletRequest request) {
        QueryParams.oneOf("step", step, List.of("1d"), "1d");
        return ledger.history(ledger.get(questionId, visibility.seesPrivate(request)),
                QueryParams.multi("forecaster", forecaster));
    }

    // ------------------------------------------------------------------------- forecasts

    /** {@code submitForecast}: 201; recorded under the caller's operator forecaster unless an agent names another. */
    @PostMapping("/v1/forecasts/questions/{questionId}/forecasts")
    @Operation(operationId = "submitForecast", tags = "Forecasts")
    public ResponseEntity<Map<String, Object>> submitForecast(@PathVariable String questionId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        AuthenticatedToken token = CurrentToken.of(request).map(CurrentToken::token).orElseThrow(() ->
                new ValidationException("Submitting a forecast needs a token: it is recorded under your principal."));
        boolean seesPrivate = visibility.seesPrivate(request);
        ForecastQuestionEntity q = ledger.get(questionId, seesPrivate);
        ForecastEntity f = ledger.submit(questionId, ForecastInputs.forecast(body, q), token, seesPrivate);
        return ResponseEntity.status(HttpStatus.CREATED).body(ForecastViews.forecast(f));
    }

    /** {@code listForecasts}: newest first, including superseded forecasts. */
    @GetMapping("/v1/forecasts/questions/{questionId}/forecasts")
    @RestrictedRead("ledger forecasts are numbers, no record content; private questions need private access")
    @Operation(operationId = "listForecasts", tags = "Forecasts")
    public Map<String, Object> listForecasts(@PathVariable String questionId,
            @RequestParam(required = false) List<String> forecaster, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        ForecastQuestionEntity q = ledger.get(questionId, visibility.seesPrivate(request));
        ForecastService.ForecastPage page = ledger.forecasts(q, QueryParams.multi("forecaster", forecaster), cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        List<Map<String, Object>> items = new ArrayList<>();
        for (ForecastEntity f : page.items()) {
            items.add(ForecastViews.forecast(f));
        }
        return ForecastViews.page(items, page.nextCursor());
    }

    // ------------------------------------------------------------------------- forecasters and scores

    /** {@code listForecasters}: by id, with the headline track record. */
    @GetMapping("/v1/forecasts/forecasters")
    @RestrictedRead("forecasters and track records are statistics, no record content")
    @Operation(operationId = "listForecasters", tags = "Forecasts")
    public Map<String, Object> listForecasters(@RequestParam(required = false) List<String> type,
            @RequestParam(required = false) String active, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        List<String> types = List.copyOf(QueryParams.enumSet("type", type, Ledger.FORECASTER_TYPES));
        Boolean activeFilter = active == null || active.isBlank() ? null : QueryParams.bool("active", active, true);
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        Map<String, Map<String, Object>> records = scoring.trackRecords();
        List<Map<String, Object>> items = new ArrayList<>();
        String next = null;
        for (ForecasterEntity f : forecasters.all()) {
            if ((!types.isEmpty() && !types.contains(f.getType()))
                    || (activeFilter != null && activeFilter != f.isActive())
                    || (cursor != null && !cursor.isBlank() && f.getId().compareTo(cursor) <= 0)) {
                continue;
            }
            if (items.size() == size) {
                next = String.valueOf(items.get(items.size() - 1).get("id"));
                break;
            }
            items.add(ForecastViews.forecaster(f, records.get(f.getId())));
        }
        return ForecastViews.page(items, next);
    }

    /** {@code updateForecaster}: the active flag and configuration. */
    @PutMapping("/v1/forecasts/forecasters/{forecasterId}")
    @Operation(operationId = "updateForecaster", tags = "Forecasts")
    public Map<String, Object> updateForecaster(@PathVariable String forecasterId, @RequestBody JsonNode body) {
        Map<String, Object> update = ForecastInputs.forecasterUpdate(body);
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) update.get("config");
        ForecasterEntity f = forecasters.update(forecasterId, (Boolean) update.get("active"), config);
        return ForecastViews.forecaster(f, scoring.trackRecords().get(f.getId()));
    }

    /** {@code getForecastScorecard}. */
    @GetMapping("/v1/forecasts/scorecard")
    @RestrictedRead("scorecards are statistics, no record content")
    @Operation(operationId = "getForecastScorecard", tags = "Forecasts")
    public Map<String, Object> getForecastScorecard(@RequestParam(required = false) String forecaster,
            @RequestParam(required = false) String category, @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return scoring.scorecard(blank(forecaster), blank(category), QueryParams.instant("from", from),
                QueryParams.instant("to", to));
    }

    /** {@code getForecastCalibration}. */
    @GetMapping("/v1/forecasts/calibration")
    @RestrictedRead("calibration curves are statistics, no record content")
    @Operation(operationId = "getForecastCalibration", tags = "Forecasts")
    public Map<String, Object> getForecastCalibration(@RequestParam(required = false) String forecaster,
            @RequestParam(required = false) String category) {
        return scoring.calibration(blank(forecaster), blank(category));
    }

    /** {@code compareForecasters}: shared questions only, with a paired bootstrap interval. */
    @GetMapping("/v1/forecasts/compare")
    @RestrictedRead("comparisons are statistics, no record content")
    @Operation(operationId = "compareForecasters", tags = "Forecasts")
    public Map<String, Object> compareForecasters(@RequestParam(required = false) String a,
            @RequestParam(required = false) String b, @RequestParam(required = false) String category) {
        if (blank(a) == null || blank(b) == null) {
            throw ValidationException.forField(blank(a) == null ? "a" : "b", "is required");
        }
        return scoring.compare(a, b, blank(category));
    }

    // ------------------------------------------------------------------------- evaluations and refit

    /** {@code createForecastEvaluation}: 202 with the evaluation. */
    @PostMapping("/v1/forecasts/evaluations")
    @Operation(operationId = "createForecastEvaluation", tags = "Forecasts")
    public ResponseEntity<Map<String, Object>> createForecastEvaluation(@RequestBody JsonNode body,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(EvaluationService.view(evaluations.submit(
                ForecastInputs.evaluation(body), tokenId(request))));
    }

    /** {@code getForecastEvaluation}. */
    @GetMapping("/v1/forecasts/evaluations/{evaluationId}")
    @Operation(operationId = "getForecastEvaluation", tags = "Forecasts")
    public Map<String, Object> getForecastEvaluation(@PathVariable String evaluationId) {
        return EvaluationService.view(evaluations.get(evaluationId));
    }

    /** {@code refitForecastEnsemble}: the monthly check now. */
    @PostMapping("/v1/forecasts/ensemble/refit")
    @Operation(operationId = "refitForecastEnsemble", tags = "Forecasts")
    public Map<String, Object> refitForecastEnsemble() {
        return ensemble.refit();
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String tokenId(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse(null);
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse("operator");
    }
}
