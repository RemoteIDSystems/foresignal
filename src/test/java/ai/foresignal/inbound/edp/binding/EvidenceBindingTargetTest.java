package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.hypothesis.HypothesisInputs;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The evidence target (prompt 138): the Admiralty grade maps to the weaker half on the 125 H/M/L scale, the default
 * is F6, and the draft cites the item as a record ref.
 */
class EvidenceBindingTargetTest {

    @Test
    @DisplayName("grade(): the weaker of reliability and credibility")
    void grades() {
        assertThat(EvidenceBindingTarget.grade("F", 6)).isEqualTo("L");
        assertThat(EvidenceBindingTarget.grade("B", 2)).isEqualTo("H");
        assertThat(EvidenceBindingTarget.grade("A", 1)).isEqualTo("H");
        assertThat(EvidenceBindingTarget.grade("C", 1)).isEqualTo("M");
        assertThat(EvidenceBindingTarget.grade("A", 3)).isEqualTo("M");
        assertThat(EvidenceBindingTarget.grade("B", 4)).isEqualTo("L");
        assertThat(EvidenceBindingTarget.grade("D", 1)).isEqualTo("L");
    }

    @Test
    @DisplayName("draft(): F6 by default, a record ref, the rating on the named hypothesis")
    void draft() {
        BoundItem item = BindingFixtures.item("incidents", "i-1", "event", 3, Map.of("category", "boarding"));
        HypothesisInputs.EvidenceDraft d = EvidenceBindingTarget.draft(Map.of("hypothesisSetId", "s",
                "hypothesisId", "h1"), BindingFixtures.stream("incidents"), item);
        assertThat(d.source()).isEqualTo("record");
        assertThat(d.ref()).isEqualTo(item.ref());
        assertThat(d.description()).contains("Admiralty F6").contains("boarding");
        assertThat(d.credibility()).isEqualTo("L");
        assertThat(d.relevance()).isEqualTo("M");
        assertThat(d.ratings()).containsExactly(Map.entry("h1", "C"));
        HypothesisInputs.EvidenceDraft graded = EvidenceBindingTarget.draft(Map.of("hypothesisSetId", "s",
                "sourceReliability", "B", "informationCredibility", 2, "relevance", "H"),
                BindingFixtures.stream("incidents"), item);
        assertThat(graded.credibility()).isEqualTo("H");
        assertThat(graded.relevance()).isEqualTo("H");
        assertThat(graded.ratings()).isEmpty();
        assertThat(graded.description()).contains("Admiralty B2");
    }

    @Test
    @DisplayName("summary(): title, text, then the observation's value")
    void summary() {
        assertThat(EvidenceBindingTarget.summary(BindingFixtures.item("levels", "x", "observation", 1,
                Map.of("seriesKey", "g", "t", "2026-10-17", "value", 2.5)))).contains("= 2.5");
        assertThat(EvidenceBindingTarget.summary(BindingFixtures.item("notes", "n", "document", 1,
                Map.of("title", "Crane out")))).isEqualTo("Crane out");
    }
}
