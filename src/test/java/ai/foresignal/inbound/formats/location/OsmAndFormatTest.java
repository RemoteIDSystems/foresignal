package ai.foresignal.inbound.formats.location;

import static ai.foresignal.inbound.formats.location.LocationTestSupport.fixture;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.form;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.json;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.query;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;

/** {@link OsmAndFormat} (prompt 039). */
class OsmAndFormatTest {

    private final OsmAndFormat format = new OsmAndFormat(LocationTestSupport.JSON);

    private static final Instant T = Instant.parse("2026-10-12T07:59:30Z");

    @Test
    @DisplayName("query parameters map every field; speed is knots")
    void queryMapping() {
        ParsedIngest parsed = format.parse(query("id", "car-1", "lat", "45.5", "lon", "-73.6", "timestamp", "1760255970",
                "speed", "10", "bearing", "45", "altitude", "33", "accuracy", "8", "batt", "77", "charge", "true"),
                source(), FormatOptions.empty());

        assertThat(parsed.rejections()).isEmpty();
        NormalizedRecord r = parsed.records().get(0);
        assertThat(r.externalId()).isEqualTo("car-1");
        assertThat(r.kind()).isEqualTo("tracker");
        assertThat(r.observedAt()).isEqualTo(T);
        assertThat(r.geometry().getCoordinate().y).isEqualTo(45.5);
        assertThat(r.geometry().getCoordinate().x).isEqualTo(-73.6);
        assertThat((Double) r.payload().get("speedMs")).isCloseTo(10 * 1852d / 3600d, within(1e-9));
        assertThat(r.payload()).containsEntry("courseDeg", 45.0).containsEntry("altitudeM", 33.0)
                .containsEntry("battery", 77.0).containsEntry("charging", true);
        assertThat(r.quality().accuracyMeters()).isEqualTo(8.0);
    }

    @ParameterizedTest(name = "timestamp {0}")
    @CsvSource({"1760255970", "1760255970000", "2026-10-12T07:59:30Z", "2026-10-12 07:59:30"})
    @DisplayName("timestamps: epoch seconds, epoch milliseconds, ISO-8601 and yyyy-MM-dd HH:mm:ss UTC")
    void timestamps(String timestamp) {
        ParsedIngest parsed = format.parse(query("deviceid", "car-1", "lat", "45.5", "lon", "-73.6",
                "timestamp", timestamp), source(), FormatOptions.empty());
        assertThat(parsed.records().get(0).observedAt()).isEqualTo(T);
    }

    @Test
    @DisplayName("location=lat,lon and heading are accepted")
    void locationParameter() {
        NormalizedRecord r = format.parse(query("id", "car-1", "location", "45.5,-73.6", "timestamp", "1760255970",
                "heading", "200"), source(), FormatOptions.empty()).records().get(0);
        assertThat(r.geometry().getCoordinate().x).isEqualTo(-73.6);
        assertThat(r.payload()).containsEntry("courseDeg", 200.0);
    }

    @Test
    @DisplayName("a form body is read like a query")
    void formBody() {
        ParsedIngest parsed = format.parse(form("id=car-1&lat=45.5&lon=-73.6&timestamp=2026-10-12%2007%3A59%3A30&speed=0"),
                source(), FormatOptions.empty());
        assertThat(parsed.rejections()).isEmpty();
        assertThat(parsed.records().get(0).observedAt()).isEqualTo(T);
        assertThat(parsed.records().get(0).payload()).containsEntry("speedMs", 0.0);
    }

    @Test
    @DisplayName("Traccar Client JSON: m/s speed, fractional battery, motion and event")
    void jsonBody() {
        ParsedIngest parsed = format.parse(json(fixture("osmand-traccar-client.json")), source(), FormatOptions.empty());

        assertThat(parsed.rejections()).isEmpty();
        NormalizedRecord r = parsed.records().get(0);
        assertThat(r.externalId()).isEqualTo("car-1");
        assertThat(r.observedAt()).isEqualTo(T);
        assertThat(r.payload()).containsEntry("speedMs", 12.5).containsEntry("courseDeg", 180.0)
                .containsEntry("altitudeM", 33.0).containsEntry("charging", true)
                .containsEntry("event", "motionchange");
        assertThat((Double) r.payload().get("battery")).isCloseTo(42.0, within(1e-9));
        assertThat(r.payload().get("motion")).isEqualTo(java.util.List.of("moving"));
        assertThat(r.quality().accuracyMeters()).isEqualTo(8.0);
    }

