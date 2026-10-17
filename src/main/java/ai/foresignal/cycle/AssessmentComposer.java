package ai.foresignal.cycle;

import ai.foresignal.briefing.DigestRenderer;
import ai.foresignal.forecast.language.EstimativeLanguage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Composes an assessment (prompt 128 §4) from the packet, the current state, the LLM roles' output and the previous
 * assessment. A deterministic template fills every section; the analyst's key judgments replace the deterministic
 * bottom line and the red team's notes augment the hypotheses (LLM text is marked {@code textOrigin}). Cycle
 * assessments have the eight ICD 203 sections plus "What we cannot see"; on-demand situation assessments have bottom
 * line, key insights, what changed, outlook, risks, speculation and sources. Probabilities are always written as a
 * number with the ICD 203 term derived from it, and never in the same sentence as a confidence level. Pure.
 */
public final class AssessmentComposer {

    /** The most deterministic bottom-line items. */
    static final int DETERMINISTIC_BLUF = 5;

    /** The most rows a list section renders (the full lists are in the section data). */
    static final int MAX_ROWS = 15;

    private AssessmentComposer() {}

    /**
     * What an assessment is composed from.
     *
     * @param title the title
     * @param cadence the cadence
     * @param periodStart the period start
     * @param periodEnd the period end
     * @param situationTitle the situation of an on-demand assessment, or null
     * @param packet the packet
     * @param state the current state
     * @param analyst the analyst's output, or null
     * @param redTeam the red team's output, or null
     * @param roles every role outcome
     * @param previous the previous assessment's id, creation time, judgments and snapshot, or null
     */
    public record Inputs(String title, String cadence, Instant periodStart, Instant periodEnd, String situationTitle,
            CyclePacketBuilder.Packet packet, CycleState state, CycleRoles.AnalystResult analyst,
            CycleRoles.RedTeamResult redTeam, List<RoleOutcome> roles, Previous previous) {}

    /**
     * The previous assessment as the diff needs it.
     *
     * @param id the id
     * @param createdAt its creation time
     * @param judgments its key judgments
     * @param snapshot its snapshot, or null
     */
    public record Previous(String id, Instant createdAt, List<Map<String, Object>> judgments,
            Map<String, Object> snapshot) {}

    /**
     * A composed assessment.
     *
     * @param bluf the key judgments
     * @param headline the headline
     * @param sections the sections
     * @param markdown the full markdown
     * @param citations every cited reference
     * @param lintWarnings the wording warnings
     * @param llmContributed true when LLM text is in the bottom line or the hypotheses
     */
    public record Composed(List<Map<String, Object>> bluf, String headline, List<Map<String, Object>> sections,
            String markdown, List<String> citations, List<String> lintWarnings, boolean llmContributed) {}

