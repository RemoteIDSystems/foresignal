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
 * A published report revision (table {@code report_revision}, prompt 132): immutable (RPT-1). It holds the frozen
 * report document (every block with pinned references and stored snapshots), the JSON bundle and the Markdown and
 * HTML renditions rendered at publication, the bundle's SHA-256 and the references it pins. Retraction only marks it
 * withdrawn; nothing else ever changes.
 */
@Entity
@Table(name = "report_revision", indexes = {
    @Index(name = "report_revision_report_idx", columnList = "report_id, revision", unique = true)
})
public class ReportRevisionEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "report_id", length = 26, nullable = false)
    private String reportId;

    @Column(name = "revision", nullable = false)
    private int revision;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Column(name = "tlp", length = 16, nullable = false)
    private String tlp;

    @Column(name = "status", length = 16, nullable = false)
    private String status = ReportVocabulary.PUBLISHED;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "published_by", length = 128, nullable = false)
    private String publishedBy;

    @Column(name = "retracted_at")
    private Instant retractedAt;

    @Column(name = "retraction_reason", length = 2000)
    private String retractionReason;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pinned_refs", nullable = false)
    private List<String> pinnedRefs = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bundle", nullable = false)
    private Map<String, Object> bundle;

    @Column(name = "bundle_sha256", length = 64, nullable = false)
    private String bundleSha256;

    @Column(name = "markdown", columnDefinition = "text", nullable = false)
    private String markdown;

    @Column(name = "html", columnDefinition = "text", nullable = false)
    private String html;

    @Column(name = "machine_generated", nullable = false)
    private boolean machineGenerated;

    /** For JPA. */
    protected ReportRevisionEntity() {}

    /**
     * A new revision.
     *
     * @param id the ULID
     * @param reportId the report
     * @param revision the revision number (1, 2, …)
     * @param title the title at publication
     * @param tlp the TLP label at publication
     * @param publishedBy the publishing principal
     * @param publishedAt when
     */
    public ReportRevisionEntity(String id, String reportId, int revision, String title, String tlp,
            String publishedBy, Instant publishedAt) {
        this.id = id;
        this.reportId = reportId;
        this.revision = revision;
        this.title = title;
        this.tlp = tlp;
        this.publishedBy = publishedBy;
        this.publishedAt = publishedAt;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The report. */
    public String getReportId() {
        return reportId;
    }

    /** The revision number. */
    public int getRevision() {
        return revision;
    }

    /** The title at publication. */
    public String getTitle() {
        return title;
    }

    /** The TLP label at publication. */
    public String getTlp() {
        return tlp;
    }

    /** {@code published} or {@code retracted}. */
    public String getStatus() {
        return status;
    }

    /** When it was published. */
    public Instant getPublishedAt() {
        return publishedAt;
    }

    /** The publishing principal. */
    public String getPublishedBy() {
        return publishedBy;
    }

    /** When it was retracted, or null. */
    public Instant getRetractedAt() {
        return retractedAt;
    }

    /** The retraction reason, or null. */
    public String getRetractionReason() {
        return retractionReason;
    }

    /** Marks the revision withdrawn (the only change a revision ever takes). */
    public void retract(String reason, Instant at) {
        this.status = ReportVocabulary.RETRACTED;
        this.retractionReason = reason;
        this.retractedAt = at;
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest (at creation). */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** The references the revision pins. */
    public List<String> getPinnedRefs() {
        return pinnedRefs;
    }

    /** Sets the pinned references (at creation). */
    public void setPinnedRefs(List<String> pinnedRefs) {
        this.pinnedRefs = pinnedRefs == null ? new ArrayList<>() : new ArrayList<>(pinnedRefs);
    }

    /** The JSON bundle ({@code ReportBundle}). */
    public Map<String, Object> getBundle() {
        return bundle;
    }

    /** Sets the JSON bundle and its digest (at creation). */
    public void setBundle(Map<String, Object> bundle, String sha256) {
        this.bundle = bundle;
        this.bundleSha256 = sha256;
    }

    /** The SHA-256 of the canonical bundle bytes, lower-case hex. */
    public String getBundleSha256() {
        return bundleSha256;
    }

    /** The Markdown rendition. */
    public String getMarkdown() {
        return markdown;
    }

    /** The HTML rendition. */
    public String getHtml() {
        return html;
    }

    /** Sets the renditions (at creation). */
    public void setRenditions(String markdown, String html) {
        this.markdown = markdown;
        this.html = html;
    }

    /** True when any block was machine-generated. */
    public boolean isMachineGenerated() {
        return machineGenerated;
    }

    /** Sets the machine-generated flag (at creation). */
    public void setMachineGenerated(boolean machineGenerated) {
        this.machineGenerated = machineGenerated;
    }
}
