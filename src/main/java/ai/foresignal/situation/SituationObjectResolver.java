package ai.foresignal.situation;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code situation:{id}} (prompt 129). With {@code asOf}, a situation created later does not exist, and its
 * status and severity come from the latest daily snapshot (118) on or before that day; title and geometry are the
 * current ones. Private situations are invisible to callers without private access.
 */
@Component
public class SituationObjectResolver implements ObjectResolver {

    private final SituationRepository situations;
    private final SituationSnapshotRepository snapshots;

    /** Creates the resolver. */
    public SituationObjectResolver(SituationRepository situations, SituationSnapshotRepository snapshots) {
        this.situations = situations;
        this.snapshots = snapshots;
    }

    @Override
    public String type() {
        return ObjectTypes.SITUATION;
    }

    @Override
    public boolean asOfAware() {
        return true;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return situations.findById(ref.id())
                .filter(s -> !s.isPrivateSituation() || context.seesPrivate())
                .filter(s -> context.existedAt(s.getCreatedAt()))
                .map(s -> envelope(ref, s, context));
    }

    private ObjectEnvelope envelope(ObjectRef ref, SituationEntity s, ResolveContext context) {
        String status = s.getStatus();
        String severity = s.getSeverity();
        Long asOfSeq = null;
        Instant end = s.getClosedAt();
        if (context.asOf() != null) {
            var day = context.asOf().atZone(ZoneOffset.UTC).toLocalDate();
            var snap = snapshots.findTop1BySituationIdAndDayLessThanEqualOrderByDayDesc(s.getId(), day);
            if (snap.isPresent()) {
                status = snap.get().getStatus();
                severity = snap.get().getSeverity();
                asOfSeq = snap.get().getAsofSeq();
            }
            if (end != null && end.isAfter(context.asOf())) {
                end = null;
            }
        }
        ObjectEnvelope.Builder b = ObjectEnvelope.builder(ref)
                .title(s.getTitle())
                .summary(s.getSummary())
                .status(status)
                .severity(severity)
                .time(s.getStartedAt(), end, s.getLastActivityAt())
                .privateObject(s.isPrivateSituation())
                .provenance(List.of(), s.getManifestId(), asOfSeq)
                .self("/v1/situations/" + s.getId());
        if (s.getGeom() != null) {
            b.geometry(s.getGeom());
        } else if (s.getCentroid() != null) {
            b.geometry(s.getCentroid());
        }
        return b.build();
    }
}
