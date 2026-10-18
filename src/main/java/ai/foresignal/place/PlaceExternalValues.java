package ai.foresignal.place;

import java.util.Map;

/**
 * The latest values or documents of external (EDP) streams bound to a place with target {@code place} (prompt 138),
 * shown as the {@code external} block of the {@code Place} response. The binding package provides the bean.
 */
public interface PlaceExternalValues {

    /**
     * The block for {@code placeId}: one entry per bound stream, keyed {@code {sourceId}/{streamId}}, or null when no
     * stream is bound to the place.
     */
    Map<String, Object> latestFor(String placeId);
}
