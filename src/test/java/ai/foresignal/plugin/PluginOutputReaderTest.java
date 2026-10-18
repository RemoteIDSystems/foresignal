package ai.foresignal.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.inbound.format.GeoJsonIngestMapper;
import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

/**
 * The ABI v1 output document (prompt 141): strict UTF-8 JSON {@code {"records":[...],"complete":bool}}, records in the
 * native ingest format's shape, per-item rejections, and refusals of everything else.
 */
class PluginOutputReaderTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final PluginOutputReader READER = new PluginOutputReader(new GeoJsonIngestMapper(JSON), JSON);
    static final SourceDescriptor TWO_KINDS = SourceDescriptor.builder("wasm-two")
            .name("Two kinds")
            .kinds("gauge", "alarm")
            .build();

    static ParserOutput read(String json) {
        return READER.read("p", json.getBytes(StandardCharsets.UTF_8), TWO_KINDS, PluginLimits.DEFAULTS);
    }

    @Test
    @DisplayName("records: every native field, kinds resolved against the source, rejections per item")
    void records() {
        ParserOutput output = read("""
                {"records":[
                  {"externalId":"a","kind":"gauge","observedAt":"2026-10-18T06:00:00Z","eventTime":"2026-10-18T05:59:00Z",
                   "geometry":{"type":"Point","coordinates":[1.5,2.5]},"payload":{"level":3.25},"quality":{"confidence":0.9}},
                  {"externalId":"b","kind":"alarm","tombstone":true},
                  {"externalId":"c"},
                  {"externalId":"d","kind":"unknown"},
                  {"kind":"gauge"},
                  {"externalId":"e","kind":"gauge","payload":"not an object"},
                  "not an object"
                ],"complete":true,"extra":"ignored"}
                """);

        assertThat(output.complete()).isTrue();
        assertThat(output.records()).extracting(NormalizedRecord::externalId).containsExactly("a", "b");
        NormalizedRecord a = output.records().get(0);
        assertThat(a.observedAt()).isEqualTo(Instant.parse("2026-10-18T06:00:00Z"));
        assertThat(a.eventTime()).isEqualTo(Instant.parse("2026-10-18T05:59:00Z"));
        assertThat(a.payload()).containsEntry("level", 3.25);
        assertThat(a.quality().confidence()).isEqualTo(0.9);
        assertThat(output.records().get(1).tombstone()).isTrue();
        assertThat(output.rejections()).extracting(ItemRejection::reason).containsExactly("UNKNOWN_KIND",
                "UNKNOWN_KIND", "EXTERNAL_ID_INVALID", "INVALID_ITEM", "INVALID_ITEM");
        assertThat(output.parsed().items()).isEqualTo(7);
    }

    @Test
    @DisplayName("complete is false when absent or null")
    void completeDefault() {
        assertThat(read("{\"records\":[]}").complete()).isFalse();
        assertThat(read("{\"records\":[],\"complete\":null}").complete()).isFalse();
        assertThat(read("{\"records\":[],\"complete\":false}").complete()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", "{}", "{\"records\":{}}", "{\"records\":[],\"complete\":\"yes\"}", "not json", "42"})
    @DisplayName("anything but the ABI object is INVALID_OUTPUT")
    void invalid(String json) {
        assertThatThrownBy(() -> read(json))
                .isInstanceOfSatisfying(PluginException.class, e -> {
                    assertThat(e.reason()).isEqualTo(PluginException.Reason.INVALID_OUTPUT);
                    assertThat(e.plugin()).isEqualTo("p");
                });
    }

    @Test
    @DisplayName("output that is not UTF-8 is refused")
    void notUtf8() {
        byte[] bad = {'{', '"', 'r', (byte) 0xC3, (byte) 0x28, '"', '}'};
        assertThatThrownBy(() -> READER.read("p", bad, TWO_KINDS, PluginLimits.DEFAULTS))
                .isInstanceOfSatisfying(PluginException.class,
                        e -> assertThat(e.getMessage()).contains("not valid UTF-8"));
    }
}
