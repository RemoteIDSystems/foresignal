package ai.foresignal.analyst;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analyst.AnalystOutputValidator.RawClaim;
import ai.foresignal.analyst.AnalystOutputValidator.RawProposal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The pure output validator (prompt 102). */
class AnalystOutputValidatorTest {

    private static AnalystOutputValidator.CitationCheck citations(String... ids) {
        Set<String> set = Set.of(ids);
        return set::contains;
    }

    private static AnalystOutputValidator.MemberCheck members(int verifiedCount, boolean verified) {
        return (type, list) -> verified && list.size() >= verifiedCount;
    }

    @Test
    @DisplayName("an evidence claim without a valid citation downgrades to speculation")
    void uncitedEvidence() {
        var out = AnalystOutputValidator.validate("t", "body", "normal",
                List.of(new RawClaim("a thing happened", AnalystClaim.EVIDENCE, List.of())), List.of(),
                citations(), members(2, true), Set.of(), Set.of("custom"));
        assertThat(out.claims()).hasSize(1);
        assertThat(out.claims().get(0).basis()).isEqualTo(AnalystClaim.SPECULATION);
        assertThat(out.claims().get(0).downgraded()).isTrue();
        assertThat(out.claimsDowngraded()).isEqualTo(1);
    }

    @Test
    @DisplayName("an unknown citation is dropped with a warning")
    void unknownCitationDropped() {
        var out = AnalystOutputValidator.validate("t", "body", "normal",
                List.of(new RawClaim("a thing happened", AnalystClaim.EVIDENCE, List.of("x:y@1", "x:z@2"))), List.of(),
                citations("x:y@1"), members(2, true), Set.of(), Set.of("custom"));
        assertThat(out.claims().get(0).citations()).containsExactly("x:y@1");
        assertThat(out.citationsDropped()).isEqualTo(1);
        assertThat(out.claims().get(0).basis()).isEqualTo(AnalystClaim.EVIDENCE);
    }

    @Test
    @DisplayName("an event proposal with fewer than two verified members is rejected")
    void eventProposalMembers() {
        var out = AnalystOutputValidator.validate("t", "body", "normal", List.of(),
                List.of(new RawProposal(AnalystActions.PROPOSE_INTEL_EVENT,
                        Map.of("type", "custom", "members", List.of(Map.of("seq", 1L))), "why", List.of())),
                citations(), members(2, true), Set.of(AnalystActions.PROPOSE_INTEL_EVENT), Set.of("custom"));
        assertThat(out.proposals()).isEmpty();
        assertThat(out.warnings()).anyMatch(w -> w.contains("fewer than two verified members"));
    }

    @Test
    @DisplayName("a disallowed proposal type is dropped")
    void disallowedProposal() {
        var out = AnalystOutputValidator.validate("t", "body", "normal", List.of(),
                List.of(new RawProposal(AnalystActions.PROPOSE_WATCH, Map.of("rule", Map.of()), "why", List.of())),
                citations(), members(2, true), Set.of(), Set.of("custom"));
        assertThat(out.proposals()).isEmpty();
        assertThat(out.warnings()).anyMatch(w -> w.contains("not in allowed_actions"));
    }

    @Test
    @DisplayName("an absence claim without a citation is warned")
    void absenceWarned() {
        var out = AnalystOutputValidator.validate("t", "body", "normal",
                List.of(new RawClaim("the vessel stopped reporting", AnalystClaim.INFERENCE, List.of())), List.of(),
                citations(), members(2, true), Set.of(), Set.of("custom"));
        assertThat(out.warnings()).anyMatch(w -> w.contains("absence claim"));
    }

    @Test
    @DisplayName("a note over 2000 words is truncated")
    void truncated() {
        String body = ("word ").repeat(2_100);
        var out = AnalystOutputValidator.validate("t", body, "normal", List.of(), List.of(), citations(),
                members(2, true), Set.of(), Set.of("custom"));
        assertThat(out.body().split("\\s+")).hasSize(2_000);
        assertThat(out.warnings()).anyMatch(w -> w.contains("truncated"));
    }

    @Test
    @DisplayName("an LLM-supplied confidence, weight, family and role are discarded from an event proposal")
    void llmConfidenceDiscarded() {
        Map<String, Object> member = new java.util.LinkedHashMap<>();
        member.put("memberType", "record");
        member.put("sourceId", "s");
        member.put("externalId", "e1");
        member.put("seq", 1L);
        member.put("weight", 1.0);
        member.put("signalFamily", "seismic");
        member.put("role", "supporting");
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("type", "custom");
        payload.put("title", "Pair");
        payload.put("confidence", 0.99);
        payload.put("members", List.of(member, Map.of("memberType", "record", "sourceId", "s", "externalId", "e2",
                "seq", 2L)));

        var out = AnalystOutputValidator.validate("t", "body", "normal", List.of(),
                List.of(new RawProposal(AnalystActions.PROPOSE_INTEL_EVENT, payload, "why", List.of())), citations(),
                members(2, true), Set.of(AnalystActions.PROPOSE_INTEL_EVENT), Set.of("custom"));

        assertThat(out.proposals()).hasSize(1);
        Map<String, Object> kept = out.proposals().get(0).payload();
        assertThat(kept).containsOnlyKeys("type", "title", "members");
        assertThat(AnalystOutputValidator.memberList(kept))
                .allSatisfy(m -> assertThat(m).containsOnlyKeys("memberType", "sourceId", "externalId", "seq"));
        assertThat(out.warnings()).anyMatch(w -> w.contains("confidence"));
    }
}
