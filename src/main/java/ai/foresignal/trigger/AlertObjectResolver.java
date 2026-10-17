package ai.foresignal.trigger;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.visibility.SourceVisibility;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code alert:{id}} (prompt 129): a 017 alert with its rule name, severity and state. Alerts of sources the
 * caller may not read are invisible (as in {@code getAlert}); sensitive alerts (rules referencing a watchlist, 036)
 * are invisible to anonymous callers.
 */
@Component
public class AlertObjectResolver implements ObjectResolver {

    private final AlertStore alerts;
    private final SourceVisibility visibility;

    /** Creates the resolver. */
    public AlertObjectResolver(AlertStore alerts, SourceVisibility visibility) {
        this.alerts = alerts;
        this.visibility = visibility;
    }

    @Override
    public String type() {
        return ObjectTypes.ALERT;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        long id;
        try {
            id = Long.parseLong(ref.id());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return alerts.find(id)
                .filter(a -> context.caller().canRead(a.sourceId()))
                .filter(a -> !a.sensitive() || !context.caller().anonymous())
                .filter(a -> context.existedAt(a.firedAt()))
                .map(a -> ObjectEnvelope.builder(ref)
                        .title(a.ruleName() == null ? "Alert " + a.id() : a.ruleName())
                        .summary(a.summary() != null ? a.summary() : a.message())
                        .textOrigin(a.summary() != null ? "llm" : "deterministic")
                        .status(a.state())
                        .severity(a.severity())
                        .time(a.firedAt(), a.resolvedAt(), a.observedAt())
                        .privateObject(isPrivate(a.sourceId()))
                        .provenance(a.sourceId() == null ? List.of() : List.of(a.sourceId()), null, a.seq())
                        .self("/v1/alerts/" + a.id())
                        .build());
    }

    private boolean isPrivate(String sourceId) {
        try {
            return sourceId != null && visibility.isPrivate(sourceId);
        } catch (RuntimeException e) {
            return true;
        }
    }
}
