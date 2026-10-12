package ai.foresignal.inbound.formats.location;

import static ai.foresignal.inbound.formats.location.LocationTestSupport.NOW;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.fixture;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.json;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.options;
import static ai.foresignal.inbound.formats.location.LocationTestSupport.source;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.inbound.format.FormatOptions;
import ai.foresignal.inbound.format.ItemRejection;
import ai.foresignal.inbound.format.NativeReply;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** {@link OwnTracksFormat} (prompt 039). */
class OwnTracksFormatTest {

    private final OwnTracksEncryptionHealthRule health = new OwnTracksEncryptionHealthRule();
    private final OwnTracksFormat format = new OwnTracksFormat(LocationTestSupport.JSON, health);

    private static HttpHeaders limitHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Limit-U", "alex");
        headers.add("X-Limit-D", "phone");
        return headers;
    }

    @Test
    @DisplayName("a location maps every documented field; externalId is X-Limit-U/X-Limit-D")
    void locationMapping() {
        ParsedIngest parsed = format.parse(json(fixture("owntracks-location.json"), limitHeaders()), source(),
                FormatOptions.empty());

        assertThat(parsed.rejections()).isEmpty();
        NormalizedRecord r = parsed.records().get(0);
        assertThat(r.externalId()).isEqualTo("alex/phone");
        assertThat(r.kind()).isEqualTo("tracker");
        assertThat(r.observedAt()).isEqualTo(Instant.ofEpochSecond(1760256000L));
        assertThat(r.geometry().getCoordinate().y).isEqualTo(45.5);
        assertThat(r.geometry().getCoordinate().x).isEqualTo(-73.6);
        assertThat(r.quality().accuracyMeters()).isEqualTo(12.0);
        assertThat((Double) r.payload().get("speedMs")).isCloseTo(10.0, within(1e-9));
        assertThat(r.payload()).containsEntry("courseDeg", 90.0).containsEntry("altitudeM", 40.0)
                .containsEntry("battery", 81.0).containsEntry("charging", true)
                .containsEntry("conn", "w").containsEntry("tid", "ph");
    }

    @Test
    @DisplayName("without X-Limit headers the tracker id names the entity; one header alone is used as is")
    void tidFallback() {
        ParsedIngest parsed = format.parse(json(fixture("owntracks-location.json")), source(), FormatOptions.empty());
        assertThat(parsed.records().get(0).externalId()).isEqualTo("ph");

        HttpHeaders user = new HttpHeaders();
        user.add("X-Limit-U", "alex");
        assertThat(format.parse(json(fixture("owntracks-location.json"), user), source(), FormatOptions.empty())
                .records().get(0).externalId()).isEqualTo("alex");
    }

    @Test
    @DisplayName("X-Foresignal-External-Id overrides the derived id")
    void headerOverride() {
        HttpHeaders headers = limitHeaders();
        headers.add(LocationRecords.EXTERNAL_ID_HEADER, "family-phone");
        assertThat(format.parse(json(fixture("owntracks-location.json"), headers), source(), FormatOptions.empty())
                .records().get(0).externalId()).isEqualTo("family-phone");
    }

    @Test
    @DisplayName("a batch: transition record, ignored types, encrypted, low accuracy and (0,0) rejections")
    void batch() {
        ParsedIngest parsed = format.parse(json(fixture("owntracks-batch.json"), limitHeaders()), source(),
                FormatOptions.empty());

        assertThat(parsed.items()).isEqualTo(9);
        assertThat(parsed.records()).hasSize(2);
        NormalizedRecord transition = parsed.records().get(1);
        assertThat(transition.kind()).isEqualTo("tracker-transition");
        assertThat(transition.externalId()).isEqualTo("alex/phone" + OwnTracksFormat.TRANSITION_SUFFIX);
        assertThat(transition.payload()).containsEntry("event", "enter").containsEntry("desc", "Office")
                .containsEntry("wtst", Instant.ofEpochSecond(1760000000L).toString())
                .containsEntry("entity", "alex/phone");
        assertThat(transition.observedAt()).isEqualTo(Instant.ofEpochSecond(1760256060L));
        assertThat(parsed.rejections()).extracting(ItemRejection::index, ItemRejection::reason).containsExactly(
                org.assertj.core.groups.Tuple.tuple(6, OwnTracksFormat.ENCRYPTED_UNSUPPORTED),
                org.assertj.core.groups.Tuple.tuple(7, LocationRecords.LOW_ACCURACY),
                org.assertj.core.groups.Tuple.tuple(8, LocationRecords.NULL_ISLAND));
    }

    @Test
    @DisplayName("an encrypted payload degrades the source with the documented warning for 24 hours")
    void encryptedHealthWarning() {
        format.parse(json("{\"_type\":\"encrypted\",\"data\":\"eA==\"}"), source(), FormatOptions.empty());

        assertThat(health.lastEncrypted("custom-family")).contains(NOW);
        HealthRuleContext context = new HealthRuleContext(source(), null, NOW.plusSeconds(60));
        assertThat(health.evaluate(context)).hasValueSatisfying(v -> {
            assertThat(v.status()).isEqualTo(HealthStatus.DEGRADED);
            assertThat(v.reason()).isEqualTo("OwnTracks payload encryption is not supported; disable it");
        });
        assertThat(health.evaluate(new HealthRuleContext(source(), null, NOW.plus(OwnTracksEncryptionHealthRule.WINDOW)
                .plusSeconds(1)))).isEmpty();
    }

    @Test
    @DisplayName("the reply is always 200 application/json []")
    void reply() {
        NativeReply reply = format.nativeReply(ParsedIngest.empty());
        assertThat(reply.status()).isEqualTo(200);
        assertThat(reply.contentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(reply.body()).isEqualTo("[]");
    }

    @Test
    @DisplayName("maxAccuracyMeters is a format option")
    void accuracyOption() {
        ParsedIngest parsed = format.parse(json(fixture("owntracks-location.json")), source(),
                options("{\"maxAccuracyMeters\":5}"));
        assertThat(parsed.records()).isEmpty();
        assertThat(parsed.rejections()).extracting(ItemRejection::reason).containsExactly(LocationRecords.LOW_ACCURACY);
        assertThat(format.options()).extracting(o -> o.name()).contains("maxAccuracyMeters");
    }

    @Test
    @DisplayName("a device key's bound id replaces the derived id without a mismatch")
    void deviceBinding() {
        ParsedIngest parsed = format.parse(json(fixture("owntracks-batch.json"), limitHeaders())
                .withBoundExternalId("dev-1"), source(), FormatOptions.empty());
        assertThat(parsed.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("dev-1", "dev-1" + OwnTracksFormat.TRANSITION_SUFFIX);
    }

    @Test
    @DisplayName("bs maps unplugged to false, charging and full to true, unknown to null")
    void batteryStatus() {
        assertThat(OwnTracksFormat.charging(0d)).isNull();
        assertThat(OwnTracksFormat.charging(1d)).isFalse();
        assertThat(OwnTracksFormat.charging(2d)).isTrue();
        assertThat(OwnTracksFormat.charging(3d)).isTrue();
        assertThat(OwnTracksFormat.charging(null)).isNull();
    }

    @Test
    @DisplayName("missing _type, missing tst, an undeclared transition kind and bad JSON")
    void invalid() {
        ParsedIngest parsed = format.parse(json("[{\"lat\":1},{\"_type\":\"location\",\"lat\":45.5,\"lon\":-73.6,\"tid\":\"ph\"}, 7]"),
                source(), FormatOptions.empty());
        assertThat(parsed.rejections()).extracting(ItemRejection::reason).containsExactly(
                LocationRecords.INVALID_ITEM, LocationRecords.MISSING_OBSERVED_AT, LocationRecords.INVALID_ITEM);

        ParsedIngest transition = format.parse(json("{\"_type\":\"transition\",\"tst\":1760256000,\"tid\":\"ph\"}"),
                LocationTestSupport.trackerOnly(), FormatOptions.empty());
        assertThat(transition.rejections()).extracting(ItemRejection::reason).containsExactly(LocationRecords.UNKNOWN_KIND);

        ParsedIngest noId = format.parse(json("{\"_type\":\"location\",\"lat\":45.5,\"lon\":-73.6,\"tst\":1760256000}"),
                source(), FormatOptions.empty());
        assertThat(noId.rejections()).extracting(ItemRejection::reason).containsExactly(LocationRecords.EXTERNAL_ID_INVALID);

        assertThatThrownBy(() -> format.parse(json("{not json"), source(), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> format.parse(json("\"text\""), source(), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
    }
}
