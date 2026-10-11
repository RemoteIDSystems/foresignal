package ai.foresignal.ingest;

/**
 * Lets trigger rules force a version when their predicate flips (Core-Data-Model §11, invariant
 * I8).
 *
 * <p>The writer reads {@link #snapshot()} once at the start of each batch and asks it, per
 * observation, whether any applicable rule's predicate differs between the <strong>persisted</strong>
 * state ({@code h.payload} paths, {@code h.persisted_geom}) and the candidate (the incoming snapshot
 * after carry-forward). Comparing against persisted state is what makes a new rule converge on each
 * entity's next observation with no reconcile pass.
 *
 * <p>{@link NoopPersistGate} is the default bean; prompt 017 adds {@code TriggerPersistGate} as
 * {@code @Primary}. Implementations hold the snapshot in an {@code AtomicReference} and swap it
 * whole when rules change.
 */
public interface PersistGate {

    /** The current rule snapshot; never null. */
    GateSnapshot snapshot();
}
