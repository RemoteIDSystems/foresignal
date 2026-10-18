package ai.foresignal.social;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.evidence.EvidenceGrouper;
import ai.foresignal.evidence.Originator;
import ai.foresignal.evidence.OriginatorRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Social originators (prompt 135, 116 evidence groups, SOC-1): a post's originator is its account
 * ({@code social:{platform}:{authorKey}}); reposts and quotes inherit the original post's originator, so they join the
 * original's evidence group and amplification never adds an independent observation.
 */
class SocialOriginatorTest {

    private static Map<String, Object> post(String postId, String authorKey, String repostOf, String quoteOf) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("platform", "bluesky");
        p.put("postId", postId);
        p.put("authorKey", authorKey);
        if (repostOf != null) {
            p.put("repostOf", Map.of("platform", "bluesky", "postId", repostOf));
        }
        if (quoteOf != null) {
            p.put("quoteOf", Map.of("platform", "bluesky", "postId", quoteOf));
        }
        return p;
    }

    private static OriginatorRegistry registry() {
        return OriginatorRegistry.of(Map.of("originators", List.of(), "sources", Map.of()),
                JsonMapper.builder().build());
    }

    @Test
    @DisplayName("a repost and a quote join the original's group; an independent post is its own group")
    void amplificationJoinsTheOriginal() {
        Map<String, Object> original = post("p1", "ka", null, null);
        Map<String, Map<String, Object>> stored = Map.of("bluesky:p1", original);
        List<EvidenceGrouper.Member> members = List.of(
                SocialOriginators.member("original", "bluesky-jetstream", original, 1, stored::get),
                SocialOriginators.member("repost", "bluesky-jetstream", post("p2", "kb", "p1", null), 2, stored::get),
                SocialOriginators.member("quote", "bluesky-search", post("p3", "kc", null, "p1"), 3, stored::get),
                SocialOriginators.member("other", "bluesky-jetstream", post("p4", "kd", null, null), 4, stored::get));
        List<EvidenceGrouper.Group> groups = EvidenceGrouper.group(members, registry());
        assertThat(groups).hasSize(2);
        assertThat(groups.get(0).members()).containsExactly("original", "repost", "quote");
        assertThat(groups.get(0).root()).isEqualTo("social:bluesky:ka");
        assertThat(groups.get(1).members()).containsExactly("other");
    }

    @Test
    @DisplayName("amplifications of an original that is not stored still share one originator; chains are followed")
    void unseenOriginalAndChains() {
        assertThat(SocialOriginators.originatorId(post("r1", "kb", "missing", null), id -> null))
                .isEqualTo("social:bluesky:post:missing");
        assertThat(SocialOriginators.originatorId(post("r2", "kc", null, "missing"), id -> null))
                .isEqualTo("social:bluesky:post:missing");
        Map<String, Map<String, Object>> stored = Map.of("bluesky:p1", post("p1", "ka", null, null),
                "bluesky:q1", post("q1", "kq", null, "p1"));
        assertThat(SocialOriginators.originatorId(post("r3", "kr", "q1", null), stored::get))
                .as("a repost of a quote traces to the quoted original").isEqualTo("social:bluesky:ka");
        Map<String, Map<String, Object>> loop = Map.of("bluesky:a", post("a", "k1", "b", null),
                "bluesky:b", post("b", "k2", "a", null));
        assertThat(SocialOriginators.originatorId(loop.get("bluesky:a"), loop::get)).startsWith("social:bluesky:");
    }

    @Test
    @DisplayName("the registry answers social originators with class social and their own independence group")
    void registryAnswersSocialIds() {
        Originator o = registry().originator("social:x:abc").orElseThrow();
        assertThat(o.originatorClass()).isEqualTo("social");
        assertThat(o.root()).isEqualTo("social:x:abc");
        assertThat(registry().originator("social:")).isEmpty();
        assertThat(SocialOriginators.of("social:x:abc").originatorClass()).isEqualTo("social");
    }
}
