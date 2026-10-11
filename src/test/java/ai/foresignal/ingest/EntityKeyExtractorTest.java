package ai.foresignal.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The default entity key extractor and its normalization rules. */
class EntityKeyExtractorTest {

    private static SourceIngestProfile profile() {
        return SourceIngestProfile.forDescriptor(
                IngestFixtures.aircraftDescriptor()
                        .entityKey("icao24", "hex")
                        .entityKey("registration", "r")
                        .entityKey("callsign", "flight")
                        .entityKey("mmsi", "mmsi")
                        .entityKey("imo", "imo")
                        .entityKey("tail-code", "tail")
                        .build());
    }

    private final EntityKeyExtractor extractor = DescriptorEntityKeyExtractor.INSTANCE;

    @Test
    @DisplayName("reads the mapped fields and normalizes each key type")
    void extracts() {
        Map<String, String> keys =
                extractor.extract(
                        profile(),
                        Map.of(
                                "hex", " A1B2C3 ",
                                "r", "n123ab",
                                "flight", "ual1  ",
                                "mmsi", 366123456L,
                                "imo", "IMO 9321483",
                                "tail", " Fin7 "));

        assertThat(keys)
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of(
                                "icao24", "a1b2c3",
                                "registration", "N123AB",
                                "callsign", "UAL1",
                                "mmsi", "366123456",
                                "imo", "9321483",
                                "tail-code", "Fin7"));
    }

    @Test
    @DisplayName("malformed or missing values are left out rather than guessed")
    void malformed() {
        Map<String, String> keys =
                extractor.extract(
                        profile(),
                        Map.of("hex", "~a1b2c3", "mmsi", "12345", "imo", "IMO 12", "r", "   ", "flight", List.of("x")));
        assertThat(keys).isEmpty();
        assertThat(DescriptorEntityKeyExtractor.normalize("icao24", "zzzzzz")).isNull();
        assertThat(DescriptorEntityKeyExtractor.normalize("icao24", null)).isNull();
    }

    @Test
    @DisplayName("empty when the source declares no keys or the payload is empty")
    void empty() {
        assertThat(extractor.extract(IngestFixtures.events(), Map.of("hex", "a1b2c3"))).isEmpty();
        assertThat(extractor.extract(profile(), Map.of())).isEmpty();
        assertThat(extractor.extract(profile(), null)).isEmpty();
        assertThat(extractor.extract(null, Map.of("hex", "a1b2c3"))).isEmpty();
    }

    @Test
    @DisplayName("the listener contract carries source, entity and keys")
    void listener() {
        StringBuilder seen = new StringBuilder();
        EntityKeyListener listener = (source, entity, keys) -> seen.append(source).append('/').append(entity).append(keys);
        listener.onEntityKeys("adsb", "a1b2c3", Map.of("icao24", "a1b2c3"));
        assertThat(seen).hasToString("adsb/a1b2c3{icao24=a1b2c3}");
    }
}
