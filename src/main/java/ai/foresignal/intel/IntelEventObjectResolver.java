package ai.foresignal.intel;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code intel-event:{id}} (prompt 129): an intelligence event (101) with its member sources. Private events
 * are invisible to callers without private access (EGR-1); with {@code asOf}, an event created later does not exist.
 */
@Component
public class IntelEventObjectResolver implements ObjectResolver {

    private final IntelligenceEventRepository events;
    private final IntelEventMemberRepository members;

    /** Creates the resolver. */
    public IntelEventObjectResolver(IntelligenceEventRepository events, IntelEventMemberRepository members) {
        this.events = events;
        this.members = members;
    }

    @Override
    public String type() {
        return ObjectTypes.INTEL_EVENT;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return events.findById(ref.id())
                .filter(e -> IntelVisibility.visible(e, context.seesPrivate()))
                .filter(e -> context.existedAt(e.getCreatedAt()))
                .map(e -> {
                    LinkedHashSet<String> sources = new LinkedHashSet<>();
                    for (IntelEventMemberEntity m : members.findByEventIdOrderByIdAsc(e.getId())) {
                        if (m.getSourceId() != null) {
                            sources.add(m.getSourceId());
                        }
                    }
                    ObjectEnvelope.Builder b = ObjectEnvelope.builder(ref)
                            .title(e.getTitle())
                            .summary(e.getExplanation())
                            .status(e.getStatus())
                            .severity(e.getSeverity())
                            .time(e.getStartTime(), e.getEndTime(), e.getLastEvidenceAt())
                            .privateObject(e.isPrivateEvent())
                            .provenance(List.copyOf(sources), e.getManifestId(), null)
                            .self("/v1/intel/events/" + e.getId());
                    b.geometry(e.getGeom() != null ? e.getGeom() : e.getCentroid());
                    return b.build();
                });
    }
}
