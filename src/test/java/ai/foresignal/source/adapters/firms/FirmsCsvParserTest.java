package ai.foresignal.source.adapters.firms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/** {@link FirmsCsvParser} against the synthetic fixtures (prompt 065). */
class FirmsCsvParserTest {

    private static final String ID = FirmsAdapter.ID;

    @Test
    @DisplayName("VIIRS: 50 records with the payload, the time base and the geometry")
    void viirs() {
        FirmsCsvParser.Result result = FirmsCsvParser.parse("VIIRS_NOAA20_NRT", AdapterFixtures.text(ID, "viirs.csv"));

        assertThat(result.records()).hasSize(50);
        assertThat(result.rows()).isEqualTo(50);
        assertThat(result.skipped()).isZero();
        assertThat(result.badCoordinates()).isZero();
        NormalizedRecord first = result.records().get(0);
        assertThat(first.kind()).isEqualTo("fire-detection");
        assertThat(first.externalId()).isEqualTo("VIIRS_NOAA20_NRT:2026-10-13:0005:34.9539:-107.6011");
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-10-13T00:05:00Z"));
        assertThat(first.eventTime()).isEqualTo(first.observedAt());
        assertThat(((Point) first.geometry()).getX()).isEqualTo(-107.60109);
        assertThat(((Point) first.geometry()).getY()).isEqualTo(34.95386);
        assertThat(first.payload())
                .containsEntry("source", "VIIRS_NOAA20_NRT")
                .containsEntry("satellite", "N20")
                .containsEntry("instrument", "VIIRS")
                .containsEntry("frpMw", 20.66)
                .containsEntry("brightK", 356.6)
                .containsEntry("bright2K", 286.54)
                .containsEntry("scanKm", 0.53)
                .containsEntry("trackKm", 0.58)
                .containsEntry("daynight", "N")
                .containsEntry("version", "2.0NRT")
                .containsEntry("confidence", "n")
                .containsEntry("confidenceClass", "nominal")
                .doesNotContainKey("type")
                .doesNotContainKey("frp");
        assertThat(result.records().get(1).payload()).containsEntry("confidence", "h")
                .containsEntry("confidenceClass", "high");
        assertThat(result.records().get(2).payload()).containsEntry("confidence", "l")
                .containsEntry("confidenceClass", "low");
    }

    @Test
    @DisplayName("MODIS: brightness and bright_t31, integer confidence, classes at 29/30/79/80")
    void modis() {
        FirmsCsvParser.Result result = FirmsCsvParser.parse("MODIS_NRT", AdapterFixtures.text(ID, "modis.csv"));

        assertThat(result.records()).hasSize(50);
        List<NormalizedRecord> records = result.records();
        assertThat(records.get(0).payload()).containsEntry("instrument", "MODIS").containsEntry("satellite", "A")
                .containsEntry("brightK", 382.6).containsEntry("bright2K", 281.4).containsEntry("confidence", 0)
                .containsEntry("confidenceClass", "low");
        assertThat(records.get(1).payload()).containsEntry("confidence", 29).containsEntry("confidenceClass", "low");
        assertThat(records.get(2).payload()).containsEntry("confidence", 30).containsEntry("confidenceClass", "nominal");
        assertThat(records.get(3).payload()).containsEntry("confidence", 79).containsEntry("confidenceClass", "nominal");
        assertThat(records.get(4).payload()).containsEntry("confidence", 80).containsEntry("confidenceClass", "high");
        assertThat(records.get(5).payload()).containsEntry("confidence", 100).containsEntry("confidenceClass", "high");
    }

    @Test
    @DisplayName("confidence classes: letters, words, the integer boundaries, unknown values")
    void confidenceClasses() {
        assertThat(FirmsCsvParser.confidenceClass("l")).isEqualTo("low");
        assertThat(FirmsCsvParser.confidenceClass("N")).isEqualTo("nominal");
        assertThat(FirmsCsvParser.confidenceClass("h")).isEqualTo("high");
        assertThat(FirmsCsvParser.confidenceClass("high")).isEqualTo("high");
        assertThat(FirmsCsvParser.confidenceClass("29")).isEqualTo("low");
        assertThat(FirmsCsvParser.confidenceClass("30")).isEqualTo("nominal");
        assertThat(FirmsCsvParser.confidenceClass("79")).isEqualTo("nominal");
        assertThat(FirmsCsvParser.confidenceClass("80")).isEqualTo("high");
        assertThat(FirmsCsvParser.confidenceClass("x")).isNull();
        assertThat(FirmsCsvParser.confidenceClass(null)).isNull();
        assertThat(FirmsCsvParser.confidenceClass(" ")).isNull();
    }

