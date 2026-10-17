package ai.foresignal.evidence;

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
 * Resolves {@code canonical-event:{id}} (prompt 129): a 116 canonical event with its family, observation and
 * independent-group counts and contributing sources. Sources the caller may not read are dropped from the
 * provenance; an event whose every source is hidden is invisible. It is private when any contributing source is.
 */
@Component
public class CanonicalEventObjectResolver implements ObjectResolver {

    private final EvidenceStore store;
    private final SourceVisibility visibility;

    /** Creates the resolver. */
    public CanonicalEventObjectResolver(EvidenceStore store, SourceVisibility visibility) {
        this.store = store;
        this.visibility = visibility;
    }

    @Override
    public String type() {
        return ObjectTypes.CANONICAL_EVENT;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return store.event(ref.id()).filter(e -> context.existedAt(e.createdAt())).flatMap(e -> {
            List<String> sources = e.sourceIds() == null ? List.of() : e.sourceIds();
            List<String> readable = sources.stream().filter(context.caller()::canRead).toList();
            if (!sources.isEmpty() && readable.isEmpty()) {
                return Optional.empty();
            }
            boolean priv = readable.stream().anyMatch(this::isPrivate);
            if (priv && !context.seesPrivate()) {
                return Optional.empty();
            }
            String title = e.family() + " event" + (e.regionCodes() == null || e.regionCodes().isEmpty() ? ""
                    : " in " + String.join(", ", e.regionCodes()));
            return Optional.of(ObjectEnvelope.builder(ref)
                    .title(title)
                    .summary(e.observationCount() + " observations in " + e.evidenceGroupCount()
                            + " independent evidence groups")
                    .status(e.status())
                    .time(e.eventTime(), null, e.firstObservedAt())
                    .point(e.lon(), e.lat())
                    .privateObject(priv)
                    .provenance(readable, null, null)
                    .self("/v1/evidence/events/" + e.id())
                    .build());
        });
    }

    private boolean isPrivate(String sourceId) {
        try {
            return visibility.isPrivate(sourceId);
        } catch (RuntimeException ex) {
            return true;
        }
    }
}
