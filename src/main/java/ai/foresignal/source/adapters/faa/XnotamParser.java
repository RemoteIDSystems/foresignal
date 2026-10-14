package ai.foresignal.source.adapters.faa;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

/**
 * Parses a tfr.faa.gov XNOTAM-Update detail document ({@code detail_6_8784.xml}) into a {@link Tfr} (prompt 087).
 * Pure; the XML reader is the hardened StAX factory ({@link FaaXml}) and refuses a DOCTYPE.
 *
 * <p><strong>Geometry.</strong> Each {@code TFRAreaGroup} contributes its {@code abdMergedArea} rings (the FAA's
 * merged outline of the area's parts, always plain {@code GRC} vertices in the 2026-10-14 capture of 76 TFRs); a group
 * without one falls back to its {@code Abd} parts. A ring is built from its {@code Avx} vertices:
 * <ul>
 *   <li>{@code GRC} (great circle) and {@code RHL} (rhumb line) vertices are points {@code geoLat}/{@code geoLong}
 *       ({@code 39.00838055N}, {@code 105.41666667W}, datum {@code WGE});
 *   <li>{@code CWA}/{@code CCA} (clockwise/counter-clockwise arc) vertices carry only the center
 *       ({@code geoLatArc}/{@code geoLongArc}) and the radius ({@code valRadiusArc}, {@code uomRadiusArc} NM or KM):
 *       the arc runs from the previous vertex to the next one, densified on the sphere in steps of at most 2°;
 *   <li>a ring that is a lone {@code CIR} vertex (center in {@code geoLat}/{@code geoLong}, radius as above) is a
 *       circle of {@value #CIRCLE_VERTICES} vertices.
 * </ul>
 * Rings are closed; one area is a Polygon and several a MultiPolygon. Invalid shapes are repaired by the normalizer's
 * {@code GeometryFixer} on the normal ingest path.
 *
 * <p><strong>Vertical limits</strong> come from each {@code aseTFRArea} ({@code valDistVerLower/Upper},
 * {@code uomDistVerLower/Upper} {@code FT} or {@code FL}, {@code codeDistVerLower/Upper} {@code SFC}/{@code ALT}/
 * {@code HEI}): the lowest lower limit and the highest upper limit over all areas, in feet, with the codes of those
 * limits.
 *
 * <p><strong>Times.</strong> {@code NotUid/dateIssued}, {@code dateEffective} and {@code dateExpire} are written
 * without an offset and are UTC: {@code codeTimeZone} names only the local zone the NOTAM text quotes. Verified
 * 2026-10-14 on every TFR whose text gives both, for example 6/9370 ({@code codeTimeZone} EDT, {@code dateEffective}
 * 2026-10-16T20:30:00) reads "EFFECTIVE 2610162030 UTC (1630 LOCAL 10/16/26)". A value with an explicit offset is
 * honored. A missing {@code dateExpire} means "until further notice".
 *
 * <p><strong>Text.</strong> {@code txtDescrTraditional}, else {@code txtDescrModern} with its HTML markup removed,
 * capped at {@value #MAX_TEXT} characters. Upstream text: untrusted, stored as data only.
 */
public final class XnotamParser {

    /** The vertices of a lone circle. */
    public static final int CIRCLE_VERTICES = 64;

    /** The largest step of a densified arc, degrees. */
    public static final double ARC_STEP_DEGREES = 2.0;

    /** The longest description kept, in characters. */
    public static final int MAX_TEXT = 8000;

    /** The mean Earth radius used for arcs and circles, meters. */
    static final double EARTH_RADIUS_M = 6_371_008.8;

    private static final Pattern COORDINATE = Pattern.compile("^(\\d{1,3}(?:\\.\\d+)?)([NSEW])$");

    private static final Pattern TAG = Pattern.compile("<[^>]*>");

    private static final GeometryFactory FACTORY = Geo.factory();

    private XnotamParser() {}

