package ai.foresignal.source.adapters.hms;

import ai.foresignal.collect.XmlLimits;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * Parses an HMS smoke KML file into {@code smoke-polygon} records (prompt 048).
 *
 * <p><b>Hardened XML:</b> a StAX reader with DTD support and external entities off, and any DTD event
 * refused outright, so an entity-expansion or external-entity (XXE) payload never resolves.
 *
 * <p><b>Placemarks</b> (verified on {@code hms_smoke20261012.kml}, 67 placemarks): a CDATA description
 * {@code Start Time: 2026285 1200UTC<br>End Time: 2026285 1500UTC<br>Density: Light<br>Satellite:
 * GOES-WEST}, a {@code styleUrl} {@code #Smoke_Light_style} / {@code _Medium_} / {@code _Heavy_}, and one
 * {@code Polygon} (a {@code MultiGeometry} of polygons is also read). {@code ExtendedData} values
 * ({@code Data} or {@code SimpleData} named {@code Density}, {@code Start}, {@code End},
 * {@code Satellite}) take precedence over the description; the style URL decides the density first.
 *
 * <p><b>Record:</b> {@code eventTime} = Start, {@code observedAt} = End, {@code externalId} = the first 16
 * hex characters of SHA-256 over {@code date|start|end|density|satellite|centroid rounded to 0.1°};
 * payload {@code density} (light, medium, heavy), {@code densityRank} (1–3), {@code start}, {@code end},
 * {@code satellite}, {@code label}. Placemarks without a usable time, density or polygon are skipped and
 * counted; a repeated id keeps its first polygon.
 */
public final class HmsKmlParser {

    /** The record kind. */
    public static final String KIND = "smoke-polygon";

    static final Pattern START = Pattern.compile("Start\\s*Time\\s*:\\s*(\\d{7})\\s*(\\d{4})", Pattern.CASE_INSENSITIVE);
    static final Pattern END = Pattern.compile("End\\s*Time\\s*:\\s*(\\d{7})\\s*(\\d{4})", Pattern.CASE_INSENSITIVE);
    static final Pattern DENSITY = Pattern.compile("Density\\s*:\\s*([A-Za-z0-9.]+)", Pattern.CASE_INSENSITIVE);
    static final Pattern SATELLITE = Pattern.compile("Satellite\\s*:\\s*([A-Za-z0-9 _-]+?)\\s*(?:<|$)",
            Pattern.CASE_INSENSITIVE);
    static final Pattern JULIAN = Pattern.compile("^\\s*(\\d{7})\\s*(\\d{4})\\s*(?:UTC)?\\s*$", Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);

    private HmsKmlParser() {}

    /**
     * The parsed file.
     *
     * @param records one per distinct polygon
     * @param placemarks placemarks seen
     * @param skipped placemarks without a usable time, density or polygon
     */
    public record Result(List<NormalizedRecord> records, int placemarks, int skipped) {

        /** The record ids. */
        public List<String> ids() {
            return records.stream().map(NormalizedRecord::externalId).toList();
        }
    }

    /** One placemark as read. */
    private static final class Placemark {
        String styleUrl;
        String description;
        final Map<String, String> data = new LinkedHashMap<>();
        final List<Polygon> polygons = new ArrayList<>();
    }

    /** The hardened factory, one per parse ({@link XMLInputFactory} is not thread-safe). */
    static XMLInputFactory hardenedFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        return XmlLimits.apply(factory);
    }

    /**
     * Parses the KML of {@code date}'s file.
     *
     * @throws UpstreamFormatException when the XML is malformed, carries a DTD, or is not KML
     */
    public static Result parse(byte[] body, LocalDate date) {
        if (body == null || body.length == 0) {
            throw new UpstreamFormatException("The HMS smoke file is empty", null);
        }
        List<Placemark> placemarks = new ArrayList<>();
        boolean sawKml = false;
        XMLStreamReader r = null;
        try {
            r = hardenedFactory().createXMLStreamReader(new ByteArrayInputStream(body));
            Placemark current = null;
            String dataName = null;
            StringBuilder text = new StringBuilder();
            boolean inOuter = false;
            boolean inInner = false;
            List<Coordinate> outer = null;
            List<List<Coordinate>> inners = new ArrayList<>();
            while (r.hasNext()) {
                int event = r.next();
                switch (event) {
                    case XMLStreamConstants.DTD -> throw new UpstreamFormatException(
                            "The HMS smoke file carries a DTD, which is refused", null);
                    case XMLStreamConstants.ENTITY_REFERENCE -> throw new UpstreamFormatException(
                            "The HMS smoke file references an entity (" + r.getLocalName() + "), which is refused", null);
                    case XMLStreamConstants.START_ELEMENT -> {
                        String name = r.getLocalName();
                        text.setLength(0);
                        switch (name) {
                            case "kml" -> sawKml = true;
                            case "Placemark" -> current = new Placemark();
                            case "Data", "SimpleData" -> dataName = r.getAttributeValue(null, "name");
                            case "Polygon" -> {
                                outer = null;
                                inners = new ArrayList<>();
                            }
                            case "outerBoundaryIs" -> inOuter = true;
                            case "innerBoundaryIs" -> inInner = true;
                            default -> {
                                // Other elements only matter through their text.
                            }
                        }
                    }
                    case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA, XMLStreamConstants.SPACE ->
                            text.append(r.getText());
                    case XMLStreamConstants.END_ELEMENT -> {
                        String name = r.getLocalName();
                        String value = text.toString().trim();
                        if (current != null) {
                            switch (name) {
                                case "styleUrl" -> current.styleUrl = value;
                                case "description" -> current.description = value;
                                case "value" -> {
                                    if (dataName != null) {
                                        current.data.put(dataName.toLowerCase(Locale.ROOT), value);
                                    }
                                }
                                case "SimpleData" -> {
                                    if (dataName != null) {
                                        current.data.put(dataName.toLowerCase(Locale.ROOT), value);
                                    }
                                    dataName = null;
                                }
                                case "Data" -> dataName = null;
                                case "coordinates" -> {
                                    List<Coordinate> ring = coordinates(value);
                                    if (inOuter) {
                                        outer = ring;
                                    } else if (inInner) {
                                        inners.add(ring);
                                    }
                                }
                                case "outerBoundaryIs" -> inOuter = false;
                                case "innerBoundaryIs" -> inInner = false;
                                case "Polygon" -> {
                                    Polygon polygon = polygon(outer, inners);
                                    if (polygon != null) {
                                        current.polygons.add(polygon);
                                    }
                                    outer = null;
                                    inners = new ArrayList<>();
                                }
                                case "Placemark" -> {
                                    placemarks.add(current);
                                    current = null;
                                }
                                default -> {
                                    // Nothing to keep.
                                }
                            }
                        }
                        text.setLength(0);
                    }
                    default -> {
                        // Comments, processing instructions and document events are ignored.
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException("The HMS smoke file is not well-formed XML: " + e.getMessage(), null, e);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (XMLStreamException e) {
                    // Nothing to release beyond the in-memory body.
                }
            }
        }
        if (!sawKml) {
            throw new UpstreamFormatException("The HMS smoke file has no <kml> root", null);
        }
        Map<String, NormalizedRecord> records = new LinkedHashMap<>();
        int skipped = 0;
        for (Placemark p : placemarks) {
            NormalizedRecord record = record(p, date);
            if (record == null) {
                skipped++;
            } else {
                records.putIfAbsent(record.externalId(), record);
            }
        }
        return new Result(List.copyOf(records.values()), placemarks.size(), skipped);
    }

    /** The record of one placemark, or null when it lacks a time, a density or a polygon. */
    private static NormalizedRecord record(Placemark p, LocalDate date) {
        String density = density(p.styleUrl, p.data.get("density"), find(DENSITY, p.description));
        Instant start = julian(p.data.get("start"), p.description, START);
        Instant end = julian(p.data.get("end"), p.description, END);
        String satellite = p.data.getOrDefault("satellite", find(SATELLITE, p.description));
        if (density == null || start == null || end == null || p.polygons.isEmpty()) {
            return null;
        }
        GeometryFactory factory = Geo.factory();
        Geometry geometry = p.polygons.size() == 1 ? p.polygons.get(0)
                : factory.createMultiPolygon(p.polygons.toArray(Polygon[]::new));
        geometry.setSRID(Geo.SRID);
        Coordinate c = geometry.getEnvelopeInternal().centre();
        try {
            c = geometry.getCentroid().getCoordinate();
        } catch (RuntimeException e) {
            // An invalid ring: the envelope center is good enough for the id.
        }
        String sat = satellite == null || satellite.isBlank() ? null : satellite.trim();
        String id = externalId(date, start, end, density, sat, c.y, c.x);
        return NormalizedRecord.builder(id, KIND)
                .eventTime(start)
                .observedAt(end)
                .geometry(geometry)
                .put("density", density)
                .put("densityRank", rank(density))
                .put("start", start.toString())
                .put("end", end.toString())
                .put("satellite", sat)
                .put("label", label(density, sat, start, end))
                .build();
    }

    /** {@code sha256(date|start|end|density|satellite|lat,lon to 0.1°)}, first 16 hex characters. */
    static String externalId(LocalDate date, Instant start, Instant end, String density, String satellite, double lat,
            double lon) {
        String input = HmsDailyFiles.COMPACT.format(date) + "|" + start + "|" + end + "|" + density + "|"
                + (satellite == null ? "" : satellite) + "|"
                + String.format(Locale.ROOT, "%.1f,%.1f", lat, lon);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /**
     * The density: from the style URL ({@code #Smoke_Light_style}), else ExtendedData {@code Density}
     * (a word, or HMS's µg/m³ classes 5, 16, 27), else the description; null when none says.
     */
    static String density(String styleUrl, String data, String description) {
        for (String candidate : new String[] {styleUrl, data, description}) {
            String d = densityWord(candidate);
            if (d != null) {
                return d;
            }
        }
        return null;
    }

    private static String densityWord(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String t = text.toLowerCase(Locale.ROOT);
        if (t.contains("heavy")) {
            return "heavy";
        }
        if (t.contains("medium")) {
            return "medium";
        }
        if (t.contains("light")) {
            return "light";
        }
        switch (t.trim()) {
            case "5", "5.0", "5.000" -> {
                return "light";
            }
            case "16", "16.0", "16.000" -> {
                return "medium";
            }
            case "27", "27.0", "27.000" -> {
                return "heavy";
            }
            default -> {
                return null;
            }
        }
    }

    /** 1 for light, 2 for medium, 3 for heavy. */
    static int rank(String density) {
        return switch (density) {
            case "heavy" -> 3;
            case "medium" -> 2;
            default -> 1;
        };
    }

    /** "Heavy smoke (HMS, GOES-EAST, 12:00-15:00 UTC)". */
    static String label(String density, String satellite, Instant start, Instant end) {
        String d = Character.toUpperCase(density.charAt(0)) + density.substring(1);
        String window = HHMM.format(start.atOffset(ZoneOffset.UTC)) + "-" + HHMM.format(end.atOffset(ZoneOffset.UTC))
                + " UTC";
        return d + " smoke (HMS" + (satellite == null ? "" : ", " + satellite) + ", " + window + ")";
    }

    /** A {@code YYYYDDD HHMM} time from ExtendedData, else from the description. */
    static Instant julian(String data, String description, Pattern inDescription) {
        if (data != null) {
            Matcher m = JULIAN.matcher(data);
            if (m.matches()) {
                return julian(m.group(1), m.group(2));
            }
        }
        if (description != null) {
            Matcher m = inDescription.matcher(description);
            if (m.find()) {
                return julian(m.group(1), m.group(2));
            }
        }
        return null;
    }

    /**
     * {@code YYYYDDD} and {@code HHMM} as an instant (UTC); null when out of range.
     */
    static Instant julian(String yearDay, String hhmm) {
        try {
            int year = Integer.parseInt(yearDay.substring(0, 4));
            int doy = Integer.parseInt(yearDay.substring(4, 7));
            int hour = Integer.parseInt(hhmm.substring(0, 2));
            int minute = Integer.parseInt(hhmm.substring(2, 4));
            if (doy < 1 || doy > 366 || hour > 24 || minute > 59) {
                return null;
            }
            return LocalDateTime.of(year, 1, 1, 0, 0).plusDays(doy - 1L).plusHours(hour).plusMinutes(minute)
                    .toInstant(ZoneOffset.UTC);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String find(Pattern pattern, String text) {
        if (text == null) {
            return null;
        }
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    /** {@code lon,lat[,alt]} tuples separated by whitespace. */
    static List<Coordinate> coordinates(String text) {
        List<Coordinate> out = new ArrayList<>();
        for (String tuple : text.trim().split("\\s+")) {
            if (tuple.isBlank()) {
                continue;
            }
            String[] parts = tuple.split(",");
            if (parts.length < 2) {
                throw new UpstreamFormatException("Malformed KML coordinate '" + tuple + "'", text);
            }
            try {
                double lon = Double.parseDouble(parts[0]);
                double lat = Double.parseDouble(parts[1]);
                if (Math.abs(lon) > 180 || Math.abs(lat) > 90) {
                    throw new UpstreamFormatException("KML coordinate out of range: " + tuple, text);
                }
                out.add(new Coordinate(lon, lat));
            } catch (NumberFormatException e) {
                throw new UpstreamFormatException("Malformed KML coordinate '" + tuple + "'", text, e);
            }
        }
        return out;
    }

    /** A polygon from its rings, closing them when needed; null when the outer ring is unusable. */
    static Polygon polygon(List<Coordinate> outer, List<List<Coordinate>> inners) {
        GeometryFactory factory = Geo.factory();
        LinearRing shell = ring(factory, outer);
        if (shell == null) {
            return null;
        }
        List<LinearRing> holes = new ArrayList<>();
        for (List<Coordinate> inner : inners) {
            LinearRing hole = ring(factory, inner);
            if (hole != null) {
                holes.add(hole);
            }
        }
        Polygon polygon = factory.createPolygon(shell, holes.toArray(LinearRing[]::new));
        polygon.setSRID(Geo.SRID);
        return polygon;
    }

    private static LinearRing ring(GeometryFactory factory, List<Coordinate> points) {
        if (points == null) {
            return null;
        }
        List<Coordinate> ring = new ArrayList<>(points);
        if (!ring.isEmpty() && !ring.get(0).equals2D(ring.get(ring.size() - 1))) {
            ring.add(new Coordinate(ring.get(0)));
        }
        if (ring.size() < 4) {
            return null;
        }
        return factory.createLinearRing(ring.toArray(Coordinate[]::new));
    }
}
