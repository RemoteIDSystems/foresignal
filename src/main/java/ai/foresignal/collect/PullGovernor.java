package ai.foresignal.collect;

import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.CollectorSettings;
import ai.foresignal.settings.CollectorSettingsService;
import ai.foresignal.settings.SettingKeys;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * The global pull governor (prompt 150): a resizable pool of permits, one held by every polling fetch for as long
 * as it pulls from upstream, so at most {@code maxConcurrentPulls} ({@link CollectorSettings}) pulls run at once
 * across every source. It is the bound beneath the others: each source still runs on its own thread at its own
 * cadence, and {@link HostPacer} and {@link BudgetGovernor} still pace and cap each host and source.
 *
 * <p>{@link FetchExecutor#execute} acquires a permit immediately before the pull (scheduled and manual fetches
 * alike) and releases it in {@code finally}; {@link ProbeService} does the same for a polling probe. Streams are
 * not gated. The pool is fair, so a source waits its turn in arrival order; a start with many sources due fills
 * the pool and the rest queue, which is the whole of the cold-start pacing. A setting change resizes the pool live
 * (more permits are released at once; fewer take effect as running pulls finish, never interrupting one).
 */
@Component
public class PullGovernor {

    private static final Logger log = LoggerFactory.getLogger(PullGovernor.class);

    private final Permits permits;
    private final CollectorSettingsService settings;
    private int cap;

    /** Creates the governor at the stored {@code maxConcurrentPulls}. */
    @Autowired
    public PullGovernor(CollectorSettingsService settings) {
        this(settings.get().maxConcurrentPulls(), settings);
    }

    /** Creates a governor of {@code cap} permits with no setting behind it (tests). */
    public PullGovernor(int cap) {
        this(cap, null);
    }

    private PullGovernor(int cap, CollectorSettingsService settings) {
        this.cap = CollectorSettings.clamp(cap);
        this.permits = new Permits(this.cap);
        this.settings = settings;
    }

    /**
     * Waits for a permit and returns it; close it to release it.
     *
     * @throws InterruptedException when the waiting thread is interrupted (a stopping collector); no permit is held
     */
    public Permit acquire() throws InterruptedException {
        permits.acquire();
        return new Permit(permits);
    }

    /** The current cap. */
    public synchronized int cap() {
        return cap;
    }

    /** How many pulls hold a permit right now (may briefly exceed {@link #cap()} just after a shrink). */
    public synchronized int inUse() {
        return cap - permits.availablePermits();
    }

    /** How many fetches are waiting for a permit. */
    public int waiting() {
        return permits.getQueueLength();
    }

    /**
     * Resizes the pool to {@code newCap}, clamped to {@value CollectorSettings#MIN_PULLS}..{@value
     * CollectorSettings#MAX_PULLS}: a larger cap admits waiting pulls at once; a smaller one admits no new pull until
     * enough running ones finish.
     */
    public synchronized void resize(int newCap) {
        int target = CollectorSettings.clamp(newCap);
        int delta = target - cap;
        if (delta > 0) {
            permits.release(delta);
        } else if (delta < 0) {
            permits.reducePermits(-delta);
        }
        if (delta != 0) {
            log.info("Pull governor resized from {} to {} concurrent pulls", cap, target);
        }
        cap = target;
    }

    /** Resizes the pool when the {@link SettingKeys#COLLECTOR} setting changes. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (settings != null && SettingKeys.COLLECTOR.equals(event.key())) {
            resize(settings.get().maxConcurrentPulls());
        }
    }

    /** A held permit; {@link #close()} releases it once. */
    public static final class Permit implements AutoCloseable {

        private final Semaphore pool;
        private boolean released;

        private Permit(Semaphore pool) {
            this.pool = pool;
        }

        /** Releases the permit; a second call does nothing. */
        @Override
        public void close() {
            if (!released) {
                released = true;
                pool.release();
            }
        }
    }

    /** A fair semaphore whose {@code reducePermits} is reachable, so the pool can shrink. */
    private static final class Permits extends Semaphore {

        Permits(int permits) {
            super(permits, true);
        }

        @Override
        protected void reducePermits(int reduction) {
            super.reducePermits(reduction);
        }
    }
}
