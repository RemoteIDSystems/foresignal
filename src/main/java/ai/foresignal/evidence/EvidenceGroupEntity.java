package ai.foresignal.evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * The observations of one canonical event that trace back to one originator (prompt 116, table
 * {@code evidence_group}). Only evidence groups count as independent confirmation.
 */
@Entity
@Table(name = "evidence_group", indexes = @Index(name = "evidence_group_event_idx", columnList = "canonical_event_id"))
public class EvidenceGroupEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "canonical_event_id", length = 26, nullable = false)
    private String canonicalEventId;

    @Column(name = "originator_root", length = 256, nullable = false)
    private String originatorRoot;

    @Column(name = "member_count", nullable = false)
    private int memberCount;

    @Column(name = "first_seq", nullable = false)
    private long firstSeq;

    /** For JPA. */
    protected EvidenceGroupEntity() {}

    /** The group id. */
    public String getId() {
        return id;
    }

    /** The canonical event. */
    public String getCanonicalEventId() {
        return canonicalEventId;
    }

    /** The originator (or cited source) every member traces to. */
    public String getOriginatorRoot() {
        return originatorRoot;
    }

    /** The members. */
    public int getMemberCount() {
        return memberCount;
    }

    /** The lowest member seq. */
    public long getFirstSeq() {
        return firstSeq;
    }
}
