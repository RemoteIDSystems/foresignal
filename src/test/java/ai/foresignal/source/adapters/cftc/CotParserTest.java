package ai.foresignal.source.adapters.cftc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/**
 * {@link CotParser} against the captured CFTC Public Reporting rows (prompt 117): net computation, the managed money
 * share of open interest, missing columns, the contract filter and the cursor dates.
 */
class CotParserTest {

    static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    private static JsonNode rows(String name) {
        return CotParser.rows(AdapterFixtures.bytes("cftc-cot", name));
    }

    private static Map<String, NormalizedRecord> byId(List<NormalizedRecord> records) {
        Map<String, NormalizedRecord> out = new HashMap<>();
        records.forEach(r -> out.put(r.externalId(), r));
        return out;
    }

    private static double num(JsonNode row, String column) {
        return Double.parseDouble(row.get(column).asString());
    }

    private static double value(NormalizedRecord record) {
        return ((Number) record.payload().get("value")).doubleValue();
    }

    @Test
    @DisplayName("disaggregated: oi, managed money long/short/net, the producer, swap, other and non-reportable nets")
    void disaggregated() {
        JsonNode rows = rows("disaggregated.json");
        JsonNode wti = null;
        for (JsonNode row : rows) {
            if ("067651".equals(row.get("cftc_contract_market_code").asString())
                    && row.get("report_date_as_yyyy_mm_dd").asString().startsWith("2026-10-13")) {
                wti = row;
            }
        }
        assertThat(wti).isNotNull();
        Map<String, NormalizedRecord> points = byId(CotParser.parse(CotReport.DISAGGREGATED, rows, Set.of(), NOW));
        String prefix = "cot.067651.disaggregated.";
        double longs = num(wti, "m_money_positions_long_all");
        double shorts = num(wti, "m_money_positions_short_all");
        double oi = num(wti, "open_interest_all");
        assertThat(value(points.get(prefix + "oi:2026-10-13"))).isEqualTo(oi);
        assertThat(value(points.get(prefix + "mm_long:2026-10-13"))).isEqualTo(longs);
        assertThat(value(points.get(prefix + "mm_short:2026-10-13"))).isEqualTo(shorts);
        assertThat(value(points.get(prefix + "mm_net:2026-10-13"))).isEqualTo(longs - shorts);
        assertThat(value(points.get(prefix + "pm_net:2026-10-13"))).isEqualTo(
                num(wti, "prod_merc_positions_long") - num(wti, "prod_merc_positions_short"));
        assertThat(value(points.get(prefix + "sd_net:2026-10-13"))).isEqualTo(
                num(wti, "swap_positions_long_all") - num(wti, "swap__positions_short_all"));
        assertThat(value(points.get(prefix + "or_net:2026-10-13"))).isEqualTo(
                num(wti, "other_rept_positions_long") - num(wti, "other_rept_positions_short"));
        assertThat(value(points.get(prefix + "nr_net:2026-10-13"))).isEqualTo(
                num(wti, "nonrept_positions_long_all") - num(wti, "nonrept_positions_short_all"));
        // Two contracts × two reports × nine measures.
        assertThat(points).hasSize(36);
    }

    @Test
    @DisplayName("mm_net_pct_oi is the managed money net share of open interest in percent")
    void netPctOi() {
        JsonNode rows = rows("disaggregated.json");
        Map<String, NormalizedRecord> points = byId(CotParser.parse(CotReport.DISAGGREGATED, rows, Set.of(), NOW));
        NormalizedRecord net = points.get("cot.088691.disaggregated.mm_net:2026-10-13");
        NormalizedRecord oi = points.get("cot.088691.disaggregated.oi:2026-10-13");
        NormalizedRecord pct = points.get("cot.088691.disaggregated.mm_net_pct_oi:2026-10-13");
        double expected = Math.round(value(net) / value(oi) * 100.0 * 1000.0) / 1000.0;
        assertThat(value(pct)).isEqualTo(expected);
        assertThat(pct.payload()).containsEntry("unit", "%");
        assertThat(net.payload()).containsEntry("unit", "{contracts}");
    }

    @Test
    @DisplayName("legacy and TFF nets come from their own columns")
    void legacyAndTff() {
        Map<String, NormalizedRecord> legacy = byId(CotParser.parse(CotReport.LEGACY, rows("legacy.json"), Set.of(),
                NOW));
        assertThat(legacy).containsKeys("cot.067651.legacy.oi:2026-10-13", "cot.067651.legacy.nc_net:2026-10-13",
                "cot.067651.legacy.comm_net:2026-10-13", "cot.13874A.legacy.nr_net:2026-10-06");
        assertThat(legacy).hasSize(2 * 2 * 4);
        Map<String, NormalizedRecord> tff = byId(CotParser.parse(CotReport.TFF, rows("tff.json"), Set.of(), NOW));
        assertThat(tff).containsKeys("cot.13874A.tff.dealer_net:2026-10-13", "cot.13874A.tff.am_net:2026-10-13",
                "cot.043602.tff.lev_net:2026-10-13", "cot.043602.tff.or_net:2026-10-06");
        assertThat(tff).hasSize(2 * 2 * 5);
    }

