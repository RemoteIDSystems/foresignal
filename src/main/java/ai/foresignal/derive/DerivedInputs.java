package ai.foresignal.derive;

import java.time.Duration;
import java.util.Set;
import java.util.TreeSet;

/**
 * What a {@link DerivedGenerator} listens to (prompt 052).
 *
 * <p>A change batch reaches the generator when it holds at least one change whose source is in
 * {@code sources} (any source when empty) <em>and</em> whose kind is in {@code kinds} (any kind when
 * empty), provided at least one of the two sets is non-empty; with both empty the generator gets no
 * change batches. Changes made by the generator's own source never reach it.
 *
 * @param sources source ids whose committed changes the generator receives
 * @param kinds record kinds whose committed changes the generator receives
 * @param products raster product ids whose frame events the generator receives
 * @param tick the period of {@link DerivedGenerator#onTick}, or null for none
 * @param places true when place edits (created, moved, deleted) should trigger a reconcile
 */
public record DerivedInputs(Set<String> sources, Set<String> kinds, Set<String> products, Duration tick,
        boolean places) {

    /** Copies the sets into sorted, immutable ones and checks the tick. */
    public DerivedInputs {
        sources = sources == null ? Set.of() : Set.copyOf(new TreeSet<>(sources));
        kinds = kinds == null ? Set.of() : Set.copyOf(new TreeSet<>(kinds));
        products = products == null ? Set.of() : Set.copyOf(new TreeSet<>(products));
        if (tick != null && (tick.isNegative() || tick.isZero())) {
            throw new IllegalArgumentException("A derived tick must be positive, was " + tick);
        }
    }

    /** No inputs at all. */
    public static DerivedInputs none() {
        return new DerivedInputs(Set.of(), Set.of(), Set.of(), null, false);
    }

    /** True when the generator subscribes to change batches. */
    public boolean wantsChanges() {
        return !sources.isEmpty() || !kinds.isEmpty();
    }

    /**
     * True when a change of {@code sourceId} and {@code kind} matches the change subscription.
     *
     * @param sourceId the changed record's source
     * @param kind the changed record's kind
     */
    public boolean matchesChange(String sourceId, String kind) {
        if (!wantsChanges()) {
            return false;
        }
        boolean sourceOk = sources.isEmpty() || sources.contains(sourceId);
        boolean kindOk = kinds.isEmpty() || kinds.contains(kind);
        return sourceOk && kindOk;
    }

    /** True when frames of {@code productId} reach the generator. */
    public boolean matchesProduct(String productId) {
        return products.contains(productId);
    }
}
