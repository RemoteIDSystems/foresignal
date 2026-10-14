package ai.foresignal.source.adapters.uasiren;

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

/**
 * {@link UaSirenParser} on the real siren.pp.ua capture (prompt 081): one record per region and type (parent alerts
 * listed under a child region counted once), kinds, the highest level, times, geometry (exact for districts and
 * states, approximate for communities), the 2022 alerts of occupied areas, edge cases, the fieldSpecs contract and a
 * stable static hash.
 */
class UaSirenParserTest {

    private static final UaRegionIndex REGIONS = new UaRegionIndex();
    private static final UaSirenAdapter ADAPTER = new UaSirenAdapter("", REGIONS);

    private static UaSirenParser.Result parse() {
        return UaSirenParser.parse(AdapterFixtures.bytes("ua-air-raid-siren", "alerts.json"), REGIONS);
    }

    private static Map<String, NormalizedRecord> byId() {
        return parse().records().stream().collect(Collectors.toMap(NormalizedRecord::externalId, Function.identity()));
    }

    @Test
    @DisplayName("52 listed alerts, 2 repeated under a child region: 50 records (43 air raid, 7 threat), all with a polygon")
    void counts() {
        UaSirenParser.Result result = parse();
        assertThat(result.records()).hasSize(50);
        assertThat(result.skipped()).isEmpty();
        assertThat(result.activeIds().get(UaSirenParser.AIR_RAID_KIND)).hasSize(43);
        assertThat(result.activeIds().get(UaSirenParser.THREAT_KIND)).hasSize(7);
        assertThat(result.records()).allSatisfy(r -> assertThat(r.geometry()).as(r.externalId()).isNotNull());
        assertThat(result.records()).extracting(NormalizedRecord::externalId).isSorted();
    }

    @Test
    @DisplayName("122:AIR (Chuhuivskyi district): red, lastUpdate and createdAt, exact polygon, names from its own entry")
    void district() {
        NormalizedRecord r = byId().get("122:AIR");
        assertThat(r.kind()).isEqualTo("air-raid-alert");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T22:55:38.279321Z"));
        assertThat(r.eventTime()).isEqualTo(Instant.parse("2026-10-13T22:55:38.605436Z"));
        assertThat(r.geometry()).isEqualTo(REGIONS.boundary("adm2:kharkivska:chuhuivskyi").orElseThrow());
        assertThat(r.payload()).containsExactlyInAnyOrderEntriesOf(Map.of("region", "Чугуївський район",
                "regionEn", "Chuhuivskyi district", "regionType", "District", "alertType", "AIR", "alertLevel", "red"));
    }

    @Test
    @DisplayName("community alerts are threats with their raion's polygon and geometryApproximate")
    void community() {
        NormalizedRecord artillery = byId().get("1313:ARTILLERY");
        assertThat(artillery.kind()).isEqualTo("threat-alert");
        assertThat(artillery.payload()).containsEntry("geometryApproximate", true)
                .containsEntry("regionType", "Community").containsEntry("region", "Вовчанська територіальна громада");
        assertThat(artillery.geometry()).isEqualTo(REGIONS.boundary("adm2:kharkivska:chuhuivskyi").orElseThrow());
        assertThat(byId().get("1313:URBAN_FIGHTS").kind()).isEqualTo("threat-alert");
    }

    @Test
    @DisplayName("Luhansk and Crimea carry air-raid alerts unchanged since 2022; Crimea has no English name")
    void occupiedAreas() {
        NormalizedRecord luhansk = byId().get("16:AIR");
        assertThat(luhansk.observedAt()).isEqualTo(Instant.parse("2022-04-04T16:45:00Z"));
        assertThat(luhansk.geometry()).isEqualTo(REGIONS.boundary("adm1:luhanska").orElseThrow());
        NormalizedRecord crimea = byId().get("9999:AIR");
        assertThat(crimea.payload()).doesNotContainKey("regionEn").containsEntry("region", "Автономна Республіка Крим");
    }

