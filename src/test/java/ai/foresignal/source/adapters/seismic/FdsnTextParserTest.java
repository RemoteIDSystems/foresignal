package ai.foresignal.source.adapters.seismic;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link FdsnTextParser} (prompt 089): the pipe-delimited FDSN text format, for both reviewed services, with
 * empty fields and a header-only body.
 */
class FdsnTextParserTest {

    @Test
    @DisplayName("GEOFON text maps to earthquake records with payload and geometry")
    void geofon() {
        List<NormalizedRecord> records = FdsnTextParser.parse(AdapterFixtures.bytes("fdsn-geofon", "events.txt"));
        assertThat(records).isNotEmpty();
        NormalizedRecord first = records.get(0);
        assertThat(first.kind()).isEqualTo("earthquake");
        assertThat(first.externalId()).isNotBlank();
        assertThat(first.eventTime()).isNotNull();
        assertThat(first.observedAt()).as("RECEIVED source leaves observedAt null").isNull();
        assertThat(first.geometry()).isNotNull();
        assertThat(first.payload()).containsKeys("mag", "magType", "place", "depthKm", "evtype");
    }

    @Test
    @DisplayName("INGV text maps the same way despite the Depth/Km spelling and the EventType column")
    void ingv() {
        List<NormalizedRecord> records = FdsnTextParser.parse(AdapterFixtures.bytes("fdsn-ingv", "events.txt"));
        assertThat(records).isNotEmpty();
        assertThat(records.get(0).payload()).containsKeys("mag", "depthKm", "place");
        assertThat(records.get(0).geometry()).isNotNull();
    }

    @Test
    @DisplayName("a header-only body is empty, empty fields are dropped, and a missing temperature row is skipped")
    void edgeCases() {
        String header = "#EventID|Time|Latitude|Longitude|Depth/km|Author|Catalog|Contributor|ContributorID|"
                + "MagType|Magnitude|MagAuthor|EventLocationName|EventType\n";
        assertThat(FdsnTextParser.parse(header.getBytes(StandardCharsets.UTF_8))).isEmpty();

        // Aligned to the header: MagType at column 9, EventLocationName at column 12.
        String row = "ev1|2026-10-14T10:00:00|1.0|2.0||||||ML|||Place|earthquake\n";
        List<NormalizedRecord> records = FdsnTextParser.parse((header + row).getBytes(StandardCharsets.UTF_8));
        assertThat(records).hasSize(1);
        assertThat(records.get(0).payload()).containsEntry("place", "Place")
                .doesNotContainKey("mag").containsEntry("magType", "ML");

        String unusable = "bad|not-a-time|1.0|2.0|||||||Place|earthquake\n";
        assertThat(FdsnTextParser.parse((header + unusable).getBytes(StandardCharsets.UTF_8))).isEmpty();
    }
}
