package ai.foresignal.social;

import ai.foresignal.evidence.EvidenceGrouper;
import ai.foresignal.evidence.Originator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Originators of social posts (prompt 135, 116 evidence identity): a post's originator is
 * {@code social:{platform}:{authorKey}}, class {@code social}, its own independence group (accounts are reporting
 * sources, judged like outlets). <strong>Reposts and quotes inherit the original post's originator</strong>, so an
 * amplification joins the original's evidence group and never adds an independent observation (SOC-1). When the
 * original is not stored, the amplifications still share one originator keyed by the original post
 * ({@code social:{platform}:post:{postId}}).
 */
public final class SocialOriginators {

    /** How many repost or quote hops are followed to the original. */
    static final int MAX_DEPTH = 8;

    private SocialOriginators() {}

    /** The originator id of an account. */
    public static String id(String platform, String authorKey) {
        return SocialVocabulary.ORIGINATOR_PREFIX + platform + ":" + authorKey;
    }

    /** The originator id standing for an original that is not stored. */
    static String postId(SocialPost.PostRef ref) {
        return SocialVocabulary.ORIGINATOR_PREFIX + ref.platform() + ":post:" + ref.postId();
    }

    /** The originator with an id (class {@code social}, independence group = the id). */
    public static Originator of(String id) {
        return new Originator(id, SocialVocabulary.ORIGINATOR_CLASS, id, List.of(),
                "social account or post (prompt 135); reposts and quotes inherit the original's originator", List.of());
    }

    /**
     * The originator id of a stored post payload, following reposts and quotes to the original.
     *
     * @param payload the post payload
     * @param lookup external id ({@code {platform}:{postId}}) to the stored payload of that post, or null
     * @return the originator id
     */
    public static String originatorId(Map<String, Object> payload, Function<String, Map<String, Object>> lookup) {
        Map<String, Object> current = payload;
        Set<String> seen = new HashSet<>();
        for (int depth = 0; depth < MAX_DEPTH && current != null; depth++) {
            SocialPost.PostRef original = SocialPosts.refOf(current, "repostOf");
            if (original == null) {
                original = SocialPosts.refOf(current, "quoteOf");
            }
            if (original == null) {
                return id(String.valueOf(current.get("platform")), SocialPosts.authorKeyOf(current));
            }
            if (!seen.add(original.externalId())) {
                return postId(original);
            }
            Map<String, Object> next = lookup == null ? null : lookup.apply(original.externalId());
            if (next == null) {
                return postId(original);
            }
            current = next;
        }
        return current == null ? id(String.valueOf(payload.get("platform")), SocialPosts.authorKeyOf(payload))
                : id(String.valueOf(current.get("platform")), SocialPosts.authorKeyOf(current));
    }

    /**
     * The evidence-group member of a stored post.
     *
     * @param key the member key
     * @param sourceId the collecting source
     * @param payload the post payload
     * @param seq the version's seq
     * @param lookup external id to the stored payload of that post, or null
     */
    public static EvidenceGrouper.Member member(String key, String sourceId, Map<String, Object> payload, long seq,
            Function<String, Map<String, Object>> lookup) {
        return new EvidenceGrouper.Member(key, sourceId, of(originatorId(payload, lookup)), null, seq);
    }
}
