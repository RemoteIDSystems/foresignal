package ai.foresignal.situation;

/**
 * The {@code situations} SSE topic filter (prompt 118): a status, a theme, and whether the caller may see private
 * situations ({@code IntelVisibility}, EGR-1). A caller without private access never sees a private situation.
 *
 * @param privateAccess whether the caller may see private situations
 * @param status only this status, or null
 * @param theme only this theme, or null
 */
public record SituationFilter(boolean privateAccess, String status, String theme) {

    /** True when {@code event} passes this filter. */
    public boolean matches(SituationEvent event) {
        if (event.privateSituation() && !privateAccess) {
            return false;
        }
        Object current = event.situation() == null ? null : event.situation().get("status");
        if (status != null && !status.equals(String.valueOf(current))) {
            return false;
        }
        Object currentTheme = event.situation() == null ? null : event.situation().get("theme");
        return theme == null || theme.equals(String.valueOf(currentTheme));
    }
}
