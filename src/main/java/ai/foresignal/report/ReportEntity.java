package ai.foresignal.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A report (table {@code report}, prompt 132): the editable draft of an analyst report. Owner, visibility, access list
 * and TLP live in the 130 registry ({@code shareable_object}); the row keeps the title, authors, source, template,
 * time window, area, the ordered blocks ({@code sections}), the optimistic-lock {@code version} of the draft and the
 * latest published revision. Published revisions are {@link ReportRevisionEntity} rows and never change.
 */
@Entity
@Table(name = "report", indexes = {
    @Index(name = "report_display_idx", columnList = "display_id", unique = true),
    @Index(name = "report_status_idx", columnList = "status")
})
public class ReportEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "display_id", length = 32, nullable = false)
    private String displayId;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Column(name = "subtitle", length = 300)
    private String subtitle;

    @Column(name = "status", length = 16, nullable = false)
    private String status = ReportVocabulary.DRAFT;

    @Column(name = "template", length = 32, nullable = false)
    private String template = ReportVocabulary.CUSTOM;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "authors", nullable = false)
    private List<String> authors = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source", nullable = false)
    private Map<String, Object> source = new LinkedHashMap<>();

    @Column(name = "source_ref", length = 1024)
    private String sourceRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "time_window")
    private Map<String, Object> timeWindow;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "area")
    private Map<String, Object> area;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sections", nullable = false)
    private List<Map<String, Object>> sections = new ArrayList<>();

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "published_revision")
    private Integer publishedRevision;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ReportEntity() {}

    /**
     * A new draft.
     *
     * @param id the ULID
     * @param displayId {@code RPT-yyyy-nnnn}
     * @param title the title
     * @param now the creation time
     */
    public ReportEntity(String id, String displayId, String title, Instant now) {
        this.id = id;
        this.displayId = displayId;
        this.title = title;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The display id ({@code RPT-yyyy-nnnn}). */
    public String getDisplayId() {
        return displayId;
    }

    /** The title. */
    public String getTitle() {
        return title;
    }

    /** Sets the title. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** The subtitle, or null. */
    public String getSubtitle() {
        return subtitle;
    }

    /** Sets the subtitle. */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /** The status ({@link ReportVocabulary#STATUSES}). */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The template. */
    public String getTemplate() {
        return template;
    }

    /** Sets the template. */
    public void setTemplate(String template) {
        this.template = template;
    }

    /** The author principal ids, the owner first. */
    public List<String> getAuthors() {
        return authors;
    }

    /** Sets the author principal ids. */
    public void setAuthors(List<String> authors) {
        this.authors = authors == null ? new ArrayList<>() : new ArrayList<>(authors);
    }

    /** The {@code ReportSource}. */
    public Map<String, Object> getSource() {
        return source;
    }

    /** Sets the source and its reference column. */
    public void setSource(Map<String, Object> source) {
        this.source = source == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source);
        Object ref = this.source.get("ref");
        this.sourceRef = ref instanceof String text ? text : null;
    }

    /** The source's object reference, or null (a selection). */
    public String getSourceRef() {
        return sourceRef;
    }

    /** The time window ({@code TimeRange}), or null. */
    public Map<String, Object> getTimeWindow() {
        return timeWindow;
    }

    /** Sets the time window. */
    public void setTimeWindow(Map<String, Object> timeWindow) {
        this.timeWindow = timeWindow;
    }

    /** The area (GeoJSON geometry), or null. */
    public Map<String, Object> getArea() {
        return area;
    }

    /** Sets the area. */
    public void setArea(Map<String, Object> area) {
        this.area = area;
    }

    /** The ordered blocks. */
    public List<Map<String, Object>> getSections() {
        return sections;
    }

    /** Sets the ordered blocks. */
    public void setSections(List<Map<String, Object>> sections) {
        this.sections = sections == null ? new ArrayList<>() : new ArrayList<>(sections);
    }

    /** The optimistic-lock version of the draft. */
    public int getVersion() {
        return version;
    }

    /** The latest published revision, or null. */
    public Integer getPublishedRevision() {
        return publishedRevision;
    }

    /** Sets the latest published revision. */
    public void setPublishedRevision(Integer publishedRevision) {
        this.publishedRevision = publishedRevision;
    }

    /** When the latest revision was published, or null. */
    public Instant getPublishedAt() {
        return publishedAt;
    }

    /** Sets the publication time. */
    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    /** The 116 manifest of the latest revision, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest id. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** The creating actor. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets the creating actor. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Records a change of the draft: bumps the version and the update time. */
    public void touch(Instant now) {
        this.version++;
        this.updatedAt = now;
    }
}
