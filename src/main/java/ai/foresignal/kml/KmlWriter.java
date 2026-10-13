package ai.foresignal.kml;

import ai.foresignal.feed.XmlText;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

/**
 * Writes OGC KML 2.2 with the Google {@code gx:} extensions (prompt 058) through StAX, and packs KMZ.
 *
 * <p><strong>Escaping.</strong> Google Earth renders names, descriptions and data values as HTML in its
 * balloons, so every such string is first {@link XmlText#html escaped for HTML} ({@code <}, {@code >},
 * {@code &}, {@code "}) and then escaped again by the XML writer. Characters XML 1.0 forbids are dropped.
 * Upstream text is never wrapped in CDATA.
 *
 * <p><strong>Colors</strong> are KML {@code aabbggrr} ({@link #color}). <strong>Element order</strong>
 * follows the KML 2.2 schema: a Feature's {@code name}, {@code Snippet}, {@code description}, time,
 * {@code styleUrl}, styles, {@code ExtendedData}, then its geometry or children.
 */
@Component
public class KmlWriter {

    /** The KML media type. */
    public static final String KML_MEDIA_TYPE = "application/vnd.google-earth.kml+xml";

    /** The KMZ media type. */
    public static final String KMZ_MEDIA_TYPE = "application/vnd.google-earth.kmz";

    /** The KML 2.2 namespace. */
    public static final String KML = "http://www.opengis.net/kml/2.2";

    /** The Google extension namespace. */
    public static final String GX = "http://www.google.com/kml/ext/2.2";

    /** The {@code viewFormat} of every NetworkLink: Google Earth fills the view's bounding box. */
    public static final String VIEW_FORMAT = "bbox=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]";

    /** The {@code viewRefreshTime} of every NetworkLink, in seconds after the view stops. */
    public static final int VIEW_REFRESH_SECONDS = 2;

    /** The folder of icons inside a KMZ. */
    public static final String KMZ_ICON_DIR = "icons/";

    /** The default color when a kind declares none: opaque {@code #1971C2}. */
    public static final String DEFAULT_COLOR = color("#1971C2");

    private final XMLOutputFactory factory = XMLOutputFactory.newFactory();

    /** {@code #RRGGBB} as opaque KML {@code ffbbggrr}; anything else is {@link #DEFAULT_COLOR}'s value. */
    public static String color(String rgb) {
        if (rgb == null || !rgb.matches("^#[0-9A-Fa-f]{6}$")) {
            return "ffc27119";
        }
        String hex = rgb.substring(1).toLowerCase(Locale.ROOT);
        return "ff" + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
    }

    /**
     * A Document with one {@code NetworkLink} per layer.
     *
     * @param name the document name
     * @param links the layers' records URLs
     * @param refreshSeconds {@code refreshInterval}, at least 30
     */
    public String networkLinks(String name, List<KmlModel.Link> links, int refreshSeconds) {
        return document(w -> {
            text(w, "name", name);
            for (KmlModel.Link link : links) {
                w.writeStartElement("NetworkLink");
                text(w, "name", link.name());
                raw(w, "refreshVisibility", "0");
                raw(w, "flyToView", "0");
                w.writeStartElement("Link");
                raw(w, "href", XmlText.clean(link.href()));
                raw(w, "refreshMode", "onInterval");
                raw(w, "refreshInterval", Integer.toString(Math.max(30, refreshSeconds)));
                raw(w, "viewRefreshMode", "onStop");
                raw(w, "viewRefreshTime", Integer.toString(VIEW_REFRESH_SECONDS));
                raw(w, "viewFormat", VIEW_FORMAT);
                w.writeEndElement();
                w.writeEndElement();
            }
        });
    }

    /**
     * A records Document: one {@code Style} per kind and one {@code Placemark} per record; when truncated
     * a {@code Snippet} "truncated: showing N of M" and {@code ExtendedData truncated=true}.
     *
     * @param records the document
     * @param iconHref icon name to the href written in {@code IconStyle}
     */
    public String records(KmlModel.Records records, Function<String, String> iconHref) {
        return document(w -> {
            text(w, "name", records.name());
            if (records.truncated()) {
                w.writeStartElement("Snippet");
                w.writeAttribute("maxLines", "1");
                w.writeCharacters(XmlText.html("truncated: showing " + records.placemarks().size() + " of "
                        + records.total() + (records.totalExact() ? "" : "+")));
                w.writeEndElement();
            }
            if (records.description() != null) {
                text(w, "description", records.description());
            }
            for (KmlModel.Style style : records.styles()) {
                style(w, style, null, iconHref);
            }
            w.writeStartElement("ExtendedData");
            data(w, new KmlModel.Data("truncated", null, Boolean.toString(records.truncated())));
            data(w, new KmlModel.Data("total", null, Long.toString(records.total())));
            w.writeEndElement();
            for (KmlModel.Placemark placemark : records.placemarks()) {
                placemark(w, placemark, iconHref);
            }
        });
    }

