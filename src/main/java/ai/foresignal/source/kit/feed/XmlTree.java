package ai.foresignal.source.kit.feed;

import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A small element tree read through a hardened StAX factory (prompt 073), so the feed formats can be interpreted
 * without a DOM parser.
 *
 * <p>The factory is {@link CapParser#secureInputFactory()}: no DTDs, no external entities, no entity expansion,
 * coalescing on. A document with a {@code DOCTYPE} or an entity reference is refused outright, so an XXE payload is
 * never resolved. The tree is capped at {@value #MAX_DEPTH} levels and {@value #MAX_NODES} elements.
 */
final class XmlTree {

    /** The deepest element nesting read. */
    static final int MAX_DEPTH = 64;

    /** The most elements read. */
    static final int MAX_NODES = 200_000;

    /** The XML namespace (for {@code xml:lang} and {@code xml:base}). */
    static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

    private XmlTree() {}

    /**
     * One element.
     *
     * @param ns its namespace URI, or empty
     * @param local its local name
     * @param attributes attribute local name to value; {@code xml:lang} and {@code xml:base} keep their prefix
     * @param text its own character content
     * @param children its child elements
     * @param content its character runs ({@link String}) and child elements ({@link Node}) in document order, for
     *     mixed content such as Atom {@code xhtml}
     */
    record Node(String ns, String local, Map<String, String> attributes, StringBuilder text, List<Node> children,
            List<Object> content) {

        /** True for {@code ns:local}. */
        boolean is(String namespace, String name) {
            return local.equals(name) && ns.equals(namespace);
        }

        /** The first child {@code ns:local}, or null. */
        Node child(String namespace, String name) {
            for (Node c : children) {
                if (c.is(namespace, name)) {
                    return c;
                }
            }
            return null;
        }

        /** Every child {@code ns:local}. */
        List<Node> all(String namespace, String name) {
            List<Node> out = new ArrayList<>();
            for (Node c : children) {
                if (c.is(namespace, name)) {
                    out.add(c);
                }
            }
            return out;
        }

        /** The trimmed text of the first child {@code ns:local}, or null when it is missing or blank. */
        String childText(String namespace, String name) {
            Node c = child(namespace, name);
            if (c == null) {
                return null;
            }
            String t = c.text().toString().strip();
            return t.isEmpty() ? null : t;
        }

        /** An attribute, or null. */
        String attr(String name) {
            String v = attributes.get(name);
            return v == null || v.isBlank() ? null : v.strip();
        }

        /**
         * The content of this element as markup (element names and text only, attributes dropped), for mixed content
         * such as Atom {@code xhtml} that is then reduced to plain text by {@link FeedText#plain}.
         */
        String innerMarkup() {
            StringBuilder out = new StringBuilder();
            markup(this, out);
            return out.toString();
        }

        private static void markup(Node node, StringBuilder out) {
            for (Object part : node.content()) {
                if (part instanceof Node child) {
                    out.append('<').append(child.local()).append('>');
                    markup(child, out);
                    out.append("</").append(child.local()).append('>');
                } else {
                    out.append(part.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
                }
            }
        }
    }

    /**
     * Reads a document.
     *
     * @param xml the bytes
     * @return the root element
     * @throws UpstreamFormatException when it is not well-formed, has a DOCTYPE or entity reference, or breaks a cap
     */
    static Node read(byte[] xml) {
        XMLStreamReader reader = null;
        try {
            reader = CapParser.secureInputFactory().createXMLStreamReader(new ByteArrayInputStream(xml));
            List<Node> stack = new ArrayList<>();
            Node root = null;
            int nodes = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.DTD, XMLStreamConstants.ENTITY_REFERENCE ->
                            throw new UpstreamFormatException("DOCTYPE declarations and entity references are not"
                                    + " allowed in a feed", "");
                    case XMLStreamConstants.START_ELEMENT -> {
                        if (++nodes > MAX_NODES) {
                            throw new UpstreamFormatException("The feed has more than " + MAX_NODES + " elements", "");
                        }
                        if (stack.size() >= MAX_DEPTH) {
                            throw new UpstreamFormatException("The feed nests deeper than " + MAX_DEPTH, "");
                        }
                        Map<String, String> attributes = new LinkedHashMap<>();
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            String ns = reader.getAttributeNamespace(i);
                            String name = reader.getAttributeLocalName(i);
                            attributes.put(XML_NS.equals(ns) ? "xml:" + name : name, reader.getAttributeValue(i));
                        }
                        String ns = reader.getNamespaceURI();
                        Node node = new Node(ns == null ? "" : ns, reader.getLocalName(), attributes,
                                new StringBuilder(), new ArrayList<>(), new ArrayList<>());
                        if (stack.isEmpty()) {
                            root = node;
                        } else {
                            stack.get(stack.size() - 1).children().add(node);
                            stack.get(stack.size() - 1).content().add(node);
                        }
                        stack.add(node);
                    }
                    case XMLStreamConstants.END_ELEMENT -> stack.remove(stack.size() - 1);
                    case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA, XMLStreamConstants.SPACE -> {
                        if (!stack.isEmpty()) {
                            String text = reader.getText();
                            stack.get(stack.size() - 1).text().append(text);
                            stack.get(stack.size() - 1).content().add(text);
                        }
                    }
                    default -> {
                        // comments and processing instructions are ignored
                    }
                }
            }
            if (root == null) {
                throw new UpstreamFormatException("The feed has no root element", "");
            }
            return root;
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException("The feed is not well-formed XML: " + e.getMessage(), "", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                    // nothing to do
                }
            }
        }
    }
}
