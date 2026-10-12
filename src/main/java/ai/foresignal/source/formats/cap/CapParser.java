package ai.foresignal.source.formats.cap;

import ai.foresignal.api.error.InvalidInputException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses OASIS CAP 1.2 alerts (prompt 024; shared with prompts 067 and 089).
 *
 * <p><strong>Secure XML only.</strong> DOCTYPE declarations are refused outright, and external
 * entities, external DTDs and XInclude are disabled, so an XXE or entity-expansion payload fails
 * with {@link InvalidInputException} before anything is resolved.
 *
 * <p>CAP writes coordinates as {@code "lat,lon"}; they are swapped to lon/lat. A
 * {@code circle} ({@code "lat,lon radiusKm"}) becomes a 64-segment polygon. Elements are matched by
 * local name, so both the CAP 1.2 namespace and un-namespaced documents are accepted.
 *
 * <p>Thread-safe: a new {@link DocumentBuilder} is created per parse.
 */
public final class CapParser {

    /** The CAP 1.2 namespace. */
    public static final String NAMESPACE = "urn:oasis:names:tc:emergency:cap:1.2";

    /** Segments of a circle approximated as a polygon. */
    public static final int CIRCLE_SEGMENTS = 64;

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    /** The geometry factory used for areas (SRID 4326). */
    public GeometryFactory factory() {
        return factory;
    }

    /**
     * Parses one alert.
     *
     * @throws InvalidInputException when the document is not well-formed, declares a DOCTYPE, or is
     *     not a CAP alert
     */
    public CapAlert parse(byte[] xml) {
        if (xml == null || xml.length == 0) {
            throw new InvalidInputException("The CAP document is empty.");
        }
        Document document;
        try {
            DocumentBuilder builder = secureFactory().newDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> {
                throw new SAXException("External entities are not allowed");
            });
            builder.setErrorHandler(null);
            document = builder.parse(new ByteArrayInputStream(xml));
        } catch (SAXException | IOException e) {
            throw new InvalidInputException("The CAP document could not be parsed: " + e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("The XML parser cannot be configured securely", e);
        }
        Element alert = document.getDocumentElement();
        if (alert == null || !"alert".equals(localName(alert))) {
            throw new InvalidInputException("The document is not a CAP alert.");
        }
        List<CapAlert.Info> infos = new ArrayList<>();
        for (Element info : children(alert, "info")) {
            infos.add(info(info));
        }
        return new CapAlert(
                text(alert, "identifier"),
                text(alert, "sender"),
                time(text(alert, "sent")),
                text(alert, "status"),
                text(alert, "msgType"),
                text(alert, "scope"),
                references(text(alert, "references")),
                infos);
    }

