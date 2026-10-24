package ai.foresignal.inbound.edp.binding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.ColumnDefault;

/**
 * A pending on-demand analysis request (prompt 155, table {@code analysis_correlation}): Foresignal asked an
 * external forecaster to forecast a question now and is waiting for the correlated result. The {@code id} is
 * the correlation id sent to the provider and echoed in its callback; the callback resolves this row to learn
 * which question and forecaster the returned forecast belongs to, then records it through the ordinary
 * {@link ForecastBindingTarget} path.
 */
@Entity
@Table(name = "analysis_correlation",
        indexes = @Index(name = "analysis_correlation_question_idx", columnList = "question_id"))
public class AnalysisCorrelationEntity {

    /** A request that has not yet been answered. */
    public static final String REQUESTED = "requested";

    /** A request whose forecast was recorded. */
    public static final String FULFILLED = "fulfilled";

    /** A request that failed or was answered with an unusable forecast. */
    public static final String FAILED = "failed";

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "stream_id", length = 48, nullable = false)
    private String streamId;

    @Column(name = "question_id", length = 26, nullable = false)
    private String questionId;

    @ColumnDefault("'same'")
    @Column(name = "orientation", length = 8, nullable = false)
    private String orientation = BindingVocabulary.SAME;

    @Column(name = "forecaster_id", length = 128, nullable = false)
    private String forecasterId;

    @Column(name = "horizon", length = 32)
    private String horizon;

    @ColumnDefault("'requested'")
    @Column(name = "status", length = 16, nullable = false)
    private String status = REQUESTED;

    @Column(name = "created_by", length = 200, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "fulfilled_at")
    private Instant fulfilledAt;

    /** For JPA. */
    protected AnalysisCorrelationEntity() {}

    /** A new pending request. */
    public AnalysisCorrelationEntity(String id, String sourceId, String streamId, String questionId,
            String orientation, String horizon, String createdBy, Instant now) {
        this.id = id;
        this.sourceId = sourceId;
        this.streamId = streamId;
        this.questionId = questionId;
        this.orientation = orientation == null ? BindingVocabulary.SAME : orientation;
        this.forecasterId = BindingVocabulary.forecasterId(sourceId, streamId);
        this.horizon = horizon;
        this.status = REQUESTED;
        this.createdBy = createdBy;
        this.createdAt = now;
    }

    /** The correlation id. */
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

    /** The requested horizon, or null. */
    public String getHorizon() {
        return horizon;
    }

    /** {@code requested}, {@code fulfilled} or {@code failed}. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Who requested it. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** When it was requested. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it was answered, or null. */
    public Instant getFulfilledAt() {
        return fulfilledAt;
    }

    /** Sets when it was answered. */
    public void setFulfilledAt(Instant fulfilledAt) {
        this.fulfilledAt = fulfilledAt;
    }
}
