package ai.foresignal.source.adapters.gdelt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * {@link GdeltExportParser} against the trimmed live export of 2026-10-13 14:30 UTC (prompt 066): the default filter,
 * every mapped column, leading-zero codes, location precision, the summary, and malformed-line counting.
 */
class GdeltExportParserTest {

    private static final String ID = GdeltEventsAdapter.ID;
    private static final CountryCentroids COUNTRIES = new CountryCentroids();
    private static final Function<String, String> FIPS_TO_ISO =
            code -> COUNTRIES.byFips(code).map(CountryRef::iso2).orElse(null);

    private static byte[] tsv() {
        return AdapterFixtures.bytes(ID, "export-40.tsv");
    }

    private static GdeltExportParser.Result parse(GdeltExportParser.Options options) {
        return GdeltExportParser.parse(tsv(), options, FIPS_TO_ISO);
    }

    private static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("the default filter keeps the 28 root 13-20 rows with a point, of 40")
    void defaultFilter() {
        GdeltExportParser.Result result = parse(GdeltExportParser.Options.DEFAULTS);
        assertThat(result.rows()).isEqualTo(40);
        assertThat(result.bad()).isZero();
        assertThat(result.records()).hasSize(28);
        assertThat(result.filtered()).isEqualTo(12);
        assertThat(result.records()).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("conflict-event");
            assertThat(Integer.parseInt((String) r.payload().get("eventRootCode"))).isBetween(13, 20);
            assertThat(r.geometry()).isNotNull();
        });
    }

    @Test
    @DisplayName("every mapped column of one row")
    void mapping() {
        NormalizedRecord r = byId(parse(GdeltExportParser.Options.DEFAULTS).records(), "1327721728");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T14:30:00Z"));
        assertThat(r.eventTime()).isEqualTo(Instant.parse("2025-10-13T00:00:00Z"));
        Point point = (Point) r.geometry();
        assertThat(point.getX()).isEqualTo(34.3063);
        assertThat(point.getY()).isEqualTo(31.3402);
        assertThat(point.getSRID()).isEqualTo(4326);
        Map<String, Object> p = r.payload();
        assertThat(p).containsEntry("eventCode", "186").containsEntry("eventBaseCode", "186")
                .containsEntry("eventRootCode", "18").containsEntry("cameoRoot", "Assault")
                .containsEntry("quadClass", 4).containsEntry("goldstein", -10.0).containsEntry("numMentions", 4)
                .containsEntry("numSources", 1).containsEntry("numArticles", 4).containsEntry("avgTone", -4.5)
                .containsEntry("actor1Name", "ISRAEL").containsEntry("actor1Country", "ISR")
                .containsEntry("actor2Name", "GAZA").containsEntry("actor2Country", "PSE")
                .containsEntry("actionGeoName", "Khan Yunis, Gaza Strip (general), Gaza Strip")
                .containsEntry("actionGeoCountry", "GZ").containsEntry("geoPrecision", "city")
                .containsEntry("geometryApproximate", false).containsEntry("isRoot", true)
                .containsEntry("sourceUrl", "https://www.i24news.tv/en/news/israel-at-war/artc-sources-to-i24news-"
                        + "assassination-of-khan-yunis-commander-was-accidental-who-was-the-real-target")
                .containsEntry("summary", "ISRAEL — Assault — GAZA in Khan Yunis, Gaza Strip (general), Gaza Strip");
        assertThat(p).as("GZ is not in the 040 country table").doesNotContainKey("actionGeoCountryIso");

        NormalizedRecord israel = byId(parse(GdeltExportParser.Options.DEFAULTS).records(), "1327721729");
        assertThat(israel.payload()).containsEntry("actionGeoCountry", "IS").containsEntry("actionGeoCountryIso", "IL")
                .containsEntry("cameoRoot", "Fight").containsEntry("geoPrecision", "country")
                .containsEntry("geometryApproximate", true);

        NormalizedRecord korea = byId(parse(GdeltExportParser.Options.DEFAULTS).records(), "1327721731");
        assertThat(korea.payload()).containsEntry("actionGeoCountryIso", "KR").containsEntry("isRoot", true)
                .doesNotContainKey("actor1Name").doesNotContainKey("actor1Country")
                .containsEntry("summary", "Unknown actor — Protest — HOSPITAL in South Korea");
    }

    @Test
    @DisplayName("without a crosswalk, actionGeoCountryIso is left out")
    void noCrosswalk() {
        NormalizedRecord r = byId(GdeltExportParser.parse(tsv(), GdeltExportParser.Options.DEFAULTS, null).records(),
                "1327721729");
        assertThat(r.payload()).containsEntry("actionGeoCountry", "IS").doesNotContainKey("actionGeoCountryIso");
    }

    @Test
    @DisplayName("event codes stay strings with their leading zeros")
    void leadingZeros() {
        GdeltExportParser.Result result = parse(new GdeltExportParser.Options(Set.of("03"), 1, true, Set.of()));
        assertThat(result.records()).hasSize(1);
        Map<String, Object> p = result.records().getFirst().payload();
        assertThat(p).containsEntry("eventCode", "0356").containsEntry("eventBaseCode", "035")
                .containsEntry("eventRootCode", "03").containsEntry("cameoRoot", "Express intent to cooperate")
                .containsEntry("avgTone", -5.66).doesNotContainKey("actor2Name");
        assertThat(p.get("summary")).isEqualTo("POLAND — Express intent to cooperate — in Leszczydol-Nowiny,"
                + " Mazowieckie, Poland");

        assertThat(parse(new GdeltExportParser.Options(Set.of("2"), 1, true, Set.of())).records())
                .as("a one-digit root code is padded").hasSize(2)
                .allSatisfy(r -> assertThat(r.payload().get("eventRootCode")).isEqualTo("02"));
    }

    @Test
    @DisplayName("geoPrecision: 1 country, 2 and 5 adm1, 3 and 4 city; country and adm1 are approximate")
    void precision() {
        List<NormalizedRecord> records = parse(GdeltExportParser.Options.DEFAULTS).records();
        assertThat(byId(records, "1327721783").payload()).containsEntry("geoPrecision", "adm1")
                .containsEntry("geometryApproximate", true);
        assertThat(byId(records, "1327722298").payload()).containsEntry("geoPrecision", "adm1")
                .containsEntry("geometryApproximate", true);
        assertThat(byId(records, "1327721782").payload()).containsEntry("geoPrecision", "city")
                .containsEntry("geometryApproximate", false);
        assertThat(GdeltExportParser.precision(1)).isEqualTo("country");
        assertThat(GdeltExportParser.precision(2)).isEqualTo("adm1");
        assertThat(GdeltExportParser.precision(3)).isEqualTo("city");
        assertThat(GdeltExportParser.precision(4)).isEqualTo("city");
        assertThat(GdeltExportParser.precision(5)).isEqualTo("adm1");
        assertThat(GdeltExportParser.precision(0)).isNull();
        assertThat(GdeltExportParser.precision(null)).isNull();
    }

    @Test
    @DisplayName("requireGeo=false keeps the event without a point, with no geometry and no precision")
    void noGeo() {
        GdeltExportParser.Result result = parse(new GdeltExportParser.Options(null, 1, false, Set.of()));
        assertThat(result.records()).hasSize(29);
        NormalizedRecord r = byId(result.records(), "1327721819");
        assertThat(r.geometry()).isNull();
        assertThat(r.payload()).doesNotContainKeys("geoPrecision", "geometryApproximate", "actionGeoName",
                "actionGeoCountry", "actionGeoCountryIso").containsEntry("eventCode", "1823")
                .containsEntry("summary", "Unknown actor — Assault — UTTAR PRADESH");
    }

    @Test
    @DisplayName("minMentions and countries filter")
    void filters() {
        assertThat(parse(new GdeltExportParser.Options(null, 5, true, Set.of())).records()).hasSize(12)
                .allSatisfy(r -> assertThat((Integer) r.payload().get("numMentions")).isGreaterThanOrEqualTo(5));
        assertThat(parse(new GdeltExportParser.Options(null, 1, true, Set.of("us"))).records()).hasSize(10)
                .allSatisfy(r -> assertThat(r.payload()).containsEntry("actionGeoCountry", "US")
                        .containsEntry("actionGeoCountryIso", "US"));
        assertThat(parse(new GdeltExportParser.Options(null, 1, true, Set.of("YM", "IS"))).records()).hasSize(4);
        assertThat(new GdeltExportParser.Options(null, 0, true, null).minMentions()).isEqualTo(1);
        assertThat(new GdeltExportParser.Options(Set.of(), 1, true, null).rootCodes())
                .containsExactlyInAnyOrderElementsOf(GdeltExportParser.Options.DEFAULT_ROOT_CODES);
    }

    @Test
    @DisplayName("the summary is deterministic, collapses whitespace and is cut to 200 characters")
    void summary() {
        List<NormalizedRecord> once = parse(GdeltExportParser.Options.DEFAULTS).records();
        List<String> lines = new ArrayList<>(List.of(new String(tsv(), StandardCharsets.UTF_8).split("\n")));
        Collections.reverse(lines);
        List<NormalizedRecord> reversed = GdeltExportParser.parse(String.join("\n", lines)
                .getBytes(StandardCharsets.UTF_8), GdeltExportParser.Options.DEFAULTS, FIPS_TO_ISO).records();
        for (NormalizedRecord r : once) {
            assertThat(byId(reversed, r.externalId()).payload()).isEqualTo(r.payload());
            assertThat(((String) r.payload().get("summary")).length()).isLessThanOrEqualTo(200);
        }
        assertThat(GdeltExportParser.summary("  A \t B ", "Fight", "", "  Here  ")).isEqualTo("A B — Fight — in Here");
        assertThat(GdeltExportParser.summary(null, "Fight", null, null)).isEqualTo("Unknown actor — Fight —");
        String long1 = "X".repeat(300);
        String cut = GdeltExportParser.summary(long1, "Assault", "Y", "Z");
        assertThat(cut).hasSize(200).endsWith("…").startsWith("XXXX");
        assertThat(GdeltExportParser.summary(long1, "Assault", "Y", "Z")).isEqualTo(cut);
        String surrogates = "😀".repeat(150);
        assertThat(GdeltExportParser.summary(surrogates, "Fight", "", "").length()).isLessThanOrEqualTo(200);
        assertThat(GdeltExportParser.clean("a\u0000b\r\nc")).isEqualTo("a b c");
    }

    @Test
    @DisplayName("malformed rows are counted; more than 5% refuses the file")
    void malformed() {
        assertThatThrownBy(() -> GdeltExportParser.parse(AdapterFixtures.bytes(ID, "short-rows.tsv"),
                GdeltExportParser.Options.DEFAULTS, FIPS_TO_ISO))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("2 malformed lines of 20");

        String shortRow = "1\t20261013\tonly three\n";
        String text = new String(tsv(), StandardCharsets.UTF_8) + shortRow + shortRow;
        GdeltExportParser.Result result = GdeltExportParser.parse(text.getBytes(StandardCharsets.UTF_8),
                GdeltExportParser.Options.DEFAULTS, FIPS_TO_ISO);
        assertThat(result.rows()).isEqualTo(42);
        assertThat(result.bad()).as("2 of 42 is under 5%").isEqualTo(2);
        assertThat(result.records()).hasSize(28);

        String[] cells = new String(tsv(), StandardCharsets.UTF_8).split("\n")[1].split("\t", -1);
        cells[GdeltExportParser.DATE_ADDED] = "20261399143000";
        String badTime = String.join("\t", cells);
        cells[GdeltExportParser.DATE_ADDED] = "20261013143000";
        cells[GdeltExportParser.GLOBAL_EVENT_ID] = "abc";
        String badId = String.join("\t", cells);
        GdeltExportParser.Result unreadable = GdeltExportParser.parse((text.replace(shortRow, "") + badTime + "\n"
                + badId + "\r\n\n").getBytes(StandardCharsets.UTF_8), GdeltExportParser.Options.DEFAULTS, FIPS_TO_ISO);
        assertThat(unreadable.bad()).isEqualTo(2);
        assertThat(unreadable.rows()).isEqualTo(42);

        String overlong = "y".repeat(GdeltExportParser.MAX_LINE_BYTES + 10) + "\n";
        GdeltExportParser.Result longLine = GdeltExportParser.parse((new String(tsv(), StandardCharsets.UTF_8)
                + overlong).getBytes(StandardCharsets.UTF_8), GdeltExportParser.Options.DEFAULTS, FIPS_TO_ISO);
        assertThat(longLine.bad()).isEqualTo(1);
        assertThat(longLine.records()).hasSize(28);

        assertThat(GdeltExportParser.parse(new byte[0], null, null).rows()).isZero();
    }

    @Test
    @DisplayName("the CAMEO root table has the twenty roots")
    void cameo() {
        assertThat(CameoRoots.CODES).hasSize(20).startsWith("01").endsWith("20");
        assertThat(CameoRoots.label("18")).isEqualTo("Assault");
        assertThat(CameoRoots.label("19")).isEqualTo("Fight");
        assertThat(CameoRoots.label("20")).isEqualTo("Use unconventional mass violence");
        assertThat(CameoRoots.label("21")).isNull();
        assertThat(CameoRoots.label(null)).isNull();
    }
}
