package ai.foresignal.hypothesis;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.baserate.RegionProfileEntity;
import ai.foresignal.baserate.RegionProfileRepository;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.language.EstimativeLanguage;
import ai.foresignal.provenance.CredibilityService;
import ai.foresignal.security.Ulids;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The one write path of hypothesis probabilities (prompt 125 §4.8, "revisions are the record"): runs the
 * {@link HypothesisUpdater} (or a logged direct change), stores the new probabilities, recomputes confidence with the
 * {@link ConfidenceAssessor}, writes the append-only {@code hypothesis_revision} with its drivers and rationale, records
 * the {@code indicator-bayes} ledger forecast when the set has a question, and emits {@code set.revised} plus
 * {@code set.band_changed} (and the rate-limited {@code hypothesis.band_changed} notification) when a band changes.
 * Callers hold the set's row lock ({@link HypothesisSetRepository#findLocked}).
 */
@Component
public class RevisionWriter {

    private static final Logger log = LoggerFactory.getLogger(RevisionWriter.class);

    /** The band-change notification rate limit per set. */
    public static final Duration BAND_NOTIFY_INTERVAL = Duration.ofHours(6);

    /** The ledger forecaster name. */
    public static final String FORECASTER_NAME = "Indicator-Bayes hypothesis updater";

    private final HypothesisSetRepository sets;
    private final HypothesisRepository hypotheses;
    private final HypothesisRevisionRepository revisions;
    private final IndicatorRepository indicators;
    private final KeyAssumptionRepository assumptions;
    private final HypothesisEvents events;
    private final HypothesisNotifications notifications;
    private final ObjectProvider<ForecastService> ledger;
    private final ObjectProvider<ForecasterRegistry> forecasters;
    private final ObjectProvider<CredibilityService> credibility;
    private final ObjectProvider<RegionProfileRepository> regions;
    private final SeqClockQuery seqClock;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the writer. */
    public RevisionWriter(HypothesisSetRepository sets, HypothesisRepository hypotheses,
            HypothesisRevisionRepository revisions, IndicatorRepository indicators,
            KeyAssumptionRepository assumptions, HypothesisEvents events, HypothesisNotifications notifications,
            ObjectProvider<ForecastService> ledger, ObjectProvider<ForecasterRegistry> forecasters,
            ObjectProvider<CredibilityService> credibility, ObjectProvider<RegionProfileRepository> regions,
            SeqClockQuery seqClock, NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.sets = sets;
        this.hypotheses = hypotheses;
        this.revisions = revisions;
        this.indicators = indicators;
        this.assumptions = assumptions;
        this.events = events;
        this.notifications = notifications;
        this.ledger = ledger;
        this.forecasters = forecasters;
        this.credibility = credibility;
        this.regions = regions;
        this.seqClock = seqClock;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * A revision request.
     *
     * @param cause the revision cause
     * @param actor the principal, {@code indicator-bayes} or a component id
     * @param drivers the updater drivers (ignored when {@code overrides} is set)
     * @param notes the drivers for the deterministic rationale
     * @param citations the citations
     * @param rationale an operator or analyst rationale, or null for the deterministic text
     * @param rationaleOrigin {@code deterministic}, {@code operator} or {@code analyst}
     * @param overrides direct probabilities by hypothesis id (reviews and overrides), or null
     * @param exceedCap whether a direct change may exceed speculative-link caps (logged)
     */
    public record Request(String cause, String actor, List<HypothesisUpdater.Driver> drivers,
            List<RevisionRationale.DriverNote> notes, List<String> citations, String rationale, String rationaleOrigin,
            Map<String, Double> overrides, boolean exceedCap) {

        /** An automatic revision from drivers. */
        public static Request automatic(String cause, List<HypothesisUpdater.Driver> drivers,
                List<RevisionRationale.DriverNote> notes, List<String> citations) {
            return new Request(cause, HypothesisUpdater.NAME, drivers, notes, citations, null, "deterministic", null,
                    false);
        }
    }

    /**
     * Writes a revision of a locked set.
     *
     * @throws ValidationException 400 {@code LINT_FAILED} when an operator or analyst rationale fails the lint
     */
    @Transactional
    public HypothesisRevisionEntity write(HypothesisSetEntity set, Request request) {
        Instant now = clock.instant();
        List<HypothesisEntity> hyps = hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId());
        if (request.rationale() != null) {
            List<EstimativeLanguage.Violation> violations = EstimativeLanguage.lint(request.rationale(), List.of());
            if (!violations.isEmpty()) {
                List<FieldError> errors = new ArrayList<>();
                for (EstimativeLanguage.Violation v : violations) {
                    errors.add(new FieldError("rationale", v.code() + ": " + v.message()));
                }
                throw new ValidationException(ErrorCodes.LINT_FAILED, "The rationale fails the estimative-language"
                        + " lint.", errors);
            }
        }
        List<HypothesisUpdater.Hyp> view = new ArrayList<>();
        for (HypothesisEntity h : hyps) {
            view.add(new HypothesisUpdater.Hyp(h.getId(), h.getLabel(), h.getProbability(), h.getProbabilityCap(),
                    h.getCapOverride() != null));
        }
        HypothesisUpdater.Result result = request.overrides() != null
                ? HypothesisUpdater.override(set.getKind(), set.active(), view, request.overrides(),
                        request.exceedCap(), request.cause())
                : HypothesisUpdater.update(set.getKind(), set.active(), view, request.drivers(), history(set.getId(),
                        now));
        return persist(set, hyps, result, request, now);
    }

