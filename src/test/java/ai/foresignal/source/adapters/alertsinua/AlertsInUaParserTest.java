package ai.foresignal.source.adapters.alertsinua;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.reference.ukr.UaRegionIndex;
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
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link AlertsInUaParser} on the synthetic fixture built from the developer docs (prompt 081): kinds, geometry from the
 * region index (exact, approximate, none), the payload with sorted threat types and without the threats'
 * {@code source_message}, the 1 KB notes cap, {@code finished_at} as an immediate tombstone, the absence scopes, the
 * fieldSpecs contract and a stable static hash.
 */
class AlertsInUaParserTest {

    private static final UaRegionIndex REGIONS = new UaRegionIndex();
    private static final AlertsInUaAdapter ADAPTER = new AlertsInUaAdapter("", REGIONS);
    private static final Instant NOW = Instant.parse("2026-10-13T23:00:00Z");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static AlertsInUaParser.Result parse() {
        return AlertsInUaParser.parse(AdapterFixtures.bytes("alerts-in-ua", "active.json"), REGIONS, NOW);
    }

    private static Map<String, NormalizedRecord> byId() {
        return parse().records().stream().collect(Collectors.toMap(NormalizedRecord::externalId, Function.identity()));
    }

    @Test
    @DisplayName("seven alerts: six in force by kind, one finished alert as a tombstone, nothing skipped")
    void kinds() {
        AlertsInUaParser.Result result = parse();
        assertThat(result.records()).hasSize(7);
        assertThat(result.skipped()).isEmpty();
        assertThat(result.activeIds().get(AlertsInUaParser.AIR_RAID_KIND))
                .containsExactly("10", "1001", "1002", "1004");
        assertThat(result.activeIds().get(AlertsInUaParser.THREAT_KIND)).containsExactly("1003", "1005");
        NormalizedRecord finished = byId().get("1006");
        assertThat(finished.tombstone()).isTrue();
        assertThat(finished.kind()).isEqualTo(AlertsInUaParser.AIR_RAID_KIND);
        assertThat(finished.observedAt()).isEqualTo(Instant.parse("2026-10-13T22:59:30.512Z"));
    }

