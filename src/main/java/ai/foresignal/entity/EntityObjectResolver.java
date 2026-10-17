package ai.foresignal.entity;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code entity:{id}} (prompt 129): a 098 registry entity (vehicle, vessel, aircraft, facility, …), following
 * {@code mergedInto} to the surviving entity. Entities carry no geometry of their own (their linked records do).
 */
@Component
public class EntityObjectResolver implements ObjectResolver {

    private final EntityRepository entities;

    /** Creates the resolver. */
    public EntityObjectResolver(EntityRepository entities) {
        this.entities = entities;
    }

    @Override
    public String type() {
        return ObjectTypes.ENTITY;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        Optional<EntityEntity> found = entities.findById(ref.id());
        for (int hops = 0; hops < 5 && found.isPresent() && found.get().getMergedInto() != null; hops++) {
            found = entities.findById(found.get().getMergedInto());
        }
        return found.filter(e -> context.existedAt(e.getCreatedAt())).map(e -> ObjectEnvelope
                .builder(ObjectRef.of(ObjectTypes.ENTITY, e.getId()))
                .title(e.getDisplayName() == null ? e.getType() + " " + e.getId() : e.getDisplayName())
                .summary(e.getType() + (e.isProvisional() ? " (provisional)" : ""))
                .status(e.getStatus())
                .time(e.getCreatedAt(), null, e.getUpdatedAt())
                .self("/v1/entities/" + e.getId())
                .build());
    }
}
