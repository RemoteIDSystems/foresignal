package ai.foresignal.cap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One stored CAP 1.2 message Foresignal issued for an alert state change (prompt 112, table
 * {@code cap_message}). Rows follow the alert's retention: {@code OperationalTablesStep} deletes the messages
 * of alerts that no longer exist.
 */
@Entity
@Table(name = "cap_message", indexes = {
    @Index(name = "cap_message_alert_idx", columnList = "alert_id, n"),
    @Index(name = "cap_message_sent_idx", columnList = "sent")
})
public class CapMessageEntity {

    @Id
    @Column(name = "identifier", length = 128, nullable = false)
    private String identifier;

    @Column(name = "alert_id", nullable = false)
    private long alertId;

    @Column(name = "n", nullable = false)
    private int n;

    @Column(name = "msg_type", length = 16, nullable = false)
    private String msgType;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Column(name = "event", length = 32, nullable = false)
    private String event;

    @Column(name = "sent", nullable = false)
    private Instant sent;

    @Column(name = "headline", length = 160)
    private String headline;

    @Column(name = "xml", columnDefinition = "text", nullable = false)
    private String xml;

    @Column(name = "references_text", columnDefinition = "text")
    private String references;

    /** For JPA. */
    protected CapMessageEntity() {}

    /**
     * Creates a row.
     *
     * @param identifier the CAP identifier
     * @param alertId the alert
     * @param n the message's number within the alert, from 1
     * @param msgType {@code Alert} or {@code Update}
     * @param status {@code Actual}, {@code Exercise} or {@code Test}
     * @param event the alert event that caused it ({@code fired}, {@code acknowledged}, …)
     * @param sent when it was issued
     * @param headline the headline
     * @param xml the CAP document
     * @param references the {@code references} value, or null for the first message
     */
    public CapMessageEntity(String identifier, long alertId, int n, String msgType, String status, String event,
            Instant sent, String headline, String xml, String references) {
        this.identifier = identifier;
        this.alertId = alertId;
        this.n = n;
        this.msgType = msgType;
        this.status = status;
        this.event = event;
        this.sent = sent;
        this.headline = headline;
        this.xml = xml;
        this.references = references;
    }

    /** The CAP identifier. */
    public String getIdentifier() {
        return identifier;
    }

    /** The alert. */
    public long getAlertId() {
        return alertId;
    }

    /** The message number within the alert. */
    public int getN() {
        return n;
    }

    /** {@code Alert} or {@code Update}. */
    public String getMsgType() {
        return msgType;
    }

    /** The CAP status. */
    public String getStatus() {
        return status;
    }

    /** The alert event that caused the message. */
    public String getEvent() {
        return event;
    }

    /** When it was issued. */
    public Instant getSent() {
        return sent;
    }

    /** The headline. */
    public String getHeadline() {
        return headline;
    }

    /** The CAP 1.2 document. */
    public String getXml() {
        return xml;
    }

    /** The {@code references} value, or null. */
    public String getReferences() {
        return references;
    }
}
