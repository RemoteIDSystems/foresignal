package ai.foresignal.social.narrative;

import static ai.foresignal.testsupport.social.NarrativeTestSupport.member;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The propagation graph (prompt 136): cross-platform edges by URL, media and near-identical text; repost edges and the
 * median repost delay; earliest observed post per platform and the cross-platform order; the first news pickup; the
 * descent share; summarization to 500 nodes.
 */
class PropagationTracerTest {

    static final Instant T0 = Instant.parse("2026-10-17T08:00:00Z");

    @Test
    @DisplayName("cross-platform edges by shared URL, shared fingerprint and near-identical text")
    void crossPlatform() {
        List<NarrativeStore.Member> members = List.of(
                member("b1", "bluesky", "a", "Explosion at the port, video below", T0, null,
                        List.of("https://news.example/story"), List.of(), "g1"),
                member("x1", "x", "b", "Totally different words here", T0.plusSeconds(600), null,
                        List.of("https://news.example/story"), List.of(), "g2"),
                member("r1", "reddit", "c", "Another unrelated text body", T0.plusSeconds(900), null, List.of(),
                        List.of("FP1"), "g3"),
                member("b2", "bluesky", "d", "Photo of the scene", T0.plusSeconds(300), null, List.of(),
                        List.of("FP1"), "g4"),
                member("m1", "mastodon", "e", "Explosion at the port, video below", T0.plusSeconds(1200), null,
                        List.of(), List.of(), "g5"));
        List<NarrativeStore.Edge> edges = PropagationTracer.edges(members, List.of(), List.of());
        assertThat(edges).anyMatch(e -> e.type().equals("cross-platform") && e.from().equals("post:b1")
                && e.to().equals("post:x1"));
        assertThat(edges).anyMatch(e -> e.type().equals("cross-platform") && e.from().equals("post:b2")
                && e.to().equals("post:r1"));
        assertThat(edges).anyMatch(e -> e.type().equals("cross-platform") && e.from().equals("post:b1")
                && e.to().equals("post:m1"));
        assertThat(edges).anyMatch(e -> e.type().equals("media-match") && e.from().equals("post:b2")
                && e.to().equals("post:r1") && e.deltaSeconds() == 600);
    }

    @Test
    @DisplayName("earliest observed per platform, platform order, news pickup and repost delay")
    void outputs() {
        List<NarrativeStore.Member> members = new ArrayList<>();
        members.add(member("x1", "x", "a", "Smoke over the refinery", T0.plusSeconds(120), null, List.of(), List.of(),
                "g1"));
        members.add(member("b1", "bluesky", "b", "Smoke over the refinery now", T0, null,
                List.of("https://news.example/refinery"), List.of(), "g2"));
        members.add(member("b2", "bluesky", "c", null, T0.plusSeconds(60), "bluesky:b1", List.of(), List.of(), "g2"));
        members.add(member("b3", "bluesky", "d", null, T0.plusSeconds(180), "bluesky:b1", List.of(), List.of(), "g2"));
        List<PropagationTracer.Article> articles = List.of(new PropagationTracer.Article("gdelt/a1",
                "record:gdelt/a1", "S1", "https://news.example/refinery", T0.minusSeconds(300), "Refinery fire"));
        List<NarrativeStore.Edge> edges = PropagationTracer.edges(members, articles, List.of());
        PropagationTracer.Graph g = PropagationTracer.graph("N", members, edges, articles, List.of(), 500);
        assertThat(g.earliestByPlatform()).extracting(m -> m.get("platform")).containsExactly("bluesky", "x");
        assertThat(g.earliestByPlatform().get(0).get("postRef")).isEqualTo(members.get(1).recordRef());
        assertThat(g.crossPlatformOrder()).extracting(m -> m.get("platform")).containsExactly("bluesky", "x");
        assertThat(g.firstNewsPickup().get("storyId")).isEqualTo("S1");
        assertThat((Double) g.firstNewsPickup().get("afterFirstPostSeconds")).isEqualTo(-300.0);
        assertThat(g.medianRepostDelaySeconds()).isEqualTo(120.0);
        assertThat(edges).anyMatch(e -> e.type().equals("link") && e.from().equals("article:gdelt/a1"));
        assertThat(g.summarized()).isFalse();
        assertThat(g.toMap()).containsKeys("summarised", "descentShare", "nodes", "edges");
    }

    @Test
    @DisplayName("a 10,000-repost cascade from three posts: three evidence groups, descent share 1.0")
    void cascade() {
        List<NarrativeGrouper.Member> grouperMembers = new ArrayList<>();
        List<NarrativeStore.Member> members = new ArrayList<>();
        for (int o = 0; o < 3; o++) {
            grouperMembers.add(new NarrativeGrouper.Member("o" + o, "bluesky:o" + o, null, "social:bluesky:root" + o,
                    null, null, List.of(), List.of(), T0.plusSeconds(o), o));
        }
        for (int i = 0; i < 10_000; i++) {
            int o = i % 3;
            grouperMembers.add(new NarrativeGrouper.Member("r" + i, "bluesky:r" + i, null, "social:bluesky:fan" + i,
                    "bluesky:o" + o, null, List.of(), List.of(), T0.plusSeconds(10 + i), 10 + i));
        }
        List<NarrativeGrouper.Group> groups = NarrativeGrouper.group(grouperMembers, Map.of());
        assertThat(groups).hasSize(3);
        assertThat(NarrativeGrouper.originConcentration(groups)).isEqualTo(1.0);
        for (NarrativeGrouper.Group g : groups) {
            for (String key : g.members()) {
                String post = key.startsWith("o") ? null : "bluesky:o" + (Integer.parseInt(key.substring(1)) % 3);
                members.add(member(key, "bluesky", key, null, T0, post, List.of(), List.of(), g.root()));
            }
        }
        List<NarrativeStore.Edge> edges = PropagationTracer.edges(members, List.of(), List.of());
        assertThat(PropagationTracer.descentShare(members, edges, 0)).isEqualTo(1.0);
        PropagationTracer.Graph graph = PropagationTracer.graph("N", members, edges, List.of(), List.of(), 500);
        assertThat(graph.summarized()).isTrue();
        assertThat(graph.nodes()).hasSize(3);
        assertThat(graph.totalNodes()).isEqualTo(10_003);
        assertThat(graph.descentShare()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("summarization keeps at most 500 nodes by collapsing evidence groups, edges merged with counts")
    void summarize() {
        List<NarrativeStore.Member> members = new ArrayList<>();
        for (int i = 0; i < 1_200; i++) {
            members.add(member("p" + i, "bluesky", "a" + i, "independent text number " + i, T0.plusSeconds(i), null,
                    List.of(), List.of(), "g" + i));
        }
        PropagationTracer.Graph graph = PropagationTracer.graph("N", members, List.of(), List.of(), List.of(), 500);
        assertThat(graph.summarized()).isTrue();
        assertThat(graph.nodes()).hasSize(500);
        assertThat(graph.totalNodes()).isEqualTo(1_200);
        assertThat(graph.descentShare()).isEqualTo(Math.round(3 / 1200.0 * 10_000) / 10_000.0);
    }
}
