package ai.foresignal.attention;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An operator-defined attention topic (prompt 117, table {@code attention_topic}): what drives the
 * {@code gdelt-timelines} and {@code wikimedia-pageviews} sources, and the keywords prompt 118's linker uses.
 *
 * <p>The model is additive: prompt 135 adds a {@code social} block (terms per language, hashtags, accounts,
 * channels, platforms, places, entities) as a further column without changing these.
 */
@Entity
@Table(name = "attention_topic")
public class AttentionTopicEntity {

    @Id
    @Column(name = "id", length = 48, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "gdelt_query", length = 250)
    private String gdeltQuery;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "wiki_articles")
    private List<Map<String, String>> wikiArticles = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keywords")
    private List<String> keywords = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "region_codes")
    private List<String> regionCodes = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "themes")
    private List<String> themes = new ArrayList<>();

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected AttentionTopicEntity() {
        // for JPA
    }

    /**
     * A new topic.
     *
     * @param id the slug id
     * @param createdAt the creation time (also the first update time)
     */
    public AttentionTopicEntity(String id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /** The slug id ({@code ^[a-z0-9-]{3,48}$}). */
    public String getId() {
        return id;
    }

    /** The display name. */
    public String getName() {
        return name;
    }

    /** Sets the display name. */
    public void setName(String name) {
        this.name = name;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** The GDELT DOC 2.0 query, or null. */
    public String getGdeltQuery() {
        return gdeltQuery;
    }

    /** Sets the GDELT query. */
    public void setGdeltQuery(String gdeltQuery) {
        this.gdeltQuery = gdeltQuery;
    }

    /** The Wikipedia articles: {@code {project, article}} maps. */
    public List<Map<String, String>> getWikiArticles() {
        return wikiArticles == null ? List.of() : wikiArticles;
    }

    /** Sets the Wikipedia articles. */
    public void setWikiArticles(List<Map<String, String>> wikiArticles) {
        this.wikiArticles = wikiArticles == null ? new ArrayList<>() : new ArrayList<>(wikiArticles);
    }

    /** The keywords prompt 118's linker uses. */
    public List<String> getKeywords() {
        return keywords == null ? List.of() : keywords;
    }

    /** Sets the keywords. */
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords == null ? new ArrayList<>() : new ArrayList<>(keywords);
    }

    /** The 040 region codes. */
    public List<String> getRegionCodes() {
        return regionCodes == null ? List.of() : regionCodes;
    }

    /** Sets the 040 region codes. */
    public void setRegionCodes(List<String> regionCodes) {
        this.regionCodes = regionCodes == null ? new ArrayList<>() : new ArrayList<>(regionCodes);
    }

    /** The themes. */
    public List<String> getThemes() {
        return themes == null ? List.of() : themes;
    }

    /** Sets the themes. */
    public void setThemes(List<String> themes) {
        this.themes = themes == null ? new ArrayList<>() : new ArrayList<>(themes);
    }

    /** True when the topic drives the attention sources. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets whether the topic drives the attention sources. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** The token id that created it, or null (a shipped example). */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets the creating token id. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it was last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when it was last changed. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
