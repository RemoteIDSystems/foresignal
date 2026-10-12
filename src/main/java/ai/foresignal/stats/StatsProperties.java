package ai.foresignal.stats;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Stats and rollup mechanics ({@code foresignal.stats.*}, prompt 038). Operator choices (which sources
 * are rolled up, retention of the rollups) are settings; these are bootstrap values.
 *
 * @param cellRes the H3 resolution of rollup cells (default 4, about 1,770 km² per cell); grids may
 *     aggregate to coarser resolutions, never finer
 * @param backfill how far back the first start rolls up (default 30 days), bounded by what retention
 *     still holds
 * @param scheduled whether the rollup job runs on its own; off in the test profile
 * @param interval the delay between scheduled runs (default 5 min)
 * @param closeDelay how long after its end an hour is considered closed (default 2 min)
 * @param maxHoursPerRun how many hours one run rolls up at most while catching up (default 6)
 * @param queryTimeout the statement timeout of every stats query (default 10 s)
 */
@ConfigurationProperties("foresignal.stats")
public record StatsProperties(
        @DefaultValue("4") int cellRes,
        @DefaultValue("30d") Duration backfill,
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("5m") Duration interval,
        @DefaultValue("2m") Duration closeDelay,
        @DefaultValue("6") int maxHoursPerRun,
        @DefaultValue("10s") Duration queryTimeout) {

    /** The finest cell resolution H3 offers. */
    public static final int MAX_CELL_RES = 15;

    /** Clamps and fills the values. */
    public StatsProperties {
        cellRes = Math.max(0, Math.min(MAX_CELL_RES, cellRes));
        backfill = backfill == null ? Duration.ofDays(30) : backfill;
        interval = interval == null ? Duration.ofMinutes(5) : interval;
        closeDelay = closeDelay == null ? Duration.ofMinutes(2) : closeDelay;
        maxHoursPerRun = maxHoursPerRun < 1 ? 6 : maxHoursPerRun;
        queryTimeout = queryTimeout == null ? Duration.ofSeconds(10) : queryTimeout;
    }

    /** The shipped defaults. */
    public static StatsProperties defaults() {
        return new StatsProperties(4, null, true, null, null, 6, null);
    }
}
