package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.CaptureRecapture;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.CoverageSupport;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.trigger.TriggerRuleService;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The 100 absence rule under the 116 detectability rule (OBS-1): over an area whose detection probability is
 * 0.3 a quiet window is {@code unknown} with reason {@code LOW_DETECTABILITY}, never "absent", and fires no alert;
 * with a detection probability of 0.9 the same quiet window is active as before.
 */
@WebIntegrationTest
@Import({TestIngestSources.class, CoverageSupport.Config.class})
class AbsenceRetrofitIT {

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");
    private static final String SOURCE = TestIngestSources.STANDARD;
    private static final Duration WINDOW = Duration.ofHours(2);

    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
    @Autowired SourceService sources;
    @Autowired CoverageSupport coverage;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    @BeforeEach
    void freeze() {
        clock.setInstant(NOW);
        sources.update(SOURCE, new SourceSettingsUpdate(true, null, Map.of(), Budget.of(1, 1), null, false, null, false));
    }

    private UUID absenceRule() {
        clock.setInstant(NOW.minus(Duration.ofHours(3)));
        UUID id = rules.create(json.readTree("{\"name\":\"quiet\",\"type\":\"absence\",\"sources\":[\"" + SOURCE
                + "\"],\"kinds\":[\"" + TestIngestSources.STANDARD_KIND + "\"],\"window\":{\"window\":\"2h\"}}"))
                .rule().getId();
        clock.setInstant(NOW);
        return id;
    }

    private void runningAndCovered() {
        coverage.fetch(SOURCE, "OK", NOW.minus(Duration.ofHours(10)));
        coverage.markCovered(SOURCE, NOW.minus(WINDOW), NOW);
        for (Instant at = NOW.minus(WINDOW).plusSeconds(600); at.isBefore(NOW); at = at.plusSeconds(600)) {
            coverage.fetch(SOURCE, "OK", at);
        }
    }

    /** A capture–recapture estimate for the source over everywhere, valid at {@link #NOW}. */
    private void estimate(double p) {
        jdbc.update("INSERT INTO observability_estimate (id, source_id, family, region_code, cell, band, period_start,"
                        + " period_end, p_detect, lo, hi, n, method, confidence, computed_at, manifest_id)"
                        + " VALUES (?, ?, ?, NULL, NULL, NULL, ?, ?, ?, ?, ?, 50, ?, 'moderate', ?, NULL)",
                ai.foresignal.security.Ulids.next(NOW), SOURCE,
                TestIngestSources.STANDARD_KIND, Timestamp.from(NOW.minus(Duration.ofDays(365))),
                Timestamp.from(NOW.minus(Duration.ofDays(1))), p, Math.max(0, p - 0.1), Math.min(1, p + 0.1),
                CaptureRecapture.METHOD, Timestamp.from(NOW.minus(Duration.ofDays(1))));
    }

    private int alerts(UUID rule) {
        Integer n = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, rule);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("p_detect 0.3: a quiet window is unknown / LOW_DETECTABILITY, never absent, and fires nothing")
    void lowDetectabilityIsUnknown() {
        runningAndCovered();
        estimate(0.3);
        UUID id = absenceRule();
        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(NOW, capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("unknown");
        assertThat(capture.lastState(id + ":*", "reason")).isEqualTo(AbsenceCheck.LOW_DETECTABILITY);
        clock.advance(Duration.ofMinutes(6));
        engine.evaluate(clock.instant(), capture);
        assertThat(capture.lastState(id + ":*", "state")).isNotEqualTo("active");
        assertThat(alerts(id)).isZero();
    }

    @Test
    @DisplayName("p_detect 0.9: the same quiet window is active, as before 116")
    void highDetectabilityIsActive() {
        runningAndCovered();
        estimate(0.9);
        UUID id = absenceRule();
        WindowEmitters.Capture capture = new WindowEmitters.Capture();
        engine.evaluate(NOW, capture);
        assertThat(capture.lastState(id + ":*", "state")).isEqualTo("active");
    }

    @Test
    @DisplayName("the downward-anomaly retrofit turns an activity-down result into insufficient-coverage")
    void lowDetectabilityAnomaly() {
        AnomalyDetector.Result down = new AnomalyDetector.Result(1, 20, 1, 1e-6, -4, "activity-down", "down", true);
        AnomalyDetector.Result retro = WindowEngine.lowDetectability(down);
        assertThat(retro.classification()).isEqualTo("insufficient-coverage");
        assertThat(retro.fire()).isFalse();
        assertThat(retro.direction()).isNull();
        assertThat(retro.expected()).isEqualTo(20);
    }
}
