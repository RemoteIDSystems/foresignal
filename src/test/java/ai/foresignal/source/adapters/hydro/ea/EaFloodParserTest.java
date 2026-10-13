package ai.foresignal.source.adapters.hydro.ea;

import static ai.foresignal.source.adapters.hydro.HydroTestSupport.node;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.databind.JsonNode;

/**
 * {@link EaFloodParser} on the 2026-10-13 Environment Agency flood list (six tidal flood alerts in East Anglia and
 * Kent) and the polygon of the tidal Deben estuary: UK local times read in {@code Europe/London} (BST, +01:00), the
 * level-4 tombstone, the 4 KB message cap, the polygon's WKB round trip, and the descriptor of
 * {@link EaFloodWarningsAdapter}.
 */
class EaFloodParserTest {

    static List<JsonNode> items() {
        return EaFloodParser.items(AdapterFixtures.bytes("ea-flood-warnings", "floods.json"));
    }

    static List<NormalizedRecord> records(Geometry polygon) {
        return items().stream().map(i -> EaFloodParser.item(i, polygon).record()).toList();
    }

    static Geometry deben() {
        return EaFloodParser.polygon(AdapterFixtures.bytes("ea-flood-warnings", "polygon-054WACDV4A.json")).geometry();
    }

    @Test
    @DisplayName("six alerts; 15:41 BST is 14:41 UTC; the payload names and the tidal flag")
    void alerts() {
        List<NormalizedRecord> records = records(null);
        assertThat(records).hasSize(6).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("flood-warning");
            assertThat(r.tombstone()).isFalse();
            assertThat(r.payload()).containsEntry("severityLevel", 3L).containsEntry("severity", "Flood alert")
                    .containsEntry("isTidal", true);
        });
        NormalizedRecord deben = EnvTestSupport.byId(records, "054WACDV4A");
        assertThat(deben.observedAt()).isEqualTo(Instant.parse("2026-10-13T14:41:00Z"));
        assertThat(deben.eventTime()).isEqualTo(Instant.parse("2026-10-13T14:41:54Z"));
        assertThat(deben.payload()).containsEntry("description", "The tidal Deben estuary")
                .containsEntry("eaAreaName", "East Anglia");
        assertThat((String) deben.payload().get("message")).startsWith("High tides overnight may lead to flooding.");
        assertThat(deben.geometry()).isNull();
        FieldSpecContract.assertPayloadKeysDeclared(new EaFloodWarningsAdapter("", () -> null).descriptor(), records);
    }

    @Test
    @DisplayName("the Deben polygon: a MultiPolygon of 4,172 vertices, not simplified, surviving the WKB round trip")
    void polygon() {
        FeatureGeometry.Result result = EaFloodParser.polygon(
                AdapterFixtures.bytes("ea-flood-warnings", "polygon-054WACDV4A.json"));
        assertThat(result.geometry().getGeometryType()).isEqualTo("MultiPolygon");
        assertThat(result.geometry().getNumPoints()).isEqualTo(4172);
        assertThat(result.simplified()).isFalse();
        Geometry back = EaFloodParser.decode(EaFloodParser.encode(result.geometry()));
        assertThat(back.equalsExact(result.geometry())).isTrue();
        assertThat(back.getSRID()).isEqualTo(4326);
        assertThat(EaFloodParser.decode("not base64!")).isNull();
        assertThat(EnvTestSupport.byId(records(deben()), "054WACDV4A").geometry()).isNotNull();
        assertThatThrownBy(() -> EaFloodParser.polygon("{\"type\": \"FeatureCollection\", \"features\": []}"
                .getBytes(StandardCharsets.UTF_8))).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("level 4 is a tombstone; no id or time is skipped; a long message is cut to 4 KB")
    void edges() {
        MappedRecord ended = EaFloodParser.item(node("""
                {"floodAreaID": "X1", "severityLevel": 4, "severity": "Warning no longer in force",
                 "timeMessageChanged": "2026-01-10T09:00:00"}"""), null);
        assertThat(ended.record().tombstone()).isTrue();
        assertThat(ended.record().observedAt()).isEqualTo(Instant.parse("2026-01-10T09:00:00Z"));
        assertThat(EaFloodParser.item(node("{\"severityLevel\": 3}"), null).reason())
                .isEqualTo(MappedRecord.MISSING_ID);
        assertThat(EaFloodParser.item(node("{\"floodAreaID\": \"X2\"}"), null).reason())
                .isEqualTo(MappedRecord.MISSING_OBSERVED_AT);
        String longText = "é".repeat(3000);
        assertThat(EaFloodParser.cap(longText).getBytes(StandardCharsets.UTF_8).length).isEqualTo(4096);
        assertThat(EaFloodParser.cap("short")).isEqualTo("short");
        assertThatThrownBy(() -> EaFloodParser.items("{\"meta\": {}}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("descriptor: event retention, the attribution, message embedded; snapshot contract")
    void descriptorAndSnapshot() {
        EaFloodWarningsAdapter adapter = new EaFloodWarningsAdapter("", () -> null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().attribution()).isEqualTo(EaFloodWarningsAdapter.ATTRIBUTION);
        assertThat(adapter.descriptor().embedFields()).contains("message");
        assertThat(adapter.polygonUri("054WACDV4A")).hasToString(
                "https://environment.data.gov.uk/flood-monitoring/id/floodAreas/054WACDV4A/polygon");
        NormalizedRecord r = EnvTestSupport.byId(records(deben()), "054WACDV4A");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(),
                List.of(r, EnvTestSupport.later(r, Duration.ofMinutes(15))));
    }
}
