package ai.foresignal.feed;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/** Fixed feeds for the writer tests (prompt 058). */
final class FeedFixtures {

    static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);
    static final Instant FIRED = Instant.parse("2026-10-13T10:00:00Z");
    static final Instant ACKED = Instant.parse("2026-10-13T10:05:30.250Z");

    private FeedFixtures() {}

    static Geometry point(double lon, double lat) {
        return GEOMETRY.createPoint(new Coordinate(lon, lat));
    }

    /** A closed ring polygon with {@code vertices} distinct vertices around (lon, lat). */
    static Geometry polygon(int vertices) {
        Coordinate[] ring = new Coordinate[vertices + 1];
        for (int i = 0; i < vertices; i++) {
            double a = 2 * Math.PI * i / vertices;
            ring[i] = new Coordinate(-97 + Math.cos(a), 30 + Math.sin(a));
        }
        ring[vertices] = ring[0];
        return GEOMETRY.createPolygon(ring);
    }

    static FeedEntry entry(long id, Geometry geometry) {
        Map<String, Object> extension = new LinkedHashMap<>();
        extension.put("severity", "critical");
        extension.put("state", "acknowledged");
        extension.put("sourceId", "usgs-earthquakes");
        extension.put("externalId", "us7000abcd");
        extension.put("geometry", Map.of("type", "Point", "coordinates", List.of(-97.5, 30.25)));
        return new FeedEntry("tag:foresignal,2026:INSTALL/alert/" + id, "Quakes <M5> & \"more\"",
                "https://fs.example/v1/alerts/" + id, FIRED, ACKED, "<p>Body &amp; <strong>bold</strong></p>",
                "Body & bold", List.of(new FeedEntry.Category("critical", FeedService.SEVERITY_SCHEME),
                        new FeedEntry.Category("ops", FeedService.TAG_SCHEME)),
                geometry, extension);
    }

    static Feed feed(FeedEntry... entries) {
        return new Feed("tag:foresignal,2026:INSTALL/alerts", "Foresignal alerts", "Alerts, newest first",
                "https://fs.example/v1/alerts/feed.atom", "https://fs.example/v1/alerts", ACKED, List.of(entries));
    }
}
