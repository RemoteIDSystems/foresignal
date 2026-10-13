package ai.foresignal.source.adapters.spc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * {@link SpcStormReportsParser} (prompt 053) on the SPC CSVs: the three sections, the convective-day dates,
 * magnitudes and units ({@code UNK} omitted), quoted and unquoted commas in comments, the office, and stable
 * externalIds. Fixtures: real {@code today.csv}/{@code yesterday.csv} of 2026-10-13 and a synthetic three-section
 * file (see the fixture README).
 */
class SpcStormReportsParserTest {

    static final String DIR = "spc-storm-reports";
    static final LocalDate DAY = LocalDate.parse("2026-10-12");

    private static Map<String, NormalizedRecord> byLocation(List<NormalizedRecord> records) {
        return records.stream().collect(Collectors.toMap(r -> (String) r.payload().get("location"), r -> r, (a, b) -> a));
    }

    @Test
    @DisplayName("all three sections: tornado, wind and hail rows with their types, magnitudes, units and titles")
    void threeSections() {
        SpcStormReportsParser.ParseResult parsed = SpcStormReportsParser.parse(AdapterFixtures.bytes(DIR, "three-sections.csv"), DAY);
        assertThat(parsed.skipped()).isZero();
        assertThat(parsed.records()).hasSize(7);
        Map<String, NormalizedRecord> r = byLocation(parsed.records());

        NormalizedRecord hail = r.get("5 WNW Oliver");
        assertThat(hail.kind()).isEqualTo("storm-report");
        assertThat(hail.payload()).containsEntry("type", "hail").containsEntry("magnitude", 1.75)
                .containsEntry("magnitudeUnits", "in").containsEntry("county", "Screven").containsEntry("state", "GA")
                .containsEntry("wfo", "CHS").containsEntry("comments", "Golf ball sized hail. (CHS)")
                .containsEntry("title", "Hail 1.75 in — 5 WNW Oliver, GA");
        assertThat(hail.externalId()).isEqualTo("75f97d5856d0");
        Point p = (Point) hail.geometry();
        assertThat(p.getX()).isEqualTo(-81.62);
        assertThat(p.getY()).isEqualTo(32.54);

        assertThat(r.get("22 SE Meadview").payload()).containsEntry("type", "wind").containsEntry("magnitude", 73)
                .containsEntry("magnitudeUnits", "mph").containsEntry("title", "Wind 73 mph — 22 SE Meadview, AZ");
        assertThat(r.get("3 NNE New Hope").payload()).containsEntry("type", "tornado").containsEntry("magnitude", "EF1")
                .containsEntry("magnitudeUnits", "EF").containsEntry("title", "Tornado EF1 — 3 NNE New Hope, NC");
        assertThat(r.get("Near Odessa").payload()).containsEntry("magnitude", 1.0)
                .containsEntry("title", "Hail 1 in — Near Odessa, TX");
    }

    @Test
    @DisplayName("UNK magnitudes are omitted, with their units; the title says only the type")
    void unknownMagnitude() {
        Map<String, NormalizedRecord> r = byLocation(SpcStormReportsParser.parse(
                AdapterFixtures.bytes(DIR, "three-sections.csv"), DAY).records());
        assertThat(r.get("1 NNE Candor").payload()).doesNotContainKeys("magnitude", "magnitudeUnits")
                .containsEntry("type", "tornado").containsEntry("title", "Tornado — 1 NNE Candor, NC");
        assertThat(r.get("2 SSE Peachland").payload()).doesNotContainKeys("magnitude", "magnitudeUnits")
                .containsEntry("title", "Wind — 2 SSE Peachland, NC");
    }

    @Test
    @DisplayName("convective day: HHMM at or after 1200 is on the date, before 1200 on the next date")
    void convectiveDay() {
        Map<String, NormalizedRecord> r = byLocation(SpcStormReportsParser.parse(
                AdapterFixtures.bytes(DIR, "three-sections.csv"), DAY).records());
        assertThat(r.get("22 SE Meadview").observedAt()).isEqualTo(Instant.parse("2026-10-12T12:15:00Z"));
        assertThat(r.get("22 SE Meadview").eventTime()).isEqualTo(Instant.parse("2026-10-12T12:15:00Z"));
        assertThat(r.get("2 SSW Muleshoe").observedAt()).isEqualTo(Instant.parse("2026-10-13T03:53:00Z"));
        assertThat(r.get("Near Odessa").eventTime()).isEqualTo(Instant.parse("2026-10-13T01:15:00Z"));
        assertThat(SpcStormReportsParser.reportTime(DAY, 11, 59)).isEqualTo(Instant.parse("2026-10-13T11:59:00Z"));
        assertThat(SpcStormReportsParser.reportTime(DAY, 12, 0)).isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
    }