    /**
     * One TFR.
     *
     * @param notamId the NOTAM id ({@code 6/8784}), from {@code txtLocalName}
     * @param geometry the areas, or null when none could be built
     * @param areas how many rings were built
     * @param lowerFt the lowest lower limit, feet, or null
     * @param upperFt the highest upper limit, feet, or null
     * @param lowerRef the code of that lower limit ({@code SFC}, {@code ALT}, {@code HEI}), or null
     * @param upperRef the code of that upper limit, or null
     * @param issued {@code dateIssued}, or null
     * @param effective {@code dateEffective}, or null
     * @param expires {@code dateExpire}, or null for "until further notice"
     * @param regulation {@code TfrNot/codeType} ({@code 91.137(a)(1)}), or null
     * @param city {@code txtNameCity}, or null
     * @param stateName {@code txtNameUSState} ({@code OREGON}), or null
     * @param facility {@code codeFacility}, or null
     * @param timeZone {@code codeTimeZone}, the local zone of the text, or null
     * @param description the NOTAM text, or null
     */
    public record Tfr(String notamId, Geometry geometry, int areas, Integer lowerFt, Integer upperFt, String lowerRef,
            String upperRef, Instant issued, Instant effective, Instant expires, String regulation, String city,
            String stateName, String facility, String timeZone, String description) {}

    /**
     * Parses a detail document.
     *
     * @param xml the document
     * @throws UpstreamFormatException when it is not well-formed, declares a DOCTYPE, or has no {@code Not}
     */
    public static Tfr parse(byte[] xml) {
        FaaXml.Node root = FaaXml.read(xml, "XNOTAM detail");
        List<FaaXml.Node> nots = root.descendants("Not");
        if (nots.isEmpty()) {
            throw new UpstreamFormatException("XNOTAM detail has no Not element", "");
        }
        FaaXml.Node not = nots.get(0);
        List<Polygon> polygons = new ArrayList<>();
        List<FaaXml.Node> groups = not.descendants("TFRAreaGroup");
        if (groups.isEmpty()) {
            addRings(not, polygons);
        } else {
            for (FaaXml.Node group : groups) {
                addRings(group, polygons);
            }
        }
        Geometry geometry = polygons.isEmpty() ? null : polygons.size() == 1 ? polygons.get(0)
                : FACTORY.createMultiPolygon(polygons.toArray(Polygon[]::new));

        Integer lower = null;
        Integer upper = null;
        String lowerRef = null;
        String upperRef = null;
        for (FaaXml.Node area : not.descendants("aseTFRArea")) {
            Integer lo = feet(area.text("valDistVerLower"), area.text("uomDistVerLower"));
            Integer hi = feet(area.text("valDistVerUpper"), area.text("uomDistVerUpper"));
            if (lo != null && (lower == null || lo < lower)) {
                lower = lo;
                lowerRef = area.text("codeDistVerLower");
            }
            if (hi != null && (upper == null || hi > upper)) {
                upper = hi;
                upperRef = area.text("codeDistVerUpper");
            }
        }
        String description = not.text("txtDescrTraditional");
        if (description == null) {
            String modern = not.text("txtDescrModern");
            description = modern == null ? null : FaaSources.trimmed(TAG.matcher(modern).replaceAll(" ")
                    .replaceAll("\\s+", " "));
        }
        if (description != null && description.length() > MAX_TEXT) {
            description = description.substring(0, MAX_TEXT);
        }
        return new Tfr(not.text("NotUid", "txtLocalName"), geometry, polygons.size(), lower, upper, lowerRef, upperRef,
                time(not.text("NotUid", "dateIssued")), time(not.text("dateEffective")),
                time(not.text("dateExpire")), not.text("TfrNot", "codeType"), not.text("AffLocGroup", "txtNameCity"),
                not.text("AffLocGroup", "txtNameUSState"), not.text("codeFacility"), not.text("codeTimeZone"),
                description);
    }

