package ai.foresignal.reference;

import java.util.List;

/**
 * Published after reference datasets were swapped in (prompt 040), outside any transaction: the region
 * tag cache is dropped, records are tagged again, and geofence presets are re-resolved by the trigger
 * gate. Listeners that write must do so in a transaction of their own.
 *
 * @param datasets the names of the datasets that changed
 */
public record ReferenceDataChangedEvent(List<String> datasets) {

    /** Copies the names. */
    public ReferenceDataChangedEvent {
        datasets = datasets == null ? List.of() : List.copyOf(datasets);
    }
}
