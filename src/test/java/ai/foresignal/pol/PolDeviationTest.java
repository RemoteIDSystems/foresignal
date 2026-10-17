package ai.foresignal.pol;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.HealthStatus;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The deviation rules (prompt 114): {@code NEW_PLACE} only after the 14-day warm-up, {@code UNUSUAL_TIME} bucket maths,
 * {@code EXPECTED_ABSENT} on its 10-minute tick with health gating, and the nightly recompute schedule.
 */
class PolDeviationTest {

    static final Instant MONDAY_8 = Instant.parse("2026-10-05T08:00:00Z");

    @Test
    @DisplayName("NEW_PLACE needs 14 days of history; before the warm-up it is not reported")
    void newPlaceWarmUp() {
        Instant start = MONDAY_8.minus(Duration.ofDays(20));
        assertThat(PolDeviations.isNewPlace(start.plus(Duration.ofDays(13)), start)).isFalse();
        assertThat(PolDeviations.isNewPlace(start.plus(Duration.ofDays(14)), start)).isTrue();
        assertThat(PolDeviations.isNewPlace(MONDAY_8, null)).as("no history at all").isFalse();
    }

    @Test
    @DisplayName("the hour-of-week bucket counts from Monday 00:00 in the operator's zone")
    void buckets() {
        assertThat(PolStats.bucket(MONDAY_8, ZoneOffset.UTC)).isEqualTo(8);
        assertThat(PolStats.bucket(Instant.parse("2026-10-11T23:30:00Z"), ZoneOffset.UTC)).isEqualTo(167);
        // 08:00 UTC is 03:00 in Chicago (CDT, UTC−5).
        assertThat(PolStats.bucket(MONDAY_8, ZoneId.of("America/Chicago"))).isEqualTo(3);
        assertThat(PolStats.bucketStart(Instant.parse("2026-10-05T08:42:10Z"), ZoneOffset.UTC)).isEqualTo(MONDAY_8);
    }

    @Test
    @DisplayName("dwell is spread over the hours a stay covers")
    void addDwell() {
        int[] h = new int[PolStats.BUCKETS];
        PolStats.addDwell(h, Instant.parse("2026-10-05T08:30:00Z"), Instant.parse("2026-10-05T10:15:00Z"), ZoneOffset.UTC);
        assertThat(h[8]).isEqualTo(30);
        assertThat(h[9]).isEqualTo(60);
        assertThat(h[10]).isEqualTo(15);
        assertThat(PolStats.total(h)).isEqualTo(105);
    }

