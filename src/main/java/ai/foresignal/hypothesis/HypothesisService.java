package ai.foresignal.hypothesis;

import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.baserate.BaseRateEntity;
import ai.foresignal.baserate.BaseRateRepository;
import ai.foresignal.baserate.analog.SituationOutcomeRepository;
import ai.foresignal.evidence.EvidenceService;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.JsonValues;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.hypothesis.HypothesisInputs.AssumptionDraft;
import ai.foresignal.hypothesis.HypothesisInputs.EvidenceDraft;
import ai.foresignal.hypothesis.HypothesisInputs.HypothesisDraft;
import ai.foresignal.hypothesis.HypothesisInputs.IndicatorDraft;
import ai.foresignal.hypothesis.HypothesisInputs.ReviewDraft;
import ai.foresignal.hypothesis.HypothesisInputs.SetDraft;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationRepository;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
 * Rolling hypothesis sets (prompt 125 §1, §2, §3, §5; Intelligence-Design §6.2–§6.4): creating a set with its
 * hypotheses, indicators, key assumptions and ledger question; changing it; recording observations, absences,
 * evidence, reviews, base-rate refreshes and resolutions. Every probability change goes through the
 * {@link RevisionWriter}; nothing here writes a probability directly.
 *
 * <p><b>Visibility.</b> A private set (and everything in it) is {@code 404 UNKNOWN_HYPOTHESIS_SET} to a caller without
 * private access (EGR-1).
 */
@Service
public class HypothesisService {

    private static final Logger log = LoggerFactory.getLogger(HypothesisService.class);

    /** The relative change of a prior's base rate that records a {@code base-rate-refresh} revision. */
    public static final double BASE_RATE_REFRESH_THRESHOLD = 0.2;

