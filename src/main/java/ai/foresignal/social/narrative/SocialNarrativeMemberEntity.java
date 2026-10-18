package ai.foresignal.social.narrative;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A narrative's member post (prompt 136, table {@code social_narrative_member}), keyed by {@code {sourceId}/{externalId}}:
 * why it joined, its evidence group, its text signature, links, media, scopes and extracted claims. The post's text is
 * not copied (it expires with the post); the signature, scopes and claims stay. Declares the schema; {@link NarrativeStore}
 * reads and writes it.
 */
@Entity
@Table(name = "social_narrative_member", indexes = {
        @Index(name = "social_narrative_member_narrative_idx", columnList = "narrative_id, created_at"),
        @Index(name = "social_narrative_member_post_idx", columnList = "post_key")})
public class SocialNarrativeMemberEntity {

    @Id
    @Column(name = "id", length = 400, nullable = false)
    private String id;

    @Column(name = "narrative_id", length = 26, nullable = false)
    private String narrativeId;

    @Column(name = "source_id", length = 128, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    @Column(name = "seq", nullable = false)
    private long seq;

    @Column(name = "link_id", length = 26)
    private String linkId;

    @Column(name = "platform", length = 32, nullable = false)
    private String platform;

    @Column(name = "post_key", length = 256, nullable = false)
    private String postKey;

    @Column(name = "url", length = 2000)
    private String url;

    @Column(name = "author_key", length = 64)
    private String authorKey;

    @Column(name = "originator_id", length = 200)
    private String originatorId;

    @Column(name = "source_key", length = 300)
    private String sourceKey;

    @Column(name = "account_created_at")
    private Instant accountCreatedAt;

    @Column(name = "signature", columnDefinition = "bytea")
    private byte[] signature;

    @Column(name = "language", length = 16)
    private String language;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "link_reason", length = 16, nullable = false)
    private String linkReason;

    @Column(name = "matched_key", length = 400)
    private String matchedKey;

    @Column(name = "similarity")
    private Double similarity;

    @Column(name = "repost_of", length = 256)
    private String repostOf;

    @Column(name = "quote_of", length = 256)
    private String quoteOf;

    @Column(name = "reply_to", length = 256)
    private String replyTo;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "links", columnDefinition = "text[]")
    private String[] links;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "media_urls", columnDefinition = "text[]")
    private String[] mediaUrls;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "fingerprints", columnDefinition = "text[]")
    private String[] fingerprints;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "topic_ids", columnDefinition = "text[]")
    private String[] topicIds;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "place_refs", columnDefinition = "text[]")
    private String[] placeRefs;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "entity_refs", columnDefinition = "text[]")
    private String[] entityRefs;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "claims")
    private List<Object> claims;

    @Column(name = "evidence_group_key", length = 400)
    private String evidenceGroupKey;

    @Column(name = "evidence_group_id", length = 26)
    private String evidenceGroupId;

    @Column(name = "centrality")
    private Double centrality;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    /** For JPA. */
    protected SocialNarrativeMemberEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
