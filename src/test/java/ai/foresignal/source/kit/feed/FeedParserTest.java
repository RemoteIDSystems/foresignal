package ai.foresignal.source.kit.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Polygon;

/**
 * {@link FeedParser} (prompt 073) on real and hand-made fixtures: RSS 2.0, RSS 1.0, Atom 1.0 and JSON Feed 1.1;
 * GeoRSS Simple, GeoRSS GML and W3C Basic Geo; an XXE payload refused; HTML stripped; odd dates.
 */
class FeedParserTest {

    static FeedDocument parse(String name, String contentType) {
        return FeedParser.parse(AdapterFixtures.bytes("kit-feeds", name), contentType);
    }

    static FeedItem item(FeedDocument doc, String id) {
        return doc.items().stream().filter(i -> i.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("RSS 2.0 (NHC, captured): items, guid ids, RFC 822 dates, CDATA HTML as plain text, the author")
    void rss2() {
        FeedDocument doc = parse("nhc-index-at.xml", "text/xml");
        assertThat(doc.format()).isEqualTo(FeedDocument.RSS2);
        assertThat(doc.title()).isEqualTo("NHC Atlantic");
        assertThat(doc.link()).isEqualTo("https://www.nhc.noaa.gov/");
        assertThat(doc.language()).isEqualTo("en-us");
        assertThat(doc.items()).hasSize(2);
        FeedItem outlook = doc.items().get(0);
        assertThat(outlook.id()).isEqualTo("https://www.nhc.noaa.gov/gtwo.php?basin=atlc&202610131717");
        assertThat(outlook.title()).isEqualTo("Atlantic Tropical Weather Outlook");
        assertThat(outlook.link()).isEqualTo("https://www.nhc.noaa.gov/gtwo.php?basin=atlc");
        assertThat(outlook.published()).isEqualTo(Instant.parse("2026-10-13T17:17:38Z"));
        assertThat(outlook.summary()).startsWith("958 ABNT20 KNHC 131717 TWOAT Tropical Weather Outlook")
                .doesNotContain("<br").doesNotContain("  ");
        assertThat(outlook.authors()).containsExactly("nhcwebmaster@noaa.gov (NHC Webmaster)");
        assertThat(outlook.geometry()).isNull();
    }

    @Test
    @DisplayName("RSS 2.0 (ECDC, captured): escaped HTML descriptions become text; dc:creator; offsets applied")
    void rss2Escaped() {
        FeedDocument doc = parse("ecdc-1307.xml", "application/rss+xml; charset=utf-8");
        assertThat(doc.items()).hasSize(10);
        FeedItem first = doc.items().get(0);
        assertThat(first.summary()).startsWith("Since May 2026, an Ebola virus disease outbreak").doesNotContain("<p>");
        assertThat(first.published()).isEqualTo(Instant.parse("2026-10-12T15:36:21Z"));
        assertThat(first.authors()).containsExactly("ECDC");
        assertThat(doc.items().get(1).summary()).doesNotContain("&nbsp;").doesNotContain("&amp;");
    }

    @Test
    @DisplayName("RSS 1.0: rdf:about ids, dc:date, dc:creator and dc:subject, relative links resolved")
    void rss1() {
        FeedDocument doc = parse("rss1.rdf", null);
        assertThat(doc.format()).isEqualTo(FeedDocument.RSS1);
        assertThat(doc.title()).isEqualTo("Example RSS 1.0 bulletins");
        assertThat(doc.language()).isEqualTo("en");
        FeedItem first = item(doc, "https://bulletins.example/b/1");
        assertThat(first.title()).isEqualTo("First & foremost");
        assertThat(first.summary()).isEqualTo("Plain bold text");
        assertThat(first.published()).isEqualTo(Instant.parse("2026-10-12T06:30:00Z"));
        assertThat(first.authors()).containsExactly("Duty Officer");
        assertThat(first.categories()).containsExactly("advisory");
        FeedItem second = item(doc, "https://bulletins.example/b/2");
        assertThat(second.link()).isEqualTo("https://bulletins.example/b/2");
        assertThat(second.published()).isEqualTo(Instant.parse("2026-10-12T00:00:00Z"));
        assertThat(doc.warnings()).anyMatch(w -> w.contains("midnight UTC"));
    }

    @Test
    @DisplayName("Atom: self and hub links, html and xhtml text, enclosure, categories, GML polygon with a hole")
    void atom() {
        FeedDocument doc = parse("atom.xml", "application/atom+xml");
        assertThat(doc.format()).isEqualTo(FeedDocument.ATOM);
        assertThat(doc.self()).isEqualTo("https://bulletins.example/atom.xml");
        assertThat(doc.hubs()).containsExactly("https://hub.example/");
        assertThat(doc.link()).isEqualTo("https://bulletins.example/");
        FeedItem flood = item(doc, "tag:bulletins.example,2026:entry-1");
        assertThat(flood.title()).isEqualTo("Flood warning");
        assertThat(flood.summary()).isEqualTo("Levels rising. Stay away.");
        assertThat(flood.link()).isEqualTo("https://bulletins.example/e/1");
        assertThat(flood.published()).isEqualTo(Instant.parse("2026-10-13T10:00:00Z"));
        assertThat(flood.updated()).isEqualTo(Instant.parse("2026-10-13T15:30:00Z"));
        assertThat(flood.authors()).containsExactly("River Office");
        assertThat(flood.categories()).containsExactly("Flood", "warning");
        assertThat(flood.enclosures()).containsExactly(new FeedItem.Enclosure("https://bulletins.example/e/1.pdf",
                "application/pdf", 12345L));
        assertThat(flood.language()).isEqualTo("en");
        Polygon polygon = (Polygon) flood.geometry();
        assertThat(polygon.getNumInteriorRing()).isEqualTo(1);
        assertThat(polygon.getExteriorRing().getCoordinateN(1).x).as("lat-lon read as lon -74").isEqualTo(-74.0);
        assertThat(polygon.getSRID()).isEqualTo(4326);
        FeedItem crs84 = item(doc, "tag:bulletins.example,2026:entry-2");
        assertThat(crs84.link()).as("javascript: links are dropped").isNull();
        assertThat(crs84.summary()).isEqualTo("Plain content, no summary.");
        assertThat(crs84.geometry().getCoordinate().x).as("CRS84 is lon-lat").isEqualTo(-122.5);
    }

    @Test
    @DisplayName("JSON Feed 1.1: hubs, feed_url, tags, attachments, _geo, HTML content stripped, numeric id")
    void jsonFeed() {
        FeedDocument doc = parse("jsonfeed.json", "application/feed+json");
        assertThat(doc.format()).isEqualTo(FeedDocument.JSON_FEED);
        assertThat(doc.self()).isEqualTo("https://bulletins.example/feed.json");
        assertThat(doc.hubs()).containsExactly("https://hub.example/websub");
        FeedItem quake = item(doc, "j-1");
        assertThat(quake.summary()).isEqualTo("Shaking reported.").doesNotContain("alert");
        assertThat(quake.categories()).containsExactly("earthquake", "felt");
        assertThat(quake.authors()).containsExactly("Seismic Desk");
        assertThat(quake.enclosures()).containsExactly(new FeedItem.Enclosure("https://bulletins.example/j/1.png",
                "image/png", 2048L));
        assertThat(quake.updated()).isEqualTo(Instant.parse("2026-10-13T07:00:00Z"));
        assertThat(quake.geometry().getCoordinate().x).isEqualTo(139.7);
        FeedItem numeric = item(doc, "42");
        assertThat(numeric.title()).isNull();
        assertThat(numeric.link()).isEqualTo("https://other.example/42");
        assertThat(numeric.published()).isEqualTo(Instant.parse("2026-10-13T05:00:00Z"));
        assertThat(doc.warnings()).anyMatch(w -> w.contains("without a zone read as UTC"));
        // Detected by the first byte when the content type says nothing useful.
        assertThat(parse("jsonfeed.json", "application/octet-stream").format()).isEqualTo(FeedDocument.JSON_FEED);
    }

    @Test
    @DisplayName("GeoRSS Simple: point, line, unclosed polygon, box (lat lon order); out of range left out")
    void georssSimple() {
        FeedDocument doc = parse("georss-simple.xml", null);
        assertThat(doc.self()).isEqualTo("https://bulletins.example/geo.xml");
        assertThat(doc.hubs()).containsExactly("https://hub.example/");
        assertThat(item(doc, "g-point").geometry().getCoordinate().x).isEqualTo(-71.92);
        assertThat(item(doc, "g-point").categories()).containsExactly("a", "b");
        assertThat(item(doc, "g-point").enclosures()).hasSize(1);
        assertThat(item(doc, "g-line").geometry().getGeometryType()).isEqualTo("LineString");
        assertThat(item(doc, "g-line").geometry().getNumPoints()).isEqualTo(3);
        assertThat(item(doc, "g-polygon").geometry().getGeometryType()).isEqualTo("Polygon");
        assertThat(item(doc, "g-polygon").geometry().getNumPoints()).isEqualTo(4);
        assertThat(item(doc, "g-box").geometry().getEnvelopeInternal().getMinX()).isEqualTo(-71.032);
        assertThat(item(doc, "g-box").geometry().getEnvelopeInternal().getMaxY()).isEqualTo(43.039);
        assertThat(item(doc, "g-bad").geometry()).isNull();
        assertThat(doc.warnings()).anyMatch(w -> w.startsWith("geometry left out"));
        assertThat(item(doc, "g-encoded").summary()).isEqualTo("From content:encoded");
    }

    @Test
    @DisplayName("W3C Basic Geo, direct and inside geo:Point")
    void basicGeo() {
        FeedDocument doc = parse("basic-geo.xml", "application/rss+xml");
        assertThat(item(doc, "bg-1").geometry().getCoordinate().y).isEqualTo(51.5);
        assertThat(item(doc, "bg-2").geometry().getCoordinate().x).isEqualTo(2.35);
    }

    @Test
    @DisplayName("an XXE payload is refused, never resolved")
    void xxe() {
        assertThatThrownBy(() -> parse("xxe.xml", "application/rss+xml"))
                .isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("DOCTYPE")
                .hasMessageNotContaining("root:");
    }

    @Test
    @DisplayName("odd dates: two-digit years, named zones, no day name, offsets; unreadable dates are null")
    void oddDates() {
        FeedDocument doc = parse("odd-dates.xml", null);
        assertThat(item(doc, "d-1").published()).isEqualTo(Instant.parse("2026-10-13T17:17:38Z"));
        assertThat(item(doc, "d-2").published()).isEqualTo(Instant.parse("2026-10-03T01:35:00Z"));
        assertThat(item(doc, "d-3").published()).isEqualTo(Instant.parse("1999-01-01T00:00:00Z"));
        assertThat(item(doc, "d-4").published()).isNull();
        assertThat(doc.warnings()).anyMatch(w -> w.contains("yesterday"));
        FeedItem noGuid = doc.items().get(4);
        assertThat(noGuid.id()).hasSize(16).matches("[0-9a-f]{16}");
        assertThat(noGuid.id()).isEqualTo(FeedParser.id(null, "https://bulletins.example/n", "No guid"));
        assertThat(noGuid.published()).isEqualTo(Instant.parse("2026-10-12T18:00:00Z"));
        List<String> warnings = new ArrayList<>();
        assertThat(FeedDates.parse("Sun, 6 Sep 2026 9:05 UTC", warnings)).isEqualTo(Instant.parse(
                "2026-09-06T09:05:00Z"));
        assertThat(FeedDates.parse("2026-10-13T10:00:00.123+02:00", warnings)).isEqualTo(Instant.parse(
                "2026-10-13T08:00:00.123Z"));
        assertThat(FeedDates.parse("13 Oct 2026 10:00:00", warnings)).isEqualTo(Instant.parse("2026-10-13T10:00:00Z"));
        assertThat(warnings).anyMatch(w -> w.contains("without a zone"));
    }

    @Test
    @DisplayName("HTML is stripped with jsoup Safelist.none(); text is cut at 4 KB on a character boundary")
    void plainText() {
        assertThat(FeedText.plain("<p>One</p><p>Two<br>Three</p><script>x()</script>&amp; &lt;b&gt;", 4096))
                .isEqualTo("One Two Three & <b>");
        String long1 = "é".repeat(3000);
        String cut = FeedText.cut(long1, FeedText.MAX_SUMMARY_BYTES);
        assertThat(cut.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(4096);
        assertThat(cut).hasSize(2048);
        assertThat(FeedText.link("ftp://x.example/f", null)).isNull();
        assertThat(FeedText.link("https://u:p@x.example/", null)).isNull();
        assertThat(FeedText.link("/a", "https://x.example/b/c")).isEqualTo("https://x.example/a");
    }

    @Test
    @DisplayName("not a feed, empty, oversized, and malformed bodies are format errors")
    void errors() {
        assertThatThrownBy(() -> FeedParser.parse("<html><body/></html>".getBytes(StandardCharsets.UTF_8), null))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("root element is html");
        assertThatThrownBy(() -> FeedParser.parse(new byte[0], null)).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> FeedParser.parse(new byte[FeedParser.MAX_BYTES + 1], null))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> FeedParser.parse("<rss><channel>".getBytes(StandardCharsets.UTF_8), null))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> FeedParser.parse("{\"items\": []}".getBytes(StandardCharsets.UTF_8),
                "application/json")).isInstanceOf(UpstreamFormatException.class).hasMessageContaining("JSON Feed");
    }
}