    /** Adds the rings of one area group: its merged areas, else its parts. */
    private static void addRings(FaaXml.Node group, List<Polygon> out) {
        List<FaaXml.Node> merged = group.descendants("abdMergedArea");
        List<FaaXml.Node> rings = merged.isEmpty() ? group.descendants("Abd") : merged;
        for (FaaXml.Node ring : rings) {
            Polygon polygon = ring(ring.children("Avx"));
            if (polygon != null) {
                out.add(polygon);
            }
        }
    }

    /**
     * The polygon of one ring of {@code Avx} vertices, or null when it has fewer than three distinct points.
     */
    static Polygon ring(List<FaaXml.Node> vertices) {
        if (vertices.size() == 1 && "CIR".equals(vertices.get(0).text("codeType"))) {
            FaaXml.Node v = vertices.get(0);
            double[] center = point(v.text("geoLat"), v.text("geoLong"));
            if (center == null) {
                center = point(v.text("geoLatArc"), v.text("geoLongArc"));
            }
            Double radius = radiusMeters(v);
            return center == null || radius == null || radius <= 0 ? null : circle(center, radius);
        }
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            FaaXml.Node v = vertices.get(i);
            String type = v.text("codeType");
            if ("CWA".equals(type) || "CCA".equals(type)) {
                double[] center = point(v.text("geoLatArc"), v.text("geoLongArc"));
                Double radius = radiusMeters(v);
                if (center == null || radius == null || coordinates.isEmpty()) {
                    continue;
                }
                Coordinate start = coordinates.get(coordinates.size() - 1);
                double[] end = nextPoint(vertices, i + 1);
                if (end == null && !coordinates.isEmpty()) {
                    end = new double[] {coordinates.get(0).y, coordinates.get(0).x};
                }
                if (end == null) {
                    continue;
                }
                coordinates.addAll(arc(center, radius, new double[] {start.y, start.x}, end, "CWA".equals(type)));
                continue;
            }
            double[] p = point(v.text("geoLat"), v.text("geoLong"));
            if (p != null) {
                coordinates.add(new Coordinate(p[1], p[0]));
            }
        }
        if (coordinates.isEmpty()) {
            return null;
        }
        if (!coordinates.get(0).equals2D(coordinates.get(coordinates.size() - 1))) {
            coordinates.add(new Coordinate(coordinates.get(0)));
        }
        if (coordinates.size() < 4) {
            return null;
        }
        return FACTORY.createPolygon(coordinates.toArray(Coordinate[]::new));
    }

    /** The next vertex at or after {@code from} that is a point, {@code [lat, lon]}, or null. */
    private static double[] nextPoint(List<FaaXml.Node> vertices, int from) {
        for (int i = from; i < vertices.size(); i++) {
            FaaXml.Node v = vertices.get(i);
            String type = v.text("codeType");
            if ("CWA".equals(type) || "CCA".equals(type)) {
                continue;
            }
            double[] p = point(v.text("geoLat"), v.text("geoLong"));
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    /**
     * The intermediate points of an arc around {@code center} from {@code start} to {@code end} (both excluded),
     * clockwise or counter-clockwise, at most {@value #ARC_STEP_DEGREES}° apart, at {@code radius} meters.
     */
    static List<Coordinate> arc(double[] center, double radius, double[] start, double[] end, boolean clockwise) {
        double b0 = bearing(center, start);
        double b1 = bearing(center, end);
        double sweep = clockwise ? normalize(b1 - b0) : normalize(b0 - b1);
        if (sweep == 0) {
            sweep = 360;
        }
        int steps = (int) Math.ceil(sweep / ARC_STEP_DEGREES);
        List<Coordinate> out = new ArrayList<>(Math.max(0, steps - 1));
        for (int i = 1; i < steps; i++) {
            double b = clockwise ? b0 + sweep * i / steps : b0 - sweep * i / steps;
            out.add(destination(center, b, radius));
        }
        return out;
    }

    /** A closed circle of {@value #CIRCLE_VERTICES} vertices around {@code center} ({@code [lat, lon]}). */
    static Polygon circle(double[] center, double radius) {
        Coordinate[] ring = new Coordinate[CIRCLE_VERTICES + 1];
        for (int i = 0; i < CIRCLE_VERTICES; i++) {
            ring[i] = destination(center, 360.0 * i / CIRCLE_VERTICES, radius);
        }
        ring[CIRCLE_VERTICES] = new Coordinate(ring[0]);
        return FACTORY.createPolygon(ring);
    }

    /** The point {@code distance} meters from {@code from} ({@code [lat, lon]}) on initial bearing {@code bearing}. */
    static Coordinate destination(double[] from, double bearing, double distance) {
        double lat1 = Math.toRadians(from[0]);
        double lon1 = Math.toRadians(from[1]);
        double b = Math.toRadians(bearing);
        double d = distance / EARTH_RADIUS_M;
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d) + Math.cos(lat1) * Math.sin(d) * Math.cos(b));
        double lon2 = lon1 + Math.atan2(Math.sin(b) * Math.sin(d) * Math.cos(lat1),
                Math.cos(d) - Math.sin(lat1) * Math.sin(lat2));
        double lon = Math.toDegrees(lon2);
        lon = ((lon + 540) % 360) - 180;
        return new Coordinate(lon, Math.toDegrees(lat2));
    }

    /** The initial great-circle bearing from {@code a} to {@code b} ({@code [lat, lon]}), 0–360°. */
    static double bearing(double[] a, double[] b) {
        double lat1 = Math.toRadians(a[0]);
        double lat2 = Math.toRadians(b[0]);
        double dLon = Math.toRadians(b[1] - a[1]);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        return normalize(Math.toDegrees(Math.atan2(y, x)));
    }

    private static double normalize(double degrees) {
        double d = degrees % 360;
        return d < 0 ? d + 360 : d;
    }

    /** The radius of an arc or circle vertex in meters ({@code NM}, {@code KM}, {@code M} or {@code FT}), or null. */
    static Double radiusMeters(FaaXml.Node vertex) {
        String value = vertex.text("valRadiusArc");
        if (value == null) {
            return null;
        }
        double radius;
        try {
            radius = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
        String unit = vertex.text("uomRadiusArc");
        return switch (unit == null ? "NM" : unit.toUpperCase(Locale.ROOT)) {
            case "KM" -> radius * 1000;
            case "M" -> radius;
            case "FT" -> radius * 0.3048;
            case "SM", "MI" -> radius * 1609.344;
            default -> radius * 1852;
        };
    }

    /** {@code [lat, lon]} of a coordinate pair such as {@code 39.00838055N}, {@code 105.41666667W}, or null. */
    static double[] point(String lat, String lon) {
        Double y = coordinate(lat, 'N', 'S', 90);
        Double x = coordinate(lon, 'E', 'W', 180);
        return x == null || y == null ? null : new double[] {y, x};
    }

    /** One signed coordinate, or null when it does not read or is out of range. */
    static Double coordinate(String text, char positive, char negative, double limit) {
        if (text == null) {
            return null;
        }
        Matcher m = COORDINATE.matcher(text.strip().toUpperCase(Locale.ROOT));
        if (!m.matches()) {
            return null;
        }
        char hemisphere = m.group(2).charAt(0);
        if (hemisphere != positive && hemisphere != negative) {
            return null;
        }
        double value = Double.parseDouble(m.group(1));
        if (value > limit) {
            return null;
        }
        return hemisphere == negative ? -value : value;
    }

    /** A vertical limit in feet: {@code FL} values times 100, {@code FT} as given; null otherwise. */
    static Integer feet(String value, String unit) {
        if (value == null) {
            return null;
        }
        int number;
        try {
            number = (int) Math.round(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return null;
        }
        if (unit != null && "FL".equalsIgnoreCase(unit.strip())) {
            return number * 100;
        }
        if (unit != null && "M".equalsIgnoreCase(unit.strip())) {
            return (int) Math.round(number / 0.3048);
        }
        return number;
    }

    /** An XNOTAM time: an ISO-8601 local date-time read as UTC, or one with an offset; null otherwise. */
    static Instant time(String text) {
        if (text == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(text).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
