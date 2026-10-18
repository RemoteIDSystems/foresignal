package ai.foresignal.social.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.chronicle.ChronicleSelector;
import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.hypothesis.HypothesisVocabulary;
import ai.foresignal.hypothesis.IndicatorEntity;
import ai.foresignal.hypothesis.IndicatorRepository;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationService;
import ai.foresignal.situation.SituationViews;
import ai.foresignal.social.narrative.NarrativeStore;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.social.CorrelationTestSupport;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Social signals inside analysis (prompt 137): the situation's social section (narratives counted apart from physical
 * corroboration) and the narrative–situation linker; hypothesis indicators {@code reported} by a claim but
 * {@code observed} only from physical evidence; chronicle snapshots keeping claim history without posts; and the
 * assessment section "What people are saying versus what we observe" passing the ICD 203 linter.
 */
@IntegrationTest
class SocialIntegrationIT {

    static final Instant T0 = Instant.parse("2026-10-14T06:00:00Z");
    static final String NARRATIVE = "01JA8Q3V7S2K9W4T6Y1B5N0M3C";
    static final String CLAIM = "01JA8Q3V7S2K9W4T6Y1B5N0M3D";

    @Autowired MutableClock clock;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired NarrativeStore narratives;
    @Autowired EvidenceStore evidence;
    @Autowired SituationService situations;
    @Autowired SituationViews situationViews;
    @Autowired NarrativeSituationLinker linker;
    @Autowired HypothesisService hypotheses;
    @Autowired IndicatorRepository indicators;
    @Autowired ForecasterRegistry registry;
    @Autowired ComparisonService comparisons;
    @Autowired ChronicleSocialWriter chronicles;
    @Autowired SocialSections sections;

