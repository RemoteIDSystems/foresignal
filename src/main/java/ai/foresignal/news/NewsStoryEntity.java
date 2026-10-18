package ai.foresignal.news;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A news story (prompt 133, table {@code news_story}): a 116 canonical event of family {@code news-story}, keyed by the
 * canonical event id, with its deterministic title, title-embedding centroid, topics, regions and coverage statistics.
 * Declares the schema; {@link NewsStoryStore} reads and writes it with SQL. Story aggregates are kept when their
 * articles expire (an intelligence table, never deleted by retention).
 */
@Entity
@Table(name = "news_story", indexes = {
        @Index(name = "news_story_last_seen_idx", columnList = "last_seen_at"),
        @Index(name = "news_story_updated_idx", columnList = "updated_at")})
public class NewsStoryEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "title_tokens", columnDefinition = "text[]")
    private String[] titleTokens = new String[0];

    @Column(name = "central_article", length = 400)
    private String centralArticle;

    @Column(name = "centroid", columnDefinition = "text")
    private String centroid;

    @Column(name = "centroid_model", length = 200)
    private String centroidModel;

    @Column(name = "centroid_n", nullable = false)
    private int centroidN;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @Column(name = "article_count", nullable = false)
    private int articleCount;

    @Column(name = "outlet_count", nullable = false)
    private int outletCount;

    @Column(name = "evidence_group_count", nullable = false)
    private int evidenceGroupCount;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "topic_ids", columnDefinition = "text[]")
    private String[] topicIds = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coverage")
    private Map<String, Object> coverage = new LinkedHashMap<>();

    @Column(name = "blindspot", length = 8)
    private String blindspot;

    @Column(name = "skewed", nullable = false)
    private boolean skewed;

    @Column(name = "n_rated", nullable = false)
    private int nRated;

    @Column(name = "state_affiliated_share", nullable = false)
    private double stateAffiliatedShare;

    @Column(name = "corroborated", nullable = false)
    private boolean corroborated;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "corroborated_by", columnDefinition = "text[]")
    private String[] corroboratedBy = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "situation_ids", columnDefinition = "text[]")
    private String[] situationIds = new String[0];

    @Column(name = "blindspot_finding_id", length = 26)
    private String blindspotFindingId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected NewsStoryEntity() {}

    /** The id (the canonical event id). */
    public String getId() {
        return id;
    }
}
