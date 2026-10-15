package ai.foresignal.source.adapters.sanctions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Secure StAX for the sanctions lists (prompt 091): DTDs and external entities are refused, so a
 * DOCTYPE/XXE document throws.
 */
public final class Xml {

    private Xml() {}

    /** A hardened factory: no DTD, no external entities, coalescing on. */
    public static XMLInputFactory secureInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newDefaultFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        // Replace predefined character/entity references (SEC's escaped-HTML summaries rely on this);
        // custom entities cannot be defined with SUPPORT_DTD=false, so this stays XXE-safe.
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        try {
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        } catch (IllegalArgumentException ignored) {
            // not every implementation knows the JAXP 1.5 property; the resolver refuses anyway
        }
        factory.setXMLResolver((publicId, systemId, baseUri, namespace) -> {
            throw new XMLStreamException("External entities are not allowed");
        });
        return factory;
    }

    /** A reader over {@code file}. */
    public static XMLStreamReader open(Path file) throws IOException, XMLStreamException {
        return secureInputFactory().createXMLStreamReader(Files.newInputStream(file));
    }

    /** A reader over {@code in} (tests). */
    public static XMLStreamReader open(InputStream in) throws XMLStreamException {
        return secureInputFactory().createXMLStreamReader(in);
    }

    /** Reads the parsed bytes of a fixture. */
    public static XMLStreamReader openBytes(byte[] bytes) throws XMLStreamException {
        return secureInputFactory().createXMLStreamReader(new java.io.ByteArrayInputStream(bytes));
    }

    /** Writes {@code bytes} to a temporary file for a disk-backed parse. */
    public static Path temporary(byte[] bytes) throws IOException {
        Path file = Files.createTempFile("sanctions-", ".xml");
        Files.write(file, bytes);
        return file;
    }

    /** True when the reader's current element's local name is {@code name}. */
    public static boolean at(XMLStreamReader reader, String name) {
        return reader.isStartElement() && name.equals(reader.getLocalName());
    }

    /**
     * Reads the element the reader is positioned on into a small tree (prompt 091). The reader is advanced
     * through the element's end tag. Entries in sanctioned-list documents are small, so a bounded tree per
     * entry is safe; the stream itself is never materialised whole.
     *
     * @throws XMLStreamException when the document is malformed or carries a DTD
     */
    public static Node readElement(XMLStreamReader reader) throws XMLStreamException {
        String name = reader.getLocalName();
        java.util.Map<String, String> attributes = new java.util.LinkedHashMap<>();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            attributes.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
        }
        StringBuilder text = new StringBuilder();
        java.util.List<Node> children = new java.util.ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                children.add(readElement(reader));
            } else if (event == XMLStreamReader.CHARACTERS || event == XMLStreamReader.CDATA) {
                text.append(reader.getText());
            } else if (event == XMLStreamReader.END_ELEMENT) {
                break;
            }
        }
        String value = text.toString().strip();
        return new Node(name, value.isEmpty() ? null : value, children, attributes);
    }

    /** A small parsed element: its local name, its own text, its attributes and its child elements. */
    public static final class Node {

        private final String name;
        private final String text;
        private final java.util.List<Node> children;
        private final java.util.Map<String, String> attributes;

        Node(String name, String text, java.util.List<Node> children, java.util.Map<String, String> attributes) {
            this.name = name;
            this.text = text;
            this.children = children;
            this.attributes = attributes;
        }

        /** The local name. */
        public String name() {
            return name;
        }

        /** An attribute value, or null. */
        public String attribute(String name) {
            return attributes.get(name);
        }

        /** This element's own text, or null. */
        public String text() {
            return text;
        }

        /** This element's children, in document order. */
        public java.util.List<Node> children() {
            return children;
        }

        /** Every descendant leaf (an element with no children) named {@code name}, in document order. */
        public java.util.List<String> leafTexts(String name) {
            java.util.List<String> out = new java.util.ArrayList<>();
            collect(name, out);
            return out;
        }

        private void collect(String name, java.util.List<String> out) {
            if (this.name.equals(name) && this.text != null) {
                out.add(this.text);
            }
            for (Node child : children) {
                child.collect(name, out);
            }
        }

        /** Every descendant named {@code name}, depth-first. */
        public java.util.List<Node> descendants(String name) {
            java.util.List<Node> out = new java.util.ArrayList<>();
            gather(name, out);
            return out;
        }

        private void gather(String name, java.util.List<Node> out) {
            if (this.name.equals(name)) {
                out.add(this);
            }
            for (Node child : children) {
                child.gather(name, out);
            }
        }

        /** The first child named {@code name}, or null. */
        public Node first(String name) {
            for (Node child : children) {
                if (child.name.equals(name)) {
                    return child;
                }
            }
            return null;
        }

        /** The text of the first child named {@code name}, or null. */
        public String childText(String name) {
            Node child = first(name);
            return child == null ? null : child.text;
        }

        /** Every child named {@code name}, in document order. */
        public java.util.List<Node> all(String name) {
            java.util.List<Node> out = new java.util.ArrayList<>();
            for (Node child : children) {
                if (child.name.equals(name)) {
                    out.add(child);
                }
            }
            return out;
        }

        /** The text following a chain of child names, or null. */
        public String path(String... names) {
            Node node = this;
            for (String next : names) {
                node = node.first(next);
                if (node == null) {
                    return null;
                }
            }
            return node.text;
        }
    }
}
