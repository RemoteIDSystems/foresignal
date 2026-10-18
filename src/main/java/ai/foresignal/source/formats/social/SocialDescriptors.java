package ai.foresignal.source.formats.social;

import ai.foresignal.social.SocialPosts;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;

/**
 * Descriptor defaults every social platform adapter shares (prompt 135): the {@code social-post} kind, STANDARD
 * history, the ALL ingest policy, no TTL, creation time as {@code observedAt} (UPSTREAM), the {@code social} analytic
 * class's 30-day retention ({@code social.postRetention}, applied per platform as the source's retention override),
 * volatile {@code observedAt} and {@code engagement} (a changed like count is not a new version; a changed text is),
 * text search, open egress, zero results normal and the originator of its network (116).
 */
public final class SocialDescriptors {

    private SocialDescriptors() {}

    /**
     * Applies the defaults.
     *
     * @param builder the descriptor builder
     * @param network the network (also the originator id)
     * @return the builder
     */
    public static SourceDescriptor.Builder posts(SourceDescriptor.Builder builder, String network) {
        return posts(builder, network, true);
    }

    /**
     * Applies the defaults, saying whether engagement is volatile: false for a platform whose statistics refresh is
     * its complete path (YouTube's tracked videos), so a changed count is a new version.
     *
     * @param builder the descriptor builder
     * @param network the network (also the originator id)
     * @param engagementVolatile whether {@code engagement} is left out of version hashing
     * @return the builder
     */
    public static SourceDescriptor.Builder posts(SourceDescriptor.Builder builder, String network,
            boolean engagementVolatile) {
        builder.volatileFields(engagementVolatile ? SocialPosts.VOLATILE_FIELDS.toArray(new String[0])
                : new String[] {"observedAt"});
        return builder.kind(SocialVocabulary.KIND)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultRetention(SocialVocabulary.DEFAULT_POST_RETENTION)
                .kindRetention(SocialVocabulary.KIND, SocialVocabulary.DEFAULT_POST_RETENTION)
                .searchFields(SocialPosts.SEARCH_FIELDS.toArray(new String[0]))
                .embedFields("text")
                .titleField("text")
                .egress(Egress.OPEN)
                .zeroResultsNormal(true)
                .originator(network)
                .kindDisplay(SocialVocabulary.KIND, new KindDisplay("{platform}: {text}", null, "#7048E8", null));
    }
}
