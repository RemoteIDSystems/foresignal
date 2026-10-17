package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.forecast.language.EstimativeLanguage;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Assessment composition (prompt 128 §4): the eight ICD 203 sections plus "What we cannot see" for cycles, the
 * on-demand sections, the deterministic bottom line without an LLM, LLM judgments replacing it, red-team notes
 * augmenting the hypotheses, numbers and terms agreeing, and the footer.
 */
class AssessmentComposerTest {

    static final Instant NOW = Instant.parse("2026-10-17T06:00:00Z");

    static CyclePacketBuilder.Packet packet() {
        Map<String, List<CyclePacketItem>> sections = new LinkedHashMap<>();
        sections.put("situations.escalated", List.of(new CyclePacketItem("situation:S1", "Red Sea shipping",
                "escalated", "critical", NOW, 3, List.of("YE"), "maritime", "S1", null, null, List.of(), null, false,
                Map.of())));
        sections.put("hypotheses.bandChanges", List.of(new CyclePacketItem("hypothesis:H1", "Response?: within 72 h",
                "band-changed", null, NOW, 1, List.of(), null, null, "SET1", "daily", List.of(), null, false,
                Map.of("probability", 0.62, "setId", "SET1"))));
        sections.put("findings", List.of(new CyclePacketItem("finding:F1", "Transits fell", "change-point",
                "significant", NOW, 2, List.of(), null, null, null, null, List.of(), null, false,
                Map.of("caveats", List.of(Map.of("code", "LATE_DATA"))))));
        sections.put("forecasts.resolved", List.of(new CyclePacketItem("question:Q1", "Strike by Friday?", "resolved",
                null, NOW, 1, List.of(), null, null, null, null, List.of(), "conflict", false,
                Map.of("scores", List.of(Map.of("forecasterId", "ensemble", "brier", 0.12))))));
        return CyclePacketBuilder.build(new CyclePacketBuilder.Inputs(NOW.minusSeconds(86_400), NOW, false, sections,
                List.of("acled"), List.of(), List.of("ER"), List.of(), List.of()), CycleScope.ALL, "weekly", false);
    }

    static CycleState state() {
        Map<String, Object> set = new LinkedHashMap<>();
        set.put("setId", "SET1");
        set.put("question", "Response within 7 days?");
        set.put("private", false);
        set.put("stakes", 3.0);
        set.put("drivers", List.of(Map.of("kind", "indicator", "signalFamily", "aviation-ops", "lnLr", 0.7)));
        set.put("hypotheses", List.of(
                Map.of("hypothesisId", "H1", "label", "within 72 h", "probability", 0.62, "type", "outcome"),
                Map.of("hypothesisId", "H2", "label", "advance knowledge", "probability", 0.05, "type",
                        "speculative-link", "alternatives", List.of("other"))));
        Map<String, Object> scorecard = new LinkedHashMap<>();
        scorecard.put("ensemble", Map.of("forecasterId", "ensemble", "n", 30, "brier", 0.15, "brierSkill", 0.2,
                "calibration", "well calibrated over 30 resolved"));
        scorecard.put("operator", null);
        scorecard.put("forecasters", List.of(Map.of("forecasterId", "ensemble", "n", 30, "brier", 0.15)));
        return new CycleState(List.of(Map.of("id", "S1", "title", "Red Sea shipping", "status", "active", "severity",
                "critical")), List.of(set), List.of(Map.of("indicatorId", "I1", "description", "Airspace closure",
                "expectedBy", NOW.plusSeconds(3600).toString())), scorecard);
    }

    static AssessmentComposer.Inputs inputs(String cadence, CycleRoles.AnalystResult analyst,
            CycleRoles.RedTeamResult redTeam, AssessmentComposer.Previous previous) {
        return new AssessmentComposer.Inputs("Weekly assessment", cadence, NOW.minusSeconds(86_400), NOW, null,
                packet(), state(), analyst, redTeam, List.of(RoleOutcome.of("analyst", "ok", null)), previous);
    }

    @Test
    @DisplayName("digest-only: a deterministic bottom line and every cycle section with the footer")
    @SuppressWarnings("unchecked")
    void digestOnly() {
        AssessmentComposer.Composed c = AssessmentComposer.compose(inputs("weekly", null, null, null));
        assertThat(c.sections()).extracting(s -> s.get("key")).containsExactly("bluf", "what-changed", "situations",
                "hypotheses", "indicators", "forecasts", "speculation", "sources", "collection-gaps");
        assertThat(c.bluf()).allSatisfy(j -> assertThat(j).containsEntry("textOrigin", "deterministic"));
        assertThat(c.bluf().get(0).get("text")).isEqualTo("Situation Red Sea shipping escalated to critical.");
        assertThat(c.bluf().get(1)).containsEntry("probability", 0.62).containsEntry("estimativeTerm", "likely")
                .containsEntry("hypothesisSetId", "SET1");
        assertThat(c.headline()).isEqualTo("Situation Red Sea shipping escalated to critical.");
        assertThat(c.markdown()).contains("## Bottom line up front").contains("## Speculation")
                .contains("Speculative link: advance knowledge").contains("Operator vs machine")
                .contains("LATE_DATA").contains(CycleVocabulary.FOOTER).contains("first assessment");
        assertThat(c.llmContributed()).isFalse();
        for (Map<String, Object> j : c.bluf()) {
            Double p = j.get("probability") instanceof Number n ? n.doubleValue() : null;
            assertThat(EstimativeLanguage.lint(String.valueOf(j.get("text")), p == null ? List.of()
                    : List.of(new EstimativeLanguage.Claim(p, 0, String.valueOf(j.get("text")).length())))).isEmpty();
        }
        Map<String, Object> changed = c.sections().get(1);
        assertThat((Map<String, Object>) changed.get("data")).containsKeys("diff", "snapshot");
    }

