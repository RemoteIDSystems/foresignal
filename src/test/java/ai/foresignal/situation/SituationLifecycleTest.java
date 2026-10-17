package ai.foresignal.situation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Geo;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

/**
 * The situation lifecycle (prompt 118 §4): emerging → active → dormant → active (reopen) → closed, closed final;
 * plus the deterministic pieces it rests on (summary template, geometry, severity mapping).
 */
class SituationLifecycleTest {

    static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");
    static final Duration DORMANCY = Duration.ofDays(7);

    @Test
    @DisplayName("emerging → active → dormant → active (reopen) → closed, and closed is final")
    void fullCycle() {
        String status = SituationLifecycle.EMERGING;
        // One family and one group: still emerging.
        status = SituationLifecycle.afterMembers(status, 1, 1, true);
        assertThat(status).isEqualTo(SituationLifecycle.EMERGING);
        // Two families but one independent group: still emerging.
        status = SituationLifecycle.afterMembers(status, 2, 1, true);
        assertThat(status).isEqualTo(SituationLifecycle.EMERGING);
        status = SituationLifecycle.afterMembers(status, 2, 2, true);
        assertThat(status).isEqualTo(SituationLifecycle.ACTIVE);
        // Quiet for less than the window: unchanged; at the window: dormant.
        assertThat(SituationLifecycle.afterTime(status, T0, T0.plus(Duration.ofDays(6)), DORMANCY))
                .isEqualTo(SituationLifecycle.ACTIVE);
        status = SituationLifecycle.afterTime(status, T0, T0.plus(DORMANCY), DORMANCY);
        assertThat(status).isEqualTo(SituationLifecycle.DORMANT);
        // A non-core change does not reopen; a new core member does.
        assertThat(SituationLifecycle.afterMembers(status, 2, 2, false)).isEqualTo(SituationLifecycle.DORMANT);
        status = SituationLifecycle.afterMembers(status, 2, 2, true);
        assertThat(status).isEqualTo(SituationLifecycle.ACTIVE);
        status = SituationLifecycle.afterTime(status, T0, T0.plus(DORMANCY), DORMANCY);
        assertThat(SituationLifecycle.afterTime(status, T0, T0.plus(Duration.ofDays(89)), DORMANCY))
                .isEqualTo(SituationLifecycle.DORMANT);
        status = SituationLifecycle.afterTime(status, T0, T0.plus(Duration.ofDays(90)), DORMANCY);
        assertThat(status).isEqualTo(SituationLifecycle.CLOSED);
        // Closed is final.
        assertThat(SituationLifecycle.afterMembers(status, 5, 5, true)).isEqualTo(SituationLifecycle.CLOSED);
        assertThat(SituationLifecycle.afterTime(status, T0, T0.plus(Duration.ofDays(900)), DORMANCY))
                .isEqualTo(SituationLifecycle.CLOSED);
        assertThat(SituationLifecycle.operatorMayMove(status, SituationLifecycle.ACTIVE)).isFalse();
    }

    @Test
    @DisplayName("an emerging situation that never activates also goes dormant; operators activate and close")
    void emergingDormantAndOperator() {
        assertThat(SituationLifecycle.afterTime(SituationLifecycle.EMERGING, T0, T0.plus(DORMANCY), DORMANCY))
                .isEqualTo(SituationLifecycle.DORMANT);
        assertThat(SituationLifecycle.operatorMayMove(SituationLifecycle.EMERGING, SituationLifecycle.ACTIVE)).isTrue();
        assertThat(SituationLifecycle.operatorMayMove(SituationLifecycle.DORMANT, SituationLifecycle.ACTIVE)).isTrue();
        assertThat(SituationLifecycle.operatorMayMove(SituationLifecycle.ACTIVE, SituationLifecycle.CLOSED)).isTrue();
        assertThat(SituationLifecycle.operatorMayMove(SituationLifecycle.ACTIVE, SituationLifecycle.DORMANT)).isFalse();
        assertThat(SituationLifecycle.afterTime(SituationLifecycle.ACTIVE, null, T0, DORMANCY))
                .isEqualTo(SituationLifecycle.ACTIVE);
        assertThat(SituationLifecycle.basis(SituationLifecycle.EMERGING, 1, 1, false))
                .contains("1 signal family").contains("1 independent evidence group");
    }

