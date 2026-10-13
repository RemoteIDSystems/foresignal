package ai.foresignal.feed;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** RFC 4287 Atom with GeoRSS-Simple (prompt 058), checked by parsing the output with a namespace-aware DOM. */
class AtomFeedWriterTest {

    static final String ATOM = AtomFeedWriter.ATOM;

    private final AtomFeedWriter writer = new AtomFeedWriter();

    static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    static String text(Element parent, String ns, String name) {
        NodeList list = parent.getElementsByTagNameNS(ns, name);
        return list.getLength() == 0 ? null : list.item(0).getTextContent();
    }

    static List<Element> children(Element parent, String ns, String name) {
        List<Element> out = new ArrayList<>();
        NodeList list = parent.getElementsByTagNameNS(ns, name);
        for (int i = 0; i < list.getLength(); i++) {
            out.add((Element) list.item(i));
        }
        return out;
    }

    @Test
    @DisplayName("the feed carries the elements RFC 4287 requires: id, title, updated, author, self link")
    void feedElements() throws Exception {
        Document doc = parse(writer.write(FeedFixtures.feed(FeedFixtures.entry(1, null))));
        Element feed = doc.getDocumentElement();
        assertThat(feed.getNamespaceURI()).isEqualTo(ATOM);
        assertThat(feed.getLocalName()).isEqualTo("feed");
        assertThat(text(feed, ATOM, "id")).isEqualTo("tag:foresignal,2026:INSTALL/alerts");
        assertThat(text(feed, ATOM, "title")).isEqualTo("Foresignal alerts");
        assertThat(text(feed, ATOM, "updated")).isEqualTo("2026-10-13T10:05:30Z");
        assertThat(text(feed, ATOM, "name")).isEqualTo("Foresignal");
        Element self = children(feed, ATOM, "link").getFirst();
        assertThat(self.getAttribute("rel")).isEqualTo("self");
        assertThat(self.getAttribute("href")).isEqualTo("https://fs.example/v1/alerts/feed.atom");
        assertThat(self.getAttribute("type")).isEqualTo("application/atom+xml");
    }

    @Test
    @DisplayName("an entry: tag id, updated and published, alternate link, categories, escaped title and HTML content")
    void entry() throws Exception {
        String xml = writer.write(FeedFixtures.feed(FeedFixtures.entry(4211, FeedFixtures.point(-97.5, 30.25))));
        assertThat(xml).doesNotContain("<![CDATA[");
        Element entry = children(parse(xml).getDocumentElement(), ATOM, "entry").getFirst();
        assertThat(text(entry, ATOM, "id")).isEqualTo("tag:foresignal,2026:INSTALL/alert/4211");
        assertThat(text(entry, ATOM, "title")).isEqualTo("Quakes <M5> & \"more\"");
        assertThat(text(entry, ATOM, "updated")).isEqualTo("2026-10-13T10:05:30Z");
        assertThat(text(entry, ATOM, "published")).isEqualTo("2026-10-13T10:00:00Z");
        Element link = children(entry, ATOM, "link").getFirst();
        assertThat(link.getAttribute("rel")).isEqualTo("alternate");
        assertThat(link.getAttribute("href")).isEqualTo("https://fs.example/v1/alerts/4211");
        List<Element> categories = children(entry, ATOM, "category");
        assertThat(categories).hasSize(2);
        assertThat(categories.get(0).getAttribute("term")).isEqualTo("critical");
        assertThat(categories.get(0).getAttribute("scheme")).isEqualTo("tag:foresignal,2026:severity");
        Element content = children(entry, ATOM, "content").getFirst();
        assertThat(content.getAttribute("type")).isEqualTo("html");
        assertThat(content.getTextContent()).isEqualTo("<p>Body &amp; <strong>bold</strong></p>");
        assertThat(text(entry, ATOM, "summary")).isEqualTo("Body & bold");
        assertThat(text(entry, GeoRss.NAMESPACE, "point")).isEqualTo("30.25 -97.5");
    }

    @Test
    @DisplayName("a small polygon is georss:polygon (lat lon pairs, closed); more than 500 vertices fall back to the centroid")
    void geoRssPolygon() throws Exception {
        Element small = children(parse(writer.write(FeedFixtures.feed(FeedFixtures.entry(1, FeedFixtures.polygon(4)))))
                .getDocumentElement(), ATOM, "entry").getFirst();
        String polygon = text(small, GeoRss.NAMESPACE, "polygon");
        assertThat(polygon).isEqualTo("30 -96 31 -97 30 -98 29 -97 30 -96");
        assertThat(text(small, GeoRss.NAMESPACE, "point")).isNull();

        Element large = children(parse(writer.write(FeedFixtures.feed(FeedFixtures.entry(2, FeedFixtures.polygon(600)))))
                .getDocumentElement(), ATOM, "entry").getFirst();
        assertThat(text(large, GeoRss.NAMESPACE, "polygon")).isNull();
        String[] centroid = text(large, GeoRss.NAMESPACE, "point").split(" ");
        assertThat(Double.parseDouble(centroid[0])).isCloseTo(30.0, org.assertj.core.data.Offset.offset(1e-6));
        assertThat(Double.parseDouble(centroid[1])).isCloseTo(-97.0, org.assertj.core.data.Offset.offset(1e-6));
    }

    @Test
    @DisplayName("GeoRss: 500 vertices is still a polygon, a line is its centroid, empty is nothing")
    void geoRssLimits() {
        assertThat(GeoRss.of(FeedFixtures.polygon(500)).element()).isEqualTo("polygon");
        assertThat(GeoRss.of(FeedFixtures.polygon(501)).element()).isEqualTo("point");
        assertThat(GeoRss.of(null)).isNull();
        assertThat(GeoRss.of(FeedFixtures.GEOMETRY.createLineString(new org.locationtech.jts.geom.Coordinate[] {
                new org.locationtech.jts.geom.Coordinate(0, 0), new org.locationtech.jts.geom.Coordinate(2, 2)})).value())
                .isEqualTo("1 1");
        assertThat(GeoRss.number(-0.0)).isEqualTo("0");
        assertThat(GeoRss.number(1.123456789)).isEqualTo("1.1234568");
    }

    @Test
    @DisplayName("control characters in upstream text never break the document; an empty feed is valid")
    void controlCharacters() throws Exception {
        FeedEntry dirty = new FeedEntry("tag:x/1", "bad\u0001title\u0008", null, null, FeedFixtures.FIRED,
                "<p>x\u0002</p>", "x", List.of(), null, null);
        Document doc = parse(writer.write(FeedFixtures.feed(dirty)));
        assertThat(text(children(doc.getDocumentElement(), ATOM, "entry").getFirst(), ATOM, "title"))
                .isEqualTo("badtitle");
        assertThat(parse(writer.write(FeedFixtures.feed())).getDocumentElement().getElementsByTagNameNS(ATOM, "entry")
                .getLength()).isZero();
        assertThat(AtomFeedWriter.time(null)).isEqualTo("1970-01-01T00:00:00Z");
    }
}
