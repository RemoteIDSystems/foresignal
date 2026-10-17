package ai.foresignal.cycle;

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
 * An assessment (prompt 128, table {@code assessment}): the ICD 203-structured product of a cycle run or an on-demand
 * situation assessment. It links the previous assessment of its cadence (or situation), carries the key judgments
 * (BLUF), the rendered sections with their data, the full markdown and HTML, the per-role outcomes, citations and
 * lint warnings, and is private when any input was.
 */
@Entity
@Table(name = "assessment", indexes = {
    @Index(name = "assessment_cadence_idx", columnList = "cadence, id"),
    @Index(name = "assessment_situation_idx", columnList = "situation_id, id")
})
public class AssessmentEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "cycle_id", length = 26)
    private String cycleId;

    @Column(name = "situation_id", length = 26)
    private String situationId;

    @Column(name = "cadence", length = 16, nullable = false)
    private String cadence;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Column(name = "previous_assessment_id", length = 26)
    private String previousAssessmentId;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bluf", nullable = false)
    private List<Map<String, Object>> bluf = new ArrayList<>();

    @Column(name = "headline", length = 1000)
    private String headline;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sections", nullable = false)
    private List<Map<String, Object>> sections = new ArrayList<>();

    @Column(name = "markdown", columnDefinition = "text")
    private String markdown;

    @Column(name = "html", columnDefinition = "text")
    private String html;

    @Column(name = "packet_ref", length = 26)
    private String packetRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roles", nullable = false)
    private Map<String, Object> roles = new LinkedHashMap<>();

    @Column(name = "model", length = 200)
    private String model;

    @Column(name = "effort", length = 8)
    private String effort;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prompt_templates", nullable = false)
    private List<Map<String, Object>> promptTemplates = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "citations", nullable = false)
    private List<String> citations = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lint_warnings", nullable = false)
    private List<String> lintWarnings = new ArrayList<>();

    @Column(name = "private", nullable = false)
    private boolean privateAssessment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected AssessmentEntity() {}

    /** Creates an assessment. */
    public AssessmentEntity(String id, String cadence, Instant periodStart, Instant periodEnd, Instant createdAt) {
        this.id = id;
        this.cadence = cadence;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.createdAt = createdAt;
        this.status = CycleVocabulary.DIGEST_ONLY;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The cycle, or null. */
    public String getCycleId() {
        return cycleId;
    }

    /** Sets the cycle. */
    public void setCycleId(String cycleId) {
        this.cycleId = cycleId;
    }

    /** The situation of an on-demand assessment, or null. */
    public String getSituationId() {
        return situationId;
    }

    /** Sets the situation. */
    public void setSituationId(String situationId) {
        this.situationId = situationId;
    }

    /** The cadence. */
    public String getCadence() {
        return cadence;
    }

    /** The period start. */
    public Instant getPeriodStart() {
        return periodStart;
    }

    /** The period end. */
    public Instant getPeriodEnd() {
        return periodEnd;
    }

    /** The previous assessment of this cadence (or situation), or null. */
    public String getPreviousAssessmentId() {
        return previousAssessmentId;
    }

    /** Sets the previous assessment. */
    public void setPreviousAssessmentId(String previousAssessmentId) {
        this.previousAssessmentId = previousAssessmentId;
    }

    /** ok, partial, digest-only or failed. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The key judgments ({@code KeyJudgment}). */
    public List<Map<String, Object>> getBluf() {
        return bluf;
    }

    /** Sets the key judgments. */
    public void setBluf(List<Map<String, Object>> bluf) {
        this.bluf = bluf == null ? new ArrayList<>() : new ArrayList<>(bluf);
    }

    /** The first judgment or the top deterministic change, or null. */
    public String getHeadline() {
        return headline;
    }

    /** Sets the headline. */
    public void setHeadline(String headline) {
        this.headline = headline;
    }

    /** The rendered sections. */
    public List<Map<String, Object>> getSections() {
        return sections;
    }

    /** Sets the sections. */
    public void setSections(List<Map<String, Object>> sections) {
        this.sections = sections == null ? new ArrayList<>() : new ArrayList<>(sections);
    }

    /** The full markdown. */
    public String getMarkdown() {
        return markdown;
    }

    /** Sets the markdown. */
    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    /** The full HTML page. */
    public String getHtml() {
        return html;
    }

    /** Sets the HTML. */
    public void setHtml(String html) {
        this.html = html;
    }

    /** The run holding the packet. */
    public String getPacketRef() {
        return packetRef;
    }

    /** Sets the packet reference. */
    public void setPacketRef(String packetRef) {
        this.packetRef = packetRef;
    }

    /** The per-role outcomes, by role. */
    public Map<String, Object> getRoles() {
        return roles;
    }

    /** Sets the role outcomes. */
    public void setRoles(Map<String, Object> roles) {
        this.roles = roles == null ? new LinkedHashMap<>() : new LinkedHashMap<>(roles);
    }

    /** The model of the analyst role (or of any role that ran), or null. */
    public String getModel() {
        return model;
    }

    /** Sets the model. */
    public void setModel(String model) {
        this.model = model;
    }

    /** The effort used, or null without an LLM. */
    public String getEffort() {
        return effort;
    }

    /** Sets the effort. */
    public void setEffort(String effort) {
        this.effort = effort;
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** The prompt templates used ({@code {id, version}}). */
    public List<Map<String, Object>> getPromptTemplates() {
        return promptTemplates;
    }

    /** Sets the prompt templates. */
    public void setPromptTemplates(List<Map<String, Object>> promptTemplates) {
        this.promptTemplates = promptTemplates == null ? new ArrayList<>() : new ArrayList<>(promptTemplates);
    }

    /** The cited object references. */
    public List<String> getCitations() {
        return citations;
    }

    /** Sets the citations. */
    public void setCitations(List<String> citations) {
        this.citations = citations == null ? new ArrayList<>() : new ArrayList<>(citations);
    }

    /** The estimative-language warnings. */
    public List<String> getLintWarnings() {
        return lintWarnings;
    }

    /** Sets the lint warnings. */
    public void setLintWarnings(List<String> lintWarnings) {
        this.lintWarnings = lintWarnings == null ? new ArrayList<>() : new ArrayList<>(lintWarnings);
    }

    /** True when any input was private. */
    public boolean isPrivateAssessment() {
        return privateAssessment;
    }

    /** Marks the assessment private. */
    public void setPrivateAssessment(boolean privateAssessment) {
        this.privateAssessment = privateAssessment;
    }

    /** Creation time. */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
