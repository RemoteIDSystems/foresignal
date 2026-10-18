package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.inbound.edp.EdpItemMapper;
import ai.foresignal.place.PlaceService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Target {@code place} (prompt 138): the latest bound value or document of the stream becomes an attribute of the 036
 * place {@code config.placeId}, served in the {@code external} block of {@code GET /v1/places/{placeId}}
 * ({@link ExternalPlaceValues}). The binding keeps the newest item (by seq); nothing is written to the place itself.
 */
@Component
public class PlaceBindingTarget implements BindingTargetHandler {

    /** The document text kept in the attribute. */
    static final int TEXT_EXCERPT = 500;

    private final ObjectProvider<PlaceService> places;

    /** Creates the target. */
    public PlaceBindingTarget(ObjectProvider<PlaceService> places) {
        this.places = places;
    }

    @Override
    public String target() {
        return BindingVocabulary.PLACE;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
        List<FieldError> errors = new ArrayList<>();
        String placeId = BindingConfig.required(config, "placeId", errors);
        PlaceService service = places.getIfAvailable();
        if (placeId != null && (service == null || service.find(placeId).isEmpty())) {
            errors.add(new FieldError("config.placeId", "no place '" + placeId + "'"));
        }
        return errors;
    }

    @Override
    public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        return BindingEffect.of(item, "place-attribute", "place:" + BindingConfig.text(binding.getConfig(), "placeId"));
    }

    @Override
    public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        Map<String, Object> latest = binding.getLatest();
        Object seq = latest == null ? null : latest.get("seq");
        if (seq instanceof Number n && n.longValue() >= item.seq()) {
            return BindingEffect.skip(item, "older-than-latest");
        }
        binding.setLatest(attribute(stream, item)); // saved with the binding's counters by the applier
        return plan(binding, stream, item);
    }

    /** The attribute of an item: its value or document summary with provenance. Pure. */
    static Map<String, Object> attribute(BoundStream stream, BoundItem item) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", item.sourceId());
        out.put("streamId", stream.streamId());
        out.put("kind", item.kind());
        out.put("itemType", stream.itemType());
        out.put("ref", item.ref());
        out.put("seq", item.seq());
        out.put("observedAt", item.observedAt() == null ? null : item.observedAt().toString());
        out.put("receivedAt", item.receivedAt() == null ? null : item.receivedAt().toString());
        Map<String, Object> p = item.payload();
        switch (stream.itemType()) {
            case "observation" -> {
                out.put("seriesKey", p.get("seriesKey"));
                out.put("t", p.get("t"));
                out.put("value", p.get("value"));
                out.put("unit", p.get("unit"));
                Object known = item.effective("effectiveKnownAt");
                out.put("knownAt", known == null ? null : known.toString());
            }
            case "document" -> {
                out.put("documentType", p.get("documentType"));
                out.put("title", p.get("title"));
                String text = item.text("text");
                out.put("text", text == null || text.length() <= TEXT_EXCERPT ? text : text.substring(0, TEXT_EXCERPT));
                out.put("url", p.get("url"));
                out.put("publishedAt", p.get("publishedAt"));
            }
            default -> {
                Map<String, Object> properties = new LinkedHashMap<>(p);
                properties.keySet().removeAll(Set.of(EdpItemMapper.PAYLOAD_EDP, EdpItemMapper.PAYLOAD_TIMES));
                out.put("properties", properties);
            }
        }
        return out;
    }
}
