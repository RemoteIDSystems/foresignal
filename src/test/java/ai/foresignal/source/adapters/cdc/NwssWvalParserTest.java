package ai.foresignal.source.adapters.cdc;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link NwssWvalParser} against a trimmed {@code atcp-73re} response (prompt 095). */
class NwssWvalParserTest {

    private static final byte[] FIXTURE = AdapterFixtures.bytes("cdc-nwss-wval", "wval.json");

    private static List<NormalizedRecord> records(boolean includeWastewaterScan) {
        return NwssWvalParser.parse(FIXTURE, new NwssWvalParser.Options(includeWastewaterScan)).levels().stream()
                .map(level -> NwssWvalParser.toRecord(level, null)).toList();
    }

    @Test
    @DisplayName("WastewaterSCAN rows are dropped by default, and a row with no wval is skipped")
    void defaults() {
        NwssWvalParser.Result result = NwssWvalParser.parse(FIXTURE, new NwssWvalParser.Options(false));
        assertThat(result.rows()).isEqualTo(4);
        assertThat(result.levels()).extracting(NwssWvalParser.Level::key)
                .containsExactlyInAnyOrder("CA-1:SARS-CoV-2", "TX-1:Influenza A");
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.wastewaterScanSkipped()).isEqualTo(1);
        FieldSpecContract.assertPayloadKeysDeclared(
                new NwssWvalAdapter("", CountyGeometries.none()).descriptor(), records(false));
    }

    @Test
    @DisplayName("included WastewaterSCAN rows carry the CC BY-NC override")
    void wastewaterScanOverride() {
        List<NormalizedRecord> all = records(true);
        assertThat(all).hasSize(3);
        NormalizedRecord scan = all.stream().filter(r -> r.externalId().equals("WA-1:SARS-CoV-2")).findFirst()
                .orElseThrow();
        assertThat(scan.payload().get("licenceOverride")).isEqualTo(NwssWvalParser.WASTEWATER_SCAN_LICENCE);
    }

    @Test
    @DisplayName("wval is rounded to one decimal and the time base is week_end / date_updated")
    void valuesAndTime() {
        NormalizedRecord ca = records(false).stream()
                .filter(r -> r.externalId().equals("CA-1:SARS-CoV-2")).findFirst().orElseThrow();
        assertThat(ca.payload().get("wval")).isEqualTo(3.5);
        assertThat(ca.eventTime()).isEqualTo(Instant.parse("2026-09-05T00:00:00Z"));
        assertThat(ca.observedAt()).isEqualTo(Instant.parse("2026-09-11T00:00:00Z"));
        assertThat(ca.payload().get("geometryApproximate")).isNull();
    }

    @Test
    @DisplayName("several weeks of one site and pathogen fold to the latest week, whatever the row order")
    void latestWeekWins() {
        String rows = """
                [
                 {"site":"CA-9","pathogen_target":"RSV","wval":"2.0","week_end":"2026-09-12",
                  "date_updated":"2026-09-18T00:00:00.000","source":"CDC","state":"CA"},
                 {"site":"CA-9","pathogen_target":"RSV","wval":"9.0","week_end":"2026-09-05",
                  "date_updated":"2026-09-25T00:00:00.000","source":"CDC","state":"CA"},
                 {"site":"CA-9","pathogen_target":"RSV","wval":"3.0","week_end":"2026-09-12",
                  "date_updated":"2026-09-19T00:00:00.000","source":"CDC","state":"CA"}
                ]
                """;
        NwssWvalParser.Result result = NwssWvalParser.parse(
                rows.getBytes(java.nio.charset.StandardCharsets.UTF_8), new NwssWvalParser.Options(false));

        assertThat(result.levels()).hasSize(1);
        NwssWvalParser.Level level = result.levels().get(0);
        assertThat(level.weekEndInstant()).isEqualTo(Instant.parse("2026-09-12T00:00:00Z"));
        assertThat(level.wval()).isEqualTo(3.0);
        assertThat(result.skipped()).isEqualTo(2);
    }
}
