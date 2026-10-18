package ai.foresignal.social.narrative;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A narrative (prompt 136, table {@code social_narrative}): posts that say the same thing, keyed by its 116 canonical
 * event id (family {@code social-narrative}), with its deterministic title, text centroid, scopes, platform statistics,
 * origin counts, velocity, status (from 137), state, coordination indicators and merge state. Declares the schema;
 * {@link NarrativeStore} reads and writes it with SQL. Narratives are intelligence objects: they are kept when their
 * posts expire.
 */
@Entity
@Table(name = "social_narrative", indexes = {
        @Index(name = "social_narrative_activity_idx", columnList = "last_activity_at"),
        @Index(name = "social_narrative_updated_idx", columnList = "updated_at"),
        @Index(name = "social_narrative_dirty_idx", columnList = "dirty")})
public class SocialNarrativeEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Column(name = "title_source", length = 16)
    private String titleSource;

    @Column(name = "central_post", length = 400)
    private String centralPost;

    @Column(name = "central_signature", columnDefinition = "bytea")
    private byte[] centralSignature;

    @Column(name = "centroid", columnDefinition = "text")
    private String centroid;

    @Column(name = "centroid_model", length = 200)
    private String centroidModel;

    @Column(name = "centroid_n", nullable = false)
    private int centroidN;

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

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "languages", columnDefinition = "text[]")
    private String[] languages;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "platforms")
    private List<Object> platforms;

    @Column(name = "first_observed_at", nullable = false)
    private Instant firstObservedAt;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt;

    @Column(name = "post_count", nullable = false)
    private int postCount;

    @Column(name = "unique_authors", nullable = false)
    private int uniqueAuthors;

    @Column(name = "evidence_group_count", nullable = false)
    private int evidenceGroupCount;

    @Column(name = "origin_concentration", nullable = false)
    private double originConcentration;

    @Column(name = "velocity")
    private Double velocity;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "media_fingerprints", columnDefinition = "text[]")
    private String[] mediaFingerprints;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "news_story_ids", columnDefinition = "text[]")
    private String[] newsStoryIds;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "situation_ids", columnDefinition = "text[]")
    private String[] situationIds;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "state", length = 16, nullable = false)
    private String state;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coordination")
    private Map<String, Object> coordination;

    @Column(name = "coordination_finding_id", length = 26)
    private String coordinationFindingId;

    @Column(name = "merged_into", length = 26)
    private String mergedInto;

    @Column(name = "tlp", length = 16, nullable = false)
    private String tlp;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "dirty", nullable = false)
    private boolean dirty;

    @Column(name = "llm_extracted_at")
    private Instant llmExtractedAt;

    @Column(name = "llm_post_count", nullable = false)
    private int llmPostCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected SocialNarrativeEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
