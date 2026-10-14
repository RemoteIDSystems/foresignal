package ai.foresignal.source.adapters.faa;

import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A small element tree read with the hardened StAX factory ({@link CapParser#secureInputFactory()}: no DTD support,
 * no external entities, no entity expansion), shared by the XNOTAM and NAS status parsers (prompt 087). A document
 * that declares a DOCTYPE or contains an entity reference is refused, as is one over the byte, element or depth
 * limits. Namespaces are ignored (local names only); attributes are kept.
 */
final class FaaXml {

    /** The largest document read. */
    static final int MAX_BYTES = 8 * 1024 * 1024;

    /** The most elements read. */
    static final int MAX_ELEMENTS = 400_000;

    /** The deepest nesting read. */
    static final int MAX_DEPTH = 64;

    private FaaXml() {}

    /**
     * One element.
     *
     * @param name the local name
     * @param attributes local attribute names to values, in order
     * @param children the child elements, in order
     */
    record Node(String name, java.util.Map<String, String> attributes, List<Node> children, StringBuilder textBuffer) {

        /** The element's own text, trimmed; empty when it has none. */
        String text() {
            return textBuffer.toString().strip();
        }

        /** The first child named {@code name}, or null. */
        Node child(String name) {
            for (Node c : children) {
                if (c.name.equals(name)) {
                    return c;
                }
            }
            return null;
        }

        /** Every child named {@code name}, in order. */
        List<Node> children(String name) {
            List<Node> out = new ArrayList<>();
            for (Node c : children) {
                if (c.name.equals(name)) {
                    out.add(c);
                }
            }
            return out;
        }

        /** Every descendant named {@code name}, depth first, in document order. */
        List<Node> descendants(String name) {
            List<Node> out = new ArrayList<>();
            collect(this, name, out);
            return out;
        }

        private static void collect(Node node, String name, List<Node> out) {
            for (Node c : node.children) {
                if (c.name.equals(name)) {
                    out.add(c);
                }
                collect(c, name, out);
            }
        }

        /** The trimmed text of the first child path ({@code "NotUid", "dateIssued"}), or null when absent or blank. */
        String text(String... path) {
            Node node = this;
            for (String step : path) {
                node = node.child(step);
                if (node == null) {
                    return null;
                }
            }
            String text = node.text();
            return text.isEmpty() ? null : text;
        }
    }

    /**
     * Reads a document.
     *
     * @param bytes the document
     * @param what what is read, for messages
     * @return the root element
     * @throws UpstreamFormatException when it is empty, too large, malformed or declares a DOCTYPE
     */
    static Node read(byte[] bytes, String what) {
        if (bytes == null || bytes.length == 0) {
            throw new UpstreamFormatException(what + " is empty", "");
        }
        if (bytes.length > MAX_BYTES) {
            throw new UpstreamFormatException(what + " is larger than " + MAX_BYTES + " bytes", "");
        }
        XMLStreamReader reader = null;
        try {
            reader = CapParser.secureInputFactory().createXMLStreamReader(new ByteArrayInputStream(bytes));
            Node root = null;
            List<Node> stack = new ArrayList<>();
            int elements = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.DTD, XMLStreamConstants.ENTITY_REFERENCE ->
                            throw new UpstreamFormatException(what + ": DOCTYPE declarations and entity references"
                                    + " are not allowed", "");
                    case XMLStreamConstants.START_ELEMENT -> {
                        if (++elements > MAX_ELEMENTS) {
                            throw new UpstreamFormatException(what + " has more than " + MAX_ELEMENTS + " elements",
                                    "");
                        }
                        if (stack.size() >= MAX_DEPTH) {
                            throw new UpstreamFormatException(what + " is nested too deeply", "");
                        }
                        java.util.Map<String, String> attributes = new java.util.LinkedHashMap<>();
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            attributes.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                        }
                        Node node = new Node(reader.getLocalName(), attributes, new ArrayList<>(), new StringBuilder());
                        if (root == null) {
                            root = node;
                        } else {
                            stack.get(stack.size() - 1).children().add(node);
                        }
                        stack.add(node);
                    }
                    case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA, XMLStreamConstants.SPACE -> {
                        if (!stack.isEmpty()) {
                            stack.get(stack.size() - 1).textBuffer().append(reader.getText());
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT -> stack.remove(stack.size() - 1);
                    default -> {
                        // Comments and processing instructions carry nothing.
                    }
                }
            }
            if (root == null) {
                throw new UpstreamFormatException(what + " has no root element", "");
            }
            return root;
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException(what + " is not well-formed XML: " + e.getMessage(), "", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                    // Nothing to release beyond the byte array.
                }
            }
        }
    }
}
