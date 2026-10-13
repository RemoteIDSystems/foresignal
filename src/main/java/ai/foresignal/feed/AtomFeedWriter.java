package ai.foresignal.feed;

import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.springframework.stereotype.Component;

/**
 * Writes a {@link Feed} as RFC 4287 Atom with StAX ({@code XMLStreamWriter}), with GeoRSS-Simple
 * geometry ({@link GeoRss}); media type {@value #MEDIA_TYPE}.
 *
 * <p>Every string goes through {@link XmlText#clean} and the writer's own escaping; the HTML content is
 * written as escaped text inside {@code content type="html"}, never as CDATA. Times are RFC 3339 UTC
 * with second precision.
 */
@Component
public class AtomFeedWriter {

    /** The Atom media type. */
    public static final String MEDIA_TYPE = "application/atom+xml";

    /** The Atom namespace. */
    public static final String ATOM = "http://www.w3.org/2005/Atom";

    private final XMLOutputFactory factory = XMLOutputFactory.newFactory();

    /** {@code feed} as an Atom document (UTF-8 text). */
    public String write(Feed feed) {
        StringWriter out = new StringWriter(8192);
        try {
            XMLStreamWriter w = factory.createXMLStreamWriter(out);
            w.writeStartDocument("UTF-8", "1.0");
            w.writeStartElement("feed");
            w.writeDefaultNamespace(ATOM);
            w.writeNamespace("georss", GeoRss.NAMESPACE);
            text(w, "id", feed.id());
            textWithType(w, "title", feed.title());
            if (feed.description() != null) {
                textWithType(w, "subtitle", feed.description());
            }
            text(w, "updated", time(feed.updated()));
            link(w, "self", feed.feedUrl(), MEDIA_TYPE);
            if (feed.homeUrl() != null) {
                link(w, "alternate", feed.homeUrl(), null);
            }
            author(w);
            text(w, "generator", Feed.AUTHOR);
            for (FeedEntry entry : feed.entries()) {
                entry(w, entry);
            }
            w.writeEndElement();
            w.writeEndDocument();
            w.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Cannot write the Atom feed", e);
        }
        return out.toString();
    }

    private static void entry(XMLStreamWriter w, FeedEntry entry) throws XMLStreamException {
        w.writeStartElement("entry");
        text(w, "id", entry.id());
        textWithType(w, "title", entry.title());
        text(w, "updated", time(entry.updated()));
        if (entry.published() != null) {
            text(w, "published", time(entry.published()));
        }
        if (entry.link() != null) {
            link(w, "alternate", entry.link(), null);
        }
        for (FeedEntry.Category category : entry.categories()) {
            w.writeEmptyElement("category");
            w.writeAttribute("term", XmlText.clean(category.term()));
            if (category.scheme() != null) {
                w.writeAttribute("scheme", XmlText.clean(category.scheme()));
            }
        }
        author(w);
        if (entry.text() != null && !entry.text().isEmpty()) {
            textWithType(w, "summary", entry.text());
        }
        w.writeStartElement("content");
        w.writeAttribute("type", "html");
        w.writeCharacters(XmlText.clean(entry.html() == null ? "" : entry.html()));
        w.writeEndElement();
        GeoRss geo = GeoRss.of(entry.geometry());
        if (geo != null) {
            w.writeStartElement("georss", geo.element(), GeoRss.NAMESPACE);
            w.writeCharacters(geo.value());
            w.writeEndElement();
        }
        w.writeEndElement();
    }

    private static void author(XMLStreamWriter w) throws XMLStreamException {
        w.writeStartElement("author");
        text(w, "name", Feed.AUTHOR);
        w.writeEndElement();
    }

    private static void link(XMLStreamWriter w, String rel, String href, String type) throws XMLStreamException {
        w.writeEmptyElement("link");
        w.writeAttribute("rel", rel);
        w.writeAttribute("href", XmlText.clean(href));
        if (type != null) {
            w.writeAttribute("type", type);
        }
    }

    private static void text(XMLStreamWriter w, String name, String value) throws XMLStreamException {
        w.writeStartElement(name);
        w.writeCharacters(XmlText.clean(value == null ? "" : value));
        w.writeEndElement();
    }

    private static void textWithType(XMLStreamWriter w, String name, String value) throws XMLStreamException {
        w.writeStartElement(name);
        w.writeAttribute("type", "text");
        w.writeCharacters(XmlText.clean(value == null ? "" : value));
        w.writeEndElement();
    }

    /** RFC 3339 UTC with second precision; null is the epoch. */
    static String time(Instant instant) {
        return (instant == null ? Instant.EPOCH : instant).truncatedTo(ChronoUnit.SECONDS).toString();
    }
}