    /** Writes the {@code created} revision of a new set (the priors as the starting probabilities). */
    @Transactional
    public HypothesisRevisionEntity created(HypothesisSetEntity set, String actor, Map<String, Double> starting,
            List<String> citations) {
        Instant now = clock.instant();
        List<HypothesisEntity> hyps = hypotheses.findBySetIdOrderByPositionAscIdAsc(set.getId());
        List<HypothesisUpdater.Hyp> view = new ArrayList<>();
        for (HypothesisEntity h : hyps) {
            view.add(new HypothesisUpdater.Hyp(h.getId(), h.getLabel(), h.getPrior(), h.getProbabilityCap(), false));
        }
        HypothesisUpdater.Result bounded = HypothesisUpdater.override(set.getKind(), set.active(), view, starting,
                false, "operator");
        HypothesisUpdater.Result result = new HypothesisUpdater.Result(starting, bounded.after(), List.of(),
                bounded.capped());
        Request request = new Request("created", actor, List.of(), List.of(), citations,
                "The set was created; each probability starts at its prior.", "deterministic", null, false);
        return persist(set, hyps, result, request, now);
    }

    private HypothesisRevisionEntity persist(HypothesisSetEntity set, List<HypothesisEntity> hyps,
            HypothesisUpdater.Result result, Request request, Instant now) {
        Map<String, String> confidenceBefore = new HashMap<>();
        for (HypothesisEntity h : hyps) {
            confidenceBefore.put(h.getId(), h.getConfidence());
        }
        boolean override = false;
        for (HypothesisEntity h : hyps) {
            double after = result.after().get(h.getId());
            if (request.overrides() != null && request.exceedCap() && h.speculative() && h.getProbabilityCap() != null
                    && after > h.getProbabilityCap() + 1e-12) {
                Map<String, Object> capOverride = new LinkedHashMap<>();
                capOverride.put("by", request.actor());
                capOverride.put("at", now.toString());
                capOverride.put("rationale", request.rationale() == null ? "operator override" : request.rationale());
                h.setCapOverride(capOverride);
                override = true;
            }
            if (Math.abs(after - h.getProbability()) > 1e-12) {
                h.setProbability(after);
                h.touch(now);
            } else if (h.getEstimativeTerm() == null) {
                h.setProbability(after);
            }
        }
        override |= request.overrides() != null && "operator".equals(request.cause());
        refreshConfidence(set, hyps);
        List<Map<String, Object>> before = new ArrayList<>();
        List<Map<String, Object>> after = new ArrayList<>();
        List<RevisionRationale.Moved> moves = new ArrayList<>();
        List<Map<String, Object>> bandChanges = new ArrayList<>();
        for (HypothesisEntity h : hyps) {
            double b = result.before().get(h.getId());
            double a = result.after().get(h.getId());
            before.add(state(h, b, confidenceBefore.get(h.getId())));
            after.add(state(h, a, h.getConfidence()));
            moves.add(new RevisionRationale.Moved(h.getLabel(), b, a, confidenceBefore.get(h.getId()),
                    h.getConfidence(), result.capped().contains(h.getId())));
            if (!"created".equals(request.cause()) && EstimativeLanguage.crossesBand(b, a)) {
                Map<String, Object> change = new LinkedHashMap<>();
                change.put("hypothesisId", h.getId());
                change.put("label", h.getLabel());
                change.put("fromTerm", EstimativeLanguage.term(b));
                change.put("toTerm", EstimativeLanguage.term(a));
                change.put("probability", a);
                bandChanges.add(change);
            }
        }
        String rationale = request.rationale() != null ? request.rationale()
                : RevisionRationale.build(moves, annotate(request, result));
        String revisionId = monotonic(set.getLastRevisionId(), Ulids.next(now));
        HypothesisRevisionEntity revision = new HypothesisRevisionEntity(revisionId, set.getId(), now,
                request.cause(), request.actor() == null ? "operator" : request.actor(), seqClock.seqAt(jdbc, now));
        revision.setBefore(before);
        revision.setAfter(after);
        List<Map<String, Object>> drivers = new ArrayList<>();
        for (HypothesisUpdater.Contribution c : result.contributions()) {
            drivers.add(c.toMap());
        }
        revision.setDrivers(drivers);
        revision.setCitations(request.citations() == null ? List.of() : request.citations());
        revision.setRationale(rationale);
        revision.setRationaleOrigin(request.rationaleOrigin() == null ? "deterministic" : request.rationaleOrigin());
        revision.setBandChanged(!bandChanges.isEmpty());
        revision.setOverride(override);
        revision.setUpdaterVersion(HypothesisUpdater.LABEL);
        revision.setLedgerForecastId(forecast(set, hyps, revisionId, rationale, request.citations()));
        revisions.save(revision);
        hypotheses.saveAll(hyps);
        set.setLastRevisionId(revisionId);
        set.setLastRevisionAt(now);
        set.touch(now);
        HypothesisSetEntity saved = sets.save(set);
        if (!"created".equals(request.cause())) {
            events.set(HypothesisEvents.SET_REVISED, saved, hyps, revisionId, request.cause(), bandChanges);
        }
        if (!bandChanges.isEmpty()) {
            events.set(HypothesisEvents.SET_BAND_CHANGED, saved, hyps, revisionId, request.cause(), bandChanges);
            bandChanged(saved, bandChanges, now);
        }
        return revision;
    }

