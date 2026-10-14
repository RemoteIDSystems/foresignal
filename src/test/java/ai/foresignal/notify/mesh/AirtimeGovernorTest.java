package ai.foresignal.notify.mesh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The airtime governor (prompt 084): one-hour window accounting, the ceiling per region, a share above the ceiling
 * refused, spacing and hourly cap deferrals, critical priority capped by the ceiling, drops, and the window kept
 * across a settings change.
 */
class AirtimeGovernorTest {

    static final Instant T0 = Instant.parse("2026-05-01T12:00:00Z");

    static AirtimeGovernor governor(MeshRegion region, double duty, Duration spacing, int perHour) {
        return new AirtimeGovernor(new AirtimeGovernor.Limits(region, duty, spacing, perHour));
    }

    @Test
    @DisplayName("the window adds admitted airtime and forgets it after an hour")
    void window() {
        AirtimeGovernor g = governor(MeshRegion.EU_868, 1.0, Duration.ZERO, 100);
        assertThat(g.tryAcquire(10, false, T0)).isInstanceOf(AirtimeGovernor.Admit.class);
        assertThat(g.tryAcquire(20, false, T0.plusSeconds(60))).isInstanceOf(AirtimeGovernor.Admit.class);
        AirtimeGovernor.Snapshot s = g.snapshot(T0.plusSeconds(120));
        assertThat(s.usedSeconds()).isEqualTo(30);
        assertThat(s.limitSeconds()).isEqualTo(36);
        assertThat(s.transmissions()).isEqualTo(2);
        AirtimeGovernor.Decision d = g.tryAcquire(10, false, T0.plusSeconds(120));
        assertThat(d).isInstanceOf(AirtimeGovernor.Defer.class);
        assertThat(((AirtimeGovernor.Defer) d).until()).as("when the first 10 s leave the window")
                .isEqualTo(T0.plus(Duration.ofHours(1)));
        assertThat(((AirtimeGovernor.Defer) d).why()).isEqualTo("dutyCyclePercent");
        assertThat(g.tryAcquire(10, false, T0.plus(Duration.ofHours(1)))).isInstanceOf(AirtimeGovernor.Admit.class);
        assertThat(g.snapshot(T0.plus(Duration.ofHours(2))).usedSeconds()).isZero();
    }

    @Test
    @DisplayName("ceilings: 10 % in EU_868, UK_868 and EU_433; none (the 10 % maximum) in US, ANZ and OTHER")
    void ceilings() {
        assertThat(MeshRegion.EU_868.ceilingPercent()).isEqualTo(10.0);
        assertThat(MeshRegion.UK_868.ceilingPercent()).isEqualTo(10.0);
        assertThat(MeshRegion.EU_433.ceilingPercent()).isEqualTo(10.0);
        assertThat(MeshRegion.US.ceilingPercent()).isNull();
        assertThat(MeshRegion.ANZ.ceilingPercent()).isNull();
        assertThat(MeshRegion.OTHER.effectiveCeilingPercent()).isEqualTo(MeshRegion.MAX_DUTY_CYCLE_PERCENT);
        assertThat(AirtimeGovernor.Limits.defaults(MeshRegion.EU_868).ceilingSeconds()).isEqualTo(360);
        assertThat(AirtimeGovernor.Limits.defaults(MeshRegion.US).limitSeconds()).isEqualTo(36);
        assertThat(MeshRegion.parse("eu_868")).isEqualTo(MeshRegion.EU_868);
        assertThat(MeshRegion.parse("MARS")).isNull();
    }

