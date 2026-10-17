package ai.foresignal.hypothesis;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.JsonValues;
import ai.foresignal.security.Ulids;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * Parses and validates the request bodies of rolling hypotheses (prompt 125): {@code HypothesisSetInput},
 * {@code HypothesisInput}, {@code IndicatorInput}, {@code KeyAssumptionInput}, {@code AchEvidenceInput},
 * {@code HypothesisReviewInput} and the merge patches. Set structure errors are {@code 400 HYPOTHESIS_SET_INVALID},
 * indicator errors {@code 400 INDICATOR_INVALID}; references to stored objects (findings, questions, situations) are
 * checked by the service.
 */
public final class HypothesisInputs {

    /** The largest number of indicators created with a set. */
    public static final int MAX_INDICATORS = 50;

    /** The tolerance of "priors sum to 1". */
    public static final double SUM_TOLERANCE = 1e-3;

    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    private static final Set<String> SET_FIELDS = Set.of("question", "description", "kind", "cadence", "horizon",
            "situationId", "questionId", "regionCodes", "themes", "private", "hypotheses", "indicators", "assumptions",
            "ledgerQuestion", "proposalId");

    private HypothesisInputs() {}

    /**
     * A parsed hypothesis.
     *
     * @param label the label
     * @param statement the statement
     * @param type the type
     * @param prior the prior
     * @param priorBasis the prior basis
     * @param ledgerOutcome the ledger outcome, or null
     * @param linkedFindingIds the linked findings (speculative links)
     * @param coincidenceBaseline the coincidence baseline, or null
     * @param alternatives the alternatives as ids or labels
     */
    public record HypothesisDraft(String label, String statement, String type, double prior,
            Map<String, Object> priorBasis, String ledgerOutcome, List<String> linkedFindingIds,
            Map<String, Object> coincidenceBaseline, List<String> alternatives) {}

    /**
     * A parsed indicator.
     *
     * @param description the description
     * @param signal the signal
     * @param signalFamily the signal family
     * @param credibility the credibility grade
     * @param relevance the relevance grade
     * @param expectedBy when it is expected by, or null
     * @param lr the likelihoods keyed by hypothesis id or label
     * @param absenceLr the absence overrides keyed by hypothesis id or label
     */
    public record IndicatorDraft(String description, Map<String, Object> signal, String signalFamily,
            String credibility, String relevance, Instant expectedBy, Map<String, Map<String, Object>> lr,
            Map<String, Double> absenceLr) {}

    /**
     * A parsed key assumption.
     *
     * @param text the text
     * @param linchpin the linchpin flag
     * @param rationale the rationale, or null
     * @param wouldBeWrongIf what would make it wrong, or null
     * @param status the status
     * @param indicatorIds the linked indicators
     * @param reviewDue when it is due for review, or null
     */
    public record AssumptionDraft(String text, boolean linchpin, String rationale, String wouldBeWrongIf,
            String status, List<String> indicatorIds, Instant reviewDue) {}

    /**
     * The ledger question to create with a set.
     *
     * @param resolver the 122 resolver ({@code type} plus its members, {@code params} flattened)
     * @param resolutionCriteria the criteria, or null for the default text
     * @param category the category, or null
     */
    public record LedgerQuestionDraft(Map<String, Object> resolver, String resolutionCriteria, String category) {}

    /**
     * A parsed set.
     *
     * @param question the key question
     * @param description the description, or null
     * @param kind the kind
     * @param cadence the cadence
     * @param horizon the horizon, or null
     * @param situationId the situation, or null
     * @param questionId an existing ledger question, or null
     * @param regionCodes the regions
     * @param themes the themes
     * @param privateSet whether it is private
     * @param hypotheses the hypotheses
     * @param indicators the indicators
     * @param assumptions the assumptions
     * @param ledgerQuestion the ledger question to create, or null
     * @param proposalId the analyst proposal being confirmed, or null
     */
    public record SetDraft(String question, String description, String kind, String cadence, LocalDate horizon,
            String situationId, String questionId, List<String> regionCodes, List<String> themes, boolean privateSet,
            List<HypothesisDraft> hypotheses, List<IndicatorDraft> indicators, List<AssumptionDraft> assumptions,
            LedgerQuestionDraft ledgerQuestion, String proposalId) {}

    /**
     * A parsed review.
     *
     * @param cause {@code review} or {@code operator}
     * @param rationale the rationale
     * @param proposalId the analyst proposal being confirmed, or null
     * @param probabilities direct probabilities by hypothesis id
     * @param lrChanges LR changes ({@code indicatorId}, {@code hypothesisId}, {@code likelihood})
     * @param assumptions assumption status changes ({@code assumptionId}, {@code status}, {@code rationale})
     * @param exceedCap whether speculative-link caps may be exceeded (logged)
     * @param citations citations
     * @param version the set version, or null
     */
    public record ReviewDraft(String cause, String rationale, String proposalId, Map<String, Double> probabilities,
            List<Map<String, Object>> lrChanges, List<Map<String, Object>> assumptions, boolean exceedCap,
            List<String> citations, Integer version) {}

    /**
     * Parsed evidence.
     *
     * @param source the source
     * @param ref the reference, or null for manual evidence
     * @param description the description
     * @param credibility the credibility grade
     * @param relevance the relevance grade
     * @param ratings the ratings per hypothesis id
     * @param citations the citations
     */
    public record EvidenceDraft(String source, String ref, String description, String credibility, String relevance,
            Map<String, String> ratings, List<String> citations) {}

    // ------------------------------------------------------------------------- sets

