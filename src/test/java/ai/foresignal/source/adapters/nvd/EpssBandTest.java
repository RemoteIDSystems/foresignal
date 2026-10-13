package ai.foresignal.source.adapters.nvd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The EPSS banding rule ({@link EpssBands}) at every boundary and {@link EpssParser} on real FIRST.org responses
 * (captured 2026-10-13): only bands are stored, never the raw score or percentile, and a missing id is reported.
 */
class EpssBandTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @ParameterizedTest(name = "epss {0} → {1}")
    @CsvSource({"0, very-low", "0.009999, very-low", "0.01, low", "0.049999999, low", "0.05, medium",
            "0.199999, medium", "0.2, high", "0.20000, high", "0.499999, high", "0.5, very-high", "1, very-high",
            "0.994090000, very-high"})
    void epssBoundaries(String value, String band) {
        assertThat(EpssBands.epssBand(new BigDecimal(value))).isEqualTo(band);
    }

    @ParameterizedTest(name = "percentile {0} → {1}")
    @CsvSource({"0, <p50", "0.499999, <p50", "0.5, p50", "0.899999, p50", "0.9, p90", "0.949999, p90", "0.95, p95",
            "0.989999, p95", "0.99, p99", "1.000000000, p99", "0.999410000, p99"})
    void percentileBoundaries(String value, String band) {
        assertThat(EpssBands.percentileBand(new BigDecimal(value))).isEqualTo(band);
    }

    @Test
    @DisplayName("two real scores become two band records; no raw value reaches the payload")
    void realResponse() {
        List<String> requested = List.of("CVE-2015-5477", "CVE-2010-0738");
        EpssParser.Result result = EpssParser.parse(AdapterFixtures.bytes("nvd-cves", "epss.json"), requested);
        assertThat(result.missing()).isEmpty();
        assertThat(result.malformed()).isZero();
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("epss:CVE-2015-5477",
                "epss:CVE-2010-0738");
        NormalizedRecord bind = result.records().getFirst();
        assertThat(bind.kind()).isEqualTo("cve-epss");
        assertThat(bind.observedAt()).isEqualTo(Instant.parse("2026-10-13T00:00:00Z"));
        assertThat(bind.payload()).containsOnlyKeys("cveId", "epssBand", "percentileBand", "modelDate")
                .containsEntry("cveId", "CVE-2015-5477").containsEntry("epssBand", "very-high")
                .containsEntry("percentileBand", "p99").containsEntry("modelDate", "2026-10-13");
        assertThat(result.records().get(1).payload()).containsEntry("epssBand", "very-high")
                .containsEntry("percentileBand", "p99");
        assertThat(result.records()).allSatisfy(r -> assertThat(r.payload().values())
                .noneMatch(v -> v.toString().contains("0.99") || v.toString().contains("0.79")));
        FieldSpecContract.assertPayloadKeysDeclared(new NvdCvesAdapter("", Duration.ZERO, d -> { }).descriptor(),
                result.records());
    }

    @Test
    @DisplayName("an id missing from the response (a CVE too new to score) is reported, not invented")
    void missingId() {
        EpssParser.Result result = EpssParser.parse(AdapterFixtures.bytes("nvd-cves", "epss-missing.json"),
                List.of("CVE-2015-5477", "CVE-2026-96951"));
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("epss:CVE-2015-5477");
        assertThat(result.missing()).containsExactly("CVE-2026-96951");
    }

    @Test
    @DisplayName("bad rows are counted; a status other than OK is a format error")
    void malformed() {
        String body = """
                {"status": "OK", "data": [{"cve": "CVE-2026-1", "epss": "0.1", "percentile": "0.5", "date": "2026-10-13"},
                 {"cve": "CVE-2026-10000", "epss": "1.5", "percentile": "0.5", "date": "2026-10-13"},
                 {"cve": "CVE-2026-10001", "epss": "0.1", "percentile": "0.5", "date": "today"},
                 {"cve": "CVE-2026-10002", "epss": "0.03", "percentile": "0.42", "date": "2026-10-13"}]}""";
        EpssParser.Result result = EpssParser.parse(body.getBytes(StandardCharsets.UTF_8), null);
        assertThat(result.malformed()).isEqualTo(3);
        assertThat(EpssParser.parse(body.getBytes(StandardCharsets.UTF_8), List.of("CVE-2026-1")).records())
                .as("rows not asked for are ignored").isEmpty();
        assertThat(result.records()).singleElement().satisfies(r -> assertThat(r.payload())
                .containsEntry("epssBand", "low").containsEntry("percentileBand", "<p50"));
        assertThatThrownBy(() -> EpssParser.parse("{\"status\": \"error\", \"data\": []}"
                .getBytes(StandardCharsets.UTF_8), List.of())).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a new model date with both bands unchanged keeps the static hash (modelDate is volatile)")
    void snapshot() {
        ObjectNode row = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("nvd-cves", "epss.json")).get("data").get(0);
        ObjectNode nextDay = row.deepCopy();
        nextDay.put("epss", "0.993000000").put("date", "2026-10-14");
        SnapshotContract.assertStableStaticHash(new NvdCvesAdapter("", Duration.ZERO, d -> { }).descriptor(),
                List.of(EpssParser.row(row), EpssParser.row(nextDay)));
    }
}
