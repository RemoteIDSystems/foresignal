package ai.foresignal.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A recorded send of a report on this instance (table {@code report_send}, prompt 132): who sent which revision to
 * whom, with each recipient's outcome ({@code ReportSend}). Retraction notifies every recipient recorded here.
 */
@Entity
@Table(name = "report_send", indexes = {@Index(name = "report_send_report_idx", columnList = "report_id")})
public class ReportSendEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "report_id", length = 26, nullable = false)
    private String reportId;

    @Column(name = "revision")
    private Integer revision;

    @Column(name = "sent_by", length = 128, nullable = false)
    private String sentBy;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "message", length = 2000)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recipients", nullable = false)
    private List<Map<String, Object>> recipients = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "principal_ids", nullable = false)
    private List<String> principalIds = new ArrayList<>();

    /** For JPA. */
    protected ReportSendEntity() {}

    /**
     * A new send.
     *
     * @param id the ULID
     * @param reportId the report
     * @param revision the revision sent
     * @param sentBy the sending principal
     * @param sentAt when
     */
    public ReportSendEntity(String id, String reportId, Integer revision, String sentBy, Instant sentAt) {
        this.id = id;
        this.reportId = reportId;
        this.revision = revision;
        this.sentBy = sentBy;
        this.sentAt = sentAt;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The report. */
    public String getReportId() {
        return reportId;
    }

    /** The revision sent. */
    public Integer getRevision() {
        return revision;
    }

    /** The sending principal. */
    public String getSentBy() {
        return sentBy;
    }

    /** When. */
    public Instant getSentAt() {
        return sentAt;
    }

    /** The message, or null. */
    public String getMessage() {
        return message;
    }

    /** Sets the message. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** The per-recipient outcomes ({@code grantee}, {@code status}, {@code channelIds}). */
    public List<Map<String, Object>> getRecipients() {
        return recipients;
    }

    /** Sets the per-recipient outcomes. */
    public void setRecipients(List<Map<String, Object>> recipients) {
        this.recipients = recipients == null ? new ArrayList<>() : new ArrayList<>(recipients);
    }

    /** Every principal reached (group members expanded), for retraction notices. */
    public List<String> getPrincipalIds() {
        return principalIds;
    }

    /** Sets the principals reached. */
    public void setPrincipalIds(List<String> principalIds) {
        this.principalIds = principalIds == null ? new ArrayList<>() : new ArrayList<>(principalIds);
    }
}
