package ai.foresignal.source.adapters.maritime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * {@link NgaMsiParser} (prompt 088) on the real active-list capture trimmed to 30 warnings and on the hand-made
 * texts: the count, the {@code navArea/msgYear/msgNumber} ids, {@code issueDate} parsing, geometry from the text,
 * the {@code cancels} references, the title and the no-coordinate flag.
 */
class NgaMsiParserTest {

    private static NgaMsiParser.Result parse(String file) {
        return NgaMsiParser.parse(MaritimeSources.json(AdapterFixtures.bytes(NgaMsiAdapter.ID, file), file));
    }

    private static Map<String, NormalizedRecord> byId(NgaMsiParser.Result result) {
        return result.records().stream().collect(Collectors.toMap(NormalizedRecord::externalId, r -> r));
    }

    @Test
    @DisplayName("the real capture: 30 warnings, ids navArea/msgYear/msgNumber, issueDate as UTC")
    void realCapture() {
        NgaMsiParser.Result result = parse("broadcast-warn-active.json");
        assertThat(result.records()).hasSize(30);
        assertThat(result.skipped()).isZero();
        assertThat(result.ids()).hasSize(30).contains("4/2024/517", "A/2024/950", "P/2024/1571", "C/2024/71",
                "12/2024/325");
        NormalizedRecord modu = byId(result).get("4/2024/517");
        assertThat(modu.kind()).isEqualTo(NgaMsiParser.KIND);
        assertThat(modu.observedAt()).isEqualTo(Instant.parse("2024-05-08T16:53:00Z"));
        assertThat(modu.payload()).containsEntry("navArea", "4").containsEntry("msgYear", 2024L)
                .containsEntry("msgNumber", 517L).containsEntry("subregion", "GEN")
                .containsEntry("authority", "SFH 0/24 081641Z MAY 24.")
                .containsEntry("title", "1. NAVAREA IV MOBILE OFFSHORE DRILLING UNITS")
                .containsEntry("cancels", List.of("4/2024/489")).containsEntry("geometryFromText", true);
        assertThat(modu.geometry().getGeometryType()).isEqualTo("MultiPoint");
        FieldSpecContract.assertPayloadKeysDeclared(new NgaMsiAdapter("").descriptor(), result.records());
    }

    @Test
    @DisplayName("the capture's geometry rules: a lettered MultiPolygon, a circle, a trackline, a point, none")
    void captureGeometry() {
        Map<String, NormalizedRecord> records = byId(parse("broadcast-warn-active.json"));
        assertThat(records.get("4/2024/12").geometry()).isInstanceOf(MultiPolygon.class);
        assertThat(records.get("4/2023/1100").geometry()).isInstanceOf(Polygon.class);
        assertThat(records.get("A/2023/2017").geometry().getCoordinates()).hasSize(65);
        assertThat(records.get("A/2024/950").geometry()).isInstanceOf(LineString.class);
        assertThat(records.get("4/2021/1157").geometry().getGeometryType()).isEqualTo("Point");
        NormalizedRecord none = records.get("C/2024/71");
        assertThat(none.geometry()).isNull();
        assertThat(none.payload()).containsEntry("geometryFromText", false)
                .containsEntry("cancels", List.of("C/2024/63"));
        assertThat(parse("broadcast-warn-active.json").withoutGeometry()).isEqualTo(4);
    }

    @Test
    @DisplayName("the hand-made texts: circle, BETWEEN line, DMS polygon with a cancel, invalid minutes")
    void handmade() {
        Map<String, NormalizedRecord> records = byId(parse("handmade.json"));
        assertThat(records).hasSize(7);
        assertThat(records.get("4/2026/901").geometry()).isInstanceOf(Polygon.class);
        assertThat(records.get("4/2026/903").geometry()).isInstanceOf(LineString.class);
        assertThat(records.get("12/2026/904").payload()).containsEntry("cancels", List.of("P/2026/1234"));
        assertThat(records.get("A/2026/905").geometry()).isNull();
        assertThat(records.get("A/2026/906").geometry()).isInstanceOf(MultiPolygon.class);
        assertThat(records.get("P/2026/907").geometry().getGeometryType()).isEqualTo("MultiLineString");
    }

    @Test
    @DisplayName("issueDate: DDHHMMZ MON YYYY in any case; anything else is null")
    void issueDate() {
        assertThat(NgaMsiParser.issueDate("081653Z MAY 2024")).isEqualTo(Instant.parse("2024-05-08T16:53:00Z"));
        assertThat(NgaMsiParser.issueDate("311159Z dec 2023")).isEqualTo(Instant.parse("2023-12-31T11:59:00Z"));
        assertThat(NgaMsiParser.issueDate("2024-05-08")).isNull();
        assertThat(NgaMsiParser.issueDate(null)).isNull();
    }

    @Test
    @DisplayName("cancels: every listed number with the area code; CANCEL THIS MSG is no reference")
    void cancels() {
        assertThat(NgaMsiParser.cancels("2. CANCEL HYDROLANT 927/24, 928/24,\n   929/24.\n3. CANCEL THIS MSG 091200Z"
                + " MAY 24.")).containsExactly("A/2024/927", "A/2024/928", "A/2024/929");
        assertThat(NgaMsiParser.cancels("CANCEL NAVAREA XII 243/23 AND THIS MSG,")).containsExactly("12/2023/243");
        assertThat(NgaMsiParser.cancels("CANCEL HYDROPAC 1234/24(65).")).containsExactly("P/2024/1234");
        assertThat(NgaMsiParser.cancels("CANCEL NAVAREA IV 429/24 AND 470/24.")).containsExactly("4/2024/429",
                "4/2024/470");
        assertThat(NgaMsiParser.cancels("CANCEL HYDROARC 63/24.")).containsExactly("C/2024/63");
        assertThat(NgaMsiParser.cancels(null)).isEmpty();
    }

    @Test
    @DisplayName("entries without an id or issue date and repeated ids are skipped (an unparseable date keeps the id"
            + " live); long text is capped")
    void skipsAndCaps() {
        String longText = "TITLE LINE\\n" + "X".repeat(9000);
        String doc = "{\"broadcast-warn\":[{\"msgYear\":2026,\"msgNumber\":1,\"navArea\":\"4\",\"issueDate\":\"010000Z"
                + " JAN 2026\",\"text\":\"" + longText + "\"},{\"msgYear\":2026,\"msgNumber\":1,\"navArea\":\"4\","
                + "\"issueDate\":\"010000Z JAN 2026\"},{\"msgNumber\":2,\"navArea\":\"4\",\"issueDate\":\"010000Z JAN"
                + " 2026\"},{\"msgYear\":2026,\"msgNumber\":3,\"navArea\":\"4\",\"issueDate\":\"soon\"}]}";
        NgaMsiParser.Result result = NgaMsiParser.parse(MaritimeSources.json(doc.getBytes(), "doc"));
        assertThat(result.records()).hasSize(1);
        assertThat(result.skipped()).isEqualTo(3);
        assertThat(result.ids()).as("a warning with an unparseable issue date stays live, so it is not tombstoned")
                .containsExactly("4/2026/1", "4/2026/3");
        assertThat((String) result.records().getFirst().payload().get("text")).hasSize(NgaMsiParser.TEXT_MAX);
        assertThat(result.records().getFirst().payload()).containsEntry("title", "TITLE LINE");
        assertThatThrownBy(() -> NgaMsiParser.parse(MaritimeSources.json("{\"x\":[]}".getBytes(), "doc")))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
