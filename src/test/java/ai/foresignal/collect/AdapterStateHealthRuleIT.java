package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.adapters.spc.SpcOutlooksAdapter;
import ai.foresignal.test.IntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A reason recorded with {@code AdapterStateStore.degrade} degrades the source (prompt 047); one recorded
 * with {@code fail} makes it failing (prompt 049).
 */
@IntegrationTest
class AdapterStateHealthRuleIT {

    @Autowired AdapterStateHealthRule rule;
    @Autowired UpstreamClient upstream;

    private HealthRuleContext context() {
        return new HealthRuleContext(new SpcOutlooksAdapter("").descriptor(),
                HealthSnapshot.empty(SpcOutlooksAdapter.ID), Instant.now());
    }

    @Test
    @DisplayName("no reason: the rule does not apply; a reason: degraded with it; cleared: gone again")
    void reason() {
        assertThat(rule.evaluate(context())).isEmpty();

        upstream.stateStore(SpcOutlooksAdapter.ID).degrade("SPC layer day3otlk_prob.lyr.geojson missing");
        var verdict = rule.evaluate(context()).orElseThrow();
        assertThat(verdict.status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(verdict.reason()).isEqualTo("SPC layer day3otlk_prob.lyr.geojson missing");

        upstream.stateStore(SpcOutlooksAdapter.ID).clearDegraded();
        assertThat(rule.evaluate(context())).isEmpty();
    }

    @Test
    @DisplayName("a failing reason (prompt 049) wins over a degraded one, and clears like it")
    void failing() {
        var state = upstream.stateStore(SpcOutlooksAdapter.ID);
        state.degrade("SatDump /api shape changed (version drift?)");
        state.fail("SatDump /api unreachable for 3 consecutive polls: refused");
        var verdict = rule.evaluate(context()).orElseThrow();
        assertThat(verdict.status()).isEqualTo(HealthStatus.FAILING);
        assertThat(verdict.reason()).isEqualTo("SatDump /api unreachable for 3 consecutive polls: refused");

        state.clearFailing();
        assertThat(rule.evaluate(context()).orElseThrow().status()).isEqualTo(HealthStatus.DEGRADED);
        state.clearDegraded();
        assertThat(rule.evaluate(context())).isEmpty();
    }
}
