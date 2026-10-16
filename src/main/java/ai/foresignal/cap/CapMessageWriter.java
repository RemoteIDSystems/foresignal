package ai.foresignal.cap;

import ai.foresignal.feed.XmlText;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Writes a {@link CapMessage} as OASIS CAP 1.2 XML (namespace {@value #NAMESPACE}) with StAX, in the element
 * order the CAP 1.2 schema requires. Times are {@code YYYY-MM-DDThh:mm:ss+00:00} (the schema's pattern
 * allows neither {@code Z} nor fractions); {@code scope} is always {@code Public}, the
 * {@link CapMessage#NOTE} is always present and {@code msgType} is never {@code Cancel}.
 */
public final class CapMessageWriter {

    /** The CAP 1.2 namespace. */
    public static final String NAMESPACE = "urn:oasis:names:tc:emergency:cap:1.2";

    /** The CAP media type. */
    public static final String MEDIA_TYPE = "application/cap+xml";

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+00:00'",
            Locale.ROOT).withZone(ZoneOffset.UTC);

    private static final XMLOutputFactory FACTORY = XMLOutputFactory.newFactory();

    private CapMessageWriter() {}

    /** A CAP time: second precision, UTC as {@code +00:00}. */
    public static String time(Instant instant) {
        return TIME.format(instant.truncatedTo(ChronoUnit.SECONDS));
    }

    /** One {@code references} entry: {@code sender,identifier,sent}. */
    public static String reference(String sender, String identifier, Instant sent) {
        return sender + "," + identifier + "," + time(sent);
    }

    /** {@code message} as a CAP 1.2 document (UTF-8 text). */
    public static String write(CapMessage message) {
        StringWriter out = new StringWriter(4096);
        try {
            XMLStreamWriter w = FACTORY.createXMLStreamWriter(out);
            w.writeStartDocument("UTF-8", "1.0");
            w.writeStartElement("alert");
            w.writeDefaultNamespace(NAMESPACE);
            text(w, "identifier", message.identifier());
            text(w, "sender", message.sender());
            text(w, "sent", time(message.sent()));
            text(w, "status", message.status().name());
            text(w, "msgType", message.msgType().name());
            text(w, "scope", "Public");
            text(w, "note", CapMessage.NOTE);
            if (!message.references().isEmpty()) {
                text(w, "references", String.join(" ", message.references()));
            }
            info(w, message.info());
            w.writeEndElement();
            w.writeEndDocument();
            w.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Cannot write the CAP message", e);
        }
        return out.toString();
    }

    private static void info(XMLStreamWriter w, CapMessage.Info info) throws XMLStreamException {
        w.writeStartElement("info");
        text(w, "language", info.language() == null ? "en-US" : info.language());
        text(w, "category", info.category());
        text(w, "event", info.event());
        if (info.allClear()) {
            text(w, "responseType", "AllClear");
        }
        text(w, "urgency", info.urgency());
        text(w, "severity", info.severity());
        text(w, "certainty", info.certainty());
        if (info.effective() != null) {
            text(w, "effective", time(info.effective()));
        }
        if (info.expires() != null) {
            text(w, "expires", time(info.expires()));
        }
        text(w, "senderName", info.senderName());
        if (info.headline() != null) {
            text(w, "headline", info.headline());
        }
        if (info.description() != null) {
            text(w, "description", info.description());
        }
        if (info.web() != null) {
            text(w, "web", info.web());
        }
        for (Map.Entry<String, String> p : info.parameters().entrySet()) {
            if (p.getValue() == null) {
                continue;
            }
            w.writeStartElement("parameter");
            text(w, "valueName", p.getKey());
            text(w, "value", p.getValue());
            w.writeEndElement();
        }
        CapMessage.Area area = info.area();
        if (area != null) {
            w.writeStartElement("area");
            text(w, "areaDesc", area.areaDesc());
            if (area.polygon() != null && !area.polygon().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (double[] p : area.polygon()) {
                    if (!sb.isEmpty()) {
                        sb.append(' ');
                    }
                    sb.append(coordinate(p[0])).append(',').append(coordinate(p[1]));
                }
                text(w, "polygon", sb.toString());
            } else if (area.circle() != null) {
                double[] c = area.circle();
                text(w, "circle", coordinate(c[0]) + "," + coordinate(c[1]) + " " + coordinate(c[2]));
            }
            w.writeEndElement();
        }
        w.writeEndElement();
    }

    /** A coordinate or radius with at most six decimals and no exponent. */
    static String coordinate(double value) {
        java.math.BigDecimal d = java.math.BigDecimal.valueOf(value).setScale(6, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return d.scale() < 0 ? d.setScale(0).toPlainString() : d.toPlainString();
    }

    private static void text(XMLStreamWriter w, String name, String value) throws XMLStreamException {
        w.writeStartElement(name);
        w.writeCharacters(XmlText.clean(value == null ? "" : value));
        w.writeEndElement();
    }
}