    /**
     * A tracks Document: one {@code Placemark} with a {@code gx:Track} per entity ({@code when} values
     * oldest first, then {@code gx:coord "lon lat alt"} values in the same order).
     *
     * @param tracks the document
     * @param iconHref icon name to the href written in {@code IconStyle}
     */
    public String tracks(KmlModel.Tracks tracks, Function<String, String> iconHref) {
        return document(w -> {
            text(w, "name", tracks.name());
            if (tracks.description() != null) {
                text(w, "description", tracks.description());
            }
            for (KmlModel.Style style : tracks.styles()) {
                style(w, style, null, iconHref);
            }
            w.writeStartElement("ExtendedData");
            data(w, new KmlModel.Data("entitiesTruncated", null, Boolean.toString(tracks.entitiesTruncated())));
            w.writeEndElement();
            for (KmlModel.Track track : tracks.tracks()) {
                w.writeStartElement("Placemark");
                text(w, "name", track.name());
                if (track.style() != null) {
                    raw(w, "styleUrl", "#" + styleId(track.style().id()));
                }
                w.writeStartElement("ExtendedData");
                data(w, new KmlModel.Data("truncated", null, Boolean.toString(track.truncated())));
                for (KmlModel.Data d : track.data()) {
                    data(w, d);
                }
                w.writeEndElement();
                w.writeStartElement("gx", "Track", GX);
                raw(w, "altitudeMode", track.altitudeMode());
                for (Instant when : track.whens()) {
                    raw(w, "when", time(when));
                }
                for (double[] c : track.coords()) {
                    w.writeStartElement("gx", "coord", GX);
                    w.writeCharacters(number(c[0]) + " " + number(c[1]) + " " + number(c.length > 2 ? c[2] : 0));
                    w.writeEndElement();
                }
                w.writeEndElement();
                w.writeEndElement();
            }
        });
    }

    /**
     * A trigger geofence Document: one {@code Placemark} with the fence polygons and, for a route
     * corridor, the route line, in one {@code MultiGeometry}.
     *
     * @param name the rule name
     * @param description plain text, or null
     * @param geometries the polygons and lines to draw
     */
    public String geofence(String name, String description, List<Geometry> geometries) {
        return document(w -> {
            text(w, "name", name);
            w.writeStartElement("Style");
            w.writeAttribute("id", "geofence");
            w.writeStartElement("LineStyle");
            raw(w, "color", "ff2a2ac9");
            raw(w, "width", "2");
            w.writeEndElement();
            w.writeStartElement("PolyStyle");
            raw(w, "color", "402a2ac9");
            w.writeEndElement();
            w.writeEndElement();
            w.writeStartElement("Placemark");
            text(w, "name", name);
            if (description != null) {
                text(w, "description", description);
            }
            raw(w, "styleUrl", "#geofence");
            w.writeStartElement("MultiGeometry");
            for (Geometry g : geometries) {
                geometry(w, g);
            }
            w.writeEndElement();
            w.writeEndElement();
        });
    }

    /** A KMZ archive: {@code doc.kml} first, then {@code icons/<name>.png} for each icon. */
    public byte[] kmz(KmlModel.Kmz kmz) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            zip.putNextEntry(new ZipEntry("doc.kml"));
            zip.write(kmz.kml().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            for (Map.Entry<String, byte[]> icon : kmz.icons().entrySet()) {
                zip.putNextEntry(new ZipEntry(KMZ_ICON_DIR + icon.getKey() + ".png"));
                zip.write(icon.getValue());
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write the KMZ", e);
        }
        return bytes.toByteArray();
    }

    // ------------------------------------------------------------------------- parts

    private interface Body {
        void write(XMLStreamWriter w) throws XMLStreamException;
    }

    private String document(Body body) {
        StringWriter out = new StringWriter(16_384);
        try {
            XMLStreamWriter w = factory.createXMLStreamWriter(out);
            w.writeStartDocument("UTF-8", "1.0");
            w.writeStartElement("kml");
            w.writeDefaultNamespace(KML);
            w.writeNamespace("gx", GX);
            w.writeStartElement("Document");
            body.write(w);
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndDocument();
            w.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Cannot write KML", e);
        }
        return out.toString();
    }

