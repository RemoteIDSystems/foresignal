package ai.foresignal.social.narrative;

import static ai.foresignal.testsupport.social.NarrativeTestSupport.locality;
import static ai.foresignal.testsupport.social.NarrativeTestSupport.places;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The neutral restatement re-validation (prompt 153): the LLM proposal is kept only when it de-hypes and introduces no
 * unresolvable specific, otherwise a deterministic restatement is synthesized. The extractor is never the arbiter.
 */
class ClaimRestatementTest {

    private static final PlaceIndex PLACES =
            places(Map.of("aden", List.of(locality("Aden", 45.03, 12.78, 800_000, "YE"))));

    @Test
    @DisplayName("a clean proposal naming only a resolvable place is kept")
    void keepsCleanProposal() {
        String out = ClaimRestatement.restatement("Explosion reported in Aden", "explosion", "asserted", null,
                "Aden", null, PLACES, EntityIndex.EMPTY);
        assertThat(out).isEqualTo("Explosion reported in Aden");
    }

    @Test
    @DisplayName("a proposal carrying hype is rejected and synthesized instead")
    void rejectsHype() {
        String out = ClaimRestatement.restatement("Outrageous strike slams Aden", "strike", "asserted", null,
                "Aden", null, PLACES, EntityIndex.EMPTY);
        assertThat(out).isEqualTo("strike in Aden");
    }

    @Test
    @DisplayName("a proposal inventing an unresolvable proper noun is rejected")
    void rejectsUnresolvableSpecific() {
        String out = ClaimRestatement.restatement("Explosion in Freedonia", "explosion", "asserted", null,
                "Aden", null, PLACES, EntityIndex.EMPTY);
        assertThat(out).isEqualTo("explosion in Aden");
    }

    @Test
    @DisplayName("an over-long proposal is rejected")
    void rejectsTooLong() {
        String proposal = "a report that " + "x".repeat(ClaimRestatement.MAX_LENGTH);
        assertThat(ClaimRestatement.validate(proposal, null, PLACES, EntityIndex.EMPTY)).isNull();
    }

    @Test
    @DisplayName("the synthesized restatement is neutral who/what/where/when with a denial suffix")
    void synthesizes() {
        String out = ClaimRestatement.synthesize("drone-strike", "denied", "Example Force", "Aden", "last night");
        assertThat(out).isEqualTo("Example Force: drone strike in Aden (last night) — denied");
    }

    @Test
    @DisplayName("a null proposal falls back to the synthesized restatement")
    void nullProposalSynthesizes() {
        String out = ClaimRestatement.restatement(null, "fire", "asserted", null, "Aden", null, PLACES,
                EntityIndex.EMPTY);
        assertThat(out).isEqualTo("fire in Aden");
    }
}
