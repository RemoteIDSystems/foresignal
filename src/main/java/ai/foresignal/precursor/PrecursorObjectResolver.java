package ai.foresignal.precursor;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Resolves {@code precursor:{id}} (prompt 129): a 120 precursor with its status, hit rate and lift. */
@Component
public class PrecursorObjectResolver implements ObjectResolver {

    private final PrecursorRepository precursors;

    /** Creates the resolver. */
    public PrecursorObjectResolver(PrecursorRepository precursors) {
        this.precursors = precursors;
    }

    @Override
    public String type() {
        return ObjectTypes.PRECURSOR;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return precursors.findById(ref.id()).filter(p -> context.existedAt(p.getCreatedAt())).map(p -> {
            StringBuilder summary = new StringBuilder();
            if (p.getHitRate() != null) {
                summary.append("hit rate ").append(Math.round(p.getHitRate() * 100)).append('%');
            }
            if (p.getLift() != null) {
                summary.append(summary.isEmpty() ? "" : ", ").append("lift ")
                        .append(Math.round(p.getLift() * 100) / 100.0);
            }
            return ObjectEnvelope.builder(ref)
                    .title(p.getDescription() == null ? "Precursor " + p.getId() : p.getDescription())
                    .summary(summary.toString())
                    .status(p.getStatus())
                    .time(p.getFirstValidatedAt(), null, p.getLastTestedAt())
                    .provenance(List.of(), p.getManifestId(), null)
                    .self("/v1/precursors/" + p.getId())
                    .build();
        });
    }
}
