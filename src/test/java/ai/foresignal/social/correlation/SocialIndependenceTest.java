package ai.foresignal.social.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.social.CorrelationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Invariant SOC-1 (prompts 136–137): a narrative or claim never counts as independent corroboration of a physical
 * event or of itself; social originators are dropped from physical evidence and from lead times.
 */
class SocialIndependenceTest {

    @Test
    @DisplayName("SOC-1: any number of social observations never corroborates")
    void socialNeverCorroborates() {
        Instant t0 = Instant.parse("2026-10-17T06:00:00Z");
        Instant end = t0.plus(Duration.ofHours(24));
        List<PhysicalEvidenceSource.Evidence> evidence = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            evidence.add(CorrelationTestSupport.social("narrative:N" + i, t0.plusSeconds(i)));
        }
        SocialPhysicalComparator.Decision d = SocialPhysicalComparator.decide(new SocialPhysicalComparator.Inputs(t0,
                end, end.plus(Duration.ofHours(24)), end.plus(Duration.ofHours(25)), evidence,
                CorrelationTestSupport.thin(), null, t0, null, null));
        assertThat(d.status()).isNotEqualTo(CorrelationVocabulary.CORROBORATED);
        assertThat(d.evidence()).isEmpty();
        assertThat(SocialPhysicalComparator.leadTimes(t0, evidence, null).get("physicalFirst")).isNull();
    }
}
