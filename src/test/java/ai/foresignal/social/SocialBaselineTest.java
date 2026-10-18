package ai.foresignal.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

/**
 * {@code SocialBaseline} 1.0.0 and the {@code social-velocity} finding (prompt 135, SOC-3): the hour-of-week median
 * over 8 weeks, denominator scaling that removes a planted platform-wide drop, the sample minimum, the velocity
 * thresholds (ratio and distinct authors), severity from the robust z, deduplication, and sampled series excluded.
 */
class SocialBaselineTest {

    static final Instant H = Instant.parse("2026-10-16T14:00:00Z");
    static final Duration WEEK = Duration.ofDays(7);

    private static Map<Instant, Long> weekly(long value, int weeks) {
        Map<Instant, Long> out = new HashMap<>();
        for (int k = 1; k <= weeks; k++) {
            out.put(H.minus(WEEK.multipliedBy(k)), value);
        }
        return out;
    }

    @Test
    @DisplayName("the same hour of the week over 8 weeks: median 10, observed 50 is 5× with a robust z")
    void hourOfWeek() {
        Map<Instant, Long> values = weekly(10, 8);
        values.put(H, 50L);
        Map<Instant, Long> denominators = weekly(1000, 8);
        denominators.put(H, 1000L);
        SocialBaseline.Point p = SocialBaseline.at(H, values, denominators, null);
        assertThat(p.baseline()).isEqualTo(10.0);
        assertThat(p.velocityRatio()).isEqualTo(5.0);
        assertThat(p.zRobust()).isCloseTo(40 / Math.sqrt(10), within(1e-9));
        assertThat(p.samples()).isEqualTo(8);
        assertThat(p.scaled()).isTrue();
        Map<Instant, Long> other = weekly(10, 8);
        other.put(H.minus(Duration.ofHours(1)).minus(WEEK), 999L);
        assertThat(SocialBaseline.at(H, other, Map.of(), null).baseline()).as("other hours are not samples")
                .isEqualTo(10.0);
    }

    @Test
    @DisplayName("a platform-wide drop (denominator halved) halves the expectation instead of reading as silence")
    void denominatorScaling() {
        Map<Instant, Long> values = weekly(10, 8);
        values.put(H, 5L);
        Map<Instant, Long> denominators = weekly(1000, 8);
        denominators.put(H, 500L);
        SocialBaseline.Point scaled = SocialBaseline.at(H, values, denominators, null);
        assertThat(scaled.baseline()).isEqualTo(5.0);
        assertThat(scaled.velocityRatio()).isEqualTo(1.0);
        SocialBaseline.Point raw = SocialBaseline.at(H, values, Map.of(), null);
        assertThat(raw.baseline()).isEqualTo(10.0);
        assertThat(raw.velocityRatio()).isEqualTo(0.5);
        assertThat(SocialBaseline.hasDenominators(Map.of(H, 0L))).as("zero denominators are none").isFalse();
    }

    @Test
    @DisplayName("fewer than 4 collected weeks: no baseline; uncollected weeks are not zero samples")
    void minimumSamples() {
        Map<Instant, Long> values = weekly(10, 3);
        values.put(H, 40L);
        assertThat(SocialBaseline.at(H, values, weekly(1000, 3), null).baseline()).isNull();
        assertThat(SocialBaseline.at(H, values, Map.of(), java.util.Set.of(H.minus(WEEK))).baseline()).isNull();
        assertThat(SocialBaseline.at(H, Map.of(H, 1L), Map.of(), null).baseline()).as("all hours collected: zeros")
                .isEqualTo(0.0);
    }

    @Test
    @DisplayName("sampled paths are never baselined; complete and paged are")
    void eligibility() {
        assertThat(SocialBaseline.eligible(SocialVocabulary.SAMPLED)).isFalse();
        assertThat(SocialBaseline.eligible(SocialVocabulary.COMPLETE)).isTrue();
        assertThat(SocialBaseline.eligible(SocialVocabulary.PAGED)).isTrue();
        assertThat(SocialBaseline.detector()).containsEntry("name", "SocialBaseline").containsEntry("version", "1.0.0");
    }

    private static SocialVolumeStore.VolumeRow row(Instant hour, String type, long posts, long authors) {
        return new SocialVolumeStore.VolumeRow("bluesky", "en", type, hour, posts, authors, 0, 0);
    }

