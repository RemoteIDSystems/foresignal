package ai.foresignal.cycle;

import ai.foresignal.briefing.DigestRenderer;
import ai.foresignal.forecast.language.EstimativeLanguage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * "What changed" between an assessment and the previous one of its cadence (prompt 128 §4, ICD 203 standard 7): key
 * judgments added, removed and re-estimated (matched by ledger question, then hypothesis set, then text), hypothesis
 * probabilities that moved (with band changes), and situations that appeared, closed or changed status or severity.
 * The comparison works on a <em>snapshot</em> each assessment stores in its {@code what-changed} section. Pure.
 */
public final class AssessmentDiff {

    /** The smallest probability move reported for a hypothesis (one point). */
    static final double MIN_MOVE = 0.01;

    private AssessmentDiff() {}

    /**
     * The snapshot an assessment stores: hypothesis probabilities by set and hypothesis, situation status and severity.
     *
     * @param sets the sets of a {@link CycleState}
     * @param situations the situations of a {@link CycleState}
     */
    public static Map<String, Object> snapshot(List<Map<String, Object>> sets, List<Map<String, Object>> situations) {
        Map<String, Object> setMap = new LinkedHashMap<>();
        for (Map<String, Object> set : sets) {
            Map<String, Object> hyps = new LinkedHashMap<>();
            for (Object raw : set.get("hypotheses") instanceof List<?> l ? l : List.of()) {
                if (raw instanceof Map<?, ?> h) {
                    Map<String, Object> state = new LinkedHashMap<>();
                    state.put("label", h.get("label"));
                    state.put("probability", h.get("probability"));
                    hyps.put(String.valueOf(h.get("hypothesisId")), state);
                }
            }
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("question", set.get("question"));
            s.put("hypotheses", hyps);
            setMap.put(String.valueOf(set.get("setId")), s);
        }
        Map<String, Object> situationMap = new LinkedHashMap<>();
        for (Map<String, Object> s : situations) {
            Map<String, Object> state = new LinkedHashMap<>();
            state.put("title", s.get("title"));
            state.put("status", s.get("status"));
            state.put("severity", s.get("severity"));
            situationMap.put(String.valueOf(s.get("id")), state);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sets", setMap);
        out.put("situations", situationMap);
        return out;
    }

    /** The identity of a judgment: its question, else its set, else its normalized text. */
    static String key(Object questionId, Object setId, Object text) {
        if (questionId != null) {
            return "q:" + questionId;
        }
        if (setId != null) {
            return "s:" + setId;
        }
        String t = text == null ? "" : String.valueOf(text).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ").strip();
        return "t:" + (t.length() > 80 ? t.substring(0, 80) : t);
    }

    /** The previous judgment on the same question, set or text, or null. */
    public static Map<String, Object> match(String questionId, String setId, String text,
            List<Map<String, Object>> previous) {
        if (previous == null) {
            return null;
        }
        String k = key(questionId, setId, text);
        for (Map<String, Object> p : previous) {
            if (k.equals(key(p.get("questionId"), p.get("hypothesisSetId"), p.get("text")))) {
                return p;
            }
        }
        return null;
    }

    /**
     * The diff.
     *
     * @param previousJudgments the previous assessment's judgments (empty for a first assessment)
     * @param previousSnapshot the previous snapshot, or null
     * @param judgments this assessment's judgments
     * @param snapshot this assessment's snapshot
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> diff(List<Map<String, Object>> previousJudgments,
            Map<String, Object> previousSnapshot, List<Map<String, Object>> judgments, Map<String, Object> snapshot) {
        List<Map<String, Object>> prev = previousJudgments == null ? List.of() : previousJudgments;
        List<Map<String, Object>> added = new ArrayList<>();
        List<Map<String, Object>> changed = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        for (Map<String, Object> j : judgments) {
            Map<String, Object> before = match(str(j.get("questionId")), str(j.get("hypothesisSetId")),
                    str(j.get("text")), prev);
            if (before == null) {
                added.add(brief(j));
                continue;
            }
            seen.add(key(before.get("questionId"), before.get("hypothesisSetId"), before.get("text")));
            Double from = num(before.get("probability"));
            Double to = num(j.get("probability"));
            boolean moved = from != null && to != null && Math.abs(from - to) >= MIN_MOVE;
            boolean appeared = (from == null) != (to == null);
            if (moved || appeared || !String.valueOf(before.get("text")).equals(String.valueOf(j.get("text")))) {
                Map<String, Object> c = brief(j);
                c.put("previousText", before.get("text"));
                c.put("previousProbability", from);
                c.put("previousTerm", from == null ? null : EstimativeLanguage.term(clamp(from)));
                changed.add(c);
            }
        }
        List<Map<String, Object>> removed = new ArrayList<>();
        for (Map<String, Object> p : prev) {
            if (!seen.contains(key(p.get("questionId"), p.get("hypothesisSetId"), p.get("text")))) {
                removed.add(brief(p));
            }
        }
        Map<String, Object> prevSets = previousSnapshot == null ? Map.of()
                : (Map<String, Object>) previousSnapshot.getOrDefault("sets", Map.of());
        Map<String, Object> curSets = (Map<String, Object>) snapshot.getOrDefault("sets", Map.of());
        List<Map<String, Object>> moves = new ArrayList<>();
        for (Map.Entry<String, Object> e : curSets.entrySet()) {
            Map<String, Object> cur = (Map<String, Object>) e.getValue();
            Map<String, Object> old = (Map<String, Object>) prevSets.get(e.getKey());
            Map<String, Object> curHyps = (Map<String, Object>) cur.getOrDefault("hypotheses", Map.of());
            Map<String, Object> oldHyps = old == null ? Map.of()
                    : (Map<String, Object>) old.getOrDefault("hypotheses", Map.of());
            for (Map.Entry<String, Object> h : curHyps.entrySet()) {
                Map<String, Object> now = (Map<String, Object>) h.getValue();
                Map<String, Object> then = (Map<String, Object>) oldHyps.get(h.getKey());
                Double to = num(now.get("probability"));
                Double from = then == null ? null : num(then.get("probability"));
                if (to == null || (from != null && Math.abs(to - from) < MIN_MOVE)) {
                    continue;
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("setId", e.getKey());
                m.put("question", cur.get("question"));
                m.put("hypothesisId", h.getKey());
                m.put("label", now.get("label"));
                m.put("from", from);
                m.put("to", to);
                m.put("fromTerm", from == null ? null : EstimativeLanguage.term(clamp(from)));
                m.put("toTerm", EstimativeLanguage.term(clamp(to)));
                m.put("bandChanged", from != null && EstimativeLanguage.crossesBand(clamp(from), clamp(to)));
                moves.add(m);
            }
        }
        Map<String, Object> prevSituations = previousSnapshot == null ? Map.of()
                : (Map<String, Object>) previousSnapshot.getOrDefault("situations", Map.of());
        Map<String, Object> curSituations = (Map<String, Object>) snapshot.getOrDefault("situations", Map.of());
        List<Map<String, Object>> newSituations = new ArrayList<>();
        List<Map<String, Object>> changedSituations = new ArrayList<>();
        List<Map<String, Object>> goneSituations = new ArrayList<>();
        for (Map.Entry<String, Object> e : curSituations.entrySet()) {
            Map<String, Object> cur = (Map<String, Object>) e.getValue();
            Map<String, Object> old = (Map<String, Object>) prevSituations.get(e.getKey());
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("id", e.getKey());
            s.put("title", cur.get("title"));
            if (old == null) {
                if (previousSnapshot != null) {
                    s.put("status", cur.get("status"));
                    s.put("severity", cur.get("severity"));
                    newSituations.add(s);
                }
                continue;
            }
            boolean status = !String.valueOf(old.get("status")).equals(String.valueOf(cur.get("status")));
            boolean severity = !String.valueOf(old.get("severity")).equals(String.valueOf(cur.get("severity")));
            if (status || severity) {
                s.put("fromStatus", old.get("status"));
                s.put("toStatus", cur.get("status"));
                s.put("fromSeverity", old.get("severity"));
                s.put("toSeverity", cur.get("severity"));
                changedSituations.add(s);
            }
        }
        for (Map.Entry<String, Object> e : prevSituations.entrySet()) {
            if (!curSituations.containsKey(e.getKey())) {
                Map<String, Object> old = (Map<String, Object>) e.getValue();
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("id", e.getKey());
                s.put("title", old.get("title"));
                s.put("status", old.get("status"));
                goneSituations.add(s);
            }
        }
        Map<String, Object> judgmentsOut = new LinkedHashMap<>();
        judgmentsOut.put("added", added);
        judgmentsOut.put("removed", removed);
        judgmentsOut.put("changed", changed);
        Map<String, Object> situationsOut = new LinkedHashMap<>();
        situationsOut.put("new", newSituations);
        situationsOut.put("changed", changedSituations);
        situationsOut.put("noLongerOpen", goneSituations);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("judgments", judgmentsOut);
        out.put("hypotheses", moves);
        out.put("situations", situationsOut);
        return out;
    }

    /**
     * The diff as markdown.
     *
     * @param diff the diff
     * @param previousId the previous assessment's id, or null for the first one
     * @param previousDate the previous assessment's creation time text, or null
     */
    @SuppressWarnings("unchecked")
    public static String markdown(Map<String, Object> diff, String previousId, String previousDate) {
        StringBuilder md = new StringBuilder();
        if (previousId == null) {
            md.append("This is the first assessment of its kind; there is nothing earlier to compare with.\n");
            return md.toString();
        }
        md.append("Compared with the previous assessment `assessment:").append(previousId).append('`');
        if (previousDate != null) {
            md.append(" (").append(previousDate).append(')');
        }
        md.append(".\n\n");
        Map<String, Object> j = (Map<String, Object>) diff.get("judgments");
        List<Map<String, Object>> changed = (List<Map<String, Object>>) j.get("changed");
        List<Map<String, Object>> added = (List<Map<String, Object>>) j.get("added");
        List<Map<String, Object>> removed = (List<Map<String, Object>>) j.get("removed");
        if (changed.isEmpty() && added.isEmpty() && removed.isEmpty()) {
            md.append("- Key judgments: unchanged.\n");
        }
        for (Map<String, Object> c : changed) {
            md.append("- Revised judgment: ").append(DigestRenderer.escape(String.valueOf(c.get("text"))));
            if (c.get("previousProbability") != null || c.get("probability") != null) {
                md.append(" Probability ").append(percent(c.get("previousProbability"))).append(" before, ")
                        .append(percent(c.get("probability"))).append(" now.");
            }
            md.append('\n');
        }
        for (Map<String, Object> a : added) {
            md.append("- New judgment: ").append(DigestRenderer.escape(String.valueOf(a.get("text")))).append('\n');
        }
        for (Map<String, Object> r : removed) {
            md.append("- No longer judged: ").append(DigestRenderer.escape(String.valueOf(r.get("text")))).append('\n');
        }
        List<Map<String, Object>> moves = (List<Map<String, Object>>) diff.get("hypotheses");
        for (Map<String, Object> m : moves) {
            md.append("- Hypothesis ").append(DigestRenderer.escape(String.valueOf(m.get("label")))).append(" (")
                    .append(DigestRenderer.escape(String.valueOf(m.get("question")))).append("): ")
                    .append(m.get("from") == null ? "new" : percent(m.get("from"))).append(" to ")
                    .append(percent(m.get("to")));
            if (Boolean.TRUE.equals(m.get("bandChanged"))) {
                md.append(". Band change: ").append(m.get("fromTerm")).append(" before, ").append(m.get("toTerm"))
                        .append(" now");
            }
            md.append(".\n");
        }
        Map<String, Object> s = (Map<String, Object>) diff.get("situations");
        for (Map<String, Object> n : (List<Map<String, Object>>) s.get("new")) {
            md.append("- New situation: ").append(DigestRenderer.escape(String.valueOf(n.get("title")))).append(" (")
                    .append(n.get("status")).append(", ").append(n.get("severity")).append(").\n");
        }
        for (Map<String, Object> c : (List<Map<String, Object>>) s.get("changed")) {
            md.append("- Situation ").append(DigestRenderer.escape(String.valueOf(c.get("title")))).append(": ")
                    .append(c.get("fromStatus")).append('/').append(c.get("fromSeverity")).append(" to ")
                    .append(c.get("toStatus")).append('/').append(c.get("toSeverity")).append(".\n");
        }
        for (Map<String, Object> g : (List<Map<String, Object>>) s.get("noLongerOpen")) {
            md.append("- No longer open or in scope: ").append(DigestRenderer.escape(String.valueOf(g.get("title"))))
                    .append(".\n");
        }
        return md.toString();
    }

    private static Map<String, Object> brief(Map<String, Object> j) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("text", j.get("text"));
        out.put("probability", j.get("probability"));
        out.put("questionId", j.get("questionId"));
        out.put("hypothesisSetId", j.get("hypothesisSetId"));
        return out;
    }

    /** {@code 62%}, or "no number". */
    static String percent(Object p) {
        Double d = num(p);
        return d == null ? "no number" : Math.round(d * 100) + "%";
    }

    private static Double num(Object o) {
        return o instanceof Number n ? n.doubleValue() : null;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static double clamp(double p) {
        return Math.max(0, Math.min(1, p));
    }
}
