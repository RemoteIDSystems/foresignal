package ai.foresignal.inbound.formats.location;

import static ai.foresignal.inbound.formats.location.LocationTestSupport.form;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.json;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.query;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.source;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.DeviceEntityMismatchException;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.inbound.format.NativeReply;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

/** {@link GpsLoggerFormat} (prompt 039). */
class GpsLoggerFormatTest {

    private final GpsLoggerFormat format = new GpsLoggerFormat(LocationTestSupport.JSON);

    private static final Instant T = Instant.parse("2026-10-12T07:59:30Z");

    private void assertMapped(ParsedIngest parsed) {
        assertThat(parsed.rejections()).isEmpty();
        NormalizedRecord r = parsed.records().get(0);
        assertThat(r.externalId()).isEqualTo("pixel-1");
        assertThat(r.kind()).isEqualTo("tracker");
        assertThat(r.observedAt()).isEqualTo(T);
        assertThat(r.geometry().getCoordinate().y).isEqualTo(45.5);
        assertThat(r.geometry().getCoordinate().x).isEqualTo(-73.6);
        assertThat(r.quality().accuracyMeters()).isEqualTo(6.0);
        assertThat(r.payload()).containsEntry("altitudeM", 31.0).containsEntry("speedMs", 2.5)
                .containsEntry("courseDeg", 120.0).containsEntry("battery", 55.0);
    }

    @Test
    @DisplayName("the documented template as a GET query")
    void getTemplate() {
        assertMapped(format.parse(query("lat", "45.5", "lon", "-73.6", "time", "2026-10-12T07:59:30Z", "acc", "6",
                "alt", "31", "spd", "2.5", "dir", "120", "batt", "55", "dev", "pixel-1"), source(), FormatOptions.empty()));
    }

    @Test
    @DisplayName("names are matched case-insensitively")
    void caseInsensitive() {
        assertMapped(format.parse(query("LAT", "45.5", "Lon", "-73.6", "TIME", "2026-10-12T07:59:30Z", "ACC", "6",
                "Alt", "31", "SPD", "2.5", "DIR", "120", "BATT", "55", "DEV", "pixel-1"), source(), FormatOptions.empty()));
    }

    @Test
    @DisplayName("a POST form body and a POST JSON body")
    void postBodies() {
        assertMapped(format.parse(form("lat=45.5&lon=-73.6&time=2026-10-12T07%3A59%3A30Z&acc=6&alt=31&spd=2.5&dir=120"
                + "&batt=55&dev=pixel-1"), source(), FormatOptions.empty()));
        assertMapped(format.parse(json("{\"Lat\":45.5,\"lon\":-73.6,\"time\":\"2026-10-12T07:59:30Z\",\"acc\":6,"
                + "\"alt\":31,\"spd\":2.5,\"dir\":120,\"batt\":55,\"dev\":\"pixel-1\"}"), source(), FormatOptions.empty()));
    }

    @Test
    @DisplayName("an epoch time and the charging flag")
    void epochAndCharging() {
        NormalizedRecord r = format.parse(query("lat", "45.5", "lon", "-73.6", "timestamp", "1791791970", "dev", "pixel-1",
                "ischarging", "true"), source(), FormatOptions.empty()).records().get(0);
        assertThat(r.observedAt()).isEqualTo(T);
        assertThat(r.payload()).containsEntry("charging", true);
    }

    @Test
    @DisplayName("low accuracy, (0,0), missing coordinates and a missing device are rejected")
    void rejections() {
        assertThat(reason(query("lat", "45.5", "lon", "-73.6", "time", "2026-10-12T07:59:30Z", "acc", "201", "dev", "p")))
                .isEqualTo(LocationRecords.LOW_ACCURACY);
        assertThat(reason(query("lat", "0", "lon", "0", "time", "2026-10-12T07:59:30Z", "dev", "p")))
                .isEqualTo(LocationRecords.NULL_ISLAND);
        assertThat(reason(query("time", "2026-10-12T07:59:30Z", "dev", "p"))).isEqualTo(LocationRecords.INVALID_FIX);
        assertThat(reason(query("lat", "45.5", "lon", "-73.6", "time", "2026-10-12T07:59:30Z")))
                .isEqualTo(LocationRecords.EXTERNAL_ID_INVALID);
    }

    @Test
    @DisplayName("a device key forces its id; another dev is 403 DEVICE_ENTITY_MISMATCH")
    void deviceBinding() {
        assertThat(format.parse(query("lat", "45.5", "lon", "-73.6", "time", "2026-10-12T07:59:30Z")
                .withBoundExternalId("dev-1"), source(), FormatOptions.empty()).records().get(0).externalId())
                .isEqualTo("dev-1");
        assertThatThrownBy(() -> format.parse(query("lat", "45.5", "lon", "-73.6", "dev", "pixel-1")
                .withBoundExternalId("dev-1"), source(), FormatOptions.empty()))
                .isInstanceOf(DeviceEntityMismatchException.class);
    }

    @Test
    @DisplayName("GET and POST, empty 200 reply; a JSON array body is a 400")
    void protocol() {
        assertThat(format.methods()).containsExactlyInAnyOrder(HttpMethod.GET, HttpMethod.POST);
        assertThat(format.pathKeyFriendly()).isTrue();
        NativeReply reply = format.nativeReply(ParsedIngest.empty());
        assertThat(reply.status()).isEqualTo(200);
        assertThat(reply.body()).isEmpty();
        assertThatThrownBy(() -> format.parse(json("[1,2]"), source(), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
    }

    private String reason(IngestRequest request) {
        ParsedIngest parsed = format.parse(request, source(), FormatOptions.empty());
        assertThat(parsed.records()).isEmpty();
        return parsed.rejections().stream().map(ItemRejection::reason).findFirst().orElseThrow();
    }
}