    @Test
    @DisplayName("UNUSUAL_TIME: a bucket under 2% of the place's dwell is unusual; 2% or more is not")
    void unusualTime() {
        int[] h = new int[PolStats.BUCKETS];
        h[8] = 980;
        h[20] = 19;
        h[21] = 1;
        assertThat(PolDeviations.unusualTimeShare(h, 20)).isCloseTo(0.019, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(PolDeviations.unusualTimeShare(h, 3)).isEqualTo(0.0);
        assertThat(PolDeviations.unusualTimeShare(h, 8)).isNull();
        h[20] = 21;
        assertThat(PolDeviations.unusualTimeShare(h, 20)).as("21 of 1002 is above 2%").isNull();
        assertThat(PolDeviations.unusualTimeShare(new int[PolStats.BUCKETS], 4)).as("no dwell yet").isNull();
    }

    @Test
    @DisplayName("EXPECTED_ABSENT: a place holding at least 60% of the bucket and 30 minutes is expected")
    void expectedPlace() {
        int[] home = new int[PolStats.BUCKETS];
        int[] work = new int[PolStats.BUCKETS];
        home[2] = 240;
        work[2] = 60;
        work[10] = 200;
        home[10] = 150;
        PolDeviations.Expected e = PolDeviations.expectedPlace(List.of(home, work), 2);
        assertThat(e.index()).isZero();
        assertThat(e.share()).isEqualTo(0.8);
        assertThat(PolDeviations.expectedPlace(List.of(home, work), 10)).as("57% is not enough").isNull();
        int[] thin = new int[PolStats.BUCKETS];
        thin[5] = 20;
        assertThat(PolDeviations.expectedPlace(List.of(thin), 5)).as("under 30 minutes").isNull();
        assertThat(PolDeviations.expectedPlace(List.of(thin), 6)).isNull();
    }

    @Test
    @DisplayName("absent: elsewhere for more than an hour, or unseen for more than an hour")
    void absent() {
        Instant now = MONDAY_8;
        assertThat(PolDeviations.absent(now, true, now.minusSeconds(60), now.minusSeconds(60))).isFalse();
        assertThat(PolDeviations.absent(now, false, now.minusSeconds(60), now.minus(Duration.ofMinutes(50))))
                .as("away for 50 minutes").isFalse();
        assertThat(PolDeviations.absent(now, false, now.minusSeconds(60), now.minus(Duration.ofMinutes(61)))).isTrue();
        assertThat(PolDeviations.absent(now, true, now.minus(Duration.ofMinutes(90)), now.minus(Duration.ofMinutes(90))))
                .as("unseen for 90 minutes").isTrue();
        assertThat(PolDeviations.absent(now, false, null, null)).isTrue();
    }

    @Test
    @DisplayName("the EXPECTED_ABSENT check runs on a 10-minute tick")
    void tick() {
        assertThat(PolDeviations.absentCheckDue(null, MONDAY_8)).isTrue();
        assertThat(PolDeviations.absentCheckDue(MONDAY_8.minus(Duration.ofMinutes(9)), MONDAY_8)).isFalse();
        assertThat(PolDeviations.absentCheckDue(MONDAY_8.minus(Duration.ofMinutes(10)), MONDAY_8)).isTrue();
    }

    @Test
    @DisplayName("health gating: only when every member source is healthy")
    void healthGate() {
        assertThat(PolDeviations.healthy(Set.of(HealthStatus.HEALTHY))).isTrue();
        assertThat(PolDeviations.healthy(Set.of(HealthStatus.HEALTHY, HealthStatus.DEGRADED))).isFalse();
        assertThat(PolDeviations.healthy(Set.of(HealthStatus.FAILING))).isFalse();
        assertThat(PolDeviations.healthy(Set.of(HealthStatus.IDLE))).isFalse();
        assertThat(PolDeviations.healthy(Set.of())).as("no member at all").isFalse();
    }

    @Test
    @DisplayName("the nightly recompute is due once after each 03:00 local")
    void nightly() {
        ZoneId zone = ZoneId.of("America/Chicago");
        Instant at0230 = Instant.parse("2026-10-05T07:30:00Z");
        Instant at0330 = Instant.parse("2026-10-05T08:30:00Z");
        assertThat(PolEngine.recomputeDue(null, at0230, zone)).isTrue();
        assertThat(PolEngine.recomputeDue(at0230.minus(Duration.ofHours(1)), at0230, zone)).isFalse();
        assertThat(PolEngine.recomputeDue(at0230, at0330, zone)).isTrue();
        assertThat(PolEngine.recomputeDue(at0330, at0330.plus(Duration.ofHours(10)), zone)).isFalse();
    }

    @Test
    @DisplayName("place statistics: centroid, p90 radius, visits, dwell and seen times")
    void placeStats() {
        List<StayPointExtractor.Stay> stays = List.of(
                new StayPointExtractor.Stay(MONDAY_8, MONDAY_8.plus(Duration.ofMinutes(60)), -97.7400, 30.2700, 10, "s/a"),
                new StayPointExtractor.Stay(MONDAY_8.plus(Duration.ofDays(1)),
                        MONDAY_8.plus(Duration.ofDays(1)).plus(Duration.ofMinutes(30)), -97.7402, 30.2700, 6, "s/a"),
                new StayPointExtractor.Stay(MONDAY_8.plus(Duration.ofDays(2)),
                        MONDAY_8.plus(Duration.ofDays(2)).plus(Duration.ofMinutes(30)), -97.7404, 30.2700, 6, "s/a"));
        LocalFrame frame = LocalFrame.around(List.of(new double[] {-97.74, 30.27}));
        PolStats.PlaceStats p = PolStats.place(stays, frame, ZoneOffset.UTC);
        assertThat(p.visits()).isEqualTo(3);
        assertThat(p.totalDwellMinutes()).isEqualTo(120.0);
        assertThat(p.lon()).isCloseTo(-97.7402, org.assertj.core.data.Offset.offset(1e-7));
        assertThat(p.radiusM()).isBetween(18.0, 20.0);
        assertThat(p.firstSeen()).isEqualTo(MONDAY_8);
        assertThat(p.hourOfWeek()[8]).isEqualTo(60);
        assertThat(PolStats.percentile90(new double[0])).isZero();
        assertThat(PolViews.typicalHours(p.hourOfWeek())).first().isEqualTo("Mon 08:00");
    }
}
