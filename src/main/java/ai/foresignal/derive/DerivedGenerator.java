package ai.foresignal.derive;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.stream.ChangeBatch;
import java.time.Instant;
import java.util.List;

/**
 * A DERIVED source (prompt 052; Weather-Imagery-Design §2.1 ⑥, §7.1): a {@link SourceAdapter} whose
 * descriptor declares {@code Mode.DERIVED} and no {@code recordTtl} (the registry rejects one), and
 * whose records are computed from data Foresignal already holds. {@code ctx.http()} throws for it.
 *
 * <p>The {@link DerivedRunner} calls the hooks one at a time on the source's own virtual thread, so an
 * implementation may keep plain mutable state. Every hook receives a fresh {@link DerivedContext};
 * whatever it emits becomes one ingest batch. An exception fails the invocation like a failed fetch.
 *
 * <p>Implementations keep the derived-record rules of {@code ai.foresignal.derive}: emit on change,
 * {@code observedAt} from the input, explicit tombstones only, no heartbeats.
 */
public interface DerivedGenerator extends SourceAdapter {

    /**
     * What the generator listens to with {@code params}. Also read at registry start to build the
     * derived-input graph, where a cycle fails startup ({@link DerivedGraph}).
     *
     * @param params the source's effective parameters
     */
    DerivedInputs inputs(ValidatedParams params);

    /**
     * Committed changes matching {@link DerivedInputs#matchesChange}; only the matching changes are in
     * the batch.
     */
    default void onChanges(ChangeBatch batch, DerivedContext ctx) {}

    /** A frame of one of {@link DerivedInputs#products()} was stored, or a forecast run completed. */
    default void onFrame(RasterFrameEvent event, DerivedContext ctx) {}

    /** The periodic tick of {@link DerivedInputs#tick()}. */
    default void onTick(Instant now, DerivedContext ctx) {}

    /**
     * Rebuilds state from the source's own heads: at startup, when the source is enabled, after a
     * change-bus gap or an inbox overflow, and after a place edit when {@link DerivedInputs#places()}.
     * Tombstones the heads whose subject no longer exists (a deleted place); never tombstones because
     * input is stale.
     *
     * @param ownHeads every head of this source, tombstones included
     */
    void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx);
}
