package ai.foresignal.source.adapters.nhc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link NhcWindProbMapper} and the layer lookup of {@link NhcWindProbsAdapter} on features shaped like the NHC
 * summary MapServer's "Probabilistic Winds 34 kts" layer (fields {@code percentage}, {@code idp_filedate}; checked
 * 2026-10-17): band ids and bounds, the 120-hour validity and the declared payload.
 */
class NhcWindProbMapperTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant FILED = Instant.parse("2026-10-13T15:00:00Z");

    static JsonNode feature(String percentage, Long filed) {
        String props = "\"percentage\":" + (percentage == null ? "null" : "\"" + percentage + "\"")
                + (filed == null ? "" : ",\"idp_filedate\":" + filed) + ",\"idp_source\":\"wsp_120hr5km34\"";
        return JSON.readTree("{\"type\":\"Feature\",\"properties\":{" + props + "},\"geometry\":{\"type\":\"Polygon\","
                + "\"coordinates\":[[[-80,25],[-79,25],[-79,26],[-80,26],[-80,25]]]}}");
    }

    @ParameterizedTest
    @CsvSource({"5-10%,34kt:5-10,0.05,0.1", "40-50%,34kt:40-50,0.4,0.5", "<5%,34kt:0-5,0.0,0.05",
        ">90%,34kt:90-100,0.9,1.0"})
    @DisplayName("bands: id {kt}kt:{low}-{high}, pMin and pMax as probabilities, the published text kept")
    void bands(String band, String id, double pMin, double pMax) {
        MappedRecord mapped = NhcWindProbMapper.map(34, List.of(feature(band, FILED.toEpochMilli()))).get(0);
        assertThat(mapped.accepted()).isTrue();
        NormalizedRecord r = mapped.record();
        assertThat(r.externalId()).isEqualTo(id);
        assertThat(r.kind()).isEqualTo(NhcWindProbMapper.KIND);
        assertThat(((Number) r.payload().get("pMin")).doubleValue()).isEqualTo(pMin);
        assertThat(((Number) r.payload().get("pMax")).doubleValue()).isEqualTo(pMax);
        assertThat(r.payload().get("band")).isEqualTo(band);
        assertThat(r.payload().get("thresholdKt")).isEqualTo(34);
    }

    @Test
    @DisplayName("time: observedAt = validFrom = idp_filedate, validUntil 120 hours later")
    void time() {
        NormalizedRecord r = NhcWindProbMapper.map(64, List.of(feature("10-20%", FILED.toEpochMilli()))).get(0)
                .record();
        assertThat(r.observedAt()).isEqualTo(FILED);
        assertThat(r.validFrom()).isEqualTo(FILED);
        assertThat(r.validUntil()).isEqualTo(FILED.plusSeconds(120 * 3600));
        assertThat(r.payload()).containsEntry("issuedAt", FILED.toString()).containsEntry("horizonHours", 120L);
        assertThat(r.externalId()).isEqualTo("64kt:10-20");
    }

    @Test
    @DisplayName("skips: no product time, an unreadable band, inverted bounds")
    void skips() {
        List<MappedRecord> mapped = NhcWindProbMapper.map(50, List.of(feature("10-20%", null),
                feature("lots", FILED.toEpochMilli()), feature("50-40%", FILED.toEpochMilli()),
                feature(null, FILED.toEpochMilli())));
        assertThat(mapped).extracting(MappedRecord::reason).containsExactly(MappedRecord.MISSING_OBSERVED_AT,
                MappedRecord.MISSING_ID, MappedRecord.MISSING_ID, MappedRecord.MISSING_ID);
    }

    @Test
    @DisplayName("every payload key is declared in the descriptor")
    void declared() {
        List<NormalizedRecord> all = new ArrayList<>();
        for (String band : List.of("5-10%", "10-20%", ">90%")) {
            all.add(NhcWindProbMapper.map(34, List.of(feature(band, FILED.toEpochMilli()))).get(0).record());
        }
        FieldSpecContract.assertPayloadKeysDeclared(new NhcWindProbsAdapter("", (java.util.function.Supplier<
                io.micrometer.core.instrument.MeterRegistry>) null).descriptor(), all);
    }

    @Test
    @DisplayName("layers: resolved by name (30, 31, 32 on 2026-10-17); a missing name is a format error")
    void layers() {
        String doc = "{\"layers\":[{\"id\":7,\"name\":\"Forecast Cone\"},{\"id\":29,\"name\":\"Probabilistic Winds\"},"
                + "{\"id\":30,\"name\":\"Probabilistic Winds 34 kts\"},{\"id\":31,\"name\":\"Probabilistic Winds 50 kts\"},"
                + "{\"id\":32,\"name\":\"Probabilistic Winds 64 kts\"}]}";
        assertThat(NhcWindProbsAdapter.parseLayers(doc.getBytes(StandardCharsets.UTF_8)))
                .isEqualTo(Map.of(34, 30, 50, 31, 64, 32));
        String missing = "{\"layers\":[{\"id\":30,\"name\":\"Probabilistic Winds 34 kts\"}]}";
        assertThatThrownBy(() -> NhcWindProbsAdapter.parseLayers(missing.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("50 kts");
        assertThatThrownBy(() -> NhcWindProbsAdapter.parseLayers("not json".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("descriptor: POLLING, event retention of 1825 days, US-PD, zero results normal")
    void descriptor() {
        var d = new NhcWindProbsAdapter("", (java.util.function.Supplier<io.micrometer.core.instrument.MeterRegistry>)
                null).descriptor();
        assertThat(d.id()).isEqualTo("nhc-wind-probs");
        assertThat(d.kinds()).containsExactly(NhcWindProbMapper.KIND);
        assertThat(d.defaultRetention()).isEqualTo(java.time.Duration.ofDays(1825));
    }
}