    /**
     * The rationale's driver notes with what the updater did to each driver: a driver whose every contribution was
     * suppressed carries the reason, one cut by damping is marked damped (notes and drivers share their order).
     */
    static List<RevisionRationale.DriverNote> annotate(Request request, HypothesisUpdater.Result result) {
        List<RevisionRationale.DriverNote> notes = request.notes() == null ? List.of() : request.notes();
        List<HypothesisUpdater.Driver> drivers = request.drivers() == null ? List.of() : request.drivers();
        if (notes.size() != drivers.size()) {
            return notes;
        }
        List<RevisionRationale.DriverNote> out = new ArrayList<>();
        for (int i = 0; i < notes.size(); i++) {
            HypothesisUpdater.Driver d = drivers.get(i);
            boolean any = false;
            boolean allSuppressed = true;
            boolean damped = false;
            String reason = null;
            for (HypothesisUpdater.Contribution c : result.contributions()) {
                if (!java.util.Objects.equals(c.id(), d.id()) || !java.util.Objects.equals(c.kind(), d.kind())) {
                    continue;
                }
                any = true;
                if (c.suppressed()) {
                    reason = c.suppressionReason();
                } else {
                    allSuppressed = false;
                }
                damped |= HypothesisUpdater.DAMPED.equals(c.suppressionReason());
            }
            RevisionRationale.DriverNote n = notes.get(i);
            out.add(new RevisionRationale.DriverNote(n.description(), n.kind(), n.lr(),
                    any && allSuppressed ? reason : null, damped));
        }
        return out;
    }

