package ai.foresignal.inbound.edp.binding;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.observability.ExternalCoverage;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The declared coverage of streams bound with target {@code canonical-family} (prompt 138), for the 116
 * observability model: the stream kinds a family gains, and each bound stream's manifest {@code coverage}
 * ({@code area} footprint, {@code pDetect}, default 1 inside the footprint) as a low-confidence {@code declared}
 * estimate of its provider for that family. A snapshot of the enabled bindings, rebuilt when bindings change.
 */
@Component
public class ExternalCoverageRegistry implements ExternalCoverage, IngestStateReset {

    private final ExternalBindingRepository bindings;
    private final BindingStreams streams;
    private final GeoJsonReader geoJson = new GeoJsonReader();
    private volatile Snapshot snapshot;

    /** Creates the registry. */
    public ExternalCoverageRegistry(ExternalBindingRepository bindings, BindingStreams streams) {
        this.bindings = bindings;
        this.streams = streams;
    }

    /**
     * The bound streams of each family.
     *
     * @param kinds the kinds bound to each family
     * @param coverage the declared coverage per {@code sourceId + '\u0001' + family}
     */
    record Snapshot(Map<String, Set<String>> kinds, Map<String, Declared> coverage) {}

    @Override
    public Set<String> kindsOf(String family) {
        return snapshot().kinds().getOrDefault(family, Set.of());
    }

    @Override
    public Declared coverage(String sourceId, String family) {
        return snapshot().coverage().get(sourceId + '\u0001' + family);
    }

    /** Drops the snapshot so the next read rebuilds it (bindings changed). */
    public void invalidate() {
        snapshot = null;
    }

    @Override
    public void resetState() {
        invalidate();
    }

    private Snapshot snapshot() {
        Snapshot s = snapshot;
        if (s == null) {
            s = build();
            snapshot = s;
        }
        return s;
    }

    private Snapshot build() {
        Map<String, Set<String>> kinds = new LinkedHashMap<>();
        Map<String, Declared> coverage = new LinkedHashMap<>();
        List<ExternalBindingEntity> rows;
        try {
            rows = new ArrayList<>(bindings.findByTargetOrderByCreatedAtAscIdAsc(BindingVocabulary.CANONICAL_FAMILY));
        } catch (RuntimeException e) {
            return new Snapshot(Map.of(), Map.of());
        }
        for (ExternalBindingEntity b : rows) {
            String family = BindingConfig.text(b.getConfig(), "family");
            if (family == null || !b.routable()) {
                continue;
            }
            Optional<BoundStream> stream = streams.stream(b);
            if (stream.isEmpty()) {
                continue;
            }
            kinds.computeIfAbsent(family, k -> new LinkedHashSet<>()).add(stream.get().kind());
            Declared declared = declared(stream.get().coverage());
            if (declared != null) {
                coverage.merge(b.getSourceId() + '\u0001' + family, declared,
                        (a, c) -> a.pDetect() >= c.pDetect() ? a : c);
            }
        }
        return new Snapshot(kinds, coverage);
    }

    /** A manifest {@code coverage} object as a declaration; null when absent or unreadable. */
    Declared declared(JsonNode coverage) {
        if (coverage == null) {
            return null;
        }
        JsonNode p = coverage.get("pDetect");
        double pDetect = p != null && p.isNumber() ? p.asDouble() : 1.0;
        JsonNode area = coverage.get("area");
        Geometry geometry = null;
        if (area != null && area.isObject()) {
            try {
                geometry = geoJson.read(area);
            } catch (RuntimeException e) {
                return null;
            }
        }
        return new Declared(geometry, pDetect);
    }
}
