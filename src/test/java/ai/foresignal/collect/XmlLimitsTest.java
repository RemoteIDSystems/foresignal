package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.formats.cap.CapParser;
import java.io.StringReader;
import java.util.function.Supplier;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link XmlLimits} under JDK 25's default limits. The build and tests run on JDK 21, which has no general-entity
 * limit, so each test sets JDK 25's {@code jaxp.properties} values as system properties for its duration.
 */
class XmlLimitsTest {

    /** rss, channel and title, then item, title, link, dc:creator and description for each of 400 items. */
    private static final int ELEMENTS = 3 + 400 * 5;

    private String previousGeneral;
    private String previousTotal;

    @BeforeEach
    void jdk25Limits() {
        previousGeneral = System.setProperty(XmlLimits.MAX_GENERAL_ENTITY, "100000");
        previousTotal = System.setProperty(XmlLimits.TOTAL_ENTITY, "100000");
    }

    @AfterEach
    void restore() {
        restore(XmlLimits.MAX_GENERAL_ENTITY, previousGeneral);
        restore(XmlLimits.TOTAL_ENTITY, previousTotal);
    }

    private static void restore(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    /** An RSS document shaped like CISA's: about 1.2 MB of items whose descriptions are escaped HTML. */
    static String rss() {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<rss version=\"2.0\""
                + " xmlns:dc=\"http://purl.org/dc/elements/1.1/\"><channel><title>Advisories</title>");
        for (int i = 0; i < 400; i++) {
            sb.append("<item><title>Advisory ").append(i).append("</title><link>https://example.org/a?x=1&amp;y=")
                    .append(i).append("</link><dc:creator>CISA</dc:creator><description>");
            for (int j = 0; j < 40; j++) {
                sb.append("&lt;p&gt;Vendor &amp;amp; product, see &lt;a href=&quot;https://example.org/&quot;&gt;"
                        + "notice&lt;/a&gt;.&lt;/p&gt;");
            }
            sb.append("</description></item>");
        }
        return sb.append("</channel></rss>").toString();
    }

    private static int elements(XMLInputFactory factory) throws XMLStreamException {
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(rss()));
        int count = 0;
        while (reader.hasNext()) {
            if (reader.next() == XMLStreamReader.START_ELEMENT) {
                count++;
            }
        }
        reader.close();
        return count;
    }

    private static XMLInputFactory unlimited(Supplier<XMLInputFactory> base) {
        return XmlLimits.apply(base.get());
    }

    @Test
    @DisplayName("without the limits applied, JDK 25's defaults refuse the feed (the production failure)")
    void defaultsRefuse() {
        assertThat(rss().length()).isGreaterThan(1_000_000);
        XMLInputFactory plain = XMLInputFactory.newDefaultFactory();
        plain.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        plain.setProperty(XMLInputFactory.IS_COALESCING, true);
        assertThatThrownBy(() -> elements(plain)).hasMessageContaining("JAXP00010003");
    }

    @Test
    @DisplayName("the shared feed and CAP factory reads the feed")
    void capFactoryReads() throws Exception {
        assertThat(elements(CapParser.secureInputFactory())).isEqualTo(ELEMENTS);
    }

    @Test
    @DisplayName("a factory with the limits applied reads the feed")
    void applied() throws Exception {
        assertThat(elements(unlimited(XMLInputFactory::newDefaultFactory))).isEqualTo(ELEMENTS);
    }
}
