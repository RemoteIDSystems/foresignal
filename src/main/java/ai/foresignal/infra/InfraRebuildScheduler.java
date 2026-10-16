package ai.foresignal.infra;

import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * When the infrastructure graph is rebuilt (prompt 104): on request ({@code POST /v1/infra/rebuild},
 * answered 202 at once), after an import of a mapped source (debounced: once no mapped source has
 * changed for {@value #DEBOUNCE_MINUTES} minutes), and weekly.
 *
 * <p>A request records a {@code running} build (or returns the one already running) and hands it to
 * a single worker thread. The build row is committed before the worker starts, because
 * {@link InfraGraphBuilder#start} commits its own transaction before this class submits the work.
 */
@Component
public class InfraRebuildScheduler implements ChangeListener {

    /** The quiet period after an import of a mapped source before a rebuild, minutes. */
    public static final long DEBOUNCE_MINUTES = 10;

    private static final Logger log = LoggerFactory.getLogger(InfraRebuildScheduler.class);

    private final InfraGraphBuilder builder;
    private final ChangeBus changeBus;
    private final Clock clock;
    private final boolean autoRebuild;
    private final AtomicReference<Instant> lastMappedChange = new AtomicReference<>();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "foresignal-infra-build");
        t.setDaemon(true);
        return t;
    });
    private volatile Set<String> mapped = Set.of();
    private volatile AutoCloseable subscription;

    /** Creates the scheduler. */
    public InfraRebuildScheduler(InfraGraphBuilder builder, ChangeBus changeBus, Clock clock,
            @Value("${foresignal.infra.auto-rebuild:true}") boolean autoRebuild) {
        this.builder = builder;
        this.changeBus = changeBus;
        this.clock = clock;
        this.autoRebuild = autoRebuild;
    }

    /** Requests a rebuild: the running build when there is one, else a new build queued on the worker. */
    public InfraBuildEntity requestRebuild() {
        return startRebuild().build();
    }

    /**
     * Requests a rebuild and says whether it started one.
     *
     * @return the new build queued on the worker ({@code created} true), or the build already running
     *     ({@code created} false, nothing queued)
     */
    public InfraGraphBuilder.Started startRebuild() {
        InfraGraphBuilder.Started started = builder.start();
        if (started.created()) {
            long id = started.build().getId();
            worker.submit(() -> {
                try {
                    builder.execute(id);
                } catch (RuntimeException e) {
                    log.warn("infrastructure build {} failed: {}", id, e.toString());
                }
            });
        }
        return started;
    }

    /** Subscribes to committed batches once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() {
        if (!autoRebuild || subscription != null) {
            return;
        }
        mapped = builder.mappedSources();
        subscription = changeBus.subscribe("infra-rebuild", this, 256);
    }

    @Override
    public void onBatch(ChangeBatch batch) {
        if (batch != null && mapped.contains(batch.sourceId())) {
            lastMappedChange.set(clock.instant());
        }
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        // Batches were dropped: one of them may have been a mapped source.
        lastMappedChange.set(clock.instant());
    }

    /** Rebuilds once mapped sources have been quiet for the debounce period. */
    @Scheduled(fixedDelayString = "${foresignal.infra.debounce-check:60s}", initialDelayString = "60s")
    public void rebuildAfterImports() {
        if (!autoRebuild) {
            return;
        }
        Instant changed = lastMappedChange.get();
        if (changed != null && !clock.instant().isBefore(changed.plus(Duration.ofMinutes(DEBOUNCE_MINUTES)))
                && lastMappedChange.compareAndSet(changed, null)) {
            requestRebuild();
        }
    }

    /** The weekly rebuild. */
    @Scheduled(cron = "${foresignal.infra.rebuild-cron:0 30 4 * * SUN}", zone = "UTC")
    public void weekly() {
        if (autoRebuild) {
            requestRebuild();
        }
    }

    /** The last change of a mapped source not yet rebuilt, or null (tests). */
    Instant pendingChange() {
        return lastMappedChange.get();
    }

    /** Stops the worker and the subscription. */
    @jakarta.annotation.PreDestroy
    void shutdown() {
        try {
            if (subscription != null) {
                subscription.close();
            }
        } catch (Exception e) {
            log.debug("closing the infra-rebuild subscription failed: {}", e.toString());
        }
        worker.shutdownNow();
        try {
            worker.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
