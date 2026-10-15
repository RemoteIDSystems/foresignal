package ai.foresignal.intel;

import java.util.Set;

/**
 * The {@code intel} SSE topic filter (prompt 101): a type, a status, and whether the caller may see
 * private events. A caller without the private grant (prompt 039) never sees an event marked private.
 */
public record IntelFilter(Set<String> excludedSources, boolean privateAccess, String type, String status) {

    /** An unfiltered, non-private filter. */
    public static IntelFilter all() {
        return new IntelFilter(Set.of(), false, null, null);
    }

    /** True when {@code event} passes this filter. */
    public boolean matches(IntelEvent event) {
        Object priv = event.summary().get("private");
        if (priv instanceof Boolean value && value && !privateAccess) {
            return false;
        }
        if (type != null && !type.equals(String.valueOf(event.summary().get("type")))) {
            return false;
        }
        return status == null || status.equals(String.valueOf(event.summary().get("status")));
    }
}
