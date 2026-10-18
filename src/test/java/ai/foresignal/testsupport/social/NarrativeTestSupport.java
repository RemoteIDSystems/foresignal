package ai.foresignal.testsupport.social;

import ai.foresignal.chronicle.network.MinHashSketch;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.StoryFamilyHandler;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialPosts;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.narrative.ClaimExtractor;
import ai.foresignal.social.narrative.NarrativeStore;
import ai.foresignal.social.narrative.PlaceIndex;
import ai.foresignal.social.narrative.TextSignatures;
import ai.foresignal.social.TextFolding;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helpers of the prompt 136 narrative tests: post versions as the narrative clusterer receives them (no ingest needed),
 * the {@code social-narrative} rule, member rows for the pure tests, and a place index over a fixed name list.
 */
public final class NarrativeTestSupport {

    /** The collecting source of test posts. */
    public static final String SOURCE = "bluesky-jetstream";

    private NarrativeTestSupport() {}

    /** The {@code social-narrative} family rule of the bundled rules. */
    public static CanonicalRules.FamilyRule rule(CanonicalRules rules) {
        return rules.family("social-narrative");
    }

    /**
     * A post spec.
     *
     * @param platform the network
     * @param postId the post id
     * @param authorKey the author key
     * @param text the text, or null
     * @param at the creation time
     * @param repostOf the reposted post id (same platform), or null
     * @param quoteOf the quoted post id (same platform), or null
     * @param links links
     * @param imageUrls image URLs
     * @param topics attention topics
     */
    public record Spec(String platform, String postId, String authorKey, String text, Instant at, String repostOf,
            String quoteOf, List<String> links, List<String> imageUrls, List<String> topics) {

        /** A plain text post on Bluesky. */
        public static Spec text(String postId, String author, String text, Instant at, List<String> topics) {
            return new Spec("bluesky", postId, author, text, at, null, null, List.of(), List.of(), topics);
        }
    }

    /** The version of a post spec. */
    public static StoryFamilyHandler.Observation post(long seq, Spec s) {
        List<SocialPost.Media> media = new ArrayList<>();
        for (String url : s.imageUrls()) {
            media.add(new SocialPost.Media("image", url, null, null));
        }
        SocialPost post = new SocialPost(s.platform(), s.postId(), "https://example.test/" + s.postId(),
                new SocialPost.Author(s.authorKey(), null, s.authorKey(), null, null, null), s.text(), "en", s.at(),
                null, s.quoteOf() == null ? null : new SocialPost.PostRef(s.platform(), s.quoteOf(), null),
                s.repostOf() == null ? null : new SocialPost.PostRef(s.platform(), s.repostOf(), null), s.links(),
                List.of(), List.of(), media, null, null, null, SocialVocabulary.COLLECTION_STREAM, null);
        Map<String, Object> payload = SocialPosts.payload(post, s.authorKey(), s.topics(), s.at());
        return new StoryFamilyHandler.Observation(seq, SOURCE, post.externalId(), SocialVocabulary.KIND, 1, s.at(),
                s.at(), payload);
    }

    /**
     * A member row for the pure tests.
     *
     * @param id the member id
     * @param platform the network
     * @param author the author key
     * @param text the text (signed)
     * @param at the creation time
     * @param repostOf the reposted post key, or null
     * @param links links
     * @param fingerprints fingerprints
     * @param groupKey the evidence-group key
     */
    public static NarrativeStore.Member member(String id, String platform, String author, String text, Instant at,
            String repostOf, List<String> links, List<String> fingerprints, String groupKey) {
        MinHashSketch sig = text == null ? null : TextSignatures.signature(text);
        return new NarrativeStore.Member(id, "N", SOURCE, platform + ":" + id, 1, null, platform, platform + ":" + id,
                null, author, "social:" + platform + ":" + author, "account:" + platform + ":" + author, null, sig,
                "en", at, "text", null, null, repostOf, null, null, links, List.of(), fingerprints, List.of(), List.of(),
                List.of(), List.of(), List.of(), groupKey, groupKey, null, at);
    }

    /**
     * A place index over fixed names: every occurrence of a name on word boundaries.
     *
     * @param names folded-or-not name to its candidates
     */
    public static PlaceIndex places(Map<String, List<PlaceIndex.Candidate>> names) {
        return folded -> {
            List<PlaceIndex.NameHit> out = new ArrayList<>();
            for (Map.Entry<String, List<PlaceIndex.Candidate>> e : names.entrySet()) {
                String name = TextFolding.fold(e.getKey());
                int at = folded.indexOf(name);
                while (at >= 0) {
                    if (TextFolding.onBoundaries(folded, name, at, at + name.length())) {
                        out.add(new PlaceIndex.NameHit(at, at + name.length(), e.getValue()));
                    }
                    at = folded.indexOf(name, at + 1);
                }
            }
            return out;
        };
    }

    /** A gazetteer locality candidate. */
    public static PlaceIndex.Candidate locality(String name, double lon, double lat, long population, String region) {
        return new PlaceIndex.Candidate(null, name, lon, lat, "locality", population, region);
    }

    /** Extracts with the bundled lexicon and the given places. */
    public static ClaimExtractor.Extraction extract(String text, Instant at, PlaceIndex places) {
        return ClaimExtractor.extract(text, at, ai.foresignal.social.narrative.ClaimLexicon.bundled(), places,
                null);
    }
}
