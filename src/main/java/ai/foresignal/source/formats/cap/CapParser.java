package ai.foresignal.source.formats.cap;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * The one OASIS CAP parser of Foresignal (prompt 067; used by 024's {@code cap} ingest format, by
 * {@code tsunami-warnings}, and by 089's CAP family).
 *
 * <p><strong>Hardened StAX.</strong> DTDs are not supported, external entities are not resolved and
 * entity references are not expanded; a document that declares a DOCTYPE or contains an entity
 * reference is refused before anything is read from it, so an XXE or entity-expansion payload fails with
 * {@link UpstreamFormatException}. Input is capped at {@link CapParseOptions#maxBytes()} (2 MB by
 * default) and at {@link CapParseOptions#maxElements()} elements.
 *
 * <p><strong>Versions.</strong> The root must be {@code alert} in the CAP 1.2 namespace
 * {@value #NAMESPACE_1_2}; CAP 1.1 ({@value #NAMESPACE_1_1}) is accepted with
 * {@link CapAlert#capVersion()} {@code "1.1"}. Anything else, including an un-namespaced
 * {@code alert}, is refused. Only elements in the root's namespace are read, so an enveloped XML
 * Signature (tsunami.gov signs its alerts) is skipped.
 *
 * <p><strong>Required elements</strong> ({@code identifier, sender, sent, status, msgType, scope}) must
 * be present and non-blank. Times are parsed as {@link OffsetDateTime}; CAP's {@code -00:00} reads as
 * UTC. An unreadable time is a format error, except inside {@code references}, where it becomes null.
 *
 * <p>A pure function of its input; thread-safe.
 */
public final class CapParser {

    /** The CAP 1.2 namespace. */
    public static final String NAMESPACE_1_2 = "urn:oasis:names:tc:emergency:cap:1.2";

    /** The CAP 1.1 namespace. */
    public static final String NAMESPACE_1_1 = "urn:oasis:names:tc:emergency:cap:1.1";

    /** The CAP 1.2 namespace (024's name for it). */
    public static final String NAMESPACE = NAMESPACE_1_2;

    private static final int MAX_DEPTH = 32;

    private CapParser() {}

    /**
     * Parses one alert with the default limits.
     *
     * @throws UpstreamFormatException when the document is not a valid CAP 1.2 or 1.1 alert
     */
    public static CapAlert parse(byte[] xml) {
        if (xml == null || xml.length == 0) {
            throw new UpstreamFormatException("The CAP document is empty", "");
        }
        return parse(new ByteArrayInputStream(xml), CapParseOptions.DEFAULTS);
    }

    /**
     * Parses one alert.
     *
     * @param in the document; read up to the size cap, not closed
     * @param options the limits, or null for {@link CapParseOptions#DEFAULTS}
     * @throws UpstreamFormatException when the document is too large, not well-formed, declares a DOCTYPE,
     *     is not a CAP 1.2 or 1.1 alert, or lacks a required element
     */
    public static CapAlert parse(InputStream in, CapParseOptions options) {
        if (in == null) {
            throw new UpstreamFormatException("The CAP document is empty", "");
        }
        CapParseOptions limits = options == null ? CapParseOptions.DEFAULTS : options;
        Node root = readTree(in, limits);
        String version;
        if (NAMESPACE_1_2.equals(root.ns)) {
            version = "1.2";
        } else if (NAMESPACE_1_1.equals(root.ns)) {
            version = "1.1";
        } else {
            throw new UpstreamFormatException("Not a CAP 1.2 or 1.1 alert: root element '" + root.local
                    + "' in namespace '" + (root.ns == null ? "" : root.ns) + "'", "");
        }
        if (!"alert".equals(root.local)) {
            throw new UpstreamFormatException("Not a CAP alert: root element is '" + root.local + "'", "");
        }
        List<CapAlert.Info> infos = new ArrayList<>();
        for (Node info : root.children("info")) {
            infos.add(info(info));
        }
        return new CapAlert(
                version,
                required(root, "identifier"),
                required(root, "sender"),
                time(required(root, "sent"), "sent"),
                required(root, "status"),
                required(root, "msgType"),
                root.text("source"),
                required(root, "scope"),
                root.text("restriction"),
                root.text("addresses"),
                root.texts("code"),
                root.text("note"),
                references(root.text("references")),
                root.text("incidents"),
                infos);
    }

    /**
     * The local name of the document element, read with the same hardened reader: {@code alert} for a
     * CAP document, {@code feed} for Atom.
     *
     * @return the local name, never null
     * @throws UpstreamFormatException when the document is empty, malformed or declares a DOCTYPE
     */
    public static String rootElementName(byte[] xml) {
        if (xml == null || xml.length == 0) {
            throw new UpstreamFormatException("The XML document is empty", "");
        }
        XMLStreamReader reader = null;
        try {
            reader = secureInputFactory().createXMLStreamReader(new ByteArrayInputStream(xml));
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.DTD || event == XMLStreamConstants.ENTITY_REFERENCE) {
                    throw new UpstreamFormatException("DOCTYPE declarations and entity references are not allowed", "");
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    return reader.getLocalName();
                }
            }
            throw new UpstreamFormatException("The XML document has no element", "");
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException("The XML document could not be parsed: " + e.getMessage(), "", e);
        } finally {
            close(reader);
        }
    }

    /**
     * A StAX factory with DTD support, external entities and entity replacement switched off, and every
     * external resolution refused. The JDK's own implementation is used, so behavior does not depend on
     * what else is on the classpath. Shared with adapters that read other XML (tsunami.gov's Atom).
     */
    public static XMLInputFactory secureInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newDefaultFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        try {
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        } catch (IllegalArgumentException ignored) {
            // Not every implementation knows the JAXP 1.5 property; the resolver below refuses anyway.
        }
        factory.setXMLResolver((publicId, systemId, baseUri, namespace) -> {
            throw new XMLStreamException("External entities are not allowed");
        });
        return factory;
    }

    private static Node readTree(InputStream in, CapParseOptions limits) {
        CappedInputStream capped = new CappedInputStream(in, limits.maxBytes());
        XMLStreamReader reader = null;
        try {
            reader = secureInputFactory().createXMLStreamReader(capped);
            Node root = null;
            List<Node> stack = new ArrayList<>();
            int elements = 0;
            int skipDepth = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.DTD, XMLStreamConstants.ENTITY_REFERENCE ->
                            throw new UpstreamFormatException(
                                    "DOCTYPE declarations and entity references are not allowed in CAP", "");
                    case XMLStreamConstants.START_ELEMENT -> {
                        if (++elements > limits.maxElements()) {
                            throw new UpstreamFormatException("The CAP document has more than "
                                    + limits.maxElements() + " elements", "");
                        }
                        if (stack.size() + skipDepth >= MAX_DEPTH) {
                            throw new UpstreamFormatException("The CAP document is nested too deeply", "");
                        }
                        String ns = reader.getNamespaceURI();
                        Node node = new Node(ns == null || ns.isEmpty() ? null : ns, reader.getLocalName());
                        if (root == null) {
                            root = node;
                            stack.add(node);
                        } else if (skipDepth > 0 || !sameNamespace(root.ns, node.ns)) {
                            skipDepth++;
                        } else {
                            stack.get(stack.size() - 1).children.add(node);
                            stack.add(node);
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT -> {
                        if (skipDepth > 0) {
                            skipDepth--;
                        } else if (!stack.isEmpty()) {
                            stack.remove(stack.size() - 1);
                        }
                    }
                    case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA, XMLStreamConstants.SPACE -> {
                        if (skipDepth == 0 && !stack.isEmpty()) {
                            stack.get(stack.size() - 1).text.append(reader.getText());
                        }
                    }
                    default -> {
                        // Comments, processing instructions and the document events carry nothing.
                    }
                }
            }
            if (root == null) {
                throw new UpstreamFormatException("The CAP document has no element", "");
            }
            return root;
        } catch (XMLStreamException e) {
            if (capped.exceeded) {
                throw new UpstreamFormatException("The CAP document exceeds " + limits.maxBytes() + " bytes", "", e);
            }
            throw new UpstreamFormatException("The CAP document could not be parsed: " + e.getMessage(), "", e);
        } catch (UpstreamFormatException e) {
            throw e;
        } catch (RuntimeException e) {
            // The JDK reader can surface an I/O failure (the size cap) as an unchecked parser exception.
            if (capped.exceeded) {
                throw new UpstreamFormatException("The CAP document exceeds " + limits.maxBytes() + " bytes", "", e);
            }
            throw new UpstreamFormatException("The CAP document could not be parsed: " + e.getMessage(), "", e);
        } finally {
            close(reader);
        }
    }

    private static boolean sameNamespace(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static void close(XMLStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException ignored) {
                // Nothing to release beyond the reader itself.
            }
        }
    }

    private static CapAlert.Info info(Node info) {
        List<CapAlert.Area> areas = new ArrayList<>();
        for (Node area : info.children("area")) {
            areas.add(new CapAlert.Area(
                    area.text("areaDesc"),
                    area.texts("polygon"),
                    area.texts("circle"),
                    namedValues(area, "geocode"),
                    number(area.text("altitude"), "altitude"),
                    number(area.text("ceiling"), "ceiling")));
        }
        List<CapAlert.Resource> resources = new ArrayList<>();
        for (Node resource : info.children("resource")) {
            String size = resource.text("size");
            Long bytes = null;
            if (size != null) {
                try {
                    bytes = Long.valueOf(size);
                } catch (NumberFormatException e) {
                    throw new UpstreamFormatException("CAP resource size '" + size + "' is not an integer", size, e);
                }
            }
            resources.add(new CapAlert.Resource(resource.text("resourceDesc"), resource.text("mimeType"), bytes,
                    resource.text("uri"), resource.text("digest")));
        }
        String language = info.text("language");
        return new CapAlert.Info(
                language == null ? "en-US" : language,
                info.texts("category"),
                info.text("event"),
                info.texts("responseType"),
                info.text("urgency"),
                info.text("severity"),
                info.text("certainty"),
                info.text("audience"),
                namedValues(info, "eventCode"),
                time(info.text("effective"), "effective"),
                time(info.text("onset"), "onset"),
                time(info.text("expires"), "expires"),
                info.text("senderName"),
                info.text("headline"),
                info.text("description"),
                info.text("instruction"),
                info.text("web"),
                info.text("contact"),
                namedValues(info, "parameter"),
                resources,
                areas);
    }

    private static List<CapAlert.NamedValue> namedValues(Node parent, String name) {
        List<CapAlert.NamedValue> out = new ArrayList<>();
        for (Node node : parent.children(name)) {
            String valueName = node.text("valueName");
            if (valueName != null) {
                out.add(new CapAlert.NamedValue(valueName, node.text("value")));
            }
        }
        return out;
    }

    /**
     * {@code references}: whitespace-separated {@code sender,identifier,sent} triples. A triple without an
     * identifier is skipped; an unreadable {@code sent} becomes null.
     */
    static List<CapAlert.Reference> references(String text) {
        List<CapAlert.Reference> out = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return out;
        }
        for (String triple : text.strip().split("\\s+")) {
            String[] parts = triple.split(",", 3);
            if (parts.length >= 2 && !parts[1].isBlank()) {
                OffsetDateTime sent = null;
                if (parts.length == 3) {
                    try {
                        sent = parseTime(parts[2]);
                    } catch (DateTimeException e) {
                        sent = null;
                    }
                }
                out.add(new CapAlert.Reference(parts[0], parts[1], sent));
            }
        }
        return out;
    }

    private static String required(Node parent, String name) {
        String value = parent.text(name);
        if (value == null) {
            throw new UpstreamFormatException("The CAP alert has no '" + name + "'", "");
        }
        return value;
    }

    private static OffsetDateTime time(String text, String element) {
        if (text == null) {
            return null;
        }
        try {
            return parseTime(text);
        } catch (DateTimeException e) {
            throw new UpstreamFormatException("CAP " + element + " '" + text + "' is not a date-time", text, e);
        }
    }

    /** A CAP date-time; {@code -00:00} (CAP's spelling of UTC) reads as {@code Z}. */
    static OffsetDateTime parseTime(String text) {
        String value = text.strip();
        if (value.endsWith("-00:00")) {
            value = value.substring(0, value.length() - 6) + "Z";
        }
        return OffsetDateTime.parse(value);
    }

    private static Double number(String text, String element) {
        if (text == null) {
            return null;
        }
        try {
            double value = Double.parseDouble(text);
            if (!Double.isFinite(value)) {
                throw new NumberFormatException("not finite");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new UpstreamFormatException("CAP " + element + " '" + text + "' is not a number", text, e);
        }
    }

    /** One element of the CAP namespace, with its text and child elements. */
    private static final class Node {
        final String ns;
        final String local;
        final StringBuilder text = new StringBuilder();
        final List<Node> children = new ArrayList<>();

        Node(String ns, String local) {
            this.ns = ns;
            this.local = local;
        }

        List<Node> children(String name) {
            List<Node> out = new ArrayList<>();
            for (Node child : children) {
                if (name.equals(child.local)) {
                    out.add(child);
                }
            }
            return out;
        }

        /** The stripped text of the first child {@code name}; null when absent or blank. */
        String text(String name) {
            for (Node child : children) {
                if (name.equals(child.local)) {
                    String value = child.text.toString().strip();
                    return value.isEmpty() ? null : value;
                }
            }
            return null;
        }

        /** The stripped, non-blank texts of every child {@code name}. */
        List<String> texts(String name) {
            List<String> out = new ArrayList<>();
            for (Node child : children) {
                if (name.equals(child.local)) {
                    String value = child.text.toString().strip();
                    if (!value.isEmpty()) {
                        out.add(value);
                    }
                }
            }
            return out;
        }
    }

    /** Fails a read once more than {@code max} bytes have been consumed. */
    private static final class CappedInputStream extends FilterInputStream {
        private final long max;
        private long count;
        boolean exceeded;

        CappedInputStream(InputStream in, long max) {
            super(in);
            this.max = max;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                count(1);
            }
            return b;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int n = super.read(buffer, offset, length);
            if (n > 0) {
                count(n);
            }
            return n;
        }

        private void count(long n) throws IOException {
            count += n;
            if (count > max) {
                exceeded = true;
                throw new IOException("CAP document exceeds " + max + " bytes");
            }
        }

        @Override
        public void close() {
            // The caller owns the underlying stream.
        }
    }
}
