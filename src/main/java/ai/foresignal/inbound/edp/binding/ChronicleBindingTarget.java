package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.chronicle.CaptureMode;
import ai.foresignal.chronicle.ChronicleLayerEntity;
import ai.foresignal.chronicle.ChronicleSelector;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Target {@code chronicle} (prompt 138): the 131 chronicle {@code config.chronicleId} gets a layer of type
 * {@code external-stream} ({@code {sourceId, streamId, filter}}, named {@code config.layerName}), and the chronicle's
 * own capture keeps copies of the stream from then on, independent of global retention. The binding creates the
 * layer once; applying an item does nothing more (the chronicle captures it).
 */
@Component
public class ChronicleBindingTarget implements BindingTargetHandler {

    private final ObjectProvider<ChronicleService> chronicles;
    private final JsonMapper json;

    /** Creates the target. */
    public ChronicleBindingTarget(ObjectProvider<ChronicleService> chronicles, JsonMapper json) {
        this.chronicles = chronicles;
        this.json = json;
    }

    @Override
    public String target() {
        return BindingVocabulary.CHRONICLE;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
        List<FieldError> errors = new ArrayList<>();
        String id = BindingConfig.required(config, "chronicleId", errors);
        BindingConfig.string(config, "layerName", errors);
        String name = BindingConfig.text(config, "layerName");
        if (name != null && name.length() > 200) {
            errors.add(new FieldError("config.layerName", "must be at most 200 characters"));
        }
        ChronicleService service = chronicles.getIfAvailable();
        if (id != null && (service == null || service.find(id).isEmpty())) {
            errors.add(new FieldError("config.chronicleId", "no chronicle '" + id + "'"));
        }
        return errors;
    }

    @Override
    public String setUp(ExternalBindingEntity binding, BoundStream stream) {
        ChronicleService service = chronicles.getIfAvailable();
        if (service == null) {
            throw new IllegalStateException("chronicles are not available");
        }
        String chronicleId = BindingConfig.text(binding.getConfig(), "chronicleId");
        String name = BindingConfig.text(binding.getConfig(), "layerName");
        ObjectNode layer = json.createObjectNode();
        layer.put("name", name != null ? name : "External stream " + binding.getSourceId() + "/" + binding.getStreamId());
        ObjectNode selector = layer.putObject("selector");
        selector.put("type", ChronicleSelector.EXTERNAL_STREAM);
        selector.put("sourceId", binding.getSourceId());
        selector.put("streamId", binding.getStreamId());
        selector.set("filter", json.valueToTree(binding.getFilter() == null ? List.of() : binding.getFilter()));
        // ChronicleLayerInput requires capture; the layer keeps a copy of every item of the stream.
        layer.set("capture", json.valueToTree(CaptureMode.FULL_MODE.toMap()));
        ChronicleLayerEntity created = service.addLayer(chronicleId, layer, ObjectCaller.system());
        return ObjectTypes.CHRONICLE + ":" + chronicleId + "/layers/" + created.getId();
    }

    @Override
    public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        return BindingEffect.of(item, "chronicle-capture", binding.getTargetRef() != null ? binding.getTargetRef()
                : ObjectTypes.CHRONICLE + ":" + BindingConfig.text(binding.getConfig(), "chronicleId"));
    }

    @Override
    public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        return plan(binding, stream, item);
    }
}
