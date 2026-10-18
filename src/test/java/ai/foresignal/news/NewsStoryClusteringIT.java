package ai.foresignal.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.evidence.ObservationFacts;
import ai.foresignal.evidence.OriginatorRegistry;
import ai.foresignal.evidence.RecordRef;
import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.situation.LinkerInput;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationMemberTypes;
import ai.foresignal.situation.SituationService;
import ai.foresignal.source.formats.news.ArticleUrls;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.news.NewsTestSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code news-story} family end to end (prompt 133, NEWS-2): articles cluster by title embedding and by the token
 * fallback, within the window and a shared topic; 30 reprints of a wire story form one evidence group with
 * {@code syndicated} members; a story links to a situation and is {@code corroborated} by a sensor-based canonical
 * event in the same region and window. The resolver is driven directly with article versions (as the evidence
 * resolver hands them over); title embeddings are stubbed.
 */
@IntegrationTest
class NewsStoryClusteringIT {

    static final Instant T0 = Instant.parse("2026-10-16T08:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired NewsStoryResolver resolver;
    @Autowired NewsStoryStore store;
    @Autowired NewsCoverageService coverage;
    @Autowired NewsCoverageJob job;
    @Autowired CanonicalRules rules;
    @Autowired EvidenceStore evidence;
    @Autowired OriginatorRegistry originators;
    @Autowired OutletRegistry outlets;
    @Autowired AttentionTopicRepository topics;
    @Autowired SituationService situations;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean TitleEmbeddings embeddings;

    long seq = 1_000;
    final Map<String, float[]> vectors = new LinkedHashMap<>();

    @BeforeEach
    void setUp() {
        NewsTestSupport.clean(jdbc);
        jdbc.update("DELETE FROM evidence_group WHERE canonical_event_id IN (SELECT id FROM canonical_event"
                + " WHERE family = 'seismic' AND first_observation_ref LIKE 'record:test-sensor/%')");
        jdbc.update("DELETE FROM observation_link WHERE source_id = 'test-sensor'");
        jdbc.update("DELETE FROM canonical_event WHERE first_observation_ref LIKE 'record:test-sensor/%'");
        topics.deleteAll();
        outlets.seedWires();
        vectors.clear();
        when(embeddings.vectors(any())).thenAnswer(inv -> {
            Collection<String> keys = inv.getArgument(0);
            Map<String, float[]> out = new LinkedHashMap<>();
            for (String k : keys) {
                if (vectors.containsKey(k)) {
                    out.put(k, vectors.get(k));
                }
            }
            return new TitleEmbeddings.TitleVectors(out.isEmpty() ? null : "test-model", out);
        });
        topic("red-sea", List.of("YE"));
        topic("hormuz", List.of("IR", "OM"));
    }

    private void topic(String id, List<String> regions) {
        AttentionTopicEntity t = new AttentionTopicEntity(id, T0);
        t.setName(id);
        t.setRegionCodes(regions);
        t.setEnabled(true);
        topics.save(t);
    }

    private String add(String url, String title, String topic, Instant at) {
        resolver.process(NewsTestSupport.rule(rules), NewsTestSupport.article(++seq, url, title, List.of(topic), at));
        return store.article("gdelt-articles", ArticleUrls.externalId(url)).orElseThrow().storyId();
    }

    private String key(String url) {
        return "gdelt-articles/" + ArticleUrls.externalId(url);
    }

    @Test
    @DisplayName("token fallback: similar titles in one topic within 48 h join; other titles, topics or times do not")
    void tokenFallback() {
        String a = add("https://one.example/a", "Houthi missile strikes cargo ship in the Red Sea", "red-sea", T0);
        String b = add("https://two.example/b", "Houthi missile strikes cargo ship in Red Sea, crew evacuated",
                "red-sea", T0.plusSeconds(3_600));
        String c = add("https://three.example/c", "Central bank raises interest rates", "red-sea", T0.plusSeconds(7_200));
        String d = add("https://four.example/d", "Houthi missile strikes cargo ship in the Red Sea", "red-sea",
                T0.plusSeconds(4 * 86_400));
        String e = add("https://five.example/e", "Houthi missile strikes cargo ship in the Red Sea", "hormuz",
                T0.plusSeconds(3_600));
        assertThat(b).isEqualTo(a);
        assertThat(c).isNotEqualTo(a);
        assertThat(d).as("more than 48 h after the story's latest article").isNotEqualTo(a);
        assertThat(e).as("no shared topic or region").isNotEqualTo(a);
        NewsStoryStore.Story story = store.story(a).orElseThrow();
        assertThat(story.articleCount()).isEqualTo(2);
        assertThat(story.topicIds()).containsExactly("red-sea");
        assertThat(story.regionCodes()).containsExactly("YE");
        assertThat(store.article("gdelt-articles", ArticleUrls.externalId("https://two.example/b")).orElseThrow()
                .matchMethod()).isEqualTo("tokens");
        assertThat(evidence.event(a).orElseThrow().family()).isEqualTo("news-story");
    }

    @Test
    @DisplayName("embeddings: a cosine of at least 0.82 to the centroid joins even with different words")
    void embedding() {
        vectors.put(key("https://one.example/a"), new float[] {1f, 0f, 0f});
        vectors.put(key("https://two.example/b"), new float[] {0.95f, 0.2f, 0f});
        vectors.put(key("https://three.example/c"), new float[] {0f, 1f, 0f});
        String a = add("https://one.example/a", "Shipping attacked off Yemen", "red-sea", T0);
        String b = add("https://two.example/b", "Vessel hit near Hodeidah port", "red-sea", T0.plusSeconds(600));
        String c = add("https://three.example/c", "Shipping attacked off Yemen again", "red-sea", T0.plusSeconds(900));
        assertThat(b).isEqualTo(a);
        assertThat(c).as("cosine 0 to the centroid: the embedding decides, not the words").isNotEqualTo(a);
        NewsStoryStore.Story story = store.story(a).orElseThrow();
        assertThat(story.centroidModel()).isEqualTo("test-model");
        assertThat(story.centroidN()).isEqualTo(2);
        assertThat(store.article("gdelt-articles", ArticleUrls.externalId("https://two.example/b")).orElseThrow()
                .matchMethod()).isEqualTo("embedding");
    }

    @Test
    @DisplayName("30 reprints of a wire story are one evidence group with syndicated members")
    void wireReprints() {
        String title = "Iran seizes oil tanker in the Strait of Hormuz, US Navy says";
        String story = add("https://apnews.com/article/hormuz-1", title, "hormuz", T0);
        for (int i = 0; i < 30; i++) {
            assertThat(add("https://outlet" + i + ".example/news/hormuz-" + i, title, "hormuz",
                    T0.plusSeconds(60L * (i + 1)))).isEqualTo(story);
        }
        NewsStoryStore.Story s = store.story(story).orElseThrow();
        assertThat(s.articleCount()).isEqualTo(31);
        assertThat(s.outletCount()).isEqualTo(31);
        assertThat(s.evidenceGroupCount()).isEqualTo(1);
        assertThat(evidence.groups(story)).singleElement().satisfies(g -> {
            assertThat(g.originatorRoot()).isEqualTo("wire:ap");
            assertThat(g.memberCount()).isEqualTo(31);
        });
        List<NewsStoryStore.Article> articles = store.articles(story);
        assertThat(articles).filteredOn(NewsStoryStore.Article::syndicated).hasSize(30);
        assertThat(articles).filteredOn(a -> !a.syndicated()).singleElement()
                .extracting(NewsStoryStore.Article::domain).isEqualTo("apnews.com");
        assertThat(evidence.event(story).orElseThrow().qualityFlags()).contains("ECHO", "SINGLE_ORIGINATOR");
        assertThat(s.title()).isEqualTo(title);
        assertThat(s.coverage().get("evidenceGroups").toString()).contains("unrated=1");
    }

    @Test
    @DisplayName("exactly once: the same seq is skipped; a newer version refreshes metadata and keeps its story")
    void exactlyOnce() {
        String story = add("https://one.example/a", "Port of Aden closed after drone attack", "red-sea", T0);
        resolver.process(NewsTestSupport.rule(rules), NewsTestSupport.article(seq, "https://one.example/a",
                "Port of Aden closed after drone attack", List.of("red-sea"), T0));
        assertThat(evidence.links(story)).hasSize(1);
        resolver.process(NewsTestSupport.rule(rules), NewsTestSupport.article(++seq, "https://one.example/a",
                "Port of Aden closed after drone attack, officials say", List.of("red-sea"), T0));
        assertThat(evidence.links(story)).hasSize(1);
        assertThat(store.articles(story)).singleElement().extracting(NewsStoryStore.Article::title)
                .isEqualTo("Port of Aden closed after drone attack, officials say");
    }

    @Test
    @DisplayName("a story links to a situation and is corroborated by a sensor-based canonical event in its region")
    void situationAndCorroboration() {
        String story = add("https://one.example/a", "Earthquake shakes western Yemen", "red-sea", T0);
        job.corroborate(T0.plusSeconds(600));

        ObservationFacts facts = new ObservationFacts("test-sensor", "eq-1", 1, 1, T0.minusSeconds(1_800),
                T0.minusSeconds(1_700), 43.2, 14.8, 5.1, null, null, null, null, null, List.of(), null);
        String event = evidence.insertEvent("seismic", facts, List.of("YE"), T0);
        evidence.insertLink(event, facts, originators.originator("usgs-anss").orElseThrow(), 1.0,
                Map.of("rule", "first-observation"), T0);
        job.corroborate(T0.plusSeconds(600));
        NewsStoryStore.Story s = store.story(story).orElseThrow();
        assertThat(s.corroborated()).isTrue();
        assertThat(s.corroboratedBy()).contains(event);
        assertThat(s.coverage().get("corroborated")).isEqualTo(true);

        SituationEntity situation = situations.create(JSON.readTree("{\"title\":\"Yemen earthquake\",\"theme\":"
                + "\"maritime\",\"status\":\"active\",\"regionCodes\":[\"YE\"]}"), "tester", "operator", true);
        situations.join(situation.getId(), new LinkerInput(0, SituationMemberTypes.RECORD,
                "foresignal-intel/" + story, "foresignal-intel", story, 1, "news-story", "attention", "info",
                s.title(), "News story", T0, null, Set.of("YE"), null, Set.of(), s.title(), null, null, false,
                "context", List.of(new RecordRef("foresignal-intel", story, null)), null), Map.of("score", 0.9));
        job.situations(T0.plusSeconds(900));
        assertThat(store.story(story).orElseThrow().situationIds()).containsExactly(situation.getId());
        Map<String, Object> stats = coverage.situationCoverage(situation.getId());
        assertThat(stats).isNotNull();
        assertThat(stats.get("scope")).isEqualTo("situation");
        assertThat(stats.get("corroboratedShare")).isEqualTo(1.0);
        List<String> ids = new ArrayList<>(coverage.storiesOfSituation(situation.getId()));
        assertThat(ids).containsExactly(story);
    }
}
