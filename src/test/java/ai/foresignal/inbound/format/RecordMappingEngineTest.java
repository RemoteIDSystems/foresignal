package ai.foresignal.inbound.format;

import static ai.foresignal.inbound.format.FormatTestSupport.json;
import static ai.foresignal.inbound.format.FormatTestSupport.options;
import static ai.foresignal.inbound.format.FormatTestSupport.source;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link RecordMapping}, {@link RecordMappingEngine} and {@link MappingFormat} (prompt 024). */
class RecordMappingEngineTest {

    private final MappingFormat format = new MappingFormat(FormatTestSupport.JSON,
            new RecordMappingEngine(FormatTestSupport.mapper()));

    @Test
    @DisplayName("recordsPath, externalId, kind, epoch-s time, lat/lon and payload pointers")
    void maps() {
        ParsedIngest parsed = format.parse(json("""
                {"data":{"stations":[
                  {"sid":"st-1","type":"weather","ts":1760256000,"pos":{"la":52.1,"lo":4.3},"obs":{"t":12.5,"rh":80}},
                  {"sid":"st-2","type":"weather","ts":"1760256060","pos":{"la":"52.2","lo":"4.4"},"obs":{"t":11}},
                  {"type":"weather"}
                ]}}"""), source("weather", "air"), options("""
                {"recordsPath":"/data/stations","externalId":"/sid","kind":"/type","observedAt":"/ts",
                 "observedAtFormat":"epoch-s","lat":"/pos/la","lon":"/pos/lo",
                 "payload":[{"from":"/obs/t","to":"temperature"},{"from":"/obs/rh","to":"humidity"}]}"""));

        assertThat(parsed.records()).hasSize(2);
        NormalizedRecord first = parsed.records().get(0);
        assertThat(first.externalId()).isEqualTo("st-1");
        assertThat(first.kind()).isEqualTo("weather");
        assertThat(first.observedAt()).isEqualTo(Instant.ofEpochSecond(1760256000L));
        assertThat(first.geometry().getCoordinate().x).isEqualTo(4.3);
        assertThat(first.payload()).containsEntry("temperature", 12.5).containsEntry("humidity", 80);
        assertThat(parsed.records().get(1).payload()).containsOnlyKeys("temperature");
        assertThat(parsed.rejections()).singleElement().satisfies(r -> {
            assertThat(r.index()).isEqualTo(2);
            assertThat(r.reason()).isEqualTo("EXTERNAL_ID_INVALID");
        });
    }

    @Test
    @DisplayName("a root object is one record; epoch-ms and ISO times; a GeoJSON geometry pointer")
    void rootObjectAndFormats() {
        ParsedIngest ms = format.parse(json("{\"id\":\"a\",\"t\":1760256000123,\"g\":{\"type\":\"Point\",\"coordinates\":[1,2]}}"),
                source("x"), options("{\"externalId\":\"/id\",\"observedAt\":\"/t\",\"observedAtFormat\":\"epoch-ms\",\"geometry\":\"/g\"}"));
        assertThat(ms.records().get(0).observedAt()).isEqualTo(Instant.ofEpochMilli(1760256000123L));
        assertThat(ms.records().get(0).geometry().getCoordinate().y).isEqualTo(2.0);

        ParsedIngest iso = format.parse(json("{\"id\":\"a\",\"t\":\"2026-10-12T07:00:00Z\"}"),
                source("x"), options("{\"externalId\":\"/id\",\"observedAt\":\"/t\"}"));
        assertThat(iso.records().get(0).observedAt()).isEqualTo(Instant.parse("2026-10-12T07:00:00Z"));
        assertThat(iso.records().get(0).kind()).isEqualTo("x");
    }

    @Test
    @DisplayName("unknown kinds and bad times are per-record rejections")
    void rejections() {
        ParsedIngest parsed = format.parse(json("[{\"id\":\"a\",\"k\":\"nope\"},{\"id\":\"b\",\"k\":\"x\",\"t\":\"later\"}]"),
                source("x", "y"), options("{\"externalId\":\"/id\",\"kind\":\"/k\",\"observedAt\":\"/t\"}"));
        assertThat(parsed.rejections()).extracting(ItemRejection::reason).containsExactly("UNKNOWN_KIND", "INVALID_ITEM");
    }

    @Test
    @DisplayName("an invalid mapping lists every field error; a missing mapping is a 400")
    void invalidMapping() {
        assertThatThrownBy(() -> RecordMapping.parse(FormatTestSupport.JSON.readTree("""
                {"recordsPath":"no-slash","observedAtFormat":"weekday","lat":"/a",
                 "payload":[{"from":"/x","to":"bad key"}]}""")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors()).extracting(FieldError::field)
                        .contains("formats.mapping.recordsPath", "formats.mapping.externalId",
                                "formats.mapping.observedAtFormat", "formats.mapping.lat",
                                "formats.mapping.payload[0].to"));
        assertThatThrownBy(() -> format.parse(json("{}"), source("x"), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> format.parse(json("{\"id\":1}"), source("x"),
                options("{\"externalId\":\"/id\",\"recordsPath\":\"/missing\"}")))
                .isInstanceOf(InvalidInputException.class);
    }
}
