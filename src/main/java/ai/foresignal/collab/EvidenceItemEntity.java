package ai.foresignal.collab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

/**
 * User-contributed evidence (table {@code evidence_item}, prompt 130): a title, a description, attachments and an
 * optional link (stored as metadata only, never fetched), graded with the Admiralty system: source reliability
 * {@code A}–{@code F} and information credibility {@code 1}–{@code 6}. Owner, visibility and TLP are in
 * {@code shareable_object}.
 */
@Entity
@Table(name = "evidence_item")
public class EvidenceItemEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachment_ids", nullable = false)
    private List<String> attachmentIds = new ArrayList<>();

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "source_description", length = 2000)
    private String sourceDescription;

    @Column(name = "source_reliability", length = 1, nullable = false)
    private String sourceReliability = "F";

    @Column(name = "information_credibility", nullable = false)
    private int informationCredibility = 6;

    @Column(name = "cited_source_id", length = 64)
    private String citedSourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cited_refs", nullable = false)
    private List<String> citedRefs = new ArrayList<>();

    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    @Column(name = "event_time")
    private Instant eventTime;

    @Column(name = "created_by", length = 64, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected EvidenceItemEntity() {}

    /**
     * A new evidence item.
     *
     * @param id its ULID
     * @param title its title
     * @param createdBy the creating principal
     * @param now the creation time
     */
    public EvidenceItemEntity(String id, String title, String createdBy, Instant now) {
        this.id = id;
        this.title = title;
        this.createdBy = createdBy;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The title. */
    public String getTitle() {
        return title;
    }

    /** Sets the title. */
    public void setTitle(String value) {
        this.title = value;
    }

    /** The markdown description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String value) {
        this.description = value;
    }

    /** The attachments. */
    public List<String> getAttachmentIds() {
        return attachmentIds;
    }

    /** Sets the attachments. */
    public void setAttachmentIds(List<String> value) {
        this.attachmentIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The link evidence (never fetched), or null. */
    public String getUrl() {
        return url;
    }

    /** Sets the link. */
    public void setUrl(String value) {
        this.url = value;
    }

    /** Where the evidence came from, in words, or null. */
    public String getSourceDescription() {
        return sourceDescription;
    }

    /** Sets the source description. */
    public void setSourceDescription(String value) {
        this.sourceDescription = value;
    }

    /** Admiralty source reliability {@code A}–{@code F}. */
    public String getSourceReliability() {
        return sourceReliability;
    }

    /** Sets the source reliability. */
    public void setSourceReliability(String value) {
        this.sourceReliability = value;
    }

    /** Admiralty information credibility 1–6. */
    public int getInformationCredibility() {
        return informationCredibility;
    }

    /** Sets the information credibility. */
    public void setInformationCredibility(int value) {
        this.informationCredibility = value;
    }

    /** The cataloged source the evidence cites, or null. */
    public String getCitedSourceId() {
        return citedSourceId;
    }

    /** Sets the cited source. */
    public void setCitedSourceId(String value) {
        this.citedSourceId = value;
    }

    /** Objects the evidence cites. */
    public List<String> getCitedRefs() {
        return citedRefs;
    }

    /** Sets the cited objects. */
    public void setCitedRefs(List<String> value) {
        this.citedRefs = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The location, or null. */
    public Geometry getGeom() {
        return geom;
    }

    /** Sets the location. */
    public void setGeom(Geometry value) {
        this.geom = value;
    }

    /** When the evidenced event happened, or null. */
    public Instant getEventTime() {
        return eventTime;
    }

    /** Sets the event time. */
    public void setEventTime(Instant value) {
        this.eventTime = value;
    }

    /** The creating principal. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the change time. */
    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }

    /** The Admiralty grade, e.g. {@code B2}. */
    public String grade() {
        return sourceReliability + informationCredibility;
    }
}
