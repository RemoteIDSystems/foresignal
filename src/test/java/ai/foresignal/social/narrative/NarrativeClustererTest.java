package ai.foresignal.social.narrative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.evidence.RecordRef;
import ai.foresignal.news.TitleEmbeddings;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.social.NarrativeTestSupport;
import ai.foresignal.testsupport.social.NarrativeTestSupport.Spec;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * The {@code social-narrative} family end to end (prompt 136), the clusterer driven directly with post versions as the
 * evidence resolver hands them over (text embeddings stubbed away, so text joins use the MinHash fallback): repost,
 * quote, text, link and media joins; the 72 h window and the shared-scope rule; evidence groups by origin; merge and
 * split with history; a post collected by two sources joining once.
 */
@IntegrationTest
class NarrativeClustererTest {

    static final Instant T0 = Instant.now().truncatedTo(ChronoUnit.HOURS).minus(Duration.ofHours(30));

    @Autowired NarrativeClusterer clusterer;
    @Autowired NarrativeService service;
    @Autowired NarrativeStore store;
    @Autowired EvidenceStore evidence;
    @Autowired CanonicalRules rules;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean TitleEmbeddings embeddings;

    long seq = 10_000;

    @BeforeEach
    void setUp() {
        when(embeddings.vectors(any())).thenReturn(new TitleEmbeddings.TitleVectors(null, Map.of()));
    }

    private String add(Spec spec) {
        return add(NarrativeTestSupport.SOURCE, spec);
    }

    private String add(String source, Spec spec) {
        StoryFamilyHandlerObservation obs = new StoryFamilyHandlerObservation(source, spec);
        clusterer.process(NarrativeTestSupport.rule(rules), obs.value(++seq));
        return store.member(source, spec.platform() + ":" + spec.postId()).map(NarrativeStore.Member::narrativeId)
                .orElse(null);
    }

    /** A post version from another source. */
    record StoryFamilyHandlerObservation(String source, Spec spec) {
        ai.foresignal.evidence.StoryFamilyHandler.Observation value(long seq) {
            ai.foresignal.evidence.StoryFamilyHandler.Observation o = NarrativeTestSupport.post(seq, spec);
            return new ai.foresignal.evidence.StoryFamilyHandler.Observation(o.seq(), source, o.externalId(), o.kind(),
                    o.version(), o.eventTime(), o.observedAt(), o.payload());
        }
    }

    private static Spec text(String id, String author, String text, Instant at, String topic) {
        return Spec.text(id, author, text, at, List.of(topic));
    }

    @Test
    @DisplayName("reposts and quotes join the original's narrative")
    void repostAndQuote() {
        String n = add(text("a", "alice", "Explosion at the port of Aden, huge fire", T0, "red-sea"));
        assertThat(n).isNotNull();
        String r = add(new Spec("bluesky", "r", "bob", null, T0.plusSeconds(60), "a", null, List.of(), List.of(),
                List.of()));
        String q = add(new Spec("bluesky", "q", "carol", "Is this confirmed?", T0.plusSeconds(120), null, "a",
                List.of(), List.of(), List.of()));
        assertThat(r).isEqualTo(n);
        assertThat(q).isEqualTo(n);
        assertThat(store.member(NarrativeTestSupport.SOURCE, "bluesky:r").orElseThrow().linkReason())
                .isEqualTo("repost");
        assertThat(store.member(NarrativeTestSupport.SOURCE, "bluesky:q").orElseThrow().linkReason())
                .isEqualTo("quote");
        assertThat(evidence.event(n).orElseThrow().family()).isEqualTo(NarrativeVocabulary.FAMILY);
        NarrativeStore.Narrative narrative = store.narrative(n).orElseThrow();
        assertThat(narrative.postCount()).isEqualTo(3);
        assertThat(narrative.evidenceGroupCount()).as("repost and quote descend from the original").isEqualTo(1);
    }

    @Test
    @DisplayName("text joins need similar text, a shared scope and the 72 h window")
    void textJoin() {
        String a = add(text("t1", "alice", "Explosion at the port of Aden, huge fire, smoke everywhere", T0,
                "red-sea"));
        String b = add(text("t2", "bob", "Explosion at the port of Aden, huge fire and smoke everywhere",
                T0.plusSeconds(3_600), "red-sea"));
        String c = add(text("t3", "carol", "Central bank raises interest rates again", T0.plusSeconds(4_000),
                "red-sea"));
        String d = add(text("t4", "dave", "Explosion at the port of Aden, huge fire, smoke everywhere",
                T0.plusSeconds(4_200), "hormuz"));
        String e = add(text("t5", "erin", "Explosion at the port of Aden, huge fire, smoke everywhere",
                T0.plus(Duration.ofDays(4)), "red-sea"));
        assertThat(b).isEqualTo(a);
        assertThat(store.member(NarrativeTestSupport.SOURCE, "bluesky:t2").orElseThrow().linkReason())
                .isEqualTo("text");
        assertThat(c).isNotEqualTo(a);
        assertThat(d).as("no shared topic, place or entity").isNotEqualTo(a);
        assertThat(e).as("more than 72 h after the narrative's latest member").isNotEqualTo(a);
    }

