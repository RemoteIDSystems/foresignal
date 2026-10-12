package ai.foresignal.llm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One configured LLM provider ({@code llm_provider}, Architecture §7.1 and §19).
 *
 * <p>The API key is encrypted with {@code CredentialCipher} into {@code secret_ciphertext} /
 * {@code secret_nonce} and never returned ({@code apiKeySet} only). Beyond the Architecture columns,
 * prompt 026 keeps the provider's health here ({@code last_success_at}, {@code last_error_at},
 * {@code last_error}, {@code consecutive_failures}, {@code supports_tools}) so it survives a restart,
 * and the chat model's {@code knowledge_cutoff} (read by prompt 122).
 */
@Entity
@Table(name = "llm_provider")
public class LlmProviderEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "base_url", nullable = false, length = 2048)
    private String baseUrl;

    @Column(name = "chat_model", length = 200)
    private String chatModel;

    @Column(name = "embedding_model", length = 200)
    private String embeddingModel;

    @Column(name = "embedding_dimensions")
    private Integer embeddingDimensions;

    /** {@code effort}, {@code maxTokens}, {@code temperature}, {@code extraHeaders}, {@code local}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", nullable = false, columnDefinition = "jsonb default '{}'::jsonb")
    private Map<String, Object> options = new LinkedHashMap<>();

    @Column(name = "secret_ciphertext")
    private byte[] secretCiphertext;

    @Column(name = "secret_nonce")
    private byte[] secretNonce;

    @Column(name = "requests_per_minute", nullable = false)
    private int requestsPerMinute;

    @Column(name = "tokens_per_day", nullable = false)
    private long tokensPerDay;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "knowledge_cutoff")
    private LocalDate knowledgeCutoff;

    /** False once the chat model rejected tools; null until known. */
    @Column(name = "supports_tools")
    private Boolean supportsTools;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_error_at")
    private Instant lastErrorAt;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "consecutive_failures", nullable = false, columnDefinition = "integer default 0 not null")
    private int consecutiveFailures;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LlmProviderEntity() {
        // for JPA
    }

    /** A provider with an id and a type; everything else is set by the service. */
    public LlmProviderEntity(String id, String type) {
        this.id = id;
        this.type = type;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The type wire value. */
    public String getType() {
        return type;
    }

    /** Sets the type wire value. */
    public void setType(String type) {
        this.type = type;
    }

    /** The base URL. */
    public String getBaseUrl() {
        return baseUrl;
    }

    /** Sets the base URL. */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** The chat model, or null. */
    public String getChatModel() {
        return chatModel;
    }

    /** Sets the chat model. */
    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }

    /** The embedding model, or null. */
    public String getEmbeddingModel() {
        return embeddingModel;
    }

    /** Sets the embedding model. */
    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /** The embedding width, or null. */
    public Integer getEmbeddingDimensions() {
        return embeddingDimensions;
    }

    /** Sets the embedding width. */
    public void setEmbeddingDimensions(Integer embeddingDimensions) {
        this.embeddingDimensions = embeddingDimensions;
    }

    /** The options. */
    public Map<String, Object> getOptions() {
        return options;
    }

    /** Sets the options. */
    public void setOptions(Map<String, Object> options) {
        this.options = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);
    }

    /** The encrypted API key, or null. */
    public byte[] getSecretCiphertext() {
        return secretCiphertext;
    }

    /** The API key's nonce, or null. */
    public byte[] getSecretNonce() {
        return secretNonce;
    }

    /** Sets (or, with nulls, clears) the encrypted API key. */
    public void setSecret(byte[] ciphertext, byte[] nonce) {
        this.secretCiphertext = ciphertext;
        this.secretNonce = nonce;
    }

    /** Requests per minute. */
    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    /** Sets requests per minute. */
    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    /** Tokens per UTC day. */
    public long getTokensPerDay() {
        return tokensPerDay;
    }

    /** Sets tokens per UTC day. */
    public void setTokensPerDay(long tokensPerDay) {
        this.tokensPerDay = tokensPerDay;
    }

    /** Whether the provider may be used. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets whether the provider may be used. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** The chat model's training-data cutoff, or null. */
    public LocalDate getKnowledgeCutoff() {
        return knowledgeCutoff;
    }

    /** Sets the chat model's training-data cutoff. */
    public void setKnowledgeCutoff(LocalDate knowledgeCutoff) {
        this.knowledgeCutoff = knowledgeCutoff;
    }

    /** False once the chat model rejected tools; null until known. */
    public Boolean getSupportsTools() {
        return supportsTools;
    }

    /** Sets whether the chat model supports tools. */
    public void setSupportsTools(Boolean supportsTools) {
        this.supportsTools = supportsTools;
    }

    /** The last successful call, or null. */
    public Instant getLastSuccessAt() {
        return lastSuccessAt;
    }

    /** Sets the last successful call. */
    public void setLastSuccessAt(Instant lastSuccessAt) {
        this.lastSuccessAt = lastSuccessAt;
    }

    /** The last failed call, or null. */
    public Instant getLastErrorAt() {
        return lastErrorAt;
    }

    /** Sets the last failed call. */
    public void setLastErrorAt(Instant lastErrorAt) {
        this.lastErrorAt = lastErrorAt;
    }

    /** The last error message, or null. */
    public String getLastError() {
        return lastError;
    }

    /** Sets the last error message. */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /** Failed calls in a row. */
    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    /** Sets failed calls in a row. */
    public void setConsecutiveFailures(int consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets when it was created. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When its configuration last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when its configuration last changed. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
