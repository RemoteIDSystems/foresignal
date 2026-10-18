package ai.foresignal.doctor;

/** What started a doctor run (OpenAPI {@code DoctorReport.trigger}). */
public enum DoctorTrigger {
    /** {@code POST /v1/sources/{id}/doctor}. */
    MANUAL,
    /** A health transition (format change, coverage drop, parse error). */
    AUTO,
    /** The MCP tool {@code diagnose_source}. */
    MCP
}