    @Test
    @DisplayName("Kyiv: the city polygon, updated_at and started_at, sorted distinct threats, no source_message")
    void kyiv() {
        NormalizedRecord r = byId().get("1001");
        assertThat(r.kind()).isEqualTo("air-raid-alert");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T22:58:12.104Z"));
        assertThat(r.eventTime()).isEqualTo(Instant.parse("2026-10-13T22:41:05Z"));
        assertThat(r.geometry()).isEqualTo(REGIONS.boundary("adm1:kyiv-city").orElseThrow());
        assertThat(r.payload()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "location", "м. Київ", "locationEn", "Kyiv City", "locationType", "city", "oblast", "м. Київ",
                "alertType", "air_raid", "alertLevel", "red",
                "threatTypes", List.of("ballistic_missiles", "drones")));
        assertThat(r.payload().toString()).doesNotContain("Дронова загроза").doesNotContain("укриття");
    }

    @Test
    @DisplayName("a hromada gets its raion's polygon and geometryApproximate; a raion is exact; an unknown UID has none")
    void geometry() {
        Map<String, NormalizedRecord> records = byId();
        NormalizedRecord hromada = records.get("1004");
        assertThat(hromada.geometry()).isEqualTo(REGIONS.boundary("adm2:volynska:volodymyr-volynskyi").orElseThrow());
        assertThat(hromada.payload()).containsEntry("geometryApproximate", true);
        NormalizedRecord raion = records.get("1003");
        assertThat(raion.geometry()).isEqualTo(hromada.geometry());
        assertThat(raion.payload()).doesNotContainKey("geometryApproximate").containsEntry("raion", "Володимирський район");
        NormalizedRecord unknown = records.get("1005");
        assertThat(unknown.geometry()).isNull();
        assertThat(unknown.kind()).isEqualTo("threat-alert");
        assertThat(unknown.payload()).containsEntry("alertType", "urban_fights")
                .containsEntry("notes", "<b>Untrusted</b> text with a link https://example.invalid/x");
        assertThat(records.get("10").geometry()).isEqualTo(REGIONS.boundary("adm1:luhanska").orElseThrow());
    }

    @Test
    @DisplayName("notes are cut to 1 KB of UTF-8 without splitting a character")
    void notesCap() {
        String notes = (String) byId().get("1003").payload().get("notes");
        assertThat(notes.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(AlertsInUaParser.NOTES_MAX_BYTES);
        assertThat(notes).startsWith("Загроза обстрілу.").doesNotContain("�");
    }

    @Test
    @DisplayName("air_raid is an air-raid alert; every other type is a threat alert")
    void kindOf() {
        assertThat(AlertsInUaParser.kindOf("air_raid")).isEqualTo("air-raid-alert");
        for (String type : new String[] {"artillery_shelling", "urban_fights", "chemical", "nuclear", null}) {
            assertThat(AlertsInUaParser.kindOf(type)).isEqualTo("threat-alert");
        }
    }

    @Test
    @DisplayName("a missing id or updated_at is skipped, a repeated id counted; a body without alerts is a format error")
    void edges() {
        String body = """
                {"alerts":[{"location_uid":"31","alert_type":"air_raid","updated_at":"2026-10-13T22:00:00Z"},
                {"id":7,"location_uid":"31","alert_type":"air_raid"},
                {"id":8,"location_uid":"31","alert_type":"air_raid","updated_at":"2026-10-13T22:00:00Z"},
                {"id":8,"location_uid":"31","alert_type":"air_raid","updated_at":"2026-10-13T22:00:00Z"},
                {"id":9,"alert_type":"air_raid","finished_at":"2026-10-13T22:01:00Z"}]}""";
        AlertsInUaParser.Result result = AlertsInUaParser.parse(body.getBytes(StandardCharsets.UTF_8), REGIONS, NOW);
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("8", "9");
        assertThat(result.records().get(1).observedAt()).as("finished without updated_at").isEqualTo(NOW);
        assertThat(result.skipped()).containsEntry(AlertsInUaParser.MISSING_ID, 1)
                .containsEntry(AlertsInUaParser.MISSING_UPDATED_AT, 1).containsEntry(AlertsInUaParser.DUPLICATE_ID, 1);
        assertThatThrownBy(() -> AlertsInUaParser.parse("{}".getBytes(StandardCharsets.UTF_8), REGIONS, NOW))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> AlertsInUaParser.parse("<html>".getBytes(StandardCharsets.UTF_8), REGIONS, NOW))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("every payload key is declared with its type (the fieldSpecs contract); the descriptor validates")
    void fieldSpecs() {
        FieldSpecContract.assertPayloadKeysDeclared(ADAPTER.descriptor(), parse().records());
        assertThat(DescriptorValidator.problems(ADAPTER.descriptor(), ADAPTER)).isEmpty();
    }

    @Test
    @DisplayName("snapshots that differ only in updated_at and threat order share one static hash")
    void stableStaticHash() {
        ObjectNode first = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("alerts-in-ua", "active.json"))
                .get("alerts").get(1);
        ObjectNode second = first.deepCopy();
        second.put("updated_at", "2026-10-13T22:59:59.000Z");
        var threats = (tools.jackson.databind.node.ArrayNode) second.get("threats");
        threats.insert(0, threats.remove(2));
        SnapshotContract.assertStableStaticHash(ADAPTER.descriptor(), List.of(
                AlertsInUaParser.record(first, "1001", "air-raid-alert", Instant.parse("2026-10-13T22:58:12Z"),
                        REGIONS),
                AlertsInUaParser.record(second, "1001", "air-raid-alert", Instant.parse("2026-10-13T22:59:59Z"),
                        REGIONS)));
    }
}