    private final HypothesisSetRepository sets;
    private final HypothesisRepository hypotheses;
    private final HypothesisRevisionRepository revisions;
    private final IndicatorRepository indicators;
    private final AchEvidenceRepository evidence;
    private final KeyAssumptionRepository assumptions;
    private final RevisionWriter writer;
    private final IndicatorWatchService watch;
    private final HypothesisEvents events;
    private final HypothesisNotifications notifications;
    private final ObjectProvider<ForecastService> ledger;
    private final AnalyticFindingRepository findings;
    private final ObjectProvider<SignalFamilyRegistry> families;
    private final ObjectProvider<EvidenceService> evidenceGroups;
    private final ObjectProvider<BaseRateRepository> baseRates;
    private final ObjectProvider<SituationRepository> situations;
    private final ObjectProvider<SituationOutcomeRepository> outcomes;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public HypothesisService(HypothesisSetRepository sets, HypothesisRepository hypotheses,
            HypothesisRevisionRepository revisions, IndicatorRepository indicators, AchEvidenceRepository evidence,
            KeyAssumptionRepository assumptions, RevisionWriter writer, IndicatorWatchService watch,
            HypothesisEvents events, HypothesisNotifications notifications, ObjectProvider<ForecastService> ledger,
            AnalyticFindingRepository findings, ObjectProvider<SignalFamilyRegistry> families,
            ObjectProvider<EvidenceService> evidenceGroups, ObjectProvider<BaseRateRepository> baseRates,
            ObjectProvider<SituationRepository> situations, ObjectProvider<SituationOutcomeRepository> outcomes,
            NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.sets = sets;
        this.hypotheses = hypotheses;
        this.revisions = revisions;
        this.indicators = indicators;
        this.evidence = evidence;
        this.assumptions = assumptions;
        this.writer = writer;
        this.watch = watch;
        this.events = events;
        this.notifications = notifications;
        this.ledger = ledger;
        this.findings = findings;
        this.families = families;
        this.evidenceGroups = evidenceGroups;
        this.baseRates = baseRates;
        this.situations = situations;
        this.outcomes = outcomes;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- reads

    /**
     * A set the caller may see.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_HYPOTHESIS_SET}
     */
    public HypothesisSetEntity get(String id, boolean seesPrivate) {
        return sets.findById(id == null ? "" : id).filter(s -> seesPrivate || !s.isPrivateSet())
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_HYPOTHESIS_SET, "No hypothesis set '" + id
                        + "'."));
    }

    /** A set by id regardless of visibility (internal use). */
    public Optional<HypothesisSetEntity> find(String id) {
        return id == null ? Optional.empty() : sets.findById(id);
    }

    /** The hypotheses of a set in order. */
    public List<HypothesisEntity> hypothesesOf(String setId) {
        return hypotheses.findBySetIdOrderByPositionAscIdAsc(setId);
    }

    /** The indicators of a set, oldest first. */
    public List<IndicatorEntity> indicatorsOf(String setId) {
        return indicators.findBySetIdOrderByIdAsc(setId);
    }

    /**
     * A page of sets.
     *
     * @param items the sets
     * @param nextCursor the cursor of the next page, or null
     */
    public record Page<T>(List<T> items, String nextCursor) {}

    /** Sets newest first, filtered; private sets only for callers with private access. */
    public Page<HypothesisSetEntity> list(String status, String cadence, String situationId, String region,
            String cursor, int limit, boolean seesPrivate) {
        StringBuilder sql = new StringBuilder("SELECT id FROM hypothesis_set WHERE 1 = 1");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (status != null) {
            sql.append(" AND status = :status");
            p.addValue("status", status);
        }
        if (cadence != null) {
            sql.append(" AND cadence = :cadence");
            p.addValue("cadence", cadence);
        }
        if (situationId != null) {
            sql.append(" AND situation_id = :situation");
            p.addValue("situation", situationId);
        }
        if (region != null) {
            sql.append(" AND :region = ANY(region_codes)");
            p.addValue("region", region);
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
        return page(ids, limit, sets::findAllById, HypothesisSetEntity::getId);
    }

    /** Indicators newest first, filtered by state and set; indicators of private sets need private access. */
    public Page<IndicatorEntity> listIndicators(String state, String setId, String cursor, int limit,
            boolean seesPrivate) {
        if (setId != null) {
            get(setId, seesPrivate);
        }
        StringBuilder sql = new StringBuilder("SELECT i.id FROM indicator i JOIN hypothesis_set s ON s.id = i.set_id"
                + " WHERE 1 = 1");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (state != null) {
            sql.append(" AND i.state = :state");
            p.addValue("state", state);
        }
        if (setId != null) {
            sql.append(" AND i.set_id = :set");
            p.addValue("set", setId);
        }
        if (!seesPrivate) {
            sql.append(" AND s.private = false");
        }
        String after = decodeCursor(cursor);
        if (after != null) {
            sql.append(" AND i.id < :after");
            p.addValue("after", after);
        }
        sql.append(" ORDER BY i.id DESC LIMIT :limit");
        p.addValue("limit", limit + 1);
        List<String> ids = jdbc.queryForList(sql.toString(), p, String.class);
        return page(ids, limit, indicators::findAllById, IndicatorEntity::getId);
    }

    /** A set's revisions newest first. */
    public Page<HypothesisRevisionEntity> revisionsOf(String setId, String cursor, int limit) {
        String before = decodeCursor(cursor);
        List<HypothesisRevisionEntity> rows = before == null
                ? revisions.findBySetIdOrderByIdDesc(setId, org.springframework.data.domain.Limit.of(limit + 1))
                : revisions.findBySetIdAndIdLessThanOrderByIdDesc(setId, before,
                        org.springframework.data.domain.Limit.of(limit + 1));
        String next = null;
        if (rows.size() > limit) {
            rows = rows.subList(0, limit);
            next = encodeCursor(rows.get(rows.size() - 1).getId());
        }
        return new Page<>(rows, next);
    }

    private static <T> Page<T> page(List<String> ids, int limit, java.util.function.Function<List<String>,
            List<T>> loader, java.util.function.Function<T, String> idOf) {
        String next = null;
        List<String> pageIds = ids;
        if (ids.size() > limit) {
            pageIds = ids.subList(0, limit);
            next = encodeCursor(pageIds.get(pageIds.size() - 1));
        }
        Map<String, T> byId = new HashMap<>();
        for (T item : loader.apply(pageIds)) {
            byId.put(idOf.apply(item), item);
        }
        List<T> out = new ArrayList<>();
        for (String id : pageIds) {
            if (byId.containsKey(id)) {
                out.add(byId.get(id));
            }
        }
        return new Page<>(out, next);
    }

    /** An opaque cursor of an id. */
    static String encodeCursor(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    /** The id of an opaque cursor, or null; 400 when malformed. */
    static String decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String id = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!Ulids.isUlid(id)) {
                throw new IllegalArgumentException("not a ULID");
            }
            return id;
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", "is not a cursor from a previous response");
        }
    }

    /**
     * The {@code HypothesisSet} view.
     *
     * @param set the set
     * @param full true for the single-set view (indicators, assumptions, the linked question)
     */
    public Map<String, Object> view(HypothesisSetEntity set, boolean full) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", set.getId());
        out.put("question", set.getQuestion());
        out.put("description", set.getDescription());
        out.put("kind", set.getKind());
        out.put("cadence", set.getCadence());
        out.put("horizon", set.getHorizon() == null ? null : set.getHorizon().toString());
        out.put("status", set.getStatus());
        out.put("situationId", set.getSituationId());
        out.put("questionId", set.getQuestionId());
        out.put("regionCodes", set.getRegionCodes());
        out.put("themes", set.getThemes());
        out.put("origin", set.getOrigin());
        out.put("confidence", set.getConfidence());
        out.put("lastReviewedAt", set.getLastReviewedAt() == null ? null : set.getLastReviewedAt().toString());
        out.put("nextReviewAt", set.getNextReviewAt() == null ? null : set.getNextReviewAt().toString());
        out.put("lastRevisionId", set.getLastRevisionId());
        out.put("lastRevisionAt", set.getLastRevisionAt() == null ? null : set.getLastRevisionAt().toString());
        out.put("private", set.isPrivateSet());
        out.put("version", set.getVersion());
        out.put("reviewFlagged", set.isReviewFlagged());
        Map<String, Map<String, Object>> previous = new HashMap<>();
        String since = null;
        if (full && set.getLastRevisionId() != null) {
            Optional<HypothesisRevisionEntity> last = revisions.findById(set.getLastRevisionId());
            if (last.isPresent()) {
                since = last.get().getId();
                for (Map<String, Object> state : last.get().getBefore()) {
                    previous.put(String.valueOf(state.get("hypothesisId")), state);
                }
            }
        }
        List<Map<String, Object>> hyps = new ArrayList<>();
        for (HypothesisEntity h : hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId())) {
            Map<String, Object> view = HypothesisViews.hypothesis(h, previous.get(h.getId()), since);
            view.put("newsCoverage", newsCoverage(h));
            hyps.add(view);
        }
        out.put("hypotheses", hyps);
        if (full) {
            List<Map<String, Object>> inds = new ArrayList<>();
            for (IndicatorEntity i : indicators.findBySetIdOrderByIdAsc(set.getId())) {
                inds.add(HypothesisViews.indicator(i));
            }
            out.put("indicators", inds);
            List<Map<String, Object>> as = new ArrayList<>();
            for (KeyAssumptionEntity a : assumptions.findBySetIdOrderByIdAsc(set.getId())) {
                as.add(HypothesisViews.assumption(a));
            }
            out.put("assumptions", as);
            out.put("linkedQuestion", linkedQuestion(set));
        }
        out.put("recordRef", set.isPrivateSet() ? null : "record:" + HypothesisSetRecordPublisher.SOURCE_ID + "/"
                + set.getId());
        out.put("createdAt", set.getCreatedAt().toString());
        out.put("updatedAt", set.getUpdatedAt().toString());
        return out;
    }

    private Map<String, Object> linkedQuestion(HypothesisSetEntity set) {
        ForecastService service = ledger.getIfAvailable();
        if (set.getQuestionId() == null || service == null) {
            return null;
        }
        return service.find(set.getQuestionId())
                .map(q -> HypothesisViews.brief(q, service.latestByForecaster(q.getId()))).orElse(null);
    }

    // ------------------------------------------------------------------------- create

    /**
     * Creates a set with its hypotheses, indicators (and their watch rules), key assumptions and ledger question, and
     * records its {@code created} revision.
     *
     * @param draft the parsed input
     * @param origin {@code operator}, {@code agent} or {@code analyst}
     * @param actor who created it
     * @throws ValidationException 400 {@code HYPOTHESIS_SET_INVALID}, {@code INDICATOR_INVALID} or
     *     {@code RESOLVER_INVALID}
     */
    @Transactional
    public HypothesisSetEntity create(SetDraft draft, String origin, String actor) {
        Instant now = clock.instant();
        List<FieldError> errors = new ArrayList<>();
        if (draft.situationId() != null) {
            SituationRepository repo = situations.getIfAvailable();
            if (repo != null && !repo.existsById(draft.situationId())) {
                errors.add(new FieldError("situationId", "names no situation"));
            }
        }
        ForecastQuestionEntity linked = null;
        if (draft.questionId() != null) {
            ForecastService service = ledger.getIfAvailable();
            linked = service == null ? null : service.find(draft.questionId()).orElse(null);
            if (linked == null) {
                errors.add(new FieldError("questionId", "names no ledger question"));
            }
        }
        for (int i = 0; i < draft.hypotheses().size(); i++) {
            for (String finding : draft.hypotheses().get(i).linkedFindingIds()) {
                if (!findings.existsById(finding)) {
                    errors.add(new FieldError("hypotheses[" + i + "].linkedFindingIds", "names no finding: "
                            + finding));
                }
            }
        }
        for (int i = 0; i < draft.indicators().size(); i++) {
            checkFamily(draft.indicators().get(i).signalFamily(), "indicators[" + i + "].signalFamily", errors);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The hypothesis set is not valid.",
                    errors);
        }

        HypothesisSetEntity set = new HypothesisSetEntity(Ulids.next(now), now);
        set.setQuestion(draft.question());
        set.setDescription(draft.description());
        set.setKind(draft.kind());
        set.setCadence(draft.cadence());
        set.setHorizon(draft.horizon());
        set.setSituationId(draft.situationId());
        set.setRegionCodes(draft.regionCodes());
        set.setThemes(draft.themes());
        set.setOrigin(HypothesisVocabulary.ORIGINS.contains(origin) ? origin : "agent");
        set.setPrivateSet(draft.privateSet());
        set.setCreatedBy(actor);
        set.setNextReviewAt(nextReview(draft.cadence(), now));
        sets.saveAndFlush(set);

        Map<String, String> idByLabel = new LinkedHashMap<>();
        List<HypothesisEntity> hyps = new ArrayList<>();
        for (int i = 0; i < draft.hypotheses().size(); i++) {
            HypothesisDraft d = draft.hypotheses().get(i);
            HypothesisEntity h = new HypothesisEntity(Ulids.next(now), set.getId(), now);
            h.setPosition(i);
            fill(h, d);
            idByLabel.put(d.label(), h.getId());
            hyps.add(h);
        }
        for (int i = 0; i < hyps.size(); i++) {
            HypothesisDraft d = draft.hypotheses().get(i);
            List<String> alternatives = new ArrayList<>();
            for (String alt : d.alternatives()) {
                alternatives.add(idByLabel.getOrDefault(alt, alt));
            }
            hyps.get(i).setAlternatives(alternatives);
        }
        if (draft.ledgerQuestion() != null || linked != null) {
            String resolverType = draft.ledgerQuestion() == null ? null
                    : String.valueOf(draft.ledgerQuestion().resolver().get("type"));
            boolean binary = linked != null ? Ledger.BINARY.equals(linked.getType())
                    : !(set.exclusive() && ("hypothesis-outcome".equals(resolverType) || "manual".equals(resolverType)));
            mapLedgerOutcomes(hyps, linked, binary);
        }
        hypotheses.saveAllAndFlush(hyps);

        if (draft.ledgerQuestion() != null) {
            set.setQuestionId(openQuestion(set, hyps, draft.ledgerQuestion(), actor));
        } else if (linked != null) {
            set.setQuestionId(linked.getId());
        }

        for (int i = 0; i < draft.indicators().size(); i++) {
            IndicatorDraft d = draft.indicators().get(i);
            createIndicatorRow(set, d, idByLabel, set.getOrigin(), "indicators[" + i + "]");
        }
        Set<String> indicatorIds = new LinkedHashSet<>();
        for (IndicatorEntity i : indicators.findBySetIdOrderByIdAsc(set.getId())) {
            indicatorIds.add(i.getId());
        }
        for (int i = 0; i < draft.assumptions().size(); i++) {
            createAssumptionRow(set, draft.assumptions().get(i), indicatorIds, actor, "assumptions[" + i + "]");
        }

        Map<String, Double> starting = new LinkedHashMap<>();
        for (HypothesisEntity h : hyps) {
            starting.put(h.getId(), h.getPrior());
        }
        List<String> citations = new ArrayList<>();
        for (HypothesisEntity h : hyps) {
            Object ref = h.getPriorBasis().get("ref");
            if (ref != null && "base-rate".equals(h.getPriorBasis().get("type"))) {
                citations.add("base-rate:" + ref);
            }
            for (String f : h.getLinkedFindingIds()) {
                citations.add("finding:" + f);
            }
        }
        if (draft.proposalId() != null) {
            citations.add("analyst-proposal:" + draft.proposalId());
        }
        writer.created(set, actor, starting, citations);
        HypothesisSetEntity saved = sets.findById(set.getId()).orElse(set);
        events.set(HypothesisEvents.SET_CREATED, saved, hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId()),
                saved.getLastRevisionId(), "created", List.of());
        return saved;
    }

    private void fill(HypothesisEntity h, HypothesisDraft d) {
        h.setLabel(d.label());
        h.setStatement(d.statement());
        h.setType(d.type());
        h.setPrior(d.prior());
        Map<String, Object> basis = new LinkedHashMap<>(d.priorBasis());
        if ("base-rate".equals(basis.get("type")) && basis.get("value") == null && basis.get("ref") != null) {
            BaseRateRepository repo = baseRates.getIfAvailable();
            if (repo != null) {
                repo.findById(basis.get("ref").toString()).ifPresent(rate -> basis.put("value",
                        rate.getPAtLeastOne()));
            }
        }
        h.setPriorBasis(basis);
        h.setProbability(d.prior());
        h.setLedgerOutcome(d.ledgerOutcome());
        h.setLinkedFindingIds(d.linkedFindingIds());
        h.setCoincidenceBaseline(d.coincidenceBaseline());
        if (HypothesisVocabulary.SPECULATIVE_LINK.equals(d.type()) && d.coincidenceBaseline() != null) {
            Double rate = JsonValues.number(d.coincidenceBaseline(), "placeboRate");
            h.setProbabilityCap(rate == null ? null : HypothesisUpdater.cap(d.prior(), rate));
        }
    }

    private void checkFamily(String family, String field, List<FieldError> errors) {
        SignalFamilyRegistry registry = families.getIfAvailable();
        if (registry != null && family != null && !registry.known(family)) {
            errors.add(new FieldError(field, "'" + family + "' is not a 119 signal family"));
        }
    }

    /** The next review of a cadence. */
    static Instant nextReview(String cadence, Instant from) {
        return switch (cadence == null ? "weekly" : cadence) {
            case "daily" -> from.plus(Duration.ofDays(1));
            case "monthly" -> from.plus(Duration.ofDays(30));
            default -> from.plus(Duration.ofDays(7));
        };
    }

    /**
     * Fills default ledger outcomes: for a set answered by a multiple-choice question each hypothesis maps to its own
     * label unless it names one; a binary question needs at least one hypothesis mapped to {@code yes}.
     */
    private void mapLedgerOutcomes(List<HypothesisEntity> hyps, ForecastQuestionEntity linked, boolean binary) {
        List<FieldError> errors = new ArrayList<>();
        if (linked != null && !binary) {
            List<String> labels = linked.labels();
            for (HypothesisEntity h : hyps) {
                if (h.getLedgerOutcome() != null && !labels.contains(h.getLedgerOutcome())) {
                    errors.add(new FieldError("hypotheses.ledgerOutcome", "'" + h.getLedgerOutcome() + "' is not an"
                            + " outcome of question " + linked.getId()));
                }
            }
        }
        if (binary) {
            boolean yes = false;
            for (HypothesisEntity h : hyps) {
                if (h.getLedgerOutcome() != null && !Ledger.YES_NO.contains(h.getLedgerOutcome())) {
                    errors.add(new FieldError("hypotheses.ledgerOutcome", "a binary question's outcomes are yes and"
                            + " no"));
                }
                yes |= "yes".equals(h.getLedgerOutcome());
            }
            if (!yes && linked == null) {
                // A machine resolver answers yes or no: the hypotheses it confirms must say so.
                errors.add(new FieldError("hypotheses.ledgerOutcome", "map at least one hypothesis to the outcome"
                        + " yes"));
            }
        } else if (linked == null) {
            for (HypothesisEntity h : hyps) {
                if (h.getLedgerOutcome() == null) {
                    h.setLedgerOutcome(h.getLabel());
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The ledger mapping is not valid.",
                    errors);
        }
    }

    /**
     * Opens the set's ledger question (prompt 125 §5): a {@code hypothesis-outcome} or {@code manual} resolver gives a
     * multiple-choice question over the hypotheses' outcomes for an exclusive set (binary otherwise); a machine
     * resolver gives a binary question. It closes and resolves at the end of the horizon day (UTC).
     */
    private String openQuestion(HypothesisSetEntity set, List<HypothesisEntity> hyps,
            HypothesisInputs.LedgerQuestionDraft draft, String actor) {
        ForecastService service = ledger.getIfAvailable();
        if (service == null) {
            return null;
        }
        Map<String, Object> resolver = new LinkedHashMap<>(draft.resolver());
        String type = String.valueOf(resolver.get("type"));
        boolean labeled = set.exclusive() && ("hypothesis-outcome".equals(type) || "manual".equals(type));
        if ("hypothesis-outcome".equals(type)) {
            resolver.put("setId", set.getId());
        }
        String questionType = labeled ? Ledger.MULTIPLE : Ledger.BINARY;
        Map<String, Object> normalized = ResolverSpec.validate(resolver, questionType);
        Map<String, Object> outcomesSpec = new LinkedHashMap<>();
        if (labeled) {
            List<String> labels = new ArrayList<>();
            for (HypothesisEntity h : hyps) {
                if (h.getLedgerOutcome() != null && !labels.contains(h.getLedgerOutcome())) {
                    labels.add(h.getLedgerOutcome());
                }
            }
            outcomesSpec.put("labels", labels);
        }
        Instant closes = set.getHorizon().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        if (!closes.isAfter(clock.instant())) {
            throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The set is not valid.",
                    List.of(new FieldError("horizon", "a ledger question needs a horizon in the future")));
        }
        String criteria = draft.resolutionCriteria() != null ? draft.resolutionCriteria()
                : "hypothesis-outcome".equals(type) || "manual".equals(type)
                        ? "Resolves to the outcome of the hypothesis that the operator confirms for the key question: "
                                + set.getQuestion()
                        : "Resolves yes when the " + type + " resolver finds what it describes by the end of "
                                + set.getHorizon() + " (UTC); otherwise no. Key question: " + set.getQuestion();
        if (criteria.length() > ForecastInputs.MAX_CRITERIA) {
            criteria = criteria.substring(0, ForecastInputs.MAX_CRITERIA);
        }
        Map<String, String> links = new LinkedHashMap<>();
        links.put("hypothesisSetId", set.getId());
        if (set.getSituationId() != null) {
            links.put("situationId", set.getSituationId());
        }
        ForecastInputs.QuestionDraft question = new ForecastInputs.QuestionDraft(set.getQuestion(), questionType,
                outcomesSpec, criteria, normalized, clock.instant(), closes, closes,
                draft.category() == null ? "hypothesis" : draft.category(), List.of("hypothesis"), links,
                "hypothesis-set:" + set.getId(), set.isPrivateSet());
        return service.openQuestion(question, set.getOrigin(), actor).question().getId();
    }

    // ------------------------------------------------------------------------- set changes

    /**
     * Applies a {@code HypothesisSetPatch} (description, cadence, status) with optimistic locking.
     *
     * @throws ConflictException 409 {@code HYPOTHESIS_SET_VERSION_CONFLICT} or {@code HYPOTHESIS_SET_RESOLVED}
     */
    @Transactional
    public HypothesisSetEntity patch(String id, Map<String, Object> patch, boolean seesPrivate) {
        get(id, seesPrivate);
        HypothesisSetEntity set = locked(id);
        checkVersion(set, patch.get("version"));
        requireUnresolved(set);
        Instant now = clock.instant();
        if (patch.containsKey("description")) {
            set.setDescription(JsonValues.text(patch, "description"));
        }
        if (patch.containsKey("cadence")) {
            set.setCadence(JsonValues.text(patch, "cadence"));
            set.setNextReviewAt(nextReview(set.getCadence(), set.getLastReviewedAt() == null ? now
                    : set.getLastReviewedAt()));
        }
        if (patch.containsKey("status")) {
            set.setStatus(JsonValues.text(patch, "status"));
        }
        set.touch(now);
        return sets.saveAndFlush(set);
    }

    // ------------------------------------------------------------------------- hypotheses

    /**
     * Adds a hypothesis to a set; an exclusive set is renormalized and an {@code operator} revision is recorded.
     *
     * @throws ValidationException 400 {@code HYPOTHESIS_SET_INVALID}
     */
    @Transactional
    public HypothesisEntity addHypothesis(String setId, HypothesisDraft draft, String actor, boolean seesPrivate) {
        get(setId, seesPrivate);
        HypothesisSetEntity set = locked(setId);
        requireUnresolved(set);
        List<HypothesisEntity> existing = hypotheses.findBySetIdOrderByPositionAscIdAsc(setId);
        List<FieldError> errors = new ArrayList<>();
        Map<String, String> idByLabel = new LinkedHashMap<>();
        for (HypothesisEntity h : existing) {
            idByLabel.put(h.getLabel(), h.getId());
            if (h.getLabel().equals(draft.label())) {
                errors.add(new FieldError("label", "labels must be unique within a set"));
            }
            if (HypothesisVocabulary.OTHER.equals(h.getType()) && HypothesisVocabulary.OTHER.equals(draft.type())
                    && set.exclusive()) {
                errors.add(new FieldError("type", "the set already has its other hypothesis"));
            }
        }
        List<String> alternatives = new ArrayList<>();
        for (String alt : draft.alternatives()) {
            String id = idByLabel.containsKey(alt) ? idByLabel.get(alt) : alt;
            if (!idByLabel.containsValue(id)) {
                errors.add(new FieldError("alternatives", "'" + alt + "' is not a hypothesis of this set"));
            } else if (!alternatives.contains(id)) {
                alternatives.add(id);
            }
        }
        if (HypothesisVocabulary.SPECULATIVE_LINK.equals(draft.type()) && alternatives.size() < 2) {
            errors.add(new FieldError("alternatives", "a speculative link needs at least 2 alternatives in the same"
                    + " set"));
        }
        for (String f : draft.linkedFindingIds()) {
            if (!findings.existsById(f)) {
                errors.add(new FieldError("linkedFindingIds", "names no finding: " + f));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The hypothesis is not valid.", errors);
        }
        Instant now = clock.instant();
        HypothesisEntity h = new HypothesisEntity(Ulids.next(now), setId, now);
        h.setPosition(existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getPosition() + 1);
        fill(h, draft);
        h.setAlternatives(alternatives);
        hypotheses.saveAndFlush(h);
        Map<String, Double> values = new LinkedHashMap<>();
        values.put(h.getId(), draft.prior());
        writer.write(set, new RevisionWriter.Request("operator", actor, List.of(), List.of(), List.of(),
                "A hypothesis was added at its prior and the others were renormalized.", "operator", values,
                false));
        return hypotheses.findById(h.getId()).orElse(h);
    }

    /**
     * Applies a {@code HypothesisPatch}; a probability override is an {@code operator} revision (logged; a
     * speculative link exceeds its cap only with {@code exceedCap}).
     *
     * @throws NotFoundException 404 {@code UNKNOWN_HYPOTHESIS}
     */
    @Transactional
    public HypothesisEntity patchHypothesis(String id, Map<String, Object> patch, String actor, boolean seesPrivate) {
        HypothesisEntity h = hypotheses.findById(id == null ? "" : id).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_HYPOTHESIS, "No hypothesis '" + id + "'."));
        HypothesisSetEntity visible = sets.findById(h.getSetId()).filter(s -> seesPrivate || !s.isPrivateSet())
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_HYPOTHESIS, "No hypothesis '" + id
                        + "'."));
        HypothesisSetEntity set = locked(visible.getId());
        requireUnresolved(set);
        if (patch.get("version") instanceof Number v && v.intValue() != h.getVersion()) {
            throw new ConflictException(ErrorCodes.HYPOTHESIS_SET_VERSION_CONFLICT, "The hypothesis is at version "
                    + h.getVersion() + ", not " + v.intValue() + ".");
        }
        Instant now = clock.instant();
        if (patch.containsKey("label")) {
            String label = JsonValues.text(patch, "label");
            for (HypothesisEntity other : hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId())) {
                if (!other.getId().equals(h.getId()) && other.getLabel().equals(label)) {
                    throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The patch is not valid.",
                            List.of(new FieldError("label", "labels must be unique within a set")));
                }
            }
            h.setLabel(label);
        }
        if (patch.containsKey("statement")) {
            h.setStatement(JsonValues.text(patch, "statement"));
        }
        if (patch.containsKey("priorBasis")) {
            h.setPriorBasis(HypothesisInputs.asMap(patch.get("priorBasis")));
        }
        if (patch.containsKey("ledgerOutcome")) {
            h.setLedgerOutcome(JsonValues.text(patch, "ledgerOutcome"));
        }
        if (patch.containsKey("alternatives")) {
            List<String> alternatives = JsonValues.strings(patch, "alternatives");
            Set<String> ids = new LinkedHashSet<>();
            for (HypothesisEntity other : hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId())) {
                if (!other.getId().equals(h.getId())) {
                    ids.add(other.getId());
                }
            }
            if (!ids.containsAll(alternatives) || (h.speculative() && alternatives.size() < 2)) {
                throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The patch is not valid.",
                        List.of(new FieldError("alternatives", "alternatives are at least 2 other hypotheses of the"
                                + " same set")));
            }
            h.setAlternatives(alternatives);
        }
        h.touch(now);
        hypotheses.saveAndFlush(h);
        Map<String, Object> override = JsonValues.child(patch, "probabilityOverride");
        if (override != null) {
            Map<String, Double> values = new LinkedHashMap<>();
            values.put(h.getId(), JsonValues.number(override, "probability"));
            writer.write(set, new RevisionWriter.Request("operator", actor, List.of(), List.of(), List.of(),
                    JsonValues.text(override, "rationale"), "operator", values,
                    Boolean.TRUE.equals(override.get("exceedCap"))));
        }
        return hypotheses.findById(h.getId()).orElse(h);
    }

    // ------------------------------------------------------------------------- indicators

    /**
     * Adds an indicator to a set and creates its watch rule.
     *
     * @throws ValidationException 400 {@code INDICATOR_INVALID}
     */
    @Transactional
    public IndicatorEntity createIndicator(String setId, IndicatorDraft draft, String origin, boolean seesPrivate) {
        get(setId, seesPrivate);
        HypothesisSetEntity set = locked(setId);
        requireUnresolved(set);
        Map<String, String> idByKey = new LinkedHashMap<>();
        for (HypothesisEntity h : hypotheses.findBySetIdOrderByPositionAscIdAsc(setId)) {
            idByKey.put(h.getLabel(), h.getId());
            idByKey.put(h.getId(), h.getId());
        }
        List<FieldError> errors = new ArrayList<>();
        checkFamily(draft.signalFamily(), "signalFamily", errors);
        for (String key : draft.lr().keySet()) {
            if (!idByKey.containsKey(key)) {
                errors.add(new FieldError("lr." + key, "names no hypothesis of this set"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.INDICATOR_INVALID, "The indicator is not valid.", errors);
        }
        return createIndicatorRow(set, draft, idByKey, HypothesisVocabulary.ORIGINS.contains(origin) ? origin
                : "agent", "");
    }

    private IndicatorEntity createIndicatorRow(HypothesisSetEntity set, IndicatorDraft d, Map<String, String> idByKey,
            String origin, String field) {
        Instant now = clock.instant();
        IndicatorEntity i = new IndicatorEntity(Ulids.next(now), set.getId(), now);
        i.setDescription(d.description());
        i.setSignal(d.signal());
        i.setSignalFamily(d.signalFamily());
        i.setCredibility(d.credibility());
        i.setRelevance(d.relevance());
        i.setExpectedBy(d.expectedBy());
        Map<String, Object> lr = new LinkedHashMap<>();
        d.lr().forEach((key, value) -> lr.put(idByKey.getOrDefault(key, key), value));
        i.setLr(lr);
        Map<String, Object> absence = new LinkedHashMap<>();
        d.absenceLr().forEach((key, value) -> absence.put(idByKey.getOrDefault(key, key), value));
        i.setAbsenceLr(absence);
        i.setOrigin(origin);
        try {
            watch.validateSignal(i.getId(), i.getDescription(), i.getSignal());
        } catch (ValidationException e) {
            if (field.isEmpty()) {
                throw e;
            }
            List<FieldError> errors = new ArrayList<>();
            for (FieldError error : e.getErrors()) {
                errors.add(new FieldError(field + "." + error.field(), error.message()));
            }
            throw new ValidationException(ErrorCodes.INDICATOR_INVALID, e.getMessage(), errors);
        }
        indicators.saveAndFlush(i);
        watch.sync(i);
        return indicators.saveAndFlush(i);
    }

    /**
     * Applies an {@code IndicatorPatch}: a signal edit replaces the watch rule; a manual indicator may be set
     * {@code observed} or {@code absent}, which calls the updater.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_INDICATOR}
     * @throws ValidationException 400 {@code INDICATOR_INVALID}
     */
    @Transactional
    public IndicatorEntity patchIndicator(String id, Map<String, Object> patch, String actor, boolean seesPrivate) {
        IndicatorEntity i = visibleIndicator(id, seesPrivate);
        HypothesisSetEntity set = locked(i.getSetId());
        requireUnresolved(set);
        if (HypothesisVocabulary.RETIRED.equals(i.getState())) {
            throw new ValidationException(ErrorCodes.INDICATOR_INVALID, "The indicator is retired.",
                    List.of(new FieldError("state", "a retired indicator does not change")));
        }
        Instant now = clock.instant();
        boolean resync = false;
        if (patch.containsKey("description")) {
            i.setDescription(JsonValues.text(patch, "description"));
            resync = true;
        }
        if (patch.containsKey("signal")) {
            i.setSignal(HypothesisInputs.asMap(patch.get("signal")));
            resync = true;
        }
        if (patch.containsKey("signalFamily")) {
            List<FieldError> errors = new ArrayList<>();
            checkFamily(JsonValues.text(patch, "signalFamily"), "signalFamily", errors);
            if (!errors.isEmpty()) {
                throw new ValidationException(ErrorCodes.INDICATOR_INVALID, "The patch is not valid.", errors);
            }
            i.setSignalFamily(JsonValues.text(patch, "signalFamily"));
        }
        if (patch.containsKey("credibility")) {
            i.setCredibility(JsonValues.text(patch, "credibility"));
        }
        if (patch.containsKey("relevance")) {
            i.setRelevance(JsonValues.text(patch, "relevance"));
        }
        if (patch.containsKey("expectedBy")) {
            String text = JsonValues.text(patch, "expectedBy");
            i.setExpectedBy(text == null ? null : JsonValues.instant(text));
        }
        if (patch.containsKey("lr") || patch.containsKey("absenceLr")) {
            Map<String, String> idByKey = new LinkedHashMap<>();
            for (HypothesisEntity h : hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId())) {
                idByKey.put(h.getLabel(), h.getId());
                idByKey.put(h.getId(), h.getId());
            }
            if (patch.containsKey("lr")) {
                Map<String, Object> lr = new LinkedHashMap<>();
                for (Map.Entry<String, Object> e : HypothesisInputs.asMap(patch.get("lr")).entrySet()) {
                    if (!idByKey.containsKey(e.getKey())) {
                        throw new ValidationException(ErrorCodes.INDICATOR_INVALID, "The patch is not valid.",
                                List.of(new FieldError("lr." + e.getKey(), "names no hypothesis of this set")));
                    }
                    lr.put(idByKey.get(e.getKey()), e.getValue());
                }
                i.setLr(lr);
            }
            if (patch.containsKey("absenceLr")) {
                Map<String, Object> absence = new LinkedHashMap<>();
                for (Map.Entry<String, Object> e : HypothesisInputs.asMap(patch.get("absenceLr")).entrySet()) {
                    if (idByKey.containsKey(e.getKey())) {
                        absence.put(idByKey.get(e.getKey()), e.getValue());
                    }
                }
                i.setAbsenceLr(absence);
            }
        }
        if (resync) {
            watch.validateSignal(i.getId(), i.getDescription(), i.getSignal());
            watch.sync(i);
        }
        i.touch(now);
        indicators.saveAndFlush(i);
        String state = JsonValues.text(patch, "state");
        if (state != null) {
            if (!"manual".equals(i.signalType())) {
                throw new ValidationException(ErrorCodes.INDICATOR_INVALID, "The patch is not valid.",
                        List.of(new FieldError("state", "only manual indicators are set observed or absent by hand")));
            }
            if (HypothesisVocabulary.OBSERVED.equals(state)) {
                Map<String, Object> evidenceMap = new LinkedHashMap<>();
                Map<String, Object> given = JsonValues.child(patch, "observedEvidence");
                if (given != null) {
                    evidenceMap.putAll(given);
                }
                evidenceMap.putIfAbsent("source", "manual");
                evidenceMap.putIfAbsent("by", actor);
                evidenceMap.putIfAbsent("at", now.toString());
                observeLocked(set, i, evidenceMap, HypothesisVocabulary.OBSERVED);
            } else {
                absentLocked(set, i, 1.0, null);
            }
        }
        return indicators.findById(i.getId()).orElse(i);
    }

    /** Retires an indicator: state {@code retired}, its watch rules disabled, its history kept. */
    @Transactional
    public IndicatorEntity retireIndicator(String id, boolean seesPrivate) {
        IndicatorEntity i = visibleIndicator(id, seesPrivate);
        locked(i.getSetId());
        i.setState(HypothesisVocabulary.RETIRED, clock.instant());
        watch.sync(i);
        i.touch(clock.instant());
        return indicators.saveAndFlush(i);
    }

    private IndicatorEntity visibleIndicator(String id, boolean seesPrivate) {
        IndicatorEntity i = indicators.findById(id == null ? "" : id).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_INDICATOR, "No indicator '" + id + "'."));
        if (sets.findById(i.getSetId()).filter(s -> seesPrivate || !s.isPrivateSet()).isEmpty()) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_INDICATOR, "No indicator '" + id + "'.");
        }
        return i;
    }

    // ------------------------------------------------------------------------- observations

    /**
     * Records an observation of an indicator (from its watch rule, a finding, a question or by hand): the indicator
     * moves to {@code observed} (or {@code emerging} for a first-stage alert), the evidence becomes an ACH row of its
     * 116 evidence group, and the updater revises the set ({@code indicator-observed}). Only waiting indicators of
     * active sets change.
     */
    @Transactional
    public void observe(String indicatorId, Map<String, Object> evidenceMap, String state) {
        IndicatorEntity probe = indicators.findById(indicatorId).orElse(null);
        if (probe == null) {
            return;
        }
        HypothesisSetEntity set = locked(probe.getSetId());
        IndicatorEntity i = indicators.findById(indicatorId).orElse(probe);
        observeLocked(set, i, evidenceMap, state);
    }

    private void observeLocked(HypothesisSetEntity set, IndicatorEntity i, Map<String, Object> evidenceMap,
            String state) {
        if (!set.active() || !(i.waiting() || HypothesisVocabulary.UNKNOWN_COVERAGE.equals(i.getState()))) {
            return;
        }
        Instant now = clock.instant();
        String previous = i.getState();
        if (HypothesisVocabulary.EMERGING.equals(state)) {
            i.setState(HypothesisVocabulary.EMERGING, now);
            i.setObservedEvidence(evidenceMap);
            i.touch(now);
            indicators.saveAndFlush(i);
            return;
        }
        Map<String, Object> observed = new LinkedHashMap<>(evidenceMap);
        String key = evidenceKey(observed);
        observed.put("evidenceKey", key);
        i.setObservedEvidence(observed);
        i.setState(HypothesisVocabulary.OBSERVED, now);
        i.touch(now);
        stopWatching(i);
        indicators.saveAndFlush(i);
        List<HypothesisEntity> hyps = hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId());
        Map<String, Double> lr = observedLr(i, hyps);
        attachRow(set, i, observed, key, lr, hyps, now);
        String ref = observed.get("ref") == null ? null : observed.get("ref").toString();
        HypothesisUpdater.Driver driver = new HypothesisUpdater.Driver("indicator", i.getId(), ref, key,
                i.getSignalFamily(), lr, null, true);
        RevisionRationale.DriverNote note = new RevisionRationale.DriverNote(i.getDescription(), "observed",
                strongest(lr), null, false);
        HypothesisRevisionEntity revision = writer.write(set, RevisionWriter.Request.automatic("indicator-observed",
                List.of(driver), List.of(note), ref == null ? List.of() : List.of(ref)));
        events.indicator(HypothesisEvents.INDICATOR_OBSERVED, i, previous, revision.getId(), set.isPrivateSet());
        notifications.indicatorObserved(set, i);
    }

    /**
     * Records that an indicator did not appear by {@code expected_by} while the sources that would have seen it were
     * healthy and detectability sufficed: the indicator moves to {@code absent} and the updater applies its absence
     * LRs scaled by {@code pDetect} ({@code indicator-absent}).
     */
    @Transactional
    public void absent(String indicatorId, double pDetect, String ref) {
        IndicatorEntity probe = indicators.findById(indicatorId).orElse(null);
        if (probe == null) {
            return;
        }
        HypothesisSetEntity set = locked(probe.getSetId());
        IndicatorEntity i = indicators.findById(indicatorId).orElse(probe);
        absentLocked(set, i, pDetect, ref);
    }

    private void absentLocked(HypothesisSetEntity set, IndicatorEntity i, double pDetect, String ref) {
        if (!set.active() || !(i.waiting() || HypothesisVocabulary.UNKNOWN_COVERAGE.equals(i.getState()))) {
            return;
        }
        Instant now = clock.instant();
        String previous = i.getState();
        i.setState(HypothesisVocabulary.ABSENT, now);
        i.setPDetect(pDetect);
        i.touch(now);
        stopWatching(i);
        indicators.saveAndFlush(i);
        List<HypothesisEntity> hyps = hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId());
        Map<String, Double> lr = absenceLr(i, hyps, pDetect);
        HypothesisUpdater.Driver driver = new HypothesisUpdater.Driver("indicator", i.getId(), ref,
                "absence:" + i.getId(), i.getSignalFamily(), lr, pDetect, true);
        RevisionRationale.DriverNote note = new RevisionRationale.DriverNote(i.getDescription(), "absent",
                strongest(lr), null, false);
        HypothesisRevisionEntity revision = writer.write(set, RevisionWriter.Request.automatic("indicator-absent",
                List.of(driver), List.of(note), ref == null ? List.of() : List.of(ref)));
        events.indicator(HypothesisEvents.INDICATOR_ABSENT, i, previous, revision.getId(), set.isPrivateSet());
    }

    /** Records that an indicator's expected time passed without the coverage to read its absence (no update). */
    @Transactional
    public void unknownCoverage(String indicatorId, Double pDetect, String reason) {
        indicators.findById(indicatorId).ifPresent(i -> {
            if (!i.waiting()) {
                return;
            }
            Instant now = clock.instant();
            i.setState(HypothesisVocabulary.UNKNOWN_COVERAGE, now);
            i.setPDetect(pDetect);
            Map<String, Object> note = new LinkedHashMap<>();
            note.put("source", "coverage");
            note.put("reason", reason);
            note.put("at", now.toString());
            i.setObservedEvidence(note);
            i.touch(now);
            indicators.saveAndFlush(i);
        });
    }

    /** The effective observed LR per hypothesis of an indicator (hypotheses without a likelihood are left out). */
    static Map<String, Double> observedLr(IndicatorEntity i, List<HypothesisEntity> hyps) {
        Map<String, Double> out = new LinkedHashMap<>();
        for (HypothesisEntity h : hyps) {
            Object likelihood = i.getLr().get(h.getId());
            if (likelihood != null) {
                out.put(h.getId(), LikelihoodRatios.observed(HypothesisInputs.asMap(likelihood)));
            }
        }
        return out;
    }

    /** The effective absence LR per hypothesis: the override, else the default, scaled by {@code pDetect}. */
    static Map<String, Double> absenceLr(IndicatorEntity i, List<HypothesisEntity> hyps, double pDetect) {
        Map<String, Double> out = new LinkedHashMap<>();
        for (HypothesisEntity h : hyps) {
            Object likelihood = i.getLr().get(h.getId());
            if (likelihood == null) {
                continue;
            }
            Object override = i.getAbsenceLr().get(h.getId());
            double absence = override instanceof Number n ? LikelihoodRatios.bound(n.doubleValue())
                    : LikelihoodRatios.defaultAbsence(LikelihoodRatios.observed(HypothesisInputs.asMap(likelihood)));
            out.put(h.getId(), LikelihoodRatios.absenceEffective(absence, pDetect));
        }
        return out;
    }

    private static Double strongest(Map<String, Double> lr) {
        Double best = null;
        for (double v : lr.values()) {
            if (best == null || Math.abs(Math.log(v)) > Math.abs(Math.log(best))) {
                best = v;
            }
        }
        return best;
    }

    /**
     * The evidence key of an observation: its 116 evidence group ({@code group:{id}}) for a record, else the record
     * without its version, else the finding, question or a fresh key for a manual observation.
     */
    String evidenceKey(Map<String, Object> observed) {
        Object ref = observed.get("ref");
        if (ref != null && ref.toString().startsWith("record:")) {
            EvidenceService service = evidenceGroups.getIfAvailable();
            if (service != null) {
                try {
                    Optional<EvidenceService.GroupKey> group = service.groupOf(ref.toString());
                    if (group.isPresent()) {
                        observed.put("evidenceGroupId", group.get().groupId());
                        return "group:" + group.get().groupId();
                    }
                } catch (RuntimeException e) {
                    log.debug("No evidence group for {}: {}", ref, e.toString());
                }
            }
            String text = ref.toString();
            int at = text.lastIndexOf('@');
            return "ref:" + (at > 0 ? text.substring(0, at) : text);
        }
        if (ref != null) {
            return "ref:" + ref;
        }
        return "manual:" + Ulids.next(clock.instant());
    }

    /** Adds an observed indicator's ACH row, or attaches the observation to the live row of its evidence group. */
    private void attachRow(HypothesisSetEntity set, IndicatorEntity i, Map<String, Object> observed, String key,
            Map<String, Double> lr, List<HypothesisEntity> hyps, Instant now) {
        String ref = observed.get("ref") == null ? null : observed.get("ref").toString();
        List<AchEvidenceEntity> existing = evidence.findBySetIdAndEvidenceGroupKeyAndRemovedAtIsNull(set.getId(), key);
        if (!existing.isEmpty()) {
            AchEvidenceEntity row = existing.get(0);
            List<String> refs = new ArrayList<>(row.getRefs());
            if (ref != null && !refs.contains(ref)) {
                refs.add(ref);
            }
            row.setRefs(refs);
            evidence.save(row);
            return;
        }
        AchEvidenceEntity row = new AchEvidenceEntity(Ulids.next(now), set.getId(), "indicator", "indicator-bayes",
                now);
        row.setRef(ref);
        row.setRefs(ref == null ? List.of() : List.of(ref));
        row.setEvidenceGroupKey(key);
        Object group = observed.get("evidenceGroupId");
        row.setEvidenceGroupId(group == null ? null : group.toString());
        row.setDescription(i.getDescription());
        row.setCredibility(i.getCredibility());
        row.setRelevance(i.getRelevance());
        List<String> ids = new ArrayList<>();
        for (HypothesisEntity h : hyps) {
            ids.add(h.getId());
        }
        row.setRatings(new LinkedHashMap<>(AchMatrix.ratingsFromLr(ids, lr)));
        row.setIndicatorId(i.getId());
        evidence.save(row);
    }

    // ------------------------------------------------------------------------- evidence

    /**
     * Adds ACH evidence (re-reports attach to the live row of their 116 evidence group). Manual evidence whose ratings
     * carry likelihood records an {@code evidence-added} revision with the LRs its ratings imply.
     */
    @Transactional
    public AchEvidenceEntity addEvidence(String setId, EvidenceDraft draft, String actor, boolean seesPrivate) {
        get(setId, seesPrivate);
        HypothesisSetEntity set = locked(setId);
        requireUnresolved(set);
        List<HypothesisEntity> hyps = hypotheses.findBySetIdOrderByPositionAscIdAsc(setId);
        Set<String> ids = new LinkedHashSet<>();
        for (HypothesisEntity h : hyps) {
            ids.add(h.getId());
        }
        for (String key : draft.ratings().keySet()) {
            if (!ids.contains(key)) {
                throw new ValidationException("The evidence is not valid.", List.of(new FieldError("ratings." + key,
                        "names no hypothesis of this set")));
            }
        }
        Instant now = clock.instant();
        Map<String, Object> probe = new LinkedHashMap<>();
        if (draft.ref() != null) {
            probe.put("ref", draft.ref());
        }
        String key = evidenceKey(probe);
        List<AchEvidenceEntity> existing = evidence.findBySetIdAndEvidenceGroupKeyAndRemovedAtIsNull(setId, key);
        if (!existing.isEmpty()) {
            AchEvidenceEntity row = existing.get(0);
            List<String> refs = new ArrayList<>(row.getRefs());
            if (draft.ref() != null && !refs.contains(draft.ref())) {
                refs.add(draft.ref());
            }
            row.setRefs(refs);
            return evidence.save(row);
        }
        AchEvidenceEntity row = new AchEvidenceEntity(Ulids.next(now), setId, draft.source(), actor == null
                ? "operator" : actor, now);
        row.setRef(draft.ref());
        row.setRefs(draft.ref() == null ? List.of() : List.of(draft.ref()));
        row.setEvidenceGroupKey(key);
        Object group = probe.get("evidenceGroupId");
        row.setEvidenceGroupId(group == null ? null : group.toString());
        row.setDescription(draft.description());
        row.setCredibility(draft.credibility());
        row.setRelevance(draft.relevance());
        Map<String, Object> ratings = new LinkedHashMap<>();
        for (String id : ids) {
            ratings.put(id, draft.ratings().getOrDefault(id, "NA"));
        }
        row.setRatings(ratings);
        row.setCitations(draft.citations());
        evidence.saveAndFlush(row);
        Map<String, Double> lr = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : draft.ratings().entrySet()) {
            double value = LikelihoodRatios.fromRating(e.getValue());
            if (Math.abs(value - 1.0) > 1e-9) {
                lr.put(e.getKey(), value);
            }
        }
        if (!lr.isEmpty()) {
            HypothesisUpdater.Driver driver = new HypothesisUpdater.Driver("evidence", row.getId(), draft.ref(), key,
                    null, lr, null, true);
            List<String> citations = new ArrayList<>(draft.citations());
            if (draft.ref() != null && !citations.contains(draft.ref())) {
                citations.add(draft.ref());
            }
            HypothesisRevisionEntity revision = writer.write(set, RevisionWriter.Request.automatic("evidence-added",
                    List.of(driver), List.of(new RevisionRationale.DriverNote(draft.description(), "evidence-added",
                            strongest(lr), null, false)), citations));
            Map<String, Object> applied = new LinkedHashMap<>();
            for (Map<String, Object> d : revision.getDrivers()) {
                if (row.getId().equals(d.get("id")) && d.get("applied") instanceof Number n) {
                    applied.merge(String.valueOf(d.get("hypothesisId")), n.doubleValue(), (a, b) ->
                            ((Number) a).doubleValue() + ((Number) b).doubleValue());
                }
            }
            row.setApplied(applied);
            evidence.save(row);
        }
        return row;
    }

    /** Marks an evidence row removed and reverses what it applied ({@code evidence-removed}). */
    @Transactional
    public void removeEvidence(String setId, String evidenceId, String actor, boolean seesPrivate) {
        get(setId, seesPrivate);
        HypothesisSetEntity set = locked(setId);
        requireUnresolved(set);
        AchEvidenceEntity row = evidence.findById(evidenceId == null ? "" : evidenceId)
                .filter(e -> e.getSetId().equals(setId) && e.getRemovedAt() == null)
                .orElseThrow(() -> NotFoundException.of("ACH evidence", evidenceId));
        Instant now = clock.instant();
        row.setRemovedAt(now);
        evidence.save(row);
        Map<String, Double> lr = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : row.getApplied().entrySet()) {
            if (e.getValue() instanceof Number n && Math.abs(n.doubleValue()) > 1e-12) {
                lr.put(e.getKey(), Math.exp(-n.doubleValue()));
            }
        }
        if (!lr.isEmpty()) {
            HypothesisUpdater.Driver driver = new HypothesisUpdater.Driver("evidence", row.getId(), row.getRef(), null,
                    null, lr, null, false);
            writer.write(set, new RevisionWriter.Request("evidence-removed", actor == null ? "operator" : actor,
                    List.of(driver), List.of(new RevisionRationale.DriverNote(row.getDescription(),
                            "evidence-removed", null, null, false)), row.getRef() == null ? List.of()
                            : List.of(row.getRef()), null, "deterministic", null, false));
        }
    }

    /** The live ACH rows of a set. */
    public List<AchEvidenceEntity> liveEvidence(String setId) {
        List<AchEvidenceEntity> out = new ArrayList<>();
        for (AchEvidenceEntity e : evidence.findBySetIdOrderByIdAsc(setId)) {
            if (e.getRemovedAt() == null) {
                out.add(e);
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- assumptions

    /** Adds a key assumption. */
    @Transactional
    public KeyAssumptionEntity createAssumption(String setId, AssumptionDraft draft, String actor,
            boolean seesPrivate) {
        get(setId, seesPrivate);
        HypothesisSetEntity set = locked(setId);
        Set<String> indicatorIds = new LinkedHashSet<>();
        for (IndicatorEntity i : indicators.findBySetIdOrderByIdAsc(setId)) {
            indicatorIds.add(i.getId());
        }
        KeyAssumptionEntity a = createAssumptionRow(set, draft, indicatorIds, actor, "");
        if (a.brokenLinchpin()) {
            linchpinBroken(set, a, null);
        }
        return a;
    }

    private KeyAssumptionEntity createAssumptionRow(HypothesisSetEntity set, AssumptionDraft d,
            Set<String> indicatorIds, String actor, String field) {
        for (String id : d.indicatorIds()) {
            if (!indicatorIds.contains(id)) {
                throw new ValidationException("The key assumption is not valid.", List.of(new FieldError(
                        (field.isEmpty() ? "" : field + ".") + "indicatorIds", "names no indicator of this set: "
                        + id)));
            }
        }
        Instant now = clock.instant();
        KeyAssumptionEntity a = new KeyAssumptionEntity(Ulids.next(now), set.getId(), now);
        a.setText(d.text());
        a.setLinchpin(d.linchpin());
        a.setRationale(d.rationale());
        a.setWouldBeWrongIf(d.wouldBeWrongIf());
        a.setStatus(d.status());
        a.setIndicatorIds(d.indicatorIds());
        a.setReviewDue(d.reviewDue());
        a.setUpdatedBy(actor);
        return assumptions.saveAndFlush(a);
    }

    /** Applies a {@code KeyAssumptionPatch}; turning {@code broken} emits {@code assumption.broken}. */
    @Transactional
    public KeyAssumptionEntity patchAssumption(String setId, String assumptionId, Map<String, Object> patch,
            String actor, boolean seesPrivate) {
        get(setId, seesPrivate);
        HypothesisSetEntity set = locked(setId);
        KeyAssumptionEntity a = assumptions.findById(assumptionId == null ? "" : assumptionId)
                .filter(x -> x.getSetId().equals(setId))
                .orElseThrow(() -> NotFoundException.of("Key assumption", assumptionId));
        String previous = a.getStatus();
        if (patch.containsKey("text")) {
            a.setText(JsonValues.text(patch, "text"));
        }
        if (patch.containsKey("linchpin")) {
            a.setLinchpin(Boolean.TRUE.equals(patch.get("linchpin")));
        }
        if (patch.containsKey("rationale")) {
            a.setRationale(JsonValues.text(patch, "rationale"));
        }
        if (patch.containsKey("wouldBeWrongIf")) {
            a.setWouldBeWrongIf(JsonValues.text(patch, "wouldBeWrongIf"));
        }
        if (patch.containsKey("status")) {
            a.setStatus(JsonValues.text(patch, "status"));
        }
        if (patch.containsKey("indicatorIds")) {
            Set<String> indicatorIds = new LinkedHashSet<>();
            for (IndicatorEntity i : indicators.findBySetIdOrderByIdAsc(setId)) {
                indicatorIds.add(i.getId());
            }
            List<String> ids = JsonValues.strings(patch, "indicatorIds");
            if (!indicatorIds.containsAll(ids)) {
                throw new ValidationException("The patch is not valid.", List.of(new FieldError("indicatorIds",
                        "names an indicator of another set")));
            }
            a.setIndicatorIds(ids);
        }
        if (patch.containsKey("reviewDue")) {
            String text = JsonValues.text(patch, "reviewDue");
            a.setReviewDue(text == null ? null : JsonValues.instant(text));
        }
        a.setUpdatedBy(actor);
        a.setUpdatedAt(clock.instant());
        assumptions.saveAndFlush(a);
        if ("broken".equals(a.getStatus()) && !"broken".equals(previous)) {
            linchpinBroken(set, a, previous);
        } else {
            refreshConfidence(set);
        }
        return a;
    }

    /** A broken assumption: {@code assumption.broken}; a broken linchpin flags the set for review now. */
    private void linchpinBroken(HypothesisSetEntity set, KeyAssumptionEntity a, String previous) {
        events.assumptionBroken(a, previous, set.isPrivateSet());
        notifications.assumptionBroken(set, a);
        if (a.isLinchpin()) {
            set.setReviewFlagged(true);
            set.setNextReviewAt(clock.instant());
            set.touch(clock.instant());
        }
        refreshConfidence(set);
    }

    private void refreshConfidence(HypothesisSetEntity set) {
        List<HypothesisEntity> hyps = hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId());
        writer.refreshConfidence(set, hyps);
        hypotheses.saveAll(hyps);
        sets.save(set);
    }

    // ------------------------------------------------------------------------- reviews

    /**
     * Records a review (prompt 125 §5): LR, assumption and probability changes, always as a revision with cause
     * {@code review} or {@code operator} and the reviewer's rationale (lint-checked). A set past its horizon without a
     * ledger question is archived by the review.
     *
     * @throws ValidationException 400 {@code HYPOTHESIS_SET_INVALID} or {@code LINT_FAILED}
     * @throws ConflictException 409 {@code HYPOTHESIS_SET_VERSION_CONFLICT} or {@code HYPOTHESIS_SET_RESOLVED}
     */
    @Transactional
    public HypothesisRevisionEntity review(String setId, ReviewDraft draft, String actor, boolean seesPrivate) {
        get(setId, seesPrivate);
        HypothesisSetEntity set = locked(setId);
        requireUnresolved(set);
        if (draft.version() != null) {
            checkVersion(set, draft.version());
        }
        List<HypothesisEntity> hyps = hypotheses.findBySetIdOrderByPositionAscIdAsc(setId);
        Set<String> ids = new LinkedHashSet<>();
        for (HypothesisEntity h : hyps) {
            ids.add(h.getId());
        }
        List<FieldError> errors = new ArrayList<>();
        for (String id : draft.probabilities().keySet()) {
            if (!ids.contains(id)) {
                errors.add(new FieldError("probabilities." + id, "names no hypothesis of this set"));
            }
        }
        Map<String, IndicatorEntity> byId = new HashMap<>();
        for (IndicatorEntity i : indicators.findBySetIdOrderByIdAsc(setId)) {
            byId.put(i.getId(), i);
        }
        for (Map<String, Object> change : draft.lrChanges()) {
            if (!byId.containsKey(String.valueOf(change.get("indicatorId")))
                    || !ids.contains(String.valueOf(change.get("hypothesisId")))) {
                errors.add(new FieldError("lr", "names an indicator or hypothesis of another set"));
            }
        }
        Map<String, KeyAssumptionEntity> assumptionById = new HashMap<>();
        for (KeyAssumptionEntity a : assumptions.findBySetIdOrderByIdAsc(setId)) {
            assumptionById.put(a.getId(), a);
        }
        for (Map<String, Object> change : draft.assumptions()) {
            if (!assumptionById.containsKey(String.valueOf(change.get("assumptionId")))) {
                errors.add(new FieldError("assumptions", "names an assumption of another set"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The review is not valid.", errors);
        }
        Instant now = clock.instant();
        for (Map<String, Object> change : draft.lrChanges()) {
            IndicatorEntity i = byId.get(String.valueOf(change.get("indicatorId")));
            Map<String, Object> lr = new LinkedHashMap<>(i.getLr());
            lr.put(String.valueOf(change.get("hypothesisId")), change.get("likelihood"));
            i.setLr(lr);
            i.touch(now);
            indicators.save(i);
        }
        List<KeyAssumptionEntity> broken = new ArrayList<>();
        Map<String, String> previousStatus = new HashMap<>();
        for (Map<String, Object> change : draft.assumptions()) {
            KeyAssumptionEntity a = assumptionById.get(String.valueOf(change.get("assumptionId")));
            String status = String.valueOf(change.get("status"));
            previousStatus.put(a.getId(), a.getStatus());
            if ("broken".equals(status) && !"broken".equals(a.getStatus())) {
                broken.add(a);
            }
            a.setStatus(status);
            if (change.get("rationale") != null) {
                a.setRationale(change.get("rationale").toString());
            }
            a.setUpdatedBy(actor);
            a.setUpdatedAt(now);
            assumptions.save(a);
        }
        List<String> citations = new ArrayList<>(draft.citations());
        if (draft.proposalId() != null) {
            citations.add("analyst-proposal:" + draft.proposalId());
        }
        HypothesisRevisionEntity revision = writer.write(set, new RevisionWriter.Request(draft.cause(), actor,
                List.of(), List.of(), citations, draft.rationale(), draft.proposalId() != null ? "analyst" : "operator",
                new LinkedHashMap<>(draft.probabilities()), draft.exceedCap()));
        HypothesisSetEntity after = sets.findById(setId).orElse(set);
        for (KeyAssumptionEntity a : broken) {
            events.assumptionBroken(a, previousStatus.get(a.getId()), after.isPrivateSet());
            notifications.assumptionBroken(after, a);
        }
        boolean linchpinBroken = false;
        for (KeyAssumptionEntity a : assumptions.findBySetIdOrderByIdAsc(setId)) {
            linchpinBroken |= a.brokenLinchpin();
        }
        after.setLastReviewedAt(now);
        after.setNextReviewAt(nextReview(after.getCadence(), now));
        after.setReviewFlagged(linchpinBroken && broken.stream().anyMatch(KeyAssumptionEntity::isLinchpin));
        if (after.getHorizon() != null && after.getQuestionId() == null
                && LocalDate.ofInstant(now, ZoneOffset.UTC).isAfter(after.getHorizon())) {
            after.setStatus(HypothesisVocabulary.SET_ARCHIVED);
            for (HypothesisEntity h : hypotheses.findBySetIdOrderByPositionAscIdAsc(setId)) {
                if ("open".equals(h.getStatus())) {
                    h.setStatus("expired");
                    hypotheses.save(h);
                }
            }
            retireWaiting(setId);
        }
        refreshConfidence(after);
        return revision;
    }

    // ------------------------------------------------------------------------- resolution and refresh

    /**
     * A linked ledger question resolved: hypotheses mapped to the outcome are {@code confirmed}, the others
     * {@code refuted}; the set is {@code resolved}, its waiting indicators are retired, and a linked situation's
     * outcome records the set's result.
     */
    @Transactional
    public void questionResolved(String questionId) {
        ForecastService service = ledger.getIfAvailable();
        if (service == null) {
            return;
        }
        ForecastQuestionEntity q = service.find(questionId).orElse(null);
        if (q == null || q.getResolution() == null) {
            return;
        }
        Object outcome = q.getResolution().get("outcome");
        for (HypothesisSetEntity found : sets.findByQuestionId(questionId)) {
            HypothesisSetEntity set = locked(found.getId());
            if (HypothesisVocabulary.SET_RESOLVED.equals(set.getStatus())) {
                continue;
            }
            Instant now = clock.instant();
            List<HypothesisEntity> hyps = hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId());
            for (HypothesisEntity h : hyps) {
                h.setStatus(outcome != null && outcome.toString().equals(h.getLedgerOutcome()) ? "confirmed"
                        : "refuted");
                h.touch(now);
            }
            hypotheses.saveAll(hyps);
            set.setStatus(HypothesisVocabulary.SET_RESOLVED);
            set.setResolvedOutcome(outcome == null ? null : outcome.toString());
            set.touch(now);
            sets.saveAndFlush(set);
            retireWaiting(set.getId());
            events.set(HypothesisEvents.SET_RESOLVED, set, hyps, set.getLastRevisionId(), null, List.of());
            recordSituationOutcome(set);
        }
    }

    private void retireWaiting(String setId) {
        Instant now = clock.instant();
        for (IndicatorEntity i : indicators.findBySetIdOrderByIdAsc(setId)) {
            if (i.waiting() || HypothesisVocabulary.UNKNOWN_COVERAGE.equals(i.getState())) {
                i.setState(HypothesisVocabulary.RETIRED, now);
                try {
                    watch.sync(i);
                } catch (RuntimeException e) {
                    log.warn("Could not disable the watch rule of indicator {}: {}", i.getId(), e.toString());
                }
                i.touch(now);
                indicators.save(i);
            } else {
                stopWatching(i);
                indicators.save(i);
            }
        }
    }

    /** Disables an indicator's watch rules once its state is final (observed, absent); failures are logged. */
    private void stopWatching(IndicatorEntity i) {
        try {
            watch.disable(i);
        } catch (RuntimeException e) {
            log.warn("Could not disable the watch rules of indicator {}: {}", i.getId(), e.toString());
        }
    }

    private void recordSituationOutcome(HypothesisSetEntity set) {
        SituationOutcomeRepository repo = outcomes.getIfAvailable();
        if (set.getSituationId() == null || repo == null) {
            return;
        }
        repo.findById(set.getSituationId()).ifPresent(o -> {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("setId", set.getId());
            value.put("outcome", set.getResolvedOutcome());
            o.setHypothesisOutcome(value);
            repo.save(o);
        });
    }

    /**
     * Re-reads the base rates behind a set's priors (prompt 125 §5): a prior whose base rate moved by more than 20%
     * relative records a {@code base-rate-refresh} revision that shifts the hypothesis by the change in prior
     * log-odds (log-probability in an exclusive set), keeping every evidence contribution.
     *
     * @return true when a revision was written
     */
    @Transactional
    public boolean refreshBaseRates(String setId) {
        BaseRateRepository repo = baseRates.getIfAvailable();
        if (repo == null) {
            return false;
        }
        HypothesisSetEntity set = locked(setId);
        if (!set.active()) {
            return false;
        }
        Map<String, Double> lr = new LinkedHashMap<>();
        List<String> citations = new ArrayList<>();
        List<HypothesisEntity> hyps = hypotheses.findBySetIdOrderByPositionAscIdAsc(setId);
        for (HypothesisEntity h : hyps) {
            Map<String, Object> basis = h.getPriorBasis();
            if (!"base-rate".equals(basis.get("type")) || basis.get("ref") == null || !"open".equals(h.getStatus())) {
                continue;
            }
            Optional<BaseRateEntity> rate = repo.findById(basis.get("ref").toString());
            if (rate.isEmpty()) {
                continue;
            }
            double old = basis.get("value") instanceof Number n ? n.doubleValue() : h.getPrior();
            double fresh = Math.max(0.001, Math.min(0.999, rate.get().getPAtLeastOne()));
            if (old <= 0 || Math.abs(fresh - old) / old <= BASE_RATE_REFRESH_THRESHOLD) {
                continue;
            }
            double shift = set.exclusive() ? fresh / old
                    : Math.exp(HypothesisUpdater.logit(fresh) - HypothesisUpdater.logit(Math.max(0.001,
                            Math.min(0.999, old))));
            lr.put(h.getId(), shift);
            Map<String, Object> updated = new LinkedHashMap<>(basis);
            updated.put("value", fresh);
            updated.put("refreshedAt", clock.instant().toString());
            h.setPriorBasis(updated);
            h.setPrior(fresh);
            hypotheses.save(h);
            citations.add("base-rate:" + rate.get().getId());
        }
        if (lr.isEmpty()) {
            return false;
        }
        HypothesisUpdater.Driver driver = new HypothesisUpdater.Driver("base-rate", null, null, null, null, lr, null,
                false);
        writer.write(set, RevisionWriter.Request.automatic("base-rate-refresh", List.of(driver),
                List.of(new RevisionRationale.DriverNote(null, "base-rate", null, null, false)), citations));
        return true;
    }

    // ------------------------------------------------------------------------- helpers

    /** The set locked for update, or 404. */
    HypothesisSetEntity locked(String id) {
        return sets.findLocked(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_HYPOTHESIS_SET,
                "No hypothesis set '" + id + "'."));
    }

    private static void checkVersion(HypothesisSetEntity set, Object version) {
        if (version instanceof Number v && v.intValue() != set.getVersion()) {
            throw new ConflictException(ErrorCodes.HYPOTHESIS_SET_VERSION_CONFLICT, "The set is at version "
                    + set.getVersion() + ", not " + v.intValue() + ".");
        }
    }

    private static void requireUnresolved(HypothesisSetEntity set) {
        if (HypothesisVocabulary.SET_RESOLVED.equals(set.getStatus())) {
            throw new ConflictException(ErrorCodes.HYPOTHESIS_SET_RESOLVED, "The set is resolved.");
        }
    }

    private ai.foresignal.news.NewsCoverageService newsCoverageService;

    /** Installs the news coverage of supporting evidence (prompt 133: {@code Hypothesis.newsCoverage}). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setNewsCoverageService(ai.foresignal.news.NewsCoverageService newsCoverageService) {
        this.newsCoverageService = newsCoverageService;
    }

    /**
     * The lean and state-affiliation balance of a hypothesis's supporting news evidence, with the
     * {@code SKEWED_COVERAGE} caveat when skewed (prompt 133); null without supporting news evidence.
     */
    private Map<String, Object> newsCoverage(HypothesisEntity h) {
        ai.foresignal.news.NewsCoverageService service = newsCoverageService;
        if (service == null) {
            return null;
        }
        try {
            return service.hypothesisSupport(h.getSetId(), h.getId()).coverage();
        } catch (RuntimeException e) {
            log.debug("No news coverage for hypothesis {}: {}", h.getId(), e.toString());
            return null;
        }
    }
}