    @SuppressWarnings("unchecked")
    private static VelocityDetector detector(SocialVolumeStore store, FindingService findings, SocialEvents events) {
        ObjectProvider<FindingService> fp = mock(ObjectProvider.class);
        when(fp.getIfAvailable()).thenReturn(findings);
        ObjectProvider<AttentionTopicRepository> tp = mock(ObjectProvider.class);
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.get(anyString(), eq(Map.class))).thenReturn(Optional.empty());
        return new VelocityDetector(store, fp, tp, events, settings, new MutableClock(H.plus(Duration.ofMinutes(5))));
    }

    private static SocialVolumeStore store(long observed, long authors, String type) {
        SocialVolumeStore store = mock(SocialVolumeStore.class);
        Map<String, List<SocialVolumeStore.VolumeRow>> hour = new LinkedHashMap<>();
        hour.put("topic:red-sea", List.of(row(H, type, observed, authors)));
        when(store.hour(H)).thenReturn(hour);
        List<SocialVolumeStore.VolumeRow> history = new java.util.ArrayList<>();
        for (int k = 1; k <= 8; k++) {
            history.add(row(H.minus(WEEK.multipliedBy(k)), type, 10, 8));
        }
        history.add(row(H, type, observed, authors));
        when(store.volume(eq("topic"), eq("red-sea"), eq("bluesky"), isNull(), any(), any())).thenReturn(history);
        Map<Instant, Long> denominators = weekly(1000, 8);
        denominators.put(H, 1000L);
        when(store.denominators(eq("bluesky"), isNull(), any(), any())).thenReturn(denominators);
        return store;
    }

    @Test
    @DisplayName("velocity: ratio ≥ 5 and ≥ 20 distinct authors make a finding with severity from z; 19 authors do not")
    void velocityThresholds() {
        VelocityDetector.Thresholds t = VelocityDetector.Thresholds.DEFAULT;
        assertThat(detector(store(60, 25, "complete"), null, null).candidates(H, t)).singleElement()
                .satisfies(c -> {
                    assertThat(c.point().velocityRatio()).isEqualTo(6.0);
                    assertThat(c.severity()).isEqualTo("significant");
                });
        assertThat(detector(store(60, 19, "complete"), null, null).candidates(H, t)).isEmpty();
        assertThat(detector(store(40, 25, "complete"), null, null).candidates(H, t)).as("ratio 4").isEmpty();
        assertThat(detector(store(60, 25, "sampled"), null, null).candidates(H, t)).as("sampled excluded").isEmpty();
        assertThat(VelocityDetector.severity(9.0)).isEqualTo("significant");
        assertThat(VelocityDetector.severity(5.0)).isEqualTo("notable");
        assertThat(VelocityDetector.severity(1.0)).isEqualTo("info");
        assertThat(VelocityDetector.severity(null)).isEqualTo("info");
    }

    @Test
    @DisplayName("a finding is recorded once per scope and platform per 6 hours and emits social.velocity")
    void recordAndDedup() {
        FindingService findings = mock(FindingService.class);
        SocialEvents events = mock(SocialEvents.class);
        AnalyticFindingEntity created = mock(AnalyticFindingEntity.class);
        when(created.getId()).thenReturn("01JFINDING0000000000000000");
        when(findings.record(any())).thenReturn(new FindingService.Outcome(created, FindingService.Change.CREATED));
        when(findings.detectedBetween(eq(SocialVocabulary.VELOCITY_FINDING), any(), any())).thenReturn(List.of());
        VelocityDetector d = detector(store(60, 25, "complete"), findings, events);
        assertThat(d.run(H)).isEqualTo(1);
        ArgumentCaptor<FindingDraft> draft = ArgumentCaptor.forClass(FindingDraft.class);
        verify(findings).record(draft.capture());
        assertThat(draft.getValue().findingType()).isEqualTo("social-velocity");
        assertThat(draft.getValue().seriesRefs()).containsExactly("social:topic:red-sea/bluesky/posts");
        assertThat(draft.getValue().dedupScope()).isEqualTo("topic:red-sea/bluesky");
        assertThat(draft.getValue().caveats()).extracting(c -> c.get("code")).contains("SOCIAL_IS_REPORTING");
        verify(events).velocity(any(), eq(false));

        FindingService again = mock(FindingService.class);
        AnalyticFindingEntity open = mock(AnalyticFindingEntity.class);
        when(open.getDedupKey()).thenReturn("social-velocity|topic:red-sea/bluesky");
        when(again.detectedBetween(eq(SocialVocabulary.VELOCITY_FINDING), any(), any())).thenReturn(List.of(open));
        assertThat(detector(store(60, 25, "complete"), again, events).run(H)).isZero();
        verify(again, never()).record(any());
    }
}
