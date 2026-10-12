package ai.foresignal.trigger;

/** The outcome of a condition evaluated in Java ({@link Operator#evaluate}). */
public enum Evaluation {
    /** The condition holds. */
    MATCH,
    /** The condition does not hold, with the value present and of the expected type. */
    NO_MATCH,
    /**
     * The value is missing or not of the type the operator expects (or a regex ran out of time).
     * Counts as not holding, as SQL's {@code NULL} does under {@code COALESCE(…, false)}; the gate
     * treats it conservatively (Core-Data-Model §11).
     */
    AMBIGUOUS;

    /** True only for {@link #MATCH}. */
    public boolean holds() {
        return this == MATCH;
    }

    /** {@link #MATCH} or {@link #NO_MATCH} from a boolean. */
    public static Evaluation of(boolean holds) {
        return holds ? MATCH : NO_MATCH;
    }
}
