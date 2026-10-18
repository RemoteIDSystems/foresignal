package ai.foresignal.fdsn;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Writes QuakeML 1.2 BED documents (prompt 141).
 *
 * <p>Shape: {@code <q:quakeml xmlns:q="http://quakeml.org/xmlns/quakeml/1.2" xmlns="http://quakeml.org/xmlns/bed/1.2">}
 * holding one {@code eventParameters} with one {@code event} per earthquake. Each event carries its
 * {@code description} (region name), {@code type}, one {@code origin} (time, latitude, longitude, depth in
 * <em>meters</em> as QuakeML requires), one {@code magnitude} when the record has one (mag, type, originID), the
 * preferred origin and magnitude ids, and {@code creationInfo} (agency, last update).
 *
 * <p>PublicIDs are {@code smi:foresignal/{sourceId}/{externalId}} with {@code /origin} and {@code /magnitude}
 * suffixes. Characters the BED {@code ResourceIdentifier} pattern does not allow in the resource path are written as
 * {@code ~XX} per UTF-8 byte ({@link #resourceId}), so every id validates.
 */
public final class QuakeMlWriter {

    /** The QuakeML root namespace. */
    public static final String QUAKEML_NS = "http://quakeml.org/xmlns/quakeml/1.2";

    /** The QuakeML BED namespace. */
    public static final String BED_NS = "http://quakeml.org/xmlns/bed/1.2";

    /** The publicID of the {@code eventParameters} element. */
    public static final String PARAMETERS_ID = "smi:foresignal/fdsnws/event/1/query";

    private static final XMLOutputFactory FACTORY = XMLOutputFactory.newFactory();

    private QuakeMlWriter() {}

    /**
     * The QuakeML document of {@code events}.
     *
     * @param events the events, in output order
     * @param created when the document was made ({@code eventParameters/creationInfo/creationTime})
     * @return the UTF-8 XML text
     */
    public static String write(List<FdsnEvent> events, Instant created) {
        StringWriter out = new StringWriter();
        try {
            XMLStreamWriter xml = FACTORY.createXMLStreamWriter(out);
            xml.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            xml.writeStartElement("q", "quakeml", QUAKEML_NS);
            xml.writeNamespace("q", QUAKEML_NS);
            xml.writeDefaultNamespace(BED_NS);
            xml.writeStartElement("eventParameters");
            xml.writeAttribute("publicID", PARAMETERS_ID);
            xml.writeStartElement("creationInfo");
            element(xml, "agencyID", "Foresignal");
            element(xml, "creationTime", created.toString());
            xml.writeEndElement();
            for (FdsnEvent event : events) {
                event(xml, event);
            }
            xml.writeEndElement();
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Could not write QuakeML", e);
        }
        return out.toString();
    }

    private static void event(XMLStreamWriter xml, FdsnEvent event) throws XMLStreamException {
        String id = publicId(event.sourceId(), event.externalId());
        String originId = id + "/origin";
        String magnitudeId = id + "/magnitude";
        xml.writeStartElement("event");
        xml.writeAttribute("publicID", id);
        element(xml, "preferredOriginID", originId);
        if (event.magnitude() != null) {
            element(xml, "preferredMagnitudeID", magnitudeId);
        }
        element(xml, "type", event.eventType());
        if (event.place() != null) {
            xml.writeStartElement("description");
            element(xml, "text", event.place());
            element(xml, "type", "region name");
            xml.writeEndElement();
        }
        creationInfo(xml, event);
        xml.writeStartElement("origin");
        xml.writeAttribute("publicID", originId);
        quantity(xml, "time", event.time().toString());
        quantity(xml, "latitude", Double.toString(event.latitude()));
        quantity(xml, "longitude", Double.toString(event.longitude()));
        if (event.depthKm() != null) {
            quantity(xml, "depth", Long.toString(Math.round(event.depthKm() * 1000)));
        }
        creationInfo(xml, event);
        xml.writeEndElement();
        if (event.magnitude() != null) {
            xml.writeStartElement("magnitude");
            xml.writeAttribute("publicID", magnitudeId);
            quantity(xml, "mag", Double.toString(event.magnitude()));
            if (event.magnitudeType() != null) {
                element(xml, "type", cut(event.magnitudeType(), 32));
            }
            element(xml, "originID", originId);
            creationInfo(xml, event);
            xml.writeEndElement();
        }
        xml.writeEndElement();
    }

    private static void creationInfo(XMLStreamWriter xml, FdsnEvent event) throws XMLStreamException {
        if (event.author() == null && event.updated() == null) {
            return;
        }
        xml.writeStartElement("creationInfo");
        if (event.author() != null) {
            element(xml, "agencyID", cut(event.author(), 64));
        }
        if (event.updated() != null) {
            element(xml, "creationTime", event.updated().toString());
        }
        xml.writeEndElement();
    }

    private static void quantity(XMLStreamWriter xml, String name, String value) throws XMLStreamException {
        xml.writeStartElement(name);
        element(xml, "value", value);
        xml.writeEndElement();
    }

    private static void element(XMLStreamWriter xml, String name, String text) throws XMLStreamException {
        xml.writeStartElement(name);
        xml.writeCharacters(text);
        xml.writeEndElement();
    }

    private static String cut(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }

    /** {@code smi:foresignal/{sourceId}/{externalId}}, each part made safe by {@link #resourceId}. */
    public static String publicId(String sourceId, String externalId) {
        return "smi:foresignal/" + resourceId(sourceId) + "/" + resourceId(externalId);
    }

    /**
     * {@code text} with every character outside the BED resource-path set ({@code A-Z a-z 0-9 - . * ( ) _ '}, and after
     * the first character also {@code = , ; +}) written as {@code ~XX} per UTF-8 byte, and a literal {@code ~} as
     * {@code ~7E}, so the mapping stays reversible. An empty text becomes {@code ~}.
     */
    static String resourceId(String text) {
        if (text == null || text.isEmpty()) {
            return "~";
        }
        StringBuilder out = new StringBuilder();
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            int c = b & 0xFF;
            boolean first = out.isEmpty();
            boolean safe = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '.' || c == '*' || c == '(' || c == ')' || c == '_' || c == '\''
                    || (!first && (c == '=' || c == ',' || c == ';' || c == '+'));
            if (safe) {
                out.append((char) c);
            } else {
                out.append('~').append(String.format("%02X", c));
            }
        }
        return out.toString();
    }
}
