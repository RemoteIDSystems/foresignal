package ai.foresignal.source.adapters.ebird;

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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link EbirdParser} against a trimmed eBird notable response (prompt 095). */
class EbirdParserTest {

    private static final PlaceTimeZones CHICAGO = (lon, lat) -> Optional.of(ZoneId.of("America/Chicago"));
    private static final byte[] FIXTURE = AdapterFixtures.bytes("ebird-notable", "notable.json");

    private static NormalizedRecord bySubId(List<NormalizedRecord> records, String subId) {
        return records.stream().filter(r -> r.externalId().equals("ebird:" + subId + ":")).findFirst()
                .or(() -> records.stream().filter(r -> r.externalId().startsWith("ebird:" + subId + ":")).findFirst())
                .orElseThrow();
    }

    @Test
    @DisplayName("parses the array and declares every payload key")
    void parses() {
        EbirdParser.Result result = EbirdParser.parse(FIXTURE, CHICAGO);
        assertThat(result.rows()).isEqualTo(3);
        assertThat(result.records()).hasSize(3);
        FieldSpecContract.assertPayloadKeysDeclared(
                new EbirdNotableAdapter("", () -> null, PlaceTimeZones.none()).descriptor(), result.records());
    }

    @Test
    @DisplayName("the local obsDt becomes an instant through the 040 timezone, and observedAt is left to the framework")
    void localTimeToUtc() {
        NormalizedRecord record = bySubId(EbirdParser.parse(FIXTURE, CHICAGO).records(), "S111111");
        assertThat(record.eventTime()).isEqualTo(Instant.parse("2026-09-01T13:30:00Z"));
        assertThat(record.observedAt()).isNull();
        assertThat(record.payload().get("obsDtLocal")).isEqualTo("2026-09-01 08:30");
    }

    @Test
    @DisplayName("a private location is rounded, flagged 5000 m and has no locName")
    void privateLocation() {
        NormalizedRecord record = bySubId(EbirdParser.parse(FIXTURE, CHICAGO).records(), "S222222");
        assertThat(record.payload()).doesNotContainKey("locName");
        assertThat(record.quality().accuracyMeters()).isEqualTo(5000.0);
        assertThat(record.geometry().getCoordinate().getX()).isEqualTo(-97.75);
        assertThat(record.geometry().getCoordinate().getY()).isEqualTo(30.30);
    }

    @Test
    @DisplayName("a public location keeps its name and exact coordinates")
    void publicLocation() {
        NormalizedRecord record = bySubId(EbirdParser.parse(FIXTURE, CHICAGO).records(), "S111111");
        assertThat(record.payload().get("locName")).isEqualTo("Hornsby Bend");
        assertThat(record.geometry().getCoordinate().getX()).isEqualTo(-97.7431);
    }

    @Test
    @DisplayName("an absent howMany is omitted from the payload")
    void absentHowMany() {
        NormalizedRecord record = bySubId(EbirdParser.parse(FIXTURE, CHICAGO).records(), "S333333");
        assertThat(record.payload()).doesNotContainKey("howMany");
    }

    @Test
    @DisplayName("a non-array body is a format change")
    void formatChange() {
        assertThatThrownBy(() -> EbirdParser.parse("{}".getBytes(StandardCharsets.UTF_8), CHICAGO))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
