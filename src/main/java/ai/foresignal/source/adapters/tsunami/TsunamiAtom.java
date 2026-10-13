package ai.foresignal.source.adapters.tsunami;

import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Reads the CAP links of a tsunami.gov Atom feed ({@code PAAQAtom.xml}, {@code PHEBAtom.xml}; prompt 067).
 *
 * <p>Verified 2026-10: the feed holds one {@code entry}, the latest message, whose
 * {@code <link rel="related" type="application/cap+xml" href="…/{center}CAP.xml"/>} points at that message's
 * own CAP document. Feed-level links (travel-time and energy maps) are images and are ignored. The XML is read
 * with {@link CapParser#secureInputFactory()}: no DTDs, no external entities, no entity expansion.
 *
 * <p>Only extracts the links; whether one may be followed is the adapter's host and path allowlist.
 */
public final class TsunamiAtom {

    /** The Atom namespace. */
    public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

    /** The media type of a CAP link. */
    public static final String CAP_MEDIA_TYPE = "application/cap+xml";

    private TsunamiAtom() {}

    /**
     * The {@code href} of every entry link with {@code rel="related"} and {@code type="application/cap+xml"},
     * in document order.
     *
     * @throws UpstreamFormatException when the document is empty, malformed, declares a DOCTYPE or is not an
     *     Atom feed
     */
    public static List<String> capLinks(byte[] xml) {
        if (xml == null || xml.length == 0) {
            throw new UpstreamFormatException("The tsunami Atom feed is empty", "");
        }
        List<String> out = new ArrayList<>();
        XMLStreamReader reader = null;
        try {
            reader = CapParser.secureInputFactory().createXMLStreamReader(new ByteArrayInputStream(xml));
            boolean sawFeed = false;
            int entryDepth = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.DTD || event == XMLStreamConstants.ENTITY_REFERENCE) {
                    throw new UpstreamFormatException("DOCTYPE declarations and entity references are not allowed", "");
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    boolean atom = ATOM_NAMESPACE.equals(reader.getNamespaceURI());
                    String name = reader.getLocalName();
                    if (!sawFeed) {
                        if (!atom || !"feed".equals(name)) {
                            throw new UpstreamFormatException("Not an Atom feed: root element '" + name + "'", "");
                        }
                        sawFeed = true;
                    } else if (atom && "entry".equals(name)) {
                        entryDepth++;
                    } else if (atom && "link".equals(name) && entryDepth > 0
                            && "related".equals(reader.getAttributeValue(null, "rel"))
                            && CAP_MEDIA_TYPE.equals(reader.getAttributeValue(null, "type"))) {
                        String href = reader.getAttributeValue(null, "href");
                        if (href != null && !href.isBlank()) {
                            out.add(href.strip());
                        }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT && entryDepth > 0
                        && ATOM_NAMESPACE.equals(reader.getNamespaceURI()) && "entry".equals(reader.getLocalName())) {
                    entryDepth--;
                }
            }
            if (!sawFeed) {
                throw new UpstreamFormatException("The tsunami Atom feed has no element", "");
            }
            return out;
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException("The tsunami Atom feed could not be parsed: " + e.getMessage(), "", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                    // Nothing else to release.
                }
            }
        }
    }
}
