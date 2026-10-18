package ai.foresignal.chronicle;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Ulids;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code chronicle:{id}} (prompt 131, contract 1.2), also accepting the display id {@code CHR-yyyy-nnnn}. The
 * envelope carries the display id and name as title, the status, the capture span as time, and the area's geometry
 * when it has one. Access lists and TLP are applied centrally by the 130 envelope decorator: a chronicle the caller
 * may not see is left out.
 */
@Component
public class ChronicleObjectResolver implements ObjectResolver {

    private final ChronicleRepository chronicles;
    private final tools.jackson.databind.json.JsonMapper json;

    /** Creates the resolver. */
    public ChronicleObjectResolver(ChronicleRepository chronicles, tools.jackson.databind.json.JsonMapper json) {
        this.chronicles = chronicles;
        this.json = json;
    }

    @Override
    public String type() {
        return ObjectTypes.CHRONICLE;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        Optional<ChronicleEntity> found = Ulids.isUlid(ref.id()) ? chronicles.findById(ref.id()) : Optional.empty();
        if (found.isEmpty() && ref.id().startsWith("CHR-")) {
            found = chronicles.findByDisplayId(ref.id());
        }
        return found.filter(c -> context.existedAt(c.getCreatedAt())).map(c -> {
            ObjectEnvelope.Builder b = ObjectEnvelope.builder(ObjectRef.of(ObjectTypes.CHRONICLE, c.getId()))
                    .title(c.getDisplayId() + " " + c.getName())
                    .summary(c.getDescription())
                    .status(c.getStatus())
                    .time(c.getStartedAt() != null ? c.getStartedAt() : c.getCreatedAt(), c.getStoppedAt(),
                            c.getUpdatedAt())
                    .self("/v1/chronicles/" + c.getId());
            ChronicleArea area = ChronicleArea.fromMap(c.getArea(), json);
            if (area != null && area.geometry() != null) {
                b.geometry(area.geometry());
            }
            return b.build();
        });
    }
}
