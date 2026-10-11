package ai.foresignal.source;

/**
 * Published after a source's settings are committed.
 *
 * <p>Carries both states so a listener can tell what actually changed — the scheduler only needs to
 * act when {@code enabled} or {@code interval} moved, not on every budget edit.
 *
 * <p>Published after commit, so a listener that reads the settings back cannot observe a value a
 * rollback then discards.
 *
 * @param sourceId the source
 * @param before the settings as they were, or null when the row was just created
 * @param after the settings as they now are
 */
public record SourceSettingsChangedEvent(String sourceId, SourceView before, SourceView after) {

    /** True when the source was switched on or off. */
    public boolean enabledChanged() {
        return before == null || before.enabled() != after.enabled();
    }

    /** True when anything the collector schedules on has changed. */
    public boolean affectsScheduling() {
        return before == null
                || before.enabled() != after.enabled()
                || !java.util.Objects.equals(before.interval(), after.interval())
                || !java.util.Objects.equals(before.params(), after.params());
    }
}
