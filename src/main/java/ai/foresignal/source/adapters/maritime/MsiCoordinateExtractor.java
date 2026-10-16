package ai.foresignal.source.adapters.maritime;

import ai.foresignal.raster.geotiff.Antimeridian;
import ai.foresignal.source.spi.Geo;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * Recovers geometry from the free text of an NGA navigational warning (prompt 088). Pure: no I/O, no state.
 *
 * <p><strong>Positions</strong> are {@code DD-MM.mmN DDD-MM.mmW} or {@code DD-MM-SS.sN DDD-MM-SS.sW}, with or without
 * the space (or a comma or line break) between latitude and longitude. Hemispheres are {@code N}/{@code S} and
 * {@code E}/{@code W}; degrees are at most 90 and 180, minutes and seconds below 60, and a position with seconds has
 * whole minutes. A position failing any check is dropped, never guessed.
 *
 * <p><strong>Geometry rules</strong>, the first that applies wins:
 * <ol>
 *   <li>{@code BOUNDED BY}, {@code AREA BOUNDED} or {@code BOUND BY} (the live texts say "IN AREA BOUND BY", verified
 *       2026-10-14) followed by at least 3 positions: a closed Polygon. When the area list is lettered ({@code A.},
 *       {@code B.}, …) and at least two letters carry 3 or more positions each, a MultiPolygon of those areas;
 *   <li>{@code WITHIN <n> (MILES|NM|METERS|…) OF <position>}: a geodesic circle of {@value #CIRCLE_VERTICES}
 *       vertices (miles are nautical miles; {@code n} may be a word, "THREE"); several such phrases make a
 *       MultiPolygon;
 *   <li>{@code TRACKLINE} or {@code BETWEEN <position> AND <position>} with at least 2 positions: a LineString of the
 *       positions after the keyword; lettered tracklines with 2 or more positions each make a MultiLineString;
 *   <li>otherwise every position: a MultiPoint, or a Point for a single one;
 *   <li>no positions: no geometry.
 * </ol>
 *
 * <p><strong>The antimeridian.</strong> Every geometry lies within ±180° longitude, as the normalizer requires. A
 * circle whose ring runs past ±180°, and a polygon or trackline whose consecutive positions are more than 180° of
 * longitude apart (for example 179-30E to 179-30W), is taken as crossing the antimeridian: its longitudes are made
 * continuous ({@link Antimeridian#unwrap}) and it is cut at ±180° into a MultiPolygon or MultiLineString with one
 * part on each side. A circle that encloses a pole becomes a polar cap reaching the pole.
 */
public final class MsiCoordinateExtractor {

    /** The vertices of a circle (the ring has one more, closing it). */
    public static final int CIRCLE_VERTICES = 64;

    /** The mean Earth radius used for circles, meters. */
    static final double EARTH_RADIUS_M = 6_371_008.8;

    /** One nautical mile, meters. */
    static final double NAUTICAL_MILE_M = 1852.0;

    /** One position: latitude, optional separator, longitude. */
    static final Pattern POSITION = Pattern.compile(
            "(?<![\\d.-])(\\d{1,2})-(\\d{1,2}(?:\\.\\d+)?)(?:-(\\d{1,2}(?:\\.\\d+)?))?\\s?([NS])"
                    + "[\\s,]{0,4}"
                    + "(\\d{1,3})-(\\d{1,2}(?:\\.\\d+)?)(?:-(\\d{1,2}(?:\\.\\d+)?))?\\s?([EW])(?![A-Z])");

    /** The polygon keywords. */
    static final Pattern BOUNDED = Pattern.compile("\\b(?:BOUNDED\\s+BY|AREAS?\\s+BOUNDED|BOUND\\s+BY)\\b");

    /** The circle phrase; the position must follow {@code OF} directly. */
    static final Pattern WITHIN = Pattern.compile("\\bWITHIN\\s+(\\d+(?:\\.\\d+)?|[A-Z]+(?:-[A-Z]+)?)\\s+"
            + "(NAUTICAL\\s+MILES?|MILES?|NM|NMI|METERS?|METRES?|KILOMETERS?|KM|YARDS?|CABLES?)\\s+OF\\s+(?=\\d)");

    /** The trackline keyword. */
    static final Pattern TRACKLINE = Pattern.compile("\\bTRACKLINES?\\b");

    /** A {@code BETWEEN <position> AND <position>} phrase. */
    static final Pattern BETWEEN = Pattern.compile("\\bBETWEEN\\s+(?=\\d{1,2}-\\d)");

    /** A lettered item at the start of a line ({@code A.}, {@code AB.}). */
    static final Pattern LETTER = Pattern.compile("(?m)^\\s*([A-Z]{1,2})\\.\\s");

    /** Number words used in warnings. */
    static final Map<String, Double> WORDS = Map.ofEntries(Map.entry("ONE", 1.0), Map.entry("TWO", 2.0),
            Map.entry("THREE", 3.0), Map.entry("FOUR", 4.0), Map.entry("FIVE", 5.0), Map.entry("SIX", 6.0),
            Map.entry("SEVEN", 7.0), Map.entry("EIGHT", 8.0), Map.entry("NINE", 9.0), Map.entry("TEN", 10.0),
            Map.entry("ELEVEN", 11.0), Map.entry("TWELVE", 12.0), Map.entry("FIFTEEN", 15.0),
            Map.entry("TWENTY", 20.0), Map.entry("TWENTY-FIVE", 25.0), Map.entry("THIRTY", 30.0),
            Map.entry("FIFTY", 50.0), Map.entry("HALF", 0.5));

    /** Which rule produced the geometry. */
    public enum Rule {
        /** Rule 1: an area bounded by positions. */
        POLYGON,
        /** Rule 2: a circle around a position. */
        CIRCLE,
        /** Rule 3: a trackline. */
        TRACKLINE,
        /** Rule 4: the positions themselves. */
        POINTS,
        /** Rule 5: no positions. */
        NONE
    }

    /**
     * The extraction.
     *
     * @param geometry the geometry in SRID 4326, or null for {@link Rule#NONE}
     * @param rule the rule that produced it
     * @param positions every valid position in the text, in order
     */
    public record Result(Geometry geometry, Rule rule, List<Coordinate> positions) {

        /** Copies the list. */
        public Result {
            positions = positions == null ? List.of() : List.copyOf(positions);
        }
    }

    /** A position and where it was found. */
    record Found(Coordinate coordinate, int start, int end) {}

    private MsiCoordinateExtractor() {}

    /**
     * Extracts the geometry of a warning text.
     *
     * @param text the warning text, possibly null
     * @return the result; never null
     */
    public static Result extract(String text) {
        String upper = text == null ? "" : text.toUpperCase(Locale.ROOT);
        List<Found> all = positions(upper, 0, upper.length());
        List<Coordinate> coordinates = all.stream().map(Found::coordinate).toList();
        if (all.isEmpty()) {
            return new Result(null, Rule.NONE, coordinates);
        }
        GeometryFactory gf = Geo.factory();
        Matcher bounded = BOUNDED.matcher(upper);
        if (bounded.find()) {
            Geometry polygon = polygon(upper, bounded.end(), gf);
            if (polygon != null) {
                return new Result(polygon, Rule.POLYGON, coordinates);
            }
        }
        Geometry circles = circles(upper, gf);
        if (circles != null) {
            return new Result(circles, Rule.CIRCLE, coordinates);
        }
        Matcher track = TRACKLINE.matcher(upper);
        Matcher between = BETWEEN.matcher(upper);
        int trackStart = track.find() ? track.end() : -1;
        if (trackStart < 0 && between.find()) {
            List<Found> after = positions(upper, between.start(), upper.length());
            if (after.size() >= 2 && upper.substring(after.get(0).end(), after.get(1).start()).contains("AND")) {
                trackStart = between.start();
            }
        }
        if (trackStart >= 0) {
            Geometry line = line(upper, trackStart, gf);
            if (line != null) {
                return new Result(line, Rule.TRACKLINE, coordinates);
            }
        }
        if (coordinates.size() == 1) {
            return new Result(gf.createPoint(coordinates.getFirst()), Rule.POINTS, coordinates);
        }
        return new Result(gf.createMultiPointFromCoords(coordinates.toArray(new Coordinate[0])), Rule.POINTS,
                coordinates);
    }

    /** Rule 1: the polygon or polygons after the keyword, or null with fewer than 3 positions. */
    private static Geometry polygon(String text, int from, GeometryFactory gf) {
        List<List<Coordinate>> groups = lettered(text, from);
        List<Polygon> polygons = new ArrayList<>();
        int areas = 0;
        for (List<Coordinate> group : groups) {
            if (group.size() >= 3) {
                polygons.addAll(polygonParts(closed(group), gf));
                areas++;
            }
        }
        if (areas >= 2) {
            return polygonal(polygons, gf);
        }
        List<Coordinate> after = positions(text, from, text.length()).stream().map(Found::coordinate).toList();
        return after.size() >= 3 ? polygonal(polygonParts(closed(after), gf), gf) : null;
    }

    /** One Polygon, or a MultiPolygon of several parts. */
    private static Geometry polygonal(List<Polygon> parts, GeometryFactory gf) {
        return parts.size() == 1 ? parts.getFirst() : gf.createMultiPolygon(parts.toArray(new Polygon[0]));
    }

    /**
     * The closed ring {@code ring} as polygons inside ±180°: one polygon when it does not cross the antimeridian,
     * otherwise its parts on each side.
     *
     * @param ring a closed ring whose longitudes may lie anywhere (they are made continuous first)
     * @param gf the geometry factory
     * @return one or more polygons; never empty
     */
    static List<Polygon> polygonParts(Coordinate[] ring, GeometryFactory gf) {
        Coordinate[] unwrapped = copy(ring);
        Antimeridian.unwrap(unwrapped);
        if (!unwrapped[0].equals2D(unwrapped[unwrapped.length - 1])) {
            // The ring winds once around a pole, so continuous longitudes cannot close it: keep the wrapped ring.
            return List.of(gf.createPolygon(rounded(wrapped(ring))));
        }
        if (inRange(unwrapped)) {
            return List.of(gf.createPolygon(rounded(unwrapped)));
        }
        MultiPolygon split = Antimeridian.split(gf.createPolygon(unwrapped));
        List<Polygon> parts = new ArrayList<>();
        for (int i = 0; i < split.getNumGeometries(); i++) {
            parts.add(gf.createPolygon(rounded(split.getGeometryN(i).getCoordinates())));
        }
        return parts.isEmpty() ? List.of(gf.createPolygon(rounded(wrapped(ring)))) : parts;
    }

    /** Rule 3: the trackline or tracklines after {@code from}, or null with fewer than 2 positions. */
    private static Geometry line(String text, int from, GeometryFactory gf) {
        List<List<Coordinate>> groups = lettered(text, from);
        List<LineString> lines = new ArrayList<>();
        int tracks = 0;
        for (List<Coordinate> group : groups) {
            if (group.size() >= 2) {
                lines.addAll(lineParts(group.toArray(new Coordinate[0]), gf));
                tracks++;
            }
        }
        if (tracks >= 2) {
            return gf.createMultiLineString(lines.toArray(new LineString[0]));
        }
        List<Coordinate> after = positions(text, from, text.length()).stream().map(Found::coordinate).toList();
        if (after.size() < 2) {
            return null;
        }
        List<LineString> parts = lineParts(after.toArray(new Coordinate[0]), gf);
        return parts.size() == 1 ? parts.getFirst() : gf.createMultiLineString(parts.toArray(new LineString[0]));
    }

    /**
     * The line through {@code points} inside ±180°: one line when it does not cross the antimeridian, otherwise its
     * pieces on each side.
     *
     * @param points the positions in order, at least two
     * @param gf the geometry factory
     * @return one or more lines; never empty
     */
    static List<LineString> lineParts(Coordinate[] points, GeometryFactory gf) {
        Coordinate[] unwrapped = copy(points);
        Antimeridian.unwrap(unwrapped);
        if (inRange(unwrapped)) {
            return List.of(gf.createLineString(rounded(unwrapped)));
        }
        LineString line = gf.createLineString(unwrapped);
        List<LineString> parts = new ArrayList<>();
        for (int shift = -2; shift <= 2; shift++) {
            double offset = shift * 360.0;
            Geometry piece = line.intersection(gf.createGeometry(Antimeridian.box(-180 + offset, -90, 180 + offset,
                    90)));
            for (int i = 0; i < piece.getNumGeometries(); i++) {
                if (piece.getGeometryN(i) instanceof LineString part && part.getNumPoints() >= 2
                        && part.getLength() > 0) {
                    Coordinate[] moved = copy(part.getCoordinates());
                    for (Coordinate c : moved) {
                        c.x -= offset;
                    }
                    parts.add(gf.createLineString(rounded(moved)));
                }
            }
        }
        return parts.isEmpty() ? List.of(gf.createLineString(rounded(wrapped(points)))) : parts;
    }

    /** The positions of each lettered item after {@code from}; empty when the text has fewer than two letters. */
    static List<List<Coordinate>> lettered(String text, int from) {
        Matcher m = LETTER.matcher(text);
        List<Integer> starts = new ArrayList<>();
        while (m.find()) {
            if (m.start() >= from) {
                starts.add(m.end());
            }
        }
        List<List<Coordinate>> out = new ArrayList<>();
        if (starts.size() < 2) {
            return out;
        }
        for (int i = 0; i < starts.size(); i++) {
            int end = i + 1 < starts.size() ? starts.get(i + 1) : text.length();
            out.add(positions(text, starts.get(i), end).stream().map(Found::coordinate).toList());
        }
        return out;
    }

    /** Rule 2: one circle per {@code WITHIN … OF <position>} phrase, or null when there is none. */
    private static Geometry circles(String text, GeometryFactory gf) {
        Matcher m = WITHIN.matcher(text);
        List<Polygon> circles = new ArrayList<>();
        while (m.find()) {
            Double distance = amount(m.group(1));
            Double meters = distance == null ? null : meters(distance, m.group(2));
            List<Found> at = positions(text, m.end(), Math.min(text.length(), m.end() + 64));
            if (meters == null || meters <= 0 || at.isEmpty() || at.getFirst().start() != m.end()) {
                continue;
            }
            Geometry circle = circle(at.getFirst().coordinate(), meters, gf);
            for (int i = 0; i < circle.getNumGeometries(); i++) {
                circles.add((Polygon) circle.getGeometryN(i));
            }
        }
        if (circles.isEmpty()) {
            return null;
        }
        return polygonal(circles, gf);
    }

    /** A number or number word, or null. */
    static Double amount(String text) {
        if (text == null) {
            return null;
        }
        if (Character.isDigit(text.charAt(0))) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return WORDS.get(text);
    }

    /** {@code value} in {@code unit} as meters; miles and cables are nautical. */
    static Double meters(double value, String unit) {
        String u = unit.replaceAll("\\s+", " ");
        if (u.startsWith("NAUTICAL") || u.startsWith("MILE") || u.equals("NM") || u.equals("NMI")) {
            return value * NAUTICAL_MILE_M;
        }
        if (u.startsWith("KILOMETER") || u.equals("KM")) {
            return value * 1000.0;
        }
        if (u.startsWith("YARD")) {
            return value * 0.9144;
        }
        if (u.startsWith("CABLE")) {
            return value * NAUTICAL_MILE_M / 10.0;
        }
        if (u.startsWith("METER") || u.startsWith("METRE")) {
            return value;
        }
        return null;
    }

    /**
     * A geodesic circle on the sphere: {@value #CIRCLE_VERTICES} vertices, closed, inside ±180°. A circle crossing
     * the antimeridian is a MultiPolygon of its two sides; a circle enclosing a pole is a polar cap whose ring runs
     * along ±180° to the pole.
     *
     * @param center the center (x = longitude, y = latitude)
     * @param radiusMeters the radius
     * @param gf the geometry factory
     * @return a Polygon, or a MultiPolygon when the circle crosses the antimeridian
     */
    static Geometry circle(Coordinate center, double radiusMeters, GeometryFactory gf) {
        double lat1 = Math.toRadians(center.y);
        double lon1 = Math.toRadians(center.x);
        double angular = radiusMeters / EARTH_RADIUS_M;
        Coordinate[] ring = new Coordinate[CIRCLE_VERTICES + 1];
        for (int i = 0; i < CIRCLE_VERTICES; i++) {
            double bearing = 2 * Math.PI * i / CIRCLE_VERTICES;
            double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angular)
                    + Math.cos(lat1) * Math.sin(angular) * Math.cos(bearing));
            double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(angular) * Math.cos(lat1),
                    Math.cos(angular) - Math.sin(lat1) * Math.sin(lat2));
            ring[i] = new Coordinate(Math.toDegrees(lon2), Math.toDegrees(lat2));
        }
        ring[CIRCLE_VERTICES] = new Coordinate(ring[0]);
        if (angular >= Math.PI / 2 - Math.abs(lat1)) {
            return polarCap(ring, center.y >= 0, gf);
        }
        return polygonal(polygonParts(ring, gf), gf);
    }

    /**
     * A circle enclosing a pole as one polygon in ±180°: its vertices sorted by longitude, joined across ±180° by
     * interpolation, and closed through the pole.
     */
    private static Polygon polarCap(Coordinate[] ring, boolean north, GeometryFactory gf) {
        List<Coordinate> vertices = new ArrayList<>();
        for (int i = 0; i < ring.length - 1; i++) {
            vertices.add(new Coordinate(wrap(ring[i].x), ring[i].y));
        }
        vertices.sort((a, b) -> Double.compare(a.x, b.x));
        Coordinate west = vertices.getFirst();
        Coordinate east = vertices.getLast();
        double gap = (west.x + 360.0) - east.x;
        double edgeLat = gap <= 0 ? east.y : east.y + (west.y - east.y) * (180.0 - east.x) / gap;
        double pole = north ? 90.0 : -90.0;
        List<Coordinate> cap = new ArrayList<>();
        cap.add(new Coordinate(-180.0, edgeLat));
        cap.addAll(vertices);
        cap.add(new Coordinate(180.0, edgeLat));
        cap.add(new Coordinate(180.0, pole));
        cap.add(new Coordinate(-180.0, pole));
        cap.add(new Coordinate(-180.0, edgeLat));
        return gf.createPolygon(rounded(cap.toArray(new Coordinate[0])));
    }

    /**
     * {@code lon} moved by a multiple of 360° into −180…180°.
     *
     * @param lon a longitude, degrees
     * @return the wrapped longitude
     */
    static double wrap(double lon) {
        double x = lon;
        while (x > 180.0) {
            x -= 360.0;
        }
        while (x < -180.0) {
            x += 360.0;
        }
        return x;
    }

    /** True when every longitude lies within ±180°. */
    private static boolean inRange(Coordinate[] coordinates) {
        for (Coordinate c : coordinates) {
            if (c.x < -180.0 || c.x > 180.0) {
                return false;
            }
        }
        return true;
    }

    /** Deep copies of {@code coordinates}. */
    private static Coordinate[] copy(Coordinate[] coordinates) {
        Coordinate[] out = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            out[i] = new Coordinate(coordinates[i].x, coordinates[i].y);
        }
        return out;
    }

    /** Copies of {@code coordinates} with each longitude wrapped into ±180°. */
    private static Coordinate[] wrapped(Coordinate[] coordinates) {
        Coordinate[] out = copy(coordinates);
        for (Coordinate c : out) {
            c.x = wrap(c.x);
        }
        return out;
    }

    /** Copies of {@code coordinates} rounded to six decimals, with longitudes clamped to ±180°. */
    private static Coordinate[] rounded(Coordinate[] coordinates) {
        Coordinate[] out = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            double x = Math.max(-180.0, Math.min(180.0, round(coordinates[i].x)));
            out[i] = new Coordinate(x, round(coordinates[i].y));
        }
        return out;
    }

    private static double round(double value) {
        return Math.round(value * 1e6) / 1e6;
    }

    /** {@code points} as a closed ring. */
    private static Coordinate[] closed(List<Coordinate> points) {
        List<Coordinate> ring = new ArrayList<>(points);
        if (!ring.getFirst().equals2D(ring.getLast())) {
            ring.add(new Coordinate(ring.getFirst()));
        }
        return ring.toArray(new Coordinate[0]);
    }

    /** Every valid position between {@code from} and {@code to}. */
    static List<Found> positions(String text, int from, int to) {
        List<Found> out = new ArrayList<>();
        Matcher m = POSITION.matcher(text);
        m.region(Math.max(0, from), Math.max(Math.max(0, from), Math.min(to, text.length())));
        while (m.find()) {
            Double lat = angle(m.group(1), m.group(2), m.group(3), 90);
            Double lon = angle(m.group(5), m.group(6), m.group(7), 180);
            if (lat == null || lon == null) {
                continue;
            }
            if (m.group(4).equals("S")) {
                lat = -lat;
            }
            if (m.group(8).equals("W")) {
                lon = -lon;
            }
            out.add(new Found(new Coordinate(round(lon), round(lat)), m.start(), m.end()));
        }
        return out;
    }

    /**
     * One angle from degrees, minutes and optional seconds.
     *
     * @param degrees the degrees
     * @param minutes the minutes, possibly decimal
     * @param seconds the seconds, possibly decimal, or null
     * @param max the largest allowed value (90 or 180)
     * @return decimal degrees, or null when a part is out of range
     */
    static Double angle(String degrees, String minutes, String seconds, int max) {
        try {
            int d = Integer.parseInt(degrees);
            double m = Double.parseDouble(minutes);
            double s = seconds == null ? 0 : Double.parseDouble(seconds);
            if (d > max || m >= 60 || s >= 60 || (seconds != null && minutes.contains("."))) {
                return null;
            }
            double value = d + m / 60.0 + s / 3600.0;
            return value > max ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
