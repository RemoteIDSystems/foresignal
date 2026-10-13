package ai.foresignal.source.adapters.hms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * {@link HmsFireParser} (prompt 048): the padded CSV, columns by header name, the -999 FRP, ids and the
 * time base.
 */
class HmsFireParserTest {

    static final String FILE = "hms_fire20261012.txt";
    static final String HEADER = "        Lon,        Lat, YearDay, Time,       Satellite,           Method, Ecosystem,        FRP\n";

    static byte[] csv(String body) {
        return (HEADER + body).getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("the captured file: 16,777 points, 55 without FRP")
    void captured() {
        HmsFireParser.Result r = HmsFireParser.parse(AdapterFixtures.bytes("hms", FILE));

        assertThat(r.rows()).isEqualTo(16_777);
        assertThat(r.records()).hasSize(16_777);
        assertThat(r.skipped()).isZero();
        assertThat(r.records().stream().filter(x -> !x.payload().containsKey("frpMw")).count()).isEqualTo(55);
        NormalizedRecord first = r.records().get(0);
        assertThat(first.externalId()).isEqualTo("hms:2026285:0201:51.3049:-119.8709");
        assertThat(first.kind()).isEqualTo("fire-detection");
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-10-12T02:01:00Z"));
        assertThat(first.eventTime()).isEqualTo(first.observedAt());
        assertThat(first.payload()).containsEntry("satellite", "GOES-EAST").containsEntry("method", "ANALYSIS")
                .containsEntry("ecosystem", 22).containsEntry("source", "HMS").doesNotContainKey("frpMw");
    }

    @Test
    @DisplayName("padded cells are trimmed; FRP to 2 significant figures; -999 is left out")
    void paddedRow() {
        HmsFireParser.Result r = HmsFireParser.parse(csv(
                " -97.745100,  30.267200, 2026285, 1830,       GOES-EAST,              FDC,        41,     123.456\n"
                        + " -97.000000,  31.000000, 2026285,  905,        NOAA 21,            VIIRS,        12,   -999.000\n"));

        assertThat(r.records()).hasSize(2);
        NormalizedRecord a = r.records().get(0);
        assertThat(a.externalId()).isEqualTo("hms:2026285:1830:30.2672:-97.7451");
        assertThat(a.payload()).containsEntry("frpMw", 120.0).containsEntry("method", "FDC").containsEntry("ecosystem", 41);
        Point p = (Point) a.geometry();
        assertThat(p.getX()).isEqualTo(-97.7451);
        assertThat(p.getY()).isEqualTo(30.2672);
        NormalizedRecord b = r.records().get(1);
        assertThat(b.externalId()).isEqualTo("hms:2026285:0905:31.0000:-97.0000");
        assertThat(b.observedAt()).isEqualTo(Instant.parse("2026-10-12T09:05:00Z"));
        assertThat(b.payload()).doesNotContainKey("frpMw").containsEntry("satellite", "NOAA 21");
    }

    @Test
    @DisplayName("columns are found by name; malformed rows are skipped; duplicates keep one id")
    void columnsAndSkips() {
        byte[] reordered = ("FRP, Lat, Lon, Time, YearDay, Method, Satellite, Ecosystem\n"
                + "  5.5, 30.0, -97.0, 0100, 2026285, FDC, GOES-EAST, 1\n"
                + "  5.5, 30.0, -97.0, 0100, 2026285, FDC, GOES-EAST, 1\n"
                + "  bad, row\n"
                + "  1.0, 95.0, -97.0, 0100, 2026285, FDC, GOES-EAST, 1\n\n").getBytes(StandardCharsets.UTF_8);
        HmsFireParser.Result r = HmsFireParser.parse(reordered);

        assertThat(r.rows()).isEqualTo(4);
        assertThat(r.skipped()).isEqualTo(2);
        assertThat(r.records()).hasSize(1);
        assertThat(r.records().get(0).payload()).containsEntry("frpMw", 5.5);
    }

    @Test
    @DisplayName("an empty file or a missing column is a format error")
    void formatErrors() {
        assertThatThrownBy(() -> HmsFireParser.parse(new byte[0])).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> HmsFireParser.parse("Lon, Lat, YearDay\n1,2,3\n".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("time");
        assertThatThrownBy(() -> HmsFireParser.parse("\n\n".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
