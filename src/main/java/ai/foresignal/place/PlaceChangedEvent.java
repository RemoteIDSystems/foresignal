package ai.foresignal.place;

/**
 * A place was created, replaced or deleted (prompt 036). Published after the save committed; the
 * places AOI recomputes on it, and later prompts (weather sources, render warming) listen too.
 * Referencing rules are re-materialized in the save's own transaction, before this is published.
 *
 * @param placeId the place
 * @param version its version after the save (the last version for a deletion)
 * @param deleted true when the place was deleted
 */
public record PlaceChangedEvent(String placeId, int version, boolean deleted) {}
