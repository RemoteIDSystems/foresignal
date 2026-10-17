package ai.foresignal.forecast;

import ai.foresignal.analytics.eventstudy.EventSelectorResolver;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.ForecastInputs.ForecastSubmission;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastInputs.QuestionDraft;
import ai.foresignal.forecast.ForecastInputs.ResolutionSubmission;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.Ulids;
import ai.foresignal.store.query.SeqClockQuery;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The forecast ledger (prompt 122, Intelligence-Design §8): questions and immutable forecasts.
 *
 * <ul>
 *   <li><b>Questions</b> open with verbatim criteria and a validated resolver ({@link #openQuestion}, the entry point
 *       for later prompts too); an existing {@code dedupKey} returns the existing question. Criteria and resolver
 *       change only until the first forecast ({@code QUESTION_IMMUTABLE}); resolved or annulled questions change no
 *       more ({@code QUESTION_CLOSED}).</li>
 *   <li><b>Forecasts</b> are append-only (LED-1): {@code made_at} and {@code asof_seq} are set here, a new forecast
 *       supersedes the forecaster's previous one by {@code supersedes_id}, and every forecast stores a 116
 *       manifest. A read-scope caller forecasts as {@code operator:{handle}}; an agent may name another registered
 *       {@code operator} or {@code external} forecaster. Every non-ensemble forecast refreshes the ensemble.</li>
 *   <li><b>Visibility</b>: private questions and their forecasts are invisible ({@code 404 UNKNOWN_QUESTION}) to
 *       callers without private access.</li>
 * </ul>
 */
@Service
public class ForecastService {

    private static final Logger log = LoggerFactory.getLogger(ForecastService.class);

    /** The component name of question manifests. */
    public static final String QUESTION_COMPONENT = "forecast-question";

    /** The component prefix of forecast manifests ({@code forecaster:{type}}). */
    public static final String FORECAST_COMPONENT_PREFIX = "forecaster:";

    private final ForecastQuestionRepository questions;
    private final ForecastRepository forecasts;
    private final ForecastScoreRepository scores;
    private final ForecasterRegistry forecasters;
    private final ForecastEvents events;
    private final ForecastNotifications notifications;
    private final SeqClockQuery seqClock;
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectProvider<EventSelectorResolver> selectors;
    private final ObjectProvider<ManifestService> manifests;
    private final ObjectProvider<LedgerHooks> hooks;
    private final Clock clock;

    /** Creates the service. */
    public ForecastService(ForecastQuestionRepository questions, ForecastRepository forecasts,
            ForecastScoreRepository scores, ForecasterRegistry forecasters, ForecastEvents events,
            ForecastNotifications notifications, SeqClockQuery seqClock, NamedParameterJdbcTemplate jdbc,
            ObjectProvider<EventSelectorResolver> selectors, ObjectProvider<ManifestService> manifests,
            ObjectProvider<LedgerHooks> hooks, Clock clock) {
        this.questions = questions;
        this.forecasts = forecasts;
        this.scores = scores;
        this.forecasters = forecasters;
        this.events = events;
        this.notifications = notifications;
        this.seqClock = seqClock;
        this.jdbc = jdbc;
        this.selectors = selectors;
        this.manifests = manifests;
        this.hooks = hooks;
        this.clock = clock;
    }

    /**
     * The result of opening a question.
     *
     * @param question the question
     * @param created false when an existing question with the same dedup key was returned
     */
    public record Opened(ForecastQuestionEntity question, boolean created) {}

    // ------------------------------------------------------------------------- questions

    /**
     * Opens a question (the entry point of 122 and of later prompts: 125 hypothesis sets, 126 outlooks and series,
     * 127 impacts). An existing question with the same {@code dedupKey} is returned unchanged.
     *
     * @param draft the validated question
     * @param origin {@code operator}, {@code agent}, {@code system} or {@code analyst}
     * @param actor who opened it (a token id, a component), or null
     * @throws ValidationException 400 {@code RESOLVER_INVALID} when the selector is not resolvable
     */
    @Transactional
    public Opened openQuestion(QuestionDraft draft, String origin, String actor) {
        if (draft.dedupKey() != null) {
            List<ForecastQuestionEntity> existing = questions.findByDedupKey(draft.dedupKey());
            if (!existing.isEmpty()) {
                existing.sort(Comparator.comparing(ForecastQuestionEntity::getId));
                return new Opened(existing.get(0), false);
            }
        }
        checkSelector(draft.resolver());
        Instant now = clock.instant();
        ForecastQuestionEntity q = new ForecastQuestionEntity(Ulids.next(now), now);
        q.setTitle(draft.title());
        q.setType(draft.type());
        q.setOutcomes(draft.outcomes());
        q.setResolutionCriteria(draft.criteria());
        q.setResolver(draft.resolver());
        q.setOpensAt(draft.opensAt());
        q.setClosesAt(draft.closesAt());
        q.setResolveBy(draft.resolveBy());
        q.setCategory(draft.category());
        q.setTags(draft.tags());
        Map<String, String> links = draft.links();
        q.setSituationId(links.get("situationId"));
        q.setHypothesisSetId(links.get("hypothesisSetId"));
        q.setPrecursorId(links.get("precursorId"));
        q.setFindingId(links.get("findingId"));
        q.setSeriesRef(links.get("seriesRef"));
        q.setOutlookId(links.get("outlookId"));
        q.setOrigin(Ledger.ORIGINS.contains(origin) ? origin : "agent");
        q.setDedupKey(draft.dedupKey());
        q.setPrivateQuestion(draft.privateQuestion());
        q.setCreatedBy(actor);
        if (!q.getClosesAt().isAfter(now)) {
            q.setStatus(ForecastStatus.CLOSED);
        }
        q.setManifestId(manifest(ManifestRequest.of(QUESTION_COMPONENT, ResolverSpec.VERSION)
                .config(Map.of("type", q.getType(), "resolver", q.getResolver(), "criteria",
                        q.getResolutionCriteria()))
                .query(Map.of("questionId", q.getId()))));
        questions.saveAndFlush(q);
        events.question(ForecastEvents.OPENED, q, 0);
        String id = q.getId();
        LedgerHooks h = hooks.getIfAvailable();
        if (h != null) {
            ForecastEvents.afterCommit(() -> h.questionOpened(id));
        }
        return new Opened(q, true);
    }

    /** Validates an event selector against the 119 resolver (families, ids), as {@code RESOLVER_INVALID}. */
    private void checkSelector(Map<String, Object> resolver) {
        Map<String, Object> selector = JsonValues.child(resolver, "selector");
        EventSelectorResolver resolverBean = selectors.getIfAvailable();
        if (selector == null || resolverBean == null) {
            return;
        }
        try {
            resolverBean.validate(selector);
        } catch (ValidationException e) {
            String message = e.getErrors().isEmpty() ? e.getMessage() : e.getErrors().get(0).message();
            throw ResolverSpec.invalid("resolver.selector", message);
        }
    }

    /**
     * A question the caller may see.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_QUESTION}
     */
    public ForecastQuestionEntity get(String id, boolean seesPrivate) {
        return questions.findById(id == null ? "" : id).filter(q -> seesPrivate || !q.isPrivateQuestion())
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_QUESTION, "No forecast question '" + id
                        + "'."));
    }

    /** A question by id regardless of visibility (internal use). */
    public Optional<ForecastQuestionEntity> find(String id) {
        return id == null ? Optional.empty() : questions.findById(id);
    }

    /** The {@code ForecastQuestion} view of a question. */
    @Transactional(readOnly = true)
    public Map<String, Object> view(ForecastQuestionEntity q) {
        List<ForecastEntity> latest = new ArrayList<>(latestByForecaster(q.getId()).values());
        latest.sort(Comparator.comparing(ForecastEntity::getMadeAt).thenComparing(ForecastEntity::getId).reversed());
        ForecastEntity ensemble = latest.stream().filter(f -> ForecasterRegistry.ENSEMBLE.equals(f.getForecasterId()))
                .findFirst().orElse(null);
        List<ForecastScoreEntity> scoreRows = ForecastStatus.RESOLVED.equals(q.getStatus())
                ? scores.findByQuestionId(q.getId()) : List.of();
        return ForecastViews.question(q, forecasts.countByQuestionId(q.getId()), latest, ensemble, scoreRows);
    }

    /** The {@code ForecastQuestionSummary} view of a question. */
    public Map<String, Object> summary(ForecastQuestionEntity q) {
        return ForecastViews.summary(q, forecasts.countByQuestionId(q.getId()));
    }

    /**
     * List filters.
     *
     * @param statuses statuses (any), or empty
     * @param categories categories (any), or empty
     * @param linked an object reference or series reference, or null
     * @param resolvers resolver types (any), or empty
     * @param dueBefore {@code resolve_by < dueBefore}, or null
     */
    public record ListQuery(List<String> statuses, List<String> categories, String linked, List<String> resolvers,
            Instant dueBefore) {}

    /**
     * A page.
     *
     * @param items the questions
     * @param nextCursor the next cursor, or null
     */
    public record Page(List<ForecastQuestionEntity> items, String nextCursor) {}

    /** Lists questions, newest first. */
    @Transactional(readOnly = true)
    public Page list(ListQuery q, String cursor, int limit, boolean seesPrivate) {
        StringBuilder sql = new StringBuilder("SELECT id FROM forecast_question WHERE 1 = 1");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (!q.statuses().isEmpty()) {
            sql.append(" AND status IN (:statuses)");
            p.addValue("statuses", q.statuses());
        }
        if (!q.categories().isEmpty()) {
            sql.append(" AND category IN (:categories)");
            p.addValue("categories", q.categories());
        }
        if (!q.resolvers().isEmpty()) {
            sql.append(" AND resolver_type IN (:resolvers)");
            p.addValue("resolvers", q.resolvers());
        }
        if (q.dueBefore() != null) {
            sql.append(" AND resolve_by < :due");
            p.addValue("due", Timestamp.from(q.dueBefore()));
        }
        if (q.linked() != null && !q.linked().isBlank()) {
            String[] link = linkColumn(q.linked());
            sql.append(" AND ").append(link[0]).append(" = :linked");
            p.addValue("linked", link[1]);
        }
        if (!seesPrivate) {
            sql.append(" AND private = false");
        }
        String after = decodeCursor(cursor);
        if (after != null) {
            sql.append(" AND id < :after");
            p.addValue("after", after);
        }
        sql.append(" ORDER BY id DESC LIMIT :limit");
        p.addValue("limit", limit + 1);
        List<String> ids = jdbc.queryForList(sql.toString(), p, String.class);
        boolean more = ids.size() > limit;
        List<String> page = more ? ids.subList(0, limit) : ids;
        Map<String, ForecastQuestionEntity> byId = new LinkedHashMap<>();
        questions.findAllById(page).forEach(e -> byId.put(e.getId(), e));
        List<ForecastQuestionEntity> items = new ArrayList<>();
        for (String id : page) {
            if (byId.containsKey(id)) {
                items.add(byId.get(id));
            }
        }
        return new Page(items, more ? encodeCursor(page.get(page.size() - 1)) : null);
    }

    /** The column and value a {@code linked} filter matches. */
    static String[] linkColumn(String linked) {
        int colon = linked.indexOf(':');
        String prefix = colon < 0 ? "" : linked.substring(0, colon);
        String value = colon < 0 ? linked : linked.substring(colon + 1);
        return switch (prefix) {
            case "situation" -> new String[] {"situation_id", value};
            case "finding" -> new String[] {"finding_id", value};
            case "precursor" -> new String[] {"precursor_id", value};
            case "hypothesis-set" -> new String[] {"hypothesis_set_id", value};
            case "outlook" -> new String[] {"outlook_id", value};
            case "series", "rollup", "rule", "social" -> new String[] {"series_ref", linked};
            default -> throw ValidationException.forField("linked", "must be situation:, finding:, precursor:,"
                    + " hypothesis-set:, outlook: or a series reference");
        };
    }

    static String encodeCursor(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    static String decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String id = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!Ulids.isUlid(id)) {
                throw new IllegalArgumentException(id);
            }
            return id;
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", "is not a cursor from this list");
        }
    }

    /**
     * Applies a merge patch.
     *
     * @throws ConflictException 409 {@code QUESTION_CLOSED} or {@code QUESTION_IMMUTABLE}
     */
    @Transactional
    public ForecastQuestionEntity patch(String id, Map<String, Object> patch, boolean seesPrivate) {
        ForecastQuestionEntity q = get(id, seesPrivate);
        if (q.finished()) {
            throw new ConflictException(ErrorCodes.QUESTION_CLOSED, "The question is " + q.getStatus()
                    + " and can no longer change.");
        }
        boolean structural = patch.containsKey("resolutionCriteria") || patch.containsKey("resolver");
        if (structural && forecasts.countByQuestionId(q.getId()) > 0) {
            throw new ConflictException(ErrorCodes.QUESTION_IMMUTABLE, "Criteria and resolver are fixed once a"
                    + " forecast exists.");
        }
        Instant now = clock.instant();
        if (patch.containsKey("title")) {
            q.setTitle((String) patch.get("title"));
        }
        if (patch.containsKey("tags")) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) patch.get("tags");
            q.setTags(tags);
        }
        if (patch.containsKey("closesAt")) {
            Instant closesAt = (Instant) patch.get("closesAt");
            if (!closesAt.isAfter(q.getOpensAt()) || closesAt.isAfter(q.getResolveBy())) {
                throw ValidationException.forField("closesAt", "must be after opensAt and not after resolveBy");
            }
            q.setClosesAt(closesAt);
            if (ForecastStatus.CLOSED.equals(q.getStatus()) && closesAt.isAfter(now)) {
                q.setStatus(ForecastStatus.OPEN);
            }
        }
        if (patch.containsKey("resolutionCriteria")) {
            q.setResolutionCriteria((String) patch.get("resolutionCriteria"));
        }
        if (patch.containsKey("resolver")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> resolver = (Map<String, Object>) patch.get("resolver");
            checkSelector(resolver);
            q.setResolver(resolver);
        }
        q.touch(now);
        return questions.save(q);
    }

    /**
     * Annuls a question; it is never scored.
     *
     * @param code {@code LOW_DETECTABILITY}, {@code COVERAGE_GAP} or {@code OPERATOR}
     * @throws ConflictException 409 {@code QUESTION_CLOSED} when already resolved or annulled
     */
    @Transactional
    public ForecastQuestionEntity annul(String id, String code, String reason, String by, boolean seesPrivate) {
        ForecastQuestionEntity q = get(id, seesPrivate);
        return annul(q, code, reason, by);
    }

    /** Annuls a loaded question (the resolution job's path). */
    @Transactional
    public ForecastQuestionEntity annul(ForecastQuestionEntity q, String code, String reason, String by) {
        if (q.finished()) {
            throw new ConflictException(ErrorCodes.QUESTION_CLOSED, "The question is already " + q.getStatus() + ".");
        }
        Instant now = clock.instant();
        Map<String, Object> annulment = new LinkedHashMap<>();
        annulment.put("code", code);
        annulment.put("reason", reason);
        annulment.put("at", now.toString());
        annulment.put("by", by == null ? "operator" : by);
        q.setAnnulment(annulment);
        q.setStatus(ForecastStatus.ANNULLED);
        q.touch(now);
        questions.save(q);
        scores.deleteAll(scores.findByQuestionId(q.getId()));
        events.question(ForecastEvents.ANNULLED, q, forecasts.countByQuestionId(q.getId()));
        return q;
    }

    /**
     * Resolves a manual (or {@code hypothesis-outcome}) question with evidence; scores follow.
     *
     * @throws ConflictException 409 {@code QUESTION_CLOSED} when finished or resolved by a machine resolver
     */
    @Transactional
    public ForecastQuestionEntity resolveManually(String id, ResolutionSubmission submission, String by,
            boolean seesPrivate) {
        ForecastQuestionEntity q = get(id, seesPrivate);
        if (q.finished()) {
            throw new ConflictException(ErrorCodes.QUESTION_CLOSED, "The question is already " + q.getStatus() + ".");
        }
        if (!Set.of("manual", "hypothesis-outcome").contains(q.getResolverType())) {
            throw new ConflictException(ErrorCodes.QUESTION_CLOSED, "A " + q.getResolverType() + " question resolves"
                    + " automatically; annul it instead if it cannot be resolved.");
        }
        Instant now = clock.instant();
        Map<String, Object> resolution = new LinkedHashMap<>();
        resolution.put("outcome", submission.outcome());
        resolution.put("value", submission.value());
        resolution.put("resolvedAt", now.toString());
        resolution.put("resolvedBy", by == null ? "operator" : by);
        resolution.put("early", now.isBefore(q.getResolveBy()));
        resolution.put("asOf", now.toString());
        resolution.put("evidence", submission.evidence());
        resolution.put("note", submission.note());
        resolution.put("caveats", List.of());
        resolution.put("manifestId", manifest(ManifestRequest.of("resolver:" + q.getResolverType(),
                ResolverSpec.VERSION).config(q.getResolver()).inputs(submission.evidence())
                .query(Map.of("questionId", q.getId(), "manual", true))));
        return finishResolution(q, resolution);
    }

    /** Records a resolution: status, event, notification and scores. */
    @Transactional
    public ForecastQuestionEntity finishResolution(ForecastQuestionEntity q, Map<String, Object> resolution) {
        Instant now = clock.instant();
        q.setResolution(resolution);
        q.setStatus(ForecastStatus.RESOLVED);
        q.touch(now);
        questions.save(q);
        events.question(ForecastEvents.RESOLVED, q, forecasts.countByQuestionId(q.getId()));
        notifications.resolved(q);
        LedgerHooks h = hooks.getIfAvailable();
        if (h != null) {
            h.questionResolved(q);
        }
        return q;
    }

    /** Closes open questions past {@code closes_at}; returns how many. */
    @Transactional
    public int closeDue() {
        Instant now = clock.instant();
        int n = 0;
        for (ForecastQuestionEntity q : questions.findByStatusAndClosesAtLessThanEqual(ForecastStatus.OPEN, now)) {
            q.setStatus(ForecastStatus.CLOSED);
            q.touch(now);
            questions.save(q);
            events.question(ForecastEvents.CLOSED, q, forecasts.countByQuestionId(q.getId()));
            n++;
        }
        return n;
    }

    /** Raises {@code forecast.resolution_needed} once for a manual question at {@code resolve_by}. */
    @Transactional
    public void requestResolution(ForecastQuestionEntity q) {
        if (q.getResolutionRequestedAt() != null) {
            return;
        }
        Instant now = clock.instant();
        q.setResolutionRequestedAt(now);
        if (ForecastStatus.OPEN.equals(q.getStatus())) {
            q.setStatus(ForecastStatus.CLOSED);
            events.question(ForecastEvents.CLOSED, q, forecasts.countByQuestionId(q.getId()));
        }
        q.touch(now);
        questions.save(q);
        notifications.resolutionNeeded(q);
    }

    /** Stores why baseline forecasters emitted nothing (by forecaster id; null removes the note). */
    @Transactional
    public void noteBaseline(String questionId, String forecasterId, String reason) {
        questions.findById(questionId).ifPresent(q -> {
            Map<String, Object> notes = new LinkedHashMap<>(q.getBaselineNotes());
            Object before = notes.get(forecasterId);
            if (reason == null) {
                notes.remove(forecasterId);
            } else {
                notes.put(forecasterId, reason);
            }
            if (!java.util.Objects.equals(before, reason)) {
                q.setBaselineNotes(notes);
                questions.save(q);
            }
        });
    }

    // ------------------------------------------------------------------------- forecasts

    /**
     * Records a forecast submitted through the API or MCP.
     *
     * @param questionId the question
     * @param submission the parsed forecast
     * @param token the caller
     * @param seesPrivate whether the caller may see private questions
     * @throws ConflictException 409 {@code QUESTION_CLOSED} when the question is not open
     * @throws NotFoundException 404 {@code UNKNOWN_FORECASTER} when the named forecaster is not an operator or
     *     external forecaster
     */
    @Transactional
    public ForecastEntity submit(String questionId, ForecastSubmission submission, AuthenticatedToken token,
            boolean seesPrivate) {
        ForecastQuestionEntity q = get(questionId, seesPrivate);
        ForecasterEntity forecaster;
        String own = forecasters.operatorId(token);
        if (submission.forecasterId() == null || submission.forecasterId().equals(own)) {
            forecaster = forecasters.operatorFor(token);
        } else if (token.scope() == Scope.READ) {
            throw ForecastInputs.invalidForecast("forecasterId", "naming another forecaster needs agent scope");
        } else {
            forecaster = forecasters.get(submission.forecasterId());
            if (!Set.of("operator", "external").contains(forecaster.getType())) {
                throw new NotFoundException(ErrorCodes.UNKNOWN_FORECASTER, "'" + forecaster.getId() + "' is not an"
                        + " operator or external forecaster.");
            }
        }
        return record(q, forecaster, submission.value(), submission.rationale(), submission.citations(), List.of(),
                Map.of("submittedBy", token.id().toString()), null, false);
    }

    /**
     * Records a forecast (every path: API, baselines, the ensemble, later prompts' forecasters).
     *
     * @param q the question (must accept forecasts)
     * @param forecaster the forecaster (must be active)
     * @param value the value (validated here against the question)
     * @param rationale the rationale, or null
     * @param citations the citations
     * @param claims typed claims (LLM forecasters)
     * @param inputs the inputs (base-rate ids, analog ids, model metadata, raw values)
     * @param knowledgeCutoff the model's knowledge cutoff (LLM forecasters), or null
     * @param recalibrated true for an ensemble forecast built from Platt-recalibrated inputs
     * @throws ConflictException 409 {@code QUESTION_CLOSED} when the question does not accept forecasts
     * @throws ValidationException 400 {@code FORECAST_INVALID}
     */
    @Transactional
    public ForecastEntity record(ForecastQuestionEntity q, ForecasterEntity forecaster, ForecastValue value,
            String rationale, List<Map<String, Object>> citations, List<Map<String, Object>> claims,
            Map<String, Object> inputs, LocalDate knowledgeCutoff, boolean recalibrated) {
        Instant now = clock.instant();
        if (!q.acceptsForecasts(now)) {
            throw new ConflictException(ErrorCodes.QUESTION_CLOSED, "The question is " + q.getStatus()
                    + (ForecastStatus.OPEN.equals(q.getStatus()) ? " but outside its forecasting window" : "")
                    + " and accepts no forecasts.");
        }
        if (!forecaster.isActive()) {
            throw ForecastInputs.invalidForecast("forecasterId", "forecaster " + forecaster.getId()
                    + " is inactive");
        }
        ForecastInputs.validateValue(value, q.getType(), q.labels());
        Optional<ForecastEntity> previous = forecasts.findTop1ByQuestionIdAndForecasterIdOrderByMadeAtDescIdDesc(
                q.getId(), forecaster.getId());
        Instant madeAt = now;
        if (previous.isPresent() && !madeAt.isAfter(previous.get().getMadeAt())) {
            // Keep a forecaster's forecasts strictly ordered even when two arrive within one clock tick.
            madeAt = previous.get().getMadeAt().plusMillis(1);
        }
        long asOfSeq = seqClock.seqAt(jdbc, now);
        ForecastEntity f = new ForecastEntity(Ulids.next(madeAt), q.getId(), forecaster.getId(), forecaster.getType(),
                madeAt, asOfSeq);
        f.setProbability(value.probability());
        f.setDistribution(value.distribution());
        f.setQuantiles(value.quantiles());
        f.setRationale(rationale);
        f.setCitations(citations);
        f.setClaims(claims);
        f.setInputs(inputs);
        f.setSupersedesId(previous.map(ForecastEntity::getId).orElse(null));
        f.setRecalibrated(recalibrated);
        f.setKnowledgeCutoff(knowledgeCutoff);
        List<String> refs = new ArrayList<>();
        refs.add("question:" + q.getId());
        for (Map<String, Object> c : citations == null ? List.<Map<String, Object>>of() : citations) {
            if (c.get("ref") != null) {
                refs.add(c.get("ref").toString());
            }
        }
        Map<String, Object> model = null;
        if (inputs != null && (inputs.get("model") != null || inputs.get("provider") != null)) {
            model = new LinkedHashMap<>();
            model.put("provider", inputs.get("provider"));
            model.put("model", inputs.get("model"));
            model.put("effort", inputs.get("effort"));
            model.put("promptHash", inputs.get("promptHash"));
            model.put("samples", inputs.get("samples"));
        }
        f.setManifestId(manifest(ManifestRequest.of(FORECAST_COMPONENT_PREFIX + forecaster.getType(),
                forecaster.getVersion()).config(forecaster.getConfig()).asOf(asOfSeq).inputs(refs).model(model)
                .query(Map.of("questionId", q.getId(), "forecasterId", forecaster.getId()))));
        forecasts.saveAndFlush(f);
        events.submitted(f, q.isPrivateQuestion());
        if (ForecasterRegistry.ENSEMBLE.equals(forecaster.getId())) {
            if (Ledger.BINARY.equals(q.getType())) {
                q.setEnsembleProbability(value.probability());
                questions.save(q);
            }
        } else {
            LedgerHooks h = hooks.getIfAvailable();
            if (h != null) {
                h.forecastRecorded(q, f);
            }
        }
        return f;
    }

    /** The latest forecast of each forecaster on a question. */
    public Map<String, ForecastEntity> latestByForecaster(String questionId) {
        Map<String, ForecastEntity> out = new LinkedHashMap<>();
        for (ForecastEntity f : forecasts.findByQuestionIdOrderByMadeAtAscIdAsc(questionId)) {
            out.put(f.getForecasterId(), f);
        }
        return out;
    }

    /** Every forecast on a question, oldest first. */
    public List<ForecastEntity> forecastsOf(String questionId) {
        return forecasts.findByQuestionIdOrderByMadeAtAscIdAsc(questionId);
    }

    /**
     * The forecasts on a question, newest first, including superseded ones.
     *
     * @param forecasterIds the forecasters (any), or empty
     * @param cursor the cursor, or null
     * @param limit the page size
     */
    @Transactional(readOnly = true)
    public ForecastPage forecasts(ForecastQuestionEntity q, List<String> forecasterIds, String cursor, int limit) {
        List<ForecastEntity> all = new ArrayList<>(forecasts.findByQuestionIdOrderByMadeAtAscIdAsc(q.getId()));
        all.sort(Comparator.comparing(ForecastEntity::getMadeAt).thenComparing(ForecastEntity::getId).reversed());
        String after = decodeCursor(cursor);
        List<ForecastEntity> filtered = new ArrayList<>();
        boolean passed = after == null;
        for (ForecastEntity f : all) {
            if (!passed) {
                if (f.getId().equals(after)) {
                    passed = true;
                }
                continue;
            }
            if (forecasterIds.isEmpty() || forecasterIds.contains(f.getForecasterId())) {
                filtered.add(f);
            }
        }
        boolean more = filtered.size() > limit;
        List<ForecastEntity> page = more ? filtered.subList(0, limit) : filtered;
        return new ForecastPage(page, more ? encodeCursor(page.get(page.size() - 1).getId()) : null);
    }

    /**
     * A page of forecasts.
     *
     * @param items the forecasts
     * @param nextCursor the next cursor, or null
     */
    public record ForecastPage(List<ForecastEntity> items, String nextCursor) {}

    /**
     * The standing forecast per UTC day for each forecaster (and the ensemble), derived from the immutable forecasts
     * ({@code ForecastHistory}): from the day the question opened to the day it closed, resolved or today, whichever
     * is first; a forecaster's series starts at its first forecast.
     *
     * @param forecasterIds the forecasters (any), or empty
     */
    @Transactional(readOnly = true)
    public Map<String, Object> history(ForecastQuestionEntity q, List<String> forecasterIds) {
        Instant now = clock.instant();
        Instant end = now;
        if (q.getClosesAt().isBefore(end)) {
            end = q.getClosesAt();
        }
        if (q.getResolution() != null && q.getResolution().get("resolvedAt") != null) {
            Instant resolved = JsonValues.instant(q.getResolution().get("resolvedAt").toString());
            if (resolved.isBefore(end)) {
                end = resolved;
            }
        }
        LocalDate first = LocalDate.ofInstant(q.getOpensAt(), ZoneOffset.UTC);
        LocalDate last = LocalDate.ofInstant(end.isAfter(q.getOpensAt()) ? end : q.getOpensAt(), ZoneOffset.UTC);
        Map<String, List<ForecastEntity>> byForecaster = new LinkedHashMap<>();
        for (ForecastEntity f : forecasts.findByQuestionIdOrderByMadeAtAscIdAsc(q.getId())) {
            if (forecasterIds.isEmpty() || forecasterIds.contains(f.getForecasterId())) {
                byForecaster.computeIfAbsent(f.getForecasterId(), k -> new ArrayList<>()).add(f);
            }
        }
        List<Map<String, Object>> series = new ArrayList<>();
        byForecaster.forEach((forecasterId, list) -> {
            List<Map<String, Object>> points = new ArrayList<>();
            for (LocalDate day = first; !day.isAfter(last); day = day.plusDays(1)) {
                Instant dayEnd = LocalDateTime.of(day.plusDays(1), LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC);
                ForecastEntity standing = null;
                for (ForecastEntity f : list) {
                    if (f.getMadeAt().isBefore(dayEnd)) {
                        standing = f;
                    }
                }
                if (standing == null) {
                    continue;
                }
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("date", day.toString());
                point.put("probability", standing.getProbability());
                point.put("distribution", standing.getDistribution());
                point.put("quantiles", standing.getQuantiles());
                point.put("forecastId", standing.getId());
                points.add(point);
            }
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("forecasterId", forecasterId);
            s.put("points", points);
            series.add(s);
        });
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("questionId", q.getId());
        out.put("type", q.getType());
        out.put("step", "P1D");
        out.put("from", first.toString());
        out.put("to", last.toString());
        out.put("series", series);
        return out;
    }

    private String manifest(ManifestRequest request) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            return service.record(request);
        } catch (RuntimeException e) {
            log.warn("Could not record a ledger manifest: {}", e.toString());
            return null;
        }
    }
}
