package ai.foresignal.api.error;

import java.net.URI;
import java.util.Locale;

/**
 * The stable machine-readable codes carried in a problem's {@code code} property (Architecture §10).
 *
 * <p>Clients branch on these rather than on HTTP status or prose, so they must not change once
 * published.
 */
public final class ErrorCodes {

    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String BUDGET_EXCEEDED = "BUDGET_EXCEEDED";
    public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    public static final String UNSUPPORTED_MEDIA_TYPE = "UNSUPPORTED_MEDIA_TYPE";
    public static final String NOT_ACCEPTABLE = "NOT_ACCEPTABLE";
    public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    public static final String PAYLOAD_TOO_LARGE = "PAYLOAD_TOO_LARGE";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    /** 400: a source's ingest-policy override breaks a Core-Data-Model §5.1 rule. */
    public static final String INGEST_POLICY_INVALID = "INGEST_POLICY_INVALID";

    /** 400: retention was set on a TRACK source, whose window is {@code retention.trackWindow}. */
    public static final String RETENTION_TIER_FIXED = "RETENTION_TIER_FIXED";

    /** 400: a pagination cursor is malformed, tampered with, or belongs to a different query. */
    public static final String INVALID_CURSOR = "INVALID_CURSOR";

    /** 400: {@code asOf} without a {@code source} or {@code kind} filter (Core-Data-Model §12). */
    public static final String ASOF_FILTER_REQUIRED = "ASOF_FILTER_REQUIRED";

    /** 400: {@code asOf} before the retention horizon of an involved source; carries {@code horizon}. */
    public static final String ASOF_BEYOND_RETENTION = "ASOF_BEYOND_RETENTION";

    /** 400: {@code asOf} in the future, or combined with a parameter it cannot be combined with. */
    public static final String ASOF_INVALID = "ASOF_INVALID";

    /**
     * 410: {@code after} is below the purge horizon; the client must re-snapshot. Carries
     * {@code purgeHorizon} and {@code watermarkSeq}.
     */
    public static final String RESYNC_REQUIRED = "RESYNC_REQUIRED";

    /** 400: a streaming response would exceed {@code foresignal.api.stream.maxRows}. */
    public static final String RESULT_TOO_LARGE = "RESULT_TOO_LARGE";

    /** 503: a read query ran past {@code foresignal.api.query.statementTimeout}. */
    public static final String QUERY_TIMEOUT = "QUERY_TIMEOUT";

    /**
     * 503 (server-wide {@code maxClients}) or 429 (per-principal stream count, with
     * {@code Retry-After}): no further SSE stream may be opened now (prompt 015).
     */
    public static final String STREAM_LIMIT = "STREAM_LIMIT";

    /** 400: an event-stream topic that is unknown or not registered yet (prompt 015). */
    public static final String UNKNOWN_TOPIC = "UNKNOWN_TOPIC";

    private static final String TYPE_PREFIX = "https://foresignal.ai/problems/";

    private ErrorCodes() {}

    /**
     * The {@code type} URI for a code: {@code BUDGET_EXCEEDED} becomes
     * {@code https://foresignal.ai/problems/budget-exceeded}.
     */
    public static URI typeUri(String code) {
        return URI.create(TYPE_PREFIX + toKebabCase(code));
    }

    static String toKebabCase(String code) {
        return code == null ? "" : code.toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
