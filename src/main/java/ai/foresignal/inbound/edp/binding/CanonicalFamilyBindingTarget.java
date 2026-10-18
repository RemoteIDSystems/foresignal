package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.EvidenceResolver;
import ai.foresignal.evidence.Originator;
import ai.foresignal.evidence.StoryFamilyHandler;
import ai.foresignal.evidence.UpstreamRefs;
import ai.foresignal.object.ObjectTypes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Target {@code canonical-family} (prompt 138): event items join a 116 canonical-event family through the
 * {@link EvidenceResolver}, counted under their originator (the item's {@code origin.originator}, else the
 * manifest's, registered with its {@code aggregates}), so EDP events corroborate or are corroborated once per
 * independent origin and never per provider. The item's {@code upstreamId} and {@code derivedFrom} are matched as
 * upstream ids in namespace {@code config.upstreamNamespace} (default {@value #DEFAULT_NAMESPACE}). The stream's
 * {@code coverage} feeds observability through {@link ExternalCoverageRegistry}.
 *
 * <p>Configuration: {@code family} (required; a bundled family takes its matching rule, an unknown one a proximity
 * rule of {@value #DEFAULT_MAX_DT_SECONDS} s and {@value #DEFAULT_MAX_KM} km), and the overrides {@code maxDtSeconds}
 * and {@code maxKm}. Story families ({@code news-story}, {@code social-narrative}) are refused: use the {@code news}
 * and {@code social} targets.
 */
@Component
public class CanonicalFamilyBindingTarget implements BindingTargetHandler {

    /** The upstream-id namespace when the binding names none. */
    public static final String DEFAULT_NAMESPACE = "upstream";

    /** The time tolerance of a family the bundled rules do not know, seconds. */
    public static final double DEFAULT_MAX_DT_SECONDS = 3600;

    /** The distance tolerance of a family the bundled rules do not know, km. */
    public static final double DEFAULT_MAX_KM = 25;

    /** The synthetic payload field the item's upstream ids are matched from. */
    static final String UPSTREAM_FIELD = "_edpUpstreamIds";

    private final CanonicalRules rules;
    private final EvidenceResolver resolver;
    private final ExternalOriginators originators;

    /** Creates the target. */
    public CanonicalFamilyBindingTarget(CanonicalRules rules, EvidenceResolver resolver,
            ExternalOriginators originators) {
        this.rules = rules;
        this.resolver = resolver;
        this.originators = originators;
    }

    @Override
    public String target() {
        return BindingVocabulary.CANONICAL_FAMILY;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, BoundStream stream) {
        List<FieldError> errors = new ArrayList<>();
        String family = BindingConfig.required(config, "family", errors);
        if (family != null && !family.matches("^[a-z0-9]+(-[a-z0-9]+)*$")) {
            errors.add(new FieldError("config.family", "must be a family slug"));
        }
        CanonicalRules.FamilyRule rule = family == null ? null : rules.family(family);
        if (rule != null && StoryFamilyHandler.STORY_MATCH.equals(rule.match())) {
            errors.add(new FieldError("config.family", "'" + family + "' is a story family; bind article documents"
                    + " with target news and social-post documents with target social"));
        }
        BindingConfig.range(config, "maxDtSeconds", 1, 30 * 86_400, errors);
        BindingConfig.range(config, "maxKm", 0.01, 5_000, errors);
        BindingConfig.string(config, "upstreamNamespace", errors);
        return errors;
    }

    /**
     * The family rule a binding uses: the bundled family's with the binding's tolerance overrides, or a proximity
     * rule for a family the bundled rules do not know. Pure apart from reading the rules.
     */
    CanonicalRules.FamilyRule rule(Map<String, Object> config) {
        String family = BindingConfig.text(config, "family");
        Double dt = BindingConfig.number(config, "maxDtSeconds");
        Double km = BindingConfig.number(config, "maxKm");
        CanonicalRules.FamilyRule bundled = rules.family(family);
        if (bundled == null) {
            return new CanonicalRules.FamilyRule(family, List.of(), "proximity",
                    dt != null ? dt : DEFAULT_MAX_DT_SECONDS, km != null ? km : DEFAULT_MAX_KM, null, null, 0.1,
                    List.of(), List.of(), List.of(), List.of(), List.of());
        }
        if (dt == null && km == null) {
            return bundled;
        }
        return new CanonicalRules.FamilyRule(bundled.family(), bundled.inputs(), bundled.match(),
                dt != null ? dt : bundled.maxDtSeconds(), km != null ? km : bundled.maxKm(), bundled.attribute(),
                bundled.bands(), bundled.cellDegrees(), bundled.countryFields(), bundled.asnFields(),
                bundled.startFields(), bundled.endFields(), bundled.typeFields(), bundled.story());
    }

    @Override
    public BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        if (item.eventTime() == null && item.observedAt() == null) {
            return BindingEffect.error(item, "the event has no time");
        }
        return BindingEffect.of(item, "canonical-link",
                "family:" + BindingConfig.text(binding.getConfig(), "family"));
    }

    @Override
    public BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item) {
        BindingEffect planned = plan(binding, stream, item);
        if (planned.failed()) {
            return planned;
        }
        Originator originator = originators.register(item, stream.manifest());
        String namespace = BindingConfig.text(binding.getConfig(), "upstreamNamespace");
        UpstreamRefs refs = new UpstreamRefs(List.of(new UpstreamRefs.EventIdField(UPSTREAM_FIELD,
                namespace == null ? DEFAULT_NAMESPACE : namespace)), List.of());
        String eventId = resolver.resolveBound(item.seq(), item.sourceId(), item.externalId(), item.kind(),
                item.version(), item.eventTime() != null ? item.eventTime() : item.observedAt(), item.observedAt(),
                item.lon(), item.lat(), payloadWithUpstream(item), rule(binding.getConfig()), originator, refs);
        if (eventId == null) {
            return BindingEffect.skip(item, "already-linked");
        }
        return BindingEffect.of(item, "canonical-link", ObjectTypes.CANONICAL_EVENT + ":" + eventId);
    }

    /** The payload with the item's upstream ids ({@code upstreamId} and {@code derivedFrom}) as one list. Pure. */
    static Map<String, Object> payloadWithUpstream(BoundItem item) {
        List<String> ids = new ArrayList<>();
        if (item.upstreamId() != null) {
            ids.add(item.upstreamId());
        }
        Object edp = item.payload().get("edp");
        if (edp instanceof Map<?, ?> m && m.get("derivedFrom") instanceof List<?> derived) {
            for (Object d : derived) {
                if (d != null && !ids.contains(String.valueOf(d))) {
                    ids.add(String.valueOf(d));
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>(item.payload());
        out.put(UPSTREAM_FIELD, ids);
        return out;
    }
}