    /** Composes the assessment. */
    public static Composed compose(Inputs in) {
        boolean onDemand = CycleVocabulary.ON_DEMAND.equals(in.cadence());
        boolean llmJudgments = in.analyst() != null && !in.analyst().judgments().isEmpty();
        List<Map<String, Object>> bluf = llmJudgments ? in.analyst().judgments() : deterministicBluf(in.packet());
        List<String> lint = new ArrayList<>(in.analyst() == null ? List.of() : in.analyst().lintWarnings());
        Set<String> citations = new LinkedHashSet<>();
        for (Map<String, Object> j : bluf) {
            for (Object c : j.get("citations") instanceof List<?> l ? l : List.of()) {
                citations.add(String.valueOf(c));
            }
        }
        if (in.redTeam() != null) {
            for (Map<String, Object> note : in.redTeam().notes()) {
                for (Object c : note.get("citations") instanceof List<?> l ? l : List.of()) {
                    citations.add(String.valueOf(c));
                }
            }
        }
        Map<String, Object> snapshot = AssessmentDiff.snapshot(in.state().sets(), in.state().situations());
        Previous prev = in.previous();
        Map<String, Object> diff = AssessmentDiff.diff(prev == null ? List.of() : prev.judgments(),
                prev == null ? null : prev.snapshot(), bluf, snapshot);
        List<Map<String, Object>> sections = new ArrayList<>();
        sections.add(section("bluf", "Bottom line up front", blufMarkdown(bluf), llmJudgments ? "llm"
                : "deterministic", Map.of("judgments", bluf)));
        Map<String, Object> changedData = new LinkedHashMap<>();
        changedData.put("previousAssessmentId", prev == null ? null : prev.id());
        changedData.put("diff", diff);
        changedData.put("snapshot", snapshot);
        Map<String, Object> changedSection = section("what-changed", onDemand ? "What changed since the previous"
                + " assessment of this situation" : "What changed since the previous " + in.cadence() + " assessment",
                AssessmentDiff.markdown(diff, prev == null ? null : prev.id(),
                        prev == null || prev.createdAt() == null ? null : prev.createdAt().toString()),
                "deterministic", changedData);
        if (onDemand) {
            sections.add(section("key-insights", "Key insights", keyInsights(in), "deterministic",
                    Map.of("situations", in.state().situations())));
            sections.add(changedSection);
            sections.add(section("outlook", "Outlook", indicatorsMarkdown(in) + "\n" + forecastsMarkdown(in, false),
                    "deterministic", Map.of("pendingIndicators", in.state().pendingIndicators())));
            sections.add(section("risks", "Risks", hypothesesMarkdown(in, prev) + "\n" + gapsMarkdown(in),
                    in.redTeam() != null && !in.redTeam().notes().isEmpty() ? "mixed" : "deterministic",
                    Map.of("sets", in.state().sets(), "redTeam", redTeamNotes(in))));
        } else {
            sections.add(changedSection);
            sections.add(section("situations", "Situations", situationsMarkdown(in), "deterministic",
                    Map.of("situations", in.state().situations())));
            sections.add(section("hypotheses", "Hypotheses", hypothesesMarkdown(in, prev),
                    in.redTeam() != null && !in.redTeam().notes().isEmpty() ? "mixed" : "deterministic",
                    Map.of("sets", in.state().sets(), "redTeam", redTeamNotes(in))));
            sections.add(section("indicators", "Indicators to watch", indicatorsMarkdown(in), "deterministic",
                    Map.of("pendingIndicators", in.state().pendingIndicators(), "proposalIds", proposalIds(in))));
            Map<String, Object> forecastData = new LinkedHashMap<>();
            forecastData.put("scorecard", in.state().scorecard());
            sections.add(section("forecasts", "Forecasts", forecastsMarkdown(in, true), "deterministic",
                    forecastData));
        }
        sections.add(section("speculation", "Speculation", speculationMarkdown(in, bluf), "deterministic", null));
        sections.add(section("sources", "Sources, coverage and caveats", sourcesMarkdown(in, citations.size()),
                "deterministic", Map.of("coverage", in.packet().stored().getOrDefault("coverage", Map.of()))));
        if (!onDemand) {
            String gapsText = gapsMarkdown(in);
            boolean comment = in.analyst() != null && in.analyst().whatWeCannotSee() != null;
            sections.add(section("collection-gaps", "What we cannot see", gapsText, comment ? "mixed"
                    : "deterministic", Map.of("collectionGaps", in.packet().stored().getOrDefault("collectionGaps",
                            List.of()))));
        }
        String headline = bluf.isEmpty() ? null : String.valueOf(bluf.get(0).get("text"));
        StringBuilder md = new StringBuilder("# ").append(DigestRenderer.escape(in.title())).append("\n\n*")
                .append(in.periodStart()).append(" to ").append(in.periodEnd()).append("*\n\n");
        for (Map<String, Object> s : sections) {
            md.append("## ").append(s.get("title")).append("\n\n").append(String.valueOf(s.get("markdown")).strip())
                    .append("\n\n");
        }
        md.append("---\n\n*").append(CycleVocabulary.FOOTER).append("*\n");
        boolean llm = llmJudgments || (in.redTeam() != null && !in.redTeam().notes().isEmpty());
        return new Composed(bluf, headline, sections, md.toString(), List.copyOf(citations), lint, llm);
    }

