package ai.foresignal.doctor;

/** The codes of deterministic doctor findings (OpenAPI {@code DoctorFinding.code}). */
public enum FindingCode {
    /** 401/403 without a data body: credentials missing, wrong or expired. */
    AUTH_FAILED,
    /** 451, or 403 with a block page: the upstream refuses this client. */
    UA_BLOCKED,
    /** 404/410, or a permanent redirect to another host. */
    ENDPOINT_MOVED,
    /** 429, or a backoff in force. */
    RATE_LIMITED,
    /** A streak of 5xx responses. */
    UPSTREAM_ERROR,
    /** An HTML page (login wall, maintenance page, captcha) where data was expected. */
    HTML_INSTEAD_OF_DATA,
    /** The structure is intact but holds no items. */
    EMPTY_RESULT,
    /** A field the parser reads appears to have been renamed. */
    FIELD_RENAMED,
    /** A field the parser reads is gone. */
    FIELD_REMOVED,
    /** A field's type changed. */
    TYPE_CHANGED,
    /** The content type, delimiter or overall format changed. */
    FORMAT_CHANGED,
    /** The parser threw {@code UpstreamFormatException} on the probe. */
    PARSER_REJECTS,
    /** Coverage fell by at least half against the median of the recent reports. */
    COVERAGE_DROPPED,
    /** Nothing changed against the fixture and the probe parsed. */
    NO_CHANGE_DETECTED,
    /** The probe was skipped because the budget was spent or a backoff was in force; diagnosed from history only. */
    PROBE_SKIPPED_BUDGET
}
