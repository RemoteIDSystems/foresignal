package ai.foresignal.collect;

/**
 * A live snapshot of the {@link PullGovernor} (prompt 160; OpenAPI schema {@code CollectorStatus}), served by
 * {@code GET /v1/settings/collector/status} so the admin app can show the governor working, not only its setting.
 *
 * @param cap the governor's current cap: the {@code maxConcurrentPulls} in force
 * @param inUse how many pulls hold a permit right now (may briefly exceed {@code cap} just after a shrink)
 * @param waiting how many fetches are queued for a permit
 */
public record CollectorStatus(int cap, int inUse, int waiting) {}
