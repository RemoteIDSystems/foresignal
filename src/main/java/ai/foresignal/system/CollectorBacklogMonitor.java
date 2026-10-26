package ai.foresignal.system;

import ai.foresignal.collect.PullGovernor;
import ai.foresignal.settings.ResourceThresholds;
import ai.foresignal.settings.ResourceThresholdsService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Raises {@code system.collector_backlog} when collection stays behind (prompt R2-014): every 30 s it reads the
 * trigger lag from {@link CatchUpMonitor} and the fetches waiting for a pull permit from {@link PullGovernor}, and when
 * either has stayed above the admin's {@code collectorBacklog} bound for {@code sustainedSeconds}, reports a
 * {@code warn} level to the {@link OperationalAlertService}; {@code system.collector_backlog_cleared} follows when
 * both are back under their bounds. Off when {@code foresignal.resources.scheduled} is false (tests call
 * {@link #evaluate(Instant, long, int)}).
 */
@Component
public class CollectorBacklogMonitor {

    /** The condition key. */
    static final String KEY = "collector_backlog";

    private final ObjectProvider<CatchUpMonitor> catchUp;
    private final ObjectProvider<PullGovernor> governor;
    private final OperationalAlertService alerts;
    private final ResourceThresholdsService thresholds;
    private final Clock clock;
    private final boolean scheduled;
    private Instant overSince;

    /**
     * Creates the monitor.
     *
     * @param catchUp the catch-up monitor (trigger lag)
     * @param governor the pull governor (waiting fetches)
     * @param alerts the operational alert path
     * @param thresholds the admin's thresholds
     * @param clock the clock
     * @param scheduled whether the 30-second tick runs
     */
    public CollectorBacklogMonitor(ObjectProvider<CatchUpMonitor> catchUp, ObjectProvider<PullGovernor> governor,
            OperationalAlertService alerts, ResourceThresholdsService thresholds, Clock clock,
            @Value("${foresignal.resources.scheduled:true}") boolean scheduled) {
        this.catchUp = catchUp;
        this.governor = governor;
        this.alerts = alerts;
        this.thresholds = thresholds;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** The 30-second tick. */
    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void tick() {
        if (!scheduled) {
            return;
        }
        CatchUpMonitor monitor = catchUp.getIfAvailable();
        PullGovernor pulls = governor.getIfAvailable();
        evaluate(clock.instant(), monitor == null ? 0 : monitor.last().triggerLagSeq(),
                pulls == null ? 0 : pulls.waiting());
    }

    /**
     * Applies one reading.
     *
     * @param now when it was taken
     * @param lagSeq the trigger lag
     * @param waiting the fetches waiting for a pull permit
     * @return {@code warn} while the backlog has lasted long enough, else {@code ok}
     */
    public synchronized String evaluate(Instant now, long lagSeq, int waiting) {
        ResourceThresholds.CollectorBacklog bound = thresholds.get().collectorBacklog();
        boolean lagOver = bound.triggerLagSeq() != null && lagSeq > bound.triggerLagSeq();
        boolean waitOver = bound.waitingPulls() != null && waiting > bound.waitingPulls();
        if (!lagOver && !waitOver) {
            overSince = null;
        } else if (overSince == null) {
            overSince = now;
        }
        boolean sustained = overSince != null
                && Duration.between(overSince, now).compareTo(Duration.ofSeconds(bound.sustainedSeconds())) >= 0;
        String level = sustained ? "warn" : (overSince == null ? "ok" : alerts.level(KEY));
        Instant since = overSince;
        alerts.report(KEY, level, (current, previous) -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("triggerLagSeq", lagSeq);
            data.put("waitingPulls", waiting);
            data.put("lagBound", bound.triggerLagSeq());
            data.put("waitingBound", bound.waitingPulls());
            data.put("since", since == null ? null : since.toString());
            if ("ok".equals(current)) {
                return new OperationalAlertService.Alert("system.collector_backlog_cleared", "info",
                        "The collector backlog has cleared",
                        "The trigger lag (" + lagSeq + " seq) and waiting fetches (" + waiting
                                + ") are back under their bounds.", "/v1/system", data);
            }
            return new OperationalAlertService.Alert("system.collector_backlog", "warning",
                    "Collection is falling behind",
                    "For at least " + bound.sustainedSeconds() + " s the trigger lag has been " + lagSeq
                            + " seq and " + waiting + " fetches have been waiting for a pull permit.",
                    "/v1/system", data);
        });
        return level;
    }
}
