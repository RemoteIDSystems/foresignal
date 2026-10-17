package ai.foresignal.situation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A cached LLM theme classification of one text (prompt 118 §3), keyed by the SHA-256 of the normalized text so a
 * text is classified at most once. Only written when {@code intel.themes.llmClassify} is on.
 */
@Entity
@Table(name = "situation_theme_cache")
public class ThemeClassificationEntity {

    @Id
    @Column(name = "text_hash", length = 64, nullable = false)
    private String textHash;

    @Column(name = "theme", length = 32, nullable = false)
    private String theme;

    @Column(name = "model", length = 200)
    private String model;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** JPA. */
    protected ThemeClassificationEntity() {}

    /** Creates a cached classification. */
    public ThemeClassificationEntity(String textHash, String theme, String model, Instant createdAt) {
        this.textHash = textHash;
        this.theme = theme;
        this.model = model;
        this.createdAt = createdAt;
    }

    /** The SHA-256 of the normalized text. */
    public String getTextHash() {
        return textHash;
    }

    /** The theme the model chose. */
    public String getTheme() {
        return theme;
    }

    /** The model, or null. */
    public String getModel() {
        return model;
    }

    /** When it was classified. */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