    @Test
    @DisplayName("the summary template renders counts, regions, titles, corroboration and the last activity")
    void summary() {
        SummaryTemplate.Facts facts = new SummaryTemplate.Facts(4, 2, List.of("YE", "YE-AD"), T0,
                List.of("Tanker struck", "GNSS jamming", "Nav warning", "Fourth"), 1, 1, T0);
        String text = SummaryTemplate.render(facts, T0.plus(Duration.ofHours(5)));
        assertThat(text).isEqualTo("4 signals from 2 families across YE, YE-AD since 2026-03-01 12:00 UTC: Tanker struck;"
                + " GNSS jamming; Nav warning. 1 of 2 members corroborated by independent evidence groups (50%)."
                + " Last activity 5 h ago.");
        assertThat(SummaryTemplate.stem(facts)).doesNotContain("Last activity");
        assertThat(SummaryTemplate.corroboration(0, 3)).isEqualTo("Built only on uncorroborated reports (3)");
        assertThat(SummaryTemplate.corroboration(0, 0)).startsWith("No member");
        assertThat(SummaryTemplate.regions(List.of())).isEqualTo("an unlocated area");
        assertThat(SummaryTemplate.regions(List.of("A1", "A2", "A3", "A4", "A5", "A6", "A7"))).endsWith("+2 more");
        assertThat(SummaryTemplate.ago(T0, T0.plusSeconds(10))).isEqualTo("just now");
        assertThat(SummaryTemplate.ago(T0, T0.plus(Duration.ofMinutes(20)))).isEqualTo("20 min ago");
        assertThat(SummaryTemplate.ago(T0, T0.plus(Duration.ofDays(3)))).isEqualTo("3 d ago");
    }

    @Test
    @DisplayName("the geometry is the 25 km-buffered hull, at most 500 vertices, with its area; null without members")
    void geometry() {
        assertThat(SituationGeometry.of(List.of())).isNull();
        Geometry one = SituationGeometry.of(List.of(Geo.point(45.0, 12.0)));
        assertThat(one.getNumPoints()).isLessThanOrEqualTo(SituationGeometry.MAX_VERTICES);
        // A 25 km disc: about π·25² ≈ 1963 km².
        assertThat(SituationGeometry.areaKm2(one)).isBetween(1900.0, 2000.0);
        assertThat(SituationGeometry.distanceKm(Geo.point(45.0, 12.0), one)).isZero();
        Geometry two = SituationGeometry.of(List.of(Geo.point(45.0, 12.0), Geo.point(46.0, 12.5)));
        assertThat(SituationGeometry.areaKm2(two)).isGreaterThan(SituationGeometry.areaKm2(one));
        assertThat(two.getSRID()).isEqualTo(Geo.SRID);
        assertThat(SituationGeometry.bbox(two)).hasSize(4);
        assertThat(SituationGeometry.of(List.of(Geo.point(45.0, 12.0), Geo.point(46.0, 12.5))).equalsExact(two))
                .isTrue();
    }

    @Test
    @DisplayName("severities map onto info, warning and critical, and the maximum wins")
    void severities() {
        assertThat(Severities.normalize("significant")).isEqualTo("warning");
        assertThat(Severities.normalize("notable")).isEqualTo("info");
        assertThat(Severities.normalize("SEVERE")).isEqualTo("critical");
        assertThat(Severities.normalize(null)).isEqualTo("info");
        assertThat(Severities.max("warning", "critical")).isEqualTo("critical");
        assertThat(Severities.max(null, "info")).isEqualTo("info");
        assertThat(Severities.max(null, null)).isNull();
    }
}