    @Test
    @DisplayName("a shared link or a shared image joins posts with different words")
    void linkAndMedia() {
        String a = add(new Spec("bluesky", "l1", "alice", "Look at this", T0, null, null,
                List.of("https://news.example/story?utm_source=x"), List.of(), List.of()));
        String b = add(new Spec("x", "l2", "bob", "Unbelievable", T0.plusSeconds(600), null, null,
                List.of("https://news.example/story"), List.of(), List.of()));
        assertThat(b).isEqualTo(a);
        assertThat(store.member(NarrativeTestSupport.SOURCE, "x:l2").orElseThrow().linkReason()).isEqualTo("link");
        String c = add(new Spec("bluesky", "i1", "carol", "Photo from the scene", T0, null, null, List.of(),
                List.of("https://cdn.example/img/1.jpg"), List.of()));
        String d = add(new Spec("mastodon", "i2", "dave", "Wow", T0.plusSeconds(900), null, null, List.of(),
                List.of("https://cdn.example/img/1.jpg"), List.of()));
        assertThat(d).isEqualTo(c);
        assertThat(store.member(NarrativeTestSupport.SOURCE, "mastodon:i2").orElseThrow().linkReason())
                .isEqualTo("media");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM social_media_queue", Integer.class)).isEqualTo(2);
    }

    @Test
    @DisplayName("evidence groups count origins: three originals with their reposts are three groups")
    void groupsByOrigin() {
        String n = add(text("o1", "alice", "Explosion and fire at the port of Aden tonight", T0, "red-sea"));
        add(text("o2", "bob", "Explosion and fire at the port of Aden tonight, many injured", T0.plusSeconds(30),
                "red-sea"));
        add(text("o3", "carol", "Explosion and fire at the port of Aden tonight, sirens", T0.plusSeconds(60),
                "red-sea"));
        for (int i = 0; i < 30; i++) {
            add(new Spec("bluesky", "rp" + i, "fan" + i, null, T0.plusSeconds(100 + i), "o" + (1 + i % 3), null,
                    List.of(), List.of(), List.of()));
        }
        NarrativeStore.Narrative narrative = store.narrative(n).orElseThrow();
        assertThat(narrative.postCount()).isEqualTo(33);
        assertThat(narrative.evidenceGroupCount()).isEqualTo(3);
        assertThat(narrative.originConcentration()).isEqualTo(1.0);
        assertThat(evidence.groups(n)).hasSize(3);
        assertThat(store.claims(n)).extracting(NarrativeStore.Claim::type).contains("explosion", "fire");
        assertThat(narrative.title()).startsWith("Reported ");
        assertThat(narrative.platforms()).hasSize(1);
        Map<String, Object> graph = service.propagation(narrative);
        assertThat(graph.get("descentShare")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("merge and split follow 118 rules and keep history")
    void mergeAndSplit() {
        String a = add(text("m1", "alice", "Drone strike reported near the airport", T0, "red-sea"));
        add(new Spec("bluesky", "m1r", "bob", null, T0.plusSeconds(10), "m1", null, List.of(), List.of(),
                List.of()));
        String b = add(text("m2", "carol", "Totally separate story about a flood downtown", T0.plusSeconds(20),
                "hormuz"));
        assertThat(b).isNotEqualTo(a);
        service.merge(a, List.of(b), "token:test", "same event");
        assertThat(store.narrative(b).orElseThrow().mergedInto()).isEqualTo(a);
        assertThat(store.narrative(a).orElseThrow().postCount()).isEqualTo(3);
        assertThat(evidence.event(b).orElseThrow().status()).isEqualTo("merged");
        assertThat(store.history(a)).extracting(h -> h.get("action")).contains("created", "merged");
        assertThat(store.history(b)).extracting(h -> h.get("action")).contains("merged-into");
        assertThatThrownBy(() -> service.merge(a, List.of(b), "token:test", null))
                .isInstanceOf(ValidationException.class);
        String split = service.split(a, List.of(RecordRef.format(NarrativeTestSupport.SOURCE, "bluesky:m1")),
                "token:test", "different event");
        assertThat(store.member(NarrativeTestSupport.SOURCE, "bluesky:m1r").orElseThrow().narrativeId())
                .as("the repost moves with its original").isEqualTo(split);
        assertThat(store.narrative(split).orElseThrow().postCount()).isEqualTo(2);
        assertThat(store.narrative(a).orElseThrow().postCount()).isEqualTo(1);
        assertThat(store.history(split)).extracting(h -> h.get("action")).contains("split-from");
        assertThat(store.history(a)).extracting(h -> h.get("action")).contains("split");
        assertThatThrownBy(() -> service.split(a, List.of("record:bluesky-jetstream/bluesky%3Anope"), "t", null))
                .isInstanceOf(ValidationException.class);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM social_event_log WHERE event LIKE 'narrative.%'",
                Integer.class)).isGreaterThan(0);
    }

    @Test
    @DisplayName("a post collected by two sources joins once")
    void twoSources() {
        String a = add(text("dup", "alice", "Explosion at the port of Aden", T0, "red-sea"));
        String again = add("bluesky-search", text("dup", "alice", "Explosion at the port of Aden", T0, "red-sea"));
        assertThat(a).isNotNull();
        assertThat(again).isNull();
        assertThat(store.narrative(a).orElseThrow().postCount()).isEqualTo(1);
    }
}
