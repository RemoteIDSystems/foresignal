package ai.foresignal.chronicle;

import java.util.Set;
import java.util.function.Predicate;

/**
 * A chronicle layer compiled for capture (prompt 131): the source and kind sets the in-memory index pre-filters by,
 * the full test (selector, conditions, membership and area, with JTS prepared geometries), the capture mode and the
 * seq from which the layer's definition applies. Immutable; the index rebuilds it when the layer or what it depends on
 * (entity links, situation members) changes.
 */
public final class CompiledLayer {

    private final String chronicleId;
    private final String layerId;
    private final String name;
    private final ChronicleSelector selector;
    private final CaptureMode capture;
    private final Long fromSeq;
    private final Set<String> sources;
    private final Set<String> kinds;
    private final Predicate<CapturedRow> test;
    private final boolean recordBacked;

    /**
     * Creates a compiled layer.
     *
     * @param chronicleId the chronicle
     * @param layerId the layer
     * @param name the layer name
     * @param selector the selector
     * @param capture the capture mode (not yet degraded by the cap)
     * @param fromSeq the seq from which the definition applies, or null for any
     * @param sources the sources a matching row must come from; empty for any
     * @param kinds the kinds a matching row must have; empty for any
     * @param test the full test of a row that passed the source and kind sets
     * @param recordBacked false for layers fed by something other than record versions (alerts; sampled series)
     */
    public CompiledLayer(String chronicleId, String layerId, String name, ChronicleSelector selector,
            CaptureMode capture, Long fromSeq, Set<String> sources, Set<String> kinds, Predicate<CapturedRow> test,
            boolean recordBacked) {
        this.chronicleId = chronicleId;
        this.layerId = layerId;
        this.name = name;
        this.selector = selector;
        this.capture = capture;
        this.fromSeq = fromSeq;
        this.sources = sources == null ? Set.of() : Set.copyOf(sources);
        this.kinds = kinds == null ? Set.of() : Set.copyOf(kinds);
        this.test = test == null ? row -> true : test;
        this.recordBacked = recordBacked;
    }

    /** The chronicle. */
    public String chronicleId() {
        return chronicleId;
    }

    /** The layer. */
    public String layerId() {
        return layerId;
    }

    /** The layer name. */
    public String name() {
        return name;
    }

    /** The selector. */
    public ChronicleSelector selector() {
        return selector;
    }

    /** The capture mode as defined (before the cap fallback). */
    public CaptureMode capture() {
        return capture;
    }

    /** The seq from which the definition applies, or null. */
    public Long fromSeq() {
        return fromSeq;
    }

    /** The sources a matching row must come from; empty for any. */
    public Set<String> sources() {
        return sources;
    }

    /** The kinds a matching row must have; empty for any. */
    public Set<String> kinds() {
        return kinds;
    }

    /** False for layers fed by something other than record versions. */
    public boolean recordBacked() {
        return recordBacked;
    }

    /**
     * True when {@code row} belongs to this layer: at or after the definition's seq, from one of the sources, of one
     * of the kinds, and passing the full test.
     */
    public boolean matches(CapturedRow row) {
        if (!recordBacked) {
            return false;
        }
        if (fromSeq != null && row.seq() < fromSeq) {
            return false;
        }
        if (!sources.isEmpty() && !sources.contains(row.sourceId())) {
            return false;
        }
        if (!kinds.isEmpty() && (row.kind() == null || !kinds.contains(row.kind()))) {
            return false;
        }
        return test.test(row);
    }
}
