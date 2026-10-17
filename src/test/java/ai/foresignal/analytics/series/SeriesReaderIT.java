package ai.foresignal.analytics.series;

import static ai.foresignal.testsupport.stats.StatsTestSources.POINTS;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINT_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * The series reader (prompt 119) over each reference type: {@code knownAt} under {@code asOf} (INT-2), revisions,
 * explicit gaps, rollup zeros and coverage flags, rule samples summed to hours, and SERIES_NOT_FOUND.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class SeriesReaderIT extends AnalyticsItSupport {

    @org.springframework.beans.factory.annotation.Autowired ai.foresignal.trigger.TriggerRuleService rules;
    @org.springframework.beans.factory.annotation.Autowired tools.jackson.databind.json.JsonMapper json;

    @BeforeEach
    void clearCoverageCache() {
        reader.clearCache();
    }

    @Test
    @DisplayName("series: points carry the ingest time of their version; asOf hides later versions and revisions")
    void seriesPoints() {
        SeriesMeta meta = SeriesMeta.of(SeriesMeta.PRICE, SeriesMeta.LOG_RETURN, 1);
        seedDaily(AnalyticsTestSources.SERIES, "oil", T0, new double[] {70, 71, 72}, meta);
        Instant revised = T0.plus(Duration.ofDays(5));
        seed(AnalyticsTestSources.SERIES, revised, dailyPoint("oil", T0, 69.5, revised));

        SeriesData now = reader.read(SeriesRef.series(AnalyticsTestSources.SERIES, "oil"), T0,
                T0.plus(Duration.ofDays(4)), null);
        assertThat(now.frequency()).isEqualTo(SeriesFrequency.DAY);
        assertThat(now.seriesFamily()).isEqualTo("price");
        assertThat(now.points()).hasSize(3);
        assertThat(now.points().get(0).value()).as("the revision").isEqualTo(69.5);
        assertThat(now.points().get(0).knownAt()).isEqualTo(revised);
        assertThat(now.gaps()).hasSize(1);

        SeriesData before = reader.read(SeriesRef.series(AnalyticsTestSources.SERIES, "oil"), T0,
                T0.plus(Duration.ofDays(4)), T0.plus(Duration.ofDays(2)).plusSeconds(90));
        assertThat(before.points()).as("only points published by then").hasSize(2);
        assertThat(before.points().get(0).value()).as("the original, before its revision").isEqualTo(70.0);
        assertThat(before.points()).allMatch(p -> !p.knownAt().isAfter(T0.plus(Duration.ofDays(2)).plusSeconds(90)));
        assertThat(before.asOfSeq()).isLessThan(now.asOfSeq());
    }

    @Test
    @DisplayName("rollup: a rolled-up bucket without rows is zero when covered; uncovered buckets are gaps")
    void rollups() {
        coverDays(POINTS, T0.minus(Duration.ofDays(2)), T0.plus(Duration.ofHours(2)));
        seed(POINTS, T0.plusSeconds(600), point("de1", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        tag(POINTS, "de1", "DE", null);
        regionRollUpAt(T0.plus(Duration.ofMinutes(135)));

        SeriesData data = reader.read(SeriesRef.rollup("hour", POINTS, POINT_KIND, "DE"), T0,
                T0.plus(Duration.ofHours(3)), null);
        assertThat(data.counts()).isTrue();
        assertThat(data.points()).hasSize(2);
        assertThat(data.points().get(0).value()).isEqualTo(1.0);
        assertThat(data.points().get(0).knownAt()).as("rollups are known at bucket close")
                .isEqualTo(T0.plus(Duration.ofHours(1)));
        assertThat(data.points().get(1).value()).as("covered and empty: zero").isEqualTo(0.0);
        assertThat(data.gaps()).as("hour three is not rolled up yet").hasSize(1);

        SeriesData all = reader.read(SeriesRef.rollup("hour", SeriesRef.ALL_SOURCES, POINT_KIND, "DE"), T0,
                T0.plus(Duration.ofHours(2)), T0.plus(Duration.ofHours(1)));
        assertThat(all.points()).as("asOf hides the bucket that closed later").hasSize(1);
        assertThat(all.sources()).isEmpty();
    }

    @Test
    @DisplayName("rule: anomaly samples summed to hours with their coverage flags")
    void ruleSamples() {
        UUID rule = rules.create(json.readTree("{\"name\":\"anomaly test\",\"type\":\"anomaly\",\"sources\":[\""
                + POINTS + "\"],\"kinds\":[\"" + POINT_KIND + "\"],\"tags\":[\"seismic\"],"
                + "\"window\":{\"window\":\"1h\",\"direction\":\"both\",\"minWeeks\":8}}")).rule().getId();
        jdbc.update("INSERT INTO activity_sample (rule_id, group_key, bucket_start, count, coverage_ok, flagged)"
                + " VALUES (?, 'YE', ?, 4, true, false), (?, 'YE', ?, 6, false, false)", rule, Timestamp.from(T0), rule,
                Timestamp.from(T0.plus(Duration.ofHours(1))));
        SeriesData data = reader.read(SeriesRef.rule(rule, "YE"), T0, T0.plus(Duration.ofHours(3)), null);
        assertThat(data.signalFamily()).isEqualTo("seismic");
        assertThat(data.regionCode()).isEqualTo("YE");
        assertThat(data.points()).hasSize(2);
        assertThat(data.points().get(0).value()).isEqualTo(4.0);
        assertThat(data.points().get(1).coverageOk()).isFalse();
        assertThat(data.points().get(1).value()).isNull();
        assertThat(data.gaps()).hasSize(1);
    }

    @Test
    @DisplayName("unknown series, rules and rollups are SERIES_NOT_FOUND")
    void notFound() {
        assertThatThrownBy(() -> reader.meta(SeriesRef.series(AnalyticsTestSources.SERIES, "nope")))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> reader.meta(SeriesRef.rule(UUID.randomUUID(), "YE")))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> reader.meta(SeriesRef.rollup("day", POINTS, "never-seen", "DE")))
                .isInstanceOf(NotFoundException.class);
        assertThat(reader.exists(SeriesRef.series(AnalyticsTestSources.SERIES, "nope"))).isFalse();
    }
}
