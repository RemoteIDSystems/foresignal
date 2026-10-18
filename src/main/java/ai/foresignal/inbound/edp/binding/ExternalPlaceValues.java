package ai.foresignal.inbound.edp.binding;

import ai.foresignal.place.PlaceExternalValues;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The {@code external} block of a place (prompt 138): for each enabled {@code place} binding on the place, the latest
 * bound value or document, keyed {@code {sourceId}/{streamId}}.
 */
@Component
public class ExternalPlaceValues implements PlaceExternalValues {

    private final ExternalBindingRepository bindings;

    /** Creates the lookup. */
    public ExternalPlaceValues(ExternalBindingRepository bindings) {
        this.bindings = bindings;
    }

    @Override
    public Map<String, Object> latestFor(String placeId) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (ExternalBindingEntity b : bindings.findByTargetOrderByCreatedAtAscIdAsc(BindingVocabulary.PLACE)) {
            if (!b.isEnabled() || !placeId.equals(BindingConfig.text(b.getConfig(), "placeId"))) {
                continue;
            }
            out.put(b.getSourceId() + "/" + b.getStreamId(), b.getLatest());
        }
        return out.isEmpty() ? null : out;
    }
}
