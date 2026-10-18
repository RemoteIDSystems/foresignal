package ai.foresignal.social.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastStatus;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.precursor.Candidate;
import ai.foresignal.precursor.CandidateGenerator;
import ai.foresignal.precursor.Fdr;
import ai.foresignal.social.narrative.NarrativeStore;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.social.CorrelationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The predictive value of social signals, measured out of sample (prompt 137, SOC-5): social volume series as 120
 * precursor candidates (antecedents of their region's physical series, Benjamini–Hochberg and later-period validation
 * unchanged); the {@code social-claim} forecaster's questions resolving yes and annulled; lead-time statistics that
 * state leads only from comparisons after the training cut-off.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
class SocialLeadLagIT {

    static final Instant T0 = Instant.parse("2026-10-14T06:00:00Z");

    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired NarrativeStore narratives;
    @Autowired EvidenceStore evidence;
    @Autowired ComparisonService comparisons;
    @Autowired ComparisonStore store;
    @Autowired ForecastService ledger;
    @Autowired ForecasterRegistry registry;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0.plus(Duration.ofHours(1)));
        registry.registerBuiltIns();
    }

    @Test
    @DisplayName("social series are antecedents of their region's physical series, never consequents (family social)")
    void precursorCandidates() {
        List<CandidateGenerator.SeriesInfo> series = List.of(
                new CandidateGenerator.SeriesInfo("social:topic:red-sea/bluesky/posts", "hour", "YE", "social",
                        null, 500, "Red Sea on Bluesky"),
                new CandidateGenerator.SeriesInfo("rollup:day/*/outage/YE", "day", "YE", "outage", null, 300,
                        "Outages in Yemen"),
                new CandidateGenerator.SeriesInfo("rollup:news/red-sea/all", "day", "YE", "attention", null, 300,
                        "News"),
                new CandidateGenerator.SeriesInfo("social:place:aden/x/posts", "hour", "YE", "social", null, 400,
                        "Aden on X"));
        CandidateGenerator.Plan plan = CandidateGenerator.plan(List.of(), List.of(), series, (a, b) -> 100, null,
                CandidateGenerator.Settings.defaults());
        List<String> pairs = new ArrayList<>();
        for (Candidate c : plan.selected()) {
            pairs.add(Candidate.seriesRef(c.antecedent()) + " -> " + Candidate.seriesRef(c.consequent()));
            if ("social".equals(c.antecedent().get("family"))) {
                assertThat(c.consequent().get("family")).isEqualTo("outage");
            }
            assertThat(c.consequent().get("family")).isNotEqualTo("social");
        }
        assertThat(pairs).contains("social:topic:red-sea/bluesky/posts -> rollup:day/*/outage/YE",
                "social:place:aden/x/posts -> rollup:day/*/outage/YE");
        Fdr.Adjusted adjusted = Fdr.adjust(new double[] {0.001, 0.04, 0.3, 0.8}, false, Fdr.DEFAULT_Q);
        assertThat(adjusted.method()).isEqualTo(Fdr.BH);
        assertThat(adjusted.survivors()).isEqualTo(1);
    }

    @Test
    @DisplayName("social-claim: a pending comparison opens a question with a forecast; corroboration resolves it yes")
    void questionResolvesYes() {
        CorrelationTestSupport.narrative(narratives, "01JA8Q3V7S2K9W4T6Y1B5N0M3C", T0, 3, List.of(), null);
        CorrelationTestSupport.claim(narratives, "01JA8Q3V7S2K9W4T6Y1B5N0M3D", "01JA8Q3V7S2K9W4T6Y1B5N0M3C",
                "explosion", "conflict", T0, 3);
        comparisons.compareNarrative("01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        ComparisonStore.Comparison pending = store.byClaim("01JA8Q3V7S2K9W4T6Y1B5N0M3D").orElseThrow();
        assertThat(pending.status()).isEqualTo(CorrelationVocabulary.PENDING);
        assertThat(pending.questionId()).isNotNull();
        assertThat(ledger.latestByForecaster(pending.questionId())).containsKey(CorrelationVocabulary.FORECASTER);
        CorrelationTestSupport.canonicalEvent(evidence, jdbc, "conflict", "acled-events", "acled", "aggregator",
                T0.plus(Duration.ofMinutes(30)), CorrelationTestSupport.LON, CorrelationTestSupport.LAT, "YE");
        clock.setInstant(T0.plus(Duration.ofDays(3)));
        comparisons.compareNarrative("01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        ForecastQuestionEntity q = ledger.find(pending.questionId()).orElseThrow();
        assertThat(q.getStatus()).isEqualTo(ForecastStatus.RESOLVED);
        assertThat(q.getResolution()).containsEntry("outcome", "yes");
        assertThat(store.revisions(pending.id())).extracting(ComparisonStore.Revision::status)
                .containsExactly(CorrelationVocabulary.PENDING, CorrelationVocabulary.CORROBORATED);
    }

    @Test
    @DisplayName("social-claim: insufficient coverage annuls the question (COVERAGE_GAP)")
    void questionAnnulled() {
        CorrelationTestSupport.narrative(narratives, "01JA8Q3V7S2K9W4T6Y1B5N0M3E", T0, 4, List.of(), null);
        CorrelationTestSupport.claim(narratives, "01JA8Q3V7S2K9W4T6Y1B5N0M3F", "01JA8Q3V7S2K9W4T6Y1B5N0M3E",
                "drone", "conflict", T0, 4);
        comparisons.compareNarrative("01JA8Q3V7S2K9W4T6Y1B5N0M3E");
        String question = store.byClaim("01JA8Q3V7S2K9W4T6Y1B5N0M3F").orElseThrow().questionId();
        clock.setInstant(T0.plus(Duration.ofDays(3)));
        comparisons.compareNarrative("01JA8Q3V7S2K9W4T6Y1B5N0M3E");
        assertThat(store.byClaim("01JA8Q3V7S2K9W4T6Y1B5N0M3F").orElseThrow().status())
                .isEqualTo(CorrelationVocabulary.INSUFFICIENT);
        ForecastQuestionEntity q = ledger.find(question).orElseThrow();
        assertThat(q.getStatus()).isEqualTo(ForecastStatus.ANNULLED);
        assertThat(q.getAnnulment()).containsEntry("code", "COVERAGE_GAP");
    }

    @Test
    @DisplayName("the forecaster backs off from sparse cells to the claim type and to all claims under a Jeffreys prior")
    void forecaster() {
        List<SocialClaimForecaster.Outcome> history = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            history.add(new SocialClaimForecaster.Outcome("explosion", List.of("x"), "YE", 3, i < 6));
        }
        history.add(new SocialClaimForecaster.Outcome("flood", List.of("x"), "PK", 3, false));
        SocialClaimForecaster.Estimate cell = SocialClaimForecaster.estimate(history, "explosion", List.of("x"), "YE",
                2);
        assertThat(cell.level()).isEqualTo("type+platforms+region+band");
        assertThat(cell.probability()).isEqualTo(6.5 / 9.0);
        SocialClaimForecaster.Estimate type = SocialClaimForecaster.estimate(history, "explosion", List.of("bluesky"),
                "SA", 2);
        assertThat(type.level()).isEqualTo("type");
        SocialClaimForecaster.Estimate all = SocialClaimForecaster.estimate(history, "fire", List.of(), null, 1);
        assertThat(all.level()).isEqualTo("all");
        assertThat(SocialClaimForecaster.estimate(List.of(), "fire", List.of(), null, 1).probability()).isEqualTo(0.5);
        assertThat(SocialClaimForecaster.band(1)).isEqualTo(1);
        assertThat(SocialClaimForecaster.band(5)).isEqualTo(4);
    }

    @Test
    @DisplayName("lead times: only comparisons after the training cut-off count as out of sample (SOC-5)")
    void outOfSample() {
        List<LeadTimeStatistics.Point> points = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            points.add(new LeadTimeStatistics.Point("explosion", List.of("bluesky"), "YE", "organic",
                    T0.plus(Duration.ofDays(i)), 600.0 * (i + 1), null, null));
        }
        points.add(new LeadTimeStatistics.Point("explosion", List.of("bluesky"), "YE", "cue",
                T0.plus(Duration.ofDays(20)), 9_999.0, null, null));
        Map<String, Object> stats = LeadTimeStatistics.compute(points, new LeadTimeStatistics.Filters(null, null, "YE",
                "organic", null), 2.0 / 3.0, T0.plus(Duration.ofDays(30)));
        Instant cutoff = Instant.parse(stats.get("trainingCutoff").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> groups = (List<Map<String, Object>>) stats.get("groups");
        assertThat(groups).hasSize(1);
        Map<String, Object> g = groups.get(0);
        assertThat(g.get("comparisons")).isEqualTo(9);
        long after = points.stream().filter(p -> "organic".equals(p.foundBy()) && p.evaluatedAt().isAfter(cutoff))
                .count();
        assertThat(g.get("outOfSample")).isEqualTo((int) after);
        @SuppressWarnings("unchecked")
        Map<String, Object> oos = (Map<String, Object>) g.get("outOfSampleOverPhysical");
        assertThat(((Number) oos.get("n")).intValue()).isEqualTo((int) after);
        assertThat(((Number) oos.get("median")).doubleValue()).isGreaterThan(((Number) ((Map<?, ?>) g
                .get("overPhysical")).get("median")).doubleValue());
        assertThat(LeadTimeStatistics.summary(List.of())).containsEntry("n", 0);
    }
}
