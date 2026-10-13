package ai.foresignal.source.adapters.env.radnet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link RadnetCsvParser} and {@link RadnetStations} on the 2026-10-13 Denver, Honolulu and Hagåtña CSVs: the newest
 * row, the collection time read as UTC, an empty dose column, a header-only file and the bundled station list.
 */
class RadnetCsvParserTest {

    static RadnetCsvParser.Row denver() {
        return RadnetCsvParser.newest(AdapterFixtures.bytes("epa-radnet", "CO_DENVER_2026.csv")).orElseThrow();
    }

    @Test
    @DisplayName("Denver: the last row, 20:41 UTC, no dose, eight bands summing to 4866 CPM")
    void lastRowEmptyDose() {
        RadnetCsvParser.Row row = denver();
        assertThat(row.time()).isEqualTo(Instant.parse("2026-10-13T20:41:00Z"));
        assertThat(row.doseRateNsvH()).isNull();
        assertThat(row.bands()).containsExactly(Map.entry("R02", 2421.0), Map.entry("R03", 1403.0),
                Map.entry("R04", 419.0), Map.entry("R05", 203.0), Map.entry("R06", 130.0), Map.entry("R07", 156.0),
                Map.entry("R08", 92.0), Map.entry("R09", 42.0));
        assertThat(row.grossCpm()).isEqualTo(4866.0);
        assertThat(row.status()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("Honolulu: the dose column is read; the record carries the station's bundled point")
    void record() {
        RadnetCsvParser.Row row = RadnetCsvParser.newest(AdapterFixtures.bytes("epa-radnet", "HI_HONOLULU_2026.csv"))
                .orElseThrow();
        assertThat(row.time()).as("UTC, not Hawaii standard time").isEqualTo(Instant.parse("2026-10-13T20:19:00Z"));
        assertThat(row.doseRateNsvH()).isEqualTo(26.0);
        assertThat(row.grossCpm()).isEqualTo(1474.0);
        RadnetStations.Station station = RadnetStations.find("HI/HONOLULU").orElseThrow();
        MappedRecord m = RadnetCsvParser.record(station, row);
        NormalizedRecord r = m.record();
        assertThat(r.externalId()).isEqualTo("HI/HONOLULU");
        assertThat(r.kind()).isEqualTo("radiation");
        assertThat(r.observedAt()).isEqualTo(row.time());
        assertThat(r.eventTime()).isEqualTo(row.time());
        assertThat(r.geometry().getCoordinate().x).isEqualTo(station.lon());
        assertThat(r.payload()).containsEntry("doseRateNsvH", 26.0).containsEntry("grossCpm", 1474.0)
                .containsEntry("status", "APPROVED").containsKey("bands");
        NormalizedRecord denver = RadnetCsvParser.record(RadnetStations.find("CO/DENVER").orElseThrow(), denver())
                .record();
        assertThat(denver.payload()).doesNotContainKey("doseRateNsvH");
        FieldSpecContract.assertPayloadKeysDeclared(new EpaRadnetAdapter("", () -> null).descriptor(), List.of(r, denver));
    }

    @Test
    @DisplayName("a header-only file has no newest row; a file without the time column is a format error")
    void emptyAndBroken() {
        assertThat(RadnetCsvParser.newest(AdapterFixtures.bytes("epa-radnet", "GU_HAGATNA_2026.csv"))).isEmpty();
        assertThatThrownBy(() -> RadnetCsvParser.newest("A,B\n1,2\n".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> RadnetCsvParser.newest(new byte[0])).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("rows out of order: the newest time wins; unreadable times and values are ignored")
    void ordering() {
        String csv = "LOCATION_NAME,SAMPLE COLLECTION TIME,DOSE EQUIVALENT RATE (nSv/h),GAMMA COUNT RATE R02 (CPM),STATUS\n"
                + "X: Y,10/13/2026 21:00:00,,100.00,PRELIMINARY\n"
                + "X: Y,10/13/2026 19:00:00,5.00,90.00,APPROVED\n"
                + "X: Y,13/45/2026 25:00:00,,1.00,APPROVED\n"
                + "\"X: Y\",10/13/2026 20:00:00,abc,95.00,APPROVED\n";
        RadnetCsvParser.Row row = RadnetCsvParser.newest(csv.getBytes(StandardCharsets.UTF_8)).orElseThrow();
        assertThat(row.time()).isEqualTo(Instant.parse("2026-10-13T21:00:00Z"));
        assertThat(row.status()).isEqualTo("PRELIMINARY");
        assertThat(row.bands()).containsExactly(Map.entry("R02", 100.0));
        assertThat(RadnetCsvParser.split("\"a,b\",c,\"d\"\"e\"")).containsExactly("a,b", "c", "d\"e");
    }

    @Test
    @DisplayName("stations: 132 bundled, found case- and space-insensitively; URLs encode spaces")
    void stations() {
        assertThat(RadnetStations.all()).hasSize(132);
        assertThat(RadnetStations.find(" ca / los angeles ")).hasValueSatisfying(s -> {
            assertThat(s.id()).isEqualTo("CA/LOS ANGELES");
            assertThat(s.state()).isEqualTo("CA");
            assertThat(s.city()).isEqualTo("LOS ANGELES");
        });
        assertThat(RadnetStations.find("GU/HAGATNA")).isEmpty();
        assertThat(new EpaRadnetAdapter("", () -> null).csvUri(RadnetStations.find("NY/NYC (EML)").orElseThrow(), 2026))
                .hasToString("https://radnet.epa.gov/cdx-radnet-rest/api/rest/csv/2026/fixed/NY/NYC%20(EML)");
    }

    @Test
    @DisplayName("checkParams: unknown stations and more than 10 are refused")
    void checkParams() {
        EpaRadnetAdapter adapter = new EpaRadnetAdapter("", () -> null);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("stations", List.of("CO/DENVER", "hi/honolulu")))))
                .isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("stations", List.of("CO/DENVER", "XX/NOWHERE")))))
                .containsKey("stations");
        List<String> eleven = RadnetStations.all().keySet().stream().limit(11).toList();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("stations", eleven)))).containsKey("stations");
    }

    @Test
    @DisplayName("descriptor valid, stations required; snapshot contract across an hour")
    void descriptorAndSnapshot() {
        EpaRadnetAdapter adapter = new EpaRadnetAdapter("", () -> null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().params()).filteredOn(p -> p.name().equals("stations"))
                .singleElement().satisfies(p -> assertThat(p.required()).isTrue());
        assertThat(adapter.descriptor().recordTtl()).isEqualTo(Duration.ofHours(24));
        assertThat(adapter.descriptor().defaultInterval()).isEqualTo(Duration.ofHours(3));
        NormalizedRecord r = RadnetCsvParser.record(RadnetStations.find("CO/DENVER").orElseThrow(), denver()).record();
        SnapshotContract.assertStableStaticHash(adapter.descriptor(), List.of(r, EnvTestSupport.later(r, Duration.ofHours(1))));
    }
}