    @Test
    @DisplayName("a point: eventTime is the Tuesday report date, observedAt the fetch time, weekly, no geometry")
    void pointShape() {
        NormalizedRecord point = byId(CotParser.parse(CotReport.LEGACY, rows("legacy.json"), Set.of(), NOW))
                .get("cot.067651.legacy.nc_net:2026-10-13");
        assertThat(point.kind()).isEqualTo("series-point");
        assertThat(point.geometry()).isNull();
        assertThat(point.eventTime()).isEqualTo(Instant.parse("2026-10-13T00:00:00Z"));
        assertThat(point.observedAt()).isEqualTo(NOW);
        assertThat(point.payload()).containsEntry("series", "cot.067651.legacy.nc_net")
                .containsEntry("frequency", "P1W").containsEntry("period", "2026-10-13")
                .containsEntry("dataset", "legacy");
        @SuppressWarnings("unchecked")
        Map<String, Object> extra = (Map<String, Object>) point.payload().get("extra");
        assertThat(extra).containsEntry("contractCode", "067651").containsEntry("measure", "nc_net");
    }

    @Test
    @DisplayName("a missing column drops only the measures that need it")
    void missingColumns() {
        String body = "[{\"report_date_as_yyyy_mm_dd\":\"2026-10-13T00:00:00.000\",\"cftc_contract_market_code\":"
                + "\"067651\",\"open_interest_all\":\"1000\",\"m_money_positions_long_all\":\"300\"}]";
        List<NormalizedRecord> points = CotParser.parse(CotReport.DISAGGREGATED,
                CotParser.rows(body.getBytes(StandardCharsets.UTF_8)), Set.of(), NOW);
        assertThat(points).extracting(r -> r.payload().get("series"))
                .containsExactlyInAnyOrder("cot.067651.disaggregated.oi", "cot.067651.disaggregated.mm_long");
    }

    @Test
    @DisplayName("the contract filter keeps only the listed codes")
    void contractFilter() {
        List<NormalizedRecord> points = CotParser.parse(CotReport.DISAGGREGATED, rows("disaggregated.json"),
                Set.of("088691"), NOW);
        assertThat(points).isNotEmpty().allSatisfy(p -> assertThat(String.valueOf(p.payload().get("series")))
                .startsWith("cot.088691."));
    }

    @Test
    @DisplayName("the cursor advances to the newest report date; the backfill bound uses the oldest")
    void cursorDates() {
        JsonNode rows = rows("tff.json");
        assertThat(CotParser.newest(rows)).isEqualTo(LocalDate.parse("2026-10-13"));
        assertThat(CotParser.oldest(rows)).isEqualTo(LocalDate.parse("2026-10-06"));
        assertThat(CotParser.distinctDates(rows)).isEqualTo(2);
        assertThat(CotParser.newest(CotParser.rows(new byte[0]))).isNull();
    }

    @Test
    @DisplayName("metadata: open interest is open-interest, the rest position; difference; a 3-day release lag")
    void metadata() {
        Map<String, NormalizedRecord> points = byId(CotParser.parse(CotReport.LEGACY, rows("legacy.json"), Set.of(),
                NOW));
        SeriesMeta oi = CotParser.meta(points.get("cot.067651.legacy.oi:2026-10-13"));
        SeriesMeta net = CotParser.meta(points.get("cot.067651.legacy.nc_net:2026-10-13"));
        assertThat(oi.family()).isEqualTo("open-interest");
        assertThat(net.family()).isEqualTo("position");
        assertThat(net.transform()).isEqualTo("difference");
        assertThat(net.releaseLagDays()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("a body that is not a JSON array is a format error")
    void notAnArray() {
        assertThatThrownBy(() -> CotParser.rows("{\"error\":true}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CotParser.rows("<html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("every payload key is declared and the descriptor validates")
    void descriptor() {
        CftcCotAdapter adapter = new CftcCotAdapter("");
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.egress().name()).isEqualTo("OPEN");
        assertThat(d.defaultBudget().perMinute()).isEqualTo(10);
        assertThat(d.defaultBudget().perDay()).isEqualTo(200);
        assertThat(d.defaultInterval()).isEqualTo(java.time.Duration.ofHours(6));
        assertThat(d.defaultRetention()).isEqualTo(java.time.Duration.ofDays(3650));
        assertThat(d.credentialFields()).extracting(c -> c.name()).containsExactly("appToken");
        assertThat(d.requiredCredentialFields()).isEmpty();
        List<NormalizedRecord> all = new java.util.ArrayList<>(CotParser.parse(CotReport.DISAGGREGATED,
                rows("disaggregated.json"), Set.of(), NOW));
        all.addAll(ai.foresignal.source.series.SeriesPoint.latestPerSeries(all,
                p -> CotParser.meta(p).withEgress(d.egress())));
        FieldSpecContract.assertPayloadKeysDeclared(d, all);
    }
}