    @Test
    @DisplayName("valid=false, (0,0), low accuracy and a missing device id are rejected, not refused")
    void rejections() {
        assertThat(reason(query("id", "car-1", "lat", "45.5", "lon", "-73.6", "timestamp", "1760255970", "valid", "false")))
                .isEqualTo(LocationRecords.INVALID_FIX);
        assertThat(reason(query("id", "car-1", "lat", "0", "lon", "0", "timestamp", "1760255970")))
                .isEqualTo(LocationRecords.NULL_ISLAND);
        assertThat(reason(query("id", "car-1", "lat", "45.5", "lon", "-73.6", "timestamp", "1760255970", "accuracy", "250")))
                .isEqualTo(LocationRecords.LOW_ACCURACY);
        assertThat(reason(query("lat", "45.5", "lon", "-73.6", "timestamp", "1760255970")))
                .isEqualTo(LocationRecords.EXTERNAL_ID_INVALID);
        assertThat(reason(query("id", "car-1", "lat", "45.5", "lon", "-73.6")))
                .isEqualTo(LocationRecords.MISSING_OBSERVED_AT);
        assertThat(reason(query("id", "car-1", "lat", "95", "lon", "-73.6", "timestamp", "1760255970")))
                .isEqualTo(LocationRecords.INVALID_FIX);
        assertThat(reason(query("id", "car-1", "lat", "45.5", "lon", "-73.6", "timestamp", "yesterday")))
                .isEqualTo(LocationRecords.INVALID_ITEM);
    }

    @Test
    @DisplayName("a device key forces its id; a different id in the request is 403 DEVICE_ENTITY_MISMATCH")
    void deviceBinding() {
        ParsedIngest forced = format.parse(query("lat", "45.5", "lon", "-73.6", "timestamp", "1760255970")
                .withBoundExternalId("dev-1"), source(), FormatOptions.empty());
        assertThat(forced.records().get(0).externalId()).isEqualTo("dev-1");

        ParsedIngest same = format.parse(query("id", "dev-1", "lat", "45.5", "lon", "-73.6", "timestamp", "1760255970")
                .withBoundExternalId("dev-1"), source(), FormatOptions.empty());
        assertThat(same.records()).hasSize(1);

        assertThatThrownBy(() -> format.parse(query("id", "other", "lat", "45.5", "lon", "-73.6")
                .withBoundExternalId("dev-1"), source(), FormatOptions.empty()))
                .isInstanceOf(DeviceEntityMismatchException.class);
        assertThatThrownBy(() -> format.parse(json(fixture("osmand-traccar-client.json")).withBoundExternalId("dev-1"),
                source(), FormatOptions.empty())).isInstanceOf(DeviceEntityMismatchException.class);
    }

    @Test
    @DisplayName("GET and POST, path-key friendly, empty 200 reply; bad JSON is a 400")
    void protocol() {
        assertThat(format.methods()).containsExactlyInAnyOrder(HttpMethod.GET, HttpMethod.POST);
        assertThat(format.pathKeyFriendly()).isTrue();
        NativeReply reply = format.nativeReply(ParsedIngest.empty());
        assertThat(reply.status()).isEqualTo(200);
        assertThat(reply.body()).isEmpty();
        assertThat(reply.contentType()).isNull();
        assertThatThrownBy(() -> format.parse(json("{\"device_id\":\"x\"}"), source(), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> format.parse(json("{broken"), source(), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
    }

    private String reason(ai.foresignal.inbound.format.IngestRequest request) {
        ParsedIngest parsed = format.parse(request, source(), FormatOptions.empty());
        assertThat(parsed.records()).isEmpty();
        return parsed.rejections().stream().map(ItemRejection::reason).findFirst().orElseThrow();
    }
}
