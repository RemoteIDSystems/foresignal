package ai.foresignal.news;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.RecordRef;
import ai.foresignal.hypothesis.ConfidenceAssessor;
import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.object.DossierService;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.LinkerInput;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationMemberTypes;
import ai.foresignal.situation.SituationService;
import ai.foresignal.situation.SituationViews;
import ai.foresignal.source.formats.news.ArticleUrls;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.news.NewsTestSupport;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Coverage in analysis (prompt 133): the situation's and the dossier's coverage section, the assessment Coverage
 * section, a hypothesis's {@code SKEWED_COVERAGE} caveat over its supporting news evidence, and the confidence
 * step-down only when skewed support has no non-news corroboration.
 */
@IntegrationTest
class CoverageInAnalysisIT {

    static final Instant T0 = Instant.parse("2026-10-16T08:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired NewsStoryResolver resolver;
    @Autowired NewsStoryStore store;
    @Autowired NewsCoverageService coverage;
    @Autowired CanonicalRules rules;
    @Autowired OutletRegistry outlets;
    @Autowired AttentionTopicRepository topics;
    @Autowired SituationService situations;
    @Autowired SituationViews views;
    @Autowired DossierService dossiers;
    @Autowired JdbcTemplate jdbc;

    long seq = 50_000;

    @BeforeEach
    void setUp() {
        NewsTestSupport.clean(jdbc);
        topics.deleteAll();
        AttentionTopicEntity t = new AttentionTopicEntity("red-sea", T0);
        t.setName("Red Sea");
        t.setRegionCodes(List.of("YE"));
        t.setEnabled(true);
        topics.save(t);
    }

    /** Registers an outlet with an operator lean rating and its consensus. */
    private String ratedOutlet(String domain, double lean) {
        String id = outlets.resolve(domain, "US", "en");
        outlets.upsertRating(new OutletRegistry.Rating(id, "operator", "lean", lean, Double.toString(lean), null, null,
                "CC BY 4.0", "Test ratings", null, "OPEN", T0));
        outlets.recomputeConsensus(id, Set.of("operator"));
        return id;
    }

    private NewsStoryStore.Article article(String url, String title) {
        resolver.process(NewsTestSupport.rule(rules), NewsTestSupport.article(++seq, url, title, List.of("red-sea"),
                T0.plusSeconds(seq - 50_000)));
        return store.article("gdelt-articles", ArticleUrls.externalId(url)).orElseThrow();
    }

    private void evidence(String setId, String ref, Map<String, String> ratings, String source) {
        jdbc.update("INSERT INTO ach_evidence (id, set_id, source, ref, refs, description, credibility, relevance,"
                        + " ratings, applied, citations, added_by, added_at) VALUES (?, ?, ?, ?, CAST(? AS jsonb), ?,"
                        + " 'M', 'M', CAST(? AS jsonb), '{}'::jsonb, '[]'::jsonb, 'tester', ?)", Ulids.next(T0), setId,
                source, ref, JSON.writeValueAsString(List.of(ref)), "evidence " + ref,
                JSON.writeValueAsString(ratings), Timestamp.from(T0));
    }

    private static String ref(NewsStoryStore.Article a) {
        return "record:" + a.sourceId() + "/" + a.externalId() + "@" + a.seq();
    }

    @Test
    @DisplayName("situation and dossier coverage sections; the assessment Coverage section")
    void situationCoverage() {
        ratedOutlet("left-daily.example", -2);
        ratedOutlet("right-daily.example", 2);
        NewsStoryStore.Article a = article("https://left-daily.example/a", "Drone attack closes the port of Aden");
        article("https://right-daily.example/b", "Drone attack closes the port of Aden, officials say");
        SituationEntity s = situations.create(JSON.readTree("{\"title\":\"Aden port\",\"theme\":\"maritime\","
                + "\"status\":\"active\",\"regionCodes\":[\"YE\"]}"), "tester", "operator", true);
        situations.join(s.getId(), new LinkerInput(0, SituationMemberTypes.RECORD, "foresignal-intel/" + a.storyId(),
                "foresignal-intel", a.storyId(), 1, "news-story", "attention", "info", "Aden", "News story", T0, null,
                Set.of("YE"), null, Set.of(), "Aden", null, null, false, "context",
                List.of(new RecordRef("foresignal-intel", a.storyId(), null)), null), Map.of("score", 0.8));

        Map<String, Object> full = views.full(situations.get(s.getId()));
        assertThat(full.get("coverage")).isInstanceOfSatisfying(Map.class, c -> {
            assertThat(c.get("scope")).isEqualTo("situation");
            assertThat(c.get("coverageBalance").toString()).contains("left=0.5").contains("right=0.5");
            assertThat(c.get("corroboratedShare")).isEqualTo(0.0);
        });
        Map<String, Object> dossier = dossiers.dossier(ObjectRef.parse("situation:" + s.getId()), List.of("coverage"),
                false, ResolveContext.system());
        assertThat(dossier.get("coverage")).isInstanceOfSatisfying(Map.class,
                c -> assertThat(c.get("status")).isEqualTo("ok"));
        Map<String, Object> assessment = coverage.assessmentCoverage(List.of(s.getId()), T0.minusSeconds(86_400),
                T0.plusSeconds(86_400));
        assertThat(assessment).isNotNull();
        assertThat(assessment.get("situations").toString()).contains(s.getId());
        SituationEntity empty = situations.create(JSON.readTree("{\"title\":\"Quiet\",\"theme\":\"maritime\","
                + "\"status\":\"active\",\"regionCodes\":[\"OM\"]}"), "tester", "operator", true);
        assertThat(views.full(situations.get(empty.getId())).get("coverage")).isNull();
    }

    @Test
    @DisplayName("hypothesis: skewed news support carries SKEWED_COVERAGE; non-news corroboration lifts the step-down")
    void hypothesisSkew() {
        ratedOutlet("right-one.example", 2);
        ratedOutlet("right-two.example", 1.5);
        ratedOutlet("right-three.example", 2);
        NewsStoryStore.Article a = article("https://right-one.example/x", "Militia shells the airport overnight");
        NewsStoryStore.Article b = article("https://right-two.example/y", "Airport shelled by militia, residents say");
        NewsStoryStore.Article c = article("https://right-three.example/z", "Overnight shelling hits the airport");
        String set = Ulids.next(T0);
        for (NewsStoryStore.Article art : List.of(a, b, c)) {
            evidence(set, ref(art), Map.of("H1", "C", "H2", "I"), "record");
        }
        NewsCoverageService.HypothesisSupport h1 = coverage.hypothesisSupport(set, "H1");
        assertThat(h1.newsGroups()).isEqualTo(3);
        assertThat(h1.skewed()).isTrue();
        assertThat(h1.corroborated()).isFalse();
        assertThat(h1.skewedUncorroborated()).isTrue();
        assertThat(h1.coverage().get("caveats").toString()).contains("SKEWED_COVERAGE");
        assertThat(h1.coverage().get("scope")).isEqualTo("hypothesis");
        NewsCoverageService.HypothesisSupport h2 = coverage.hypothesisSupport(set, "H2");
        assertThat(h2.coverage()).as("inconsistent evidence is not support").isNull();
        assertThat(h2.skewedUncorroborated()).isFalse();

        ConfidenceAssessor.Inputs base = new ConfidenceAssessor.Inputs(List.of(new ConfidenceAssessor.Observed("i1",
                "conflict", "M", null)), false, false, null, null, null, h1.skewedUncorroborated());
        ConfidenceAssessor.Assessment lowered = ConfidenceAssessor.assess(base);
        assertThat(lowered.level()).isEqualTo("low");
        assertThat(lowered.basis()).containsEntry("newsCoverage", "SKEWED_COVERAGE")
                .containsEntry("assessor", "ConfidenceAssessor 1.2.0");

        evidence(set, "record:usgs-earthquakes/us7000test@5", Map.of("H1", "CC"), "record");
        NewsCoverageService.HypothesisSupport corroborated = coverage.hypothesisSupport(set, "H1");
        assertThat(corroborated.skewed()).isTrue();
        assertThat(corroborated.corroborated()).isTrue();
        assertThat(corroborated.skewedUncorroborated()).isFalse();
        ConfidenceAssessor.Assessment kept = ConfidenceAssessor.assess(new ConfidenceAssessor.Inputs(
                base.observed(), false, false, null, null, null, corroborated.skewedUncorroborated()));
        assertThat(kept.level()).isEqualTo("moderate");
        assertThat(kept.basis()).containsEntry("newsCoverage", "ok");
    }

    @Test
    @DisplayName("a skewed story becomes corroborated through its story, which also lifts the step-down")
    void storyCorroboration() {
        ratedOutlet("r1.example", 2);
        ratedOutlet("r2.example", 2);
        ratedOutlet("r3.example", 2);
        NewsStoryStore.Article a = article("https://r1.example/1", "Bridge collapse reported in the capital");
        NewsStoryStore.Article b = article("https://r2.example/2", "Bridge collapse in capital, reports say");
        NewsStoryStore.Article c = article("https://r3.example/3", "Capital bridge collapse reported");
        String set = Ulids.next(T0);
        for (NewsStoryStore.Article art : List.of(a, b, c)) {
            evidence(set, ref(art), Map.of("H1", "C"), "record");
        }
        assertThat(coverage.hypothesisSupport(set, "H1").skewedUncorroborated()).isTrue();
        store.updateCorroboration(a.storyId(), true, List.of("01TESTEVENT000000000000000"), T0);
        assertThat(coverage.hypothesisSupport(set, "H1").skewedUncorroborated()).isFalse();
    }
}
