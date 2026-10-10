package ai.elaro.store;

/**
 * When a rule fires relative to a record's previous state.
 *
 * <p>{@code MATCH} fires on every change that satisfies the conditions. {@code ENTER} and
 * {@code EXIT} fire only on the transition, which is what makes a geofence useful: a vessel sitting
 * inside a fence should announce itself once, not on every position update.
 */
public enum TriggerMode {
    MATCH,
    ENTER,
    EXIT;

    /** The wire value stored in {@code trigger_rule.mode} and used by the API. */
    public String wireValue() {
        return name().toLowerCase();
    }
}
