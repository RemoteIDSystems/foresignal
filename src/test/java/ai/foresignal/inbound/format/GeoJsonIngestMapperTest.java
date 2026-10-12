package ai.foresignal.inbound.format;

import static ai.foresignal.inbound.format.FormatTestSupport.json;
import static ai.foresignal.inbound.format.FormatTestSupport.source;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code native} format: {@link GeoJsonIngestMapper} and {@link NativeFormat} (prompt 024). */
class GeoJsonIngestMapperTest {

    private final NativeFormat format = new NativeFormat(FormatTestSupport.JSON, FormatTestSupport.mapper());

    @Test
    @DisplayName("a FeatureCollection maps id, kind, times, tombstone, quality and payload; geometry is a JTS point")
    void featureCollection() {
        ParsedIngest parsed = format.parse(json("""
                {"type":"FeatureCollection","features":[
                  {"type":"Feature","id":"ship-1","geometry":{"type":"Point","coordinates":[4.5,52.1]},
                   "properties":{"kind":"vessel","observedAt":"2026-10-12T07:59:00Z","eventTime":"2026-10-12T07:00:00Z",
                     "validUntil":"2026-10-12T09:00:00Z","quality":{"accuracyMeters":12.5},"name":"Example","speed":11}},
                  {"type":"Feature","id":42,"geometry":null,"properties":{"kind":"vessel","tombstone":true,"observedAt":"2026-10-12T07:59:00Z"}}
                ]}"""), source("vessel", "buoy"), FormatOptions.empty());

        assertThat(parsed.rejections()).isEmpty();
        assertThat(parsed.items()).isEqualTo(2);
        assertThat(parsed.indexes()).containsExactly(0, 1);
        NormalizedRecord first = parsed.records().get(0);
        assertThat(first.externalId()).isEqualTo("ship-1");
        assertThat(first.kind()).isEqualTo("vessel");
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-10-12T07:59:00Z"));
        assertThat(first.eventTime()).isEqualTo(Instant.parse("2026-10-12T07:00:00Z"));
        assertThat(first.validUntil()).isEqualTo(Instant.parse("2026-10-12T09:00:00Z"));
        assertThat(first.quality().accuracyMeters()).isEqualTo(12.5);
        assertThat(first.payload()).containsEntry("name", "Example").containsEntry("speed", 11)
                .doesNotContainKeys("kind", "observedAt", "quality");
        assertThat(first.geometry().getCoordinate().x).isEqualTo(4.5);
        assertThat(first.geometry().getSRID()).isEqualTo(4326);
        assertThat(parsed.records().get(1).externalId()).isEqualTo("42");
        assertThat(parsed.records().get(1).tombstone()).isTrue();
    }

    @Test
    @DisplayName("kind is optional with a single kind; missing observedAt is left for the writer")
    void singleKindAndNoDefaultTime() {
        ParsedIngest parsed = format.parse(json("""
                {"type":"FeatureCollection","features":[{"type":"Feature","id":"a","geometry":null,"properties":{"v":1}}]}"""),
                source("reading"), FormatOptions.empty());

        assertThat(parsed.records()).singleElement().satisfies(r -> {
            assertThat(r.kind()).isEqualTo("reading");
            assertThat(r.observedAt()).isNull();
        });
    }

    @Test
    @DisplayName("missing id, unknown or ambiguous kind, bad geometry and bad times are per-item rejections")
    void rejections() {
        ParsedIngest parsed = format.parse(json("""
                {"type":"FeatureCollection","features":[
                  {"type":"Feature","geometry":null,"properties":{"kind":"a"}},
                  {"type":"Feature","id":"b","geometry":null,"properties":{"kind":"zzz"}},
                  {"type":"Feature","id":"c","geometry":null,"properties":{}},
                  {"type":"Feature","id":"d","geometry":{"type":"Polygon","coordinates":[[[0,0],[1,1]]]},"properties":{"kind":"a"}},
                  {"type":"Feature","id":"e","geometry":null,"properties":{"kind":"a","observedAt":"yesterday"}},
                  {"type":"Feature","id":"f","geometry":null,"properties":{"kind":"a"}}
                ]}"""), source("a", "b"), FormatOptions.empty());

        assertThat(parsed.rejections()).extracting(ItemRejection::reason).containsExactly(
                "EXTERNAL_ID_INVALID", "UNKNOWN_KIND", "UNKNOWN_KIND", "GEOMETRY_INVALID", "INVALID_ITEM");
        assertThat(parsed.rejections()).extracting(ItemRejection::index).containsExactly(0, 1, 2, 3, 4);
        assertThat(parsed.records()).extracting(NormalizedRecord::externalId).containsExactly("f");
        assertThat(parsed.indexes()).containsExactly(5);
        assertThat(parsed.items()).isEqualTo(6);
    }

    @Test
    @DisplayName("{records:[...]} in the NormalizedRecordPreview shape")
    void recordsShape() {
        ParsedIngest parsed = format.parse(json("""
                {"records":[{"externalId":"x1","kind":"a","observedAt":"2026-10-12T07:00:00Z",
                  "geometry":{"type":"Point","coordinates":[1,2]},"payload":{"level":3},"tombstone":false}]}"""),
                source("a"), FormatOptions.empty());

        NormalizedRecord record = parsed.records().get(0);
        assertThat(record.externalId()).isEqualTo("x1");
        assertThat(record.payload()).containsEntry("level", 3);
        assertThat(record.geometry().getCoordinate().y).isEqualTo(2.0);
    }

    @Test
    @DisplayName("a body of neither shape, or not JSON, is a 400")
    void wholeRequestFailures() {
        assertThatThrownBy(() -> format.parse(json("{\"foo\":1}"), source("a"), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> format.parse(json("not json"), source("a"), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> format.parse(json(""), source("a"), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("resolveKind: requested must be declared; otherwise only a single kind is implied")
    void resolveKind() {
        assertThat(GeoJsonIngestMapper.resolveKind("a", source("a", "b"))).isEqualTo("a");
        assertThat(GeoJsonIngestMapper.resolveKind("c", source("a", "b"))).isNull();
        assertThat(GeoJsonIngestMapper.resolveKind(null, source("a", "b"))).isNull();
        assertThat(GeoJsonIngestMapper.resolveKind(null, source("a"))).isEqualTo("a");
    }

    @Test
    @DisplayName("IngestTimes accepts instants, offsets and epoch millis and refuses garbage")
    void times() {
        assertThat(IngestTimes.parse("2026-10-12T10:00:00+02:00")).isEqualTo(Instant.parse("2026-10-12T08:00:00Z"));
        assertThat(IngestTimes.parse(FormatTestSupport.JSON.readTree("1760256000000")))
                .isEqualTo(Instant.ofEpochMilli(1760256000000L));
        assertThat(IngestTimes.parse((String) null)).isNull();
        assertThat(IngestTimes.parseEpoch(FormatTestSupport.JSON.readTree("\"1760256000\""), false))
                .isEqualTo(Instant.ofEpochSecond(1760256000L));
        assertThatThrownBy(() -> IngestTimes.parse("soon")).isInstanceOf(InvalidInputException.class);
    }
}
