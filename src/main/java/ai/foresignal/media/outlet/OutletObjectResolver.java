package ai.foresignal.media.outlet;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code outlet:{id}} (prompt 133, contract 1.4): a news outlet of the registry with its type, country and lean
 * consensus bucket. A merged outlet resolves to its survivor's envelope under the asked reference.
 */
@Component
public class OutletObjectResolver implements ObjectResolver {

    private final OutletRegistry registry;

    /** Creates the resolver. */
    public OutletObjectResolver(OutletRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String type() {
        return ObjectTypes.OUTLET;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return registry.find(ref.id()).filter(o -> context.existedAt(o.createdAt())).map(o -> {
            Map<String, Consensus.Result> consensus = registry.consensus(o.id());
            Consensus.Result lean = consensus.get(MediaVocabulary.LEAN);
            String bucket = lean == null || lean.bucket() == null ? MediaVocabulary.UNRATED : lean.bucket();
            String summary = o.type() + (o.country() == null ? "" : ", " + o.country()) + "; lean " + bucket
                    + (lean == null ? "" : " (" + lean.nProviders() + " providers)")
                    + (o.domains().isEmpty() ? "" : "; " + String.join(", ", o.domains()));
            return ObjectEnvelope.builder(ref)
                    .title(o.name())
                    .summary(summary.length() > 280 ? summary.substring(0, 279) + "…" : summary)
                    .status(o.rated() ? "rated" : MediaVocabulary.UNRATED)
                    .privateObject(false)
                    .provenance(List.of(), null, null)
                    .self("/v1/media/outlets/" + o.id())
                    .build();
        });
    }
}
