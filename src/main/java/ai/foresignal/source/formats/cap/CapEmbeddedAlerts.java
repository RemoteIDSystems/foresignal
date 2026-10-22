package ai.foresignal.source.formats.cap;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * The CAP alerts carried whole inside a feed, each re-serialized as its own document for {@link CapParser}.
 *
 * <p>Brazil's national feed ({@code idapfile.mdr.gov.br/idap/api/rss/cap}) is Atom whose entries have no link:
 * each {@code <content type="text/xml">} holds a complete CAP 1.2 {@code <alert>} (55 entries, 6.6 MB on
 * 2026-10-22). Every {@code alert} element in a CAP 1.2 or 1.1 namespace is copied out, at any depth, with its
 * namespace declarations repaired so it stands alone; an alert inside another is part of its parent.
 */
public final class CapEmbeddedAlerts {

    private CapEmbeddedAlerts() {}

    /**
     * Copies out every embedded alert, in document order.
     *
     * @throws UpstreamFormatException when the feed is not well-formed XML or declares a DOCTYPE
     */
    public static List<byte[]> extract(byte[] feed) {
        List<byte[]> out = new ArrayList<>();
        XMLOutputFactory outputs = XMLOutputFactory.newDefaultFactory();
        outputs.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        try {
            XMLEventReader reader = CapParser.secureInputFactory()
                    .createXMLEventReader(new ByteArrayInputStream(feed));
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.getEventType() == XMLEvent.DTD) {
                    throw new UpstreamFormatException("The CAP feed declares a DOCTYPE", "");
                }
                if (event.isStartElement() && isAlert(event.asStartElement())) {
                    out.add(copy(event, reader, outputs));
                }
            }
            reader.close();
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException("The CAP feed is not well-formed XML: " + e.getMessage(), "");
        }
        return out;
    }

    private static boolean isAlert(StartElement element) {
        String ns = element.getName().getNamespaceURI();
        return "alert".equals(element.getName().getLocalPart())
                && (CapParser.NAMESPACE_1_2.equals(ns) || CapParser.NAMESPACE_1_1.equals(ns));
    }

    private static byte[] copy(XMLEvent start, XMLEventReader reader, XMLOutputFactory outputs)
            throws XMLStreamException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        XMLEventWriter writer = outputs.createXMLEventWriter(bytes, "UTF-8");
        writer.add(start);
        int depth = 1;
        while (depth > 0 && reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                depth++;
            } else if (event.isEndElement()) {
                depth--;
            }
            writer.add(event);
        }
        writer.flush();
        writer.close();
        return bytes.toByteArray();
    }
}
