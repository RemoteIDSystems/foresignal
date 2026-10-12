package ai.foresignal.inbound.formats.location;

import static ai.foresignal.inbound.formats.location.LocationTestSupport.fixture;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.json;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.options;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.source;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.DeviceEntityMismatchException;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.inbound.format.NativeReply;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/** {@link OverlandFormat} (prompt 039). */
class OverlandFormatTest {

    private final OverlandFormat format = new OverlandFormat(LocationTestSupport.JSON);

    @Test
    @DisplayName("each Feature maps geometry and properties; wifi is dropped; trip and current are ignored")
    void mapping() {
        ParsedIngest parsed = format.parse(json(fixture("overland-batch.json")), source(), FormatOptions.empty());

        assertThat(parsed.items()).isEqualTo(3);
        assertThat(parsed.records()).hasSize(1);
        NormalizedRecord r = parsed.records().get(0);
        assertThat(r.externalId()).isEqualTo("iphone-1");
        assertThat(r.kind()).isEqualTo("tracker");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-12T07:58:00Z"));
        assertThat(r.geometry().getCoordinate().x).isEqualTo(-73.6);
        assertThat(r.geometry().getCoordinate().y).isEqualTo(45.5);
        assertThat(r.quality().accuracyMeters()).isEqualTo(15.0);
        assertThat(r.payload()).containsEntry("altitudeM", 30.0).containsEntry("speedMs", 1.4)
                .containsEntry("courseDeg", 270.0).containsEntry("motion", List.of("walking"))
                .containsEntry("charging", false).containsEntry("batteryState", "unplugged")
                .doesNotContainKey("wifi");
        assertThat((Double) r.payload().get("battery")).isCloseTo(67.0, within(1e-9));
    }

    @Test
    @DisplayName("low accuracy and (0,0) points are counted as rejections")
    void rejections() {
        ParsedIngest parsed = format.parse(json(fixture("overland-batch.json")), source(), FormatOptions.empty());
        assertThat(parsed.rejections()).extracting(ItemRejection::index, ItemRejection::reason).containsExactly(
                org.assertj.core.groups.Tuple.tuple(1, LocationRecords.LOW_ACCURACY),
                org.assertj.core.groups.Tuple.tuple(2, LocationRecords.NULL_ISLAND));
    }

    @Test
    @DisplayName("keepWifi keeps the network name")
    void keepWifi() {
        ParsedIngest parsed = format.parse(json(fixture("overland-batch.json")), source(), options("{\"keepWifi\":true}"));
        assertThat(parsed.records().get(0).payload()).containsEntry("wifi", "home-net");
    }

    @Test
    @DisplayName("the reply is {\"result\":\"ok\"} even with rejections")
    void reply() {
        ParsedIngest parsed = format.parse(json(fixture("overland-batch.json")), source(), FormatOptions.empty());
        NativeReply reply = format.nativeReply(parsed);
        assertThat(reply.status()).isEqualTo(200);
        assertThat(reply.contentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(reply.body()).isEqualTo("{\"result\":\"ok\"}");
    }

    @Test
    @DisplayName("more than 1,000 points, a missing locations array and bad JSON are 400s")
    void limits() {
        StringBuilder many = new StringBuilder("{\"locations\":[");
        for (int i = 0; i < 1001; i++) {
            many.append(i == 0 ? "" : ",").append("{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,1]},"
                    + "\"properties\":{\"timestamp\":\"2026-10-12T07:58:00Z\",\"device_id\":\"d\"}}");
        }
        many.append("]}");
        assertThatThrownBy(() -> format.parse(json(many.toString()), source(), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> format.parse(json("{\"trip\":{}}"), source(), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> format.parse(json("nope"), source(), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("a non-Point geometry, a missing timestamp and a missing device id are rejected")
    void invalidPoints() {
        ParsedIngest parsed = format.parse(json("""
                {"locations":[
                 {"type":"Feature","geometry":{"type":"LineString","coordinates":[[1,1],[2,2]]},"properties":{"timestamp":"2026-10-12T07:58:00Z","device_id":"d"}},
                 {"type":"Feature","geometry":{"type":"Point","coordinates":[-73.6,45.5]},"properties":{"device_id":"d"}},
                 {"type":"Feature","geometry":{"type":"Point","coordinates":[-73.6,45.5]},"properties":{"timestamp":"2026-10-12T07:58:00Z"}}
                ]}"""), source(), FormatOptions.empty());
        assertThat(parsed.rejections()).extracting(ItemRejection::reason).containsExactly(
                LocationRecords.INVALID_FIX, LocationRecords.MISSING_OBSERVED_AT, LocationRecords.EXTERNAL_ID_INVALID);
    }

    @Test
    @DisplayName("a device key forces its id; another device_id is 403 DEVICE_ENTITY_MISMATCH")
    void deviceBinding() {
        String noId = "{\"locations\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-73.6,45.5]},"
                + "\"properties\":{\"timestamp\":\"2026-10-12T07:58:00Z\"}}]}";
        assertThat(format.parse(json(noId).withBoundExternalId("dev-1"), source(), FormatOptions.empty())
                .records().get(0).externalId()).isEqualTo("dev-1");
        assertThatThrownBy(() -> format.parse(json(fixture("overland-batch.json")).withBoundExternalId("dev-1"), source(),
                FormatOptions.empty())).isInstanceOf(DeviceEntityMismatchException.class);
    }
}