    private static void style(XMLStreamWriter w, KmlModel.Style style, Double heading, Function<String, String> iconHref)
            throws XMLStreamException {
        w.writeStartElement("Style");
        if (heading == null) {
            w.writeAttribute("id", styleId(style.id()));
        }
        w.writeStartElement("IconStyle");
        raw(w, "color", style.color());
        if (heading != null) {
            raw(w, "heading", number(((heading % 360) + 360) % 360));
        }
        if (style.icon() != null) {
            w.writeStartElement("Icon");
            raw(w, "href", XmlText.clean(iconHref.apply(style.icon())));
            w.writeEndElement();
        }
        w.writeEndElement();
        w.writeStartElement("LineStyle");
        raw(w, "color", style.color());
        raw(w, "width", "2");
        w.writeEndElement();
        w.writeStartElement("PolyStyle");
        raw(w, "color", "40" + style.color().substring(2));
        w.writeEndElement();
        w.writeEndElement();
    }

    private static void placemark(XMLStreamWriter w, KmlModel.Placemark p, Function<String, String> iconHref)
            throws XMLStreamException {
        w.writeStartElement("Placemark");
        text(w, "name", p.name());
        if (p.description() != null && !p.description().isEmpty()) {
            text(w, "description", p.description());
        }
        if (p.when() != null) {
            w.writeStartElement("TimeStamp");
            raw(w, "when", time(p.when()));
            w.writeEndElement();
        }
        if (p.style() != null) {
            raw(w, "styleUrl", "#" + styleId(p.style().id()));
            if (p.heading() != null) {
                // An inline IconStyle replaces the shared one, so it repeats the icon and color with the heading.
                style(w, p.style(), p.heading(), iconHref);
            }
        }
        if (!p.data().isEmpty()) {
            w.writeStartElement("ExtendedData");
            for (KmlModel.Data d : p.data()) {
                data(w, d);
            }
            w.writeEndElement();
        }
        if (p.geometry() != null && !p.geometry().isEmpty()) {
            geometry(w, p.geometry());
        }
        w.writeEndElement();
    }

    private static void data(XMLStreamWriter w, KmlModel.Data d) throws XMLStreamException {
        w.writeStartElement("Data");
        w.writeAttribute("name", XmlText.clean(d.name()));
        if (d.displayName() != null) {
            text(w, "displayName", d.displayName());
        }
        text(w, "value", d.value());
        w.writeEndElement();
    }

    /** Writes a JTS geometry; collections become {@code MultiGeometry}. */
    static void geometry(XMLStreamWriter w, Geometry g) throws XMLStreamException {
        if (g instanceof Point p) {
            w.writeStartElement("Point");
            raw(w, "coordinates", coordinates(p.getCoordinates()));
            w.writeEndElement();
        } else if (g instanceof LineString line) {
            w.writeStartElement("LineString");
            raw(w, "tessellate", "1");
            raw(w, "coordinates", coordinates(line.getCoordinates()));
            w.writeEndElement();
        } else if (g instanceof Polygon polygon) {
            w.writeStartElement("Polygon");
            raw(w, "tessellate", "1");
            ring(w, "outerBoundaryIs", polygon.getExteriorRing());
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                ring(w, "innerBoundaryIs", polygon.getInteriorRingN(i));
            }
            w.writeEndElement();
        } else if (g instanceof GeometryCollection collection) {
            w.writeStartElement("MultiGeometry");
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                geometry(w, collection.getGeometryN(i));
            }
            w.writeEndElement();
        }
    }

    private static void ring(XMLStreamWriter w, String boundary, LineString ring) throws XMLStreamException {
        w.writeStartElement(boundary);
        w.writeStartElement("LinearRing");
        raw(w, "coordinates", coordinates(ring.getCoordinates()));
        w.writeEndElement();
        w.writeEndElement();
    }

    private static String coordinates(Coordinate[] coordinates) {
        StringBuilder out = new StringBuilder(coordinates.length * 24);
        for (Coordinate c : coordinates) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(number(c.x)).append(',').append(number(c.y));
        }
        return out.toString();
    }

    /** A style id from a kind: the characters an XML id allows, prefixed {@code k-}. */
    static String styleId(String kind) {
        return "k-" + (kind == null ? "" : kind.replaceAll("[^A-Za-z0-9_.-]", "_"));
    }

    /** Upstream or operator text: escaped for HTML here, for XML by the writer. */
    private static void text(XMLStreamWriter w, String name, String value) throws XMLStreamException {
        w.writeStartElement(name);
        w.writeCharacters(XmlText.html(value));
        w.writeEndElement();
    }

    /** Text Foresignal produced itself (numbers, enumerations, URLs): escaped for XML only. */
    private static void raw(XMLStreamWriter w, String name, String value) throws XMLStreamException {
        w.writeStartElement(name);
        w.writeCharacters(value == null ? "" : value);
        w.writeEndElement();
    }

    /** RFC 3339 UTC; milliseconds only when present. */
    static String time(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MILLIS).toString();
    }

    /** A number with at most seven decimals and no exponent. */
    static String number(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        String text = String.format(Locale.ROOT, "%.7f", value);
        text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        return "-0".equals(text) ? "0" : text;
    }
}
