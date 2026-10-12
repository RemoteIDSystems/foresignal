package ai.foresignal.system;

import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.trigger.TriggerEngine;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The catch-up signal ({@code /v1/system.catchUp}, the {@code catchup} health group, prompt 019).
 *
 * <p>{@code triggerLagSeq = ChangeBus.lastPublishedSeq() − trigger cursor}. Foresignal is
 * {@code catchingUp} while that lag exceeds {@value #LAG_THRESHOLD} seq, or while the ingest queue
 * has stayed above 80% of its capacity for more than a minute. Catching up is a separate signal from
 * readiness: a node that is behind still serves reads correctly.
 */
@Component
public class CatchUpMonitor {

    /** Lag above which the node is catching up. */
    public static final long LAG_THRESHOLD = 10_000;

    /** Queue fill ratio that counts as high. */
    static final double QUEUE_HIGH = 0.8;

    /** How long the queue must stay high. */
    static final Duration QUEUE_HIGH_FOR = Duration.ofMinutes(1);

    /**
     * A reading.
     *
     * @param triggerLagSeq seq published but not yet evaluated by the trigger engine
     * @param ingestQueueDepth commands waiting for the writer
     * @param catchingUp whether the node is catching up
     */
    public record CatchUp(long triggerLagSeq, int ingestQueueDepth, boolean catchingUp) {}

    private final ChangeBus changeBus;
    private final TriggerEngine engine;
    private final IngestQueue queue;
    private final Clock clock;
    private Instant queueHighSince;
    private volatile CatchUp current = new CatchUp(0, 0, false);

    /** Creates the monitor. */
    public CatchUpMonitor(ChangeBus changeBus, TriggerEngine engine, IngestQueue queue, Clock clock) {
        this.changeBus = changeBus;
        this.engine = engine;
        this.queue = queue;
        this.clock = clock;
    }

    /** Re-reads the inputs every 5 seconds. */
    @Scheduled(fixedDelay = 5_000, initialDelay = 5_000)
    public void refresh() {
        long lag = Math.max(0L, changeBus.lastPublishedSeq() - engine.cursor());
        evaluate(clock.instant(), lag, queue.depth(), queue.capacity());
    }

    /** Applies one reading; package-private so tests can drive the timing. */
    synchronized CatchUp evaluate(Instant now, long lag, int depth, int capacity) {
        boolean high = capacity > 0 && depth > QUEUE_HIGH * capacity;
        if (!high) {
            queueHighSince = null;
        } else if (queueHighSince == null) {
            queueHighSince = now;
        }
        boolean queueStuck = queueHighSince != null && Duration.between(queueHighSince, now).compareTo(QUEUE_HIGH_FOR) > 0;
        current = new CatchUp(lag, depth, lag > LAG_THRESHOLD || queueStuck);
        return current;
    }

    /** The latest reading, refreshed first. */
    public CatchUp snapshot() {
        refresh();
        return current;
    }

    /** The latest reading without refreshing (the health indicator). */
    public CatchUp last() {
        return current;
    }
}
