package ai.foresignal.forecast.resolve;

import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.JsonValues;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Evaluates a question's machine resolver (prompt 122 §3), pure given a {@link ResolutionData}.
 *
 * <ul>
 *   <li><b>Early resolution:</b> an {@code event-exists} or {@code alert-fired} question resolves {@code yes} as soon
 *       as its event occurs in the window, before {@code resolve_by}.</li>
 *   <li><b>Release lags:</b> everything else waits until {@code resolve_by + lag}, where {@code lag} is the involved
 *       sources' {@code releaseLagDays} (for a series period that ends after {@code resolve_by}, the lag counts from
 *       the period's end), and reads as of that time. A lag over {@value #MAX_LAG_DAYS} days is cut at
 *       {@value #MAX_LAG_DAYS} days, and the resolution then carries the caveat {@code LATE_DATA}.</li>
 *   <li><b>Coverage:</b> when the resolver's sources were failing or not covering for more than 20% of the window
 *       (covered share below {@value #COVERAGE_MIN}), a negative or numeric answer is {@code annulled} with
 *       {@code COVERAGE_GAP}; a series with more than 20% of its buckets missing likewise.</li>
 *   <li><b>Detectability:</b> a negative answer ("no event", a count below the threshold) also needs 116
 *       {@code canSeeAbsence} over the question's area and window; otherwise the question is {@code annulled} with
 *       {@code LOW_DETECTABILITY}.</li>
 *   <li>{@code hypothesis-outcome} and {@code manual} questions are never resolved here.</li>
 * </ul>
 */
public final class ResolverEngine {

    /** The longest release lag waited for. */
    public static final long MAX_LAG_DAYS = 14;

    /** The covered share below which a window has a coverage gap. */
    public static final double COVERAGE_MIN = 0.8;

    /** The most evidence refs a resolution pins. */
    public static final int MAX_EVIDENCE = 50;

    private ResolverEngine() {}

    /**
     * What to do with a question now.
     *
     * @param kind {@code WAIT}, {@code RESOLVE} or {@code ANNUL}
     * @param outcome the outcome label ({@code yes}/{@code no} for binary), or null
     * @param value the numeric value, or null
     * @param early true when resolved before {@code resolve_by}
     * @param asOf the knowledge cutoff used
     * @param evidence the pinned evidence refs
     * @param caveats caveats ({@code code}, {@code text})
     * @param annulCode the annulment code
     * @param reason the annulment reason or a note
     */
    public record Verdict(Kind kind, String outcome, Double value, boolean early, Instant asOf, List<String> evidence,
            List<Map<String, Object>> caveats, String annulCode, String reason) {

        /** Not yet. */
        public static Verdict waiting(String reason) {
            return new Verdict(Kind.WAIT, null, null, false, null, List.of(), List.of(), null, reason);
        }

        /** An annulment. */
        public static Verdict annul(String code, String reason) {
            return new Verdict(Kind.ANNUL, null, null, false, null, List.of(), List.of(), code, reason);
        }
    }

    /** The verdict kinds. */
    public enum Kind {
        /** Not resolvable yet. */
        WAIT,
        /** Resolve with the outcome or value. */
        RESOLVE,
        /** Annul (not scored). */
        ANNUL
    }

    /** The {@code resolvedBy} of a machine resolution. */
    public static String resolvedBy(ForecastQuestionEntity q) {
        return "resolver:" + q.getResolverType() + "/" + ResolverSpec.VERSION;
    }

    /**
     * Evaluates a question.
     *
     * @param q the question (open or closed)
     * @param now the time
     * @param data the data port
     */
    public static Verdict evaluate(ForecastQuestionEntity q, Instant now, ResolutionData data) {
        Map<String, Object> resolver = q.getResolver();
        String type = q.getResolverType();
        if ("manual".equals(type) || "hypothesis-outcome".equals(type)) {
            return Verdict.waiting("resolved by the operator");
        }
        Instant[] window = ResolverSpec.window(resolver, q.getResolveBy());
        double lagDays = Math.max(0, data.releaseLagDays(resolver));
        List<Map<String, Object>> caveats = new ArrayList<>();
        long lagMinutes = (long) Math.ceil(Math.min(lagDays, MAX_LAG_DAYS) * 24 * 60);
        if (lagDays > MAX_LAG_DAYS) {
            caveats.add(caveat("LATE_DATA", "The sources publish about " + Math.round(lagDays) + " days late;"
                    + " resolved after " + MAX_LAG_DAYS + " days with what exists, so late data may be missing."));
        }
        // A series period ending after resolve_by is waited for to its end; the lag counts from there.
        Instant anchor = "series-value".equals(type) && window != null && window[1].isAfter(q.getResolveBy())
                ? window[1] : q.getResolveBy();
        Instant due = anchor.plus(Duration.ofMinutes(lagMinutes));
        return switch (type) {
            case "event-exists", "alert-fired" -> events(q, resolver, window, now, due, caveats, data);
            case "count-threshold" -> count(q, resolver, window, now, due, caveats, data);
            case "series-value" -> series(q, resolver, window, now, due, caveats, data);
            case "situation-status" -> situation(q, resolver, now, data);
            default -> Verdict.waiting("unknown resolver " + type);
        };
    }

    private static Verdict events(ForecastQuestionEntity q, Map<String, Object> resolver, Instant[] window,
            Instant now, Instant due, List<Map<String, Object>> caveats, ResolutionData data) {
        Map<String, Object> selector = "alert-fired".equals(q.getResolverType())
                ? Map.of("alertRuleId", resolver.get("ruleId")) : JsonValues.child(resolver, "selector");
        Map<String, Object> area = JsonValues.child(resolver, "area");
        Instant end = window[1].isBefore(now) ? window[1] : now;
        if (!end.isAfter(window[0]) && now.isBefore(q.getResolveBy())) {
            return Verdict.waiting("the window has not started");
        }
        List<ResolutionData.Occurrence> found = end.isAfter(window[0])
                ? data.occurrences(selector, area, window[0], end) : List.of();
        if (!found.isEmpty()) {
            return new Verdict(Kind.RESOLVE, "yes", null, now.isBefore(q.getResolveBy()), now, refs(found),
                    caveats, null, found.size() + " occurrence" + (found.size() == 1 ? "" : "s") + " in the window");
        }
        if (now.isBefore(due)) {
            return Verdict.waiting(now.isBefore(q.getResolveBy()) ? "no event yet" : "waiting for late data");
        }
        ResolutionData.Scope scope = data.scope(resolver);
        Verdict gap = coverageGap(scope, window, data);
        if (gap != null) {
            return gap;
        }
        ResolutionData.Absence absence = data.canSeeAbsence(scope, area, window[0], window[1]);
        if (!absence.ok()) {
            return Verdict.annul("LOW_DETECTABILITY", "No event was found, but its absence cannot be confirmed: "
                    + absence.reason());
        }
        return new Verdict(Kind.RESOLVE, "no", null, false, due, absenceEvidence(scope, area), caveats, null,
                "no occurrence in the window, with healthy coverage and adequate detectability");
    }

    private static Verdict count(ForecastQuestionEntity q, Map<String, Object> resolver, Instant[] window,
            Instant now, Instant due, List<Map<String, Object>> caveats, ResolutionData data) {
        if (now.isBefore(due)) {
            return Verdict.waiting(now.isBefore(q.getResolveBy()) ? "not due" : "waiting for late data");
        }
        if (resolver.get("op") == null) {
            return numericCount(q, resolver, window, now, due, caveats, data);
        }
        String op = resolver.get("op").toString();
        double threshold = ((Number) resolver.get("threshold")).doubleValue();
        double count;
        List<String> evidence;
        ResolutionData.Scope scope = data.scope(resolver);
        Map<String, Object> area = JsonValues.child(resolver, "area");
        if (resolver.get("ref") != null) {
            ResolutionData.Aggregate agg = data.seriesAggregate(resolver.get("ref").toString(), window[0], window[1],
                    "sum", due);
            if (agg.gapShare() > 1 - COVERAGE_MIN) {
                return Verdict.annul("COVERAGE_GAP", Math.round(agg.gapShare() * 100) + "% of the window has no"
                        + " covered data");
            }
            count = agg.value() == null ? 0 : agg.value();
            evidence = agg.evidence();
        } else {
            List<ResolutionData.Occurrence> found = data.occurrences(JsonValues.child(resolver, "selector"), area,
                    window[0], window[1]);
            count = found.size();
            evidence = refs(found);
            Verdict gap = coverageGap(scope, window, data);
            if (gap != null) {
                return gap;
            }
        }
        boolean met = ResolverSpec.compare(count, op, threshold);
        boolean lowCount = "gt".equals(op) ? count <= threshold : count < threshold;
        if (lowCount && resolver.get("ref") == null) {
            ResolutionData.Absence absence = data.canSeeAbsence(scope, area, window[0], window[1]);
            if (!absence.ok()) {
                return Verdict.annul("LOW_DETECTABILITY", "The count is below the threshold, but missed events"
                        + " cannot be ruled out: " + absence.reason());
            }
            if (evidence.isEmpty()) {
                evidence = absenceEvidence(scope, area);
            }
        }
        return new Verdict(Kind.RESOLVE, met ? "yes" : "no", null, false, due, evidence, caveats, null,
                "count " + num(count) + " " + op + " " + num(threshold) + ": " + (met ? "met" : "not met"));
    }

    /**
     * The numeric mode of {@code count-threshold} (prompt 126): the count itself. A {@code referenceFamily}
     * selector counts the family's reference-history events in the area's regions (one source per region), waiting
     * until the source covers every week of the window and annulling {@code COVERAGE_GAP} after
     * {@value #MAX_LAG_DAYS} days without it; a {@code rollup:} ref sums the series; any other selector counts
     * occurrences, which needs 116 detectability (missed events would bias the count down).
     */
    private static Verdict numericCount(ForecastQuestionEntity q, Map<String, Object> resolver, Instant[] window,
            Instant now, Instant due, List<Map<String, Object>> caveats, ResolutionData data) {
        Map<String, Object> selector = JsonValues.child(resolver, "selector");
        Map<String, Object> area = JsonValues.child(resolver, "area");
        if (selector != null && selector.get("referenceFamily") != null) {
            List<String> regions = new ArrayList<>(JsonValues.strings(area, "regionCodes"));
            for (String code : JsonValues.strings(selector, "regionCodes")) {
                if (!regions.contains(code)) {
                    regions.add(code);
                }
            }
            ResolutionData.ReferenceCount rc = data.referenceCount(selector.get("referenceFamily").toString(),
                    regions, window[0], window[1]);
            if (rc == null || rc.value() == null || !rc.covered()) {
                if (now.isBefore(q.getResolveBy().plus(Duration.ofDays(MAX_LAG_DAYS)))) {
                    return Verdict.waiting("waiting for the reference history to cover the window");
                }
                return Verdict.annul("COVERAGE_GAP", "no reference source covers every week of the window "
                        + MAX_LAG_DAYS + " days after it ended");
            }
            List<String> evidence = rc.evidence() == null ? List.of() : rc.evidence();
            return new Verdict(Kind.RESOLVE, null, rc.value(), false, due, evidence, caveats, null,
                    "count " + num(rc.value()) + " (" + rc.source() + ")");
        }
        if (resolver.get("ref") != null) {
            ResolutionData.Aggregate agg = data.seriesAggregate(resolver.get("ref").toString(), window[0], window[1],
                    "sum", due);
            if (agg.gapShare() > 1 - COVERAGE_MIN) {
                return Verdict.annul("COVERAGE_GAP", Math.round(agg.gapShare() * 100) + "% of the window has no"
                        + " covered data");
            }
            double count = agg.value() == null ? 0 : agg.value();
            return new Verdict(Kind.RESOLVE, null, count, false, due, agg.evidence(), caveats, null,
                    "count " + num(count));
        }
        ResolutionData.Scope scope = data.scope(resolver);
        List<ResolutionData.Occurrence> found = data.occurrences(selector, area, window[0], window[1]);
        Verdict gap = coverageGap(scope, window, data);
        if (gap != null) {
            return gap;
        }
        ResolutionData.Absence absence = data.canSeeAbsence(scope, area, window[0], window[1]);
        if (!absence.ok()) {
            return Verdict.annul("LOW_DETECTABILITY", "A count of occurrences needs adequate detectability, so that"
                    + " missed events do not bias it: " + absence.reason());
        }
        List<String> evidence = refs(found);
        return new Verdict(Kind.RESOLVE, null, (double) found.size(), false, due, evidence.isEmpty()
                ? absenceEvidence(scope, area) : evidence, caveats, null, "count " + found.size());
    }

    private static Verdict series(ForecastQuestionEntity q, Map<String, Object> resolver, Instant[] window,
            Instant now, Instant due, List<Map<String, Object>> caveats, ResolutionData data) {
        Instant waitUntil = due;
        if (now.isBefore(waitUntil)) {
            return Verdict.waiting("waiting for the period and its release lag");
        }
        String aggregate = resolver.get("aggregate") == null ? "last" : resolver.get("aggregate").toString();
        ResolutionData.Aggregate agg = data.seriesAggregate(resolver.get("ref").toString(), window[0], window[1],
                aggregate, waitUntil);
        if (agg.gapShare() > 1 - COVERAGE_MIN || agg.value() == null) {
            return Verdict.annul("COVERAGE_GAP", agg.value() == null ? "the series has no covered value in the"
                    + " period" : Math.round(agg.gapShare() * 100) + "% of the period has no covered data");
        }
        if (resolver.get("op") != null) {
            boolean met = ResolverSpec.compare(agg.value(), resolver.get("op").toString(),
                    ((Number) resolver.get("threshold")).doubleValue());
            return new Verdict(Kind.RESOLVE, met ? "yes" : "no", null, false, waitUntil, agg.evidence(), caveats, null,
                    aggregate + " " + num(agg.value()) + " " + resolver.get("op") + " " + resolver.get("threshold"));
        }
        return new Verdict(Kind.RESOLVE, null, agg.value(), false, waitUntil, agg.evidence(), caveats, null,
                aggregate + " = " + num(agg.value()));
    }

    private static Verdict situation(ForecastQuestionEntity q, Map<String, Object> resolver, Instant now,
            ResolutionData data) {
        if (now.isBefore(q.getResolveBy())) {
            return Verdict.waiting("not due");
        }
        String situationId = resolver.get("situationId").toString();
        ResolutionData.SituationState state = data.situationAt(situationId, q.getResolveBy());
        if (state == null) {
            return Verdict.annul("COVERAGE_GAP", "the situation " + situationId + " is unknown at resolve_by");
        }
        Object status = resolver.get("status");
        Object min = resolver.get("minSeverity");
        boolean statusOk = status == null || status.toString().equals(state.status());
        boolean severityOk = min == null || ResolverSpec.SEVERITIES.indexOf(state.severity())
                >= ResolverSpec.SEVERITIES.indexOf(min.toString());
        boolean met = statusOk && severityOk;
        return new Verdict(Kind.RESOLVE, met ? "yes" : "no", null, false, q.getResolveBy(),
                List.of("situation:" + situationId), List.of(), null, "status " + state.status() + ", severity "
                        + state.severity() + " at resolve_by");
    }

    private static Verdict coverageGap(ResolutionData.Scope scope, Instant[] window, ResolutionData data) {
        if (!scope.judgeable()) {
            return null;
        }
        double covered = data.coverage(scope, window[0], window[1]);
        if (!Double.isNaN(covered) && covered < COVERAGE_MIN) {
            return Verdict.annul("COVERAGE_GAP", "the resolver's sources covered " + Math.round(covered * 100)
                    + "% of the window (failing or not covering for more than 20%)");
        }
        return null;
    }

    private static List<String> refs(List<ResolutionData.Occurrence> found) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (ResolutionData.Occurrence o : found) {
            if (out.size() >= MAX_EVIDENCE) {
                break;
            }
            if (o.ref() != null) {
                out.add(o.ref());
            }
        }
        return new ArrayList<>(out);
    }

    private static List<String> absenceEvidence(ResolutionData.Scope scope, Map<String, Object> area) {
        String where = area == null ? "global" : area.get("regionCodes") != null
                ? String.join(",", JsonValues.strings(area, "regionCodes"))
                : area.get("placeId") != null ? "place:" + area.get("placeId") : "area";
        return List.of("observability:" + (scope.family() == null ? "unknown" : scope.family()) + "/" + where);
    }

    /** A caveat map. */
    public static Map<String, Object> caveat(String code, String text) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", code);
        out.put("text", text);
        return out;
    }

    private static String num(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
