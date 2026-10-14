package ai.foresignal.source.adapters.ourairports;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link OurAirportsParser} contract (prompt 094). */
class OurAirportsParserTest {

    private final SourceDescriptor descriptor = new OurAirportsAdapter("").descriptor();

    @Test
    @DisplayName("the default types keep large/medium/small airports and drop heliports and closed fields")
    void typeFilter() {
        Set<String> types = OurAirportsParser.types(OurAirportsParser.DEFAULT_TYPES);
        List<NormalizedRecord> records =
                OurAirportsParser.parseAirportsAll(AdapterFixtures.bytes(OurAirportsAdapter.ID, "airports.csv"),
                        types);

        assertThat(records).extracting(NormalizedRecord::externalId)
                .containsExactlyInAnyOrder("KJFK", "EGLL", "LFPG", "ZZZZ");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);

        NormalizedRecord jfk = records.stream().filter(r -> r.externalId().equals("KJFK")).findFirst().orElseThrow();
        assertThat(jfk.payload()).containsEntry("name", "John F Kennedy International Airport")
                .containsEntry("type", "large_airport")
                .containsEntry("elevationFt", 13L)
                .containsEntry("isoCountry", "US")
                .containsEntry("isoRegion", "US-NY")
                .containsEntry("municipality", "New York")
                .containsEntry("scheduledService", "yes")
                .containsEntry("gpsCode", "KJFK")
                .containsEntry("iataCode", "JFK")
                .containsEntry("localCode", "JFK");
        assertThat(jfk.geometry()).isNotNull();

        NormalizedRecord noCoords =
                records.stream().filter(r -> r.externalId().equals("ZZZZ")).findFirst().orElseThrow();
        assertThat(noCoords.geometry()).isNull();
    }

    @Test
    @DisplayName("adding a type includes it")
    void typeFilterIncludes() {
        Set<String> types = OurAirportsParser.types("heliport");
        List<NormalizedRecord> records =
                OurAirportsParser.parseAirportsAll(AdapterFixtures.bytes(OurAirportsAdapter.ID, "airports.csv"),
                        types);
        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly("KFFO");
    }

    @Test
    @DisplayName("navaids parse with a prefixed externalId and declared keys")
    void navaids() {
        List<NormalizedRecord> records =
                OurAirportsParser.parseNavaidsAll(AdapterFixtures.bytes(OurAirportsAdapter.ID, "navaids.csv"));
        assertThat(records).hasSize(2);
        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly("navaid:1", "navaid:2");
        assertThat(records.get(0).kind()).isEqualTo(OurAirportsParser.NAVAID_KIND);
        assertThat(records.get(0).payload()).containsEntry("name", "KENNEDY VOR-DME")
                .containsEntry("frequencyKhz", 11590L)
                .containsEntry("associatedAirport", "KJFK");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);
    }
}
