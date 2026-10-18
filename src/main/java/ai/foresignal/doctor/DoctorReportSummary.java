package ai.foresignal.doctor;

import java.time.Instant;

/**
 * The latest doctor report on source health (OpenAPI {@code DoctorReportSummary}, {@code SourceHealth.lastDoctorReport}).
 *
 * @param id the report
 * @param status its verdict
 * @param topFinding its worst finding, or null
 * @param createdAt when it ran
 */
public record DoctorReportSummary(String id, DoctorStatus status, TopFinding topFinding, Instant createdAt) {

    /**
     * The worst finding.
     *
     * @param code the code
     * @param suggestedAction what to do
     */
    public record TopFinding(FindingCode code, String suggestedAction) {}
}
