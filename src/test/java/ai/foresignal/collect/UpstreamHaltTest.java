package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamHaltException;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link HealthEvaluator}'s halt rule and {@link UpstreamHaltException} itself (prompt 064). */
class UpstreamHaltTest {

    private static final Instant NOW = Instant.parse("2026-10-13T12:00:00Z");
    private static final SourceDescriptor D = FakePollingAdapter.defaultDescriptor("halt").build();
    private static final String REASON =
            "CelesTrak returned 404; automatic retries stopped per CelesTrak usage policy — investigate, then use fetch-now";

    private static HealthSnapshot failed(String lastError, Integer status, Instant backoffUntil) {
        return new HealthSnapshot("halt", NOW.minusSeconds(60), NOW.minusSeconds(3600), NOW.minusSeconds(60), lastError,
                status, 1, 0, 0, List.of(5, 5, 5), backoffUntil, null, List.of(), NOW.minusSeconds(3600));
    }

    @Test
    @DisplayName("a halt is failing with the halt reason, without the exception class prefix, from the first failure")
    void failingWithReason() {
        HealthAssessment a = new HealthEvaluator().evaluate(new HealthInputs(D, true,
                failed("UpstreamHaltException: " + REASON, 404, NOW.plus(Duration.ofHours(24))), null,
                UsageTotalsWindow.ZERO, null, null, NOW));
        assertThat(a.status()).isEqualTo(HealthStatus.FAILING);
        assertThat(a.statusReason()).isEqualTo(REASON);
    }

    @Test
    @DisplayName("a halt caused by the body (status 0) is recognized by its marker too; recovery clears it")
    void bodyHalt() {
        String reason = "CelesTrak returned 200 with a body that is not CSV; automatic retries stopped per CelesTrak"
                + " usage policy — investigate, then use fetch-now";
        HealthAssessment a = new HealthEvaluator().evaluate(new HealthInputs(D, true,
                failed("UpstreamHaltException: " + reason, null, null), null, UsageTotalsWindow.ZERO, null, null, NOW));
        assertThat(a.status()).isEqualTo(HealthStatus.FAILING);
        HealthSnapshot recovered = new HealthSnapshot("halt", NOW, NOW, NOW.minusSeconds(60), "UpstreamHaltException: "
                + reason, null, 0, 5, 0, List.of(5, 5, 5), null, null, List.of(), NOW);
        assertThat(new HealthEvaluator().evaluate(new HealthInputs(D, true, recovered, null, UsageTotalsWindow.ZERO, null,
                null, NOW)).status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    @DisplayName("the exception carries status, a 24 h default hold and always the marker")
    void exception() {
        UpstreamHaltException e = new UpstreamHaltException(301, "moved");
        assertThat(e.status()).isEqualTo(301);
        assertThat(e.httpStatus()).hasValue(301);
        assertThat(e.hold()).isEqualTo(Duration.ofHours(24));
        assertThat(e.getMessage()).contains(UpstreamHaltException.MARKER);
        assertThat(new UpstreamHaltException(0, REASON, Duration.ofHours(2)).hold()).isEqualTo(Duration.ofHours(2));
        assertThat(new UpstreamHaltException(0, REASON, Duration.ZERO).httpStatus()).isEmpty();
        assertThat(HealthEvaluator.haltReason("plain")).isEqualTo("plain");
    }
}
