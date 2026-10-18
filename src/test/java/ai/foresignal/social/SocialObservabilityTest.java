package ai.foresignal.social;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.observability.Place;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.social.SocialTestSupport;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Social observability (prompt 135, the {@code social-attention} family): silence counts only where a healthy
 * complete stream was collecting and the region's trailing 8-week baseline reaches {@code social.minRegionBaseline};
 * a thin region is {@code LOW_SOCIAL_COVERAGE}, an unreplayed stream gap or a disabled stream is
 * {@code SOCIAL_STREAM_GAP}; {@code ObservabilityService.canSeeAbsence} delegates the family.
 */
@IntegrationTest
class SocialObservabilityTest {

    @Autowired SocialObservability observability;
    @Autowired ObservabilityService service;
    @Autowired SourceService sources;
    @Autowired StreamLedger ledger;
    @Autowired JdbcTemplate jdbc;

    Instant window;

    @BeforeEach
    void setUp() {
        SocialTestSupport.clean(jdbc);
        sources.seedDefaults();
        sources.update(SocialVocabulary.BLUESKY_JETSTREAM, SourceSettingsUpdate.enabled(true));
        window = Instant.now().truncatedTo(ChronoUnit.HOURS).minus(Duration.ofHours(8));
        seed("YE", 30, window.minus(Duration.ofDays(56)), window.plus(Duration.ofHours(6)));
        seed("OM", 5, window.minus(Duration.ofDays(56)), window.plus(Duration.ofHours(6)));
    }

    /** Complete hours with 1,000 denominator posts and {@code perHour} posts attributed to the region. */
    private void seed(String region, int perHour, Instant from, Instant to) {
        List<Object[]> denominators = new ArrayList<>();
        List<Object[]> coverage = new ArrayList<>();
        Timestamp now = Timestamp.from(Instant.now());
        for (Instant h = from; h.isBefore(to); h = h.plus(Duration.ofHours(1))) {
            Timestamp t = Timestamp.from(h);
            denominators.add(new Object[] {"bluesky|en|" + h.getEpochSecond(), t, now});
            coverage.add(new Object[] {"bluesky|" + region + "|" + h.getEpochSecond(), region, t, perHour, now});
        }
        jdbc.batchUpdate("INSERT INTO social_denominator_hourly (id, platform, language, hour, posts, complete,"
                + " updated_at) VALUES (?, 'bluesky', 'en', ?, 1000, true, ?) ON CONFLICT (id) DO NOTHING",
                denominators);
        jdbc.batchUpdate("INSERT INTO social_coverage (id, platform, region_code, hour, posts_seen, denominator,"
                + " complete, updated_at) VALUES (?, 'bluesky', ?, ?, ?, 1000, true, ?)", coverage);
    }

    @Test
    @DisplayName("a healthy complete stream and a baseline of 30 posts an hour: silence counts")
    void ok() {
        AbsenceCheck check = observability.canSeeAbsence("YE", window, window.plus(Duration.ofHours(6)));
        assertThat(check.ok()).isTrue();
        assertThat(check.reason()).isNull();
        assertThat(observability.expected("YE", window, window.plus(Duration.ofHours(6))))
                .filteredOn(e -> e.platformId().equals(SocialVocabulary.BLUESKY_JETSTREAM)).singleElement()
                .satisfies(e -> {
                    assertThat(e.complete()).isTrue();
                    assertThat(e.baselinePostsPerHour()).isEqualTo(30.0);
                    assertThat(e.expectedPosts()).isEqualTo(180.0);
                });
        assertThat(service.canSeeAbsence(SocialVocabulary.ATTENTION_FAMILY, Place.region("YE"), window,
                window.plus(Duration.ofHours(6)), null).ok()).as("ObservabilityService delegates the family").isTrue();
    }

    @Test
    @DisplayName("a region below social.minRegionBaseline is LOW_SOCIAL_COVERAGE")
    void lowCoverage() {
        AbsenceCheck check = observability.canSeeAbsence("OM", window, window.plus(Duration.ofHours(6)));
        assertThat(check.ok()).isFalse();
        assertThat(check.reason()).isEqualTo(SocialVocabulary.LOW_SOCIAL_COVERAGE);
        assertThat(check.p()).isEqualTo(0.25);
        assertThat(observability.canSeeAbsence("FR", window, window.plus(Duration.ofHours(6))).reason())
                .as("no posts there at all").isEqualTo(SocialVocabulary.LOW_SOCIAL_COVERAGE);
    }

    @Test
    @DisplayName("an unreplayed gap in the window fails; a replayed one does not; a disabled stream fails")
    void streamGap() {
        ledger.gap(SocialVocabulary.BLUESKY_JETSTREAM, window.plus(Duration.ofHours(1)),
                window.plus(Duration.ofHours(2)), true, "resumed");
        assertThat(observability.canSeeAbsence("YE", window, window.plus(Duration.ofHours(6))).ok()).isTrue();
        ledger.gap(SocialVocabulary.BLUESKY_JETSTREAM, window.plus(Duration.ofHours(3)),
                window.plus(Duration.ofHours(4)), false, "dropped");
        AbsenceCheck gap = observability.canSeeAbsence("YE", window, window.plus(Duration.ofHours(6)));
        assertThat(gap.ok()).isFalse();
        assertThat(gap.reason()).isEqualTo(SocialVocabulary.SOCIAL_STREAM_GAP);
        assertThat(observability.canSeeAbsence("YE", window.plus(Duration.ofHours(5)),
                window.plus(Duration.ofHours(6))).ok()).as("a window after the gap").isTrue();

        sources.update(SocialVocabulary.BLUESKY_JETSTREAM, SourceSettingsUpdate.enabled(false));
        assertThat(observability.canSeeAbsence("YE", window.plus(Duration.ofHours(5)),
                window.plus(Duration.ofHours(6))).reason()).isEqualTo(SocialVocabulary.SOCIAL_STREAM_GAP);
    }

    @Test
    @DisplayName("an hour marked incomplete in the window fails like a gap")
    void incompleteHour() {
        jdbc.update("UPDATE social_denominator_hourly SET complete = false WHERE hour = ?",
                Timestamp.from(window.plus(Duration.ofHours(2))));
        assertThat(observability.canSeeAbsence("YE", window, window.plus(Duration.ofHours(6))).reason())
                .isEqualTo(SocialVocabulary.SOCIAL_STREAM_GAP);
    }
}