    /** The deterministic bottom line: the top changes of the period. */
    public static List<Map<String, Object>> deterministicBluf(CyclePacketBuilder.Packet packet) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (CyclePacketItem i : packet.section("situations.escalated")) {
            add(out, "Situation " + i.title() + " escalated to " + i.severity() + ".", i, null, null);
        }
        for (CyclePacketItem i : packet.section("situations.activated")) {
            add(out, "Situation " + i.title() + " became active.", i, null, null);
        }
        for (CyclePacketItem i : packet.section("situations.new")) {
            if (!"info".equals(i.severity())) {
                add(out, "New situation: " + i.title() + " (" + i.severity() + ").", i, null, null);
            }
        }
        for (CyclePacketItem i : packet.section("hypotheses.bandChanges")) {
            Double p = i.detail().get("probability") instanceof Number n ? n.doubleValue() : null;
            String setId = i.detail().get("setId") == null ? null : String.valueOf(i.detail().get("setId"));
            String text = p == null ? i.title() + " changed band." : i.title() + " now stands at "
                    + Math.round(p * 100) + "% (" + EstimativeLanguage.term(clamp(p)) + ").";
            add(out, text, i, p, setId);
        }
        for (CyclePacketItem i : packet.section("findings")) {
            if ("critical".equals(i.severity()) || "significant".equals(i.severity())) {
                add(out, "Finding (" + i.change() + ", " + i.severity() + "): " + i.title() + ".", i, null, null);
            }
        }
        for (CyclePacketItem i : packet.section("forecasts.resolved")) {
            add(out, "Question resolved: " + i.title() + ".", i, null, null);
        }
        for (CyclePacketItem i : packet.section("outlooks.indexChanges")) {
            Object delta = i.detail().get("delta");
            add(out, i.title() + " " + i.change() + " by " + (delta instanceof Number n ? Math.round(Math.abs(
                    n.doubleValue()) * 10) / 10.0 : delta) + " points.", i, null, null);
        }
        for (CyclePacketItem i : packet.section("situations.closed")) {
            add(out, "Situation " + i.title() + " closed.", i, null, null);
        }
        if (out.size() > DETERMINISTIC_BLUF) {
            out = new ArrayList<>(out.subList(0, DETERMINISTIC_BLUF));
        }
        if (out.isEmpty()) {
            Map<String, Object> j = judgment("dj-1", "No material change in scope this period.", List.of(), null, null);
            out.add(j);
        }
        return out;
    }

    private static void add(List<Map<String, Object>> out, String text, CyclePacketItem item, Double p, String setId) {
        out.add(judgment("dj-" + (out.size() + 1), text, List.of(item.ref()), p, setId));
    }

    private static Map<String, Object> judgment(String id, String text, List<String> citations, Double p,
            String setId) {
        Map<String, Object> j = new LinkedHashMap<>();
        j.put("id", id);
        j.put("text", text);
        j.put("probability", p);
        j.put("estimativeTerm", p == null ? null : EstimativeLanguage.term(clamp(p)));
        j.put("confidence", null);
        j.put("confidenceStatement", null);
        j.put("citations", citations);
        j.put("hypothesisSetId", p == null ? null : setId);
        j.put("questionId", null);
        j.put("textOrigin", "deterministic");
        j.put("change", null);
        j.put("downgraded", false);
        j.put("warnings", List.of());
        return j;
    }

    static String blufMarkdown(List<Map<String, Object>> bluf) {
        StringBuilder md = new StringBuilder();
        for (Map<String, Object> j : bluf) {
            md.append("- ").append(DigestRenderer.escape(String.valueOf(j.get("text"))));
            if (j.get("probability") instanceof Number p && !"deterministic".equals(j.get("textOrigin"))) {
                md.append(" Probability: ").append(Math.round(p.doubleValue() * 100)).append("% (")
                        .append(EstimativeLanguage.term(clamp(p.doubleValue()))).append(").");
            }
            if (j.get("confidenceStatement") != null) {
                md.append(' ').append(DigestRenderer.escape(String.valueOf(j.get("confidenceStatement"))));
            }
            refs(md, j.get("citations"));
            md.append('\n');
        }
        return md.toString();
    }

    private static String keyInsights(Inputs in) {
        StringBuilder md = new StringBuilder();
        md.append(situationsMarkdown(in)).append('\n');
        List<CyclePacketItem> findings = in.packet().section("findings");
        if (!findings.isEmpty()) {
            md.append("Findings in the period:\n\n");
            for (CyclePacketItem f : findings.subList(0, Math.min(MAX_ROWS, findings.size()))) {
                md.append("- ").append(DigestRenderer.escape(f.title())).append(" (").append(f.change()).append(", ")
                        .append(f.severity()).append(") `").append(f.ref()).append("`\n");
            }
        }
        return md.toString();
    }

    static String situationsMarkdown(Inputs in) {
        List<Map<String, Object>> situations = in.state().situations();
        if (situations.isEmpty()) {
            return "No open situation in scope.\n";
        }
        Map<String, String> trajectory = new LinkedHashMap<>();
        for (String section : List.of("situations.closed", "situations.dormant", "situations.activated",
                "situations.new", "situations.escalated")) {
            for (CyclePacketItem i : in.packet().section(section)) {
                trajectory.put(i.id(), i.change());
            }
        }
        StringBuilder md = new StringBuilder("| Situation | Status | Severity | This period | Last activity |\n");
        md.append("|---|---|---|---|---|\n");
        for (Map<String, Object> s : situations.subList(0, Math.min(MAX_ROWS, situations.size()))) {
            md.append("| ").append(cell(s.get("title"))).append(" `situation:").append(s.get("id")).append("` | ")
                    .append(s.get("status")).append(" | ").append(s.get("severity")).append(" | ")
                    .append(trajectory.getOrDefault(String.valueOf(s.get("id")), "steady")).append(" | ")
                    .append(s.get("lastActivityAt") == null ? "" : s.get("lastActivityAt")).append(" |\n");
        }
        return md.toString();
    }

    @SuppressWarnings("unchecked")
    static String hypothesesMarkdown(Inputs in, Previous prev) {
        List<Map<String, Object>> sets = in.state().sets();
        StringBuilder md = new StringBuilder();
        if (sets.isEmpty()) {
            md.append("No active hypothesis set in scope.\n");
        } else {
            Map<String, Object> prevSets = prev == null || prev.snapshot() == null ? Map.of()
                    : (Map<String, Object>) prev.snapshot().getOrDefault("sets", Map.of());
            md.append("| Set | Hypothesis | Probability | Term | Change | Top drivers |\n|---|---|---|---|---|---|\n");
            for (Map<String, Object> set : sets) {
                Map<String, Object> oldSet = (Map<String, Object>) prevSets.get(String.valueOf(set.get("setId")));
                Map<String, Object> oldHyps = oldSet == null ? Map.of()
                        : (Map<String, Object>) oldSet.getOrDefault("hypotheses", Map.of());
                boolean first = true;
                for (Object raw : set.get("hypotheses") instanceof List<?> l ? l : List.of()) {
                    Map<String, Object> h = (Map<String, Object>) raw;
                    double p = h.get("probability") instanceof Number n ? n.doubleValue() : 0;
                    Map<String, Object> old = (Map<String, Object>) oldHyps.get(String.valueOf(h.get("hypothesisId")));
                    String change = old == null || !(old.get("probability") instanceof Number o) ? "new"
                            : signed(Math.round((p - o.doubleValue()) * 100)) + " pts";
                    md.append("| ").append(first ? cell(set.get("question")) + " `hypothesis-set:" + set.get("setId")
                            + "`" : "").append(" | ").append(cell(h.get("label"))).append(" | ")
                            .append(Math.round(p * 100)).append("% | ").append(EstimativeLanguage.term(clamp(p)))
                            .append(" | ").append(change).append(" | ")
                            .append(first ? drivers(set.get("drivers")) : "").append(" |\n");
                    first = false;
                }
            }
        }
        for (Map<String, Object> note : redTeamNotes(in)) {
            md.append("\n### Red team: ").append(DigestRenderer.escape(String.valueOf(note.get("question"))))
                    .append("\n\n*LLM text, cited where marked.*\n\n");
            md.append("- Strongest case for the runner-up: ").append(text(note.get("strongestCase"))).append('\n');
            for (Object raw : note.get("disconfirmingEvidence") instanceof List<?> l ? l : List.of()) {
                Map<String, Object> e = (Map<String, Object>) raw;
                md.append("- Disconfirming evidence (").append(e.get("basis")).append("): ").append(text(e.get("text")));
                refs(md, e.get("citations"));
                md.append('\n');
            }
            for (Object a : note.get("assumptionsAtRisk") instanceof List<?> l ? l : List.of()) {
                md.append("- Assumption at risk: ").append(text(a)).append('\n');
            }
            md.append("- Premortem: ").append(text(note.get("premortem"))).append('\n');
            if (note.get("forecastId") != null) {
                md.append("- Red-team forecast recorded in the ledger: `forecast:").append(note.get("forecastId"))
                        .append("`\n");
            }
        }
        return md.toString();
    }

    static String indicatorsMarkdown(Inputs in) {
        StringBuilder md = new StringBuilder();
        List<Map<String, Object>> pending = in.state().pendingIndicators();
        if (pending.isEmpty()) {
            md.append("No pending indicator is expected in the next period.\n");
        } else {
            for (Map<String, Object> i : pending.subList(0, Math.min(MAX_ROWS, pending.size()))) {
                md.append("- ").append(cell(i.get("description"))).append(", expected by ").append(i.get("expectedBy"))
                        .append(" `indicator:").append(i.get("indicatorId")).append("`\n");
            }
        }
        List<CyclePacketItem> observed = in.packet().section("hypotheses.indicatorsObserved");
        if (!observed.isEmpty()) {
            md.append("\nObserved this period: ").append(observed.size()).append(" indicator")
                    .append(observed.size() == 1 ? "" : "s").append(".\n");
        }
        List<String> proposals = proposalIds(in);
        if (!proposals.isEmpty()) {
            md.append("\nProposals of this run (pending unless allowed): ");
            for (String id : proposals) {
                md.append('`').append(id).append("` ");
            }
            md.append('\n');
        }
        return md.toString();
    }

    @SuppressWarnings("unchecked")
    static String forecastsMarkdown(Inputs in, boolean withScorecard) {
        StringBuilder md = new StringBuilder();
        list(md, "Opened", in.packet().section("forecasts.opened"));
        list(md, "Due", in.packet().section("forecasts.due"));
        List<CyclePacketItem> resolved = in.packet().section("forecasts.resolved");
        if (!resolved.isEmpty()) {
            md.append("Resolved:\n\n");
            for (CyclePacketItem i : resolved.subList(0, Math.min(MAX_ROWS, resolved.size()))) {
                md.append("- ").append(DigestRenderer.escape(i.title())).append(" `").append(i.ref()).append('`');
                Object scores = i.detail().get("scores");
                if (scores instanceof List<?> l && !l.isEmpty()) {
                    md.append(": ");
                    List<String> parts = new ArrayList<>();
                    for (Object raw : l) {
                        Map<String, Object> s = (Map<String, Object>) raw;
                        parts.add(s.get("forecasterId") + " Brier " + round(s.get("brier")));
                    }
                    md.append(String.join(", ", parts));
                }
                md.append('\n');
            }
            md.append('\n');
        }
        if (md.isEmpty()) {
            md.append("No question opened, due or resolved in the period.\n\n");
        }
        Map<String, Object> scorecard = in.state().scorecard();
        if (withScorecard && scorecard != null) {
            md.append("Scorecard, last 90 days:\n\n| Forecaster | Resolved | Brier | Skill vs base rate | Calibration |\n")
                    .append("|---|---|---|---|---|\n");
            List<Map<String, Object>> rows = (List<Map<String, Object>>) scorecard.getOrDefault("forecasters",
                    List.of());
            for (Map<String, Object> r : rows.subList(0, Math.min(MAX_ROWS, rows.size()))) {
                md.append("| ").append(cell(r.get("forecasterId"))).append(" | ").append(r.get("n")).append(" | ")
                        .append(round(r.get("brier"))).append(" | ").append(round(r.get("brierSkill"))).append(" | ")
                        .append(r.get("calibration") == null ? "" : r.get("calibration")).append(" |\n");
            }
            Map<String, Object> machine = (Map<String, Object>) scorecard.get("ensemble");
            Map<String, Object> operator = (Map<String, Object>) scorecard.get("operator");
            md.append("\nOperator vs machine: ");
            if (machine == null) {
                md.append("the ensemble has no resolved question in the window");
            } else {
                md.append("ensemble Brier ").append(round(machine.get("brier")));
            }
            if (operator == null) {
                md.append("; the operator has fewer than ").append(CycleStateReader.OPERATOR_MIN)
                        .append(" resolved forecasts in the window");
            } else {
                md.append("; operator Brier ").append(round(operator.get("brier")));
            }
            md.append(".\n");
        }
        return md.toString();
    }

    @SuppressWarnings("unchecked")
    static String speculationMarkdown(Inputs in, List<Map<String, Object>> bluf) {
        StringBuilder md = new StringBuilder("*Speculation is labeled and kept apart from the judgments above.*\n\n");
        int count = 0;
        for (Map<String, Object> set : in.state().sets()) {
            for (Object raw : set.get("hypotheses") instanceof List<?> l ? l : List.of()) {
                Map<String, Object> h = (Map<String, Object>) raw;
                if (!"speculative-link".equals(h.get("type"))) {
                    continue;
                }
                count++;
                double p = h.get("probability") instanceof Number n ? n.doubleValue() : 0;
                md.append("- Speculative link: ").append(cell(h.get("label"))).append(" (")
                        .append(cell(set.get("question"))).append("), ").append(Math.round(p * 100)).append("%.");
                Object alternatives = h.get("alternatives");
                if (alternatives instanceof List<?> alt && !alt.isEmpty()) {
                    md.append(" Alternatives: ").append(DigestRenderer.escape(String.valueOf(alt))).append('.');
                }
                if (h.get("coincidenceBaseline") != null) {
                    md.append(" Coincidence baseline: ").append(DigestRenderer.escape(String.valueOf(
                            h.get("coincidenceBaseline")))).append('.');
                }
                md.append('\n');
            }
        }
        for (Map<String, Object> j : bluf) {
            if (Boolean.TRUE.equals(j.get("downgraded")) && "llm".equals(j.get("textOrigin"))) {
                count++;
                md.append("- Uncited or downgraded judgment (speculation): ")
                        .append(DigestRenderer.escape(String.valueOf(j.get("text")))).append('\n');
            }
        }
        if (count == 0) {
            md.append("No speculative link or speculative judgment in this assessment.\n");
        }
        return md.toString();
    }

    @SuppressWarnings("unchecked")
    static String sourcesMarkdown(Inputs in, int citationCount) {
        StringBuilder md = new StringBuilder();
        Map<String, Object> coverage = (Map<String, Object>) in.packet().stored().getOrDefault("coverage", Map.of());
        List<Object> failing = (List<Object>) coverage.getOrDefault("failingSources", List.of());
        List<Object> degraded = (List<Object>) coverage.getOrDefault("degradedSources", List.of());
        List<Object> low = (List<Object>) coverage.getOrDefault("lowReportingDensityRegions", List.of());
        md.append("- Failing sources: ").append(failing.isEmpty() ? "none" : DigestRenderer.escape(failing.toString()))
                .append(".\n");
        md.append("- Degraded sources: ").append(degraded.isEmpty() ? "none"
                : DigestRenderer.escape(degraded.toString())).append(".\n");
        md.append("- Regions with low reporting density (absence of reports means less there): ")
                .append(low.isEmpty() ? "none" : DigestRenderer.escape(low.size() > 20 ? low.subList(0, 20) + " …"
                        : low.toString())).append(".\n");
        Map<String, Integer> caveats = new LinkedHashMap<>();
        for (CyclePacketItem f : in.packet().section("findings")) {
            for (Object c : f.detail().get("caveats") instanceof List<?> l ? l : List.of()) {
                Object code = c instanceof Map<?, ?> m ? m.get("code") : c;
                caveats.merge(String.valueOf(code), 1, Integer::sum);
            }
        }
        if (!caveats.isEmpty()) {
            md.append("- Finding caveats: ").append(DigestRenderer.escape(caveats.toString())).append(".\n");
        }
        Object truncated = in.packet().stored().get("truncatedForLlm");
        if (truncated instanceof Map<?, ?> t && !t.isEmpty()) {
            md.append("- The LLM roles saw the 30 best-ranked items per section; cut: ")
                    .append(DigestRenderer.escape(t.toString())).append(".\n");
        }
        for (RoleOutcome r : in.roles()) {
            md.append("- Role ").append(r.role()).append(": ").append(r.status());
            if (r.reason() != null) {
                md.append(" (").append(DigestRenderer.escape(r.reason())).append(')');
            }
            if (r.model() != null) {
                md.append(", model ").append(DigestRenderer.escape(r.model()));
            }
            md.append(".\n");
        }
        md.append("- Citations: ").append(citationCount).append("; packet items: ").append(in.packet().size())
                .append(".\n");
        return md.toString();
    }

    @SuppressWarnings("unchecked")
    static String gapsMarkdown(Inputs in) {
        StringBuilder md = new StringBuilder();
        List<Object> gaps = (List<Object>) in.packet().stored().getOrDefault("collectionGaps", List.of());
        if (gaps.isEmpty()) {
            md.append("No collection gap is recorded for the hypothesis sets in scope.\n");
        } else {
            for (Object raw : gaps.subList(0, Math.min(MAX_ROWS, gaps.size()))) {
                Map<String, Object> g = (Map<String, Object>) raw;
                md.append("- Indicator `indicator:").append(g.get("indicatorId")).append("` of set `hypothesis-set:")
                        .append(g.get("setId")).append("`: potential information gain ").append(round(g.get("potentialEig")))
                        .append(", achievable ").append(round(g.get("actualEig"))).append(".\n");
            }
        }
        if (in.analyst() != null && in.analyst().whatWeCannotSee() != null) {
            md.append("\nAnalyst comment (LLM): ").append(DigestRenderer.escape(in.analyst().whatWeCannotSee()))
                    .append('\n');
        }
        return md.toString();
    }

    private static void list(StringBuilder md, String label, List<CyclePacketItem> items) {
        if (items.isEmpty()) {
            return;
        }
        md.append(label).append(":\n\n");
        for (CyclePacketItem i : items.subList(0, Math.min(MAX_ROWS, items.size()))) {
            md.append("- ").append(DigestRenderer.escape(i.title())).append(" `").append(i.ref()).append('`');
            Object p = i.detail().get("ensembleProbability");
            if (p instanceof Number n) {
                md.append(", ensemble ").append(Math.round(n.doubleValue() * 100)).append("% (")
                        .append(EstimativeLanguage.term(clamp(n.doubleValue()))).append(')');
            }
            if (i.detail().get("resolveBy") != null) {
                md.append(", resolves by ").append(i.detail().get("resolveBy"));
            }
            md.append('\n');
        }
        md.append('\n');
    }

    private static List<Map<String, Object>> redTeamNotes(Inputs in) {
        return in.redTeam() == null ? List.of() : in.redTeam().notes();
    }

    private static List<String> proposalIds(Inputs in) {
        List<String> out = new ArrayList<>();
        for (RoleOutcome r : in.roles()) {
            if (CycleVocabulary.PROPOSER.equals(r.role())) {
                Object ids = r.toMap().get("proposalIds");
                if (ids instanceof List<?> l) {
                    l.forEach(o -> out.add(String.valueOf(o)));
                }
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static String drivers(Object raw) {
        List<String> parts = new ArrayList<>();
        for (Object o : raw instanceof List<?> l ? l : List.of()) {
            if (o instanceof Map<?, ?> m) {
                Object family = m.get("signalFamily");
                Object kind = m.get("kind");
                Object ln = m.get("lnLr");
                parts.add((kind == null ? "" : kind + " ") + (family == null ? "" : family)
                        + (ln instanceof Number n ? " (ln LR " + Math.round(n.doubleValue() * 100) / 100.0 + ")" : ""));
            }
        }
        return parts.isEmpty() ? "" : cell(String.join("; ", parts));
    }

    private static void refs(StringBuilder md, Object citations) {
        if (citations instanceof List<?> l && !l.isEmpty()) {
            md.append(' ');
            for (Object c : l) {
                md.append("`").append(String.valueOf(c).replace("`", "")).append("` ");
            }
        }
    }

    private static Map<String, Object> section(String key, String title, String markdown, String origin,
            Map<String, Object> data) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("key", key);
        s.put("title", title);
        s.put("markdown", markdown);
        s.put("textOrigin", origin);
        s.put("data", data);
        return s;
    }

    private static String cell(Object text) {
        return text == null ? "" : DigestRenderer.escape(String.valueOf(text)).replace("|", "\\|");
    }

    private static String text(Object text) {
        return text == null ? "" : DigestRenderer.escape(String.valueOf(text));
    }

    private static String round(Object value) {
        return value instanceof Number n ? String.valueOf(Math.round(n.doubleValue() * 1000) / 1000.0) : "n/a";
    }

    private static String signed(long value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }

    private static double clamp(double p) {
        return Math.max(0, Math.min(1, p));
    }
}
