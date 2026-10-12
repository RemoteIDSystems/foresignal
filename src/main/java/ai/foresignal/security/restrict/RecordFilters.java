package ai.foresignal.security.restrict;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;

/** Combinators over {@link RecordFilter}s (prompt 036). */
public final class RecordFilters {

    private RecordFilters() {}

    /** A filter that passes only what both {@code a} and {@code b} pass. */
    public static RecordFilter and(RecordFilter a, RecordFilter b) {
        return new And(a, b);
    }

    /** Both filters must pass; each keeps its own membership memory. */
    private record And(RecordFilter a, RecordFilter b) implements RecordFilter {

        @Override
        public String canonical() {
            return "(" + a.canonical() + ")&(" + b.canonical() + ")";
        }

        @Override
        public boolean needsPayload() {
            return a.needsPayload() || b.needsPayload();
        }

        @Override
        public boolean testShape(String sourceId, String kind, Geometry geometry) {
            return a.testShape(sourceId, kind, geometry) && b.testShape(sourceId, kind, geometry);
        }

        @Override
        public boolean testPayload(String sourceId, Map<String, Object> payload) {
            return a.testPayload(sourceId, payload) && b.testPayload(sourceId, payload);
        }

        @Override
        public Optional<String> sql(String sourceColumn, String kindColumn, String geomColumn, String payloadExpression,
                Function<Object, String> bind) {
            List<String> parts = new ArrayList<>();
            a.sql(sourceColumn, kindColumn, geomColumn, payloadExpression, bind).ifPresent(parts::add);
            b.sql(sourceColumn, kindColumn, geomColumn, payloadExpression, bind).ifPresent(parts::add);
            return parts.isEmpty() ? Optional.empty() : Optional.of("(" + String.join(" AND ", parts) + ")");
        }

        @Override
        public void remember(String sourceId, String externalId, boolean member) {
            a.remember(sourceId, externalId, member);
            b.remember(sourceId, externalId, member);
        }

        @Override
        public boolean remembered(String sourceId, String externalId) {
            return a.remembered(sourceId, externalId) && b.remembered(sourceId, externalId);
        }
    }
}