    /**
     * A revision id that sorts after the set's previous one: ULIDs made in the same millisecond are random in their
     * lower bits, so when the candidate does not sort after {@code previous} the previous id plus one is used (the set's
     * row lock makes this safe). Revisions are listed newest first by id.
     */
    static String monotonic(String previous, String candidate) {
        if (previous == null || candidate.compareTo(previous) > 0) {
            return candidate;
        }
        char[] chars = previous.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            int digit = Ulids.CROCKFORD.indexOf(chars[i]);
            if (digit < Ulids.CROCKFORD.length() - 1) {
                chars[i] = Ulids.CROCKFORD.charAt(digit + 1);
                return new String(chars);
            }
            chars[i] = Ulids.CROCKFORD.charAt(0);
        }
        return candidate;
    }

    private static Map<String, Object> state(HypothesisEntity h, double probability, String confidence) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("hypothesisId", h.getId());
        out.put("label", h.getLabel());
        out.put("probability", probability);
        out.put("estimativeTerm", EstimativeLanguage.term(probability));
        out.put("confidence", confidence);
        return out;
    }

    // ------------------------------------------------------------------------- history

    /**
     * What the set's earlier revisions applied: every evidence key ever applied, and today's (UTC) net evidence change
     * and strongest counted |ln LR| per family and hypothesis.
     */
    HypothesisUpdater.History history(String setId, Instant now) {
        Instant dayStart = now.truncatedTo(ChronoUnit.DAYS);
        Map<String, Double> dayNet = new HashMap<>();
        Map<String, Double> familyDay = new HashMap<>();
        for (HypothesisRevisionEntity r : revisions.findBySetIdAndAtGreaterThanEqualOrderByIdAsc(setId, dayStart)) {
            for (Map<String, Object> d : r.getDrivers()) {
                if (Boolean.TRUE.equals(d.get("suppressed")) || !Boolean.TRUE.equals(d.get("evidence"))) {
                    continue;
                }
                String hyp = String.valueOf(d.get("hypothesisId"));
                double applied = d.get("applied") instanceof Number n ? n.doubleValue() : 0.0;
                dayNet.merge(hyp, applied, Double::sum);
                Object family = d.get("signalFamily");
                if (family != null && d.get("lnLr") instanceof Number ln) {
                    String key = family + "|" + hyp;
                    Double previous = familyDay.get(key);
                    if (previous == null || Math.abs(ln.doubleValue()) > Math.abs(previous)) {
                        familyDay.put(key, ln.doubleValue());
                    }
                }
            }
        }
        Set<String> keys = new HashSet<>();
        for (HypothesisRevisionEntity r : revisions.findBySetIdOrderByIdDesc(setId, Limit.of(1000))) {
            for (Map<String, Object> d : r.getDrivers()) {
                if (!Boolean.TRUE.equals(d.get("suppressed")) && Boolean.TRUE.equals(d.get("evidence"))
                        && d.get("evidenceKey") != null) {
                    keys.add(d.get("evidenceKey").toString());
                }
            }
        }
        return new HypothesisUpdater.History(keys, familyDay, dayNet);
    }

    // ------------------------------------------------------------------------- confidence

    /** Recomputes the confidence of every hypothesis of a set and of the set itself (no revision: not a probability). */
    @Transactional
    public void refreshConfidence(HypothesisSetEntity set, List<HypothesisEntity> hyps) {
        List<IndicatorEntity> observed = new ArrayList<>();
        for (IndicatorEntity i : indicators.findBySetIdOrderByIdAsc(set.getId())) {
            if (HypothesisVocabulary.OBSERVED.equals(i.getState())) {
                observed.add(i);
            }
        }
        boolean brokenLinchpin = false;
        for (KeyAssumptionEntity a : assumptions.findBySetIdOrderByIdAsc(set.getId())) {
            brokenLinchpin |= a.brokenLinchpin();
        }
        boolean lowDensity = lowReportingDensity(set.getRegionCodes());
        List<ConfidenceAssessor.Observed> all = new ArrayList<>();
        for (IndicatorEntity i : observed) {
            all.add(new ConfidenceAssessor.Observed(i.getId(), i.getSignalFamily(), i.getCredibility(),
                    reliability(i)));
        }
        Double agreement = agreement(set);
        Map<String, Boolean> skewed = new java.util.HashMap<>();
        HypothesisEntity leading = null;
        for (HypothesisEntity h : hyps) {
            skewed.put(h.getId(), skewedUncorroborated(set.getId(), h.getId()));
            if (leading == null || h.getProbability() > leading.getProbability()) {
                leading = h;
            }
        }
        ConfidenceAssessor.Assessment setLevel = ConfidenceAssessor.assess(new ConfidenceAssessor.Inputs(all,
                brokenLinchpin, lowDensity, null, null, agreement,
                leading != null && Boolean.TRUE.equals(skewed.get(leading.getId()))));
        set.setConfidence(setLevel.level());
        for (HypothesisEntity h : hyps) {
            List<ConfidenceAssessor.Observed> bearing = new ArrayList<>();
            for (IndicatorEntity i : observed) {
                Object likelihood = i.getLr().get(h.getId());
                if (likelihood != null
                        && Math.abs(LikelihoodRatios.observed(HypothesisInputs.asMap(likelihood)) - 1.0) > 1e-9) {
                    bearing.add(new ConfidenceAssessor.Observed(i.getId(), i.getSignalFamily(), i.getCredibility(),
                            reliability(i)));
                }
            }
            ConfidenceAssessor.Assessment a = ConfidenceAssessor.assess(new ConfidenceAssessor.Inputs(bearing,
                    brokenLinchpin, lowDensity, null, null, agreement, Boolean.TRUE.equals(skewed.get(h.getId()))));
            h.setConfidence(a.level());
            h.setConfidenceBasis(a.basis());
        }
    }

    private ai.foresignal.news.NewsCoverageService newsCoverage;
    private ai.foresignal.news.NewsReliability newsReliability;

    /**
     * Installs the news layer (prompt 133): the coverage of each hypothesis's supporting news evidence and the outlet
     * reliability of news-article observations.
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setNews(ai.foresignal.news.NewsCoverageService newsCoverage,
            ai.foresignal.news.NewsReliability newsReliability) {
        this.newsCoverage = newsCoverage;
        this.newsReliability = newsReliability;
    }

    /** True when a hypothesis's supporting news evidence is skewed and has no non-news corroboration (133). */
    private boolean skewedUncorroborated(String setId, String hypothesisId) {
        ai.foresignal.news.NewsCoverageService service = newsCoverage;
        if (service == null) {
            return false;
        }
        try {
            return service.hypothesisSupport(setId, hypothesisId).skewedUncorroborated();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private String reliability(IndicatorEntity indicator) {
        Map<String, Object> evidence = indicator.getObservedEvidence();
        Object source = evidence == null ? null : evidence.get("sourceId");
        ai.foresignal.news.NewsReliability news = newsReliability;
        if (news != null && source != null && evidence.get("externalId") != null) {
            try {
                String grade = news.grade(source.toString(), evidence.get("externalId").toString());
                if (grade != null) {
                    return grade;
                }
            } catch (RuntimeException e) {
                // fall back to the source grade
            }
        }
        CredibilityService service = credibility.getIfAvailable();
        if (source == null || service == null) {
            return null;
        }
        try {
            return service.reliability(source.toString());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private boolean lowReportingDensity(List<String> codes) {
        RegionProfileRepository repository = regions.getIfAvailable();
        if (repository == null) {
            return false;
        }
        for (String code : codes) {
            String country = code.contains("-") ? code.substring(0, code.indexOf('-')) : code;
            Optional<RegionProfileEntity> profile = repository.findById(country);
            if (profile.isPresent() && profile.get().getDensityDecile()
                    <= ai.foresignal.baserate.ReportingDensity.LOW_DECILE) {
                return true;
            }
        }
        return false;
    }

    /** Agreement between the latest binary forecasts on the linked question (1 − range), or null. */
    private Double agreement(HypothesisSetEntity set) {
        ForecastService service = ledger.getIfAvailable();
        if (set.getQuestionId() == null || service == null) {
            return null;
        }
        double min = 1;
        double max = 0;
        int n = 0;
        for (ForecastEntity f : service.latestByForecaster(set.getQuestionId()).values()) {
            if (f.getProbability() != null && !"ensemble".equals(f.getForecasterId())) {
                min = Math.min(min, f.getProbability());
                max = Math.max(max, f.getProbability());
                n++;
            }
        }
        return n < 2 ? null : 1 - (max - min);
    }

    // ------------------------------------------------------------------------- ledger

    /**
     * Records the {@code indicator-bayes} forecast of a revision on the set's ledger question: a binary question gets
     * the summed probability of the hypotheses mapped to {@code yes}; a multiple question the distribution over its
     * labels (hypotheses summed per label, renormalized, each label at least 0.001). Returns the forecast id, or null
     * when the set has no open question or the mapping does not fit it.
     */
    String forecast(HypothesisSetEntity set, List<HypothesisEntity> hyps, String revisionId, String rationale,
            List<String> citations) {
        ForecastService service = ledger.getIfAvailable();
        ForecasterRegistry registry = forecasters.getIfAvailable();
        if (set.getQuestionId() == null || service == null || registry == null) {
            return null;
        }
        Optional<ForecastQuestionEntity> found = service.find(set.getQuestionId());
        if (found.isEmpty() || !found.get().acceptsForecasts(clock.instant())) {
            return null;
        }
        ForecastQuestionEntity q = found.get();
        ForecastInputs.ForecastValue value = value(q, hyps);
        if (value == null) {
            return null;
        }
        try {
            ForecastInputs.validateValue(value, q.getType(), q.labels());
        } catch (ValidationException e) {
            log.warn("Hypothesis set {}: the indicator-bayes forecast does not fit question {}: {}", set.getId(),
                    q.getId(), e.getMessage());
            return null;
        }
        ForecasterEntity forecaster = registry.ensure(HypothesisUpdater.NAME, HypothesisUpdater.NAME, FORECASTER_NAME,
                HypothesisUpdater.VERSION, Map.of());
        if (!forecaster.isActive()) {
            return null;
        }
        List<Map<String, Object>> refs = new ArrayList<>();
        refs.add(Map.of("ref", "hypothesis-set:" + set.getId()));
        for (String c : citations == null ? List.<String>of() : citations) {
            refs.add(Map.of("ref", c));
        }
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("setId", set.getId());
        inputs.put("revisionId", revisionId);
        inputs.put("updater", HypothesisUpdater.LABEL);
        ForecastEntity f = service.record(q, forecaster, value, rationale, refs, List.of(), inputs, null, false);
        return f.getId();
    }

    /** The forecast value of a set's probabilities on its question, or null when no hypothesis maps to it. */
    static ForecastInputs.ForecastValue value(ForecastQuestionEntity q, List<HypothesisEntity> hyps) {
        if (Ledger.BINARY.equals(q.getType())) {
            double yes = 0;
            boolean mapped = false;
            for (HypothesisEntity h : hyps) {
                if ("yes".equals(h.getLedgerOutcome())) {
                    yes += h.getProbability();
                    mapped = true;
                }
            }
            return mapped ? ForecastInputs.ForecastValue.binary(Math.max(0.001, Math.min(0.999, yes))) : null;
        }
        if (Ledger.MULTIPLE.equals(q.getType()) || Ledger.ORDERED.equals(q.getType())) {
            List<String> labels = q.labels();
            Map<String, Double> mass = new LinkedHashMap<>();
            for (String label : labels) {
                mass.put(label, 0.0);
            }
            boolean mapped = false;
            for (HypothesisEntity h : hyps) {
                if (h.getLedgerOutcome() != null && mass.containsKey(h.getLedgerOutcome())) {
                    mass.merge(h.getLedgerOutcome(), h.getProbability(), Double::sum);
                    mapped = true;
                }
            }
            if (!mapped) {
                return null;
            }
            double sum = 0;
            for (String label : labels) {
                mass.put(label, Math.max(0.001, mass.get(label)));
                sum += mass.get(label);
            }
            Map<String, Double> distribution = new LinkedHashMap<>();
            double running = 0;
            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);
                double v = i == labels.size() - 1 ? 1.0 - running : mass.get(label) / sum;
                distribution.put(label, v);
                running += v;
            }
            return ForecastInputs.ForecastValue.distribution(distribution);
        }
        return null;
    }

    // ------------------------------------------------------------------------- band changes

    /**
     * Sends the {@code hypothesis.band_changed} notification, at most once per set per
     * {@link #BAND_NOTIFY_INTERVAL}: a change inside the interval waits as the set's pending change (merged so that
     * each hypothesis keeps the term it was last notified at and takes the latest term), and
     * {@link #flushPendingBandChange} sends it once the interval has passed. Only sets whose cadence notifies by
     * default (daily, weekly) notify.
     */
    void bandChanged(HypothesisSetEntity set, List<Map<String, Object>> changes, Instant now) {
        if (!HypothesisNotifications.bandChangeDefaultOn(set.getCadence()) || set.isPrivateSet()) {
            return;
        }
        Instant last = set.getLastBandNotifiedAt();
        List<Map<String, Object>> merged = merge(set.getPendingBandChange(), changes);
        if (last == null || !now.isBefore(last.plus(BAND_NOTIFY_INTERVAL))) {
            if (!merged.isEmpty()) {
                notifications.bandChanged(set, merged);
            }
            set.setLastBandNotifiedAt(now);
            set.setPendingBandChange(null);
        } else {
            Map<String, Object> pending = new LinkedHashMap<>();
            pending.put("changes", merged);
            pending.put("at", now.toString());
            set.setPendingBandChange(merged.isEmpty() ? null : pending);
        }
        sets.save(set);
    }

    /** Sends a set's pending band change once the rate limit has passed; returns true when one was sent. */
    @Transactional
    public boolean flushPendingBandChange(HypothesisSetEntity set, Instant now) {
        if (set.getPendingBandChange() == null) {
            return false;
        }
        Instant last = set.getLastBandNotifiedAt();
        if (last != null && now.isBefore(last.plus(BAND_NOTIFY_INTERVAL))) {
            return false;
        }
        List<Map<String, Object>> changes = merge(set.getPendingBandChange(), List.of());
        if (!changes.isEmpty()) {
            notifications.bandChanged(set, changes);
        }
        set.setLastBandNotifiedAt(now);
        set.setPendingBandChange(null);
        sets.save(set);
        return !changes.isEmpty();
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> merge(Map<String, Object> pending, List<Map<String, Object>> latest) {
        Map<String, Map<String, Object>> byHypothesis = new LinkedHashMap<>();
        if (pending != null && pending.get("changes") instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    Map<String, Object> change = new LinkedHashMap<>((Map<String, Object>) m);
                    byHypothesis.put(String.valueOf(change.get("hypothesisId")), change);
                }
            }
        }
        for (Map<String, Object> change : latest) {
            String id = String.valueOf(change.get("hypothesisId"));
            Map<String, Object> earlier = byHypothesis.get(id);
            Map<String, Object> next = new LinkedHashMap<>(change);
            if (earlier != null) {
                next.put("fromTerm", earlier.get("fromTerm"));
            }
            byHypothesis.put(id, next);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> change : byHypothesis.values()) {
            if (!java.util.Objects.equals(change.get("fromTerm"), change.get("toTerm"))) {
                out.add(change);
            }
        }
        return out;
    }
}
