package ai.foresignal.evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A link from an observation (a record version) to a canonical event (prompt 116, table
 * {@code observation_link}), unique per {@code (source_id, external_id, canonical_event_id)}: a later version
 * of the same record updates its link. {@code attributes} keeps the comparable facts the matcher read (the
 * compared attribute, country, ASN, interval, hazard type, upstream ids and citation), so later observations
 * are matched against stored facts, never against re-read history.
 */
@Entity
@Table(name = "observation_link", uniqueConstraints = @UniqueConstraint(name = "observation_link_record_event_key",
        columnNames = {"source_id", "external_id", "canonical_event_id"}), indexes = {
        @Index(name = "observation_link_event_idx", columnList = "canonical_event_id"),
        @Index(name = "observation_link_upstream_idx", columnList = "upstream_event_id"),
        @Index(name = "observation_link_source_idx", columnList = "source_id, canonical_event_id")})
public class ObservationLinkEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "canonical_event_id", length = 26, nullable = false)
    private String canonicalEventId;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 512, nullable = false)
    private String externalId;

    @Column(name = "seq", nullable = false)
    private long seq;

    @Column(name = "version")
    private Integer version;

    @Column(name = "originator_id", length = 128, nullable = false)
    private String originatorId;

    @Column(name = "independence_root", length = 128, nullable = false)
    private String independenceRoot;

    @Column(name = "upstream_event_id", length = 600)
    private String upstreamEventId;

    @Column(name = "evidence_group_id", length = 26)
    private String evidenceGroupId;

    @Column(name = "link_confidence", nullable = false)
    private double linkConfidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "link_reason", nullable = false)
    private Map<String, Object> linkReason;

    @Column(name = "event_time")
    private Instant eventTime;

    @Column(name = "observed_at")
    private Instant observedAt;

    @Column(name = "lon")
    private Double lon;

    @Column(name = "lat")
    private Double lat;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes")
    private Map<String, Object> attributes;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    /** For JPA. */
    protected ObservationLinkEntity() {}

    /** The link id. */
    public String getId() {
        return id;
    }

    /** The canonical event. */
    public String getCanonicalEventId() {
        return canonicalEventId;
    }

    /** The record's source. */
    public String getSourceId() {
        return sourceId;
    }

    /** The record's external id. */
    public String getExternalId() {
        return externalId;
    }

    /** The version's seq. */
    public long getSeq() {
        return seq;
    }

    /** The record version number, or null. */
    public Integer getVersion() {
        return version;
    }

    /** The originator. */
    public String getOriginatorId() {
        return originatorId;
    }

    /** The originator's independence root. */
    public String getIndependenceRoot() {
        return independenceRoot;
    }

    /** The upstream's own event id ({@code namespace:value}), or null. */
    public String getUpstreamEventId() {
        return upstreamEventId;
    }

    /** The evidence group. */
    public String getEvidenceGroupId() {
        return evidenceGroupId;
    }

    /** The link confidence, 0–1. */
    public double getLinkConfidence() {
        return linkConfidence;
    }

    /** Why the observation was linked. */
    public Map<String, Object> getLinkReason() {
        return linkReason;
    }

    /** The observation's event time. */
    public Instant getEventTime() {
        return eventTime;
    }

    /** The observation's observed time. */
    public Instant getObservedAt() {
        return observedAt;
    }

    /** The observation's longitude, or null. */
    public Double getLon() {
        return lon;
    }

    /** The observation's latitude, or null. */
    public Double getLat() {
        return lat;
    }

    /** The comparable facts. */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /** When the link was made or last updated. */
    public Instant getLinkedAt() {
        return linkedAt;
    }
}
