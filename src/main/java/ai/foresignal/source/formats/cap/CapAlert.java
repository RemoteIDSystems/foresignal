package ai.foresignal.source.formats.cap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

/**
 * One parsed OASIS CAP 1.2 {@code alert} (the subset Foresignal uses).
 *
 * @param identifier the alert identifier
 * @param sender the sender
 * @param sent when it was sent
 * @param status {@code Actual}, {@code Exercise}, {@code System}, {@code Test} or {@code Draft}
 * @param msgType {@code Alert}, {@code Update}, {@code Cancel}, {@code Ack} or {@code Error}
 * @param scope {@code Public}, {@code Restricted} or {@code Private}
 * @param references the earlier messages this one updates or cancels
 * @param infos the {@code info} blocks, in document order
 */
public record CapAlert(
        String identifier,
        String sender,
        Instant sent,
        String status,
        String msgType,
        String scope,
        List<Reference> references,
        List<Info> infos) {

    /** Copies the lists. */
    public CapAlert {
        references = references == null ? List.of() : List.copyOf(references);
        infos = infos == null ? List.of() : List.copyOf(infos);
    }

    /** The first info block, or null. */
    public Info firstInfo() {
        return infos.isEmpty() ? null : infos.get(0);
    }

    /**
     * Every area polygon of every info block as one geometry (SRID 4326): null when there is none,
     * the polygon itself when there is one, else a MultiPolygon.
     */
    public Geometry geometry(GeometryFactory factory) {
        List<Polygon> polygons = new ArrayList<>();
        for (Info info : infos) {
            for (Area area : info.areas()) {
                polygons.addAll(area.polygons());
            }
        }
        if (polygons.isEmpty()) {
            return null;
        }
        Geometry geometry = polygons.size() == 1
                ? polygons.get(0)
                : factory.createMultiPolygon(polygons.toArray(new Polygon[0]));
        geometry.setSRID(factory.getSRID());
        return geometry;
    }

    /**
     * One {@code references} entry, {@code sender,identifier,sent}.
     *
     * @param sender the referenced sender
     * @param identifier the referenced identifier
     * @param sent the referenced sent time as written, or null
     */
    public record Reference(String sender, String identifier, String sent) {}

    /**
     * One {@code info} block.
     *
     * @param language the language, or null
     * @param event the event type
     * @param urgency the urgency
     * @param severity the severity
     * @param certainty the certainty
     * @param headline the headline, or null
     * @param description the description, or null
     * @param instruction the instruction, or null
     * @param onset the expected onset, or null
     * @param effective when the information becomes effective, or null
     * @param expires when it expires, or null
     * @param areas the areas
     */
    public record Info(
            String language,
            String event,
            String urgency,
            String severity,
            String certainty,
            String headline,
            String description,
            String instruction,
            Instant onset,
            Instant effective,
            Instant expires,
            List<Area> areas) {

        /** Copies the list. */
        public Info {
            areas = areas == null ? List.of() : List.copyOf(areas);
        }
    }

    /**
     * One {@code area} block; circles are already converted to polygons.
     *
     * @param areaDesc the area description
     * @param polygons the polygons and circles, as lon/lat polygons in SRID 4326
     * @param geocodes the geocodes
     */
    public record Area(String areaDesc, List<Polygon> polygons, List<Geocode> geocodes) {

        /** Copies the lists. */
        public Area {
            polygons = polygons == null ? List.of() : List.copyOf(polygons);
            geocodes = geocodes == null ? List.of() : List.copyOf(geocodes);
        }
    }

    /**
     * One {@code geocode}.
     *
     * @param valueName the code system, for example {@code SAME} or {@code UGC}
     * @param value the code
     */
    public record Geocode(String valueName, String value) {}
}
