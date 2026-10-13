package ai.foresignal.collect;

import java.time.Duration;

/**
 * The upstream refused this client rather than this request (prompt 062): an HTTP 451, or an HTTP 403
 * from a source whose descriptor sets {@code accessDeniedIsTerminal}.
 *
 * <p>{@code UpstreamClient} puts the source in a one-hour backoff before throwing it, so the source is
 * retried hourly and never faster, and {@link HealthEvaluator} reports it {@code failing} with
 * {@link #reason(int)}. Foresignal never answers a refusal by changing its User-Agent, adding browser
 * headers or rotating its egress: the Operator Guide section "adsb.lol access" describes the supported
 * route (feeding the upstream, and a key once one is published).
 */
public class UpstreamAccessDeniedException extends UpstreamHttpException {

    private static final long serialVersionUID = 1L;

    /** The backoff a refusal earns: one hour, the {@code BackoffRegistry} ceiling. */
    public static final Duration REFUSAL_BACKOFF = Duration.ofHours(1);

    /** The start of every refusal message, which {@link HealthEvaluator} recognizes in {@code last_error}. */
    public static final String REASON_PREFIX = "upstream refused this client";

    /** The source-specific reason (prompt 080), or null. */
    private final String detail;

    /**
     * Creates the exception.
     *
     * @param status 451 or 403
     * @param bodySnippet the truncated response body, possibly empty
     */
    public UpstreamAccessDeniedException(int status, String bodySnippet) {
        this(status, bodySnippet, null);
    }

    /**
     * Creates the exception with a source-specific reason (prompt 080).
     *
     * @param status 451 or 403
     * @param bodySnippet the truncated response body, possibly empty
     * @param detail the descriptor's {@code accessDeniedReasons} value the body matched, or null for the
     *     generic reason
     */
    public UpstreamAccessDeniedException(int status, String bodySnippet, String detail) {
        super(status, bodySnippet);
        this.detail = detail == null || detail.isBlank() ? null : detail;
    }

    /** The health reason of a refusal with {@code status}. */
    public static String reason(int status) {
        return REASON_PREFIX + " (HTTP " + status + "); see Operator Guide → adsb.lol access";
    }

    /**
     * The health reason of a refusal with {@code status} and a source-specific {@code detail} (prompt 080), for
     * example {@code upstream refused this client (HTTP 403): access approval required (contact airplanes.live)}.
     *
     * @param detail the reason, or null for {@link #reason(int)}
     */
    public static String reason(int status, String detail) {
        return detail == null || detail.isBlank() ? reason(status) : detailPrefix(status) + detail;
    }

    /** The text in front of a source-specific reason, which {@link HealthEvaluator} looks for in {@code last_error}. */
    public static String detailPrefix(int status) {
        return REASON_PREFIX + " (HTTP " + status + "): ";
    }

    /** The source-specific reason, or null. */
    public String getDetail() {
        return detail;
    }

    /** The health reason of this refusal. */
    @Override
    public String getMessage() {
        return reason(getStatus(), detail);
    }
}
