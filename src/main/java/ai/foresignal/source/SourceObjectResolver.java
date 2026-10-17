package ai.foresignal.source;

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
 * Resolves {@code source:{id}} (prompt 129): a registered source with its name and description. Sources the caller may
 * not read are invisible; private sources are private.
 */
@Component
public class SourceObjectResolver implements ObjectResolver {

    private final SourceRegistry registry;
    private final SourceVisibility visibility;

    /** Creates the resolver. */
    public SourceObjectResolver(SourceRegistry registry, SourceVisibility visibility) {
        this.registry = registry;
        this.visibility = visibility;
    }

    @Override
    public String type() {
        return ObjectTypes.SOURCE;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        if (!context.caller().canRead(ref.id())) {
            return Optional.empty();
        }
        return registry.find(ref.id()).map(r -> {
            boolean priv;
            try {
                priv = visibility.isPrivate(ref.id());
            } catch (RuntimeException e) {
                priv = true;
            }
            return ObjectEnvelope.builder(ref)
                    .title(r.descriptor().name())
                    .summary(r.descriptor().description())
                    .privateObject(priv)
                    .provenance(List.of(ref.id()), null, null)
                    .self("/v1/sources/" + ref.id())
                    .build();
        });
    }
}