    @Test
    @DisplayName("LLM judgments replace the bottom line; red-team notes augment the hypotheses; change is explained")
    void withLlm() {
        Map<String, Object> judgment = new LinkedHashMap<>();
        judgment.put("id", "kj-1");
        judgment.put("text", "A coalition response within 72 hours is likely.");
        judgment.put("probability", 0.62);
        judgment.put("estimativeTerm", "likely");
        judgment.put("confidenceStatement", "Analytic confidence is moderate.");
        judgment.put("citations", List.of("finding:F1"));
        judgment.put("hypothesisSetId", "SET1");
        judgment.put("textOrigin", "llm");
        judgment.put("downgraded", false);
        Map<String, Object> note = new LinkedHashMap<>();
        note.put("setId", "SET1");
        note.put("question", "Response within 7 days?");
        note.put("strongestCase", "Talks are progressing.");
        note.put("disconfirmingEvidence", List.of(Map.of("text", "No tanker movement.", "citations",
                List.of("finding:F1"), "basis", "evidence")));
        note.put("assumptionsAtRisk", List.of("Strike capacity is intact."));
        note.put("premortem", "Diplomacy succeeded.");
        note.put("citations", List.of("finding:F1"));
        note.put("forecastId", "01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        AssessmentComposer.Previous previous = new AssessmentComposer.Previous("01JA8Q3V7S2K9W4T6Y1B5N0M3D",
                NOW.minusSeconds(604_800), List.of(Map.of("text", "Old", "hypothesisSetId", "SET1", "probability",
                        0.4)), AssessmentDiff.snapshot(List.of(Map.of("setId", "SET1", "question", "Response?",
                        "hypotheses", List.of(Map.of("hypothesisId", "H1", "label", "within 72 h", "probability",
                                0.4)))), List.of()));
        AssessmentComposer.Composed c = AssessmentComposer.compose(inputs("weekly", new CycleRoles.AnalystResult(
                RoleOutcome.of("analyst", "ok", null), List.of(judgment), "We cannot see the airbases.",
                List.of("lint warning")), new CycleRoles.RedTeamResult(RoleOutcome.of("red-team", "ok", null),
                List.of(note)), previous));
        assertThat(c.bluf()).hasSize(1);
        assertThat(c.headline()).isEqualTo("A coalition response within 72 hours is likely.");
        assertThat(c.citations()).containsExactly("finding:F1");
        assertThat(c.lintWarnings()).containsExactly("lint warning");
        assertThat(c.llmContributed()).isTrue();
        assertThat(c.markdown()).contains("Probability: 62% (likely). Analytic confidence is moderate.")
                .contains("### Red team: Response within 7 days?").contains("Premortem: Diplomacy succeeded.")
                .contains("+22 pts").contains("Revised judgment").contains("We cannot see the airbases.");
        assertThat(c.sections().get(0)).containsEntry("textOrigin", "llm");
        assertThat(c.sections().get(3)).containsEntry("key", "hypotheses").containsEntry("textOrigin", "mixed");
    }

    @Test
    @DisplayName("on-demand assessments have the situation sections")
    void onDemand() {
        AssessmentComposer.Composed c = AssessmentComposer.compose(new AssessmentComposer.Inputs("Situation assessment",
                "on-demand", NOW.minusSeconds(3600), NOW, "Red Sea shipping", packet(), state(), null, null, List.of(),
                null));
        assertThat(c.sections()).extracting(s -> s.get("key")).containsExactly("bluf", "key-insights", "what-changed",
                "outlook", "risks", "speculation", "sources");
    }

    @Test
    @DisplayName("an empty period still has a bottom line")
    void empty() {
        CyclePacketBuilder.Packet p = CyclePacketBuilder.build(new CyclePacketBuilder.Inputs(NOW, NOW, true, Map.of(),
                List.of(), List.of(), List.of(), List.of(), List.of()), CycleScope.ALL, "daily", false);
        assertThat(AssessmentComposer.deterministicBluf(p)).singleElement().satisfies(j ->
                assertThat(j.get("text")).isEqualTo("No material change in scope this period."));
    }
}