    @Test
    @DisplayName("columns are found by name in a shuffled header")
    void shuffledHeader() {
        String text = AdapterFixtures.text(ID, "viirs.csv");
        List<String> lines = text.lines().toList();
        String[] header = lines.get(0).split(",");
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < header.length; i++) {
            order.add(i);
        }
        Collections.shuffle(order, new Random(65));
        StringBuilder shuffled = new StringBuilder();
        for (String line : lines) {
            String[] cells = line.split(",", -1);
            List<String> out = new ArrayList<>();
            for (int i : order) {
                out.add(cells[i]);
            }
            shuffled.append(String.join(",", out)).append('\n');
        }

        FirmsCsvParser.Result original = FirmsCsvParser.parse("VIIRS_NOAA20_NRT", text);
        FirmsCsvParser.Result reordered = FirmsCsvParser.parse("VIIRS_NOAA20_NRT", shuffled.toString());

        assertThat(reordered.records()).hasSameSizeAs(original.records());
        for (int i = 0; i < original.records().size(); i++) {
            assertThat(reordered.records().get(i).externalId()).isEqualTo(original.records().get(i).externalId());
            assertThat(reordered.records().get(i).payload()).isEqualTo(original.records().get(i).payload());
            assertThat(reordered.records().get(i).observedAt()).isEqualTo(original.records().get(i).observedAt());
        }
    }

    @Test
    @DisplayName("unpadded acq_time: 5 is 00:05, 45 is 00:45, 1305 is 13:05; the id pads to four digits")
    void unpaddedTimes() {
        FirmsCsvParser.Result result =
                FirmsCsvParser.parse("VIIRS_NOAA20_NRT", AdapterFixtures.text(ID, "unpadded-times.csv"));

        assertThat(result.records()).extracting(NormalizedRecord::observedAt).containsExactly(
                Instant.parse("2026-10-13T00:05:00Z"), Instant.parse("2026-10-13T00:45:00Z"),
                Instant.parse("2026-10-13T13:05:00Z"));
        assertThat(result.records().get(0).externalId()).isEqualTo("VIIRS_NOAA20_NRT:2026-10-13:0005:40.1235:-105.5432");
        assertThat(result.records().get(1).externalId()).contains(":0045:");
        assertThat(result.records().get(2).externalId()).contains(":1305:");
        assertThat(FirmsCsvParser.acquisition("2026-10-13", "0")).isEqualTo(Instant.parse("2026-10-13T00:00:00Z"));
        assertThat(FirmsCsvParser.acquisition("2026-10-13", "2460")).isNull();
        assertThat(FirmsCsvParser.acquisition("2026-10-13", "12345")).isNull();
        assertThat(FirmsCsvParser.acquisition("2026-13-01", "100")).isNull();
        assertThat(FirmsCsvParser.acquisition(null, "100")).isNull();
    }

    @Test
    @DisplayName("externalId is stable across parses and independent of row order")
    void externalIdStable() {
        String text = AdapterFixtures.text(ID, "viirs.csv");
        List<String> lines = new ArrayList<>(text.lines().toList());
        List<String> reversed = new ArrayList<>(lines.subList(1, lines.size()));
        Collections.reverse(reversed);
        reversed.add(0, lines.get(0));

        List<String> a = FirmsCsvParser.parse("VIIRS_NOAA20_NRT", text).records().stream()
                .map(NormalizedRecord::externalId).sorted().toList();
        List<String> b = FirmsCsvParser.parse("VIIRS_NOAA20_NRT", String.join("\n", reversed)).records().stream()
                .map(NormalizedRecord::externalId).sorted().toList();

        assertThat(a).isEqualTo(b).doesNotHaveDuplicates();
        assertThat(FirmsCsvParser.externalId("MODIS_NRT", "2026-10-13", "45", 1.0, -2.0))
                .isEqualTo("MODIS_NRT:2026-10-13:0045:1.0000:-2.0000");
    }

    @Test
    @DisplayName("a header-only CSV is valid and has zero rows")
    void headerOnly() {
        FirmsCsvParser.Result result =
                FirmsCsvParser.parse("VIIRS_NOAA20_NRT", AdapterFixtures.text(ID, "header-only.csv"));
        assertThat(result.records()).isEmpty();
        assertThat(result.rows()).isZero();
        assertThat(FirmsCsvParser.isLikelyCsv(AdapterFixtures.text(ID, "header-only.csv"))).isTrue();
    }

    @Test
    @DisplayName("rows with invalid coordinates are skipped and counted")
    void badCoordinates() {
        FirmsCsvParser.Result result = FirmsCsvParser.parse("VIIRS_NOAA20_NRT", AdapterFixtures.text(ID, "bad-coords.csv"));
        assertThat(result.records()).hasSize(2);
        assertThat(result.rows()).isEqualTo(7);
        assertThat(result.badCoordinates()).isEqualTo(5);
        assertThat(result.skipped()).isZero();
    }

    @Test
    @DisplayName("a malformed time is skipped; a repeated id keeps its first row")
    void malformedAndRepeated() {
        String header = "latitude,longitude,acq_date,acq_time,frp\n";
        FirmsCsvParser.Result result = FirmsCsvParser.parse("VIIRS_SNPP_NRT",
                header + "1,2,2026-10-13,9x,3\n1,2,2026-10-13,100,3\n1,2,2026-10-13,100,4\n\n");
        assertThat(result.rows()).isEqualTo(3);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).payload()).containsEntry("frpMw", 3.0);
    }

    @Test
    @DisplayName("the byte overload keeps only acquisitions inside [from, to]")
    void range() {
        byte[] body = AdapterFixtures.bytes(ID, "unpadded-times.csv");
        FirmsCsvParser.Result result = FirmsCsvParser.parse("VIIRS_NOAA20_NRT", body,
                Instant.parse("2026-10-13T00:30:00Z"), Instant.parse("2026-10-13T12:00:00Z"));
        assertThat(result.records()).extracting(NormalizedRecord::observedAt)
                .containsExactly(Instant.parse("2026-10-13T00:45:00Z"));
        assertThat(result.outside()).isEqualTo(2);
    }

    @Test
    @DisplayName("isLikelyCsv refuses HTML, the bad-key text, an empty body and a header without the time columns")
    void likelyCsv() {
        assertThat(FirmsCsvParser.isLikelyCsv(AdapterFixtures.text(ID, "viirs.csv"))).isTrue();
        assertThat(FirmsCsvParser.isLikelyCsv("  \r\n" + AdapterFixtures.text(ID, "modis.csv"))).isTrue();
        assertThat(FirmsCsvParser.isLikelyCsv(AdapterFixtures.text(ID, "html-error.html"))).isFalse();
        assertThat(FirmsCsvParser.isLikelyCsv(AdapterFixtures.text(ID, "invalid-key.txt"))).isFalse();
        assertThat(FirmsCsvParser.isLikelyCsv("")).isFalse();
        assertThat(FirmsCsvParser.isLikelyCsv(null)).isFalse();
        assertThat(FirmsCsvParser.isLikelyCsv("latitude,longitude,acq_date\n1,2,3")).isFalse();
        assertThat(FirmsCsvParser.isInvalidKey(AdapterFixtures.text(ID, "invalid-key.txt"))).isTrue();
        assertThat(FirmsCsvParser.isInvalidKey("Upstream returned HTTP 400: INVALID map_key.")).isTrue();
        assertThat(FirmsCsvParser.isInvalidKey(null)).isFalse();
        assertThat(FirmsCsvParser.head("x".repeat(10_000).getBytes(StandardCharsets.UTF_8)))
                .hasSize(FirmsCsvParser.SNIFF_CHARS);
    }

    @Test
    @DisplayName("a body without the FIRMS header is a format error")
    void noHeader() {
        assertThatThrownBy(() -> FirmsCsvParser.parse("MODIS_NRT", "a,b,c\n1,2,3\n"))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("latitude");
        assertThatThrownBy(() -> FirmsCsvParser.parse("MODIS_NRT", ""))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("instrument from the product; MODIS type when present")
    void instrumentAndType() {
        assertThat(FirmsCsvParser.instrument("VIIRS_SNPP_NRT")).isEqualTo("VIIRS");
        assertThat(FirmsCsvParser.instrument("MODIS_NRT")).isEqualTo("MODIS");
        assertThat(FirmsCsvParser.instrument("LANDSAT_NRT")).isEqualTo("OLI");
        assertThat(FirmsCsvParser.instrument("OTHER")).isNull();
        assertThat(FirmsCsvParser.instrument(null)).isNull();
        FirmsCsvParser.Result result = FirmsCsvParser.parse("MODIS_NRT",
                "latitude,longitude,acq_date,acq_time,confidence,type\n1,2,2026-10-13,1200,55,0\n");
        assertThat(result.records().get(0).payload()).containsEntry("type", 0).containsEntry("confidence", 55)
                .containsEntry("confidenceClass", "nominal");
    }
}
