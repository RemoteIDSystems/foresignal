package ai.foresignal.social;

/**
 * The {@code social} SSE topic filter (prompt 135): whether the caller may see events built from private sources
 * (EGR-1; an EDP provider bound as a private source).
 *
 * @param privateAccess whether the caller may see private events
 */
public record SocialFilter(boolean privateAccess) {

    /** True when an event with {@code privateEvent} passes this filter. */
    public boolean matches(boolean privateEvent) {
        return !privateEvent || privateAccess;
    }
}