    String situationId;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0.plus(Duration.ofHours(1)));
        registry.registerBuiltIns();
        SituationEntity s = situations.create(json.readTree("{\"title\":\"Aden port\",\"theme\":\"mixed\"}"), "tester",
                "operator", false);
        situationId = s.getId();
        jdbc.update("UPDATE situation SET region_codes = '{YE}', signal_families = '{conflict}', started_at = ?,"
                + " last_activity_at = ? WHERE id = ?", Timestamp.from(T0), Timestamp.from(T0), situationId);
        CorrelationTestSupport.narrative(narratives, NARRATIVE, T0, 3, List.of(situationId), 7.0);
        CorrelationTestSupport.claim(narratives, CLAIM, NARRATIVE, "explosion", "conflict", T0, 3);
    }

    @Test
    @DisplayName("situation: a social section with the narrative and its evidence groups; the linker finds the situation")
    void situationSection() {
        Map<String, Object> full = situationViews.full(situations.get(situationId));
        @SuppressWarnings("unchecked")
        Map<String, Object> social = (Map<String, Object>) full.get("social");
        assertThat(social).isNotNull();
        assertThat((List<?>) social.get("narratives")).hasSize(1);
        assertThat(social.get("socialEvidenceGroups")).isEqualTo(3);
        assertThat(social.get("socialCoverageOk")).isEqualTo(false);
        assertThat(full.get("evidence")).isNotNull();
        assertThat(linker.situations(List.of("YE-AD"), List.of("conflict"), T0, T0)).contains(situationId);
        assertThat(linker.situations(List.of("YE"), List.of("fire"), T0, T0)).doesNotContain(situationId);
    }

    @Test
    @DisplayName("hypotheses: a claim marks the indicator reported (no revision); only physical evidence observes it")
    void indicators() {
        HypothesisSetEntity set = hypotheses.create(HypothesisInputs.set(json.readTree(setJson())), "agent", "tester");
        IndicatorEntity indicator = hypotheses.indicatorsOf(set.getId()).get(0);
        comparisons.compareNarrative(NARRATIVE);
        IndicatorEntity reported = indicators.findById(indicator.getId()).orElseThrow();
        assertThat(reported.getState()).isEqualTo(HypothesisVocabulary.REPORTED);
        assertThat(reported.getObservedEvidence()).containsEntry("source", "social-claim");
        assertThat(reported.waiting()).isTrue();

        String event = CorrelationTestSupport.canonicalEvent(evidence, jdbc, "conflict", "acled-events", "acled",
                "aggregator", T0.plus(Duration.ofMinutes(20)), CorrelationTestSupport.LON, CorrelationTestSupport.LAT,
                "YE");
        clock.setInstant(T0.plus(Duration.ofDays(3)));
        comparisons.compareNarrative(NARRATIVE);
        IndicatorEntity observed = indicators.findById(indicator.getId()).orElseThrow();
        assertThat(observed.getState()).isEqualTo(HypothesisVocabulary.OBSERVED);
        assertThat(observed.getObservedEvidence()).containsEntry("ref", "canonical-event:" + event)
                .containsEntry("source", "record");
    }

    @Test
    @DisplayName("chronicles: snapshots keep the narrative's claims with no post left; layers select by scope and status")
    void chronicleHistory() {
        NarrativeStore.Narrative n = narratives.narrative(NARRATIVE).orElseThrow();
        assertThat(narratives.members(NARRATIVE)).isEmpty();
        Map<String, Object> snapshot = chronicles.snapshot(n, true);
        assertThat((List<?>) snapshot.get("claims")).hasSize(1);
        assertThat(snapshot).doesNotContainKey("centralPostText");
        ChronicleSelector.SocialNarratives layer = new ChronicleSelector.SocialNarratives(List.of(), List.of(),
                List.of("YE"), List.of(), List.of("unassessed", "corroborated"), false);
        assertThat(layer.takes(n.topicIds(), n.placeRefs(), n.regionCodes(), n.situationIds(), n.status())).isTrue();
        assertThat(layer.takes(n.topicIds(), n.placeRefs(), List.of("SA"), List.of(), n.status())).isFalse();
        assertThat(new ChronicleSelector.SocialVolume(List.of(List.of("topic", "red-sea")), List.of("bluesky"),
                List.of()).refs()).containsExactly("social:topic:red-sea/bluesky/posts");
    }

    @Test
    @DisplayName("assessments: the social section counts narratives by status and its wording passes the ICD 203 linter")
    void assessmentSection() {
        Map<String, Object> section = sections.versusObserved(List.of(situationId), T0.minus(Duration.ofDays(1)),
                T0.plus(Duration.ofDays(1)));
        assertThat(section.get("narrativesByStatus")).isEqualTo(Map.of("unassessed", 1));
        assertThat((List<?>) section.get("leadTimes")).isEmpty();
        String markdown = SocialSections.markdown(section);
        assertThat(markdown).contains("out of sample only").doesNotContain("likely");
        assertThat(SocialSections.lint(markdown)).isEmpty();
    }

    private String setJson() {
        return "{\"question\":\"Coalition strike on Aden port within 7 days?\",\"kind\":\"exclusive\",\"cadence\":"
                + "\"daily\",\"horizon\":\"2026-10-24\",\"situationId\":\"" + situationId + "\",\"hypotheses\":["
                + "{\"label\":\"strike\",\"statement\":\"A strike happens.\",\"type\":\"outcome\",\"prior\":0.3,"
                + "\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Recent pattern.\"}},"
                + "{\"label\":\"none\",\"statement\":\"No strike.\",\"type\":\"outcome\",\"prior\":0.65,"
                + "\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Restraint.\"}},"
                + "{\"label\":\"other\",\"statement\":\"Other.\",\"type\":\"other\",\"prior\":0.05,"
                + "\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Residual.\"}}],"
                + "\"indicators\":[{\"description\":\"Explosions at the port\",\"signal\":{\"type\":\"manual\"},"
                + "\"signalFamily\":\"conflict\",\"credibility\":\"H\",\"relevance\":\"H\",\"lr\":{\"strike\":"
                + "{\"class\":\"high\",\"direction\":\"supports\"},\"none\":{\"class\":\"high\",\"direction\":"
                + "\"weakens\"}}}],\"assumptions\":[]}";
    }
}
