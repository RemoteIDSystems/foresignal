package ai.foresignal.social.narrative;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.hypothesis.HypothesisVocabulary;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Claims as 125 evidence items (prompt 136, {@code source = social-claim}): Admiralty source reliability from the
 * account's or channel's 137 track record and operator overrides, information credibility from the claim's status,
 * and the grade a claim view carries.
 */
class SocialEvidenceItemTest {

    @Test
    @DisplayName("reliability: F by default and below 10 assessed claims; E at 70%, D at 85%")
    void trackRecord() {
        assertThat(SocialEvidenceGrades.reliability(0, 0, null)).isEqualTo("F");
        assertThat(SocialEvidenceGrades.reliability(9, 9, null)).as("too few assessed").isEqualTo("F");
        assertThat(SocialEvidenceGrades.reliability(10, 6, null)).isEqualTo("F");
        assertThat(SocialEvidenceGrades.reliability(10, 7, null)).isEqualTo("E");
        assertThat(SocialEvidenceGrades.reliability(20, 16, null)).isEqualTo("E");
        assertThat(SocialEvidenceGrades.reliability(20, 17, null)).isEqualTo("D");
        assertThat(SocialEvidenceGrades.reliability(40, 40, null)).isEqualTo("D");
    }

    @Test
    @DisplayName("operator overrides per account or channel win; malformed overrides are ignored")
    void overrides() {
        Map<String, Object> overrides = Map.of("channel:reddit:worldnews", "c", "account:x:abc", "B",
                "account:x:bad", "Z");
        assertThat(SocialEvidenceGrades.reliability(0, 0, SocialEvidenceGrades.override(overrides,
                "channel:reddit:worldnews"))).isEqualTo("C");
        assertThat(SocialEvidenceGrades.reliability(40, 40, SocialEvidenceGrades.override(overrides,
                "account:x:abc"))).isEqualTo("B");
        assertThat(SocialEvidenceGrades.reliability(0, 0, SocialEvidenceGrades.override(overrides,
                "account:x:bad"))).isEqualTo("F");
        assertThat(SocialEvidenceGrades.override(null, "account:x:abc")).isNull();
        assertThat(SocialEvidenceGrades.sourceKey("reddit", "worldnews", "k")).isEqualTo("channel:reddit:worldnews");
        assertThat(SocialEvidenceGrades.sourceKey("x", null, "k")).isEqualTo("account:x:k");
    }

    @Test
    @DisplayName("credibility from the 137 status: 6 unassessed, 2 corroborated, 5 unobserved, 4 contradicted")
    void credibility() {
        assertThat(SocialEvidenceGrades.credibility("unassessed")).isEqualTo(6);
        assertThat(SocialEvidenceGrades.credibility("pending")).isEqualTo(6);
        assertThat(SocialEvidenceGrades.credibility("insufficient-coverage")).isEqualTo(6);
        assertThat(SocialEvidenceGrades.credibility("corroborated")).isEqualTo(2);
        assertThat(SocialEvidenceGrades.credibility("unobserved")).isEqualTo(5);
        assertThat(SocialEvidenceGrades.credibility("contradicted")).isEqualTo(4);
        assertThat(SocialEvidenceGrades.grade("F", 6)).isEqualTo("F6");
    }

    @Test
    @DisplayName("the claim view grades from the current status, and social-claim is an accepted evidence source")
    void claimView() {
        Instant t = Instant.parse("2026-10-17T10:00:00Z");
        NarrativeStore.Claim c = new NarrativeStore.Claim("01JA8Q3V7S2K9W4T6Y1B5N0M3C", "N", "explosion|asserted|aden",
                "explosion", "conflict", "asserted", false, null, null, null, null, List.of(), null, null, 3, 2, t,
                "deterministic", "01JA8Q3V7S2K9W4T6Y1B5N0M3D", 0.7, "corroborated", null, "account:x:k", "E", 6,
                "CLEAR", t, t);
        Map<String, Object> view = NarrativeViews.claim(c);
        assertThat(view.get("informationCredibility")).isEqualTo(2);
        assertThat(view.get("grade")).isEqualTo("E2");
        assertThat(view.get("ref")).isEqualTo("claim:01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        assertThat(HypothesisVocabulary.INPUT_EVIDENCE_SOURCES).contains(NarrativeVocabulary.EVIDENCE_SOURCE);
    }
}
