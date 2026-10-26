package ai.foresignal.retention;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.system.OperationalAlertService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The database-size cap of cap history (prompt R2-015): keeps {@code pg_database_size} under
 * {@code retention.maxDbGb} by stepping the data-history window down, oldest first.
 *
 * <p>Postgres reuses the space that deleted rows free but returns it to the file system only when a partition is
 * dropped or a table is rewritten, so the database size often does not fall right after a delete. Stepping down
 * until the size fell would therefore delete far more history than needed. The cap instead works as a ratchet:
 *
 * <ol>
 *   <li>Each run first applies the window it last stepped down to ({@link #tighten}).
 *   <li>After the run, when the database is over the cap <em>and</em> larger than at the previous run (or it has no
 *       previous measurement), it halves the window (whole days, never below {@link #FLOOR_DAYS}) and prunes again
 *       through the {@link RetentionCapPruner}, re-measuring after each step. It keeps stepping only while a step
 *       actually shrank the database (a partition drop), and stops at the cap.
 *   <li>Over the cap but not growing, it holds the window: the freed space is being reused.
 *   <li>Well under the cap (below {@link #RELAX_RATIO} of it), it doubles the window back toward the configured one.
 *       That stops further pruning; nothing deleted comes back.
 * </ol>
 *
 * <p>When the window is at the floor and the database is still over the cap, or {@code overwriteOldest} is off, it
 * raises the operational alert {@code system.retention_cap_unmet} (prompt R2-014) and deletes nothing more. The window
 * never drops below the floor, so an entity reported within the last {@link #FLOOR_DAYS} day keeps its head and live
 * row; heads of entities not reported within the window are purged as under any window (invariant I7).
 * {@code system.retention_cap_met} clears the alert.
 */
@Component
public class RetentionDbCap {

    private static final Logger log = LoggerFactory.getLogger(RetentionDbCap.class);

    /** The shortest window the cap steps down to. */
    public static final int FLOOR_DAYS = 1;

    /** At most this many steps in one run. */
    static final int MAX_STEPS = 8;

    /** Below this share of the cap the window steps back up. */
    static final double RELAX_RATIO = 0.8;

    /** The operational alert key. */
    static final String ALERT = "retention_cap";

    /**
     * The persisted working state.
     *
     * @param windowDays the window stepped down to, or null when the configured window applies
     * @param lastSizeBytes the database size measured at the end of the last run, or null
     * @param updatedAt when it was written
     */
    public record State(Integer windowDays, Long lastSizeBytes, Instant updatedAt) {

        /** No tightening and no measurement. */
        public static final State NONE = new State(null, null, null);
    }

    /**
     * What the cap did in one run; {@code RetentionRunResult.dbCap}.
     *
     * @param capBytes the cap
     * @param sizeBeforeBytes the database size after the window pass, before any cap step
     * @param sizeAfterBytes the database size at the end
     * @param bytesReclaimed how far the size fell during the cap steps
     * @param windowDays the history window in force at the end, or null for the configured windows
     * @param steps cap steps taken this run
     * @param action {@code under} (within the cap), {@code stepped} (stepped down), {@code holding} (over the cap
     *     and not growing), {@code floor} (at the floor and still over) or {@code disabled} ({@code overwriteOldest}
     *     off)
     * @param deleted rows deleted by the cap steps, by table
     */
    public record Outcome(long capBytes, long sizeBeforeBytes, long sizeAfterBytes, long bytesReclaimed,
            Integer windowDays, int steps, String action, Map<String, Long> deleted) {

        public Outcome {
            deleted = deleted == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(deleted));
        }

        /** True when the database ended within the cap. */
        public boolean met() {
            return sizeAfterBytes <= capBytes;
        }
    }

    private final AppSettingService settings;
    private final RetentionResolver resolver;
    private final ObjectProvider<OperationalAlertService> alerts;
    private final LongSupplier databaseSize;
    private final Clock clock;

    /**
     * Creates the cap.
     *
     * @param settings the settings store (the working state)
     * @param resolver the retention resolver (the configured windows)
     * @param alerts the operational alert path (prompt R2-014)
     * @param jdbc the database, measured with {@code pg_database_size}
     * @param clock the clock
     */
    @org.springframework.beans.factory.annotation.Autowired
    public RetentionDbCap(AppSettingService settings, RetentionResolver resolver,
            ObjectProvider<OperationalAlertService> alerts, JdbcTemplate jdbc, Clock clock) {
        this(settings, resolver, alerts,
                () -> jdbc.queryForObject("SELECT pg_database_size(current_database())", Long.class), clock);
    }

    /**
     * Creates the cap with a size probe (tests).
     *
     * @param settings the settings store
     * @param resolver the retention resolver
     * @param alerts the operational alert path
     * @param databaseSize measures the database
     * @param clock the clock
     */
    RetentionDbCap(AppSettingService settings, RetentionResolver resolver,
            ObjectProvider<OperationalAlertService> alerts, LongSupplier databaseSize, Clock clock) {
        this.settings = settings;
        this.resolver = resolver;
        this.alerts = alerts;
        this.databaseSize = databaseSize;
        this.clock = clock;
    }

    /** The persisted working state. */
    public State state() {
        return settings.getOrDefault(SettingKeys.RETENTION_DB_CAP, State.class, () -> State.NONE);
    }

    /**
     * The settings a run applies: {@code configured}, with {@code maxDays} lowered to the window the cap stepped
     * down to when the size cap is on and has stepped down. With the size cap off, any working state is cleared.
     *
     * @param configured the stored settings
     * @return the settings to run with
     */
    public RetentionSettings tighten(RetentionSettings configured) {
        OptionalLong cap = configured.capBytes();
        State state = state();
        if (cap.isEmpty()) {
            if (state.windowDays() != null || state.lastSizeBytes() != null) {
                settings.remove(SettingKeys.RETENTION_DB_CAP);
            }
            return configured;
        }
        if (state.windowDays() == null) {
            return configured;
        }
        int base = baseDays(configured);
        return state.windowDays() < base ? configured.withMaxDays(state.windowDays()) : configured;
    }

    /**
     * Applies the size cap after a run's window pass.
     *
     * @param configured the stored settings
     * @param pruner how to prune to a shorter window
     * @param steps runs every retention step under given settings
     * @return what it did, or null when the size cap is off
     */
    public Outcome enforce(RetentionSettings configured, RetentionCapPruner pruner,
            Function<RetentionSettings, RetentionRun> steps) {
        OptionalLong capOpt = configured.capBytes();
        if (capOpt.isEmpty()) {
            return null;
        }
        long cap = capOpt.getAsLong();
        State state = state();
        int base = baseDays(configured);
        Integer window = state.windowDays() != null && state.windowDays() < base ? state.windowDays() : null;
        long before = databaseSize.getAsLong();
        long size = before;
        int taken = 0;
        Map<String, Long> deleted = new LinkedHashMap<>();
        String action;
        if (size <= cap) {
            action = "under";
            if (window != null && size < RELAX_RATIO * cap) {
                int relaxed = window * 2;
                window = relaxed >= base ? null : relaxed;
            }
        } else if (!Boolean.TRUE.equals(configured.overwriteOldest())) {
            action = "disabled";
        } else if (state.lastSizeBytes() != null && size <= state.lastSizeBytes()) {
            action = "holding";
        } else {
            action = "stepped";
            int current = window == null ? base : window;
            while (taken < MAX_STEPS) {
                int next = Math.max(FLOOR_DAYS, (current + 1) / 2);
                if (next >= current) {
                    action = "floor";
                    break;
                }
                RetentionRun run = pruner.prune(configured.withMaxDays(next), steps);
                if (run != null) {
                    run.deletedByTable().forEach((table, n) -> deleted.merge(table, n, Long::sum));
                }
                taken++;
                current = next;
                window = next;
                long measured = databaseSize.getAsLong();
                boolean shrank = measured < size;
                size = measured;
                if (size <= cap || !shrank) {
                    break;
                }
            }
            if (size > cap && current <= FLOOR_DAYS) {
                action = "floor";
            }
        }
        settings.put(SettingKeys.RETENTION_DB_CAP, new State(window, size, clock.instant()));
        Outcome outcome = new Outcome(cap, before, size, Math.max(0, before - size), window, taken, action, deleted);
        report(outcome);
        if (taken > 0) {
            log.warn("Retention size cap: the database was {} bytes over its {}-byte cap; stepped the history window"
                    + " down to {} days in {} steps, now {} bytes", before - cap, cap, window, taken, size);
        }
        return outcome;
    }

    /**
     * The configured history window in whole days, which the cap steps down from: {@code maxDays} when set, else the
     * largest window in force.
     *
     * @param configured the stored settings
     * @return the window in days, at least {@link #FLOOR_DAYS}
     */
    int baseDays(RetentionSettings configured) {
        if (configured.capWindow().isPresent()) {
            return Math.max(FLOOR_DAYS, configured.maxDays());
        }
        Duration largest = resolver.largestWindow();
        long days = (largest.toHours() + 23) / 24;
        return (int) Math.max(FLOOR_DAYS, Math.min(Integer.MAX_VALUE, days));
    }

    private void report(Outcome outcome) {
        OperationalAlertService path = alerts.getIfAvailable();
        if (path == null) {
            return;
        }
        boolean unmet = !outcome.met() && ("floor".equals(outcome.action()) || "disabled".equals(outcome.action()));
        path.report(ALERT, unmet ? "warn" : (outcome.met() ? "ok" : path.level(ALERT)), (level, previous) -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("capBytes", outcome.capBytes());
            data.put("sizeBytes", outcome.sizeAfterBytes());
            data.put("windowDays", outcome.windowDays());
            data.put("action", outcome.action());
            if ("ok".equals(level)) {
                return new OperationalAlertService.Alert("system.retention_cap_met", "info",
                        "The database is back within its size cap",
                        "The database is " + outcome.sizeAfterBytes() + " bytes, within its cap of "
                                + outcome.capBytes() + " bytes.", "/v1/settings/retention", data);
            }
            String why = "disabled".equals(outcome.action())
                    ? "overwriteOldest is off, so retention does not remove history for the cap."
                    : "The history window is already at its " + FLOOR_DAYS + "-day floor; retention will not shorten"
                            + " history further for the cap. Space freed by deletes is reused but returned to the"
                            + " file system only by a partition drop or VACUUM FULL.";
            return new OperationalAlertService.Alert("system.retention_cap_unmet", "warning",
                    "The database is over its size cap",
                    "The database is " + outcome.sizeAfterBytes() + " bytes, over its cap of " + outcome.capBytes()
                            + " bytes. " + why, "/v1/settings/retention", data);
        });
    }
}
