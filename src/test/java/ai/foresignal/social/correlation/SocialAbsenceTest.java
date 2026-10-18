package ai.foresignal.social.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.testsupport.social.CorrelationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Invariant SOC-2 (prompt 137): {@code unobserved} requires {@code canSeeAbsence} ok over the full window, otherwise
 * {@code insufficient-coverage}; an attention gap requires adequate social coverage, otherwise no finding.
 */
class SocialAbsenceTest {

    static final Instant T0 = Instant.parse("2026-10-17T06:00:00Z");

    @Test
    @DisplayName("SOC-2: no match reads as unobserved only with coverage")
    void comparisons() {
        Instant end = T0.plus(Duration.ofHours(24));
        for (boolean ok : new boolean[] {true, false}) {
            SocialPhysicalComparator.Decision d = SocialPhysicalComparator.decide(new SocialPhysicalComparator.Inputs(
                    T0, end, end.plus(Duration.ofHours(24)), end.plus(Duration.ofHours(25)), List.of(),
                    ok ? CorrelationTestSupport.covered() : CorrelationTestSupport.thin(), null, T0, null, null));
            assertThat(d.status()).isEqualTo(ok ? CorrelationVocabulary.UNOBSERVED : CorrelationVocabulary.INSUFFICIENT);
        }
    }

    @Test
    @DisplayName("SOC-2: an attention gap is not judged without adequate social coverage")
    void attentionGap() {
        AttentionGapDetector detector = AttentionGapDetectorTest.detector(null);
        detector.setReader(new AttentionGapDetector.VolumeReader() {
            @Override
            public AttentionGapDetector.Attention attention(String regionCode, Instant from, Instant to) {
                return new AttentionGapDetector.Attention(0, 100, List.of("bluesky"));
            }

            @Override
            public AbsenceCheck coverage(String regionCode, Instant from, Instant to) {
                return new AbsenceCheck(false, 0.2, "LOW_SOCIAL_COVERAGE");
            }
        });
        assertThat(detector.evaluate(new AttentionGapDetector.Candidate("finding:F", "Outage", "finding:outage",
                "significant", "YE", T0), new SocialSettingsService.Gap("significant", 10, 365, 6), T0.plusSeconds(
                        30_000))).isNull();
    }
}
