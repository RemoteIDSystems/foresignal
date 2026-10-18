package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.situation.SituationLinker;
import ai.foresignal.situation.SituationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Target {@code situation} (prompt 138): matching records either join the 118 situation {@code config.situationId}
 * as pinned {@code record} members (role {@code config.role}, {@code core} by default), or, with
 * {@code config.linker: true}, are offered to the 118 linker, which joins them to the best-scoring situation above its
 * threshold (never seeding one).
 */
@Component
public class SituationBindingTarget implements BindingTargetHandler {

    private final ObjectProvider<SituationService> situations;
    private final ObjectProvider<SituationLinker> linker;
    private final JsonMapper json;

    /** Creates the target. */
    public SituationBindingTarget(ObjectProvider<SituationService> situations, ObjectProvider<SituationLinker> linker,
            JsonMapper json) {
        this.situations = situations;
        this.linker = linker;
        this.json = json;
    }

    @Override
    public String target() {
        return BindingVocabulary.SITUATION;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
        List<FieldError> errors = new ArrayList<>();
        String id = BindingConfig.text(config, "situationId");
        BindingConfig.string(config, "situationId", errors);
        BindingConfig.bool(config, "linker", errors);
        BindingConfig.oneOf(config, "role", List.of("core", "context"), errors);
        boolean useLinker = BindingConfig.flag(config, "linker", false);
        if ((id == null) == !useLinker) {
            errors.add(new FieldError("config", "give exactly one of situationId and linker: true"));
        }
        if (id != null) {
            SituationService service = situations.getIfAvailable();
            try {
                if (service == null) {
                    errors.add(new FieldError("config.situationId", "situations are not available"));
                } else {
                    service.get(id);
                }
            } catch (ForesignalException e) {
                errors.add(new FieldError("config.situationId", "no situation '" + id + "'"));
            }
        }
        return errors;
    }

    @Override
    public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        String id = BindingConfig.text(binding.getConfig(), "situationId");
        return id != null ? BindingEffect.of(item, "situation-member", ObjectTypes.SITUATION + ":" + id)
                : BindingEffect.of(item, "situation-linker", null);
    }

    @Override
    public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        String id = BindingConfig.text(binding.getConfig(), "situationId");
        if (id == null) {
            SituationLinker l = linker.getIfAvailable();
            if (l == null) {
                return BindingEffect.error(item, "the situation linker is not available");
            }
            Optional<SituationLinker.Decision> decision = l.consider(item.sourceId(), item.externalId(), item.seq());
            if (decision.isEmpty()) {
                return BindingEffect.error(item, "the record version could not be read");
            }
            SituationLinker.Decision d = decision.get();
            return d.situationId() == null ? BindingEffect.skip(item, "linker-" + d.action())
                    : BindingEffect.of(item, "situation-" + d.action(), ObjectTypes.SITUATION + ":" + d.situationId());
        }
        SituationService service = situations.getIfAvailable();
        if (service == null) {
            return BindingEffect.error(item, "situations are not available");
        }
        ObjectNode body = json.createObjectNode();
        body.put("memberType", "record");
        body.put("ref", item.recordKey());
        body.put("seq", item.seq());
        String role = BindingConfig.text(binding.getConfig(), "role");
        body.put("role", role == null ? "core" : role);
        SituationService.AddResult result = service.addMember(id, body, "binding:" + binding.getId(), "operator",
                true);
        return result.created() ? plan(binding, stream, item) : BindingEffect.skip(item, "already-member");
    }
}
