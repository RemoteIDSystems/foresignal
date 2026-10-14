package ai.foresignal.collect;

import ai.foresignal.source.spi.AdapterException;

/**
 * The upstream answered with a status of 400 or more.
 *
 * <p>Carries a truncated body snippet, which is usually where an upstream explains itself — an
 * expired key, a malformed bbox, a quota message — and is the difference between "it returned 403"
 * and a diagnosis.
 */
public class UpstreamHttpException extends AdapterException {

    private static final long serialVersionUID = 1L;

    /** Enough to carry an upstream's error message, small enough for a log line. */
    public static final int MAX_SNIPPET_LENGTH = 500;

    /**
     * The reason of a response that is a bot-mitigation challenge page (a Cloudflare {@code cf-mitigated} header, or
     * an HTML page where the API documents JSON), prompt 082: {@code openmhz-systems}. Foresignal never solves a
     * challenge or retries with another User-Agent; the source stays failing until the upstream admits API clients.
     */
    public static final String REASON_BLOCKED_CHALLENGE = "BLOCKED_CHALLENGE";

    private final int status;
    private final String bodySnippet;
    private final String reason;

    public UpstreamHttpException(int status, String bodySnippet) {
        this(status, bodySnippet, null);
    }

    /**
     * An error status with a machine-readable reason (prompt 082).
     *
     * @param status the HTTP status
     * @param bodySnippet the truncated body, possibly empty
     * @param reason a reason such as {@link #REASON_BLOCKED_CHALLENGE}, or null
     */
    public UpstreamHttpException(int status, String bodySnippet, String reason) {
        super("Upstream returned HTTP " + status + (reason == null ? "" : " (" + reason + ")")
                + (bodySnippet == null || bodySnippet.isEmpty() ? "" : ": " + bodySnippet));
        this.status = status;
        this.bodySnippet = bodySnippet == null ? "" : bodySnippet;
        this.reason = reason;
    }

    /** The machine-readable reason, such as {@link #REASON_BLOCKED_CHALLENGE}, or null. */
    public String getReason() {
        return reason;
    }

    /** True when the response was a bot-mitigation challenge (prompt 082). */
    public boolean isBlockedChallenge() {
        return REASON_BLOCKED_CHALLENGE.equals(reason);
    }

    public int getStatus() {
        return status;
    }

    @Override
    public java.util.OptionalInt httpStatus() {
        return java.util.OptionalInt.of(status);
    }

    public String getBodySnippet() {
        return bodySnippet;
    }

    /** True for statuses that mean "ask again later" rather than "this request was wrong". */
    public boolean isThrottle() {
        return status == 429 || status == 503;
    }

    /** Truncates {@code body} to {@value #MAX_SNIPPET_LENGTH} characters. */
    public static String snippet(String body) {
        if (body == null) {
            return "";
        }
        String trimmed = body.strip();
        return trimmed.length() <= MAX_SNIPPET_LENGTH
                ? trimmed
                : trimmed.substring(0, MAX_SNIPPET_LENGTH) + "… (truncated)";
    }
}
