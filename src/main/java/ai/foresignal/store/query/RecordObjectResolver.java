package ai.foresignal.store.query;

import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectResolver;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Resolves {@code record:{sourceId}/{externalId}[@{seq}]} (prompt 129): the current head plus live state; the pinned
 * version for {@code @seq}; or the version known at {@code asOf} (037 time travel, 400 {@code ASOF_BEYOND_RETENTION}
 * beyond the source's horizon). Records of excluded sources are invisible; records of private sources are private.
 */
@Component
public class RecordObjectResolver implements ObjectResolver {

    private final RecordQueryService records;
    private final AsOfQueryService asOf;
    private final SourceVisibility visibility;
    private final GeoJsonReader reader = new GeoJsonReader();

    /** Creates the resolver. */
    public RecordObjectResolver(RecordQueryService records, AsOfQueryService asOf, SourceVisibility visibility) {
        this.records = records;
        this.asOf = asOf;
        this.visibility = visibility;
    }

    @Override
    public String type() {
        return ObjectTypes.RECORD;
    }

    @Override
    public boolean asOfAware() {
        return true;
    }

    @Override
    public Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context) {
        String sourceId = ref.recordSourceId();
        String externalId = ref.recordExternalId();
        Set<String> excluded = context.caller().excludedSources();
        if (excluded.contains(sourceId)) {
            return Optional.empty();
        }
        Long seq = ref.recordSeq();
        Optional<RecordRow> row;
        if (seq != null) {
            row = Optional.ofNullable(records.versionsBySeq(List.of(seq)).get(seq))
                    .filter(r -> r.sourceId().equals(sourceId) && r.externalId().equals(externalId));
        } else if (context.asOf() != null) {
            AsOfQueryService.AsOfQuery q = new AsOfQueryService.AsOfQuery(context.asOf(), List.of(sourceId), null,
                    List.of(externalId), null, null, null, 1, FieldSelection.ALL, GeometryOptions.DEFAULT,
                    GeometryEncoding.GEOJSON, false, excluded);
            row = asOf.list(q).rows().stream().findFirst();
        } else {
            row = records.get(sourceId, externalId, false, FieldSelection.ALL, GeometryOptions.DEFAULT, excluded);
        }
        return row.filter(r -> !r.tombstone()).map(r -> envelope(ref, r));
    }

    private ObjectEnvelope envelope(ObjectRef ref, RecordRow r) {
        ObjectEnvelope.Builder b = ObjectEnvelope.builder(ref)
                .title(r.title() == null ? r.kind() + " " + r.externalId() : r.title())
                .summary(r.kind() + " from " + r.sourceId() + ", version " + r.version())
                .status(Boolean.FALSE.equals(r.live()) ? "expired" : "current")
                .time(r.validFrom() != null ? r.validFrom() : r.eventTime(), r.validUntil(), r.observedAt())
                .privateObject(isPrivate(r.sourceId()))
                .provenance(List.of(r.sourceId()), null, r.seq())
                .self("/v1/records/" + r.sourceId() + "/" + java.net.URLEncoder.encode(r.externalId(),
                        java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));
        if (r.geometry() != null && !r.geometry().isBlank()) {
            try {
                b.geometry(reader.read(r.geometry()));
            } catch (RuntimeException e) {
                // An unreadable geometry leaves the envelope without one.
            }
        }
        return b.build();
    }

    private boolean isPrivate(String sourceId) {
        try {
            return visibility.isPrivate(sourceId);
        } catch (RuntimeException e) {
            return true;
        }
    }
}
