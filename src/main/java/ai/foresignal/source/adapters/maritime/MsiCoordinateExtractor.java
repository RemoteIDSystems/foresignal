package ai.foresignal.source.adapters.maritime;

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
        for (List<Coordinate> group : groups) {
            if (group.size() >= 3) {
                polygons.add(gf.createPolygon(closed(group)));
            }
        }
        if (polygons.size() >= 2) {
            return gf.createMultiPolygon(polygons.toArray(new Polygon[0]));
        }
        List<Coordinate> after = positions(text, from, text.length()).stream().map(Found::coordinate).toList();
        return after.size() >= 3 ? gf.createPolygon(closed(after)) : null;
    }

    /** Rule 3: the trackline or tracklines after {@code from}, or null with fewer than 2 positions. */
    private static Geometry line(String text, int from, GeometryFactory gf) {
        List<List<Coordinate>> groups = lettered(text, from);
        List<LineString> lines = new ArrayList<>();
        for (List<Coordinate> group : groups) {
            if (group.size() >= 2) {
                lines.add(gf.createLineString(group.toArray(new Coordinate[0])));
            }
        }
        if (lines.size() >= 2) {
            return gf.createMultiLineString(lines.toArray(new LineString[0]));
        }
        List<Coordinate> after = positions(text, from, text.length()).stream().map(Found::coordinate).toList();
        return after.size() >= 2 ? gf.createLineString(after.toArray(new Coordinate[0])) : null;
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
            circles.add(circle(at.getFirst().coordinate(), meters, gf));
        }
        if (circles.isEmpty()) {
            return null;
        }
        return circles.size() == 1 ? circles.getFirst() : gf.createMultiPolygon(circles.toArray(new Polygon[0]));
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
     * A geodesic circle on the sphere: {@value #CIRCLE_VERTICES} vertices, closed.
     *
     * @param center the center (x = longitude, y = latitude)
     * @param radiusMeters the radius
     * @param gf the geometry factory
     * @return the polygon
     */
    static Polygon circle(Coordinate center, double radiusMeters, GeometryFactory gf) {
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
            ring[i] = new Coordinate(round(Math.toDegrees(lon2)), round(Math.toDegrees(lat2)));
        }
        ring[CIRCLE_VERTICES] = new Coordinate(ring[0]);
        return gf.createPolygon(ring);
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
