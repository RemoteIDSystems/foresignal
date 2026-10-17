package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * "What changed" between two assessments (prompt 128 §4): judgments added, removed and re-estimated (matched by
 * question, set, then text), hypothesis probabilities that moved with band changes, and situation changes.
 */
class AssessmentDiffTest {

    static Map<String, Object> judgment(String text, Double p, String questionId, String setId) {
        Map<String, Object> j = new LinkedHashMap<>();
        j.put("text", text);
        j.put("probability", p);
        j.put("questionId", questionId);
        j.put("hypothesisSetId", setId);
        return j;
    }

    static Map<String, Object> set(String id, String question, double p72, double pNone) {
        return Map.of("setId", id, "question", question, "hypotheses", List.of(
                Map.of("hypothesisId", "H72", "label", "within 72 h", "probability", p72),
                Map.of("hypothesisId", "HNONE", "label", "none in 7 days", "probability", pNone)));
    }

    static Map<String, Object> situation(String id, String status, String severity) {
        return Map.of("id", id, "title", "Situation " + id, "status", status, "severity", severity);
    }

    @Test
    @DisplayName("judgments: re-estimated by question, new, dropped, and unchanged by text")
    @SuppressWarnings("unchecked")
    void judgments() {
        List<Map<String, Object>> before = List.of(judgment("A strike within a week is unlikely.", 0.3, "Q1", null),
                judgment("Shipping keeps avoiding the strait.", null, null, null),
                judgment("Old judgment about ports.", null, null, null));
        List<Map<String, Object>> after = List.of(judgment("A strike within a week is likely.", 0.62, "Q1", null),
                judgment("Shipping keeps avoiding the strait!", null, null, null),
                judgment("New judgment on airspace.", null, null, "SET-1"));
        Map<String, Object> diff = AssessmentDiff.diff(before, null, after, AssessmentDiff.snapshot(List.of(),
                List.of()));
        Map<String, Object> j = (Map<String, Object>) diff.get("judgments");
        List<Map<String, Object>> changed = (List<Map<String, Object>>) j.get("changed");
        assertThat(changed).hasSize(2);
        assertThat(changed.get(0)).containsEntry("previousProbability", 0.3).containsEntry("probability", 0.62)
                .containsEntry("previousTerm", "unlikely");
        assertThat((List<Map<String, Object>>) j.get("added")).extracting(m -> m.get("text"))
                .containsExactly("New judgment on airspace.");
        assertThat((List<Map<String, Object>>) j.get("removed")).extracting(m -> m.get("text"))
                .containsExactly("Old judgment about ports.");
        assertThat(AssessmentDiff.match(null, null, "shipping keeps avoiding the strait", before)).isNotNull();
        assertThat(AssessmentDiff.match("Q9", null, "x", before)).isNull();
    }

    @Test
    @DisplayName("hypotheses: moves of at least one point, with band changes, and new hypotheses")
    @SuppressWarnings("unchecked")
    void hypotheses() {
        Map<String, Object> prev = AssessmentDiff.snapshot(List.of(set("S1", "Response?", 0.40, 0.50)), List.of());
        Map<String, Object> cur = AssessmentDiff.snapshot(List.of(set("S1", "Response?", 0.62, 0.505),
                set("S2", "Other?", 0.1, 0.9)), List.of());
        Map<String, Object> diff = AssessmentDiff.diff(List.of(), prev, List.of(), cur);
        List<Map<String, Object>> moves = (List<Map<String, Object>>) diff.get("hypotheses");
        assertThat(moves).extracting(m -> m.get("setId") + "/" + m.get("hypothesisId"))
                .containsExactly("S1/H72", "S2/H72", "S2/HNONE");
        assertThat(moves.get(0)).containsEntry("from", 0.40).containsEntry("to", 0.62)
                .containsEntry("fromTerm", "unlikely").containsEntry("toTerm", "likely")
                .containsEntry("bandChanged", true);
        assertThat(moves.get(1)).containsEntry("from", null).containsEntry("bandChanged", false);
        String md = AssessmentDiff.markdown(diff, "01JA8Q3V7S2K9W4T6Y1B5N0M3C", "2026-10-16T06:00:00Z");
        assertThat(md).contains("assessment:01JA8Q3V7S2K9W4T6Y1B5N0M3C").contains("40% to 62%")
                .contains("Band change: unlikely before, likely now").contains("Key judgments: unchanged");
    }

    @Test
    @DisplayName("situations: new, changed status or severity, and no longer open")
    @SuppressWarnings("unchecked")
    void situations() {
        Map<String, Object> prev = AssessmentDiff.snapshot(List.of(), List.of(situation("A", "emerging", "info"),
                situation("B", "active", "warning")));
        Map<String, Object> cur = AssessmentDiff.snapshot(List.of(), List.of(situation("A", "active", "critical"),
                situation("C", "emerging", "info")));
        Map<String, Object> s = (Map<String, Object>) AssessmentDiff.diff(List.of(), prev, List.of(), cur)
                .get("situations");
        assertThat((List<Map<String, Object>>) s.get("changed")).singleElement().satisfies(c ->
                assertThat(c).containsEntry("fromStatus", "emerging").containsEntry("toStatus", "active")
                        .containsEntry("toSeverity", "critical"));
        assertThat((List<Map<String, Object>>) s.get("new")).extracting(m -> m.get("id")).containsExactly("C");
        assertThat((List<Map<String, Object>>) s.get("noLongerOpen")).extracting(m -> m.get("id"))
                .containsExactly("B");
        Map<String, Object> first = (Map<String, Object>) AssessmentDiff.diff(List.of(), null, List.of(), cur)
                .get("situations");
        assertThat((List<?>) first.get("new")).isEmpty();
    }

    @Test
    @DisplayName("the first assessment says there is nothing to compare with")
    void first() {
        Map<String, Object> diff = AssessmentDiff.diff(List.of(), null, List.of(), AssessmentDiff.snapshot(List.of(),
                List.of()));
        assertThat(AssessmentDiff.markdown(diff, null, null)).contains("first assessment");
        assertThat(AssessmentDiff.percent(0.255)).isEqualTo("26%");
        assertThat(AssessmentDiff.percent(null)).isEqualTo("no number");
    }
}