    @Test
    @DisplayName("dutyCyclePercent above the ceiling, above 10 or not positive is refused")
    void shareAboveCeiling() {
        assertThatThrownBy(() -> governor(MeshRegion.EU_868, 10.5, Duration.ZERO, 20))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> governor(MeshRegion.US, 12, Duration.ZERO, 20))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> governor(MeshRegion.US, 0, Duration.ZERO, 20))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> governor(MeshRegion.US, 1, Duration.ZERO, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(governor(MeshRegion.EU_868, 10, Duration.ZERO, 20).limits().limitSeconds()).isEqualTo(360);
    }

    @Test
    @DisplayName("minSpacing and maxMessagesPerHour defer to the earliest time both hold")
    void spacingAndCount() {
        AirtimeGovernor g = governor(MeshRegion.EU_868, 10, Duration.ofSeconds(30), 2);
        assertThat(g.tryAcquire(1, false, T0)).isInstanceOf(AirtimeGovernor.Admit.class);
        AirtimeGovernor.Defer spacing = (AirtimeGovernor.Defer) g.tryAcquire(1, false, T0.plusSeconds(10));
        assertThat(spacing.until()).isEqualTo(T0.plusSeconds(30));
        assertThat(spacing.why()).isEqualTo("minSpacing");
        assertThat(g.tryAcquire(1, false, T0.plusSeconds(30))).isInstanceOf(AirtimeGovernor.Admit.class);
        AirtimeGovernor.Defer count = (AirtimeGovernor.Defer) g.tryAcquire(1, false, T0.plusSeconds(90));
        assertThat(count.until()).isEqualTo(T0.plus(Duration.ofHours(1)));
        assertThat(count.why()).isEqualTo("maxMessagesPerHour");
        assertThat(g.snapshot(T0.plusSeconds(90)).deferredLastHour()).isEqualTo(2);
    }

    @Test
    @DisplayName("critical jumps the operator's limits but never the regulatory ceiling")
    void critical() {
        AirtimeGovernor g = governor(MeshRegion.EU_868, 1.0, Duration.ofSeconds(30), 1);
        assertThat(g.tryAcquire(30, false, T0)).isInstanceOf(AirtimeGovernor.Admit.class);
        assertThat(g.tryAcquire(2, false, T0.plusSeconds(1))).isInstanceOf(AirtimeGovernor.Defer.class);
        assertThat(g.tryAcquire(2, true, T0.plusSeconds(1))).as("spacing, count and share ignored")
                .isInstanceOf(AirtimeGovernor.Admit.class);
        assertThat(g.tryAcquire(300, true, T0.plusSeconds(2))).isInstanceOf(AirtimeGovernor.Admit.class);
        AirtimeGovernor.Decision over = g.tryAcquire(30, true, T0.plusSeconds(3));
        assertThat(over).as("332 + 30 > 360 s").isInstanceOf(AirtimeGovernor.Defer.class);
        assertThat(((AirtimeGovernor.Defer) over).why()).isEqualTo("the regulatory duty-cycle ceiling");
        assertThat(((AirtimeGovernor.Defer) over).until()).isEqualTo(T0.plus(Duration.ofHours(1)));
        assertThat(g.tryAcquire(400, true, T0.plusSeconds(4))).isInstanceOf(AirtimeGovernor.Drop.class);
    }

    @Test
    @DisplayName("deferral then fold: folded alerts and drops are counted in the window")
    void deferralThenFold() {
        AirtimeGovernor g = governor(MeshRegion.US, 1.0, Duration.ofSeconds(30), 20);
        assertThat(g.tryAcquire(1, false, T0)).isInstanceOf(AirtimeGovernor.Admit.class);
        AirtimeGovernor.Defer a = (AirtimeGovernor.Defer) g.tryAcquire(1, false, T0.plusSeconds(5));
        AirtimeGovernor.Defer b = (AirtimeGovernor.Defer) g.tryAcquire(1, false, T0.plusSeconds(6));
        assertThat(a.until()).isEqualTo(b.until()).as("released together").isEqualTo(T0.plusSeconds(30));
        assertThat(g.tryAcquire(1, false, a.until())).isInstanceOf(AirtimeGovernor.Admit.class);
        g.noteFolded(1, a.until());
        assertThat(g.tryAcquire(37, false, a.until().plusSeconds(60))).isInstanceOf(AirtimeGovernor.Drop.class);
        g.noteDropped(a.until().plusSeconds(61));
        AirtimeGovernor.Snapshot s = g.snapshot(a.until().plusSeconds(62));
        assertThat(s.foldedLastHour()).isEqualTo(1);
        assertThat(s.droppedLastHour()).isEqualTo(2);
        assertThat(s.deferredLastHour()).isEqualTo(2);
    }

    @Test
    @DisplayName("new limits keep the window")
    void withLimits() {
        AirtimeGovernor g = governor(MeshRegion.EU_868, 1.0, Duration.ZERO, 20);
        g.tryAcquire(30, false, T0);
        AirtimeGovernor h = g.withLimits(new AirtimeGovernor.Limits(MeshRegion.EU_868, 2.0, Duration.ZERO, 20));
        assertThat(h.snapshot(T0.plusSeconds(1)).usedSeconds()).isEqualTo(30);
        assertThat(h.tryAcquire(40, false, T0.plusSeconds(1))).isInstanceOf(AirtimeGovernor.Admit.class);
        assertThat(h.tryAcquire(10, false, T0.plusSeconds(2))).isInstanceOf(AirtimeGovernor.Defer.class);
    }
}
