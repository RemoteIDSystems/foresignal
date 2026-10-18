package ai.foresignal.social.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.social.SocialBaseline;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.testsupport.social.CorrelationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Invariant SOC-3 (prompt 137): sampled and cue-found posts never feed volume baselines, and a cue never corroborates
 * its own trigger (a trigger-only match is caveated {@code CUE_TRIGGER_MATCH}; an independent match is not).
 */
class SocialCueLoopTest {

    static final Instant T0 = Instant.parse("2026-10-17T06:00:00Z");

    static SocialPhysicalComparator.Decision decide(List<PhysicalEvidenceSource.Evidence> evidence, String trigger) {
        Instant end = T0.plus(Duration.ofHours(24));
        return SocialPhysicalComparator.decide(new SocialPhysicalComparator.Inputs(T0, end, end.plus(
                Duration.ofHours(24)), end.plus(Duration.ofHours(25)), evidence, CorrelationTestSupport.covered(), null,
                T0, null, trigger));
    }

    @Test
    @DisplayName("SOC-3: sampled collection is never baselined")
    void sampledNotBaselined() {
        assertThat(SocialBaseline.eligible(SocialVocabulary.SAMPLED)).isFalse();
        assertThat(SocialBaseline.eligible(SocialVocabulary.COMPLETE)).isTrue();
    }

    @Test
    @DisplayName("SOC-3: a trigger-only match is caveated; independent evidence besides the trigger is not")
    void triggerOnly() {
        PhysicalEvidenceSource.Evidence trigger = CorrelationTestSupport.physical("canonical-event:T", "aggregator", T0);
        PhysicalEvidenceSource.Evidence other = CorrelationTestSupport.physical("finding:F", "derived", T0);
        assertThat(decide(List.of(trigger), "canonical-event:T").caveats())
                .anyMatch(c -> CorrelationVocabulary.CUE_TRIGGER_MATCH.equals(c.get("code")));
        assertThat(decide(List.of(trigger, other), "canonical-event:T").caveats())
                .noneMatch(c -> CorrelationVocabulary.CUE_TRIGGER_MATCH.equals(c.get("code")));
        assertThat(decide(List.of(trigger), null).caveats())
                .noneMatch(c -> CorrelationVocabulary.CUE_TRIGGER_MATCH.equals(c.get("code")));
    }
}
