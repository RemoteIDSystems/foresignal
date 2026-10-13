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

    /**
     * Creates the exception.
     *
     * @param status 451 or 403
     * @param bodySnippet the truncated response body, possibly empty
     */
    public UpstreamAccessDeniedException(int status, String bodySnippet) {
        super(status, bodySnippet);
    }

    /** The health reason of a refusal with {@code status}. */
    public static String reason(int status) {
        return REASON_PREFIX + " (HTTP " + status + "); see Operator Guide → adsb.lol access";
    }

    /** The health reason of this refusal. */
    @Override
    public String getMessage() {
        return reason(getStatus());
    }
}
