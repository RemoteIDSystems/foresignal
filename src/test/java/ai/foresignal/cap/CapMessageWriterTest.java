package ai.foresignal.cap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.StringReader;
import java.net.URL;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.PrecisionModel;

class CapMessageWriterTest {

    static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);
    static final Instant SENT = Instant.parse("2026-10-16T12:34:56.789Z");
    static Schema schema;

    @BeforeAll
    static void loadSchema() throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL xsd = CapMessageWriterTest.class.getResource("/cap/CAP-v1.2.xsd");
        schema = factory.newSchema(xsd);
    }

    static void validate(String xml) throws Exception {
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new StringReader(xml)));
    }

    static CapMessage.Info info(boolean allClear, CapMessage.Area area) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("foresignalRuleId", "6f1c3c1e-8a4f-4bb4-9d55-1f3b5f0c1a2b");
        parameters.put("foresignalSource", "usgs-earthquakes");
        parameters.put("foresignalExternalId", "us7000n7tc");
        parameters.put("foresignalSeq", "42");
        parameters.put("foresignalGrade", "B3");
        return new CapMessage.Info("en-US", "Geo", "Big quakes near Austin", allClear, "Unknown", "Severe", "Observed",
                SENT, SENT.plusSeconds(3600), "Foresignal (bench)", "M5.4 – 10 km N of Austin & <co>",
                "A magnitude 5.4 earthquake.\nSecond line.", "foresignal://alerts/7", parameters, area);
    }

    static CapMessage.Area circle() {
        return CapAreas.of(GF.createPoint(new Coordinate(-97.74, 30.27)), null, 5000.0);
    }

    @Test
    @DisplayName("Alert, Update and Update/AllClear validate against the CAP 1.2 XSD")
    void validates() throws Exception {
        CapMessage alert = new CapMessage("abcd1234-7-1", "foresignal@abcd1234", SENT, CapMessage.Status.Actual,
                CapMessage.MsgType.Alert, List.of(), info(false, circle()));
        String first = CapMessageWriter.write(alert);
        validate(first);
        String ref = CapMessageWriter.reference("foresignal@abcd1234", "abcd1234-7-1", SENT);
        CapMessage update = new CapMessage("abcd1234-7-2", "foresignal@abcd1234", SENT.plusSeconds(60),
                CapMessage.Status.Actual, CapMessage.MsgType.Update, List.of(ref), info(false, circle()));
        validate(CapMessageWriter.write(update));
        CapMessage clear = new CapMessage("abcd1234-7-3", "foresignal@abcd1234", SENT.plusSeconds(120),
                CapMessage.Status.Actual, CapMessage.MsgType.Update, List.of(ref, ref.replace("-1,", "-2,")),
                info(true, circle()));
        String xml = CapMessageWriter.write(clear);
        validate(xml);
        assertThat(xml).contains("<responseType>AllClear</responseType>").contains("<msgType>Update</msgType>")
                .doesNotContain("Cancel");
        assertThat(first).contains("<msgType>Alert</msgType>").doesNotContain("<references>");
    }

    @Test
    @DisplayName("scope is always Public, the not-official note is present, times use +00:00 without fractions")
    void scopeNoteTimes() {
        String xml = CapMessageWriter.write(new CapMessage("x-1-1", "s", SENT, CapMessage.Status.Actual,
                CapMessage.MsgType.Alert, List.of(), info(false, null)));
        assertThat(xml).contains("<scope>Public</scope>")
                .contains("<note>" + CapMessage.NOTE + "</note>")
                .contains("<sent>2026-10-16T12:34:56+00:00</sent>")
                .contains("<expires>2026-10-16T13:34:56+00:00</expires>")
                .contains("xmlns=\"urn:oasis:names:tc:emergency:cap:1.2\"")
                .contains("&amp; &lt;co&gt;");
        assertThat(CapMessage.NOTE).contains("Not an official warning");
    }

    @Test
    @DisplayName("references list each earlier message as sender,identifier,sent separated by spaces")
    void references() {
        String a = CapMessageWriter.reference("s", "i-1-1", SENT);
        assertThat(a).isEqualTo("s,i-1-1,2026-10-16T12:34:56+00:00");
        String xml = CapMessageWriter.write(new CapMessage("i-1-3", "s", SENT, CapMessage.Status.Actual,
                CapMessage.MsgType.Update, List.of(a, "s,i-1-2,2026-10-16T12:40:00+00:00"), info(false, null)));
        assertThat(xml).contains("<references>s,i-1-1,2026-10-16T12:34:56+00:00 s,i-1-2,2026-10-16T12:40:00+00:00</references>");
    }

    @Test
    @DisplayName("Exercise and Test statuses validate")
    void statuses() throws Exception {
        for (CapMessage.Status status : CapMessage.Status.values()) {
            String xml = CapMessageWriter.write(new CapMessage("x-1-1", "s", SENT, status, CapMessage.MsgType.Alert,
                    List.of(), info(false, circle())));
            validate(xml);
            assertThat(xml).contains("<status>" + status.name() + "</status>");
        }
    }

    @Test
    @DisplayName("a point record is a circle lat,lon radiusKm; the radius is the rule buffer or 1 km")
    void circles() {
        CapMessage.Area area = circle();
        assertThat(area.circle()).containsExactly(30.27, -97.74, 5.0);
        assertThat(area.areaDesc()).isEqualTo("30.27,-97.74");
        CapMessage.Area plain = CapAreas.of(GF.createPoint(new Coordinate(1, 2)), "Austin", null);
        assertThat(plain.circle()[2]).isEqualTo(CapAreas.DEFAULT_RADIUS_KM);
        assertThat(plain.areaDesc()).isEqualTo("Austin");
        String xml = CapMessageWriter.write(new CapMessage("x-1-1", "s", SENT, CapMessage.Status.Actual,
                CapMessage.MsgType.Alert, List.of(), info(false, area)));
        assertThat(xml).contains("<circle>30.27,-97.74 5</circle>");
    }

    @Test
    @DisplayName("a polygon is a closed lat,lon ring simplified to at most 100 points, and it validates")
    void polygons() throws Exception {
        Coordinate[] ring = new Coordinate[1001];
        for (int i = 0; i < 1000; i++) {
            double a = 2 * Math.PI * i / 1000;
            ring[i] = new Coordinate(-97 + Math.cos(a) * (1 + 0.01 * Math.sin(17 * a)), 30 + Math.sin(a));
        }
        ring[1000] = ring[0];
        LinearRing shell = GF.createLinearRing(ring);
        Geometry polygon = GF.createPolygon(shell);
        CapMessage.Area area = CapAreas.of(polygon, null, null);
        assertThat(area.polygon()).hasSizeLessThanOrEqualTo(CapAreas.MAX_POINTS).hasSizeGreaterThanOrEqualTo(4);
        assertThat(area.polygon().getFirst()).containsExactly(area.polygon().getLast());
        assertThat(area.polygon().getFirst()[0]).isBetween(29.0, 31.0);
        assertThat(area.polygon().getFirst()[1]).isBetween(-98.5, -95.5);
        String xml = CapMessageWriter.write(new CapMessage("x-1-1", "s", SENT, CapMessage.Status.Actual,
                CapMessage.MsgType.Alert, List.of(), info(false, area)));
        validate(xml);
        assertThat(xml).contains("<polygon>");
    }

    @Test
    @DisplayName("a multipolygon uses its largest part; a line uses its hull; no geometry has no area")
    void otherGeometries() {
        Geometry small = GF.createPolygon(new Coordinate[] {new Coordinate(0, 0), new Coordinate(0.1, 0),
            new Coordinate(0.1, 0.1), new Coordinate(0, 0)});
        Geometry big = GF.createPolygon(new Coordinate[] {new Coordinate(10, 10), new Coordinate(12, 10),
            new Coordinate(12, 12), new Coordinate(10, 12), new Coordinate(10, 10)});
        CapMessage.Area multi = CapAreas.of(GF.createMultiPolygon(new org.locationtech.jts.geom.Polygon[] {
            (org.locationtech.jts.geom.Polygon) small, (org.locationtech.jts.geom.Polygon) big}), null, null);
        assertThat(multi.polygon()).allSatisfy(p -> assertThat(p[0]).isBetween(10.0, 12.0));
        CapMessage.Area line = CapAreas.of(GF.createLineString(new Coordinate[] {new Coordinate(0, 0),
            new Coordinate(1, 0), new Coordinate(1, 1)}), null, null);
        assertThat(line.polygon()).isNotNull();
        CapMessage.Area twoPoints = CapAreas.of(GF.createLineString(new Coordinate[] {new Coordinate(0, 0),
            new Coordinate(1, 0)}), null, null);
        assertThat(twoPoints.circle()).isNotNull();
        assertThat(CapAreas.of(null, null, null)).isNull();
    }

    @Test
    @DisplayName("the category table maps kinds as the prompt lists, else Other")
    void categories() {
        CapCategories c = CapCategories.load();
        assertThat(c.of("earthquake")).isEqualTo("Geo");
        assertThat(c.of("tsunami-warning")).isEqualTo("Geo");
        assertThat(c.of("nws-alert")).isEqualTo("Met");
        assertThat(c.of("weather-station")).isEqualTo("Met");
        assertThat(c.of("fire-hotspot")).isEqualTo("Fire");
        assertThat(c.of("power-outage")).isEqualTo("Infra");
        assertThat(c.of("aircraft")).isEqualTo("Transport");
        assertThat(c.of("vessel")).isEqualTo("Transport");
        assertThat(c.of("cyber-vulnerability")).isEqualTo("Security");
        assertThat(c.of("conflict-event")).isEqualTo("Security");
        assertThat(c.of("health-alert")).isEqualTo("Health");
        assertThat(c.of("launch")).isEqualTo("Other");
        assertThat(c.of(null)).isEqualTo("Other");
    }

    @Test
    @DisplayName("severity maps critical, warning and info to Severe, Moderate and Minor")
    void severities() {
        assertThat(CapService.severity("critical")).isEqualTo("Severe");
        assertThat(CapService.severity("warning")).isEqualTo("Moderate");
        assertThat(CapService.severity("info")).isEqualTo("Minor");
        assertThat(CapService.severity(null)).isEqualTo("Unknown");
    }

    @Test
    @DisplayName("coordinates are written with at most six decimals and no exponent")
    void coordinates() {
        assertThat(CapMessageWriter.coordinate(1e-7)).isEqualTo("0");
        assertThat(CapMessageWriter.coordinate(30.123456789)).isEqualTo("30.123457");
        assertThat(CapMessageWriter.coordinate(100)).isEqualTo("100");
        assertThatCode(() -> CapMessageWriter.time(Instant.EPOCH)).doesNotThrowAnyException();
    }
}
