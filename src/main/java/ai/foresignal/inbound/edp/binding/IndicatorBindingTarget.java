package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisVocabulary;
import ai.foresignal.hypothesis.IndicatorRepository;
import ai.foresignal.object.ObjectTypes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Target {@code indicator} (prompt 138): when an item passes the binding's filter, the 125 indicator
 * {@code config.indicatorId} is marked {@code observed} for feature, event and observation streams (physical data,
 * with the item as evidence and its ACH row), or {@code reported} for document streams (a report never makes an
 * indicator observed, as for social claims in 137). An indicator that is no longer waiting is left alone.
 */
@Component
public class IndicatorBindingTarget implements BindingTargetHandler {

    private final ObjectProvider<HypothesisService> hypotheses;
    private final IndicatorRepository indicators;

    /** Creates the target. */
    public IndicatorBindingTarget(ObjectProvider<HypothesisService> hypotheses, IndicatorRepository indicators) {
        this.hypotheses = hypotheses;
        this.indicators = indicators;
    }

    @Override
    public String target() {
        return BindingVocabulary.INDICATOR;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
        List<FieldError> errors = new ArrayList<>();
        String id = BindingConfig.required(config, "indicatorId", errors);
        if (id != null && !indicators.existsById(id)) {
            errors.add(new FieldError("config.indicatorId", "no indicator '" + id + "'"));
        }
        return errors;
    }

    /** {@code reported} for documents, {@code observed} otherwise. */
    static String state(BoundStream stream) {
        return "document".equals(stream.itemType()) ? HypothesisVocabulary.REPORTED : HypothesisVocabulary.OBSERVED;
    }

    @Override
    public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        return BindingEffect.of(item, "indicator-" + state(stream),
                ObjectTypes.INDICATOR + ":" + BindingConfig.text(binding.getConfig(), "indicatorId"));
    }

    @Override
    public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        HypothesisService service = hypotheses.getIfAvailable();
        if (service == null) {
            return BindingEffect.error(item, "hypotheses are not available");
        }
        String indicatorId = BindingConfig.text(binding.getConfig(), "indicatorId");
        Map<String, Object> evidence = evidence(binding, item);
        if (HypothesisVocabulary.REPORTED.equals(state(stream))) {
            if (!service.reported(indicatorId, evidence)) {
                return BindingEffect.skip(item, "indicator-not-waiting");
            }
        } else {
            service.observe(indicatorId, evidence, HypothesisVocabulary.OBSERVED);
        }
        return plan(binding, stream, item);
    }

    /** The evidence map stored on the indicator. Pure. */
    static Map<String, Object> evidence(ExternalBindingEntity binding, BoundItem item) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("source", "record");
        evidence.put("ref", item.ref());
        evidence.put("sourceId", item.sourceId());
        evidence.put("externalId", item.externalId());
        evidence.put("seq", item.seq());
        evidence.put("bindingId", binding.getId());
        evidence.put("at", (item.eventTime() != null ? item.eventTime() : item.observedAt()) == null ? null
                : String.valueOf(item.eventTime() != null ? item.eventTime() : item.observedAt()));
        return evidence;
    }
}
