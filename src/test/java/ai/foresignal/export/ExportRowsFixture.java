package ai.foresignal.export;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.WKBWriter;

/** Builds export rows (the maps {@link ExportRows#map} produces) for the writer tests (prompt 111). */
final class ExportRowsFixture {

    static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);
    static final Instant T0 = Instant.parse("2026-03-01T12:00:00.123456Z");

    private ExportRowsFixture() {}

    /** A point. */
    static Geometry point(double lon, double lat) {
        return WGS84.createPoint(new Coordinate(lon, lat));
    }

    /** A two-vertex line. */
    static Geometry line(double lon, double lat) {
        return WGS84.createLineString(new Coordinate[] {new Coordinate(lon, lat), new Coordinate(lon + 1, lat + 1)});
    }

    /** Little-endian 2D WKB of {@code geometry}. */
    static byte[] wkb(Geometry geometry) {
        return new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN).write(geometry);
    }

    /** A row of {@code source}/{@code externalId} at {@code seq}, with {@code geometry} (null for none). */
    static Map<String, Object> row(String source, String externalId, String kind, long seq, Geometry geometry) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("source_id", source);
        row.put("external_id", externalId);
        row.put("kind", kind);
        row.put("seq", seq);
        row.put("version", 1);
        row.put("observed_at", T0.plusSeconds(seq));
        row.put("event_time", null);
        row.put("valid_until", null);
        row.put("ingested_at", T0.plusSeconds(seq + 1));
        row.put("title", "Title <" + externalId + "> & co");
        row.put("payload", "{\"v\":" + seq + ",\"alt_geom\":" + (seq * 10) + "}");
        if (geometry != null) {
            row.put(ExportWriter.GEOMETRY, wkb(geometry));
            Envelope e = geometry.getEnvelopeInternal();
            row.put(ExportWriter.BBOX, new double[] {e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()});
        }
        return row;
    }
}
