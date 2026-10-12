package ai.foresignal.ask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link CitationVerifier}: formats, verification, de-duplication, titles and uncited-claim heuristics. */
class CitationVerifierTest {

    CitationSet citable;
    CitationVerifier verifier;

    @BeforeEach
    void setUp() {
        citable = new CitationSet();
        citable.addRecord("usgs:us1", 100, "M 5.5 - Tokyo Bay");
        citable.addRecord("usgs:us1", 120, null);
        citable.addRecord("usgs:us2", 7, null);
        citable.addAlert("42", "Big quakes");
        citable.addBriefing("b0c1", "Morning brief");
        citable.addText("{\"title\":\"M 5.5 - Tokyo Bay\",\"place\":\"Honshu\",\"name\":\"Tokyo Bay\"}");
        verifier = new CitationVerifier((id, seq) -> id.equals("usgs:us2") ? Optional.of("Looked up " + seq)
                : Optional.empty());
    }

    @Test
    @DisplayName("formats: @seq, bare (resolved to the highest returned seq), alert and briefing")
    void formats() {
        CitationVerifier.Verification v = verifier.verify(
                "One [usgs:us1@100]. Two [usgs:us1]. Alert [alert:42]. Brief [briefing:b0c1].", citable);

        assertThat(v.citations()).extracting(AskCitation::id, AskCitation::seq, AskCitation::type).containsExactly(
                tuple("usgs:us1", 100L, "record"),
                tuple("usgs:us1", 120L, "record"),
                tuple("alert:42", null, "alert"),
                tuple("briefing:b0c1", null, "briefing"));
        assertThat(v.citations().get(0).title()).isEqualTo("M 5.5 - Tokyo Bay");
        assertThat(v.citations().get(2).title()).isEqualTo("Big quakes");
        assertThat(v.citations().get(3).title()).isEqualTo("Morning brief");
        assertThat(v.warnings()).isEmpty();
    }

    @Test
    @DisplayName("drops an unknown id, an unreturned @seq, an unknown alert and briefing, each with a warning")
    void drops() {
        CitationVerifier.Verification v = verifier.verify(
                "A [usgs:nope]. B [usgs:us1@101]. C [alert:7]. D [briefing:zzz]. E [usgs:us1@100].", citable);

        assertThat(v.citations()).hasSize(1);
        assertThat(v.warnings()).containsSubsequence(
                "dropped citation [usgs:nope]: not returned by any tool",
                "dropped citation [usgs:us1@101]: not returned by any tool",
                "dropped citation [alert:7]: not returned by any tool",
                "dropped citation [briefing:zzz]: not returned by any tool");
    }

    @Test
    @DisplayName("de-duplicates repeated citations in order of first appearance")
    void deduplicates() {
        CitationVerifier.Verification v = verifier.verify(
                "[usgs:us2@7] then [usgs:us1@100] then [usgs:us2@7] and [usgs:us2].", citable);

        assertThat(v.citations()).extracting(AskCitation::id).containsExactly("usgs:us2", "usgs:us1");
    }

    @Test
    @DisplayName("titles come from tool output, else from the version lookup, else the id")
    void titles() {
        citable.addRecord("usgs:us3", 9, null);
        CitationVerifier.Verification v = verifier.verify("[usgs:us1@100] [usgs:us2@7] [usgs:us3@9]", citable);

        assertThat(v.citations()).extracting(AskCitation::title)
                .containsExactly("M 5.5 - Tokyo Bay", "Looked up 7", "usgs:us3");
    }

    @Test
    @DisplayName("markdown links are not citations")
    void links() {
        CitationVerifier.Verification v = verifier.verify("See [docs:guide](https://example.org) [usgs:us1@100].",
                citable);

        assertThat(v.citations()).hasSize(1);
        assertThat(v.warnings()).isEmpty();
    }

    @Test
    @DisplayName("uncited claims: numbers, dates, coordinates and returned names are flagged; cited segments are not")
    void uncitedHeuristics() {
        String answer = """
                ## Summary
                - A magnitude 5.5 quake was recorded.
                - It happened on Monday morning.
                - Shaking was felt near 35.6, 139.8.
                - The epicenter was off the coast of Honshu.
                - Residents near Tokyo Bay felt it [usgs:us1@100].
                - Nothing else is known.
                - Experts in Narnia were surprised.
                """;
        CitationVerifier.Verification v = verifier.verify(answer, citable);

        assertThat(v.warnings()).containsExactly(
                "uncited claim: A magnitude 5.5 quake was recorded.",
                "uncited claim: It happened on Monday morning.",
                "uncited claim: Shaking was felt near 35.6, 139.8.",
                "uncited claim: The epicenter was off the coast of Honshu.");
    }

    @Test
    @DisplayName("sentences are checked one by one, and a long claim is cut to 80 characters")
    void sentencesAndCut() {
        String longClaim = "The quake reached 5.5 and " + "very ".repeat(30) + "strong.";
        CitationVerifier.Verification v = verifier.verify("Cited [usgs:us1@100]. " + longClaim, citable);

        assertThat(v.warnings()).hasSize(1);
        assertThat(v.warnings().getFirst()).isEqualTo("uncited claim: " + longClaim.substring(0, 80));
    }

    @Test
    @DisplayName("after ten uncited claims the rest are counted in one warning")
    void cap() {
        StringBuilder answer = new StringBuilder("Source [usgs:us1@100].\n");
        for (int i = 1; i <= 13; i++) {
            answer.append("- Item ").append(i).append(" happened.\n");
        }
        CitationVerifier.Verification v = verifier.verify(answer.toString(), citable);

        assertThat(v.warnings()).hasSize(11);
        assertThat(v.warnings().getLast()).isEqualTo("…and 3 more");
    }

    @Test
    @DisplayName("tools returned data but the answer cites nothing: 'answer contains no citations'")
    void noCitations() {
        CitationVerifier.Verification v = verifier.verify("Nothing to report.", citable);
        assertThat(v.warnings()).containsExactly(CitationVerifier.NO_CITATIONS);

        CitationVerifier.Verification empty = verifier.verify("Nothing to report.", new CitationSet());
        assertThat(empty.warnings()).isEmpty();
    }

    @Test
    @DisplayName("code fences are skipped by the claim check")
    void codeFences() {
        CitationVerifier.Verification v = verifier.verify("Rule [usgs:us1@100].\n```\nmag >= 6\n```", citable);

        assertThat(v.warnings()).isEmpty();
    }
}