    @Test
    @DisplayName("the highest level wins, the earliest createdAt is eventTime; edge cases are skipped; non-arrays fail")
    void edges() {
        String body = """
                [{"regionId":"122","regionType":"District","regionName":"Чугуївський район","regionEngName":"",
                "lastUpdate":"2026-10-13T22:00:00Z","activeAlerts":[
                {"regionId":"122","regionType":"District","type":"AIR","lastUpdate":"2026-10-13T22:00:00Z",
                "activeAlertLevels":[{"alertLevel":"Yellow","createdAt":"2026-10-13T21:50:00Z"},
                {"alertLevel":"Red","createdAt":"2026-10-13T21:55:00Z"}]},
                {"regionType":"District","type":"AIR","lastUpdate":"2026-10-13T22:00:00Z"},
                {"regionId":"122","regionType":"District","type":"ARTILLERY"},
                {"regionId":"77777","regionType":"Community","type":"AIR","lastUpdate":"2026-10-13T22:00:00Z",
                "activeAlertLevels":[]}]}]""";
        UaSirenParser.Result result = UaSirenParser.parse(body.getBytes(StandardCharsets.UTF_8), REGIONS);
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("122:AIR", "77777:AIR");
        NormalizedRecord air = result.records().getFirst();
        assertThat(air.payload()).containsEntry("alertLevel", "red").doesNotContainKey("regionEn");
        assertThat(air.eventTime()).isEqualTo(Instant.parse("2026-10-13T21:50:00Z"));
        NormalizedRecord unknown = result.records().get(1);
        assertThat(unknown.geometry()).isNull();
        assertThat(unknown.payload()).doesNotContainKeys("alertLevel", "region");
        assertThat(result.skipped()).containsEntry(UaSirenParser.MISSING_ID, 1)
                .containsEntry(UaSirenParser.MISSING_LAST_UPDATE, 1);
        assertThat(UaSirenParser.higher("yellow", "red")).isEqualTo("red");
        assertThat(UaSirenParser.higher("red", "yellow")).isEqualTo("red");
        assertThat(UaSirenParser.higher(null, "other")).isEqualTo("other");
        assertThatThrownBy(() -> UaSirenParser.parse("{}".getBytes(StandardCharsets.UTF_8), REGIONS))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("every payload key is declared with its type (the fieldSpecs contract); the descriptor validates")
    void fieldSpecs() {
        FieldSpecContract.assertPayloadKeysDeclared(ADAPTER.descriptor(), parse().records());
        assertThat(DescriptorValidator.problems(ADAPTER.descriptor(), ADAPTER)).isEmpty();
        assertThat(ADAPTER.descriptor().credentialFields()).isEmpty();
    }

    @Test
    @DisplayName("a later lastUpdate with the same level and region is the same static content")
    void stableStaticHash() {
        String template = """
                [{"regionId":"122","regionType":"District","regionName":"Чугуївський район",
                "regionEngName":"Chuhuivskyi district","lastUpdate":"%1$s","activeAlerts":[{"regionId":"122",
                "regionType":"District","type":"AIR","lastUpdate":"%1$s","activeAlertLevels":[{"alertLevel":"Red",
                "reason":"%2$s","createdAt":"2026-10-13T22:55:38Z"}]}]}]""";
        NormalizedRecord first = UaSirenParser.parse(template.formatted("2026-10-13T22:55:38Z", "a")
                .getBytes(StandardCharsets.UTF_8), REGIONS).records().getFirst();
        NormalizedRecord second = UaSirenParser.parse(template.formatted("2026-10-13T23:05:00Z", "b")
                .getBytes(StandardCharsets.UTF_8), REGIONS).records().getFirst();
        SnapshotContract.assertStableStaticHash(ADAPTER.descriptor(), List.of(first, second));
    }
}
