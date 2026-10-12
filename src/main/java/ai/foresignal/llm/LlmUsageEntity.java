package ai.foresignal.llm;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Per-minute LLM accounting for one provider ({@code llm_usage}, Architecture §7.1).
 *
 * <p>Written by {@link LlmUsageMeter} with native upserts; the entity exists so Hibernate owns the
 * table. {@code refused} counts calls the budget declined.
 */
@Entity
@Table(name = "llm_usage")
public class LlmUsageEntity {

    @EmbeddedId
    private LlmUsageId id;

    @Column(name = "requests", nullable = false)
    private long requests;

    @Column(name = "input_tokens", nullable = false)
    private long inputTokens;

    @Column(name = "output_tokens", nullable = false)
    private long outputTokens;

    @Column(name = "cached_tokens", nullable = false)
    private long cachedTokens;

    @Column(name = "errors", nullable = false)
    private long errors;

    @Column(name = "refused", nullable = false)
    private long refused;

    protected LlmUsageEntity() {
        // for JPA
    }

    /** Creates an empty row. */
    public LlmUsageEntity(LlmUsageId id) {
        this.id = id;
    }

    /** The id. */
    public LlmUsageId getId() {
        return id;
    }

    /** Calls sent. */
    public long getRequests() {
        return requests;
    }

    /** Input tokens. */
    public long getInputTokens() {
        return inputTokens;
    }

    /** Output tokens. */
    public long getOutputTokens() {
        return outputTokens;
    }

    /** Cached input tokens. */
    public long getCachedTokens() {
        return cachedTokens;
    }

    /** Failed calls. */
    public long getErrors() {
        return errors;
    }

    /** Calls the budget refused. */
    public long getRefused() {
        return refused;
    }
}
