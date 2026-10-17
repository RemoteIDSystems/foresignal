package ai.foresignal.cycle;

/**
 * The {@code assessments} SSE topic filter (prompt 128): whether the caller may see private assessments (EGR-1).
 *
 * @param privateAccess whether the caller may see private assessments
 */
public record AssessmentFilter(boolean privateAccess) {

    /** True when an event with {@code privateEvent} passes this filter. */
    public boolean matches(boolean privateEvent) {
        return !privateEvent || privateAccess;
    }
}
