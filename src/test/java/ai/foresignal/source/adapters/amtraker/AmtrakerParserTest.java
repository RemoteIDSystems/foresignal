package ai.foresignal.source.adapters.amtraker;

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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link AmtrakerParser} on the real capture (prompt 080): every provider covered, mph to km/h, compass text to
 * degrees, {@code lastValTS} with its offset as {@code observedAt}, dropped fields, {@code Predeparture} trains left
 * out, the edge cases, the fieldSpecs contract and a stable static hash.
 */
class AmtrakerParserTest {

    private static final AmtrakerAdapter ADAPTER = new AmtrakerAdapter("");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static AmtrakerParser.Result trains() {
        return AmtrakerParser.parse(AdapterFixtures.bytes("amtraker", "trains.json"));
    }

    private static Map<String, NormalizedRecord> byId(List<NormalizedRecord> records) {
        return records.stream().collect(Collectors.toMap(NormalizedRecord::externalId, Function.identity()));
    }

    @Test
    @DisplayName("ten trains: nine records across Amtrak, VIA and Brightline; the predeparture train is left out")
    void providers() {
        AmtrakerParser.Result result = trains();
        assertThat(result.records()).hasSize(9);
        assertThat(result.predeparture()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        Map<String, NormalizedRecord> records = byId(result.records());
        assertThat(records).doesNotContainKey("28-13");
        assertThat(result.records()).extracting(r -> r.payload().get("provider"))
                .containsOnly("Amtrak", "Via", "Brightline");
        assertThat(records.get("4-11").payload()).containsEntry("state", "Completed");
    }

    @Test
    @DisplayName("1-12: lastValTS with its offset, rounded point, mph to km/h, NW = 315, the renamed fields")
    void conversions() {
        NormalizedRecord r = byId(trains().records()).get("1-12");
        assertThat(r.kind()).isEqualTo("train");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T22:45:40Z"));
        Point point = (Point) r.geometry();
        assertThat(point.getX()).isEqualTo(-108.590358);
        assertThat(point.getY()).isEqualTo(32.303674);
        assertThat(r.payload()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "trainNumber", "1", "route", "Sunset Limited", "provider", "Amtrak", "state", "Active",
                "nextStationCode", "DEM", "originCode", "NOL", "destinationCode", "LAX", "speedKph", 124.8,
                "headingDeg", 315.0));

        NormalizedRecord via = byId(trains().records()).get("v29-13");
        assertThat(via.observedAt()).isEqualTo(Instant.parse("2026-10-13T22:49:50Z"));
        assertThat(via.payload()).containsEntry("speedKph", 53.0).containsEntry("headingDeg", 270.0)
                .containsEntry("trainNumber", "v29");
        NormalizedRecord brightline = byId(trains().records()).get("b5151-13");
        assertThat(brightline.observedAt()).isEqualTo(Instant.parse("2026-10-13T22:50:50.765Z"));
        assertThat(brightline.payload()).containsEntry("speedKph", 0.0).containsEntry("headingDeg", 180.0);
    }

    @Test
    @DisplayName("stations, alerts, colors, time zones, names and the blank trainTimely never reach the payload")
    void droppedFields() {
        assertThat(trains().records()).allSatisfy(r -> assertThat(r.payload()).doesNotContainKeys("stations",
                "alerts", "iconColor", "textColor", "eventTZ", "originTZ", "destTZ", "eventName", "origName",
                "destName", "timeliness", "statusMsg", "providerShort", "trainNumRaw", "objectID", "onlyOfTrainNum",
                "dataSource", "createdAt", "updatedAt", "velocity", "heading"));
    }

    @Test
    @DisplayName("edge cases: no position and an unreadable time are skipped; NNE and a timeliness text are kept")
    void edges() {
        AmtrakerParser.Result result = AmtrakerParser.parse(AdapterFixtures.bytes("amtraker", "trains-edge.json"));
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("902-13");
        assertThat(result.skipped()).isEqualTo(2);
        assertThat(result.records().getFirst().payload()).containsEntry("headingDeg", 22.5)
                .containsEntry("timeliness", "5 Minutes Late").containsEntry("speedKph", 16.1)
                .doesNotContainKeys("stations", "alerts", "iconColor");
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({"N, 0.0", "NE, 45.0", "E, 90.0", "SE, 135.0", "S, 180.0", "SW, 225.0", "W, 270.0", "NW, 315.0",
            "nne, 22.5", "' W ', 270.0"})
    @DisplayName("compass text to degrees")
    void compass(String text, double degrees) {
        assertThat(AmtrakerParser.headingDegrees(text)).isEqualTo(degrees);
    }

    @Test
    @DisplayName("an unknown heading text is no heading")
    void unknownCompass() {
        assertThat(AmtrakerParser.headingDegrees("up")).isNull();
        assertThat(AmtrakerParser.headingDegrees(null)).isNull();
    }

    @Test
    @DisplayName("a body that is not an object of arrays is a format error")
    void formatErrors() {
        assertThatThrownBy(() -> AmtrakerParser.parse("[]".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> AmtrakerParser.parse("{\"1\": {}}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> AmtrakerParser.parse("not json".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("every payload key is declared with its type (the fieldSpecs contract); the descriptor validates")
    void fieldSpecs() {
        FieldSpecContract.assertPayloadKeysDeclared(ADAPTER.descriptor(), trains().records());
        FieldSpecContract.assertPayloadKeysDeclared(ADAPTER.descriptor(),
                AmtrakerParser.parse(AdapterFixtures.bytes("amtraker", "trains-edge.json")).records());
        assertThat(DescriptorValidator.problems(ADAPTER.descriptor(), ADAPTER)).isEmpty();
    }

    @Test
    @DisplayName("snapshots that differ in position, speed, heading and next station share one static hash")
    void stableStaticHash() {
        ObjectNode first = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("amtraker", "trains.json")).get("1").get(0);
        ObjectNode second = first.deepCopy();
        second.put("lat", 32.4).put("lon", -108.8).put("velocity", 60.0).put("heading", "W")
                .put("eventCode", "LDB").put("lastValTS", "2026-10-13T16:50:40-06:00");
        SnapshotContract.assertStableStaticHash(ADAPTER.descriptor(),
                List.of(AmtrakerParser.record(first), AmtrakerParser.record(second)));
    }
}
