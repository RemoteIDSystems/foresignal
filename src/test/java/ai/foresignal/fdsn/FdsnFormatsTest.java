package ai.foresignal.fdsn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * The FDSN event pieces without a database (prompt 141): parameter parsing (long and short names, defaults, errors),
 * the exact filters and orderings, the text format, and QuakeML validated against the official BED schema.
 */
class FdsnFormatsTest {

    static final Instant T = Instant.parse("2026-03-01T12:00:00Z");

    static FdsnEvent event(String id, Instant time, double lat, double lon, Double mag) {
        return new FdsnEvent("usgs-earthquakes", id, time, lat, lon, 10.5, mag, "mb", "Near | Somewhere\nelse", "us",
                "us", "earthquake", time.plusSeconds(60));
    }

    static Map<String, String[]> params(String... pairs) {
        Map<String, String[]> out = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            out.put(pairs[i], new String[] {pairs[i + 1]});
        }
        return out;
    }

    /** Validates the eventParameters element of {@code xml} against the BED XSD and returns the document. */
    static Document validate(String xml) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(FdsnFormatsTest.class.getResource("/fdsn/QuakeML-BED-1.2.xsd"));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        Element root = doc.getDocumentElement();
        assertThat(root.getNamespaceURI()).isEqualTo(QuakeMlWriter.QUAKEML_NS);
        assertThat(root.getLocalName()).isEqualTo("quakeml");
        Element parameters = (Element) root.getElementsByTagNameNS(QuakeMlWriter.BED_NS, "eventParameters").item(0);
        assertThat(parameters).isNotNull();
        schema.newValidator().validate(new DOMSource(parameters));
        return doc;
    }

    @Test
    @DisplayName("defaults: xml, time order, the whole globe, limit 20,000, nodata 204, no circle")
    void defaults() {
        FdsnEventQuery q = FdsnEventQuery.parse(Map.of());
        assertThat(q.format()).isEqualTo("xml");
        assertThat(q.orderBy()).isEqualTo(FdsnEventQuery.OrderBy.TIME);
        assertThat(q.limit()).isEqualTo(FdsnEventQuery.MAX_LIMIT);
        assertThat(q.noData()).isEqualTo(204);
        assertThat(q.hasBox()).isFalse();
        assertThat(q.hasCircle()).isFalse();
        assertThat(q.start()).isNull();
    }

    @Test
    @DisplayName("long and short names, case-insensitive; date-only and zone-less times are UTC; key passes through")
    void names() {
        FdsnEventQuery q = FdsnEventQuery.parse(params("START", "2026-03-01", "endtime", "2026-03-02T06:30:00",
                "minlat", "10", "maxlatitude", "20", "minlon", "-30", "maxlon", "40", "minmag", "4.5",
                "maxmagnitude", "7", "orderby", "magnitude-asc", "limit", "10", "format", "TEXT", "nodata", "404",
                "key", "fsf_feedkey"));
        assertThat(q.start()).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
        assertThat(q.end()).isEqualTo(Instant.parse("2026-03-02T06:30:00Z"));
        assertThat(q.minLatitude()).isEqualTo(10);
        assertThat(q.maxLongitude()).isEqualTo(40);
        assertThat(q.minMagnitude()).isEqualTo(4.5);
        assertThat(q.orderBy()).isEqualTo(FdsnEventQuery.OrderBy.MAGNITUDE_ASC);
        assertThat(q.limit()).isEqualTo(10);
        assertThat(q.format()).isEqualTo("text");
        assertThat(q.noData()).isEqualTo(404);
        assertThat(q.hasBox()).isTrue();
        assertThat(FdsnEventQuery.parse(params("starttime", "2026-03-01T00:00:00+02:00")).start())
                .isEqualTo(Instant.parse("2026-02-28T22:00:00Z"));
    }

    @Test
    @DisplayName("a radius alone centers on 0,0; latitude alone defaults longitude to 0")
    void circleDefaults() {
        FdsnEventQuery radius = FdsnEventQuery.parse(params("maxradius", "5"));
        assertThat(radius.hasCircle()).isTrue();
        assertThat(radius.latitude()).isEqualTo(0.0);
        assertThat(radius.longitude()).isEqualTo(0.0);
        FdsnEventQuery lat = FdsnEventQuery.parse(params("lat", "35"));
        assertThat(lat.longitude()).isEqualTo(0.0);
        assertThat(lat.maxRadius()).isEqualTo(180.0);
    }

    @Test
    @DisplayName("bad values, unsupported or repeated parameters and inverted ranges are FDSN 400s")
    void errors() {
        List<Map<String, String[]>> bad = new ArrayList<>(List.of(
                params("eventid", "us1"), params("includeallorigins", "true"), params("starttime", "yesterday"),
                params("minlatitude", "91"), params("maxlongitude", "x"), params("limit", "0"),
                params("limit", "20001"), params("format", "json"), params("orderby", "depth"),
                params("nodata", "200"), params("starttime", "2026-03-02", "endtime", "2026-03-01"),
                params("minlat", "20", "maxlat", "10"), params("minradius", "5", "maxradius", "1"),
                params("minmag", "6", "maxmag", "5"), params("minmag", "")));
        bad.add(Map.of("minmag", new String[] {"1", "2"}));
        for (Map<String, String[]> p : bad) {
            assertThatThrownBy(() -> FdsnEventQuery.parse(p)).as(p.keySet().toString())
                    .isInstanceOf(FdsnRequestException.class);
        }
    }

    @Test
    @DisplayName("exact filters: time bounds inclusive, antimeridian rectangles, degree radius, magnitudes")
    void matches() {
        FdsnEvent e = event("a", T, 10, 179.5, 5.0);
        assertThat(FdsnEventService.matches(e, FdsnEventQuery.parse(params("starttime", T.toString(), "endtime",
                T.toString())))).isTrue();
        assertThat(FdsnEventService.matches(e, FdsnEventQuery.parse(params("starttime", T.plusSeconds(1).toString()))))
                .isFalse();
        assertThat(FdsnEventService.matches(e, FdsnEventQuery.parse(params("minlon", "170", "maxlon", "-170"))))
                .as("antimeridian").isTrue();
        assertThat(FdsnEventService.matches(e, FdsnEventQuery.parse(params("minlon", "-170", "maxlon", "170"))))
                .isFalse();
        assertThat(FdsnEventService.matches(e, FdsnEventQuery.parse(params("lat", "10", "lon", "179", "maxradius",
                "1")))).isTrue();
        assertThat(FdsnEventService.matches(e, FdsnEventQuery.parse(params("lat", "10", "lon", "179", "minradius",
                "1")))).isFalse();
        assertThat(FdsnEventService.matches(e, FdsnEventQuery.parse(params("minmag", "5")))).isTrue();
        assertThat(FdsnEventService.matches(e, FdsnEventQuery.parse(params("maxmag", "4.9")))).isFalse();
        FdsnEvent noMag = event("b", T, 0, 0, null);
        assertThat(FdsnEventService.matches(noMag, FdsnEventQuery.parse(params("minmag", "1")))).isFalse();
        assertThat(FdsnEventService.matches(noMag, FdsnEventQuery.parse(Map.of()))).isTrue();
        assertThat(FdsnEventService.arcDegrees(0, 0, 0, 90)).isCloseTo(90.0, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    @DisplayName("orderings: time, time-asc, magnitude (missing last), magnitude-asc")
    void orderings() {
        FdsnEvent early = event("early", T, 0, 0, 6.0);
        FdsnEvent late = event("late", T.plusSeconds(60), 0, 0, 4.0);
        FdsnEvent none = event("none", T.plusSeconds(30), 0, 0, null);
        List<FdsnEvent> list = new ArrayList<>(List.of(early, late, none));
        list.sort(FdsnEventService.comparator(FdsnEventQuery.OrderBy.TIME));
        assertThat(list).extracting(FdsnEvent::externalId).containsExactly("late", "none", "early");
        list.sort(FdsnEventService.comparator(FdsnEventQuery.OrderBy.TIME_ASC));
        assertThat(list).extracting(FdsnEvent::externalId).containsExactly("early", "none", "late");
        list.sort(FdsnEventService.comparator(FdsnEventQuery.OrderBy.MAGNITUDE));
        assertThat(list).extracting(FdsnEvent::externalId).containsExactly("early", "late", "none");
        list.sort(FdsnEventService.comparator(FdsnEventQuery.OrderBy.MAGNITUDE_ASC));
        assertThat(list).extracting(FdsnEvent::externalId).containsExactly("none", "late", "early");
    }

    @Test
    @DisplayName("text: the 13-column header, UTC times with milliseconds, plain numbers, pipes and newlines cleaned")
    void text() {
        String out = FdsnTextWriter.write(List.of(event("us7000abcd", Instant.parse("2026-03-01T12:00:00.5Z"),
                -8.1449, 120.2734, 5.2)));
        String[] lines = out.split("\n");
        assertThat(lines[0]).isEqualTo(FdsnTextWriter.HEADER);
        assertThat(lines[0].split("\\|")).hasSize(13);
        assertThat(lines[1]).isEqualTo("us7000abcd|2026-03-01T12:00:00.500|-8.1449|120.2734|10.5|us|us"
                + "|usgs-earthquakes|us7000abcd|mb|5.2|us|Near   Somewhere else");
        assertThat(lines[1].split("\\|", -1)).hasSize(13);
        assertThat(FdsnTextWriter.number(10.0)).isEqualTo("10");
        assertThat(FdsnTextWriter.number(1.0e-5)).isEqualTo("0.00001");
        FdsnEvent sparse = new FdsnEvent("s", "x", T, 1, 2, null, null, null, null, null, null, null, null);
        assertThat(FdsnTextWriter.write(List.of(sparse)).split("\n")[1])
                .isEqualTo("x|2026-03-01T12:00:00.000|1|2||||s|x||||");
    }

    @Test
    @DisplayName("QuakeML validates against the BED XSD; ids are smi:foresignal/{source}/{externalId}; depth in meters")
    void quakeMl() throws Exception {
        FdsnEvent full = event("us7000abcd", T, 35.5, -120.25, 4.7);
        FdsnEvent odd = new FdsnEvent("emsc-seismic", "2026/03:01 #1", T.minusSeconds(3600), -10, 20, null, null,
                null, null, null, null, "quarry blast", null);
        String xml = QuakeMlWriter.write(List.of(full, odd), Instant.parse("2026-03-02T00:00:00Z"));
        Document doc = validate(xml);
        var events = doc.getElementsByTagNameNS(QuakeMlWriter.BED_NS, "event");
        assertThat(events.getLength()).isEqualTo(2);
        Element first = (Element) events.item(0);
        assertThat(first.getAttribute("publicID")).isEqualTo("smi:foresignal/usgs-earthquakes/us7000abcd");
        assertThat(xml).contains("<preferredOriginID>smi:foresignal/usgs-earthquakes/us7000abcd/origin</preferredOriginID>")
                .contains("<depth><value>10500</value></depth>")
                .contains("<mag><value>4.7</value></mag>")
                .contains("<type>region name</type>")
                .contains("Near | Somewhere");
        Element second = (Element) events.item(1);
        assertThat(second.getAttribute("publicID")).isEqualTo("smi:foresignal/emsc-seismic/2026~2F03~3A01~20~231");
        assertThat(second.getElementsByTagNameNS(QuakeMlWriter.BED_NS, "magnitude").getLength()).isZero();
        assertThat(QuakeMlWriter.resourceId("")).isEqualTo("~");
        assertThat(QuakeMlWriter.resourceId("=a")).isEqualTo("~3Da");
        assertThat(QuakeMlWriter.resourceId("a=b+c")).isEqualTo("a=b+c");
        assertThat(QuakeMlWriter.resourceId("é")).isEqualTo("~C3~A9");
        // An empty document still validates.
        validate(QuakeMlWriter.write(List.of(), T));
    }
}
