package ai.foresignal.source.adapters.inat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PlaceTimeZones;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link InatParser} against a trimmed iNaturalist v1 response (prompt 095). */
class InatParserTest {

    private static final PlaceTimeZones NONE = PlaceTimeZones.none();
    private static final byte[] FIXTURE = AdapterFixtures.bytes("inat-observations", "observations.json");

    private static InatParser.Options defaults() {
        return new InatParser.Options("research", List.of(), true, false, NONE);
    }

    private static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals("inat:" + id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("keeps research observations with an open licence and drops private, unlicensed, captive and placeless rows")
    void keepsAndSkips() {
        InatParser.Result result = InatParser.parse(FIXTURE, defaults());
        assertThat(result.rows()).isEqualTo(7);
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .containsExactlyInAnyOrder("inat:1001", "inat:1002", "inat:1006");
        assertThat(result.skipped()).isEqualTo(4);
        FieldSpecContract.assertPayloadKeysDeclared(new InatObservationsAdapter("", NONE, () -> null).descriptor(),
                result.records());
    }

    @Test
    @DisplayName("an obscured observation carries the upstream accuracy and never a refined position")
    void obscured() {
        NormalizedRecord record = byId(InatParser.parse(FIXTURE, defaults()).records(), "1002");
        assertThat(record.payload().get("obscured")).isEqualTo(true);
        assertThat(record.quality().accuracyMeters()).isEqualTo(200.0);
        assertThat(record.geometry()).isNotNull();
        assertThat(record.geometry().getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("the time base is updated_at for observedAt and time_observed_at for eventTime")
    void timeBase() {
        NormalizedRecord record = byId(InatParser.parse(FIXTURE, defaults()).records(), "1001");
        assertThat(record.observedAt()).isEqualTo(Instant.parse("2026-09-02T12:00:00Z"));
        assertThat(record.eventTime()).isEqualTo(Instant.parse("2026-09-01T14:30:00Z"));
    }

    @Test
    @DisplayName("a date-only observed_on is midnight in the 040 timezone of the point")
    void dateOnlyUsesTimezone() {
        String json = "{\"results\":[{\"id\":42,\"observed_on\":\"2026-09-01\",\"updated_at\":\"2026-09-02T00:00:00Z\","
                + "\"quality_grade\":\"research\",\"license_code\":\"cc0\",\"geojson\":{\"type\":\"Point\","
                + "\"coordinates\":[-87.6,41.8]},\"taxon\":{\"id\":1,\"name\":\"x\",\"iconic_taxon_name\":\"Plantae\"}}]}";
        PlaceTimeZones chicago = (lon, lat) -> java.util.Optional.of(ZoneId.of("America/Chicago"));
        InatParser.Result result = InatParser.parse(json.getBytes(StandardCharsets.UTF_8),
                new InatParser.Options("research", List.of(), true, false, chicago));
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).eventTime()).isEqualTo(Instant.parse("2026-09-01T05:00:00Z"));
    }

    @Test
    @DisplayName("photoUrl is present only on the open-data host with an open licence")
    void photoHostRule() {
        List<NormalizedRecord> records = InatParser.parse(FIXTURE, defaults()).records();
        assertThat(byId(records, "1001").payload().get("photoUrl"))
                .isEqualTo("https://inaturalist-open-data.s3.amazonaws.com/photos/1001/square.jpg");
        assertThat(byId(records, "1006").payload()).doesNotContainKey("photoUrl");
    }

    @Test
    @DisplayName("no contributor identity, description or comment is ever stored")
    void noUserFields() {
        for (NormalizedRecord record : InatParser.parse(FIXTURE, defaults()).records()) {
            assertThat(record.payload().keySet()).allSatisfy(key ->
                    assertThat(key.toLowerCase(Locale.ROOT)).doesNotContain("user", "login", "description", "comment"));
        }
    }

    @Test
    @DisplayName("the licensedOnly and includeCaptive flags change which rows are kept")
    void flags() {
        InatParser.Result unlicensed = InatParser.parse(FIXTURE,
                new InatParser.Options("research", List.of(), false, false, NONE));
        assertThat(unlicensed.records()).extracting(NormalizedRecord::externalId).contains("inat:1004");
        InatParser.Result captive = InatParser.parse(FIXTURE,
                new InatParser.Options("research", List.of(), true, true, NONE));
        assertThat(captive.records()).extracting(NormalizedRecord::externalId).contains("inat:1005");
    }

    @Test
    @DisplayName("a non-envelope body is a format change")
    void formatChange() {
        assertThatThrownBy(() -> InatParser.parse("{\"nope\":[]}".getBytes(StandardCharsets.UTF_8), defaults()))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("geometry is always a 2D point (no Z coordinate)")
    void geometryIsTwoDimensional() {
        for (NormalizedRecord record : InatParser.parse(FIXTURE, defaults()).records()) {
            assertThat(record.geometry().getCoordinates()[0].getZ()).isNaN();
        }
    }
}
