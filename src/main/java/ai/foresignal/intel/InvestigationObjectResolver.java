package ai.foresignal.intel;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code investigation:{id}} (prompt 129), also accepting the display id {@code INV-yyyy-nnnn}. An
 * investigation is private when any of its items is private (101), and then invisible to callers without private
 * access.
 */
@Component
public class InvestigationObjectResolver implements ObjectResolver {

    private final InvestigationRepository investigations;
    private final InvestigationItemRepository items;

    /** Creates the resolver. */
    public InvestigationObjectResolver(InvestigationRepository investigations, InvestigationItemRepository items) {
        this.investigations = investigations;
        this.items = items;
    }

    @Override
    public String type() {
        return ObjectTypes.INVESTIGATION;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        Optional<InvestigationEntity> found = investigations.findById(ref.id());
        if (found.isEmpty() && ref.id().startsWith("INV-")) {
            found = investigations.findByDisplayId(ref.id());
        }
        return found.filter(i -> context.existedAt(i.getCreatedAt())).flatMap(i -> {
            List<InvestigationItemEntity> all = items.findByInvestigationIdOrderByIdAsc(i.getId());
            boolean priv = all.stream().anyMatch(InvestigationItemEntity::isPrivateItem);
            if (priv && !context.seesPrivate()) {
                return Optional.empty();
            }
            ObjectRef canonical = ObjectRef.of(ObjectTypes.INVESTIGATION, i.getId());
            return Optional.of(ObjectEnvelope.builder(canonical)
                    .title(i.getDisplayId() + " " + i.getTitle())
                    .summary(i.getSummary())
                    .status(i.getStatus())
                    .time(i.getCreatedAt(), null, i.getUpdatedAt())
                    .privateObject(priv)
                    .self("/v1/investigations/" + i.getId())
                    .build());
        });
    }
}
