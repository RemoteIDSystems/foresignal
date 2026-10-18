package ai.foresignal.doctor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * One doctor report as the API returns it (OpenAPI {@code DoctorReport}). Every proposed patch inside {@code llm}
 * carries the label {@value DoctorLlm#PATCH_LABEL}.
 *
 * @param id the ULID
 * @param sourceId the source
 * @param createdAt when the run started
 * @param trigger {@code MANUAL}, {@code AUTO} or {@code MCP}
 * @param status {@code OK}, {@code DEGRADED_INFO} or {@code BROKEN}
 * @param findings the deterministic findings, worst first
 * @param diff the structure diff, or null
 * @param probe the probe summary, or null
 * @param fixture the fixture compared against, or null
 * @param llm the LLM block, or null
 * @param adapterVersion the Foresignal version that produced the report
 */
public record DoctorReportView(String id, String sourceId, Instant createdAt, DoctorTrigger trigger,
        DoctorStatus status, List<DoctorFinding> findings, Map<String, Object> diff, Map<String, Object> probe,
        Map<String, Object> fixture, Map<String, Object> llm, String adapterVersion) {

    /** Copies the findings. */
    public DoctorReportView {
        findings = findings == null ? List.of() : List.copyOf(findings);
    }

    /** The first (worst) finding, or null. */
    public DoctorFinding topFinding() {
        return findings.isEmpty() ? null : findings.get(0);
    }

    /** The summary on source health. */
    public DoctorReportSummary summary() {
        DoctorFinding top = topFinding();
        return new DoctorReportSummary(id, status, top == null ? null
                : new DoctorReportSummary.TopFinding(top.code(), top.suggestedAction()), createdAt);
    }
}
