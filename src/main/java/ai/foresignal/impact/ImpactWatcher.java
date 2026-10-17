package ai.foresignal.impact;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.impact.hazard.HazardAdapters;
import ai.foresignal.impact.hazard.HazardReader;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.Subscription;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runs the impact projector when a hazard record version arrives (prompt 127 §4): a {@link ChangeBus} subscriber with
 * the durable cursor {@code impact.cursor} (a retention consumer through {@link ImpactCursorConsumer}), catching up on
 * its own virtual thread and never blocking ingest.
 *
 * <p>Each pass reads the hazard keys changed between the cursor and the watermark ({@link HazardReader#changes}) and
 * schedules each hazard {@link #DEBOUNCE} after its first unprocessed version, so a burst of versions (the bands of one
 * advisory) runs once. A due hazard is projected ({@link ImpactProjectionService#run}). The durable cursor stops just
 * before the first version of the oldest hazard still waiting, so a restart re-reads (and re-schedules) every hazard
 * not yet projected; re-projecting an unchanged hazard changes nothing.
 */
@Component
public class ImpactWatcher implements ChangeListener, DisposableBean, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(ImpactWatcher.class);

    /** The change-bus subscriber name. */
    public static final String SUBSCRIBER = "impact-projector";

    /** The debounce per hazard. */
    public static final Duration DEBOUNCE = Duration.ofMinutes(10);

    /** The seq chunk read per pass. */
    public static final long CHUNK = 50_000;

    /** The pause between passes. */
    static final long PAUSE_MS = 15_000;

    private final ChangeBus changeBus;
    private final AppSettingService settings;
    private final HazardReader reader;
    private final ImpactProjectionService projection;
    private final Clock clock;
    private final boolean autoStart;
    private final Map<String, Pending> pending = new LinkedHashMap<>();

    private volatile Subscription subscription;
    private volatile Thread thread;
    private volatile boolean running;
    private volatile long cursor = -1;
    private long scanned = -1;

    /**
     * A hazard waiting for its run.
     *
     * @param firstSeq its first unprocessed version
     * @param due when it runs
     */
    record Pending(long firstSeq, Instant due) {}

    /** Creates the watcher. */
    public ImpactWatcher(ChangeBus changeBus, AppSettingService settings, HazardReader reader,
            ImpactProjectionService projection, Clock clock,
            @Value("${foresignal.impact.auto-start:${foresignal.analytics.scheduled:true}}") boolean autoStart) {
        this.changeBus = changeBus;
        this.settings = settings;
        this.reader = reader;
        this.projection = projection;
        this.clock = clock;
        this.autoStart = autoStart;
    }

    /** Starts the subscriber and the loop once the application is ready (unless disabled). */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.IMPACT)
    public synchronized void start() {
        if (running || !autoStart) {
            return;
        }
        running = true;
        subscription = changeBus.subscribe(SUBSCRIBER, this, 256);
        thread = Thread.ofVirtual().name("impact-projector").start(this::loop);
    }

    /** Stops the watcher. */
    @Override
    public synchronized void destroy() {
        running = false;
        if (subscription != null) {
            subscription.close();
        }
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onBatch(ChangeBatch batch) {
        // The loop reads from the durable cursor.
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        // The loop catches up from the durable cursor.
    }

    /** The durable cursor. */
    public long cursor() {
        if (cursor < 0) {
            try {
                cursor = settings.get(SettingKeys.IMPACT_CURSOR, Long.class).orElse(0L);
            } catch (RuntimeException e) {
                cursor = 0;
            }
        }
        return cursor;
    }

    /** The hazards waiting, by key (tests). */
    public synchronized Map<String, Instant> pending() {
        Map<String, Instant> out = new LinkedHashMap<>();
        pending.forEach((k, v) -> out.put(k, v.due()));
        return out;
    }

    /** Restarts at 0 after the record history was reset; waiting hazards are forgotten. */
    @Override
    public synchronized void resetState() {
        cursor = 0;
        scanned = -1;
        pending.clear();
        settings.remove(SettingKeys.IMPACT_CURSOR);
    }

    private void loop() {
        while (running) {
            try {
                poll();
            } catch (RuntimeException e) {
                log.warn("Impact projector pass failed: {}", e.toString());
            }
            try {
                Thread.sleep(PAUSE_MS);
            } catch (InterruptedException e) {
                if (!running) {
                    return;
                }
            }
        }
    }

    /**
     * One pass: schedules the hazards changed up to the watermark, runs the due ones and stores the cursor.
     *
     * @return the hazards projected
     */
    public synchronized List<String> poll() {
        scan();
        return runDue(clock.instant());
    }

    /** Reads the changes between the scan position and the watermark and schedules their hazards. */
    public synchronized void scan() {
        long watermark = changeBus.lastPublishedSeq();
        long from = scanned >= 0 ? scanned : cursor();
        if (from > watermark) {
            from = 0;
        }
        Set<String> activeGauges = new LinkedHashSet<>();
        for (String key : projection.activeHazards()) {
            if (key.startsWith(HazardAdapters.NWPS_GAUGES + ":")) {
                activeGauges.add(key.substring(HazardAdapters.NWPS_GAUGES.length() + 1));
            }
        }
        Instant now = clock.instant();
        while (from < watermark) {
            long to = Math.min(from + CHUNK, watermark);
            for (HazardReader.Change change : reader.changes(from, to, activeGauges)) {
                pending.putIfAbsent(change.hazardKey(), new Pending(change.seq(), now.plus(DEBOUNCE)));
            }
            from = to;
        }
        scanned = from;
        storeCursor();
    }

    /**
     * Runs every hazard due at {@code now} and stores the cursor.
     *
     * @param now the time
     * @return the hazards projected
     */
    public synchronized List<String> runDue(Instant now) {
        List<String> due = new ArrayList<>();
        pending.forEach((k, v) -> {
            if (!v.due().isAfter(now)) {
                due.add(k);
            }
        });
        for (String key : due) {
            try {
                projection.run(key);
            } catch (RuntimeException e) {
                log.warn("Impact projection of {} failed: {}", key, e.toString());
            }
            pending.remove(key);
        }
        storeCursor();
        return due;
    }

    /** Projects every waiting hazard now, whatever its debounce (tests and an operator-triggered catch-up). */
    public synchronized List<String> flush() {
        scan();
        return runDue(Instant.MAX);
    }

    private void storeCursor() {
        long position = scanned >= 0 ? scanned : cursor();
        for (Pending p : pending.values()) {
            position = Math.min(position, p.firstSeq() - 1);
        }
        position = Math.max(0, position);
        if (position != cursor) {
            cursor = position;
            settings.put(SettingKeys.IMPACT_CURSOR, position);
        }
    }
}
