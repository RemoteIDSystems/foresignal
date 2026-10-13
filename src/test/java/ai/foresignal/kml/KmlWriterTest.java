package ai.foresignal.kml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** KML 2.2 and KMZ writing (prompt 058): escaping, colors, truncation, gx:Track order and the zip layout. */
class KmlWriterTest {

    static final String KML = KmlWriter.KML;
    static final String GX = KmlWriter.GX;
    static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);
    static final Function<String, String> HREF = name -> "https://fs.example/v1/map/icons/" + name + ".png";

    private final KmlWriter writer = new KmlWriter();

    static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    static List<Element> all(Document doc, String ns, String name) {
        List<Element> out = new ArrayList<>();
        NodeList list = doc.getElementsByTagNameNS(ns, name);
        for (int i = 0; i < list.getLength(); i++) {
            out.add((Element) list.item(i));
        }
        return out;
    }

    static String first(Element parent, String name) {
        NodeList list = parent.getElementsByTagNameNS(KML, name);
        return list.getLength() == 0 ? null : list.item(0).getTextContent();
    }

    static KmlModel.Placemark placemark(String name, String description) {
        return new KmlModel.Placemark(name, Instant.parse("2026-10-13T10:00:00Z"), description,
                new KmlModel.Style("quake", "earthquake", KmlWriter.color("#D9480F")), null,
                List.of(new KmlModel.Data("mag", "Magnitude", "4.5")),
                GEOMETRY.createPoint(new Coordinate(-97.5, 30.25)));
    }

    @Test
    @DisplayName("names, descriptions and values are escaped for HTML and then XML: <b>, &, quotes; control characters drop")
    void escaping() throws Exception {
        String xml = writer.records(new KmlModel.Records("Doc <i>", "Credit & \"thanks\"",
                List.of(new KmlModel.Style("quake", "earthquake", "ff0f48d9")),
                List.of(placemark("<b>Big</b> & \"bold\"\u0001", "mag: <script>x</script>\u0007")), false, 1, true),
                HREF);
        assertThat(xml).doesNotContain("<![CDATA[").doesNotContain("<b>").doesNotContain("<script>");
        // The raw document holds the HTML entity escaped once more for XML.
        assertThat(xml).contains("&amp;lt;b&amp;gt;Big&amp;lt;/b&amp;gt; &amp;amp; &amp;quot;bold&amp;quot;");
        Document doc = parse(xml);
        Element pm = all(doc, KML, "Placemark").getFirst();
        // After XML parsing, Google Earth sees HTML entities, so the balloon shows the literal characters.
        assertThat(first(pm, "name")).isEqualTo("&lt;b&gt;Big&lt;/b&gt; &amp; &quot;bold&quot;");
        assertThat(first(pm, "description")).isEqualTo("mag: &lt;script&gt;x&lt;/script&gt;");
        Element document = all(doc, KML, "Document").getFirst();
        assertThat(first(document, "name")).isEqualTo("Doc &lt;i&gt;");
        assertThat(first(document, "description")).isEqualTo("Credit &amp; &quot;thanks&quot;");
    }

    @Test
    @DisplayName("#RRGGBB becomes opaque aabbggrr; anything else is the default color")
    void colors() {
        assertThat(KmlWriter.color("#D9480F")).isEqualTo("ff0f48d9");
        assertThat(KmlWriter.color("#112233")).isEqualTo("ff332211");
        assertThat(KmlWriter.color("red")).isEqualTo(KmlWriter.DEFAULT_COLOR);
        assertThat(KmlWriter.color(null)).isEqualTo("ffc27119");
    }

    @Test
    @DisplayName("styles, placemark time, styleUrl, ExtendedData and Point coordinates; the icon href comes from the function")
    void records() throws Exception {
        Document doc = parse(writer.records(new KmlModel.Records("Doc", null,
                List.of(new KmlModel.Style("quake", "earthquake", "ff0f48d9")), List.of(placemark("Q", "d")), false, 1,
                true), HREF));
        Element style = all(doc, KML, "Style").getFirst();
        assertThat(style.getAttribute("id")).isEqualTo("k-quake");
        assertThat(first(style, "href")).isEqualTo("https://fs.example/v1/map/icons/earthquake.png");
        assertThat(first(style, "color")).isEqualTo("ff0f48d9");
        Element pm = all(doc, KML, "Placemark").getFirst();
        assertThat(first(pm, "when")).isEqualTo("2026-10-13T10:00:00Z");
        assertThat(first(pm, "styleUrl")).isEqualTo("#k-quake");
        assertThat(first(pm, "coordinates")).isEqualTo("-97.5,30.25");
        Element data = (Element) pm.getElementsByTagNameNS(KML, "Data").item(0);
        assertThat(data.getAttribute("name")).isEqualTo("mag");
        assertThat(first(data, "displayName")).isEqualTo("Magnitude");
        assertThat(first(data, "value")).isEqualTo("4.5");
        assertThat(all(doc, KML, "Snippet")).isEmpty();
    }

    @Test
    @DisplayName("a truncated document gets the Snippet and ExtendedData truncated=true with the total")
    void truncation() throws Exception {
        Document doc = parse(writer.records(new KmlModel.Records("Doc", null, List.of(),
                List.of(placemark("A", null), placemark("B", null)), true, 7, true), HREF));
        assertThat(all(doc, KML, "Snippet").getFirst().getTextContent()).isEqualTo("truncated: showing 2 of 7");
        Element document = all(doc, KML, "Document").getFirst();
        Map<String, String> values = new LinkedHashMap<>();
        NodeList data = ((Element) document.getElementsByTagNameNS(KML, "ExtendedData").item(0))
                .getElementsByTagNameNS(KML, "Data");
        for (int i = 0; i < data.getLength(); i++) {
            Element d = (Element) data.item(i);
            values.put(d.getAttribute("name"), first(d, "value"));
        }
        assertThat(values).containsEntry("truncated", "true").containsEntry("total", "7");
        assertThat(parse(writer.records(new KmlModel.Records("Doc", null, List.of(), List.of(), true, 9, false), HREF))
                .getElementsByTagNameNS(KML, "Snippet").item(0).getTextContent()).isEqualTo("truncated: showing 0 of 9+");
    }

    @Test
    @DisplayName("a heading writes an inline IconStyle that repeats the icon and color")
    void heading() throws Exception {
        KmlModel.Style style = new KmlModel.Style("plane", "plane", "ff332211");
        KmlModel.Placemark p = new KmlModel.Placemark("P", null, null, style, -90.0, List.of(),
                GEOMETRY.createPoint(new Coordinate(1, 2)));
        Document doc = parse(writer.records(new KmlModel.Records("Doc", null, List.of(style), List.of(p), false, 1, true),
                HREF));
        Element pm = all(doc, KML, "Placemark").getFirst();
        assertThat(first(pm, "heading")).isEqualTo("270");
        assertThat(first(pm, "href")).isEqualTo("https://fs.example/v1/map/icons/plane.png");
    }

    @Test
    @DisplayName("gx:Track: altitudeMode, then every when oldest first, then gx:coord lon lat alt in the same order")
    void track() throws Exception {
        List<Instant> whens = List.of(Instant.parse("2026-10-13T10:00:00Z"), Instant.parse("2026-10-13T10:01:00.500Z"));
        List<double[]> coords = List.of(new double[] {-97.5, 30.25, 1000}, new double[] {-97.4, 30.3, 1200.5});
        KmlModel.Track track = new KmlModel.Track("N1 <x>", null, whens, coords, "absolute", true,
                List.of(new KmlModel.Data("pointsScanned", null, "2")));
        String xml = writer.tracks(new KmlModel.Tracks("Tracks", null, List.of(), List.of(track), false), HREF);
        Document doc = parse(xml);
        Element gxTrack = all(doc, GX, "Track").getFirst();
        List<String> order = new ArrayList<>();
        NodeList children = gxTrack.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element e) {
                order.add(e.getLocalName() + "=" + e.getTextContent());
            }
        }
        assertThat(order).containsExactly("altitudeMode=absolute", "when=2026-10-13T10:00:00Z",
                "when=2026-10-13T10:01:00.500Z", "coord=-97.5 30.25 1000", "coord=-97.4 30.3 1200.5");
        assertThat(xml).contains("N1 &amp;lt;x&amp;gt;");
        Element pm = all(doc, KML, "Placemark").getFirst();
        assertThat(xml).contains("<Data name=\"truncated\">");
        assertThat(first(pm, "value")).isEqualTo("true");
    }

    @Test
    @DisplayName("a NetworkLink: href, onInterval refresh (at least 30 s), onStop after 2 s and the bbox viewFormat")
    void networkLink() throws Exception {
        Document doc = parse(writer.networkLinks("Foresignal",
                List.of(new KmlModel.Link("quakes", "https://fs.example/v1/kml/records.kml?layer=quakes&x=1")), 10));
        Element link = all(doc, KML, "Link").getFirst();
        assertThat(first(link, "href")).isEqualTo("https://fs.example/v1/kml/records.kml?layer=quakes&x=1");
        assertThat(first(link, "refreshMode")).isEqualTo("onInterval");
        assertThat(first(link, "refreshInterval")).isEqualTo("30");
        assertThat(first(link, "viewRefreshMode")).isEqualTo("onStop");
        assertThat(first(link, "viewRefreshTime")).isEqualTo("2");
        assertThat(first(link, "viewFormat")).isEqualTo("bbox=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]");
    }

    @Test
    @DisplayName("a geofence: polygon with outer ring and a route line in one MultiGeometry")
    void geofence() throws Exception {
        var polygon = GEOMETRY.createPolygon(new Coordinate[] {new Coordinate(20, 49.5), new Coordinate(21, 49.5),
                new Coordinate(21, 50.5), new Coordinate(20, 49.5)});
        var line = GEOMETRY.createLineString(new Coordinate[] {new Coordinate(20, 49), new Coordinate(21, 51)});
        Document doc = parse(writer.geofence("Fence & co", "route corridor", List.of(polygon, line)));
        assertThat(all(doc, KML, "MultiGeometry")).hasSize(1);
        assertThat(all(doc, KML, "outerBoundaryIs")).hasSize(1);
        assertThat(all(doc, KML, "LineString")).hasSize(1);
        assertThat(all(doc, KML, "LinearRing").getFirst().getTextContent())
                .isEqualTo("20,49.5 21,49.5 21,50.5 20,49.5");
    }

    @Test
    @DisplayName("a KMZ holds doc.kml first, then icons/<name>.png")
    void kmz() throws Exception {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G'};
        byte[] zip = writer.kmz(new KmlModel.Kmz("<kml/>", Map.of("earthquake", png)));
        List<String> names = new ArrayList<>();
        Map<String, byte[]> contents = new LinkedHashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry e = in.getNextEntry(); e != null; e = in.getNextEntry()) {
                names.add(e.getName());
                contents.put(e.getName(), in.readAllBytes());
            }
        }
        assertThat(names).containsExactly("doc.kml", "icons/earthquake.png");
        assertThat(new String(contents.get("doc.kml"), StandardCharsets.UTF_8)).isEqualTo("<kml/>");
        assertThat(contents.get("icons/earthquake.png")).isEqualTo(png);
    }

    @Test
    @DisplayName("numbers have at most seven decimals and no exponent; style ids are XML-safe")
    void helpers() {
        assertThat(KmlWriter.number(1e-9)).isEqualTo("0");
        assertThat(KmlWriter.number(-0.0)).isEqualTo("0");
        assertThat(KmlWriter.number(12.5)).isEqualTo("12.5");
        assertThat(KmlWriter.number(Double.NaN)).isEqualTo("0");
        assertThat(KmlWriter.styleId("a b/c")).isEqualTo("k-a_b_c");
    }
}
