package ai.foresignal.stats;

import static ai.foresignal.testsupport.stats.StatsTestSources.OTHER_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINTS;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINT_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.retention.RetentionRunResult;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * The region rollup (prompt 119) against a real PostGIS: hour and day rows by 040 region with the cross-source
 * {@code *} rows (private sources left out), the {@code admin1} opt-in, {@code ZZ} for untagged records, the durable
 * cursor across a restart (nothing rolled twice), the consumer clamp and the retention step.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class RegionRollupJobIT extends AnalyticsItSupport {

    @org.springframework.beans.factory.annotation.Autowired ai.foresignal.retention.RetentionSettingsService retentionSettings;
    @org.springframework.beans.factory.annotation.Autowired tools.jackson.databind.json.JsonMapper json;

    @Test
    @DisplayName("hour and day rows by region; ZZ for untagged; * rows exclude private sources; days from raw rows")
    void regions() {
        seed(POINTS, T0.plusSeconds(600), point("de1", POINT_KIND, 10, 50, T0.plusSeconds(600), 1),
                point("ye1", POINT_KIND, 44, 15, T0.plusSeconds(600), 1), plain("x1", OTHER_KIND, T0.plusSeconds(600),
                        1));
        seed(POINTS, H1.plusSeconds(600), point("de1", POINT_KIND, 10, 50, H1.plusSeconds(600), 2));
        seed(SECRET, T0.plusSeconds(900), point("s1", SECRET_KIND, 10, 50, T0.plusSeconds(900), 1));
        tag(POINTS, "de1", "DE", "DE-HE");
        tag(POINTS, "ye1", "YE", null);
        untag(POINTS, "x1");
        tag(SECRET, "s1", "DE", null);

        regionRollUpAt(T0.plus(Duration.ofHours(24)).plus(Duration.ofMinutes(15)));

        assertThat(regionRows(2)).contains(POINTS + "|" + POINT_KIND + "|DE|1", POINTS + "|" + POINT_KIND + "|YE|1",
                POINTS + "|" + OTHER_KIND + "|ZZ|1", "*|" + POINT_KIND + "|DE|1");
        assertThat(regionRows(3)).as("de1 in two hours is one entity in its day")
                .contains(POINTS + "|" + POINT_KIND + "|DE|1", "*|" + POINT_KIND + "|DE|1");
        assertThat(regionRows(3)).noneMatch(r -> r.startsWith("*|" + SECRET_KIND));
        assertThat(jdbc.queryForObject("SELECT versions FROM activity_rollup WHERE resolution = 3 AND source_id = ?"
                + " AND kind = ? AND region_code = 'DE'", Long.class, POINTS, POINT_KIND)).isEqualTo(2L);
        assertThat(jdbc.queryForObject("SELECT cell FROM activity_rollup WHERE resolution = 3 AND source_id = ? AND"
                + " kind = ? AND region_code = 'DE'", Long.class, POINTS, POINT_KIND))
                .isEqualTo(RegionRollupComputer.regionCell("DE"));
        assertThat(rollup.cursor()).as("the H3 rollup is a separate job").isNull();
    }

    @Test
    @DisplayName("statsRegionLevel=admin1 rolls a source up by its first-level region")
    void admin1() {
        settings.put(SettingKeys.STATS_REGION_LEVEL, Map.of(POINTS, "admin1"));
        seed(POINTS, T0.plusSeconds(600), point("tx1", POINT_KIND, -97, 30, T0.plusSeconds(600), 1),
                point("us1", POINT_KIND, -100, 40, T0.plusSeconds(600), 1));
        tag(POINTS, "tx1", "US", "US-TX");
        tag(POINTS, "us1", "US", null);

        regionRollUpAt(T0.plus(Duration.ofMinutes(75)));

        assertThat(regionRows(2)).contains(POINTS + "|" + POINT_KIND + "|US-TX|1", POINTS + "|" + POINT_KIND + "|US|1");
        assertThat(regionRollup.admin1Sources()).containsExactly(POINTS);
        assertThat(RegionRollupComputer.region("US", "US-TX", false)).isEqualTo("US");
        assertThat(RegionRollupComputer.region(null, null, true)).isEqualTo("ZZ");
    }

    @Test
    @DisplayName("the cursor survives a restart: nothing is rolled twice, and it clamps retention")
    void cursorExactlyOnce() {
        seed(POINTS, T0.plusSeconds(600), point("r1", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        tag(POINTS, "r1", "DE", null);
        int hours = regionRollUpAt(T0.plus(Duration.ofMinutes(75)));
        assertThat(hours).isEqualTo(1);
        RollupCursor cursor = regionRollup.cursor();
        assertThat(cursor.lastHour()).isEqualTo(T0);
        long before = jdbc.queryForObject("SELECT coalesce(sum(versions), 0) FROM activity_rollup WHERE resolution = 2",
                Long.class);

        regionRollup.resetState();
        assertThat(regionRollup.cursor()).isEqualTo(cursor);
        assertThat(regionRollUpAt(T0.plus(Duration.ofMinutes(80)))).as("the hour is not rolled again").isZero();
        assertThat(jdbc.queryForObject("SELECT coalesce(sum(versions), 0) FROM activity_rollup WHERE resolution = 2",
                Long.class)).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT value::text FROM app_setting WHERE key = ?", String.class,
                SettingKeys.STATS_REGION_CURSOR)).contains("lastHour");
        assertThat(new RegionRollupCursorConsumer(regionRollup).cursorSeq()).isPresent();
        assertThat(new RegionRollupCursorConsumer(regionRollup).name()).isEqualTo("stats.region.cursor");
    }

    @Test
    @DisplayName("region-hour rows go after stats.regionHourly, region-day rows stay until stats.regionDaily")
    void retention() {
        seed(POINTS, T0.plusSeconds(600), point("k1", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        tag(POINTS, "k1", "DE", null);
        regionRollUpAt(T0.plus(Duration.ofHours(24)).plus(Duration.ofMinutes(15)));
        assertThat(regionRows(2)).isNotEmpty();
        retentionSettings.update(json.readTree("{\"default\": \"7d\", \"stats\": {\"regionHourly\": \"2d\"}}"));
        assertThat(retentionSettings.get().stats().regionHourly()).isEqualTo(Duration.ofDays(2));
        assertThat(StatsRollupRetentionStep.regionWindows(ai.foresignal.settings.RetentionSettings.Stats.defaults()))
                .containsEntry(RegionRollupResolution.HOUR, Duration.ofDays(1095))
                .containsEntry(RegionRollupResolution.DAY, Duration.ofDays(3650));

        RetentionRunResult result = runAt(T0.plus(Duration.ofDays(3)));
        assertThat(result).isNotNull();
        assertThat(regionRows(2)).as("older than 2 days").isEmpty();
        assertThat(regionRows(3)).isNotEmpty();
    }
}
