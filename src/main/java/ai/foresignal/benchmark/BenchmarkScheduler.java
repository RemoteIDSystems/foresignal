package ai.foresignal.benchmark;

import ai.foresignal.api.error.ConflictException;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The benchmark schedule (prompt 123), UTC: the synthetic suite every Sunday at 03:00 and at startup when a component's
 * version differs from the version last benchmarked. The monthly null-case sampling and historical suite are a step of
 * prompt 128's monthly maintenance.
 * {@code foresignal.analytics.scheduled=false} turns all of it off (tests call the services directly).
 */
@Component
public class BenchmarkScheduler {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkScheduler.class);

    private final BenchmarkService service;
    private final HistoricalSuite historical;
    private final BenchmarkGate gate;
    private final Clock clock;
    private final boolean scheduled;

    /** Creates the scheduler. */
    public BenchmarkScheduler(BenchmarkService service, HistoricalSuite historical, BenchmarkGate gate, Clock clock,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.service = service;
        this.historical = historical;
        this.gate = gate;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** The weekly synthetic suite. */
    @Scheduled(cron = "0 0 3 * * SUN", zone = "UTC")
    public void weekly() {
        if (scheduled) {
            submit("synthetic", "schedule");
        }
    }

    /** At startup, a synthetic run when a component's version changed since it was last benchmarked. */
    @EventListener(ApplicationReadyEvent.class)
    public void versionCheck() {
        if (!scheduled) {
            return;
        }
        try {
            List<String> changed = gate.versionChanged();
            if (!changed.isEmpty()) {
                log.info("Benchmark: component versions changed since the last run ({}); running the synthetic suite",
                        changed);
                submit("synthetic", "version-change");
            }
        } catch (RuntimeException e) {
            log.warn("The benchmark version check failed: {}", e.toString());
        }
    }

    /** Queues a run unless one is in progress. */
    boolean submit(String suite, String trigger) {
        try {
            service.submit(new BenchmarkInputs.RunRequest(suite, BenchmarkInputs.COMPONENTS, null), "scheduler",
                    trigger);
            return true;
        } catch (ConflictException e) {
            log.info("Benchmark {} run skipped: {}", suite, e.getMessage());
            return false;
        } catch (RuntimeException e) {
            log.warn("Could not queue the {} benchmark run: {}", suite, e.toString());
            return false;
        }
    }
}
