package ai.elaro.store;

/**
 * Aggregated usage for one source over a window.
 *
 * @param requests requests actually sent upstream
 * @param bytes response bytes received
 * @param errors requests that failed
 * @param refused requests the budget governor declined to send
 */
public record UsageTotals(long requests, long bytes, long errors, long refused) {}
