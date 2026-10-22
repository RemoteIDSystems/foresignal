package ai.foresignal.collect;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;

/**
 * Sets the JDK's XML entity-size limits on a parser factory to {@link #ENTITY_LIMIT_BYTES}.
 *
 * <p>JDK 25's {@code jaxp.properties} sets {@code jdk.xml.maxGeneralEntitySizeLimit} and
 * {@code jdk.xml.totalEntitySizeLimit} to 100,000; JDK 21 left the first unlimited and the second at 5e7. On the
 * JDK 25 runtime the CISA advisories feed (1.36 MB of escaped HTML) failed from its first run with
 * {@code JAXP00010003: The length of entity "[xml]" is "100,001"} (2026-10-22). Every factory here already turns
 * DTDs and external entities off and reads a size-capped body, so no entity can be declared and the limit only
 * counts predefined references such as {@code &amp;amp;}.
 */
public final class XmlLimits {

    /** The entity-size limit applied, bytes: above every response cap in the project. */
    public static final int ENTITY_LIMIT_BYTES = 64 * 1024 * 1024;

    static final String MAX_GENERAL_ENTITY = "jdk.xml.maxGeneralEntitySizeLimit";
    static final String TOTAL_ENTITY = "jdk.xml.totalEntitySizeLimit";

    private XmlLimits() {}

    /** Applies the limits to a StAX factory; an implementation that does not know them is left as it is. */
    public static XMLInputFactory apply(XMLInputFactory factory) {
        try {
            factory.setProperty(MAX_GENERAL_ENTITY, ENTITY_LIMIT_BYTES);
            factory.setProperty(TOTAL_ENTITY, ENTITY_LIMIT_BYTES);
        } catch (IllegalArgumentException ignored) {
            // Not the JDK's implementation; it has no such limits to raise.
        }
        return factory;
    }

    /** Applies the limits to a DOM factory; an implementation that does not know them is left as it is. */
    public static DocumentBuilderFactory apply(DocumentBuilderFactory factory) {
        try {
            factory.setAttribute(MAX_GENERAL_ENTITY, ENTITY_LIMIT_BYTES);
            factory.setAttribute(TOTAL_ENTITY, ENTITY_LIMIT_BYTES);
        } catch (IllegalArgumentException ignored) {
            // Not the JDK's implementation; it has no such limits to raise.
        }
        return factory;
    }
}
