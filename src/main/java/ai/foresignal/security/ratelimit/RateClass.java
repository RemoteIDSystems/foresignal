package ai.foresignal.security.ratelimit;

import java.util.Locale;

/**
 * The rate-limit classes of {@link RateLimitSettings} (prompt 035). Each request falls into exactly
 * one class ({@link ClientRateLimitFilter#classify}); an anonymous caller is additionally charged to
 * {@link #ANONYMOUS}.
 */
public enum RateClass {

    /** Data reads that fall into no other class: {@code read}, 600/min by default. */
    READ,

    /** Map tiles and TileJSON: {@code tiles}, 1,200/min by default. */
    TILES,

    /** SSE opens: {@code stream.opensPerMinute}, 30/min, plus {@code stream.concurrent} open streams. */
    STREAM,

    /** Search, Ask, LLM-backed MCP tools and {@code /v1/sample}: {@code expensive}, 20/min. */
    EXPENSIVE,

    /** Every method other than GET, HEAD and OPTIONS: {@code write}, 120/min. */
    WRITE,

    /** Every request of a caller without a credential, per client IP: {@code anonymous}, 120/min. */
    ANONYMOUS,

    /** {@code /actuator/prometheus} per client IP, only when {@code metricsPerMinute} is set. */
    METRICS;

    /** The lowercase name used as the {@code class} tag of {@code foresignal.ratelimit.rejected}. */
    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
