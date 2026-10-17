package ai.foresignal.hypothesis;

/**
 * The {@code hypotheses} SSE topic filter (prompt 125): whether the caller may see private sets (EGR-1).
 *
 * @param privateAccess whether the caller may see private sets
 */
public record HypothesisFilter(boolean privateAccess) {

    /** True when an event of a set with {@code privateSet} passes this filter. */
    public boolean matches(boolean privateSet) {
        return !privateSet || privateAccess;
    }
}
