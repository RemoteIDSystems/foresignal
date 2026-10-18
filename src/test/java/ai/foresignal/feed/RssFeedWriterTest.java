package ai.foresignal.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** {@link RssFeedWriter} (prompt 141): RSS 2.0 with enclosures, escaped text and RFC 822 dates. */
class RssFeedWriterTest {

    final RssFeedWriter writer = new RssFeedWriter();

    static FeedEntry entry(String id, String title, Instant at) {
        return new FeedEntry(id, title, "https://fs.example/v1/briefings/b/runs/" + id + "?format=html", at, at,
                "<p>html</p>", "Plain text of " + title, List.of(), null, null);
    }

    static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("channel and items: title, link, guid (not a permalink), pubDate, description, enclosure, atom:link self")
    void writes() throws Exception {
        Instant at = Instant.parse("2026-10-18T07:00:05.123Z");
        Feed feed = new Feed("tag:foresignal,2026:i/briefing/b/podcast", "Foresignal briefing audio: Morning",
                "Audio of a Foresignal briefing's runs, newest first", "https://fs.example/v1/briefings/b/podcast.rss",
                "https://fs.example/v1/briefings/b", at, List.of(entry("r1", "Morning <& \"quotes\">", at)));
        PodcastFeed podcast = new PodcastFeed(feed,
                List.of(new PodcastFeed.Enclosure("https://fs.example/v1/media/01J?x=1&y=2", 12345, "audio/mpeg")));

        String xml = writer.write(podcast);
        Document doc = parse(xml);

        Element rss = doc.getDocumentElement();
        assertThat(rss.getTagName()).isEqualTo("rss");
        assertThat(rss.getAttribute("version")).isEqualTo("2.0");
        Element channel = (Element) rss.getElementsByTagName("channel").item(0);
        assertThat(channel.getElementsByTagName("title").item(0).getTextContent())
                .isEqualTo("Foresignal briefing audio: Morning");
        assertThat(doc.getElementsByTagName("lastBuildDate").item(0).getTextContent())
                .isEqualTo("Sun, 18 Oct 2026 07:00:05 GMT");
        Element self = (Element) doc.getElementsByTagNameNS(AtomFeedWriter.ATOM, "link").item(0);
        assertThat(self.getAttribute("rel")).isEqualTo("self");
        assertThat(self.getAttribute("type")).isEqualTo("application/rss+xml");
        Element item = (Element) doc.getElementsByTagName("item").item(0);
        assertThat(item.getElementsByTagName("title").item(0).getTextContent()).isEqualTo("Morning <& \"quotes\">");
        Element guid = (Element) item.getElementsByTagName("guid").item(0);
        assertThat(guid.getAttribute("isPermaLink")).isEqualTo("false");
        assertThat(guid.getTextContent()).isEqualTo("r1");
        assertThat(item.getElementsByTagName("pubDate").item(0).getTextContent()).isEqualTo("Sun, 18 Oct 2026 07:00:05 GMT");
        assertThat(item.getElementsByTagName("description").item(0).getTextContent()).isEqualTo("Plain text of Morning <& \"quotes\">");
        Element enclosure = (Element) item.getElementsByTagName("enclosure").item(0);
        assertThat(enclosure.getAttribute("url")).isEqualTo("https://fs.example/v1/media/01J?x=1&y=2");
        assertThat(enclosure.getAttribute("length")).isEqualTo("12345");
        assertThat(enclosure.getAttribute("type")).isEqualTo("audio/mpeg");
        assertThat(xml).doesNotContain("CDATA");
    }

    @Test
    @DisplayName("an empty podcast is a valid channel without items; one enclosure per entry is required")
    void emptyAndMismatch() throws Exception {
        Feed empty = new Feed("tag:x", "T", null, "https://fs.example/p.rss", null, null, List.of());
        Document doc = parse(writer.write(new PodcastFeed(empty, List.of())));
        assertThat(doc.getElementsByTagName("item").getLength()).isZero();
        assertThat(doc.getElementsByTagName("link").item(0).getTextContent()).isEqualTo("https://fs.example/p.rss");
        assertThat(doc.getElementsByTagName("lastBuildDate").item(0).getTextContent())
                .isEqualTo("Thu, 1 Jan 1970 00:00:00 GMT");

        Feed one = new Feed("tag:x", "T", null, "https://fs.example/p.rss", null, Instant.EPOCH,
                List.of(entry("r", "t", Instant.EPOCH)));
        assertThatThrownBy(() -> new PodcastFeed(one, List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("control characters that XML cannot carry are removed")
    void controlCharacters() throws Exception {
        Feed feed = new Feed("tag:x", "T\u0001itle", "d", "https://fs.example/p.rss", null, Instant.EPOCH,
                List.of(entry("r", "bad\u0007bell", Instant.EPOCH)));
        String xml = writer.write(new PodcastFeed(feed, List.of(new PodcastFeed.Enclosure("u", -1, "audio/ogg"))));
        Document doc = parse(xml);
        assertThat(doc.getElementsByTagName("title").item(0).getTextContent()).isEqualTo("Title");
        assertThat(((Element) doc.getElementsByTagName("enclosure").item(0)).getAttribute("length")).isEqualTo("0");
    }
}
