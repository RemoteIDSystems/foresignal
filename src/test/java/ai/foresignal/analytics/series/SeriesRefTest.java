package ai.foresignal.analytics.series;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Series references and data (prompt 119): parse and format of the three forms, SERIES_REF_INVALID, grids. */
class SeriesRefTest {

    @Test
    @DisplayName("series:, rollup: (region, cell, *) and rule: references round-trip")
    void roundTrip() {
        SeriesRef series = SeriesRef.parse("series:cftc-cot/cot.067651.disaggregated.mm_net");
        assertThat(series.type()).isEqualTo(SeriesRef.Type.SERIES);
        assertThat(series.sourceId()).isEqualTo("cftc-cot");
        assertThat(series.seriesId()).isEqualTo("cot.067651.disaggregated.mm_net");
        assertThat(SeriesRef.parse("series:wikimedia-pageviews/wiki.en.wikipedia.A/B").seriesId())
                .isEqualTo("wiki.en.wikipedia.A/B");
        SeriesRef rollup = SeriesRef.parse("rollup:day/acled/conflict-event/YE");
        assertThat(rollup.region()).isEqualTo("YE");
        assertThat(rollup.regionCode()).isEqualTo("YE");
        assertThat(rollup.allSources()).isFalse();
        assertThat(SeriesRef.parse("rollup:hour/*/earthquake/US-CA").allSources()).isTrue();
        SeriesRef cell = SeriesRef.parse("rollup:day/usgs-earthquakes/earthquake/cell:8428309ffffffff");
        assertThat(cell.cell()).isEqualTo("8428309ffffffff");
        assertThat(cell.region()).isNull();
        UUID id = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");
        SeriesRef rule = SeriesRef.parse("rule:" + id + "/YE");
        assertThat(rule.ruleId()).isEqualTo(id);
        assertThat(rule.regionCode()).isEqualTo("YE");
        for (String text : new String[] {"series:cftc-cot/cot.067651.disaggregated.mm_net",
            "rollup:day/acled/conflict-event/YE", "rollup:hour/*/earthquake/US-CA",
            "rollup:day/usgs-earthquakes/earthquake/cell:8428309ffffffff", "rule:" + id + "/YE"}) {
            assertThat(SeriesRef.parse(text).format()).isEqualTo(text);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "nothing", "series:", "series:/x", "series:*/x", "rollup:week/a/b/YE",
        "rollup:day/a/b", "rollup:day/a/b/usa", "rollup:day/a/b/cell:zz", "rule:not-a-uuid/YE", "social:x",
        "other:x"})
    @DisplayName("malformed references are 400 SERIES_REF_INVALID")
    void invalid(String text) {
        assertThatThrownBy(() -> SeriesRef.parse(text)).isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo("SERIES_REF_INVALID"));
    }

    @Test
    @DisplayName("the grid fills missing buckets as gaps, knownBy hides later-known values, counts resample by sum")
    void grid() {
        Instant d0 = Instant.parse("2026-03-02T00:00:00Z");
        List<SeriesValue> points = List.of(
                new SeriesValue(d0, d0.plusSeconds(86_400), 3.0, d0.plusSeconds(86_400), true, null, null, null),
                new SeriesValue(d0.plusSeconds(2 * 86_400), d0.plusSeconds(3 * 86_400), 5.0,
                        d0.plusSeconds(10 * 86_400), true, null, null, null));
        SeriesData data = new SeriesData(SeriesRef.rollup("day", "*", "earthquake", "US"), d0,
                d0.plusSeconds(7 * 86_400), null, 0, SeriesFrequency.DAY, "count", "seismic", "count", "log-level", 0,
                "US", null, Set.of(), false, points,
                SeriesReader.gapsOf(SeriesFrequency.DAY, d0, d0.plusSeconds(7 * 86_400), points));
        SeriesData.Grid grid = data.grid();
        assertThat(grid.size()).isEqualTo(7);
        assertThat(grid.gap()).containsExactly(false, true, false, true, true, true, true);
        assertThat(data.gaps()).hasSize(2);
        SeriesData.Grid known = grid.knownBy(d0.plusSeconds(5 * 86_400));
        assertThat(known.gap()[2]).as("a value published after the decision time is not used").isTrue();
        SeriesData weekly = data.resample(SeriesFrequency.WEEK);
        assertThat(weekly.points()).as("a week with uncovered days is not covered").isEmpty();
        assertThat(SeriesFrequency.WEEK.floor(Instant.parse("2026-03-05T12:00:00Z"))).isEqualTo(d0);
        assertThat(SeriesFrequency.ofIsoPeriod("P1W")).isEqualTo(SeriesFrequency.WEEK);
        assertThat(SeriesFrequency.ofIsoPeriod("PT15M")).isEqualTo(SeriesFrequency.IRREGULAR);
    }
}
