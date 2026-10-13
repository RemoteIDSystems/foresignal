package ai.foresignal.source.adapters.celestrak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.space.OmmElements;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link CelestrakGpParser} over the captured {@code GROUP=stations} element sets (prompt 064). */
class CelestrakGpParserTest {

    private static final String ID = CelestrakGpAdapter.ID;

    private static CelestrakGpParser.Parsed csv() {
        return CelestrakGpParser.parseCsv(AdapterFixtures.bytes(ID, "stations.csv"), "stations");
    }

    private static CelestrakGpParser.Parsed json() {
        return CelestrakGpParser.parseJson(AdapterFixtures.bytes(ID, "stations.json"), "stations");
    }

    @Test
    @DisplayName("the CSV and JSON fixtures produce identical element sets and records")
    void csvEqualsJson() {
        CelestrakGpParser.Parsed a = csv();
        CelestrakGpParser.Parsed b = json();
        assertThat(a.elements()).hasSize(21).isEqualTo(b.elements());
        assertThat(a.skipped()).isZero();
        List<NormalizedRecord> ra = a.elements().stream().map(e -> CelestrakGpParser.record(e, List.of("stations"))).toList();
        List<NormalizedRecord> rb = b.elements().stream().map(e -> CelestrakGpParser.record(e, List.of("stations"))).toList();
        assertThat(ra).isEqualTo(rb);
    }

    @Test
    @DisplayName("six-digit catalog numbers parse and become the externalId")
    void sixDigit() {
        OmmElements soyuz = csv().elements().stream().filter(e -> e.norad() == 100057).findFirst().orElseThrow();
        assertThat(soyuz.objectName()).isEqualTo("SOYUZ-MS 29");
        assertThat(soyuz.tleCompatible()).isFalse();
        NormalizedRecord r = CelestrakGpParser.record(soyuz, List.of());
        assertThat(r.externalId()).isEqualTo("100057");
        assertThat(r.payload().get("NORAD_CAT_ID")).isEqualTo(100057L);
    }

    @Test
    @DisplayName("EPOCH without a zone is UTC and becomes observedAt; numbers stay numbers; no geometry")
    void epochAndPayload() {
        OmmElements iss = csv().elements().get(0);
        assertThat(iss.epoch()).isEqualTo(Instant.parse("2026-10-13T02:41:20.970528Z"));
        assertThat(iss.bstar()).isEqualTo(0.11499036E-3);
        assertThat(iss.meanMotionDot()).isEqualTo(0.5858E-4);
        NormalizedRecord r = CelestrakGpParser.record(iss, List.of("stations", "visual"));
        assertThat(r.kind()).isEqualTo("satellite");
        assertThat(r.observedAt()).isEqualTo(iss.epoch());
        assertThat(r.eventTime()).isNull();
        assertThat(r.geometry()).isNull();
        Map<String, Object> p = r.payload();
        assertThat(p.get("EPOCH")).isEqualTo("2026-10-13T02:41:20.970528");
        assertThat(p.get("MEAN_MOTION")).isEqualTo(15.48846742);
        assertThat(p.get("OBJECT_ID")).isEqualTo("1998-067A");
        assertThat(p.get("groups")).isEqualTo(List.of("stations", "visual"));
        assertThat(p.keySet()).containsAll(OmmElements.KEYS);
        assertThat(OmmElements.fromPayload(p)).isEqualTo(iss);
    }

    @Test
    @DisplayName("zero objects for active is a format error; for another group it is an empty result")
    void emptyActive() {
        byte[] header = (String.join(",", OmmElements.KEYS) + "\r\n").getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> CelestrakGpParser.parseCsv(header, "active"))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("zero objects");
        assertThatThrownBy(() -> CelestrakGpParser.parseJson("[]".getBytes(StandardCharsets.UTF_8), "active"))
                .isInstanceOf(UpstreamFormatException.class);
        assertThat(CelestrakGpParser.parseCsv(header, "stations").elements()).isEmpty();
    }

    @Test
    @DisplayName("a header without a required column and a non-array JSON body are format errors")
    void malformed() {
        assertThatThrownBy(() -> CelestrakGpParser.parseCsv("OBJECT_NAME,EPOCH\r\nX,2026-01-01T00:00:00\r\n"
                .getBytes(StandardCharsets.UTF_8), "stations")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CelestrakGpParser.parseJson("{\"a\":1}".getBytes(StandardCharsets.UTF_8), "stations"))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("an unreadable row is skipped and counted, quoted names with commas parse")
    void skippedAndQuoted() {
        String body = String.join(",", OmmElements.KEYS) + "\r\n"
                + "\"NAME, WITH COMMA\",2026-001A,2026-10-13T00:00:00.000000,15.5,.0001,51.6,10,20,30,0,U,123456,999,5,.1E-3,.1E-4,0\r\n"
                + "BROKEN,2026-001B,not-a-date,15.5,.0001,51.6,10,20,30,0,U,123457,999,5,0,0,0\r\n";
        CelestrakGpParser.Parsed parsed = CelestrakGpParser.parseCsv(body.getBytes(StandardCharsets.UTF_8), "stations");
        assertThat(parsed.elements()).hasSize(1);
        assertThat(parsed.elements().get(0).objectName()).isEqualTo("NAME, WITH COMMA");
        assertThat(parsed.skipped()).isEqualTo(1);
    }

    @Test
    @DisplayName("looksLike recognizes CSV and JSON and refuses an HTML error page")
    void looksLike() {
        assertThat(CelestrakGpParser.looksLike(AdapterFixtures.bytes(ID, "stations.csv"), "csv")).isTrue();
        assertThat(CelestrakGpParser.looksLike(AdapterFixtures.bytes(ID, "stations.json"), "json")).isTrue();
        assertThat(CelestrakGpParser.looksLike("<html>error</html>".getBytes(StandardCharsets.UTF_8), "csv")).isFalse();
        assertThat(CelestrakGpParser.looksLike("OBJECT_NAME,".getBytes(StandardCharsets.UTF_8), "json")).isFalse();
    }
}
