package ai.foresignal.ingest;

/**
 * An immutable view of the trigger rules, as the policy engine consults them
 * (Core-Data-Model §11).
 */
public interface GateSnapshot {

    /** A snapshot with no rules: no paths, no geometry, never a flip. */
    GateSnapshot EMPTY =
            new GateSnapshot() {
                @Override
                public GateSpec spec(String sourceId) {
                    return GateSpec.NONE;
                }

                @Override
                public boolean flips(String sourceId, String kind, GateState persisted, GateState candidate) {
                    return false;
                }
            };

    /**
     * What the rules of {@code sourceId} need loaded from the head: payload paths, and whether the
     * persisted geometry is needed. {@link GateSpec#NONE} when no rule applies.
     */
    GateSpec spec(String sourceId);

    /**
     * True when any rule applicable to {@code sourceId} and {@code kind} evaluates differently on
     * {@code persisted} and {@code candidate}.
     */
    boolean flips(String sourceId, String kind, GateState persisted, GateState candidate);
}
