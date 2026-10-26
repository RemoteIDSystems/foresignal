package ai.foresignal.source.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.FieldError;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The stand-in for a domain without a backend (prompt R2-019). Every domain has one since R2-023, so the stand-in is
 * reached only when a domain's bean is missing; it stores a disabled configuration but cannot be enabled.
 */
class PendingDomainAggregatorTest {

    private final PendingDomainAggregator pending = new PendingDomainAggregator("radar", "Local radar");

    @Test
    @DisplayName("it is not implemented, drives and supersedes nothing, and refuses to be enabled")
    void refusesToBeEnabled() {
        assertThat(pending.implemented()).isFalse();
        assertThat(pending.sourceIds()).isEmpty();
        assertThat(pending.supersededSourceIds()).isEmpty();

        List<FieldError> errors = new ArrayList<>();
        pending.validate(new AggregatorSettings(false, "receiver", "tcp://sensors:10110", Map.of("note", "later"),
                true, List.of()), errors);
        assertThat(errors).isEmpty();
        pending.validate(new AggregatorSettings(true, null, null, Map.of(), true, List.of()), errors);
        assertThat(errors).singleElement().satisfies(e -> assertThat(e.field()).isEqualTo("enabled"));
    }

    @Test
    @DisplayName("its test says there is no backend")
    void testSaysSo() {
        assertThat(pending.test(AggregatorSettings.defaults())).containsEntry("ok", false)
                .containsEntry("reachable", false).containsEntry("message", "This server has no Local radar backend yet.");
    }
}
