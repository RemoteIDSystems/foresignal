package ai.foresignal.quality;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The nightly alert-quality job (prompt 115). Once per UTC day, after {@value #RUN_AFTER_HOUR}:00 UTC, it:
 *
 * <ol>
 *   <li>records {@code UNOPENED} for {@code alert.fired} notifications unread for 24 h ({@link AlertSignalStore});
 *   <li>recomputes {@code rule_quality_daily} for yesterday and today so far ({@link RuleQualityDailyStore});
 *   <li>generates and persists the tuning suggestions of every rule that fired in the last
 *       {@value #SUGGESTION_LOOKBACK_DAYS} days ({@link TuningWorkflowService#generate}).
 * </ol>
 *
 * <p>The tick runs every 15 minutes; {@code foresignal.quality.scheduled=false} turns it off (tests call
 * {@link #run(Instant)} directly).
 */
@Component
public class RuleQualityDailyJob {

    private static final Logger log = LoggerFactory.getLogger(RuleQualityDailyJob.class);

    /** The UTC hour after which the nightly run may start. */
    public static final int RUN_AFTER_HOUR = 2;

    /** The days of alerts the nightly suggestion pass looks back. */
    public static final int SUGGESTION_LOOKBACK_DAYS = 30;

    private final AlertSignalStore signals;
    private final RuleQualityDailyStore daily;
    private final TuningWorkflowService tuning;
    private final Clock clock;
    private final boolean scheduled;
    private volatile LocalDate lastRun;

    /** Creates the job. */
    public RuleQualityDailyJob(AlertSignalStore signals, RuleQualityDailyStore daily, TuningWorkflowService tuning,
            Clock clock, @Value("${foresignal.quality.scheduled:true}") boolean scheduled) {
        this.signals = signals;
        this.daily = daily;
        this.tuning = tuning;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** The scheduler tick: runs once per UTC day after {@value #RUN_AFTER_HOUR}:00. */
    @Scheduled(fixedDelayString = "PT15M", initialDelayString = "PT2M")
    public void tick() {
        if (!scheduled) {
            return;
        }
        Instant now = clock.instant();
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        if (today.equals(lastRun) || now.atZone(ZoneOffset.UTC).getHour() < RUN_AFTER_HOUR) {
            return;
        }
        try {
            run(now);
            lastRun = today;
        } catch (RuntimeException e) {
            log.warn("The nightly alert-quality job failed: {}", e.toString());
        }
    }

    /**
     * One run at {@code now}.
     *
     * @return what it did
     */
    public Result run(Instant now) {
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        int unopened = signals.recordUnopened(now.minus(Duration.ofDays(8)), now.minus(AlertSignalType.UNOPENED_AFTER));
        int rows = daily.aggregate(today.minusDays(1), now) + daily.aggregate(today, now);
        int generated = 0;
        for (UUID ruleId : daily.rulesSince(today.minusDays(SUGGESTION_LOOKBACK_DAYS))) {
            try {
                generated += tuning.generate(ruleId, now.minus(Duration.ofDays(SUGGESTION_LOOKBACK_DAYS))).size();
            } catch (RuntimeException e) {
                log.debug("No suggestions for rule {}: {}", ruleId, e.toString());
            }
        }
        return new Result(unopened, rows, generated);
    }

    /**
     * What one run did.
     *
     * @param unopened {@code UNOPENED} signals recorded
     * @param dailyRows {@code rule_quality_daily} rows written
     * @param suggestions suggestions generated (new or refreshed)
     */
    public record Result(int unopened, int dailyRows, int suggestions) {}
}
