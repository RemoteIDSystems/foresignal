/**
 * Per-client rate limits and the auth-failure backoff of the access perimeter (prompt 035):
 * {@link ai.foresignal.security.ratelimit.ClientRateLimitFilter} and
 * {@link ai.foresignal.security.ratelimit.ClientRateLimiter} (Bucket4j token buckets per token,
 * feed key or client IP and class), the operator setting
 * {@link ai.foresignal.security.ratelimit.RateLimitSettings} ({@code app_setting['ratelimits']}) and
 * {@link ai.foresignal.security.ratelimit.AuthFailureBackoff}.
 */
package ai.foresignal.security.ratelimit;