    static DocumentBuilderFactory secureFactory() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        return dbf;
    }

    private CapAlert.Info info(Element info) {
        List<CapAlert.Area> areas = new ArrayList<>();
        for (Element area : children(info, "area")) {
            areas.add(area(area));
        }
        return new CapAlert.Info(
                text(info, "language"),
                text(info, "event"),
                text(info, "urgency"),
                text(info, "severity"),
                text(info, "certainty"),
                text(info, "headline"),
                text(info, "description"),
                text(info, "instruction"),
                time(text(info, "onset")),
                time(text(info, "effective")),
                time(text(info, "expires")),
                areas);
    }

    private CapAlert.Area area(Element area) {
        List<Polygon> polygons = new ArrayList<>();
        for (Element polygon : children(area, "polygon")) {
            polygons.add(polygon(polygon.getTextContent()));
        }
        for (Element circle : children(area, "circle")) {
            polygons.add(circle(circle.getTextContent()));
        }
        List<CapAlert.Geocode> geocodes = new ArrayList<>();
        for (Element geocode : children(area, "geocode")) {
            geocodes.add(new CapAlert.Geocode(text(geocode, "valueName"), text(geocode, "value")));
        }
        return new CapAlert.Area(text(area, "areaDesc"), polygons, geocodes);
    }

    /**
     * A CAP polygon: whitespace-separated {@code lat,lon} pairs, first equal to last.
     *
     * @throws InvalidInputException when a pair does not parse or the ring is invalid
     */
    public Polygon polygon(String text) {
        String[] pairs = text == null ? new String[0] : text.strip().split("\\s+");
        List<Coordinate> coordinates = new ArrayList<>();
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }
            double[] latLon = latLon(pair);
            coordinates.add(new Coordinate(latLon[1], latLon[0]));
        }
        if (!coordinates.isEmpty() && !coordinates.get(0).equals2D(coordinates.get(coordinates.size() - 1))) {
            coordinates.add(new Coordinate(coordinates.get(0)));
        }
        if (coordinates.size() < 4) {
            throw new InvalidInputException("A CAP polygon needs at least four points.");
        }
        try {
            LinearRing ring = factory.createLinearRing(coordinates.toArray(new Coordinate[0]));
            return factory.createPolygon(ring);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("The CAP polygon is malformed: " + e.getMessage(), e);
        }
    }

    /**
     * A CAP circle {@code "lat,lon radiusKm"} as a {@value #CIRCLE_SEGMENTS}-segment polygon on a
     * spherical earth.
     *
     * @throws InvalidInputException when it does not parse
     */
    public Polygon circle(String text) {
        String[] parts = text == null ? new String[0] : text.strip().split("\\s+");
        if (parts.length != 2) {
            throw new InvalidInputException("A CAP circle is \"lat,lon radiusKm\".");
        }
        double[] center = latLon(parts[0]);
        double radiusKm;
        try {
            radiusKm = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("The CAP circle radius is not a number.", e);
        }
        if (!Double.isFinite(radiusKm) || radiusKm < 0) {
            throw new InvalidInputException("The CAP circle radius must be a non-negative number.");
        }
        double lat1 = Math.toRadians(center[0]);
        double lon1 = Math.toRadians(center[1]);
        double angular = radiusKm / EARTH_RADIUS_KM;
        Coordinate[] ring = new Coordinate[CIRCLE_SEGMENTS + 1];
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            double bearing = 2 * Math.PI * i / CIRCLE_SEGMENTS;
            double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angular)
                    + Math.cos(lat1) * Math.sin(angular) * Math.cos(bearing));
            double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(angular) * Math.cos(lat1),
                    Math.cos(angular) - Math.sin(lat1) * Math.sin(lat2));
            double lonDeg = Math.toDegrees(lon2);
            lonDeg = ((lonDeg + 540) % 360) - 180;
            ring[i] = new Coordinate(lonDeg, Math.toDegrees(lat2));
        }
        ring[CIRCLE_SEGMENTS] = new Coordinate(ring[0]);
        return factory.createPolygon(ring);
    }

    private static double[] latLon(String pair) {
        String[] parts = pair.split(",");
        if (parts.length != 2) {
            throw new InvalidInputException("'" + pair + "' is not a lat,lon pair.");
        }
        try {
            double lat = Double.parseDouble(parts[0]);
            double lon = Double.parseDouble(parts[1]);
            if (!Double.isFinite(lat) || !Double.isFinite(lon) || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                throw new InvalidInputException("'" + pair + "' is out of range.");
            }
            return new double[] {lat, lon};
        } catch (NumberFormatException e) {
            throw new InvalidInputException("'" + pair + "' is not a lat,lon pair.", e);
        }
    }

    /** {@code references}: whitespace-separated {@code sender,identifier,sent} triples. */
    static List<CapAlert.Reference> references(String text) {
        List<CapAlert.Reference> out = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return out;
        }
        for (String triple : text.strip().split("\\s+")) {
            String[] parts = triple.split(",", 3);
            if (parts.length >= 2 && !parts[1].isBlank()) {
                out.add(new CapAlert.Reference(parts[0], parts[1], parts.length == 3 ? parts[2] : null));
            }
        }
        return out;
    }

    private static Instant time(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String value = text.strip();
        // CAP writes UTC as "-00:00"; read it as Z whatever the JDK's offset parser makes of it.
        if (value.endsWith("-00:00")) {
            value = value.substring(0, value.length() - 6) + "Z";
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeException e) {
            try {
                return Instant.parse(value);
            } catch (DateTimeException again) {
                throw new InvalidInputException("'" + text + "' is not a CAP date-time.", again);
            }
        }
    }

    private static String text(Element parent, String name) {
        for (Element child : children(parent, name)) {
            String value = child.getTextContent();
            return value == null ? null : value.strip();
        }
        return null;
    }

    private static List<Element> children(Element parent, String name) {
        List<Element> out = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && name.equals(localName(element))) {
                out.add(element);
            }
        }
        return out;
    }

    private static String localName(Element element) {
        return element.getLocalName() != null ? element.getLocalName() : element.getNodeName();
    }
}
