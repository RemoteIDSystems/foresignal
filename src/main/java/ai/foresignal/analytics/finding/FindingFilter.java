package ai.foresignal.analytics.finding;

/**
 * The {@code analytics} SSE topic filter (prompt 119): whether the caller may see private findings (EGR-1).
 *
 * @param privateAccess whether the caller may see private findings
 */
public record FindingFilter(boolean privateAccess) {

    /** True when {@code event} passes this filter. */
    public boolean matches(FindingEvent event) {
        return !event.privateFinding() || privateAccess;
    }
}
