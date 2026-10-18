package ai.foresignal.news;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One news article as the story resolver placed it (prompt 133, table {@code news_article}): the article record's
 * metadata, its story, outlet, evidence group and {@code syndicated} flag. {@code id} is {@code {sourceId}/{externalId}}.
 * Metadata only: no body, byline or author name (NEWS-2). Rows follow the articles' {@code bulk-event} retention
 * (90 days, {@link NewsCoverageJob}); the story aggregates stay.
 */
@Entity
@Table(name = "news_article", indexes = {
        @Index(name = "news_article_story_idx", columnList = "story_id"),
        @Index(name = "news_article_outlet_idx", columnList = "outlet_id"),
        @Index(name = "news_article_seen_idx", columnList = "seen_at"),
        @Index(name = "news_article_record_idx", columnList = "source_id, external_id")})
public class NewsArticleEntity {

    @Id
    @Column(name = "id", length = 400, nullable = false)
    private String id;

    @Column(name = "source_id", length = 128, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    @Column(name = "seq", nullable = false)
    private long seq;

    @Column(name = "story_id", length = 26)
    private String storyId;

    @Column(name = "link_id", length = 26)
    private String linkId;

    @Column(name = "evidence_group_key", length = 400)
    private String evidenceGroupKey;

    @Column(name = "evidence_group_id", length = 26)
    private String evidenceGroupId;

    @Column(name = "outlet_id", length = 26)
    private String outletId;

    @Column(name = "domain", length = 253, nullable = false)
    private String domain;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "title_tokens", columnDefinition = "text[]")
    private String[] titleTokens = new String[0];

    @Column(name = "url", length = 4000, nullable = false)
    private String url;

    @Column(name = "language", length = 8)
    private String language;

    @Column(name = "source_country", length = 2)
    private String sourceCountry;

    @Column(name = "seen_at", nullable = false)
    private Instant seenAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "topic_ids", columnDefinition = "text[]")
    private String[] topicIds = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "image_url", length = 4000)
    private String imageUrl;

    @Column(name = "syndicated", nullable = false)
    private boolean syndicated;

    @Column(name = "wire_originator", length = 120)
    private String wireOriginator;

    @Column(name = "similarity")
    private Double similarity;

    @Column(name = "match_method", length = 16)
    private String matchMethod;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected NewsArticleEntity() {}

    /** The id ({@code {sourceId}/{externalId}}). */
    public String getId() {
        return id;
    }
}
