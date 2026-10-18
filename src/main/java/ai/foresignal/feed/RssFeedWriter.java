package ai.foresignal.feed;

import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.springframework.stereotype.Component;

/**
 * Writes a {@link PodcastFeed} as RSS 2.0 with StAX (prompt 141, the 058 feed writer family), media type
 * {@value #MEDIA_TYPE}: one {@code <item>} per briefing run with audio, each with an {@code <enclosure url length
 * type>}, a non-permalink {@code <guid>} (the entry's tag URI), {@code <pubDate>} in RFC 822 form and the plain-text
 * rendition as {@code <description>}; the channel carries {@code <atom:link rel="self">}.
 *
 * <p>Every string goes through {@link XmlText#clean} and the writer's own escaping; nothing is written as CDATA.
 */
@Component
public class RssFeedWriter {

    /** The RSS media type. */
    public static final String MEDIA_TYPE = "application/rss+xml";

    /** RFC 822 dates as RSS 2.0 requires ({@code Sun, 18 Oct 2026 12:00:00 GMT}). */
    static final DateTimeFormatter RFC_822 = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);

    private final XMLOutputFactory factory = XMLOutputFactory.newFactory();

    /** {@code podcast} as an RSS 2.0 document (UTF-8 text). */
    public String write(PodcastFeed podcast) {
        Feed feed = podcast.feed();
        StringWriter out = new StringWriter(8192);
        try {
            XMLStreamWriter w = factory.createXMLStreamWriter(out);
            w.writeStartDocument("UTF-8", "1.0");
            w.writeStartElement("rss");
            w.writeAttribute("version", "2.0");
            w.writeNamespace("atom", AtomFeedWriter.ATOM);
            w.writeStartElement("channel");
            text(w, "title", feed.title());
            text(w, "link", feed.homeUrl() == null ? feed.feedUrl() : feed.homeUrl());
            text(w, "description", feed.description() == null ? feed.title() : feed.description());
            text(w, "generator", Feed.AUTHOR);
            text(w, "lastBuildDate", date(feed.updated()));
            w.writeEmptyElement("atom", "link", AtomFeedWriter.ATOM);
            w.writeAttribute("href", XmlText.clean(feed.feedUrl()));
            w.writeAttribute("rel", "self");
            w.writeAttribute("type", MEDIA_TYPE);
            for (int i = 0; i < feed.entries().size(); i++) {
                item(w, feed.entries().get(i), podcast.enclosures().get(i));
            }
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndDocument();
            w.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Cannot write the RSS feed", e);
        }
        return out.toString();
    }

    private static void item(XMLStreamWriter w, FeedEntry entry, PodcastFeed.Enclosure enclosure)
            throws XMLStreamException {
        w.writeStartElement("item");
        text(w, "title", entry.title());
        if (entry.link() != null) {
            text(w, "link", entry.link());
        }
        w.writeStartElement("guid");
        w.writeAttribute("isPermaLink", "false");
        w.writeCharacters(XmlText.clean(entry.id()));
        w.writeEndElement();
        text(w, "pubDate", date(entry.published() == null ? entry.updated() : entry.published()));
        if (entry.text() != null && !entry.text().isEmpty()) {
            text(w, "description", entry.text());
        }
        w.writeEmptyElement("enclosure");
        w.writeAttribute("url", XmlText.clean(enclosure.url()));
        w.writeAttribute("length", Long.toString(Math.max(0, enclosure.length())));
        w.writeAttribute("type", XmlText.clean(enclosure.type()));
        w.writeEndElement();
    }

    private static void text(XMLStreamWriter w, String name, String value) throws XMLStreamException {
        w.writeStartElement(name);
        w.writeCharacters(XmlText.clean(value == null ? "" : value));
        w.writeEndElement();
    }

    /** RFC 822 in UTC with second precision; null is the epoch. */
    static String date(Instant instant) {
        return RFC_822.format((instant == null ? Instant.EPOCH : instant).truncatedTo(ChronoUnit.SECONDS));
    }
}
