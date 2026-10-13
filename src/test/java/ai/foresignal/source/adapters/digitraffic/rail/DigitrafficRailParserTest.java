package ai.foresignal.source.adapters.digitraffic.rail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * {@link DigitrafficRailParser} and the pure helpers of {@link DigitrafficRailAdapter} (prompt 080) on the real
 * captures: identity {@code departureDate:trainNumber}, the fix time as {@code observedAt}, km/h kept, the lookup's
 * fields joined (blank ones omitted), skipped entries, the cache round trip and staleness, the fieldSpecs contract
 * and a stable static hash.
 */
class DigitrafficRailParserTest {

    private static final DigitrafficRailAdapter ADAPTER = new DigitrafficRailAdapter("", () -> "1.2.3");

    private static DigitrafficRailParser.Locations latest() {
        return DigitrafficRailParser.parseLocations(AdapterFixtures.bytes("digitraffic-rail", "latest.json"));
    }

    private static DigitrafficRailParser.Enrichment lookup() {
        return DigitrafficRailParser.parseTrain(AdapterFixtures.bytes("digitraffic-rail", "train-2026-10-14-2241.json"));
    }

    private static DigitrafficRailParser.Position position(String externalId) {
        return latest().positions().stream().filter(p -> p.externalId().equals(externalId)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("latest: all 32 trains, identity departureDate:trainNumber, observedAt = timestamp, point and payload")
    void latestCapture() {
        DigitrafficRailParser.Locations locations = latest();
        assertThat(locations.positions()).hasSize(32);
        assertThat(locations.skipped()).isZero();

        NormalizedRecord record = DigitrafficRailParser.record(position("2026-10-14:2241"), null);
        assertThat(record.kind()).isEqualTo("train");
        assertThat(record.externalId()).isEqualTo("2026-10-14:2241");
        assertThat(record.observedAt()).isEqualTo(Instant.parse("2026-10-13T22:50:41Z"));
        Point point = (Point) record.geometry();
        assertThat(point.getX()).isEqualTo(27.301046);
        assertThat(point.getY()).isEqualTo(61.885707);
        assertThat(record.payload()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "trainNumber", 2241, "departureDate", "2026-10-14", "speedKph", 71.0, "gps", true, "accuracyM", 0));
    }

    @Test
    @DisplayName("the lookup adds trainType, trainCategory and operatorShortCode; the blank commuterLineID is omitted")
    void enrichment() {
        DigitrafficRailParser.Enrichment e = lookup();
        assertThat(e).isEqualTo(new DigitrafficRailParser.Enrichment("T", "Cargo", "vr", null));

        NormalizedRecord record = DigitrafficRailParser.record(position("2026-10-14:2241"), e);
        assertThat(record.payload()).containsEntry("trainType", "T").containsEntry("trainCategory", "Cargo")
                .containsEntry("operatorShortCode", "vr").doesNotContainKey("commuterLineID");
        assertThat(DigitrafficRailParser.parseTrain("[]".getBytes(StandardCharsets.UTF_8)))
                .isEqualTo(DigitrafficRailParser.Enrichment.NONE);
    }

    @Test
    @DisplayName("edge cases: no location, (0, 0), an unreadable timestamp and no train number are skipped and counted")
    void edges() {
        DigitrafficRailParser.Locations locations =
                DigitrafficRailParser.parseLocations(AdapterFixtures.bytes("digitraffic-rail", "latest-edge.json"));
        assertThat(locations.positions()).extracting(DigitrafficRailParser.Position::externalId)
                .containsExactly("2026-10-13:8555");
        assertThat(locations.skipped()).isEqualTo(4);
        assertThat(locations.positions().getFirst().accuracyM()).isEqualTo(12);
    }

    @Test
    @DisplayName("a body that is not a JSON array is a format error")
    void formatErrors() {
        assertThatThrownBy(() -> DigitrafficRailParser.parseLocations("{}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> DigitrafficRailParser.parseLocations("<html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> DigitrafficRailParser.parseTrain(new byte[0]))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("every payload key is declared with its type (the fieldSpecs contract), enriched or not")
    void fieldSpecs() {
        List<NormalizedRecord> records = new ArrayList<>();
        for (DigitrafficRailParser.Position p : latest().positions()) {
            records.add(DigitrafficRailParser.record(p, null));
        }
        records.add(DigitrafficRailParser.record(position("2026-10-14:2241"), lookup()));
        records.add(DigitrafficRailParser.record(position("2026-10-14:2241"),
                new DigitrafficRailParser.Enrichment("HL", "Commuter", "vr", "K")));
        FieldSpecContract.assertPayloadKeysDeclared(ADAPTER.descriptor(), records);
        assertThat(DescriptorValidator.problems(ADAPTER.descriptor(), ADAPTER)).isEmpty();
    }

    @Test
    @DisplayName("snapshots that differ in position, speed and accuracy share one static hash")
    void stableStaticHash() {
        DigitrafficRailParser.Position p = position("2026-10-14:2241");
        DigitrafficRailParser.Position moved = new DigitrafficRailParser.Position(p.trainNumber(), p.departureDate(),
                p.observedAt().plusSeconds(30), p.lon() + 0.01, p.lat() + 0.005, 80.0, true, 25);
        SnapshotContract.assertStableStaticHash(ADAPTER.descriptor(),
                List.of(DigitrafficRailParser.record(p, lookup()), DigitrafficRailParser.record(moved, lookup())));
    }

    @Test
    @DisplayName("URLs and headers: the fixed paths and Digitraffic-User: Foresignal/<version>")
    void urlsAndHeaders() {
        assertThat(ADAPTER.latestUri().toString())
                .isEqualTo("https://rata.digitraffic.fi/api/v1/train-locations/latest");
        assertThat(ADAPTER.trainUri("2026-10-14", 2241).toString())
                .isEqualTo("https://rata.digitraffic.fi/api/v1/trains/2026-10-14/2241");
        assertThat(ADAPTER.headers()).containsEntry("Digitraffic-User", "Foresignal/1.2.3");
        assertThat(new DigitrafficRailAdapter("http://localhost:1/", (java.util.function.Supplier<String>) null).latestUri().toString())
                .isEqualTo("http://localhost:1/api/v1/train-locations/latest");
    }

    @Test
    @DisplayName("the lookup cache round-trips, drops entries older than yesterday, and counts lookups per UTC day")
    void cache() {
        Map<String, DigitrafficRailParser.Enrichment> cache = new TreeMap<>();
        cache.put("2026-10-14:2241", lookup());
        cache.put("2026-10-13:8555", DigitrafficRailParser.Enrichment.NONE);
        String stored = DigitrafficRailAdapter.writeCache(cache);
        assertThat(DigitrafficRailAdapter.readCache(stored)).isEqualTo(cache);
        assertThat(DigitrafficRailAdapter.readCache("not json")).isEmpty();
        assertThat(DigitrafficRailAdapter.readCache(null)).isEmpty();

        LocalDate today = LocalDate.parse("2026-10-14");
        assertThat(DigitrafficRailAdapter.isStale("2026-10-13:8555", today)).isFalse();
        assertThat(DigitrafficRailAdapter.isStale("2026-10-12:8555", today)).isTrue();
        assertThat(DigitrafficRailAdapter.isStale("garbage", today)).isTrue();

        assertThat(DigitrafficRailAdapter.lookupsToday("2026-10-14:17", today)).isEqualTo(17);
        assertThat(DigitrafficRailAdapter.lookupsToday("2026-10-13:199", today)).isZero();
        assertThat(DigitrafficRailAdapter.lookupsToday(null, today)).isZero();
        assertThat(DigitrafficRailAdapter.lookupsToday("2026-10-14:x", today)).isZero();
    }
}
