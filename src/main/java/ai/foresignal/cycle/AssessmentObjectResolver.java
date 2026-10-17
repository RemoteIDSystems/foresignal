package ai.foresignal.cycle;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Resolves {@code assessment:{id}} (prompt 129): a 128 assessment with its headline, cadence and period. */
@Component
public class AssessmentObjectResolver implements ObjectResolver {

    private final AssessmentRepository assessments;

    /** Creates the resolver. */
    public AssessmentObjectResolver(AssessmentRepository assessments) {
        this.assessments = assessments;
    }

    @Override
    public String type() {
        return ObjectTypes.ASSESSMENT;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return assessments.findById(ref.id())
                .filter(a -> !a.isPrivateAssessment() || context.seesPrivate())
                .filter(a -> context.existedAt(a.getCreatedAt()))
                .map(a -> ObjectEnvelope.builder(ref)
                        .title(a.getHeadline() == null ? a.getCadence() + " assessment" : a.getHeadline())
                        .summary(a.getCadence() + " assessment, " + a.getStatus())
                        .status(a.getStatus())
                        .time(a.getPeriodStart(), a.getPeriodEnd(), a.getCreatedAt())
                        .privateObject(a.isPrivateAssessment())
                        .provenance(List.of(), a.getManifestId(), null)
                        .self("/v1/assessments/" + a.getId())
                        .build());
    }
}
