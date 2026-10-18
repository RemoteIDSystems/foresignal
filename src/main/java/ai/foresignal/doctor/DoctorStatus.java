package ai.foresignal.doctor;

import java.util.List;

/** The overall verdict of a doctor report (OpenAPI {@code DoctorReport.status}). */
public enum DoctorStatus {
    /** No finding above info. */
    OK,
    /** At least one warning, no critical finding. */
    DEGRADED_INFO,
    /** At least one critical finding. */
    BROKEN;

    /** The verdict of a list of findings: the worst severity decides. */
    public static DoctorStatus of(List<DoctorFinding> findings) {
        boolean warning = false;
        for (DoctorFinding f : findings) {
            if (f.severity() == DoctorFinding.Severity.CRITICAL) {
                return BROKEN;
            }
            warning |= f.severity() == DoctorFinding.Severity.WARNING;
        }
        return warning ? DEGRADED_INFO : OK;
    }
}