    /** Parses a {@code HypothesisSetInput}. */
    public static SetDraft set(JsonNode body) {
        return set(map(body, "HypothesisSetInput", ErrorCodes.HYPOTHESIS_SET_INVALID));
    }

    /** Parses a {@code HypothesisSetInput} given as a map (drafts, MCP). */
    public static SetDraft set(Map<String, Object> body) {
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.keySet()) {
            if (!SET_FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not a HypothesisSetInput field"));
            }
        }
        String question = text(body, "question", 1, 300, true, errors);
        String description = text(body, "description", 0, 4000, false, errors);
        String kind = oneOf(body, "kind", HypothesisVocabulary.KINDS, true, errors);
        String cadence = oneOf(body, "cadence", HypothesisVocabulary.CADENCES, true, errors);
        LocalDate horizon = null;
        String horizonText = JsonValues.text(body, "horizon");
        if (horizonText != null) {
            try {
                horizon = LocalDate.parse(horizonText);
            } catch (RuntimeException e) {
                errors.add(new FieldError("horizon", "must be a date (YYYY-MM-DD)"));
            }
        }
        String situationId = ulid(body, "situationId", errors);
        String questionId = ulid(body, "questionId", errors);
        String proposalId = ulid(body, "proposalId", errors);
        List<String> regions = regions(body.get("regionCodes"), "regionCodes", errors);
        List<String> themes = stringList(body.get("themes"), "themes", 32, 64, errors);
        boolean privateSet = bool(body, "private", false, errors);
        List<HypothesisDraft> hypotheses = new ArrayList<>();
        Object rawHyps = body.get("hypotheses");
        if (!(rawHyps instanceof List<?> hypList)) {
            errors.add(new FieldError("hypotheses", "is required and must be an array of at least 2"));
        } else {
            for (int i = 0; i < hypList.size(); i++) {
                hypotheses.add(hypothesis(asMap(hypList.get(i)), "hypotheses[" + i + "]", errors));
            }
        }
        List<IndicatorDraft> indicators = new ArrayList<>();
        Object rawIndicators = body.get("indicators");
        if (rawIndicators instanceof List<?> list) {
            if (list.size() > MAX_INDICATORS) {
                errors.add(new FieldError("indicators", "at most " + MAX_INDICATORS + " indicators"));
            }
            for (int i = 0; i < list.size(); i++) {
                try {
                    indicators.add(indicator(asMap(list.get(i)), "indicators[" + i + "]"));
                } catch (ValidationException e) {
                    errors.addAll(e.getErrors());
                }
            }
        } else if (rawIndicators != null) {
            errors.add(new FieldError("indicators", "must be an array"));
        }
        List<AssumptionDraft> assumptions = new ArrayList<>();
        Object rawAssumptions = body.get("assumptions");
        if (rawAssumptions instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                assumptions.add(assumption(asMap(list.get(i)), "assumptions[" + i + "]", errors));
            }
        } else if (rawAssumptions != null) {
            errors.add(new FieldError("assumptions", "must be an array"));
        }
        LedgerQuestionDraft ledger = null;
        Map<String, Object> rawLedger = JsonValues.child(body, "ledgerQuestion");
        if (rawLedger != null) {
            ledger = ledgerQuestion(rawLedger, errors);
        } else if (body.get("ledgerQuestion") != null) {
            errors.add(new FieldError("ledgerQuestion", "must be an object"));
        }
        if (errors.isEmpty()) {
            structure(kind, hypotheses, indicators, errors);
        }
        if (ledger != null && horizon == null) {
            errors.add(new FieldError("ledgerQuestion", "a ledger question needs a horizon"));
        }
        if (ledger != null && questionId != null) {
            errors.add(new FieldError("ledgerQuestion", "give either questionId or ledgerQuestion, not both"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The hypothesis set is not valid.",
                    errors);
        }
        return new SetDraft(question, description, kind, cadence, horizon, situationId, questionId, regions, themes,
                privateSet, hypotheses, indicators, assumptions, ledger, proposalId);
    }

    /**
     * Validates a set's structure: at least two hypotheses with unique labels; an exclusive set has exactly one
     * {@code other} hypothesis and priors summing to 1; speculative links have their alternatives among the set's
     * hypotheses; indicator LRs name hypotheses of the set.
     */
    static void structure(String kind, List<HypothesisDraft> hypotheses, List<IndicatorDraft> indicators,
            List<FieldError> errors) {
        if (hypotheses.size() < 2) {
            errors.add(new FieldError("hypotheses", "a set needs at least 2 competing hypotheses"));
            return;
        }
        Set<String> labels = new HashSet<>();
        int others = 0;
        double sum = 0;
        for (int i = 0; i < hypotheses.size(); i++) {
            HypothesisDraft h = hypotheses.get(i);
            if (!labels.add(h.label())) {
                errors.add(new FieldError("hypotheses[" + i + "].label", "labels must be unique within a set"));
            }
            if (HypothesisVocabulary.OTHER.equals(h.type())) {
                others++;
            }
            sum += h.prior();
        }
        if (HypothesisVocabulary.EXCLUSIVE.equals(kind)) {
            if (others != 1) {
                errors.add(new FieldError("hypotheses", "an exclusive set has exactly one hypothesis of type other"
                        + " (\"other / none of these\"); it has " + others));
            }
            if (Math.abs(sum - 1.0) > SUM_TOLERANCE) {
                errors.add(new FieldError("hypotheses", "the priors of an exclusive set must sum to 1 (they sum to "
                        + Math.round(sum * 10000) / 10000.0 + ")"));
            }
        }
        for (int i = 0; i < hypotheses.size(); i++) {
            HypothesisDraft h = hypotheses.get(i);
            if (!HypothesisVocabulary.SPECULATIVE_LINK.equals(h.type())) {
                continue;
            }
            Set<String> distinct = new HashSet<>();
            for (String alt : h.alternatives()) {
                if (alt.equals(h.label())) {
                    errors.add(new FieldError("hypotheses[" + i + "].alternatives", "an alternative must be another"
                            + " hypothesis"));
                } else if (!labels.contains(alt) && !Ulids.isUlid(alt)) {
                    errors.add(new FieldError("hypotheses[" + i + "].alternatives", "'" + alt + "' is not a hypothesis"
                            + " of this set"));
                } else {
                    distinct.add(alt);
                }
            }
            if (distinct.size() < 2) {
                errors.add(new FieldError("hypotheses[" + i + "].alternatives", "a speculative link needs at least 2"
                        + " alternatives in the same set"));
            }
        }
        for (int i = 0; i < indicators.size(); i++) {
            IndicatorDraft d = indicators.get(i);
            for (String key : d.lr().keySet()) {
                if (!labels.contains(key)) {
                    errors.add(new FieldError("indicators[" + i + "].lr." + key, "names no hypothesis of this set (use"
                            + " the hypothesis label)"));
                }
            }
            for (String key : d.absenceLr().keySet()) {
                if (!labels.contains(key)) {
                    errors.add(new FieldError("indicators[" + i + "].absenceLr." + key, "names no hypothesis of this"
                            + " set"));
                }
            }
        }
    }

    /** Parses a {@code HypothesisInput}. */
    public static HypothesisDraft hypothesis(JsonNode body) {
        List<FieldError> errors = new ArrayList<>();
        HypothesisDraft draft = hypothesis(map(body, "HypothesisInput", ErrorCodes.HYPOTHESIS_SET_INVALID), "",
                errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The hypothesis is not valid.", errors);
        }
        return draft;
    }

    static HypothesisDraft hypothesis(Map<String, Object> h, String prefix, List<FieldError> errors) {
        String p = prefix.isEmpty() ? "" : prefix + ".";
        String label = text(h, "label", 1, 80, true, errors, p + "label");
        String statement = text(h, "statement", 1, 2000, true, errors, p + "statement");
        String type = oneOf(h, "type", HypothesisVocabulary.HYPOTHESIS_TYPES, true, errors, p + "type");
        Double prior = JsonValues.number(h, "prior");
        if (prior == null || prior <= 0 || prior >= 1) {
            errors.add(new FieldError(p + "prior", "is required and must be a probability strictly between 0 and 1"));
            prior = 0.5;
        }
        Map<String, Object> basis = priorBasis(JsonValues.child(h, "priorBasis"), p + "priorBasis", errors);
        String ledgerOutcome = text(h, "ledgerOutcome", 1, 64, false, errors, p + "ledgerOutcome");
        List<String> findings = stringList(h.get("linkedFindingIds"), p + "linkedFindingIds", 50, 26, errors);
        Map<String, Object> baseline = JsonValues.child(h, "coincidenceBaseline");
        List<String> alternatives = stringList(h.get("alternatives"), p + "alternatives", 20, 80, errors);
        if (HypothesisVocabulary.SPECULATIVE_LINK.equals(type)) {
            if (findings.isEmpty()) {
                errors.add(new FieldError(p + "linkedFindingIds", "a speculative link needs at least 1 linked finding"));
            }
            for (String f : findings) {
                if (!Ulids.isUlid(f)) {
                    errors.add(new FieldError(p + "linkedFindingIds", "'" + f + "' is not a finding id"));
                }
            }
            if (baseline == null) {
                errors.add(new FieldError(p + "coincidenceBaseline", "a speculative link needs the coincidence baseline"
                        + " (placeboRate and method) from its event study"));
            } else {
                Double rate = JsonValues.number(baseline, "placeboRate");
                if (rate == null || rate < 0 || rate > 1) {
                    errors.add(new FieldError(p + "coincidenceBaseline.placeboRate", "must be a probability"));
                }
                if (JsonValues.text(baseline, "method") == null) {
                    errors.add(new FieldError(p + "coincidenceBaseline.method", "is required"));
                }
            }
        }
        return new HypothesisDraft(label, statement, type, prior, basis, ledgerOutcome, findings, baseline,
                alternatives);
    }

    /** Validates a prior basis: a type, a reference for base rates and analogs, a rationale for operator judgment. */
    static Map<String, Object> priorBasis(Map<String, Object> basis, String field, List<FieldError> errors) {
        if (basis == null) {
            errors.add(new FieldError(field, "every prior needs its outside-view basis (base-rate, analogs or operator"
                    + " with a rationale)"));
            return new LinkedHashMap<>();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        String type = JsonValues.text(basis, "type");
        if (type == null || !HypothesisVocabulary.PRIOR_BASES.contains(type)) {
            errors.add(new FieldError(field + ".type", "must be one of " + HypothesisVocabulary.PRIOR_BASES));
        }
        String ref = JsonValues.text(basis, "ref");
        String rationale = JsonValues.text(basis, "rationale");
        if (("base-rate".equals(type) || "analogs".equals(type)) && (ref == null || ref.isBlank())) {
            errors.add(new FieldError(field + ".ref", "a " + type + " basis names its reference"));
        }
        if ("operator".equals(type) && (rationale == null || rationale.isBlank())) {
            errors.add(new FieldError(field + ".rationale", "an operator judgment states its rationale"));
        }
        if (rationale != null && rationale.length() > 2000) {
            errors.add(new FieldError(field + ".rationale", "at most 2000 characters"));
        }
        out.put("type", type);
        out.put("ref", ref);
        out.put("rationale", rationale);
        Double value = JsonValues.number(basis, "value");
        if (value != null) {
            out.put("value", value);
        }
        return out;
    }

    // ------------------------------------------------------------------------- indicators

    /** Parses an {@code IndicatorInput}. */
    public static IndicatorDraft indicator(JsonNode body) {
        return indicator(map(body, "IndicatorInput", ErrorCodes.INDICATOR_INVALID), "");
    }

    /**
     * Parses an {@code IndicatorInput} given as a map.
     *
     * @throws ValidationException 400 {@code INDICATOR_INVALID}
     */
    public static IndicatorDraft indicator(Map<String, Object> d, String prefix) {
        List<FieldError> errors = new ArrayList<>();
        String p = prefix.isEmpty() ? "" : prefix + ".";
        String description = text(d, "description", 1, 2000, true, errors, p + "description");
        Map<String, Object> signal = JsonValues.child(d, "signal");
        if (signal == null) {
            errors.add(new FieldError(p + "signal", "is required"));
            signal = new LinkedHashMap<>();
        } else {
            signal(signal, p + "signal", errors);
        }
        String family = text(d, "signalFamily", 1, 64, true, errors, p + "signalFamily");
        String credibility = oneOf(d, "credibility", HypothesisVocabulary.GRADES, true, errors, p + "credibility");
        String relevance = oneOf(d, "relevance", HypothesisVocabulary.GRADES, true, errors, p + "relevance");
        Instant expectedBy = instant(d, "expectedBy", p + "expectedBy", errors);
        Map<String, Map<String, Object>> lr = new LinkedHashMap<>();
        Map<String, Object> rawLr = JsonValues.child(d, "lr");
        if (rawLr == null || rawLr.isEmpty()) {
            errors.add(new FieldError(p + "lr", "is required: a likelihood per hypothesis the indicator bears on"));
        } else {
            for (Map.Entry<String, Object> e : rawLr.entrySet()) {
                lr.put(e.getKey(), likelihood(asMap(e.getValue()), p + "lr." + e.getKey(), errors));
            }
        }
        Map<String, Double> absence = new LinkedHashMap<>();
        Map<String, Object> rawAbsence = JsonValues.child(d, "absenceLr");
        if (rawAbsence != null) {
            for (Map.Entry<String, Object> e : rawAbsence.entrySet()) {
                if (!(e.getValue() instanceof Number n) || n.doubleValue() < LikelihoodRatios.MIN
                        || n.doubleValue() > LikelihoodRatios.MAX) {
                    errors.add(new FieldError(p + "absenceLr." + e.getKey(), "must be a number in [0.05, 20]"));
                } else {
                    absence.put(e.getKey(), n.doubleValue());
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.INDICATOR_INVALID, "The indicator is not valid.", errors);
        }
        return new IndicatorDraft(description, signal, family, credibility, relevance, expectedBy, lr, absence);
    }

    /** Validates an {@code IndicatorLikelihood}: class, direction, and an explicit value only with a rationale. */
    static Map<String, Object> likelihood(Map<String, Object> l, String field, List<FieldError> errors) {
        Map<String, Object> out = new LinkedHashMap<>();
        String lrClass = JsonValues.text(l, "class");
        String direction = JsonValues.text(l, "direction");
        if (lrClass == null || !HypothesisVocabulary.LR_CLASSES.contains(lrClass)) {
            errors.add(new FieldError(field + ".class", "must be one of " + HypothesisVocabulary.LR_CLASSES));
        }
        if (direction == null || !HypothesisVocabulary.LR_DIRECTIONS.contains(direction)) {
            errors.add(new FieldError(field + ".direction", "must be one of " + HypothesisVocabulary.LR_DIRECTIONS));
        }
        Double value = JsonValues.number(l, "value");
        String rationale = JsonValues.text(l, "rationale");
        if (value != null) {
            if (value < LikelihoodRatios.MIN || value > LikelihoodRatios.MAX) {
                errors.add(new FieldError(field + ".value", "an explicit LR is bounded to [0.05, 20]"));
            }
            if (rationale == null || rationale.isBlank()) {
                errors.add(new FieldError(field + ".rationale", "an explicit LR needs a rationale"));
            }
        }
        if (rationale != null && rationale.length() > 2000) {
            errors.add(new FieldError(field + ".rationale", "at most 2000 characters"));
        }
        out.put("class", lrClass);
        out.put("direction", direction);
        if (value != null) {
            out.put("value", value);
        }
        if (rationale != null) {
            out.put("rationale", rationale);
        }
        return out;
    }

    /** Validates an {@code IndicatorSignal}'s shape (the watch rule itself is validated when it is built). */
    static void signal(Map<String, Object> s, String field, List<FieldError> errors) {
        String type = JsonValues.text(s, "type");
        if (type == null || !HypothesisVocabulary.SIGNAL_TYPES.contains(type)) {
            errors.add(new FieldError(field + ".type", "must be one of " + HypothesisVocabulary.SIGNAL_TYPES));
            return;
        }
        switch (type) {
            case "rule" -> {
                if (JsonValues.child(s, "rule") == null) {
                    errors.add(new FieldError(field + ".rule", "a rule signal carries a TriggerRuleInput"));
                }
            }
            case "precursor" -> {
                if (!Ulids.isUlid(JsonValues.text(s, "precursorId"))) {
                    errors.add(new FieldError(field + ".precursorId", "a precursor signal names a 120 precursor id"));
                }
            }
            case "series" -> {
                Map<String, Object> series = JsonValues.child(s, "series");
                if (series == null || JsonValues.text(series, "ref") == null) {
                    errors.add(new FieldError(field + ".series.ref", "a series signal names a series reference"));
                } else {
                    Map<String, Object> condition = JsonValues.child(series, "condition");
                    boolean changePoint = Boolean.TRUE.equals(series.get("changePoint"));
                    if (condition == null && !changePoint && series.get("direction") == null) {
                        errors.add(new FieldError(field + ".series", "give a condition, changePoint: true or a"
                                + " direction"));
                    }
                    if (condition != null) {
                        String op = JsonValues.text(condition, "op");
                        if (op == null || !List.of("gt", "gte", "lt", "lte").contains(op)) {
                            errors.add(new FieldError(field + ".series.condition.op", "must be gt, gte, lt or lte"));
                        }
                        if (JsonValues.number(condition, "value") == null) {
                            errors.add(new FieldError(field + ".series.condition.value", "must be a number"));
                        }
                    }
                    String direction = JsonValues.text(series, "direction");
                    if (direction != null && !List.of("up", "down", "any").contains(direction)) {
                        errors.add(new FieldError(field + ".series.direction", "must be up, down or any"));
                    }
                }
            }
            case "intelEvent" -> {
                Map<String, Object> event = JsonValues.child(s, "intelEvent");
                if (event == null || JsonValues.text(event, "type") == null) {
                    errors.add(new FieldError(field + ".intelEvent.type", "an intel-event signal names the event"
                            + " type"));
                } else {
                    regions(event.get("regionCodes"), field + ".intelEvent.regionCodes", errors);
                    String bbox = JsonValues.text(event, "bbox");
                    if (bbox != null && bbox(bbox) == null) {
                        errors.add(new FieldError(field + ".intelEvent.bbox", "must be minLon,minLat,maxLon,maxLat"));
                    }
                }
            }
            case "situation" -> {
                Map<String, Object> situation = JsonValues.child(s, "situation");
                if (situation == null || !Ulids.isUlid(JsonValues.text(situation, "situationId"))) {
                    errors.add(new FieldError(field + ".situation.situationId", "a situation signal names a"
                            + " situation id"));
                } else {
                    String status = JsonValues.text(situation, "status");
                    String severity = JsonValues.text(situation, "minSeverity");
                    if (status == null && severity == null) {
                        errors.add(new FieldError(field + ".situation", "give a status or a minSeverity"));
                    }
                    if (status != null && !List.of("emerging", "active", "dormant", "closed").contains(status)) {
                        errors.add(new FieldError(field + ".situation.status", "must be emerging, active, dormant"
                                + " or closed"));
                    }
                    if (severity != null && !List.of("info", "warning", "critical").contains(severity)) {
                        errors.add(new FieldError(field + ".situation.minSeverity", "must be info, warning or"
                                + " critical"));
                    }
                }
            }
            case "question" -> {
                Map<String, Object> question = JsonValues.child(s, "question");
                if (question == null || !Ulids.isUlid(JsonValues.text(question, "questionId"))
                        || JsonValues.text(question, "outcome") == null) {
                    errors.add(new FieldError(field + ".question", "a question signal names a 122 question id and"
                            + " an outcome"));
                }
            }
            default -> {
                // manual: observed or absent through PATCH /v1/indicators/{id}.
            }
        }
        Object baseRate = s.get("baseRate");
        if (baseRate != null && (!(baseRate instanceof Number n) || n.doubleValue() <= 0 || n.doubleValue() >= 1)) {
            errors.add(new FieldError(field + ".baseRate", "must be a probability strictly between 0 and 1"));
        }
    }

    /** Parses {@code minLon,minLat,maxLon,maxLat}, or null when malformed. */
    public static double[] bbox(String text) {
        String[] parts = text.split(",");
        if (parts.length != 4) {
            return null;
        }
        double[] out = new double[4];
        try {
            for (int i = 0; i < 4; i++) {
                out[i] = Double.parseDouble(parts[i].trim());
            }
        } catch (NumberFormatException e) {
            return null;
        }
        if (out[0] < -180 || out[2] > 180 || out[1] < -90 || out[3] > 90 || out[0] >= out[2] || out[1] >= out[3]) {
            return null;
        }
        return out;
    }

    // ------------------------------------------------------------------------- assumptions and evidence

    /** Parses a {@code KeyAssumptionInput}. */
    public static AssumptionDraft assumption(JsonNode body) {
        List<FieldError> errors = new ArrayList<>();
        AssumptionDraft draft = assumption(map(body, "KeyAssumptionInput", ErrorCodes.VALIDATION_FAILED), "", errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The key assumption is not valid.", errors);
        }
        return draft;
    }

    static AssumptionDraft assumption(Map<String, Object> a, String prefix, List<FieldError> errors) {
        String p = prefix.isEmpty() ? "" : prefix + ".";
        String text = text(a, "text", 1, 2000, true, errors, p + "text");
        boolean linchpin = bool(a, "linchpin", false, errors);
        String rationale = text(a, "rationale", 0, 4000, false, errors, p + "rationale");
        String wrongIf = text(a, "wouldBeWrongIf", 0, 4000, false, errors, p + "wouldBeWrongIf");
        String status = oneOf(a, "status", HypothesisVocabulary.ASSUMPTION_STATUSES, false, errors, p + "status");
        List<String> indicators = stringList(a.get("indicatorIds"), p + "indicatorIds", 50, 26, errors);
        Instant reviewDue = instant(a, "reviewDue", p + "reviewDue", errors);
        return new AssumptionDraft(text, linchpin, rationale, wrongIf, status == null ? "solid" : status, indicators,
                reviewDue);
    }

    /** Parses an {@code AchEvidenceInput}. */
    public static EvidenceDraft evidence(JsonNode body) {
        Map<String, Object> e = map(body, "AchEvidenceInput", ErrorCodes.VALIDATION_FAILED);
        List<FieldError> errors = new ArrayList<>();
        String source = oneOf(e, "source", HypothesisVocabulary.INPUT_EVIDENCE_SOURCES, true, errors, "source");
        String ref = text(e, "ref", 1, 512, false, errors, "ref");
        String description = text(e, "description", 1, 2000, true, errors, "description");
        String credibility = oneOf(e, "credibility", HypothesisVocabulary.GRADES, true, errors, "credibility");
        String relevance = oneOf(e, "relevance", HypothesisVocabulary.GRADES, true, errors, "relevance");
        Map<String, String> ratings = new LinkedHashMap<>();
        Map<String, Object> rawRatings = JsonValues.child(e, "ratings");
        if (rawRatings != null) {
            for (Map.Entry<String, Object> r : rawRatings.entrySet()) {
                String rating = r.getValue() == null ? null : r.getValue().toString();
                if (rating == null || !HypothesisVocabulary.RATINGS.contains(rating)) {
                    errors.add(new FieldError("ratings." + r.getKey(), "must be one of " + HypothesisVocabulary.RATINGS));
                } else {
                    ratings.put(r.getKey(), rating);
                }
            }
        }
        if (source != null && !"manual".equals(source) && ref == null) {
            errors.add(new FieldError("ref", "is required unless the source is manual"));
        }
        if ("manual".equals(source) && ratings.isEmpty()) {
            errors.add(new FieldError("ratings", "manual evidence is rated by its author"));
        }
        List<String> citations = stringList(e.get("citations"), "citations", 100, 512, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The evidence is not valid.", errors);
        }
        return new EvidenceDraft(source, ref, description, credibility, relevance, ratings, citations);
    }

    /** Parses a {@code HypothesisReviewInput}. */
    public static ReviewDraft review(JsonNode body) {
        Map<String, Object> r = map(body, "HypothesisReviewInput", ErrorCodes.VALIDATION_FAILED);
        List<FieldError> errors = new ArrayList<>();
        String cause = oneOf(r, "cause", List.of("review", "operator"), false, errors, "cause");
        String rationale = text(r, "rationale", 1, 8000, true, errors, "rationale");
        String proposalId = ulid(r, "proposalId", errors);
        Map<String, Double> probabilities = new LinkedHashMap<>();
        Map<String, Object> rawP = JsonValues.child(r, "probabilities");
        if (rawP != null) {
            for (Map.Entry<String, Object> e : rawP.entrySet()) {
                if (!(e.getValue() instanceof Number n) || n.doubleValue() < 0 || n.doubleValue() > 1) {
                    errors.add(new FieldError("probabilities." + e.getKey(), "must be a probability"));
                } else {
                    probabilities.put(e.getKey(), n.doubleValue());
                }
            }
        }
        List<Map<String, Object>> lr = new ArrayList<>();
        if (r.get("lr") instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> change = asMap(list.get(i));
                Map<String, Object> likelihood = likelihood(asMap(change.get("likelihood")), "lr[" + i + "].likelihood",
                        errors);
                if (!Ulids.isUlid(JsonValues.text(change, "indicatorId"))
                        || !Ulids.isUlid(JsonValues.text(change, "hypothesisId"))) {
                    errors.add(new FieldError("lr[" + i + "]", "needs indicatorId and hypothesisId"));
                }
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("indicatorId", JsonValues.text(change, "indicatorId"));
                out.put("hypothesisId", JsonValues.text(change, "hypothesisId"));
                out.put("likelihood", likelihood);
                lr.add(out);
            }
        }
        List<Map<String, Object>> assumptions = new ArrayList<>();
        if (r.get("assumptions") instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> change = asMap(list.get(i));
                String status = JsonValues.text(change, "status");
                if (!Ulids.isUlid(JsonValues.text(change, "assumptionId")) || status == null
                        || !HypothesisVocabulary.ASSUMPTION_STATUSES.contains(status)) {
                    errors.add(new FieldError("assumptions[" + i + "]", "needs assumptionId and a status"));
                }
                assumptions.add(change);
            }
        }
        boolean exceedCap = bool(r, "exceedCap", false, errors);
        List<String> citations = stringList(r.get("citations"), "citations", 100, 512, errors);
        Integer version = r.get("version") instanceof Number n ? n.intValue() : null;
        if (!errors.isEmpty()) {
            throw new ValidationException("The review is not valid.", errors);
        }
        return new ReviewDraft(cause == null ? "review" : cause, rationale, proposalId, probabilities, lr, assumptions,
                exceedCap, citations, version);
    }

    /** Parses a {@code HypothesisSetPatch}; returns the members present ({@code version} required). */
    public static Map<String, Object> setPatch(JsonNode body) {
        Map<String, Object> p = map(body, "HypothesisSetPatch", ErrorCodes.HYPOTHESIS_SET_INVALID);
        List<FieldError> errors = new ArrayList<>();
        for (String field : p.keySet()) {
            if (!List.of("description", "cadence", "status", "version").contains(field)) {
                errors.add(new FieldError(field, "is not a HypothesisSetPatch field"));
            }
        }
        if (!(p.get("version") instanceof Number)) {
            errors.add(new FieldError("version", "is required (optimistic lock)"));
        }
        if (p.containsKey("cadence")) {
            oneOf(p, "cadence", HypothesisVocabulary.CADENCES, true, errors, "cadence");
        }
        if (p.containsKey("status")) {
            oneOf(p, "status", List.of(HypothesisVocabulary.SET_ACTIVE, HypothesisVocabulary.SET_ARCHIVED), true,
                    errors, "status");
        }
        if (p.containsKey("description")) {
            text(p, "description", 0, 4000, false, errors, "description");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The patch is not valid.", errors);
        }
        return p;
    }

    /** Parses a {@code HypothesisPatch}; returns the members present (validated). */
    public static Map<String, Object> hypothesisPatch(JsonNode body) {
        Map<String, Object> p = map(body, "HypothesisPatch", ErrorCodes.HYPOTHESIS_SET_INVALID);
        List<FieldError> errors = new ArrayList<>();
        for (String field : p.keySet()) {
            if (!List.of("label", "statement", "priorBasis", "ledgerOutcome", "alternatives", "probabilityOverride",
                    "version").contains(field)) {
                errors.add(new FieldError(field, "is not a HypothesisPatch field"));
            }
        }
        if (p.containsKey("label")) {
            text(p, "label", 1, 80, true, errors, "label");
        }
        if (p.containsKey("statement")) {
            text(p, "statement", 1, 2000, true, errors, "statement");
        }
        if (p.containsKey("priorBasis")) {
            p.put("priorBasis", priorBasis(JsonValues.child(p, "priorBasis"), "priorBasis", errors));
        }
        if (p.containsKey("ledgerOutcome")) {
            text(p, "ledgerOutcome", 1, 64, false, errors, "ledgerOutcome");
        }
        if (p.containsKey("alternatives")) {
            for (String id : stringList(p.get("alternatives"), "alternatives", 20, 26, errors)) {
                if (!Ulids.isUlid(id)) {
                    errors.add(new FieldError("alternatives", "'" + id + "' is not a hypothesis id"));
                }
            }
        }
        Map<String, Object> override = JsonValues.child(p, "probabilityOverride");
        if (override != null) {
            Double probability = JsonValues.number(override, "probability");
            if (probability == null || probability < 0 || probability > 1) {
                errors.add(new FieldError("probabilityOverride.probability", "must be a probability"));
            }
            String rationale = JsonValues.text(override, "rationale");
            if (rationale == null || rationale.isBlank()) {
                errors.add(new FieldError("probabilityOverride.rationale", "an override states its rationale"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.HYPOTHESIS_SET_INVALID, "The patch is not valid.", errors);
        }
        return p;
    }

    /** Parses an {@code IndicatorPatch}; returns the members present (shape-validated). */
    public static Map<String, Object> indicatorPatch(JsonNode body) {
        Map<String, Object> p = map(body, "IndicatorPatch", ErrorCodes.INDICATOR_INVALID);
        List<FieldError> errors = new ArrayList<>();
        for (String field : p.keySet()) {
            if (!List.of("description", "signal", "signalFamily", "credibility", "relevance", "expectedBy", "lr",
                    "absenceLr", "state", "observedEvidence").contains(field)) {
                errors.add(new FieldError(field, "is not an IndicatorPatch field"));
            }
        }
        if (p.containsKey("description")) {
            text(p, "description", 1, 2000, true, errors, "description");
        }
        if (p.containsKey("signal")) {
            Map<String, Object> signal = JsonValues.child(p, "signal");
            if (signal == null) {
                errors.add(new FieldError("signal", "must be an object"));
            } else {
                signal(signal, "signal", errors);
            }
        }
        if (p.containsKey("signalFamily")) {
            text(p, "signalFamily", 1, 64, true, errors, "signalFamily");
        }
        if (p.containsKey("credibility")) {
            oneOf(p, "credibility", HypothesisVocabulary.GRADES, true, errors, "credibility");
        }
        if (p.containsKey("relevance")) {
            oneOf(p, "relevance", HypothesisVocabulary.GRADES, true, errors, "relevance");
        }
        if (p.containsKey("expectedBy")) {
            instant(p, "expectedBy", "expectedBy", errors);
        }
        if (p.containsKey("lr")) {
            Map<String, Object> lr = JsonValues.child(p, "lr");
            if (lr == null) {
                errors.add(new FieldError("lr", "must be an object"));
            } else {
                Map<String, Object> out = new LinkedHashMap<>();
                for (Map.Entry<String, Object> e : lr.entrySet()) {
                    out.put(e.getKey(), likelihood(asMap(e.getValue()), "lr." + e.getKey(), errors));
                }
                p.put("lr", out);
            }
        }
        if (p.containsKey("state")) {
            oneOf(p, "state", List.of(HypothesisVocabulary.OBSERVED, HypothesisVocabulary.ABSENT), true, errors,
                    "state");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.INDICATOR_INVALID, "The patch is not valid.", errors);
        }
        return p;
    }

    /** Parses a {@code KeyAssumptionPatch}. */
    public static Map<String, Object> assumptionPatch(JsonNode body) {
        Map<String, Object> p = map(body, "KeyAssumptionPatch", ErrorCodes.VALIDATION_FAILED);
        List<FieldError> errors = new ArrayList<>();
        for (String field : p.keySet()) {
            if (!List.of("text", "linchpin", "rationale", "wouldBeWrongIf", "status", "indicatorIds", "reviewDue")
                    .contains(field)) {
                errors.add(new FieldError(field, "is not a KeyAssumptionPatch field"));
            }
        }
        if (p.containsKey("text")) {
            text(p, "text", 1, 2000, true, errors, "text");
        }
        if (p.containsKey("status")) {
            oneOf(p, "status", HypothesisVocabulary.ASSUMPTION_STATUSES, true, errors, "status");
        }
        if (p.containsKey("linchpin") && !(p.get("linchpin") instanceof Boolean)) {
            errors.add(new FieldError("linchpin", "must be a boolean"));
        }
        if (p.containsKey("indicatorIds")) {
            stringList(p.get("indicatorIds"), "indicatorIds", 50, 26, errors);
        }
        if (p.containsKey("reviewDue")) {
            instant(p, "reviewDue", "reviewDue", errors);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The patch is not valid.", errors);
        }
        return p;
    }

    // ------------------------------------------------------------------------- helpers

    private static LedgerQuestionDraft ledgerQuestion(Map<String, Object> q, List<FieldError> errors) {
        Map<String, Object> resolver = JsonValues.child(q, "resolver");
        if (resolver == null || JsonValues.text(resolver, "type") == null) {
            errors.add(new FieldError("ledgerQuestion.resolver", "is required with a type"));
            return null;
        }
        Map<String, Object> flat = new LinkedHashMap<>();
        flat.put("type", JsonValues.text(resolver, "type"));
        Map<String, Object> params = JsonValues.child(resolver, "params");
        if (params != null) {
            flat.putAll(params);
        }
        for (Map.Entry<String, Object> e : resolver.entrySet()) {
            if (!"type".equals(e.getKey()) && !"params".equals(e.getKey())) {
                flat.put(e.getKey(), e.getValue());
            }
        }
        String criteria = text(q, "resolutionCriteria", 1, 2000, false, errors, "ledgerQuestion.resolutionCriteria");
        String category = text(q, "category", 1, 64, false, errors, "ledgerQuestion.category");
        return new LedgerQuestionDraft(flat, criteria, category);
    }

    /** A JSON body as a map, or 400 with {@code code} when it is not an object. */
    static Map<String, Object> map(JsonNode body, String schema, String code) {
        if (body == null || !body.isObject()) {
            throw new ValidationException(code, "The body must be a " + schema + " object.", List.of());
        }
        return JsonValues.map(body);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> asMap(Object o) {
        return o instanceof Map<?, ?> m ? (Map<String, Object>) m : new LinkedHashMap<>();
    }

    private static String text(Map<String, Object> m, String key, int min, int max, boolean required,
            List<FieldError> errors) {
        return text(m, key, min, max, required, errors, key);
    }

    private static String text(Map<String, Object> m, String key, int min, int max, boolean required,
            List<FieldError> errors, String field) {
        Object v = m.get(key);
        if (v == null) {
            if (required) {
                errors.add(new FieldError(field, "is required"));
            }
            return null;
        }
        if (!(v instanceof String s)) {
            errors.add(new FieldError(field, "must be a string"));
            return null;
        }
        if (s.length() < min || s.length() > max) {
            errors.add(new FieldError(field, "must be " + min + " to " + max + " characters"));
            return null;
        }
        return s;
    }

    private static String oneOf(Map<String, Object> m, String key, List<String> allowed, boolean required,
            List<FieldError> errors) {
        return oneOf(m, key, allowed, required, errors, key);
    }

    private static String oneOf(Map<String, Object> m, String key, List<String> allowed, boolean required,
            List<FieldError> errors, String field) {
        String value = JsonValues.text(m, key);
        if (value == null) {
            if (required) {
                errors.add(new FieldError(field, "is required"));
            }
            return null;
        }
        if (!allowed.contains(value)) {
            errors.add(new FieldError(field, "must be one of " + allowed));
            return null;
        }
        return value;
    }

    private static boolean bool(Map<String, Object> m, String key, boolean defaultValue, List<FieldError> errors) {
        Object v = m.get(key);
        if (v == null) {
            return defaultValue;
        }
        if (!(v instanceof Boolean b)) {
            errors.add(new FieldError(key, "must be a boolean"));
            return defaultValue;
        }
        return b;
    }

    private static String ulid(Map<String, Object> m, String key, List<FieldError> errors) {
        String value = JsonValues.text(m, key);
        if (value != null && !Ulids.isUlid(value)) {
            errors.add(new FieldError(key, "must be a ULID"));
            return null;
        }
        return value;
    }

    private static Instant instant(Map<String, Object> m, String key, String field, List<FieldError> errors) {
        String value = JsonValues.text(m, key);
        if (value == null) {
            return null;
        }
        try {
            return JsonValues.instant(value);
        } catch (RuntimeException e) {
            errors.add(new FieldError(field, "must be an ISO-8601 date-time"));
            return null;
        }
    }

    private static List<String> regions(Object raw, String field, List<FieldError> errors) {
        List<String> out = stringList(raw, field, 50, 6, errors);
        for (String code : out) {
            if (!REGION.matcher(code).matches()) {
                errors.add(new FieldError(field, "'" + code + "' is not an ISO 3166-1 alpha-2 or 3166-2 code"));
            }
        }
        return out;
    }

    private static List<String> stringList(Object raw, String field, int maxItems, int maxLength,
            List<FieldError> errors) {
        List<String> out = new ArrayList<>();
        if (raw == null) {
            return out;
        }
        if (!(raw instanceof List<?> list)) {
            errors.add(new FieldError(field, "must be an array of strings"));
            return out;
        }
        if (list.size() > maxItems) {
            errors.add(new FieldError(field, "at most " + maxItems + " items"));
        }
        for (Object o : list) {
            if (!(o instanceof String s) || s.isEmpty() || s.length() > maxLength) {
                errors.add(new FieldError(field, "items must be strings of 1 to " + maxLength + " characters"));
            } else if (!out.contains(s)) {
                out.add(s);
            }
        }
        return out;
    }
}
