package ai.foresignal.source.adapters.space.satnogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link SatnogsParser} on 20 real online stations of 2026-10-13 (owners redacted), DRF pages and {@code Link}
 * headers.
 */
class SatnogsParserTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("the fixture: 20 stations with points, antennas as band/type pairs, observations left out")
    void fixture() {
        SatnogsParser.Page page = SatnogsParser.parse(AdapterFixtures.bytes("satnogs-stations", "stations-online.json"));
        assertThat(page.records()).hasSize(20).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("ground-station");
            assertThat(r.geometry()).isNotNull();
            assertThat(r.payload()).containsEntry("status", "Online")
                    .doesNotContainKeys("observations", "owner", "description", "image");
        });
        assertThat(page.next()).isNull();
        NormalizedRecord first = page.records().getFirst();
        assertThat(first.externalId()).isEqualTo("26");
        assertThat(first.geometry().getCoordinate().x).isEqualTo(21.828);
        assertThat(first.geometry().getCoordinate().y).isEqualTo(38.395);
        assertThat(first.payload()).containsEntry("name", "SV1IYO/A").containsEntry("altitudeM", 145L)
                .containsEntry("qthLocator", "KM08vj").containsEntry("clientVersion", "2.1.2")
                .containsEntry("lastSeen", "2026-10-13T20:49:43Z")
                .containsEntry("antennas", List.of(Map.of("band", "VHF", "type", "turnstile"),
                        Map.of("band", "UHF", "type", "turnstile")));
        FieldSpecContract.assertPayloadKeysDeclared(new SatnogsStationsAdapter("").descriptor(), page.records());
    }

    @Test
    @DisplayName("a DRF page: results and next; a station without an id is skipped; repeated antennas collapse")
    void drfPage() {
        String body = """
                {"count": 3, "next": "https://network.satnogs.org/api/stations/?page=2&status=online", "results": [
                 {"id": 7, "name": "A", "lat": 10.0, "lng": 20.0, "antenna": [
                   {"band": "UHF", "antenna_type": "yagi"}, {"band": "UHF", "antenna_type": "yagi"}]},
                 {"name": "no id"},
                 {"id": 7, "name": "A again"}]}""";
        SatnogsParser.Page page = SatnogsParser.parse(body.getBytes(StandardCharsets.UTF_8));
        assertThat(page.next()).isEqualTo("https://network.satnogs.org/api/stations/?page=2&status=online");
        assertThat(page.records()).hasSize(1);
        assertThat(page.records().getFirst().payload()).containsEntry("antennas",
                List.of(Map.of("band", "UHF", "type", "yagi")));
        assertThat(page.skipped()).containsEntry(MappedRecord.MISSING_ID, 1).containsEntry(MappedRecord.DUPLICATE_ID, 1);
        assertThatThrownBy(() -> SatnogsParser.parse("{\"detail\": \"x\"}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> SatnogsParser.parse("<html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("Link headers: rel=next among several relations, quoted or not; none without next")
    void linkHeader() {
        assertThat(SatnogsParser.nextLink("<https://network.satnogs.org/api/stations/?page=2>; rel=\"next\","
                + " <https://network.satnogs.org/api/stations/?page=9>; rel=\"last\""))
                .isEqualTo(URI.create("https://network.satnogs.org/api/stations/?page=2"));
        assertThat(SatnogsParser.nextLink("<https://x.example/p?page=1>; rel=\"prev\", <https://x.example/p?page=3>;"
                + " rel=next")).isEqualTo(URI.create("https://x.example/p?page=3"));
        assertThat(SatnogsParser.nextLink("<https://x.example/p?page=1>; rel=\"prev\"")).isNull();
        assertThat(SatnogsParser.nextLink(null)).isNull();
    }

    @Test
    @DisplayName("lastSeen is volatile: a station seen again later keeps its static hash")
    void snapshot() {
        ObjectNode station = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("satnogs-stations", "stations-online.json"))
                .get(0);
        NormalizedRecord a = SatnogsParser.station(station).record();
        station.put("last_seen", "2026-10-13T23:00:00Z");
        NormalizedRecord b = SatnogsParser.station(station).record();
        assertThat(a.payload().get("lastSeen")).isNotEqualTo(b.payload().get("lastSeen"));
        SatnogsStationsAdapter adapter = new SatnogsStationsAdapter("");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(), List.of(a, b));
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().volatileFields()).containsExactly("lastSeen");
        assertThat(adapter.descriptor().fetchTimeout()).isEqualTo(Duration.ofSeconds(120));
        assertThat(adapter.descriptor().defaultInterval()).isEqualTo(Duration.ofHours(6));
        assertThat(adapter.descriptor().observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(adapter.stationsUri().toString()).isEqualTo("https://network.satnogs.org/api/stations/?status=online");
    }
}
