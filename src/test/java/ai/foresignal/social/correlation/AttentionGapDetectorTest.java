package ai.foresignal.social.correlation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.social.SocialEvents;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@link AttentionGapDetector} 1.0.0 (prompt 137): the expected-attention model (log-normal regression, region-family
 * fallback), a gap below the 10th percentile with adequate coverage, and no finding under {@code LOW_SOCIAL_COVERAGE}.
 */
class AttentionGapDetectorTest {

    static final Instant T0 = Instant.parse("2026-10-17T06:00:00Z");
    static final SocialSettingsService.Gap GAP = new SocialSettingsService.Gap("significant", 10, 365, 6);

    @SuppressWarnings("unchecked")
    static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> p = mock(ObjectProvider.class);
        when(p.getIfAvailable()).thenReturn(value);
        return p;
    }

    /** A detector whose past candidates are {@code past} (or none for null) and whose finding service is given. */
    static AttentionGapDetector detector(FindingService findings) {
        return detector(findings, null);
    }

    static AttentionGapDetector detector(FindingService findings, List<AttentionGapDetector.Candidate> past) {
        return new AttentionGapDetector(mock(JdbcTemplate.class), provider(findings), provider(null), provider(null),
                mock(SocialSettingsService.class), mock(SocialEvents.class), mock(SocialNotifications.class),
                Clock.fixed(T0, ZoneOffset.UTC)) {
            @Override
            List<Candidate> past(Candidate c, Instant from, Instant to) {
                return past == null ? List.of() : past;
            }
        };
    }

    static AttentionGapDetector.VolumeReader reader(boolean ok, double observedPosts, double pastPosts) {
        return new AttentionGapDetector.VolumeReader() {
            @Override
            public AttentionGapDetector.Attention attention(String regionCode, Instant from, Instant to) {
                return new AttentionGapDetector.Attention(from.equals(T0) ? observedPosts : pastPosts, 100,
                        List.of("bluesky"));
            }

            @Override
            public AbsenceCheck coverage(String regionCode, Instant from, Instant to) {
                return ok ? new AbsenceCheck(true, 1.0, null) : new AbsenceCheck(false, 0.4, "LOW_SOCIAL_COVERAGE");
            }
        };
    }

    static List<AttentionGapDetector.Candidate> past(int n) {
        List<AttentionGapDetector.Candidate> out = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            out.add(new AttentionGapDetector.Candidate("finding:P" + i, "Past outage", "finding:outage", "significant",
                    "YE", T0.minusSeconds(86_400L * 7 * i)));
        }
        return out;
    }

    static AttentionGapDetector.Candidate candidate() {
        return new AttentionGapDetector.Candidate("finding:F", "Outage change point in Aden", "finding:outage",
                "significant", "YE", T0);
    }

    @Test
    @DisplayName("expected attention: log-normal fit over the same band, region-family fallback, none without history")
    void model() {
        List<Double> band = List.of(3.0, 3.2, 2.8, 3.1, 2.9, 3.0);
        AttentionGapDetector.Expected e = AttentionGapDetector.expected(band, List.of(), 10);
        assertThat(e.method()).isEqualTo("regression");
        assertThat(e.n()).isEqualTo(6);
        assertThat(e.expected()).isBetween(2.8, 3.2);
        assertThat(e.threshold()).isLessThan(e.expected()).isGreaterThan(0);
        AttentionGapDetector.Expected f = AttentionGapDetector.expected(List.of(1.0), List.of(2.0, 4.0, 6.0), 10);
        assertThat(f.method()).isEqualTo("region-family-median");
        assertThat(f.expected()).isEqualTo(4.0);
        assertThat(f.threshold()).isBetween(2.0, 4.0);
        assertThat(AttentionGapDetector.expected(List.of(1.0), List.of(1.0), 10)).isNull();
        assertThat(AttentionGapDetector.gap(0.1, e)).isTrue();
        assertThat(AttentionGapDetector.gap(3.0, e)).isFalse();
        assertThat(AttentionGapDetector.gap(0.1, null)).isFalse();
    }

    @Test
    @DisplayName("a gap below p10 with adequate coverage records an attention-gap finding with expected and observed")
    void gapRecorded() {
        FindingService findings = mock(FindingService.class);
        AnalyticFindingEntity entity = mock(AnalyticFindingEntity.class);
        when(entity.getId()).thenReturn("01JA8Q3V7S2K9W4T6Y1B5N0M3G");
        when(findings.record(any())).thenReturn(new FindingService.Outcome(entity, FindingService.Change.CREATED));
        AttentionGapDetector detector = detector(findings, past(8));
        detector.setReader(reader(true, 5, 300));
        assertThat(detector.evaluate(candidate(), GAP, T0.plusSeconds(30_000))).isEqualTo("01JA8Q3V7S2K9W4T6Y1B5N0M3G");
        ArgumentCaptor<FindingDraft> draft = ArgumentCaptor.forClass(FindingDraft.class);
        verify(findings).record(draft.capture());
        assertThat(draft.getValue().findingType()).isEqualTo(CorrelationVocabulary.ATTENTION_GAP_FINDING);
        assertThat(draft.getValue().stats()).containsEntry("observed", 5.0).containsKey("expected")
                .containsEntry("platforms", List.of("bluesky")).containsEntry("method", "regression");
        assertThat(draft.getValue().members()).containsExactly("finding:F");
    }

    @Test
    @DisplayName("usual attention is no gap")
    void noGap() {
        FindingService findings = mock(FindingService.class);
        AttentionGapDetector detector = detector(findings, past(8));
        detector.setReader(reader(true, 290, 300));
        assertThat(detector.evaluate(candidate(), GAP, T0.plusSeconds(30_000))).isNull();
        verify(findings, never()).record(any());
    }

    @Test
    @DisplayName("LOW_SOCIAL_COVERAGE: no finding, whatever the volume")
    void lowCoverage() {
        FindingService findings = mock(FindingService.class);
        AttentionGapDetector detector = detector(findings, past(8));
        detector.setReader(reader(false, 0, 300));
        assertThat(detector.evaluate(candidate(), GAP, T0.plusSeconds(30_000))).isNull();
        verify(findings, never()).record(any());
    }

    @Test
    @DisplayName("the finding draft says what was discussed, never what happened")
    void draft() {
        FindingDraft d = AttentionGapDetector.draft(candidate(), new AttentionGapDetector.Attention(5, 100,
                List.of("bluesky")), new AttentionGapDetector.Expected(3, 2, "regression", 8), 300, GAP, T0);
        assertThat(d.caveats()).anyMatch(c -> "SOCIAL_IS_REPORTING".equals(c.get("code")));
        assertThat(d.dedupScope()).isEqualTo("finding:F");
        assertThat(d.detectorName()).isEqualTo(CorrelationVocabulary.GAP_DETECTOR);
    }
}
