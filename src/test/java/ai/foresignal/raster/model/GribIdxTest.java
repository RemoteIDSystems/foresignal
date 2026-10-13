package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code .idx} inventories (prompt 051), on real GFS and HRRR inventories of the 2026-10-12 12z runs
 * ({@code fixtures/gfs}, {@code fixtures/hrrr}); the small cases follow God's Eye View's
 * {@code src/data/windGfs.test.mjs} (MIT).
 */
class GribIdxTest {

    static String fixture(String path) {
        return new String(DecoderFixtures.bytes("fixtures/" + path), StandardCharsets.US_ASCII);
    }

    @Test
    @DisplayName("parses a real GFS 0.25° f006 inventory: 743 messages, offsets, date and time range kept")
    void gfsInventory() {
        GribIdx idx = GribIdx.parse(fixture("gfs/gfs.t12z.pgrb2.0p25.f006.idx"));
        assertThat(idx.messages()).hasSize(743);
        GribIdx.Message first = idx.messages().get(0);
        assertThat(first.number()).isEqualTo(1);
        assertThat(first.offset()).isZero();
        assertThat(first.date()).isEqualTo("2026101212");
        assertThat(first.label()).isEqualTo("PRMSL:mean sea level");
        assertThat(first.timeRange()).isEqualTo("6 hour fcst");
        GribIdx.Message u = idx.find(FieldSelector.of("UGRD", "10 m above ground")).orElseThrow();
        assertThat(u.number()).isEqualTo(588);
        assertThat(u.offset()).isEqualTo(426_075_914L);
        assertThat(idx.next(u).orElseThrow().label()).isEqualTo("VGRD:10 m above ground");
        assertThat(idx.find(FieldSelector.of("GUST", "surface")).orElseThrow().offset()).isEqualTo(6_688_508L);
        assertThat(idx.find(FieldSelector.of("TMP", "2 m above ground")).orElseThrow().number()).isEqualTo(581);
        assertThat(idx.isLast(idx.messages().get(742))).isTrue();
        assertThat(idx.next(idx.messages().get(742))).isEmpty();
    }

    @Test
    @DisplayName("the duplicated APCP 0-6 hour acc line takes the first match; the interval accumulation never matches")
    void duplicateApcp() {
        GribIdx idx = GribIdx.parse(fixture("gfs/gfs.t12z.pgrb2.0p25.f006.idx"));
        GribIdx.Message apcp = idx.find(FieldSelector.runTotal("APCP", "surface", 6)).orElseThrow();
        assertThat(apcp.number()).isEqualTo(596);
        assertThat(apcp.offset()).isEqualTo(431_485_461L);
        assertThat(idx.find(FieldSelector.runTotal("APCP", "surface", 3))).isEmpty();
        GribIdx f009 = GribIdx.parse("596:434185047:d=2026101212:APCP:surface:6-9 hour acc fcst:\n"
                + "597:434502963:d=2026101212:APCP:surface:0-9 hour acc fcst:\n");
        assertThat(f009.find(FieldSelector.runTotal("APCP", "surface", 9)).orElseThrow().number()).isEqualTo(597);
    }

    @Test
    @DisplayName("run totals at whole days are written 0-N day acc (verified at f024 … f120)")
    void dayAccumulations() {
        GribIdx f024 = GribIdx.parse("596:438243868:d=2026101212:APCP:surface:18-24 hour acc fcst:\n"
                + "597:438615112:d=2026101212:APCP:surface:0-1 day acc fcst:\n");
        assertThat(f024.find(FieldSelector.runTotal("APCP", "surface", 24)).orElseThrow().number()).isEqualTo(597);
        GribIdx f120 = GribIdx.parse("596:431013718:d=2026101212:APCP:surface:114-120 hour acc fcst:\n"
                + "597:431374059:d=2026101212:APCP:surface:0-5 day acc fcst:\n");
        assertThat(f120.find(FieldSelector.runTotal("APCP", "surface", 120)).orElseThrow().number()).isEqualTo(597);
        GribIdx f060 = GribIdx.parse("1:0:d=x:APCP:surface:0-60 hour acc fcst:\n");
        assertThat(f060.find(FieldSelector.runTotal("APCP", "surface", 6))).as("0-6 is not 0-60").isEmpty();
        assertThat(FieldSelector.runTotal("APCP", "surface", 6).label()).isEqualTo("APCP:surface:0-6 hour acc");
    }

