package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link MeshtasticMapParser} (prompt 083): the trimmed real node list, and a 10,000-node synthetic file read as a stream
 * (records arrive while most of the file is still unread, so no tree of the whole document is built).
 */
class MeshtasticMapParserTest {

    @TempDir Path dir;

    private static List<NormalizedRecord> fixture() {
        List<NormalizedRecord> records = new ArrayList<>();
        MeshtasticMapParser.Counts counts = MeshtasticMapParser.parse(new ByteArrayInputStream(
                AdapterFixtures.bytes("meshtastic-map", "nodes-200.json")), records::add);
        assertThat(counts).isEqualTo(new MeshtasticMapParser.Counts(200, 0));
        return records;
    }

    @Test
    @DisplayName("the trimmed real list: 200 nodes, 138 positioned, unsigned ids, names and the ×1e-7 scaling")
    void trimmedFixture() {
        List<NormalizedRecord> records = fixture();
        assertThat(records).hasSize(200).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(MeshRecords.NODE_KIND);
            assertThat(r.externalId()).matches("^![0-9a-f]{8}$");
            assertThat(r.observedAt()).isNotNull();
            assertThat(r.payload()).containsEntry("network", "meshtastic");
            assertThat(r.payload().values()).doesNotContainNull();
        });
        assertThat(records.stream().filter(r -> r.geometry() != null)).hasSize(138);
        assertThat(records.stream().filter(r -> r.externalId().startsWith("!f"))).hasSize(93);
        assertThat(records.stream().filter(r -> Boolean.TRUE.equals(r.payload().get("lowBattery")))).hasSize(1);

        NormalizedRecord first = records.get(0);
        // The fixture's first node_id is 4026531840 (0xF0000000), read as unsigned 32-bit.
        assertThat(first.externalId()).isEqualTo("!f0000000");
        assertThat(first.payload()).containsEntry("longName", "Synthetic Node 000").containsEntry("shortName", "S000")
                .containsEntry("hwModel", "HELTEC_WSL_V3").containsEntry("role", "CLIENT").containsEntry("batteryLevel", 87)
                .containsEntry("voltage", 4.032).containsEntry("lowBattery", false)
                .doesNotContainKey("positionPrecisionBits");
        assertThat(first.geometry()).isNull();
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-10-14T01:10:47.344Z"));

        NormalizedRecord second = records.get(1);
        assertThat(second.payload()).containsEntry("role", "CLIENT_BASE").containsEntry("firmware", "2.7.15.567b8ea")
                .containsEntry("positionPrecisionBits", 16).containsEntry("uptimeS", 11163537L);
        assertThat(second.geometry().getCoordinate().y).isEqualTo(53.71);
        assertThat(second.geometry().getCoordinate().x).isEqualTo(-2.79);
        assertThat(second.quality().accuracyMeters()).isEqualTo(364.8);
    }

    @Test
    @DisplayName("10,000 synthetic nodes stream through: the first record arrives before 5 % of the file is read")
    void syntheticTenThousand() throws IOException {
        Path file = dir.resolve("nodes.json");
        try (Writer out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write("{\"nodes\":[");
            for (int i = 0; i < 10_000; i++) {
                out.write((i == 0 ? "" : ",") + "{\"node_id\":\"" + (3_000_000_000L + i) + "\",\"long_name\":\"N" + i
                        + "\",\"short_name\":\"n\",\"hardware_model\":43,\"role\":2,\"latitude\":" + (400_000_000 + i)
                        + ",\"longitude\":-" + (700_000_000 + i) + ",\"position_precision\":13,\"battery_level\":"
                        + (i % 100) + ",\"voltage\":\"3.9\",\"updated_at\":\"2026-10-14T00:00:00.000Z\"}");
            }
            out.write("],\"extra\":{\"ignored\":[1,2,3]}}");
        }
        long size = Files.size(file);
        AtomicLong read = new AtomicLong();
        AtomicLong readAtFirst = new AtomicLong(-1);
        List<String> ids = new ArrayList<>();
        try (InputStream in = new FilterInputStream(Files.newInputStream(file)) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int n = super.read(b, off, Math.min(len, 4096));
                if (n > 0) {
                    read.addAndGet(n);
                }
                return n;
            }
        }) {
            MeshtasticMapParser.Counts counts = MeshtasticMapParser.parse(in, record -> {
                if (readAtFirst.get() < 0) {
                    readAtFirst.set(read.get());
                }
                if (ids.size() < 3) {
                    ids.add(record.externalId());
                }
            });
            assertThat(counts.records()).isEqualTo(10_000);
        }
        assertThat(readAtFirst.get()).isPositive().isLessThan(size / 20);
        assertThat(ids).containsExactly("!b2d05e00", "!b2d05e01", "!b2d05e02");
    }

    @Test
    @DisplayName("nodes without an id or a time are skipped and counted; position_updated_at is the fallback")
    void skipsAndFallback() {
        String json = "{\"nodes\":[{\"node_id\":\"1\"},{\"long_name\":\"x\",\"updated_at\":\"2026-10-14T00:00:00Z\"},"
                + "{\"node_id\":2,\"position_updated_at\":\"2026-10-13T00:00:00Z\",\"latitude\":0,\"longitude\":0},"
                + "{\"node_id\":\"99999999999\",\"updated_at\":\"2026-10-14T00:00:00Z\"},3]}";
        List<NormalizedRecord> records = new ArrayList<>();
        MeshtasticMapParser.Counts counts = MeshtasticMapParser.parse(new ByteArrayInputStream(
                json.getBytes(StandardCharsets.UTF_8)), records::add);
        assertThat(counts).isEqualTo(new MeshtasticMapParser.Counts(1, 4));
        assertThat(records.get(0).observedAt()).isEqualTo(Instant.parse("2026-10-13T00:00:00Z"));
        assertThat(records.get(0).geometry()).as("0,0 is no position").isNull();
    }

    @Test
    @DisplayName("a body that is not an object with a nodes array is a format error")
    void formatErrors() {
        for (String body : List.of("[]", "{\"other\":1}", "{\"nodes\":[{\"node_id\":", "")) {
            assertThatThrownBy(() -> MeshtasticMapParser.parse(new ByteArrayInputStream(
                    body.getBytes(StandardCharsets.UTF_8)), r -> {})).as(body)
                    .isInstanceOf(UpstreamFormatException.class);
        }
    }

    @Test
    @DisplayName("the descriptor: daily, minInterval 24 h, 64 MB, budget 1/min and 2/day, a 30 h TTL")
    void descriptor() {
        MeshtasticMapAdapter adapter = new MeshtasticMapAdapter("");
        assertThat(adapter.uri().toString()).isEqualTo(MeshtasticMapAdapter.URL);
        var d = adapter.descriptor();
        assertThat(d.minInterval()).isEqualTo(Duration.ofHours(24));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofHours(24));
        assertThat(d.maxResponseBytes()).isEqualTo(64 * 1024 * 1024);
        assertThat(d.defaultBudget().perMinute()).isEqualTo(1);
        assertThat(d.defaultBudget().perDay()).isEqualTo(2);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(30));
        assertThat(d.attribution()).isEqualTo("Meshtastic Map by Liam Cottle");
        assertThat(d.licence()).isEqualTo("unspecified (community service)");
        assertThat(new MeshtasticMapAdapter("http://127.0.0.1:9").uri().toString())
                .isEqualTo("http://127.0.0.1:9/meshtastic.liamcottle.net/api/v1/nodes");
    }
}
