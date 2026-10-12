package ai.foresignal.trigger;

import ai.foresignal.ask.CitationSet;
import java.util.List;
import java.util.Objects;

/**
 * The assembled context of one alert's enrichment (prompt 030).
 *
 * @param anchor where and when the context was gathered
 * @param json the serialized context sent to the model, at most the configured cap
 * @param citable what the model may cite: the alert, every record version and alert in {@code json}
 * @param counts how many items each part kept after trimming ({@code history}, {@code nearbyRecords},
 *     {@code nearbyAlerts}, {@code related}, {@code sections}), in that order
 * @param trimmed how many items were removed to respect the size cap
 * @param notes remarks about what was skipped (no geometry, search unavailable), in order
 */
public record EnrichmentContext(EnrichmentAnchor anchor, String json, CitationSet citable, Counts counts, int trimmed,
        List<String> notes) {

    /** Requires the members; copies the notes. */
    public EnrichmentContext {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(citable, "citable");
        Objects.requireNonNull(counts, "counts");
        notes = notes == null ? List.of() : List.copyOf(notes);
    }

    /**
     * Items kept per part.
     *
     * @param history prior versions of the alerted entity
     * @param nearbyRecords records of any kind within the radius and window
     * @param nearbyAlerts alerts within the radius and window
     * @param related semantic matches
     * @param sections contributor sections
     */
    public record Counts(int history, int nearbyRecords, int nearbyAlerts, int related, int sections) {

        /** True when nothing besides the alert and its record is in the context. */
        public boolean empty() {
            return history == 0 && nearbyRecords == 0 && nearbyAlerts == 0 && related == 0 && sections == 0;
        }
    }
}
