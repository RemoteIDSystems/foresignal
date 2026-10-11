package ai.elaro.collect;

import ai.elaro.source.spi.AdapterException;

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

    private final int status;
    private final String bodySnippet;

    public UpstreamHttpException(int status, String bodySnippet) {
        super("Upstream returned HTTP " + status + (bodySnippet.isEmpty() ? "" : ": " + bodySnippet));
        this.status = status;
        this.bodySnippet = bodySnippet;
    }

    public int getStatus() {
        return status;
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
