package ai.foresignal.testsupport.raster;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.derive.RasterFrameEvent;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A test-only DERIVED source for the prompt 052 runtime ITs ({@value #ID}): it listens to the changes of
 * {@code test-standard} and to frames of the synthetic observation product, records every hook call, can be
 * held at a gate (to fill its inbox) or told to fail, and emits one counter record per hook so the
 * {@code fetch_log} and version counts can be checked. Lives under {@code testsupport}, so the built-in adapter
 * classification of {@code FamilyDefaultsTest} never sees it.
 */
public class TestDerivedGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "test-derived";

    /** The record kind. */
    public static final String KIND = "test-derived-count";

    private final SourceDescriptor descriptor = SourceDescriptor.builder(ID)
            .name("Test derived source")
            .description("Counts its inputs")
            .kind(KIND)
            .licence("test")
            .mode(Mode.DERIVED)
            .defaultBudget(Budget.of(1, 1))
            .zeroResultsNormal(true)
            .build();

    /** Reconcile calls. */
    public final AtomicInteger reconciles = new AtomicInteger();

    /** Change summaries received. */
    public final AtomicInteger changes = new AtomicInteger();

    /** Change batches received. */
    public final AtomicInteger batches = new AtomicInteger();

    /** Frame events received, in order. */
    public final List<RasterFrameEvent> frames = new CopyOnWriteArrayList<>();

    /** Ticks received. */
    public final AtomicInteger ticks = new AtomicInteger();

    private volatile CountDownLatch gate;
    private volatile CountDownLatch entered;
    private volatile boolean failNext;

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        return new DerivedInputs(Set.of(TestIngestSources.STANDARD), Set.of(), Set.of(SyntheticRasterAdapter.OBS), null,
                false);
    }

    /** Clears the counters and opens the gate. */
    public void reset() {
        reconciles.set(0);
        changes.set(0);
        batches.set(0);
        frames.clear();
        ticks.set(0);
        failNext = false;
        open();
    }

    /** Holds the next hook call at the gate until {@link #open()}. */
    public void hold() {
        entered = new CountDownLatch(1);
        gate = new CountDownLatch(1);
    }

    /** Waits until a held hook call has reached the gate. */
    public boolean awaitHeld(long seconds) throws InterruptedException {
        CountDownLatch e = entered;
        return e == null || e.await(seconds, TimeUnit.SECONDS);
    }

    /** Releases a held hook call. */
    public void open() {
        CountDownLatch g = gate;
        gate = null;
        if (g != null) {
            g.countDown();
        }
    }

    /** Makes the next hook call throw. */
    public void failNext() {
        failNext = true;
    }

    private void step() {
        CountDownLatch g = gate;
        if (g != null) {
            CountDownLatch e = entered;
            if (e != null) {
                e.countDown();
            }
            try {
                g.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        if (failNext) {
            failNext = false;
            throw new IllegalStateException("test failure");
        }
    }

    private static void count(DerivedContext ctx, String what, int n, Instant at) {
        ctx.emit(NormalizedRecord.builder(what, KIND).observedAt(at).put("count", n).build());
    }

    @Override
    public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        step();
        count(ctx, "reconcile", reconciles.incrementAndGet(), ctx.now());
    }

    @Override
    public void onChanges(ChangeBatch batch, DerivedContext ctx) {
        step();
        batches.incrementAndGet();
        count(ctx, "changes", changes.addAndGet(batch.changes().size()), ctx.now());
    }

    @Override
    public void onFrame(RasterFrameEvent event, DerivedContext ctx) {
        step();
        frames.add(event);
        count(ctx, "frames", frames.size(), event.observedAt());
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        step();
        ticks.incrementAndGet();
    }
}
