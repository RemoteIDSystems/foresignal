package ai.foresignal.source.adapters.humanitarian;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code nasa-eonet} parsing (prompt 090): JSON detection by first byte, latest geometry, payload. */
class EonetParserTest {

    private static final EonetAdapter ADAPTER = new EonetAdapter("");

    @Test
    @DisplayName("the captured events parse to natural-event records with geometry")
    void events() {
        List<NormalizedRecord> records = ADAPTER.parse(AdapterFixtures.bytes("nasa-eonet", "events.json"));
        assertThat(records).isNotEmpty();
        assertThat(records).extracting(NormalizedRecord::kind).containsOnly("natural-event");
        assertThat(records).anyMatch(r -> r.geometry() != null);
        NormalizedRecord first = records.get(0);
        assertThat(first.externalId()).isNotBlank();
        assertThat(first.payload()).containsKeys("title", "categories", "sources", "trackPoints");
    }

    @Test
    @DisplayName("the latest geometry wins; a polygon event keeps its Polygon")
    void latestGeometry() {
        NormalizedRecord record = ADAPTER.parse(AdapterFixtures.bytes("nasa-eonet", "polygon.json")).get(0);
        assertThat(record.geometry().getGeometryType()).isEqualTo("Polygon");
        assertThat(record.eventTime()).isBefore(record.observedAt());
        assertThat(record.payload()).containsEntry("trackPoints", 2).containsEntry("magnitudeUnit", "acres");
    }

    @Test
    @DisplayName("JSON is detected by the first non-space byte, not the content type")
    void sniffsJson() {
        assertThat(EonetAdapter.looksJson("{\"events\":[]}".getBytes(StandardCharsets.UTF_8))).isTrue();
        assertThat(EonetAdapter.looksJson("  <?xml".getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(EonetAdapter.looksJson(new byte[0])).isFalse();
    }
}