    @Test
    @DisplayName("comments: commas inside quotes stay in the comment, an unquoted comma is joined back, at most 500 chars")
    void comments() {
        Map<String, NormalizedRecord> r = byLocation(SpcStormReportsParser.parse(
                AdapterFixtures.bytes(DIR, "three-sections.csv"), DAY).records());
        assertThat(r.get("2 SSE Peachland").payload()).containsEntry("comments",
                "Trees down, wires down, and debris in the road at Horne Town Rd, Mineral Springs Church Rd. (RAH)")
                .containsEntry("wfo", "RAH").containsEntry("county", "Anson");
        assertThat(r.get("Near Odessa").payload()).containsEntry("comments", "Quarter sized hail, covering the ground. (MAF)")
                .containsEntry("wfo", "MAF");
        String longComment = "x".repeat(700);
        NormalizedRecord longRow = SpcStormReportsParser.row("hail",
                SpcStormReportsParser.split("1800,100,Somewhere,County,TX,31.0,-98.0," + longComment), DAY);
        assertThat((String) longRow.payload().get("comments")).hasSize(SpcStormReportsParser.MAX_COMMENT);
        assertThat(longRow.payload()).doesNotContainKey("wfo");
        assertThat(SpcStormReportsParser.split("a,\"b,\"\"c\"\"\",d")).containsExactly("a", "b,\"c\"", "d");
    }

    @Test
    @DisplayName("externalIds are stable across re-parses and differ by type, date, time, place")
    void stableIds() {
        byte[] csv = AdapterFixtures.bytes(DIR, "three-sections.csv");
        List<String> first = SpcStormReportsParser.parse(csv, DAY).records().stream().map(NormalizedRecord::externalId).toList();
        List<String> again = SpcStormReportsParser.parse(csv, DAY).records().stream().map(NormalizedRecord::externalId).toList();
        assertThat(again).isEqualTo(first).doesNotHaveDuplicates().allSatisfy(id -> assertThat(id).matches("[0-9a-f]{12}"));
        List<String> nextDay = SpcStormReportsParser.parse(csv, DAY.plusDays(1)).records().stream()
                .map(NormalizedRecord::externalId).toList();
        assertThat(nextDay).doesNotContainAnyElementsOf(first);
        assertThat(SpcStormReportsParser.externalId("wind", DAY, "0353", "34.21", "-102.74", "2 SSW Muleshoe"))
                .isEqualTo("3177780650fc");
    }

    @Test
    @DisplayName("the real files: today's single wind report, yesterday's 15 tornado and 43 wind reports")
    void realFiles() {
        SpcStormReportsParser.ParseResult today = SpcStormReportsParser.parse(AdapterFixtures.bytes(DIR, "today-20261012.csv"),
                DAY);
        assertThat(today.records()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("3177780650fc");
            assertThat(r.payload()).containsEntry("magnitude", 58).containsEntry("wfo", "LUB")
                    .containsEntry("comments", "(LUB)");
        });
        SpcStormReportsParser.ParseResult yesterday = SpcStormReportsParser.parse(
                AdapterFixtures.bytes(DIR, "yesterday-20261011.csv"), LocalDate.parse("2026-10-11"));
        assertThat(yesterday.skipped()).isZero();
        Map<Object, Long> types = yesterday.records().stream()
                .collect(Collectors.groupingBy(r -> r.payload().get("type"), Collectors.counting()));
        assertThat(types).containsEntry("tornado", 15L).containsEntry("wind", 43L).doesNotContainKey("hail");
    }

    @Test
    @DisplayName("a header-only file is valid and empty; rows before any header are a format error; bad rows are skipped")
    void emptyAndMalformed() {
        assertThat(SpcStormReportsParser.parse(AdapterFixtures.bytes(DIR, "header-only.csv"), DAY).records()).isEmpty();
        assertThat(SpcStormReportsParser.parse(new byte[0], DAY).records()).isEmpty();
        assertThatThrownBy(() -> SpcStormReportsParser.parse(
                "1215,73,X,Y,AZ,35.7,-113.8,c\n".getBytes(StandardCharsets.UTF_8), DAY))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> SpcStormReportsParser.parse(
                "Time,Gust,Location\n".getBytes(StandardCharsets.UTF_8), DAY))
                .isInstanceOf(UpstreamFormatException.class);
        SpcStormReportsParser.ParseResult bad = SpcStormReportsParser.parse(("Time,Size,Location,County,State,Lat,Lon,Comments\n"
                + "12xx,100,A,B,TX,31,-98,c\n2500,100,A,B,TX,31,-98,c\n1300,100,A,B,TX,north,-98,c\n1300,100,A\n")
                .getBytes(StandardCharsets.UTF_8), DAY);
        assertThat(bad.records()).isEmpty();
        assertThat(bad.skipped()).isEqualTo(4);
    }
}
