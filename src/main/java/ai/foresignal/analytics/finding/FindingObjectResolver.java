package ai.foresignal.analytics.finding;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code finding:{id}} (prompt 129): an analytic finding (119) with its detector window, geometry, manifest
 * and {@code asOfSeq}. Private findings are invisible to callers without private access; with {@code asOf}, a finding
 * detected later does not exist.
 */
@Component
public class FindingObjectResolver implements ObjectResolver {

    private final AnalyticFindingRepository findings;

    /** Creates the resolver. */
    public FindingObjectResolver(AnalyticFindingRepository findings) {
        this.findings = findings;
    }

    @Override
    public String type() {
        return ObjectTypes.FINDING;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        return findings.findById(ref.id())
                .filter(f -> !f.isPrivateFinding() || context.seesPrivate())
                .filter(f -> context.existedAt(f.getCreatedAt()))
                .map(f -> {
                    ObjectEnvelope.Builder b = ObjectEnvelope.builder(ref)
                            .title(f.getTitle())
                            .summary(f.getExplanation())
                            .status(f.getStatus())
                            .severity(f.getSeverity())
                            .time(f.getWindowStart(), f.getWindowEnd(), f.getDetectedAt())
                            .privateObject(f.isPrivateFinding())
                            .provenance(List.of(), f.getManifestId(), f.getAsOfSeq())
                            .self("/v1/analytics/findings/" + f.getId());
                    b.geometry(f.getGeom());
                    return b.build();
                });
    }
}