    @Test
    @DisplayName("parses a real HRRR wrfsfcf01 inventory with the four fields 051 reads")
    void hrrrInventory() {
        GribIdx idx = GribIdx.parse(fixture("hrrr/hrrr.t12z.wrfsfcf01.grib2.idx"));
        assertThat(idx.messages()).hasSize(170);
        assertThat(idx.find(FieldSelector.of("REFC", "entire atmosphere")).orElseThrow().offset()).isZero();
        assertThat(idx.find(FieldSelector.of("GUST", "surface")).orElseThrow().offset()).isEqualTo(3_314_894L);
        assertThat(idx.find(FieldSelector.of("MASSDEN", "8 m above ground")).orElseThrow().number()).isEqualTo(76);
        GribIdx.Message u = idx.find(FieldSelector.of("UGRD", "10 m above ground")).orElseThrow();
        assertThat(u.offset()).isEqualTo(52_027_080L);
        assertThat(idx.next(u).orElseThrow().offset()).isEqualTo(54_408_695L);
        GribIdx f00 = GribIdx.parse(fixture("hrrr/hrrr.t12z.wrfsfcf00.grib2.idx"));
        assertThat(f00.find(FieldSelector.of("REFC", "entire atmosphere")).orElseThrow().timeRange()).isEqualTo("anl");
    }

    @Test
    @DisplayName("malformed lines are skipped (God's Eye View cases)")
    void malformedSkipped() {
        GribIdx idx = GribIdx.parse("1:10:d=x:UGRD:10 m above ground:anl:\n"
                + "garbage\n"
                + "x:20:d=x:VGRD:10 m above ground:anl:\n"
                + "2:abc:d=x:VGRD:10 m above ground:anl:\n"
                + "3:30:d=x::level:anl:\n"
                + "4:40:d=x:VAR:\n"
                + "\r\n"
                + "5:50:d=x:VGRD:10 m above ground:anl:\r\n"
                + "6:60:d=x:X:y:z");
        assertThat(idx.messages()).extracting(GribIdx.Message::number).containsExactly(1, 5, 6);
        assertThat(idx.messages().get(1).offset()).isEqualTo(50);
        assertThat(idx.messages().get(1).position()).isEqualTo(1);
    }

    @Test
    @DisplayName("an HTML error page, an empty body or null is never an empty inventory")
    void htmlRejected() {
        String html = "<!DOCTYPE html><html><head><title>404 Not Found</title></head><body>NoSuchKey</body></html>";
        assertThatThrownBy(() -> GribIdx.parse(html)).isInstanceOf(RasterFormatException.class)
                .hasMessageContaining("malformed GRIB2 index");
        assertThatThrownBy(() -> GribIdx.parse("")).isInstanceOf(RasterFormatException.class);
        assertThatThrownBy(() -> GribIdx.parse(null)).isInstanceOf(RasterFormatException.class);
        assertThatThrownBy(() -> GribIdx.parse("<Error><Code>AccessDenied</Code></Error>"))
                .isInstanceOf(RasterFormatException.class);
    }

    @Test
    @DisplayName("lookup is by variable and level, never by message number")
    void byVariableAndLevel() {
        GribIdx idx = GribIdx.parse("1:0:d=x:UGRD:10 mb:anl:\n2:5:d=x:UGRD:10 m above ground:anl:\n"
                + "3:9:d=x:VGRD:10 m above ground:anl:\n");
        assertThat(idx.find(FieldSelector.of("UGRD", "10 m above ground")).orElseThrow().number()).isEqualTo(2);
        assertThat(idx.find(FieldSelector.of("UGRD", "850 mb"))).isEmpty();
        assertThat(idx.all()).hasSize(3);
    }
}
