package ai.foresignal.inbound.edp.binding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * A provider question linked to a ledger question (prompt 138, table {@code external_question_link}): forecast items
 * of the stream whose {@code question.externalId} is {@code externalQuestionId} become forecasts of the external
 * forecaster {@code edp:{sourceId}/{streamId}} on {@code questionId}, inverted for orientation {@code inverse}.
 */
@Entity
@Table(name = "external_question_link", uniqueConstraints = @UniqueConstraint(
        name = "external_question_link_key", columnNames = {"source_id", "stream_id", "external_question_id"}),
        indexes = @Index(name = "external_question_link_question_idx", columnList = "question_id"))
public class ExternalQuestionLinkEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "stream_id", length = 48, nullable = false)
    private String streamId;

    @Column(name = "external_question_id", length = 256, nullable = false)
    private String externalQuestionId;

    @Column(name = "question_id", length = 26, nullable = false)
    private String questionId;

    @Column(name = "orientation", length = 8, nullable = false)
    private String orientation = BindingVocabulary.SAME;

    @Column(name = "forecaster_id", length = 128, nullable = false)
    private String forecasterId;

    @Column(name = "created_by", length = 200, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_forecast_at")
    private Instant lastForecastAt;

    /** For JPA. */
    protected ExternalQuestionLinkEntity() {}

    /** A new link. */
    public ExternalQuestionLinkEntity(String id, String sourceId, String streamId, String externalQuestionId,
            String questionId, String orientation, String createdBy, Instant now) {
        this.id = id;
        this.sourceId = sourceId;
        this.streamId = streamId;
        this.externalQuestionId = externalQuestionId;
        this.questionId = questionId;
        this.orientation = orientation == null ? BindingVocabulary.SAME : orientation;
        this.forecasterId = BindingVocabulary.forecasterId(sourceId, streamId);
        this.createdBy = createdBy;
        this.createdAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The provider. */
    public String getSourceId() {
        return sourceId;
    }

    /** The stream. */
    public String getStreamId() {
        return streamId;
    }

    /** The provider's question id. */
    public String getExternalQuestionId() {
        return externalQuestionId;
    }

    /** The ledger question. */
    public String getQuestionId() {
        return questionId;
    }

    /** {@code same} or {@code inverse}. */
    public String getOrientation() {
        return orientation;
    }

    /** The external forecaster id. */
    public String getForecasterId() {
        return forecasterId;
    }

    /** Who linked it. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** When it was linked. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When a forecast was last recorded through it, or null. */
    public Instant getLastForecastAt() {
        return lastForecastAt;
    }

    /** Sets when a forecast was last recorded through it. */
    public void setLastForecastAt(Instant lastForecastAt) {
        this.lastForecastAt = lastForecastAt;
    }
}
