package ai.foresignal.report;

import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.Ulids;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code report:{id}} (prompt 132, contract 1.3), also accepting the display id {@code RPT-yyyy-nnnn}, and
 * {@code report:{id}#{blockId}}, one block of a report (the target of a comment on that block). The
 * envelope carries the display id and title, the status, the time window (else the creation and publication times),
 * and the area's geometry when it has one. Access lists and TLP are applied centrally by the 130 envelope decorator: a
 * report the caller may not see is left out.
 */
@Component
public class ReportObjectResolver implements ObjectResolver {

    private final ReportRepository reports;

    /** Creates the resolver. */
    public ReportObjectResolver(ReportRepository reports) {
        this.reports = reports;
    }

    @Override
    public String type() {
        return ObjectTypes.REPORT;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        int hash = ref.id().indexOf('#');
        String id = hash > 0 ? ref.id().substring(0, hash) : ref.id();
        String blockId = hash > 0 ? ref.id().substring(hash + 1) : null;
        Optional<ReportEntity> found = Ulids.isUlid(id) ? reports.findById(id) : Optional.empty();
        if (found.isEmpty() && id.startsWith(ReportVocabulary.DISPLAY_PREFIX)) {
            found = reports.findByDisplayId(id);
        }
        if (blockId != null) {
            return found.filter(r -> context.existedAt(r.getCreatedAt())).flatMap(r -> block(r, blockId));
        }
        return found.filter(r -> context.existedAt(r.getCreatedAt())).map(r -> {
            java.time.Instant start = r.getCreatedAt();
            java.time.Instant end = null;
            if (r.getTimeWindow() != null && r.getTimeWindow().get("start") instanceof String s) {
                start = java.time.Instant.parse(s);
                end = r.getTimeWindow().get("end") instanceof String e ? java.time.Instant.parse(e) : null;
            }
            ObjectEnvelope.Builder b = ObjectEnvelope.builder(ObjectRef.of(ObjectTypes.REPORT, r.getId()))
                    .title(r.getDisplayId() + " " + r.getTitle())
                    .summary(r.getSubtitle())
                    .status(r.getStatus())
                    .time(start, end, r.getPublishedAt() != null ? r.getPublishedAt() : r.getUpdatedAt())
                    .provenance(java.util.List.of(), r.getManifestId(), null)
                    .self("/v1/reports/" + r.getId());
            if (r.getArea() != null) {
                double[] box = ReportPublisher.envelope(r.getArea());
                if (box != null) {
                    b.point((box[0] + box[2]) / 2, (box[1] + box[3]) / 2);
                }
            }
            return b.build();
        });
    }

    /** The envelope of one block of a report, or empty when the report has no such block. */
    private static Optional<ObjectEnvelope> block(ReportEntity r, String blockId) {
        for (java.util.Map<String, Object> b : r.getSections()) {
            if (blockId.equals(b.get("id"))) {
                Object title = b.get("title");
                String label = title instanceof String t && !t.isBlank() ? t : String.valueOf(b.get("type"));
                return Optional.of(ObjectEnvelope.builder(ObjectRef.of(ObjectTypes.REPORT, r.getId() + "#" + blockId))
                        .title(r.getDisplayId() + " " + r.getTitle() + ": " + label)
                        .status(r.getStatus())
                        .time(r.getCreatedAt(), null, r.getUpdatedAt())
                        .self("/v1/reports/" + r.getId())
                        .build());
            }
        }
        return Optional.empty();
    }
}
