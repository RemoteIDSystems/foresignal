package ai.foresignal.notify.push;

import java.time.Duration;

/**
 * The outcome of one send (prompt 031): {@code SENT | GONE | RETRY(after) | REJECTED(reason) |
 * FAILED(reason)}.
 *
 * <ul>
 *   <li>{@link Sent}: accepted (Web Push 201/202).
 *   <li>{@link Gone}: the target no longer exists (404/410); it is deleted.
 *   <li>{@link Retry}: try again after a wait (429/503 {@code Retry-After}, or the local per-host
 *       rate limit, which does not count as an attempt).
 *   <li>{@link Rejected}: refused for good (413, other 4xx, an endpoint the address policy no longer
 *       allows); not retried.
 *   <li>{@link Failed}: a transient failure (5xx, I/O); retried on the 018 ladder.
 * </ul>
 */
public sealed interface PushResult {

    /** The push service's HTTP status, or null when no response was received. */
    Integer status();

    /**
     * Accepted.
     *
     * @param status the HTTP status
     */
    record Sent(Integer status) implements PushResult {}

    /**
     * The target is gone.
     *
     * @param status the HTTP status
     */
    record Gone(Integer status) implements PushResult {}

    /**
     * Try again later.
     *
     * @param after how long to wait
     * @param counted whether this counts as an attempt (false for the local rate limit)
     * @param status the HTTP status, or null
     * @param reason a short explanation
     */
    record Retry(Duration after, boolean counted, Integer status, String reason) implements PushResult {}

    /**
     * Refused for good.
     *
     * @param status the HTTP status, or null
     * @param code a stable code such as {@code URL_NOT_ALLOWED}, or null
     * @param reason a short explanation (never the full endpoint)
     */
    record Rejected(Integer status, String code, String reason) implements PushResult {}

    /**
     * A transient failure.
     *
     * @param status the HTTP status, or null
     * @param reason a short explanation (never the full endpoint)
     */
    record Failed(Integer status, String reason) implements PushResult {}
}
